package com.heroku.java.model;

public class PurchaseProduct {
    private Long purchaseId;
    private Long productId;
    private int productQuantity;
    private Product product;

    // Default constructor
    public PurchaseProduct() {}

    // Constructor with fields
    public PurchaseProduct(Long productId, int productQuantity) {
        this.productId = productId;
        this.productQuantity = productQuantity;
    }

    // Getters and Setters

    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Long purchaseId) {
        this.purchaseId = purchaseId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }
}












/* 
package com.heroku.java.model;

public class ProductPurchase {
    private Long productId;
    private int quantity;

    // Getters and setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

*/