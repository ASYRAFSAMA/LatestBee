package com.heroku.java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.heroku.java.model.Customer;

import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

@SpringBootApplication
@Controller
public class GettingStartedApplication {
    private final DataSource dataSource;

    @Autowired
    public GettingStartedApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/logoutViewRoom")
    public String logoutViewRoom() {
        return "logoutViewRoom";
    }

    @GetMapping("/index")
    public String index1(@RequestParam(name = "success", required = false) Boolean success, HttpSession session) {
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        return "index";
    }

/*      @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("customer", new Customer());
        return "login";
    }
*/

    @PostMapping("/login")
    public String login(@ModelAttribute Customer customer, HttpSession session, Model model) {
        // Dummy login logic, should be replaced with actual authentication logic
        if ("test@example.com".equals(customer.getCustomerEmail()) && "password".equals(customer.getCustomerPhoneNum())) {
            session.setAttribute("guestICNumber", customer.getCustomerEmail());
            return "redirect:/index?success=true";
        }
        model.addAttribute("error", "Invalid email or password");
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("customer", new Customer());
        return "register";
    }

/* 

    @PostMapping("/register")
    public String register(@ModelAttribute Customer customer, Model model) {
        // Save the customer to the database
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO customers (customer_name, customer_dob, customer_email, customer_phone_num, customer_address) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, customer.getCustomerName());
            ps.setDate(2, java.sql.Date.valueOf(customer.getCustomerDob()));
            ps.setString(3, customer.getCustomerEmail());
            ps.setString(4, customer.getCustomerPhoneNum());
            ps.setString(5, customer.getCustomerAddress());
            ps.executeUpdate();
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while registering. Please try again.");
            return "register";
        }
        return "redirect:/login";
    }

    */
    public static void main(String[] args) {
        SpringApplication.run(GettingStartedApplication.class, args);
    }
}
