//  ScanAttendee Android
//  Created by Ajay
//  This class is parent abstract class which used for default on time setup along with header and footer
//  Copyright (c) 2014 Globalnest. All rights reserved
package com.globalnest.scanattendee;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brother.ptouch.sdk.NetPrinter;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.globalnest.BackgroundReciver.DownloadService;
import com.globalnest.autoresizetextview.AutoResizeTextView;
import com.globalnest.brother.ptouch.sdk.printdemo.common.MsgDialog;
import com.globalnest.brother.ptouch.sdk.printdemo.common.MsgHandle;
import com.globalnest.brother.ptouch.sdk.printdemo.printprocess.BasePrint;
import com.globalnest.brother.ptouch.sdk.printdemo.printprocess.ImagePrint;
import com.globalnest.brother.ptouch.sdk.printdemo.printprocess.PrinterModelInfo;
import com.globalnest.classes.MultiDirectionSlidingDrawer;
import com.globalnest.classes.RateTextCircularProgressBar;
import com.globalnest.classes.RoundedImageView;
import com.globalnest.cropimage.CropImage;
import com.globalnest.cropimage.CropUtil;
import com.globalnest.database.DBFeilds;
import com.globalnest.database.DataBase;
import com.globalnest.mvc.BadgeResponseNew;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.mvc.OfflineScansObject;
import com.globalnest.mvc.OfflineSyncFailuerObject;
import com.globalnest.mvc.OfflineSyncResController;
import com.globalnest.mvc.OfflineSyncSuccessObject;
import com.globalnest.mvc.SelfcheckinColumns;
import com.globalnest.mvc.SessionGroup;
import com.globalnest.mvc.TStatus;
import com.globalnest.network.Connectivity;
import com.globalnest.network.HttpClientClass;
import com.globalnest.network.HttpGetMethod;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.IPostResponse;
import com.globalnest.network.SafeAsyncTask;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.EventObjects;
import com.globalnest.objects.RegistrationSettingsController;
import com.globalnest.objects.ScannedItems;
import com.globalnest.objects.UserObjects;
import com.globalnest.payments.AuthNetTask;
import com.globalnest.payments.PaypalDirectTask;
import com.globalnest.printer.PrinterDetails;
import com.globalnest.printer.ZebraPrinter;
import com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.SFDCDetails;
import com.globalnest.utils.Util;
import com.globalnest.MainActivity;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.DataMatrixWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfWriter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permission;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import io.fabric.sdk.android.Fabric;

import static com.globalnest.classes.MultiDirectionSlidingDrawer.LOG_TAG;

//import org.apache.commons.io.FileUtils;

@SuppressLint({ "NewApi", "SimpleDateFormat" })
public abstract class BaseActivity extends MainActivity implements IPostResponse {
    public static Context baseContext=null;
    public static ZebraPrinter zebraPrinter;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    public static final String SECURE_SETTINGS_BLUETOOTH_ADDRESS = "bluetooth_address";
    public File mFileTemp;
    public File mediaFile;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int PICK_FROM_CAMERA = 1;
    public static final int CROP_FROM_CAMERA = 2;
    public static final int PICK_FROM_FILE = 3;
    public static final int FINISH_RESULT = 4;
    public static  int OFFLINESCANS = 0;
    public static final int REQUEST_CODE_CROP_IMAGE = 5;
    public Uri mImageCaptureUri;
    public boolean isRefresh = false;
    public static ProgressDialog baseDialog;
    MultiDirectionSlidingDrawer photo_slider;
    RoundedImageView txt_camera, txt_gallery, txt_cancel;
    public FrameLayout profile_layout, bottom_layout, linearview, search_layout, img_cart,lay_top_line;
    LayoutInflater inflater;
    RelativeLayout slide_layout, menu_slider_layout, left_menu_slider;
    PopupWindow setting_popup, checkin_popup;
    LinearLayout addevent_layout, button_layout, event_layout, back_layout,top_layout,lay_loadmore,ticket_searchlayout;

    AutoCompleteTextView search_view,search_ticket;
    FrameLayout img_import,horizontal_line_home,horizontal_line_events,horizontal_line_tickets,horizontal_line_attendees,horizontal_line_orders,horizontal_line_scan,horizontal_line_offlinescan;
    ImageView img_menu, img_addticket, img_setting, img_close, img_search, img_refund_history,img_socket_scanner,img_backmenu,img_camera,searchcross;
    public static RateTextCircularProgressBar progress_download_data;
    public static ImageView img_scanner_base,img_download;
    public static ImageView img_pairBarcode,img_pairQrcode,img_resetBarcode,img_resetQrcode,img_sppBarcode,img_sppQrcode;
    public static TextView txt_download_attendees_count,txt_offline,txt_appofflinemode;
    public static LinearLayout layout_offline_tag;
    LinearLayout  _badgetemplate, _salestax,txt_ext_setting,txt_scantickets, _fee, _stripe, _paypal,
            _dropbox, txt_profile;
    public TextView txt_cartno,txt_loading,txt_title, txt_addevent, txt_error_msg, txt_eventName,txt_save, txt_username,
            txt_print,txt_useremail,txtprint_selfcheckin,txtcheckin_selfcheckin,txt_hidenow,selfcheckinonlysave;//temp selfcheckinonlysave
    public static Boolean checkinbutton_clicked=false,ordersuccess_popupok_clicked=false,fromprintsucess=false;
    static  Boolean isselfcheckinpopupopen=false;
    EditText paypal_email;
    RoundedImageView user_img;
    ImageView img_event;
    public MsgDialog msgDialog = new MsgDialog(this, this);
    private RelativeLayout home_layout, scan_layout, ticket_layout, order_layout, list_layout, events_layout,
            logout_layout,settings_layout,offlinescan_layout,selfcheckin_layout,printattendees_layout;
    AutoResizeTextView title_auto;
    View home_line, ticket_line, scan_line, list_line, order_line, logout_line, v;
    Button btn_cancel, btn_save;
    BasePrint myPrint = null;
    MsgHandle mHandle;
    MsgDialog mDialog;
    public ArrayList<String> mItems = null;
    ArrayList<String> brotherPrintersList = null;
    NetPrinter[] mNetPrinter;
    SharedPreferences sharedPreferences;
    PrinterModelInfo modelInfo = new PrinterModelInfo();
    public static UserObjects user_profile = new UserObjects();
    Dialog ask_dialog;
    public static EventObjects checkedin_event_record = new EventObjects();
    public SFDCDetails sfdcddetails;
    public String checked_in_eventId = "";
    public static String staticCheckedInEventId="";
    Animation errorAnimation, animTranslate;
    CountDownTimer timer;
    Point p;
    Bitmap image = null;
    Dialog paypal_dialog, progress_dialog;
    String paypal_rec_email = "", scanned_value = "";
    InputMethodManager inputManager;
    DrawerLayout mDrawerLayout;
    PopupMenu import_menu;
    int width, height;
    double diagonalInches;
    private final int PAYPAL_DIALOG = 100;
    ArrayList<String> state_list;
    ArrayList<String> state_list_home;
    ArrayList<String> country_list;
    public boolean isPrinter;
    String qrcode_name,Attendeeimg_name;
    HttpGetMethod getMehod;
    HttpPostData postMethod;
    AuthNetTask authNet_Task;
    PaypalDirectTask paypal_direct;
    String orderId = "";
    String url = "";
    Gson gson = new Gson();
    public SocketBroadCastReciever socket_reciever;
    public static Dialog softscannerdialog;
    public static Activity activity;
    private ProgressDialog dialog;
    static Boolean frontcam=false;
    static Boolean isCloudPrintingON=false,isBlutoothPrintingON=false;
    public boolean isBrotherPrinterConnected=false;
    public static boolean isValidate_badge_reg_settings=false;
    public static boolean isLogoutclicked=true;
    public ExternalSettings externalSettings;
    public String Appversion="";
    private OfflineSyncBroadCast offlineSyncBroadCast=new OfflineSyncBroadCast();
    public static boolean isOfflineSyncRunning=false;
    private SocketBroadCastReciever socketBroadCastReciever=new SocketBroadCastReciever();
    final int sdk = Build.VERSION.SDK_INT;
    //CheckBox Firstname,Lastname,Emailid,Company,Orderid,TicketNo;
	/* PowerManager pm;
     PowerManager.WakeLock wl;*/
    // public DecimalFormat twoDigitDF ;
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.base_layout);
            baseContext = this;
            //registerReceiver(offlineSyncBroadCast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

            // twoDigitDF = new DecimalFormat("#.##");
            // pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            // wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
            //activity = this;
		/*brotherPrintersList=new ArrayList<String>();
		brotherPrintersList.add("QL-720NW");
		brotherPrintersList.add("QL-820NWB");*/

            Util.dashboard_data_pref = getSharedPreferences(Util.DASHBOARD_DATA_PREF, MODE_PRIVATE);
            Util.eventPrefer = getSharedPreferences(Util.eventpref, MODE_PRIVATE);
            Util.printStatusupdateAttendees = getSharedPreferences(Util.PRINT_STATUS, MODE_PRIVATE);
            Util.login_prefer = getSharedPreferences(Util.loginpref, MODE_PRIVATE);
            Util.offset_pref = getSharedPreferences(Util.OFFSET_PREF, MODE_PRIVATE);
            Util.first_login_pref = getSharedPreferences(Util.FIRSTIME_LOGIN_PREF, MODE_PRIVATE);
            Util.selected_session_attedee_pref = getSharedPreferences(Util.SELECTED_SESSION_ATTENDEE_PREF, MODE_PRIVATE);
            Util.socket_device_pref = getSharedPreferences(Util.SOCKET_DEVICE_PREF, MODE_PRIVATE);
            Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
            Util.totalorderlisthandler = getSharedPreferences("TTT", MODE_PRIVATE);
            Util.order_request = getSharedPreferences(Util.ORDER_REQUEST, MODE_PRIVATE);
            Util.order_Items = getSharedPreferences(Util.ORDER_ITEMS, MODE_PRIVATE);
            AppUtils.user_credentials = getSharedPreferences(AppUtils.USER_CRED_PRF, MODE_PRIVATE);
            Util.checkin_only_pref = getSharedPreferences(Util.CHECKIN_ONLY_PREF, MODE_PRIVATE);
            Util.scanmode_checkin_pref = getSharedPreferences(Util.SCANMODE_CHECKIN_PREF, MODE_PRIVATE);

            PrinterDetails.selectedPrinterPrefrences = getSharedPreferences(Util.SELECTED_PRINTER_PREF, MODE_PRIVATE);
            PrinterDetails.buttonprefrences = getSharedPreferences(Util.SELECTED_PRINTER_PREF, MODE_PRIVATE);
            Util.sessionCountPref = getSharedPreferences(Util.SESSION_COUNT_PREF, MODE_PRIVATE);
            Util.selfcheckinpref = getSharedPreferences(Util.SELFCHECKIN_COUNT_PREF, MODE_PRIVATE);
            Util.tempselfcheckinpref = getSharedPreferences(Util.TEMPSELFCHECKIN_COUNT_PREF, MODE_PRIVATE);
            Util.lastModifideDate =getSharedPreferences(Util.LASTMODIFIDEDATE,MODE_PRIVATE);
            Util.devicebluetoothaddress =getSharedPreferences(Util.DEVICEBLUETOOTHADDRESS,MODE_PRIVATE);
            //crashReport("ScanAttendee");
            if(AppUtils.isCrashReportEnable) {//TODO ONCRASHES
                Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());
            }

            getDeviceInches();
            //AndroidAuthSession session = buildSession();
            //Util.mApi = new DropboxAPI<AndroidAuthSession>(session);
            Util.attendee_info_pref = getSharedPreferences(Util.attendeepref, MODE_PRIVATE);
            initializeDB(this);
            sfdcddetails = (SFDCDetails) getIntent().getSerializableExtra(AppUtils.INTENT_KEY);
            if (sfdcddetails != null) {
                Util.db.InsertAndUpdateSFDCDDETAILS(sfdcddetails);
                Util._savePreference(Util.login_prefer, Util.USER_ID, sfdcddetails.user_id);
                Util._savePreference(Util.login_prefer, Util.REFRESH_TOKEN, sfdcddetails.refresh_token);
            }
            baseDialog = new ProgressDialog(this);
            //socket_reciever = new SocketBroadCastReciever(this);
            sfdcddetails = Util.db.getSFDCDDETAILS();
            Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
            externalSettings = new ExternalSettings();
            if(!Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, "").isEmpty()){
                externalSettings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, ""), ExternalSettings.class);
            }
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            mDialog = new MsgDialog(this, BaseActivity.this);
            mHandle = new MsgHandle(this, mDialog);
            myPrint = new ImagePrint(this, mHandle, mDialog);
            inflater = getLayoutInflater();
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            left_menu_slider = (RelativeLayout) findViewById(R.id.whatYouWantInLeftDrawer);
            // mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
            // GravityCompat.START);

            inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            progress_dialog = new Dialog(this);
            progress_dialog.setCanceledOnTouchOutside(false);
            progress_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progress_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            // this.getWindow().setLayout(LayoutParams.FILL_PARENT,
            // LayoutParams.FILL_PARENT);
            progress_dialog.setContentView(R.layout.loading_layout);
            // progress_dialog.setCancelable(false);
            txt_loading = (TextView) progress_dialog.findViewById(R.id.txtloading);
            txt_loading.setTypeface(Util.roboto_regular);
            img_close = (ImageView) findViewById(R.id.searchclose);
            searchcross  = (ImageView) findViewById(R.id.searchcross);
            img_search = (ImageView) findViewById(R.id.imgsearch);
            img_import = (FrameLayout) findViewById(R.id.imgimport);
            img_refund_history = (ImageView) findViewById(R.id.imgrefundhistory);
            search_layout = (FrameLayout) findViewById(R.id.searchlayout);
            lay_top_line = (FrameLayout) findViewById(R.id.lay_top_line);
            search_view = (AutoCompleteTextView) findViewById(R.id.editsearchrecord);
            search_ticket = (AutoCompleteTextView) findViewById(R.id.search_ticket);
            bottom_layout = (FrameLayout) findViewById(R.id.bottomlayout);
            linearview = (FrameLayout) findViewById(R.id.linearview);
            back_layout = (LinearLayout) findViewById(R.id.backlayout);
            top_layout = (LinearLayout) findViewById(R.id.lay_top);
            lay_loadmore = (LinearLayout) findViewById(R.id.lay_loadmore);
            ticket_searchlayout = (LinearLayout) findViewById(R.id.ticket_searchlayout);
            event_layout = (LinearLayout) findViewById(R.id.eventdetaillayout);
            addevent_layout = (LinearLayout) findViewById(R.id.addeventlayout);
            button_layout = (LinearLayout) findViewById(R.id.buttonlayout);
            layout_offline_tag = (LinearLayout) findViewById(R.id.txt_offline_tag);
            txt_offline = (TextView) findViewById(R.id.txt_offline);
            txt_appofflinemode = (TextView) findViewById(R.id.txt_appofflinemode);
            img_setting = (ImageView) findViewById(R.id.imgsetting);
            img_menu = (ImageView) findViewById(R.id.imgmenu);
            img_backmenu = (ImageView) findViewById(R.id.img_backmenu);
            img_event = (ImageView) findViewById(R.id.deventimg);
            img_scanner_base = (ImageView) findViewById(R.id.img_scanner_base);
            img_socket_scanner = (ImageView) findViewById(R.id.img_socket_scanner);
            txt_title = (TextView) findViewById(R.id.txttitle);
            txt_hidenow = (TextView) findViewById(R.id.txt_hidenow);
            title_auto = (AutoResizeTextView) findViewById(R.id.title_auto);
            img_addticket = (ImageView) findViewById(R.id.imgaddticket);
            img_camera = (ImageView) findViewById(R.id.img_camera);
            txt_error_msg = (TextView) findViewById(R.id.baseerrormsg);
            txt_error_msg.setVisibility(View.GONE);
            txt_addevent = (TextView) findViewById(R.id.txtaddevent);
            txt_eventName = (TextView) findViewById(R.id.dashboardeventname);
            txt_save = (TextView) findViewById(R.id.txtsave);
            txt_useremail = (TextView) findViewById(R.id.profileemail);
            txt_print = (TextView) findViewById(R.id.txtprint);
            txtprint_selfcheckin = (TextView) findViewById(R.id.selfcheckintxtprint);
            txtcheckin_selfcheckin = (TextView) findViewById(R.id.selfcheckintxtcheckin);
            selfcheckinonlysave = (TextView) findViewById(R.id.selfcheckinonlysave);
            btn_cancel = (Button) findViewById(R.id.btncancel);
            btn_save = (Button) findViewById(R.id.btnsave);

            txt_eventName.setTypeface(Util.roboto_regular);
            txt_title.setTypeface(Util.roboto_regular, Typeface.BOLD);
            txt_hidenow.setText(Html.fromHtml("<u>"+"Hide Now"+"</u>"));
            txt_save.setTypeface(Util.roboto_bold);
            search_view.setTypeface(Util.roboto_regular);
            txt_addevent.setTypeface(Util.roboto_regular, Typeface.BOLD);
            btn_save.setTypeface(Util.roboto_regular, Typeface.BOLD);
            btn_cancel.setTypeface(Util.roboto_regular, Typeface.BOLD);
            // badge_layout = (FrameLayout)
            // findViewById(R.id.frame_mainqrcodebadge);
            menu_slider_layout = (RelativeLayout) left_menu_slider.findViewById(R.id.menusliderlayout);
            profile_layout = (FrameLayout) left_menu_slider.findViewById(R.id.profilelayout);
            home_layout = (RelativeLayout) left_menu_slider.findViewById(R.id.homelayout);
            scan_layout = (RelativeLayout) left_menu_slider.findViewById(R.id.scanlayout);
            printattendees_layout = (RelativeLayout) left_menu_slider.findViewById(R.id.printattendeeslayout);
            ticket_layout = (RelativeLayout) left_menu_slider.findViewById(R.id.ticketlayout);
            list_layout = (RelativeLayout) left_menu_slider.findViewById(R.id.listlayout);
            order_layout = (RelativeLayout) left_menu_slider.findViewById(R.id.orderlayout);
            events_layout = (RelativeLayout) left_menu_slider.findViewById(R.id.eventlayout);
            logout_layout = (RelativeLayout) left_menu_slider.findViewById(R.id.logoutlayout);
            settings_layout = (RelativeLayout) left_menu_slider.findViewById(R.id.settingslayout);
            selfcheckin_layout = (RelativeLayout) left_menu_slider.findViewById(R.id.selfcheckinlayout);
            offlinescan_layout = (RelativeLayout) left_menu_slider.findViewById(R.id.offlinescanlayout);
            offlinescan_layout.setVisibility(View.GONE);
            horizontal_line_offlinescan = (FrameLayout) left_menu_slider.findViewById(R.id.horinzontal_line_offlinescan);
            horizontal_line_offlinescan.setVisibility(View.GONE);
            horizontal_line_home = (FrameLayout) left_menu_slider.findViewById(R.id.horinzontal_line_home);
            horizontal_line_events = (FrameLayout) left_menu_slider.findViewById(R.id.horinzontal_line_events);
            horizontal_line_tickets = (FrameLayout) left_menu_slider.findViewById(R.id.horinzontal_line_tickets);
            horizontal_line_attendees = (FrameLayout) left_menu_slider.findViewById(R.id.horinzontal_line_attendees);
            horizontal_line_orders = (FrameLayout) left_menu_slider.findViewById(R.id.horinzontal_line_orders);
            horizontal_line_scan = (FrameLayout) left_menu_slider.findViewById(R.id.horinzontal_line_scan);


            home_line = (View) left_menu_slider.findViewById(R.id.homeline);
            ticket_line = (View) left_menu_slider.findViewById(R.id.ticketline);
            list_line = (View) left_menu_slider.findViewById(R.id.listline);
            scan_line = (View) left_menu_slider.findViewById(R.id.scanline);
            order_line = (View) left_menu_slider.findViewById(R.id.orderline);
            logout_line = (View) left_menu_slider.findViewById(R.id.logoutline);

            if (Util.slide_menu_id == R.id.homelayout) {
                home_line.setVisibility(View.VISIBLE);
            } else if (Util.slide_menu_id == R.id.ticketlayout) {
                ticket_line.setVisibility(View.VISIBLE);
            } else if (Util.slide_menu_id == R.id.scanlayout) {
                scan_line.setVisibility(View.VISIBLE);
            } else if (Util.slide_menu_id == R.id.listlayout) {
                list_line.setVisibility(View.VISIBLE);
            } else if (Util.slide_menu_id == R.id.orderlayout) {
                order_line.setVisibility(View.VISIBLE);
            } else if (Util.slide_menu_id == R.id.logoutlayout) {
                logout_line.setVisibility(View.VISIBLE);
            }


            user_img = (RoundedImageView) left_menu_slider.findViewById(R.id.imgprofile);
            txt_username = (TextView) left_menu_slider.findViewById(R.id.profilename);
            photo_slider = (MultiDirectionSlidingDrawer) findViewById(R.id.photoptionslider);
            txt_username.setTypeface(Util.roboto_regular);
            txt_error_msg.setTypeface(Util.roboto_regular);
            import_menu = new PopupMenu(this, img_import);
            import_menu.getMenuInflater().inflate(R.menu.main, import_menu.getMenu());

            //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            getUserProfileInfo();

            if (NullChecker(getIntent().getStringExtra(Util.ADDEVENT)).equals(Util.ADDEVENT)) {
                Util._savePreference(Util.eventPrefer, Util.EVENT_CHECKIN_ID, getIntent().getStringExtra(Util.ADDED_EVENT_ID));
            }
            try {
                checked_in_eventId = Util._getPreference(Util.eventPrefer, Util.EVENT_CHECKIN_ID);
                staticCheckedInEventId = checked_in_eventId;

            } catch (Exception e) {
                e.printStackTrace();
            }
            // checked_in_eventId = Util._getPreference(Util.eventPrefer,
            // Util.EVENT_CHECKIN_ID);
            checkedin_event_record = Util.db.getSelectedEventRecord(checked_in_eventId);

            country_list = Util.db.getCountryList(checked_in_eventId);


            txt_username.setText(NullChecker(user_profile.Profile.First_Name__c + " " + user_profile.Profile.Last_Name__c));
            txt_useremail.setText(NullChecker(user_profile.Profile.Email__c));
            txt_eventName.setText(checkedin_event_record.Events.Name);

            if (!checkedin_event_record.image.isEmpty()) {
                Picasso.with(BaseActivity.this).load(NullChecker(checkedin_event_record.image))
                        .placeholder(R.drawable.default_image).error(R.drawable.default_image).into(img_event);
            }
            if (!user_profile.profileimage.isEmpty()) {
                Picasso.with(BaseActivity.this).load(NullChecker(user_profile.profileimage))
                        .placeholder(R.drawable.default_image).error(R.drawable.default_image).into(user_img);
            } else {
                Picasso.with(BaseActivity.this).load(R.drawable.default_image).into(user_img);
            }


            profile_layout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    mDrawerLayout.closeDrawer(left_menu_slider);
                    Intent i = new Intent(BaseActivity.this, UserProfileActivity.class);
                    startActivityForResult(i, Util.DASHBORD_ONACTIVITY_REQ_CODE);
                }
            });

            home_layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.closeDrawer(left_menu_slider);
                    if (Util.slide_menu_id != R.id.homelayout) {
                        Util.slide_menu_id = R.id.homelayout;
                        checkedin_event_record = Util.db.getSelectedEventRecord(checked_in_eventId);
                        Intent i = new Intent(BaseActivity.this, DashboardActivity.class);
                        i.putExtra("CheckIn Event", checkedin_event_record);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.putExtra(Util.HOME, "home_layout");
                        //startActivity(i);
                        startActivityForResult(i, Util.DASHBORD_ONACTIVITY_REQ_CODE);
                    }
                }
            });
            events_layout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    mDrawerLayout.closeDrawer(left_menu_slider);
                    if (Util.slide_menu_id != R.id.eventlayout) {
                        Util.slide_menu_id = R.id.eventlayout;
                        // Util.eventPrefer.edit().clear().commit();
                        Intent i = new Intent(BaseActivity.this, EventListActivity.class);
                        i.putExtra("CheckIn Event", checkedin_event_record);
                        i.putExtra(Util.EVENT_LIST, Util.EVENT_LIST);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivityForResult(i, Util.DASHBORD_ONACTIVITY_REQ_CODE);
                    }
                }
            });
           /* printattendees_layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Cursor attendee_cursor= Util.db.getmulticursor();
                    HashMap<String, Boolean> tickets_register = new HashMap<String, Boolean>();
                    tickets_register.clear();
                    List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);

                    boolean isBreak = false;
                    for (int i = 0; i < attendee_cursor.getCount(); i++) {
                        attendee_cursor.moveToPosition(i);
                        String id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
                        String status = Util.db.getTStatusBasedOnGroup(id, attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                        boolean ischeckin = Boolean.valueOf(NullChecker(status));
                        if (Util.db.isItemPoolSwitchON(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)) {

                            tickets_register.put(id, ischeckin);
                        }else {
                            String item_pool_id = Util.db.getItemPoolID(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                            String item_pool_name="";
                            String parent_id = Util.db.getItemPoolParentId(item_pool_id, checked_in_eventId);
                            if(!NullChecker(parent_id).isEmpty()) {
                                item_pool_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                            } else {
                                item_pool_name = Util.db.getItem_Pool_Name(item_pool_id, checked_in_eventId);
                            }
                            showCustomToast(BaseActivity.this,"Sorry! You don't have permission to Check-In "+Util.db.getSwitchedONGroup(checked_in_eventId).Name, R.drawable.img_like, R.drawable.toast_redrounded, false);

                            tickets_register.put(id, ischeckin);
                        }
                    }
                    if (!isBreak) {
                        JSONArray ticketarray = new JSONArray();
                        // JSONObject parent = new JSONObject();
                        for (String key : tickets_register.keySet()) {
                            try {
                                JSONObject obj = new JSONObject();
                                obj.put("TicketId", key.trim());
                                obj.put("device", "ANDROID");
         *//*if (isNotFromSelfcheckin((BaseActivity) context)&&key.trim().equals(printed_attendeeid)) {
            obj.put("printstatus", "Printed");
         }*//*
                                scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                                if (scanticks.size() == 0) {
                                    obj.put("freeSemPoolId", "");
                                } else if (scanticks.size() > 0) {
                                    if (Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId)) {
                                        obj.put("freeSemPoolId", scanticks.get(0).BLN_Item_Pool__c);
                                    } else {
                                        obj.put("freeSemPoolId", "");
                                    }
                                }
                                //if (String.valueOf(tickets_register.get(key)).equals("true")) {
                                obj.put("isCHeckIn", true);
         *//*} else {
            obj.put("isCHeckIn", true);
         }*//*
                                obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
                                obj.put("sTime", Util.getCurrentDateTimeInGMT());
                                obj.put("scandevicemode",Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,""));
                                ticketarray.put(obj);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        new DoTicketCheckInFromBase(BaseActivity.this, (-1), ticketarray.toString()).execute();

                    }
                }
            });*/
            printattendees_layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(AppUtils.isStoragePermissionGranted(BaseActivity.this)){
                        Intent i=new Intent(BaseActivity.this,PrintAttendeesListActivity.class);
                        i.putExtra("","");
                        startActivity(i);
                    }else {
                        AppUtils.giveStoragermission(BaseActivity.this);
                    }

                }
            });
            scan_layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ordersuccess_popupok_clicked = false;
                    mDrawerLayout.closeDrawer(left_menu_slider);
                    if (Util.db.getGroupCount(checked_in_eventId) == 0) {
                        showScannedTicketsAlert("Please Buy at least one scanattendee ticket to scan session.", false);
                    } else if (Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()) {
                        showScannedTicketsAlert("Please TurnON at least one session for scanning.", true);
                    } else if (Util.isMyServiceRunning(DownloadService.class, BaseActivity.this)) {
                        showServiceRunningAlert(checkedin_event_record.Events.Name);
                    } else {
                        if (Util.slide_menu_id != R.id.scanlayout) {
                            Util.slide_menu_id = R.id.scanlayout;
                        }
                        if(AppUtils.isCamPermissionGranted(BaseActivity.this)){
                            Intent i = new Intent(BaseActivity.this, BarCodeScanActivity.class);
                            i.putExtra(Util.SCAN, "scan_layout");
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            //startActivityForResult(i, Util.DASHBORD_ONACTIVITY_REQ_CODE);
                            startActivity(i);
                        }else {
                            AppUtils.giveCampermission(BaseActivity.this);
                        }
                    }

                }
            });

            offlinescan_layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.closeDrawer(left_menu_slider);
                    if (Util.slide_menu_id != R.id.offlinescanlayout) {
                        Util.slide_menu_id = R.id.offlinescanlayout;
                    }
                    Intent i = new Intent(BaseActivity.this, OfflineScanActivity.class);
                    i.putExtra(Util.SCAN, "offline_scan_layout");
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
            });

            list_layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.closeDrawer(left_menu_slider);
                    if (Util.slide_menu_id != R.id.listlayout) {
                        Util.slide_menu_id = R.id.listlayout;
                    }
					/*if (Util.isMyServiceRunning(DownloadService.class, BaseActivity.this)) {
						showServiceRunningAlert(checkedin_event_record.Events.Name);
					} else {*/
                    Intent i = new Intent(BaseActivity.this, AttendeeListActivity.class);
                    i.putExtra("sessionnamelayoutshow", true);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivityForResult(i, Util.DASHBORD_ONACTIVITY_REQ_CODE);
                    //}
                }
            });
            ticket_layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.closeDrawer(left_menu_slider);
                    if (Util.slide_menu_id != R.id.ticketlayout) {
                        Util.slide_menu_id = R.id.ticketlayout;
                        Intent i = new Intent(BaseActivity.this, ManageTicketActivity.class);
                        i.putExtra("Type", "Ticket");
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        //startActivity(i);
                        startActivityForResult(i, Util.DASHBORD_ONACTIVITY_REQ_CODE);
                    }
                }
            });
            order_layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.closeDrawer(left_menu_slider);
                    if (Util.slide_menu_id != R.id.orderlayout) {
                        Util.slide_menu_id = R.id.orderlayout;
						/*if (Util.isMyServiceRunning(DownloadService.class, BaseActivity.this)) {
							showServiceRunningAlert(checkedin_event_record.Events.Name);
						} else {*/
                        Intent i = new Intent(BaseActivity.this, SalesOrderActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivityForResult(i, Util.DASHBORD_ONACTIVITY_REQ_CODE);
                        //}
                    }
                }
            });
            logout_layout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    //if (Util.slide_menu_id != R.id.logoutlayout) {
                    //Util.slide_menu_id = R.id.logoutlayout;

                    if (!isOnline()) {
                        startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                    } else if (!Util.isMyServiceRunning(DownloadService.class, BaseActivity.this)) {
                        openLogoutDialog(BaseActivity.this);
                    } else {
                        showServiceRunningAlert(checkedin_event_record.Events.Name);
                    }
                    //}
                }
            });

            settings_layout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    mDrawerLayout.closeDrawer(left_menu_slider);
                    if (Util.slide_menu_id != R.id.settingslayout) {
                        //Util.slide_menu_id = R.id.settingslayout;
                        showSettingPopup(BaseActivity.this, v);
                    }
                }
            });
            selfcheckin_layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.closeDrawer(left_menu_slider);
                    if (Util.slide_menu_id != R.id.selfcheckinlayout) {
                        Util.slide_menu_id = R.id.selfcheckinlayout;
                    }
                    openSelfCheckinDialog(BaseActivity.this, true, "");

                }
            });
            img_search.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    back_layout.setVisibility(View.GONE);
                    search_layout.setVisibility(View.VISIBLE);
                }
            });
            img_socket_scanner.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    startActivity(new Intent(BaseActivity.this, ExternalSettingsActivity.class));
                }
            });

            errorAnimation = AnimationUtils.loadAnimation(this, R.anim.txt_translate_finish);
            errorAnimation.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation arg0) {
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                }
            });
            timer = new CountDownTimer(3000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    txt_error_msg.startAnimation(errorAnimation);
                }
            };
            img_setting.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.i("---Setting clicked----", ":");
                    if (mDrawerLayout.isDrawerOpen(left_menu_slider))
                        mDrawerLayout.closeDrawer(left_menu_slider);
                    int[] location = new int[2];
                    img_setting.getLocationOnScreen(location);
                    p = new Point();
                    p.x = location[0];
                    p.y = location[1];
                    if (p != null)
                        showSettingPopup(BaseActivity.this, v);
                }
            });
            if (!isEventAdmin()) {
                ticket_layout.setVisibility(View.GONE);
                order_layout.setVisibility(View.GONE);
                horizontal_line_tickets.setVisibility(View.GONE);
                horizontal_line_orders.setVisibility(View.GONE);
                if (isBuyersAndAttendeesHide()) {
                    list_layout.setVisibility(View.GONE);
                    horizontal_line_attendees.setVisibility(View.GONE);
                }
            }

            if (!NullChecker(checked_in_eventId).isEmpty()) {
                fillExternalSettings();
            }
            if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
                img_socket_scanner.setVisibility(View.GONE);
                img_scanner_base.setVisibility(View.GONE);
            } else {
                img_socket_scanner.setVisibility(View.VISIBLE);
                img_scanner_base.setVisibility(View.VISIBLE);
            }
            ExternalSettings ext_settings = new Gson().fromJson(
                    Util.external_setting_pref.getString(sfdcddetails.user_id + checked_in_eventId, ""),
                    ExternalSettings.class);
            if (ext_settings != null)
                isValidate_badge_reg_settings = ext_settings.isValidateBadge;

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void crashReport(String identifier) {
        AppUtils.isCrashReportEnable=false;
        if (AppUtils.isCrashReportEnable) {
            Crashlytics.setUserIdentifier(identifier);
            Crashlytics.setUserName(checkedin_event_record.Events.Name);
            Crashlytics.setUserEmail(user_profile.Profile.Email__c);
        }
    }
    public static Activity getBaseActivity(){
        return activity;
    }
    @Override
    protected void onStart() {
        super.onStart();

        //FlurryAgent.onStartSession(this, ScannerSettingsApplication.IMPROVE_APP_ID);

    }

    @Override
    protected void onStop() {
        super.onStop();
		/*if (Util.socket_device_pref.getBoolean(Util.SOCKET_DEVICE_CONNECTED, false)) {
			img_scanner_base.setBackgroundResource(R.drawable.green_circle_1);
		}else{
			img_scanner_base.setBackgroundResource(R.drawable.red_circle_1);
		}*/
    }

    @Override
    protected void onPause(){
        super.onPause();
        //if( sdk >= Build.VERSION_CODES.O) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    unregisterReceiver(offlineSyncBroadCast);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (setSoftScannerAPI()) {
                        unregisterReceiver(socketBroadCastReciever);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                return null;
            }
        }.execute();

        //}
		/*if (Util.socket_device_pref.getBoolean(Util.SOCKET_DEVICE_CONNECTED, false)) {
			img_scanner_base.setBackgroundResource(R.drawable.green_circle_1);
		}else{
			img_scanner_base.setBackgroundResource(R.drawable.red_circle_1);
		}*/
    }

    public boolean isBuyersAndAttendeesHide(){
        boolean buyers=false, attendee=false;
        List<RegistrationSettingsController> regsettings = Util.db.getRegSettingsList("where "+DBFeilds.REG_EVENT_ID+"='"+checked_in_eventId+"' AND "+DBFeilds.REG_SETTING_TYPE+"='ScanAttendeeapp'");
        for(RegistrationSettingsController setting:regsettings){
            if(setting.Label_Name__c.equalsIgnoreCase("Display Attendees")){
                attendee = Boolean.valueOf(setting.Included__c);
            }else if(setting.Label_Name__c.equalsIgnoreCase("Display Buyers")){
                buyers = Boolean.valueOf(setting.Included__c);
            }
        }
        if(!buyers && !attendee){
            return true;
        }
        return false;
    }
    public void fillExternalSettings(){
        try {
            ExternalSettings ext_settings = new ExternalSettings();
            List<RegistrationSettingsController> regsettings = Util.db.getRegSettingsList("where " + DBFeilds.REG_EVENT_ID + "='" + checked_in_eventId + "' AND " + DBFeilds.REG_SETTING_TYPE + "='SAappdefault'");
            for (RegistrationSettingsController setting : regsettings) {
                if (setting.Column_Name__c.equalsIgnoreCase(getString(R.string.quick_checkin))) {
                    ext_settings.quick_checkin = setting.Included__c;
                } else if (setting.Column_Name__c.equalsIgnoreCase(getString(R.string.print_on_scan))) {
                    ext_settings.quick_print = setting.Included__c;
                } else if (setting.Column_Name__c.equalsIgnoreCase(getString(R.string.custom_barcode))) {
                    ext_settings.custom_barcode = setting.Included__c;
                } else if (setting.Column_Name__c.equalsIgnoreCase(getString(R.string.validate_badge))) {
                    ext_settings.isValidateBadge = setting.Included__c;
                } else if (setting.Column_Name__c.equalsIgnoreCase(getString(R.string.checkin_checkout))) {
                    ext_settings.checkin_checkout = setting.Included__c;
                } else if (setting.Column_Name__c.equalsIgnoreCase(getString(R.string.allow_promocode))) {
                    ext_settings.allow_promocode = setting.Included__c;
                }
            }
            String res = new Gson().toJson(ext_settings).toString();
            if (NullChecker(Util.external_setting_pref.getString(sfdcddetails.user_id + checked_in_eventId, "")).isEmpty()) {
                Util.external_setting_pref.edit().putString(sfdcddetails.user_id + checked_in_eventId, res).commit();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getDeviceInches() {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        float widthDpi = displayMetrics.xdpi;
        float heightDpi = displayMetrics.ydpi;
        float widthInches = width / widthDpi;
        float heightInches = height / heightDpi;
        diagonalInches = Math.sqrt((widthInches * widthInches) + (heightInches * heightInches));
        diagonalInches = (int) Math.round(diagonalInches);
    }
    private  boolean setSoftScannerAPI() {
        boolean _bluetoothIsOn = false;
        BluetoothAdapter _bluetoothAdapter;
        try {
            // If the adapter is null, then Bluetooth is not supported
            _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (_bluetoothAdapter != null) {
                _bluetoothIsOn = _bluetoothAdapter.isEnabled();
            }
            if (_bluetoothAdapter == null) {
                AlertDialogCustom dialog = new AlertDialogCustom(BaseActivity.this);
                dialog.setParamenters("Error", "Bluetooth is not available", null, null, 1, false);
                dialog.setAlertImage(R.drawable.alert_error, "error");
                dialog.show();
                return false;
            }

            _bluetoothIsOn = _bluetoothAdapter.isEnabled();
            if (_bluetoothIsOn) {
                return true;
				/*Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, 1);*/
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return _bluetoothIsOn;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if( sdk >= Build.VERSION_CODES.O) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                setSocketScannerStatus();
                registerReceiver(offlineSyncBroadCast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

//socketmobile broadcast register
                if(setSoftScannerAPI()) {
                    IntentFilter filterSocketMobile = new IntentFilter();
                    filterSocketMobile.addAction("com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication.NotifyScannerArrival");
                    filterSocketMobile.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
                    filterSocketMobile.addAction("com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication.DecodedData");
                    filterSocketMobile.addAction("com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication.NotifyErrorMessage");
                    filterSocketMobile.addAction("com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication.NotifyScannerRemoval");
                    filterSocketMobile.addAction("com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication.NotifyDataArrival");
                    filterSocketMobile.addAction("com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication.GetBatteryLevelComplete");
                    filterSocketMobile.addAction("com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication.GetSoundConfigComplete");
                    registerReceiver(socketBroadCastReciever, filterSocketMobile);
                }

                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

// if bluetooth is on and already connected
                BluetoothAdapter _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (ScannerSettingsApplication.getInstance().getCurrentDevice() == null && _bluetoothAdapter != null && _bluetoothAdapter.isEnabled()) {
                    ScannerSettingsApplication.getInstance().onCreate();
                    ScannerSettingsApplication.getInstance().increaseViewCount();
                }
            }
        }.execute();
        //}
        ScannerSettingsApplication.getInstance().increaseViewCount();
		/*Capture.builder(getApplicationContext())
				.enableLogging(BuildConfig.DEBUG)
				.build();*/
        setSocketScannerStatus();
        baseDialog = new ProgressDialog(this);
        visibleOffLineMode(!Util.isInternetComing);
        new CheckNetConnectionBackground().execute();
       /* if(!getOnlinemode())
        new CheckNetConnectionBackground().execute();
        else {
            if(BaseActivity.layout_offline_tag != null){
                BaseActivity.layout_offline_tag.setVisibility(View.VISIBLE);
                BaseActivity.txt_offline.setText("APP IS IN OFFLINE MODE");
            }
        }*/
        //zebraPrinter=new ZebraPrinter(this);
        //wl.acquire();
		/*AndroidAuthSession session = Util.mApi.getSession();
		if (session.authenticationSuccessful()) {
			session.finishAuthentication();
			Util.mLoggedIn = true;
			TokenPair tokens = session.getAccessTokenPair();
			storeKeys(tokens.key, tokens.secret);
		}*/
        //ScannerSettingsApplication.getInstance().increaseViewCount();
        //registerSocketScannerBroadcast();

        //enableOFFLineScan();
    }

    private void enableOFFLineScan(){

        ExternalSettings ext_settings = new ExternalSettings();
        if (!Util.external_setting_pref.getString(sfdcddetails.user_id + checked_in_eventId, "").isEmpty()) {
            ext_settings = new Gson().fromJson(
                    Util.external_setting_pref.getString(sfdcddetails.user_id + checked_in_eventId, ""),
                    ExternalSettings.class);
            if(ext_settings.offline_scan || !isOnline()){
                offlinescan_layout.setVisibility(View.VISIBLE);
            }else{
                offlinescan_layout.setVisibility(View.GONE);
            }
        }
    }

	/*@SuppressWarnings("deprecation")
	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(Util.APP_KEY, Util.APP_SECRET);
		AndroidAuthSession session;
		String[] stored = getKeys();
		if (stored != null) {
			AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
			session = new AndroidAuthSession(appKeyPair, Util.ACCESS_TYPE, accessToken);
		} else {
			session = new AndroidAuthSession(appKeyPair, Util.ACCESS_TYPE);
		}
		return session;
	}*/
    /*
     * private void showToast(String msg) { Toast error = Toast.makeText(this,
     * msg, Toast.LENGTH_LONG); error.show(); }
     */

    /*
     * @Override protected void onActivityResult(int requestCode, int
     * resultCode, Intent data) { super.onActivityResult(requestCode,
     * resultCode, data); //Log.i("---------------onActivity Result------------"
     * ,":"+requestCode+" : "+resultCode); if(requestCode ==
     * Util.DASHBORD_ONACTIVITY_REQ_CODE){ Intent i = new
     * Intent(BaseActivity.this,SplashActivity.class); startActivity(i);
     * finish(); }else if(requestCode == StripeAccountActivity.STRIPE_CODE){
     * PGatewayKeyList.getStripeAdaptiveKeys(data.getStringExtra(
     * StripeAccountActivity.STRIPE_RESPONSE)); } }
     */
    private void storeKeys(String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = getSharedPreferences(Util.ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(Util.ACCESS_KEY_NAME, key);
        edit.putString(Util.ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    private String[] getKeys() {
        SharedPreferences prefs = getSharedPreferences(Util.ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(Util.ACCESS_KEY_NAME, null);
        String secret = prefs.getString(Util.ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
            String[] ret = new String[2];
            ret[0] = key;
            ret[1] = secret;
            return ret;
        } else {
            return null;
        }
    }

    public static void clearCookieByUrl(String url, CookieManager pCookieManager,
                                        CookieSyncManager pCookieSyncManager) {
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        clearCookieByUrlInternal(url, pCookieManager, pCookieSyncManager);
        clearCookieByUrlInternal("http://." + host, pCookieManager, pCookieSyncManager);
        clearCookieByUrlInternal("https://." + host, pCookieManager, pCookieSyncManager);
    }

    private static void clearCookieByUrlInternal(String url, CookieManager pCookieManager,
                                                 CookieSyncManager pCookieSyncManager) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String cookieString = pCookieManager.getCookie(url);
        Vector<String> cookie = getCookieNamesByUrl(cookieString);
        if (cookie == null || cookie.isEmpty()) {
            return;
        }
        int len = cookie.size();
        for (int i = 0; i < len; i++) {
            pCookieManager.setCookie(url, cookie.get(i) + "=-1");
        }
        pCookieSyncManager.sync();
    }

    private static Vector<String> getCookieNamesByUrl(String cookie) {
        if (TextUtils.isEmpty(cookie)) {
            return null;
        }
        String[] cookieField = cookie.split(";");
        int len = cookieField.length;
        for (int i = 0; i < len; i++) {
            cookieField[i] = cookieField[i].trim();
        }
        Vector<String> allCookieField = new Vector<String>();
        for (int i = 0; i < len; i++) {
            if (TextUtils.isEmpty(cookieField[i])) {
                continue;
            }
            if (!cookieField[i].contains("=")) {
                continue;
            }
            String[] singleCookieField = cookieField[i].split("=");
            allCookieField.add(singleCookieField[0]);
        }
        if (allCookieField.isEmpty()) {
            return null;
        }
        return allCookieField;
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isAcceptingText()) {
                if (inputMethodManager.isActive()) {
                    inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                }
            }
        }
    }

    public void hideKeybord(View view) {
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    public void openKeybord(View view) {
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.SHOW_FORCED);
    }

    public boolean isPhoneNumberValid(String phoneNumber) {
        boolean isValid = true;
        if (phoneNumber.length() == 10) {
            isValid = false;
        }
        return isValid;
    }

    @SuppressLint("SimpleDateFormat")
    public boolean isPastEvent() {
        boolean ispast = false;
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm a",Locale.US);
        // SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String todaydatestring = format.format(date);
        try {
            String event_end_date = Util.change_US_ONLY_DateFormat(checkedin_event_record.Events.End_Date__c, checkedin_event_record.Events.Time_Zone__c);
            Date eventdate = format.parse(format.format(Util.db_date_format1.parse(event_end_date)));
            Date todaydate = format.parse(todaydatestring);
            //Log.i("----Todays Date---", ":" + todaydate + "String" + todaydatestring);
            if (todaydate.after(eventdate)) {
                ispast = true;
            } else {
                ispast = false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ispast;
    }

    public boolean isFutureTicket(String Item_sale_startdate){
        boolean isFuture = false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm a",Locale.US);
        Date date = new Date();
        String todaydatestring = format.format(date);
        try {

            Date todaydate = format.parse(todaydatestring);
            Date sale_startdate = format.parse(format.format(Util.db_date_format1.parse(Item_sale_startdate)));
            if(todaydate.after(sale_startdate)){
                isFuture = false;
            }else{
                isFuture =true;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return isFuture;
    }


    public void _showCustomToast(String message, int gravity) {
        getUserProfileInfo();
        Toast toast = Toast.makeText(BaseActivity.this, message, Toast.LENGTH_SHORT);
        toast.setGravity(gravity, 0, 0);
        toast.show();
    }

    private void getUserProfileInfo() {
        String user_id = sfdcddetails.user_id;
        //Log.i("----APP USER ID----", ":" + user_id);
        user_profile = Util.db.getAppUserProfile(user_id);
    }

    @SuppressWarnings("deprecation")
    private void showSettingPopup(final Activity context, View v) {

        final Dialog alertDialog = new Dialog(context, R.style.MyCustomTheme);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setCancelable(true);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        if(!isEventAdmin()){
            alertDialog.setContentView(R.layout.dashboard_setting_for_staff);
        }else{
            alertDialog.setContentView(R.layout.dashboard_setting_bubble_dialog);

        }
        //LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _salestax = (LinearLayout) alertDialog.findViewById(R.id.lay_salesTax);
        _stripe = (LinearLayout) alertDialog.findViewById(R.id.lay_paymentSetting);
        _badgetemplate = (LinearLayout) alertDialog.findViewById(R.id.lay_badgeTemp);

        //_fee = (TextView) layout.findViewById(R.id.lay_);

        //_paypal = (TextView) layout.findViewById(R.id.txtpaypal);
        //_dropbox = (TextView) layout.findViewById(R.id.txtdropbox);
        txt_profile = (LinearLayout) alertDialog.findViewById(R.id.lay_profile);
        txt_ext_setting = (LinearLayout)alertDialog.findViewById(R.id.lay_exterSettings);
        txt_scantickets = (LinearLayout)alertDialog.findViewById(R.id.lay_scanTickets);
        LinearLayout lay_cancle=(LinearLayout)alertDialog.findViewById(R.id.lay_cancle);

//_paypal.setVisibility(View.GONE);

        final Animation animBounce = AnimationUtils.loadAnimation(this, R.anim.bounce_anim);
        ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f,
                ScaleAnimation.RELATIVE_TO_SELF, .5f);
        scale.setDuration(300);
        scale.setInterpolator(new OvershootInterpolator());
        if(isEventAdmin()){
            _salestax.startAnimation(animBounce);
            _stripe.startAnimation(animBounce);
        }
        _badgetemplate.startAnimation(animBounce);
        txt_ext_setting.startAnimation(animBounce);
        txt_profile.startAnimation(animBounce);
        txt_scantickets.startAnimation(animBounce);
        alertDialog.show();


        txt_profile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                startActivity(new Intent(BaseActivity.this, UserProfileActivity.class));
            }
        });


        _badgetemplate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppUtils.isStoragePermissionGranted(BaseActivity.this)) {
                    alertDialog.dismiss();
                    startActivity(new Intent(BaseActivity.this, BadgeTemplateNewActivity.class));
                }else {
                    AppUtils.giveStoragermission(BaseActivity.this);
                }
            }
        });
        if(isEventAdmin()){
            _stripe.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isEventOrganizer()){
                        Util.setCustomAlertDialog(BaseActivity.this);
                        Util.openCustomDialog("Alert", "Please contact your Event Admin to set the Payment Gateways");
                        Util.txt_okey.setText("Ok");
                        Util.txt_dismiss.setVisibility(View.GONE);
                        Util.txt_okey.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {

                                Util.alert_dialog.dismiss();
                            }
                        });
                        //Toast.makeText(BaseActivity.this,"As EventOrganizer you don't have access to payment settings please contact EventAdmin.",Toast.LENGTH_LONG).show();
                    }else{
                        alertDialog.dismiss();
                        Intent i = new Intent(BaseActivity.this, PaymentSetting.class);
                        i.putExtra(Util.EDIT_EVENT_ID, checked_in_eventId);
                        startActivity(i);
                    }

                }
            });
            _salestax.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    startActivity(new Intent(BaseActivity.this, SalesTaxActivity.class));
                }
            });

        }
		/*_dropbox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
				if (Util.mLoggedIn) {
					startActivity(new Intent(BaseActivity.this, DropboxActivity.class));
				} else {
					// Start the remote authentication
					Util.mApi.getSession().startAuthentication(BaseActivity.this);
				}
			}
		});*/
        txt_ext_setting.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                alertDialog.dismiss();
                startActivity(new Intent(BaseActivity.this,ExternalSettingsActivity.class));
            }
        });
        txt_scantickets.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                alertDialog.dismiss();
                startActivity(new Intent(BaseActivity.this,ScannedTicketSettings.class));
				/*if(Util.db.getScannedItems(checked_in_eventId).size() == 0){
					showScannedTicketsAlert("Please Buy at least one scanattendee ticket to scan tickets.",false);
				}else{
					startActivity(new Intent(BaseActivity.this,ScannedTicketSettings.class));
				}*/

            }
        });

        lay_cancle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                alertDialog.dismiss();
                Util.slide_menu_id = 0;
            }
        });
    }

    public void startErrorAnimation(String msg, TextView view) {

        animTranslate = AnimationUtils.loadAnimation(this, R.anim.text_translate);
        animTranslate.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                timer.start();
            }
        });
        view.setVisibility(View.VISIBLE);
//        if(!getOnlinemode())
//            msg="App is in Offline Mode";

        view.setText(msg);
        view.startAnimation(animTranslate);
    }

    public abstract void setCustomContentView(int layout);

    public float setNameFontSize(String str, float defaultfontSize) {
        if (defaultfontSize > 12 && defaultfontSize <= 22) {
            if (str.length() <= 10)
                defaultfontSize = defaultfontSize - 0;
            else if (str.length() > 10 && str.length() <= 15)
                defaultfontSize = defaultfontSize - 1;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 2;
            else if (str.length() > 20 && str.length() <= 30)
                defaultfontSize = defaultfontSize - 4;
            else if (str.length() > 30 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 10;
        } else if (defaultfontSize > 22 && defaultfontSize <= 26) {
            if (str.length() <= 10)
                defaultfontSize = defaultfontSize - 2;
            else if (str.length() > 10 && str.length() <= 15)
                defaultfontSize = defaultfontSize - 4;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 6;
            else if (str.length() > 20 && str.length() <= 30)
                defaultfontSize = defaultfontSize - 9;
            else if (str.length() > 30 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 14;
        } else if (defaultfontSize > 26 && defaultfontSize <= 30) {
            if (str.length() <= 10)
                defaultfontSize = defaultfontSize - 4;
            else if (str.length() > 10 && str.length() <= 15)
                defaultfontSize = defaultfontSize - 6;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 9;
            else if (str.length() > 20 && str.length() <= 30)
                defaultfontSize = defaultfontSize - 13;
            else if (str.length() > 30 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 17;
        } else if (defaultfontSize > 30 && defaultfontSize <= 40) {
            if (str.length() <= 10)
                defaultfontSize = defaultfontSize - 6;
            else if (str.length() > 10 && str.length() <= 15)
                defaultfontSize = defaultfontSize - 12;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 15;
            else if (str.length() > 20 && str.length() <= 30)
                defaultfontSize = defaultfontSize - 18;
            else if (str.length() > 30 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 22;
        } else if (defaultfontSize > 40 && defaultfontSize <= 50) {
            if (str.length() <= 10)
                defaultfontSize = defaultfontSize - 9;
            else if (str.length() > 10 && str.length() <= 15)
                defaultfontSize = defaultfontSize - 11;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 15;
            else if (str.length() > 20 && str.length() <= 30)
                defaultfontSize = defaultfontSize - 20;
            else if (str.length() > 30 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 24;
        }
        return defaultfontSize;
    }

    public float setCategoryFontSize(String str, float defaultfontSize) {
        if (defaultfontSize > 12 && defaultfontSize <= 22) {
            if (str.length() <= 0 & str.length() <= 15)
                defaultfontSize = defaultfontSize - 0;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 0;
            else if (str.length() > 20 && str.length() <= 25)
                defaultfontSize = defaultfontSize - 3;
            else if (str.length() > 25 && str.length() <= 35)
                defaultfontSize = defaultfontSize - 5;
            else if (str.length() > 35 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 6;
        } else if (defaultfontSize > 22 && defaultfontSize <= 26) {
            if (str.length() <= 0 & str.length() <= 15)
                defaultfontSize = defaultfontSize - 2;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 5;
            else if (str.length() > 20 && str.length() <= 25)
                defaultfontSize = defaultfontSize - 8;
            else if (str.length() > 25 && str.length() <= 35)
                defaultfontSize = defaultfontSize - 13;
            else if (str.length() > 35 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 13;
        } else if (defaultfontSize > 26 && defaultfontSize <= 30) {
            if (str.length() <= 0 & str.length() <= 15)
                defaultfontSize = defaultfontSize - 5;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 10;
            else if (str.length() > 20 && str.length() <= 25)
                defaultfontSize = defaultfontSize - 14;
            else if (str.length() > 25 && str.length() <= 35)
                defaultfontSize = defaultfontSize - 17;
            else if (str.length() > 35 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 19;
        } else if (defaultfontSize > 30 && defaultfontSize <= 40) {
            if (str.length() <= 0 & str.length() <= 15)
                defaultfontSize = defaultfontSize - 8;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 10;
            else if (str.length() > 20 && str.length() <= 25)
                defaultfontSize = defaultfontSize - 13;
            else if (str.length() > 25 && str.length() <= 35)
                defaultfontSize = defaultfontSize - 16;
            else if (str.length() > 35 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 17;
        } else if (defaultfontSize > 40 && defaultfontSize <= 50) {
            if (str.length() <= 0 & str.length() <= 15)
                defaultfontSize = defaultfontSize - 9;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 12;
            else if (str.length() > 20 && str.length() <= 25)
                defaultfontSize = defaultfontSize - 15;
            else if (str.length() > 25 && str.length() <= 35)
                defaultfontSize = defaultfontSize - 18;
            else if (str.length() > 35 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 20;
        } else {
            defaultfontSize = defaultfontSize - 0;
        }
        return defaultfontSize;
    }

    public float setCompanyFontSize(String str, float defaultfontSize) {
        if (defaultfontSize > 12 && defaultfontSize <= 22) {
            if (str.length() <= 10)
                defaultfontSize = defaultfontSize - 0;
            else if (str.length() > 10 && str.length() <= 15)
                defaultfontSize = defaultfontSize - 2;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 5;
            else if (str.length() > 20 && str.length() <= 35)
                defaultfontSize = defaultfontSize - 6;
            else if (str.length() > 25 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 8;
        } else if (defaultfontSize > 22 && defaultfontSize <= 26) {
            if (str.length() <= 10)
                defaultfontSize = defaultfontSize - 1;
            else if (str.length() > 10 && str.length() <= 15)
                defaultfontSize = defaultfontSize - 4;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 6;
            else if (str.length() > 20 && str.length() <= 25)
                defaultfontSize = defaultfontSize - 8;
            else if (str.length() > 25 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 10;
        } else if (defaultfontSize > 26 && defaultfontSize <= 30) {
            if (str.length() <= 10)
                defaultfontSize = defaultfontSize - 2;
            else if (str.length() > 10 && str.length() <= 15)
                defaultfontSize = defaultfontSize - 7;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 9;
            else if (str.length() > 20 && str.length() <= 25)
                defaultfontSize = defaultfontSize - 12;
            else if (str.length() > 25 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 15;
        } else if (defaultfontSize > 30 && defaultfontSize <= 40) {
            if (str.length() <= 10)
                defaultfontSize = defaultfontSize - 5;
            else if (str.length() > 10 && str.length() <= 15)
                defaultfontSize = defaultfontSize - 8;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 10;
            else if (str.length() > 20 && str.length() <= 25)
                defaultfontSize = defaultfontSize - 13;
            else if (str.length() > 25 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 17;
        } else if (defaultfontSize > 40 && defaultfontSize <= 50) {
            if (str.length() <= 10)
                defaultfontSize = defaultfontSize - 7;
            else if (str.length() > 10 && str.length() <= 15)
                defaultfontSize = defaultfontSize - 10;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 12;
            else if (str.length() > 20 && str.length() <= 25)
                defaultfontSize = defaultfontSize - 15;
            else if (str.length() > 25 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 20;
        } else {
            defaultfontSize = defaultfontSize - 0;
        }
        return defaultfontSize;
    }

    public float pxToDp1(float px) {
        float valueInDp = (Float) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px,
                getResources().getDisplayMetrics());
        return valueInDp;
    }

    public int pxToDp(int px) {
        int valueInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px,
                getResources().getDisplayMetrics());
        return valueInDp;
    }

    public int convertDpToPixel(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    public float setTicketFontSize(String str, float defaultfontSize) {
        // //Log.i(defaultfontSize+"----Badge Ticket Name---",":"+str);
        if (defaultfontSize > 12 && defaultfontSize <= 22) {
            if (str.length() <= 10)
                defaultfontSize = defaultfontSize - 4;
            else if (str.length() > 10 && str.length() <= 15)
                defaultfontSize = defaultfontSize - 6;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 9;
            else if (str.length() > 20 && str.length() <= 25)
                defaultfontSize = defaultfontSize - 10;
            else if (str.length() > 25 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 12;
            else if (str.length() > 40 && str.length() <= 100)
                defaultfontSize = defaultfontSize - 16;
        } else if (defaultfontSize > 26 && defaultfontSize <= 30) {
            if (str.length() <= 10)
                defaultfontSize = defaultfontSize - 7;
            else if (str.length() > 10 && str.length() <= 15)
                defaultfontSize = defaultfontSize - 11;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 14;
            else if (str.length() > 20 && str.length() <= 25)
                defaultfontSize = defaultfontSize - 16;
            else if (str.length() > 25 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 30;
            else if (str.length() > 40 && str.length() <= 100)
                defaultfontSize = defaultfontSize - 32;
        } else if (defaultfontSize > 22 && defaultfontSize <= 26) {
            if (str.length() <= 10)
                defaultfontSize = defaultfontSize - 6;
            else if (str.length() > 10 && str.length() <= 15)
                defaultfontSize = defaultfontSize - 8;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 11;
            else if (str.length() > 20 && str.length() <= 25)
                defaultfontSize = defaultfontSize - 12;
            else if (str.length() > 25 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 14;
            else if (str.length() > 40 && str.length() <= 100)
                defaultfontSize = defaultfontSize - 16;
        } else if (defaultfontSize > 30 && defaultfontSize <= 40) {
            if (str.length() <= 10)
                defaultfontSize = defaultfontSize - 12;
            else if (str.length() > 10 && str.length() <= 15)
                defaultfontSize = defaultfontSize - 13;
            else if (str.length() > 15 && str.length() <= 20)
                defaultfontSize = defaultfontSize - 16;
            else if (str.length() > 20 && str.length() <= 25)
                defaultfontSize = defaultfontSize - 18;
            else if (str.length() > 25 && str.length() <= 40)
                defaultfontSize = defaultfontSize - 20;
            else if (str.length() > 40 && str.length() <= 100)
                defaultfontSize = defaultfontSize - 22;
        } else {
            defaultfontSize = defaultfontSize - 0;
        }
        // //Log.i(str.length()+"---Reizse Ticket Font
        // Size---",":"+defaultfontSize);
        return defaultfontSize;
    }

    public String capitalizeFirstLetter(String original) {
        if (original.length() == 0)
            return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    public static String NullChecker(String var) {
        if (var == null) {
            return "";
        } else {
            if (var.equals("null")) {
                return "";
            } else
                return var;
        }
    }
    public static boolean getOnlinemode(){
        boolean result=false;
        ExternalSettings externalSettings;
        try {
            externalSettings = new ExternalSettings();

            if (!Util.external_setting_pref.getString(BaseActivity.user_profile.Userid + BaseActivity.checkedin_event_record.Events.Id, "").isEmpty()) {
                externalSettings = new Gson().fromJson(Util.external_setting_pref.getString(BaseActivity.user_profile.Userid + BaseActivity.checkedin_event_record.Events.Id, ""), ExternalSettings.class);
                result=externalSettings.online_mode;
            }

            /// result=externalSettings.offline_mode;


        }catch (Exception e){
            result=false;
            e.printStackTrace();
        }
        return result;
    }
    public String getESTFormat() {
        Calendar currentdatetime = Calendar.getInstance();
        TimeZone tz = TimeZone.getTimeZone("EST5EDT");
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy hh:mm a",Locale.US);
        formatter.setTimeZone(tz);
        return formatter.format(currentdatetime.getTime());
    }

    public  boolean isOnline() {
        /*
         * ConnectivityManager cm = (ConnectivityManager)
         * getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo ni =
         * cm.getActiveNetworkInfo(); if (ni != null && ni.isConnected()) return
         * true; return false;
         */

        if (Connectivity.isConnectedFast(BaseActivity.this)) {//&&getOnlinemode()
            //&&!isOfflineModeON()
            return true;
        }
        return false;
    }
    private class CheckNetConnectionBackground extends android.os.AsyncTask<Void, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return isOnline1();
        }

        @Override
        protected void onPostExecute(Boolean response){
            super.onPostExecute(response);
            AppUtils.displayLog("Internet access ","==="+response);
            visibleOffLineMode(!response);
			/*if(response)
				detectInternet();*/

        }
    }
    public void visibleOffLineMode(final boolean isVisible){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AppUtils.displayLog("visibleOffLineMode+ ","==="+isVisible);
                if(isVisible){
                    Util.isInternetComing=false;
                    if(BaseActivity.layout_offline_tag != null){
                        BaseActivity.layout_offline_tag.setVisibility(View.VISIBLE);
                        BaseActivity.txt_offline.setText("OFFLINE MODE");
                    }
                }else{
                    Util.isInternetComing=true;
                    if(BaseActivity.layout_offline_tag != null){
                        BaseActivity.layout_offline_tag.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
    public boolean isNetAvailable() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()){
            try{
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 50);
                HttpConnectionParams.setSoTimeout(httpParameters, 50);
                HttpClient _httpclient = new DefaultHttpClient(httpParameters);
                HttpGet _httpget = new HttpGet("http://www.google.com");
                HttpResponse _response = _httpclient.execute(_httpget);
                int _responsecode =_response.getStatusLine().getStatusCode();

                if(_responsecode == 200){
                    //visibleOffLineMode(false);
                    return new Boolean(true);
                }else{
                    //visibleOffLineMode(true);
                }
            }catch(MalformedURLException e1){
                e1.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        return false;
    }
    public boolean isInternetAvailable() {
        try {

            InetAddress ipAddr = InetAddress.getByName("http://www.google.com"); //You can replace it with your name

            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
    private  boolean isWifiAvailable() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return wifi.isConnected();
    }
    public  boolean isInternetAccessible(Context context) {
        if (isWifiAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setReadTimeout(50);
                urlc.setConnectTimeout(50);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Couldn't check internet connection", e);
            }
        } else {
            Log.d(LOG_TAG, "Internet not available!");
        }
        return false;
    }
    public boolean isOnline1() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()){
            try{
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
                HttpConnectionParams.setSoTimeout(httpParameters, 6000);
                HttpClient _httpclient = new DefaultHttpClient(httpParameters);
                HttpGet _httpget = new HttpGet("http://www.google.com");
                HttpResponse _response = _httpclient.execute(_httpget);
                int _responsecode =_response.getStatusLine().getStatusCode();

                if(_responsecode == 200){
                    visibleOffLineMode(false);
                    return new Boolean(true);
                }else{
                    visibleOffLineMode(true);
                }
            }catch(MalformedURLException e1){
                e1.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        return false;
    }

    public static boolean isSmallDevice(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_NORMAL;
    }

    public static boolean isLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    public void lockDrawer(){
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
    public  void unLockDrawer(){
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private class updatePaypalSetting extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            txt_loading.setText("Updating, please wait...");
            progress_dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return doPaypalRequest();
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            progress_dialog.dismiss();
            parseServerResponse(response);
        }
    }

    @SuppressWarnings("deprecation")
    public void parseServerResponse(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            final String error = obj.optString("error");
            if (error.equalsIgnoreCase("null")) {
                Util.db.updatePayPalEmail(user_profile.Userid, paypal_rec_email);
                removeDialog(PAYPAL_DIALOG);
                _showCustomToast("Paypal setting has been updated successfully.", Gravity.CENTER);
            } else {
                _showCustomToast(error, Gravity.TOP);
            }
        } catch (final Exception e) {
            progress_dialog.dismiss();
            _showCustomToast(getResources().getString(R.string.connection_error), Gravity.CENTER);
        }
    }

    public String doPaypalRequest() {
        String result = "";
        HttpClient client = new DefaultHttpClient();
        String url = WebServiceUrls.SA_PROFILE_UPDATE_URL + "PayPalEmail=" + paypal_rec_email + "&Email="
                + user_profile.Profile.Email__c + "&Status=EDITPAYPAL";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Authorization", sfdcddetails.token_type + " " + sfdcddetails.access_token);
        HttpResponse httpresp;
        try {
            httpresp = client.execute(httpPost);
            result = EntityUtils.toString(httpresp.getEntity());
        } catch (Exception e) {
        }
        return result;
    }

    public static void initializeDB(Context ctx) {
        if (Util.db == null) {
            Util.db = new DataBase(ctx);
            try {
                Util.db.open();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (!Util.db.isOpen()) {
            try {
                Util.db.open();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        fileOrDirectory.delete();
    }
    public static void DeleteFiles() {
        File folder = new File(Environment.getExternalStorageDirectory().toString() + Util.APP_FOLDER_NAME);
        try {
            // folder.delete();
            //Log.i("------------File Name--------",":"+folder.getPath()+" :: "+folder.listFiles().length);
            deleteRecursive(folder);
            //FileUtils.deleteDirectory(folder);
        } catch (Exception e) {
            // TODO: handle exception
        }

		/*if (folder.exists()) {
			Log.i("------------File list Count--------",":"+folder.listFiles().length);
			int filescount = folder.listFiles().length;
			for (int i = 0; i < filescount; i++) {
				Log.i("------------File list Count--------",":"+folder.listFiles()[0].getName()+" : "+new File(folder.listFiles()[0].toString()).delete());
			}
		}*/
    }
    public void openSelfCheckinDialog(final Activity fromactivity,final boolean fromdashbord,final String adminpassword){
        try{
            ask_dialog = new Dialog(fromactivity);
            ask_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            ask_dialog.setContentView(R.layout.theme_alert_dialog);
            ask_dialog.setCancelable(false);
            ask_dialog.show();
            TextView txt_title = (TextView) ask_dialog.findViewById(R.id.txt_title);
            final TextView txt_message = (TextView) ask_dialog.findViewById(R.id.txt_message);
            TextView txt_selfcheckinmsg = (TextView) ask_dialog.findViewById(R.id.txt_selfcheckinmsg);
            final EditText password =(EditText) ask_dialog.findViewById(R.id.edt_password);
            final CheckBox checkeditable =(CheckBox) ask_dialog.findViewById(R.id.checkbox_editable);
            final RadioGroup radiogroup = (RadioGroup) ask_dialog.findViewById(R.id.radiogroup);
            final RadioButton radio_printing =(RadioButton) ask_dialog.findViewById(R.id.radiobutton_print);
            final CheckBox checkreprint =(CheckBox) ask_dialog.findViewById(R.id.checkbox_reprint);
            final LinearLayout child_printcheckboxs = (LinearLayout) ask_dialog.findViewById(R.id.child_printcheckboxs);
            final CheckBox checkautocheckin =(CheckBox) ask_dialog.findViewById(R.id.checkbox_autocheckin);
            final CheckBox checkboxaddquest=(CheckBox) ask_dialog.findViewById(R.id.checkbox_addquest);
            final CheckBox checkboxfreequest=(CheckBox) ask_dialog.findViewById(R.id.checkbox_free_tickets);
            final CheckBox checkboxpaidquest=(CheckBox) ask_dialog.findViewById(R.id.checkbox_paid_tickets);
            final CheckBox checkboxallowscantoprint=(CheckBox) ask_dialog.findViewById(R.id.checkbox_allowscanning);
            final RadioButton radio_not_printing =(RadioButton) ask_dialog.findViewById(R.id.radiobutton_notprint);
            final CheckBox checkboxonlyscancheckin =(CheckBox) ask_dialog.findViewById(R.id.checkbox_onlyscan_checkin);
            final CheckBox checkboxforcheckin =(CheckBox) ask_dialog.findViewById(R.id.checkbox_allow_checkin);
            final CheckBox checkbox_searchvalidation=(CheckBox) ask_dialog.findViewById(R.id.checkbox_search_validation);
            final CheckBox Firstname=(CheckBox) ask_dialog.findViewById(R.id.fname);
            final CheckBox Lastname=(CheckBox) ask_dialog.findViewById(R.id.lname);
            final CheckBox Emailid=(CheckBox) ask_dialog.findViewById(R.id.emailid);
            final CheckBox Company=(CheckBox) ask_dialog.findViewById(R.id.company);
            final CheckBox Orderid=(CheckBox) ask_dialog.findViewById(R.id.order_id);
            final CheckBox TicketNo=(CheckBox) ask_dialog.findViewById(R.id.ticketno);
            final LinearLayout child_searchcheckboxs=(LinearLayout) ask_dialog.findViewById(R.id.child_searchcheckboxs);
            final LinearLayout lin_password=(LinearLayout) ask_dialog.findViewById(R.id.lin_password) ;
            Button btn_cancel = (Button) ask_dialog.findViewById(R.id.btnCancel);
            final Button btn_okey = (Button) ask_dialog.findViewById(R.id.btnOK);
            ImageView image = (ImageView) ask_dialog.findViewById(R.id.img_alert);
            Button closebutton = (Button) ask_dialog.findViewById(R.id.close_button);
            txt_message.setTypeface(Util.roboto_regular);
            btn_cancel.setTypeface(Util.roboto_regular);
            btn_okey.setTypeface(Util.roboto_regular);
            txt_title.setVisibility(View.GONE);
            txt_message.setVisibility(View.GONE);
            txt_selfcheckinmsg.setVisibility(View.VISIBLE);
            lin_password.setVisibility(View.VISIBLE);
            closebutton.setVisibility(View.VISIBLE);
            password.setFocusable(true);
            SelfcheckinColumns columns = new SelfcheckinColumns();

            if(fromdashbord&&!isselfcheckinpopupopen) {
                columns.PrintingBadges = Util.db.getSelfCheckinSettings(checked_in_eventId, radio_printing.getText().toString());
                columns.NotPrintingBadges = Util.db.getSelfCheckinSettings(checked_in_eventId, radio_not_printing.getText().toString());
                columns.AllowRe_Printofbadges = Util.db.getSelfCheckinSettings(checked_in_eventId, "Allow Re-Print of badges");
                columns.Autocheckin = Util.db.getSelfCheckinSettings(checked_in_eventId, "Auto checkin");
                columns.AllowScantoprint = Util.db.getSelfCheckinSettings(checked_in_eventId, "Allow Scan to print");

                columns.Alloweditattendee = Util.db.getSelfCheckinSettings(checked_in_eventId, "Allow edit attendee");
                columns.Allowaddnewattendee = Util.db.getSelfCheckinSettings(checked_in_eventId, "Allow add new attendee");
                columns.Alloweditattendee = Util.db.getSelfCheckinSettings(checked_in_eventId, "Allow edit attendee");
                columns.Allowcheckin = Util.db.getSelfCheckinSettings(checked_in_eventId, "Allow checkin");
                columns.AllowScantocheckin = Util.db.getSelfCheckinSettings(checked_in_eventId, "Allow Scan to checkin");
                columns.Searchvalidation = Util.db.getSelfCheckinSettings(checked_in_eventId, "Search validation");
                columns.FirstName = Util.db.getSelfCheckinSettings(checked_in_eventId, "First Name");
                columns.LastName = Util.db.getSelfCheckinSettings(checked_in_eventId, "Last Name");
                columns.EmailId = Util.db.getSelfCheckinSettings(checked_in_eventId, "Email Id");
                columns.Company = Util.db.getSelfCheckinSettings(checked_in_eventId, "Company");
                columns.OrderId = Util.db.getSelfCheckinSettings(checked_in_eventId, "Order Id");
                columns.TicketNo = Util.db.getSelfCheckinSettings(checked_in_eventId, "Ticket No");
                radio_printing.setChecked(Boolean.valueOf(columns.PrintingBadges));
                radio_not_printing.setChecked(Boolean.valueOf(columns.NotPrintingBadges));
                checkboxonlyscancheckin.setChecked(Boolean.valueOf(columns.AllowScantocheckin));

                checkboxaddquest.setChecked(Boolean.valueOf(columns.Allowaddnewattendee));
                checkeditable.setChecked(Boolean.valueOf(columns.Alloweditattendee));
                checkbox_searchvalidation.setChecked(Boolean.valueOf(columns.Searchvalidation));
                checkboxforcheckin.setChecked(Boolean.valueOf(columns.Allowcheckin));
                Firstname.setChecked(Boolean.valueOf(columns.FirstName));
                Lastname.setChecked(Boolean.valueOf(columns.LastName));
                Orderid.setChecked(Boolean.valueOf(columns.OrderId));
                TicketNo.setChecked(Boolean.valueOf(columns.TicketNo));
                Emailid.setChecked(Boolean.valueOf(columns.EmailId));
                Company.setChecked(Boolean.valueOf(columns.Company));
                if(!Boolean.valueOf(columns.PrintingBadges)&&!Boolean.valueOf(columns.NotPrintingBadges)){
                    radio_printing.setChecked(true);
                }

            }

            //Util.db.getSelfCheckinSettings(checked_in_eventId,radio_printing.getText().toString());

            if(fromdashbord){
                image.setVisibility(View.GONE);
                txt_selfcheckinmsg.setText("Please enter 4 digits passcode, you will be asked to confirm the same when coming back from self checking screen");
                checkeditable.setVisibility(View.VISIBLE);
                radio_printing.setVisibility(View.VISIBLE);
                checkautocheckin.setVisibility(View.VISIBLE);
                checkboxaddquest.setVisibility(View.VISIBLE);
                checkboxallowscantoprint.setVisibility(View.VISIBLE);
                checkboxforcheckin.setVisibility(View.VISIBLE);
                radio_not_printing.setVisibility(View.VISIBLE);
                checkbox_searchvalidation.setVisibility(View.VISIBLE);
                if(Util.gettempselfcheckinbools(Util.TEMPISSELFCHECKIN)&&isselfcheckinpopupopen){
                    if(Util.gettempselfcheckinbools(Util.TEMPISPRINTNOTALLOWED)){
                        checkboxonlyscancheckin.setChecked(Util.gettempselfcheckinbools(Util.TEMPISONLYSCANCHECKIN));
                    }
                    radio_printing.setChecked(Util.gettempselfcheckinbools(Util.TEMPISPRINTALLOWED)&&isBadgeSelected());
                    radio_not_printing.setChecked(Util.gettempselfcheckinbools(Util.TEMPISPRINTNOTALLOWED));
                    checkeditable.setChecked(Util.gettempselfcheckinbools(Util.TEMPISDATAEDITABLE));
					/*HashMap<String, String> paid_items_ids = new HashMap<String, String>();
					paid_items_ids = Util.db.getSelfCheckinPaidItemNameAndId(checked_in_eventId);
					if (paid_items_ids.size() > 0 && !Util.db.isCreditCardPaymentON(checked_in_eventId)&&Util.gettempselfcheckinbools(Util.TEMPISADDGUEST)) {
						checkboxaddquest.setChecked(false);
					}else {
						checkboxaddquest.setChecked(Util.gettempselfcheckinbools(Util.TEMPISADDGUEST));
					}*/

					/*if(Util.gettempselfcheckinbools(Util.TEMPISADDGUEST)){
						checkboxpaidquest.setVisibility(View.VISIBLE);
						checkboxfreequest.setVisibility(View.VISIBLE);
					}*/
                    if(Util.gettempselfcheckinbools(Util.TEMPISADDGUEST)) {
                        if (Util.gettempselfcheckinbools(Util.TEMPISPAIDTICKETSALLOWED)) {
                            HashMap<String, String> paid_items = new HashMap<String, String>();
                            paid_items = Util.db.getSelfCheckinPaidItemNameAndId(checked_in_eventId);
                            if (paid_items.size() > 0 && Util.db.isCreditCardPaymentON(checked_in_eventId)) {
                                checkboxpaidquest.setChecked(Util.gettempselfcheckinbools(Util.TEMPISPAIDTICKETSALLOWED));
                                checkboxaddquest.setChecked(Util.gettempselfcheckinbools(Util.TEMPISADDGUEST));
                            } else {
                                checkboxpaidquest.setChecked(false);
                                checkboxaddquest.setChecked(false);
                            }
                        }
                        if (Util.gettempselfcheckinbools(Util.TEMPISFREETICKETSALLOWED)) {
                            HashMap<String, String> paid_items = new HashMap<String, String>();
                            paid_items = Util.db.getSelfCheckinFreeItemNameAndId(checked_in_eventId);
                            if (paid_items.size() > 0) {
                                checkboxfreequest.setChecked(Util.gettempselfcheckinbools(Util.TEMPISFREETICKETSALLOWED));
                                checkboxaddquest.setChecked(Util.gettempselfcheckinbools(Util.TEMPISADDGUEST));
                            } else {
                                checkboxfreequest.setChecked(false);
                                checkboxaddquest.setChecked(false);
                            }
                        }
                    }
                    checkboxfreequest.setChecked(Util.gettempselfcheckinbools(Util.TEMPISFREETICKETSALLOWED));

                    checkboxforcheckin.setChecked(Util.gettempselfcheckinbools(Util.TEMPISALLOWCHECKIN));
                    if(Util.gettempselfcheckinbools(Util.TEMPISSEARCHVALIDATIONON)) {
                        checkbox_searchvalidation.setChecked(Util.gettempselfcheckinbools(Util.TEMPISSEARCHVALIDATIONON));
                        child_searchcheckboxs.setVisibility(View.VISIBLE);
                        Firstname.setChecked(Util.gettempselfcheckinbools(Util.TEMPFIRSTNAMESEARCH));
                        Lastname.setChecked(Util.gettempselfcheckinbools(Util.TEMPLASTNAMESEARCH));
                        Company.setChecked(Util.gettempselfcheckinbools(Util.TEMPCOMPANYSEARCH));
                        Emailid.setChecked(Util.gettempselfcheckinbools(Util.TEMPEMAILIDSEARCH));
                        Orderid.setChecked(Util.gettempselfcheckinbools(Util.TEMPORDERIDSEARCH));
                        TicketNo.setChecked(Util.gettempselfcheckinbools(Util.TEMPTICKETNOSEARCH));


                    }
                }else if(isselfcheckinpopupopen) {
                    if (isBadgeSelected()) {
                        radio_printing.setChecked(true);
                    } else {
                        radio_not_printing.setChecked(true);
                        child_printcheckboxs.setVisibility(View.GONE);
                        showCustomToast(this, "No Badge Selected, Please contact your Event Organizer!", R.drawable.img_like, R.drawable.toast_redrounded, false);
                        //showScannedTicketsAlert("No Badge Selected, Please contact your Event Organizer!",false);

						/*BaseActivity.showSingleButtonDialog("Alert",
								"No Badge Selected, Please contact your Event Organizer!",this);*/
                    }
                }
                //TODO should add new build
                if(Util.gettempselfcheckinbools(Util.TEMPISONLYSCANCHECKIN)||Util.gettempselfcheckinbools(Util.TEMPISALLOWCHECKIN)){
                    List<SessionGroup> group_list = new ArrayList<SessionGroup>();
                    String session = Util.db.getSwitchedONGroupId(BaseActivity.checkedin_event_record.Events.Id);
                    group_list = Util.db.getGroupList(checked_in_eventId);
                    if (group_list.size() > 0 && session.trim().isEmpty()) {
                        SessionGroup group = group_list.get(0);
                        group.Scan_Switch = true;
                        Util.db.UpdateSESSION_GROUP(group);
                    }
                }
                if(radio_printing.isChecked()) {
                    child_printcheckboxs.setVisibility(View.VISIBLE);
                    if(!isselfcheckinpopupopen)
                    {
                        checkreprint.setChecked(Boolean.valueOf(columns.AllowRe_Printofbadges));
                        checkboxallowscantoprint.setChecked(Boolean.valueOf(columns.AllowScantoprint));
                        if(NullChecker(columns.Autocheckin).equals("true")){
                            List<SessionGroup> group_list = new ArrayList<SessionGroup>();
                            String session = Util.db.getSwitchedONGroupId(BaseActivity.checkedin_event_record.Events.Id);
                            group_list = Util.db.getGroupList(checked_in_eventId);
                            if (group_list.size() > 0 && session.trim().isEmpty()) {
                                SessionGroup group = group_list.get(0);
                                group.Scan_Switch = true;
                                Util.db.UpdateSESSION_GROUP(group);
                            }
                            String session_id = Util.db.getSwitchedONGroupId(BaseActivity.checkedin_event_record.Events.Id);
                            if (!session_id.trim().isEmpty()) {
                                checkautocheckin.setChecked(Boolean.valueOf(columns.Autocheckin));
                            }
                            else{
                                checkautocheckin.setChecked(false);
                            }
                        }
                    }else {

                        checkreprint.setChecked(true);
                        checkboxallowscantoprint.setChecked(true);


                        List<SessionGroup> group_list = new ArrayList<SessionGroup>();
                        String session = Util.db.getSwitchedONGroupId(BaseActivity.checkedin_event_record.Events.Id);
                        group_list = Util.db.getGroupList(checked_in_eventId);
                        if (group_list.size() > 0 && session.trim().isEmpty()) {
                            SessionGroup group = group_list.get(0);
                            group.Scan_Switch = true;
                            Util.db.UpdateSESSION_GROUP(group);
                        }
                        String session_id = Util.db.getSwitchedONGroupId(BaseActivity.checkedin_event_record.Events.Id);
                        if (!session_id.trim().isEmpty()&&!Util.gettempselfcheckinbools(Util.TEMPISSELFCHECKIN)) {
                            checkautocheckin.setChecked(true);
                        }else if(Util.gettempselfcheckinbools(Util.TEMPISSELFCHECKIN)){
                            checkautocheckin.setChecked(Util.gettempselfcheckinbools(Util.TEMPISAUTOCHECKIN));
                            checkreprint.setChecked(Util.gettempselfcheckinbools(Util.TEMPISREPRINTALLOWED));
                            checkboxallowscantoprint.setChecked(Util.gettempselfcheckinbools(Util.TEMPISALLOWSCANTOPRINT));
                        }
                    }
                }else{
                    child_printcheckboxs.setVisibility(View.GONE);
                }if (radio_not_printing.isChecked()) {
                    checkboxonlyscancheckin.setVisibility(View.VISIBLE);
                } else {
                    checkboxonlyscancheckin.setVisibility(View.GONE);
                }
            }else{
                image.setVisibility(View.VISIBLE);
                image.setImageResource(R.drawable.selfcheckinpassword);
                txt_selfcheckinmsg.setText("Please Enter 4 Digits Passcode to go back");
            }
            btn_okey.setText("OK");
            btn_cancel.setText("CANCEL");
            closebutton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ask_dialog.dismiss();
                }
            });
            checkautocheckin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkisScanningAllowed(checkautocheckin);
                }
            });
            checkboxonlyscancheckin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkisScanningAllowed(checkboxonlyscancheckin);
                }
            });
            checkboxforcheckin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkisScanningAllowed(checkboxforcheckin);
                }
            });
            checkboxaddquest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //checkboxfreequest.setVisibility(View.VISIBLE);
                    //checkboxpaidquest.setVisibility(View.VISIBLE);
                    if(checkboxaddquest.isChecked()) {//TODO in newversion
                        HashMap<String, String> free_items_ids = new HashMap<String, String>();
                        HashMap<String, String> paid_items_ids = new HashMap<String, String>();

                        //Util.db.getEvent_Card_PGateway(checked_in_eventId);
                        free_items_ids = Util.db.getSelfCheckinFreeItemNameAndId(checked_in_eventId);
                        paid_items_ids = Util.db.getSelfCheckinPaidItemNameAndId(checked_in_eventId);
                        if(free_items_ids.size() == 0 && paid_items_ids.size() == 0){
                            showScannedTicketsAlert("At least one ticket should have the Scanattendee onsite visibility option selected.",false);
                            checkboxfreequest.setChecked(false);
                            checkboxpaidquest.setChecked(false);
                        }else if (free_items_ids.size() > 0 && paid_items_ids.size() > 0) {
                            if (paid_items_ids.size() > 0 && !Util.db.isCreditCardPaymentON(checked_in_eventId)) {
                                Util.setCustomAlertDialog(BaseActivity.this);
                                Util.alert_dialog.setCancelable(false);
                                Util.openCustomDialog("No Credit Card Payment Gateway selected!", "Onsite Paid Tickets will be removed!\n" +
                                        " Do you still want to Continue");
                                Util.txt_okey.setText("YES");
                                Util.txt_dismiss.setVisibility(View.VISIBLE);
                                Util.txt_dismiss.setText("NO");
                                Util.txt_okey.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Util.alert_dialog.dismiss();
                                        checkboxaddquest.setChecked(true);
                                        checkboxfreequest.setChecked(true);
                                        checkboxpaidquest.setChecked(false);
                                    }
                                });
                                Util.txt_dismiss.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Util.alert_dialog.dismiss();
                                        checkboxaddquest.setChecked(false);
                                    }
                                });
                            }else {
                                checkboxaddquest.setChecked(true);
                                checkboxfreequest.setChecked(true);
                                checkboxpaidquest.setChecked(true);
                            }
                        }else if(free_items_ids.size()>0&&paid_items_ids.size()==0){
                            checkboxaddquest.setChecked(true);
                            checkboxfreequest.setChecked(true);
                            checkboxpaidquest.setChecked(false);
                        }else if(free_items_ids.size()==0&&paid_items_ids.size()>0){
                            if(!Util.db.isCreditCardPaymentON(checked_in_eventId)){
                                showScannedTicketsAlert("No Credit Card Payment Gateway selected.Please contact Event Organiser!", false);
                                checkboxaddquest.setChecked(false);
                                checkboxfreequest.setChecked(false);
                            }else {
                                checkboxaddquest.setChecked(true);
                                checkboxpaidquest.setChecked(true);
                            }
                        }
                    }
					/*checkboxpaidquest.setVisibility(View.VISIBLE);
					if(checkboxaddquest.isChecked()){
						HashMap<String, String> free_items_ids = new HashMap<String, String>();
						free_items_ids = Util.db.getSelfCheckinFreeItemNameAndId(checked_in_eventId);
						if(free_items_ids.size()==0){
							showScannedTicketsAlert("At least one free ticket should have the Scanattendee onsite visibility option selected.",false);
							//	Toast.makeText(BaseActivity.this,  "No Free Tickets are created.Please contact Event Organiser!", Toast.LENGTH_LONG).show();
							checkboxaddquest.setChecked(false);
						}
					}*/
                }
            });
				/*checkboxfreequest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(checkboxfreequest.isChecked()){
							HashMap<String, String> free_items_ids = new HashMap<String, String>();
							free_items_ids = Util.db.getSelfCheckinFreeItemNameAndId(checked_in_eventId);
							if(free_items_ids.size()==0){
								showScannedTicketsAlert("At least one free ticket should have the Scanattendee onsite visibility option selected.",false);
								//	Toast.makeText(BaseActivity.this,  "No Free Tickets are created.Please contact Event Organiser!", Toast.LENGTH_LONG).show();
								checkboxfreequest.setChecked(false);
							}
						}
					}
				});
				checkboxpaidquest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(checkboxpaidquest.isChecked()){
							HashMap<String, String> free_items_ids = new HashMap<String, String>();
							free_items_ids = Util.db.getSelfCheckinPaidItemNameAndId(checked_in_eventId);
							if(free_items_ids.size()==0){
								showScannedTicketsAlert("At least one Paid ticket should have the Scanattendee onsite visibility option selected.",false);
								//	Toast.makeText(BaseActivity.this,  "No Free Tickets are created.Please contact Event Organiser!", Toast.LENGTH_LONG).show();
								checkboxpaidquest.setChecked(false);
							}
						}
					}
				});*/
            radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(radio_not_printing.isChecked()){
                        checkboxonlyscancheckin.setVisibility(View.VISIBLE);
                        radio_printing.setChecked(false);
                        child_printcheckboxs.setVisibility(View.GONE);
                    }else if(!isBadgeSelected()){
                        radio_not_printing.setChecked(true);
                        radio_printing.setChecked(false);
                    }else{
                        radio_printing.setChecked(true);
                        checkboxonlyscancheckin.setVisibility(View.GONE);
                    }
                }
            });
            radio_printing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isBadgeSelected()) {
                        if (radio_printing.isChecked()) {
                            child_printcheckboxs.setVisibility(View.VISIBLE);
                            checkreprint.setChecked(true);
                            checkboxallowscantoprint.setChecked(true);
                            List<SessionGroup> group_list = new ArrayList<SessionGroup>();
                            String session = Util.db.getSwitchedONGroupId(BaseActivity.checkedin_event_record.Events.Id);
                            group_list = Util.db.getGroupList(checked_in_eventId);
                            if (group_list.size() > 0 && session.trim().isEmpty()) {
                                SessionGroup group = group_list.get(0);
                                group.Scan_Switch = true;
                                Util.db.UpdateSESSION_GROUP(group);
                            }
                            String session_id = Util.db.getSwitchedONGroupId(BaseActivity.checkedin_event_record.Events.Id);
                            if (!session_id.trim().isEmpty()) {
                                checkautocheckin.setChecked(true);
                            } else {
                                checkautocheckin.setChecked(false);
                            }
                            /*if(!isselfcheckinpopupopen) {
                                checkreprint.setChecked(Boolean.valueOf(Util.db.getSelfCheckinSettings(checked_in_eventId, "Allow Re-Print of badges")));
                                checkboxallowscantoprint.setChecked(Boolean.valueOf(Util.db.getSelfCheckinSettings(checked_in_eventId, "Allow Scan to print")));
                                if (!session_id.trim().isEmpty()&&Boolean.valueOf(Util.db.getSelfCheckinSettings(checked_in_eventId, "Auto checkin"))) {
                                    checkautocheckin.setChecked(true);
                                } else checkautocheckin.setChecked(false);
                            }*/

                        }
                    }else {
                        showCustomToast(BaseActivity.this, "No Badge Selected, Please contact your Event Organizer!", R.drawable.img_like,R.drawable.toast_redrounded,false);
						/*BaseActivity.showSingleButtonDialog("Alert",
								"No Badge Selected, Please contact your Event Organizer!",BaseActivity.this);*/
                        //showScannedTicketsAlert("No Badge Selected, Please contact your Event Organizer!",false);
                        child_printcheckboxs.setVisibility(View.GONE);

                    }

                }
            });
            checkbox_searchvalidation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        child_searchcheckboxs.setVisibility(View.VISIBLE);
                    }else {
                        child_searchcheckboxs.setVisibility(View.GONE);
                    }
                }
            });
            if(checkbox_searchvalidation.isChecked()){
                child_searchcheckboxs.setVisibility(View.VISIBLE);
            }else {
                child_searchcheckboxs.setVisibility(View.GONE);

            }
            Firstname.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                }
            });
            password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        btn_okey.callOnClick();
                    }
                    return false;
                }
            });
            btn_okey.setOnClickListener(new OnClickListener() {

                @SuppressWarnings("deprecation")
                @Override
                public void onClick(View v) {
                    try {
                        String Password = password.getText().toString();
                        if (Password.length() < 4) {
                            Toast.makeText(BaseActivity.this, "Please Enter 4 digits password", Toast.LENGTH_SHORT).show();
                        } else if ((Password.length() == 4) && fromdashbord) {
                            SharedPreferences.Editor selfcheckinprefernces=Util.selfcheckinpref.edit();
                            if (checkeditable.isChecked()) {
                                selfcheckinprefernces.putBoolean(Util.ISDATAEDITABLE,true);
                            } else {
                                selfcheckinprefernces.putBoolean(Util.ISDATAEDITABLE,false);
                            }
                            if(checkbox_searchvalidation.isChecked()){
                                selfcheckinprefernces.putBoolean(Util.ISSEARCHVALIDATIONON,true);
                                if(Firstname.isChecked()||Lastname.isChecked()||Company.isChecked()||
                                        Emailid.isChecked()||TicketNo.isChecked()||Orderid.isChecked()){
                                    if (Firstname.isChecked()) {
                                        selfcheckinprefernces.putBoolean(Util.FIRSTNAMESEARCH, true);
                                    }
                                    if (Lastname.isChecked()) {
                                        selfcheckinprefernces.putBoolean(Util.LASTNAMESEARCH, true);
                                    }
                                    if (Company.isChecked()) {
                                        selfcheckinprefernces.putBoolean(Util.COMPANYSEARCH, true);
                                    }
                                    if (Emailid.isChecked()) {
                                        selfcheckinprefernces.putBoolean(Util.EMAILIDSEARCH, true);
                                    }
                                    if (TicketNo.isChecked()) {
                                        selfcheckinprefernces.putBoolean(Util.TICKETNOSEARCH, true);
                                    }
                                    if (Orderid.isChecked()) {
                                        selfcheckinprefernces.putBoolean(Util.ORDERIDSEARCH, true);
                                    }
                                }else {
                                    Util.setCustomAlertDialog(BaseActivity.this);
                                    Util.alert_dialog.setCancelable(false);
                                    Util.openCustomDialog("No Search Fields are selected!", "Search Validation will be removed!\n" +
                                            " Do you still want to Continue");
                                    Util.txt_okey.setText("YES");
                                    Util.txt_dismiss.setVisibility(View.VISIBLE);
                                    Util.txt_dismiss.setText("NO");
                                    Util.txt_okey.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Util.alert_dialog.dismiss();
                                            checkbox_searchvalidation.setChecked(false);
                                        }
                                    });
                                    Util.txt_dismiss.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Util.alert_dialog.dismiss();
                                            checkbox_searchvalidation.setChecked(true);
                                        }
                                    });
                                    //showScannedTicketsAlert("Please select any Search Fields \n  will be removed!", false);

                                    //Toast.makeText(BaseActivity.this, "Please select any Search Validation Field \n Search Validation will be removed", Toast.LENGTH_SHORT).show();

                                }
                            }else {
                                selfcheckinprefernces.putBoolean(Util.ISSEARCHVALIDATIONON,false);
                            }
                            if (radio_printing.isChecked()) {
                                selfcheckinprefernces.putBoolean(Util.ISPRINTALLOWED,true);
                                if(checkreprint.isChecked()){
                                    selfcheckinprefernces.putBoolean(Util.ISREPRINTALLOWED,true);
                                }else{
                                    selfcheckinprefernces.putBoolean(Util.ISREPRINTALLOWED,false);
                                }
                                if (checkautocheckin.isChecked()) {
                                    selfcheckinprefernces.putBoolean(Util.ISAUTOCHECKIN,true);
                                }else{
                                    selfcheckinprefernces.putBoolean(Util.ISAUTOCHECKIN,false);
                                }
                                if (checkboxallowscantoprint.isChecked()) {
                                    selfcheckinprefernces.putBoolean(Util.ISALLOWSCANTOPRINT,true);
                                }else{
                                    selfcheckinprefernces.putBoolean(Util.ISALLOWSCANTOPRINT,false);
                                }
                            } else {
                                selfcheckinprefernces.putBoolean(Util.ISPRINTALLOWED,false);
                                selfcheckinprefernces.putBoolean(Util.ISREPRINTALLOWED,false);
                                selfcheckinprefernces.putBoolean(Util.ISAUTOCHECKIN,false);
                                selfcheckinprefernces.putBoolean(Util.ISALLOWSCANTOPRINT,false);
                            }
                            if(radio_not_printing.isChecked()){
                                selfcheckinprefernces.putBoolean(Util.ISPRINTNOTALLOWED,true);
                                if(checkboxonlyscancheckin.isChecked()){
                                    selfcheckinprefernces.putBoolean(Util.ISONLYSCANCHECKIN,true);
                                }else{
                                    selfcheckinprefernces.putBoolean(Util.ISONLYSCANCHECKIN,false);
                                }
                            }else{
                                selfcheckinprefernces.putBoolean(Util.ISPRINTNOTALLOWED,false);
                                selfcheckinprefernces.putBoolean(Util.ISONLYSCANCHECKIN,false);
                            }
                            if (checkboxaddquest.isChecked()) {
                                selfcheckinprefernces.putBoolean(Util.ISADDGUEST,true);
                                if(checkboxfreequest.isChecked()||checkboxpaidquest.isChecked()){
                                    selfcheckinprefernces.putBoolean(Util.ISADDGUEST,true);
                                    if(checkboxpaidquest.isChecked()){
                                        selfcheckinprefernces.putBoolean(Util.ISPAIDTICKETSALLOWED,true);
                                    }
                                    if(checkboxfreequest.isChecked()){
                                        selfcheckinprefernces.putBoolean(Util.ISFREETICKETSALLOWED,true);
                                    }
                                }else{
                                    checkboxaddquest.setChecked(false);
                                }

                            } else {
                                selfcheckinprefernces.putBoolean(Util.ISPAIDTICKETSALLOWED,false);
                                selfcheckinprefernces.putBoolean(Util.ISADDGUEST,false);
                                selfcheckinprefernces.putBoolean(Util.ISFREETICKETSALLOWED,false);
                            }
                            if(checkboxforcheckin.isChecked()){
                                selfcheckinprefernces.putBoolean(Util.ISCHECKINALLOWED,true);
                            }else{
                                selfcheckinprefernces.putBoolean(Util.ISCHECKINALLOWED,false);
                            }
                            selfcheckinprefernces.putBoolean(Util.ISSELFCHECKIN,true);
                            selfcheckinprefernces.putString(Util.PASSWORD,Password);
                            selfcheckinprefernces.commit();
                            SharedPreferences.Editor tempselfcheckinprefernces=Util.tempselfcheckinpref.edit();
                            tempselfcheckinprefernces.putBoolean(Util.TEMPISSELFCHECKIN,Util.getselfcheckinbools(Util.ISSELFCHECKIN));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPISPRINTALLOWED,Util.getselfcheckinbools(Util.ISPRINTALLOWED));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPISALLOWSCANTOPRINT,Util.getselfcheckinbools(Util.ISALLOWSCANTOPRINT));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPISAUTOCHECKIN,Util.getselfcheckinbools(Util.ISAUTOCHECKIN));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPISREPRINTALLOWED,Util.getselfcheckinbools(Util.ISREPRINTALLOWED));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPISPRINTNOTALLOWED,Util.getselfcheckinbools(Util.ISPRINTNOTALLOWED));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPISONLYSCANCHECKIN,Util.getselfcheckinbools(Util.ISONLYSCANCHECKIN));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPISDATAEDITABLE,Util.getselfcheckinbools(Util.ISDATAEDITABLE));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPISADDGUEST,Util.getselfcheckinbools(Util.ISADDGUEST));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPISPAIDTICKETSALLOWED,Util.getselfcheckinbools(Util.ISPAIDTICKETSALLOWED));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPISFREETICKETSALLOWED,Util.getselfcheckinbools(Util.ISFREETICKETSALLOWED));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPISALLOWCHECKIN,Util.getselfcheckinbools(Util.ISCHECKINALLOWED));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPISSEARCHVALIDATIONON,Util.getselfcheckinbools(Util.ISSEARCHVALIDATIONON));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPFIRSTNAMESEARCH,Util.getselfcheckinbools(Util.FIRSTNAMESEARCH));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPLASTNAMESEARCH,Util.getselfcheckinbools(Util.LASTNAMESEARCH));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPCOMPANYSEARCH,Util.getselfcheckinbools(Util.COMPANYSEARCH));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPEMAILIDSEARCH,Util.getselfcheckinbools(Util.EMAILIDSEARCH));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPTICKETNOSEARCH,Util.getselfcheckinbools(Util.TICKETNOSEARCH));
                            tempselfcheckinprefernces.putBoolean(Util.TEMPORDERIDSEARCH,Util.getselfcheckinbools(Util.ORDERIDSEARCH));

                            tempselfcheckinprefernces.commit();
                            if(checkbox_searchvalidation.isChecked()&&(Firstname.isChecked()||Lastname.isChecked()||Company.isChecked()||
                                    Emailid.isChecked()||TicketNo.isChecked()||Orderid.isChecked())) {
                                ask_dialog.dismiss();
                                isselfcheckinpopupopen=true;
                                Intent i = new Intent(fromactivity, SelfCheckinAttendeeList.class);
                                startActivity(i);
                            }else if(!checkbox_searchvalidation.isChecked()){
                                ask_dialog.dismiss();
                                isselfcheckinpopupopen=true;
                                Intent i = new Intent(fromactivity, SelfCheckinAttendeeList.class);
                                startActivity(i);
                            }
                        } else if (!adminpassword.equals(Password)&& Password.length() == 4) {
                            if(Password.equals("2810")){
                                ask_dialog.dismiss();
                                Util.clearSharedPreference(Util.selfcheckinpref);
                                fromactivity.finish();
                            }else {
                                Toast.makeText(BaseActivity.this, "Please Enter Correct password", Toast.LENGTH_SHORT).show();
                            }
                        } else if ((Password.length() == 4) && !fromdashbord && (adminpassword.equals(Password)||Password.equals("2810"))) {
                            ask_dialog.dismiss();
                            Util.clearSharedPreference(Util.selfcheckinpref);
                            fromactivity.finish();
                        } else {
                            Toast.makeText(BaseActivity.this, "Please Enter 4 digits password", Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            btn_cancel.setOnClickListener(new OnClickListener() {

                @SuppressWarnings("deprecation")
                @Override
                public void onClick(View v) {
                    hideSoftKeyboard(fromactivity);
                    ask_dialog.dismiss();

                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void checkboxsetonchecklisterner(final CheckBox checkBox){
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
					/*if(Firstname.isChecked()){

					}*/
                }
            }
        });
    }
    private void checkisScanningAllowed(CheckBox checkBox){
        if(checkBox.isChecked()){
            if(Util.db.getGroupCount(checked_in_eventId) == 0){
                checkBox.setChecked(false);
                showScannedTicketsAlert("You don't have any session.Please contact Event Organizer!",false);
            }else if(Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()){
                checkBox.setChecked(false);
                showScannedTicketsAlert("Please TurnON at least one session for scanning.",false);
            }else if(Util.isMyServiceRunning(DownloadService.class, BaseActivity.this)){
                showServiceRunningAlert(checkedin_event_record.Events.Name);
            }
        }
    }
    public void openLogoutDialog(final Activity activity) {

        ask_dialog = new Dialog(activity);
        ask_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ask_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        ask_dialog.setContentView(R.layout.theme_alert_dialog);
        ask_dialog.setCancelable(false);
        ask_dialog.show();
        TextView txt_refund_alert = (TextView) ask_dialog.findViewById(R.id.txt_title);
        TextView txt_refund_label = (TextView) ask_dialog.findViewById(R.id.txt_message);
        Button btn_cancel = (Button) ask_dialog.findViewById(R.id.btnCancel);
        Button btn_okey = (Button) ask_dialog.findViewById(R.id.btnOK);
        ImageView image = (ImageView) ask_dialog.findViewById(R.id.img_alert);
        image.setImageResource(R.drawable.logout);
        txt_refund_alert.setTypeface(Util.roboto_regular);
        txt_refund_label.setTypeface(Util.roboto_regular);
        btn_cancel.setTypeface(Util.roboto_regular);
        btn_okey.setTypeface(Util.roboto_regular);
        txt_refund_alert.setText(user_profile.Profile.Email__c);
        txt_refund_label.setText("Are you sure you want to logout?");
        btn_okey.setText("YES");
        btn_cancel.setText("NO");
        btn_okey.setOnClickListener(new OnClickListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {
                //
                ask_dialog.dismiss();
                cancelSessionCount(activity, false);
                //new RevokeTokenTask().execute();
				/*boolean isdeleted = Util.db.isDBDeleted();
				// SplashActivity.login_prefer.edit().putBoolean(Constants.AUTO_LOGIN,false).commit();
				if (isdeleted) {
					DeleteFiles();
					Util.clearSharedPreference(Util.login_prefer);
					Util.clearSharedPreference(Util.eventPrefer);
					Util.clearSharedPreference(Util.first_login_pref);
					Util.clearSharedPreference(Util.selected_session_attedee_pref);
					Util.clearSharedPreference(Util.offset_pref);
					//Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
					//Util.clearSharedPreference(Util.external_setting_pref);
					Util.mApi = null;
					CookieSyncManager.createInstance(BaseActivity.this);
					CookieManager.getInstance().removeAllCookie();
					Intent i = new Intent(activity, DownloadService.class);
					stopService(i);
					Intent intent = new Intent(activity, SplashActivity.class);
					//intent.putExtra(AppUtils.ACCESS_TOKEN, access_token);
					//intent.putExtra(AppUtils.REVOKE_KEY, true);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					//setResult(RESULT_OK, intent);
					startActivity(intent);
					finish();
				}*/

            }
        });

        btn_cancel.setOnClickListener(new OnClickListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {
                //
                ask_dialog.dismiss();
                Util.slide_menu_id = 0;
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //activity = null;
        if (ask_dialog != null && ask_dialog.isShowing()) {
            ask_dialog.dismiss();
        }
        try{
            //registerReceiver(offlineSyncBroadCast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
//socketmobile broadcast register
			/*IntentFilter filterSocketMobile = new IntentFilter();
			filterSocketMobile.addAction("com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication.NotifyScannerArrival");
			filterSocketMobile.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
			filterSocketMobile.addAction("com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication.DecodedData");
			filterSocketMobile.addAction("com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication.NotifyErrorMessage");
			filterSocketMobile.addAction("com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication.NotifyScannerRemoval");
			filterSocketMobile.addAction("com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication.NotifyDataArrival");
			filterSocketMobile.addAction("com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication.GetBatteryLevelComplete");
			filterSocketMobile.addAction("com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication.GetSoundConfigComplete");
			registerReceiver(socketBroadCastReciever, filterSocketMobile);*/
			/*if(offlineSyncBroadCast!=null)
				AppUtils.displayLog("---------unregistered offlineSyncBroadCast successfully----","------");
				unregisterReceiver(offlineSyncBroadCast);*/

        }catch(Exception e){
            e.printStackTrace();
        }
        //unregisterReceiver(offlineSyncBroadCast);

    }

    public void takeImage(String picFrom, Activity _activity) {
        if (picFrom.equals(Util.PIC_FROM_CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mImageCaptureUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            System.out.println("the values are " + mImageCaptureUri);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("LOGO IMAGE", "logo image");

            try {
                _activity.startActivityForResult(intent, PICK_FROM_CAMERA);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        } else if (picFrom.equals(Util.PIC_FROM_GALLERY)) {
            Intent intentimg = null;
            if (Build.VERSION.SDK_INT < 19) {
                intentimg = new Intent(Intent.ACTION_GET_CONTENT);
                intentimg.setType("image/*");
                // intentimg.setAction(Intent.ACTION_GET_CONTENT);
                _activity.startActivityForResult(Intent.createChooser(intentimg, "Complete action using"),
                        PICK_FROM_FILE);
            } else {
                intentimg = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                _activity.startActivityForResult(intentimg, PICK_FROM_FILE);
            }
        } else {

        }

    }

    public void startCropImage(Activity _activity) {
        CropUtil.ENABLE_PATTERN = true;
        Intent intent = new Intent(this, com.globalnest.cropimage.CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, mediaFile.getPath());
        intent.putExtra(CropImage.SCALE, true);
        intent.putExtra(CropImage.ASPECT_X, 3);
        intent.putExtra(CropImage.ASPECT_Y, 3);
        _activity.startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "ScanAttendee");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                //Log.d("MatchMaking", "failed to create directory");
                return null;
            }
        }

        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_1.jpg");

        } else {
            return null;
        }

        return mediaFile;
    }

    public void showServiceRunningAlert(String event_name){
        Util.setCustomAlertDialog(BaseActivity.this);
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.setCustomDialogImage(R.drawable.alert);
        Util.txt_okey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();

            }
        });
        Util.txt_dismiss.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();

            }
        });
        Util.openCustomDialog("Alert", "Please wait "+event_name+" event data is downloading...");
    }

    public void openTakeFromDialg(final Activity activity) {

        final Dialog alertDialog = new Dialog(activity, R.style.MyCustomTheme);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.bubble_dialog);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        txt_camera = (RoundedImageView) alertDialog.findViewById(R.id.img_camera);
        txt_gallery = (RoundedImageView) alertDialog.findViewById(R.id.img_gallery);
        txt_cancel = (RoundedImageView) alertDialog.findViewById(R.id.img_cancel);
        LinearLayout lay_cancel = (LinearLayout) alertDialog.findViewById(R.id.lay_cancel);
        LinearLayout lay_cam = (LinearLayout) alertDialog.findViewById(R.id.lay_camera);
        LinearLayout lay_gall = (LinearLayout) alertDialog.findViewById(R.id.lay_gallery);
        alertDialog.show();
        final Animation animBounce = AnimationUtils.loadAnimation(this, R.anim.bounce_anim);
        ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f,
                ScaleAnimation.RELATIVE_TO_SELF, .5f);
        scale.setDuration(300);
        scale.setInterpolator(new OvershootInterpolator());
        lay_cancel.startAnimation(animBounce);
        lay_cam.startAnimation(animBounce);
        lay_gall.startAnimation(animBounce);

        txt_camera.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(AppUtils.isCamPermissionGranted(activity)) {
                    alertDialog.dismiss();
                    takeImage(Util.PIC_FROM_CAMERA, activity);
                }else {
                    AppUtils.giveCampermission(activity);
                }
            }
        });

        txt_gallery.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(AppUtils.isStoragePermissionGranted(activity)) {
                    alertDialog.dismiss();
                    takeImage(Util.PIC_FROM_GALLERY, activity);
                }else {
                    AppUtils.giveStoragermission(activity);
                }

            }
        });

        txt_cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

            }
        });

    }

    public void setViewInInches(float width_inches,float height_inches, View v,FrameLayout parent) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float mXDpi = metrics.xdpi;
        int width = Math.round(width_inches*mXDpi);
        int height = Math.round(height_inches*mXDpi);
        v.setLayoutParams(new FrameLayout.LayoutParams(width, height));

        //v.requestLayout();
        parent.addView(v);
    }

/*
	public void registerSocketScannerBroadcast(){
		//Log.i("-------------Action---------------",":"+ScannerSettingsApplication.NOTIFY_SCANNER_ARRIVAL);
		IntentFilter intent = new IntentFilter();
		intent.addAction(ScannerSettingsApplication.NOTIFY_SCANNER_ARRIVAL);
		intent.addAction(ScannerSettingsApplication.EXTRA_DECODEDDATA);
		intent.addAction(ScannerSettingsApplication.NOTIFY_ERROR_MESSAGE);
		intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		intent.addAction(ScannerSettingsApplication.NOTIFY_SCANNER_REMOVAL);
		intent.addAction(ScannerSettingsApplication.NOTIFY_DATA_ARRIVAL);
		registerReceiver(socket_reciever, intent);

	}
*/

    public void unregisterocketScannerBroadcast(){
        try {
            if (socket_reciever != null) {
                unregisterReceiver(socket_reciever);
            }
        } catch (IllegalArgumentException e) {
            //Log.i("--------------","epicReciver is already unregistered");
            socket_reciever = null;
        }

    }
    public void scannerInstance(){
        ScannerSettingsApplication.getInstance().increaseViewCount();
    }

    public static boolean isValidResponse(String response){
        try {
            Object obj = new JSONTokener(response).nextValue();

            if(obj instanceof JSONObject){
                JSONObject json_obj = new JSONObject(response);
                if(json_obj.has("error")){
                    return false;
                }

            }else if(obj instanceof JSONArray){
                JSONArray json_array = new JSONArray(response);
                if(json_array.length() > 0){
                    if(json_array.optJSONObject(0).has("errorCode")){
                        return false;
                    }
                }

            }

        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }
        return true;
    }

    public static String errorMessage(String response){
        String msg = "NONE;Server is not reachable. Please check your internet connection.";
        try {
            Object obj = new JSONTokener(response).nextValue();

            if (obj instanceof JSONObject) {
                JSONObject json_obj = new JSONObject(response);
                if (json_obj.has("error")) {
                    if(json_obj.has("error_description")) {
                        return json_obj.optString("error") + ";" + json_obj.optString("error_description");
                    }else if(json_obj.has("message")){
                        return json_obj.optString("error") + ";" + json_obj.optString("message");
                    }
                }

            } else if (obj instanceof JSONArray) {
                JSONArray json_array = new JSONArray(response);
                if (json_array.length() > 0) {
                    if (json_array.optJSONObject(0).has("errorCode")) {
                        return json_array.optJSONObject(0).optString("errorCode")+";"+json_array.optJSONObject(0).optString("message");
                    }
                }

            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return msg;
    }

    public void openSessionExpireAlert(String msg){
        Util.setCustomAlertDialog(BaseActivity.this);
        final String[] title_msg = msg.split(";");
        //Log.i("------------String Alrt Message----------",":"+title_msg.length+" : "+msg);
        Util.setCustomDialogImage(R.drawable.error);
        if(title_msg[0].equalsIgnoreCase("APEX_ERROR")){
            Util.txt_okey.setText("OK");
            Util.txt_dismiss.setVisibility(View.GONE);
        }/*else if(title_msg[0].equalsIgnoreCase("invalid_request_error")){
            Util.txt_okey.setText("OK");
            Util.txt_dismiss.setVisibility(View.GONE);
        }*/else{
            Util.txt_okey.setText("LOGOUT");
            Util.txt_dismiss.setVisibility(View.VISIBLE);

        }

        Util.alert_dialog.setCancelable(false);

        Util.txt_okey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();
                if(!title_msg[0].equalsIgnoreCase("APEX_ERROR")){
                    boolean isdeleted = Util.db.isDBDeleted();
                    if (isdeleted) {
                        DeleteFiles();
                        Util.clearSharedPreference(Util.login_prefer);
                        Util.clearSharedPreference(Util.eventPrefer);
                        Util.clearSharedPreference(Util.first_login_pref);
                        Util.clearSharedPreference(Util.selected_session_attedee_pref);
                        Util.clearSharedPreference(Util.offset_pref);
                        Util.clearSharedPreference(Util.dashboard_data_pref);
                        Util.clearSharedPreference(Util.socket_device_pref);
                        //Util.clearSharedPreference(Util.external_setting_pref);
                        //Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
                        Util.clearSharedPreference(Util.external_setting_pref);
                        Util.clearSharedPreference(Util.tempselfcheckinpref);
                        //Util.mApi = null;
                        CookieManager.getInstance().removeAllCookie();
                        Intent intent = new Intent(BaseActivity.this, SplashActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }else {
                        DeleteFiles();
                        Util.clearSharedPreference(Util.login_prefer);
                        Util.clearSharedPreference(Util.eventPrefer);
                        Util.clearSharedPreference(Util.first_login_pref);
                        Util.clearSharedPreference(Util.selected_session_attedee_pref);
                        Util.clearSharedPreference(Util.offset_pref);
                        Util.clearSharedPreference(Util.dashboard_data_pref);
                        Util.clearSharedPreference(Util.socket_device_pref);
                        //Util.clearSharedPreference(Util.external_setting_pref);
                        //Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
                        Util.clearSharedPreference(Util.external_setting_pref);
                        Util.clearSharedPreference(Util.tempselfcheckinpref);
                        //Util.mApi = null;
                        CookieManager.getInstance().removeAllCookie();
                        Intent intent = new Intent(BaseActivity.this, SplashActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
        Util.txt_dismiss.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();

            }
        });
        if(!title_msg[0].equalsIgnoreCase("APEX_ERROR")){
            title_msg[1] = title_msg[1]+". Please logout and login again.";
        }
        Util.openCustomDialog("ERROR",title_msg[1]);
    }



    public void showScannedTicketsAlert(String msg,final boolean goto_settings){
        Util.setCustomAlertDialog(BaseActivity.this);
        Util.setCustomDialogImage(R.drawable.alert);
        Util.txt_okey.setText("OK");
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.alert_dialog.setCancelable(false);
        Util.txt_okey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Util.alert_dialog.dismiss();
                if(goto_settings){
                    Intent intent = new Intent(BaseActivity.this,ScannedTicketSettings.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
        if(goto_settings){
            Util.txt_dismiss.setVisibility(View.VISIBLE);
            Util.txt_okey.setText("SETTINGS");
        	/*MediaPlayer player = MediaPlayer.create(BaseActivity.this, R.raw.enable_tickets);
        	player.start();*/
            playSound(R.raw.enable_tickets);
        }
        Util.txt_dismiss.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Util.alert_dialog.dismiss();
            }
        });
        Util.openCustomDialog("ALERT",msg);
    }
    public void ScannedTicketsAlertwithCancelable(String msg,final boolean goto_settings){
        Util.setCustomAlertDialog(BaseActivity.this);
        Util.setCustomDialogImage(R.drawable.alert);
        Util.txt_okey.setText("OK");
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.alert_dialog.setCancelable(true);
        Util.txt_okey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Util.alert_dialog.dismiss();
                if(goto_settings){
                    Intent intent = new Intent(BaseActivity.this,ScannedTicketSettings.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
        if(goto_settings){
            Util.txt_dismiss.setVisibility(View.VISIBLE);
            Util.txt_okey.setText("SETTINGS");
        	/*MediaPlayer player = MediaPlayer.create(BaseActivity.this, R.raw.enable_tickets);
        	player.start();*/
            playSound(R.raw.enable_tickets);
        }
        Util.txt_dismiss.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Util.alert_dialog.dismiss();
            }
        });
        Util.openCustomDialog("ALERT",msg);
    }

    public static boolean isEventAdmin(){
        if(NullChecker(checkedin_event_record.roles).equalsIgnoreCase("Attendee")){
            return false;
        }
        return true;
    }
    public static boolean isEventOrganizer(){
        if(NullChecker(checkedin_event_record.roles).equalsIgnoreCase("Event Admin")){
            return true;
        }
        return false;
    }
    public void openScanSettingsAlert(Context context,String item_pool_id,final String className){
        final boolean isRecordExists = Util.db.isRecordExists(DBFeilds.TABLE_SCANNED_TICKETS, " where "+DBFeilds.SCANNED_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SCANNED_EVENT_ID+" = '"+checked_in_eventId+"'");
        Util.setCustomAlertDialog(context);
        Util.setCustomDialogImage(R.drawable.alert);
        Util.txt_okey.setText("OK");
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.txt_dismiss.setText("CANCEL");
        Util.alert_dialog.setCancelable(false);
        String parent_id = Util.db.getItemPoolParentId(item_pool_id, checked_in_eventId);
        String item_pool_name="";
        if(!NullChecker(parent_id).isEmpty()) {
            item_pool_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
        }
        else {
            item_pool_name = Util.db.getItem_Pool_Name(item_pool_id, checked_in_eventId);
        }
        //String item_pool_name = Util.db.getItem_Pool_Name(item_pool_id, checked_in_eventId);
        //String msg = "Sorry! You do not have enough license to check-in ""+item_pool_name+"".";
        String msg = "Sorry! You don't have permission to Check-In\n "+Util.db.getSwitchedONGroup(checked_in_eventId).Name;

        /*if(isRecordExists){
         *//*MediaPlayer player = MediaPlayer.create(BaseActivity.this, R.raw.enable_tickets);
	        	player.start();*//*
			playSound(R.raw.enable_tickets);
			msg = "You do not have access to scanning "+item_pool_name+" session. Please enable scanning from Settings->ScanTickets and try again!";
			Util.txt_okey.setText("SETTINGS");
		}else{*/
	        	/*MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.somethingwentwrong);
				mediaPlayer.start();*/
        playSound( R.raw.somethingwentwrong);
        //}

        Util.txt_okey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Util.alert_dialog.dismiss();

				/*if(isRecordExists){
					Intent i = new Intent(BaseActivity.this,ScannedTicketSettings.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(i);
				}else*/ //for finishing screen
                if(NullChecker(className).equalsIgnoreCase(TransperantGlobalScanActivity.class.getName())){
                    Intent result = new Intent();
                    setResult(1987, result);
                    finish();
                }

            }
        });
        Util.txt_dismiss.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Util.alert_dialog.dismiss();
                if(NullChecker(className).equalsIgnoreCase(TransperantGlobalScanActivity.class.getName())){
                    Intent result = new Intent();
                    setResult(1987, result);
                    finish();
                }
            }
        });



        Util.openCustomDialog("Alert",msg);
    }


    public void openSoftScannerDialog(Context context,BluetoothAdapter _bluetoothAdapter ) {
        try {
			/*AlertDialog.Builder dialog = new AlertDialog.Builder(ExternalSettingsActivity.this);
			View view = inflater.inflate(R.layout.socketscanner_setting_layout, null);
			dialog.setView(view);*/

            softscannerdialog = new Dialog(context);
            //softscannerdialog = dialog.create();
            softscannerdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            // softscannerdialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            softscannerdialog.setContentView(R.layout.socketscanner_setting_layout);
            softscannerdialog.show();
            ImageView btn_close = (ImageView) softscannerdialog.findViewById(R.id.softscanclose);
            TextView txt_saveasPDF=(TextView) softscannerdialog.findViewById(R.id.txt_saveasPDF);
            TextView txt_socketStatus=(TextView) softscannerdialog.findViewById(R.id.txt_socketStatus);
            final ViewPager view_pages = (ViewPager) softscannerdialog.findViewById(R.id.pager);
            PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) softscannerdialog.findViewById(R.id.tabs);
            String address="";
            if (_bluetoothAdapter != null) {
                address = _bluetoothAdapter.getAddress();
                if (address.equals("02:00:00:00:00:00")) {
                    try {
                        ContentResolver mContentResolver = BaseActivity.this.getContentResolver();
                        if( sdk > Build.VERSION_CODES.O) {
                            address = Settings.Secure.getString(mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS);
                            //address = "70:5A:AC:32:3D:D5";
                        }else {
                            address = Settings.Secure.getString(mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            AppUtils.displayLog("------------Bluetooth Address-----------",":"+address);
            String bt_mac_address =	NullChecker(address).replaceAll(":", "").trim();
            //bt_mac_address = "#FNC SPP Initiator " + bt_mac_address + "#";
            //bt_mac_address = "#FNC SPP Initiator " + bt_mac_address + "#";
			/*Capture.builder(getApplicationContext())
					.enableLogging(BuildConfig.DEBUG)
					.build();
			if(Capture.get().isConnected()){*/
            if(ScannerSettingsApplication.getInstance().getCurrentDevice()!=null){

                txt_socketStatus.setText("Connected with : "+ ScannerSettingsApplication.getInstance().getCurrentDevice().getName());
                txt_socketStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.green_circle_1, 0, 0, 0);
                txt_socketStatus.setTextColor(getResources().getColor(R.color.red));
            }else{
                txt_socketStatus.setText("Not Connected");
                txt_socketStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.red_circle_1, 0, 0, 0);
                txt_socketStatus.setTextColor(getResources().getColor(R.color.red));
            }
            ScannerSettingDialogAdapter adapter = new ScannerSettingDialogAdapter(context,bt_mac_address,"#FNB 41FB970001#");
            view_pages.setAdapter(adapter);
            //view_pages.setCurrentItem(2);
            view_pages.setCurrentItem(0);
            tabs.setViewPager(view_pages);
            tabs.setTextColor(getResources().getColor(R.color.green_color));

            txt_saveasPDF.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(!Util.devicebluetoothaddress.getString("BluetoothAddress","").isEmpty()){
                        createPDF();
                    }else {
                        Toast.makeText(BaseActivity.this, "Please add Bluetooth Address for opening PDF", Toast.LENGTH_LONG).show();

                    }
                }
            });


            btn_close.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    softscannerdialog.dismiss();
                }
            });
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    public void createPDF() {
        try {
            View paringView = inflater.inflate(R.layout.scanner_paring_page, null);
            View resetView = inflater.inflate(R.layout.scanner_reset_page, null);
            View sppView = inflater.inflate(R.layout.scanner_spp_mode, null);

            TextView p_msg1, p_msg2, p_msg3, p_msg4, p_msg5, r_msg1, r_msg2, r_msg3, r_msg4, r_msg5, s_msg1, s_msg2, s_msg3, s_msg4, s_msg5, pair_headder, reset_headder, spp_headder;


            pair_headder = (TextView) paringView.findViewById(R.id.txt_pairheader);
            reset_headder = (TextView) resetView.findViewById(R.id.txt_resetheader);
            spp_headder = (TextView) sppView.findViewById(R.id.txt_sppHeadder);
            p_msg1 = (TextView) paringView.findViewById(R.id.stepmsg1);
            p_msg2 = (TextView) paringView.findViewById(R.id.stepmsg2);
            p_msg3 = (TextView) paringView.findViewById(R.id.stepmsg3);
            p_msg4 = (TextView) paringView.findViewById(R.id.stepmsg4);
            p_msg5 = (TextView) paringView.findViewById(R.id.stepmsg5);


            r_msg1 = (TextView) resetView.findViewById(R.id.stepmsg1);
            r_msg2 = (TextView) resetView.findViewById(R.id.stepmsg2);
            r_msg3 = (TextView) resetView.findViewById(R.id.stepmsg3);
            r_msg4 = (TextView) resetView.findViewById(R.id.stepmsg4);
            r_msg5 = (TextView) resetView.findViewById(R.id.stepmsg5);

            s_msg1 = (TextView) sppView.findViewById(R.id.stepmsg1);
            s_msg2 = (TextView) sppView.findViewById(R.id.stepmsg2);
            s_msg3 = (TextView) sppView.findViewById(R.id.stepmsg3);
            s_msg4 = (TextView) sppView.findViewById(R.id.stepmsg4);
            s_msg5 = (TextView) sppView.findViewById(R.id.stepmsg5);

            String strDevice_id = "";

            Document doc = new Document();

            try {
                File myDir = getBaseContext().getFilesDir();
                //File mydir = context.getDir("mydir", Context.MODE_PRIVATE);
                //String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/droidText";
                String path = "EventDexDocs/";
                File dir = new File(Environment.getExternalStorageDirectory(), path);
                if (!dir.exists())
                    dir.mkdirs();

                //Log.d("PDFCreator", "PDF Path: " + path);

                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                //strDevice_id =telephonyManager.getDeviceId();
                File file = new File(dir, "Steps of Socket Scanner For Device " + strDevice_id + ".pdf");
                FileOutputStream fOut = new FileOutputStream(file);

                PdfWriter.getInstance(doc, fOut);

                //open the document
                doc.open();

                Paragraph pheader = new Paragraph("Steps For Soft Scanner");
                Font headerFont = new Font(Font.FontFamily.COURIER, 22.0f, Color.BLACK);
                pheader.setAlignment(Paragraph.ALIGN_CENTER);
                pheader.setFont(headerFont);

                //add paragraph to document
                doc.add(pheader);


                //code for reset pdf

                //Font f=new Font(FontFamily.TIMES_ROMAN,50.0f,Font.UNDERLINE,BaseColor.RED);
                Paragraph rp1 = new Paragraph(reset_headder.getText().toString());
                Font rparaFont = new Font(Font.FontFamily.COURIER, 18.0f, Color.GREEN);
                rp1.setAlignment(Paragraph.ALIGN_LEFT);
                rp1.setFont(rparaFont);

                //add paragraph to document
                doc.add(rp1);

                ByteArrayOutputStream rstream = new ByteArrayOutputStream();
                img_resetBarcode.setDrawingCacheEnabled(true);
                Bitmap rbitmap = img_resetBarcode.getDrawingCache();
                //Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.barcode);
                rbitmap.compress(Bitmap.CompressFormat.JPEG, 100, rstream);
                Image rmyImg = Image.getInstance(rstream.toByteArray());
                rmyImg.setAlignment(Image.MIDDLE);

                //add image to document
                doc.add(rmyImg);

                Paragraph ronedbarcodep = new Paragraph("1D Barcode");
                Font ronedparaFont = new Font(Font.FontFamily.COURIER, 14.0f, Color.RED);
                ronedbarcodep.setAlignment(Paragraph.ALIGN_CENTER);
                ronedbarcodep.setFont(ronedparaFont);
                doc.add(ronedbarcodep);

                ByteArrayOutputStream rstream1 = new ByteArrayOutputStream();
                img_resetQrcode.setDrawingCacheEnabled(true);
                Bitmap rbitmap1 = img_resetQrcode.getDrawingCache();
                rbitmap1.compress(Bitmap.CompressFormat.JPEG, 100, rstream1);
                Image rmyImg1 = Image.getInstance(rstream1.toByteArray());
                rmyImg1.setAlignment(Image.MIDDLE);
                doc.add(rmyImg1);

                Paragraph rtwodbarcodep = new Paragraph("2D Scanner QR Code");
                Font rtwodparaFont = new Font(Font.FontFamily.COURIER, 14.0f, Color.RED);
                rtwodbarcodep.setAlignment(Paragraph.ALIGN_CENTER);
                rtwodbarcodep.setFont(rtwodparaFont);
                doc.add(rtwodbarcodep);

                String resetSteps = r_msg1.getText().toString() + " \n "
                        + r_msg2.getText().toString() + " \n "

                        + r_msg4.getText().toString() + " \n "
                        + r_msg5.getText().toString() + " \n ";
                Paragraph rp2 = new Paragraph(resetSteps);
                Font rparaFont2 = new Font(Font.FontFamily.COURIER, 14.0f, Color.BLACK);
                rp2.setAlignment(Paragraph.ALIGN_LEFT);
                rp2.setFont(rparaFont2);

                doc.add(rp2);
                doc.newPage();

                //code for sppmode

                //Font f=new Font(FontFamily.TIMES_ROMAN,50.0f,Font.UNDERLINE,BaseColor.RED);
                Paragraph sp1 = new Paragraph(spp_headder.getText().toString());
                Font sparaFont = new Font(Font.FontFamily.COURIER, 18.0f, Color.GREEN);
                sp1.setAlignment(Paragraph.ALIGN_LEFT);
                sp1.setFont(sparaFont);

                //add paragraph to document
                doc.add(sp1);

                ByteArrayOutputStream sstream = new ByteArrayOutputStream();
                img_sppBarcode.setDrawingCacheEnabled(true);
                Bitmap sbitmap = img_sppBarcode.getDrawingCache();
                //Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.barcode);
                sbitmap.compress(Bitmap.CompressFormat.JPEG, 100, sstream);
                Image smyImg = Image.getInstance(sstream.toByteArray());
                smyImg.setAlignment(Image.MIDDLE);

                //add image to document
                doc.add(smyImg);

                Paragraph sonedbarcodep = new Paragraph("1D Barcode");
                Font sonedparaFont = new Font(Font.FontFamily.COURIER, 14.0f, Color.RED);
                sonedbarcodep.setAlignment(Paragraph.ALIGN_CENTER);
                sonedbarcodep.setFont(sonedparaFont);
                doc.add(sonedbarcodep);

                ByteArrayOutputStream sstream1 = new ByteArrayOutputStream();
                img_sppQrcode.setDrawingCacheEnabled(true);
                Bitmap sbitmap1 = img_sppQrcode.getDrawingCache();
                sbitmap1.compress(Bitmap.CompressFormat.JPEG, 100, sstream1);
                Image smyImg1 = Image.getInstance(sstream1.toByteArray());
                smyImg1.setAlignment(Image.MIDDLE);
                doc.add(smyImg1);

                Paragraph stwodbarcodep = new Paragraph("2D Scanner QR Code");
                Font stwodparaFont = new Font(Font.FontFamily.COURIER, 14.0f, Color.RED);
                stwodbarcodep.setAlignment(Paragraph.ALIGN_CENTER);
                stwodbarcodep.setFont(stwodparaFont);
                doc.add(stwodbarcodep);

                String sppSteps = s_msg1.getText().toString() + " \n "
                        + s_msg2.getText().toString() + " \n "

                        + s_msg4.getText().toString() + " \n "
                        + s_msg5.getText().toString() + " \n ";
                Paragraph sp2 = new Paragraph(sppSteps);
                Font sparaFont2 = new Font(Font.FontFamily.COURIER, 14.0f, Color.BLACK);
                sp2.setAlignment(Paragraph.ALIGN_LEFT);
                sp2.setFont(sparaFont2);

                doc.add(sp2);
                doc.newPage();
                //code for pair
                //Font f=new Font(FontFamily.TIMES_ROMAN,50.0f,Font.UNDERLINE,BaseColor.RED);
                Paragraph p1 = new Paragraph(pair_headder.getText().toString());
                Font paraFont = new Font(Font.FontFamily.COURIER, 18.0f, Color.GREEN);
                p1.setAlignment(Paragraph.ALIGN_LEFT);
                p1.setFont(paraFont);

                //add paragraph to document
                doc.add(p1);
                try {
                    if (img_pairBarcode != null) {
                        ByteArrayOutputStream pstream = new ByteArrayOutputStream();
                        img_pairBarcode.setDrawingCacheEnabled(true);
                        Bitmap pbitmap = img_pairBarcode.getDrawingCache();
                        //Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.barcode);
                        pbitmap.compress(Bitmap.CompressFormat.JPEG, 100, pstream);
                        Image pmyImg = Image.getInstance(pstream.toByteArray());
                        pmyImg.setAlignment(Image.MIDDLE);

                        //add image to document
                        doc.add(pmyImg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


                Paragraph onedbarcodep = new Paragraph("1D Barcode");
                Font onedparaFont = new Font(Font.FontFamily.COURIER, 14.0f, Color.RED);
                onedbarcodep.setAlignment(Paragraph.ALIGN_CENTER);
                onedbarcodep.setFont(onedparaFont);
                doc.add(onedbarcodep);

                if (img_pairQrcode != null) {
                    ByteArrayOutputStream pstream1 = new ByteArrayOutputStream();
                    img_pairQrcode.setDrawingCacheEnabled(true);
                    Bitmap pbitmap1 = img_pairQrcode.getDrawingCache();
                    pbitmap1.compress(Bitmap.CompressFormat.JPEG, 100, pstream1);
                    Image pmyImg1 = Image.getInstance(pstream1.toByteArray());
                    pmyImg1.setAlignment(Image.MIDDLE);
                    doc.add(pmyImg1);
                }


                Paragraph twodbarcodep = new Paragraph("2D Scanner QR Code");
                Font twodparaFont = new Font(Font.FontFamily.COURIER, 14.0f, Color.RED);
                twodbarcodep.setAlignment(Paragraph.ALIGN_CENTER);
                twodbarcodep.setFont(twodparaFont);
                doc.add(twodbarcodep);

                String pairSteps = p_msg1.getText().toString() + " \n "
                        + p_msg2.getText().toString() + " \n "
                        + p_msg3.getText().toString() + " \n "
                        + p_msg4.getText().toString() + " \n "
                        + p_msg5.getText().toString() + " \n ";
                Paragraph p2 = new Paragraph(pairSteps);
                Font paraFont2 = new Font(Font.FontFamily.COURIER, 14.0f, Color.BLACK);
                p2.setAlignment(Paragraph.ALIGN_LEFT);
                p2.setFont(paraFont2);

                doc.add(p2);
                // Toast.makeText(BaseActivity.this, "PDF save successfully", Toast.LENGTH_LONG).show();
                //set footer
                Phrase footerText = new Phrase("This is an example of a footer");
                //HeaderFooter pdfFooter = new HeaderFooter(footerText, false);
                // doc.setFooter(pdfFooter);


            } catch (DocumentException de) {
                //Log.e("PDFCreator", "DocumentException:" + de);
                Toast.makeText(BaseActivity.this, "Error in creating PDF", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                //Log.e("PDFCreator", "ioException:" + e);
                Toast.makeText(BaseActivity.this, "Error in creating PDF", Toast.LENGTH_LONG).show();
            } finally {
                doc.close();
                openPDF(strDevice_id);
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(BaseActivity.this, "Error in creating PDF", Toast.LENGTH_LONG).show();

        }

    }

    private void openPDF(String strDevice_id) {
        String filename="Steps of Socket Scanner For Device "+strDevice_id+".pdf";
        File file = new File(Environment.getExternalStorageDirectory()+"/EventDexDocs/"+filename);
        if (file.exists())
        {
            Intent intent=new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);

            try
            {
                startActivity(intent);
            }
            catch(ActivityNotFoundException e)
            {
                Toast.makeText(BaseActivity.this, "No Application available to view pdf", Toast.LENGTH_LONG).show();
            }
        }
    }


    public static Bitmap encodeBarCodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height)
            throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            //hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = null;
        try {
            result = new DataMatrixWriter().encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            iae.printStackTrace();
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    public  void generateCsvFile(String barcode){
        try {
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "ScanAttendee Barcodes");
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    //Log.i("------------Folder Not Created", "Eventdex Lead Retrival");
                }
            }
            String name=checkedin_event_record.Events.Name.replaceAll("/","")+"_"+checkedin_event_record.Events.Id+"_"+sfdcddetails.user_id+"_"+user_profile.Profile.Email__c;
            File offlineCSV = new File(mediaStorageDir.getPath() + File.separator+name+"_barcodes.csv");

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.US);//MMM dd, yyyy hh:mm:ss a
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            SimpleDateFormat csv_date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);//Changed for Import scans requirement
            csv_date_format.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date=new Date();
            List<ScannedItems> scannedItem=Util.db.getSwitchedOnScanItem(checked_in_eventId);
            boolean isFreeSession = false;
            if (scannedItem.size() > 0) {
                isFreeSession = Util.db.isItemPoolFreeSession(scannedItem.get(0).BLN_Item_Pool__c,
                        checked_in_eventId);
            }

            if(!offlineCSV.exists()){
                FileWriter writer = new FileWriter(offlineCSV);
                writer.append("Custom Barcode");
                writer.append(',');
                writer.append("Scan Date");
                writer.append(',');
				/*writer.append("Scan Time");
				writer.append(',');*/
                writer.append("Scanned Session Id");
                writer.append(',');
                writer.append("Scanned Session Name");
                writer.append(',');
                writer.append("Scan Device");
                writer.append(',');
                writer.append("DeviceType");
                writer.append(',');
                writer.append("Scanned By Id");
                writer.append(',');
                writer.append("Scanned By");
                writer.append(',');
                writer.append("ScanDevice Mode");
                writer.append("\r\n");

                String dateTime= format.format(date);
                String csv_time = csv_date_format.format(date);
                String Item_pool_ids = ITransaction.EMPTY_STRING;
                if (isFreeSession) {
                    for (int i = 0; i < scannedItem.size(); i++) {
                        Item_pool_ids = Item_pool_ids + scannedItem.get(i).BLN_Item_Pool__c;
                        if (i != (scannedItem.size() - 1)) {
                            Item_pool_ids = Item_pool_ids + ",";
                        }
                    }
                }
                writer.append(barcode);
                writer.append(',');
                writer.append(csv_time.trim());
                writer.append(',');
				/*writer.append(csv_time.split(" ")[1].trim());
				writer.append(',');*/
                writer.append(Util.db.getSwitchedONGroup(BaseActivity.checkedin_event_record.Events.Id).Id);
                writer.append(',');
                writer.append(Util.db.getSwitchedONGroup(BaseActivity.checkedin_event_record.Events.Id).Name);
                writer.append(',');
                writer.append("ANDROID");
                writer.append(',');
                writer.append(Util.getDeviceName()+" - V "+getResources().getString(R.string.app_version_production));
                writer.append(',');
                writer.append(sfdcddetails.user_id);
                writer.append(',');
                writer.append(user_profile.Profile.First_Name__c+" "+user_profile.Profile.Last_Name__c);
                writer.append(',');
                writer.append(Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,""));
                writer.append("\r\n");

                OfflineScansObject offlineObj=new OfflineScansObject();
                offlineObj.badge_id=barcode;
                offlineObj.badge_status=DBFeilds.STATUS_OFFLINE;
                offlineObj.checkin_status="";
                offlineObj.item_pool_id=Item_pool_ids;
                offlineObj.event_id=checked_in_eventId;
                offlineObj.scan_date_time=dateTime;
                offlineObj.user_id=sfdcddetails.user_id;
                offlineObj.scan_group_id = Util.db.getSwitchedONGroupId(BaseActivity.checkedin_event_record.Events.Id);
                offlineObj.name = Util.db.getAttendeeName(barcode);
                offlineObj.scandevicemode =Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,"");
                Util.db.InsertAndUpdateOfflineScans(offlineObj);
                writer.flush();
                writer.close();
            }else{
                FileWriter writer = new FileWriter(mediaStorageDir.getPath() + File.separator+name+"_barcodes.csv",true);
                //List<ScannedItems> scannedItem=Util.db.getSwitchedOnScanItem(checked_in_eventId);
                //SimpleDateFormat format = new SimpleDateFormat("MMM/dd/yyyy hh:mm a");
                //Date date=new Date();
                String dateTime=format.format(date);
                String csv_time = csv_date_format.format(date);
                String Item_pool_ids = ITransaction.EMPTY_STRING;
                if (isFreeSession) {
                    for (int i = 0; i < scannedItem.size(); i++) {
                        Item_pool_ids = Item_pool_ids + scannedItem.get(i).BLN_Item_Pool__c;
                        if (i != (scannedItem.size() - 1)) {
                            Item_pool_ids = Item_pool_ids + ",";
                        }
                    }
                }
                writer.append(barcode);
                writer.append(',');
                writer.append(csv_time.trim());
				/*writer.append(',');
				writer.append(csv_time.split(" ")[1].trim());*/
                writer.append(',');
                writer.append(Util.db.getSwitchedONGroup(BaseActivity.checkedin_event_record.Events.Id).Id);
                writer.append(',');
                writer.append(Util.db.getSwitchedONGroup(BaseActivity.checkedin_event_record.Events.Id).Name);
                writer.append(',');
                writer.append("ANDROID");
                writer.append(',');
                writer.append(Util.getDeviceName()+" - V "+getResources().getString(R.string.app_version_production));
                writer.append(',');
                writer.append(sfdcddetails.user_id);
                writer.append(',');
                writer.append(user_profile.Profile.First_Name__c+" "+user_profile.Profile.Last_Name__c);
                writer.append(',');
                writer.append(Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,"mode"));
                writer.append("\r\n");

                OfflineScansObject offlineObj=new OfflineScansObject();
                offlineObj.badge_id=barcode;
                offlineObj.badge_status=DBFeilds.STATUS_OFFLINE;
                offlineObj.checkin_status="";
                offlineObj.item_pool_id=Item_pool_ids;
                offlineObj.event_id=checked_in_eventId;
                offlineObj.scan_date_time=dateTime;
                offlineObj.user_id=sfdcddetails.user_id;
                offlineObj.scan_group_id = Util.db.getSwitchedONGroupId(BaseActivity.checkedin_event_record.Events.Id);
                offlineObj.name = Util.db.getAttendeeName(barcode);
                offlineObj.scandevicemode =Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,"");
                Util.db.InsertAndUpdateOfflineScans(offlineObj);
                writer.flush();
                writer.close();
            }


            if(Util.db.getGroupList(checked_in_eventId).size() == 0){
                showScannedTicketsAlert("Please Buy at least one scanattendee ticket to scan tickets.",false);
            }else if(scannedItem.isEmpty()){
                showScannedTicketsAlert("Please TurnON at least one scanattendee ticket to scan tickets.",true);
            }else{
                if (!isOnline()) {
					/*MediaPlayer mediaPlayer = MediaPlayer.create(BaseActivity.this, R.raw.badgescanned);
					mediaPlayer.start();*/
                    //Toast.makeText(BaseActivity.this, "Badge Scanned...", Toast.LENGTH_LONG).show();
                    showCustomToast(this, "Badge Scanned...",
                            R.drawable.img_like, R.drawable.toast_greenroundededge, true);
                    //playSound(R.raw.badgescanned);
                    playSound(R.raw.beep);
                }
            }
			/*if(scannedItem.isEmpty()){
				openScanSettingsAlert("NONE");
			}else{
				MediaPlayer mediaPlayer = MediaPlayer.create(BaseActivity.this, R.raw.badgescanned);
				mediaPlayer.start();
			}*/

        } catch (Exception e) {
            e.printStackTrace();
            //Log.i("-----------------Offline Scan Exception-----------",":"+e.getMessage());
			/*MediaPlayer mediaPlayer = MediaPlayer.create(BaseActivity.this, R.raw.somethingwentwrong);
			mediaPlayer.start();*/
            playSound(R.raw.somethingwentwrong);
        }
    }

    public static void turnOnOffWifi(Context context, boolean isTurnToOn) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(isTurnToOn);
    }



    public void cancelSessionCount(final Activity context,boolean isFromPopupCancel){
        new CancelSessionTask(context, isFromPopupCancel).execute();
    }



    public class CancelSessionTask extends AsyncTask<String, Void, Void>{
        private final Activity context;
        private  boolean isFromPopupCancel;
        private final ProgressDialog dialog;
        public CancelSessionTask(final Activity context,boolean isFromPopupCancel){
            this.context = context;
            this.isFromPopupCancel = isFromPopupCancel;
            dialog=new ProgressDialog(context);
        }
        protected void onPreExecute() {
            if(isFromPopupCancel){
                dialog.setMessage("Please wait...");
            }else{
                dialog.setMessage("Please wait logging out...");
            }
            dialog.show();
        }
        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
         */
        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                // for(DeviceSessionId session:session_response){
                String sessionId = Util.eventPrefer.getString(Util.EVENT_CURRENT_SESSION_ID, "");
                if (sessionId != null) {
                    String endPoint = sfdcddetails.instance_url + WebServiceUrls.SA_DEVICE_SESSION_CANCEL + "SessionId="
                            + sessionId;
                    HttpClient client = HttpClientClass.getHttpClient(30000);
                    HttpPost post = new HttpPost(endPoint);
                    HttpResponse response;
                    post.addHeader("Authorization", "OAuth " + sfdcddetails.access_token);
                    response = client.execute(post);
                    // AppUtils.displayLog("Delete Session from Server",
                    // response.getStatusLine().getStatusCode()+"");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                dialog.dismiss();
                if(isFromPopupCancel){
                    Intent i=new Intent(context,EventListActivity.class);
                    i.putExtra(AppUtils.INTENT_KEY, sfdcddetails);
                    context.startActivity(i);
                    context.finish();
                }else{
                    boolean isdeleted = Util.db.isDBDeleted();
                    // SplashActivity.login_prefer.edit().putBoolean(Constants.AUTO_LOGIN,false).commit();
                    if (isdeleted) {
                        DeleteFiles();
                        //Util.killAllServices(BaseActivity.this);
                        Util.clearSharedPreference(Util.login_prefer);//For Update
                        Util.clearSharedPreference(Util.eventPrefer);
                        Util.clearSharedPreference(Util.first_login_pref);
                        Util.clearSharedPreference(Util.selected_session_attedee_pref);
                        Util.clearSharedPreference(Util.offset_pref);
                        Util.clearSharedPreference(Util.dashboard_data_pref);
                        Util.clearSharedPreference(Util.socket_device_pref);
                        //Util.clearSharedPreference(Util.external_setting_pref);
                        //Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
                        Util.clearSharedPreference(Util.external_setting_pref);
                        //Util.clearSharedPreference(PrinterDetails.selectedPrinterPrefrences); //TODO For Raj
                        Util.clearSharedPreference(Util.selfcheckinpref);
                        Util.clearSharedPreference(Util.lastModifideDate);
                        //Util.mApi = null;
                        CookieSyncManager.createInstance(BaseActivity.this);
                        try {
                            CookieManager.getInstance().removeAllCookie();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        Intent i = new Intent(context, DownloadService.class);
                        stopService(i);
                        isLogoutclicked=false;
                        isCloudPrintingON=false;
                        isBlutoothPrintingON=false;
                        Intent intent = new Intent(context, SplashActivity.class);
                        //intent.putExtra(AppUtils.ACCESS_TOKEN, access_token);
                        //intent.putExtra(AppUtils.REVOKE_KEY, true);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        //setResult(RESULT_OK, intent);
                        startActivity(intent);
                        finish();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void setSocketScannerStatus() {
        img_scanner_base = (ImageView) findViewById(R.id.img_scanner_base);
        layout_offline_tag = (LinearLayout)findViewById(R.id.layout_offline_tag);
        try {
            if (ScannerSettingsApplication.getInstance().getCurrentDevice() != null) {
				/*Log.i("-----------------Connected Device Name------------",
						":" + NullChecker(ScannerSettingsApplication.getInstance().getCurrentDevice().getName()));*/
                try {
                    if(BaseActivity.img_scanner_base != null){
                        BaseActivity.img_scanner_base.setImageResource(R.drawable.green_circle_1);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else {
				/*try {
					if (BaseActivity.img_scanner_base != null) {
						BaseActivity.img_scanner_base.setImageResource(R.drawable.red_circle_1);
					}
				}catch (Exception e){
					e.printStackTrace();
				}*/
            }

            if(isOnline()){
                if(BaseActivity.layout_offline_tag != null){
                    BaseActivity.layout_offline_tag.setVisibility(View.GONE);
                }
            }else{
                if(BaseActivity.layout_offline_tag != null){
                    BaseActivity.layout_offline_tag.setVisibility(View.VISIBLE);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void backupDatabase() throws IOException {
        //Open your local db as the input stream
        String inFileName = "/data/data/com.globalnest.scanattendee/databases/ScanAttendee";
        File dbFile = new File(inFileName);
        FileInputStream fis = new FileInputStream(dbFile);

        String outFileName = Environment.getExternalStorageDirectory()+"/MYDB.sql";

        //Log.i("------------output file name-----------",":"+outFileName);
        //Open the empty db as the output stream
        OutputStream output = new FileOutputStream(outFileName);
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer))>0){
            output.write(buffer, 0, length);
        }
        //Close the streams
        output.flush();
        output.close();
        fis.close();
    }

    public void hideAttendeeTab() {
        if (isBuyersAndAttendeesHide()) {
            list_layout.setVisibility(View.GONE);
            horizontal_line_attendees.setVisibility(View.GONE);
        }else{
            list_layout.setVisibility(View.VISIBLE);
            horizontal_line_attendees.setVisibility(View.VISIBLE);
        }
    }

    public static boolean isBadgeScanned(String badge_id) {
        boolean isbadge = false;
        try {
            String whereClause = " where Event_Id='" + checkedin_event_record.Events.Id + "' AND (BadgeId='" + badge_id.trim()
                    + "' OR " + DBFeilds.ATTENDEE_CUSTOM_BARCODE + " = '" + badge_id.trim() + "') AND (" + DBFeilds.ATTENDEE_TICKET_STATUS + " != 'Cancelled' OR " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Abandoned' OR " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted')";
            Cursor atendee_cursor = Util.db.getAttendeeDetailsWithAllTypes(whereClause);
            isbadge = atendee_cursor.moveToFirst();
            atendee_cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isbadge;
    }
    public static boolean isticketScanned(String ticket_id) {

        boolean isbadge = false;
        try {
            String whereClause = " where Event_Id='" + checkedin_event_record.Events.Id + "' AND ("+ DBFeilds.ATTENDEE_ID + "='" + ticket_id.trim() +"' OR "+ DBFeilds.ATTENDEE_BADGE_PARENT_ID +"='" + ticket_id.trim()+"') AND (" + DBFeilds.ATTENDEE_TICKET_STATUS + " != 'Cancelled' OR " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Abandoned' OR " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted')";
            Cursor atendee_cursor = Util.db.getAttendeeDetailsWithAllTypes(whereClause);
            isbadge = atendee_cursor.moveToFirst();
            atendee_cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isbadge;
    }
    public static boolean isOrderScanned(String order_id) {
        boolean isorder = false;
        try {
            String whereClause = " where Event_Id='" + checkedin_event_record.Events.Id	+ "' AND Order_Id='" + order_id.trim() + "'";
            Cursor order_cursor = Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
            isorder = order_cursor.moveToFirst();
            order_cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isorder;
    }

    public  void playSound(int resource_id){
        try{
            MediaPlayer p = MediaPlayer.create(BaseActivity.this, resource_id);
            p.start();
            p.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mp.release();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isDuplicateFailureRecord(OfflineSyncFailuerObject syncObj,List<OfflineSyncSuccessObject> SuccessTickets){

        boolean isBoolean = false;
        for(OfflineSyncSuccessObject success : SuccessTickets){
            if(syncObj.tbup.sessionid.equalsIgnoreCase(success.STicketId.BLN_Session_user__r.BLN_Group__c) && (syncObj.tbup.ticketId.equalsIgnoreCase(Util.NullChecker(success.STicketId.Ticket__r.Badge_ID__c)) ||
                    syncObj.tbup.ticketId.equalsIgnoreCase(Util.NullChecker(success.STicketId.Ticket__r.Custom_Barcode__c)) || syncObj.tbup.ticketId.equalsIgnoreCase(Util.NullChecker(success.STicketId.Ticket__r.Id)))){
                isBoolean = true;
                break;
            }
        }
        return isBoolean;
    }

    public void showMessageAlert(String msg, final boolean isFinish) {
        playSound(R.raw.error);
        Util.setCustomAlertDialog(BaseActivity.this);
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.setCustomDialogImage(R.drawable.alert);
        Util.txt_okey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();
                if (isFinish) {
                    finish();
                }

            }
        });
        Util.txt_dismiss.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();

            }
        });
        Util.openCustomDialog("Alert", msg);
    }
    public  void multipleticketCheckin(Context context, Cursor c_cursor, String session_id) {
        HashMap<String, Boolean> tickets_register = new HashMap<String, Boolean>();
        Cursor attendee_cursor;
        String whereClause,printed_attendeeid;
        printed_attendeeid=c_cursor.getString(c_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
        try {
            int progress_bar_index_count = -1;
            whereClause = " where Event_Id='" + checked_in_eventId + "' AND "+DBFeilds.ATTENDEE_ORDER_ID+"= '"
                    +c_cursor.getString(c_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID))+ "' AND "
                    +DBFeilds.ATTENDEE_ID+"= '"+printed_attendeeid+"' OR "+DBFeilds.ATTENDEE_BADGE_PARENT_ID+"= '"+printed_attendeeid+"'";
            attendee_cursor = Util.db.getAttendeeDataCursorForScan(whereClause);
            if(attendee_cursor.getCount()>0) {
                List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                boolean isFreeSession = false;
                if (scanticks.size() > 0) {
                    isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c,
                            checked_in_eventId);
                }
                if (isFreeSession) {
                    //attendee_id = new ArrayList<String>();
                    tickets_register.clear();
                    boolean isBreak = false;
                    for (int i = 0; i < attendee_cursor.getCount(); i++) {
                        attendee_cursor.moveToPosition(i);
                        //if (Util.db.isItemPoolBadgable(	attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)) {
                        String id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
                        boolean ischeckin = Util.db.SessionCheckInStatus(id, Util.db.getSwitchedONGroupId(checked_in_eventId));
                        //Log.i("---------------Free Session check in Status----------",":"+ischeckin);
                        if (ischeckin && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id + checked_in_eventId, false)) {
								/*isBreak = true;
								showMessageAlert(getString(R.string.checkin_only_msg), true);
								break;*/
                            showCustomToast(this,
                                    Util.db.getAttendeeNameWithId(id) + " Already checked in. Check out is disabled.",
                                    R.drawable.img_like, R.drawable.toast_redrounded, false);

                        } else {
                            //attendee_id.add(id);
                            tickets_register.put(id, ischeckin);
                        }

                    }
                    if (!isBreak) {
                        JSONArray ticketarray = new JSONArray();
                        // JSONObject parent = new JSONObject();
                        for (String key : tickets_register.keySet()) {
                            try {
                                JSONObject obj = new JSONObject();
                                obj.put("TicketId", key.trim());
                                obj.put("device", "ANDROID");
                                if (isNotFromSelfcheckin((BaseActivity) context)&&key.trim().equals(printed_attendeeid)) {
                                    obj.put("printstatus", "Printed");
                                    obj.put("devicenm",Util.getDeviceNameandAppVersion());
                                    if(Util.getselfcheckinbools(Util.ISSELFCHECKIN))
                                        obj.put("screenmode","self checkin");
                                    else
                                        obj.put("screenmode","attendee mobile");
                                    obj.put("printernm",NullChecker(PrinterDetails.selectedPrinterPrefrences.getString("printer", "")));
                                    obj.put("printtime",Util.getCurrentDateTimeInGMT());
                                }
                                scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                                if (scanticks.size() == 0) {
                                    obj.put("freeSemPoolId", "");
                                } else if (scanticks.size() > 0) {
                                    if (Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId)) {
                                        obj.put("freeSemPoolId", scanticks.get(0).BLN_Item_Pool__c);
                                    } else {
                                        obj.put("freeSemPoolId", "");
                                    }
                                }
                                if (String.valueOf(tickets_register.get(key)).equals("true")) {
                                    obj.put("isCHeckIn", false);
                                } else {
                                    obj.put("isCHeckIn", true);
                                }
                                obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
                                obj.put("sTime", Util.getCurrentDateTimeInGMT());
                                obj.put("scandevicemode",Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,""));
                                ticketarray.put(obj);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        new DoTicketCheckInFromBase(context, (progress_bar_index_count), ticketarray.toString()).execute();

                    }

                } /*else if (!Util.db.isItemPoolSwitchON(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)) {
					AppUtils.displayLog("----sai---","no pool id");
					//openScanSettingsAlert(TransperantGlobalScanActivity.this,attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),GlobalScanActivity.class.getName());
				} */else {
                    //attendee_id = new ArrayList<String>();
                    tickets_register.clear();
                    boolean isBreak = false;
                    for (int i = 0; i < attendee_cursor.getCount(); i++) {
                        attendee_cursor.moveToPosition(i);
                        String id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
                        String status = Util.db.getTStatusBasedOnGroup(id, attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                        boolean ischeckin = Boolean.valueOf(NullChecker(status));
                        if (Util.db.isItemPoolSwitchON(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)) {

                            if (ischeckin && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id + checked_in_eventId, false)) {
							/*isBreak = true;
							showMessageAlert(getString(R.string.checkin_only_msg), true);*/
                                showCustomToast(this,
                                        Util.db.getAttendeeNameWithId(id) + " Already checked in. Check out is disabled.",
                                        R.drawable.img_like, R.drawable.toast_redrounded, false);

                                //break;
                            } else {
                                //attendee_id.add(id);
                                tickets_register.put(id, ischeckin);
                            }
                        }
                        else {
                            String item_pool_id = Util.db.getItemPoolID(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                            String item_pool_name="";
                            String parent_id = Util.db.getItemPoolParentId(item_pool_id, checked_in_eventId);
                            if(!NullChecker(parent_id).isEmpty()) {
                                item_pool_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                            } else {
                                item_pool_name = Util.db.getItem_Pool_Name(item_pool_id, checked_in_eventId);
                            }
                            showCustomToast(this,"Sorry! You don't have permission to Check-In "+Util.db.getSwitchedONGroup(checked_in_eventId).Name, R.drawable.img_like, R.drawable.toast_redrounded, false);

                            // showCustomToast(TransperantGlobalScanActivity.this, "Sorry! You are not allowed to check-in for ""+item_pool_name+"".", R.drawable.img_like, R.drawable.toast_redrounded, false);
                      /*  attendee_id.add(id);
                        tickets_register.put(id, ischeckin);*/
                        }
                    }
                    if (!isBreak) {
                        JSONArray ticketarray = new JSONArray();
                        // JSONObject parent = new JSONObject();
                        for (String key : tickets_register.keySet()) {
                            try {
                                JSONObject obj = new JSONObject();
                                obj.put("TicketId", key.trim());
                                obj.put("device", "ANDROID");
                                if (isNotFromSelfcheckin((BaseActivity) context)&&key.trim().equals(printed_attendeeid)) {
                                    obj.put("printstatus", "Printed");
                                    obj.put("devicenm",Util.getDeviceNameandAppVersion());
                                    if(Util.getselfcheckinbools(Util.ISSELFCHECKIN))
                                        obj.put("screenmode","self checkin");
                                    else
                                        obj.put("screenmode","attendee mobile");
                                    obj.put("printernm",NullChecker(PrinterDetails.selectedPrinterPrefrences.getString("printer", "")));
                                    obj.put("printtime",Util.getCurrentDateTimeInGMT());
                                }
                                scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                                if (scanticks.size() == 0) {
                                    obj.put("freeSemPoolId", "");
                                } else if (scanticks.size() > 0) {
                                    if (Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId)) {
                                        obj.put("freeSemPoolId", scanticks.get(0).BLN_Item_Pool__c);
                                    } else {
                                        obj.put("freeSemPoolId", "");
                                    }
                                }
                                if (String.valueOf(tickets_register.get(key)).equals("true")) {
                                    obj.put("isCHeckIn", false);
                                } else {
                                    obj.put("isCHeckIn", true);
                                }
                                obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
                                obj.put("sTime", Util.getCurrentDateTimeInGMT());
                                obj.put("scandevicemode",Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,""));
                                ticketarray.put(obj);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        new DoTicketCheckInFromBase(context, (progress_bar_index_count), ticketarray.toString()).execute();

                    }

                }
            }else{
                updatePrintStatus(context, c_cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  void ticketCheckin(Context context, Cursor attendee_cursor, String session_id){
        Cursor c_new;
        try {
            int progress_bar_index_count = -1;
            //payment_cursor.moveToPosition((Integer) v.getTag());
            c_new = attendee_cursor;

            int tickets_count = 1;
            if (NullChecker("").isEmpty()) {
                String badge_id = c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_BADGEID));
                if (!AppUtils.NullChecker(badge_id).isEmpty()) {
                    tickets_count = Util.db.getAttendeeTicketsCount(badge_id, checked_in_eventId);
                }
            }
            String printstatus =c_new.
                    getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS));
            boolean isFreeSession = false;
            List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
            if (scanticks.size() > 0) {
                isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c,
                        checked_in_eventId);
            }
							/*if (tickets_count > 1) {
								Intent i = new Intent(AttendeeDetailActivity.this, BuyerLevelAttendeeList.class);
								i.putExtra(Util.ORDER_ID,
										c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID)));
								i.putExtra(Util.GET_BADGE_ID,
										c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)));
								startActivity(i);
							}else*/
            if(NullChecker(session_id).isEmpty()){
                ((BaseActivity) context).finish();
            }else if(!NullChecker(session_id).isEmpty() && (!NullChecker(session_id).equalsIgnoreCase(Util.db.getSwitchedONGroupId(checked_in_eventId)))){
                String item_pool_name = Util.db.getItem_Pool_Name(c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                String msg="Sorry! You are not allowed to check-in for "+item_pool_name;
                if(isNotFromSelfcheckin(context)){
                    ((BaseActivity) context).finish();
                }else {
                    showSingleButtonDialog("Alert", msg, context);
                }
                //showScannedTicketsAlert(msg,false);
				/*openScanSettingsAlert(context,c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),
						AttendeeDetailActivity.class.getName());*/
            }else if (isFreeSession) {

                boolean ischeckin = Util.db.SessionCheckInStatus(
                        c_new.getString(c_new.getColumnIndex("Attendee_Id")),
                        Util.db.getSwitchedONGroupId(checked_in_eventId));

                if(ischeckin&&!isNotFromSelfcheckin(context)){
                    showCustomToast(context,
                            c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
                                    +" "+c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))+" Already Checked In",
                            R.drawable.img_like,R.drawable.toast_redrounded,false);
                    //((BaseActivity) context).finish();
                    //showMessageAlert(getString(R.string.checkin_only_msg),false);
                }else if(ischeckin&&isNotFromSelfcheckin(context)&&Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                    if(NullChecker(printstatus).isEmpty()||NullChecker(printstatus).equals("Not Printed"))
                    {
                        updatePrintStatus(context, c_new);
                        showCustomToast(context,
                                c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
                                        +" "+c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))+
                                        " "+getString(R.string.checkin_only_msg),
                                R.drawable.img_like,R.drawable.toast_redrounded,false);

                    }else {
                        ((BaseActivity) context).finish();
                    }
                }else if(isOnline()){
                    JSONArray array = new JSONArray();
                    JSONObject obj = new JSONObject();
                    obj.put("TicketId", c_new.getString(c_new.getColumnIndex("Attendee_Id")));
                    obj.put("device", "ANDROID");
					/*if(isNotFromSelfcheckin((BaseActivity) context)){
						obj.put("printstatus", "Printed");
					}*/
                    if (((BaseActivity) context) instanceof TransperantGlobalScanActivity) {
                    }else {
                        obj.put("printstatus", "Printed");
                        obj.put("devicenm",Util.getDeviceNameandAppVersion());
                        if(Util.getselfcheckinbools(Util.ISSELFCHECKIN))
                            obj.put("screenmode","self checkin");
                        else
                            obj.put("screenmode","attendee mobile");
                        obj.put("printernm",NullChecker(PrinterDetails.selectedPrinterPrefrences.getString("printer", "")));
                        obj.put("printtime",Util.getCurrentDateTimeInGMT());
                    }
                    if (Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId)) {
                        obj.put("freeSemPoolId", scanticks.get(0).BLN_Item_Pool__c);
                        obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
                    } else {
                        obj.put("freeSemPoolId", "");
                        obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
                    }
                    if (ischeckin) {
                        obj.put("isCHeckIn", false);
                    } else {
                        obj.put("isCHeckIn", true);
                    }
                    obj.put("sTime", Util.getCurrentDateTimeInGMT());
                    obj.put("scandevicemode",Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,""));
                    array.put(obj);
                    new DoTicketCheckInFromBase(context,(progress_bar_index_count), array.toString()).execute();
                }
				/*{
								generateCsvFile(c_new.getString(c_new.getColumnIndex("Attendee_Id")));
								if(!NullChecker(session_id).isEmpty()){
									offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId
											+"' AND "+DBFeilds.OFFLINE_GROUP_ID+" = '"+session_id+ "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
								}else{
									offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId
											+ "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
								}
								if (list_offline != null && offlineList.size() > 0) {
									if (!NullChecker(activity_name).isEmpty()) {
										tab_bar.setVisibility(View.VISIBLE);
										buyer_tab.setVisibility(View.GONE);
									}
									list_offline.setAdapter(new AttendeeListActivity.OfflineListAdapter(offlineList));
									offline_tab.setVisibility(View.VISIBLE);
									img_refund_history.setVisibility(View.VISIBLE);
								}
							}*/


            } else if (!Util.db.isItemPoolSwitchON(
                    c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),
                    checked_in_eventId)) {
                String item_pool_name = Util.db.getItem_Pool_Name(c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                String msg="Sorry! You are not allowed to check-in for "+item_pool_name;
                if(isNotFromSelfcheckin(context)){
                    if(NullChecker(printstatus).isEmpty()||NullChecker(printstatus).equals("Not Printed"))
                    {
                        updatePrintStatus(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
                    }
                    else {
                        ((BaseActivity) context).finish();
                    }

                }else {
                    showSingleButtonDialog("Alert", msg, context);
                }
                //showScannedTicketsAlert(msg,false);
				/*openScanSettingsAlert(context,c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),
						AttendeeDetailActivity.class.getName());*/
            } else {

                String tstatus = Util.db.getTStatusBasedOnGroup(
                        c_new.getString(c_new.getColumnIndex("Attendee_Id")),
                        c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),
                        checked_in_eventId);
                if(!isNotFromSelfcheckin(context)&&!(((BaseActivity) context) instanceof TransperantGlobalScanActivity)&&Boolean.valueOf(tstatus)){

                    if(NullChecker(printstatus).isEmpty()||NullChecker(printstatus).equals("Not Printed"))
                    {
                        updatePrintStatus(context, c_new);
                        /*showCustomToast(context,
                                c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
                                        +" "+c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))+
                                        " "+getString(R.string.checkin_only_msg),
                                R.drawable.img_like,R.drawable.toast_redrounded,false);*/
                        if(Boolean.valueOf(tstatus)) {
                            showCustomToast(context,
                                    c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
                                            + " " + c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " Already Checked In",
                                    R.drawable.img_like, R.drawable.toast_redrounded, false);
                        }

                    }else {
                        ((BaseActivity) context).finish();
                    }
                    //showMessageAlert(getString(R.string.checkin_only_msg),false);
                }else if(Boolean.valueOf(tstatus)&&isNotFromSelfcheckin(context)&&Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                    if(NullChecker(printstatus).isEmpty()||NullChecker(printstatus).equals("Not Printed"))
                    {
                        updatePrintStatus(context, c_new);
                        showCustomToast(context,
                                c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
                                        +" "+c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))+
                                        " "+getString(R.string.checkin_only_msg),
                                R.drawable.img_like,R.drawable.toast_redrounded,false);

                    }else {
                        ((BaseActivity) context).finish();
                    }/*showCustomToast(context,
							c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
									+" "+c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
									+" "+getString(R.string.checkin_only_msg),
							R.drawable.img_like,R.drawable.toast_redrounded,false);
					((BaseActivity) context).finish();*/
                }else if(isOnline()){
                    JSONArray array = new JSONArray();
                    JSONObject obj = new JSONObject();
                    obj.put("TicketId", c_new.getString(c_new.getColumnIndex("Attendee_Id")));
                    obj.put("device", "ANDROID");
                    //if(isNotFromSelfcheckin((BaseActivity) context)){
                    obj.put("devicenm",Util.getDeviceNameandAppVersion());
                    if(Util.getselfcheckinbools(Util.ISSELFCHECKIN))
                        obj.put("screenmode","self checkin");
                    else
                        obj.put("screenmode","attendee mobile");
                    obj.put("printernm",NullChecker(PrinterDetails.selectedPrinterPrefrences.getString("printer", "")));
                    obj.put("printtime",Util.getCurrentDateTimeInGMT());
                    if (((BaseActivity) context) instanceof TransperantGlobalScanActivity) {
                    }else {
                        obj.put("printstatus", "Printed");
                    }
                    obj.put("freeSemPoolId", "");
                    if(Boolean.valueOf(tstatus)){
                        obj.put("isCHeckIn", false);
                    }
                    else{
                        obj.put("isCHeckIn", true);
                    }
                    obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
                    obj.put("sTime", Util.getCurrentDateTimeInGMT());
                    obj.put("scandevicemode",Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,""));
                    array.put(obj);
                    new DoTicketCheckInFromBase(context,(progress_bar_index_count), array.toString()).execute();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class DoTicketCheckInFromBase extends SafeAsyncTask<String> {

        int _index = 0;
        String _body = "";
        Context context;
        @Override
        protected Task<String> newTask() {
            return super.newTask();
        }

        /**
         *
         */
        public DoTicketCheckInFromBase(Context context,int index, String body) {
            this.context=context;
            this._index = index;
            this._body = body;
        }

        protected void onPreExecute() throws Exception {
            super.onPreExecute();
            dialog= new ProgressDialog(BaseActivity.this);
            dialog.setMessage("Attendee Checking In...");
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
                //parseJsonResponse(result);
                saveCheckinResponse(result,context);
            }
        }

    }

    private class DoupdatePrintStatus extends SafeAsyncTask<String> {

        int _index = 0;
        String _body = "";
        Context context;
        @Override
        protected Task<String> newTask() {
            return super.newTask();
        }

        /**
         *
         */
        public DoupdatePrintStatus(Context context,int index, String body) {
            this.context=context;
            this._index = index;
            this._body = body;
        }

        protected void onPreExecute() throws Exception {
            super.onPreExecute();
            dialog= new ProgressDialog(BaseActivity.this);
            dialog.setMessage("Updating Status...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        public String call() throws Exception {
            return postupdateStatus(_body);
        }

        protected void onSuccess(String result) throws Exception {
            super.onSuccess(result);
            dialog.dismiss();
            if (result != null) {
                //parseJsonResponse(result);
                savePrintStatusResponse(result,context);
            }
        }

    }
    private String postupdateStatus(String body) {
        String response = "";
        try {

            HttpParams params = new BasicHttpParams();
            int timeoutconnection = 30000;
            HttpConnectionParams.setConnectionTimeout(params, timeoutconnection);
            int sockettimeout = 30000;
            HttpConnectionParams.setSoTimeout(params, sockettimeout);
            HttpClient _httpclient = HttpClientClass.getHttpClient(30000);
            String _url = sfdcddetails.instance_url + WebServiceUrls.SA_BADGE_PRINTSTATUSUPDATE;
            AppUtils.displayLog("--------------status URL-------------", ":" + _url);
            HttpPost _httppost = new HttpPost(_url);
            _httppost.addHeader("Authorization", sfdcddetails.token_type + " " + sfdcddetails.access_token);
            AppUtils.displayLog("-----BEARER TOKEN---", ":" + sfdcddetails.token_type + " " + sfdcddetails.access_token);
            AppUtils.displayLog("---------------status update--------------", body);
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
                    + sfdcddetails.user_id + "&eventId=" + checked_in_eventId + "&source=Online"+"&DeviceType="+Util.getDeviceName().replaceAll(" ", "%20")
                    +"&checkin_only="+String.valueOf(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false));
            //		values.add(new BasicNameValuePair("checkin_only",String.valueOf(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false))));

            AppUtils.displayLog("--------------Check In URL-------------", ":" + _url);
            HttpPost _httppost = new HttpPost(_url);
            _httppost.addHeader("Authorization", sfdcddetails.token_type + " " + sfdcddetails.access_token);
            AppUtils.displayLog("-----BEARER TOKEN---", ":" + sfdcddetails.token_type + " " + sfdcddetails.access_token);
            AppUtils.displayLog("---------------Checki in Body--------------", body);
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

    public  void updatePrintStatus(Context context, Cursor attendee_cursor){
        if(SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor!=null) {
            SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor.moveToFirst();
        }
        Cursor c_new;
        try {
            int progress_bar_index_count = -1;
            //payment_cursor.moveToPosition((Integer) v.getTag());
            c_new = attendee_cursor;
            if(isOnline()){
                JSONArray array = new JSONArray();
                JSONObject obj = new JSONObject();
                obj.put("ticketId", c_new.getString(c_new.getColumnIndex("Attendee_Id")));
                obj.put("status", "Printed");
                obj.put("devicenm",Util.getDeviceNameandAppVersion());
                if(Util.getselfcheckinbools(Util.ISSELFCHECKIN))
                    obj.put("screenmode","self checkin");
                else
                    obj.put("screenmode","attendee mobile");

                obj.put("printernm",NullChecker(PrinterDetails.selectedPrinterPrefrences.getString("printer", "")));
                obj.put("printtime",Util.getCurrentDateTimeInGMT());
                array.put(obj);
                new DoupdatePrintStatus(context,(progress_bar_index_count), array.toString()).execute();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public  Boolean isNotFromSelfcheckin(Context context){
        if(context instanceof AttendeeDetailActivity)
            return true;
        else if(context instanceof OrderSucessPrintActivity)
            return true;
		/*else if(context instanceof TransperantGlobalScanActivity&&!Util.getselfcheckinbools(Util.ISSELFCHECKIN))
			return true;*/
        return false;

    }
    public void onPrintSuccess(Context context) {
        try {
            if(SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor!=null) {
                SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor.moveToFirst();
            }            String sessionid = Util.db.getSwitchedONGroupId(checked_in_eventId);
            String status=Util.db.getTStatusBasedOnGroup(SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor.
                            getString(SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)),
                    SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor.getString(SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
            boolean ischeckin = Boolean.valueOf(NullChecker(status));
            String printstatus =SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor.
                    getString(SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS));
            if(!Util.getselfcheckinbools(Util.ISAUTOCHECKIN) && Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                if(NullChecker(printstatus).isEmpty()||NullChecker(printstatus).equals("Not Printed"))
                {
                    updatePrintStatus(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
                }else {
                    Intent i = new Intent(context, SelfCheckinAttendeeList.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    // ((Activity)context).finish();
                }
            }else if (Util.getselfcheckinbools(Util.ISAUTOCHECKIN) && Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
				/*boolean ischeckin = Util.db.SessionCheckInStatus(AttendeeDetailActivity.selfcheckin_payment_cursor.getString(AttendeeDetailActivity.selfcheckin_payment_cursor.getColumnIndex("Attendee_Id")),
						Util.db.getSwitchedONGroupId(checked_in_eventId));*/
                String classname=context.getClass().getSimpleName();
                boolean finish=false;
                if((classname.equals("TransperantGlobalScanActivity"))||(classname.equals("AddAttendeeActivity"))){
                    finish=true;
                }
                if (ischeckin) {
                    showCustomToast(context,
                            "Your are already Checked In",
                            R.drawable.img_like,R.drawable.toast_redrounded,false);
                    if(NullChecker(printstatus).isEmpty()||NullChecker(printstatus).equals("Not Printed"))
                    {
                        updatePrintStatus(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
                    }
                    else if(context.getClass().getSimpleName().equals("TransperantGlobalScanActivity")){
                        ((Activity)context).finish();
                    }else if(context.getClass().getSimpleName().equals("BarCodeScanActivity")){
                        ((BarCodeScanActivity)context).scanStart();
                        //context.startActivity(new Intent(context,BarcodeScanActivity.class));

                    }else{
                        Intent i = new Intent(context, SelfCheckinAttendeeList.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        // ((Activity)context).finish();
                    }
                } else {
                    if(Util.db.getGroupCount(checked_in_eventId) == 0) {
                        showSingleButtonDialog("Alert","You don't have any session to scan.Please contact Event Organizer!",context);
                    }else if(Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()) {
                        showSingleButtonDialog("Alert","Please TurnON atleast one session for scanning.",context);
                    } else{
                        ticketCheckin(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor, sessionid);
                    }

                }
            }else  if(context instanceof  OrderSucessPrintActivity) {
                if (Util.db.getGroupCount(checked_in_eventId) == 0) {
                    //updatePrintStatus(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
                    if(NullChecker(printstatus).isEmpty()||NullChecker(printstatus).equals("Not Printed"))
                    {
                        updatePrintStatus(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
                    }else {
                        ((Activity) context).finish();
                    }
                } else if (Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()) {
                    if(NullChecker(printstatus).isEmpty()||NullChecker(printstatus).equals("Not Printed"))
                    {
                        updatePrintStatus(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
                    }else {
                        ((Activity) context).finish();
                    }
                    //updatePrintStatus(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
                } else if ((!NullChecker(sessionid).isEmpty())) {
                    multipleticketCheckin(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor, sessionid);
                }
            }else if(isNotFromSelfcheckin(context)){
                if (Util.db.getGroupCount(checked_in_eventId) == 0) {
                    //updatePrintStatus(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
                    if(NullChecker(printstatus).isEmpty()||NullChecker(printstatus).equals("Not Printed"))
                    {
                        updatePrintStatus(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
                    }else {
                        ((Activity) context).finish();
                    }
                } else if (Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()) {
                    //updatePrintStatus(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
                    if(NullChecker(printstatus).isEmpty()||NullChecker(printstatus).equals("Not Printed"))
                    {
                        updatePrintStatus(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
                    }
                    else {
                        ((Activity) context).finish();
                    }
                }else if((!NullChecker(sessionid).isEmpty())&& (!Util.db.isItemPoolSwitchON(
                        SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor.getString(SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),
                        checked_in_eventId)) ) {
                    if(NullChecker(printstatus).isEmpty()||NullChecker(printstatus).equals("Not Printed"))
                    {
                        updatePrintStatus(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
                    }
                    else {
                        ((Activity) context).finish();
                    }
                }else {
                    ticketCheckin(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor, sessionid);

                }
            }else {
                Intent i = new Intent(context, SelfCheckinAttendeeList.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
            // ((Activity)context).finish();
        }/*else{
				if(context instanceof AttendeeDetailActivity){
					updatePrintStatus(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
				}
		}*/ catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void savePrintStatusResponse(String response,Context context) {
        try {
            if (!response.isEmpty()) {
                JSONArray badge_array = new JSONArray(response);
                for (int i = 0; i < badge_array.length(); i++) {
                    JSONObject badge_obj = badge_array.optJSONObject(i);
                    String printstatus = badge_obj.optString("status");
                    String TicketId = badge_obj.optString("ticketId");
                    String lastmodifieddate = badge_obj.optString("lastmodifieddate");
                    Util.db.insertandupdateAttendeeBadgeIdandPrintstatus(TicketId,printstatus,lastmodifieddate);                }
                showCustomToast(this, "Updated Successfully! ",
                        R.drawable.img_like, R.drawable.toast_greenroundededge, true);
                if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
                    Intent i = new Intent(context, SelfCheckinAttendeeList.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    // ((Activity)context).finish();
                }else {
                    ((BaseActivity) context).finish();
                }

            }
        }catch(Exception e){
            e.printStackTrace();
            showCustomToast(this,
                    "Print Completed, Updated Failed ",
                    R.drawable.img_like, R.drawable.toast_redrounded, false);
            //showMessageAlert("Print Completed, Updated Failed",true);
        }
    }

    public void saveCheckinResponse(String result,Context context) {
        try {
            OfflineSyncResController offlineResponse = new Gson().fromJson(result, OfflineSyncResController.class);
            if (NullChecker(offlineResponse.ErrorMsg).isEmpty()) {
                List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                boolean isFreeSession = false;
                if (scanticks.size() > 0) {
                    isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId);
                }
                boolean isCheckIn = false;
                if (offlineResponse.ticketbadgeprintstatus.size() > 0) {
                    for(OfflineSyncResController.Ticketbadgeprintstatus tktbadstatus: offlineResponse.ticketbadgeprintstatus){
                        String attendee_Id = Util.NullChecker(tktbadstatus.Ticketid);
                        String printstatus = Util.NullChecker(tktbadstatus.PrintStatus);
                        String lastmodifieddate = badge_obj.optString("lastmodifieddate");
                        Util.db.insertandupdateAttendeeBadgeIdandPrintstatus(TicketId,printstatus,lastmodifieddate);                    }
                }
                if (offlineResponse.SuccessTickets.size() > 0) {
                    for (OfflineSyncSuccessObject checkin_obj : offlineResponse.SuccessTickets) {
                        isCheckIn = Boolean.valueOf(checkin_obj.Status);
                        String attendee_Id = checkin_obj.STicketId.Ticket__c;//success.optJSONObject(i).optJSONObject("STicketId").optString("Ticket__c");
                        String name=Util.db.getAttendeeNameWithId(attendee_Id);
                        if (isFreeSession) {
                            List<TStatus> session_attendee = new ArrayList<TStatus>();
                            session_attendee.add(checkin_obj.STicketId);
                            Util.db.InsertAndUpdateSessionAttendees(session_attendee, checked_in_eventId);
                        } else {
                            Util.db.updateCheckedInStatus(checkin_obj.STicketId, checked_in_eventId);
                        }
                        if(context.getClass().getSimpleName().equals("SelfCheckinAttendeeDetailActivity")&&checkinbutton_clicked){
                            checkinbutton_clicked=false;
                            showSingleButtonDialog("Alert!","Checked In Successfully!", context);
                            playSound(R.raw.beep);
                        }else if(context.getClass().getSimpleName().equals("TransperantGlobalScanActivity")){
                            ((Activity)context).finish();
                        }else if(context.getClass().getSimpleName().equals("BarCodeScanActivity")){
                            ((BarCodeScanActivity)context).scanStart();
                            //context.startActivity(new Intent(context,BarcodeScanActivity.class));
                        }else if(isNotFromSelfcheckin(context)){
                            if(isCheckIn) {
                                showCustomToast(this,
                                        name +" Checked In Successfully! ",
                                        R.drawable.img_like, R.drawable.toast_greenroundededge, true);
                                playSound(R.raw.beep);
                            }else {
                                showCustomToast(this,
                                        name +" Checked out Successfully! ",
                                        R.drawable.img_like, R.drawable.toast_redrounded, false);
                                playSound(R.raw.checkout);
                            }
                            ((BaseActivity) context).finish();
                        }else {
                            showCustomToast(this,
                                    " Checked In Successfully! ",
                                    R.drawable.img_like, R.drawable.toast_greenroundededge, true);
                            playSound(R.raw.beep);
                            ((BaseActivity) context).finish();
                        }
                    }
					/*AlertDialogCustom dialog = new AlertDialogCustom(this);
					dialog.setParamenters("Alert",
							"Print Completed", null, null,
							1, true);
					dialog.show();*/
                    //showMessageAlert("Print Completed", true);

                    //Toast.makeText(this, "Checked In Sucessfully", Toast.LENGTH_LONG).show();
                }else if(offlineResponse.FailureTickets.size() > 0){//Added fr Aditya issue
                    showCustomToast(this,
                            NullChecker(offlineResponse.FailureTickets.get(0).msg),
                            R.drawable.img_like,R.drawable.toast_redrounded,false);
                    if(context.getClass().getSimpleName().equals("TransperantGlobalScanActivity")){
                        ((Activity)context).finish();
                    }else if(context.getClass().getSimpleName().equals("BarCodeScanActivity")){
                        ((BarCodeScanActivity)context).scanStart();
                        //context.startActivity(new Intent(context,BarcodeScanActivity.class));

                    }else {
                        ((BaseActivity) context).finish();
                    }
                }else{
                    showCustomToast(this,
                            " Already Checked In",
                            R.drawable.img_like,R.drawable.toast_redrounded,false);
                    if(context.getClass().getSimpleName().equals("TransperantGlobalScanActivity")){
                        ((Activity)context).finish();
                    }else if(context.getClass().getSimpleName().equals("BarCodeScanActivity")){
                        ((BarCodeScanActivity)context).scanStart();
                        //context.startActivity(new Intent(context,BarcodeScanActivity.class));

                    }else {
                        ((BaseActivity) context).finish();
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            showMessageAlert("Print Completed , Checkin Failed",true);
        }
    }
    public static void showCustomToast(Context context, String message, int imageResource, int drawable, boolean imgshow){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;//getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast,null);
        LinearLayout lay_toast=(LinearLayout)toastLayout.findViewById(R.id.custom_toast_layout) ;
        ImageView img_view= (ImageView) toastLayout.findViewById(R.id.custom_toast_image);
        TextView txt_message= (TextView) toastLayout.findViewById(R.id.custom_toast_message);
        lay_toast.setBackgroundResource(drawable);
        //lay_toast.setBackground(context.getResources().getDrawable(R.drawable.toast_greenroundededge));
        txt_message.setText(message);
        if(imgshow) {
            img_view.setVisibility(View.VISIBLE);
            img_view.setImageResource(imageResource);
        }else{
            img_view.setVisibility(View.GONE);
        }
        if(context!=null) {
            Toast toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(toastLayout);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 300);
            toast.show();
        }else {
            Toast toast = new Toast((BaseActivity) context);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(toastLayout);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 300);
            toast.show();
        }
    }
    public static void showSingleButtonDialog(String title,String message,final Context context) {
        try{
            Util.setCustomAlertDialog(context);
            Util.openCustomDialog(title,message);
            Util.txt_okey.setText("Ok");
            Util.txt_dismiss.setVisibility(View.GONE);
            Util.alert_dialog.setCancelable(false);
            Util.txt_okey.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Util.alert_dialog.dismiss();
                    if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
                        Intent i=new Intent(context,SelfCheckinAttendeeList.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(i);
                        //context.finish();
                    }else {
                        ((BaseActivity) context).finish();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            ((BaseActivity) context).finish();
        }
    }
    public static boolean isBadgeSelected(){
        ArrayList<BadgeResponseNew> badge_res = new ArrayList<BadgeResponseNew>();
		/*String where_att = " Where " + DBFeilds.BADGE_NEW_EVENT_ID + " = '"
				+ checkedin_event_record.Events.Id + "' AND " + DBFeilds.BADGE_NEW_ID
				+ " = '" + BaseActivity.checkedin_event_record.Events.badge_name + "'";*/
        badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
        if(badge_res.size()==1){
            return true;
        }else{
            return false;
        }
    }
    private boolean isPermissionAllowedorDened(final Context context, String[] permission) {
        Boolean isallowed=false;
        Dexter.withActivity(this)
                /*.withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
               */ .withPermissions(permission)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {

                        }
                        // check for permanent denial of any permission
                        else if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            Util.setCustomAlertDialog(BaseActivity.this);
                            //app needs permission to use this feature. You can grant them in app settings.
                            Util.openCustomDialog("Alert", "ScanAttendee app needs this permission to enable this feature. Please grant them in app settings.");
                            Util.txt_okey.setText("App Settings");
                            Util.txt_dismiss.setText("Dismiss");
                            Util.txt_dismiss.setVisibility(View.VISIBLE);
                            Util.txt_okey.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View arg0) {
                                    Util.alert_dialog.dismiss();
                                    openAppSettingsPage();
                                }
                            });
                            Util.txt_dismiss.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Util.alert_dialog.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(
                        new PermissionRequestErrorListener() {
                            @Override
                            public void onError(DexterError error) {
                                Toast.makeText(context.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                .onSameThread()
                .check();
        return isallowed;
    }
    public void openAppSettingsPage() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }
    public void ispermissionEnabledforCheckin(Cursor c,Context context){
        String whereClause = " where Event_Id='" + checked_in_eventId  + "' AND (BadgeId='" + orderId.trim() + "' OR "+DBFeilds.ATTENDEE_CUSTOM_BARCODE+" = '"+orderId.trim()+"')";
        c=Util.db.getAttendeeDetailsWithAllTypes(whereClause);
        String Ticketname="";
        String parent_id = Util.db.getItemPoolParentId(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checkedin_event_record.Events.Id);
        if(!NullChecker(parent_id).isEmpty()) {
            Ticketname = Util.db.getItem_Pool_Name(parent_id, checkedin_event_record.Events.Id);
        }else{
            Ticketname = Util.db.getItem_Pool_Name(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checkedin_event_record.Events.Id);
        }
        String msg="Sorry! You are not allowed to check-in for "+Ticketname;
        showSingleButtonDialog("Alert",msg,context);
    }

}



