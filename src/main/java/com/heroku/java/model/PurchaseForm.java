package com.heroku.java.model;
import java.util.List;

public class PurchaseForm {
    private Long staffId;
    private Long customerId;
    private List<ProductPurchase> products;

    // Getters and setters
    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<ProductPurchase> getProducts() {
        return products;
    }

    public void setProducts(List<ProductPurchase> products) {
        this.products = products;
    }
}
