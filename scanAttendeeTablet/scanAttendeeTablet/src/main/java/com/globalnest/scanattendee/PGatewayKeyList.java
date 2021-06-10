package com.globalnest.scanattendee;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.globalnest.database.DBFeilds;
import com.globalnest.database.EventPayamentSettings;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.EventPaymentTypes;
import com.globalnest.objects.PaymentGateWays;
import com.globalnest.objects.PaymentType;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PGatewayKeyList extends BaseActivity {

	//private static final int STRIPE_CODE = 2016;

	private String _pgatewayname,where_clause;
	private TextView txt_pgname;
	private ListView pgatewaykey_list;
	private GatewayKeyListAdapter adapter;
	private PaymentGateWays _checked_PGateway;
	boolean is_saved=true;
	//private int saved_valueposition;;

	private String TAG="------------ PGateway Key List----------";
	private List<PaymentType> _paygateway_key_list=new ArrayList<PaymentType>();
	private PaymentType _save_PayGateway,delete_payGatewayId;
	private String requestType="";
	//private String gateway_name="";
	public PaymentType _clicked_pay_type;
	//dialog fields
	private EditText edit_app_id,edit_app_key,edit_key_name,edt_signature,edt_seventh_param,edt_sixth_param,edt_fifth_param,edt_fourth_param,edt_eigth_param;
	private TextView txt_name,txt_app_key,txt_eror,txt_app_id,txt_signature,txt_fourth,txt_fifthh,txt_sixth,txt_seventh,txt_eigth;
	private Dialog add_key_dialog;
	private LinearLayout ll_fourth,ll_fifth,ll_sixth,ll_seventh,ll_eigth;
	private EventPaymentTypes _cardptype=new EventPaymentTypes();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.activity_pgateway_key_list);
		adapter = new GatewayKeyListAdapter();
		where_clause=" where "+ EventPayamentSettings.KEY_GN_USER+" = '"+user_profile.Profile.Id+"' AND "+ EventPayamentSettings.KEY_PGATEWAY_TYPE_ID+" = '"+_checked_PGateway.Id.trim()+"'";

		// where_clause=" where "+EventPayamentSettings.KEY_COMPANY+" = '"+checkedin_event_record.Events.organizer_id__c+"' AND "+EventPayamentSettings.KEY_PGATEWAY_TYPE_ID+" = '"+_checked_PGateway.Id.trim()+"'";
		//where_clause=" where "+EventPayamentSettings.KEY_PGATEWAY_TYPE_ID+" = '"+_checked_PGateway.Id.trim()+"'";
		_paygateway_key_list= Util.db.getPay_Gateway_Key(where_clause);
		Cursor _event_card_pgateway = Util.db.getEvent_Card_PGateway(checked_in_eventId);
		_event_card_pgateway.moveToFirst();
		//Log.i(TAG, "--------------card payment size---------"+	_event_card_pgateway.getCount());
		if(_event_card_pgateway.getCount()>0)
		{
			//Log.i(TAG, " Nmae of card type "+_event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME)));
			_cardptype.Events__c=checked_in_eventId;
			_cardptype.Name=_event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME));
			_cardptype.Pay_Gateway__c=_event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY_GATEWAY__C));
			_cardptype.Id=_event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY__ID));
			_cardptype.Pay_Gateway__r.PGateway_Type__c = _event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PGATEWAY_ID));

		}

		if(_paygateway_key_list == null){
			txt_pgname.setVisibility(View.VISIBLE);
			txt_pgname.setText("Opps! No Keys Found...");
		}else if(_paygateway_key_list.isEmpty()){
			txt_pgname.setVisibility(View.VISIBLE);
			txt_pgname.setText("Opps! No Keys Found...");
		}else{
			txt_pgname.setVisibility(View.GONE);
			pgatewaykey_list.setAdapter(adapter);
		}

		img_addticket.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Util.ACTION= Util.SAVE;
				//Log.i("--------------Payment Gateway name---------------",":"+_pgatewayname);
				if(_pgatewayname.equalsIgnoreCase(getString(R.string.stripe_adaptive))||_pgatewayname.equalsIgnoreCase(getString(R.string.stripe_direct))){
					Intent stripe_login = new Intent(PGatewayKeyList.this,StripeAccountActivity.class);
					startActivityForResult(stripe_login, StripeAccountActivity.STRIPE_CODE);
				}else{
					openAddPaymentKeyDialog(_pgatewayname, null);
					adapter.notifyDataSetChanged();
				}

			}
		});

		pgatewaykey_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {

				_clicked_pay_type=adapter.getItem(position);
				//Log.i("", "--------------Pay Gateway List size---------"+_clicked_pay_type.Id);
				_cardptype.Events__c = checked_in_eventId;
				_cardptype.Name = _clicked_pay_type.Name;
				_cardptype.Pay_Gateway__c = _clicked_pay_type.Id;
				_cardptype.Id = _clicked_pay_type.PGateway_Type__c;
				_cardptype.Pay_Gateway__r.PGateway_Type__c = _clicked_pay_type.PGateway_Type__c;
				sendResponse(_cardptype);
				adapter.notifyDataSetChanged();

				finish();
				/*if (isOnline()) {
					requestType = Util.UPDATE_KEY;
					doRequest();
				} else {
					startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
				}*/

			}
		});

		back_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				sendResponse(_cardptype);
				finish();
			}
		});

	}

	/* (non-Javadoc)
	 * @see com.globalnest.scanattendee.BaseActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Util.setCustomAlertDialog(PGatewayKeyList.this);

		// adapter.notifyDataSetChanged();
	}
	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {

		String access_token = sfdcddetails.token_type+" "+sfdcddetails.access_token;
		if(requestType.equals(Util.SAVE)){
			requestType = Util.SAVE;
/*			_postdataMethod = new HttpPostDataObject(setAddKeyUrl(),
					setAddKeyValues(), null, sfdcddetails.token_type,
					sfdcddetails.access_token, image, PGatewayKeyList.this);
*/
			postMethod = new HttpPostData("Saving Please Wait...",setAddKeyUrl(), setAddKeyValues().toString(), access_token, PGatewayKeyList.this);
			postMethod.execute();

		} else if (requestType.equals(Util.UPDATE_KEY)) {
			/*_postdataMethod = new HttpPostDataObject(setUpdateKeyURL(),
					setUpdateKeyValues(), null, sfdcddetails.token_type,
					sfdcddetails.access_token, null, PGatewayKeyList.this);*/
			postMethod = new HttpPostData("Updating Please Wait...",setUpdateKeyURL(), setUpdateKeyValues().toString(), access_token, PGatewayKeyList.this);
			postMethod.execute();

		} else {
			requestType = Util.DELETE;
			/*_postdataMethod = new HttpPostDataObject(setDeleteURL(), null,
					null, sfdcddetails.token_type, sfdcddetails.access_token,
					null, PGatewayKeyList.this);
			_postdataMethod.execute();*/

			postMethod = new HttpPostData("Deleting Please Wait...",setDeleteURL(), null, access_token, PGatewayKeyList.this);
			postMethod.execute();
		}

	}

	private String setUpdateKeyURL()
	{
		String cash="false",check="false",external="false";
		@SuppressWarnings("unchecked")
		HashMap<String,EventPaymentTypes> _payment_setting= (HashMap<String, EventPaymentTypes>) getIntent().getSerializableExtra(Util.PAYMENT_SETTING);
		for(String key:_payment_setting.keySet()){

			if(key.equalsIgnoreCase(getString(R.string.check))){
				check="true";
			}else if(key.equalsIgnoreCase(getString(R.string.cash))){
				cash="true";
			}else if(key.equalsIgnoreCase(getString(R.string.extrn_pay))){
				external="true";
			}
		}
		return sfdcddetails.instance_url+ WebServiceUrls.SA_PAYMENT_SETTINGS+"Event_Id="+checked_in_eventId+"&Module="+"ScanAttendee"+"&Check="+check+"&Cash="+cash+"&External="+external;
	}
	private JSONObject setUpdateKeyValues()
	{
		JSONObject jobj=new JSONObject();

		try {
			jobj.put("pgopt", _pgatewayname);
			jobj.put("firstparam", _clicked_pay_type.Id);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return jobj;
	}

	private String setDeleteURL()
	{
		return sfdcddetails.instance_url+ WebServiceUrls.SA_DELETE_PAYGATEWAY_KEY+"Paygatewayid="+delete_payGatewayId.Id;
	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
	 */
	@Override
	public void parseJsonResponse(String response) {

		//Log.i("---------------Save Keys Response------------",":"+response);
		//JsonObject

		gson=new Gson();
		try{
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}
			if (requestType.equals(Util.DELETE)) {
				if (response.contains("Success") || response.contains("Error")) {
					String PGatewaywhere = "";
					PGatewaywhere = " where " + EventPayamentSettings.EVENT_ID
							+ " = '" + checked_in_eventId + "' AND "
							+ EventPayamentSettings.EVENT_PAY_GATEWAY__C
							+ " = '" + delete_payGatewayId.Id + "' AND "
							+ EventPayamentSettings.EVENT_PGATEWAY_ID + " = '"
							+ delete_payGatewayId.PGateway_Type__c + "'";

					if (Util.db.isRecordExists(
							DBFeilds.TABLE_EVENT_PAYAMENT_SETTINGS,
							PGatewaywhere)) {
						/*
						 * PGatewaywhere=" where "+EventPayamentSettings.EVENT_ID
						 * +
						 * " = '"+checked_in_eventId+"' AND "+EventPayamentSettings
						 * .EVENT_PAY_GATEWAY__C+" = '"+delete_payGatewayId.Id+
						 * "' AND "
						 * +EventPayamentSettings.EVENT_PGATEWAY_ID+" = '"
						 * +delete_payGatewayId.PGateway_Type__c+"'";
						 */
						Util.db.delete_Event_PayGateway_from_PGateway(PGatewaywhere);
					}

					String where = " where "
							+ EventPayamentSettings.KEY_PAY_GATEWAY_ID + " = '"
							+ delete_payGatewayId.Id + "'";
					Util.db.deletePayGateway_Keys(where);
					where_clause = " where "
							+ EventPayamentSettings.KEY_GN_USER+" = '"+user_profile.Profile.Id
							+ "' AND "
							+ EventPayamentSettings.KEY_PGATEWAY_TYPE_ID
							+ " = '" + _checked_PGateway.Id.trim() + "'";
					_paygateway_key_list.clear();
					_paygateway_key_list = Util.db.getPay_Gateway_Key(where_clause);

					if(_paygateway_key_list == null){
						_paygateway_key_list = new ArrayList<PaymentType>();
					}
					adapter.notifyDataSetChanged();
				}
			} else if (requestType.equals(Util.UPDATE_KEY)) {
				//Log.i(TAG, "----Updated----");

				EventPaymentTypes[] update_setting_response = gson.fromJson(
						response, EventPaymentTypes[].class);

				if (update_setting_response.length > 0) {
					Util.db.delete_Event_PGateway(checked_in_eventId);
					for (EventPaymentTypes _paysetting : update_setting_response) {
						Util.db.InsertAndUpdateEventPaymentSetting(_paysetting);
						Intent i = new Intent(PGatewayKeyList.this,
								PaymentSetting.class);
						i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
					}
				} else {
					Util.openCustomDialog("Alert", "This key is Deleted");
					Util.txt_okey.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Util.alert_dialog.dismiss();
						}
					});
				}

			} else {
				JSONObject obj = new JSONObject(response);
				if (!obj.optString("status").equalsIgnoreCase("Error")) {
					PaymentType pay_type = gson.fromJson(obj.getJSONObject("pg").toString(), PaymentType.class);
					String payment_setting_name = Util.db.getPayment_Type_Name(pay_type.PGateway_Type__c);
					//Log.i(TAG, "----Updated------------------"+response);
					AppUtils.displayLog("-----------------Payment gateway Name----------------",":"+payment_setting_name);
					if (payment_setting_name.equalsIgnoreCase(getString(R.string.stripe_direct))) {
						pay_type.PG_User_Key__c = edit_app_key.getText().toString().trim();
						pay_type.PG_Pass_Secret__c = edit_app_id.getText().toString().trim();
					} else if (payment_setting_name.equalsIgnoreCase(getString(R.string.stripe_adaptive))) {
						pay_type.PG_Pass_Secret__c = edit_app_id.getText().toString().trim();
						pay_type.PG_Signature__c = edit_app_key.getText().toString().trim();
						pay_type.PG_User_Key__c = edt_signature.getText().toString().trim();
					}else if (payment_setting_name.equalsIgnoreCase(getString(R.string.paypal_direct))) {
						pay_type.PG_User_Key__c = edit_app_id.getText().toString().trim();
						pay_type.PG_Signature__c = edt_signature.getText().toString().trim();
						pay_type.PG_Pass_Secret__c = edit_app_key.getText().toString().trim();
					}  else if (payment_setting_name.equalsIgnoreCase(getString(R.string.paytm_direct))) {
						pay_type.PG_Signature__c= edit_app_id.getText().toString().trim();
						pay_type.PG_Pass_Secret__c  = edt_signature.getText().toString().trim();
						pay_type.PG_User_Key__c  = edit_app_key.getText().toString().trim();
						pay_type.PP_Fee_Payer__c  = edt_fourth_param.getText().toString().trim();
						pay_type.PP_Payment_Type__c  = edt_fifth_param.getText().toString().trim();
						pay_type.citrus_param__c  = edt_seventh_param.getText().toString().trim();
						pay_type.Service_Fee__c  = edt_eigth_param.getText().toString().trim();


					}else if (payment_setting_name.equalsIgnoreCase(getString(R.string.citrus_direct))) {

						pay_type.PG_Signature__c= edit_app_id.getText().toString().trim();
						pay_type.PG_Pass_Secret__c  = edit_app_key.getText().toString().trim();
						pay_type.PG_User_Key__c  = edt_signature.getText().toString().trim();
						pay_type.PP_Fee_Payer__c  = edt_fourth_param.getText().toString().trim();
						pay_type.PP_Payment_Type__c  = edt_fifth_param.getText().toString().trim();
						pay_type.citrus_param__c  = edt_sixth_param.getText().toString().trim();
						pay_type.Service_Fee__c  = edt_seventh_param.getText().toString().trim();

					}else {
						pay_type.PG_User_Key__c = edit_app_id.getText().toString().trim();
						pay_type.PG_Signature__c = edit_app_key.getText().toString().trim();
					}
					/*String payment_setting_name = Util.db.getPGateway_Name(pay_type.PGateway_Type__c);
					//Log.i("-----------------Payment gateway Name----------------",":"+payment_setting_name);
					if (payment_setting_name.equalsIgnoreCase(getString(R.string.stripe))) {
						pay_type.PG_User_Key__c = edit_app_id.getText().toString().trim();
						pay_type.PG_Pass_Secret__c = edit_app_key.getText().toString().trim();
					} else if(payment_setting_name.equalsIgnoreCase(getString(R.string.paypal))){
						pay_type.PG_User_Key__c = edit_app_id.getText().toString().trim();
						pay_type.PG_Signature__c = edt_signature.getText().toString().trim();
						pay_type.PG_Pass_Secret__c = edit_app_key.getText().toString().trim();
					}else{
						pay_type.PG_User_Key__c = edit_app_id.getText().toString().trim();
						pay_type.PG_Signature__c = edit_app_key.getText().toString().trim();
					}*/
					pay_type.BLN_GN_User__c = user_profile.Profile.Id;
					Util.db.InsertAndUpdatePay_GAteway_Key(pay_type);
				} else {
					Util.openCustomDialog("Alert!",obj.optString("message"));
					Util.alert_dialog.show();
					Util.txt_okey.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Util.alert_dialog.dismiss();

						}
					});
				}
			}

			where_clause=" where "+ EventPayamentSettings.KEY_GN_USER+" = '"+user_profile.Profile.Id+"' AND "+ EventPayamentSettings.KEY_PGATEWAY_TYPE_ID+" = '"+_checked_PGateway.Id.trim()+"'";

			//Log.i("------------Where Clause------------", ":" + where_clause);
			_paygateway_key_list = Util.db.getPay_Gateway_Key(where_clause);

			if (_paygateway_key_list != null) {
				if (_paygateway_key_list.size() > 0) {
					txt_pgname.setVisibility(View.GONE);
					pgatewaykey_list.setAdapter(adapter);
				}

				else {
					txt_pgname.setVisibility(View.VISIBLE);
					txt_pgname.setText("Opps! On Keys Found...");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			startErrorAnimation(
					getResources().getString(R.string.connection_error),
					txt_error_msg);
		}


	}
	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {


	}
	/* (non-Javadoc)
	 * @see com.globalnest.scanattendee.BaseActivity#setCustomContentView(int)
	 */
	@Override
	public void setCustomContentView(int layout) {

		View v = inflater.inflate(layout, null);
		linearview.addView(v);
		_checked_PGateway=(PaymentGateWays) getIntent().getSerializableExtra(Util.PGATEWAY);
		_pgatewayname=_checked_PGateway.Name+" "+_checked_PGateway.Adaptive_Type__c;
		img_menu.setImageResource(R.drawable.back_button);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		button_layout.setVisibility(View.GONE);
		btn_cancel.setVisibility(View.GONE);
		event_layout.setVisibility(View.VISIBLE);
		txt_title.setText(_pgatewayname + " Keys");
		img_addticket.setVisibility(View.VISIBLE);
		txt_pgname = (TextView) findViewById(R.id.txt_pgatewayname);
		pgatewaykey_list = (ListView) findViewById(R.id.listview_pgatewaykeys);
		Util.setCustomAlertDialog(PGatewayKeyList.this);

		// where_clause=" where "+EventPayamentSettings.KEY_PGATEWAY_TYPE_ID+" = '"+Util.db.getPayment_Type_Id(_pgatewayname)+"' and "+EventPayamentSettings.KEY_PAY_GATEWAY_ID+" = '"+Util.db.getPayment_Type_Id(_pgatewayname)+"'";
	
		/*if(_checked_PGateway.Name.equalsIgnoreCase(getString(R.string.stripe)))
		{
		where_clause=" where "+EventPayamentSettings.KEY_PGATEWAY_TYPE_ID+" = '"+_checked_PGateway.Id+"'";
		}else{
			where_clause=" where "+EventPayamentSettings.KEY_PGATEWAY_TYPE_ID+" = '"+Util.db.getPayment_Type_Id(_pgatewayname)+"'";
		}*/

		//Log.i(TAG, "----PGateway Name----"+_checked_PGateway.Name+"----Pgateway Id----"+_checked_PGateway.Id);

		//EventObjects selected_event=Util.db.getSelectedEventRecord(checked_in_eventId);


		//adapter=new GatewayKeyListAdapter();


	}

	public class GatewayKeyListAdapter extends BaseAdapter
	{
		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return _paygateway_key_list.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public PaymentType getItem(int arg0) {

			return _paygateway_key_list.get(arg0);
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int arg0) {

			return arg0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(final int position, View converterview, ViewGroup arg2) {

			TextView txt_appid,txt_appkey,txt_first,txt_second;
			Button btn_deletekey,btn_edit_pkey;
			RadioButton radio_btn_keyname;

			converterview =inflater.inflate(R.layout.pgateway_list_item, null);
			txt_appid=(TextView) converterview.findViewById(R.id.txt_appid);
			txt_appkey=(TextView) converterview.findViewById(R.id.txt_appkey);
			//txt_keyname=(TextView) converterview.findViewById(R.id.txt_keyname);
			txt_second=(TextView) converterview.findViewById(R.id.txt_secondkey);
			txt_first=(TextView) converterview.findViewById(R.id.txt_firstkey);
			btn_deletekey=(Button) converterview.findViewById(R.id.btn_delete_pkey);
			btn_edit_pkey=(Button) converterview.findViewById(R.id.btn_save_pkey);
			radio_btn_keyname=(RadioButton) converterview.findViewById(R.id.radio_key_name);
			radio_btn_keyname.setClickable(false);

			where_clause=" where "+ EventPayamentSettings.KEY_GN_USER+" = '"+user_profile.Profile.Id+"' AND "+ EventPayamentSettings.KEY_PGATEWAY_TYPE_ID+" = '"+_checked_PGateway.Id.trim()+"'";
			_paygateway_key_list= Util.db.getPay_Gateway_Key(where_clause);
			//Log.i(TAG, "--------------Pay Gateway List size---------"+	_paygateway_key_list.get(position).Id);
				/*Cursor _event_card_pgateway = Util.db.getEvent_Card_PGateway(checked_in_eventId);
				_event_card_pgateway.moveToFirst();
				//Log.i(TAG, "--------------card payment size---------"+	_event_card_pgateway.getCount());
				EventPaymentTypes _cardptype=new EventPaymentTypes();
				if(_event_card_pgateway.getCount()>0)
				{
					//Log.i(TAG, " Nmae of card type "+_event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME)));
					_cardptype.Events__c=checked_in_eventId;
					_cardptype.Name=_event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME));
					_cardptype.Pay_Gateway__c=_event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY_GATEWAY__C));
					_cardptype.Id=_event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY__ID));

				}
				*/
			if(_pgatewayname.equalsIgnoreCase(getString(R.string.paypal_adaptive)))
			{
				txt_appkey.setVisibility(View.GONE);
				txt_second.setVisibility(View.GONE);
				txt_first.setText("PayPal ID");
				if(!_paygateway_key_list.get(position).PG_Email_Id__c.isEmpty())
				{
					txt_appid.setText(_paygateway_key_list.get(position).PG_Email_Id__c);
				}
				if(!_paygateway_key_list.get(position).Paygateway_name__c.isEmpty())
				{
					radio_btn_keyname.setText(_paygateway_key_list.get(position).Paygateway_name__c);
				}

			} else {
				if (!_paygateway_key_list.get(position).Paygateway_name__c.isEmpty()) {
					radio_btn_keyname
							.setText(_paygateway_key_list.get(position).Paygateway_name__c);
				}
				if (!_paygateway_key_list.get(position).PG_User_Key__c
						.isEmpty()) {
					txt_appid
							.setText(_paygateway_key_list.get(position).PG_User_Key__c);
				}
				if (!_paygateway_key_list.get(position).PG_Signature__c
						.isEmpty()) {
					txt_appkey
							.setText(_paygateway_key_list.get(position).PG_Signature__c);
				}

				if (_pgatewayname
						.equalsIgnoreCase(getString(R.string.auth_net_direct))) {
					txt_first.setText("Secret Id");
					txt_second.setText("Secret Key");
				} else if (_pgatewayname
						.equalsIgnoreCase(getString(R.string.trust_commerce_direct))) {
					txt_first.setText("User Name");
					txt_second.setText("Password");
				} else {
					txt_first.setText("Secret Id");
					txt_second.setText("Secret Key");
				}

			}



			if(_paygateway_key_list.get(position).Id.equalsIgnoreCase(_cardptype.Pay_Gateway__c))
			{
				radio_btn_keyname.setSelected(true);
				////Log.i("--------------List Key Name----------------",":"+_paygateway_key_list.get(position).Paygateway_name__c);
				////Log.i("--------------Selected Key Name----------------",":"+_paygateway_key_list.get(position).Id.equalsIgnoreCase(_cardptype.Pay_Gateway__c));
				radio_btn_keyname.setChecked(true);
			}else{
				////Log.i("------------ Else  List Key Name----------------",":"+_paygateway_key_list.get(position).Paygateway_name__c);
				////Log.i("-------------Else Selected Key Name----------------",":"+_paygateway_key_list.get(position).Id.equalsIgnoreCase(_cardptype.Pay_Gateway__c));

				radio_btn_keyname.setSelected(false);
				radio_btn_keyname.setChecked(false);
			}

			btn_deletekey.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					//Log.i(TAG, "   Clicked  "+position+"  Action  = "+Util.ACTION);

					Util.openCustomDialog("Alert", "Do you want to delete that key ?");
					Util.txt_dismiss.setVisibility(View.VISIBLE);
					Util.txt_okey.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {

							delete_payGatewayId=_paygateway_key_list.get(position);
							if(isOnline())
							{
								requestType= Util.DELETE;
								doRequest();
							}else{startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
								Util.alert_dialog.dismiss();
							}
							Util.alert_dialog.dismiss();
						}
					});

					Util.txt_dismiss.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Util.alert_dialog.dismiss();
						}
					});
				}
			});


			btn_edit_pkey.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Util.ACTION= Util.EDIT;
					openAddPaymentKeyDialog(_pgatewayname, _paygateway_key_list.get(position));
				}
			});

			txt_appkey.setVisibility(View.GONE);
			txt_second.setVisibility(View.GONE);
			//edit_app_id.setVisibility(View.GONE);
			//edit_app_key.setVisibility(View.GONE);


			return converterview;
		}

	}

	private void sentRequest()
	{
		if(isOnline()){
			requestType= Util.SAVE;
			doRequest();
		}else{
			startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
		}
	}

	private String setAddKeyUrl()
	{
		setAddKeyValues();

		String _url;
		if(!_save_PayGateway.Id.isEmpty())
			_url= sfdcddetails.instance_url+ WebServiceUrls.SA_ADD_PAYGATEWAY_KEYS+"Event_Id="+checked_in_eventId+"&Paygatewaykeyname="+_save_PayGateway.Paygateway_name__c.replaceAll(" ", "%20")
					+"&Paygatewayid="+_save_PayGateway.Id+"&Module="+"ScanAttendee"+"&userid="+user_profile.Userid;
		else
			_url= sfdcddetails.instance_url+ WebServiceUrls.SA_ADD_PAYGATEWAY_KEYS+"Event_Id="+checked_in_eventId+"&Paygatewaykeyname="+_save_PayGateway.Paygateway_name__c.replaceAll(" ", "%20")
					+"&Module="+"ScanAttendee"+"&userid="+user_profile.Userid;
		////Log.i(TAG, "-------Json request------"+_url);
		return _url;
	}

	private JSONObject setAddKeyValues()
	{
		JSONObject _jobject=new JSONObject();
		try {

			if(_save_PayGateway!=null){

				//saved_Paygatewayid=_paygateway_key_list.get(i).Id;
				if(_pgatewayname.equalsIgnoreCase(getString(R.string.stripe_direct))){
					_jobject.put("pgopt", _checked_PGateway.Name);
					_jobject.put("firstparam", _save_PayGateway.PG_User_Key__c);
					_jobject.put("secondparam",_save_PayGateway.PG_Pass_Secret__c);
					_jobject.put("thirdparam",_checked_PGateway.Adaptive_Type__c);
				}else if(_pgatewayname.equalsIgnoreCase(getString(R.string.paypal_adaptive))){
					_jobject.put("pgopt", _checked_PGateway.Name);
					_jobject.put("firstparam", _save_PayGateway.PG_Email_Id__c);
					_jobject.put("secondparam","");
					_jobject.put("thirdparam", _checked_PGateway.Adaptive_Type__c);
				}else if(_pgatewayname.equalsIgnoreCase(getString(R.string.stripe_adaptive))){
					/*_jobject.put("firstparam", _save_PayGateway.PG_Signature__c);
					_jobject.put("secondparam",_save_PayGateway.PG_User_Key__c);*/
					_jobject.put("pgopt", _checked_PGateway.Name);
					_jobject.put("firstparam", _save_PayGateway.PG_User_Key__c);
					_jobject.put("secondparam",_save_PayGateway.PG_Pass_Secret__c);
					_jobject.put("thirdparam", _checked_PGateway.Adaptive_Type__c);
					_jobject.put("fourthparam", _save_PayGateway.PG_Signature__c);
				}else if(_pgatewayname.equalsIgnoreCase(getString(R.string.auth_net_direct))){
					_jobject.put("pgopt", _checked_PGateway.Name);
					_jobject.put("firstparam", _save_PayGateway.PG_User_Key__c);
					_jobject.put("secondparam",_save_PayGateway.PG_Signature__c);
					_jobject.put("thirdparam",_checked_PGateway.Adaptive_Type__c );
				}else if(_pgatewayname.equalsIgnoreCase(getString(R.string.trust_commerce_direct))){
					_jobject.put("pgopt", _checked_PGateway.Name);
					_jobject.put("firstparam", _save_PayGateway.PG_User_Key__c);
					_jobject.put("secondparam",_save_PayGateway.PG_Signature__c);
					_jobject.put("thirdparam", _checked_PGateway.Adaptive_Type__c);
					_jobject.put("fourthparam", "true");
				}else if(_pgatewayname.equalsIgnoreCase(getString(R.string.paypal_direct))){
					_jobject.put("pgopt", _checked_PGateway.Name);
					_jobject.put("firstparam", _save_PayGateway.PG_User_Key__c);
					_jobject.put("secondparam",_save_PayGateway.PG_Pass_Secret__c);
					_jobject.put("thirdparam", _checked_PGateway.Adaptive_Type__c);
					_jobject.put("fourthparam", _save_PayGateway.PG_Signature__c);

				}else if(_pgatewayname.equalsIgnoreCase(getString(R.string.citrus_direct))){
					_jobject.put("pgopt", _checked_PGateway.Name);
					_jobject.put("firstparam", _save_PayGateway.PG_Signature__c);
					_jobject.put("secondparam",_save_PayGateway.PG_Pass_Secret__c);
					_jobject.put("thirdparam", _checked_PGateway.Adaptive_Type__c);
					_jobject.put("fourthparam", _save_PayGateway.PG_User_Key__c);
					_jobject.put("fifthparam", _save_PayGateway.PP_Fee_Payer__c+"##~~"+_save_PayGateway.PP_Payment_Type__c);
					_jobject.put("sixthparam", _save_PayGateway.citrus_param__c+"##~~"+_save_PayGateway.Service_Fee__c);

				}else if(_pgatewayname.equalsIgnoreCase(getString(R.string.paytm_direct))){
					_jobject.put("pgopt", _checked_PGateway.Name);
					_jobject.put("firstparam", _save_PayGateway.PG_Signature__c);
					_jobject.put("secondparam",_save_PayGateway.PG_User_Key__c);
					_jobject.put("thirdparam", _checked_PGateway.Adaptive_Type__c);
					_jobject.put("fourthparam", _save_PayGateway.PG_Pass_Secret__c);
					_jobject.put("fifthparam", _save_PayGateway.PP_Fee_Payer__c);
					_jobject.put("sixthparam", _save_PayGateway.PP_Payment_Type__c+"##~~"+false+"##~~"+_save_PayGateway.citrus_param__c+"##~~"+_save_PayGateway.Service_Fee__c);// false need to check

				}
			}

		}catch (JSONException e) {
			e.printStackTrace();
		}

		////Log.i(TAG, "-------Json request------"+_jobject.toString());
		return _jobject;
	}

	private void openAddPaymentKeyDialog(final String _pgateway_name ,final PaymentType _payType)
	{
		// create a Dialog component
		//add_key_dialog = new Dialog(PGatewayKeyList.this);
		add_key_dialog = new Dialog(PGatewayKeyList.this, R.style.DialogBottomSlideAnim);
		add_key_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		add_key_dialog.setCancelable(false);
		add_key_dialog.setContentView(R.layout.add_payment_key_dialog);


		edit_key_name=(EditText) add_key_dialog.findViewById(R.id.edt_key_name);
		edit_app_id=(EditText) add_key_dialog.findViewById(R.id.edt_first_param);
		edit_app_key=(EditText) add_key_dialog.findViewById(R.id.edt_second_param);
		edt_signature = (EditText)add_key_dialog.findViewById(R.id.edt_third_param);
		edt_seventh_param = (EditText)add_key_dialog.findViewById(R.id.edt_seventh_param);
		edt_fifth_param = (EditText)add_key_dialog.findViewById(R.id.edt_fifth_param);
		edt_fourth_param = (EditText)add_key_dialog.findViewById(R.id.edt_fourth_param);
		edt_sixth_param = (EditText)add_key_dialog.findViewById(R.id.edt_sixth_param);
		edt_eigth_param = (EditText)add_key_dialog.findViewById(R.id.edt_eigth_param);

		ll_fourth = (LinearLayout)add_key_dialog.findViewById(R.id.ll_fourth);
		ll_fifth = (LinearLayout)add_key_dialog.findViewById(R.id.ll_fifth);
		ll_sixth = (LinearLayout)add_key_dialog.findViewById(R.id.ll_sixth);
		ll_seventh = (LinearLayout)add_key_dialog.findViewById(R.id.ll_seventh);
		ll_eigth  = (LinearLayout)add_key_dialog.findViewById(R.id.ll_eigth);

		txt_name=(TextView) add_key_dialog.findViewById(R.id.txt_pkeyname);
		txt_app_id=(TextView) add_key_dialog.findViewById(R.id.txt_first);
		txt_app_key=(TextView) add_key_dialog.findViewById(R.id.txt_second);
		txt_signature = (TextView)add_key_dialog.findViewById(R.id.txt_third);
		txt_fifthh = (TextView)add_key_dialog.findViewById(R.id.txt_fifth);
		txt_fourth = (TextView)add_key_dialog.findViewById(R.id.txt_fourth);
		txt_sixth = (TextView)add_key_dialog.findViewById(R.id.txt_sixth);
		txt_seventh = (TextView)add_key_dialog.findViewById(R.id.txt_seventh);
		txt_eigth = (TextView)add_key_dialog.findViewById(R.id.txt_eigth);

		txt_eror=(TextView) add_key_dialog.findViewById(R.id.txt_PG_error);
		Button btn_dialog_save = (Button) add_key_dialog.findViewById(R.id.btn_dialog_save);
		Button btn_dialog_cancle = (Button) add_key_dialog.findViewById(R.id.btn_dialog_cancle);

		//Log.i("-------------Payment Type------------",":"+_checked_PGateway.Adaptive_Type__c);
		if(_pgateway_name.equalsIgnoreCase(getString(R.string.paypal_adaptive))){
			edit_app_key.setVisibility(View.GONE);
			txt_app_key.setVisibility(View.GONE);
			edt_signature.setVisibility(View.GONE);
			txt_signature.setVisibility(View.GONE);
			edit_app_id.setHint("PayPal Id");
			txt_app_id.setText("PayPal ID");
			edit_app_id.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.paypal_direct))){
			txt_app_id.setText("User Name");
			txt_app_key.setText("Password");
			txt_signature.setText("Signature");
			edit_app_id.setHint("Api User Name");
			edit_app_key.setHint("Api Password");
			edt_signature.setHint("Signature");

		}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.stripe_direct))){
			txt_app_id.setText("Secret");
			txt_app_key.setText("Publish");
			edit_app_id.setHint("Secret Key");
			edit_app_key.setHint("Publish Key");
			edt_signature.setVisibility(View.GONE);
			txt_signature.setVisibility(View.GONE);
		}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.stripe_adaptive))){

			txt_app_key.setText("Refresh");
			txt_app_id.setText("Access");
			edit_app_key.setHint("Refresh Token");
			edit_app_id.setHint("Access Token");

			edt_signature.setVisibility(View.GONE);
			txt_signature.setVisibility(View.GONE);

		}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.auth_net_direct))){
			txt_app_id.setText("API ID");
			txt_app_key.setText("Key");
			edit_app_id.setHint("API Login Id");
			edit_app_key.setHint("Transaction Key");

			edt_signature.setVisibility(View.GONE);
			txt_signature.setVisibility(View.GONE);
		}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.trust_commerce_direct))){
			txt_app_id.setText("User Name");
			txt_app_key.setText("Password");
			edit_app_id.setHint("User Name");
			edit_app_key.setHint("Password");
			edt_signature.setVisibility(View.GONE);
			txt_signature.setVisibility(View.GONE);
		}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.citrus_direct))){

			txt_app_id.setText("Vanity Url");
			edit_app_id.setHint("Vanity Url");
			txt_app_key.setText("Secret Key");
			edit_app_key.setHint("Secret Key");
			txt_signature.setText("Access Key");
			edt_signature.setHint("Access Key");
			ll_fourth.setVisibility(View.VISIBLE);
			ll_fifth.setVisibility(View.VISIBLE);
			ll_sixth.setVisibility(View.VISIBLE);
			ll_seventh.setVisibility(View.VISIBLE);


		}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.paytm_direct))){

			txt_app_id.setText("Website for WEB");
			edit_app_id.setHint("Website for WEB");
			txt_app_key.setText("Merchant ID");
			edit_app_key.setHint("Merchant ID");
			txt_signature.setText("Merchant Key");
			edt_signature.setHint("Merchant Key");
			txt_fourth.setText("Industry Type Id");
			edt_fourth_param.setHint("Industry Type Id");
			txt_fifthh.setText("Channel ID");
			edt_fifth_param.setHint("Channel ID for WEB");
			txt_sixth.setText("Use PayTM Wallet");
			edt_sixth_param.setHint("Use PayTM Wallet");
			txt_seventh.setText("Generate Checksum");
			edt_seventh_param.setHint("Generate Checksum URL");
			txt_eigth.setText("Verify Checksum");
			edt_eigth_param.setHint("Verify Checksum URL");
			ll_fourth.setVisibility(View.VISIBLE);
			ll_fifth.setVisibility(View.VISIBLE);
			//ll_sixth.setVisibility(View.VISIBLE);
			ll_seventh.setVisibility(View.VISIBLE);
			ll_eigth.setVisibility(View.VISIBLE);

		}

		if(Util.ACTION.equalsIgnoreCase(Util.EDIT))
		{
			txt_name.setText("Edit Key");
			if (_pgateway_name.equalsIgnoreCase(getString(R.string.paypal_adaptive))) {
				txt_app_id.setText("PayPal ID");
				edit_app_id.setHint("PayPal Id");
				edit_app_id.setText(_payType.PG_Email_Id__c);
				edit_key_name.setText(_payType.Paygateway_name__c);
			}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.paypal_direct))){
				txt_app_id.setText("User Name");
				txt_app_key.setText("Password");
				txt_signature.setText("Signature");
				edit_app_id.setText(_payType.PG_User_Key__c);
				edit_app_key.setText(_payType.PG_Pass_Secret__c);
				edit_key_name.setText(_payType.Paygateway_name__c);
				edt_signature.setText(_payType.PG_Signature__c);
			}else if (_pgateway_name.equalsIgnoreCase(getString(R.string.stripe_direct))) {
				txt_app_id.setText("Secret");
				txt_app_key.setText("Publish");
				edit_app_id.setText(_payType.PG_Pass_Secret__c);
				edit_app_key.setText(_payType.PG_User_Key__c);
				edit_key_name.setText(_payType.Paygateway_name__c);
                edit_app_id.setEnabled(false);
                edit_app_key.setEnabled(false);
               // edit_key_name.setEnabled(false);
			} else if(_pgateway_name.equalsIgnoreCase(getString(R.string.stripe_adaptive))){
				edit_app_id.setText(_payType.PG_Pass_Secret__c);
				edit_app_key.setText(_payType.PG_Signature__c);
				edit_key_name.setText(_payType.Paygateway_name__c);
				edt_signature.setText(_payType.PG_User_Key__c);//publishable key
                edit_app_id.setEnabled(false);
                edit_app_key.setEnabled(false);
                //edit_key_name.setEnabled(false);
                edt_signature.setEnabled(false);
			}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.auth_net_direct))){
				txt_app_id.setText("API Id");
				txt_app_key.setText("Key");
				edit_app_id.setText(_payType.PG_User_Key__c);
				edit_app_key.setText(_payType.PG_Signature__c);
				edit_key_name.setText(_payType.Paygateway_name__c);
			}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.trust_commerce_direct))){
				txt_app_id.setText("User Name");
				txt_app_key.setText("Password");
				edit_app_id.setText(_payType.PG_User_Key__c);
				edit_app_key.setText(_payType.PG_Signature__c);
				edit_key_name.setText(_payType.Paygateway_name__c);
			}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.citrus_direct))){

				ll_fourth.setVisibility(View.VISIBLE);
				ll_fifth.setVisibility(View.VISIBLE);
				ll_sixth.setVisibility(View.VISIBLE);
				ll_seventh.setVisibility(View.VISIBLE);

				edit_key_name.setText(_payType.Paygateway_name__c);
				txt_app_id.setText("Vanity Url");
				edit_app_id.setText(_payType.PG_Signature__c);
				txt_app_key.setText("Secret Key");
				edit_app_key.setText(_payType.PG_Pass_Secret__c);
				txt_signature.setText("Access Key");
				edt_signature.setText(_payType.PG_User_Key__c);
				edt_fourth_param.setText(_payType.PP_Fee_Payer__c);
				edt_fifth_param.setText(_payType.PP_Payment_Type__c);
				edt_sixth_param.setText(_payType.citrus_param__c);
				edt_seventh_param.setText(_payType.Service_Fee__c);
			}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.paytm_direct))){

				ll_fourth.setVisibility(View.VISIBLE);
				ll_fifth.setVisibility(View.VISIBLE);
				//ll_sixth.setVisibility(View.VISIBLE);
				ll_seventh.setVisibility(View.VISIBLE);
				ll_eigth.setVisibility(View.VISIBLE);

				edit_key_name.setText(_payType.Paygateway_name__c);

				txt_app_id.setText("Website for WEB");
				edit_app_id.setText(_payType.PG_Signature__c);
				txt_app_key.setText("Merchant ID");
				edt_signature.setText(_payType.PG_Pass_Secret__c);
				txt_signature.setText("Merchant Key");
				edit_app_key.setText(_payType.PG_User_Key__c);
				txt_fourth.setText("Industry Type Id");
				edt_fourth_param.setText(_payType.PP_Fee_Payer__c);
				txt_fifthh.setText("Channel ID for WEB");
				edt_fifth_param.setText(_payType.PP_Payment_Type__c);
				//txt_sixth.setText("Use PayTM Wallet");

				txt_seventh.setText("Generate Checksum");
				edt_seventh_param.setText(_payType.citrus_param__c);
				txt_eigth.setText("Verify Checksum");
				edt_eigth_param.setText(_payType.Service_Fee__c);
			}
		}else{
			txt_name.setText("Add Key");
		}

		btn_dialog_save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {


				if(edit_key_name.getText().toString().trim().isEmpty())
				{
					txt_eror.setVisibility(View.VISIBLE);
					txt_eror.setText("Key Name should not be empty");
				}
				else if(!_pgateway_name.equalsIgnoreCase(getString(R.string.paypal)) && edit_app_key.getText().toString().trim().isEmpty())
				{

					txt_eror.setVisibility(View.VISIBLE);
					txt_eror.setText("App key should not be empty");

				}else if(edit_app_id.getText().toString().trim().isEmpty())
				{
					txt_eror.setVisibility(View.VISIBLE);
					txt_eror.setText("API ID should not be empty");
				}
				else{
					txt_eror.setVisibility(View.GONE);
					_save_PayGateway=new PaymentType();
					_save_PayGateway.Paygateway_name__c=edit_key_name.getText().toString();

					if (_pgateway_name.equalsIgnoreCase(getString(R.string.paypal_direct))) {
						_save_PayGateway.PG_User_Key__c = edit_app_id.getText().toString().trim();
						_save_PayGateway.PG_Pass_Secret__c = edit_app_key.getText().toString().trim();
						_save_PayGateway.PG_Signature__c = edt_signature.getText().toString().trim();
					}else if (_pgateway_name.equalsIgnoreCase(getString(R.string.paypal_adaptive))) {
						_save_PayGateway.PG_Email_Id__c = edit_app_id.getText().toString().trim();
					}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.stripe_direct))){
						_save_PayGateway.PG_Pass_Secret__c= edit_app_id.getText().toString().trim();
						_save_PayGateway.PG_User_Key__c  = edit_app_key.getText().toString().trim();
					}
					else if(_pgateway_name.equalsIgnoreCase(getString(R.string.stripe_adaptive))){
						_save_PayGateway.PG_Pass_Secret__c= edit_app_id.getText().toString().trim();
						_save_PayGateway.PG_Signature__c  = edit_app_key.getText().toString().trim();
						_save_PayGateway.PG_User_Key__c = edt_signature.getText().toString().trim();//publishable key
					}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.citrus_direct))){

						_save_PayGateway.PG_Signature__c= edit_app_id.getText().toString().trim();
						_save_PayGateway.PG_Pass_Secret__c  = edit_app_key.getText().toString().trim();
						_save_PayGateway.PG_User_Key__c  = edt_signature.getText().toString().trim();
						_save_PayGateway.PP_Fee_Payer__c  = edt_fourth_param.getText().toString().trim();
						_save_PayGateway.PP_Payment_Type__c  = edt_fifth_param.getText().toString().trim();
						_save_PayGateway.citrus_param__c  = edt_sixth_param.getText().toString().trim();
						_save_PayGateway.Service_Fee__c  = edt_seventh_param.getText().toString().trim();

					}else if(_pgateway_name.equalsIgnoreCase(getString(R.string.paytm_direct))){
						/*_save_PayGateway.PG_Signature__c= edit_app_id.getText().toString().trim();
						_save_PayGateway.PG_Pass_Secret__c  = edit_app_key.getText().toString().trim();
						_save_PayGateway.PG_User_Key__c  = edt_signature.getText().toString().trim();
						_save_PayGateway.PP_Fee_Payer__c  = edt_fourth_param.getText().toString().trim();
						_save_PayGateway.PP_Payment_Type__c  = edt_fifth_param.getText().toString().trim();
						_save_PayGateway.citrus_param__c  = edt_seventh_param.getText().toString().trim();
						_save_PayGateway.Service_Fee__c  = edt_eigth_param.getText().toString().trim();*/
						_save_PayGateway.PG_Signature__c= edit_app_id.getText().toString().trim();
						_save_PayGateway.PG_Pass_Secret__c  = edt_signature.getText().toString().trim();
						_save_PayGateway.PG_User_Key__c  = edit_app_key.getText().toString().trim();
						_save_PayGateway.PP_Fee_Payer__c  = edt_fourth_param.getText().toString().trim();
						_save_PayGateway.PP_Payment_Type__c  = edt_fifth_param.getText().toString().trim();
						_save_PayGateway.citrus_param__c  = edt_seventh_param.getText().toString().trim();
						_save_PayGateway.Service_Fee__c  = edt_eigth_param.getText().toString().trim();
					}else {
						_save_PayGateway.PG_User_Key__c = edit_app_id.getText().toString().trim();
						_save_PayGateway.PG_Signature__c = edit_app_key.getText().toString().trim();
						//_save_PayGateway.PG_Signature__c = edt_signature.getText().toString().trim();
					}
					if(Util.ACTION.equalsIgnoreCase(Util.EDIT))
						_save_PayGateway.Id=_payType.Id;
					else
						_save_PayGateway.Id="";
					add_key_dialog.dismiss();
					sentRequest();

				}
			}
		});

		btn_dialog_cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				add_key_dialog.dismiss();
			}
		});

		add_key_dialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Log.i("---------------onActivity Result------------",":"+requestCode+" : "+resultCode);
		if(requestCode == StripeAccountActivity.STRIPE_CODE && resultCode == StripeAccountActivity.STRIPE_CODE){
			getStripeAdaptiveKeys(data.getStringExtra(StripeAccountActivity.STRIPE_RESPONSE));
			//Util.ACTION = Util.EDIT;
			//openAddPaymentKeyDialog(_pgatewayname, payment_type);
		}
	}

	private PaymentType getStripeAdaptiveKeys(String response){
		PaymentType paymeny_type = new PaymentType();
		try {
			JSONObject object = new JSONObject(response);
			if(_pgatewayname.equalsIgnoreCase(getString(R.string.stripe_adaptive))) {
				paymeny_type.PG_Pass_Secret__c= object.optString("access_token");
				paymeny_type.PG_User_Key__c  = object.optString("stripe_publishable_key");
				paymeny_type.PG_Signature__c = object.optString("refresh_token");
				/*paymeny_type.PG_User_Key__c = object.optString("access_token");
				paymeny_type.PG_Signature__c = object.optString("refresh_token");*/
			}else {
				paymeny_type.PG_Pass_Secret__c = object.optString("access_token");
				paymeny_type.PG_User_Key__c = object.optString("stripe_publishable_key");
			}
			Util.ACTION = Util.EDIT;
			openAddPaymentKeyDialog(_pgatewayname, paymeny_type);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return paymeny_type;
	}

	private void sendResponse(EventPaymentTypes card_type){
		Intent result = new Intent();
		result.putExtra(PaymentSetting.KEYS, card_type);
		setResult(PaymentSetting.KEY_CODE, result);
	}

}
