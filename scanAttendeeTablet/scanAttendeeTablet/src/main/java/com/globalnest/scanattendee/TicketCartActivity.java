package com.globalnest.scanattendee;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.BlockTicketListController;
import com.globalnest.mvc.BlockTicketResponse;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.mvc.PromoCodeResponseController;
import com.globalnest.mvc.TicketTypeContoller;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.EventObjects;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class TicketCartActivity extends BaseActivity implements OnClickListener {

	TicketCartAdapter t_cart_adapter;
	ListView ticketinfo;
	TextView buynow,btnbuynow_info,txt_afterapplied;
	LinearLayout layout_promocode;
	EditText edt_promocode;
	Button btnapplynow;
	double servicefee=0,total_serviceFee=0,servicetax=0,total_servicetax=0;
	double main_servicefee=0,main_total_serviceFee=0,main_servicetax=0,main_total_servicetax=0
			,main_discountedamount=0,main_totalafterdiscountwithTax=0;
	double main_total = 0,main_sub_total;
	String promocode="";
	EventObjects event_data;
	double total,sub_total,total_amount=0;
	ArrayList<TicketTypeContoller> _ticketcart;
	ArrayList<TicketTypeContoller> _ticketcarttotal;
	ArrayList<BlockTicketResponse> block_tic_res;
	PromoCodeResponseController promoCodeResponseController;
	HashMap<String,ArrayList<BlockTicketListController>> block_ticket_map=new HashMap<String, ArrayList<BlockTicketListController>>();
	ArrayList<BlockTicketListController> ticketname_for_packageitems = new ArrayList<BlockTicketListController>();
	int pos=0,innerpos=0;
	String requestType="";
	ExternalSettings ext_settings;
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.activity_ticket_cart);
		Util.setCustomAlertDialog(TicketCartActivity.this);
		event_data = Util.db.getSelectedEventRecord(checked_in_eventId);
		//Log.i("Ticket Cart Activity", "On creeate" + event_data.Events.Name);
		Intent ticketdata = getIntent();
		_ticketcart = (ArrayList<TicketTypeContoller>) ticketdata.getSerializableExtra("CART TICKETS");
		///String ticid=_ticketcart.get(0).getTicketPoolId();
		//Log.i("Ticket Cart Activity", "On creeate" + _ticketcart.size());
		/*if(isOnline()){
			doRequest();
		}*/
		t_cart_adapter = new TicketCartAdapter();
		//t_cart_adapter = new TicketCartAdapter(_ticketcart);
		ticketinfo.setAdapter(t_cart_adapter);
		getTotalAmount();
		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Util.alert_dialog.dismiss();
			}
		});
		
		/*if(checkedin_event_record.sessiontime == null){
			checkedin_event_record.sessiontime = "10";
		}*/
		if(!Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, "").isEmpty()){
			ext_settings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, ""), ExternalSettings.class);
		}
		if (ext_settings.allow_promocode&&main_total>0) {
				layout_promocode.setVisibility(View.VISIBLE);
			}

		btnapplynow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 promocode=edt_promocode.getText().toString().trim();
				if(NullChecker(promocode).isEmpty()){
					edt_promocode.setError("Please Enter any Promcode!");
					edt_promocode.requestFocus();
				}else {
					requestType=WebServiceUrls.SA_PROMOCODE;
					doRequest();
				}
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public void setCustomContentView(int layout) {
		activity = this;
		View v = inflater.inflate(layout, null);
		linearview.addView(v);
		txt_title.setText("Cart Items");
		img_menu.setImageResource(R.drawable.back_button);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		ticketinfo = (ListView) linearview.findViewById(R.id.lstviewcart_t_data);
		buynow = (TextView) linearview.findViewById(R.id.btnbuynow);
		btnbuynow_info=(TextView) linearview.findViewById(R.id.btnbuynow_info);
		layout_promocode=(LinearLayout) linearview.findViewById(R.id.lay_promocode);
		txt_afterapplied=(TextView) linearview.findViewById(R.id.txt_appliedpromo);
		edt_promocode =(EditText) linearview.findViewById(R.id.edt_promocode);
		btnapplynow =(Button) linearview.findViewById(R.id.btnapplynow);
		back_layout.setOnClickListener(this);
		buynow.setOnClickListener(this);
		if(!isOnline()){
			buynow.setVisibility(View.GONE);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (v == back_layout) {
			finish();
		}else if(v == buynow){
			//getTotalAmount();
			//DecimalFormat df = new DecimalFormat("0.00");

			if(isOnline()){
				doRequest();
			}else{
				AlertDialogCustom dialog = new AlertDialogCustom(TicketCartActivity.this);
				dialog.setParamenters("Warning", "Please check your internet connection.", null, null, 2, true);
				dialog.show();
			}
			
		/*	Intent i=new Intent(TicketCartActivity.this,CollectOrderInfo.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra(Util.CART_TICKETS, _ticketcart);
			i.putExtra(Util.TOTAL, main_total);
			i.putExtra(Util.SERVICE_TAX, main_total_servicetax);
			i.putExtra(Util.SERVICE_FEE,main_total_serviceFee);
			i.putExtra(Util.BLOCK_TICKET_MAP, block_ticket_map);
			i.putExtra(Util.BLOCK_PACKAGE_TICKET, ticketname_for_packageitems);
			//getTotalAmount();
			startActivity(i);*/
		}
	}

	public class TicketCartAdapter extends BaseAdapter {

		TextView t_name, t_price, t_fee, t_qty, t_total,txtcart_t_Tax;
		ImageView img_delete_cart_item;
		/*public TicketCartAdapter(ArrayList<TicketTypeContoller> list){
			_ticketcart=list;
			ticketinfo.setAdapter(t_cart_adapter);
		}*/

		@Override
		public int getCount() {

			return _ticketcart.size();
		}

		@Override
		public Object getItem(int pos) {
			// 
			return _ticketcart.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			// 
			return pos;
		}

		@Override
		public View getView(final int pos, View view, ViewGroup groupview) {

			//Log.i("Ticket Cart Activity", "On creeate"+pos);			
			final View v = inflater.inflate(R.layout.ticket_cart_items, null);
			t_name = (TextView) v.findViewById(R.id.txtcart_t_Name);
			t_price = (TextView) v.findViewById(R.id.txtcart_t_Price);
			t_fee = (TextView) v.findViewById(R.id.txtcart_t_Fee);
			t_qty = (TextView) v.findViewById(R.id.txtcart_t_Qty);
			t_total = (TextView) v.findViewById(R.id.txtcart_t_total);
			txtcart_t_Tax= (TextView) v.findViewById(R.id.txtcart_t_Tax);
			img_delete_cart_item= (ImageView) v.findViewById(R.id.img_delete_cart_item);
			img_delete_cart_item.setFocusable(false);
			img_delete_cart_item.setVisibility(View.GONE);//TODO FOR REFRESH
			t_name.setText(_ticketcart.get(pos).getTicketTypeName());
			t_price.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c
					+String.format( "%.2f",_ticketcart.get(pos).getTicketPrice()));
			t_qty.setText(String.valueOf(_ticketcart.get(pos).getSelectedTickets()));
			servicefee=0;
			servicetax=0;
			sub_total=0;
			_ticketcarttotal=new ArrayList<TicketTypeContoller>();
			if (NullChecker(checkedin_event_record.Events.Accept_Tax_Rate__c).equals("true")&&Util.NullChecker(_ticketcart.get(pos).getIsTicketTaxable()).equalsIgnoreCase("true") && (Util.NullChecker(_ticketcart.get(pos).getTicketPaymentType()).equalsIgnoreCase("Paid") || Util.NullChecker(_ticketcart.get(pos).getTicketPaymentType()).equalsIgnoreCase("Donation"))) {
				if(!event_data.Events.Tax_Rate__c.trim().isEmpty()){
					if(Util.NullChecker(_ticketcart.get(pos).getTicketFeeSetting()).equalsIgnoreCase("true")){
						servicefee = Util.db.getItemFee(checked_in_eventId, _ticketcart.get(pos).getTicketsId()) * Double.parseDouble(_ticketcart.get(pos).getSelectedTickets());
					}
					servicetax= Double.valueOf(event_data.Events.Tax_Rate__c);
					servicetax=(servicetax/100)*(_ticketcart.get(pos).getTicketPrice()+servicefee) * Double.parseDouble(_ticketcart.get(pos).getSelectedTickets());
				}
				txtcart_t_Tax.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+ String.format( "%.2f", servicetax ));
			} else {
				txtcart_t_Tax.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+0.00);
			}

			if (Util.NullChecker(_ticketcart.get(pos).getTicketFeeSetting()).equalsIgnoreCase("true") && (Util.NullChecker(_ticketcart.get(pos).getTicketPaymentType()).equalsIgnoreCase("Paid")
					|| Util.NullChecker(_ticketcart.get(pos).getTicketPaymentType()).equalsIgnoreCase("Donation"))) {
				servicefee = Util.db.getItemFee(checked_in_eventId, _ticketcart.get(pos).getTicketsId()) * Double.parseDouble(_ticketcart.get(pos).getSelectedTickets());
				t_fee.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+String.format( "%.2f",servicefee));
				total_serviceFee=total_serviceFee+servicefee;
			} else {
				t_fee.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+0.00);
				total_serviceFee=0.0;
			}
			_ticketcart.get(pos).setBLFeeAmount(String.valueOf(servicefee));
			if(Integer.parseInt(t_qty.getText().toString())!=0)
			{
				t_total.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+String.format("%.2f",Double.valueOf((_ticketcart.get(pos).getTicketPrice() * Integer.parseInt(_ticketcart.get(pos).getSelectedTickets()))+ servicetax+servicefee)));
				sub_total =(_ticketcart.get(pos).getTicketPrice() * Integer.parseInt(_ticketcart.get(pos).getSelectedTickets()))+ servicetax+servicefee;
				total=total+sub_total;
			}else{
				t_total.setText(Util.nf.format(0.00));
				sub_total=0;
			}
			//Log.i("TicketCartActivity", "Total=" + total);
			img_delete_cart_item.setTag(pos);
			img_delete_cart_item.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(_ticketcart.size()==1)
					{
						total=0.0;
						img_delete_cart_item.setFocusable(false);
						_ticketcart.remove(pos);
						Intent startentent = new Intent(TicketCartActivity.this,ManageTicketActivity.class);
						startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(startentent);
						finish();
						//t_cart_adapter.notifyDataSetChanged();
					}else if(_ticketcart.size()>1){
						total=0.0;
						img_delete_cart_item.setFocusable(false);
						_ticketcart.remove(pos);
						t_cart_adapter.notifyDataSetChanged();
					}else{
						finish();
					}
				}
			});
			/*if(total==0)
			{
				//buynow.setText("");
				buynow.setEnabled(false);
				buynow.setVisibility(View.GONE);
			}else{
				buynow.setEnabled(true);
				buynow.setVisibility(View.VISIBLE);
			}*/
			return v;
		}
	}

	@Override
	public void doRequest() {
		String access_token =  sfdcddetails.token_type +" "+sfdcddetails.access_token;
/*		postdataObject=new HttpPostDataObject(sfdcddetails.instance_url+WebServiceUrls.SA_BLOCKING_TICKETS, null, setJsonArray(), sfdcddetails.token_type, sfdcddetails.access_token, null, TicketCartActivity.this);
		postdataObject.execute();
*/
		if(requestType.equalsIgnoreCase(WebServiceUrls.SA_PROMOCODE)){
			String _url = sfdcddetails.instance_url + WebServiceUrls.SA_PROMOCODE + "&eventid=" + checked_in_eventId+"&isPromo=true&promocode="+edt_promocode.getText().toString();
			postMethod = new HttpPostData("Applying Promocode...", _url, setJsonArray().toString(), access_token, TicketCartActivity.this);
			postMethod.execute();
		}
		else {
			String _url = sfdcddetails.instance_url + WebServiceUrls.SA_BLOCKING_TICKETS + "&sessiontime=" + checkedin_event_record.sessiontime;
			postMethod = new HttpPostData("Checking Tickets Availability...", _url, setJsonArray().toString(), access_token, TicketCartActivity.this);
			postMethod.execute();
		}
	}

	/**
	 * @return
	 */

	private JSONArray setJsonArray() {
		JSONArray ticketArray = new JSONArray();
		try {
			for (int i = 0; i < _ticketcart.size(); i++) {
				JSONObject obj = new JSONObject();
				obj.put("ItemId", _ticketcart.get(i).getTicketsId());
				obj.put("Qty", Integer.valueOf(_ticketcart.get(i).getSelectedTickets()));
				ticketArray.put(obj);
			}
		} catch (JSONException e) {

			e.printStackTrace();
		}
		return ticketArray;
	}

	@Override
	public void parseJsonResponse(String response) {

		String zeroAvailableTicketArray ="";
		String lessAvailableTicketArray="";
		//Log.i("--------TicketCartActivity--------", ":"+response);
		try {
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}
			gson = new Gson();
			if(requestType.equalsIgnoreCase(WebServiceUrls.SA_PROMOCODE)){
				promoCodeResponseController = new Gson().fromJson(response, PromoCodeResponseController.class);
				String c=Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c;
				if(NullChecker(promoCodeResponseController.Msg).equalsIgnoreCase("Success")){
					layout_promocode.setVisibility(View.GONE);
					txt_afterapplied.setTextColor(getResources().getColor(R.color.green_connected));
					txt_afterapplied.setVisibility(View.VISIBLE);
					txt_afterapplied.setText(Html.fromHtml(edt_promocode.getText().toString()+" Promo Code Applied Successfully! " +
							"<font color=#438bca >"+"\n Saved Amount : "+c+promoCodeResponseController.DiscountedAmount+"</font>"));
					/*main_total=(Double.valueOf(Double.valueOf(promoCodeResponseController.ActualAmount)+Double.valueOf(promoCodeResponseController.BlfeeOntop))-Double.valueOf(promoCodeResponseController.DiscountedAmount));
					main_total_serviceFee=Double.valueOf(promoCodeResponseController.BlfeeOntop);
					main_total_servicetax=Double.valueOf(promoCodeResponseController.TaxAmount);*/
					main_discountedamount=Double.valueOf(promoCodeResponseController.DiscountedAmount);
					main_totalafterdiscountwithTax=Double.valueOf(promoCodeResponseController.TotalAmountwithTaxes);
					main_total_servicetax=Double.valueOf(promoCodeResponseController.TaxAmount);
					main_total_serviceFee=Double.valueOf(promoCodeResponseController.BlfeeOntop);
					btnbuynow_info.setText(Html.fromHtml("<font color=#438bca >"+"Sub Total: "+"</b> </font>"+c+String.format("%.2f",main_total)    +
							"<font color=#438bca >"+"     Fee: "+"</font>"+c+String.format("%.2f",main_total_serviceFee) +
							"<font color=#438bca >"+"     Tax: "+"</font>"+c+String.format("%.2f", main_total_servicetax) +
							"<br> <font color=#438bca > <b>"+" Amount Payable: "+"</b> </font>"+c+promoCodeResponseController.TotalAmountwithTaxes));

					//main_total_serviceFee
					/*i.putExtra(Util.TOTAL, main_total);
					i.putExtra(Util.SERVICE_TAX, main_total_servicetax);
					i.putExtra(Util.SERVICE_FEE, main_total_serviceFee);*/
				}else {
					txt_afterapplied.setTextColor(getResources().getColor(R.color.red));
					txt_afterapplied.setText(promoCodeResponseController.Msg+" ("+edt_promocode.getText().toString()+")");
					txt_afterapplied.setVisibility(View.VISIBLE);
				}
				requestType="";
			}else {
				ticketname_for_packageitems.clear();
				java.lang.reflect.Type listType = new TypeToken<ArrayList<BlockTicketResponse>>() {
				}.getType();
				block_tic_res = gson.fromJson(response, listType);

				for (int i = 0; i < block_tic_res.size(); i++) {
					int package_qty = 0;
					pos = i;

					ArrayList<BlockTicketListController> ticketname = new ArrayList<BlockTicketListController>();
					Cursor c = Util.db.getTicketCursor(" where " + DBFeilds.ADDED_ITEM_ID + " = '" + block_tic_res.get(i).ItemId + "'");
					c.moveToFirst();
					String item_type = c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_TYPENAME));
					String attendee_setting = c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_OPTION));
					c.close();
					if (item_type.equalsIgnoreCase("Package")) {

						if (attendee_setting.equalsIgnoreCase(getString(R.string.donotinfofromattendee))) {
							for (int j = 0; j < block_tic_res.get(i).ticketsList.size(); j++) {
								if (block_tic_res.get(i).ticketsList.get(j).Item_Type__r.Name.equalsIgnoreCase("Package") && !TextUtils.isEmpty(block_tic_res.get(i).ticketsList.get(j).Parent_ID__c)) {
									ticketname.add(block_tic_res.get(i).ticketsList.get(j));
								} else {
									package_qty = package_qty + 1;
								}
							}
						} else {
							for (int j = 0; j < block_tic_res.get(i).ticketsList.size(); j++) {
								if (block_tic_res.get(i).ticketsList.get(j).Item_Type__r.Name.equalsIgnoreCase("Package") && TextUtils.isEmpty(block_tic_res.get(i).ticketsList.get(j).Parent_ID__c)) {
									package_qty = package_qty + 1;
									ticketname.add(block_tic_res.get(i).ticketsList.get(j));
								} else {
									ticketname_for_packageitems.add(block_tic_res.get(i).ticketsList.get(j));
								}
							}
						}

					} else {
						for (int j = 0; j < block_tic_res.get(i).ticketsList.size(); j++) {
							ticketname.add(block_tic_res.get(i).ticketsList.get(j));
						}
					}
					block_ticket_map.put(block_tic_res.get(i).ItemId, ticketname);

					if (!item_type.equalsIgnoreCase("Package")) {
					/*for (int k = 0; k < _ticketcart.size(); k++) {
						if (_ticketcart.get(k).getTicketsId().equals(block_tic_res.get(i).ItemId)){
							_ticketcart.get(k).setSelectedTickets(block_tic_res.get(i).tickesAvilable);
						}
					}*/
						if (Integer.parseInt(block_tic_res.get(i).tickesAvilable) == 0) {
							zeroAvailableTicketArray = zeroAvailableTicketArray + ", " + Util.db.getItemName(block_tic_res.get(i).ItemId);
						} else {
							for (int m = 0; m < _ticketcart.size(); m++) {
								if (_ticketcart.get(m).getTicketsId().equals(block_tic_res.get(i).ItemId)) {
									if (Integer.parseInt(block_tic_res.get(i).tickesAvilable) < Integer
											.parseInt(_ticketcart.get(m).getSelectedTickets())) {
										innerpos = m;
										lessAvailableTicketArray = lessAvailableTicketArray + ", "
												+ Util.db.getItemName(block_tic_res.get(i).ItemId);
									}
								}
							}
						}

					} else {
						if (Integer.parseInt(block_tic_res.get(i).tickesAvilable) == 0) {
							zeroAvailableTicketArray = Util.db.getItemName(block_tic_res.get(i).ItemId);

						}
						//_ticketcart.get(i).setSelectedTickets(block_tic_res.get(i).tickesAvilable);
						else {
							for (int m = 0; m < _ticketcart.size(); m++) {
								if (_ticketcart.get(m).getTicketsId().equals(block_tic_res.get(i).ItemId)) {
									if (Integer.parseInt(block_tic_res.get(i).tickesAvilable) < Integer
											.parseInt(_ticketcart.get(m).getSelectedTickets())) {
										innerpos = m;
										lessAvailableTicketArray = lessAvailableTicketArray + ", "
												+ Util.db.getItemName(block_tic_res.get(i).ItemId);
									}
								}
							}
							//	_ticketcart.get(i).setSelectedTickets(String.valueOf(package_qty));
						}
					}
				}

				AlertDialogCustom dialog;
				if (!lessAvailableTicketArray.isEmpty()) {
					String msg = "";
					Util.setCustomAlertDialog(TicketCartActivity.this);
					msg = "Your selected quantity of " + _ticketcart.get(innerpos).getSelectedTickets() + " for " + Util.db.getItemName(block_tic_res.get(pos).ItemId)
							+ " ticket is more than available quantity." + "\n" + "Would you like to decrease the Ticket Quantity to "
							+ block_tic_res.get(pos).tickesAvilable + " and continue the Order";
					Util.alert_dialog.setCancelable(false);
					Util.openCustomDialog("Alert",
							msg);
					Util.txt_okey.setText("Ok");
					Util.txt_dismiss.setVisibility(View.VISIBLE);
					Util.alert_dialog.setCancelable(false);
					Util.txt_okey.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Util.alert_dialog.dismiss();
							for (int m = 0; m < _ticketcart.size(); m++) {
								if (_ticketcart.get(m).getTicketsId().equals(block_tic_res.get(pos).ItemId)) {
									_ticketcart.get(m).setSelectedTickets(block_tic_res.get(pos).tickesAvilable);
									t_cart_adapter.notifyDataSetChanged();
									break;
								}
							}

						}
					});
					Util.txt_dismiss.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Util.alert_dialog.dismiss();

						}
					});
				/*dialog=new AlertDialogCustom(TicketCartActivity.this);
				dialog.setParamenters("Alert", "Your selected quantity of "+ lessAvailableTicketArray +" is more than available quantity.", null, null, 1, false);
				dialog.show();*/
				} else if (!zeroAvailableTicketArray.isEmpty()) {
					String msg = "";
					Util.setCustomAlertDialog(TicketCartActivity.this);
					if (_ticketcart.size() == 1) {
						msg = "Sorry! " + Util.db.getItemName(block_tic_res.get(pos).ItemId) +
								" tickets are not available.";

					} else if (_ticketcart.size() > 1) {
						msg = "Sorry! " + Util.db.getItemName(block_tic_res.get(pos).ItemId) +
								" tickets are not available. Would you like to remove the Ticket and continue the Order";
					}
					Util.alert_dialog.setCancelable(false);
					Util.openCustomDialog("Alert",
							msg);
					Util.txt_okey.setText("Ok");
					Util.txt_dismiss.setVisibility(View.VISIBLE);
					Util.alert_dialog.setCancelable(false);
					Util.txt_okey.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Util.alert_dialog.dismiss();
							if (_ticketcart.size() == 1) {
								Intent startentent = new Intent(TicketCartActivity.this, ManageTicketActivity.class);
								startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(startentent);
								finish();
							} else if (_ticketcart.size() > 1) {
								for (int m = 0; m < _ticketcart.size(); m++) {
									if (_ticketcart.get(m).getTicketsId().equals(block_tic_res.get(pos).ItemId)) {
										_ticketcart.remove(m);
										t_cart_adapter.notifyDataSetChanged();
										break;
									}
								}
								//if (_ticketcart.get(m).getTicketsId().equals(block_tic_res.get(i).ItemId)) {
								/*_*//*ticketcart.indexOf(block_tic_res.get(pos).ItemId);
							_ticketcart.remove(pos);*/

							}

							//finish();
						}
					});
					Util.txt_dismiss.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Util.alert_dialog.dismiss();

						}
					});

				/*dialog=new AlertDialogCustom(TicketCartActivity.this);
				dialog.setParamenters("Alert", "Sorry! +"+zeroAvailableTicketArray +"tickets are not available.", null, null, 1, false);
				//dialog.setParamenters("Alert", "Your selected quantity of "+zeroAvailableTicketArray +" is more than available quantity.", null, null, 1, false);
				dialog.show();*/


				} else if (!lessAvailableTicketArray.isEmpty() && !zeroAvailableTicketArray.isEmpty()) {
					dialog = new AlertDialogCustom(TicketCartActivity.this);
					dialog.setParamenters("Alert", "Your selected quantity of " + lessAvailableTicketArray + " " + zeroAvailableTicketArray + " is more than available quantity.", null, null, 1, false);
					dialog.show();
				} else {
					//getTotalAmount();
					Intent i = new Intent(TicketCartActivity.this, CollectOrderInfo.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					i.putExtra(Util.CART_TICKETS, _ticketcart);
					i.putExtra(Util.ONLY1FORM, false);
					i.putExtra(Util.TOTAL, main_total);
					i.putExtra(Util.SERVICE_TAX, main_total_servicetax);
					i.putExtra(Util.SERVICE_FEE, main_total_serviceFee);
					i.putExtra(Util.PROMOCODE, promocode);
					i.putExtra(Util.DISCOUNTEDVALUE,main_discountedamount);
					i.putExtra(Util.AFTERDISCOUNTTOTALWITHTAX,main_totalafterdiscountwithTax);
					i.putExtra(Util.BLOCK_TICKET_MAP, block_ticket_map);
					i.putExtra(Util.BLOCK_PACKAGE_TICKET, ticketname_for_packageitems);
					//getTotalAmount();
					startActivity(i);
				}
			}

			//getTotalAmount();
			//ticketinfo.setAdapter(t_cart_adapter);
		} catch (Exception e) {
			// TODO: handle exception
			startErrorAnimation(e.getMessage(), txt_error_msg);
		}


	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
		// 
	}
	private void getTotalAmount()
	{
		main_total=0;main_servicefee=0;main_total_serviceFee=0;main_servicetax=0;main_total_servicetax=0;

		for (int i = 0; i < _ticketcart.size(); i++) {
			main_servicefee=0;main_servicetax=0;main_sub_total=0;
			if(Integer.parseInt(_ticketcart.get(i).getSelectedTickets())!=0)
			{
				if (NullChecker(checkedin_event_record.Events.Accept_Tax_Rate__c).equals("true")&&Util.NullChecker(_ticketcart.get(i).getIsTicketTaxable()).equalsIgnoreCase("true") && (Util.NullChecker(_ticketcart.get(i).getTicketPaymentType()).equalsIgnoreCase("Paid") ||
						Util.NullChecker(_ticketcart.get(i).getTicketPaymentType()).equalsIgnoreCase("Donation"))) {
					if(!event_data.Events.Tax_Rate__c.trim().isEmpty()){
						if(Util.NullChecker(_ticketcart.get(i).getTicketFeeSetting()).equalsIgnoreCase("true")){
							main_servicefee = Util.db.getItemFee(checked_in_eventId, _ticketcart.get(i).getTicketsId()) * Double.parseDouble(_ticketcart.get(i).getSelectedTickets());
						}
						main_servicetax= Double.valueOf(event_data.Events.Tax_Rate__c);
						main_servicetax=(main_servicetax/100)*(Integer.parseInt(_ticketcart.get(i).getSelectedTickets())*_ticketcart.get(i).getTicketPrice()+main_servicefee);
					}
					main_total_servicetax=main_total_servicetax+Double.valueOf(String.format("%.2f",main_servicetax));
				} else {
					main_total_servicetax=main_total_servicetax+0.00;
				}

				if (Util.NullChecker(_ticketcart.get(i).getTicketFeeSetting()).equalsIgnoreCase("true") && (Util.NullChecker(_ticketcart.get(i).getTicketPaymentType()).equalsIgnoreCase("Paid")
						|| Util.NullChecker(_ticketcart.get(i).getTicketPaymentType()).equalsIgnoreCase("Donation"))) {
					main_servicefee = Util.db.getItemFee(checked_in_eventId, _ticketcart.get(i).getTicketsId()) * Double.parseDouble(_ticketcart.get(i).getSelectedTickets());
					main_total_serviceFee=main_total_serviceFee+main_servicefee;
				} else {
					main_total_serviceFee=main_total_serviceFee+0.00;
				}
				main_sub_total =(_ticketcart.get(i).getTicketPrice() * Integer.parseInt(_ticketcart.get(i).getSelectedTickets()));
				main_total=main_total+main_sub_total;
			}else{
				main_total=main_total+0.00;
			}
		}
		main_totalafterdiscountwithTax=main_total;
		//btnbuynow_info.setText("Order Total="+String.format("%.2f",main_total+main_total_serviceFee+ main_total_servicetax ));
		String c=Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c;
		btnbuynow_info.setText(Html.fromHtml("<font color=#438bca >"+"Sub Total: "+"</b> </font>"+c+String.format("%.2f",main_total)    +
				"<font color=#438bca >"+"     Fee: "+"</font>"+c+String.format("%.2f",main_total_serviceFee) +
				"<font color=#438bca >"+"     Tax: "+"</font>"+c+String.format("%.2f", main_total_servicetax) +
				"<br> <font color=#438bca > <b>"+" Total Amount: "+"</b> </font>"+c+String.format("%.2f",(main_total+main_total_servicetax+main_total_serviceFee))));
	}


}
