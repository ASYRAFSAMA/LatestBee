package com.heroku.java.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.heroku.java.model.Purchase;
import com.heroku.java.model.Customer;
import com.heroku.java.model.Product;

@Controller
public class PurchaseController {
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
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve products", e);
        }

        model.addAttribute("products", products);
        return "Purchase/CustomerPurchase";
    }

    @PostMapping("/createPurchase")
    public String createPurchase(@RequestParam("productId") Long productId,
                                 @RequestParam("productQuantity") int productQuantity,
                                 Model model) {
        try {
            // Validate input parameters
            if (productId == null || productQuantity <= 0) {
                throw new IllegalArgumentException("Invalid product ID or quantity");
            }

            double purchaseTotal = calculateTotalPrice(productId, productQuantity);
            model.addAttribute("productId", productId);
            model.addAttribute("productQuantity", productQuantity);
            model.addAttribute("purchaseTotal", purchaseTotal);
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve product price", e);
        }
        return productPrice;
    }
}
