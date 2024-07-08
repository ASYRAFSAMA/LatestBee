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

import javax.security.auth.login.LoginException;
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

    @GetMapping("/registerStaffs")          //boxxde s
    public String registerStaffs() {
        return "registerStaffs";
    }


    @PostMapping("/registerStaff")
    public String registerStaff(@ModelAttribute("registerStaff") Staff staff) {

        try {
            Connection conn = dataSource.getConnection();
            String sql = "INSERT INTO public.staff (staffname,staffemail,staffphonenum,staffaddress,staffpassword,managerid) VALUES (?,?,?,?,?,?)";
            PreparedStatement statement = conn.prepareStatement(sql);
			
			statement.setString(1, staff.getStaffName());
			statement.setString(2, staff.getStaffEmail());
            statement.setString(3, staff.getStaffPhoneNum());
            statement.setString(4, staff.getStaffRole());
			statement.setString(5, staff.getStaffPass());
			 if (staff.getManagerId() != null) {
	                statement.setInt(6, staff.getManagerId());
	            } else {
	                statement.setNull(6, java.sql.Types.INTEGER);
	            }
			
			statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "redirect:/createStaffSuccess";
    }

    @GetMapping("/createStaffSuccess")
    public String createstaffSuccess() {
        return "Staff/CreateStaffSuccess";
    }

    @GetMapping("/staffLogin")
    public String staffLogin() {
        return "Staff/StaffLogin";
    }

    @PostMapping("/staffLogins")
    public String staffLogins(HttpSession session,@RequestParam("staffEmail") String staffEmail, @RequestParam("staffPassword") String staffPassword,Staff staff) throws LoginException, SQLException{
        try {
            try (Connection conn = dataSource.getConnection()) {
                String sql = "SELECT staffid,staffname,staffemail,staffphonenum,staffrole,staffpass,managerid FROM public.staff WHERE staffemail=?";
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1,staffEmail);
                
                ResultSet resultSet = statement.executeQuery();
                if(resultSet.next()) {
                    staff = new Staff();
                    staff.setStaffId(resultSet.getLong("staffid"));
                    staff.setStaffName(resultSet.getString("staffname"));
                    staff.setStaffEmail(resultSet.getString("staffemail"));
                    staff.setStaffPhoneNum(resultSet.getString("staffphonenum"));
                    staff.setStaffRole(resultSet.getString("staffrole"));
                    staff.setStaffPass(resultSet.getString("staffpass"));

                    Long staffId = staff.getStaffId();
                                                                                                //sinixlehtukar
                    if(staff.getStaffEmail().equals(staffEmail) && staff.getStaffPass().equals(staffPassword)) {
                   
                    session.setAttribute("staffname", staff.getStaffName());
                    
                    session.setAttribute("staffid", staffId);
                    
                    return "redirect:/indexStaff";
                   }
                   
                }
                     conn.close();
                     return "redirect:/staffLoginError";

                }

             
    }catch(SQLException e){
        return "redirect:/StaffError";
    }
   
}


    @GetMapping("/indexStaff")
    public String indexStaff(HttpSession session) {
        Long staffid = (Long) session.getAttribute("staffid");
        return "indexstaff";
    }

    @GetMapping("/staffLoginError")
    public String staffLoginError(){
        return "Staff/StaffLoginError";

    }

//STAFF VIEW PROFILE

@GetMapping("/staffProfile")
public String staffProfile(HttpSession session,Model model){
    Object staffIdObj = session.getAttribute("staffid");

    if (staffIdObj == null) {
        // Handle the case where staffid is not found in the session
        // Redirect to a login page or show an error message
        return "redirect:/staffLogin"; // Change this to your actual login page
    }

    Long staffId = (Long) staffIdObj;
    
        try {
            
            Connection conn = dataSource.getConnection();
            String sql = "Select staffname,staffemail,staffphonenum,staffrole,staffpass,managerid FROM public.staff WHERE staffid=? ";
            
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setLong(1, staffId);
            ResultSet resultSet = statement.executeQuery();
            
            if(resultSet.next()) {
                String staffname = resultSet.getString("staffname");
                String staffemail = resultSet.getString("staffemail");
                String staffphonenum = resultSet.getString("staffphonenum");
                String staffrole = resultSet.getString("staffrole");
                String staffpass = resultSet.getString("staffpass");
                Integer managerid = resultSet.getInt("managerid");
                
                 if (resultSet.wasNull()) {
                        managerid = null;
                    }

                Staff staff= new Staff();
                
                staff.setStaffName(staffname);
                staff.setStaffEmail(staffemail);
                staff.setStaffPhoneNum(staffphonenum);
                staff.setStaffRole(staffrole);
                staff.setStaffPass(staffpass);
                staff.setManagerId(managerid);

                 model.addAttribute("staff",staff);
            }

            conn.close();
  
}catch(SQLException e){
} 
return "Staff/StaffProfile";
}


@GetMapping("/staffUpdate")
public String staffUpdate(HttpSession session,Model model){
    session.getAttribute("staffid");
    Object staffIdObj = session.getAttribute("staffid");

    if (staffIdObj == null) {
            return "redirect:/staffLogin"; // redirect to login if staffId is not in session
        }

        Long staffId = (Long) staffIdObj;
        try {
            Connection conn = dataSource.getConnection();
            String sql = "Select staffname,staffemail,staffphonenum,staffrole,staffpass,managerid FROM public.staff WHERE staffid=? ";
            
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setLong(1, staffId);
            ResultSet resultSet = statement.executeQuery();
            
            if(resultSet.next()) {
                String staffname = resultSet.getString("staffname");
                String staffemail = resultSet.getString("staffemail");
                String staffphonenum = resultSet.getString("staffphonenum");
                String staffrole = resultSet.getString("staffrole");
                String staffpass = resultSet.getString("staffpass");
                Integer managerid = resultSet.getInt("managerid");
                
                 if (resultSet.wasNull()) {
                        managerid = null;
                    }

                Staff staff= new Staff();
                
                staff.setStaffName(staffname);
                staff.setStaffEmail(staffemail);
                staff.setStaffPhoneNum(staffphonenum);
                staff.setStaffRole(staffrole);
                staff.setStaffPass(staffpass);
                staff.setManagerId(managerid);

                 model.addAttribute("staff",staff);
            }

            conn.close();
  
}catch(SQLException e){
} 
return "Staff/StaffUpdate";
}


@PostMapping("/staffUpdate")
public String staffUpdate(HttpSession session,@ModelAttribute("staffUpdate") Staff staff, Model model){
    Long staffId = (Long) session.getAttribute("staffid");

    if (staffId == null) {
            return "redirect:/staffLogin"; // redirect to login if staffId is not in session
        }

        try {
            Connection conn = dataSource.getConnection();
            String sql = "UPDATE public.staff SET staffname=?,staffemail=?,staffphonenum=?,staffrole=?,staffpass=?,managerid=? WHERE staffid=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setLong(7,staffId);
            statement.setString(1, staff.getStaffName());
            statement.setString(2, staff.getStaffEmail());
            statement.setString(3, staff.getStaffPhoneNum());
            statement.setString(4, staff.getStaffRole());
            statement.setString(5, staff.getStaffPass());
            if (staff.getManagerId() != null) {
                statement.setInt(6, staff.getManagerId());
            } else {
                statement.setNull(6, java.sql.Types.INTEGER);
            }
            statement.executeUpdate();
            
            conn.close();

        } catch (SQLException e) {
        } return "redirect:/staffProfile";
}


@GetMapping("/staffDelete")
public String staffDelete(HttpSession session){
    Long custId = (Long) session.getAttribute("staffid");

    try {
        Connection conn= dataSource.getConnection();
        String sql = "Delete From public.staff WHERE staffid=?";
        PreparedStatement statement = conn.prepareStatement(sql);

        statement.setLong(1, custId);

        statement.executeUpdate();
    } catch (SQLException e) {
    } return "redirect:/deleteStaffSuccess";

}

@GetMapping("/deleteStaffSuccess")
public String deleteStaffSuccess(){
    return "Staff/DeleteStaffSuccess";
}

@GetMapping("/logoutConfirmationStaff")
    public String logoutConfirmationStaff(HttpSession session){
        session.getAttribute("staffid");
        return "Staff/StaffLogout";
    }   


/*   
    


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
        */
}
