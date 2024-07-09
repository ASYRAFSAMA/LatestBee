package com.heroku.java.controller;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.springframework.web.bind.annotation.RequestParam;

import com.heroku.java.model.Purchase;
import com.heroku.java.model.PurchaseProduct;
import com.heroku.java.model.Product;

import jakarta.servlet.http.HttpSession;

@Controller
public class PurchaseController {
    private final DataSource dataSource;

    @Autowired
    public PurchaseController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/purchaseProductList")
    public String listProducts(Model model, HttpSession session) {
        List<Product> products = new ArrayList<>();
        Long customerId = (Long) session.getAttribute("customerid");

        if (customerId == null) {
            return "redirect:/custLogin";
        }

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
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve products", e);
        }

        Purchase purchase = new Purchase();
        purchase.setCustomerId(customerId);
        List<PurchaseProduct> purchaseProducts = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            purchaseProducts.add(new PurchaseProduct());
        }
        purchase.setPurchaseProducts(purchaseProducts);

        model.addAttribute("products", products);
        model.addAttribute("purchase", purchase);
        return "Purchase/CustomerPurchase";
    }

    @PostMapping("/createPurchase")
public String createPurchase(@ModelAttribute Purchase purchase, HttpSession session, Model model) {
    Long customerId = (Long) session.getAttribute("customerid");

    if (customerId == null) {
        return "redirect:/custLogin";
    }

    // Set the customerId to the purchase object
    purchase.setCustomerId(customerId);

    try (Connection connection = dataSource.getConnection()) {
        // Start a transaction
        connection.setAutoCommit(false);

        try {
            // Insert the purchase record
            String insertPurchaseSql = "INSERT INTO public.purchase (customerid) VALUES (?) RETURNING purchaseid";
            Long purchaseId;
            try (PreparedStatement purchaseStatement = connection.prepareStatement(insertPurchaseSql)) {
                purchaseStatement.setLong(1, purchase.getCustomerId());
                try (ResultSet rs = purchaseStatement.executeQuery()) {
                    if (rs.next()) {
                        purchaseId = rs.getLong("purchaseid");
                    } else {
                        throw new SQLException("Failed to retrieve purchase ID.");
                    }
                }
            }

            // Insert the purchase product records
            String insertPurchaseProductSql = "INSERT INTO public.purchase_product (purchaseid, productid, purchasequantity) VALUES (?, ?, ?)";
            try (PreparedStatement purchaseProductStatement = connection.prepareStatement(insertPurchaseProductSql)) {
                for (PurchaseProduct purchaseProduct : purchase.getPurchaseProducts()) {
                    purchaseProductStatement.setLong(1, purchaseId);
                    purchaseProductStatement.setLong(2, purchaseProduct.getProductId());
                    purchaseProductStatement.setInt(3, purchaseProduct.getProductQuantity());
                    purchaseProductStatement.addBatch();
                }
                purchaseProductStatement.executeBatch();
            }

            // Commit the transaction
            connection.commit();
        } catch (SQLException e) {
            // Rollback the transaction in case of error
            connection.rollback();
            e.printStackTrace();
            throw new RuntimeException("Failed to create purchase", e);
        } finally {
            // Restore auto-commit mode
            connection.setAutoCommit(true);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        throw new RuntimeException("Database connection error", e);
    }

    return "redirect:/purchaseConfirmation"; // Redirect to a confirmation page or purchase summary
}

}