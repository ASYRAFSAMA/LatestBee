
package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.heroku.java.model.Customer;

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
                        throw new SQLException("Failed to insert activity, no ID obtained.");
                    }
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
            // Handle exception appropriately, e.g., log error or rethrow as runtime exception
            throw new RuntimeException("Failed to insert customer", e);
        }

        return "redirect:/register"; // Return the created activity object
    }
}




/* 
    {
        model.addAttribute("customer", new customer());
        return "register";
    }

    @PostMapping("/register")
    public String registerCustomer(@ModelAttribute("customer") customer customer, Model model) {

        try {
            Connection connection = dataSource.getConnection();
            String sql = "INSERT INTO public.customer(customername, customerdob, customeremail, customerphonenum, customeraddress, password) VALUES (?, ?, ?, ?, ?, ?);";
            final var statement = connection.prepareStatement(sql);

            String customerName = customer.getCustomerName();
            String customerDob = customer.getCustomerDob();
            String customerEmail = customer.getCustomerEmail();
            String customerPhoneNum = customer.getCustomerPhoneNum();
            String customerAddress = customer.getCustomerAddress();
            String password = customer.getPassword();


            statement.setString(1, customer.getCustomerName());
            statement.setString(2, customer.getCustomerDob());
            statement.setString(3, customer.getCustomerEmail());
            statement.setString(4, customer.getCustomerPhoneNum());
            statement.setString(5, customer.getCustomerAddress());
            statement.setString(6, customer.getPassword());
            statement.executeUpdate();



            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/index";
        }
        return "redirect:/guestLogin";
    }
}



/* 
    @PostMapping("/register")
    public String registerCustomer(@ModelAttribute("customer") Customer customer, Model model) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO customer (customername, customerdob, customeremail, customerphonenum, customeraddress, password) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, customer.getCustomerName());
            statement.setString(2, customer.getCustomerDob());
            statement.setString(3, customer.getCustomerEmail());
            statement.setString(4, customer.getCustomerPhoneNum());
            statement.setString(5, customer.getCustomerAddress());
            statement.setString(6, customer.getPassword());
            
//sini
    final var resultSet = statement.executeQuery();



if (resultSet.next()) {

    String customerName = resultSet.getString("CustomerName");
    String customerEmail = resultSet.getString("customerEmail");
    String customerDob = resultSet.getString("customerDob");
    String customerPhoneNum = resultSet.getString("customerPhoneNum");
    String customerAddress = resultSet.getString("customerAddress");
    String Password = resultSet.getString("Password");

}

connection.close();
return "redirect:/guestLogin?invalidUsername&Password";




//sini

            resultSet.executeUpdate();
            return "redirect:/login";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while registering the customer.");
            return "register";
        }
        
    }
}

*/