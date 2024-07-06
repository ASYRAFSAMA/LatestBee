package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

import com.heroku.java.model.Product;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Controller
public class ProductController {
    private final DataSource dataSource;

    @Autowired
    public ProductController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostMapping("/createProduct")
    public String addProduct(@ModelAttribute("createProduct")
                             @RequestParam("productname") String productName,
                             @RequestParam("producttype") String productType,
                             @RequestParam("productquantity") Integer productQuantity,
                             @RequestParam("productprice") Double productPrice,
                             @RequestParam("productimage") MultipartFile productImage) throws IOException {
        Product product = new Product();
        product.setProductName(productName);
        product.setProductType(productType);
        product.setProductQuantity(productQuantity);
        product.setProductPrice(productPrice);

        // Encode image as Base64 string
        String base64Image = Base64.getEncoder().encodeToString(productImage.getBytes());
        product.setProductImage(base64Image);

        try (Connection connection = dataSource.getConnection()) {
            String createProductSql = "INSERT INTO public.product (productname, producttype, productquantity, productprice, productimage) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createProductSql)) {
                statement.setString(1, product.getProductName());
                statement.setString(2, product.getProductType());
                statement.setInt(3, product.getProductQuantity());
                statement.setDouble(4, product.getProductPrice());
                statement.setString(5, product.getProductImage());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to insert product", e);
        }

        return "redirect:/productList";
    }

    @GetMapping("/productList")
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
        return "listproduct";
    }

    @GetMapping("/updateProduct")
public String updateTicket(@RequestParam("ticketId") Long ticketId, Model model) {

    try {
        Connection connection = dataSource.getConnection();
        String sql = "SELECT ticketid,tickettype,ticketprice FROM public.ticket WHERE ticketid=?";
        final var statement= connection.prepareStatement(sql);
        statement.setLong(1,ticketId);
        final var resultSet = statement.executeQuery();

        if (resultSet.next()){
            
            String ticketType = resultSet.getString("tickettype");
            double ticketPrice = resultSet.getDouble("ticketprice");

            ticket ticket = new ticket();
            ticket.setTicketId(ticketId);
            ticket.setTicketPrice(ticketPrice);
            ticket.setTicketType(ticketType);
            model.addAttribute("ticket",ticket);

            connection.close();
        }
    }
    catch(Exception e){
        e.printStackTrace();
    }
    return "Ticket/updateTicket";
}


}





/**
package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;


import com.heroku.java.model.Product;

import javax.sql.DataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
                                @RequestParam("productimage") MultipartFile productImage) throws IOException {

        Product product = new Product();
        product.setProductName(productName);
        product.setProductType(productType);
        product.setProductQuantity(productQuantity);
        product.setProductPrice(productPrice);
        
    
       // Encode image as Base64 string
       String base64Image = Base64.getEncoder().encodeToString(productImage.getBytes());
       product.setProductImage(base64Image);


    try (Connection connection = dataSource.getConnection()) {

            System.out.println("Received product details:");
            System.out.println("Product Name: " + product.getProductName());
            System.out.println("Product Type: " + product.getProductType());
            System.out.println("Product Quantity: " + product.getProductQuantity());
            System.out.println("Product Price: " + product.getProductPrice());
            System.out.println("Image: " + productImage.getOriginalFilename());


            String createProductSql = "INSERT INTO public.product(productname, producttype, productquantity, productprice, productimage) VALUES (?, ?, ?, ?, ?);";

            try (PreparedStatement statement = connection.prepareStatement(createProductSql)) {
                statement.setString(1, product.getProductName());
                statement.setString(2, product.getProductType());
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

        return "redirect:/productList";
    }

    @GetMapping("/productList")
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
        return "listproduct";
    }
}




*/