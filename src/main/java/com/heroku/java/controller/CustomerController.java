
package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.heroku.java.model.Customer;

import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Controller
public class CustomerController {
    private final DataSource dataSource;

    @Autowired
    public CustomerController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostMapping("/registerAcc")
    public String register(@ModelAttribute("registerAcc")
                                @RequestParam("customername") String customerName,
                                @RequestParam("customerdob") String customerDob,
                                @RequestParam("customeremail") String customerEmail,
                                @RequestParam("customerphonenum") String customerPhoneNum,
                                @RequestParam("customeraddress") String customerAddress,
                                @RequestParam("password") String Password) throws IOException {

         // Check if required parameters are present
    if (customerName.isEmpty() || customerDob.isEmpty() || customerEmail.isEmpty() ||
        customerPhoneNum.isEmpty() || customerAddress.isEmpty() || Password == null) {
        // Handle missing parameters gracefully
        // For example, return an error page or redirect to registration page with error message
        return "redirect:/register?error=missing_params";
    }

        Customer customer = new Customer();
        customer.setCustomerName(customerName);
        customer.setCustomerEmail(customerEmail);
        customer.setCustomerPhoneNum(customerPhoneNum);
        customer.setCustomerAddress(customerAddress);
        customer.setPassword(Password);
    
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(customerDob, formatter);
        customer.setCustomerDob(date);

    try (Connection connection = dataSource.getConnection()) {

            System.out.println("Received activity details:");
            System.out.println("Name: " + customer.getCustomerName());
            System.out.println("BirthDate: " + customer.getCustomerDob());
            System.out.println("Email: " + customer.getCustomerEmail());
            System.out.println("Num: " + customer.getCustomerPhoneNum());
            System.out.println("Address: " + customer.getCustomerAddress());
            System.out.println("Password: " + customer.getPassword());

            String RegisterSql = "INSERT INTO public.customer(customername, customerdob, customeremail, customerphonenum, customeraddress, password) VALUES (?, ?, ?, ?, ?, ?);";

            try (PreparedStatement statement = connection.prepareStatement(RegisterSql)) {
                statement.setString(1, customer.getCustomerName());
                statement.setDate(2, java.sql.Date.valueOf(customer.getCustomerDob()));
                statement.setString(3, customer.getCustomerEmail());
                statement.setString(4, customer.getCustomerPhoneNum());
                statement.setString(5, customer.getCustomerAddress());
                statement.setString(6, customer.getPassword());


                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        long customerId = resultSet.getLong("customerid");
                        customer.setCustomerId(customerId);
                        System.out.println("Inserted customer with ID: " + customerId);
                    } else {
                        throw new SQLException("Failed to insert customer, no ID obtained.");
                    }
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
            // Handle exception appropriately, e.g., log error or rethrow as runtime exception
            throw new RuntimeException("Failed to insert customer", e);
        }

        return "redirect:/custLogin"; // Return the created activity object
        
    }


    @GetMapping("/custLogin")
    public String custLogin() {
        return "custLogin"; // Ensure this view exists
}

    

   /*  @GetMapping("/customerLogin")
        public String customerLogin(){
            return "Customer/CustomerLogin";
        }
*/

        @PostMapping("/customerLogins")
        public String customerLogin(HttpSession session, 
                                    @RequestParam("customeremail") String customerEmail, 
                                    @RequestParam("password") String custpassword, 
                                    Model model) {
            // Check if required parameters are present
            if (customerEmail.isEmpty() || custpassword.isEmpty()) {
                // Handle missing parameters gracefully
                return "redirect:/customerLoginError?error=missing_params";
            }
        
            try (Connection connection = dataSource.getConnection()) {
                String sql = "SELECT customerid, customername, customeremail, customerphonenum, customeraddress, password FROM public.customer WHERE customeremail=?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, customerEmail);
        
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            Long customerId = resultSet.getLong("customerid");
                            String customerName = resultSet.getString("customername");
                            String storedPassword = resultSet.getString("password");
        
                            if (custpassword.equals(storedPassword)) {
                                session.setAttribute("customername", customerName);
                                session.setAttribute("customerid", customerId);
                                return "redirect:/customerProfile";
                            } else {
                                // Incorrect password
                                return "redirect:/customerLoginError?error=incorrect_password";
                            }
                        } else {
                            // No user found with that email
                            return "redirect:/customerLoginError?error=user_not_found";
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "redirect:/error";
            }
        }
        

        @GetMapping("/customerProfile")
        public String customerProfile(HttpSession session, Model model) {
        Long custId = (Long) session.getAttribute("customerid");

    if (custId == null) {
        return "redirect:/customerLogin"; // redirect to login if custId is not in session
    }

    try (Connection connection = dataSource.getConnection()) {
        String sql = "SELECT customername, customerdob, customeremail, customerphonenum, customeraddress, password " +
                     "FROM public.customer " +
                     "WHERE customerid = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, custId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String customerName = resultSet.getString("customername");
                    LocalDate customerDob = resultSet.getDate("customerdob").toLocalDate();
                    String customerEmail = resultSet.getString("customeremail");
                    String customerPhoneNum = resultSet.getString("customerphonenum");
                    String customerAddress = resultSet.getString("customeraddress");
                    String password = resultSet.getString("password");

                    Customer customer = new Customer();
                    customer.setCustomerName(customerName);
                    customer.setCustomerDob(customerDob);
                    customer.setCustomerEmail(customerEmail);
                    customer.setCustomerPhoneNum(customerPhoneNum);
                    customer.setCustomerAddress(customerAddress);
                    customer.setPassword(password);

                    model.addAttribute("customer", customer);
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        return "redirect:/error";
    }

    return "custProfile";
            }
    
            @GetMapping("/customerUpdate")
            public String customerUpdate(@RequestParam(required = false) Long customerId, HttpSession session, Model model) {
                if (customerId == null) {
                    return "redirect:/customerProfile"; // Redirect to profile if customerId is not provided
                }
            
                try (Connection connection = dataSource.getConnection()) {
                    String sql = "SELECT customername, customerdob, customeremail, customerphonenum, customeraddress, password " +
                                 "FROM public.customer " +
                                 "WHERE customerid = ?";
            
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        statement.setLong(1, customerId);
            
                        try (ResultSet resultSet = statement.executeQuery()) {
                            if (resultSet.next()) {
                                Customer customer = new Customer();
                                customer.setCustomerName(resultSet.getString("customername"));
                                customer.setCustomerDob(resultSet.getDate("customerdob").toLocalDate());
                                customer.setCustomerEmail(resultSet.getString("customeremail"));
                                customer.setCustomerPhoneNum(resultSet.getString("customerphonenum"));
                                customer.setCustomerAddress(resultSet.getString("customeraddress"));
                                customer.setPassword(resultSet.getString("password"));
            
                                model.addAttribute("customer", customer);
                            } else {
                                return "redirect:/customerProfile"; // Redirect if no customer found
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return "redirect:/error"; // Redirect to error page
                }
            
                return "Customer/CustomerUpdate";
            }

    
        @PostMapping("/customerUpdate")
        public String customerUpdate(HttpSession session, @ModelAttribute("customer") Customer customer) {
            Long customerId = (Long) session.getAttribute("customerid");
    
            if (customerId == null) {
                return "redirect:/customerLogin"; // redirect to login if custId is not in session
            }
    
            try (Connection conn = dataSource.getConnection()) {
                String sql = "UPDATE public.customer SET customername=?, customerdob=?, customeremail=?, customerphonenum=?, customeraddress=?, password=? WHERE customerid=?";
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, customer.getCustomerName());
                statement.setDate(2, java.sql.Date.valueOf(customer.getCustomerDob()));
                statement.setString(3, customer.getCustomerEmail());
                statement.setString(4, customer.getCustomerPhoneNum());
                statement.setString(5, customer.getCustomerAddress());
                statement.setString(6, customer.getPassword());
                statement.setLong(7, customerId);
                statement.executeUpdate();
    
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
    
            return "redirect:/customerProfile";
        }
    
        @GetMapping("/deleteCustomer")
        public String deleteCustomer(@RequestParam("customerId") Long customerId) {
            try (Connection connection = dataSource.getConnection()) {
                String sql = "DELETE FROM public.customer WHERE customerid = ?";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setLong(1, customerId);
                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
                return "redirect:/error";
            }
    
            return "redirect:/customerProfile";
        }
    }