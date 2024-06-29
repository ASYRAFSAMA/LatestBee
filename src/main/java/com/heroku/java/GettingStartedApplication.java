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
        return "index";
    }

    @GetMapping("/register")
    public String register() {
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
