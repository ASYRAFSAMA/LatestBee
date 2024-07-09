package com.heroku.java.model;

import java.time.LocalDate;

public class Customer {
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private LocalDate customerDob;
    private String customerPhoneNum;
    private String customerAddress;
    private String password;

    public Customer(String customerName2, String customerEmail2, String customerAddress2, String customerphonenum2,
            String password2) {
        //TODO Auto-generated constructor stub
    }
    public Long getCustomerId() {
        return customerId;
    }
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public LocalDate getCustomerDob() {
        return customerDob;
    }

    public void setCustomerDob(LocalDate customerDob) {
        this.customerDob = customerDob;
    }

    public String getCustomerPhoneNum() {
        return customerPhoneNum;
    }

    public void setCustomerPhoneNum(String customerPhoneNum) {
        this.customerPhoneNum = customerPhoneNum;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

