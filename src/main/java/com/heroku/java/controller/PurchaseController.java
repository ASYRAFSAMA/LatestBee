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
    public String purchaseProductList(HttpSession session, Model model){
        session.getAttribute("customerid");
        List<Product> products = new ArrayList<>();

        try {
            Connection conn = dataSource.getConnection();
            String sql = "SELECT productname,productprice from public.product";
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){
                String productName = resultSet.getString("productname");
                double productPrice = resultSet.getDouble("productprice");
                Product product = new Product();
                product.setProductName(productName);
                product.setProductPrice(productPrice);
                product.add(product);
                
            }
            model.addAttribute("product", products);
            
            
        } catch (SQLException e) {
        } return "Purchase/CustomerPurchase";
    }
}
/* 
    @GetMapping("/customerPurchase")
    public String customerPurchase(HttpSession session, Model model) {
        Long customerid = (Long) session.getAttribute("customerid");
        int productQuantity = (int) session.getAttribute("productQuantity");
        LocalDate purchaseDate = (LocalDate) session.getAttribute("purchaseDate");
        String productName = (String) session.getAttribute("productName");


        double subtotal = calculateSubTotal(customerid, productName, productQuantity);
        model.addAttribute("subtotal",subtotal);
        model.addAttribute("bookingDate", bookingDate);
        model.addAttribute("ticketQuantity", ticketQuantity);
        model.addAttribute("ticketType", ticketType);
    
        double totalPrice = calculateTotalPrice(custid, ticketType, ticketQuantity);
        model.addAttribute("totalPrice", totalPrice);
        }catch(SQLException e){

        }

        return "Booking/CreateBooking";
    }
    

    //retrieve price from ticket table
    public double retrieveTicketPrice(String tickettype){
        double ticketprice=0.0;
        try {
            Connection conn = dataSource.getConnection();
            String sql = "Select ticketprice from public.ticket WHERE tickettype=?";
            PreparedStatement statement = conn.prepareStatement(sql);

            statement.setString(1, tickettype);
            ResultSet resultSet =statement.executeQuery();

            if(resultSet.next()){
                 ticketprice = resultSet.getDouble("ticketprice");
            }

            conn.close();

        } catch (SQLException e) {
        }
        return ticketprice;
    }

    //calculate totalPrice
    public double calculateTotalPrice(Long custid, String tickettype, int ticketQuantity) {
        double ticketprice = retrieveTicketPrice(tickettype);
        double subtotal = ticketprice * ticketQuantity;
        double total = 0.00;

        return total;
    }



    @PostMapping("/createPurchase")
    public String createPurchase(HttpSession session, @ModelAttribute("createPurchase") Purchase purchase,
                                @RequestParam("productName") String productname,
                                @RequestParam("purchaseDate") LocalDate purchaseDate,
                                @RequestParam("productQuantity") int productQuantity,Model model) {
    
        Long customerid = (Long) session.getAttribute("customerid");
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        PreparedStatement statementCreate = null;
        PreparedStatement statementInsert = null;
        PreparedStatement statementUpdate = null;
    
        try {
            // RETRIEVE TICKET ID
            Connection conn = dataSource.getConnection();
            String sqlProductId = "SELECT productid FROM Product WHERE productname=?";
            statement = conn.prepareStatement(sqlProductId);
            statement.setString(1, productname);
            resultSet = statement.executeQuery();
            int productid = -1;
    
            if (resultSet.next()) {
                productid = resultSet.getInt("productid");
            }

            
            // CREATE BOOKING (INSERT TO DATABASE)
            String sql = "INSERT INTO public.purchase(customerid, productdate, purchasetotal, purchasestatus) VALUES (?, ?, ?, ?) RETURNING purchaseid";
            statementCreate = conn.prepareStatement(sql);
            statementCreate.setLong(1, customerid);
            statementCreate.setObject(2, purchaseDate);
            statementCreate.setDouble(3, 0.0); // Initial total price, adjust as needed
            statementCreate.setString(4, "Unpaid");
            resultSet = statementCreate.executeQuery();
    
            if (resultSet.next()) {
                int purchaseid = resultSet.getInt("purchaseid");
    
                // INSERT INTO BRIDGE
                String sqlInsert = "INSERT INTO public.purchaseproduct(purchaseid, productid, productquantity) VALUES (?, ?, ?)";
                statementInsert = conn.prepareStatement(sqlInsert);
                statementInsert.setInt(1, purchaseid);
                statementInsert.setInt(2, productid);
                statementInsert.setInt(3, productQuantity);
                statementInsert.executeUpdate();
    



                 
                // Update total price
                
                double totalPrice = calculateTotalPrice(customerid, productname, purchaseQuantity);
                String sqlPrice = "UPDATE public.purchase SET totalprice=? WHERE purchaseid=?";
                statementUpdate = conn.prepareStatement(sqlPrice);
                statementUpdate.setDouble(1, totalPrice);
                statementUpdate.setInt(2, purchaseid);
                statementUpdate.executeUpdate();

                
            }

        } catch (SQLException e) {
            // Handle SQL exceptions
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

    

 */