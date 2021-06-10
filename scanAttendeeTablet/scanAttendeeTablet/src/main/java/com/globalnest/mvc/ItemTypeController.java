package com.globalnest.mvc;

import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.Bitmap;

public class ItemTypeController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String total_item = "", itemName = "", itemId = "", eventId,
			itemSelected = "", server_item_id = "", isTaxable = "",
			image_url = "", item_pool_id = "",item_type_name="",item_fee_setting="",bl_fee="",
			item_paid_type = "",collectinfo_settings="",item_type_id="";

	double itemPrice = 0;
	int itemQty = 0, soldItems = 0;
	private ArrayList<OrderItemListHandler> orderline_items;
	private double salexTax;
	private Bitmap image_data = null;

	public void setItemServerId(String id) {
		this.server_item_id = id;
	}

	public String getItemServerId() {
		return server_item_id;
	}

	public void setItemEventId(String id) {
		this.eventId = id;
	}

	public String getItemEventId() {
		return eventId;
	}

	public void setIsItemTaxable(String tax) {
		this.isTaxable = tax;
	}

	public String getIsItemTaxable() {
		return isTaxable;
	}

	public void setSoldItems(int sold) {
		this.soldItems = sold;
	}

	public int getSoldItems() {
		return soldItems;
	}

	public void setNoofItems(String number) {
		this.total_item = number;
	}

	public String getNoofItems() {
		return total_item;
	}

	public void setSelectedItems(String purchaseitem) {
		this.itemSelected = purchaseitem;
	}

	public String getSelectedItems() {
		return itemSelected;
	}

	public void setItemName(String name) {
		this.itemName = name;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemQuantity(int qty) {
		this.itemQty = qty;
	}

	public int getItemQuantity() {
		return itemQty;
	}

	public void setItemPrice(double price) {
		this.itemPrice = price;
	}

	public double getItemPrice() {
		return itemPrice;
	}

	public void setItemId(String id) {
		this.itemId = id;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemImage(Bitmap image) {
		this.image_data = image;
	}

	public Bitmap getItemImage() {
		return image_data;
	}

	public void setItemSalexTax(double tax) {
		this.salexTax = tax;
	}

	public double getItemSalexTax() {
		return salexTax;
	}

	public void setItemImageUrl(String url) {
		this.image_url = url;
	}

	public String getItemImageUrl() {
		return image_url;
	}

	public void setItemPoolId(String item_pool_id) {
		this.item_pool_id = item_pool_id;
	}
	
	public void setItemTypeName(String item_type){
		this.item_type_name = item_type;
	}
	public String getItemTypeName(){
		return item_type_name;
	}

	public String getItemPoolId() {
		return item_pool_id;
	}
	
	public void setItemFeeSetting(String fee_setting){
		this.item_fee_setting = fee_setting;
	}
	public String getItemFeeSetting(){
		return item_fee_setting;
	}

	public void setBL_Fee(String bl_fee){
		this.bl_fee = bl_fee;
	}
	public String getBL_Fee(){
		return bl_fee;
	}
	
	public void setItemPaidType(String paid_type){
		this.item_paid_type = paid_type;
	}
	public String getItemPaidType(){
		return item_paid_type;
	}
	
	public void setItemTypeId(String item_type_id){
		this.item_type_id = item_type_id;
	}
	public String getItemTypeId(){
		return item_type_id;
	}
	
	public void setOrderLineItems(ArrayList<OrderItemListHandler> order_items) {
		this.orderline_items = order_items;
	}

	public ArrayList<OrderItemListHandler> getOrderLineItems() {
		return orderline_items;
	}

}
