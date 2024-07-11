package com.heroku.java.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.heroku.java.model.Purchase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heroku.java.model.Payment;

import jakarta.servlet.http.HttpSession;

@Controller
public class PaymentController {
    private final DataSource dataSource;
    

    @Autowired
    public PaymentController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/makePayment")
    public String makePayment(@RequestParam("selectedPurchases")List<Integer> selectedPurchases,Model model,HttpSession session){
        session.getAttribute("customerid");
        List<Purchase> purchaseDetails = new ArrayList<>();
        double paymentAmount=0.0;
        
        try {
            Connection conn = dataSource.getConnection();
             String sql = "SELECT purchaseid, purchasedate, purchasetotal, purchasestatus FROM public.purchase WHERE purchaseid = ?";
        for (int purchaseId : selectedPurchases){
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, purchaseId);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                Purchase purchase = new Purchase();
                purchase.setPurchaseId(resultSet.getInt("purchaseid"));
                purchase.setPurchaseDate(resultSet.getDate("purchasedate"));
                purchase.setPurchaseTotal(resultSet.getDouble("purchasetotal"));
                purchase.setPurchaseStatus(resultSet.getString("purchasestatus"));
                
                String bookingStatus = resultSet.getString("bookingstatus");
                if (bookingStatus.equalsIgnoreCase("Paid")||bookingStatus.equalsIgnoreCase("Approved")) {
                    return "Payment/PaymentExistError";
                }
                

                purchaseDetails.add(purchase);
                paymentAmount+= purchase.getPurchaseTotal();
                
            }


        } 
        conn.close();
    

        model.addAttribute("purchases",purchaseDetails);
        model.addAttribute("totalAmount", paymentAmount);

        } catch (SQLException e) {
        } return "Payment/PaymentPage";
    }

    @PostMapping("/makePayment")
public String makePayment(HttpSession session,
                          @RequestParam("paymentAmount") double totalAmount,
                          @RequestParam("paymentReceipt") MultipartFile paymentReceipt,
                          @RequestParam("cartItems") String cartItemsJson) {
    Long customerId = (Long) session.getAttribute("customerid");
    if (customerId == null) {
        return "redirect:/custLogin";
    }

    List<Map<String, Object>> cartItems;
    try {
        cartItems = new ObjectMapper().readValue(cartItemsJson, new TypeReference<List<Map<String, Object>>>(){});
    } catch (JsonProcessingException e) {
        e.printStackTrace();
        return "Payment/PaymentError";
    }

    byte[] receiptData;
    try {
        receiptData = paymentReceipt.getBytes();
    } catch (IOException e) {
        e.printStackTrace();
        return "Payment/PaymentSuccessful";
    }

    try (Connection conn = dataSource.getConnection()) {
        conn.setAutoCommit(false);

        // Insert into payment table
        String paymentSql = "INSERT INTO public.payment (paymentamount, paymentreceipt) VALUES (?, ?) RETURNING paymentid";
        int paymentId;
        try (PreparedStatement paymentStatement = conn.prepareStatement(paymentSql)) {
            paymentStatement.setDouble(1, totalAmount);
            paymentStatement.setBytes(2, receiptData);
            ResultSet rs = paymentStatement.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Failed to insert payment record");
            }
            paymentId = rs.getInt(1);
        }

        // Insert into purchase table
        String purchaseSql = "INSERT INTO public.purchase (customerid, purchasedate, purchasetotal, purchasestatus, paymentid) VALUES (?, CURRENT_DATE, ?, 'Unpaid', ?) RETURNING purchaseid";
        int purchaseId;
        try (PreparedStatement purchaseStatement = conn.prepareStatement(purchaseSql)) {
            purchaseStatement.setLong(1, customerId);
            purchaseStatement.setDouble(2, totalAmount);
            purchaseStatement.setInt(3, paymentId);
            ResultSet rs = purchaseStatement.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Failed to insert purchase record");
            }
            purchaseId = rs.getInt(1);
        }

        // Insert into purchaseproduct table
        String purchaseProductSql = "INSERT INTO public.purchaseproduct (purchaseid, productid, productquantity) VALUES (?, ?, ?)";
        try (PreparedStatement purchaseProductStatement = conn.prepareStatement(purchaseProductSql)) {
            for (Map<String, Object> item : cartItems) {
                int productId = Integer.parseInt(String.valueOf(item.get("id")));
                int productQuantity = Integer.parseInt(String.valueOf(item.get("quantity")));
                double productPrice = Double.parseDouble(String.valueOf(item.get("price")));  // If price is sent as string
                purchaseProductStatement.setInt(1, purchaseId);
                purchaseProductStatement.setInt(2, productId);
                purchaseProductStatement.setInt(3, productQuantity);
                purchaseProductStatement.addBatch();
            }
            purchaseProductStatement.executeBatch();
        }

        conn.commit();
    } catch (SQLException e) {
        e.printStackTrace();
        return "Payment/PaymentError";
    }

    return "Payment/PaymentSuccessful";
}

        //STAFF VIEW PAYMENT
        @GetMapping("/viewPayment")
        public String viewPayment(HttpSession session,@RequestParam("purchaseId") int purchaseId,Model model){
            session.getAttribute("staffid");

            try {
                Connection conn = dataSource.getConnection();
                String sql = "SELECT paymentid,paymentreceipt,purchaseid FROM public.payment WHERE purchaseid=?";
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setInt(1, purchaseId);
                ResultSet resultSet = statement.executeQuery();

                if(resultSet.next()){
                    Payment payment = new Payment();
                    payment.setPaymentId(resultSet.getInt("paymentid"));
                    payment.setPurchaseId(resultSet.getInt("purchaseid"));
                    payment.setPaymentReceipt(resultSet.getBytes("paymentreceipt"));

                    model.addAttribute("payments", payment);

                }else{
                    return "Payment/PaymentNotMade";
                }

                conn.close();
                
                
            } catch (SQLException e) {
                e.printStackTrace();
            } return "Payment/StaffViewPayment";
        }

        @GetMapping("/verifyPayment")
        public String verifyPayment(HttpSession session,@RequestParam("bookingId") int bookingId){
            session.getAttribute("staffid");

            try {
                Connection conn = dataSource.getConnection();
                String sql ="SELECT bookingstatus FROM public.booking WHERE bookingid=?";
                PreparedStatement statementcheck = conn.prepareStatement(sql);
                statementcheck.setInt(1, bookingId);
                ResultSet resultSet = statementcheck.executeQuery();

                if(resultSet.next()){
                    String status = resultSet.getString("bookingstatus");
                    if(status.equalsIgnoreCase("Approved")){
                        return "Payment/VerifyPaymentInvalid";
                    }else{
                        String sqlUpdate = "UPDATE public.booking SET bookingstatus=? WHERE bookingid=?";
                        PreparedStatement statement = conn.prepareStatement(sqlUpdate);
                        statement.setInt(2, bookingId);
                        statement.setString(1, "Approved");
                        statement.executeUpdate();

                    }
                }
            } catch (SQLException e) {
            } return "Payment/VerifyPaymentSuccess";
        }

        @GetMapping("/InvalidPayment")
        public String InvalidPayment(HttpSession session,@RequestParam("bookingId") int bookingId){
            session.getAttribute("staffid");

            try {
                Connection conn = dataSource.getConnection();
                String sql ="SELECT bookingstatus FROM public.booking WHERE bookingid=?";
                PreparedStatement statementcheck = conn.prepareStatement(sql);
                statementcheck.setInt(1, bookingId);
                ResultSet resultSet = statementcheck.executeQuery();

                if(resultSet.next()){
                    String status = resultSet.getString("bookingstatus");
                    if(status.equalsIgnoreCase("Approved")){
                        return "Payment/VerifyPaymentInvalid";
                    }else{
                        String sqlUpdate = "UPDATE public.booking SET bookingstatus=? WHERE bookingid=?";
                        PreparedStatement statement = conn.prepareStatement(sqlUpdate);
                        statement.setInt(2, bookingId);
                        statement.setString(1, "Invalid");
                        statement.executeUpdate();

                    }
                }
            } catch (SQLException e) {
            } return "Payment/InvalidPayment";
        }
        



}