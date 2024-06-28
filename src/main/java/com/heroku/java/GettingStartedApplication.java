package com.heroku.java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import com.heroku.java.model.Customer;

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
        return "register";
    }
/* 
    @GetMapping("/logoutViewRoom")
    public String logoutViewRoom() {
        return "logoutViewRoom";
    }

    @GetMapping("/index")
    public String index1(@RequestParam(name = "success", required = false) Boolean success, HttpSession session) {
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        return "index";
    }

    @GetMapping("/guestLogin")
    public String guestLogin() {
        return "guest/guestLogin";
    }

    @GetMapping("/index_logout")
    public String index_logout() {
        return "index_logout";
    }
 */

 /* 
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "register";
    }

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

            statement.executeUpdate();
            return "redirect:/login";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while registering the customer.");
            return "register";
        }
    }
*/
    
    @GetMapping("/database")
    String database(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
            statement.executeUpdate("INSERT INTO ticks VALUES (now())");

            final var resultSet = statement.executeQuery("SELECT tick FROM ticks");
            final var output = new ArrayList<>();
            while (resultSet.next()) {
                output.add("Read from DB: " + resultSet.getTimestamp("tick"));
            }

            model.put("records", output);
            return "database";
        } catch (Throwable t) {
            model.put("message", t.getMessage());
            return "error";
        }
    }

    

    public static void main(String[] args) {
        SpringApplication.run(GettingStartedApplication.class, args);
    }
}
