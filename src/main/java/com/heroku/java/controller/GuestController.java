package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.heroku.java.model.CustomerModel;

import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class GuestController {
    private final DataSource dataSource;

    @Autowired
    public GuestController(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @GetMapping("/guestRegister")
    public String guestRegister() {
        return "guest/guestRegister";
    }

    @GetMapping("/managerGuestList")
    public String managerGuestList(Model model, HttpSession session) {
        List<CustomerModel> guests = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT guest_id, guest_name, guest_phone_number, guest_ic_number, guest_gender, guest_religion, guest_race, guest_address, guest_email, guest_password FROM public.guest ORDER BY guest_name";
            var statement = connection.prepareStatement(sql);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                CustomerModel guest = new CustomerModel();
                //guest.setGuestId(resultSet.getString("customerid"));
                guest.setGuestName(resultSet.getString("customername"));
                guest.setGuestPhoneNumber(resultSet.getString("customerphonenum"));
                guest.setGuestIcNumber(resultSet.getString("guest_ic_number"));
                guest.setGuestGender(resultSet.getString("guest_gender"));
                guest.setGuestReligion(resultSet.getString("guest_religion"));
                guest.setGuestRace(resultSet.getString("guest_race"));
                guest.setGuestAddress(resultSet.getString("customeraddress"));

                
                // Set other fields as needed

                guests.add(guest);
            }

            model.addAttribute("guests", guests);
            return "manager/managerGuestList";
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @PostMapping("/managerGuestList")
    public String managerGuestList(Model model, HttpSession session, @RequestParam("searchInput") String searchInput) {
        searchInput = searchInput.trim();
        List<CustomerModel> guests = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT guest_id, guest_name, guest_phone_number, guest_ic_number, guest_gender, guest_religion, guest_race, guest_address, guest_email, guest_password FROM public.guest WHERE guest_ic_number ILIKE ? OR guest_name ILIKE ? ORDER BY guest_name DESC";
            var statement = connection.prepareStatement(sql);
            statement.setString(1, "%" + searchInput + "%");
            statement.setString(2, "%" + searchInput + "%");
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                CustomerModel guest = new CustomerModel();
                guest.setGuestId(resultSet.getString("guest_id"));
                guest.setGuestName(resultSet.getString("guest_name"));
                guest.setGuestPhoneNumber(resultSet.getString("guest_phone_number"));
                guest.setGuestIcNumber(resultSet.getString("guest_ic_number"));
                guest.setGuestGender(resultSet.getString("guest_gender"));
                guest.setGuestReligion(resultSet.getString("guest_religion"));
                guest.setGuestRace(resultSet.getString("guest_race"));
                guest.setGuestAddress(resultSet.getString("guest_address"));
                // Set other fields as needed

                guests.add(guest);
            }

            model.addAttribute("guests", guests);
            if (guests.isEmpty()) {
                model.addAttribute("messageNoResult", "No results found for \"" + searchInput + "\"");
            }
            return "manager/managerGuestList";
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/staffGuestList")
    public String staffGuestList(Model model, HttpSession session) {
        List<CustomerModel> guests = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT guest_id, guest_name, guest_phone_number, guest_ic_number, guest_gender, guest_religion, guest_race, guest_address, guest_email, guest_password FROM public.guest ORDER BY guest_name";
            var statement = connection.prepareStatement(sql);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                CustomerModel guest = new CustomerModel();
                guest.setGuestId(resultSet.getString("guest_id"));
                guest.setGuestName(resultSet.getString("guest_name"));
                guest.setGuestPhoneNumber(resultSet.getString("guest_phone_number"));
                guest.setGuestIcNumber(resultSet.getString("guest_ic_number"));
                guest.setGuestGender(resultSet.getString("guest_gender"));
                guest.setGuestReligion(resultSet.getString("guest_religion"));
                guest.setGuestRace(resultSet.getString("guest_race"));
                guest.setGuestAddress(resultSet.getString("guest_address"));
                // Set other fields as needed

                guests.add(guest);
            }

            model.addAttribute("guests", guests);
            return "staff/staffGuestList";
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @PostMapping("/guestRegister")
    public String guestRegister(@ModelAttribute("guestRegister") CustomerModel guest) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO public.guest(guest_id, guest_name, guest_phone_number, guest_ic_number, guest_gender, guest_religion, guest_race, guest_address, guest_email, guest_password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            var statement = connection.prepareStatement(sql);

            statement.setString(1, guest.getGuestId());
            statement.setString(2, guest.getGuestName());
            statement.setString(3, guest.getGuestPhoneNumber());
            statement.setString(4, guest.getGuestIcNumber());
            statement.setString(5, guest.getGuestGender());
            statement.setString(6, guest.getGuestReligion());
            statement.setString(7, guest.getGuestRace());
            statement.setString(8, guest.getGuestAddress());
            statement.setString(9, guest.getGuestPassword());
            statement.executeUpdate();

            return "redirect:/guestLogin";
        } catch (SQLException e) {
            e.printStackTrace();
            return "redirect:/index";
        }
    }

    @GetMapping("/managerViewGuest")
    public String managerViewGuest(@RequestParam("guestICNumber") String guestICNumber, Model model, HttpSession session) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT guest_id, guest_name, guest_phone_number, guest_ic_number, guest_gender, guest_religion, guest_race, guest_address, guest_email, guest_password FROM public.guest WHERE guest_ic_number LIKE ?";
            var statement = connection.prepareStatement(sql);
            statement.setString(1, guestICNumber);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                CustomerModel guest = new CustomerModel();
                guest.setGuestId(resultSet.getString("guest_id"));
                guest.setGuestName(resultSet.getString("guest_name"));
                guest.setGuestPhoneNumber(resultSet.getString("guest_phone_number"));
                guest.setGuestIcNumber(resultSet.getString("guest_ic_number"));
                guest.setGuestGender(resultSet.getString("guest_gender"));
                guest.setGuestReligion(resultSet.getString("guest_religion"));
                guest.setGuestRace(resultSet.getString("guest_race"));
                guest.setGuestAddress(resultSet.getString("guest_address"));
                guest.setGuestEmail(resultSet.getString("guest_email"));
                guest.setGuestPassword(resultSet.getString("guest_password"));
                model.addAttribute("guest", guest);
            }

            return "manager/managerViewGuest";
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/staffViewGuest")
    public String staffViewGuest(@RequestParam("guestICNumber") String guestICNumber, Model model, HttpSession session) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT guest_id, guest_name, guest_phone_number, guest_ic_number, guest_gender, guest_religion, guest_race, guest_address, guest_email, guest_password FROM public.guest WHERE guest_ic_number LIKE ?";
            var statement = connection.prepareStatement(sql);
            statement.setString(1, guestICNumber);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                CustomerModel guest = new CustomerModel();
                guest.setGuestId(resultSet.getString("guest_id"));
                guest.setGuestName(resultSet.getString("guest_name"));
                guest.setGuestPhoneNumber(resultSet.getString("guest_phone_number"));
                guest.setGuestIcNumber(resultSet.getString("guest_ic_number"));
                guest.setGuestGender(resultSet.getString("guest_gender"));
                guest.setGuestReligion(resultSet.getString("guest_religion"));
                guest.setGuestRace(resultSet.getString("guest_race"));
                guest.setGuestAddress(resultSet.getString("guest_address"));
                guest.setGuestEmail(resultSet.getString("guest_email"));
                guest.setGuestPassword(resultSet.getString("guest_password"));
                model.addAttribute("guest", guest);
            }

            return "staff/staffViewGuest";
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
    }
}
