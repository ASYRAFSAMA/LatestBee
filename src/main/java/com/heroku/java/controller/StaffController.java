package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.heroku.java.model.Staff;

import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Controller
public class StaffController {
    private final DataSource dataSource;

    @Autowired
    public StaffController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/registerStaffs")
    public String registerStaffs() {
        return "registerStaffs";
    }
    
    @PostMapping("/registerStaff")
    public String registerStaff(
            @RequestParam("staffname") String staffName,
            @RequestParam("staffemail") String staffEmail,
            @RequestParam("staffphonenum") String staffPhoneNum,
            @RequestParam("staffrole") String staffRole,
            @RequestParam("staffpass") String staffPass,
            @RequestParam("managerid") Integer managerId) throws IOException {
    
        if (staffName.isEmpty() || staffEmail.isEmpty() || staffPhoneNum.isEmpty() || staffRole.isEmpty() || staffPass.isEmpty() || managerId == null) {
            return "redirect:/registerStaffs?error=missing_params";
        }
    
        Staff staff = new Staff();
        staff.setStaffName(staffName);
        staff.setStaffEmail(staffEmail);
        staff.setStaffPhoneNum(staffPhoneNum);
        staff.setStaffRole(staffRole);
        staff.setStaffPass(staffPass);
        staff.setManagerId(managerId);
    
        try (Connection connection = dataSource.getConnection()) {
            String registerSql = "INSERT INTO public.staff (staffname, staffemail, staffphonenum, staffrole, staffpass, managerid) VALUES (?, ?, ?, ?, ?, ?)";
    
            try (PreparedStatement statement = connection.prepareStatement(registerSql)) {
                statement.setString(1, staff.getStaffName());
                statement.setString(2, staff.getStaffEmail());
                statement.setString(3, staff.getStaffPhoneNum());
                statement.setString(4, staff.getStaffRole());
                statement.setString(5, staff.getStaffPass());
                statement.setInt(6, staff.getManagerId());
    
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to insert staff", e);
        }
    
        return "redirect:/login";
    }
    


    @GetMapping("/staffLogin")
    public String staffLogin() {
        return "Staff/StaffLogin";
    }

    @PostMapping("/staffLogins")
    public String staffLogin(HttpSession session,
                             @RequestParam("staffemail") String staffEmail,
                             @RequestParam("staffpass") String staffPass,
                             Model model) {
        // Check if required parameters are present
        if (staffEmail.isEmpty() || staffPass.isEmpty()) {
            // Handle missing parameters gracefully
            return "redirect:/staffLoginError?error=missing_params";
        }

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT staffid, staffname, staffemail, staffphonenum, staffpass, managerid FROM public.staff WHERE staffemail=?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, staffEmail);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Long staffId = resultSet.getLong("staffid");
                        String staffName = resultSet.getString("staffname");
                        String storedPassword = resultSet.getString("staffpass");

                        if (staffPass.equals(storedPassword)) {
                            session.setAttribute("staffname", staffName);
                            session.setAttribute("staffid", staffId);
                            return "redirect:/staffProfile";
                        } else {
                            // Incorrect password
                            return "redirect:/staffLoginError?error=incorrect_password";
                        }
                    } else {
                        // No user found with that email
                        return "redirect:/staffLoginError?error=user_not_found";
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    @GetMapping("/staffProfile")
    public String staffProfile(HttpSession session, Model model) {
        Long staffId = (Long) session.getAttribute("staffid");

        if (staffId == null) {
            return "redirect:/staffLogin"; // redirect to login if staffId is not in session
        }

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT staffname, staffrole, staffphonenum, staffemail, staffpass, managerid FROM public.staff WHERE staffid = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, staffId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String staffName = resultSet.getString("staffname");
                        String staffRole = resultSet.getString("staffrole");
                        String staffPhoneNum = resultSet.getString("staffphonenum");
                        String staffEmail = resultSet.getString("staffemail");
                        String staffPass = resultSet.getString("staffpass");
                        Integer managerId = resultSet.getInt("managerid");

                        Staff staff = new Staff();
                        staff.setStaffName(staffName);
                        staff.setStaffRole(staffRole);
                        staff.setStaffPhoneNum(staffPhoneNum);
                        staff.setStaffEmail(staffEmail);
                        staff.setStaffPass(staffPass);
                        staff.setManagerId(managerId);

                        model.addAttribute("staff", staff);

                        // Recursively get manager details
                        if (managerId != null && managerId > 0) {
                            Staff manager = getManagerDetails(connection, managerId);
                            model.addAttribute("manager", manager);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }

        return "staffProfile";
    }

    private Staff getManagerDetails(Connection connection, int managerId) throws SQLException {
        String sql = "SELECT staffid, staffname, staffrole, staffphonenum, staffemail, managerid FROM public.staff WHERE staffid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, managerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Long staffId = resultSet.getLong("staffid");
                    String staffName = resultSet.getString("staffname");
                    String staffRole = resultSet.getString("staffrole");
                    String staffPhoneNum = resultSet.getString("staffphonenum");
                    String staffEmail = resultSet.getString("staffemail");
                    Integer managerIdNext = resultSet.getInt("managerid");

                    Staff manager = new Staff();
                    manager.setStaffId(staffId);
                    manager.setStaffName(staffName);
                    manager.setStaffRole(staffRole);
                    manager.setStaffPhoneNum(staffPhoneNum);
                    manager.setStaffEmail(staffEmail);
                    manager.setManagerId(managerIdNext);

                    return manager;
                }
            }
        }

        return null;
    }
}
