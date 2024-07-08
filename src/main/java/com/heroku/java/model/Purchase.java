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