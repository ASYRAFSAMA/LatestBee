package com.heroku.java.controller;

import java.sql.Connection;
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
    public String purchaseProductList(HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute("customerid");
        if (customerId == null) {
            return "redirect:/custLogin";
        }

        List<Product> products = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT productname, productprice FROM public.product";
            try (PreparedStatement statement = conn.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String productName = resultSet.getString("productname");
                    double productPrice = resultSet.getDouble("productprice");
                    Product product = new Product();
                    product.setProductName(productName);
                    product.setProductPrice(productPrice);
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve products", e);
        }

        model.addAttribute("products", products);
        return "Purchase/CustomerPurchase";
    }

    @PostMapping("/customerPurchase")
    public String customerPurchase(HttpSession session, @RequestParam("productName") String productName,
                                   @RequestParam("productQuantity") int productQuantity, @RequestParam("purchaseDate") LocalDate purchaseDate,
                                   Model model) {
        Long customerId = (Long) session.getAttribute("customerid");
        if (customerId == null) {
            return "redirect:/custLogin";
        }

        session.setAttribute("productName", productName);
        session.setAttribute("productQuantity", productQuantity);
        session.setAttribute("purchaseDate", purchaseDate);

        double subtotal = calculateSubTotal(productName, productQuantity);
        double totalPrice = calculateTotalPrice(productName, productQuantity);

        model.addAttribute("subtotal", subtotal);
        model.addAttribute("purchaseDate", purchaseDate);
        model.addAttribute("productQuantity", productQuantity);
        model.addAttribute("productName", productName);
        model.addAttribute("totalPrice", totalPrice);

        return "Purchase/CreatePurchase";
    }

    private double retrieveProductPrice(String productName) {
        double productPrice = 0.0;
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT productprice FROM public.product WHERE productname = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, productName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        productPrice = resultSet.getDouble("productprice");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve product price", e);
        }
        return productPrice;
    }

    private double calculateSubTotal(String productName, int productQuantity) {
        double productPrice = retrieveProductPrice(productName);
        return productPrice * productQuantity;
    }

    private double calculateTotalPrice(String productName, int productQuantity) {
        double subtotal = calculateSubTotal(productName, productQuantity);
        double total = subtotal; // Add any other calculations if needed (e.g., tax, discounts)
        return total;
    }

    @PostMapping("/createPurchase")
    public String createPurchase(HttpSession session, @ModelAttribute("createPurchase") Purchase purchase,
                                 @RequestParam("productName") String productName,
                                 @RequestParam("purchaseDate") LocalDate purchaseDate,
                                 @RequestParam("productQuantity") int productQuantity, Model model) {

        Long customerId = (Long) session.getAttribute("customerid");
        if (customerId == null) {
            return "redirect:/custLogin";
        }

        ResultSet resultSet = null;
        PreparedStatement statement = null;
        PreparedStatement statementCreate = null;
        PreparedStatement statementInsert = null;
        PreparedStatement statementUpdate = null;

        try (Connection conn = dataSource.getConnection()) {
            // RETRIEVE PRODUCT ID
            String sqlProductId = "SELECT productid FROM public.product WHERE productname=?";
            statement = conn.prepareStatement(sqlProductId);
            statement.setString(1, productName);
            resultSet = statement.executeQuery();
            int productId = -1;

            if (resultSet.next()) {
                productId = resultSet.getInt("productid");
            }

            // CREATE PURCHASE (INSERT TO DATABASE)
            String sql = "INSERT INTO public.purchase(customerid, purchasedate, purchasetotal, purchasestatus) VALUES (?, ?, ?, ?) RETURNING purchaseid";
            statementCreate = conn.prepareStatement(sql);
            statementCreate.setLong(1, customerId);
            statementCreate.setObject(2, purchaseDate);
            statementCreate.setDouble(3, 0.0); // Initial total price, adjust as needed
            statementCreate.setString(4, "Unpaid");
            resultSet = statementCreate.executeQuery();

            if (resultSet.next()) {
                int purchaseId = resultSet.getInt("purchaseid");

                // INSERT INTO PURCHASEPRODUCT
                String sqlInsert = "INSERT INTO public.purchaseproduct(purchaseid, productid, productquantity) VALUES (?, ?, ?)";
                statementInsert = conn.prepareStatement(sqlInsert);
                statementInsert.setInt(1, purchaseId);
                statementInsert.setInt(2, productId);
                statementInsert.setInt(3, productQuantity);
                statementInsert.executeUpdate();

                // UPDATE TOTAL PRICE
                double totalPrice = calculateTotalPrice(productName, productQuantity);
                String sqlPrice = "UPDATE public.purchase SET purchasetotal=? WHERE purchaseid=?";
                statementUpdate = conn.prepareStatement(sqlPrice);
                statementUpdate.setDouble(1, totalPrice);
                statementUpdate.setInt(2, purchaseId);
                statementUpdate.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "redirect:/error"; // Redirect to error page or handle error as needed
        } finally {
            // Close resources in finally block
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (statementCreate != null) statementCreate.close();
                if (statementInsert != null) statementInsert.close();
                if (statementUpdate != null) statementUpdate.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return "redirect:/purchaseSuccess";
    }
}
