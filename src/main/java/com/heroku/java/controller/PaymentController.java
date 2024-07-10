package com.heroku.java.controller;

import java.io.IOException;
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
import org.springframework.web.multipart.MultipartFile;

import com.heroku.java.model.Purchase;
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
    public String makePayment(HttpSession session,@RequestParam("paymentAmount") double totalAmount,
                    @RequestParam("paymentReceipt") MultipartFile paymentReceipt,
                    @RequestParam("selectedPurchases") List<Integer> selectedPurchases,
                    Model model)
        {
            session.getAttribute("customerid");
            byte[] receiptData = null;

            try {
                receiptData = paymentReceipt.getBytes();
            } catch (IOException e) {
            }

            try {
                Connection conn = dataSource.getConnection();
                
                String paymentSql = "INSERT INTO public.payment (paymentamount, paymentreceipt, bookingid) VALUES (?, ?, ?)";
                for (int bookingId : selectedPurchases) {

                    String invalidsql = "SELECT bookingstatus FROM public.booking WHERE bookingid=?";
                    PreparedStatement statementInvalid = conn.prepareStatement(invalidsql);
                    statementInvalid.setInt(1, bookingId);
                    ResultSet resultSet = statementInvalid.executeQuery();
                    if (resultSet.next()){
                        String status = resultSet.getString("bookingstatus");

                        if(status.equalsIgnoreCase("Invalid")){
                            String updateSQL = "UPDATE public.payment SET paymentreceipt=? WHERE bookingid=?";
                            PreparedStatement statementUpdate = conn.prepareStatement(updateSQL);
                            statementUpdate.setInt(2, bookingId);
                            statementUpdate.setBytes(1, receiptData);
                            statementUpdate.executeUpdate();

                            //CHANGE PAYMENT STATUS
                            String sqlStatus = "UPDATE public.booking SET bookingStatus=? WHERE bookingid=?";
                            PreparedStatement statementStatus = conn.prepareStatement(sqlStatus);
                            statementStatus.setInt(2, bookingId);
                            statementStatus.setString(1, "Paid");
                            statementStatus.executeUpdate();
                        }else{  
                            
                            PreparedStatement paymentStatement = conn.prepareStatement(paymentSql);
                            paymentStatement.setDouble(1, totalAmount);
                            paymentStatement.setBytes(2, receiptData);
                            paymentStatement.setInt(3, bookingId);
                            paymentStatement.executeUpdate();

                            //CHANGE PAYMENT STATUS
                            String sqlStatus = "UPDATE public.booking SET bookingStatus=? WHERE bookingid=?";
                            PreparedStatement statementStatus = conn.prepareStatement(sqlStatus);
                            statementStatus.setInt(2, bookingId);
                            statementStatus.setString(1, "Paid");
                            statementStatus.executeUpdate();}

                    }

                    
                  
                    
            } 

            conn.close();
            }catch (SQLException e) { 
                e.printStackTrace();
                return "Payment/PaymentError";
               
            }
                return "Payment/PaymentSuccessful";

        }   

        //STAFF VIEW PAYMENT
        @GetMapping("/viewPayment")
        public String viewPayment(HttpSession session,@RequestParam("bookingId") int bookingId,Model model){
            session.getAttribute("staffid");

            try {
                Connection conn = dataSource.getConnection();
                String sql = "SELECT paymentid,paymentreceipt,bookingid FROM public.payment WHERE bookingid=?";
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setInt(1, bookingId);
                ResultSet resultSet = statement.executeQuery();

                if(resultSet.next()){
                    Payment payment = new Payment();
                    payment.setPaymentId(resultSet.getInt("paymentid"));
                    payment.setBookingId(resultSet.getInt("bookingid"));
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