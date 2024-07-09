package com.heroku.java.controller;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heroku.java.model.Product;
import com.heroku.java.model.PurchaseForm;
import com.heroku.java.model.ProductPurchase;

@Controller
public class PurchaseController {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseController.class);
    private final DataSource dataSource;

    @Autowired
    public PurchaseController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/purchaseProductList")
    public String listProducts(Model model) {
        List<Product> products = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String getProductsSql = "SELECT * FROM public.product";
            try (PreparedStatement statement = connection.prepareStatement(getProductsSql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Product product = new Product();
                        product.setProductId(resultSet.getLong("productid"));
                        product.setProductName(resultSet.getString("productname"));
                        product.setProductType(resultSet.getString("producttype"));
                        product.setProductQuantity(resultSet.getInt("productquantity"));
                        product.setProductPrice(resultSet.getDouble("productprice"));
                        product.setProductImage(resultSet.getString("productimage"));
                        products.add(product);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve products", e);
            throw new RuntimeException("Failed to retrieve products", e);
        }

        model.addAttribute("products", products);
        model.addAttribute("purchaseForm", new PurchaseForm());
        return "Purchase/CustomerPurchase";
    }

    @PostMapping("/createPurchase")
    public String createPurchase(@ModelAttribute PurchaseForm purchaseForm, Model model) {
        logger.info("Starting purchase creation process.");
        try (Connection connection = dataSource.getConnection()) {
            double totalPurchaseAmount = 0.0;

            for (ProductPurchase productPurchase : purchaseForm.getProducts()) {
                Long productId = productPurchase.getProductId();
                int productQuantity = productPurchase.getQuantity();
                double purchaseTotal = calculateTotalPrice(productId, productQuantity);
                totalPurchaseAmount += purchaseTotal;
            }

            // Insert into purchase table
            String insertPurchaseSql = "INSERT INTO public.purchase (staffid, customerid, purchasetotal, purchasedate, purchasestatus) VALUES (?, ?, ?, ?, ?) RETURNING purchaseid";
            long purchaseId;
            try (PreparedStatement statement = connection.prepareStatement(insertPurchaseSql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, purchaseForm.getStaffId());
                statement.setLong(2, purchaseForm.getCustomerId());
                statement.setDouble(3, totalPurchaseAmount);
                statement.setDate(4, Date.valueOf(LocalDate.now()));
                statement.setString(5, "Pending");
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        purchaseId = generatedKeys.getLong(1);
                        logger.info("Purchase created with ID: " + purchaseId);
                    } else {
                        throw new SQLException("Creating purchase failed, no ID obtained.");
                    }
                }
            }

            // Insert into purchaseproduct table
            String insertPurchaseProductSql = "INSERT INTO public.purchaseproduct (purchaseid, productid, productquantity) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertPurchaseProductSql)) {
                for (ProductPurchase productPurchase : purchaseForm.getProducts()) {
                    statement.setLong(1, purchaseId);
                    statement.setLong(2, productPurchase.getProductId());
                    statement.setInt(3, productPurchase.getQuantity());
                    statement.addBatch();
                }
                statement.executeBatch();
                logger.info("PurchaseProduct records inserted successfully.");
            }

            model.addAttribute("totalPurchaseAmount", totalPurchaseAmount);
            model.addAttribute("purchaseDetails", purchaseForm.getProducts());
        } catch (SQLException e) {
            logger.error("An error occurred while processing the purchase", e);
            model.addAttribute("errorMessage", "An error occurred while processing your purchase: " + e.getMessage());
            return "error";
        }
        return "Purchase/CreatePurchase";
    }

    public double calculateTotalPrice(Long productId, int productQuantity) {
        double productPrice = retrieveProductPrice(productId);
        double purchaseTotal = productPrice * productQuantity;
        return purchaseTotal;
    }

    public double retrieveProductPrice(Long productId) {
        double productPrice = 0.0;
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT productprice FROM public.product WHERE productid = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setLong(1, productId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        productPrice = resultSet.getDouble("productprice");
                    } else {
                        throw new SQLException("Product not found for ID: " + productId);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve product price", e);
            throw new RuntimeException("Failed to retrieve product price", e);
        }
        return productPrice;
    }
}
