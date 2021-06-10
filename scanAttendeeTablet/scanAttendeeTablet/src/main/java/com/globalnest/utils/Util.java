package com.globalnest.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.globalnest.BackgroundReciver.DownloadService;
import com.globalnest.database.DBFeilds;
import com.globalnest.database.DataBase;
import com.globalnest.mvc.DashboradHandler;
import com.globalnest.printer.ZebraPrinter;
import com.globalnest.scanattendee.BaseActivity;
import com.globalnest.scanattendee.R;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class Util {

	public static enum ITEM_TYPES {
		Package
	}

	public static enum FEILDS {
		FULLNAME, FIRSTNAME, LASTNAME, COMPANY, TITLE, TICKETTYPE, BADGELABEL, BADGESTATUS, BUYERNAME,SEATNO, PAYMENTSTATUS, ORDERID, TICKETID, BADGEID, UNIQUEID, EMAIL ,UNKNOWN
		,TICKETNAME
	}

	public static DataBase db;
	public static Dialog alert_dialog;
	public static TextView txt_alert_title, txt_alert_msg;
	public static Button txt_okey, txt_dismiss,close_button;
	public static ImageView img_alert;
	public final static int DASHBORD_ONACTIVITY_REQ_CODE = 2015;
	public final static String PIC_FROM_GALLERY = "pic_from_gallery";
	public final static String PIC_FROM_CAMERA = "pic_from_camera";
	public final static String CHECK_SERVICE = "check_service";
	public static final String TICKET_ID = "ticket_id";
	public static final String ATTENDEE_NAME = "attendee_name";
	public static final String ATTENDEE_ID = "ATTENDEE_ID";
	public static final String ORDER_ID = "ORDER_ID";
	public static final String CLASSNAME = "classname";
	public static final String ORDER_CLOUD_PRINT = "ORDER_CLOUD_PRINT";
	public static final String EVENT_ID = "EVENT_ID";
	public final static String APP_FOLDER_NAME = "/ScanAttendee";
	public final static String eventpref = "Event Pref";
	public final static String loginpref = "Login Pref";
	public final static String OFFSET_PREF = "offset_pref";
	// public final static String OFFSET_STRING = "offset_string";
	public static final String FIRSTIME_LOGIN_PREF = "first_time_login_pref";
	public static final String SELECTED_SESSION_ATTENDEE_PREF = "selected_session_attendee_pref";
	public static final String SELECTED_SESSION_ATTENDEE_KEY = "selected_session_attendee_key";
	public static final String SOCKET_DEVICE_PREF = "socket_device_pref_key";
	public static final String SOCKET_DEVICE_CONNECTED = "socket_device_connected";
	public static final String SOCKET_DEVICE_NAME = "socket_device_name";
	public static final String SOCKET_DEVICE_BATTERY_LEVEL = "socket_device_battery";
	public static final String DASHBOARD_DATA_PREF = "dashboard_data_pref";
	public final static String SESSION_EVENT_ID = "session_event_id";
	public final static String EVENT_CURRENT_SESSION_ID = "event_current_session_id";
	public final static String CHECKIN_ONLY_PREF = "checkin_only_pref";
	public final static String SCANMODE_CHECKIN_PREF = "scanmode_checkin_pref";
	public final static String SELECTED_PRINTER_PREF = "selected_printer_pref";
	public final static String SESSION_COUNT_PREF = "sessionCountPref";
	public final static String SELFCHECKIN_COUNT_PREF = "selfcheckinCountPref";
	public final static String TEMPSELFCHECKIN_COUNT_PREF = "tempselfcheckinCountPref";
	public final static String LASTMODIFIDEDATE = "lastmodifidedate";
	public final static String DEVICEBLUETOOTHADDRESS = "devicebluetoothaddress";

	public final static String NOPRINTPERMISSION = "Please Buy ScanAttendee Ticket for Printing Permission!";
	public static boolean isInternetComing=true;
	//public final static String CHECKIN_ONLY_KEY = "checkin_only_key";

	public static int slide_menu_id = 0;
	public final static String attendeepref = "Attendee pref";
	public static SharedPreferences eventPrefer, login_prefer, attendee_info_pref, external_setting_pref, offset_pref,
			first_login_pref, selected_session_attedee_pref, socket_device_pref, dashboard_data_pref,order_request,order_Items,totalorderlisthandler,
			checkin_only_pref,scanmode_checkin_pref,selectedPrinterPref,sessionCountPref;
	public  static SharedPreferences selfcheckinpref,tempselfcheckinpref;
	public static SharedPreferences lastModifideDate,devicebluetoothaddress;
	public static SharedPreferences printStatusupdateAttendees;
	public final static String ISDATAEDITABLE="isdataeditable",ISPRINTALLOWED="isprintallowed",
			ISPRINTNOTALLOWED="isprintnotallowed",ISREPRINTALLOWED="isreprintallowed",
			ISAUTOCHECKIN="isautocheckin",ISADDGUEST="isaddguest",ISALLOWSCANTOPRINT="isallowscantoprint",
			ISSELFCHECKIN="isselfcheckin",PASSWORD="selfcheckinpassword",ISONLYSCANCHECKIN="isonlyscancheckin",
			ISCHECKINALLOWED="ischeckinallowed",ISPAIDTICKETSALLOWED="ispaidticketsallowed",ISFREETICKETSALLOWED="isfreeticketsallowed"
			,ISSEARCHVALIDATIONON="issearchvalidation",FIRSTNAMESEARCH="firstnamesearch",LASTNAMESEARCH="lastnamesearch",
			EMAILIDSEARCH="emailidsearch",COMPANYSEARCH="companysearch",ORDERIDSEARCH="orderidsearch",TICKETNOSEARCH="ticketnosearch";
	public final static String TEMPISDATAEDITABLE="tempisdataeditable",TEMPISPRINTALLOWED="tempisprintallowed",
			TEMPISPRINTNOTALLOWED="tempisprintnotallowed",TEMPISREPRINTALLOWED="tempisreprintallowed",
			TEMPISAUTOCHECKIN="tempisautocheckin",TEMPISADDGUEST="tempisaddguest",TEMPISALLOWSCANTOPRINT="tempisallowscantoprint",
			TEMPISSELFCHECKIN="tempisselfcheckin",TEMPISONLYSCANCHECKIN="tempisonlyscancheckin",TEMPISALLOWCHECKIN="tempisallowcheckin"
			,TEMPISPAIDTICKETSALLOWED="tempispaidticketsllowed",TEMPISFREETICKETSALLOWED="tempisfreeticketsallowed"
			,TEMPISSEARCHVALIDATIONON="tempissearchvalidation",TEMPFIRSTNAMESEARCH="tempfirstnamesearch",TEMPLASTNAMESEARCH="templastnamesearch",
			TEMPEMAILIDSEARCH="tempemailidsearch",TEMPCOMPANYSEARCH="tempcompanysearch",TEMPORDERIDSEARCH="temporderidsearch",TEMPTICKETNOSEARCH="tempticketnosearch";
	public static DashboradHandler dashboardHandler = new DashboradHandler();
	// public final static String APP_ACCESS_TOKEN = "OAUTH TOKEN";
	// public static final String APP_ACCESS_TOKEN_TIME = "TOKENTIME";
	public final static String NEXT = "Next >>";
	public final static String ADDEVENT = "Add Event";
	public final static String CLASS_NAME = "Class_name";
	public final static String ADDED_EVENT_ID = "Added_Event_Id";
	public final static String EVENT_ACTION = "EVENT ACTION";

	public final static String EVENT_CHECKIN_ID = "EVENT ID";
	public final static String USER_AUTO_LOGIN = "AUTO LOGIN";
	public final static String USER_EMAIL_ID = "USER_EMAIL";
	public final static String USER_ID = "USER_ID";
	public final static String REFRESH_TOKEN = "REFRESH_TOKEN";
	public final static String INDEX = "index";
	public final static String ISFINISH = "ISFINISH";
	public static String ACTION = "action";
	public final static String PAYMENT_SETTING = "payment_setting";
	public final static String EDIT = "edit";
	public final static String SAVE = "save";
	public final static String DELETE = "delete";
	public final static String UPDATE_KEY = "update";
	public final static String PGATEWAY = "pgateway";
	public final static String UPDATE_BADGE = "Update";
	public final static String DASHBORD = "getdashbord";
	public final static String LOAD_BADGE = "load_badges";
	public final static String UPDATE_PRINTSTATUS = "update_printstatus";
	public final static String FORMCOUNT = "count";
	public final static String COPY_VALUES = "copy values";
	public final static String ATTENDEE_FNAME = "a_fname";
	public final static String ATTENDEE_LNAME = "a_lname";
	public final static String ATTENDEE_EMAIL = "a_email";
	public final static String ATTENDEE_COMPANY = "a_company";
	public final static String EDIT_CHECK_NO = "edit_check_no";
	public final static String CASH = "Cash";
	public final static String BLOCK_TICKET_MAP = "block_ticket_map";
	public final static String BLOCK_PACKAGE_TICKET = "block_package_ticket";
	public final static String ISCOLLECT_INFO_FROM_BUYER = "iscollect_info_from_buyer";
	public final static String CHECK = "Check";
	public final static String EDIT_EVENT_ID = "edit_event_id";
	public final static String EVENT_LIST = "event_list";
	public final static String SCANDATA = "scandata";
	public final static String ORDERID = "order_id";
	public final static String PAYPAL = "PayPal";
	public final static String CRADITCARD = "Credit Card";
	public final static String TICKETSETTING = "ticket setting";
	public final static String EVENT_REFRESH = "refresh";
	public final static String CHEKINS_REFRESH = "checkin_refresh";
	public static final String DEVICE_SESSION = "device_session";
	public static final String ITEMS_ORDERS_REFRESH = "ItemsAndOrders";
	public static final String EVENT_DETAILS = "EventDetails";
	public final static String CHECKIN = "checkin";
	public final static String GETTICKET = "getticket";
	public final static String GET_TICKET = "get_ticket_data";
	public final static String GET_BADGE_ID = "getbadgeid";
	public final static String GET_ATTENDEE = "getattendee";
	public final static String LIST = "list_layout";
	public final static String TICKET = "ticket_layout";
	public final static String HOME = "home_layout";
	public final static String ORDERS = "order_layout";
	public final static String BUYER_NAME = "buyer_name";
	public final static String SCAN = "scan_layout";
	public final static String GLOBAL_PRINT = "global_print";
	public final static String SPLIT_STRING = "$GN@";
	public static final String FREE = "Free";
	public static final String EXTERNAL_PAY = "External Pay Gateway";
	public static final String EXTERNAL_PREF = "external_pref";
	public static final String ORDER_REQUEST = "order_request";
	public static final String ORDER_REQUEST_STRING = "order_request_string";
	public static final String PRINT_STATUS = "print_status";
	public static final String ORDER_ITEMS_STRING = "order_items_string";
	public static final String ORDER_ITEMS = "order_items";
	public static final String ITEMSANDATTENDEESLASTMODITIFEDATE="itemsandattendeeslastmoditifedate";
	public static final String EVENTREFRESHLASTMODITIFEDATE="eventrefreshlastmoditifedate";

	public static final String SessionUserCheckOuts="SessionUserCheckOuts",
			SessionUserCheckIns="SessionUserCheckIns",
			SessionStartTime="SessionStartTime",
			SessionCheckOuts="SessionCheckOuts",
			SessionCheckIns="SessionCheckIns",
			TotalCount="TotalCount",
			SessionId="SessionId",
			SessionsToBeRefreshed="RefreshedSessions",
			isExternalDataDownloaded="isExternalDataDownloaded",
			isSessionRefreshed = "true";
	// public static final String EXTERNAL_STRING = "external_string";
	// public final static String CASH="cash";

	public final static String ACCESS_KEY_COLLECT_ORDER_INFO = "key collect order info";
	public final static String ACCESS_KEY_ATTENDEE_DETAIL = "ket attendee detail";
	public final static String COLLECT_ORDER_INFO = "collect order info";
	public final static String ATTENDEE_DETAIL = "Attendee Details";
	public final static String ATTENDEE_DETAIL_DATA = "Attendee Details Data";
	// public final static String APP_ACCESS_TOKEN_TYPE="TOKEN_TYPE";
	// public final static String APP_BASE_URL="INSTANCE_URL";
	// public final static String APP_REFREH_TOKEN="REFRESH_TOKEN";
	public static final String ONLY1FORM = "only_1form";
	public static final String COPY1STFORM = "copy-1stform";
	public static final String CART_TICKETS = "cart_tickets";
	public static final String INTENT_KEY_1 = "intent_key_1";
	public static final String INTENT_KEY_2 = "intent_key_2";
	public static final String INTENT_KEY_3 = "intent_key_3";
	public static final String INTENT_KEY_4 = "intent_key_4";
	public static final String TOTAL = "total_amount";
	public static final String DISCOUNTEDVALUE = "discountedvalue";
	public static final String AFTERDISCOUNTTOTALWITHTAX = "afterdiscounttotalwithtax";
	public static final String SERVICE_TAX = "service_tax";
	public static final String SERVICE_FEE = "servicefee";
	public static final String PROMOCODE = "promocode";
	public static final String ITEMSQUANTITY = "itemsquantity";
	public static final String BROTHER_DK_12345 = "(Brother DK1234)";
	public static final String BROTHER_DK_1202 = "(Brother DK1202)";
	public static final String BROTHER_DKN_5224 = "(Brother DKN5224)";
	// public final static String STRIPE_CLIENT_ID =
	// "ca_38gideD49DrWpka18X0KiZH9G7BUcGnT";
	// public final static String STRIPE_SECRET_KEY =
	// "sk_test_SEppbKiXBACmm9tRsJZkpB8l"; //"sk_test_J7Kl5CIyN1ONxhk558F8F321";

	public final static String PAYPAL_APP_KEY = "APP-80W284485P519543T";
	public static Typeface roboto_regular, roboto_bold, droid_boldItalic, droid_bold, droid_regrular, roboto_boldItalic,
			stylish_bold1, VarelaRound, OpenSans, raleway_regular, arial, arial_italic, futura, brush_scripts, calibri,
			papyrus, verdana_regular,verdana_bold, times_roman,sanfrancisco_iphonefont;
	public static final String FILEFORMAT = ".png";
	public static final String Registration_Type__c = "ScanAttendee";

	/*
	 * public static ItemTypeParse item_type_parser = new ItemTypeParse();
	 * public static TicketParser ticket_list_parser = new TicketParser();
	 *
	 * public static ItemParser item_list_parser = new ItemParser(); public
	 * static AttendeeParser attendee_list_parser = new AttendeeParser(); public
	 * static PaymentInfoParser payment_parser = new PaymentInfoParser(); public
	 * static RefundInfoParser refund_list_parser = new RefundInfoParser();
	 */
	final static public String APP_KEY = "g131nz3n5k0imd1";
	final static public String APP_SECRET = "fz3pd3bd18gvwbx";
	//final static public AccessType ACCESS_TYPE = AccessType.DROPBOX;
	final static public String ACCOUNT_PREFS_NAME = "prefs";
	final static public String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static public String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	public final static String STRIPE_ACCESS_TOKEN = "ACCESS_TOKEN";
	public final static String STRIPE_PUB_KEY = "PUBLISH_KEY";
	public final static String STRIPE_REFRESH_TOKEN = "REFRESH_KEY";
	public static boolean mLoggedIn;
	public final static String STRIPE_LOGIN = "STRIPE LOGIN";
	public static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
	public static SimpleDateFormat db_server_format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	public static SimpleDateFormat db_server_ticket_format = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
	public static SimpleDateFormat date_format = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
	public static SimpleDateFormat eventDate_format = new SimpleDateFormat("MMMM dd, yyyy h:mm a", Locale.US);
	public static SimpleDateFormat server_dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
	public static SimpleDateFormat db_date_format = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US);
	public static SimpleDateFormat new_db_date_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
	public static SimpleDateFormat db_date_format1 = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
	public static SimpleDateFormat past_event_format = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss", Locale.US);
	public static SimpleDateFormat past_event_format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
	public static SimpleDateFormat date_format_sec = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US);
	public static SimpleDateFormat date_format_new_sec = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US);
	public static final String CARDIO_APP_TOKEN = "4dc8320898ff4e05a7fb044e9cce1d6f";
	//public static DropboxAPI<AndroidAuthSession> mApi;
	public static String[] cnty = { "United States Of America", "India" };
	public static String[] indiastates = { "--None--", "Andhra Pradesh","Telangana", "Arunachal Pradesh", "Assam", "Bihar",
			"Chhattisgarh", "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jammu and Kashmir", "Jharkhand",
			"Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland",
			"Orissa", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Tripura", "Uttar Pradesh", "Uttarakhand",
			"West Bengal", "Andaman and Nicobar Islands", "Daman and Diu", "Lakshadweep",
			"National Capital Territory of Delhi" };
	public static String[] americastates = { "--None--", "Alabama", "Arizona", "Arkansas", "California", "Colorado",
			"Connecticut", "Delaware", "District of Columbia", "Florida", "Georgia", "Hawaii", "Idaho", "Illinois",
			"Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan",
			"Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey",
			"New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania",
			"Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia",
			"Washington", "West Virginia", "Wisconsin", "Wyoming" };

	public static NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
	public static Format in_nf = NumberFormat.getCurrencyInstance(new Locale("en", "in"));

	// Group default names
	public static final String BASIC_INFORMATION="Basic Information";
	public static final String WORK_INFORMATION="Work Information";
	public static final String WORK_ADDRESS="Work Address";
	public static final String HOME_ADDRESS="Home Address";
	public static final String BILLING_ADDRESS="Billing Address";
	public static final String SPEAKER_INFORMATION="Speaker Information";

	public static final String ATTENDEECOMPANY="Name";
	public static final String ATTENDEE_GENDER="gender__c";
	//values from list
	public static final String ATTENDEE_ETHNICITY="Ethnicity";
	public static final String ATTENDEE_BUSINESS_STRUCTURE="Business Structure";
	public static final String ATTENDEE_NUMBER_OF_EMPLOYEES="Number Of Employees";
	public static final String ATTENDEE_GEOGRAPHICAL_REGION="Geographical Region";
	public static final String ATTENDEE_REVENUE="Revenue";

	public static final String ATTENDEE_WORK_PHONE = "Work_Phone__c";
	public static final String ATTENDEE_WORK_ADDRESS_1 = "Work Address1";
	public static final String ATTENDEE_WORK_ADDRESS_2 = "Work Address2";
	public static final String ATTENDEE_WORK_CITY = "Work City";
	public static final String ATTENDEE_WORK_STATE = "Work State";
	public static final String ATTENDEE_WORK_COUNTRY = "Work Country";
	public static final String ATTENDEE_WORK_ZIPCODE = "Work Zip Code";

	public static final String ATTENDEE_HOME_PHONE = "Home_Phone__c";
	public static final String ATTENDEE_HOME_ADDRESS_1 = "Home Address1";
	public static final String ATTENDEE_HOME_ADDRESS_2 = "Home Address2";
	public static final String ATTENDEE_HOME_CITY = "Home City";
	public static final String ATTENDEE_HOME_STATE = "Home State";
	public static final String ATTENDEE_HOME_COUNTRY = "Home Country";
	public static final String ATTENDEE_HOME_ZIPCODE = "Home Zip Code";

	public static final String ATTENDEE_BILLING_PHONE = "Billing_Phone__c";
	public static final String ATTENDEE_BILLING_ADDRESS_1 = "Billing Address1";
	public static final String ATTENDEE_BILLING_ADDRESS_2 = "Billing Address2";
	public static final String ATTENDEE_BILLING_CITY = "Billing City";
	public static final String ATTENDEE_BILLING_STATE = "Billing State";
	public static final String ATTENDEE_BILLING_COUNTRY = "Billing Country";
	public static final String ATTENDEE_BILLING_ZIPCODE = "Billing Zip Code";

	public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
		Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Config.ARGB_8888);

		float scaleX = newWidth / (float) bitmap.getWidth();
		float scaleY = newHeight / (float) bitmap.getHeight();
		float pivotX = 0;
		float pivotY = 0;

		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

		Canvas canvas = new Canvas(scaledBitmap);
		canvas.setMatrix(scaleMatrix);
		canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

		return scaledBitmap;
	}

	public static String sessionDateTimeFormat(String date) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("EEE,MMM dd,yyyy hh:mm aa", Locale.US);
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
			/*
			 * TimeZone gmtTime = TimeZone.getDefault();//
			 * formatter.setTimeZone(gmtTime);
			 */
			return dateFormat.format(formatter.parse(date));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getESTFormat() {

		Calendar currentdatetime = Calendar.getInstance();
		TimeZone tz = TimeZone.getTimeZone("EST5EDT");
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.US);

		formatter.setTimeZone(tz);

		return formatter.format(currentdatetime.getTime());
	}

	public static String get_GMT_FormatNew(String date_time) {
		// 2015-12-04T14:00:00
		try {
			Date date = new_db_date_format.parse(date_time);
			TimeZone time_zone = TimeZone.getTimeZone("GMT");
			// new_db_date_format.setTimeZone(TimeZone.getTimeZone("GMT"));
			new_db_date_format.setTimeZone(time_zone);

			// eventDate_format.setTimeZone(time_zone);
			return new_db_date_format.format(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public static String get_EventTimeZone_Format(String time_zone, String date_time) {

		eventDate_format.setTimeZone(TimeZone.getTimeZone(time_zone));
		try {
			return eventDate_format.format(eventDate_format.parse(date_time));
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "";
	}
	public static String change_GMT_DateFormat(String dateandtime,String onlydate, String gmtFormat) {
		SimpleDateFormat actualformat=null;
		dateandtime = dateandtime.split("\\.")[0].trim();
		if(onlydate.isEmpty()){
			actualformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
		}else{
			dateandtime=onlydate;
			actualformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
			Date date=null;
			try {
				date =actualformat.parse(dateandtime);
			} catch (Exception e) {
				return dateandtime;
			}
			return format.format(date).toString();
		}
		TimeZone tzInAmerica = TimeZone.getTimeZone("GMT");
		actualformat.setTimeZone(tzInAmerica);

		SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
		TimeZone tzInAmerica1 = TimeZone.getTimeZone(gmtFormat);
		format.setTimeZone(tzInAmerica1);

		Date formetteddate = null;
		try {
			// //Log.i("-------------Actual Date-------------",":"+date_string);
			formetteddate = actualformat.parse(dateandtime);

		} catch (Exception e) {
			// TODO: handle exception
			// //Log.i("-------------Date Change
			// Exception-------------",":"+e.getMessage());
			// Log.i("***********************____________Event Date
			// Function___________",":"+e.getMessage());
			return "";
		}
		return format.format(formetteddate).toString();
	}

	public static String change_US_ONLY_DateFormat(String date_string, String gmtFormat) {

		date_string = date_string.split("\\.")[0].trim();
		SimpleDateFormat actualformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
		TimeZone tzInAmerica = TimeZone.getTimeZone("GMT");
		actualformat.setTimeZone(tzInAmerica);

		SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
		TimeZone tzInAmerica1 = TimeZone.getTimeZone(gmtFormat);
		format.setTimeZone(tzInAmerica1);

		Date formetteddate = null;
		try {
			// //Log.i("-------------Actual Date-------------",":"+date_string);
			formetteddate = actualformat.parse(date_string);

		} catch (Exception e) {
			// TODO: handle exception
			// //Log.i("-------------Date Change
			// Exception-------------",":"+e.getMessage());
			// Log.i("***********************____________Event Date
			// Function___________",":"+e.getMessage());
			return "";
		}
		return format.format(formetteddate).toString();
	}

	/*public static Date get_US_DateFormat(String event_date,String event_timezone){
		String date_string = ITransaction.EMPTY_STRING;
		try{
			event_date = event_date.split("\\.")[0].trim();
			SimpleDateFormat actualformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
			TimeZone tzGMT = TimeZone.getTimeZone("GMT");
			actualformat.setTimeZone(tzGMT);
			
			SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
			TimeZone tzEvent = TimeZone.getTimeZone(event_timezone);
			format.setTimeZone(tzEvent);
		}catch(Exception e){
			
		}
		return date_string;
	}*/
	public static String change_US_ONLY_DateFormatWithSEC(String date_string, String gmtFormat) {

		date_string = date_string.split("\\.")[0].trim();
		SimpleDateFormat actualformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
		TimeZone tzInAmerica = TimeZone.getTimeZone("GMT");
		actualformat.setTimeZone(tzInAmerica);

		SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.US);
		TimeZone tzInAmerica1 = TimeZone.getTimeZone(gmtFormat);
		format.setTimeZone(tzInAmerica1);

		Date formetteddate = null;
		try {
			// //Log.i("-------------Actual Date-------------",":"+date_string);
			formetteddate = actualformat.parse(date_string);

		} catch (Exception e) {
			// TODO: handle exception
			// //Log.i("-------------Date Change
			// Exception-------------",":"+e.getMessage());
			// Log.i("***********************____________Event Date
			// Function___________",":"+e.getMessage());
			return "";
		}
		return format.format(formetteddate).toString();
	}

	public static String changeGMTtoEventTimeZone(String date_string, String event_time_zone) {

		date_string = date_string.split("\\.")[0].trim();
		SimpleDateFormat actualformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
		TimeZone tzInAmerica = TimeZone.getTimeZone("GMT");
		actualformat.setTimeZone(tzInAmerica);

		SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.US);
		TimeZone tzInAmerica1 = TimeZone.getTimeZone(event_time_zone);
		format.setTimeZone(tzInAmerica1);

		Date formetteddate = null;
		try {
			AppUtils.displayLog("-------------Actual Date-------------", ":" + date_string);
			formetteddate = actualformat.parse(date_string);

		} catch (Exception e) {
			// TODO: handle exception
			// //Log.i("-------------Date Change
			// Exception-------------",":"+e.getMessage());
			// Log.i("***********************____________Event Date
			// Function___________",":"+e.getMessage());
			return "";
		}
		return format.format(formetteddate).toString();
	}

	public static String changeEventTimeZone(String date_string, String event_time_zone) {

		date_string = date_string.split("\\.")[0].trim();
		SimpleDateFormat actualformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
		/*
		 * TimeZone tzInAmerica = TimeZone.getTimeZone("GMT");
		 * actualformat.setTimeZone(tzInAmerica);
		 */

		SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.US);
		TimeZone tzInAmerica1 = TimeZone.getTimeZone(event_time_zone);
		format.setTimeZone(tzInAmerica1);

		Date formetteddate = null;
		try {
			AppUtils.displayLog("-------------Actual Date-------------", ":" + date_string);
			formetteddate = actualformat.parse(date_string);

		} catch (Exception e) {
			// TODO: handle exception
			// //Log.i("-------------Date Change
			// Exception-------------",":"+e.getMessage());
			// Log.i("***********************____________Event Date
			// Function___________",":"+e.getMessage());
			return "";
		}
		return format.format(formetteddate).toString();
	}

	public static String getDateSuffix(int day) {
		switch (day) {
			case 1:
			case 21:
			case 31:
				return ("st");

			case 2:
			case 22:
				return ("nd");

			case 3:
			case 23:
				return ("rd");

			default:
				return ("th");
		}
	}

	public static void setFontStype(Activity app) {

		roboto_regular = Typeface.createFromAsset(app.getAssets(), "Roboto-Regular.ttf");
		roboto_bold = Typeface.createFromAsset(app.getAssets(), "Roboto-Bold.ttf");
		droid_boldItalic = Typeface.createFromAsset(app.getAssets(), "DroidSerif-BoldItalic.ttf");
		droid_regrular = Typeface.createFromAsset(app.getAssets(), "DroidSerif-Regular.ttf");
		droid_bold = Typeface.createFromAsset(app.getAssets(), "DroidSerif-Bold.ttf");
		sanfrancisco_iphonefont = Typeface.createFromAsset(app.getAssets(), "sf-compact.otf");
		// stylish_boldItalic1=
		// Typeface.createFromAsset(app.getAssets(),"LeagueGothic-CondensedItalic.otf");
		// stylish_bold1=
		// Typeface.createFromAsset(app.getAssets(),"LeagueGothic-CondensedRegular.otf");

		roboto_boldItalic = Typeface.createFromAsset(app.getAssets(), "Roboto-BlackItalic.ttf");
		stylish_bold1 = Typeface.createFromAsset(app.getAssets(), "LeagueGothic-Regular.otf");

		VarelaRound = Typeface.createFromAsset(app.getAssets(), "VarelaRound-Regular.ttf");
		OpenSans = Typeface.createFromAsset(app.getAssets(), "SlabRegular.ttf");
		arial = Typeface.createFromAsset(app.getAssets(), "arial.ttf");
		raleway_regular = Typeface.createFromAsset(app.getAssets(), "Raleway-Regular.ttf");
		futura = Typeface.createFromAsset(app.getAssets(), "futura_normal.ttf");
		brush_scripts = Typeface.createFromAsset(app.getAssets(), "brush_regular.ttf");
		calibri = Typeface.createFromAsset(app.getAssets(), "Calibri_regular.ttf");
		papyrus = Typeface.createFromAsset(app.getAssets(), "PAPYRUS.TTF");
		verdana_regular = Typeface.createFromAsset(app.getAssets(), "verdana_regular.ttf");
		verdana_bold = Typeface.createFromAsset(app.getAssets(), "verdana_bold.ttf");

		times_roman = Typeface.createFromAsset(app.getAssets(), "times.ttf");
	}

	public static void _savePreference(SharedPreferences perf, String key, boolean result) {
		perf.edit().putBoolean(key, result).commit();
	}

	public static void _savePreference(SharedPreferences perf, String key, String result) {
		perf.edit().putString(key, result).commit();
	}

	public static void _saveTimePreference(SharedPreferences perf, String key, long result) {
		perf.edit().putLong(key, result).commit();
	}

	public static void _saveDataPreference(SharedPreferences perf, String key, String result) {
		perf.edit().putString(key, result).commit();
	}

	public static String _getPreference(SharedPreferences perf, String key) {
		String value = perf.getString(key, "");
		return value;
	}

	public static void clearSharedPreference(SharedPreferences perf) {
		perf.edit().clear().commit();

	}

	public static boolean _getBooleanPreference(SharedPreferences perf, String key) {

		boolean value = perf.getBoolean(key, false);

		return value;
	}

	public static int getStatePosition(ArrayList<String> stateList, String stateName) {
		int pos = 0;
		for (int i = 0; i < stateList.size(); i++) {
			if (NullChecker(stateList.get(i)).trim().equals(stateName.trim())) {
				pos = i;
			}
		}
		return pos;
	}
	public static String SingleCodeChecker(String var) {

		if (var.contains("'")) {
			var=var.replaceAll("'","''");
			return var;
		} else {
			return var;
		}

	}

	public static String NullChecker(String var) {

		if (var == null || var.equals("null")) {
			return "";
		} else {
			return var;
		}

	}

	public static void setCustomAlertDialog(Context ctx) {

		alert_dialog = new Dialog(ctx, R.style.MyCustomTheme);
		alert_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		alert_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		alert_dialog.setContentView(R.layout.theme_alert_dialog);

		// Grab the window of the dialog, and change the width
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		Window window = alert_dialog.getWindow();
		lp.copyFrom(window.getAttributes());

		// This makes the dialog take up the full width
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		alert_dialog.getWindow().setAttributes(lp);

		txt_alert_title = (TextView) alert_dialog.findViewById(R.id.txt_title);
		txt_alert_msg = (TextView) alert_dialog.findViewById(R.id.txt_message);
		txt_okey = (Button) alert_dialog.findViewById(R.id.btnOK);
		close_button = (Button) alert_dialog.findViewById(R.id.close_button);
		txt_dismiss = (Button) alert_dialog.findViewById(R.id.btnCancel);
		img_alert = (ImageView) alert_dialog.findViewById(R.id.img_alert);

	}

	public static void openCustomDialog(String title, String message) {
		try {
			txt_alert_title.setText(title);
			txt_alert_msg.setText(message);
			txt_okey.setText(NullChecker(txt_okey.getText().toString().toUpperCase()));
			txt_dismiss.setText(NullChecker(txt_dismiss.getText().toString().toUpperCase()));
			alert_dialog.show();
			/*close_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(alert_dialog!=null){
						if(alert_dialog.isShowing())
							alert_dialog.dismiss();
					}
				}
			});*/
		}catch (Exception e){
			if(alert_dialog!=null){
				if(alert_dialog.isShowing())
					alert_dialog.dismiss();
			}
			e.printStackTrace();
		}
	}

	public static void setCustomDialogImage(int imgId) {
		if (imgId != 0)
			img_alert.setImageResource(imgId);
	}

	public static void deleteImageFile(String filename) {

		File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), APP_FOLDER_NAME);
		File mediaFile = new File(mediaStorageDir.getPath() + File.separator + filename);

		if (mediaFile.isFile()) {
			mediaFile.delete();
		}
		// Log.i("--Deleted Successfully----", filename);

	}

	public static String RoundTo2Decimals(double val) {
		final NumberFormat df = DecimalFormat.getInstance();
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(2);
		// df.setRoundingMode(RoundingMode.DOWN);

		return df.format(val);
	}

	public static boolean isMyServiceRunning(Class<DownloadService> serviceClass, Activity activity) {
		ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static void killAllServices(Activity activity) {
		ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();

		Iterator<RunningAppProcessInfo> iter = runningAppProcesses.iterator();

		while (iter.hasNext()) {
			RunningAppProcessInfo next = iter.next();

			String pricessName = activity.getPackageName() + ":service";

			if (next.processName.equals(pricessName)) {
				Process.killProcess(next.pid);

				break;
			}
		}
	}

	public static String getCurrentDateTimeInGMT() {
		// yyyy-MM-dd HH:mm:ss old
		// MM/dd/yyyy hh:mm:ss a new
		SimpleDateFormat date_format_new_sec = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		Date date = new Date();
		TimeZone tzInAmerica = TimeZone.getTimeZone("GMT");
		date_format_new_sec.setTimeZone(tzInAmerica);
		String date_string = date_format_new_sec.format(date);
		return date_string;
	}
	/*
	 * public static String ENCODE_BASE64_STRING(String s){ String text =
	 * ITransaction.EMPTY_STRING; try { if (!Util.NullChecker(s).isEmpty()) {
	 * byte[] data = s.getBytes("UTF-8"); return Base64.encodeToString(data,
	 * Base64.DEFAULT); } } catch (Exception e) { // TODO: handle exception }
	 * return text; }
	 *
	 * public static String DECODE_BASE64_STRING(String s){ String text =
	 * ITransaction.EMPTY_STRING; try { if (!Util.NullChecker(s).isEmpty()) {
	 * byte[] string_s = Base64.decode(s, Base64.DEFAULT); text = new
	 * String(string_s, "UTF-8"); } } catch (Exception e) { // TODO: handle
	 * exception }
	 *
	 * return text; }
	 */

	public static String getOfflineSyncServerFormat(String date) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

			/*
			 * TimeZone tzInAmerica = TimeZone.getTimeZone("GMT");
			 * formatter.setTimeZone(tzInAmerica);
			 */
			/*
			 * TimeZone gmtTime = TimeZone.getDefault();//
			 * formatter.setTimeZone(gmtTime);
			 */
			return formatter.format(dateFormat.parse(date));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getOfflineSyncClientFormat(String date, String event_time_zone) {
		try {
			date = date.split("\\.")[0].trim();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);// "2016-05-17
			/*
			 * TimeZone time_zone = TimeZone.getTimeZone(event_time_zone);
			 * formatter.setTimeZone(time_zone);
			 */
			return dateFormat.format(dateFormat.parse(date));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}


	public static String getCSVDateFormat(String date) {
		try {
			//date = date.split("\\.")[0].trim();
			DateFormat input = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss",Locale.US);
			DateFormat output = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.US);// "2016-05-17
			TimeZone time_zone = TimeZone.getTimeZone("GMT");
			input.setTimeZone(time_zone);
			return output.format(input.parse(date));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		}
		return capitalize(manufacturer) + " " + model;
	}
	public static String getDeviceNameandAppVersion() {
		String manufacturer="Permission not given";
		String model="";
		//if(AppUtils.isPhoneStatePermissionGranted(BaseActivity.baseContext)){
		manufacturer = Build.MANUFACTURER;
		model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		}
		//}
		return capitalize(manufacturer) + " " + model+" - V "+ BaseActivity.baseContext.getResources().getString(R.string.app_version_production);
	}

	private static String capitalize(String str) {
		if (TextUtils.isEmpty(str)) {
			return str;
		}
		char[] arr = str.toCharArray();
		boolean capitalizeNext = true;
		String phrase = "";
		for (char c : arr) {
			if (capitalizeNext && Character.isLetter(c)) {
				phrase += Character.toUpperCase(c);
				capitalizeNext = false;
				continue;
			} else if (Character.isWhitespace(c)) {
				capitalizeNext = true;
			}
			phrase += c;
		}
		return phrase;
	}

	public static String getKey(HashMap<String, String> map, String value) {
		String key = ITransaction.EMPTY_STRING;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (value != null && value.equals(entry.getValue())) {
				key = entry.getKey();
				break;
			}
		}
		return key;
	}

	public static String getKey(TreeMap<String, String> map, String value) {
		String key = ITransaction.EMPTY_STRING;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (value != null && value.equals(entry.getValue())) {
				key = entry.getKey();
				break;
			}
		}
		return key;
	}

	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
	}

	public static void setListViewHeight(ExpandableListView listView, int group) {
		ExpandableListAdapter listAdapter = listView.getExpandableListAdapter();
		int totalHeight = 0;
		int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(),
				MeasureSpec.AT_MOST);
		for (int i = 0; i < listAdapter.getGroupCount(); i++) {
			View groupItem = listAdapter.getGroupView(i, false, null, listView);
			groupItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
			totalHeight += groupItem.getMeasuredHeight();
			if(i == ( listAdapter.getGroupCount() - 1)){
				totalHeight = totalHeight + (totalHeight * listAdapter.getChildrenCount(i)+50);
			}

			if (((listView.isGroupExpanded(i)) && (i != group))
					|| ((!listView.isGroupExpanded(i)) && (i == group))) {
				for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
					View listItem = listAdapter.getChildView(i, j, false, null,listView);
					listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);

					totalHeight += listItem.getMeasuredHeight();
				}
			}
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		int height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getGroupCount()-1));
		if (height < 10)
			height = 200;
		params.height = height;
		listView.setLayoutParams(params);
		listView.requestLayout();


	}

	/*public static boolean isprinterconnected(){
		if(!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
			return  true;
		}
		return  false;
	}*/
	public static void openprinternotconnectedpopup(Activity activity){
		Util.setCustomAlertDialog(activity);
		Util.openCustomDialog("Alert", "Printer is not connected please contact event Organizer!");
		Util.txt_okey.setText("Ok");
		Util.txt_dismiss.setVisibility(View.GONE);
		Util.txt_okey.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Util.alert_dialog.dismiss();
			}
		});

	}
	public static  void OpenConnectPrinter(final Activity activity){
		Util.setCustomAlertDialog(activity);
		Util.openCustomDialog("Alert", "Printer is not connected.Do you want to Connect?");
		Util.txt_okey.setText("CONNECT");
		Util.txt_dismiss.setVisibility(View.VISIBLE);

	}
	public static  String getWifiName(Context context) {
		WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (manager.isWifiEnabled()) {
			WifiInfo wifiInfo = manager.getConnectionInfo();
			if (wifiInfo != null) {
				NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
				if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
					return wifiInfo.getSSID();
				}
			}
		} return null;
	}
	public static  String getWifiIpAdress(Context context) {
		WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
		WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return ip;
	}
	public static boolean getselfcheckinbools(String keyname){
		Boolean result=false;
		try {
			result=Util.selfcheckinpref.getBoolean(keyname,false);
		}catch (Exception e){
			result=false;
			e.printStackTrace();
		}
		return result;
	}
	public static boolean gettempselfcheckinbools(String keyname){
		Boolean result=false;
		try {
			result=Util.tempselfcheckinpref.getBoolean(keyname,false);
		}catch (Exception e){
			result=false;
			e.printStackTrace();
		}
		return result;
	}

	public static void openCustomProgressDialog(Context ctx) {

		alert_dialog = new Dialog(ctx, R.style.MyCustomTheme);
		alert_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		alert_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		alert_dialog.setContentView(R.layout.theme_alert_dialog);

		// Grab the window of the dialog, and change the width
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		Window window = alert_dialog.getWindow();
		lp.copyFrom(window.getAttributes());

		// This makes the dialog take up the full width
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		alert_dialog.getWindow().setAttributes(lp);

		txt_alert_title = (TextView) alert_dialog.findViewById(R.id.txt_title);
		txt_alert_msg = (TextView) alert_dialog.findViewById(R.id.txt_message);
		txt_okey = (Button) alert_dialog.findViewById(R.id.btnOK);
		txt_dismiss = (Button) alert_dialog.findViewById(R.id.btnCancel);
		img_alert = (ImageView) alert_dialog.findViewById(R.id.img_alert);

	}
	public static void createPDF(ArrayList<String> imageFiles,boolean isFitToPage){
		Document doc = new Document();
		try{

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
			File file = new File(filename,"printPdf.pdf");// myimage.png
			//Log.d("PDFCreator", "PDF Path: " + path);
			FileOutputStream fOut = new FileOutputStream(file);
			PdfWriter.getInstance(doc, fOut);
			//open the document
			doc.open();
			ByteArrayOutputStream rstream = new ByteArrayOutputStream();
			for(String fPath:imageFiles) {
				Bitmap tempBitmap = BitmapFactory.decodeFile(fPath);
				if(isFitToPage)
					tempBitmap= ZebraPrinter.rotateBitmap(tempBitmap,90);
				tempBitmap.compress(Bitmap.CompressFormat.JPEG, 90, rstream);
				Image rmyImg = Image.getInstance(rstream.toByteArray());
				rmyImg.setAlignment(Image.MIDDLE);
				if(isFitToPage) {
					//int indentation = 0;
					//myImg.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
                    /*float scaler = ((doc.getPageSize().getWidth() - doc.leftMargin()
                            - doc.rightMargin() - indentation) / rmyImg.getWidth()) * 100;
                    rmyImg.scalePercent(scaler);*/

					float documentWidth = doc.getPageSize().getWidth() - doc.leftMargin() - doc.rightMargin();
					float documentHeight = doc.getPageSize().getHeight() - doc.topMargin() - doc.bottomMargin();
					rmyImg.scaleAbsolute(documentWidth, documentHeight);
				}

				//add image to document
				doc.add(rmyImg);
				if(isFitToPage)
					doc.newPage();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		finally
		{
			doc.close();
		}
	}
}
