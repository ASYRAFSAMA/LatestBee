
package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.heroku.java.model.Product;

import javax.sql.DataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


@Controller
public class ProductController {
    private final DataSource dataSource;

    @Autowired
    public ProductController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostMapping("/createProduct")
    public String addproduct(@ModelAttribute("createProduct")
                                @RequestParam("productname") String productName,
                                @RequestParam("producttype") String productType,
                                @RequestParam("productquantity") Integer productQuantity,
                                @RequestParam("productprice") Double productPrice,
                                @RequestParam("productimage") String productImage) throws IOException {
/* 
         // Check if required parameters are present
    if (customerName.isEmpty() || customerDob.isEmpty() || customerEmail.isEmpty() ||
        customerPhoneNum.isEmpty() || customerAddress.isEmpty() || Password == null) {
        // Handle missing parameters gracefully
        // For example, return an error page or redirect to registration page with error message
        return "redirect:/register?error=missing_params";
    }
*/
        Product product = new Product();
        product.setProductName(productName);
        product.setProductType(productType);
        product.setProductQuantity(productQuantity);
        product.setProductPrice(productPrice);
        product.setProductImage(productImage);
    
       


    try (Connection connection = dataSource.getConnection()) {

            System.out.println("Received product details:");
            System.out.println("Product Name: " + product.getProductName());
            System.out.println("Product Type: " + product.getProductType());
            System.out.println("Product Quantity: " + product.getProductQuantity());
            System.out.println("Product Price: " + product.getProductPrice());
            System.out.println("Product Image: " + product.getProductImage());


            String createProductSql = "INSERT INTO public.product(productname, producttype, productquantity, productprice, productimage) VALUES (?, ?, ?, ?, ?);";

            try (PreparedStatement statement = connection.prepareStatement(createProductSql)) {
                statement.setString(1, product.getProductName());
                statement.setString(1, product.getProductType());
                statement.setInt(3, product.getProductQuantity());
                statement.setDouble(4, product.getProductPrice());
                statement.setString(5, product.getProductImage());


                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        long productId = resultSet.getLong("productid");
                        product.setProductId(productId);
                        System.out.println("Inserted customer with ID: " + productId);
                    } else {
                        throw new SQLException("Failed to insert product, no ID obtained.");
                    }
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
            // Handle exception appropriately, e.g., log error or rethrow as runtime exception
            throw new RuntimeException("Failed to insert product", e);
        }

        return "redirect:/register"; // Return the created activity object
    }
}


