package com.heroku.java.controller;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.heroku.java.model.Purchase;
import com.heroku.java.model.PurchaseProduct;
import com.heroku.java.model.Product;

import jakarta.servlet.http.HttpSession;

@Controller
public class PurchaseController {
    private final DataSource dataSource;

    @Autowired
    public PurchaseController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/purchaseProductList")
    public String listProducts(Model model, HttpSession session) {
        List<Product> products = new ArrayList<>();
        Long customerId = (Long) session.getAttribute("customerid");

        if (customerId == null) {
            return "redirect:/custLogin";
        }

        try (Connection connection = dataSource.getConnection()) {
            String getProductsSql = "SELECT * FROM public.product";
            try (PreparedStatement statement = connection.prepareStatement(getProductsSql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Product product = new Product();
                        product.setProductId(resultSet.getLong("productid"));
                        product.setProductName(resultSet.getString("productname"));
                        product.setProductType(resultSet.getString("producttype"));
                        product.setProductQuantity(resultSet.getInt("productquantity"));
                        product.setProductPrice(resultSet.getDouble("productprice"));
                        product.setProductImage(resultSet.getString("productimage"));
                        products.add(product);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve products", e);
        }

        Purchase purchase = new Purchase();
        purchase.setCustomerId(customerId);
        List<PurchaseProduct> purchaseProducts = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            purchaseProducts.add(new PurchaseProduct());
        }
        purchase.setPurchaseProducts(purchaseProducts);

        model.addAttribute("products", products);
        model.addAttribute("purchase", purchase);
        return "Purchase/CustomerPurchase";
    }

    @PostMapping("/createPurchase")
    public String createPurchase(@ModelAttribute Purchase purchase, HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute("customerid");
        if (customerId == null) {
            return "redirect:/custLogin";
        }

        double totalPurchaseAmount = 0.0;
        List<PurchaseProduct> purchaseDetails = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            
            String insertPurchaseSql = "INSERT INTO public.purchase (staffid, customerid, purchasetotal, purchasedate, purchasestatus) VALUES (NULL, ?, ?, ?, 'PENDING') RETURNING purchaseid";
            try (PreparedStatement insertPurchaseStmt = connection.prepareStatement(insertPurchaseSql)) {
                insertPurchaseStmt.setLong(1, customerId);
                insertPurchaseStmt.setDouble(2, totalPurchaseAmount);
                insertPurchaseStmt.setDate(3, Date.valueOf(LocalDate.now()));
                ResultSet rs = insertPurchaseStmt.executeQuery();
                rs.next();
                Long purchaseId = rs.getLong("purchaseid");
                rs.close();

                String insertPurchaseProductSql = "INSERT INTO public.purchaseproduct (purchaseid, productid, productquantity) VALUES (?, ?, ?)";
                try (PreparedStatement insertPurchaseProductStmt = connection.prepareStatement(insertPurchaseProductSql)) {
                    for (PurchaseProduct purchaseProduct : purchase.getPurchaseProducts()) {
                        if (purchaseProduct.getProductQuantity() > 0) {
                            insertPurchaseProductStmt.setLong(1, purchaseId);
                            insertPurchaseProductStmt.setLong(2, purchaseProduct.getProductId());
                            insertPurchaseProductStmt.setInt(3, purchaseProduct.getProductQuantity());
                            insertPurchaseProductStmt.addBatch();

                            totalPurchaseAmount += purchaseProduct.getProductQuantity() * getProductPriceById(purchaseProduct.getProductId(), connection);
                            purchaseDetails.add(new PurchaseProduct(purchaseProduct.getProductId(), purchaseProduct.getProductQuantity()));
                        }
                    }
                    insertPurchaseProductStmt.executeBatch();
                }

                String updatePurchaseTotalSql = "UPDATE public.purchase SET purchasetotal = ? WHERE purchaseid = ?";
                try (PreparedStatement updatePurchaseTotalStmt = connection.prepareStatement(updatePurchaseTotalSql)) {
                    updatePurchaseTotalStmt.setDouble(1, totalPurchaseAmount);
                    updatePurchaseTotalStmt.setLong(2, purchaseId);
                    updatePurchaseTotalStmt.executeUpdate();
                }

                connection.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create purchase", e);
        }

        model.addAttribute("purchaseDetails", purchaseDetails);
        model.addAttribute("totalPurchaseAmount", totalPurchaseAmount);
        return "Purchase/CreatePurchase";
    }

    private double getProductPriceById(Long productId, Connection connection) throws SQLException {
        String getProductPriceSql = "SELECT productprice FROM public.product WHERE productid = ?";
        try (PreparedStatement getProductPriceStmt = connection.prepareStatement(getProductPriceSql)) {
            getProductPriceStmt.setLong(1, productId);
            try (ResultSet resultSet = getProductPriceStmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("productprice");
                }
            }
        }
        throw new SQLException("Product price not found for product ID: " + productId);
    }
}











/* 

package com.heroku.java.controller;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.heroku.java.model.Product;
import com.heroku.java.model.PurchaseForm;
import com.heroku.java.model.Customer;

import jakarta.servlet.http.HttpSession;

import com.heroku.java.model.ProductPurchase;

@Controller
public class PurchaseController {
    private final DataSource dataSource;

    @Autowired
    public PurchaseController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/purchaseProductList")
    public String purchaseProductList(HttpSession session, Model model){
        session.getAttribute("customerid");
        List<Product> products = new ArrayList<>();

        try {
            Connection conn = dataSource.getConnection();
            String sql = "SELECT productname,productprice from public.product";
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){
                String productName = resultSet.getString("productname");
                double productPrice = resultSet.getDouble("productprice");
                Product product = new Product();
                product.setProductName(productName);
                product.setProductPrice(productPrice);
                products.add(product);
                
            }
            model.addAttribute("product", products);
            
            
        } catch (SQLException e) {
        } return "Purchase/CustomerPurchase";
    }

    @GetMapping("/customerPurchase")
    public String customerPurchase(HttpSession session, Model model) {
        Long customerid = (Long) session.getAttribute("customerid");
        int productQuantity = (int) session.getAttribute("productQuantity");
        LocalDate purchaseDate = (LocalDate) session.getAttribute("purchaseDate");
        String productName = (String) session.getAttribute("productName");
        

       
            //RETRIEVE CUSTOMER TYPE

            try{Connection conn = dataSource.getConnection();
            String sqlType = "SELECT customername, customeremail, customeraddress, c.customerphonenum, password ";
            PreparedStatement statementType = conn.prepareStatement(sqlType);
            statementType.setLong(1,customerid);
            ResultSet resultSet = statementType.executeQuery();
            
            if(resultSet.next()){
                String customerName = resultSet.getString("customername");
                String customerEmail = resultSet.getString("customeremail");
                String customerAddress = resultSet.getString("customeraddress");
                String customerphonenum= resultSet.getString("customerphonenum");
                String Password = resultSet.getString("password");
                Customer customer = null;
                String customerType =null;
/* 
                if (custic != null){

                    customer = new Citizen(custName, custEmail, custphonenum, custAddress, custPassword, custid, custic);
                    customerType = "Citizen";
                    } else if (custPassport!= null){
                        customer = new NonCitizen(custName, custEmail, custphonenum, custAddress, custPassword, custid, custPassport);
                        customerType = "NonCitizen";
                    }
                    
                   
                    
                    model.addAttribute("customer",customer);
                    model.addAttribute("customerType", customerType);

            }
             double subtotal = calculateSubTotal(customerid, productName, productQuantity);
        model.addAttribute("subtotal",subtotal);
        model.addAttribute("productDate", purchaseDate);
        model.addAttribute("productQuantity", productQuantity);
        model.addAttribute("productName", productName);
    
        double totalPrice = calculateTotalPrice(customerid, productName, productQuantity);
        model.addAttribute("totalPrice", totalPrice);
        }catch(SQLException e){

        }

        return "Booking/CreateBooking";
    }
    

    //retrieve price from ticket table
    public double retrieveProductPrice(String productname){
        double productprice=0.0;
        try {
            Connection conn = dataSource.getConnection();
            String sql = "Select productprice from public.product WHERE productname=?";
            PreparedStatement statement = conn.prepareStatement(sql);

            statement.setString(1, productname);
            ResultSet resultSet =statement.executeQuery();

            if(resultSet.next()){
                 productprice = resultSet.getDouble("productprice");
            }

            conn.close();

        } catch (SQLException e) {
        }
        return productprice;
    }

    public Customer retrieveCustomerById(Long customerId) throws Exception {
        Customer customer = null;
    
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT c.customerid, c.customername, c.customeremail, c.customeraddress, c.customerphonenum, password " +
                         "FROM public.customers c " +
                         "WHERE c.customerid = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setLong(1, customerId);
            ResultSet resultSet = statement.executeQuery();
    
            if (resultSet.next()) {
                String customername = resultSet.getString("customername");
                String customeremail = resultSet.getString("customeremail");
                String customeraddress = resultSet.getString("customeraddress");
                String customerphonenum = resultSet.getString("customerphonenum");
                String customerpassword = resultSet.getString("password");
    
                // Complete the object creation logic as needed
                // e.g., customer = new Customer(customername, customeremail, customeraddress, customerphonenum, customerpassword);
    
                // Currently commented out incomplete logic
                // if (custicnum != null) {
                //     customer = new Citizen(custname, custemail, custphonenum, custaddress, custpassword,custId, custicnum);
                // } else if (custpassport != null) {
                //     customer = new NonCitizen(custname, custemail, custphonenum, custaddress, custpassword,custId,custpassport);
                // }
            }
            resultSet.close();
            statement.close();
    
        } catch (Exception e) {
            e.printStackTrace(); // Better to log the exception
            throw new Exception("Error retrieving customer with ID: " + customerId, e);
        }
    
        if (customer == null) {
            throw new Exception("Customer not found for ID: " + customerId);
        }
    
        return customer;
    }
    


    //CALCULATE SUBTOTAL

    public double calculateSubTotal(Long customerid, String productname, int productQuantity) {
        double productprice = retrieveProductPrice(productname);
        double subtotal = productprice * productQuantity;
        
        return subtotal;
    }




    //calculate totalPrice
    public double calculateTotalPrice(Long customerid, String productname, int productQuantity) {
        double productprice = retrieveProductPrice(productname);
        double subtotal = productprice * productQuantity;
        double total = 0.00;
    }
        
    

}

 */