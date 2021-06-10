package com.globalnest.scanattendee;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ebay.redlasersdk.BarcodeScanActivity;
import com.globalnest.classes.QRCodeEncoder;
import com.globalnest.data.Contents;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.AttendeeDetailsController;
import com.globalnest.mvc.CollectionController;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.mvc.PrintDetails;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.network.HttpGetMethod;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.ScannedItems;
import com.globalnest.printer.PrinterDetails;
import com.globalnest.result.ResultHandler;
import com.globalnest.result.ResultHandlerFactory;
import com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.result.ParsedResultType;

import RLSDK.ac;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

@SuppressLint("NewApi")
public class BarCodeScanActivity extends BaseActivity {

	/*static {
		System.loadLibrary("iconv");
	}*/

	TotalOrderListHandler totalorderlisthandler;

	Dialog result_dialog, ticket_dialog, progress_dialog;
	FrameLayout camera_layout, frame;
	ImageView img_frame, tick_img;
	Cursor attendee_cursor;
	CheckBox check_select;
	Button btn_result_cancel, btn_result_okey;
	ImageView img_checkin_done, img_checkin_cancel, img_print;
	TextView txt_loading, ticketname, txtticketnum,txt_session_name;
	LinearLayout layout_session;
	private boolean previewing = true, barcodeScanned = false,_bluetoothIsOn;
	// private BluetoothAdapter _bluetoothAdapter = null;
	private static final int REQUEST_ENABLE_BT = 1;
	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;
	ImageScanner scanner;
	private BluetoothAdapter _bluetoothAdapter = null;
	SurfaceView view;
	// ListCheckInAdapter checkin_adapter;
	// private List<BarcodeFormat> mFormats;
	String attendee_qrCode_id = "", QrId = "", orderId = "", url = "",
			checked_time = "", attendeeId = "", attendee_fname = "",
			requestType = "";
	HashMap<String, Boolean> tickets_register = new HashMap<String, Boolean>();
	HashMap<String, String> tickets_register1 = new HashMap<String, String>();
	HashMap<String, String> checkin_time_map = new HashMap<String, String>();
	ArrayList<String> ticket_list = new ArrayList<String>();
	ArrayList<String> ticket_list1 = new ArrayList<String>();

	//private boolean _unregisteredBroadcast = false;
	@SuppressWarnings("unused")
	private String _symbologyname, bt_mac_address = "";
	boolean isOrderScaneed;
	HttpGetMethod getMehod;
	HttpPostData postMehod;
	static  int  currentCameraId=0;
	Dialog softscannerdialog;
	ArrayList<CollectionController> payment_details_controller = new ArrayList<CollectionController>();
	ArrayList<AttendeeDetailsController> attendee_controller;
	AttendeeDetailsController attnedee = new AttendeeDetailsController();
	String fromclass_name="";
	private AlertDialog.Builder print_dialog;
	Boolean isReasonEmpty=false;
	String reason="";
	PrintAndCheckin printT;
	PrintDetails printDetails;
	//private boolean quick_scan=false;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//frontcam=false;
		fromclass_name = this.getIntent().getStringExtra(Util.INTENT_KEY_1);
		progress_dialog = new Dialog(this);
		progress_dialog.setCanceledOnTouchOutside(false);
		progress_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progress_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		this.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		progress_dialog.setContentView(R.layout.loading_layout);
		// progress_dialog.setCancelable(false);
		txt_loading = (TextView) progress_dialog.findViewById(R.id.txtloading);
		txt_loading.setTypeface(Util.roboto_regular);
		// checkin_adapter = new ListCheckInAdapter();
		setCustomContentView(R.layout.scan_layout);
		if(Util.db.getSwitchedONGroup(checked_in_eventId).Name.isEmpty()&&Util.getselfcheckinbools(Util.ISALLOWSCANTOPRINT)){
			layout_session.setVisibility(View.GONE);
		}else {
			txt_session_name.setText(Util.db.getSwitchedONGroup(checked_in_eventId).Name);
		}
		if(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)) {
			Util.checkin_only_pref.edit().putBoolean(sfdcddetails.user_id+checked_in_eventId, true).commit();
			Util.scanmode_checkin_pref.edit().putString(sfdcddetails.user_id+checked_in_eventId, "Checkin").commit();
		}else {
			Util.scanmode_checkin_pref.edit().putString(sfdcddetails.user_id+checked_in_eventId, "Checkinout").commit();

		}
		autoFocusHandler = new Handler();
		mCamera = getCameraInstance();
		/* Instance barcode scanner */
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);
		LayoutParams camera_param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		camera_param.gravity = Gravity.CENTER;
		mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
		// mPreview.setLayoutParams(camera_param);
		camera_layout.addView(mPreview);
		// handler.sendEmptyMessage(0);
		//scanStart();
		if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
			img_camera.setVisibility(View.VISIBLE);
			//frontcam=true;
		}else{
			img_camera.setVisibility(View.GONE);
			frontcam=false;
		}
		img_camera.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(frontcam){
					frontcam=false;
				}else{
					frontcam=true;
				}
				//frontcam=isChecked;
				releaseCamera();
				startActivity(new Intent(BarCodeScanActivity.this,BarCodeScanActivity.class));
				finish();
			}
		});

		back_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				/*if (mDrawerLayout.isDrawerOpen(left_menu_slider))
					mDrawerLayout.closeDrawer(left_menu_slider);
				else
					mDrawerLayout.openDrawer(left_menu_slider);*/

		/*		Intent result = new Intent();
				setResult(1987, result);*/
				releaseCamera();
				finish();
			}
		});

		img_search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setSoftScannerAPI();
				openSoftScannerDialog(BarCodeScanActivity.this,_bluetoothAdapter);
			}
		});
		ScannerSettingsApplication.getInstance().increaseViewCount();
	}
	@Override
	protected void onResume() {
		super.onResume();
		try {
			hideSoftKeyboard(BarCodeScanActivity.this);
			//Util.alert_dialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*if (Util.socket_device_pref.getBoolean(Util.SOCKET_DEVICE_CONNECTED, false)) {
			img_scanner_base.setBackgroundResource(R.drawable.green_circle_1);
		}else{
			img_scanner_base.setBackgroundResource(R.drawable.red_circle_1);
		}*/
		AppUtils.displayLog("--------------Barcode on Resume-----------",":"+barcodeScanned);

		scanStart();
	}
	@Override
	protected void onStart() {
		super.onStart();
	}
	@Override
	protected void onStop() {
		super.onStop();
		AppUtils.displayLog("----------------OnStop----------",":"+barcodeScanned);
		if(!Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
			releaseCamera();
			finish();
		}
		//unregisterocketScannerBroadcast();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Util.slide_menu_id = 0;

	}

	private void setSoftScannerAPI() {
		try {
			_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

			// If the adapter is null, then Bluetooth is not supported
			if (_bluetoothAdapter == null) {
				// Util.openCustomDialog("error", "Bluetooth is not available");
				AlertDialogCustom dialog = new AlertDialogCustom(BarCodeScanActivity.this);
				dialog.setParamenters("error", "Bluetooth is not available", null, null, 1, false);
				dialog.setAlertImage(R.drawable.alert_error, "error");
				dialog.show();
				// openAlertDialog("Bluetooth is not available","error",
				// BarCodeScanActivity.this);
				// finish();
				return;
			}

			_bluetoothIsOn = _bluetoothAdapter.isEnabled();
			if (!_bluetoothIsOn) {
				Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public Object onRetainNonConfigurationInstance() {
		super.onRetainNonConfigurationInstance();
		try {
			NonConfiguration object = new NonConfiguration();
			object.setDecodedData(scanned_value);

			return object;
		} catch (Exception e) {

			e.printStackTrace();
		}
		return null;
	}



	public void CreateQrcode(String btAddress,ImageView img_barcode) {
		try {
			WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
			Display display = manager.getDefaultDisplay();

			@SuppressWarnings("deprecation")
			int width = display.getWidth();// point.x;
			@SuppressWarnings("deprecation")
			int height = display.getHeight();
			int smallerDimension = width < height ? width : height;
			smallerDimension = smallerDimension * 3 / 4;

			QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(btAddress, null, Contents.Type.TEXT,
					BarcodeFormat.QR_CODE.toString(), smallerDimension);

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			// int ht = metrics.heightPixels;
			// int wd = metrics.widthPixels;

			try {
				Bitmap b = qrCodeEncoder.encodeAsBitmap();
				img_barcode.setImageBitmap(b);
			} catch (WriterException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			//Log.i("------------On Activity Result-----------",":"+quick_scan);
			if(requestCode == REQUEST_ENABLE_BT){
				if (_bluetoothIsOn) {
					ScannerSettingsApplication.getInstance().increaseViewCount();;
				}
			}else if(requestCode==Util.DASHBORD_ONACTIVITY_REQ_CODE){
				Intent i = new Intent(BarCodeScanActivity.this, SplashActivity.class);
				startActivity(i);
				finish();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			releaseCamera();
			/*Intent startentent = new Intent(BarCodeScanActivity.this,DashboardActivity.class);
			startentent.putExtra("CheckIn Event", checkedin_event_record);
			startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startentent.putExtra(Util.HOME, "home_layout");
			startActivity(startentent);*/
			/*Intent result = new Intent();
			setResult(1987, result);*/

			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onPause() {
		super.onPause();
		/*if (_bluetoothIsOn == true) {
			ScannerSettingsApplication.getInstance().decreaseViewCount();
		}*/
	}

	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};

	public void scanStart() {
		try {
			if (barcodeScanned) {
				barcodeScanned = false;
				mCamera.setPreviewCallback(previewCb);
				mCamera.startPreview();
				previewing = true;
				mCamera.autoFocus(autoFocusCB);
				//hideSoftKeyboard(BarCodeScanActivity.this);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			if(frontcam){
				//currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

				c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
			}else {
				//c.CameraInfo.CAMERA_FACING_FRONT;
				currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
				c = Camera.open(currentCameraId);
			}
		} catch (Exception e) {
			c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
			e.printStackTrace();
		}
		return c;
	}

	public void releaseCamera() {
		try {
			if (mCamera != null) {
				previewing = false;
				mCamera.setPreviewCallback(null);
				mCamera.release();
				mCamera = null;
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	private final Runnable doAutoFocus = new Runnable() {
		@Override
		public void run() {
			try {
				if (previewing)
					if(mCamera!=null)
						mCamera.autoFocus(autoFocusCB);
					else
					{
						releaseCamera();
						finish();
					}
			}catch (Exception e){
				releaseCamera();
				finish();
				e.printStackTrace();
			}
		}
	};

	PreviewCallback previewCb = new PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			try {
				Camera.Parameters parameters = camera.getParameters();
				Size size = parameters.getPreviewSize();

				Image barcode = new Image(size.width, size.height, "Y800");

				barcode.setData(data);

				int result = scanner.scanImage(barcode);

				if (result != 0) {
					previewing = false;
					mCamera.setPreviewCallback(null);
					mCamera.stopPreview();

					SymbolSet syms = scanner.getResults();
					for (Symbol sym : syms) {

						Result result1 = new Result(sym.getData(),
								sym.getDataBytes(), null, BarcodeFormat.QR_CODE);
						ResultHandler resultHandler = ResultHandlerFactory
								.makeResultHandler(BarCodeScanActivity.this,
										result1);
						handleDecodeInternally(result1, resultHandler, null);
						barcodeScanned = true;

					}
				}
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	};

	@SuppressWarnings("unused")
	private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
		try {
			// onPause();
			attendee_qrCode_id = "";
			ParsedResultType QRtype = resultHandler.getType();
			isOrderScaneed = false;
			CharSequence displayContents = resultHandler.getDisplayContents();
			//Log.i("------------------------------------Scan result---------", "Vlaue " + displayContents.toString());
			/*MediaPlayer p = MediaPlayer.create(this, R.raw.beep);
			p.start();*/
			//playSound(R.raw.badgescanned);
			orderId = displayContents.toString();
			isOrderScaneed = false;
			//releaseCamera();
			if (NullChecker(fromclass_name).equalsIgnoreCase(CollectOrderInfo.class.getName())) {
				releaseCamera();
				Intent i = new Intent();
				i.putExtra(Util.INTENT_KEY_1, NullChecker(orderId));
				i.putExtra(Util.INTENT_KEY_2, getIntent().getIntExtra(Util.INTENT_KEY_2, 0));
				setResult(100, i);
				finish();
			} else if (NullChecker(fromclass_name).equalsIgnoreCase(AttendeeDetailActivity.class.getName())) {
				releaseCamera();
				Intent i = new Intent();
				//Log.i("------------------------------------Scan result---------", "Vlaue " + orderId);
				i.putExtra(Util.INTENT_KEY_1, NullChecker(orderId));
				/*i.putExtra("EVENT_ID", getIntent().getStringExtra("EVENT_ID"));
				i.putExtra("ATTENDEE_ID", getIntent().getStringExtra("ATTENDEE_ID"));
				i.putExtra("ORDER_ID", getIntent().getStringExtra("ORDER_ID"));*/
				setResult(100, i);
				finish();
			} else {
				Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
				ExternalSettings ext_settings = new ExternalSettings();
				if(!Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, "").isEmpty()){
					ext_settings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, ""), ExternalSettings.class);
				}
				if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
					getAttendeeCursor();
					checkForSelfCheckinWithSingleAttendee();
				}else {
					/*if (ext_settings.quick_checkin && !isOrderScanned(orderId) &&!Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
						//quick_scan = true;
						Intent i = new Intent(BarCodeScanActivity.this, TransperantGlobalScanActivity.class);
						i.putExtra(Util.SCANDATA, orderId.toCharArray());
						i.putExtra(Util.ACTION, BarcodeScanActivity.class.getName());
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
						//finish();
					}*//*else if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
						if(getAttendeeCursor().getCount()==1){
							Intent i = new Intent(BarCodeScanActivity.this, TransperantGlobalScanActivity.class);
							i.putExtra(Util.SCANDATA, orderId.toCharArray());
							i.putExtra(Util.ACTION, BarcodeScanActivity.class.getName());
							i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(i);
						}else{
							releaseCamera();
							Intent i = new Intent(BarCodeScanActivity.this, GlobalScanActivity.class);
							i.putExtra(Util.SCANDATA, orderId.toCharArray());
							i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(i);
							finish();
						}
					}*//*
					else*/
					if (!orderId.trim().isEmpty()) {
						if (ext_settings.quick_checkin) {
							//quick_scan = true;
							Intent i = new Intent(BarCodeScanActivity.this, TransperantGlobalScanActivity.class);
							i.putExtra(Util.SCANDATA, orderId.toCharArray());
							i.putExtra(Util.ACTION, BarcodeScanActivity.class.getName());
							i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(i);
							//finish();
						} else {
							releaseCamera();
							Intent i = new Intent(BarCodeScanActivity.this, GlobalScanActivity.class);
							i.putExtra(Util.SCANDATA, orderId.toCharArray());
							i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(i);
							finish();
						}
					}else {
						showCustomToast(this,
								"No data in Barcode or Qrcode",
								R.drawable.img_like, R.drawable.toast_redrounded, false);
						//releaseCamera();
						barcodeScanned=true;
						scanStart();
					}
				}
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void showServiceRunningAlert(String event_name){
		Util.setCustomAlertDialog(BarCodeScanActivity.this);
		Util.openCustomDialog("Alert", event_name);

		Util.txt_dismiss.setVisibility(View.GONE);
		Util.setCustomDialogImage(R.drawable.alert);
		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Util.alert_dialog.dismiss();
				startActivity(new Intent(BarCodeScanActivity.this,BarCodeScanActivity.class));
				finish();
			}
		});
	}

	private void checkForSelfCheckinWithSingleAttendee() {
		try {
			//if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
			if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)&&Util.getselfcheckinbools(Util.ISPRINTALLOWED)&&Util.getselfcheckinbools(Util.ISALLOWSCANTOPRINT)) {
				String whereClause = "";
				if(BaseActivity.isBadgeScanned(orderId)){
					//if(Util.getselfcheckinbools(Util.ISAUTOCHECKIN)){
					Intent i = new Intent(BarCodeScanActivity.this, TransperantGlobalScanActivity.class);
					i.putExtra(Util.SCANDATA, orderId.toCharArray());
					i.putExtra(Util.ACTION, BarcodeScanActivity.class.getName());
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				} else {
					if (BaseActivity.isOrderScanned(orderId)) {
						whereClause = " where Event_Id='" + checked_in_eventId + "' AND Order_Id='" + orderId.trim() + "'";
						attendee_cursor = Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
					} else if(isticketScanned(orderId)){
						isOrderScaneed= false;
						attendee_cursor = Util.db.getAllAttendeeswithBadgeId(orderId);
					}else {
						whereClause = " where Event_Id='" + checked_in_eventId + "' AND (BadgeId='" + orderId.trim() + "' OR " + DBFeilds.ATTENDEE_CUSTOM_BARCODE + " = '" + orderId.trim() + "')";
						attendee_cursor = Util.db.getAttendeeDataCursorForScan(whereClause);
					}
					if (attendee_cursor.getCount() == 0) {
						doRequest();
					} else if (attendee_cursor.getCount() == 1) {
						attendee_cursor.moveToFirst();
						if (NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE))).equalsIgnoreCase("B - Badge")&&!isBadgeScanned(orderId)) {
							FrameLayout print_badge = (FrameLayout) linearview.findViewById(R.id.frame_attdetailqrcodebadge);
							FrameLayout frame_transparentbadge = (FrameLayout) linearview.findViewById(R.id.frame_transparentbadge);
							attendee_cursor.moveToFirst();
							String attendeeid = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
							printT = new PrintAndCheckin();
							printDetails = new PrintDetails();
							printDetails.attendeeId = attendeeid;
							printDetails.checked_in_eventId = checked_in_eventId;
							printDetails.frame_transparentbadge = frame_transparentbadge;
							printDetails.order_id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID));
							printDetails.print_badge = print_badge;
							printDetails.sfdcddetails = sfdcddetails;
							/**these params are required when any order/badge will scanned**/
							printDetails.attendeeWhereClause = whereClause;
							printDetails.isOrderScaneed = isOrderScaneed;
							printDetails.qrCode = orderId;
							//SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor=attendee_cursor;
							if (!(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty()) && !(Util.getselfcheckinbools(Util.ISREPRINTALLOWED))) {

								String sessionid = Util.db.getSwitchedONGroupId(checked_in_eventId);
								String status = Util.db.getTStatusBasedOnGroup(attendee_cursor.
												getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)),
										attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
								boolean ischeckin = Boolean.valueOf(NullChecker(status));
								if (!sessionid.isEmpty() && ischeckin) {
									Util.setCustomAlertDialog(BarCodeScanActivity.this);
									Util.alert_dialog.setCancelable(false);
									Util.openCustomDialog("Alert", "Your Badge is Already Printed.Please contact Event Organizer!");
									Util.txt_okey.setText("OK");
									Util.txt_dismiss.setVisibility(View.GONE);
									Util.txt_okey.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View arg0) {
											Util.alert_dialog.dismiss();
											startActivity(new Intent(BarCodeScanActivity.this, BarCodeScanActivity.class));
											finish();
										}
									});
								} else {
									showCustomToast(this,
											"Your Badge is Already Printed.Please contact Event Organizer!",
											R.drawable.img_like, R.drawable.toast_redrounded, false);
									if (!sessionid.isEmpty()) {
										ticketCheckin(this, attendee_cursor, sessionid);
									}else{
										finish();
									}
								}
						/*AlertDialogCustom dialog = new AlertDialogCustom(
								BarCodeScanActivity.this);
						dialog.setParamenters("Alert",
								"Your Badge is Already Printed.Please contact Event Organizer!", null, null,
								1, false);
						dialog.show();*/
							} else if(!(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty()) && (Util.getselfcheckinbools(Util.ISREPRINTALLOWED))){
								if(!BaseActivity.isValidate_badge_reg_settings&&isBadgeSelected()){
									printT.doSaveAndPrint(this, printDetails);
								}else if(isBadgeSelected()) {
									openprintDialog();
								}else {
									BaseActivity.showSingleButtonDialog("Alert",
											"No Badge Selected, Please contact your Event Organizer!",this);
								}
							}else {
								if(!BaseActivity.isValidate_badge_reg_settings&&isBadgeSelected()) {
									printT.doSaveAndPrint(this, printDetails);
								}else if(BaseActivity.isValidate_badge_reg_settings&&isBadgeSelected()){

								}else{
									BaseActivity.showSingleButtonDialog("Alert",
											"No Badge Selected, Please contact your Event Organizer!",this);
								}
							}
						}else{
							String sessionid = Util.db.getSwitchedONGroupId(checked_in_eventId);
							String status=Util.db.getTStatusBasedOnGroup(attendee_cursor.
											getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)),
									attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
							boolean ischeckin = Boolean.valueOf(NullChecker(status));
							if (!NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE))).equalsIgnoreCase("B - Badge")) {
								showCustomToast(this,
										"Order ID contains NON-Badgeable ticket",
										R.drawable.img_like, R.drawable.toast_redrounded, false);
							}
							if (!sessionid.isEmpty()&&!ischeckin) {
								ticketCheckin(this, attendee_cursor, sessionid);
							}
							barcodeScanned=true;
							scanStart();
						}
					}/*else if(attendee_cursor.getCount() == 1&&!NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE))).equalsIgnoreCase("B - Badge")){
						String sessionid = Util.db.getSwitchedONGroupId(checked_in_eventId);
						String status=Util.db.getTStatusBasedOnGroup(attendee_cursor.
										getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)),
								attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
						boolean ischeckin = Boolean.valueOf(NullChecker(status));
						showCustomToast(this,
								"Order ID contains NON-Badgeable ticket",
								R.drawable.img_like, R.drawable.toast_redrounded, false);

						if (!sessionid.isEmpty()&&!ischeckin) {
							ticketCheckin(this, attendee_cursor, sessionid);
						}
						barcodeScanned=true;
						scanStart();
					}*/ else {
						//	isScannedItemNotopened();
						releaseCamera();
						Intent i = new Intent(BarCodeScanActivity.this, GlobalScanActivity.class);
						i.putExtra(Util.SCANDATA, orderId.toCharArray());
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
						finish();
					}
				}
			}else if(Util.getselfcheckinbools(Util.ISONLYSCANCHECKIN)){
				try {
					if(attendee_cursor.getCount()==0){
						if(isOnline()) {
							doRequest();
						}else{
							startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
						}
					}else if(attendee_cursor.getCount()==1){
						Intent i = new Intent(BarCodeScanActivity.this, TransperantGlobalScanActivity.class);
						i.putExtra(Util.SCANDATA, orderId.toCharArray());
						i.putExtra(Util.ACTION, BarcodeScanActivity.class.getName());
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
					}else{
						releaseCamera();
						Intent i = new Intent(BarCodeScanActivity.this, GlobalScanActivity.class);
						i.putExtra(Util.SCANDATA, orderId.toCharArray());
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
						finish();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			/*else if(Util.getselfcheckinbools(Util.ISONLYSCANCHECKIN)&& getAttendeeCursor().getCount()==1){
				try {
					Intent i = new Intent(BarCodeScanActivity.this, TransperantGlobalScanActivity.class);
					i.putExtra(Util.SCANDATA, orderId.toCharArray());
					i.putExtra(Util.ACTION, BarcodeScanActivity.class.getName());
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}*/
		}catch (Exception e){
			e.printStackTrace();
		}
	}
/*
	private void isScannedItemNotopened(){
		String whereClause = " where Even" +
				"t_Id='" + checked_in_eventId  + "' AND (BadgeId='" + orderId.trim() + "' OR "+DBFeilds.ATTENDEE_CUSTOM_BARCODE+" = '"+orderId.trim()+"')";
		attendee_cursor = Util.db.getAttendeeDetailsWithAllTypes(whereClause);
		*/
/*MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.somethingwentwrong);
		mediaPlayer.start();*//*


		if(attendee_cursor == null){
			openCancelledOrderAlert("Error", "This badge is not valid for this session. Please check with event admin.");
		}else if(attendee_cursor.getCount() > 0){
			if(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_TICKET_STATUS))).equalsIgnoreCase("Cancelled")){
				openCancelledOrderAlert("Error", "This ticket is cancelled. Please check with event admin.");
			}else{
				openScanSettingsAlert(BarCodeScanActivity.this,attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),TransperantGlobalScanActivity.class.getName());
			}
		}
	}
*/

	@Override
	public void setCustomContentView(int layout) {
		try {
			activity = this;
			View v = inflater.inflate(layout, null);

			linearview.addView(v);
			txt_title.setText("Scan QR Code");
			img_setting.setVisibility(View.GONE);
			img_menu.setImageResource(R.drawable.back_button);
			img_socket_scanner.setVisibility(View.GONE);
			img_scanner_base.setVisibility(View.GONE);
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			event_layout.setVisibility(View.GONE);
			button_layout.setVisibility(View.GONE);
			event_layout.setVisibility(View.VISIBLE);
			img_refund_history.setVisibility(View.GONE);
			if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
				img_search.setVisibility(View.GONE);
			}else {
				img_search.setVisibility(View.VISIBLE);
				img_search.setImageResource(R.drawable.info_dark);
			}
			frame = (FrameLayout) findViewById(R.id.indicator);
			img_frame = (ImageView) findViewById(R.id.frame);
			camera_layout = (FrameLayout) findViewById(R.id.cameraPreview);
			layout_session =(LinearLayout)linearview.findViewById(R.id.lay_session_name);
			txt_session_name = (TextView)linearview.findViewById(R.id.txt_session_name);

		} catch (Exception e) {
			e.printStackTrace();
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

	@Override
	public void doRequest() {
		String access_token = sfdcddetails.token_type + " " + sfdcddetails.access_token;
		postMehod = new HttpPostData("Attendee Loading...",setTicketInfoUrl(), null, access_token,BarCodeScanActivity.this);
		postMehod.execute();
	}
	public String setTicketInfoUrl() {
		try {
			String url="";
//if (!mPrintData.isOrderScaneed) {
			url = sfdcddetails.instance_url + WebServiceUrls.SA_SCAN_TICKET +getTicketValues();
/*} else {

//Log.i("----------Scan result---------", " ORCode === " + QrId);
url = mPrintData.sfdcddetails.instance_url
+ WebServiceUrls.SA_TICKETS_SCAN_URL + "EventId="
+ mPrintData.checked_in_eventId + "&UserId=" + BaseActivity.user_profile.Userid
+ "&QRcode=" + mPrintData.qrCode;
}*/
			return url;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getTicketValues(){
		List<ScannedItems> items = Util.db.getSwitchedOnScanItem(checked_in_eventId);
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("Event_id", checked_in_eventId));
		values.add(new BasicNameValuePair("User_id", BaseActivity.user_profile.Userid));
		values.add(new BasicNameValuePair("Order_Id", orderId));
		if(items.size() > 0){
			values.add(new BasicNameValuePair("itemId", items.get(0).BLN_Item_Pool__c));
		}



		return AppUtils.getQuery(values);
	}
	/*
     * (non-Javadoc)
     * 
     * @see
     * com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
     */
	@Override
	public void parseJsonResponse(String response) {
		Context mContext = BarCodeScanActivity.this;
		try {
			//if (requestType.equals(Util.GET_TICKET)) {
			JSONObject order_obj = new JSONObject(response);
			Gson gson = new Gson();
			TotalOrderListHandler totalorderlisthandler = gson.fromJson(response, TotalOrderListHandler.class);
			if (totalorderlisthandler.TotalLists.size() > 0) {
				isOrderScaneed = Boolean.valueOf(NullChecker((order_obj.optString("isorder"))));
				Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
				if (totalorderlisthandler.TotalLists.get(0).orderInn.getOrderStatus().equalsIgnoreCase("Cancelled")) {
					String msg = "This order is cancelled.";
					if (totalorderlisthandler.TotalLists.get(0).orderInn.getOrderTotalAmount() > 0) {
						msg = "This order is cancelled please refund " + String.format("%.2f", totalorderlisthandler.TotalLists.get(0).orderInn.getOrderTotalAmount()) + " to " + totalorderlisthandler.TotalLists.get(0).orderInn.buyerInfo.getFirstName() + " "
								+ totalorderlisthandler.TotalLists.get(0).orderInn.buyerInfo.getLastName();
					}
					openCancelledOrderAlert("Error", msg);
				} else {
					//orderId =totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId();
					getAttendeeCursor();
					if(attendee_cursor.getCount()==0){
						showServiceRunningAlert("Please Scan only Order QR Code of "+checkedin_event_record.Events.Name);
					}else {
						checkForSelfCheckinWithSingleAttendee();
					}
					//checkForSelfCheckinWithSingleAttendee();
						/*Cursor attendee_cursor = Util.db.getAttendeeDataCursorForScan(mPrintData.attendeeWhereClause);
						mPrintData.attendeeId = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
						mPrintData.order_id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID));
						checkData();*/
				}
			} else {
				((BaseActivity) mContext).playSound(R.raw.somethingwentwrong);
				Util.setCustomAlertDialog(mContext);
				Util.alert_dialog.setCancelable(true);
				Util.txt_dismiss.setVisibility(View.GONE);
				Util.setCustomDialogImage(R.drawable.error);
				Util.txt_okey.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
							//onResume();
							scanStart();
						}
						Util.alert_dialog.dismiss();
					}
				});
				Util.txt_dismiss.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
							//onResume();
							scanStart();
						}
						Util.alert_dialog.dismiss();
					}
				});
				String eventName="";

				if(BaseActivity.checkedin_event_record!=null)
					eventName=AppUtils.NullChecker(BaseActivity.checkedin_event_record.Events.Name);
				else
					eventName="";

				if(eventName.trim().equalsIgnoreCase(""))
					eventName="this";

				Util.openCustomDialog("Alert","Invalid Badge \n This Order does not belongs to "+eventName+" event.");

				//Util.openCustomDialog("Alert", "This Order does not belongs to this event.");
				if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
					Util.alert_dialog.setCancelable(true);
				}
			}
			Util.alert_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
						//onResume();
						scanStart();
					}
				}
			});

			//}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class NonConfiguration extends Object {
		private String _symbologyName;
		private String _decodedData;
		private String _decodedDataLength;

		public void setSymbologyName(String name) {
			_symbologyName = name;
		}

		public String getSymbologyName() {
			return _symbologyName;
		}

		public void setDecodedData(String decodedData) {
			_decodedData = decodedData;
		}

		public String getDecodedData() {
			return _decodedData;
		}

		public void setDecodedDataLength(String decodedDataLength) {
			_decodedDataLength = decodedDataLength;
		}

		public String getDecodedDataLength() {
			return _decodedDataLength;
		}
	}
	public void openCancelledOrderAlert(String alert,String msg) {
		/*MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.somethingwentwrong);
		mediaPlayer.start();*/
		playSound(R.raw.somethingwentwrong);
		Util.setCustomAlertDialog(this);
		Util.txt_dismiss.setVisibility(View.GONE);
		Util.setCustomDialogImage(R.drawable.error);
		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
				// ShowTicketsDialog();
                /*Intent startentent = new Intent(mContext,DashboardActivity.class);
                startentent.putExtra("CheckIn Event", mPrintDatacheckedin_event_record);
                startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startentent.putExtra(Util.HOME, "home_layout");
                startActivity(startentent);
                finish();*/
			}
		});
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Util.alert_dialog.dismiss();
			}
		});
		Util.openCustomDialog(alert, msg);
	}
	private Cursor getAttendeeCursor(){
		String whereClause = "";
		if (BaseActivity.isOrderScanned(orderId)) {
			whereClause = " where Event_Id='" + checked_in_eventId + "' AND Order_Id='" + orderId.trim() + "'";
			attendee_cursor = Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
		} else if(isticketScanned(orderId)){
			isOrderScaneed= false;
			attendee_cursor = Util.db.getAllAttendeeswithBadgeId(orderId);
		}else {
			whereClause = " where Event_Id='" + checked_in_eventId + "' AND (BadgeId='" + orderId.trim() + "' OR " + DBFeilds.ATTENDEE_CUSTOM_BARCODE + " = '" + orderId.trim() + "')";
			attendee_cursor = Util.db.getAttendeeDataCursorForScan(whereClause);
		}
		attendee_cursor.moveToFirst();
		return attendee_cursor;
	}
	public void openprintDialog() {
		final Context context = BarCodeScanActivity.this;
		try {
			print_dialog = new AlertDialog.Builder(BarCodeScanActivity.this);
			LayoutInflater li = LayoutInflater
					.from(BarCodeScanActivity.this);
			View promptsView = li.inflate(R.layout.print_dialog_layout, null);
			print_dialog.setView(promptsView);
			final EditText edit_reason = (EditText) promptsView.findViewById(R.id.edit_reason);
			final TextView txt_message=(TextView) promptsView.findViewById(R.id.txt_message);
			final TextView txt_top=(TextView) promptsView.findViewById(R.id.textView1);
			edit_reason.setText("");
			if(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty()){
				txt_message.setVisibility(View.VISIBLE);
				edit_reason.setVisibility(View.GONE);
			}else{
				txt_top.setText("Badge is already printed. Do you want to reprint ?\n" +
						"The previous badge will become invalid.");
				txt_message.setVisibility(View.GONE);
				edit_reason.setVisibility(View.VISIBLE);
			}
			print_dialog
					.setCancelable(false)
					.setPositiveButton("Reprint",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									reason = edit_reason.getText().toString();
									if(!attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty()){
										if (!reason.equalsIgnoreCase("") ) {
											try {
												dialog.dismiss();
												printDetails.reason=edit_reason.getText().toString();
												printT.doSaveAndPrint(context, printDetails);
											} catch (SQLException e) {
												e.printStackTrace();
											}
										}else if(reason.trim().isEmpty()){
											isReasonEmpty=true;
											edit_reason.setFocusable(true);
											edit_reason.setError("Reason should not be empty");

										}
									}
								}})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							isReasonEmpty=false;
							hideSoftKeyboard(BarCodeScanActivity.this);
							barcodeScanned=true;
							scanStart();
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
							//hideSoftKeyboard(BarCodeScanActivity.this);
							if (isReasonEmpty) {
								hideSoftKeyboard(BarCodeScanActivity.this);
								edit_reason.setEnabled(false);
								alertDialog.dismiss();
								alertDialog.show();
								edit_reason.setError("Reason should not be empty");
								edit_reason.requestFocus();
							} else {
								return;
							}

						}
					});
			alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
			txt_message.setText("Do you want to print the badge?");




		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
