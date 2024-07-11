package com.heroku.java.model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Purchase {
    private int purchaseId;
    private int staffId;
    private int customerId;
    private Double purchaseTotal;
    private Date purchaseDate;
    private String purchaseStatus;
    private List<PurchaseProduct> purchaseProducts;
	private String customerName;


    private int productQuantity;

    private String productName;

    public Purchase() {}
	
	public Purchase(int purchaseId,int customerId,int staffId,double purchaseTotal,Date purchaseDate,String productName,int productQuantity) {
		
		this.purchaseId=purchaseId;
		this.customerId=customerId;
		this.staffId=staffId;
		this.purchaseTotal=purchaseTotal;
		this.purchaseDate=purchaseDate;
		this.productName=productName;
        this.productQuantity=productQuantity;
	}


	public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public void addProduct(Product product, int quantity) {
        if (this.purchaseProducts == null) {
            this.purchaseProducts = new ArrayList<>();
        }
        PurchaseProduct purchaseProduct = new PurchaseProduct();
        purchaseProduct.setProductId(product.getProductId());
        purchaseProduct.setProductQuantity(quantity);
        this.purchaseProducts.add(purchaseProduct);
    }

    public int getPurchaseId() {
		return purchaseId;
	}
	public void setPurchaseId(int purchaseId) {
		this.purchaseId = purchaseId;
	}
	public int getCustomerId() {
		return customerId;
	}
	public void setCustId(int customerId) {
		this.customerId = customerId;
	}
	public int getStaffId() {
		return staffId;
	}
	public void setStaffId(int staffId) {
		this.staffId = staffId;
	}
    // Getters and Setters
/* 
    public Long getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Long purchaseId) {
        this.purchaseId = purchaseId;
    }

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
    */
    public Double getPurchaseTotal() {
        return purchaseTotal;
    }

    public void setPurchaseTotal(double purchaseTotal) {
        this.purchaseTotal = purchaseTotal;
    }


    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getPurchaseStatus() {
        return purchaseStatus;
    }

    public void setPurchaseStatus(String purchaseStatus) {
        this.purchaseStatus = purchaseStatus;
    }

    public List<PurchaseProduct> getPurchaseProducts() {
        return purchaseProducts;
    }

    public void setPurchaseProducts(List<PurchaseProduct> purchaseProducts) {
        this.purchaseProducts = purchaseProducts;
    }


    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

	
}












/* 
package com.heroku.java.model;



import java.sql.Date;

public class Purchase{
	
	private int purchaseId;
	private int customerId;
	private int staffId;
	
	private double purchaseTotal;
	private Date purchaseDate;
    private String purchaseStatus;

	
	public Purchase() {}
	
	public Purchase(int purchaseId,int customerId,int staffId,double purchaseTotal,Date purchaseDate,String ticketType,int ticketQuantity) {
		
		this.purchaseId=purchaseId;
		this.customerId=customerId;
		this.staffId=staffId;
		this.purchaseTotal=purchaseTotal;
		this.purchaseDate=purchaseDate; //boxetakstatus
		
	}
	
	
	
	public int getPurchaseId() {
		return purchaseId;
	}
	public void setPurchaseId(int bookingId) {
		this.purchaseId = bookingId;
	}
	public int getCustomerId() {
		return customerId;
	}
	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}
	public int getStaffId() {
		return staffId;
	}
	public void setStaffId(int staffId) {
		this.staffId = staffId;
	}
	public double getPurchaseTotal() {
		return purchaseTotal;
	}
	public void setPurchaseTotal(double totalPrice) {
		this.purchaseTotal = totalPrice;
	}

	public Date getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public String getPurchaseStatus() {
		return purchaseStatus;
	}

	
} 

*/