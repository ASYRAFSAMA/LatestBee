package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.heroku.java.model.Customer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Controller
public class CustomerController {
    private final DataSource dataSource;

    @Autowired
    public CustomerController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "register";
    }

    @PostMapping("/register")
    public String registerCustomer(@ModelAttribute("customer") Customer customer, Model model) {

        try {
            Connection connection = dataSource.getConnection();
            String sql = "INSERT INTO public.guest(guesticnumber, guestname, guestphonenumber, guestgender, guestreligion, guestrace, guestaddress, guestemail, guestpassword) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
            final var statement = connection.prepareStatement(sql);

            String CustomerName = customer.getCustomerName();
            String CustomerDob = customer.getCustomerDob();
            String CustomerEmail = customer.getCustomerEmail();
            String CustomerPhoneNum = customer.getCustomerPhoneNum();
            String CustomerAddress = customer.getCustomerAddress();
            String Password = customer.getPassword();


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