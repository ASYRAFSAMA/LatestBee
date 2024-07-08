package com.heroku.java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.sql.Connection;
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
        return "index";
    }

    @GetMapping("/staff")
    public String indexstaff() {
        return "indexstaff";
    }
    @GetMapping("/cust")
    public String indexcust() {
        return "indexcust";
    }

    @GetMapping("/custregister")
    public String register() {
        return "custregister";
    }

    


    @GetMapping("/custProfile")
    public String custProfile() {
        return "custProfile";
    }

    @GetMapping("/staffregister")
    public String staffregister() {
        return "staffregister";
    }



    @GetMapping("/addproduct")
    public String addproduct() {
        return "addproduct";
    }

    @GetMapping("/listproduct")
    public String listProduct() {
        return "listproduct";
    }

    @GetMapping("/viewproduct")
    public String viewProduct() {
        return "viewproduct";
    }

    @GetMapping("/updateproduct")
    public String updateProduct() {
        return "updateproduct";
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
