//  ScanAttendee Android
//  Created by Ajay
//  This class is used for to edit an attendee info and saved it on the backend
//  Copyright (c) 2014 Globalnest. All rights reserved
package com.globalnest.scanattendee;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brother.ptouch.sdk.NetPrinter;
import com.brother.ptouch.sdk.Printer;
import com.globalnest.brother.ptouch.sdk.printdemo.common.Common;
import com.globalnest.brother.ptouch.sdk.printdemo.printprocess.ImagePrint;
import com.globalnest.classes.QRCodeEncoder;
import com.globalnest.cropimage.CropImage;
import com.globalnest.cropimage.CropUtil;
import com.globalnest.data.Contents;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.BadgeCreation;
import com.globalnest.mvc.BadgeDataNew;
import com.globalnest.mvc.BadgeResponseNew;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.mvc.OfflineSyncResController;
import com.globalnest.mvc.TStatus;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.network.HttpClientClass;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.SafeAsyncTask;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.RegistrationSettingsController;
import com.globalnest.objects.ScannedItems;
import com.globalnest.patternedEditText.FloatingHintEditTextLayout;
import com.globalnest.printer.PrinterDetails;
import com.globalnest.printer.ZebraPrinter;
import com.globalnest.retrofit.rest.ApiClient;
import com.globalnest.retrofit.rest.ApiInterface;
import com.globalnest.scanattendee.ExpandablePanel.OnExpandListener;
import com.globalnest.stripe.android.compat.AsyncTask;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("NewApi")
public class AttendeeDetailActivity extends BaseActivity implements
		OnClickListener, AnimationListener {
	boolean isprinterconnected = false;
	Cursor att;
	String att_name = "", att_lname = "", att_email = "", att_work_address1 = "",
			att_work_address2 = "", att_work_city = "", att_work_country = "",
			att_job_title = "", att_work_state = "", att_work_zipcode = "", att_company = "",
			att_work_mobile = "", att_paymentStatus = "", att_ticket_type = "",
			att_seats = "", eventId = "", attendee_category = "",
			att_note = "", att_badge_lable = "", att_tag = "",
			whereClause = "", requestType = "", att_home_phone = "", att_home_add1 = "",
			att_home_add2 = "", att_home_city = "", att_home_state = "", att_home_country = "",
			att_home_zipcode = "", att_mobile = "", att_custombarcode = "";
	SearchThread searchPrinter;
	private Dialog add_key_dialog;
	private AlertDialog.Builder print_dialog;
	EditText fname, lname, company, city, zipCode, email, edit_add1,
			edit_add2, edit_jobtitle, edit_att_category, edit_tag,
			edit_badgeLabel, edit_note, edt_home_add1, edt_home_add2, edt_home_city, edt_home_zip, edt_custombarcode;
	//EditText s_firstname,s_lastname,s_company,s_emailid,s_badgelabel,s_jobtitle,s_workphno,s_phonenumber;
	ImageView img_attendee;
	PaymentModeAdapter paymentModeAdapter;
	Cursor payment_modes;
	// String back_type="";
	EditText edt_work_number, edt_home_number, edt_mobile;
	FloatingHintEditTextLayout homephone_layout, workphone_layout;
	boolean back_type = false, saveAndPrint = false, isPrinted = false;
	ArrayList<String> tagArray = new ArrayList<String>();
	Bitmap attendee_photo;
	Spinner state_spinner, country_spinner, state_home_spnr, country_home_spnr;
	// HorizontalListView tag_listview ;
	ArrayAdapter<String> mAdapter;
	ExpandablePanel extand_att_address, expand_home_address;
	LinearLayout profile_detail_layout, att_detail_layout;
	FrameLayout print_badge, frame_transparentbadge, frame_barcode;
	TextView txt_att_cat, txt_att_date, txt_checkin_date,
			txt_info_header, txt_address_header, txt_home_address_header, txt_seat_value, ticket_name,
			ticket_qty, ticket_price, ticket_fee, txt_badgestatus,
			txtcheckinhistory, txt_order_name, //txt_order_type,status,//txt_seat,
			txt_order_status, txt_note;
	ListView payment_modeslist;
	Button btn_expand, btn_seat_cancel, btn_seat_done, btn_barcode;
	Cursor payment_cursor;
	Cursor t_cursor, updated_badge1, address_cursor1, tagCursor;
	ArrayAdapter<String> work_state_adapter, home_state_adapter, countryadapter;
	int select = 0;
	private String _check_no = "", _check_status = "", payment_id = "";
	LayoutParams lp;
	boolean isExapnd, isReasonEmpty = false;
	EditText edit_seatno;
	InputMethodManager inputMethodManager;
	String attendee_id = "", event_id = "", order_id = "", reason = "",
			tags = "";
	ArrayList<String> mFiles = new ArrayList<String>();
	BadgeCreation badge_crator;
	ArrayList<BadgeResponseNew> badge_res = new ArrayList<BadgeResponseNew>();
	String session_id;
	private ProgressDialog dialog;
	private ProgressDialog progressDialog;
	private ExternalSettings ext_settings;
	private TotalOrderListHandler totalorderlisthandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent attendee_intent = getIntent();
		setCustomContentView(R.layout.attendee_detail_layout);
		setCustumViewData();
		if (fromprintsucess) {
			doRequest();
			requestType = Util.UPDATE_PRINTSTATUS;
		}
		getExternalSettings();
		dialog = new ProgressDialog(AttendeeDetailActivity.this);

		inputMethodManager = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();

		width = display.getWidth();// point.x;
		height = display.getHeight();

		badge_crator = new BadgeCreation(this, width, height);


		txtcheckinhistory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Intent i = new Intent(AttendeeDetailActivity.this,
						CheckinHistory.class);
				i.putExtra(Util.TICKET_ID, payment_cursor
						.getString(payment_cursor
								.getColumnIndex(DBFeilds.ATTENDEE_ID)));
				i.putExtra(
						Util.ATTENDEE_NAME,
						payment_cursor.getString(payment_cursor
								.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
								+ " "
								+ payment_cursor.getString(payment_cursor
								.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));
				//i.putExtra(Util.INTENT_KEY_1,payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)));
				i.putExtra(Util.INTENT_KEY_2, getIntent().getStringExtra(Util.INTENT_KEY_2));
				startActivity(i);

			}
		});

		country_spinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0,
											   View arg1, int arg2, long arg3) {

						String country = (String) country_spinner
								.getSelectedItem();
						setStateAdapter(country);

					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});

		country_home_spnr.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0,
									   View arg1, int arg2, long arg3) {

				String country = (String) country_home_spnr.getSelectedItem();
				setHomeStateAdapter(country);

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		extand_att_address
				.setOnExpandListener(new ExpandablePanel.OnExpandListener() {
					public void onCollapse(View handle, View content) {

						txt_address_header
								.setCompoundDrawablesWithIntrinsicBounds(0,
										0, R.drawable.plus_small, 0);
						// extand_panel.setCollapsedHeight(100);
					}

					public void onExpand(View handle, View content) {
						txt_address_header
								.setCompoundDrawablesWithIntrinsicBounds(0,
										0, R.drawable.minus_small, 0);
					}
				});

		expand_home_address.setOnExpandListener(new OnExpandListener() {

			@Override
			public void onExpand(View handle, View content) {
				// TODO Auto-generated method stub
				txt_home_address_header
						.setCompoundDrawablesWithIntrinsicBounds(0,
								0, R.drawable.minus_small, 0);
			}

			@Override
			public void onCollapse(View handle, View content) {
				// TODO Auto-generated method stub
				txt_home_address_header
						.setCompoundDrawablesWithIntrinsicBounds(0,
								0, R.drawable.plus_small, 0);
			}
		});

		btn_barcode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(AppUtils.isCamPermissionGranted(AttendeeDetailActivity.this)) {
					Intent i = new Intent(AttendeeDetailActivity.this, BarCodeScanActivity.class);
					i.putExtra(Util.INTENT_KEY_1, AttendeeDetailActivity.class.getName());
					/*i.putExtra("EVENT_ID", payment_cursor.getString(payment_cursor.getColumnIndex("Event_Id")));
					i.putExtra("ATTENDEE_ID", payment_cursor.getString(payment_cursor.getColumnIndex("Attendee_Id")));
					i.putExtra("ORDER_ID", payment_cursor.getString(payment_cursor.getColumnIndex("Order_Id")));*/
					//payment_cursor.close();
					startActivityForResult(i, 100);
				}else {
					AppUtils.giveCampermission(AttendeeDetailActivity.this);
				}
			}
		});


		txt_print.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					/*if (!Util.dashboardHandler.availableScanAttendeeTicket) {
						Util.setCustomAlertDialog(AttendeeDetailActivity.this);
						Util.openCustomDialog("Alert", Util.NOPRINTPERMISSION);
						Util.txt_okey.setText("OK");
						Util.txt_dismiss.setVisibility(View.GONE);
						Util.txt_okey.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								Util.alert_dialog.dismiss();
							}
						});
					} else {*/
						txt_print.setText("Printing....");
						//printClicked();
						if (AppUtils.isStoragePermissionGranted(AttendeeDetailActivity.this)) {
							if (Util.NullChecker(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID))).isEmpty()) {
								if (AppUtils.isStoragePermissionGranted(AttendeeDetailActivity.this)) {
									if (isEdited() && !isValidate_badge_reg_settings) {
										saveAndPrint = true;
										openSaveandPrintDialog();
									} else {
										printAttendeeRequest();
									}
								} else {
									AppUtils.giveStoragermission(AttendeeDetailActivity.this);
								}

							} else {
								String mergeparentname = Util.db.getAttendeeBadgeParentTicketName(Util.NullChecker(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID))));
								openBadgeParentalert("Printing is disabled!" + " \n Your Ticket " + payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)) + " was merged with " + mergeparentname);
							}
						} else {
							AppUtils.giveStoragermission(AttendeeDetailActivity.this);
						}

					//}
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		});

		txt_save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideSoftKeyboard(AttendeeDetailActivity.this);
				isPrinted = false;
				saveAndPrint = false;
				//Log.i("-----------------Is Valid Attendee--------",":"+isValidAttendeeRecord());
				if (isEdited()) {
					if (isValidAttendeeRecord()) {
						saveAttendeeRequest();
					}
				} else {
					Toast.makeText(AttendeeDetailActivity.this, "No changes Updated!", Toast.LENGTH_LONG).show();
				}
			}
		});
			/*Util.txt_okey.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Util.alert_dialog.dismiss();
				}
			});
			Util.txtalertcancle.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Util.alert_dialog.dismiss();
				}
			});*/
		back_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (back_type) {
					print_badge.setVisibility(View.GONE);
					frame_transparentbadge.setVisibility(View.GONE);
					btn_save.setVisibility(View.GONE);
					event_layout.setVisibility(View.VISIBLE);
					back_type = false;
					Intent i = new Intent();
					setResult(2017, i);
					finish();

				} else {
					Intent i = new Intent();
					setResult(2017, i);
					finish();
				}
			}
		});
		hideSoftKeyboard(AttendeeDetailActivity.this);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.globalnest.scanattendee.BaseActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		try {
			hideSoftKeyboard(AttendeeDetailActivity.this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openSaveandPrintDialog() {

		try {
			isReasonEmpty = false;
			print_dialog = new AlertDialog.Builder(AttendeeDetailActivity.this);
			LayoutInflater li = LayoutInflater.from(AttendeeDetailActivity.this);
			View promptsView = li.inflate(R.layout.print_dialog_layout, null);
			print_dialog.setView(promptsView);
			final EditText edit_reason = (EditText) promptsView.findViewById(R.id.edit_reason);
			final TextView txt_message = (TextView) promptsView.findViewById(R.id.txt_message);
			print_dialog
					.setCancelable(true)
					.setPositiveButton("Print",
							//.setPositiveButton("Save & Print",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									if (isOnline()) {
										if (isValidAttendeeRecord()) {
											saveAttendeeRequest();
											dialog.dismiss();
										}
									} else {
										startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
									}


								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									dialog.dismiss();
								}
							});/*.setNeutralButton("Print", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (isOnline()) {
						executePrinterStatusTask();
						dialog.dismiss();
					} else {
						startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
					}

				}
			});*/

			// create alert dialog
			final AlertDialog alertDialog = print_dialog.create();

			alertDialog.show();

			txt_message.setVisibility(View.VISIBLE);
			edit_reason.setVisibility(View.GONE);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView()
				.getWindowToken(), 0);

		super.onPause();
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onStop() {
		super.onStop();

	}

/*	private final BroadcastReceiver newItemsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent.getAction().equalsIgnoreCase(
						ScannerSettingsApplication.NOTIFY_DATA_ARRIVAL)) {
					// _symbologyname=intent.getStringExtra(ScannerSettingsApplication.EXTRA_SYMBOLOGY_NAME);
					char[] data = intent
							.getCharArrayExtra(ScannerSettingsApplication.EXTRA_DECODEDDATA);
					Intent i = new Intent(AttendeeDetailActivity.this,
							GlobalScanActivity.class);
					i.putExtra(Util.SCANDATA, data);
					startActivity(i);
				} else if (intent.getAction().equalsIgnoreCase(
						ScannerSettingsApplication.NOTIFY_SCANNER_REMOVAL)) {
					// openAlertDialog("Unpaired Successfully with Socket 7xi.","error",
					// OrderDetailsActivity.this);
					Util.openCustomDialog("Alert",
							"Unpaired Successfully with Socket 7xi. ");
					AlertDialogCustom dialog=new AlertDialogCustom(AttendeeDetailActivity.this);
					dialog.setParamenters("Alert","Unpaired Successfully with Socket 7xi. ", null, null, 1, false);
					dialog.show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};*/

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			/*Intent i = new Intent(AttendeeDetailActivity.this,	AttendeeListActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);*/
			Intent i = new Intent();
			setResult(2017, i);
			finish();

			return true;
		}
		return false;
	}

	@Override
	public void setCustomContentView(int layout) {
		try {
			activity = this;
			View v = inflater.inflate(layout, null);
			linearview.addView(v);
			img_socket_scanner.setVisibility(View.GONE);
			img_scanner_base.setVisibility(View.GONE);
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			txt_title.setText("Attendee");
			img_setting.setVisibility(View.GONE);
			img_menu.setImageResource(R.drawable.back_button);
			event_layout.setVisibility(View.GONE);
			button_layout.setVisibility(View.GONE);
			event_layout.setVisibility(View.VISIBLE);
			txt_save.setVisibility(View.VISIBLE);
			txt_save.setOnClickListener(this);


			// ======================================= Layout Fields// ==========================================//
			print_badge = (FrameLayout) linearview
					.findViewById(R.id.frame_attdetailqrcodebadge);
			frame_transparentbadge = (FrameLayout) linearview
					.findViewById(R.id.frame_transparentbadge);
			extand_att_address = (ExpandablePanel) linearview
					.findViewById(R.id.attendee_address_panel);
			expand_home_address = (ExpandablePanel) linearview.findViewById(R.id.attendee_home_address_panel);
			edit_seatno = (EditText) linearview.findViewById(R.id.seatvalue);

			// ====================================== EditText Fields
			// ========================================//
			img_attendee = (ImageView) linearview.findViewById(R.id.attendeedetailpic);
			fname = (EditText) linearview.findViewById(R.id.attfname);
			lname = (EditText) linearview.findViewById(R.id.attlname);
			email = (EditText) linearview.findViewById(R.id.attemail);
			edt_work_number = (EditText) linearview.findViewById(R.id.attphone);
			edt_home_number = (EditText) linearview.findViewById(R.id.att_home_phone);
			homephone_layout = (FloatingHintEditTextLayout) linearview.findViewById(R.id.homephone_layout);
			workphone_layout = (FloatingHintEditTextLayout) linearview.findViewById(R.id.workphone_layout);

			edt_mobile = (EditText) linearview.findViewById(R.id.att_mobile);
			edit_add1 = (EditText) linearview.findViewById(R.id.att_address1);
			edit_add2 = (EditText) linearview.findViewById(R.id.att_address2);
			company = (EditText) linearview.findViewById(R.id.attcomp);
			edit_jobtitle = (EditText) linearview.findViewById(R.id.attjobtitle);
			edit_tag = (EditText) linearview.findViewById(R.id.atttag);
			edit_badgeLabel = (EditText) linearview
					.findViewById(R.id.attbadgeLabel);
			edit_note = (EditText) linearview.findViewById(R.id.edt_attreg_note);
			// tag_listview = (HorizontalListView)
			// findViewById(R.id.listView_atttag);
			// listview.setAdapter(mAdapter)
			city = (EditText) linearview.findViewById(R.id.attcity);
			zipCode = (EditText) linearview.findViewById(R.id.attzipcode);
			edit_att_category = (EditText) linearview
					.findViewById(R.id.attcategoryvalue);
			edt_home_add1 = (EditText) linearview.findViewById(R.id.att_home_address1);
			edt_home_add2 = (EditText) linearview.findViewById(R.id.att_home_address2);
			edt_home_city = (EditText) linearview.findViewById(R.id.att_home_city);
			edt_home_zip = (EditText) linearview.findViewById(R.id.att_home_zipcode);
			edt_custombarcode = (EditText) linearview.findViewById(R.id.attCustomBarcode);
			frame_barcode = (FrameLayout) linearview.findViewById(R.id.frame_barcode);
			btn_barcode = (Button) linearview.findViewById(R.id.btn_barcode);
			frame_barcode.setVisibility(View.GONE);
			edt_custombarcode.setVisibility(View.GONE);

			Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
			ExternalSettings ext_settings = new ExternalSettings();
			if (!Util.external_setting_pref.getString(sfdcddetails.user_id + checked_in_eventId, "").isEmpty()) {
				ext_settings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id + checked_in_eventId, ""), ExternalSettings.class);
			}
			if (ext_settings.custom_barcode) {
				frame_barcode.setVisibility(View.VISIBLE);
				edt_custombarcode.setVisibility(View.VISIBLE);
			}
			// ====================================== Spinner Fields
			// ========================================//

			country_spinner = (Spinner) linearview.findViewById(R.id.attcountry);
			state_spinner = (Spinner) linearview.findViewById(R.id.attstate);
			state_home_spnr = (Spinner) linearview.findViewById(R.id.att_home_state);
			country_home_spnr = (Spinner) linearview.findViewById(R.id.att_home_country);
			// ====================================== TextView Fields
			// ========================================//

			txt_address_header = (TextView) linearview
					.findViewById(R.id.txt_address_header);
			txt_home_address_header = (TextView) linearview.findViewById(R.id.txt_home_address_header);
			txt_info_header = (TextView) linearview
					.findViewById(R.id.txt_info_header);
			txt_att_cat = (TextView) linearview.findViewById(R.id.txtattcategory);
			txt_att_date = (TextView) linearview.findViewById(R.id.txtcheckindate);
			txt_checkin_date = (TextView) linearview
					.findViewById(R.id.txtcheckinvalue);
			txtcheckinhistory = (TextView) linearview
					.findViewById(R.id.txtcheckinhistory);
			//txt_seat = (TextView) linearview.findViewById(R.id.txtseat);
			ticket_name = (TextView) linearview.findViewById(R.id.attTicketName);
			ticket_qty = (TextView) linearview.findViewById(R.id.attTicketQty);
			ticket_price = (TextView) linearview.findViewById(R.id.attTicketPrice);
			//ticket_fee = (TextView) linearview.findViewById(R.id.attTicketfee);
			txt_badgestatus = (TextView) linearview
					.findViewById(R.id.txtbadgestatus);
		/*txt_order_status = (TextView) linearview
				.findViewById(R.id.txt_order_status_value);
				status = (TextView) linearview.findViewById(R.id.attTicketStatus);*/
			txt_order_name = (TextView) linearview
					.findViewById(R.id.txt_order_name_value);
			payment_modeslist = (ListView) linearview.findViewById(R.id.payment_modeslist);
			/*txt_order_type = (TextView) linearview
					.findViewById(R.id.txt_order_type_value);*/
			txt_note = (TextView) linearview.findViewById(R.id.txt_att_note);

			// ==================================== Setting TypeFace
			// ==========================================//

			edit_att_category.setTypeface(Util.roboto_regular);
			txt_att_cat.setTypeface(Util.roboto_regular);
			txt_att_date.setTypeface(Util.roboto_regular);
			txt_checkin_date.setTypeface(Util.roboto_regular);
			//txt_seat.setTypeface(Util.roboto_regular);
			fname.setTypeface(Util.roboto_regular);
			lname.setTypeface(Util.roboto_regular);
			email.setTypeface(Util.roboto_regular);
			edt_work_number.setTypeface(Util.roboto_regular);
			edit_add1.setTypeface(Util.roboto_regular);
			edit_add2.setTypeface(Util.roboto_regular);
			company.setTypeface(Util.roboto_regular);
			edit_jobtitle.setTypeface(Util.roboto_regular);
			city.setTypeface(Util.roboto_regular);
			zipCode.setTypeface(Util.roboto_regular);
			txt_print.setTypeface(Util.roboto_bold);
			txt_print.setVisibility(View.VISIBLE);
			fname.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					if (motionEvent.getAction() == MotionEvent.ACTION_UP){
						if (motionEvent.getX()>(view.getWidth()-50)){
							fname.setText("");
						}
					}
					return false;
				}
			});
			lname.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					if (motionEvent.getAction() == MotionEvent.ACTION_UP){
						if (motionEvent.getX()>(view.getWidth()-50)){
							lname.setText("");
						}
					}
					return false;
				}
			});
			company.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					if (motionEvent.getAction() == MotionEvent.ACTION_UP){
						if (motionEvent.getX()>(view.getWidth()-50)){
							company.setText("");
						}
					}
					return false;
				}
			});
			email.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					if (motionEvent.getAction() == MotionEvent.ACTION_UP){
						if (motionEvent.getX()>(view.getWidth()-50)){
							email.setText("");
						}
					}
					return false;
				}
			});
			edit_jobtitle.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					if (motionEvent.getAction() == MotionEvent.ACTION_UP){
						if (motionEvent.getX()>(view.getWidth()-50)){
							edit_jobtitle.setText("");
						}
					}
					return false;
				}
			});
			edt_mobile.setInputType(InputType.TYPE_CLASS_NUMBER);
			edt_work_number.setInputType(InputType.TYPE_CLASS_NUMBER);
			edt_home_number.setInputType(InputType.TYPE_CLASS_NUMBER);
			edt_mobile.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					if (motionEvent.getAction() == MotionEvent.ACTION_UP){
						if (motionEvent.getX()>(view.getWidth()-50)){
							edt_mobile.setText("");
						}
					}
					return false;
				}
			});
			edt_work_number.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					if (motionEvent.getAction() == MotionEvent.ACTION_UP){
						if (motionEvent.getX()>(view.getWidth()-50)){
							edt_work_number.setText("");
						}
					}
					return false;
				}
			});
			edt_home_number.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					if (motionEvent.getAction() == MotionEvent.ACTION_UP){
						if (motionEvent.getX()>(view.getWidth()-50)){
							edt_work_number.setText("");
						}
					}
					return false;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setCustumViewData() {
		try {

			Intent attendee_intent = getIntent();
			attendee_id = attendee_intent.getStringExtra("ATTENDEE_ID");
			event_id = attendee_intent.getStringExtra("EVENT_ID");
			order_id = attendee_intent.getStringExtra("ORDER_ID");
			att = Util.db.getBadgeableTicketOrderDetails(order_id);
			//badge_id= attendee_intent.getStringExtra("BADGE_ID");
			whereClause = " where " + DBFeilds.ATTENDEE_EVENT_ID + " = '"
					+ checked_in_eventId + "'" + " AND " + DBFeilds.ATTENDEE_ID
					+ " = " + "'" + attendee_id + "'" + " AND "
					+ DBFeilds.ATTENDEE_ORDER_ID + " = " + "'" + order_id + "'";
			payment_cursor = Util.db.getAttendeeDetails(whereClause);
			payment_cursor.moveToFirst();
		/*address_cursor = Util.db.getAttendeeAddress(" where "
				+ AddressDBFeilds.COMPANY_ID
				+ " = '"
				+ payment_cursor.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_COMPANY_ID)) + "'");
		if (address_cursor.getCount() > 0)
			address_cursor.moveToFirst();*/

			if (!Util.NullChecker(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE))).equalsIgnoreCase("B - Badge")) {
				txt_print.setVisibility(View.GONE);
			}


			String item_id = payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_ID));
			//Log.i("--------------Item Id---------------",":"+item_id);
			/*ArrayList<RegistrationSettingsController> reg_setting_list = Util.db.getRegSettingsList("where " + DBFeilds.REG_ITEM_ID + " = '" + item_id + "'");
			//Log.i("--------------Item Id---------------",":"+reg_setting_list.size());
			for (RegistrationSettingsController each_setting : reg_setting_list) {
				//Log.i("--------------Item Id---------------",":"+reg_setting_list.size());
				if (each_setting.Defaullt_Label__c.equalsIgnoreCase(getString(R.string.work_address)) && Boolean.valueOf(each_setting.Included__c)) {
					extand_att_address.setVisibility(View.VISIBLE);
				} else if (each_setting.Defaullt_Label__c.equalsIgnoreCase(getString(R.string.home_address)) && Boolean.valueOf(each_setting.Included__c)) {
					expand_home_address.setVisibility(View.VISIBLE);
				} else if (each_setting.Defaullt_Label__c.equalsIgnoreCase(getString(R.string.work_pone_number)) && !Boolean.valueOf(each_setting.Included__c)) {
					workphone_layout.setVisibility(View.GONE);
					edt_work_number.setVisibility(View.GONE);
				} else if (each_setting.Defaullt_Label__c.equalsIgnoreCase(getString(R.string.home_pone_number)) && Boolean.valueOf(each_setting.Included__c)) {
					homephone_layout.setVisibility(View.VISIBLE);
					edt_home_number.setVisibility(View.VISIBLE);
				}
			}*/
			extand_att_address.setVisibility(View.VISIBLE);
			expand_home_address.setVisibility(View.VISIBLE);
			homephone_layout.setVisibility(View.VISIBLE);
			edt_home_number.setVisibility(View.VISIBLE);
			tagCursor = Util.db
					.getTicketTag(payment_cursor.getString(payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_ID)));
			String colorArray[] = {"#3399FF", "#5F0606", "#0B614B", "#4B0B70", "#048D3D", "#669900"};
			if (tagCursor.getCount() > 0) {
				int j = -1;
				for (int i = 0; i < tagCursor.getCount(); i++) {
					j++;
					if (j == colorArray.length) {
						j = 0;
					}
					tagCursor.moveToPosition(i);
					// tagArray.add(tagCursor.getString(tagCursor.getColumnIndex(DBFeilds.TAG_NAME)));
					if (i == tagCursor.getCount() - 1)
						tags = tags
								+ "<FONT color="
								+ colorArray[j]
								+ ">"
								+ (Util
								.NullChecker(tagCursor
										.getString(
												tagCursor
														.getColumnIndex(DBFeilds.TAG_NAME))))
								.trim() + "</FONT>";
					else
						tags = tags
								+ "<FONT color="
								+ colorArray[j]
								+ ">"
								+ (Util
								.NullChecker(tagCursor
										.getString(
												tagCursor
														.getColumnIndex(DBFeilds.TAG_NAME)))).trim() + "</FONT>" + ", ";

					// tags=tags+tagCursor.getString(tagCursor.getColumnIndex(DBFeilds.TAG_NAME))+", ";

				}

				edit_tag.setText(Html.fromHtml((Util.NullChecker(tags))));
				// tagAdapter=new TicketTagAdapter();
				// tag_listview.setAdapter(tagAdapter);
			}
			String[] fullurl = checkedin_event_record.image.split("&id=");
			String url = fullurl[0];
			Picasso.with(AttendeeDetailActivity.this).load(url + "&id=" + payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_IMAGE)))
					.placeholder(R.drawable.default_image)
					.error(R.drawable.default_image).into(img_attendee);
			if (NullChecker(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_IMAGE))).isEmpty()) {
				img_attendee.setImageResource(R.drawable.default_image);
			}
			fname.setText(NullChecker(payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))));
			lname.setText(NullChecker(payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME))));
			email.setText(payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID)));
			company.setText(payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_COMPANY)));
			//Log.i("-----------------Custom Barcode-------------",":"+payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_CUSTOM_BARCODE)));

			edt_custombarcode.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_CUSTOM_BARCODE)));

		/*if (user_profile.Profile.Email__c.equalsIgnoreCase(payment_cursor
				.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID))))
			email.setEnabled(false);
		else
			email.setEnabled(true);*/

			edit_note.setText(payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_NOTE)));


			edit_badgeLabel.setText(Util.NullChecker(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))).replaceAll("\\<.*?\\>", ""));
			// edit_tag.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_TAG)));
			edit_seatno.setText(payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_TICKET_SEAT_NUMBER)));
			countryadapter = new ArrayAdapter<String>(this,
					R.layout.spinner_item, country_list) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					View v = super.getView(position, convertView, parent);
					((TextView) v).setTextColor(getResources().getColor(
							R.color.black));
					((TextView) v).setTypeface(Util.roboto_regular);
					return v;
				}
			};

			country_spinner.setAdapter(countryadapter);
			country_home_spnr.setAdapter(countryadapter);
			edit_add1.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_ADDRESS_1)));
			edit_add2.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_ADDRESS_2)));
			city.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_CITY)));
			zipCode.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_ZIPCODE)));

			edt_home_add1.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_HOME_ADDRESS_1)));
			edt_home_add2.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_HOME_ADDRESS_2)));
			edt_home_city.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_HOME_CITY)));
			edt_home_zip.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_HOME_ZIPCODE)));

			//Log.i("--------------Work Country Name-----------",":"+payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_COUNTRY)));
			if (!payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_COUNTRY)).isEmpty()) {
				for (int i = 0; i < country_list.size(); i++) {
					if (country_list.get(i).trim().equalsIgnoreCase(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_COUNTRY)).trim())) {
						//Log.i("--------------Work Country Name-----------",":"+i+" :: "+country_list.get(i).trim()+" :: "+country_list.get(i).trim().equalsIgnoreCase(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_COUNTRY)).trim()));
						country_spinner.setSelection(i);
						setStateAdapter(country_list.get(i));
						break;
					} else {
						country_spinner.setSelection(0);
						state_spinner.setSelection(0);
					}
				}
			} else {
				country_spinner.setSelection(0);
				state_spinner.setSelection(0);
			}
			//Log.i("--------------Home Country Name-----------",":"+payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_HOME_COUNTRY)));
			if (!payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_HOME_COUNTRY)).isEmpty()) {
				for (int i = 0; i < country_list.size(); i++) {
					if (country_list.get(i).trim().equalsIgnoreCase(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_HOME_COUNTRY)).trim())) {
						country_home_spnr.setSelection(i);
						setHomeStateAdapter(country_list.get(i));
						break;
					} else {
						country_home_spnr.setSelection(0);
						state_home_spnr.setSelection(0);
					}
				}
			} else {
				country_home_spnr.setSelection(0);
				state_home_spnr.setSelection(0);
			}

			/*if (NullChecker(
					payment_cursor.getString(payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_WORK_PHONE))).isEmpty())
				edt_work_number.setText(payment_cursor.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_WORK_PHONE)));
			else {
				if (isPhoneNumberValid(payment_cursor.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_WORK_PHONE))))
					edt_work_number.setText(payment_cursor.getString(payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_WORK_PHONE)));
				else
					edt_work_number.setText(String.format(
							"(%s) %s-%s",
							payment_cursor
									.getString(
											payment_cursor
													.getColumnIndex(DBFeilds.ATTENDEE_WORK_PHONE))
									.substring(0, 3),
							payment_cursor
									.getString(
											payment_cursor
													.getColumnIndex(DBFeilds.ATTENDEE_WORK_PHONE))
									.substring(3, 6),
							payment_cursor
									.getString(
											payment_cursor
													.getColumnIndex(DBFeilds.ATTENDEE_WORK_PHONE))
									.substring(6, 10)));
			}

			if (NullChecker(
					payment_cursor.getString(payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_HOME_PHONE))).isEmpty())
				edt_home_number.setText(payment_cursor.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_HOME_PHONE)));
			else {
				if (isPhoneNumberValid(payment_cursor.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_HOME_PHONE))))
					edt_home_number.setText(payment_cursor.getString(payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_HOME_PHONE)));
				else
					edt_home_number.setText(String.format(
							"(%s) %s-%s",
							payment_cursor
									.getString(
											payment_cursor
													.getColumnIndex(DBFeilds.ATTENDEE_HOME_PHONE))
									.substring(0, 3),
							payment_cursor
									.getString(
											payment_cursor
													.getColumnIndex(DBFeilds.ATTENDEE_HOME_PHONE))
									.substring(3, 6),
							payment_cursor
									.getString(
											payment_cursor
													.getColumnIndex(DBFeilds.ATTENDEE_HOME_PHONE))
									.substring(6, 10)));
			}

			if (NullChecker(
					payment_cursor.getString(payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_MOBILE))).isEmpty())
				edt_mobile.setText(payment_cursor.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_MOBILE)));
			else {
				if (isPhoneNumberValid(payment_cursor.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_MOBILE))))
					edt_mobile.setText(payment_cursor.getString(payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_MOBILE)));
				else
					edt_mobile.setText(String.format(
							"(%s) %s-%s",
							payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_MOBILE)).substring(0, 3),
							payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_MOBILE)).substring(3, 6),
							payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_MOBILE)).substring(6, 10)));
			}*/

			edt_work_number.setText(NullChecker(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_PHONE)).replaceAll("\\p{P}", "").replaceAll("-", "").replaceAll(" ", "").trim()));

			edt_home_number.setText(NullChecker(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_HOME_PHONE)).replaceAll("\\p{P}", "").replaceAll("-", "").replaceAll(" ", "").trim()));

			edt_mobile.setText(NullChecker(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_MOBILE)).replaceAll("\\p{P}", "").replaceAll("-", "").replaceAll(" ", "").trim()));

			if (!payment_cursor.getString(
					payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_ZIPCODE))
					.equals("null")
					&& !payment_cursor.getString(
					payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_WORK_ZIPCODE))
					.isEmpty()
					&& !payment_cursor.getString(
					payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_WORK_ZIPCODE))
					.equals("(null)")) {
				if (payment_cursor.getString(
						payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_ZIPCODE))
						.length() == 4
						&& !payment_cursor.getString(
						payment_cursor
								.getColumnIndex(DBFeilds.ATTENDEE_WORK_ZIPCODE))
						.startsWith("0"))
					zipCode.setText("0"
							+ payment_cursor.getString(payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_WORK_ZIPCODE)));
				else
					zipCode.setText(payment_cursor.getString(payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_WORK_ZIPCODE)));
			}
			// edit_add.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ADDRESS)));
			edit_jobtitle.setText(payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_JOB_TILE)));
			if (!NullChecker(
					payment_cursor.getString(payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_CHECKEDINDATE)))
					.isEmpty()) {


				String checkin_status = "";
				String tstatus = "";
				session_id = getIntent().getStringExtra(Util.INTENT_KEY_2);
				String checkin_time = ITransaction.EMPTY_STRING;
				if (NullChecker(session_id).isEmpty()) {
					tstatus = Util.db.getTStatusBasedOnGroup(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)), payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
					checkin_time = Util.db.getScantimeBasedOnGroup(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)), payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);//Util.change_US_ONLY_DateFormatWithSEC(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_CHECKEDINDATE)),checkedin_event_record.Events.Time_Zone__c);
				} else {
					tstatus = Util.db.getTStatusBasedOnGroup(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)), payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId, session_id);
					checkin_time = Util.db.getScantimeBasedOnGroup(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)), payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), session_id, checked_in_eventId);//Util.change_US_ONLY_DateFormatWithSEC(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_CHECKEDINDATE)),checkedin_event_record.Events.Time_Zone__c);
				}

				if (NullChecker(tstatus).equalsIgnoreCase("true")) {
					checkin_status = "Check In";
					txt_checkin_date.setTextColor(getResources().getColor(R.color.green_button_color));
				} else if (NullChecker(tstatus).equalsIgnoreCase("false")) {
					checkin_status = "Check Out";
					txt_checkin_date.setTextColor(getResources().getColor(
							R.color.orange_bg));
				} else {
					checkin_status = "Registered";
					txt_checkin_date.setTextColor(getResources().getColor(
							R.color.gray_color));

				}

				if (NullChecker(checkin_time).isEmpty()) {
					checkin_time = payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_CHECKEDINDATE));
				}

				checkin_time = Util.change_US_ONLY_DateFormatWithSEC(checkin_time, checkedin_event_record.Events.Time_Zone__c);
				txt_checkin_date.setText(checkin_time + "(" + checkin_status + ")");
				// txt_checkin_date.setText(Util.db_date_format1.format(Util.new_db_date_format.parse(payment_cursor.getString(payment_cursor
				// .getColumnIndex(DBFeilds.ATTENDEE_CHECKEDINDATE))))+"("+checkin_status+")");

			}
			if (!Util.NullChecker(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID))).isEmpty()) {
				txt_badgestatus.setText(Util.db.getAttendeeBadgeParentPrintStatus
						(Util.NullChecker(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID)))));
			} else {
				if (!NullChecker(payment_cursor.getString(
						payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS)))
						.isEmpty() && NullChecker(payment_cursor.getString(
						payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS))).equals("Printed")) {
					txt_badgestatus.setText("Printed");
				} else {
					txt_badgestatus.setText("Not Printed");
				}
			}

/*if (!NullChecker(payment_cursor.getString(
					payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS)))
					.isEmpty()&&NullChecker(payment_cursor.getString(
					payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS))).equals("Printed")) {
				txt_badgestatus.setText("Printed"+payment_cursor.getString(
						payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)));
			} else {
				txt_badgestatus.setText("Not Printed"+payment_cursor.getString(
						payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)));
			}*/
			whereClause = " where "
					+ DBFeilds.ORDERITEM_EVENT_ID
					+ " = '"
					+ payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_EVENT_ID))
					+ "'"
					+ " AND "
					+ DBFeilds.ORDERITEM_ORDER_ID
					+ " = "
					+ "'"
					+ payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID))
					+ "'"
					+ " AND "
					+ DBFeilds.ORDERITEM_ORDER_ITEM_ID
					+ " = "
					+ "'"
					+ payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ITEM_ID)) + "'";
			Cursor orderitemdetail = Util.db.getOrderDetailPool(whereClause);

			payment_cursor.moveToFirst();
			orderitemdetail.moveToFirst();
			double amt = 0;
			if (orderitemdetail.getCount() > 0) {
				String parent_id = Util.db.getItemPoolParentId(
						payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
				if (!NullChecker(parent_id).isEmpty()) {
					String package_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
					ticket_name.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)) + " ( "
							+ package_name + " ) - " + Html.fromHtml("<font color=#E3E2E5>"
							+ payment_cursor.getString(payment_cursor.getColumnIndex("Tikcet_Number")) + "</font>"));
				} else {
					ticket_name.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)) + " - "
							+ Html.fromHtml("<font color=#E3E2E5>" + payment_cursor.getString(payment_cursor.getColumnIndex("Tikcet_Number"))
							+ "</font>"));
				}
				if (NullChecker(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_ID))).isEmpty()) {
					//ticket_name.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)));
					amt = Double.parseDouble(orderitemdetail.getString(orderitemdetail.getColumnIndex(DBFeilds.ORDERITEM_EACH_ITEM_PRICE)));
					ticket_price.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + amt);
					ticket_qty.setText("1");
				} else {
					//ticket_name.setText(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)) + " - " + Html.fromHtml("<font color=#E3E2E5>" + payment_cursor.getString(payment_cursor.getColumnIndex("Tikcet_Number")) + "</font>"));
					amt = Double.parseDouble(orderitemdetail.getString(orderitemdetail.getColumnIndex(DBFeilds.ORDERITEM_EACH_ITEM_PRICE)));
					ticket_price.setText(Util.nf.format(amt));
					ticket_qty.setText("1");
				}
			}
			whereClause = " where "
					+ DBFeilds.ORDER_EVENT_ID
					+ " = '"
					+ payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_EVENT_ID))
					+ "'"
					+ " AND "
					+ DBFeilds.ORDER_ORDER_ID
					+ " = "
					+ "'"
					+ payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID)) + "'";
			final Cursor orderdetail = Util.db.getPaymentCursor(whereClause);
			//Log.i("Attendee Detail ", "Order cursor size=" + orderdetail.getCount());
			orderdetail.moveToFirst();
			if (orderdetail.getCount() > 0) {
				/*status.setText(orderdetail.getString(orderdetail
						.getColumnIndex(DBFeilds.ORDER_ORDER_STATUS)));*/
			/*ticket_fee.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Country__c).Currency_Symbol__c+
					Util.RoundTo2Decimals(Double.parseDouble(orderdetail.getString(orderdetail
							.getColumnIndex(DBFeilds.ORDER_FEE_AMOUNT)))));*/

				/*double fee = Double.parseDouble(orderdetail.getString(orderdetail
						.getColumnIndex(DBFeilds.ORDER_FEE_AMOUNT)));
				ticket_price.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c +
						Util.RoundTo2Decimals(amt + fee));*/
				if (isEventAdmin()) {
					txt_order_name.setText(Html.fromHtml("<u>" + orderdetail.getString(orderdetail
							.getColumnIndex(DBFeilds.ORDER_ORDER_NAME)) + "</u>"));
					txt_order_name.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent i = new Intent(AttendeeDetailActivity.this,
									OrderDetailActivity.class);
							i.putExtra(Util.ORDER_ID,
									orderdetail.getString(orderdetail.getColumnIndex(DBFeilds.ORDER_ORDER_ID)));
							startActivity(i);
						}
					});
				} else {
					txt_order_name.setText(orderdetail.getString(orderdetail
							.getColumnIndex(DBFeilds.ORDER_ORDER_NAME)));
				}


			/*txt_order_status.setText(orderdetail.getString(orderdetail
					.getColumnIndex(DBFeilds.ORDER_ORDER_STATUS)));*/

				//TODO PAYMENTITEMS
				/*txt_order_type.setText(orderdetail.getString(orderdetail
						.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE)));*/
				payment_modes = Util.db.getPaymentModeswithOrderID(orderdetail.getString(orderdetail
						.getColumnIndex(DBFeilds.ORDER_ORDER_ID)));
				paymentModeAdapter = new PaymentModeAdapter(AttendeeDetailActivity.this, payment_modes, false);
				payment_modeslist.setAdapter(paymentModeAdapter);
				Util.setListViewHeightBasedOnChildren(payment_modeslist);
				if (orderdetail.getString(
						orderdetail.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE))
						.equalsIgnoreCase("Check")) {
					//txt_note.setVisibility(View.VISIBLE);
				}
				img_attendee.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						openTakeFromDialg(AttendeeDetailActivity.this);
					}
				});
				// _seat_no=orderdetail.getString(orderdetail.getColumnIndex(DBFeilds.ORDER_CHECK_NUMBER));
				txt_note.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						whereClause = " where "
								+ DBFeilds.ORDER_EVENT_ID
								+ " = '"
								+ payment_cursor.getString(payment_cursor
								.getColumnIndex(DBFeilds.ATTENDEE_EVENT_ID))
								+ "'"
								+ " AND "
								+ DBFeilds.ORDER_ORDER_ID
								+ " = "
								+ "'"
								+ payment_cursor.getString(payment_cursor
								.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID))
								+ "'";
						Cursor orderdetail = Util.db.getPaymentCursor(whereClause);
						orderdetail.moveToFirst();
						openEditCheckDialog(
								orderdetail.getString(orderdetail
										.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_REF_NUMBER)), //TODO PAYMENTITEMS replased ORDER_CHECK_NUMBER
								orderdetail.getString(orderdetail
										.getColumnIndex(DBFeilds.ORDER_ORDER_STATUS)), orderdetail.getString(orderdetail
										.getColumnIndex(DBFeilds.ORDER_ORDER_ID)));
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setStateAdapter(String country) {
		state_list = Util.db.getStateList(Util.db.getCountryId(country));
		/*Arrays.sort(state_list);
		state_list[0] = "--Select--";*/
		work_state_adapter = new ArrayAdapter<String>(this,
				R.layout.spinner_item, state_list) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				((TextView) v).setTextColor(getResources().getColor(
						R.color.black));
				((TextView) v).setTypeface(Util.roboto_regular);
				return v;
			}
		};
		if (!payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_STATE)).isEmpty()) {
			int select = Util
					.getStatePosition(
							state_list, payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_STATE)));
			state_spinner.setAdapter(work_state_adapter);
			state_spinner.setSelection(select, true);
		} else {
			state_spinner.setAdapter(work_state_adapter);
			state_spinner.setSelection(0);
		}
	}

	private void setHomeStateAdapter(String country) {
		state_list_home = Util.db.getStateList(Util.db.getCountryId(country));
		/*Arrays.sort(state_list);
		state_list[0] = "--Select--";*/
		home_state_adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, state_list_home) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				((TextView) v).setTextColor(getResources().getColor(
						R.color.black));
				((TextView) v).setTypeface(Util.roboto_regular);
				return v;
			}
		};
		if (!payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_HOME_STATE)).isEmpty()) {
			int select = Util
					.getStatePosition(
							state_list_home, payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_HOME_STATE)));
			state_home_spnr.setAdapter(home_state_adapter);
			state_home_spnr.setSelection(select, true);
		} else {
			state_home_spnr.setAdapter(home_state_adapter);
			state_home_spnr.setSelection(0);
		}
	}

	public static class Utility_List {
		public static void setListViewHeightBasedOnChildren(ListView listView) {
			try {
				ListAdapter listAdapter = listView.getAdapter();
				if (listAdapter == null) {
					// pre-condition
					return;
				}
				int totalHeight = 0;
				int desiredWidth = MeasureSpec.makeMeasureSpec(
						listView.getWidth(), MeasureSpec.AT_MOST);
				for (int i = 0; i < listAdapter.getCount(); i++) {
					View listItem = listAdapter.getView(i, null, listView);
					listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
					totalHeight += listItem.getMeasuredHeight();
				}
				ViewGroup.LayoutParams params = listView.getLayoutParams();
				params.height = totalHeight
						+ (listView.getDividerHeight() * (listAdapter
						.getCount() - 1));
				listView.setLayoutParams(params);
				listView.requestLayout();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClick(View v) {
		try {
			/*if (v == back_layout) {
				// finish();
				if (back_type) {
					print_badge.setVisibility(View.GONE);
					frame_transparentbadge.setVisibility(View.GONE);
					btn_save.setVisibility(View.GONE);
					event_layout.setVisibility(View.VISIBLE);
					back_type = false;
					Intent i = new Intent();
					setResult(2017, i);
					finish();

				} else {
					Intent i = new Intent();
					setResult(2017, i);
					finish();
				}
			} else */
			/*if (v == txt_save) {
				hideSoftKeyboard(AttendeeDetailActivity.this);
				isPrinted = false;
				saveAndPrint = false;
				//Log.i("-----------------Is Valid Attendee--------",":"+isValidAttendeeRecord());
				if (isEdited()) {
					if (isValidAttendeeRecord()) {
						saveAttendeeRequest();
					}
				} else {
					Toast.makeText(AttendeeDetailActivity.this, "No changes Updated!", Toast.LENGTH_LONG).show();
				}
			}*/ /*else if (v == txt_print) {
				//Log.i("======Attendee Detail Print Clicked=====", ":");
				saveAndPrint=false;
				*//*if(isEdited()){
					if (isValidAttendeeRecord()) {
						saveAndPrint=true;
						printAttendeeRequest();
					}
				}else {*//*
				printAttendeeRequest();
				//}
			}*/
		} catch (NotFoundException e) {
			Intent i = new Intent();
			setResult(2017, i);
			finish();
			e.printStackTrace();
		}
	}

	public void printAttendeeRequest() {
		if (isEdited() == false) {
			saveAndPrint = false;
		} else {
			saveAndPrint = true;
		}
		checkedin_event_record = Util.db.getSelectedEventRecord(checked_in_eventId);
		badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
		whereClause = " where " + DBFeilds.ATTENDEE_EVENT_ID + " = '"
				+ checked_in_eventId + "'" + " AND " + DBFeilds.ATTENDEE_ID
				+ " = " + "'" + attendee_id + "'" + " AND "
				+ DBFeilds.ATTENDEE_ORDER_ID + " = " + "'" + order_id + "'";
		payment_cursor = Util.db.getAttendeeDetails(whereClause);
		payment_cursor.moveToFirst();
		if (badge_res.size() == 0) {
			AlertDialogCustom custom = new AlertDialogCustom(
					AttendeeDetailActivity.this);
			custom.setParamenters("Alert",
					"No Badge Selected, Do you want to select a Badge",
					new Intent(AttendeeDetailActivity.this,
							BadgeTemplateNewActivity.class), null, 2, false);
			custom.show();

		} else if (badge_res.size() > 0) {
			/*if (Util.NullChecker(
					payment_cursor.getString(payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS)))
					.equalsIgnoreCase("")) {*/
			if (Util.NullChecker(
					payment_cursor.getString(payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)))
					.isEmpty()) {
				isPrinted = false;
				if (saveAndPrint) {
					openprintDialog();
				} else {
					//if (isOnline()) {
					executePrinterStatusTask();
                    /*} else {
                        startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                    }*/
					/*if (isOnline()) {
						executePrinterStatusTask();
					} else {
						startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
					}*/
					/*if (isprinterconnected(AttendeeDetailActivity.this)) {
						if (isOnline()) {
							requestType = Util.GET_BADGE_ID;
							doRequest();
						} else {
							startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
						}
					}*/  /* else {
						Util.setCustomAlertDialog(AttendeeDetailActivity.this);
						String alertmsg = "";
						if (!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
							alertmsg = "Printer is disconnected.Do you want to Connect?";
						} else {
							alertmsg = "Printer is not connected.Do you want to Connect?";
						}
						Util.openCustomDialog("Alert", alertmsg);
						Util.txt_okey.setText("CONNECT");
						Util.txt_dismiss.setVisibility(View.VISIBLE);
						Util.txt_okey.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								//ShowTicketsDialog();
								startActivity(new Intent(AttendeeDetailActivity.this, PrintersListActivity.class));
								Utilâ‚¬.alert_dialog.dismiss();
							}
						});
						Util.txt_dismiss.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {

								Util.alert_dialog.dismiss();
							}
						});
						//Util.openprinternotconnectedpopup(AttendeeDetailActivity.this);
					}*/
				}
			} else {
				if (!Util.NullChecker(
						payment_cursor.getString(payment_cursor
								.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)))
						.isEmpty())
					isPrinted = true;
				if (isValidate_badge_reg_settings) {
					if (saveAndPrint)
						openprintDialog();
					else
						openSinglePrintButtonDialog();
				} else {
					executePrinterStatusTask();
				}
			}
		} else {
			if (isOnline()) {
				requestType = Util.LOAD_BADGE;
				doRequest();
			} else {
				startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
			}
		}
	}

	public boolean isprinterconnected() {
		try {
			zebraPrinter = new ZebraPrinter();
			if (!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty() && isOnline()) {
				zebraPrinter.createTCPConnection();
				if (zebraPrinter.getTCPConnection().isConnected()) {
					return true;
				}
			} else {
				if (zebraPrinter.getBTConnection().isConnected()) {
					return true;
				}else{
					zebraPrinter.createBTConnection();
					return  zebraPrinter.getBTConnection().isConnected();
				}
			}
			return false;
		} catch (Exception e) {
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    openprinterNotConnectedDialog(AttendeeDetailActivity.this);
                }
            });*/
			e.printStackTrace();
			return false;
		}
	}

	private void executePrinterStatusTask() {
		//ZebraPrinter.baseStartTime=System.currentTimeMillis();
		if (isOnline()) {
			if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Brother")) {
				if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty()) {
					new IsBrotherPrinterConnectTask().execute();
				}else if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_BT_MAC, "").isEmpty()){
					startPrintTask();
				}
                /*}else{
                    if (isOnline()) {
                        requestType = Util.GET_BADGE_ID;
                        doRequest();
                    } else {
                        startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                    }
                }*/
			} else if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")) {
				if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty()) {
					new IsPrinterConnectTask().execute();
				}else if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_BT_MAC, "").isEmpty()){
					startPrintTask();
				}
			} else if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Cloud Print") && isCloudPrintingON) {
				if (isOnline()) {
					requestType = Util.GET_BADGE_ID;
					doRequest();
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
						}
					});
				}
//openprinterNotConnectedDialog(this);
			} else {
				openprinterNotConnectedDialog(this);
			}
		} else {
			startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
		}
	}

	public void SearchPrinterStatusThread() /*extends Thread*/ {

		/* search for the printer for 10 times until printer has been found. */
		/*@Override
		public void run() {*/
		try {
			// search for net printer.
			if (netPrinterList(5)) {
				isBrotherPrinterConnected = true;
			} else {
				isBrotherPrinterConnected = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//}
	}

	private class IsBrotherPrinterConnectTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			baseDialog.setMessage("Please wait...");
			baseDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				SearchPrinterStatusThread();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			baseDialog.dismiss();
			//startPrintTask();
			if (isBrotherPrinterConnected) {
				startPrintTask();
			} else {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						AttendeeDetailActivity.openprinterNotConnectedDialog(AttendeeDetailActivity.this);
					}
				});
			}
		}
	}

	private class IsPrinterConnectTask extends AsyncTask<Void, Void, Void> {
		private boolean isPrinterConnectedStatus = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			baseDialog.setMessage("Please wait...");
			baseDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				isPrinterConnectedStatus = isprinterconnected();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			baseDialog.dismiss();
			//startPrintTask();
			if (isPrinterConnectedStatus) {
				startPrintTask();
			} else {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						openprinterNotConnectedDialog(AttendeeDetailActivity.this);
					}
				});
			}
		}
	}

	public static void openprinterNotConnectedDialog(final Context context) {
		Util.setCustomAlertDialog(context);
		String alertmsg = "";
		if (!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
			alertmsg = "Printer is disconnected.Do you want to Connect?";
		} else {
			alertmsg = "Printer is not connected.Do you want to Connect?";
		}
		Util.openCustomDialog("Alert", alertmsg);
		Util.txt_okey.setText("CONNECT");
		Util.txt_dismiss.setVisibility(View.VISIBLE);
		Util.txt_okey.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				//ShowTicketsDialog();
				context.startActivity(new Intent(context, PrintersListActivity.class));
				Util.alert_dialog.dismiss();
			}
		});
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Util.alert_dialog.dismiss();
			}
		});
	}


	public void saveAttendeeRequest() {
		if (isOnline()) {
			requestType = Util.ATTENDEE_DETAIL;
			doRequest();
		} else {
			startErrorAnimation(getResources().getString(R.string.network_error),
					txt_error_msg);
		}
	}

	public boolean isValidAttendeeRecord() {

		//address_cursor.moveToFirst();
		att_name = fname.getText().toString().trim();
		att_lname = lname.getText().toString().trim();
		att_email = email.getText().toString().toLowerCase().trim();
		att_company = company.getText().toString().trim();
		att_mobile = edt_mobile.getText().toString().trim();
		att_custombarcode = edt_custombarcode.getText().toString().trim();
		att_work_address1 = edit_add1.getText().toString().trim();
		att_work_address2 = edit_add2.getText().toString().trim();
		att_work_city = city.getText().toString().trim();
		if (country_spinner.getSelectedItem() != null) {
			att_work_country = country_spinner.getSelectedItem().toString();
		}

		if (state_spinner.getSelectedItem() != null) {
			att_work_state = state_spinner.getSelectedItem().toString();
		}
		att_work_mobile = edt_work_number.getText().toString().trim();
		att_work_zipcode = zipCode.getText().toString().trim();

		att_home_phone = edt_home_number.getText().toString().trim();
		att_home_add1 = edt_home_add1.getText().toString().trim();
		att_home_add2 = edt_home_add2.getText().toString().trim();
		att_home_city = edt_home_city.getText().toString().trim();
		if (state_home_spnr.getSelectedItem() != null) {
			att_home_state = state_home_spnr.getSelectedItem().toString();
		}
		if (country_home_spnr.getSelectedItem() != null) {
			att_home_country = country_home_spnr.getSelectedItem().toString();
		}

		att_home_zipcode = edt_home_zip.getText().toString().trim();
		if (att_home_state.equalsIgnoreCase("--None--")) {
			att_home_state = "";
		}

		// att_country = checkedin_event_record.country;
		att_seats = edit_seatno.getText().toString();
		att_job_title = edit_jobtitle.getText().toString();
		attendee_category = edit_att_category.getText().toString();
		att_note = edit_note.getText().toString();
		att_badge_lable = edit_badgeLabel.getText().toString();
		att_tag = Util.NullChecker(edit_tag.getText().toString());
		if (att_work_state.equalsIgnoreCase("--None--")) {
			att_work_state = "";
		}
		if (attendee_category.isEmpty()) {
			attendee_category = "Attendee";
		}
		if (att_name.equals("")) {
			fname.setError(getResources().getString(R.string.fname_alert));
			fname.requestFocus();
			return false;
		} else if (att_lname.equals("")) {
			lname.setError(getResources().getString(R.string.lname_alert));
			lname.requestFocus();
			return false;
		} else if (!Pattern.matches(Validation.EMAIL_REGEX, att_email)) {
			email.setError(getResources().getString(R.string.email_alert));
			email.requestFocus();
			return false;
		} else if (edt_custombarcode.isShown() && att_custombarcode.isEmpty()) {
			edt_custombarcode.setError(getResources().getString(R.string.barcode_alert));
			edt_custombarcode.requestFocus();
			return false;
		} /*else if (att_mobile.length() < 10 && !att_mobile.isEmpty()) {
			edt_mobile.setError(getResources().getString(R.string.phone_alert));
			edt_mobile.requestFocus();
			return false;
		} else if (att_work_mobile.length() < 10 && !att_work_mobile.isEmpty() && edt_work_number.isShown()) {
			edt_work_number.setError(getResources().getString(R.string.phone_alert));
			edt_work_number.requestFocus();
			return false;
		} else if (att_home_phone.length() < 10 && !att_home_phone.isEmpty() && edt_home_number.isShown()) {
			edt_home_number.setError(getResources().getString(R.string.phone_alert));
			edt_home_number.requestFocus();
			return false;
		} */else if (extand_att_address.isShown() && att_work_zipcode.length() < 5 && !att_work_zipcode.isEmpty()) {
			zipCode.setError(getResources().getString(R.string.zipcode_alert));
			zipCode.requestFocus();
			return false;
		} else if (expand_home_address.isShown() && att_home_zipcode.length() < 5 && !att_home_zipcode.isEmpty()) {
			edt_home_zip.setError(getResources().getString(R.string.zipcode_alert));
			edt_home_zip.requestFocus();
			return false;
		} else {
			return true;
		}
	}


	public boolean isEdited() {

		//Log.i("Attendee Detail Activity", "Save Button Clickes");
		//address_cursor.moveToFirst();
		boolean isedited = false;
		att_name = fname.getText().toString().trim();
		att_lname = lname.getText().toString().trim();
		att_email = email.getText().toString().toLowerCase().trim();
		att_company = company.getText().toString().trim();
		att_mobile = edt_mobile.getText().toString();

		att_work_address1 = edit_add1.getText().toString();
		att_work_address2 = edit_add2.getText().toString();
		att_work_city = city.getText().toString().trim();
		if (country_spinner.getSelectedItem() != null) {
			att_work_country = country_spinner.getSelectedItem().toString();
		}

		//Log.i("---------------Spinner Selected Item--------------",":"+state_spinner.getSelectedItem()+" : "+state_spinner.getSelectedItemPosition());
		if (state_spinner.getSelectedItem() != null) {
			att_work_state = state_spinner.getSelectedItem().toString();
		}

		att_work_zipcode = zipCode.getText().toString().trim();
		att_work_mobile = edt_work_number.getText().toString();
		String image = NullChecker(Util.db.getimagedata(Util.db.getByteArray(attendee_photo)));

		att_home_phone = edt_home_number.getText().toString().trim();
		att_home_add1 = edt_home_add1.getText().toString().trim();
		att_home_add2 = edt_home_add2.getText().toString().trim();
		att_home_city = edt_home_city.getText().toString().trim();
		att_custombarcode = edt_custombarcode.getText().toString().trim();

		if (state_home_spnr.getSelectedItem() != null) {
			att_home_state = state_home_spnr.getSelectedItem().toString();
		}
		if (country_home_spnr.getSelectedItem() != null) {
			att_home_country = country_home_spnr.getSelectedItem().toString();
		}

		att_home_zipcode = edt_home_zip.getText().toString().trim();

		att_seats = edit_seatno.getText().toString();
		att_job_title = edit_jobtitle.getText().toString();
		attendee_category = edit_att_category.getText().toString();
		att_note = edit_note.getText().toString();
		att_badge_lable = Util.NullChecker(edit_badgeLabel.getText().toString());
		att_tag = (Util
				.NullChecker(edit_tag.getText().toString()).replace("(null)", ""));

		if (att_home_state.equalsIgnoreCase("--None--")) {
			att_home_state = "";
		}
		if (att_work_state.equalsIgnoreCase("--None--")) {
			att_work_state = "";
		}
		if (attendee_category.isEmpty()) {
			attendee_category = "Attendee";
		}
		whereClause = " where " + DBFeilds.ATTENDEE_EVENT_ID + " = '"
				+ checked_in_eventId + "'" + " AND " + DBFeilds.ATTENDEE_ID
				+ " = " + "'" + attendee_id + "'" + " AND "
				+ DBFeilds.ATTENDEE_ORDER_ID + " = " + "'" + order_id + "'";
		payment_cursor = Util.db.getAttendeeDetails(whereClause);
		payment_cursor.moveToFirst();
		if (!att_name.equalsIgnoreCase(Util.NullChecker(payment_cursor
				.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))))) {
			return true;
		} else if (!att_lname.equalsIgnoreCase(Util.NullChecker(payment_cursor
				.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME))))) {
			return true;
		} else if (!att_email.equalsIgnoreCase(Util.NullChecker(payment_cursor
				.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID))))) {
			return true;
		} else if (!att_custombarcode.equalsIgnoreCase(Util.NullChecker(payment_cursor.
				getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_CUSTOM_BARCODE))))) { //212-555-9465
			return true;
		} else if (!att_mobile.replaceAll("\\p{P}", "").replaceAll("-", "").replaceAll(" ", "").trim().equalsIgnoreCase(Util.NullChecker(
				payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_MOBILE))).replaceAll("\\p{P}", "").replaceAll("-", "").replaceAll(" ", "").trim())) {
			return true;
		} else if (!att_work_mobile.replaceAll("\\p{P}", "").replaceAll("-", "").replaceAll(" ", "").trim().equalsIgnoreCase(Util.NullChecker(payment_cursor
				.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_PHONE))).replaceAll("\\p{P}", "").replaceAll("-", "").replaceAll(" ", "").trim())) {
			return true;
		} else if (!att_home_phone.replaceAll("\\p{P}", "").replaceAll("-", "").replaceAll(" ", "").trim().equalsIgnoreCase(Util.NullChecker(payment_cursor
				.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_HOME_PHONE))).replaceAll("\\p{P}", "").replaceAll("-", "").replaceAll(" ", "").trim())) {
			return true;
		} else if (!att_company.equalsIgnoreCase(Util
				.NullChecker(payment_cursor.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_COMPANY))))) {
			return true;
		} else if (!att_note.equalsIgnoreCase(Util.NullChecker(payment_cursor
				.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_NOTE))))) {
			return true;
		}  /*else if (!(Util
				.NullChecker(att_tag).equalsIgnoreCase(Util
                .NullChecker(tags)))) {
            return true;
        }*/ else if (!att_seats
				.equalsIgnoreCase(Util.NullChecker(payment_cursor.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_TICKET_SEAT_NUMBER))))) {
			return true;
		} else if (!att_job_title.equalsIgnoreCase(Util
				.NullChecker(payment_cursor.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_JOB_TILE))))) {
			return true;
		} else if (!att_badge_lable.equalsIgnoreCase(Util
				.NullChecker(payment_cursor.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))))) {
			return true;
		} else if (!Util
				.NullChecker(image).isEmpty()) {
			return true;
		} else {
			return isedited;
		}
	}

	private JSONObject setAttendeeJsonBody() {

		JSONObject json = null;
		try {
			json = new JSONObject();
			json.put("fn", att_name);
			json.put("ln", att_lname);
			json.put("email", att_email);
			json.put("comp", att_company);
			if (att_company.equals(payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_COMPANY))))
				json.put("compid", payment_cursor.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_COMPANY_ID)));
			else
				json.put("compid", "");
			json.put("mobile", att_mobile);
			json.put("BPhone", att_work_mobile);
			json.put("phone", att_home_phone);
			json.put("title", att_job_title);
			json.put("badgelabel", att_badge_lable);
			json.put("CustomBarcode", att_custombarcode);
			String image = "";
			if (attendee_photo != null)
				image = Util.NullChecker(Util.db.getimagedata(Util.db.getByteArray(attendee_photo)));
			if (payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_IMAGE)).length() > 0 && NullChecker(image).length() == 0) {
				json.put("UserPic", payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_IMAGE)));
			} else if (NullChecker(image).length() > 0) {
				json.put("AttendeeImage", image);
			} else {
				json.put("AttendeeImage", "");
			}

			json.put("tag", "");
			json.put("seatno", att_seats);
			json.put("note", att_note);
			String item_id = payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_ID));
			ArrayList<RegistrationSettingsController> reg_setting_list = Util.db.getRegSettingsList("where " + DBFeilds.REG_ITEM_ID + " = '" + item_id + "'");
			/*for (RegistrationSettingsController each_setting : reg_setting_list) {
				if (each_setting.Column_Name__c.equalsIgnoreCase(getString(R.string.work_address)) && Boolean.valueOf(each_setting.Included__c)) {
			*/		json.put("BAddress1", att_work_address1);
					json.put("BAddress2", att_work_address2);
					json.put("BCity", att_work_city);
					json.put("BZipcode", att_work_zipcode);

					if (!Util.db.getStateId(att_work_state.trim()).isEmpty())
						json.put("BState", Util.db.getStateId(att_work_state.trim()));
					if (!Util.db.getCountryId(att_work_country.trim()).isEmpty())
						json.put("BCountry", Util.db.getCountryId(att_work_country.trim()));
				//} else if (each_setting.Column_Name__c.equalsIgnoreCase(getString(R.string.home_address)) && Boolean.valueOf(each_setting.Included__c)) {
					json.put("add1", att_home_add1);
					json.put("add2", att_home_add2);
					json.put("city", att_home_city);
					if (!Util.db.getStateId(att_home_state.trim()).isEmpty())
						json.put("state", Util.db.getStateId(att_home_state.trim()));
					if (!Util.db.getCountryId(att_home_country.trim()).isEmpty())
						json.put("country", Util.db.getCountryId(att_home_country.trim()));
					json.put("zipcode", att_home_zipcode);
				//}
			//}


			json.put("tid", payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_ID)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}

	public void printClicked() {
		try {
			inputMethodManager.hideSoftInputFromWindow(getWindow()
					.getDecorView().getWindowToken(), 0);
			whereClause = " where " + DBFeilds.ATTENDEE_EVENT_ID + " = '"
					+ event_id + "'" + " AND " + DBFeilds.ATTENDEE_ID + " = "
					+ "'" + attendee_id + "'" + " AND " + DBFeilds.ATTENDEE_ORDER_ID + " = " + "'" + order_id + "'";
			payment_cursor = Util.db.getAttendeeDetails(whereClause);
			SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor = payment_cursor;//For Checkin if Quick Checkin is ON
			payment_cursor.moveToFirst();
			back_type = true;

			print_badge.setVisibility(View.VISIBLE);
			/*print_badge.setDrawingCacheEnabled(true);
			print_badge.buildDrawingCache(true);
			print_badge.buildDrawingCache();*/
			/*String where_att = " Where " + DBFeilds.BADGE_NEW_EVENT_ID + " = '"
					+ event_id + "' AND " + DBFeilds.BADGE_NEW_ID
					+ " = '"+checkedin_event_record.Events.badge_name+"'";
			badge_res = Util.db.getAllBadges(where_att);
			if(badge_res.size() > 0){
				printAttendeeRequest();
			}else{

				AlertDialogCustom custom = new AlertDialogCustom(
						AttendeeDetailActivity.this);
				custom.setParamenters("Alert",
						"No Badge Selected, Do you want to select a Badge",
						new Intent(AttendeeDetailActivity.this,
								BadgeTemplateNewActivity.class), null, 2, false);
				custom.show();
			}*/

			if (badge_res.size() == 0) {
				badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
				if (badge_res.size() == 0) {
					AlertDialogCustom custom = new AlertDialogCustom(
							AttendeeDetailActivity.this);
					custom.setParamenters("Alert",
							"No Badge Selected, Do you want to select a Badge",
							new Intent(AttendeeDetailActivity.this,
									BadgeTemplateNewActivity.class), null, 2, false);
					custom.show();
				}
			}


			badge_crator.createBadgeTemplate(badge_res.get(0), print_badge, payment_cursor, true);

			qrcode_name = payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
					+ " " + payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));


			//saveBitmap(print_badge.getDrawingCache(), qrcode_name);
			button_layout.setVisibility(View.GONE);
			btn_cancel.setVisibility(View.GONE);
			event_layout.setVisibility(View.GONE);
			frame_transparentbadge.setVisibility(View.VISIBLE);
			print_badge.setDrawingCacheEnabled(true);
			print_badge.buildDrawingCache(true);
			print_badge.buildDrawingCache();
			saveBitmap(loadBitmapFromView(print_badge), qrcode_name);
			if (saveBitmap(loadBitmapFromView(print_badge), qrcode_name))
				mListViewDidLoadHanlder.sendEmptyMessage(0);
			if (ext_settings.doubleSide_badge) {
				if (ext_settings.mirror_doubleSide_badge)
					saveBitmap(ZebraPrinter.rotateBitmap(loadBitmapFromView(print_badge), 180), qrcode_name + "mirror");
			}
			//overlay(print_badge.getDrawingCache(),ZebraPrinter.rotateBitmap(print_badge.getDrawingCache(),180));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Bitmap loadBitmapFromView(View view) {
		Bitmap returnedBitmap=null;
		try {
			returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(returnedBitmap);
			Drawable bgDrawable = view.getBackground();
			if (bgDrawable != null)
				bgDrawable.draw(canvas);
			else
				canvas.drawColor(Color.WHITE);
			view.draw(canvas);

			return returnedBitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return returnedBitmap;
		}
		/*Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
		v.draw(c);
		return b;*/

		/*Bitmap scaledBitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		float scaleX = v.getWidth() / (float) b.getWidth();
		float scaleY = v.getHeight() / (float) b.getHeight();
		float pivotX = 0; float pivotY = 0;
		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);
		Canvas canvas = new Canvas(scaledBitmap);
		canvas.setMatrix(scaleMatrix);
		canvas.drawBitmap(b, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
		v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
		v.draw(canvas);
		return scaledBitmap;*/


	}


	public void doPrint() {
		try {
			if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						mFiles.clear();
						print_badge.setDrawingCacheEnabled(true);
						print_badge.buildDrawingCache(true);
						print_badge.buildDrawingCache();
						saveBitmap(loadBitmapFromView(print_badge), qrcode_name);
						File root = android.os.Environment.getExternalStorageDirectory();
						File dir = new File(root.getAbsolutePath() + "/ScanAttendee/Badges");
						String file_path = dir.toString() + "/" + qrcode_name + ".png";
						mFiles.add(file_path);
						if(BaseActivity.zebraPrinter!=null)
							BaseActivity.zebraPrinter.doZebraPrint(AttendeeDetailActivity.this, mFiles);
						else {
							BaseActivity.zebraPrinter = new ZebraPrinter();
							BaseActivity.zebraPrinter.doZebraPrint(AttendeeDetailActivity.this, mFiles);
						}

					}
				}).start();

			} else if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Brother")) {
				print_badge.setDrawingCacheEnabled(true);
				print_badge.buildDrawingCache(true);
				print_badge.buildDrawingCache();
				saveBitmap(loadBitmapFromView(print_badge), qrcode_name);
				//setDialog();
				if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty()) {
					searchPrinter = new SearchThread();
					searchPrinter.start();
				}else{
					BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
					myPrint.setBluetoothAdapter(bluetoothAdapter);
					NetPrinter printer=new NetPrinter();
					printer.modelName=PrinterDetails.selectedPrinterPrefrences.getString("printer","");
					printer.serNo="";
					printer.ipAddress="";
					printer.macAddress=PrinterDetails.selectedPrinterPrefrences.getString("macAddress","");
					setPrefereces(printer);
					printBadge();
				}
			}else if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Cloud Print")&&isCloudPrintingON) {

                /*if (!isOnline()) {
                    Toast.makeText(this,
                            "Network connection not available, Please try later",
                            Toast.LENGTH_SHORT).show();
                } else {*/
				print_badge.setDrawingCacheEnabled(true);
				print_badge.buildDrawingCache();
				qrcode_name = payment_cursor.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
						+ " "
						+ payment_cursor
						.getString(payment_cursor
								.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));

				saveBitmap(loadBitmapFromView(print_badge), qrcode_name);
				if (PrinterDetails.selectedPrinterPrefrences.getBoolean(ZebraPrinter.FIT_TO_PAPER, false)){
					mFiles.clear();
					File root = android.os.Environment.getExternalStorageDirectory();
					File dir = new File(root.getAbsolutePath() + "/ScanAttendee/Badges");
					String file_path = dir.toString() + "/" + qrcode_name + ".png";
					mFiles.add(file_path);
					Util.createPDF(mFiles, true);
					ZebraPrinter.googleCloudPrint(this, "");
				}else {
					ZebraPrinter.googleCloudPrintImage(this, qrcode_name);
				}
				//}
			}else{
				openprinterNotConnectedDialog(AttendeeDetailActivity.this);
			}
		}
		/*try {
			if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						zebraPrinter.doZebraPrint(AttendeeDetailActivity.this, print_badge);
					}
				}).start();

			} else {
				print_badge.setDrawingCacheEnabled(true);
				print_badge.buildDrawingCache(true);
				print_badge.buildDrawingCache();
				saveBitmap(print_badge.getDrawingCache(), qrcode_name);
				setDialog();
				if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty()) {
					searchPrinter = new SearchThread();
					searchPrinter.start();
				}else{
					BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
					myPrint.setBluetoothAdapter(bluetoothAdapter);
					NetPrinter printer=new NetPrinter();
					printer.modelName=PrinterDetails.selectedPrinterPrefrences.getString("printer","");
					printer.serNo="";
					printer.ipAddress="";
					printer.macAddress=PrinterDetails.selectedPrinterPrefrences.getString("macAddress","");
					setPrefereces(printer);
					printBadge();
				}
			}
		}*/catch (Exception e){
			e.printStackTrace();
			startErrorAnimation(getResources().getString(R.string.network_error1), txt_error_msg);
		}
	}
	public static void openBadgeParentalert(String msg){
		Util.setCustomAlertDialog(activity);
		Util.openCustomDialog("Alert", msg);
		Util.txt_okey.setText("Ok");
		Util.txt_dismiss.setVisibility(View.GONE);
		Util.txt_okey.setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Util.alert_dialog.dismiss();
					}
				});

	}

	public void openprintDialog() {

		try {
			isReasonEmpty=false;
			print_dialog = new AlertDialog.Builder(AttendeeDetailActivity.this);
			LayoutInflater li = LayoutInflater.from(AttendeeDetailActivity.this);
			View promptsView = li.inflate(R.layout.print_dialog_layout, null);
			print_dialog.setView(promptsView);
			final EditText edit_reason = (EditText) promptsView.findViewById(R.id.edit_reason);
			final TextView txt_message=(TextView) promptsView.findViewById(R.id.txt_message);
			print_dialog
					.setCancelable(false)
					.setPositiveButton("Print",
						/*new DialogInterface.OnClickListener() { //Commented for Raj
								public void onClick(DialogInterface dialog,
													int id) {
									reason = edit_reason.getText().toString();
									if(isPrinted) {
										if (!reason.equalsIgnoreCase("")) {
											isReasonEmpty = false;
											if (isOnline()) {
												executePrinterStatusTask();
												dialog.dismiss();
											} else {
												startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
											}
										} else {
											isReasonEmpty = true;
											edit_reason.setError("Reason should not be empty");
											edit_reason.setFocusable(true);
										}
									}
									else{
										if(isOnline()) {
											isReasonEmpty = false;
											executePrinterStatusTask();
											dialog.dismiss();
										} else {
											startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
										}
									}

								}})
					.setNegativeButton("Save & Print",*/
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {

									reason = edit_reason.getText().toString();
									if(isPrinted)
									{
										if(!reason.equalsIgnoreCase("") ) {
											isReasonEmpty=false;
											if(isValidAttendeeRecord()){
												if (isOnline()) {
													if(isValidAttendeeRecord()){
														saveAttendeeRequest();
													}
													dialog.dismiss();
												}else{
													startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
												}
											}
										}
										else {
											isReasonEmpty=true;
											edit_reason.setError("Reason should not be empty");
											edit_reason.requestFocus();
										}

									}else{
										if (isOnline()) {
											isReasonEmpty=false;
											if(isValidAttendeeRecord())
												saveAttendeeRequest();
											dialog.dismiss();
										}else{
											startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
										}
									}

								}
							}).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					isReasonEmpty=false;
					dialog.dismiss();

				}
			});

			// create alert dialog
			final AlertDialog alertDialog = print_dialog.create();

			alertDialog.show();

			alertDialog
					.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
							//If the error flag was set to true then show the dialog again
							if (isReasonEmpty) {
								alertDialog.show();
								edit_reason.setError("Reason should not be empty");
								edit_reason.requestFocus();
							} else {
								return;
							}

						}
					});
			if(!saveAndPrint) {
				alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
				txt_message.setText("Do you want to print the badge?");
			}else {
				alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
				//txt_message.setText("Do you want to print the badge?");
			}
			if(!isPrinted){
				txt_message.setVisibility(View.VISIBLE);
				edit_reason.setVisibility(View.GONE);
			}else{
				txt_message.setVisibility(View.GONE);
				edit_reason.setVisibility(View.VISIBLE);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void openSinglePrintButtonDialog() {

		try {
			isReasonEmpty=false;
			print_dialog = new AlertDialog.Builder(AttendeeDetailActivity.this);
			LayoutInflater li = LayoutInflater.from(AttendeeDetailActivity.this);
			View promptsView = li.inflate(R.layout.print_dialog_layout, null);
			print_dialog.setView(promptsView);
			final EditText edit_reason = (EditText) promptsView.findViewById(R.id.edit_reason);
			final TextView txt_message=(TextView) promptsView.findViewById(R.id.txt_message);
			print_dialog
					.setCancelable(false)
					.setPositiveButton("Print",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									reason = edit_reason.getText().toString();
									if(isPrinted) {
										if (!reason.equalsIgnoreCase("")) {
											isReasonEmpty = false;
											if (isOnline()) {
												executePrinterStatusTask();
												dialog.dismiss();
											} else {
												startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
											}

										} else {
											isReasonEmpty = true;
											edit_reason.setError("Reason should not be empty");
											edit_reason.setFocusable(true);
										}
									}else{
										if(isOnline()) {
											isReasonEmpty = false;
											executePrinterStatusTask();
											dialog.dismiss();
										} else {
											startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
										}
									}

								}})
					/*.setNegativeButton("Save & Print",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {

                                    reason = edit_reason.getText().toString();
                                    if(isPrinted)
                                    {
                                        if(!reason.equalsIgnoreCase("") ) {
                                            isReasonEmpty=false;
                                            if(isValidAttendeeRecord()){
                                                if (isOnline()) {
                                                    if(isValidAttendeeRecord()){
                                                        saveAttendeeRequest();
                                                    }
                                                    dialog.dismiss();
                                                }else{
                                                    startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
                                                }
                                            }
                                        }
                                        else {
                                            isReasonEmpty=true;
                                            edit_reason.setError("Reason should not be empty");
                                            edit_reason.requestFocus();
                                        }

                                    }else{
                                        if (isOnline()) {
                                            isReasonEmpty=false;
                                            if(isValidAttendeeRecord())
                                                saveAttendeeRequest();
                                            dialog.dismiss();
                                        }else{
                                            startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
                                        }
                                    }

                                }
                            })*/.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					isReasonEmpty=false;
					dialog.dismiss();

				}
			});

			// create alert dialog
			final AlertDialog alertDialog = print_dialog.create();

			alertDialog.show();

			alertDialog
					.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
							//If the error flag was set to true then show the dialog again
							if (isReasonEmpty) {
								alertDialog.show();
								edit_reason.setError("Reason should not be empty");
								edit_reason.requestFocus();
							} else {
								return;
							}

						}
					});
			if(!saveAndPrint) {
				alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
				txt_message.setText("Do you want to print the badge?");
			}else {
				alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
				//txt_message.setText("Do you want to print the badge?");
			}
			if(!isPrinted){
				txt_message.setVisibility(View.VISIBLE);
				edit_reason.setVisibility(View.GONE);
			}else{
				txt_message.setVisibility(View.GONE);
				edit_reason.setVisibility(View.VISIBLE);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public Bitmap viewToBitmap(View view) {
		Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		return bitmap;
	}

	@Override
	public void onAnimationStart(Animation animation) {
		//Log.i("----Animation Startyed----", ":");
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		//Log.i("----Animation Finished----", ":");
		if (isExapnd) {
			// profile_detail_layout.setVisibility(View.GONE);
		} else {
			// profile_detail_layout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	public Bitmap encodeQrCode(String data) {
		Bitmap b = null;
		try {
			int smallerDimension = width < height ? width : height;
			smallerDimension = smallerDimension * 3 / 4;
			QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(data, null,
					Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(),
					smallerDimension);
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			b = qrCodeEncoder.encodeAsBitmap();
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return b;
	}

	public void setDialog() {
		try {
			msgDialog.showMsgNoButton(
					getString(R.string.netPrinterListTitle_label),
					getString(R.string.search_printer));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class SearchThread extends Thread {
		/* search for the printer for 10 times until printer has been found. */
		@Override
		public void run() {
			try {
				// search for net printer.
				if (netPrinterList(5)) {
					isPrinter = true;
					msgDialog.close();
					print_badge.setDrawingCacheEnabled(true);
					print_badge.buildDrawingCache();
					qrcode_name = payment_cursor.getString(payment_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
							+ " "
							+ payment_cursor
							.getString(payment_cursor
									.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
					saveBitmap(loadBitmapFromView(print_badge), qrcode_name);
					if(ext_settings.doubleSide_badge){
						if(ext_settings.mirror_doubleSide_badge)
							saveBitmap(ZebraPrinter.rotateBitmap(print_badge.getDrawingCache(),180),qrcode_name+"mirror");
					}
					//overlay(print_badge.getDrawingCache(),ZebraPrinter.rotateBitmap(print_badge.getDrawingCache(),180));
					printBadge();
				} else {
					msgDialog.close();
					AttendeeDetailActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Util.setCustomAlertDialog(AttendeeDetailActivity.this);
							Util.openCustomDialog("Alert", "No printer found. Do you want to reprint.");
							Util.txt_okey.setText("REPRINT");
							Util.txt_dismiss.setVisibility(View.VISIBLE);
							Util.txt_okey.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									//ShowTicketsDialog();
									//doPrint();
									startPrintTask();
									Util.alert_dialog.dismiss();
								}
							});

							Util.txt_dismiss.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									frame_transparentbadge
											.setVisibility(View.INVISIBLE);
									Util.alert_dialog.dismiss();
								}
							});

						}
					});

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean netPrinterList(int times) {
		boolean searchEnd = false;
		try {
			// clear the item list
			if (mItems != null) {
				mItems.clear();
			}
			// get net printers of the particular model
			mItems = new ArrayList<String>();
			Printer myPrinter = new Printer();
			AppUtils.displayLog("-AttendeeDetail Printer Model Name-",":"+PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.PRINTERMODEL,"QL_720NW").replace("_","-"));
			String printerIP=PrinterDetails.selectedPrinterPrefrences.getString("address","");
			mNetPrinter = myPrinter.getNetPrinters(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.PRINTERMODEL,"QL_720NW").replace("_","-"));
			final int netPrinterCount = mNetPrinter.length;

			// when find printers,set the printers' information to the list.
			if (netPrinterCount > 0) {
				searchEnd = true;
				for(NetPrinter printer:mNetPrinter){
					if(printerIP.equals(printer.ipAddress))
						setPrefereces(printer);
				}
			} else if (netPrinterCount == 0
					&& times == (Common.SEARCH_TIMES - 1)) { // when no printer
				// is found
				String dispBuff[] = new String[1];
				dispBuff[0] = getString(R.string.noNetDevice);
				mItems.add(dispBuff[0]);
				searchEnd = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return searchEnd;
	}


	/*
        private Bitmap overlay(Bitmap bm1,Bitmap bm2){
            */
/*Bitmap bm1 = null;
		Bitmap bm2 =  null;*//*

		Bitmap newBitmap = null;

		try {
			*/
/*bm1 = BitmapFactory.decodeStream(
					getContentResolver().openInputStream(source1));
			bm2 = BitmapFactory.decodeStream(
					getContentResolver().openInputStream(source2));*//*


			int w = bm1.getWidth() + bm2.getWidth();
			int h;
			if(bm1.getHeight() >= bm2.getHeight()){
				h = bm1.getHeight();
			}else{
				h = bm2.getHeight();
			}

			Bitmap.Config config = bm1.getConfig();
			if(config == null){
				config = Bitmap.Config.ARGB_8888;
			}

			newBitmap = Bitmap.createBitmap(w, h, config);
			Canvas newCanvas = new Canvas(newBitmap);

			newCanvas.drawBitmap(bm1, 0, bm2.getHeight(), null);
			newCanvas.drawBitmap(bm2, bm2.getWidth(), bm2.getHeight(), null);
			saveBitmap(newBitmap,"sai");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return newBitmap;
	}
*/
	private static int[] getRelativeCoords(View v){
		View parent = v.getRootView();
		int[] viewLocation = new int[2];
		v.getLocationInWindow(viewLocation);

		int[] rootLocation = new int[2];
		parent.getLocationInWindow(rootLocation);

		int relativeLeft = viewLocation[0] - rootLocation[0];
		int relativeTop  = viewLocation[1] - rootLocation[1];

		return new int[]{relativeLeft, relativeTop};
	}
	public void setPrefereces(NetPrinter mNetPrinter) {
		try {
			// initialization for print
			/*PrinterInfo printerInfo = new PrinterInfo();
			Printer printer = new Printer();
			printerInfo = printer.getPrinterInfo();*/
			if (sharedPreferences.getString("printerModel", "").equals("")) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString("printerModel", PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.PRINTERMODEL,"QL_720NW"));
				if(!mNetPrinter.ipAddress.toString().isEmpty())
					editor.putString("port", "NET");
				else
					editor.putString("port", "BLUETOOTH");

				editor.putString("macAddress", mNetPrinter.macAddress.toString());
				editor.putString("address", mNetPrinter.ipAddress.toString());
				editor.putString("address", mNetPrinter.ipAddress);
				editor.putString("macAddress", mNetPrinter.macAddress);
				editor.putString("printer", mNetPrinter.modelName);
				editor.putString("paperSize", "W62H100");
				editor.putString("serNo", mNetPrinter.serNo);

				if(badge_res.size()==0) {
					badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
				}

				if(badge_res.size()>0){
					BadgeDataNew badge_data = new Gson().fromJson(badge_res.get(0).badge.Data__c, BadgeDataNew.class);
					//Log.i("-----------------Badge Paper Size-----------",":"+badge_data.paperSize.contains(Util.BROTHER_DKN_5224)+" : "+badge_data.paperSize);
					if (badge_data.paperSize.contains(Util.BROTHER_DK_1202)) {
						// editor.putString("paperSize", "W62H100");
						editor.putString("paperSize", "W62H100");
					} else if (badge_data.paperSize.contains(Util.BROTHER_DK_12345)) {
						editor.putString("paperSize", "W60H86");
					} else if (badge_data.paperSize.contains(Util.BROTHER_DKN_5224)) {
						editor.putString("paperSize", "W54");
					}else if (badge_data.paperSize.contains("3\" x 1\"")) {//added for test in QA6
						editor.putString("paperSize", "W29H90");
					}else{
						editor.putString("paperSize", "W62");
					}
				}else{
					AlertDialogCustom custom = new AlertDialogCustom(
							AttendeeDetailActivity.this);
					custom.setParamenters("Alert",
							"No Badge Selected, Do you want to select a Badge",
							new Intent(AttendeeDetailActivity.this,
									BadgeTemplateNewActivity.class), null, 2, false);
					custom.show();
				}
				editor.putString("orientation", "LANDSCAPE");
				editor.putString("numberOfCopies", "1");
				editor.putString("halftone", "PATTERNDITHER");
				editor.putString("printMode", "FIT_TO_PAGE");
				editor.putString("pjCarbon", "false");
				editor.putString("pjDensity", "5");
				editor.putString("pjFeedMode", "PJ_FEED_MODE_FIXEDPAGE");
				editor.putString("align", "CENTER");
				editor.putString("leftMargin", "0");
				editor.putString("valign", "MIDDLE");
				editor.putString("topMargin", "0");
				editor.putString("customPaperWidth", "0");
				editor.putString("customPaperLength", "0");
				editor.putString("customFeed", "0");
				editor.putString("customSetting",
						sharedPreferences.getString("customSetting", ""));
				editor.putString("rjDensity", "0");
				editor.putString("rotate180", "false");
				editor.putString("peelMode", "false");
				editor.putString("autoCut", "true");
				editor.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean saveBitmap(Bitmap bitmap, String name) {
		String newFolder = "/Badges";
		File root = android.os.Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath() + "/ScanAttendee");
		if (!dir.exists()) {
			dir.mkdir();
		}
		File filename = new File(dir + newFolder);
		// Create a name for the saved image
		if (!filename.exists()) {
			filename.mkdir();
		}
		File file = new File(filename, name + ".png");// myimage.png
		//Log.i("Attendee Detail", "Save image path" + dir.toString() + newFolder);
		// Where to save it
		FileOutputStream out;
		try {
			out = new FileOutputStream(file);
			// boolean success =
			// bitmap.compress(CompressFormat.PNG, 100,
			// out);

			if (bitmap != null)
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			else
				//Log.i("Attendeee Detail", "Bitmap is null");
				out.flush();
			out.close();
			return true;
			// Toast.makeText(getApplicationContext(), "File is Saved in  " +
			// filename, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	private void getExternalSettings() {
		if(ext_settings==null) {
			ext_settings = new ExternalSettings();
			if (!Util.external_setting_pref.getString(BaseActivity.user_profile.Userid + BaseActivity.checkedin_event_record.Events.Id, "").isEmpty()) {
				ext_settings = new Gson().fromJson(Util.external_setting_pref.getString(BaseActivity.user_profile.Userid + BaseActivity.checkedin_event_record.Events.Id, ""), ExternalSettings.class);
			}
		}
	}

	public void printBadge() {
		try {
//Log.i("-----------------Badge Paper Size while printing-----------",":"+sharedPreferences.getString("paperSize", ""));
			mFiles.clear();
			File root = android.os.Environment.getExternalStorageDirectory();
			File dir = new File(root.getAbsolutePath() + "/ScanAttendee/Badges");
			String file_path = dir.toString() + "/" + qrcode_name + ".png";
			String rev_file_path="";
			if(ext_settings.doubleSide_badge) {
				if(ext_settings.mirror_doubleSide_badge||ext_settings.mirror_doubleSide_badge_two_in_one)
				{
					sharedPreferences.edit().putString("autoCut","").commit();
					sharedPreferences.edit().putString("endCut","").commit();
					//sharedPreferences.edit().putString("rotate180","true");
					if(ext_settings.mirror_doubleSide_badge_two_in_one) {
						saveBitmap(ZebraPrinter.rotateBitmapTwoInOne(loadBitmapFromView(print_badge), 180), qrcode_name + "mirror");
						sharedPreferences.edit().putString("numberOfCopies", "1").commit();
					}else {
						mFiles.add(file_path);
						saveBitmap(ZebraPrinter.rotateBitmap(loadBitmapFromView(print_badge),180),qrcode_name+"mirror");
						sharedPreferences.edit().putString("numberOfCopies", "1").commit();
					}
					rev_file_path=dir.toString() + "/" + qrcode_name+"mirror" + ".png";
					mFiles.add(rev_file_path);
				}else {
					sharedPreferences.edit().putString("autoCut", "false").commit();
					sharedPreferences.edit().putString("endCut", "true").commit();
					if(ext_settings.identical_doubleSide_badge_two_in_one) {
						sharedPreferences.edit().putString("numberOfCopies", "1").commit();
						saveBitmap(ZebraPrinter.identicalBitmapTwoInOne(loadBitmapFromView(print_badge), 0), qrcode_name);
					}else
						sharedPreferences.edit().putString("numberOfCopies", "2").commit();
					file_path = dir.toString() + "/" + qrcode_name + ".png";
					mFiles.add(file_path);
				}
			}else{
				sharedPreferences.edit().putString("autoCut","true").commit();
				sharedPreferences.edit().putString("endCut","").commit();
				sharedPreferences.edit().putString("numberOfCopies", "1").commit();
				mFiles.add(file_path);
			}
			((ImagePrint) myPrint).setFiles(mFiles);
			myPrint.print();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void printMirrorBadge() {
		try {
//Log.i("-----------------Badge Paper Size while printing-----------",":"+sharedPreferences.getString("paperSize", ""));
			File root = android.os.Environment.getExternalStorageDirectory();
			File dir = new File(root.getAbsolutePath() + "/ScanAttendee/Badges");
			String rev_file_path="";
			if(ext_settings.doubleSide_badge) {
				if (ext_settings.mirror_doubleSide_badge) {
					rev_file_path = dir.toString() + "/" + qrcode_name + "mirror" + ".png";
					mFiles.add(rev_file_path);
					sharedPreferences.edit().putString("autoCut", "").commit();
					sharedPreferences.edit().putString("endCut", "true").commit();
					sharedPreferences.edit().putString("numberOfCopies", "1").commit();
				}
			}
			((ImagePrint) myPrint).setFiles(mFiles);
			myPrint.print();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.globalnest.network.IPostResponse1#doRequest()
	 */
	private String setBadgeIdUrl() {
		return sfdcddetails.instance_url + WebServiceUrls.SA_BADGE_PRINT;
	}
	private JSONArray setPrintStatusBody() {
		try {
			JSONArray badgearray = new JSONArray();

			try {
				JSONObject obj = new JSONObject();
				obj.put("TicketId", payment_cursor.getString(payment_cursor
						.getColumnIndex(DBFeilds.ATTENDEE_ID)));
				obj.put("status", "Printed");
				//"devicenm":"ANdroid" ,"screenmode":"SelfCheckin" ,"printernm":"BrotherPrinter"
				obj.put("devicenm",Util.getDeviceNameandAppVersion());
				obj.put("screenmode","attendee mobile");
				obj.put("printernm",NullChecker(PrinterDetails.selectedPrinterPrefrences.getString("printer", "")));
				obj.put("printtime",Util.getCurrentDateTimeInGMT());
				badgearray.put(obj);

			} catch (JSONException e) {
				e.printStackTrace();
			}
			return badgearray;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private JSONArray setPrintBadgeBody() {


		JSONArray badgearray = new JSONArray();
		JSONObject obj = new JSONObject();
		try {
			obj.put("TicketId", payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_ID)));
			obj.put("BadgeLabel", NullChecker(payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))));
			obj.put("Reason", reason);
			obj.put("devicenm",Util.getDeviceNameandAppVersion());
			obj.put("screenmode","attendee mobile");
			obj.put("printernm",NullChecker(PrinterDetails.selectedPrinterPrefrences.getString("printer", "")));
			obj.put("printtime",Util.getCurrentDateTimeInGMT());
			badgearray.put(obj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return badgearray;
	}
	private class CheckNetConnection extends android.os.AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			txt_loading.setText("Checking Connection, please wait...");
			progress_dialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			return isNetAccess();
		}

		@Override
		protected void onPostExecute(Boolean response) {
			super.onPostExecute(response);
			if(response){
				requestType = Util.GET_BADGE_ID;
				doRequest();
			}else{
				startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);

			}
			progress_dialog.dismiss();
		}
	}

	public boolean isNetAccess(){
		if(Util.isInternetComing){
			return true;
		}else{
			return isOnline1();
		}
	}

	@Override
	public void doRequest() {
		String access_token = sfdcddetails.token_type + " "
				+ sfdcddetails.access_token;
		if (requestType.equals(Util.GET_BADGE_ID)) {

			postMethod = new HttpPostData("Getting Badge Id...",
					setBadgeIdUrl(), setPrintBadgeBody().toString(),
					access_token, AttendeeDetailActivity.this);
			postMethod.execute();

		} else if(requestType.equals(Util.UPDATE_PRINTSTATUS)){
			postMethod = new HttpPostData("Updating PrintStatus...",sfdcddetails.instance_url + WebServiceUrls.SA_BADGE_PRINTSTATUSUPDATE, setPrintStatusBody().toString(), access_token, AttendeeDetailActivity.this);
			postMethod.execute();
		}else if (requestType.equals(Util.EDIT_CHECK_NO)) {
			postMethod = new HttpPostData("", setEditCheckUrl(), null,
					access_token, AttendeeDetailActivity.this);
			postMethod.execute();
		} else if(requestType.equalsIgnoreCase(Util.LOAD_BADGE)){
			String _url = sfdcddetails.instance_url + WebServiceUrls.SA_GET_BADGE_TEMPLATE_NEW + "Event_Id="+ checked_in_eventId;
			postMethod = new HttpPostData("Loading Badges...", _url, null, access_token, AttendeeDetailActivity.this);
			postMethod.execute();
		}
		else if(requestType.equalsIgnoreCase(Util.ATTENDEE_DETAIL)){
			String url = sfdcddetails.instance_url
					+ WebServiceUrls.SA_ATTENDEE_DETAIL + "eventId="
					+ checked_in_eventId;
			postMethod = new HttpPostData("Saving Attendee Info...", url,
					setAttendeeJsonBody().toString(), access_token,
					AttendeeDetailActivity.this);
			postMethod.execute();
		}
		else if(requestType.equalsIgnoreCase(Util.ATTENDEE_DETAIL)){
			progressDialog =new ProgressDialog(AttendeeDetailActivity.this);
			progressDialog.setMessage("Saving Attendee Info...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			ApiInterface apiService = ApiClient.getClient(sfdcddetails.instance_url).create(ApiInterface.class);
			// Call<Void> jsonbody= apiService.setSurveys(setSellJsonBody());
			Call<TotalOrderListHandler> call = apiService.getAttendeeDetailvalues(checked_in_eventId,setAttendeeJsonBody().toString(),sfdcddetails.token_type + " "+ sfdcddetails.access_token);
			if(AppUtils.isLogEnabled) {
				AppUtils.displayLog(call + "------ Url-------", sfdcddetails.instance_url);
				AppUtils.displayLog(call + "------JSON Retrofit-------", setAttendeeJsonBody().toString());
			}
			call.enqueue(new Callback<TotalOrderListHandler>() {
				@Override
				public void onResponse(Call<TotalOrderListHandler> call, Response<TotalOrderListHandler> response) {
					Log.e(call+"------success-------", "------response started-------");
					if(AppUtils.isLogEnabled){AppUtils.displayLog(call+"------JSON Response-------", response.toString());}
					try {
						if (!isValidResponse(response.toString())) {
							dismissProgressDialog();
							openSessionExpireAlert(errorMessage(response.toString()));
						} else if (response.code() == 200) {
							totalorderlisthandler = response.body();
							if (!NullChecker(totalorderlisthandler.errorMsg).isEmpty()) {
								AlertDialogCustom dialog = new AlertDialogCustom(
										AttendeeDetailActivity.this);
								dialog.setParamenters("Alert",
										AppUtils.NullChecker(totalorderlisthandler.errorMsg), null, null,
										1, false);
								dialog.show();
							} else {
								if (totalorderlisthandler.TotalLists.size() > 0) {
									Util.db.upadteOrderList(
											totalorderlisthandler.TotalLists,
											checked_in_eventId);
								}
								if (!saveAndPrint) {
									if (AppUtils.NullChecker(totalorderlisthandler.errorMsg).isEmpty())
										Toast.makeText(AttendeeDetailActivity.this, "Saved Successfully", Toast.LENGTH_LONG).show();
									else {

										AlertDialogCustom dialog = new AlertDialogCustom(
												AttendeeDetailActivity.this);
										dialog.setParamenters("Alert",
												AppUtils.NullChecker(totalorderlisthandler.errorMsg), null, null,
												1, false);
										dialog.show();
										//AppUtils.showError(AttendeeDetailActivity.this,AppUtils.NullChecker(totalorderlisthandler.errorMsg));
									}
								} else {
									if (isOnline()) {
										saveAndPrint = false;
										executePrinterStatusTask();
									} else {
										startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
									}
									attendee_photo = null;

								}
							}
							dismissProgressDialog();
						}
					}catch (Exception e) {
						e.printStackTrace();
						startErrorAnimation(
								getResources().getString(R.string.network_error1),
								txt_error_msg);
					}
				}




				@Override
				public void onFailure(Call<TotalOrderListHandler> call, Throwable t) {
					// Log error here since request failed
					Log.e("------failure-------", t.toString());
					dismissProgressDialog();
				}
			});
			/*String url = sfdcddetails.instance_url
					+ WebServiceUrls.SA_ATTENDEE_DETAIL + "eventId="
					+ checked_in_eventId;
			postMethod = new HttpPostData("Saving Attendee Info...", url,
					setAttendeeJsonBody().toString(), access_token,
					AttendeeDetailActivity.this);
			postMethod.execute();*/
		}
	}
	private void dismissProgressDialog() {
		if(progressDialog!=null) {
			if(progressDialog.isShowing())
				progressDialog.dismiss();
		}
	}
	public String setEditCheckUrl() {
		payment_id=payment_modes.getString(payment_modes
				.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_ID));
		//payment_modes
		return sfdcddetails.instance_url
				+ WebServiceUrls.SA_EDIT_CHECK_NO
				+ "orderId="
				+ payment_cursor.getString(payment_cursor
				.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID))
				+ "&paymentId="+ payment_modes.getString(payment_modes.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_ID))
				+ "&chkNo=" + _check_no.replace(" ", "%20")+ "&chkStatus="
				+ _check_status.replace(" ", "%20");
		/*return sfdcddetails.instance_url
				+ WebServiceUrls.SA_EDIT_CHECK_NO
				+ "orderId="
				+ payment_cursor.getString(payment_cursor
				.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID))
				+ "&chkNo=" + _check_no.replace(" ", "%20") + "&chkStatus="
				+ _check_status.replace(" ", "%20");*/
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.globalnest.network.IPostResponse1#parseJsonResponse(java.lang.String)
	 */
	@Override
	public void parseJsonResponse(String response) {
		//Log.i("Attendee Detail", "respons====>" + response);
		try {
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}
			if (requestType.equals(Util.GET_BADGE_ID)) {
				if (!response.isEmpty()) {
					JSONArray badge_array = new JSONArray(response);
					for (int i = 0; i < badge_array.length(); i++) {
						JSONObject badge_obj = badge_array.optJSONObject(i);
						if (badge_obj.optString("Error").equalsIgnoreCase(
								"null")) {
							String BadgeLabel = badge_obj
									.optString("BadgeLabel");
							String BadgeId = badge_obj.optString("BadgeId");
							String Reason = badge_obj.optString("Reason");
							String TicketId = badge_obj.optString("TicketId");
							String printstatus = "Printed";
							String BadgeParentId = Util.NullChecker(badge_obj.optString("BadgeParentId"));
							Util.db.insertandupdateAttendeeBadgeId(BadgeLabel,
									BadgeId, Reason, TicketId,printstatus,BadgeParentId);
							/*if(!BadgeParentId.isEmpty()){
								Util.db.InsertandupdateBadgeIdforParentandChild(BadgeParentId,BadgeId);
							}*/
							printClicked();
						} else {
							startErrorAnimation(getResources().getString(R.string.network_error1), txt_error_msg);
						}
					}
				} else {
					//Log.i("Attendee Detail", "respons====>" + response);
					startErrorAnimation("Error in network", txt_error_msg);
				}
			}else if(requestType.equals(Util.UPDATE_PRINTSTATUS)){
				if (!response.isEmpty()) {
					JSONArray badge_array = new JSONArray(response);
					for (int i = 0; i < badge_array.length(); i++) {
						JSONObject badge_obj = badge_array.optJSONObject(i);
						String printstatus = badge_obj.optString("status");
						String TicketId = badge_obj.optString("ticketId");
						String lastmodifieddate = badge_obj.optString("lastmodifieddate");
						Util.db.insertandupdateAttendeeBadgeIdandPrintstatus(TicketId,printstatus,lastmodifieddate);
					}
					Util.printStatusupdateAttendees.edit().clear().commit();
					showCustomToast(this, "Updated Successfully! ",
							R.drawable.img_like, R.drawable.toast_greenroundededge, true);
					//finish();
				}
			} /*else if (requestType.equals(Util.EDIT_CHECK_NO)) {
				if (response.contains("Success")) {

					Util.db.InsertAndUpdateCheckNo(_check_no,payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID)),
							_check_status,payment_modes.getString(payment_modes.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_ID)));
					Toast.makeText(AttendeeDetailActivity.this,"Check Status Updated Sucessfully!",Toast.LENGTH_LONG).show();
					finish();
				} else {
					AlertDialogCustom dialog = new AlertDialogCustom(
							AttendeeDetailActivity.this);
					dialog.setParamenters("Alert",
							"Sorry! Failed to update check number", null, null,
							1, false);
					dialog.show();
				}
			}*/ else if (requestType.equals(Util.ATTENDEE_DETAIL)) {

				try {
					gson = new Gson();
					TotalOrderListHandler totalorderlisthandler = gson
							.fromJson(response, TotalOrderListHandler.class);

					if (totalorderlisthandler.TotalLists.size() > 0) {
						Util.db.upadteOrderList(
								totalorderlisthandler.TotalLists,
								checked_in_eventId);
					}
					if (!saveAndPrint) {
						if(AppUtils.NullChecker(totalorderlisthandler.errorMsg).isEmpty())
							Toast.makeText(AttendeeDetailActivity.this, "Saved Successfully", Toast.LENGTH_LONG).show();
						else{

							AlertDialogCustom dialog = new AlertDialogCustom(
									AttendeeDetailActivity.this);
							dialog.setParamenters("Alert",
									AppUtils.NullChecker(totalorderlisthandler.errorMsg), null, null,
									1, false);
							dialog.show();
							//AppUtils.showError(AttendeeDetailActivity.this,AppUtils.NullChecker(totalorderlisthandler.errorMsg));
						}
					} else {
						if (isOnline()) {
							saveAndPrint = false;
							executePrinterStatusTask();
						} else {
							startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
						}
						attendee_photo=null;
                        /*if(isprinterconnected(AttendeeDetailActivity.this)) {
							if (isOnline()) {
								saveAndPrint = false;
								requestType = Util.GET_BADGE_ID;
								doRequest();
							} else {
								startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
							}
						}*/ /*else{
							Util.OpenConnectPrinter(AttendeeDetailActivity.this);
							Util.txt_okey.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View arg0) {
									//ShowTicketsDialog();
									startActivity(new Intent(AttendeeDetailActivity.this,PrintersListActivity.class));
									Util.alert_dialog.dismiss();
								}
							});
							Util.txt_dismiss.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View arg0) {
									Util.alert_dialog.dismiss();
								}
							});
							//Util.openprinternotconnectedpopup(AttendeeDetailActivity.this);
						}*/
					}
				} catch (Exception e) {
					e.printStackTrace();
					startErrorAnimation(
							getResources().getString(R.string.network_error1),
							txt_error_msg);
				}
			}else if(requestType.equalsIgnoreCase(Util.LOAD_BADGE)){
				Type listType = new TypeToken<List<BadgeResponseNew>>() {}.getType();
				List<BadgeResponseNew> badges =  new Gson().fromJson(response, listType);
				AppUtils.displayLog("---------------- parseJsonResponse Badge Size----------", ":"+checkedin_event_record.Events.Mobile_Default_Badge__c+" : " + response);
				Util.db.deleteBadges(checked_in_eventId);
				sharedPreferences.edit().clear().commit();
				for(BadgeResponseNew badge : badges){
					badge.badge.event_id = checked_in_eventId;
					Util.db.InsertAndUpdateBadgeTemplateNew(badge);
				}
				badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
				if(badge_res.size() > 0){
					printAttendeeRequest();
				}else{
					AlertDialogCustom custom = new AlertDialogCustom(
							AttendeeDetailActivity.this);
					custom.setParamenters("Alert",
							"No Badge Selected, Do you want to select a Badge",
							new Intent(AttendeeDetailActivity.this,
									BadgeTemplateNewActivity.class), null, 2, false);
					custom.show();
				}

			}else if (requestType.equalsIgnoreCase("Check in")) {
				final OfflineSyncResController offlineResponse = new Gson().fromJson(response,
						OfflineSyncResController.class);
				String dialogtime = ITransaction.EMPTY_STRING;
				List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
				boolean isFreeSession = false;
				if (scanticks.size() > 0) {
					isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c,
							checked_in_eventId);
				}

				if (offlineResponse.SuccessTickets.size() > 0) {
					boolean status = Boolean.valueOf(offlineResponse.SuccessTickets.get(0).Status);// success.optJSONObject(0).optBoolean("Status");
					String time = offlineResponse.SuccessTickets.get(0).STicketId.scan_time__c;// success.optJSONObject(0).optString("TimeStamp");
					String attendee_Id = offlineResponse.SuccessTickets.get(0).STicketId.Ticket__c;// success.optJSONObject(0).optJSONObject("STicketId").optString("Ticket__c");
					dialogtime = Util.change_US_ONLY_DateFormatWithSEC(time,checkedin_event_record.Events.Time_Zone__c);
					if (isFreeSession) {
						List<TStatus> session_attendee = new ArrayList<TStatus>();
						session_attendee.add(offlineResponse.SuccessTickets.get(0).STicketId);
						Util.db.InsertAndUpdateSessionAttendees(session_attendee, checked_in_eventId);
					} else {
						Util.db.updateCheckedInStatus(offlineResponse.SuccessTickets.get(0).STicketId,
								checked_in_eventId);
						Toast.makeText(AttendeeDetailActivity.this,  "Printed SucessFully", Toast.LENGTH_LONG).show();

					}
					//updateView();

				}else {
					Toast.makeText(AttendeeDetailActivity.this,  "Printed SucessFully", Toast.LENGTH_LONG).show();

					//startErrorAnimation(offlineResponse.ErrorMsg, txt_error_msg);
				}}
		} catch (JSONException e) {
			e.printStackTrace();
			startErrorAnimation(
					getResources().getString(R.string.network_error1),
					txt_error_msg);
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

	private Handler mListViewDidLoadHanlder = new Handler(
			new Handler.Callback() {
				@Override
				public boolean handleMessage(Message message) {
					doPrint();
					return false;
				}

			});

	private void openEditCheckDialog(String check_no, String status,String order_id) {
		try{
			String _type[] = { "Check Not Received", "Paid" };
			payment_modes=Util.db.getPaymentModeswithOrderID(order_id);
		/*payment_id=payment_modes.getString(payment_modes
				.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_ID));*/
			// create a Dialog component
			// add_key_dialog = new Dialog(PGatewayKeyList.this);
			add_key_dialog = new Dialog(AttendeeDetailActivity.this,
					R.style.DialogBottomSlideAnim);
			add_key_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			add_key_dialog.setCancelable(false);
			add_key_dialog.setContentView(R.layout.edit_check_number_dialog);

			final TextView edt_att_check_no = (TextView) add_key_dialog
					.findViewById(R.id.edit_att_check_no);
			ListView paytype =(ListView) add_key_dialog.findViewById(R.id.paytpye);
			final Spinner spinner_type = (Spinner) add_key_dialog
					.findViewById(R.id.spinner_edit_check_type);
			Button btn_dialog_save = (Button) add_key_dialog
					.findViewById(R.id.btn_dialog_save);
			Button btn_dialog_cancle = (Button) add_key_dialog
					.findViewById(R.id.btn_dialog_cancle);

			edt_att_check_no.setText(check_no);
			ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this,
					R.layout.spinner_item_layout, _type) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					View v = super.getView(position, convertView, parent);
					((TextView) v).setTextColor(getResources().getColor(
							R.color.black));
					((TextView) v).setTypeface(Util.roboto_regular);
					return v;
				}
			};

			paymentModeAdapter=new PaymentModeAdapter(AttendeeDetailActivity.this,payment_modes,true);
			paytype.setAdapter(paymentModeAdapter);
			Util.setListViewHeightBasedOnChildren(paytype);
			spinner_type.setAdapter(typeAdapter);

			for (int i = 0; i < _type.length; i++) {
				if (_type[i].equals(status)) {
					spinner_type.setSelection(i);
				}
			}

			// _seat_no=check_no;
			btn_dialog_save.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					_check_no = edt_att_check_no.getText().toString();
					_check_status = spinner_type.getSelectedItem().toString();
					hideKeybord(edt_att_check_no);
					if (isOnline()) {
						requestType = Util.EDIT_CHECK_NO;
						doRequest();
					} else {
						startErrorAnimation(
								getResources().getString(R.string.network_error),
								txt_error_msg);
					}
					add_key_dialog.dismiss();
				}
			});

			btn_dialog_cancle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					hideKeybord(edt_att_check_no);
					// InputMethodManager imm = (InputMethodManager)
					// getSystemService(Context.INPUT_METHOD_SERVICE);
					// imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
					// 0);

					add_key_dialog.dismiss();
				}
			});

			add_key_dialog.show();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public class  PaymentModeAdapter extends CursorAdapter{
		Cursor c_new;
		EditText checkno;
		Boolean forcheck;
		TextView txt_payment_mode,txt_payment_status,txt_payment_amount;
		//ImageView img_edit;
		LinearLayout layout_payment_status,lay_edit;
		public PaymentModeAdapter(Context context, Cursor c,boolean forcheck) {
			super(context, c);
			this.c_new = c;
			this.forcheck=forcheck;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = inflater.inflate(R.layout.attendee_payment_listitem, null, false);
			checkno=(EditText) v.findViewById(R.id.edt_checkno);
			layout_payment_status=(LinearLayout) v.findViewById(R.id.layout_payment_status);
			txt_payment_mode=(TextView) v.findViewById(R.id.txt_payment_mode);
			txt_payment_status=(TextView) v.findViewById(R.id.txt_payment_status);
			lay_edit=(LinearLayout) v.findViewById(R.id.lay_edit);
			txt_payment_amount=(TextView) v.findViewById(R.id.txt_payment_amount);
			v.setTag(cursor.getPosition());
			return v;
		}

		@Override
		public void bindView(View view, Context context, final Cursor cursor) {
			//view.getTag();
			if(forcheck) {
				/*checkno.setVisibility(View.VISIBLE);
				checkno.setText(cursor.getString(cursor
						.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_REF_NUMBER)));*/
			}else{
				layout_payment_status.setVisibility(View.VISIBLE);
				txt_payment_mode.setText(cursor.getString(cursor
						.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE)));
				/*if(cursor.getString(cursor
						.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE)).equalsIgnoreCase("Check")){
					lay_edit.setVisibility(View.VISIBLE);
				}*/
				txt_payment_status.setText(cursor.getString(cursor
						.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS)));
				if(!cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS)).equalsIgnoreCase("Paid")&&
						!cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS)).isEmpty()){
					txt_payment_amount.setTextColor(getResources().getColor(R.color.red));
				}
				txt_payment_amount.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+" "+
						Util.RoundTo2Decimals(cursor.getDouble(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_AMOUNT))));
			}
			lay_edit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					openEditCheckDialog(
							cursor.getString(cursor
									.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_REF_NUMBER)), //TODO PAYMENTITEMS replased ORDER_CHECK_NUMBER
							cursor.getString(cursor
									.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS)),
							cursor.getString(cursor
									.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_ORDER_ID)));
				}
			});
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		Log.i("---------------onActivity Result------------", ":" + requestCode + " : " + data.getStringExtra(Util.INTENT_KEY_1));
		try {
			if (requestCode == 100&&data!=null) {
				//setCustumViewData();
				//if(frame_barcode.isShown()){
				edt_custombarcode.setText(NullChecker(data.getStringExtra(Util.INTENT_KEY_1)));
				//}

			}


			if((requestCode == REQUEST_CODE_CROP_IMAGE)&& (data!=null)){

				String path = data.getStringExtra(CropImage.IMAGE_PATH);

				if ((path == null)||(TextUtils.isEmpty(path))) {
					return;
				}
				Bitmap  bitmap = BitmapFactory.decodeFile(path);
				/*if(bitmapArrayList.size()<formPosition)
					bitmapArrayList.add(bitmap);
				else
					bitmapArrayList.set(formPosition,bitmap);*/
				img_attendee.setImageBitmap(bitmap);
				attendee_photo = bitmap;

				if(mFileTemp!=null){
					mFileTemp.delete();
				}
			}else if ((requestCode == PICK_FROM_CAMERA) && (resultCode == RESULT_OK)) {
				if (data != null) {

					//doCrop();
				} else {
					File mediaStorageDir = new File(
							Environment.getExternalStorageDirectory(),
							"ScanAttendee");
					if(!mediaStorageDir.exists()){
						mediaStorageDir.mkdir();
					}
					mediaFile = new File(mediaStorageDir.getPath() + File.separator
							+ "IMG_1.jpg");
					mImageCaptureUri = Uri.fromFile(mediaFile);


					if (mImageCaptureUri != null) {
						try {
							startCropImage(AttendeeDetailActivity.this);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}

			} else if (requestCode == PICK_FROM_FILE && data != null
					&& data.getData() != null) {

				mImageCaptureUri = data.getData();
				//mediaFile = new File(getRealPathFromURI(mImageCaptureUri));
				try {
					File mediaStorageDir = new File(
							Environment.getExternalStorageDirectory(),
							"ScanAttendee");
					if(!mediaStorageDir.exists()){
						mediaStorageDir.mkdir();
					}
					mFileTemp = new File(mediaStorageDir.getPath() + File.separator
							+ "IMG_1.jpg");
					InputStream inputStream = getContentResolver().openInputStream(data.getData());
					FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
					CropUtil.copyStream(inputStream, fileOutputStream);
					mediaFile = mFileTemp;
					startCropImage(AttendeeDetailActivity.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (requestCode == CROP_FROM_CAMERA) {

				Bundle extras = data.getExtras();

				if (extras != null) {
					attendee_photo = extras.getParcelable("data");
					img_attendee.setImageBitmap(attendee_photo);

				}
			}else if (requestCode == FINISH_RESULT) {
				startActivity(new Intent(AttendeeDetailActivity.this, SplashActivity.class));
				finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private class doTicketCheckIn extends SafeAsyncTask<String> {

		int _index = 0;
		String _body = "";

		/**
		 *
		 */
		public doTicketCheckIn(int index, String body) {

			this._index = index;
			this._body = body;
		}

		protected void onPreExecute() throws Exception {
			super.onPreExecute();
			dialog.setMessage("Attendee Checking In/Out...");
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		public String call() throws Exception {
			return postTicketCheckIn(_body);
		}

		protected void onSuccess(String result) throws Exception {
			super.onSuccess(result);
			dialog.dismiss();
			if (result != null) {
				parseJsonResponse(result);
			}
		}

	}
	public void startPrintTask(){
		if (!isValidate_badge_reg_settings) {
			if (!payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty()) {
				printClicked();
			} else {
				new CheckNetConnection().execute();
			}
		} else if (isValidate_badge_reg_settings) {
			if (!payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty()&&
					!payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS)).equalsIgnoreCase("Printed")) {
				printClicked();
			} else {
				new CheckNetConnection().execute();
			}
		}
		/*if(!isValidate_badge_reg_settings){
			if(!payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty()){
				printClicked();
			}else{
				new CheckNetConnection().execute();
			}
		}else{
			new CheckNetConnection().execute();
		}*/
	}

	private String postTicketCheckIn(String body) {
		String response = "";
		try {

			HttpParams params = new BasicHttpParams();
			int timeoutconnection = 30000;
			HttpConnectionParams.setConnectionTimeout(params, timeoutconnection);
			int sockettimeout = 30000;
			HttpConnectionParams.setSoTimeout(params, sockettimeout);
			HttpClient _httpclient = HttpClientClass.getHttpClient(30000);
			String _url = sfdcddetails.instance_url + WebServiceUrls.SA_TICKETS_SCAN_URL + "scannedby="
					+ sfdcddetails.user_id + "&eventId=" + checked_in_eventId + "&source=Online"+"&DeviceType="+Util.getDeviceNameandAppVersion().replaceAll(" ", "%20")+"&checkin_only="+String.valueOf(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false));
			AppUtils.displayLog("--------------Check In URL-------------", ":" + _url);
			HttpPost _httppost = new HttpPost(_url);
			_httppost.addHeader("Authorization", sfdcddetails.token_type + " " + sfdcddetails.access_token);
			AppUtils.displayLog("-----BEARER TOKEN---", ":" + sfdcddetails.token_type + " " + sfdcddetails.access_token);
			AppUtils.displayLog("---------------Checkin in Body--------------", body);
			_httppost.setEntity(new StringEntity(body.toString()));

			HttpResponse _httpresponse = _httpclient.execute(_httppost);
			// int _responsecode =
			// _httpresponse.getStatusLine().getStatusCode();
			// Log.i("HTTP RESPONSE CODE", ":" + _responsecode);

			response = EntityUtils.toString(_httpresponse.getEntity());
			AppUtils.displayLog("--------------Post Method Response -----------", ":" + response);

		} catch (Exception e) {
			e.printStackTrace();
			response = e.getLocalizedMessage();
		}
		return response;

	}
	/*public class SearchPrinterStatusThread extends Thread {
	 *//* search for the printer for 10 times until printer has been found. *//*
		@Override
		public void run() {
			try {
				// search for net printer.
				if (netPrinterList(5)) {
					isprinterconnected=true;

				} else {
					isprinterconnected=false;
					if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)&&(!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty())){
						msgDialog.close();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Util.setCustomAlertDialog(AttendeeDetailActivity.this);
								Util.openCustomDialog("Alert", "Printer is disconnected please contact event Organizer!");
								Util.txt_okey.setText("Ok");
								Util.txt_dismiss.setVisibility(View.GONE);
								Util.txt_okey.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View arg0) {
										frame_transparentbadge
												.setVisibility(View.INVISIBLE);
										Util.alert_dialog.dismiss();
										finish();
									}
								});

							}
						});

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}*/


}
