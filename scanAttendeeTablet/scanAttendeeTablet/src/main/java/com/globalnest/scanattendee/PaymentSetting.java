package com.globalnest.scanattendee;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.globalnest.database.EventPayamentSettings;
import com.globalnest.database.PaymentTypeDBFeilds;
import com.globalnest.mvc.PaytmStatusCheckGson;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.Event;
import com.globalnest.objects.Event.Organaizer;
import com.globalnest.objects.EventObjects;
import com.globalnest.objects.EventPaymentTypes;
import com.globalnest.objects.PaymentGateWays;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

public class PaymentSetting extends BaseActivity {
	// private LinearLayout layout_creditcard;
	public static final int KEY_CODE = 2017;
	public static final String KEYS = "payment_keys";
	private ExpandableListView expand_list_view, expand_eventadmin_payinfo;
	private List<PaymentGateWays> pgateways_list, payment_type_list;
	private List<ExpandClass> expand_list = new ArrayList<ExpandClass>();
	private PGatewayAdapter adapter;
	private int child_selected = -1;
	private Cursor _event_payment_setting, _event_card_pgateway;
	HashMap<String, EventPaymentTypes> _event_pgateway_map = new HashMap<String, EventPaymentTypes>();
	HashMap<String, EventPaymentTypes> _event_card_pgateway_map = new HashMap<String, EventPaymentTypes>();
	private String TAG = "-----Payment Setting Activity------";
	private EventObjects event;
	private Organaizer organaizer;
	private String[] admin_pay_options;
	private AdminPayInfoAdapter admin_adapter;
	// private String Customer_ID;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.payment_settings_new);
		Util.setCustomAlertDialog(PaymentSetting.this);

		if(isEventOrganizer()){
			Util.openCustomDialog("Alert", "As EventOrganizer you don't have access to payment settings please contact EventAdmin");
			Util.txt_okey.setText("Ok");
			Util.txt_dismiss.setVisibility(View.GONE);
			Util.alert_dialog.setCanceledOnTouchOutside(false);
			Util.txt_okey.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Util.alert_dialog.dismiss();
					PaymentSetting.this.finish();
				}
			});
		}
		admin_pay_options = new String[] { getString(R.string.check), getString(R.string.creditcard) };
		admin_adapter = new AdminPayInfoAdapter();
		expand_eventadmin_payinfo.setAdapter(admin_adapter);
		payment_type_list = Util.db.getPGateways("where " + PaymentTypeDBFeilds.PAYMENT_TYPE_DISPLAY + "='Checkbox'");
		pgateways_list = Util.db.getPGateways("where " + PaymentTypeDBFeilds.PAYMENT_TYPE_DISPLAY + "='Radio' AND "+PaymentTypeDBFeilds.PAYMENT_CURRENCY_ID+" = '"+checkedin_event_record.Events.BLN_Currency__c+"'");
		event = Util.db.getSelectedEventRecord(checked_in_eventId);
		organaizer = Util.db.getEventOrganaizerPayInfo(event.Events.organizer_id__c);

		////Log.i("--------------Organaizer Override------------", ":" + organaizer.PG_Credit_Card_Overwrite__c);
		if (organaizer.PG_Credit_Card_Overwrite__c) {
			admin_pay_options = new String[] { getString(R.string.check), getString(R.string.creditcard)
			};
		} else {
			admin_pay_options = new String[] { getString(R.string.creditcard) };
		}

		_event_payment_setting = Util.db.getEvent_Payment_Setting(checked_in_eventId);
		_event_card_pgateway = Util.db.getEvent_Card_PGateway(checked_in_eventId);
		_event_card_pgateway.moveToFirst();

		////Log.i(TAG, "--------------event payment size---------" + _event_payment_setting.getCount());
		////Log.i(TAG, "--------------card payment size---------" + _event_card_pgateway.getCount());

		if (_event_card_pgateway.getCount() > 0) {
			// //Log.i(TAG,
			// " Nmae of card type
			// "+_event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME)));
			if (!_event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME)).isEmpty()) {
				EventPaymentTypes _cardptype = new EventPaymentTypes();
				_cardptype.Events__c = checked_in_eventId;
				_cardptype.Name = _event_card_pgateway
						.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME));
				_cardptype.Pay_Gateway__c = _event_card_pgateway
						.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY_GATEWAY__C));
				_cardptype.Id = _event_card_pgateway
						.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY__ID));
				_cardptype.Pay_Gateway__r.PGateway_Type__c = _event_card_pgateway
						.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PGATEWAY_ID));

				String GatewayName = Util.db.getPayment_Type_Name(_cardptype.Pay_Gateway__r.PGateway_Type__c);

				if (AppUtils.NullChecker(GatewayName).trim().isEmpty()) {
					GatewayName = getString(R.string.none);
				}

				_event_card_pgateway_map.put(GatewayName, _cardptype);
			} else {
				_event_card_pgateway_map.put(getString(R.string.creditcard), new EventPaymentTypes());
				_event_card_pgateway_map.clear();
			}

		}

		if (_event_payment_setting.getCount() > 0) {
			for (int i = 0; i < _event_payment_setting.getCount(); i++) {
				_event_payment_setting.moveToPosition(i);
				EventPaymentTypes _ptype = new EventPaymentTypes();
				_ptype.Events__c = checked_in_eventId;
				_ptype.Name = _event_payment_setting
						.getString(_event_payment_setting.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME));
				_ptype.Pay_Gateway__c = _event_payment_setting
						.getString(_event_payment_setting.getColumnIndex(EventPayamentSettings.EVENT_PAY_GATEWAY__C));
				_ptype.Id = _event_payment_setting
						.getString(_event_payment_setting.getColumnIndex(EventPayamentSettings.EVENT_PAY__ID));
				_event_pgateway_map.put(_ptype.Name, _ptype);
			}
		}

		// RefreshCrediCardUI(_event_pgateway_map, _event_card_pgateway_map);;

		Collections.sort(pgateways_list, new Comparator<PaymentGateWays>() {

			@Override
			public int compare(PaymentGateWays lhs, PaymentGateWays rhs) {
				return lhs.Name.compareToIgnoreCase(rhs.Name);
			}
		});

		//Log.i("--------------Card Number-----------------", ":" + organaizer.PG_Authorization_CC_Last_four_Digit__c);

		if(pgateways_list.size() > 0){
			pgateways_list.add(pgateways_list.size() - 1, pgateways_list.get(0));
			//pgateways_list.remove(0);
			PaymentGateWays p_gateway = new PaymentGateWays();
			p_gateway.Name = "None";
			p_gateway.Id = "0";
			pgateways_list.add(p_gateway);

			pgateways_list.remove(0);
		}

		//	pgateways_list.remove(0);

		PaymentGateWays pgatway = new PaymentGateWays();
		pgatway.Name = getString(R.string.creditcard);
		payment_type_list.add(pgatway);

		Collections.sort(payment_type_list, new Comparator<PaymentGateWays>() {
			@Override
			public int compare(PaymentGateWays lhs, PaymentGateWays rhs) {
				return lhs.Name.compareToIgnoreCase(rhs.Name);
			}
		});

		for (PaymentGateWays pg : payment_type_list) {
			ExpandClass expand = new ExpandClass();
			//Log.i("----------------Payment Gate Way Name-----------", ":" + pg.Name);
			if (pg.Name.equalsIgnoreCase(getString(R.string.creditcard))) {
				expand.payment_type = pg;
				expand.payamen_gateways = pgateways_list;
			} else {
				expand.payment_type = pg;
			}

			if(!pg.Name.equalsIgnoreCase(getString(R.string.free))){
				expand_list.add(expand);
			}
		}
		adapter = new PGatewayAdapter();
		expand_list_view.setAdapter(adapter);

		back_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				if(getIntent().getStringExtra(Util.ADDED_EVENT_ID)==null){
					finish();
				}else{
					Intent i=new Intent(PaymentSetting.this,ManageTicketActivity.class);
					i.putExtra(Util.ADDED_EVENT_ID, getIntent().getStringExtra(Util.ADDED_EVENT_ID));
					i.putExtra(Util.TICKET,true);
					startActivity(i);
					finish();
				}
				//finish();
			}
		});

		expand_list_view.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				CheckBox check_box = (CheckBox) v.findViewById(R.id.checkbox_pyament_type);
				EventPaymentTypes _cardptype = new EventPaymentTypes();
				//PaymentGateWays payment = getGroup(groupPosition);
				String checkboxname=payment_type_list.get(groupPosition).Name;
				if (!checkboxname.equalsIgnoreCase(getString(R.string.creditcard))) {
					if (!check_box.isChecked()) {
						_cardptype.Events__c = checked_in_eventId;
						_cardptype.Pay_Gateway__r.Paygateway_Label__c=check_box.getText().toString();
						_cardptype.Name = checkboxname;
						_event_pgateway_map.put(checkboxname, _cardptype);
					} else {
						// //Log.i(TAG, "------Check box is un checked !EXP");
						_event_pgateway_map.remove(checkboxname);
					}
				}
				// Toast.makeText(getApplicationContext(),
				// Toast.LENGTH_SHORT).show();
				if (!checkboxname.equalsIgnoreCase(getString(R.string.creditcard)))
					expand_list_view.expandGroup(groupPosition);
				adapter.notifyDataSetChanged();

				// RefreshCrediCardUI(_event_pgateway_map,_event_card_pgateway_map);
				return false;
			}
		});

		expand_list_view.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition,
										long id) {
				// select_name = "";
				// isclicked = true;
				//Log.i("----------------Child Postion---------", ":" + getString(R.string.none));
				// _event_pgateway_map.remove("External Pay Gateway");
				child_selected = childPosition;
				PaymentGateWays _child_PGateway = adapter.getChild(groupPosition, childPosition);
				RadioButton selected_radio = (RadioButton) v.findViewById(R.id.radio_btn_pgatway);

				/*if(selected_radio.getText().toString().trim().equalsIgnoreCase(getString(R.string.paytm_direct))){
					openWaringAlert();
				}else*/ if (!selected_radio.getText().toString().trim().equalsIgnoreCase(getString(R.string.none))) {

					Intent i = new Intent(PaymentSetting.this, PGatewayKeyList.class);
					i.putExtra(Util.PAYMENT_SETTING, _event_pgateway_map);
					i.putExtra(Util.PGATEWAY, _child_PGateway);
					startActivityForResult(i, KEY_CODE);

				} else if (selected_radio.getText().toString().trim().equalsIgnoreCase(getString(R.string.none))) {
					_event_card_pgateway_map.put(getString(R.string.creditcard), new EventPaymentTypes());
					_event_card_pgateway_map.clear();

					//child_selected = -1;
				}
				adapter.notifyDataSetChanged();
				expand_list_view.expandGroup(groupPosition);
				// RefreshCrediCardUI(_event_pgateway_map,_event_card_pgateway_map);
				return false;
			}
		});
		btn_save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {

					if (!isOnline()) {
						startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
					} else if (_event_card_pgateway_map.size() == 0 && _event_pgateway_map.size() == 0) {
						Util.openCustomDialog("Alert!", "Payment Setting should not be empty.");
						Util.txt_dismiss.setVisibility(View.GONE);
						Util.txt_okey.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								Util.alert_dialog.dismiss();
							}
						});

					} else if (NullChecker(organaizer.PG_Authorization_Key__c).trim().isEmpty()) {
						Util.openCustomDialog("Alert", "Please provide credit card details for Fee charges. The amount is charged at the end of the event. and you will receive an invoice for the charges.");
						Util.txt_dismiss.setVisibility(View.GONE);
						Util.txt_okey.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								Util.alert_dialog.dismiss();
							}
						});
					} else {
						doRequest();
					}



				} catch (Exception e) {
					e.printStackTrace();
					//Log.i("----------------Save Exception-------------", ":" + e.getMessage());
				}
			}
		});
		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {


				//Log.i("--------------Organaizer ID-------------", ":" + event.Events.organizer_id__c);

/*
				if(getIntent().getStringExtra(Util.ADDED_EVENT_ID)==null && getIntent().getStringExtra(Util.ADDED_EVENT_ID).isEmpty())
					finish();
					else{
						Intent i=new Intent(PaymentSetting.this,ManageTicketActivity.class);
						i.putExtra(Util.TICKET,true);
						startActivity(i);
					}*/

				cancelDialog();
			}
		});



		expand_eventadmin_payinfo.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition,
										long id) {

				organaizer = Util.db.getEventOrganaizerPayInfo(event.Events.organizer_id__c);

				if (admin_pay_options[childPosition].equalsIgnoreCase(getString(R.string.creditcard))) {
					Intent edit_card = new Intent(PaymentSetting.this, EventAdminCreditCard.class);
					edit_card.putExtra(KEYS, organaizer);
					startActivityForResult(edit_card, EventAdminCreditCard.ACTIVITY_CODE);
				} else {
					organaizer.PG_Authorization_Key__c = admin_pay_options[childPosition];
					organaizer.PG_Authorization_Card_Type__c = "";
					organaizer.PG_Authorization_CC_Last_four_Digit__c = "";

				}

				admin_adapter.notifyDataSetChanged();
				return false;
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if(getIntent().getStringExtra(Util.ADDED_EVENT_ID)==null){

				finish();}
			else{
				Intent i=new Intent(PaymentSetting.this,ManageTicketActivity.class);
				i.putExtra(Util.ADDED_EVENT_ID, getIntent().getStringExtra(Util.ADDED_EVENT_ID));
				i.putExtra(Util.TICKET,true);
				startActivity(i);
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void cancelDialog() {
		Util.openCustomDialog("Alert !", "Do you want to save all payment settings ?");
		Util.txt_dismiss.setVisibility(View.VISIBLE);
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Util.alert_dialog.dismiss();
				if(getIntent().getStringExtra(Util.ADDED_EVENT_ID)==null){

					finish();}
				else{
					Intent i=new Intent(PaymentSetting.this,ManageTicketActivity.class);
					i.putExtra(Util.ADDED_EVENT_ID, getIntent().getStringExtra(Util.ADDED_EVENT_ID));
					i.putExtra(Util.TICKET,true);
					startActivity(i);
					finish();
				}

			}
		});
		Util.txt_okey.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Util.alert_dialog.dismiss();
				//Log.i("---------------Payment Settings Alert------------", ":" + _event_card_pgateway_map.size());

				if (!isOnline()) {
					startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
				} else if (_event_card_pgateway_map.size() == 0 && _event_pgateway_map.size() == 0) {
					Util.openCustomDialog("Alert!", "Payment Setting should not be empty.");
					Util.txt_okey.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Util.alert_dialog.dismiss();
						}
					});

				} else if (NullChecker(organaizer.PG_Authorization_Key__c).trim().isEmpty()) {
					Util.openCustomDialog("Alert!", "Please Authorize Your CreditCard to Proceed you forward..!");
					Util.txt_okey.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Util.alert_dialog.dismiss();
						}
					});
				} else {
					doRequest();
				}


			}
		});
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.globalnest.scanattendee.BaseActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Util.setCustomAlertDialog(PaymentSetting.this);


	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.globalnest.scanattendee.BaseActivity#setCustomContentView(int)
	 */
	@Override
	public void setCustomContentView(int layout) {
		activity = this;
		View v = inflater.inflate(layout, null);
		linearview.addView(v);
		img_menu.setImageResource(R.drawable.back_button);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		button_layout.setVisibility(View.VISIBLE);
		event_layout.setVisibility(View.GONE);
		txt_title.setText("Payment Settings");
		img_socket_scanner.setVisibility(View.GONE);
		if(BaseActivity.img_scanner_base != null){
			BaseActivity.img_scanner_base.setVisibility(View.GONE);
		}
		expand_list_view = (ExpandableListView) linearview.findViewById(R.id.expand_payaments);
		expand_eventadmin_payinfo = (ExpandableListView) linearview.findViewById(R.id.expand_eventadmin_payinfo);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {
		String access_token = sfdcddetails.token_type + " " + sfdcddetails.access_token;
		postMethod = new HttpPostData("Saving Payment Info...",setUpdateKeyURL(), setUpdateKeyValues().toString(), access_token,
				PaymentSetting.this);
		postMethod.execute();
	}

	private String setUpdateKeyURL() {
		String cash = "false", check = "false", external = "false",checklbl="" ,cashlbl="", extlbl="";
		for (String key : _event_pgateway_map.keySet()) {
			//Log.i(TAG, " Event map value------>" + key);
			if (key == null) {
				continue;
			} else if (key.equalsIgnoreCase(getString(R.string.check))) {
				if(_event_pgateway_map.get(key).Pay_Gateway__r.Paygateway_Label__c.isEmpty()){
					checklbl=key;
				}else {
					checklbl = _event_pgateway_map.get(key).Pay_Gateway__r.Paygateway_Label__c;
					try {
						checklbl =	URLEncoder.encode(checklbl, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				check = "true";
			} else if (key.equalsIgnoreCase(getString(R.string.cash))) {
				if(_event_pgateway_map.get(key).Pay_Gateway__r.Paygateway_Label__c.isEmpty()) {
					cashlbl=key;
				}else {
					cashlbl = _event_pgateway_map.get(key).Pay_Gateway__r.Paygateway_Label__c;
					try {
						cashlbl =	URLEncoder.encode(cashlbl, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				cash = "true";
			} else if (key.equalsIgnoreCase(getString(R.string.extrn_pay))) {
				if(_event_pgateway_map.get(key).Pay_Gateway__r.Paygateway_Label__c.isEmpty()) {
					extlbl=key;
				}else {
					extlbl = _event_pgateway_map.get(key).Pay_Gateway__r.Paygateway_Label__c;
					try {
						extlbl =	URLEncoder.encode(extlbl, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				external = "true";
			}

		}

		String url = sfdcddetails.instance_url + WebServiceUrls.SA_PAYMENT_SETTINGS;

		if (_event_pgateway_map.containsKey(getString(R.string.check))
				|| _event_pgateway_map.containsKey(getString(R.string.cash))
				|| _event_pgateway_map.containsKey(getString(R.string.extrn_pay))) {

			String last_4_type = "";
			if (!NullChecker(organaizer.PG_Authorization_Key__c).equalsIgnoreCase(getString(R.string.check))) {
				last_4_type = organaizer.PG_Authorization_Card_Type__c.replace(" ", "%20") + ";"
						+ organaizer.PG_Authorization_CC_Last_four_Digit__c.replace(";", "%3B");
			}
			url = url + "Event_Id=" + checked_in_eventId + "&Module=" + "ScanAttendee" + "&Check=" + check + "&Cash="
					+ cash + "&External=" + external +"&checklbl="+checklbl+"&cashlbl="+cashlbl+"&extlbl="+extlbl+"&cashcheckexternalcardcustomerid="
					+ organaizer.PG_Authorization_Key__c + "&cashcheckexternalcarddetails=" + last_4_type
					+ "&RequestFrom=M&AcceptCheckRange=&AcceptCheckRangeMinValue=&AcceptGatewayRange=&AcceptGatewayRangeMinValue=";
			/*url = url + "Event_Id=" + checked_in_eventId + "&Module=" + "ScanAttendee" + "&Check=" + check + "&Cash="
					+ cash + "&External=" + external + "&cashcheckexternalcardcustomerid="
					+ organaizer.PG_Authorization_Key__c + "&cashcheckexternalcarddetails=" + last_4_type
					+ "&RequestFrom=M&AcceptCheckRange=&AcceptCheckRangeMinValue=&AcceptGatewayRange=&AcceptGatewayRangeMinValue=";

		*/} else {
			/*url = url + "Event_Id=" + checked_in_eventId + "&Module=" + "ScanAttendee" + "&Check=" + check + "&Cash="
					+ cash + "&External=" + external + "&RequestFrom=M&AcceptCheckRange=&AcceptCheckRangeMinValue=&AcceptGatewayRange=&AcceptGatewayRangeMinValue=";
		*/
			url = url + "Event_Id=" + checked_in_eventId + "&Module=" + "ScanAttendee" + "&Check=" + check + "&Cash="
					+ cash + "&External=" + external + "&RequestFrom=M&AcceptCheckRange=&AcceptCheckRangeMinValue=&AcceptGatewayRange=&AcceptGatewayRangeMinValue="+"&checklbl="+checklbl+"&cashlbl="+cashlbl+"&extlbl="+extlbl;
		}
		// BLN_ASC_PaymentSettings?Event_Id=a17L0000001WfnNIAS&Module=ScanAttendee&Check=false&Cash=true&External=false
		return url;

	}

	private JSONObject setUpdateKeyValues() {
		JSONObject jobj = null;

		try {
			if (_event_card_pgateway_map.size() > 0) {
				jobj = new JSONObject();
				for (String key : _event_card_pgateway_map.keySet()) {
					if(key == null){
						key = "";
					}

					jobj.put("pgopt", key.split(" ")[0].trim());
					jobj.put("firstparam", _event_card_pgateway_map.get(key).Pay_Gateway__c);


					if (!key.equalsIgnoreCase(getString(R.string.stripe_adaptive)) && !key.equalsIgnoreCase(getString(R.string.paypal_adaptive))) {
						if (key.split(" ").length > 1) {
							jobj.put("secondparam", organaizer.PG_Authorization_Key__c);
							jobj.put("thirdparam", key.split(" ")[1].trim());

							String last_4_type = "";
							if (!NullChecker(organaizer.PG_Authorization_Key__c).equalsIgnoreCase(getString(R.string.check))) {
								last_4_type = organaizer.PG_Authorization_Card_Type__c + ";"
										+ organaizer.PG_Authorization_CC_Last_four_Digit__c;
							}

							// String last_4_type
							// =organaizer.PG_Authorization_Card_Type__c+":"+organaizer.PG_Authorization_CC_Last_four_Digit__c;
							jobj.put("fourthparam", last_4_type);
						}
					}
				}
				return jobj;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (jobj == null) {
			jobj = new JSONObject();
		}
		return jobj;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
	 */
	@Override
	public void parseJsonResponse(String response) {

		gson = new Gson();
		try {
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}else {
				EventPaymentTypes[] update_setting_response = gson.fromJson(response, EventPaymentTypes[].class);
				if (update_setting_response.length > 0) {
					Util.db.delete_Event_PGateway(checked_in_eventId);
					for (EventPaymentTypes _paysetting : update_setting_response) {
						if (!NullChecker(_paysetting.Pay_Gateway__r.PGateway_Type__c).isEmpty()) {
							Util.db.InsertAndUpdateEventPaymentSetting(_paysetting);
							Util.db.InsertAndUpdateOrganizerPayInfo(_paysetting.Events__r.organizer_id__r);
						}
					}
					if (getIntent().getStringExtra(Util.ADDED_EVENT_ID) == null) {
						finish();
					} else {
						Intent i = new Intent(PaymentSetting.this, ManageTicketActivity.class);
						i.putExtra(Util.ADDED_EVENT_ID, getIntent().getStringExtra(Util.ADDED_EVENT_ID));
						i.putExtra(Util.TICKET, true);
						startActivity(i);
						finish();
					}
				} else {
					Util.openCustomDialog("Error!", "No record found");
					Util.txt_okey.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							Util.alert_dialog.dismiss();
						}
					});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
	}

	private class PGatewayAdapter extends BaseExpandableListAdapter {
		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getGroupCount()
		 */
		@Override
		public int getGroupCount() {
			return expand_list.size();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getChildrenCount(int)
		 */
		@Override
		public int getChildrenCount(int groupPosition) {
			return expand_list.get(groupPosition).payamen_gateways.size();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getGroup(int)
		 */
		@Override
		public PaymentGateWays getGroup(int groupPosition) {
			return expand_list.get(groupPosition).payment_type;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getChild(int, int)
		 */
		@Override
		public PaymentGateWays getChild(int groupPosition, int childPosition) {
			return expand_list.get(groupPosition).payamen_gateways.get(childPosition);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getGroupId(int)
		 */
		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getChildId(int, int)
		 */
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#hasStableIds()
		 */
		@Override
		public boolean hasStableIds() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean,
		 * android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

			View v = inflater.inflate(R.layout.expand_group, null);
			try {
				final CheckBox check_box = (CheckBox) v.findViewById(R.id.checkbox_pyament_type);
				ImageView image_expand = (ImageView) v.findViewById(R.id.image_expand);
				image_expand.setVisibility(View.GONE);
				PaymentGateWays payment = getGroup(groupPosition);
				//check_box.setText(payment.Name);
				if (!payment.Name.equalsIgnoreCase(NullChecker(Util.db.getPaymentgatewayLabel(payment.Id)))&&!NullChecker(Util.db.getPaymentgatewayLabel(payment.Id)).isEmpty()) {
					check_box.setText(Util.db.getPaymentgatewayLabel(payment.Id));
				}
				else{
					check_box.setText(payment.Name);
				}
				check_box.setChecked(isExpanded);
				check_box.setTag(payment.Id);
				check_box.setClickable(false);
				check_box.setChecked(false);

				if (payment.Name.equalsIgnoreCase(getString(R.string.creditcard))) {
					image_expand.setVisibility(View.VISIBLE);
					if (child_selected != -1) {
						check_box.setChecked(true);
					}
				}
				if (isExpanded) {
					image_expand.setImageResource(R.drawable.minus);
				} else {
					image_expand.setImageResource(R.drawable.plus);
				}



				if (_event_pgateway_map.size() > 0) {

					for (String key : _event_pgateway_map.keySet()) {
						// //Log.i(TAG,
						// "------Payment Type Name =
						// "+_event_pgateway_map.get(key).Name);

						if (!payment.Name.equalsIgnoreCase(getString(R.string.creditcard))) {
							if (NullChecker(_event_pgateway_map.get(key).Name).equalsIgnoreCase(payment.Name)) {
								check_box.setChecked(true);
							}
						} else {
							if (_event_card_pgateway_map.size()> 0)
								check_box.setChecked(true);
							else
								check_box.setChecked(false);
						}

					}
				}

				if (check_box.getText().toString().equalsIgnoreCase(getString(R.string.creditcard))) {
					if(_event_card_pgateway_map.size() > 0)
						check_box.setChecked(true);
					else
						check_box.setChecked(false);
				}

				if (payment.Name.equalsIgnoreCase(getString(R.string.check))) {
					if (check_box.isChecked()) {
						EventPaymentTypes payment_typegateway = new EventPaymentTypes();
						payment_typegateway.Events__c = checked_in_eventId;
						payment_typegateway.Pay_Gateway__r.Paygateway_Label__c=check_box.getText().toString();
						payment_typegateway.Name = payment.Name;
						_event_pgateway_map.put(payment.Name, payment_typegateway);
						// layout_creditcard.setVisibility(View.VISIBLE);
					} else {
						// layout_creditcard.setVisibility(View.GONE);
						_event_pgateway_map.remove(payment.Name);
					}
				}
				if (payment.Name.equalsIgnoreCase(getString(R.string.cash))) {
					if (check_box.isChecked()) {
						EventPaymentTypes payment_typegateway = new EventPaymentTypes();
						payment_typegateway.Events__c = checked_in_eventId;
						payment_typegateway.Pay_Gateway__r.Paygateway_Label__c=check_box.getText().toString();
						payment_typegateway.Name = payment.Name;
						_event_pgateway_map.put(payment.Name, payment_typegateway);
						// layout_creditcard.setVisibility(View.VISIBLE);
					} else {
						// layout_creditcard.setVisibility(View.GONE);
						_event_pgateway_map.remove(payment.Name);
					}
				}
				if (payment.Name.equalsIgnoreCase(getString(R.string.extrn_pay))) {
					if (check_box.isChecked()) {
						EventPaymentTypes payment_typegateway = new EventPaymentTypes();
						payment_typegateway.Events__c = checked_in_eventId;
						payment_typegateway.Pay_Gateway__r.Paygateway_Label__c=check_box.getText().toString();
						payment_typegateway.Name = payment.Name;
						_event_pgateway_map.put(payment.Name, payment_typegateway);
					} else {
						_event_pgateway_map.remove(payment.Name);
					}
				}


			} catch (Exception e) {
				e.printStackTrace();
			}

			return v;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getChildView(int, int,
		 * boolean, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
								 ViewGroup parent) {

			View v = inflater.inflate(R.layout.expand_child_layout, null);
			final RadioButton radio_btn = (RadioButton) v.findViewById(R.id.radio_btn_pgatway);
			radio_btn.setClickable(false);
			PaymentGateWays payament = null;
			try {
				if (getGroup(groupPosition).Name.equalsIgnoreCase(getString(R.string.creditcard))) {
					payament = getChild(groupPosition, childPosition);
					radio_btn.setText(Html.fromHtml(payament.Name + " <font  color=#337744><i><b>"
							+ payament.Adaptive_Type__c + "</b></i></font>"));
				}

				/*for (String s : _event_card_pgateway_map.keySet()) {
					Log.i("--------------Hash Map Keys------------", ":" + s);
				}*/

				if(_event_card_pgateway_map.size()>0){
					radio_btn.setChecked(_event_card_pgateway_map.containsKey(radio_btn.getText().toString().trim()));
				}else{
					if(radio_btn.getText().toString().trim().equalsIgnoreCase(getString(R.string.none)))
					{
						radio_btn.setChecked(true);
					}
				}

				if (radio_btn.isChecked()) {
					child_selected = childPosition;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return v;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#isChildSelectable(int, int)
		 */
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			//Log.i("----------------Child Postion isChildSelectable---------", ":" + childPosition);
			return true;
		}

	}

	private class ExpandClass {
		public PaymentGateWays payment_type = new PaymentGateWays();
		public List<PaymentGateWays> payamen_gateways = new ArrayList<PaymentGateWays>();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Log.i("---------------onActivity Result------------", ":" + requestCode + " : " + resultCode);
		if (requestCode == KEY_CODE && resultCode == KEY_CODE) {
			EventPaymentTypes _card_type = (EventPaymentTypes) data.getSerializableExtra(KEYS);

			_event_card_pgateway_map.clear();
			_event_card_pgateway_map.put(Util.db.getPayment_Type_Name(_card_type.Pay_Gateway__r.PGateway_Type__c),
					_card_type);
			adapter.notifyDataSetChanged();
		} else if (requestCode == resultCode) {
			// Customer_ID =
			// data.getStringExtra(EventAdminCreditCard.CUSTOMER_KEY);
			organaizer = (Organaizer) data.getSerializableExtra(KEYS);
			// viewCreditCard(organaizer);

			admin_adapter.notifyDataSetChanged();
		}
	}
	private class AdminPayInfoAdapter extends BaseExpandableListAdapter {

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getGroupCount()
		 */
		@Override
		public int getGroupCount() {

			return 1;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getChildrenCount(int)
		 */
		@Override
		public int getChildrenCount(int groupPosition) {

			return admin_pay_options.length;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getGroup(int)
		 */
		@Override
		public Object getGroup(int groupPosition) {

			return null;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getChild(int, int)
		 */
		@Override
		public Object getChild(int groupPosition, int childPosition) {

			return admin_pay_options[childPosition];
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getGroupId(int)
		 */
		@Override
		public long getGroupId(int groupPosition) {

			return 0;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getChildId(int, int)
		 */
		@Override
		public long getChildId(int groupPosition, int childPosition) {

			return 0;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#hasStableIds()
		 */
		@Override
		public boolean hasStableIds() {

			return false;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean,
		 * android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

			View v = inflater.inflate(R.layout.expand_group_admin, null);
			ImageView image_expand = (ImageView) v.findViewById(R.id.image_expand);
			CheckBox check = (CheckBox) v.findViewById(R.id.checkbox_pyament_type);
			check.setText(getString(R.string.admin_payinfo));
			check.setClickable(false);
			check.setChecked(false);
			if (!NullChecker(organaizer.PG_Authorization_Key__c).isEmpty()) {
				check.setChecked(true);
			}

			if (isExpanded) {
				image_expand.setImageResource(R.drawable.minus);
			} else {
				image_expand.setImageResource(R.drawable.plus);
			}

			// To expandable list view show in always expandable to show child
			ExpandableListView mExpandableListView = (ExpandableListView) parent;
			mExpandableListView.expandGroup(groupPosition);
			return v;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#getChildView(int, int,
		 * boolean, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
								 ViewGroup parent) {


			View v = inflater.inflate(R.layout.expand_child_layout, null);
			LinearLayout ll = (LinearLayout)v.findViewById(R.id.ll_child);
			ll.setBackgroundColor(getResources().getColor(R.color.light_gray));
			RadioButton radio_btn = (RadioButton) v.findViewById(R.id.radio_btn_pgatway);
			radio_btn.setChecked(false);
			// radio_btn.setEnabled(false);
			radio_btn.setClickable(false);
			radio_btn.setText(admin_pay_options[childPosition]);
			//Log.i("-----------------Value Of Key------------", ":" + NullChecker(organaizer.PG_Authorization_Key__c));

			if (NullChecker(organaizer.PG_Authorization_Key__c).equalsIgnoreCase(radio_btn.getText().toString().trim())) {
				radio_btn.setChecked(true);
			} else if (NullChecker(organaizer.PG_Authorization_Key__c).contains("cus") && ! ( radio_btn.getText().toString().trim().equalsIgnoreCase("check")) ) {
				radio_btn.setChecked(true);
			} else{
				radio_btn.setChecked(false);
			}
			/*
			 * if(childPosition == 0){
			 * if(organaizer.PG_Authorization_Key__c.equalsIgnoreCase(radio_btn.
			 * getText().toString().trim())){ radio_btn.setChecked(true); }
			 *
			 * }else if(childPosition == 1){
			 * if(organaizer.PG_Authorization_Key__c.contains("cus")){
			 * radio_btn.setChecked(true); } }
			 */
			return v;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ExpandableListAdapter#isChildSelectable(int, int)
		 */
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {

			return true;
		}

	}

	private void openWaringAlert() {
		Util.setCustomAlertDialog(PaymentSetting.this);
		Util.txt_dismiss.setVisibility(View.GONE);
		//Util.setCustomDialogImage(R.drawable.app_icon_1);
		//Util.txt_dismiss.setText("CANCEL");
		Util.txt_okey.setText("OK");
		Util.alert_dialog.setCancelable(false);

		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();

			}
		});

		Util.openCustomDialog("Alert","Intergation will see you soon...!");
	}

}

