package com.globalnest.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Base64;

import com.facebook.internal.ServerProtocol;
import com.globalnest.mvc.AttendeeAddressHandler;
import com.globalnest.mvc.AttendeeAddressListObject;
import com.globalnest.mvc.BadgeController;
import com.globalnest.mvc.BadgeElements;
import com.globalnest.mvc.BadgeLayerNew;
import com.globalnest.mvc.BadgeResponseNew;
import com.globalnest.mvc.BuyerInfoHandler;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.mvc.MultiListValues;
import com.globalnest.mvc.OfflineScansObject;
import com.globalnest.mvc.OrderDetailsHandler;
import com.globalnest.mvc.OrderItemListHandler;
import com.globalnest.mvc.OrderItemPoolHandler;
import com.globalnest.mvc.OrderListHandler;
import com.globalnest.mvc.PaymentTypeHandler;
import com.globalnest.mvc.RefreshResponse;
import com.globalnest.mvc.SelfcheckinColumns;
import com.globalnest.mvc.SeminaAgenda;
import com.globalnest.mvc.SessionGroup;
import com.globalnest.mvc.TStatus;
import com.globalnest.mvc.TicketHandler;
import com.globalnest.mvc.TicketListResponseHandler;
import com.globalnest.mvc.TicketPoolHandler;
import com.globalnest.mvc.TicketResponseHandler;
import com.globalnest.mvc.TicketTagHandler;
import com.globalnest.mvc.TicketTypeHandler;
import com.globalnest.network.HttpGetMethod;
import com.globalnest.network.HttpPostData;
import com.globalnest.objects.BadgeRecord;
import com.globalnest.objects.Country;
import com.globalnest.objects.CountryResposneObj;
import com.globalnest.objects.Currency;
import com.globalnest.objects.EditEventResponse;
import com.globalnest.objects.Event;
import com.globalnest.objects.Event.Organaizer;
import com.globalnest.objects.EventObjects;
import com.globalnest.objects.EventPaymentTypes;
import com.globalnest.objects.EventPriceObject;
import com.globalnest.objects.ItemType;
import com.globalnest.objects.PaymentGateWays;
import com.globalnest.objects.PaymentType;
import com.globalnest.objects.AllPickListValues;
import com.globalnest.objects.RegistrationSettingsController;
import com.globalnest.objects.ScannedItems;
import com.globalnest.objects.StateResponseObj;
import com.globalnest.objects.States;
import com.globalnest.objects.UserObjects;
import com.globalnest.scanattendee.BaseActivity;
import com.globalnest.scanattendee.R;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.SFDCDetails;
import com.globalnest.utils.Util;
import com.globalnest.utils.Util.ITEM_TYPES;
import com.google.gson.Gson;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;



@SuppressLint({"NewApi", "SimpleDateFormat"})
public class DataBase {
    private static final String CREATE_ADDEDTICKETS = "CREATE TABLE IF NOT EXISTS ItemDetails(_id integer primary key autoincrement , ItemId  TEXT , ItemName  TEXT , ItemPrice  DOUBLE , ItemPoolId  TEXT , ItemQuantity  TEXT , Item_max_per_order__c  TEXT ,Item_min_per_order__c  TEXT , ItemSoldQuantity  TEXT , ItemType  TEXT , ItemSalesStartDate  NVARCHAR , ItemImageUrl  TEXT , ItemPaymentType  TEXT , ItemSalesEnddate  NVARCHAR , ItemOption  TEXT , ItemTypeName  VARCHAR , EventId  TEXT , ItemStatus  TEXT , isTaxApplicable  TEXT , Item_BL_Fee TEXT ,ItemFee TEXT ,Item_SA_Visibility TEXT , ServiceFee  TEXT )";
    private static final String CREATE_ADDRESS = "CREATE TABLE IF NOT EXISTS UserAddress (_id integer primary key autoincrement,address_type TEXT, address_1 TEXT, address_2 TEXT, city TEXT, STATE TEXT, company_id TEXT, company_name TEXT, creditcard_authorize_key TEXT,work_phone TEXT, zipcode TEXT, country TEXT )";
    private static final String CREATE_BADGE_TEMPLATE_LAYERS = "CREATE TABLE IF NOT EXISTS BadgeLayers(_id integer primary key autoincrement, layer_id TEXT, layer_data TEXT,event_id TEXT,badge_id TEXT)";
    private static final String CREATE_BADGE_TEMPLATE_NEW = "CREATE TABLE IF NOT EXISTS BadgeTemplateNew(_id integer primary key autoincrement, badge_id TEXT, badge_name TEXT,badge_desc TEXT,badge_data TEXT,module TEXT,event_id TEXT,badge_isselcted TEXT DEFAULT 'false',badge_defaultbadge_selected TEXT DEFAULT 'false')";
    private static final String CREATE_EVENTDETAILS = "CREATE TABLE IF NOT EXISTS EventDetails(_id integer primary key autoincrement , EventName TEXT,  StartDate NVARCHAR ,  StartTime NVARCHAR ,  EndDate NVARCHAR ,  EndTime NVARCHAR ,  isCheckedIn TEXT ,  EventID TEXT ,  Address TEXT ,  Location TEXT ,  City TEXT ,  Country TEXT ,  Country_Id TEXT ,  Country_Short TEXT ,  State TEXT ,  State_Id TEXT ,  State_Short TEXT ,  Phone TEXT ,  ZipCode TEXT ,  EventCategory TEXT ,  TotalSeatsAvailable TEXT ,  isEventStaff TEXT ,  EventImageUrl TEXT ,  SADashboardimgUrl TEXT , selfcheckinbgcolor TEXT ,selfcheckineventlogo TEXT ,registration_link TEXT ,  EventBadgeName TEXT ,  LastModifyDate TEXT ,  FeeApplicable TEXT  DEFAULT true , ImageBlob LONGBLOB DEFAULT NULL , EventSalesTax TEXT Default (00.00) ,  EventDescription TEXT ,  EventStatus TEXT ,  TimeZone TEXT ,  scan_attendee_limit__c TEXT , organizer_id__c TEXT , organaizer_name TEXT , organizer_email TEXT , event_total_attendee_count TEXT , orders_count TEXT , event_total_orders_count TEXT , sessiontime TEXT,TicketTypes TEXT,user_role TEXT,country_codes TEXT,AllowNoticketSessions__c TEXT DEFAULT 'false',event_dashboardvisibity TEXT DEFAULT 'true',event_currency TEXT)";
    private static final String CREATE_EVENTPRICING = "CREATE TABLE IF NOT EXISTS Event_Pricing(_id integer primary key autoincrement, event_id_pricing TEXT,pricing_id TEXT,pricing_name TEXT,pricing_item_type TEXT,pricing_per TEXT,pricing_maxfee TEXT,pricing_is_product TEXT,pricing_fee TEXT)";
    private static final String CREATE_EVENT_PAYMENT_SETTINGS = "CREATE TABLE IF NOT EXISTS PaymentSettings (_id integer primary key autoincrement,event_id TEXT, PGateway_id TEXT, Id TEXT, Name TEXT, Event_Pay_Adoptive_Type__c TEXT, PayGateway_label__c TEXT,Pay_Gateway__c TEXT, Registration_Type__c TEXT)";
    private static final String CREATE_ITEMPOOL = "CREATE TABLE IF NOT EXISTS ItemPoolDetails(_id integer primary key autoincrement , EventId TEXT , ItemPoolId TEXT , ItemPoolName TEXT , ItemTypeId TEXT , ItemPoolCount TEXT , Addon_Count__c TEXT ,Addon_Parent__c TEXT , Badgable__c TEXT ,Badge_Label__c TEXT , Ticket_Settings__c TEXT ,PoolName TEXT,ItemPool_Ticketed_Sessions__c TEXT DEFAULT 'false')";
    private static final String CREATE_ITEMTYPE = "CREATE TABLE IF NOT EXISTS ItemType(_id integer primary key autoincrement,ItemTypeId TEXT , ItemTypeName TEXT , ItemTypeCurrency TEXT)";
    private static final String CREATE_ITEM_REG_SETTINGS = "CREATE TABLE IF NOT EXISTS Item_Reg_Settings(_id integer primary key autoincrement, reg_id TEXT, reg_item_id TEXT, reg_column_name TEXT, reg_default_label TEXT, reg_include TEXT, reg_label_name TEXT, reg_required TEXT,reg_eventid TEXT,reg_setting_type TEXT,reg_group_name__c TEXT,reg_group_label__c TEXT ,reg_fieldtype TEXT) ";//
    private static final String CREATE_ORDERDETAILS = "CREATE TABLE IF NOT EXISTS OrderDetails (_id integer primary key autoincrement , Event_Id NVARCHAR , Order_Id NVARCHAR , Buyer_Id NVARCHAR , Payment_Type NVARCHAR , Order_Status NVARCHAR , Order_Total DOUBLE , Order_Date NVARCHAR , PayKey NVARCHAR , Order_Name NVARCHAR , Order_SubTotal  DOUBLE  , Fee_Amount DOUBLE , Amount_Paid DOUBLE ,Amount_Due DOUBLE ,Order_Discount DOUBLE , Order_Tax DOUBLE , payment_id NVARCHAR ,check_number NVARCHAR , card_type NVARCHAR , last_card_degits NVARCHAR , registration_type NVARCHAR , paygateway_type_id NVARCHAR , paygateway_type_name NVARCHAR , Payment_Mode NVARCHAR )";
    private static final String CREATE_ORDER_PAYMENT_ITEMS ="CREATE TABLE IF NOT EXISTS OrderPaymentItemDetails (_id integer primary key autoincrement , payment_item_id NVARCHAR ,payment_createddate NVARCHAR ,payment_settlementdate NVARCHAR , payment_credit_card_4digits TEXT, payment_credit_card_type TEXT,currency_code TEXT , payment_lastmodifieddate TEXT ,payment_name TEXT , payment_order_id TEXT ,pay_reg_type TEXT , payment_amount DOUBLE ,payment_mode TEXT , payment_ref_number TEXT , payment_status TEXT , payment_type  TEXT , payment_pay_gateway_id  TEXT , payment_pay_gateway_name TEXT ,payment_item_eventid TEXT , payment_notes TEXT )";
    private static final String CREATE_ORDERITEMTABLE = "CREATE TABLE IF NOT EXISTS OrderItemDetails (_id integer primary key autoincrement , Order_Item_Id NVARCHAR , Order_Id NVARCHAR , Pool_Name NVARCHAR , Item_Id NVARCHAR , Item_Name NVARCHAR ,Item_Type NVARCHAR, Event_Id NVARCHAR , Item_Qty NVARCHAR  , Item_Total_Price DOUBLE , Each_Item_Price DOUBLE , Item_Discount DOUBLE , Order_Item_Currency NVARCHAR, Order_Item_Status NVARCHAR, Order_Item_Fee NVARCHAR, Order_Item_Tax NVARCHAR, Promo_Code NVARCHAR )";
    //newly added Attendee_Image
    private static final String CREATE_HIDEITEMS ="CREATE TABLE IF NOT EXISTS HideItems (_id integer primary key autoincrement, Item_Poolid TEXT, Event_id TEXT,Hide_Status TEXT DEFAULT 'true' )";
    private static final String CREATE_PICKLISTVALUES ="CREATE TABLE IF NOT EXISTS PicklistValues (_id integer primary key autoincrement, Picklistvalue VARCHAR, fieldName TEXT, CustomvalueName VARCHAR , List_Description__c TEXT , List_Code__c VARCHAR , Customvalueid TEXT ,Sort_Order__c integer  )";
    // private static final String CREATE_ORDERTICKETDETAILS = "CREATE TABLE IF NOT EXISTS OrderTicketDetails ( Attendee_Id TEXT , Attendee_First_Name  TEXT , Attendee_Last_Name  TEXT  , Attendee_Suffix  TEXT , Attendee_Prefix  TEXT  , Attendee_Email_Id  TEXT , Attendee_Company   TEXT DEFAULT '' ,  Attendee_Image   TEXT DEFAULT '' ,Attendee_Company_Logo TEXT DEFAULT '' ,Attendee_Company_Id   TEXT DEFAULT '' , Ticket_Status  TEXT DEFAULT '' , RSVP_Status  TEXT DEFAULT '' , Attendee_Job_Tile  TEXT DEFAULT '' , Attendee_Mobile TEXT DEFAULT '' ,Attendee_Phone  TEXT DEFAULT '' , Attendee_Address_1  TEXT DEFAULT '' , Attendee_Address_2  TEXT DEFAULT '' , Attendee_City  TEXT DEFAULT '' , Attendee_State  TEXT DEFAULT '' , Attendee_Country  TEXT DEFAULT '' , Attendee_Zipcode  TEXT DEFAULT '' , Attendee_Home_Phone  TEXT DEFAULT '' , Attendee_Home_Address_1  TEXT DEFAULT '' , Attendee_Home_Address_2  TEXT DEFAULT '' , Attendee_Home_City  TEXT DEFAULT '' , Attendee_Home_State  TEXT DEFAULT '' , Attendee_Home_Country  TEXT DEFAULT '' , Attendee_Home_Zipcode  TEXT DEFAULT '' , CheckedInDate  NVARCHAR , Event_Id  NVARCHAR , Attendee_Badge_Name  NVARCHAR DEFAULT '' , BadgeId  TEXT DEFAULT '' , Badge_Parent_Id  TEXT, Reason  TEXT DEFAULT '' , Item_Id  NVARCHAR , Item_Name  VARCHAR , Item_Type_Id  NVARCHAR , Item_Pool_Id  NVARCHAR , Tikcet_Number  NVARCHAR , isCheckin  TEXT DEFAULT '' , Ticket_Seat_Number  VARCHAR , Order_Id  NVARCHAR , Order_Id_Number NVARCHAR , Order_Item_Id  NVARCHAR , buyer_id  NVARCHAR , note NVARCHAR , tag NVARCHAR , Badge_Label__c NVARCHAR ,item_pool_name NVARCHAR ,item_type_name NVARCHAR ,item_badgable NVARCHAR ,Parent_Id NVARCHAR ,custom_barcode TEXT DEFAULT '',print_status TEXT DEFAULT '',unique_number TEXT DEFAULT '',scan_id TEXT DEFAULT '', _id integer primary key autoincrement )";
    private static final String CREATE_ORGANAIZER_PAY_INFO = "CREATE TABLE IF NOT EXISTS Organaizer_Info(_id integer primary key autoincrement, organizer_id TEXT, organizer_name TEXT,PG_Authorization_Key__c TEXT,PG_Credit_Card_Overwrite__c TEXT,PG_Authorization_Card_Type__c TEXT,PG_Authorization_CC_Last_four_Digit__c TEXT)";
    private static final String CREATE_PAYMENT_GATEWAYS = "CREATE TABLE IF NOT EXISTS PaymentGateways (_id integer primary key autoincrement,payment_type_id TEXT, payment_type_name TEXT, Online_Flag__c TEXT, Adoptive_Type__c TEXT, payment_type_display TEXT,currency_id TEXT,paygatewaytype_id TEXT)";
    private static final String CREATE_PAY_GATEWAY_KEYS = "CREATE TABLE IF NOT EXISTS Pay_Gateway_Keys (_id integer primary key autoincrement,Pay_Gateway_Id__c TEXT, PGateway_Type__c TEXT, BLN_GN_User__c TEXT, Company__c TEXT, Paygateway_name__c TEXT,PayGateway_label__c TEXT, PG_Pass_Secret__c TEXT, PG_Signature__c TEXT, PG_User_Key__c TEXT, Service_Fee__c TEXT, PP_Payment_Type__c TEXT, PG_Email_Id__c TEXT, PP_Fee_Payer__c TEXT , citrus_param__c TEXT )";
    private static final String CREATE_TABLE_COUNTRY = "CREATE TABLE IF NOT EXISTS Country(_id integer primary key autoincrement , CountryId TEXT, Country_Long_Name TEXT,  currency_id TEXT, currency_name TEXT, currency_full_name TEXT, currencey_symbol TEXT, Country_Short_Name TEXT  )";
    private static final String CREATE_TABLE_CURRENCY = "CREATE TABLE IF NOT EXISTS Currency(_id integer primary key autoincrement , currency_id TEXT, currency_name TEXT, currency_full_name TEXT, currencey_symbol TEXT )";

    private static final String CREATE_TABLE_SFDCD_DETAILS = "CREATE TABLE IF NOT EXISTS sfdcd_details(_id integer primary key autoincrement , user_id TEXT , instance_url TEXT, access_token TEXT, refresh_token TEXT, token_type TEXT, token_time TEXT)";
    private static final String CREATE_TABLE_STATE = "CREATE TABLE IF NOT EXISTS State(_id integer primary key autoincrement , CountryId TEXT ,  State_Long_Name TEXT '' ,  StateId TEXT ,  State_Short_Name TEXT '' )";
    private static final String CREATE_TABLE_USER = "CREATE TABLE IF NOT EXISTS USER_PROFILE(_id integer primary key autoincrement , UserID TEXT ,  GNUserID TEXT ,  CompanyId TEXT ,  ImageURL TEXT ,  ImageBlob LONGBLOB DEFAULT NULL , FirstName TEXT ,  LastName TEXT ,  Company TEXT ,  EmailID TEXT ,  PhoneNumber TEXT , City TEXT ,  State TEXT , Password TEXT ,  UserRole TEXT , designation TEXT ,  Event_Id TEXT ,  Order_Id TEXT ,  Order_Status TEXT ,  LastRefreshedDate TEXT, Country TEXT )";
    private static final String CREATE_TICKET_TAG_TABLE = "CREATE TABLE IF NOT EXISTS ticket_tag(_id integer primary key autoincrement, tag_id TEXT, event_id TEXT, ticket_id TEXT, ticket_name TEXT, tag_name TEXT )";
    private static final String CREATE_SCANNED_TICKETS = "CREATE TABLE IF NOT EXISTS ScannedTickets(_id integer primary key autoincrement, Id TEXT,Event_Id TEXT,GNUser_Id TEXT,Item_Pool_Id Text,Status TEXT,ScanSwitch TEXT DEFAULT 'false',Scan_Group_Id TEXT)";
    private static final String CREATE_SESSION_ATTENDEES = "CREATE TABLE IF NOT EXISTS SessionAttendees(_id integer primary key autoincrement,session_attendee_id TEXT,session_item_pool_id TEXT,session_event_id TEXT,session_attendee_ticket_id TEXT,session_attendee_first_name TEXT,session_attendee_last_name TEXT,session_attendee_email_id TEXT,session_attendee_company TEXT,session_attendee_scantime TEXT,session_attendee_checkin_status TEXT DEFAULT 'false',"
            + DBFeilds.SESSION_BADGE_LABEL + " TEXT , " + DBFeilds.SESSION_CUSTOM_BARCODE + " TEXT , "
            + DBFeilds.SESSION_MOBILE + " TEXT ," + DBFeilds.SESSION_TKT_JOB_TITLE + " TEXT , "
            + DBFeilds.SESSION_ITEM_TYPE_NAME + " TEXT , "
            + DBFeilds.SESSION_GROUP_ID + " TEXT , "
            + DBFeilds.SESSION_GROUP_NAME + " TEXT , "
            + DBFeilds.SESSION_ITEM_TYPE_ID + " TEXT )";


    private static final String CREATE_OFFLINE_SCANS = "CREATE TABLE IF NOT EXISTS " + DBFeilds.TABLE_OFFLINE_SCANS + "("
            + "_id integer primary key autoincrement , "
            + DBFeilds.OFFLINE_ITEM_POOL_ID + " TEXT , "
            + DBFeilds.OFFLINE_BADGE_ID + " TEXT , "
            + DBFeilds.OFFLINE_EVENT_ID + " TEXT , "
            + DBFeilds.OFFLINE_USER_ID + " TEXT , "
            + DBFeilds.OFFLINE_BADGE_STATUS + " TEXT , "
            + DBFeilds.OFFLINE_CHECKIN_STATUS + " TEXT , "
            + DBFeilds.OFFLINE_ERROR_MSG + " TEXT , "
            + DBFeilds.OFFLINE_ATTENDEE_NAME + " TEXT, "
            + DBFeilds.OFFLINE_GROUP_ID + " TEXT, "
            + DBFeilds.OFFLINE_SCANDEVICE_MODE + " TEXT, "
            + DBFeilds.OFFLINE_SCAN_TIME + " TEXT )";

    private static final String CREATE_SEMINAR_AGENDA = "CREATE TABLE IF NOT EXISTS " + DBFeilds.TABLE_SEMINA_AGENDA + "("
            + "_id integer primary key autoincrement , "
            + DBFeilds.SEMINAR_ITEM_POOL_ID + " TEXT,"
            + DBFeilds.SEMINAR_AGENDA_NAME + " TEXT,"
            + DBFeilds.SEMINAR_START_DATE_TIME + " TEXT,"
            + DBFeilds.SEMINAR_END_DATE_TIME + " TEXT,"
            + DBFeilds.SEMINAR_ROOM_NAME + " TEXT,"
            + DBFeilds.SEMINAR_ROOM_NUMBER + " TEXT,"
            + DBFeilds.SEMINAR_AGENDA_DESC + " TEXT)";

    private static final String CREATE_SCANNED_TICKETS_GROUP = "CREATE TABLE IF NOT EXISTS " + DBFeilds.TABLE_SCANNEDTICKETS_GROUP + " ("
            + "_id integer primary key autoincrement , "
            + DBFeilds.GROUP_ID + " TEXT,"
            + DBFeilds.GROUP_NAME + " TEXT,"
            + DBFeilds.GROUP_EVENT_ID + " TEXT,"
            + DBFeilds.GROUP_SCAN_SWITCH + " TEXT DEFAULT 'false',"
            + DBFeilds.GROUP_PRODUCT_TYPE + " TEXT )";

    private static final String CREATE_TSTATUS = "CREATE TABLE IF NOT EXISTS " + DBFeilds.TABLE_TSTATUS + " ("
            + "_id integer primary key autoincrement , "
            + DBFeilds.T_EVENT_ID + " TEXT,"
            + DBFeilds.T_TICKET_ID + " TEXT,"
            + DBFeilds.T_CHECKIN_STATUS + " TEXT,"
            + DBFeilds.T_GROUP_ID + " TEXT,"
            + DBFeilds.T_GROUP_NAME + " TEXT,"
            + DBFeilds.T_SESSION_ITEM_POOL + " TEXT,"
            + DBFeilds.T_SESSION_USER + " TEXT,"
            + DBFeilds.T_SCAN_TIME + " TEXT,"
            + DBFeilds.T_USER_ID + " TEXT,"
            + DBFeilds.T_GNUSER_ID + " TEXT,"
            + DBFeilds.T_ISLATEST + " TEXT)";

    private static final String CREATE_ORDERTICKETDETAILS="CREATE TABLE IF NOT EXISTS OrderTicketDetails (Attendee_Id TEXT ,Age__c   TEXT DEFAULT '' , BBB_Number__c   TEXT DEFAULT '' , Billing_Address__c   TEXT DEFAULT '' , " +
            "Biography__c   TEXT DEFAULT '' , BLN_Company__c   TEXT DEFAULT '' , Blog_URL__c   TEXT DEFAULT '' , Blogger__c   TEXT DEFAULT '' , Business_Structure__c   TEXT DEFAULT '' , CageCode__c   TEXT DEFAULT '' , Description__c   TEXT DEFAULT '' , Company_Logo__c   TEXT DEFAULT '' " +
            ", Website_URL__c   TEXT DEFAULT '' , DBA__c   TEXT DEFAULT '' , distribution_Country__c   TEXT DEFAULT '' , DOB__c   TEXT DEFAULT '' , Duns_Number__c   TEXT DEFAULT '' , Email__c   TEXT DEFAULT '' , Established_Date__c   TEXT DEFAULT '' " +
            ", Ethnicity__c   TEXT DEFAULT '' , Exceptional_Keywords__c   TEXT DEFAULT '' " +
            ", SpeakerFacebookId   TEXT DEFAULT '' , FaxNumber__c   TEXT DEFAULT '' , First_Name__c   TEXT DEFAULT '' , FourSquareId__c   TEXT DEFAULT '' , " +
            "Full_Name__c   TEXT DEFAULT '' , Gender__c   TEXT DEFAULT '' , Geographical_Region__c   TEXT DEFAULT '' ,Attendee_Company_Id   TEXT DEFAULT '' ,"+
            "GSA_Schedule__c   TEXT DEFAULT ''  ,Address1__c  TEXT DEFAULT '' , Address2__c  TEXT DEFAULT '' , Work_Phone__c TEXT DEFAULT '',Attendee_City  TEXT DEFAULT '' , " +
            "Attendee_State  TEXT DEFAULT '' , Attendee_Country  TEXT DEFAULT '' , Attendee_Zipcode  TEXT DEFAULT '' , Attendee_Home_Phone  TEXT DEFAULT '' ," +
            " Attendee_Home_Address_1  TEXT DEFAULT '' , Attendee_Home_Address_2  TEXT DEFAULT '' , Attendee_Home_City  TEXT DEFAULT '' , " +
            "Attendee_Home_State  TEXT DEFAULT '' , Attendee_Home_Country  TEXT DEFAULT '' , Attendee_Home_Zipcode  TEXT DEFAULT '' ," +
            " Attendee_Billing_Address_1  TEXT DEFAULT '' , Attendee_Billing_Address_2  TEXT DEFAULT '' , Attendee_Billing_City  TEXT DEFAULT '' , " +
            " Attendee_Billing_State  TEXT DEFAULT '' , Attendee_Billing_Country  TEXT DEFAULT '' , Attendee_Billing_Zipcode  TEXT DEFAULT '' ," +
            " Instagram__c   TEXT DEFAULT '' , IP_Address__c   TEXT DEFAULT '' , Keywords__c   TEXT DEFAULT '' , " +
            "Last_Name__c   TEXT DEFAULT '' , LinkedInId__c   TEXT DEFAULT '' , Manufactures_Country__c   TEXT DEFAULT '' , Mobile__c   TEXT DEFAULT '' ," +
            "Number_Of_Employees__c   TEXT DEFAULT '' , Outside_Facilities__c   TEXT DEFAULT '' , Prefix__c   TEXT DEFAULT '' ," +
            " Primary_Business_Category__c   TEXT DEFAULT '' , References1__c   TEXT DEFAULT '' , References2__c   TEXT DEFAULT '' ," +
            " Revenue__c   TEXT DEFAULT '' ,  ScopeOfWork1__c   TEXT DEFAULT '' , ScopeOfWork2__c   TEXT DEFAULT '' ," +
            " Secondary_Business_Category__c   TEXT DEFAULT '' , Secondary_email__c   TEXT DEFAULT '' ," +
            " Shipping_Address__c   TEXT DEFAULT '' , Skype__c   TEXT DEFAULT '' , Snapchat__c   TEXT DEFAULT '' ," +
            " SpeakerLinkedInId   TEXT DEFAULT '' , SpeakerTwitterId   TEXT DEFAULT '' , SpeakerBlogger   TEXT DEFAULT '' ," +
            " Suffix__c   TEXT DEFAULT '' , Tax_Id__c   TEXT DEFAULT '' , TKT_Company__c   TEXT DEFAULT '' , Title__c   TEXT DEFAULT '' ," +
            " TwitterId__c   TEXT DEFAULT '' , User_Pic__c   TEXT DEFAULT '' , SpeakerVideo   TEXT DEFAULT '' , Wechat__c   TEXT DEFAULT '' ," +
            " WhatsApp__c   TEXT DEFAULT '' ,  Year_in_business__c   TEXT DEFAULT '' , " + " Phone__c TEXT DEFAULT '' , "+
            " Home_Phone__c TEXT DEFAULT '' , "+" Name TEXT DEFAULT '' , "+
            " Location_Room__c TEXT DEFAULT '' , "+" Table_Number__c TEXT DEFAULT '' , "+
            " CheckedInDate  NVARCHAR , Event_Id  NVARCHAR , Attendee_Badge_Name  NVARCHAR DEFAULT '' , BadgeId  TEXT DEFAULT '' ," +
            " Badge_Parent_Id  TEXT, Reason  TEXT DEFAULT '' , Item_Id  NVARCHAR , Item_Name  VARCHAR , Item_Type_Id  NVARCHAR , " +
            "Item_Pool_Id  NVARCHAR , Tikcet_Number  NVARCHAR , isCheckin  TEXT DEFAULT '' , Ticket_Seat_Number  VARCHAR ," +
            " Order_Id  NVARCHAR , Order_Id_Number NVARCHAR , Order_Item_Id  NVARCHAR , buyer_id  NVARCHAR , note NVARCHAR , tag NVARCHAR ," +
            " Badge_Label__c NVARCHAR ,item_pool_name NVARCHAR ,item_type_name NVARCHAR ,item_badgable NVARCHAR ,Parent_Id NVARCHAR ," +
            "custom_barcode TEXT DEFAULT '',print_status TEXT DEFAULT '',unique_number TEXT DEFAULT '',scan_id TEXT DEFAULT ''," +
            "Ticket_Status__c TEXT DEFAULT '',RSVP__c TEXT DEFAULT '',List_Type__c TEXT DEFAULT '',List_Description__c TEXT DEFAULT ''," +
            "List_Code__c TEXT DEFAULT ''," + " _id integer primary key autoincrement )";


    private static final String CREATE_ORDERTICKETFIELDTYPES="CREATE TABLE IF NOT EXISTS OrderTicketFeildTypes (Age__c   TEXT DEFAULT '' , BBB_Number__c   TEXT DEFAULT '' , Billing_Address__c   TEXT DEFAULT '' , " +
            "Biography__c   TEXT DEFAULT '' , BLN_Company__c   TEXT DEFAULT '' , Blog_URL__c   TEXT DEFAULT '' , Blogger__c   TEXT DEFAULT '' , Business_Structure__c   TEXT DEFAULT '' , CageCode__c   TEXT DEFAULT '' , Description__c   TEXT DEFAULT '' , Company_Logo__c   TEXT DEFAULT '' " +
            ", Website_URL__c   TEXT DEFAULT '' , DBA__c   TEXT DEFAULT '' , distribution_Country__c   TEXT DEFAULT '' , DOB__c   TEXT DEFAULT '' , Duns_Number__c   TEXT DEFAULT '' , Email__c   TEXT DEFAULT '' , Established_Date__c   TEXT DEFAULT '' " +
            ", Ethnicity__c   TEXT DEFAULT '' , Exceptional_Keywords__c   TEXT DEFAULT '' " +
            ", SpeakerFacebookId   TEXT DEFAULT '' , FaxNumber__c   TEXT DEFAULT '' , First_Name__c   TEXT DEFAULT '' , FourSquareId__c   TEXT DEFAULT '' , " +
            "Full_Name__c   TEXT DEFAULT '' , Gender__c   TEXT DEFAULT '' , Geographical_Region__c   TEXT DEFAULT '' ,Attendee_Company_Id   TEXT DEFAULT '' ,"+
            "GSA_Schedule__c   TEXT DEFAULT ''  ,Address1__c  TEXT DEFAULT '' , Address2__c  TEXT DEFAULT '' , Work_Phone__c TEXT DEFAULT '',Attendee_City  TEXT DEFAULT '' , " +
            "Attendee_State  TEXT DEFAULT '' , Attendee_Country  TEXT DEFAULT '' , Attendee_Zipcode  TEXT DEFAULT '' , Attendee_Home_Phone  TEXT DEFAULT '' ," +
            " Attendee_Home_Address_1  TEXT DEFAULT '' , Attendee_Home_Address_2  TEXT DEFAULT '' , Attendee_Home_City  TEXT DEFAULT '' , " +
            " Attendee_Home_State  TEXT DEFAULT '' , Attendee_Home_Country  TEXT DEFAULT '' , Attendee_Home_Zipcode  TEXT DEFAULT '' ," +
            " Instagram__c   TEXT DEFAULT '' , IP_Address__c   TEXT DEFAULT '' , Keywords__c   TEXT DEFAULT '' , " +
            "Last_Name__c   TEXT DEFAULT '' , LinkedInId__c   TEXT DEFAULT '' , Manufactures_Country__c   TEXT DEFAULT '' , Mobile__c   TEXT DEFAULT '' ," +
            "Number_Of_Employees__c   TEXT DEFAULT '' , Outside_Facilities__c   TEXT DEFAULT '' , Prefix__c   TEXT DEFAULT '' ," +
            " Primary_Business_Category__c   TEXT DEFAULT '' , References1__c   TEXT DEFAULT '' , References2__c   TEXT DEFAULT '' ," +
            " Revenue__c   TEXT DEFAULT '' ,  ScopeOfWork1__c   TEXT DEFAULT '' , ScopeOfWork2__c   TEXT DEFAULT '' ," +
            " Secondary_Business_Category__c   TEXT DEFAULT '' , Secondary_email__c   TEXT DEFAULT '' ," +
            " Shipping_Address__c   TEXT DEFAULT '' , Skype__c   TEXT DEFAULT '' , Snapchat__c   TEXT DEFAULT '' ," +
            " SpeakerLinkedInId   TEXT DEFAULT '' , SpeakerTwitterId   TEXT DEFAULT '' , SpeakerBlogger   TEXT DEFAULT '' ," +
            " Suffix__c   TEXT DEFAULT '' , Tax_Id__c   TEXT DEFAULT '' , TKT_Company__c   TEXT DEFAULT '' , Title__c   TEXT DEFAULT '' ," +
            " TwitterId__c   TEXT DEFAULT '' , User_Pic__c   TEXT DEFAULT '' , SpeakerVideo   TEXT DEFAULT '' , Wechat__c   TEXT DEFAULT '' ," +
            " WhatsApp__c   TEXT DEFAULT '' ,  Year_in_business__c   TEXT DEFAULT '' , " + " Phone__c TEXT DEFAULT '' , "+
            " Home_Phone__c TEXT DEFAULT '' , "+" Name TEXT DEFAULT '' , "+
            " Location_Room__c TEXT DEFAULT '' , "+" Table_Number__c TEXT DEFAULT '' , "+
            " CheckedInDate  NVARCHAR , Event_Id  NVARCHAR , Attendee_Badge_Name  NVARCHAR DEFAULT '' , BadgeId  TEXT DEFAULT '' ," +
            " Badge_Parent_Id  TEXT, Reason  TEXT DEFAULT '' , Item_Id  NVARCHAR , Item_Name  VARCHAR , Item_Type_Id  NVARCHAR , " +
            "Item_Pool_Id  NVARCHAR , Tikcet_Number  NVARCHAR , isCheckin  TEXT DEFAULT '' , Ticket_Seat_Number  VARCHAR ," +
            " Order_Id  NVARCHAR , Order_Id_Number NVARCHAR , Order_Item_Id  NVARCHAR , buyer_id  NVARCHAR , note NVARCHAR , tag NVARCHAR ," +
            " Badge_Label__c NVARCHAR ,item_pool_name NVARCHAR ,item_type_name NVARCHAR ,item_badgable NVARCHAR ,Parent_Id NVARCHAR ," +
            "custom_barcode TEXT DEFAULT '',print_status TEXT DEFAULT '',unique_number TEXT DEFAULT '',scan_id TEXT DEFAULT ''," +
            "Ticket_Status__c TEXT DEFAULT '',RSVP__c TEXT DEFAULT '',List_Type__c TEXT DEFAULT '',List_Description__c TEXT DEFAULT ''," +
            "List_Code__c TEXT DEFAULT ''," + " _id integer primary key autoincrement )";
    private static final String TABLE_SALES_REFUND;
    //private final String TAG;
    Calendar cal;
    private final Context context;
    private final DatabaseHelper dataHelper;
    private ContentValues datavalues;
    private SQLiteDatabase db;
    SimpleDateFormat format;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private ContentValues datavalues;
        private SQLiteDatabase db;
        private Context context;

        DatabaseHelper(Context context) {
            super(context, DBFeilds.DATABASE_NAME, null, DBFeilds.DATABASE_VERSION);
            this.context = context;
        }

        private boolean checkDataBase() {
            return new File("data/data/com.globalnest.scanattendee/databases/ScanAttendee").exists();
        }

        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DataBase.CREATE_TABLE_SFDCD_DETAILS);
                db.execSQL(DataBase.CREATE_TABLE_USER);
                db.execSQL(DataBase.CREATE_EVENTDETAILS);
                db.execSQL(DataBase.CREATE_TABLE_COUNTRY);
                db.execSQL(DataBase.CREATE_TABLE_STATE);
                db.execSQL(DataBase.CREATE_ADDEDTICKETS);
                db.execSQL(DataBase.CREATE_ITEMPOOL);
                db.execSQL(DataBase.CREATE_ITEMTYPE);
                db.execSQL(DataBase.CREATE_ORDERTICKETDETAILS);
                //db.execSQL(DataBase.CREATE_ORDERTICKETFIELDTYPES);
                db.execSQL(DataBase.CREATE_ORDERDETAILS);
                db.execSQL(DataBase.CREATE_ORDER_PAYMENT_ITEMS);
                db.execSQL(DataBase.CREATE_ORDERITEMTABLE);
                // db.execSQL(DataBase.CREATE_BADGETEMPLATE);
                db.execSQL(DataBase.CREATE_PAYMENT_GATEWAYS);
                db.execSQL(DataBase.CREATE_EVENT_PAYMENT_SETTINGS);
                db.execSQL(DataBase.CREATE_ADDRESS);
                db.execSQL(DataBase.CREATE_TICKET_TAG_TABLE);
                db.execSQL(DataBase.CREATE_PAY_GATEWAY_KEYS);
                db.execSQL(DataBase.CREATE_ORGANAIZER_PAY_INFO);
                db.execSQL(DataBase.CREATE_EVENTPRICING);
                db.execSQL(DataBase.CREATE_BADGE_TEMPLATE_NEW);
                db.execSQL(DataBase.CREATE_BADGE_TEMPLATE_LAYERS);
                db.execSQL(DataBase.CREATE_ITEM_REG_SETTINGS);
                db.execSQL(DataBase.CREATE_SCANNED_TICKETS);
                db.execSQL(DataBase.CREATE_TABLE_CURRENCY);
                db.execSQL(DataBase.CREATE_SESSION_ATTENDEES);
                db.execSQL(DataBase.CREATE_OFFLINE_SCANS);
                db.execSQL(DataBase.CREATE_SEMINAR_AGENDA);
                db.execSQL(DataBase.CREATE_SCANNED_TICKETS_GROUP);
                db.execSQL(DataBase.CREATE_TSTATUS);
                db.execSQL(DataBase.CREATE_HIDEITEMS);
                db.execSQL(DataBase.CREATE_PICKLISTVALUES);
                this.db = db;
                LoadCountriesAndStates();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < newVersion) {
        	  /* db.execSQL(DataBase.CREATE_SCANNED_TICKETS);
        	   db.execSQL(DataBase.CREATE_TABLE_CURRENCY);
        	   db.execSQL("ALTER TABLE.
        	    "+DBFeilds.TABLE_EVENT_DETAILS+" ADD COLUMN "+DBFeilds.EVENT_USER_ROLE+" TEXT");
        	   db.execSQL("ALTER TABLE "+DBFeilds.TABLE_EVENT_DETAILS+" ADD COLUMN "+DBFeilds.EVENT_SELECTED_COUNTRY_CODES+" TEXT");
        	   LoadCountriesAndStates();*/
                if (isDropAllTables(db)) {
                    onCreate(db);
                }

            }
        }


        public boolean isDropAllTables(SQLiteDatabase db) {
            try {
                db.execSQL("drop table if exists " + DBFeilds.TABLE_USER);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_ADDED_TICKETS);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_ITEM_POOL);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_EVENT_DETAILS);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_BADGE_TEMPLATE);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_ATTENDEE_DETAILS);
                //db.execSQL("drop table if exists " + DBFeilds.TABLE_ATTENDEE_FEILDTYPES);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_SALES_ORDER_ITEM);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_ITEM_TYPE);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_SALES_ORDER);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_ORDER_PAYMENT_ITEMS);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_SFDCD_DETAILS);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_PAYMENT_GATEWAYS);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_EVENT_PAYAMENT_SETTINGS);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_PAY_GATEWAY_KEYS);
                db.execSQL("drop table if exists " + DBFeilds.USER_COUNTRY);
                db.execSQL("drop table if exists " + DBFeilds.USER_STATE);
                // this.db.delete(DBFeilds.USER_COUNTRY, null, null);
                // this.db.delete(DBFeilds.USER_STATE, null, null);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_ORGANAIZER_INFO);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_BADGE_TEMPLATE_NEW);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_BADGE_LAYERS);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_ITEM_REG_SETTINGS);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_SCANNED_TICKETS);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_SESSION_ATTENDEES);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_OFFLINE_SCANS);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_SEMINA_AGENDA);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_SCANNEDTICKETS_GROUP);
                db.execSQL("drop table if exists " + DBFeilds.TABLE_TSTATUS);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private void LoadCountriesAndStates() {
            try {
                InputStream country_is = context.getAssets().open("country_json.txt");
                java.util.Scanner s = new java.util.Scanner(country_is).useDelimiter("\\A");
                String country_json = s.hasNext() ? s.next() : "";
                /*StringWriter writer = new StringWriter();
                IOUtils.copy(country_is, writer, Charset.defaultCharset());
                String country_json = writer.toString();*/
                // String country_json = IOUtils.toString(country_is, Charset.defaultCharset());
                country_is.close();
                CountryResposneObj country_res = new Gson().fromJson(country_json, CountryResposneObj.class);
                InputStream state_is = context.getAssets().open("states_json.txt");
                InputStreamReader state_is_reader = new InputStreamReader(state_is, "ISO-8859-1");
                java.util.Scanner state = new java.util.Scanner(state_is_reader).useDelimiter("\\A");
                String states_json = state.hasNext() ? state.next() : "";
                //String states_json = IOUtils.toString(state_is_reader);
                state_is.close();
                StateResponseObj states_res = new Gson().fromJson(states_json, StateResponseObj.class);
                // db.delete(DBFeilds.TABLE_COUNTRY, null, null);
                // db.delete(DBFeilds.TABLE_STATE, null, null);
                InsertCountry(country_res.countryList);
                InsertState(states_res.statesList);
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: handle exception
            }

        }

        private void InsertCountry(List<Country> CountriesList) {
            try {
                for (Country countryData : CountriesList) {
                    this.datavalues = new ContentValues();
                    this.datavalues.put(DBFeilds.STATE_COUNTRY_ID, countryData.Id);
                    this.datavalues.put(DBFeilds.COUNTRY_LONG_NAME, countryData.Long_Name__c);
                    this.datavalues.put(DBFeilds.CURRENCY_ID, countryData.Currency_code__c);
                    this.datavalues.put(DBFeilds.COUNTRY_SHORT_NAME, countryData.Short_Name__c);
                    // this.datavalues.put(DBFeilds.CURRENCY_NAME, countryData.currencym.Name);
                    // this.datavalues.put(DBFeilds.CURRENCY_FULL_NAME, countryData.currencym.Currency_Name__c);
                    // this.datavalues.put(DBFeilds.CURRENCY_SYMBOL, countryData.currencym.Currency_Symbol__c);
                    if (isRecordExists(DBFeilds.USER_COUNTRY, " where CountryId='" + countryData.Id + "'")) {
                        db.update(DBFeilds.TABLE_COUNTRY, this.datavalues, "CountryId='" + countryData.Id + "'", null);
                    } else {
                        db.insert(DBFeilds.TABLE_COUNTRY, null, this.datavalues);
                    }
                    /*for (States state : countryData.States) {
                        InsertState(state);
                    }*/
                }
                //Log.i("----Country Data Inserted----", "Success");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void InsertState(List<States> states_list) {

            try {
                for (States states : states_list) {
                    this.datavalues = new ContentValues();
                    this.datavalues.put(DBFeilds.STATE_ID, states.Id);
                    this.datavalues.put(DBFeilds.STATE_SHORT_NAME, states.Short_Name__c);
                    this.datavalues.put(DBFeilds.STATE_COUNTRY_ID, states.Country__c);
                    this.datavalues.put(DBFeilds.STATE_LONG_NAME, states.Long_Name__c);
                    if (isRecordExists(DBFeilds.USER_STATE, " where StateId='" + states.Id + "'")) {
                        this.db.update(DBFeilds.USER_STATE, this.datavalues, "StateId='" + states.Id + "'", null);
                    } else {
                        this.db.insert(DBFeilds.USER_STATE, null, this.datavalues);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private boolean isRecordExists(String tableName, String whereCondition) {
            boolean isuserindb = false;
            try {
                Cursor c = this.db.rawQuery("select * from " + tableName + whereCondition, null);
                if (c != null) {
                    if (c.getCount() == 0) {
                        isuserindb = false;
                    } else {
                        isuserindb = true;
                    }
                }
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isuserindb;
        }
    }

    static {
        TABLE_SALES_REFUND = DBFeilds.TABLE_SALES_REFUND;

    }

    public DataBase(Context ctx) {
        // this.TAG = "DATABASE CLASS";
        this.format = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm", Locale.US);
        this.cal = Calendar.getInstance();
        this.context = ctx;
        this.dataHelper = new DatabaseHelper(this.context);
    }

    public DataBase open() throws SQLException {
        try {
            if (!this.dataHelper.checkDataBase()) {
                this.db = this.dataHelper.getReadableDatabase();
                this.db = this.dataHelper.getWritableDatabase();
            }
            this.db = this.dataHelper.getReadableDatabase();
            this.db = this.dataHelper.getWritableDatabase();
            //Log.i("----------------------------DB Is Open-----------------------", ":" + this.db.isOpen());
        } catch (Exception e) {
        }
        return this;
    }

    public boolean isDBExist() {
        return this.dataHelper.checkDataBase();
    }

    public void close() {
        if (this.db != null && this.db.isOpen()) {
            this.db.close();
        }
    }

    public boolean isOpen() {
        return this.db.isOpen();
    }

    public boolean isUserExists(String tableName, String whereCondition) {
        boolean isuserindb = false;
        try {
            Cursor c = this.db.rawQuery("select * from " + tableName + whereCondition, null);
            if (c != null) {
                if (c.getCount() == 0) {
                    isuserindb = false;
                } else {
                    isuserindb = true;
                }
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isuserindb;
    }

    public boolean isRecordExists(String tableName, String whereCondition) {
        boolean isuserindb = false;
        try {
            Cursor c = this.db.rawQuery("select * from " + tableName + whereCondition, null);
            if (c.moveToFirst()) {
                isuserindb = true;
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isuserindb;
    }

    public boolean isDBDeleted() {
        this.db.beginTransaction();
        try {
            this.db.delete(DBFeilds.TABLE_USER, null, null);
            this.db.delete(DBFeilds.TABLE_ADDED_TICKETS, null, null);
            this.db.delete(DBFeilds.TABLE_ITEM_POOL, null, null);
            this.db.delete(DBFeilds.TABLE_EVENT_DETAILS, null, null);
            //this.db.delete(DBFeilds.TABLE_BADGE_TEMPLATE, null, null);
            this.db.delete(DBFeilds.TABLE_ATTENDEE_DETAILS, null, null);
            //this.db.delete(DBFeilds.TABLE_ATTENDEE_FEILDTYPES, null, null);
            this.db.delete(DBFeilds.TABLE_SALES_ORDER_ITEM, null, null);
            this.db.delete(DBFeilds.TABLE_ORDER_PAYMENT_ITEMS, null, null);
            this.db.delete(DBFeilds.TABLE_ITEM_TYPE, null, null);
            this.db.delete(DBFeilds.TABLE_SALES_ORDER, null, null);
            this.db.delete(DBFeilds.TABLE_SFDCD_DETAILS, null, null);
            this.db.delete(DBFeilds.TABLE_PAYMENT_GATEWAYS, null, null);
            this.db.delete(DBFeilds.TABLE_EVENT_PAYAMENT_SETTINGS, null, null);
            this.db.delete(DBFeilds.TABLE_PAY_GATEWAY_KEYS, null, null);
            // this.db.delete(DBFeilds.USER_COUNTRY, null, null);
            // this.db.delete(DBFeilds.USER_STATE, null, null);
            this.db.delete(DBFeilds.TABLE_ORGANAIZER_INFO, null, null);
            this.db.delete(DBFeilds.TABLE_BADGE_TEMPLATE_NEW, null, null);
            this.db.delete(DBFeilds.TABLE_BADGE_LAYERS, null, null);
            this.db.delete(DBFeilds.TABLE_ITEM_REG_SETTINGS, null, null);
            this.db.delete(DBFeilds.TABLE_SCANNED_TICKETS, null, null);
            this.db.delete(DBFeilds.TABLE_SESSION_ATTENDEES, null, null);
            //this.db.delete(DBFeilds.TABLE_OFFLINE_SCANS, null, null);
            this.db.delete(DBFeilds.TABLE_SEMINA_AGENDA, null, null);
            this.db.delete(DBFeilds.TABLE_CURRENCY, null, null);
            this.db.delete(DBFeilds.TABLE_SCANNEDTICKETS_GROUP, null, null);
            this.db.delete(DBFeilds.TABLE_TSTATUS, null, null);
            this.db.delete(DBFeilds.TABLE_HIDEITEMS, null, null);
            this.db.delete(DBFeilds.TABLE_PICKLISTVALUES, null, null);
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
            this.db.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void InsertAndUpdateSFDCDDETAILS(SFDCDetails sfdcdetails) {
        try {
            this.datavalues = new ContentValues();
            this.datavalues.put(DBFeilds.INSTANCE_URL, sfdcdetails.instance_url);
            this.datavalues.put(DBFeilds.ACCESS_TOKEN, sfdcdetails.access_token);
            this.datavalues.put(DBFeilds.ACCESS_TOKEN_TIME, String.valueOf(new Date().getTime()));
            if(!sfdcdetails.refresh_token.isEmpty())
                this.datavalues.put(DBFeilds.REFRESH_TOKEN, sfdcdetails.refresh_token);
            this.datavalues.put(DBFeilds.ACCESS_TOKEN_TYPE, sfdcdetails.token_type);
            this.datavalues.put(DBFeilds.USER_ID, sfdcdetails.user_id);
            if (isUserExists(DBFeilds.TABLE_SFDCD_DETAILS, " where user_id = '" + sfdcdetails.user_id + "'")) {
                this.db.update(DBFeilds.TABLE_SFDCD_DETAILS, this.datavalues, "user_id = '" + sfdcdetails.user_id + "'", null);
                //Log.i("DATABASE", "SFDCD Details Updated Successfully");
                return;
            }else
                this.db.insert(DBFeilds.TABLE_SFDCD_DETAILS, null, this.datavalues);
            //Log.i("DATABASE", "SFDCD Details Inserted Successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SFDCDetails getSFDCDDETAILS() {
        SFDCDetails sfdcddetails = new SFDCDetails();
        try {
            //Log.i("DATABASE", "get SFDCD Details");
            Cursor c = this.db.rawQuery("Select * from sfdcd_details", null);
            //Log.i("---User Profile Found---", ":" + c.getCount());
            if (c.getCount() > 1) {
                this.db.delete(DBFeilds.TABLE_SFDCD_DETAILS, " access_token=''", null);
                this.db.delete(DBFeilds.TABLE_SFDCD_DETAILS, " user_id=''", null);
            }
            //Log.i("---User Profile Found---", ":" + c.getCount());
            if (c.getCount() != 0) {
                c.moveToFirst();
                sfdcddetails.instance_url = c.getString(c.getColumnIndex(DBFeilds.INSTANCE_URL));
                sfdcddetails.access_token = c.getString(c.getColumnIndex(DBFeilds.ACCESS_TOKEN));
                sfdcddetails.refresh_token = c.getString(c.getColumnIndex(DBFeilds.REFRESH_TOKEN));
                sfdcddetails.user_id = c.getString(c.getColumnIndex(DBFeilds.USER_ID));
                sfdcddetails.token_type = c.getString(c.getColumnIndex(DBFeilds.ACCESS_TOKEN_TYPE));
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sfdcddetails;
    }

    public void InsertAndUpdateUser(UserObjects user) {
        try {
            //Log.i("DataBase", " Userid=" + user.Userid);
            //Log.i("DataBase", " First_Name__c=" + user.Profile.First_Name__c);
            this.datavalues = new ContentValues();
            if (isUserExists(DBFeilds.TABLE_USER, " where GNUserID = '" + user.Profile.Id + "'")) {
                this.datavalues.put(DBFeilds.USER_IMAGE_URL, Util.NullChecker(user.profileimage));
                this.datavalues.put(DBFeilds.USER_USER_ROLE, ITransaction.LOGGEDIN_USER);
                this.datavalues.put(DBFeilds.USER_FIRST_NAME, user.Profile.First_Name__c);
                this.datavalues.put(DBFeilds.USER_LAST_NAME, user.Profile.Last_Name__c);
                this.datavalues.put(DBFeilds.USER_EMAIL_ID, user.Profile.Email__c);
                if (Util.NullChecker(user.Profile.Default_Company_ID__r.Name).contains("a0w")) {
                    this.datavalues.put(DBFeilds.USER_COMPANY, ITransaction.EMPTY_STRING);
                } else {
                    this.datavalues.put(DBFeilds.USER_COMPANY, Util.NullChecker(user.Profile.Default_Company_ID__r.Name));
                }
                this.datavalues.put(DBFeilds.USER_COMPANY_ID, Util.NullChecker(user.Profile.Default_Company_ID__c));
                this.datavalues.put(DBFeilds.USER_PHONE_NUMBER, Util.NullChecker(user.Profile.Mobile__c));
                this.datavalues.put(DBFeilds.USER_CITY, Util.NullChecker(user.profileCity));
                this.datavalues.put(DBFeilds.USER_DESIGNATION, Util.NullChecker(user.designation));
                if (Util.NullChecker(user.profilecountry).isEmpty()) {
                    this.datavalues.put(DBFeilds.USER_COUNTRY, Util.NullChecker(ITransaction.EMPTY_STRING));
                } else {
                    this.datavalues.put(DBFeilds.USER_COUNTRY, Util.NullChecker(user.profilecountry));
                }
                if (Util.NullChecker(user.profilestate).isEmpty()) {
                    this.datavalues.put(DBFeilds.USER_STATE, ITransaction.EMPTY_STRING);
                } else {
                    this.datavalues.put(DBFeilds.USER_STATE, Util.NullChecker(user.profilestate));
                }
                this.db.update(DBFeilds.TABLE_USER, this.datavalues, "GNUserID='" + user.Profile.Id + "'", null);
                //Log.i("DATABASE CLASS", "USER DETAILS UPDATED SUCCESSFULLY");
                return;
            }
            //Log.i("DataBase", " Userid=" + user.Userid);
            //Log.i("DataBase", " First_Name__c=" + user.Profile.First_Name__c);
            this.datavalues.put(DBFeilds.USER_PASSWORD, ITransaction.EMPTY_STRING);
            this.datavalues.put(DBFeilds.USER_GNUSER_ID, user.Profile.Id);
            this.datavalues.put(DBFeilds.USER_USERID, user.Userid);
            this.datavalues.put(DBFeilds.USER_FIRST_NAME, user.Profile.First_Name__c);
            this.datavalues.put(DBFeilds.USER_LAST_NAME, user.Profile.Last_Name__c);
            this.datavalues.put(DBFeilds.USER_EMAIL_ID, user.Profile.Email__c);
            this.datavalues.put(DBFeilds.USER_COMPANY, Util.NullChecker(user.Profile.Default_Company_ID__r.Name));
            this.datavalues.put(DBFeilds.USER_COMPANY_ID, Util.NullChecker(user.Profile.Default_Company_ID__c));
            this.datavalues.put(DBFeilds.USER_PHONE_NUMBER, Util.NullChecker(user.Profile.Mobile__c));
            this.datavalues.put(DBFeilds.USER_CITY, Util.NullChecker(user.profileCity));
            if (Util.NullChecker(user.profilecountry).isEmpty()) {
                this.datavalues.put(DBFeilds.USER_COUNTRY, ITransaction.EMPTY_STRING);
            } else {
                this.datavalues.put(DBFeilds.USER_COUNTRY, Util.NullChecker(user.profilecountry));
            }
            if (Util.NullChecker(user.profilestate).isEmpty()) {
                this.datavalues.put(DBFeilds.USER_STATE, ITransaction.EMPTY_STRING);
            } else {
                this.datavalues.put(DBFeilds.USER_STATE, Util.NullChecker(user.profilestate));
            }
            this.datavalues.put(DBFeilds.USER_IMAGE_URL, Util.NullChecker(user.profileimage));
            this.datavalues.put(DBFeilds.USER_USER_ROLE, ITransaction.LOGGEDIN_USER);

            this.db.insert(DBFeilds.TABLE_USER, null, this.datavalues);
            //Log.i("DATABASE CLASS", "USER DETAILS INSERTED SUCCESSFULLY");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void InsertAndUpdateUserProfile(UserObjects user) {
        try {
            this.datavalues = new ContentValues();
            if (isUserExists(DBFeilds.TABLE_USER, " where GNUserID = '" + user.Profile.Id + "'")) {
                this.datavalues.put(DBFeilds.USER_IMAGE_URL, Util.NullChecker(user.profileimage));
                this.datavalues.put(DBFeilds.USER_USER_ROLE, ITransaction.LOGGEDIN_USER);
                this.datavalues.put(DBFeilds.USER_FIRST_NAME, user.Profile.First_Name__c);
                this.datavalues.put(DBFeilds.USER_LAST_NAME, user.Profile.Last_Name__c);
                this.datavalues.put(DBFeilds.USER_EMAIL_ID, user.Profile.Email__c);
                if (Util.NullChecker(user.Profile.Default_Company_ID__r.Name).contains("a0w")) {
                    this.datavalues.put(DBFeilds.USER_COMPANY, ITransaction.EMPTY_STRING);
                } else {
                    this.datavalues.put(DBFeilds.USER_COMPANY, Util.NullChecker(user.Profile.Default_Company_ID__r.Name));
                }
                this.datavalues.put(DBFeilds.USER_COMPANY_ID, Util.NullChecker(user.Profile.Default_Company_ID__c));
                this.datavalues.put(DBFeilds.USER_PHONE_NUMBER, Util.NullChecker(user.Profile.Mobile__c));
                this.datavalues.put(DBFeilds.USER_CITY, Util.NullChecker(user.profileCity));
                if (Util.NullChecker(user.profilecountry).isEmpty()) {
                    this.datavalues.put(DBFeilds.USER_COUNTRY, Util.NullChecker(ITransaction.EMPTY_STRING));
                } else {
                    this.datavalues.put(DBFeilds.USER_COUNTRY, Util.NullChecker(user.profilecountry));
                }
                if (Util.NullChecker(user.profilestate).isEmpty()) {
                    this.datavalues.put(DBFeilds.USER_STATE, ITransaction.EMPTY_STRING);
                } else {
                    this.datavalues.put(DBFeilds.USER_STATE, Util.NullChecker(user.profilestate));
                }
                this.db.update(DBFeilds.TABLE_USER, this.datavalues, "GNUserID = '" + user.Profile.Id + "'", null);
                //Log.i("DATABASE CLASS", "USER DETAILS UPDATED SUCCESSFULLY");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void InsertAndUpdateGNUser(OrderListHandler orderHandler) {
        BuyerInfoHandler user = orderHandler.buyerInfo;
        try {
            if (!Util.NullChecker(user.getId()).equalsIgnoreCase(ITransaction.EMPTY_STRING)) {
                this.datavalues = new ContentValues();
                //Log.i("!!!!!!!!!!!!!^^^^^^^^^^^^^^^^^-GN User Id-^^^^^^^^^^^^^^^^^!!!!!!!!!!!!", ":" + user.getFirstName() + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + user.getLastName() + " :: " + user.getBuyer_userid() + " :: " + user.getId());
                this.datavalues.put(DBFeilds.USER_GNUSER_ID, user.getBuyer_userid());
                this.datavalues.put(DBFeilds.USER_USERID, user.getId());
                this.datavalues.put(DBFeilds.USER_FIRST_NAME, user.getFirstName());
                this.datavalues.put(DBFeilds.USER_LAST_NAME, user.getLastName());
                this.datavalues.put(DBFeilds.USER_EMAIL_ID, user.getEmail());
                this.datavalues.put(DBFeilds.USER_COMPANY, user.getCompany());
                this.datavalues.put(DBFeilds.USER_PHONE_NUMBER, user.getMobile());
                this.datavalues.put(DBFeilds.USER_CITY, user.getBuyer_city());
                this.datavalues.put(DBFeilds.USER_IMAGE_URL, ITransaction.EMPTY_STRING);
                this.datavalues.put(DBFeilds.USER_USER_ROLE, ITransaction.EMPTY_STRING);
                this.datavalues.put(DBFeilds.USER_EVENT_ID, orderHandler.getEventId());
                this.datavalues.put(DBFeilds.ORDER_ORDER_STATUS, orderHandler.getOrderStatus());
                this.datavalues.put(DBFeilds.ORDER_ORDER_ID, orderHandler.getOrderId());
                //Log.i("----DataBase----", "----GnUser id----" + user.getId());
                if (isUserExists(DBFeilds.TABLE_USER, " where UserID = '" + user.getId() + "' AND " + DBFeilds.USER_EVENT_ID + " = '" + orderHandler.getEventId() + "'")) {
                    this.db.update(DBFeilds.TABLE_USER, this.datavalues, "UserID = '" + user.getId() + "' AND " + DBFeilds.USER_EVENT_ID + " = '" + orderHandler.getEventId() + "'", null);
                    //Log.i("DATABASE CLASS", "GN ATTENDEE USER DETAILS UPDATED SUCCESSFULLY");
                    return;
                }
                this.db.insert(DBFeilds.TABLE_USER, null, this.datavalues);
                //Log.i("DATABASE CLASS", "GN ATTENDEE USER DETAILS INSERTED SUCCESSFULLY");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserObjects getAppUserProfile(String userID) {
        UserObjects userprofile = new UserObjects();
        Cursor c = null;
        try {
            c = this.db.rawQuery("Select * from USER_PROFILE where UserID = '" + userID + "'", null);
            //Log.i("---User Profile Found---", ":" + c.getCount());
            if (c.getCount() != 0) {
                c.moveToFirst();
                userprofile.Userid = c.getString(c.getColumnIndex(DBFeilds.USER_USERID));
                userprofile.Profile.Id = c.getString(c.getColumnIndex(DBFeilds.USER_GNUSER_ID));
                userprofile.Profile.City__c = c.getString(c.getColumnIndex(DBFeilds.USER_CITY));
                userprofile.Profile.Company_Name__c = c.getString(c.getColumnIndex(DBFeilds.USER_COMPANY));
                userprofile.Profile.Default_Company_ID__c = c.getString(c.getColumnIndex(DBFeilds.USER_COMPANY_ID));
                userprofile.profilecountry = c.getString(c.getColumnIndex(DBFeilds.USER_COUNTRY));
                userprofile.Profile.Email__c = c.getString(c.getColumnIndex(DBFeilds.USER_EMAIL_ID));
                userprofile.Profile.First_Name__c = c.getString(c.getColumnIndex(DBFeilds.USER_FIRST_NAME));
                userprofile.Profile.Last_Name__c = c.getString(c.getColumnIndex(DBFeilds.USER_LAST_NAME));
                userprofile.Profile.Mobile__c = c.getString(c.getColumnIndex(DBFeilds.USER_PHONE_NUMBER));
                userprofile.profilestate = c.getString(c.getColumnIndex(DBFeilds.USER_STATE));
                userprofile.profileimage = c.getString(c.getColumnIndex(DBFeilds.USER_IMAGE_URL));
                userprofile.designation = c.getString(c.getColumnIndex(DBFeilds.USER_DESIGNATION));

            }
            if (c != null) {
                c.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (c != null) {
                c.close();
            }
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
        }
        return userprofile;
    }

/*    public void InsertCountry(List<CountriesObj> CountriesList) {
        try {
            for (CountriesObj countryData : CountriesList) {
                this.datavalues = new ContentValues();
                //Log.i("DataBase Insert Country", "Countries_Id= " + countryData.Country.Id);
                //Log.i("DataBase Insert Country", "Countries_name= " + countryData.Country.Long_Name__c);
                this.datavalues.put(DBFeilds.STATE_COUNTRY_ID, countryData.Country.Id);
                this.datavalues.put(DBFeilds.COUNTRY_LONG_NAME, countryData.Country.Long_Name__c);
                this.datavalues.put(DBFeilds.CURRENCY_ID, countryData.currencym.Id);
                this.datavalues.put(DBFeilds.CURRENCY_NAME, countryData.currencym.Name);
                this.datavalues.put(DBFeilds.CURRENCY_FULL_NAME, countryData.currencym.Currency_Name__c);
                this.datavalues.put(DBFeilds.CURRENCY_SYMBOL, countryData.currencym.Currency_Symbol__c);
                if (isRecordExists(DBFeilds.USER_COUNTRY, " where CountryId='" + countryData.Country.Id + "'")) {
                    this.db.update(DBFeilds.USER_COUNTRY, this.datavalues, "CountryId='" + countryData.Country.Id + "'", null);
                } else {
                    this.db.insert(DBFeilds.USER_COUNTRY, null, this.datavalues);
                }
                for (States state : countryData.States) {
                    InsertState(state);
                }
            }
            //Log.i("----Country Data Inserted----", "Success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public void InsertCurrency(List<Currency> Currency_list) {
        try {
            for (Currency currency : Currency_list) {
                this.datavalues = new ContentValues();
                this.datavalues.put(DBFeilds.CURRENCY_ID, currency.Id);
                this.datavalues.put(DBFeilds.CURRENCY_NAME, currency.Name);
                this.datavalues.put(DBFeilds.CURRENCY_FULL_NAME, currency.Currency_Name__c);
                this.datavalues.put(DBFeilds.CURRENCY_SYMBOL, currency.Currency_Symbol__c);

                if (isRecordExists(DBFeilds.TABLE_CURRENCY, " where currency_id='" + currency.Id + "'")) {
                    this.db.update(DBFeilds.TABLE_CURRENCY, this.datavalues, "currency_id='" + currency.Id + "'", null);
                } else {
                    this.db.insert(DBFeilds.TABLE_CURRENCY, null, this.datavalues);
                }
            }
            //Log.i("----Country Data Inserted----", "Success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* public void InsertState(States states) {
     try {
             this.datavalues = new ContentValues();
             this.datavalues.put(DBFeilds.STATE_ID, states.Id);
             this.datavalues.put(DBFeilds.STATE_SHORT_NAME, states.Short_Name__c);
             this.datavalues.put(DBFeilds.STATE_COUNTRY_ID, states.Country__c);
             this.datavalues.put(DBFeilds.STATE_LONG_NAME, states.Long_Name__c);
             if (isRecordExists(DBFeilds.USER_STATE, " where StateId='" + states.Id + "'")) {
                 this.db.update(DBFeilds.USER_STATE, this.datavalues, "StateId='" + states.Id + "'", null);
             } else {
                 this.db.insert(DBFeilds.USER_STATE, null, this.datavalues);
             }

         } catch (Exception e) {
             e.printStackTrace();
         }
     }*/
    public boolean isBadgePrinted(String ticId){
        boolean isBadgeId=false;
        Cursor c = db.rawQuery("select "+DBFeilds.ATTENDEE_BADGEID +" from "+DBFeilds.TABLE_ATTENDEE_DETAILS+" where "+DBFeilds.ATTENDEE_ID+" = '"+ticId+"'", null);
        if(c.getCount()>0){
            if(!c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty())
                isBadgeId=true;
        }
        c.close();
        return isBadgeId;
    }

    public ArrayList<String> getCountryList(String event_id) {
        ArrayList<String> country_list = new ArrayList<String>();
        try {
            String query = ITransaction.EMPTY_STRING;

            if (AppUtils.NullChecker(event_id).isEmpty()) {
                query = "Select * from Country";
            } else {
                EventObjects event = getSelectedEventRecord(event_id);
                query = "Select * from Country where CountryId IN ('" + event.countrycodes + "')";
            }

            Cursor c = this.db.rawQuery(query, null);
            if (c.getCount() == 0) {
                c.close();
                query = "Select * from Country";
                c = this.db.rawQuery(query, null);
            }
            //country_list = new String[c.getCount()];
            if (c.moveToFirst()) {
                do {
                    //Log.i("DataBase getCountry", "Countries_Id= " + c.getString(c.getColumnIndex(DBFeilds.STATE_COUNTRY_ID)));
                    //Log.i("DataBase getCountry", "Countries_name= " + c.getString(c.getColumnIndex(DBFeilds.COUNTRY_LONG_NAME)));
                    country_list.add(c.getString(c.getColumnIndex(DBFeilds.COUNTRY_LONG_NAME)));
                } while (c.moveToNext());
            }
            Collections.sort(country_list);
            country_list.add(0, "--Select--");
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return country_list;
    }

    public String getCountryId(String name) {
        String country_id = ITransaction.EMPTY_STRING;
        Cursor c = this.db.rawQuery("Select * from Country Where Country_Long_Name = '" + name.trim() + "'", null);
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                //Log.i("DataBAse", " Country_id=" + c.getString(c.getColumnIndex(DBFeilds.STATE_COUNTRY_ID)));
                country_id = c.getString(c.getColumnIndex(DBFeilds.STATE_COUNTRY_ID));
            }
            c.close();
        }
        return country_id;
    }

    public String getCountryName(String id) {
        String country_name = ITransaction.EMPTY_STRING;
        Cursor c = this.db.rawQuery("Select * from Country Where CountryId = '" + id + "'", null);
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                country_name = c.getString(c.getColumnIndex(DBFeilds.COUNTRY_LONG_NAME));
            }
            c.close();
        }
        return country_name;
    }

    public String getCountrySortName(String id) {
        String country_name = ITransaction.EMPTY_STRING;
        Cursor c = this.db.rawQuery("Select * from Country Where CountryId = '" + id + "'", null);
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                country_name = c.getString(c.getColumnIndex(DBFeilds.COUNTRY_SHORT_NAME));
            }
            c.close();
        }
        return country_name;
    }

    public Currency getCurrencyFromCountry(String id) {
        Currency currency = new Currency();
        String currency_name = "INR";
        try {

            Cursor country_cursor = this.db.rawQuery("select * from " + DBFeilds.TABLE_COUNTRY + " where " + DBFeilds.COUNTRY_ID + " = '" + id + "'", null);
            if (country_cursor.moveToFirst()) {
                currency_name = country_cursor.getString(country_cursor.getColumnIndex(DBFeilds.CURRENCY_ID));
                country_cursor.close();
            }

            Cursor c = this.db.rawQuery("Select * from Currency Where currency_name = '" + currency_name + "'", null);
            c.moveToFirst();
            currency.Id = c.getString(c.getColumnIndex(DBFeilds.CURRENCY_ID));
            currency.Name = c.getString(c.getColumnIndex(DBFeilds.CURRENCY_NAME));
            currency.Currency_Name__c = c.getString(c.getColumnIndex(DBFeilds.CURRENCY_FULL_NAME));
            currency.Currency_Symbol__c = c.getString(c.getColumnIndex(DBFeilds.CURRENCY_SYMBOL));
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currency;
    }

    public Currency getCurrency(String id) {
        Currency currency = new Currency();
        try {
            Cursor c = this.db.rawQuery("Select * from Currency Where currency_id = '" + id + "'", null);
            if (c.moveToFirst()) {
                currency.Id = c.getString(c.getColumnIndex(DBFeilds.CURRENCY_ID));
                currency.Name = c.getString(c.getColumnIndex(DBFeilds.CURRENCY_NAME));
                currency.Currency_Name__c = c.getString(c.getColumnIndex(DBFeilds.CURRENCY_FULL_NAME));
                currency.Currency_Symbol__c = c.getString(c.getColumnIndex(DBFeilds.CURRENCY_SYMBOL));
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currency;
    }

    public List<Currency> getCurrencyList() {
        List<Currency> currency_list = new ArrayList<Currency>();
        try {
            Cursor c = db.rawQuery("select * from Currency", null);
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                Currency currency = new Currency();
                currency.Id = c.getString(c.getColumnIndex(DBFeilds.CURRENCY_ID));
                currency.Name = c.getString(c.getColumnIndex(DBFeilds.CURRENCY_NAME));
                currency.Currency_Name__c = c.getString(c.getColumnIndex(DBFeilds.CURRENCY_FULL_NAME));
                currency.Currency_Symbol__c = c.getString(c.getColumnIndex(DBFeilds.CURRENCY_SYMBOL));
                currency_list.add(currency);
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return currency_list;
    }

    public String getStateName(String id) {
        String sname = ITransaction.EMPTY_STRING;
        try {
            Cursor c = this.db.rawQuery("Select * from State Where StateId = '" + id + "'", null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                sname = c.getString(c.getColumnIndex(DBFeilds.STATE_SHORT_NAME));
            }
            c.close();
            return sname;
        } catch (Exception e) {
            return ITransaction.EMPTY_STRING;
        }
    }

    public String getStateLongName(String id) {
        String name = ITransaction.EMPTY_STRING;
        try {
            Cursor c = this.db.rawQuery("Select * from State Where StateId = '" + id + "'", null);
            if (c.moveToFirst()) {
                name = c.getString(c.getColumnIndex(DBFeilds.STATE_LONG_NAME));
            }
            c.close();
        } catch (Exception e) {
        }
        return name;
    }

    public String getStateId(String name) {
        String state_id = ITransaction.EMPTY_STRING;
        Cursor c = this.db.rawQuery("Select * from State Where State_Long_Name = '" + name + "'", null);
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                state_id = c.getString(c.getColumnIndex(DBFeilds.STATE_ID));
            }
            c.close();
        }
        return state_id;
    }

    public ArrayList<String> getStateList(String id) {
        ArrayList<String> state_list = new ArrayList();
        try {
            Cursor c = this.db.rawQuery("Select * from State Where CountryId = '" + id + "'", null);
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                state_list.add(c.getString(c.getColumnIndex(DBFeilds.STATE_LONG_NAME)));
            }
            c.close();
            Collections.sort(state_list);
            state_list.add(0, "--Select--");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return state_list;
    }

    public long UpdateEditEvent(EditEventResponse event_data) {
        try {
            this.datavalues = new ContentValues();
            if (event_data.Events.Event_Pay_Gateway__r.records.size() > 0) {
                Iterator it = event_data.Events.Event_Pay_Gateway__r.records.iterator();
                while (it.hasNext()) {
                    InsertAndUpdateEventPaymentSetting((EventPaymentTypes) it.next());
                }
            }
            InsertAndUpdateEventPricing(event_data.Events.Event_Price__r.records);
            this.datavalues.put(DBFeilds.EVENT_NAME, event_data.Events.Name);
            this.datavalues.put(DBFeilds.EVENT_START_DATE, event_data.Events.Start_Date__c);
            //this.datavalues.put(DBFeilds.EVENT_ZEBRADPI, event_data.Events.ZebraDpi__c);
            this.datavalues.put(DBFeilds.EVENT_END_DATE, event_data.Events.End_Date__c);
            this.datavalues.put(DBFeilds.EVENT_ADDRESS, event_data.Events.Street1__c);
            this.datavalues.put(DBFeilds.EVENT_LOCATION, event_data.Events.Venue_Name__c);
            this.datavalues.put(DBFeilds.USER_CITY, Util.NullChecker(event_data.Events.City__c));
            if (event_data.Events.BLN_State__r != null) {
                this.datavalues.put(DBFeilds.USER_STATE, Util.NullChecker(Util.db.getStateLongName(event_data.Events.BLN_State__r.Name)));
                this.datavalues.put(DBFeilds.EVENT_STATE_ID, event_data.Events.BLN_State__r.Name);
            } else {
                this.datavalues.put(DBFeilds.USER_STATE, ITransaction.EMPTY_STRING);
            }
            if (!event_data.lastRefreshDate.isEmpty()) {
                this.datavalues.put(DBFeilds.EVENT_LASTMODIFYDATE, event_data.lastRefreshDate);
            }
            this.datavalues.put(DBFeilds.EVENT_STATE_SHORT, ITransaction.EMPTY_STRING);
            this.datavalues.put(DBFeilds.EVENT_PHONE, event_data.Events.Phone_Number__c);
            this.datavalues.put(DBFeilds.EVENT_ZIPCODE, event_data.Events.ZipCode__c);
            this.datavalues.put(DBFeilds.EVENT_CATEGORY, event_data.Events.Event_Type__c);
            this.datavalues.put(DBFeilds.EVENT_DESCRIPTION, event_data.Events.Description__c);
            this.datavalues.put(DBFeilds.EVENT_STATUS, event_data.Events.Event_Status__c);
            if (event_data.Events.BLN_Country__r != null) {
                this.datavalues.put(DBFeilds.USER_COUNTRY, Util.db.getCountryName(event_data.Events.BLN_Country__r.Name));
                this.datavalues.put(DBFeilds.EVENT_COUNTRY_ID, event_data.Events.BLN_Country__r.Name);
            } else {
                this.datavalues.put(DBFeilds.USER_COUNTRY, ITransaction.EMPTY_STRING);
            }
            this.datavalues.put(DBFeilds.EVENT_COUNTRY_SHORT_NAME, ITransaction.EMPTY_STRING);
            this.datavalues.put(DBFeilds.EVENT_SALESTAX, event_data.Events.Tax_Rate__c);
            this.datavalues.put(DBFeilds.EVENT_IMAGE_URL, event_data.image);
            this.datavalues.put(DBFeilds.EVENT_FEE_APPLICABLE, event_data.Events.Accept_Tax_Rate__c);
            this.datavalues.put(DBFeilds.EVENT_TIME_ZONE, event_data.Events.Time_Zone__c);
            this.datavalues.put(DBFeilds.EVENT_SCANATTENDEE_LIMIT, String.valueOf(event_data.Events.scan_attendee_limit__c));
            this.datavalues.put(DBFeilds.EVENT_TOTAL_ATTENDEES_COUNT, Integer.valueOf(event_data.Events.tickets__r.totalSize));
            this.datavalues.put(DBFeilds.EVENT_TOTAL_ORDERS_COUNT, Integer.valueOf(event_data.Events.Orders__r.totalSize));
            this.datavalues.put(DBFeilds.EVENT_ORGANIZER_ID, event_data.Events.organizer_id__r.Id);
            this.datavalues.put(DBFeilds.EVENT_ORGANAIZER_NAME, event_data.Events.organizer_id__r.Name);
            this.datavalues.put(DBFeilds.EVENT_ORGANIZER_ID, event_data.Events.organizer_id__c);
            this.datavalues.put(DBFeilds.EVENT_ORGANIZER_EMAIL, event_data.Events.Organizer_Email__c);
            this.datavalues.put(DBFeilds.EVENT_REGISTRATION_LINK, event_data.RegistrationLink);
            this.datavalues.put(DBFeilds.EVENT_SESSION_TIME, event_data.sessiontime);
            this.datavalues.put(DBFeilds.EVENT_BADGE_NAME, event_data.Events.Mobile_Default_Badge__c);
            this.datavalues.put(DBFeilds.EVENT_DASHBOARDVISIBILITY, event_data.Events.Revenue_visibility__c);
            this.datavalues.put(DBFeilds.EVENT_CURRENCY, event_data.Events.BLN_Currency__c);
           /* if (event_data.Events.BLN_Badge_Styles__r.records.size() > 0) {
                this.datavalues.put(DBFeilds.EVENT_BADGE_NAME, ((BadgeRecord) event_data.Events.BLN_Badge_Styles__r.records.get(0)).Id);
            } else {
                this.datavalues.put(DBFeilds.EVENT_BADGE_NAME, ITransaction.EMPTY_STRING);
            }*/
            if (isUserExists(Util.EVENT_DETAILS, " where EventID = '" + event_data.Events.Id + "'")) {
                this.db.update(Util.EVENT_DETAILS, this.datavalues, "EventID = '" + event_data.Events.Id + "'", null);
                //Log.i("DATABASE CLASS", "EVENT DETAILS UPDATED SUCCESSFULLY OF " + event_data.Events.Id + " Name " + event_data.Events.Name);
            } else {
                this.datavalues.put(DBFeilds.EVENT_EVENT_ID, event_data.Events.Id);
                this.db.insert(Util.EVENT_DETAILS, null, this.datavalues);
                //Log.i("DATABASE CLASS", "EVENT DETAILS INSERT SUCCESSFULLY" + event_data.Events.Id + " Name " + event_data.Events.Name);
            }
            return 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public long InsertAndUpdateEvent(List<EventObjects> events, boolean isResetSettings) {
        long mobile_eventid = 0;
        int i = 0;
        try {
            this.db.beginTransaction();
            for (EventObjects event_data : events) {

                if (event_data.Events.Event_Pay_Gateway__r.records.size() > 0) {
                    Util.db.delete_Event_PGateway(event_data.Events.Id);
                    Iterator it = event_data.Events.Event_Pay_Gateway__r.records.iterator();
                    while (it.hasNext()) {
                        EventPaymentTypes _setting = (EventPaymentTypes) it.next();
                        if (AppUtils.NullChecker(_setting.Registration_Type__c).equalsIgnoreCase(Util.Registration_Type__c) && !AppUtils.NullChecker(_setting.Pay_Gateway__r.PGateway_Type__c).isEmpty()) {
                            InsertAndUpdateEventPaymentSetting(_setting);
                        }
                    }
                }

                /*if (isResetSettings) {// commeted for session missing after reset
                    deleteEventScannedTickets(event_data.Events.Id);
                    InsertAndUpdateScannedItems(event_data.Events.BLN_Scanusr_items__r.records, event_data.Events.Id);
                }*/
                InsertAndUpdateOrganizerPayInfo(event_data.Events.organizer_id__r);
                InsertAndUpdateEventPricing(event_data.Events.Event_Price__r.records);
                InsertUpdateRegsettings(event_data.Events.Reg_Settings__r.records, isResetSettings);

                this.datavalues = new ContentValues();
                this.datavalues.put(DBFeilds.EVENT_NAME, event_data.Events.Name);
                //Log.i("---------------Name & Time Zone----------", ":" + event_data.Events.Name + " :: " + event_data.Events.Time_Zone__c);
                this.datavalues.put(DBFeilds.EVENT_START_DATE, event_data.Events.Start_Date__c);
                this.datavalues.put(DBFeilds.EVENT_END_DATE, event_data.Events.End_Date__c);
                //this.datavalues.put(DBFeilds.EVENT_ZEBRADPI, event_data.Events.ZebraDpi__c);
                this.datavalues.put(DBFeilds.EVENT_ADDRESS, event_data.Events.Street1__c);
                this.datavalues.put(DBFeilds.EVENT_LOCATION, event_data.Events.Venue_Name__c);
                this.datavalues.put(DBFeilds.USER_CITY, Util.NullChecker(event_data.Events.City__c));
                if (!event_data.lastRefreshDate.isEmpty()) {
                    this.datavalues.put
                            (DBFeilds.EVENT_LASTMODIFYDATE, event_data.lastRefreshDate);
                }
                if (event_data.Events.BLN_State__c.isEmpty()) {
                    this.datavalues.put(DBFeilds.USER_STATE, ITransaction.EMPTY_STRING);
                } else {
                    this.datavalues.put(DBFeilds.USER_STATE, Util.NullChecker(Util.db.getStateLongName(event_data.state)));
                    this.datavalues.put(DBFeilds.EVENT_STATE_ID, event_data.state);
                }
                this.datavalues.put(DBFeilds.EVENT_STATE_SHORT, ITransaction.EMPTY_STRING);
                this.datavalues.put(DBFeilds.EVENT_PHONE, event_data.Events.Phone_Number__c);
                this.datavalues.put(DBFeilds.EVENT_ZIPCODE, event_data.Events.ZipCode__c);
                this.datavalues.put(DBFeilds.EVENT_CATEGORY, event_data.Events.Event_Type__c);
                this.datavalues.put(DBFeilds.EVENT_DESCRIPTION, event_data.Events.Description__c);
                this.datavalues.put(DBFeilds.EVENT_STATUS, event_data.Events.Event_Status__c);
                if (event_data.Events.BLN_Country__c != null) {
                    this.datavalues.put(DBFeilds.USER_COUNTRY, Util.db.getCountryName(event_data.country));
                    this.datavalues.put(DBFeilds.EVENT_COUNTRY_ID, event_data.country);
                } else {
                    this.datavalues.put(DBFeilds.USER_COUNTRY, ITransaction.EMPTY_STRING);
                }
                this.datavalues.put(DBFeilds.EVENT_COUNTRY_SHORT_NAME, ITransaction.EMPTY_STRING);
                this.datavalues.put(DBFeilds.EVENT_SALESTAX, event_data.Events.Tax_Rate__c);
                this.datavalues.put(DBFeilds.EVENT_IMAGE_URL, event_data.image);
                if(AppUtils.NullChecker(event_data.selfcheckinoption).equals("color"))
                    this.datavalues.put(DBFeilds.EVENT_SA_SELFCHECKIN_BACKGROUNDCOLOUR,event_data.selfcheckinbgcolor);
                else
                    this.datavalues.put(DBFeilds.EVENT_SA_SELFCHECKIN_BACKGROUNDCOLOUR,"");
                this.datavalues.put(DBFeilds.EVENT_SA_SELFCHECKIN_LOGO,event_data.selfcheckineventlogo);
                this.datavalues.put(DBFeilds.EVENT_SA_SELFCHECKIN_IMAGE_URL, event_data.SADashboardimgUrl);
                this.datavalues.put(DBFeilds.EVENT_FEE_APPLICABLE, event_data.Events.Accept_Tax_Rate__c);
                this.datavalues.put(DBFeilds.EVENT_TIME_ZONE, event_data.Events.Time_Zone__c);
                this.datavalues.put(DBFeilds.EVENT_REGISTRATION_LINK, event_data.RegistrationLink);
                this.datavalues.put(DBFeilds.EVENT_ORGANIZER_ID, event_data.Events.organizer_id__c);
                this.datavalues.put(DBFeilds.EVENT_ORGANIZER_EMAIL, event_data.Events.Organizer_Email__c);
                this.datavalues.put(DBFeilds.EVENT_SESSION_TIME, event_data.sessiontime);
                //Log.i("--------Scan Attendee Limit----------", " : " + event_data.Events.Name + " : " + event_data.Events.scan_attendee_limit__c);
                this.datavalues.put(DBFeilds.EVENT_SCANATTENDEE_LIMIT, String.valueOf(event_data.Events.scan_attendee_limit__c));
                this.datavalues.put(DBFeilds.EVENT_USER_ROLE, event_data.roles);
                this.datavalues.put(DBFeilds.EVENT_SELECTED_COUNTRY_CODES, event_data.countrycodes);
                this.datavalues.put(DBFeilds.EVENT_CURRENCY, event_data.Events.BLN_Currency__c);
                this.datavalues.put(DBFeilds.EVENT_DASHBOARDVISIBILITY, event_data.Events.Revenue_visibility__c);
                if (!Util.NullChecker(String.valueOf(event_data.Events.AllowNoticketSessions__c)).isEmpty()) {
                    this.datavalues.put(DBFeilds.EVENT_ALLOW_SESSION, String.valueOf(event_data.Events.AllowNoticketSessions__c));
                }
                // if (isResetSettings) {//updating directly from badgetempleteclass only
                if (isResetSettings) {
                    this.datavalues.put(DBFeilds.EVENT_BADGE_NAME, event_data.Events.Mobile_Default_Badge__c);
                }
                else if(!event_data.Events.Mobile_Default_Badge__c.isEmpty()) {
                    this.datavalues.put(DBFeilds.EVENT_BADGE_NAME, event_data.Events.Mobile_Default_Badge__c);
                }
                //}
               /* if (event_data.Events.BLN_Badge_Styles__r.records.size() > 0) {
                    //Log.i("--------Batabase-----------", "<<<<<<<<<<<<<  Badge Field is not null >>>>>>>>>>>>>" + ((BadgeRecord) event_data.Events.BLN_Badge_Styles__r.records.get(0)).Id);
                    this.datavalues.put(DBFeilds.EVENT_BADGE_NAME, ((BadgeRecord) event_data.Events.BLN_Badge_Styles__r.records.get(0)).Id);
                } else {
                    this.datavalues.put(DBFeilds.EVENT_BADGE_NAME, ITransaction.EMPTY_STRING);
                }*/
                mobile_eventid = 0;
                if (isUserExists(DBFeilds.TABLE_EVENT_DETAILS, " where EventID = '" + event_data.Events.Id + "'")) {
                    this.db.update(DBFeilds.TABLE_EVENT_DETAILS, this.datavalues, "EventID = '" + event_data.Events.Id + "'", null);
                    //Log.i("DATABASE CLASS", "EVENT DETAILS UPDATED SUCCESSFULLY");
                } else {
                    InsertAndUpdateScannedItems(event_data.Events.BLN_Scanusr_items__r.records, event_data.Events.Id);
                    this.datavalues.put(DBFeilds.EVENT_BADGE_NAME, event_data.Events.Mobile_Default_Badge__c);

                    this.datavalues.put(DBFeilds.EVENT_EVENT_ID, event_data.Events.Id);
                    this.db.insert(DBFeilds.TABLE_EVENT_DETAILS, null, this.datavalues);
                    //Log.i("DATABASE CLASS", "EVENT DETAILS INSERT SUCCESSFULLY");
                }
                i++;
                if (HttpPostData.dataloading != null) {
                    HttpPostData.dataloading.onProgressUpdate(i);
                }
            }
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
            return mobile_eventid;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public void InsertAndUpdateEventPricing(List<EventPriceObject> event_pricing) {
        for (EventPriceObject event_price : event_pricing) {
            ContentValues values = new ContentValues();
            values.put(DBFeilds.PRICING_EVENT_ID, event_price.Event__c);
            values.put(DBFeilds.PRICING_ID, event_price.Id);
            values.put(DBFeilds.PRICING_NAME, event_price.Name);
            values.put(DBFeilds.PRICING_PERCENTAGE, new StringBuilder(String.valueOf(event_price.BL_Fee_Percentage__c)).toString());
            values.put(DBFeilds.PRICING_FEE, new StringBuilder(String.valueOf(event_price.BL_Fee_Amount__c)).toString());
            values.put(DBFeilds.PRICING_ITEM_TYPE, event_price.Item_type__c);
            values.put(DBFeilds.PRICING_MAX_FEE, new StringBuilder(String.valueOf(event_price.Max_bl_fee__c)).toString());
            values.put(DBFeilds.PRICING_IS_PRODUCT, String.valueOf(event_price.Item_type__r.Eventdex_Product__c));
            if (isRecordExists(DBFeilds.TABLE_EVENT_PRICING, " where pricing_id='" + event_price.Id + "'")) {
                this.db.update(DBFeilds.TABLE_EVENT_PRICING, values, "pricing_id='" + event_price.Id + "'", null);
            } else {
                this.db.insert(DBFeilds.TABLE_EVENT_PRICING, null, values);
            }
        }
    }
    public double getItemFee(String eventid,String itemId){
        double d = 0.0d;
        Cursor c = this.db.rawQuery("select * from ItemDetails where EventId='" + eventid + "' AND " + DBFeilds.ADDED_ITEM_ID + "='" + itemId + "'", null);
        if (c != null) {
            if (c.getCount() == 0) {
                c.close();
            } else {
                c.moveToFirst();
                d = Double.parseDouble(Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_FEE))));
                c.close();
            }
        }
        return d;
    }
    public double getItemType_BL_FEE(String eventid, String item_typeid) {
        double d = 0.0d;
        Cursor c = this.db.rawQuery("select * from Event_Pricing where event_id_pricing='" + eventid + "' AND " + DBFeilds.PRICING_ITEM_TYPE + "='" + item_typeid + "'", null);
        if (c != null) {
            if (c.getCount() == 0) {
                c.close();
            } else {
                c.moveToFirst();
                d = Double.parseDouble(c.getString(c.getColumnIndex(DBFeilds.PRICING_FEE)));
               /* if (Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.PRICING_IS_PRODUCT))).booleanValue()) {
                    c.close();
                    Cursor items = this.db.rawQuery("select * from ItemType where ItemTypeName = 'Admissions'", null);
                    items.moveToFirst();
                    item_typeid = items.getString(items.getColumnIndex(DBFeilds.ITEM_TYPE_ID));
                    items.close();
                    c = this.db.rawQuery("select * from Event_Pricing where event_id_pricing='" + eventid + "' AND " + DBFeilds.PRICING_ITEM_TYPE + "='" + item_typeid + "'", null);
                    c.moveToFirst();
                    d = Double.parseDouble(c.getString(c.getColumnIndex(DBFeilds.PRICING_FEE)));
                } else {
                    d = Double.parseDouble(c.getString(c.getColumnIndex(DBFeilds.PRICING_FEE)));
                }*/
                c.close();
            }
        }
        return d;
    }

    public boolean isItemProduct(String eventid, String item_typeid) {
        Cursor c = this.db.rawQuery("select * from Event_Pricing where event_id_pricing='" + eventid + "' AND " + DBFeilds.PRICING_ITEM_TYPE + "='" + item_typeid + "'", null);
        if (c != null) {
            if (c.moveToFirst()) {
                return Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.PRICING_IS_PRODUCT))).booleanValue();
            }
        }
        return false;
    }

    public double getItemType_BL_MAX_FEE(String eventid, String item_typeid) {
        Cursor c = this.db.rawQuery("select * from Event_Pricing where event_id_pricing='" + eventid + "' AND " + DBFeilds.PRICING_ITEM_TYPE + "='" + item_typeid + "'", null);
        if (c == null) {
            return 0.0d;
        }
        if (c.getCount() == 0) {
            c.close();
            return 0.0d;
        }
        c.moveToFirst();
        double fee_value = Double.parseDouble(c.getString(c.getColumnIndex(DBFeilds.PRICING_MAX_FEE)));
        c.close();
        return fee_value;
    }

    public double getItemType_BL_PER(String eventid, String item_typeid) {
        Cursor c = this.db.rawQuery("select * from Event_Pricing where event_id_pricing='" + eventid + "' AND " + DBFeilds.PRICING_ITEM_TYPE + "='" + item_typeid + "'", null);
        if (c == null) {
            return 0.0d;
        }
        if (c.getCount() == 0) {
            c.close();
            return 0.0d;
        }
        c.moveToFirst();
        double fee_value = Double.parseDouble(c.getString(c.getColumnIndex(DBFeilds.PRICING_PERCENTAGE)));
        c.close();
        return fee_value;
    }

    public boolean getItemType_ISPRODUCT(String eventid, String item_type_id) {
        Cursor c = this.db.rawQuery("select * from Event_Pricing where event_id_pricing='" + eventid + "' AND " + DBFeilds.PRICING_ITEM_TYPE + "='" + item_type_id + "'", null);
        if (c == null) {
            return false;
        }
        if (c.getCount() == 0) {
            c.close();
            return false;
        }
        c.moveToFirst();
        boolean fee_value = Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.PRICING_IS_PRODUCT))).booleanValue();
        c.close();
        return fee_value;
    }

    public int delete_Event_PGateway(String event_id) {
        try {
            return this.db.delete(DBFeilds.TABLE_EVENT_PAYAMENT_SETTINGS, "event_id = '" + event_id + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void deleteEventScannedTicketsGroup(String event_id) {
        try {
            db.delete(DBFeilds.TABLE_SCANNEDTICKETS_GROUP, DBFeilds.GROUP_EVENT_ID + " = '" + event_id + "'", null);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void deleteEventScannedTickets(String event_id) {
        try {
            db.delete(DBFeilds.TABLE_SCANNED_TICKETS, DBFeilds.SCANNED_EVENT_ID + " = '" + event_id + "'", null);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void deleteEventScannedTickets(String event_id, String scanitemid) {
        try {
            db.delete(DBFeilds.TABLE_SCANNED_TICKETS, DBFeilds.SCANNED_EVENT_ID + " = '" + event_id + "' AND " + DBFeilds.SCANNED_ID + " = '" + scanitemid + "'", null);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public Cursor getEvent_Payment_Setting(String _event_id) {
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("Select * from PaymentSettings where event_id = '" + _event_id + "' AND Name != ''", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public String getPayment_Adoptive_Type_Name(String _pay_type_id) {
        String name = ITransaction.EMPTY_STRING;
        try {
            Cursor c = this.db.rawQuery("Select * from PaymentGateways where payment_type_id = '" + _pay_type_id + "'", null);
            if (c != null) {
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    name = c.getString(c.getColumnIndex(PaymentTypeDBFeilds.PAYMENT_ADOPTIVE_TYPE));
                }
                c.close();
            }
            return name;
        } catch (Exception e) {
            e.printStackTrace();
            return name;
        }
    }

    public String getPayment_Type_Name(String _pay_type_id) {
        String name = ITransaction.EMPTY_STRING;
        try {
            Cursor c = this.db.rawQuery("Select * from PaymentGateways where payment_type_id = '" + _pay_type_id + "'", null);
            if (c != null) {
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    name = c.getString(c.getColumnIndex(PaymentTypeDBFeilds.PAYMENT_TYPE_NAME)) + " " + c.getString(c.getColumnIndex(PaymentTypeDBFeilds.PAYMENT_ADOPTIVE_TYPE));
                }
                c.close();
            }
            return name;
        } catch (Exception e) {
            e.printStackTrace();
            return name;
        }
    }

    public String getPayment_Type_Id(String _pay_type_name) {
        String name = ITransaction.EMPTY_STRING;
        try {
            //Log.i("DATABASE CLASS", "%-------Payment Type ID-------%" + _pay_type_name);
            Cursor c = this.db.rawQuery("Select * from PaymentGateways where payment_type_name = '" + _pay_type_name + "'", null);
            if (c != null) {
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    name = c.getString(c.getColumnIndex(PaymentTypeDBFeilds.PAYMENT_TYPE_ID));
                    //Log.i("DATABASE CLASS", "%-------Payment Type Name-------%" + name);
                }
                c.close();
            }
            return name;
        } catch (Exception e) {
            e.printStackTrace();
            return name;
        }
    }

    public Cursor getEvent_Card_PGateway(String _event_id) {
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("Select * from PaymentSettings where event_id = '" + _event_id + "'" + " AND " + EventPayamentSettings.EVENT_PAY_NAME + " NOT IN ('Cash','Check','External Pay Gateway','Credit Card','Free','')", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public boolean isCreditCardPaymentON(String _event_id) {
        Cursor cursor = null;
        boolean ON=false;
        try {
            cursor = this.db.rawQuery("Select * from PaymentSettings where event_id = '" + _event_id + "'" + " AND " + EventPayamentSettings.EVENT_PAY_NAME + " NOT IN ('Cash','Check','External Pay Gateway','Credit Card','Free','')", null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    ON=true;
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ON;
    }

    public Cursor getEvent_PGateway_Key(String where) {
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("Select * from PaymentSettings" + where, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public ArrayList<EventObjects> getEventDetails() {
        ArrayList<EventObjects> event_data = new ArrayList<EventObjects>();
        try {
            Cursor c = this.db.rawQuery("Select * from EventDetails ORDER BY StartDate ASC ", null);
            if (c.getCount() != 0) {
                c.moveToFirst();
                for (int i = 0; i < c.getCount(); i++) {
                    EventObjects event = new EventObjects();
                    event.Events.Id = c.getString(c.getColumnIndex(DBFeilds.EVENT_EVENT_ID));
                    event.Events.checkin = c.getString(c.getColumnIndex(DBFeilds.EVENT_ISCHECKEDIN));
                    event.Events.Name = c.getString(c.getColumnIndex(DBFeilds.EVENT_NAME));
                    event.Events.Start_Date__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_START_DATE));
                    //event.Events.ZebraDpi__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ZEBRADPI));
                    event.Events.End_Date__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_END_DATE));
                    event.Events.Street1__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ADDRESS));
                    event.Events.Venue_Name__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_LOCATION));
                    event.Events.City__c = c.getString(c.getColumnIndex(DBFeilds.USER_CITY));
                    event.state = c.getString(c.getColumnIndex(DBFeilds.USER_STATE));
                    event.Events.ZipCode__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ZIPCODE));
                    event.country = c.getString(c.getColumnIndex(DBFeilds.USER_COUNTRY));
                    event.Events.Accept_Tax_Rate__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_FEE_APPLICABLE));
                    event.Events.Description__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_DESCRIPTION));
                    event.Events.Event_Status__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_STATUS));
                    event.Events.Tax_Rate__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_SALESTAX));
                    event.image = c.getString(c.getColumnIndex(DBFeilds.EVENT_IMAGE_URL));
                    event.SADashboardimgUrl = Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.EVENT_SA_SELFCHECKIN_IMAGE_URL)));
                    event.selfcheckinbgcolor = Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.EVENT_SA_SELFCHECKIN_BACKGROUNDCOLOUR)));
                    event.selfcheckineventlogo = Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.EVENT_SA_SELFCHECKIN_LOGO)));
                    event.Events.Time_Zone__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_TIME_ZONE));
                    event.Events.Organizer_Email__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ORGANIZER_EMAIL));
                    event.Events.organizer_id__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ORGANIZER_ID));
                    event.RegistrationLink = c.getString(c.getColumnIndex(DBFeilds.EVENT_REGISTRATION_LINK));
                    event.sessiontime = c.getString(c.getColumnIndex(DBFeilds.EVENT_SESSION_TIME));
                    event.Events.BLN_State__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_STATE_ID));
                    event.Events.roles = c.getString(c.getColumnIndex(DBFeilds.EVENT_USER_ROLE));
                    BadgeRecord badge = new BadgeRecord();
                    badge.Id = c.getString(c.getColumnIndex(DBFeilds.EVENT_BADGE_NAME));
                    event.Events.BLN_Badge_Styles__r.records.add(badge);
                    event_data.add(event);
                    c.moveToNext();
                }
            }
            c.close();
            return event_data;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ArrayList<EventObjects> getEventDetails(String where_caluse) {
        ArrayList<EventObjects> event_data = new ArrayList<EventObjects>();
        try {
            Cursor c = this.db.rawQuery("Select * from EventDetails " + where_caluse + " ORDER BY StartDate ASC ", null);
            if (c.getCount() != 0) {
                c.moveToFirst();
                for (int i = 0; i < c.getCount(); i++) {
                    EventObjects event = new EventObjects();
                    event.Events.Id = c.getString(c.getColumnIndex(DBFeilds.EVENT_EVENT_ID));
                    event.Events.checkin = c.getString(c.getColumnIndex(DBFeilds.EVENT_ISCHECKEDIN));
                    event.Events.Name = c.getString(c.getColumnIndex(DBFeilds.EVENT_NAME));
                    event.Events.Start_Date__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_START_DATE));
                    //event.Events.ZebraDpi__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ZEBRADPI));
                    event.Events.End_Date__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_END_DATE));
                    event.Events.Street1__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ADDRESS));
                    event.Events.Venue_Name__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_LOCATION));
                    event.Events.City__c = c.getString(c.getColumnIndex(DBFeilds.USER_CITY));
                    event.state = c.getString(c.getColumnIndex(DBFeilds.USER_STATE));
                    event.Events.ZipCode__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ZIPCODE));
                    event.country = c.getString(c.getColumnIndex(DBFeilds.USER_COUNTRY));
                    event.Events.Accept_Tax_Rate__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_FEE_APPLICABLE));
                    event.Events.Description__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_DESCRIPTION));
                    event.Events.Event_Status__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_STATUS));
                    event.Events.Tax_Rate__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_SALESTAX));
                    event.image = c.getString(c.getColumnIndex(DBFeilds.EVENT_IMAGE_URL));
                    event.Events.Time_Zone__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_TIME_ZONE));
                    event.Events.Organizer_Email__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ORGANIZER_EMAIL));
                    event.Events.organizer_id__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ORGANIZER_ID));
                    event.RegistrationLink = c.getString(c.getColumnIndex(DBFeilds.EVENT_REGISTRATION_LINK));
                    event.sessiontime = c.getString(c.getColumnIndex(DBFeilds.EVENT_SESSION_TIME));
                    event.Events.BLN_State__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_STATE_ID));
                    event.Events.roles = c.getString(c.getColumnIndex(DBFeilds.EVENT_USER_ROLE));
                    event.Events.Revenue_visibility__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_DASHBOARDVISIBILITY));
                    BadgeRecord badge = new BadgeRecord();
                    badge.Id = c.getString(c.getColumnIndex(DBFeilds.EVENT_BADGE_NAME));
                    event.Events.BLN_Badge_Styles__r.records.add(badge);
                    event_data.add(event);
                    c.moveToNext();
                }
            }
            c.close();
            return event_data;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public EventObjects getSelectedEventRecord(String eventId) {
        EventObjects controller = new EventObjects();
        try {
            Cursor c = this.db.rawQuery("Select * from EventDetails where EventID = '" + eventId + "'", null);
            if (c.getCount() != 0) {
                c.moveToFirst();
                controller.Events.mobileevent_id = c.getString(c.getColumnIndex("_id"));
                controller.Events.Id = c.getString(c.getColumnIndex(DBFeilds.EVENT_EVENT_ID));
                controller.Events.Name = c.getString(c.getColumnIndex(DBFeilds.EVENT_NAME));
                controller.Events.Street1__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ADDRESS));
                controller.Events.Venue_Name__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_LOCATION));
                controller.Events.City__c = c.getString(c.getColumnIndex(DBFeilds.USER_CITY));
                controller.state = c.getString(c.getColumnIndex(DBFeilds.USER_STATE));
                controller.Events.last_modified_date = c.getString(c.getColumnIndex(DBFeilds.EVENT_LASTMODIFYDATE));
                controller.country = c.getString(c.getColumnIndex(DBFeilds.USER_COUNTRY));
                controller.Events.ZipCode__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ZIPCODE));
                controller.Events.Start_Date__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_START_DATE));
                //controller.Events.ZebraDpi__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ZEBRADPI));
                controller.Events.End_Date__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_END_DATE));
                controller.Events.Description__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_DESCRIPTION));
                controller.Events.Time_Zone__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_TIME_ZONE));
                controller.Events.Tax_Rate__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_SALESTAX));
                controller.Events.Accept_Tax_Rate__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_FEE_APPLICABLE));
                controller.Events.Organizer_Email__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ORGANIZER_EMAIL));
                controller.Events.organizer_id__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_ORGANIZER_ID));
                controller.Events.BLN_Country__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_COUNTRY_ID));
                controller.Events.BLN_State__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_STATE_ID));
                controller.Events.AllowNoticketSessions__c = Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.EVENT_ALLOW_SESSION)));
                if (Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.EVENT_BADGE_NAME))).isEmpty()) {
                    controller.Events.Mobile_Default_Badge__c = ITransaction.EMPTY_STRING;
                } else {
                    controller.Events.Mobile_Default_Badge__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_BADGE_NAME));
                }
                controller.Events.Revenue_visibility__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_DASHBOARDVISIBILITY));
                controller.image = c.getString(c.getColumnIndex(DBFeilds.EVENT_IMAGE_URL));
                controller.SADashboardimgUrl = c.getString(c.getColumnIndex(DBFeilds.EVENT_SA_SELFCHECKIN_IMAGE_URL));
                controller.selfcheckinbgcolor = c.getString(c.getColumnIndex(DBFeilds.EVENT_SA_SELFCHECKIN_BACKGROUNDCOLOUR));
                controller.selfcheckineventlogo = c.getString(c.getColumnIndex(DBFeilds.EVENT_SA_SELFCHECKIN_LOGO));
                controller.Events.checkin = c.getString(c.getColumnIndex(DBFeilds.EVENT_ISCHECKEDIN));
                controller.RegistrationLink = c.getString(c.getColumnIndex(DBFeilds.EVENT_REGISTRATION_LINK));
                controller.sessiontime = c.getString(c.getColumnIndex(DBFeilds.EVENT_SESSION_TIME));
                //Log.i("-------------------Scan Attendee Limit-----------------", ":" + c.getString(c.getColumnIndex(DBFeilds.EVENT_SCANATTENDEE_LIMIT)));
                controller.Events.scan_attendee_limit__c = Integer.parseInt(c.getString(c.getColumnIndex(DBFeilds.EVENT_SCANATTENDEE_LIMIT)));
                controller.lastRefreshDate = c.getString(c.getColumnIndex(DBFeilds.EVENT_LASTMODIFYDATE));
                controller.roles = c.getString(c.getColumnIndex(DBFeilds.EVENT_USER_ROLE));
                controller.countrycodes = c.getString(c.getColumnIndex(DBFeilds.EVENT_SELECTED_COUNTRY_CODES));
                controller.Events.BLN_Currency__c = c.getString(c.getColumnIndex(DBFeilds.EVENT_CURRENCY));
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return controller;
    }

    public boolean deleteEventData(String event_id) {
        try {
            this.db.beginTransaction();
            this.db.delete(DBFeilds.TABLE_ADDED_TICKETS, "EventId = '" + event_id + "'", null);
            this.db.delete(DBFeilds.TABLE_ITEM_POOL, "EventId = '" + event_id + "'", null);
            this.db.delete(DBFeilds.TABLE_ATTENDEE_DETAILS, "Event_Id = '" + event_id + "'", null);
            this.db.delete(DBFeilds.TABLE_SALES_ORDER, "Event_Id = '" + event_id + "'", null);
            this.db.delete(DBFeilds.TABLE_SALES_ORDER_ITEM, "Event_Id = '" + event_id + "'", null);
            this.db.delete(Util.EVENT_DETAILS, "EventID = '" + event_id + "'", null);
            this.db.delete(DBFeilds.TABLE_EVENT_PRICING, "event_id_pricing = '" + event_id + "'", null);
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
            Util.deleteImageFile(new StringBuilder(String.valueOf(event_id)).append("-").append(event_id).append(Util.FILEFORMAT).toString());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public int checkEventCount() {
        Cursor c = this.db.rawQuery("Select * from EventDetails", null);
        if (c == null) {
            return 0;
        }
        int count = c.getCount();
        c.close();
        return count;
    }

    public OrderListHandler getTicketOrderDetails(String orderId) {
        OrderListHandler orderList = new OrderListHandler();
        try {
            Cursor c = this.db.rawQuery("select * from OrderDetails Where Order_Id = '" + orderId + "'", null);
            //Log.i("-----Size of Order--", new StringBuilder(String.valueOf(orderId)).append(" Count--").append(c.getCount()).toString());
            if (c.getCount() > 0) {
                c.moveToFirst();
                orderList.setOrderStatus(c.getString(c.getColumnIndex(DBFeilds.ORDER_ORDER_STATUS)));
                orderList.setOrderDate(c.getString(c.getColumnIndex(DBFeilds.ORDER_ORDER_DATE)));
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderList;
    }
    public Cursor getBadgeableTicketOrderDetails(String orderId) {
        Cursor c = this.db.rawQuery("select * from OrderTicketDetails Where Order_Id = '"+orderId+"'AND " + DBFeilds.ATTENDEE_BADGABLE + " = 'B - Badge'", null);
        // Cursor c = this.db.rawQuery("select * from OrderDetails Where Order_Id = '" + orderId + "'AND " + DBFeilds.ATTENDEE_BADGABLE + " = 'B - Badge'", null);
        try {//AND " + DBFeilds.ATTENDEE_BADGABLE + " = 'B - Badge'
            //Log.i("-----Size of Order--", new StringBuilder(String.valueOf(orderId)).append(" Count--").append(c.getCount()).toString());
            if (c.getCount() > 0) {
                c.moveToFirst();
                return  c;
            }
            //   Util.getAttendeeDataCursorForScan();
            // c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Cursor getAttendeeDataCursor(String whereClause, String item_pool_id, String activity_name, String session_id) {
        //("Ticket_Status = 'Booked' or Ticket_Status = 'Wait List' ")+"
        try {
            if (whereClause.contains("where")) {
                whereClause = whereClause.replace("where", ITransaction.EMPTY_STRING);
            } else if (whereClause.contains("Where")) {
                whereClause = whereClause.replace("Where", ITransaction.EMPTY_STRING);
            }
            if (whereClause.contains("innercondition")) {
                whereClause = whereClause.replace("innercondition", "where");
            }
            /*if(whereClause.contains(" GROUP BY Attendee_Email_Id,Attendee_First_Name,Attendee_Last_Name")) {
               //&&!Util.NullChecker(item_pool_id).isEmpty()&&!Util.NullChecker(session_id).isEmpty()
                String[] sai = whereClause.split("GROUP BY Attendee_Email_Id,Attendee_First_Name,Attendee_Last_Name");
                //int i=sai.length;
                whereClause="";
                for(int i=0;i<sai.length;i++){
                    whereClause=whereClause+sai[i];
                }

            }*/
            String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND " + DBFeilds.ATTENDEE_BADGABLE + " = 'B - Badge' AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";//DBFeilds.ATTENDEE_BADGE_PARENT_ID + "='' AND " +
            if(whereClause.equals(" where Event_Id='"+BaseActivity.checkedin_event_record.Events.Id+"'  AND (print_status= '') OR (print_status= 'Not Printed') ORDER BY CASE WHEN Scan_Time NOT NULL THEN Scan_Time  ELSE  CheckedInDate END DESC"));
            {
                where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND " + DBFeilds.ATTENDEE_BADGABLE + " = 'B - Badge' AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";

            }String tstatus_condition = ITransaction.EMPTY_STRING;
            if (!Util.NullChecker(item_pool_id).isEmpty()) {
                String condition = ITransaction.EMPTY_STRING;
                //Log.i("---------------itemPool Contains------------",":"+item_pool_id.contains(";")+" : "+item_pool_id);
                if (item_pool_id.contains(";")) {
                    String[] s = item_pool_id.split(";");
                    //Log.i("---------------itemPool Contains------------",":"+s.length+" : "+s);
                    for (int i = 0; i < s.length; i++) {
                        condition = condition + DBFeilds.ATTENDEE_ITEM_POOL_ID + " = '" + s[i].trim() + "'";
                        tstatus_condition = tstatus_condition + "BLN_Session_Item__c = '" + s[i].trim() + "'";
                        if (i != (s.length - 1)) {
                            condition = condition + " OR ";
                            tstatus_condition = tstatus_condition + " OR ";
                        }
                    }
                    //item_pool_id = DBFeilds.ATTENDEE_ITEM_POOL_ID+" = '"+item_pool_id+"'";
                } else {
                    condition = DBFeilds.ATTENDEE_ITEM_POOL_ID + " = '" + item_pool_id + "'";
                    tstatus_condition = "BLN_Session_Item__c = '" + item_pool_id + "'";
                }
                //String condition = DBFeilds.ATTENDEE_ITEM_POOL_ID+" = '"+item_pool_id+"'";
                where_condition = " where (" + condition + ") AND " + " ((item_type_name = '" + ITEM_TYPES.Package
                        + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR ("
                        + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";//("Ticket_Status = 'Booked' or Ticket_Status = 'Wait List' ")+"
                //Log.i("-------------------Attendee Where Condition--------------", ":" + where_condition);
            } /*else if (!BaseActivity.isEventAdmin()) {
                String items = "";
                int i = 0;
                List<ScannedItems> scanned_item_list = Util.db.getScannedItems(BaseActivity.checkedin_event_record.Events.Id);
                for (ScannedItems scan_items : scanned_item_list) {
                    items = items + DBFeilds.ATTENDEE_ITEM_POOL_ID + "='" + scan_items.BLN_Item_Pool__c + "'";
                    if (i != (scanned_item_list.size() - 1)) {
                        items = items + " OR ";
                    }
                    i++;
                }
                if (!items.isEmpty()) {
                    where_condition = " where (" + items + ") AND " + " ((item_type_name = '" + ITEM_TYPES.Package
                            + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR ("
                            + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
                }
            }*/
            if (BaseActivity.NullChecker(activity_name).isEmpty()) {
                where_condition = where_condition + " " + DBFeilds.ATTENDEE_BADGABLE + " = 'B - Badge' AND "+DBFeilds.ATTENDEE_BADGE_PARENT_ID + "='' AND ";
            }
            // Log.i("&&&&&&&&&&&&&-----Attendee Where Condition------&&&&&&&&&&&", ":" +"Select * from OrderTicketDetails"+ where_condition+whereClause);
            // String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND " + DBFeilds.ATTENDEE_BADGABLE + " = 'B - Badge' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " = 'Booked' AND ";

            String innerQuery = "NULL AS Scan_Time";
            if (!Util.NullChecker(session_id).isEmpty()&&!tstatus_condition.trim().isEmpty()) {
                innerQuery = "(Select T_Scan_Time  from  TStatus where (" + tstatus_condition + ") AND T_Event_Id ='" + BaseActivity.checkedin_event_record.Events.Id + "' AND Ticket__c = Attendee_Id AND Islatest__c = 'true' AND Group_Id = '" + session_id + "' LIMIT 1) AS Scan_Time";
            }
            //Log.i("-------------------Attendee Where Condition--------------", ":" + where_condition + whereClause);
            Cursor c = this.db.rawQuery("Select *," + innerQuery + " from OrderTicketDetails" + where_condition + whereClause, null);
            c.moveToFirst();
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getmulticursor(){
        try{
            Cursor c=this.db.rawQuery("Select *,(Select T_Scan_Time  from  TStatus where (BLN_Session_Item__c = 'a1f6C000000gfipQAA') AND T_Event_Id ='a196C0000006PaBQAU' AND Ticket__c = Attendee_Id AND Islatest__c = 'true' AND Group_Id = 'a2V6C0000001hyQUAQ' LIMIT 1) AS Scan_Time from OrderTicketDetails where (Item_Pool_Id = 'a1f6C000000gfipQAA') AND  ((item_type_name = 'Package' AND Parent_Id != 'null') OR (item_type_name != 'Package')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS + " != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND item_badgable = 'B - Badge' AND Badge_Parent_Id='' AND   Event_Id='a196C0000006PaBQAU'  AND Badge_Parent_Id='' ORDER BY CASE  WHEN Scan_Time NOT NULL THEN Scan_Time  ELSE  CheckedInDate END DESC",null);
            c.moveToFirst();
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Cursor getSelfCheckinAttendeeDataCursor(String whereClause, String item_pool_id, String activity_name, String session_id) {
        try {
            if (whereClause.contains("where")) {
                whereClause = whereClause.replace("where", ITransaction.EMPTY_STRING);
            } else if (whereClause.contains("Where")) {
                whereClause = whereClause.replace("Where", ITransaction.EMPTY_STRING);
            }
            if (whereClause.contains("innercondition")) {
                whereClause = whereClause.replace("innercondition", "where");
            }

            String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND " + DBFeilds.ATTENDEE_BADGABLE + " = 'B - Badge' AND (" + DBFeilds.ATTENDEE_TICKET_STATUS + " != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND "+DBFeilds.ATTENDEE_BADGE_PARENT_ID + "='' AND ";//DBFeilds.ATTENDEE_BADGE_PARENT_ID + "='' AND " +
            String tstatus_condition = ITransaction.EMPTY_STRING;
            if (!Util.NullChecker(item_pool_id).isEmpty()) {
                String condition = ITransaction.EMPTY_STRING;
                //Log.i("---------------itemPool Contains------------",":"+item_pool_id.contains(";")+" : "+item_pool_id);
                if (item_pool_id.contains(";")) {
                    String[] s = item_pool_id.split(";");
                    //Log.i("---------------itemPool Contains------------",":"+s.length+" : "+s);
                    for (int i = 0; i < s.length; i++) {
                        condition = condition + DBFeilds.ATTENDEE_ITEM_POOL_ID + " = '" + s[i].trim() + "'";
                        tstatus_condition = tstatus_condition + "BLN_Session_Item__c = '" + s[i].trim() + "'";
                        if (i != (s.length - 1)) {
                            condition = condition + " OR ";
                            tstatus_condition = tstatus_condition + " OR ";
                        }
                    }
                    //item_pool_id = DBFeilds.ATTENDEE_ITEM_POOL_ID+" = '"+item_pool_id+"'";
                } else {
                    condition = DBFeilds.ATTENDEE_ITEM_POOL_ID + " = '" + item_pool_id + "'";
                    tstatus_condition = "BLN_Session_Item__c = '" + item_pool_id + "'";
                }
                //String condition = DBFeilds.ATTENDEE_ITEM_POOL_ID+" = '"+item_pool_id+"'";
                where_condition = " where (" + condition + ") AND " + " ((item_type_name = '" + ITEM_TYPES.Package
                        + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR ("
                        + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS + " != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
            }
            if (BaseActivity.NullChecker(activity_name).isEmpty()) {
                where_condition = where_condition + " " + DBFeilds.ATTENDEE_BADGABLE + " = 'B - Badge' AND ";
            }
            String innerQuery = "NULL AS Scan_Time";
            if (!Util.NullChecker(session_id).isEmpty()) {
                innerQuery = "(Select T_Scan_Time  from  TStatus where (" + tstatus_condition + ") AND T_Event_Id ='" + BaseActivity.checkedin_event_record.Events.Id + "' AND Ticket__c = Attendee_Id AND Islatest__c = 'true' AND Group_Id = '" + session_id + "' LIMIT 1) AS Scan_Time";
            }
            Cursor c = this.db.rawQuery("Select *," + innerQuery + " from OrderTicketDetails" + where_condition + whereClause, null);
            c.moveToFirst();
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getAttendeeDetailsForPrint(String whereClause) {
        try {
            Cursor c = this.db.rawQuery("Select * from OrderTicketDetails"  + whereClause, null);
            c.moveToFirst();
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Cursor getAttendeeDetails(String whereClause) {
        try {
            if (whereClause.contains("where")) {
                whereClause = whereClause.replace("where", ITransaction.EMPTY_STRING);
            } else if (whereClause.contains("Where")) {
                whereClause = whereClause.replace("Where", ITransaction.EMPTY_STRING);
            }

            String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";


            /*if(!BaseActivity.isEventAdmin()){
				List<ScannedItems> scanned_item_list = Util.db.getScannedItems(BaseActivity.checkedin_event_record.Events.Id);
				String items = "";
				int i = 0;
				for(ScannedItems scan_items: scanned_item_list){
					items =  items+DBFeilds.ATTENDEE_ITEM_POOL_ID+"='"+scan_items.BLN_Item_Pool__c+"'";

					if(i != (scanned_item_list.size() - 1)){
					   items = items+" OR ";
					}
					i++;
				}
				if(!items.isEmpty()){
				   where_condition = " where ("+items+") AND "+" ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND " + DBFeilds.ATTENDEE_BADGABLE + " = 'B - Badge' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " = 'Booked' AND ";
				}
			}*/
            //Log.i("-------------------Attendee Where Condition--------------", ":" + where_condition+whereClause);
            // String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND " + DBFeilds.ATTENDEE_BADGABLE + " = 'B - Badge' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " = 'Booked' AND ";
            //Log.i("-------------------Attendee Where Condition--------------", ":" + where_condition + whereClause);
            Cursor c = this.db.rawQuery("Select * from OrderTicketDetails" + where_condition + whereClause, null);
            c.moveToFirst();
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Cursor getAllAttendeeswithBadgeId(String whereClause) {
        try {
            String badgeid="";

            if (whereClause.contains("where")) {
                whereClause = whereClause.replace("where", ITransaction.EMPTY_STRING);
            } else if (whereClause.contains("Where")) {
                whereClause = whereClause.replace("Where", ITransaction.EMPTY_STRING);
            }else {
                whereClause = "  Event_Id='" + BaseActivity.checkedin_event_record.Events.Id + "' AND ("+ DBFeilds.ATTENDEE_ID + "='" + whereClause.trim() +"') AND (" + DBFeilds.ATTENDEE_TICKET_STATUS + " != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted')";
            }
            String where_condition = " where  Event_Id='" + BaseActivity.checkedin_event_record.Events.Id +"' AND ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND "; //+ DBFeilds.ATTENDEE_TICKET_STATUS + " = 'Booked' AND ";
            // Log.i("-------------------Attendee Where Condition--------------", ":" + where_condition+whereClause);
            Cursor c = this.db.rawQuery("Select "+DBFeilds.ATTENDEE_BADGEID+" from OrderTicketDetails" + where_condition + whereClause, null);
            if(c.getCount()>0) {
                c.moveToFirst();
                badgeid=c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGEID));
                if(!Util.NullChecker(badgeid).isEmpty()) {
                    c = this.db.rawQuery("Select * from OrderTicketDetails" + where_condition + DBFeilds.ATTENDEE_BADGEID + "='" + badgeid + "'", null);
                }else{
                    c = this.db.rawQuery("Select * from OrderTicketDetails" + where_condition + whereClause, null);

                }
            }

            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public Cursor getAttendeeDetailsWithAllTypes(String whereClause) {
        try {
            if (whereClause.contains("where")) {
                whereClause = whereClause.replace("where", ITransaction.EMPTY_STRING);
            } else if (whereClause.contains("Where")) {
                whereClause = whereClause.replace("Where", ITransaction.EMPTY_STRING);
            }
            String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND "; //+ DBFeilds.ATTENDEE_TICKET_STATUS + " = 'Booked' AND ";
            // Log.i("-------------------Attendee Where Condition--------------", ":" + where_condition+whereClause);
            Cursor c = this.db.rawQuery("Select * from OrderTicketDetails" + where_condition + whereClause, null);
            c.moveToFirst();
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getAttendeeDataCursorForScan(String whereClause) {
        try {
            if (whereClause.contains("where")) {
                whereClause = whereClause.replace("where", ITransaction.EMPTY_STRING);
            } else if (whereClause.contains("Where")) {
                whereClause = whereClause.replace("Where", ITransaction.EMPTY_STRING);
            }

            String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
            //String items = Util.db.getSwitchedOnScanItem(BaseActivity.checkedin_event_record.Events.Id);
            SFDCDetails sfdcddetails = getSFDCDDETAILS();
            ExternalSettings ext_settings = new ExternalSettings();
            if (!Util.external_setting_pref.getString(sfdcddetails.user_id + BaseActivity.checkedin_event_record.Events.Id, "").isEmpty()) {
                ext_settings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id + BaseActivity.checkedin_event_record.Events.Id, ""), ExternalSettings.class);
            }

            if (ext_settings.quick_checkin) {
                List<ScannedItems> scanned_item_list = Util.db.getSwitchedOnScanItem(BaseActivity.checkedin_event_record.Events.Id);
                String items = ITransaction.EMPTY_STRING;
                int i = 0;
                for (ScannedItems scan_items : scanned_item_list) {
                    if (Util.db.isItemPoolFreeSession(scan_items.BLN_Item_Pool__c, BaseActivity.checkedin_event_record.Events.Id)) {
                        break;
                    }
                    items = items + DBFeilds.ATTENDEE_ITEM_POOL_ID + "='" + scan_items.BLN_Item_Pool__c + "'";
                    if (i != (scanned_item_list.size() - 1)) {
                        items = items + " OR ";
                    }
                    i++;
                }
                if (!items.isEmpty()) {
                    where_condition = " where (" + items + ") AND " + " ((item_type_name = '" + ITEM_TYPES.Package
                            + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR ("
                            + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS + " != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
                }
            }



     			/*if(!items.isEmpty()){
    			   items = DBFeilds.ATTENDEE_ITEM_POOL_ID+"='"+items+"'";
    			   where_condition = " where ("+items+") AND "+" ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND "  + DBFeilds.ATTENDEE_TICKET_STATUS + " = 'Booked' AND ";
    			}*/


            // Log.i("-------------------Attendee Where Condition--------------", ":" + where_condition+whereClause);
            // String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND " + DBFeilds.ATTENDEE_BADGABLE + " = 'B - Badge' AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
            //Log.i("-------------------Attendee Where Condition--------------", ":" + where_condition + whereClause);
            Cursor c = this.db.rawQuery("Select * from OrderTicketDetails" + where_condition + whereClause, null);
            c.moveToFirst();
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getAllTypeAttendeeCursor(String whereClause) {
        Cursor c = this.db.rawQuery("Select * from OrderTicketDetails " + whereClause, null);
        c.moveToFirst();
        return c;
    }

    public boolean isCustomBarcode(String custom_barcode) {
        Cursor c = db.rawQuery("select * from " + DBFeilds.TABLE_ATTENDEE_DETAILS + " where " + DBFeilds.ATTENDEE_CUSTOM_BARCODE + " = '" + custom_barcode + "'", null);
        if (c.moveToFirst()) {
            c.close();
            return true;
        } else {
            c.close();
        }
        return false;
    }

    public String getAttendeeName(String badgeid_or_custombarcode) {
        Cursor c = db.rawQuery("select " + DBFeilds.ATTENDEE_FIRST_NAME + "," + DBFeilds.ATTENDEE_LAST_NAME + " from " + DBFeilds.TABLE_ATTENDEE_DETAILS + " where (" + DBFeilds.ATTENDEE_BADGEID + " = '" + badgeid_or_custombarcode + "' OR " + DBFeilds.ATTENDEE_CUSTOM_BARCODE + " = '" + badgeid_or_custombarcode + "' OR " + DBFeilds.ATTENDEE_ID + " = '" + badgeid_or_custombarcode + "')", null);
        if (c.moveToFirst()) {
            String email = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " " + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));;
            c.close();
            return email;
        } else {
            c.close();
        }
        return ITransaction.EMPTY_STRING;
    }
    public boolean isAttendeeValidForSession(String badgeid_or_custombarcode) {
        Boolean hasPermission=false;
        Cursor c = db.rawQuery("select " + DBFeilds.ATTENDEE_ITEM_POOL_ID  + " from " + DBFeilds.TABLE_ATTENDEE_DETAILS + " where (" + DBFeilds.ATTENDEE_BADGEID + " = '" + badgeid_or_custombarcode + "' OR " + DBFeilds.ATTENDEE_CUSTOM_BARCODE + " = '" + badgeid_or_custombarcode + "' OR " + DBFeilds.ATTENDEE_ID + " = '" + badgeid_or_custombarcode + "')", null);
        if (c.moveToFirst()) {
            String itempoolid = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID));
            hasPermission=isItemPoolSwitchON(itempoolid,BaseActivity.checkedin_event_record.Events.Id);
            c.close();
            return hasPermission;
        } else {
            c.close();
        }
        return hasPermission;
    }
    public String getAttendeeBadgeParentPrintStatus(String badgeparentid) {
        try {
            String status;
            Cursor c = db.rawQuery("select " + DBFeilds.ATTENDEE_BADGE_PRINTSTATUS + " from " + DBFeilds.TABLE_ATTENDEE_DETAILS + " where (" + DBFeilds.ATTENDEE_ID + " = '" + badgeparentid + "')", null);
            if (c.moveToFirst()) {
                status = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS));
                c.close();
                return status;
            } else {
                c.close();
            }
            return ITransaction.EMPTY_STRING;
        }catch (Exception e){
            return "Not Printed";
        }
    }
    public String getAttendeeBadgePrintStatus(String badgeid) {
        try {
            String status;
            Cursor c = db.rawQuery("select " + DBFeilds.ATTENDEE_BADGE_PRINTSTATUS + " from " + DBFeilds.TABLE_ATTENDEE_DETAILS + " where (" + DBFeilds.ATTENDEE_ID + " = '" + badgeid + "')", null);
            if (c.moveToFirst()) {
                status = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS));
                c.close();
                return status;
            } else {
                c.close();
            }
            return ITransaction.EMPTY_STRING;
        }catch (Exception e){
            return "Not Printed";
        }
    }
    public List<OrderItemListHandler> getChildTicketIds(String ticketid) {
        List<OrderItemListHandler> child_ticketids = new ArrayList<>();
        try {
            //Log.i("-------------Query-------------",":"+"Select * from ItemDetails item,ItemPoolDetails item_pool  where item."+DBFeilds.ADDED_ITEM_POOLID+" = item_pool."+DBFeilds.ITEMPOOL_ID+" AND " + DBFeilds.ADDED_ITEM_PAYMENTTYPE +" = 'Free' AND "+DBFeilds.ADDED_ITEM_EVENTID+"='"+event_id+"' AND item_pool."+DBFeilds.ITEMPOOL_ALLOW_TICKETED_SESSION+"='true'");
            Cursor c = db.rawQuery("Select  " + DBFeilds.ATTENDEE_ITEM_POOL_ID +","+DBFeilds.ATTENDEE_BADGE_LABLE+","+DBFeilds.ATTENDEE_ID+ " from "+ DBFeilds.TABLE_ATTENDEE_DETAILS +" where Badge_parent_Id "+" = '"+ticketid+"' OR "+DBFeilds.ATTENDEE_ID+" = '"+ticketid+"'", null);
            for(int i = 0;i<c.getCount();i++){
                c.moveToPosition(i);
                OrderItemListHandler orderItemListHandler=new OrderItemListHandler();
                orderItemListHandler.setItemPoolId(c.getString(c.getColumnIndex("Item_Pool_Id")));
                orderItemListHandler.setBadgeLabel(c.getString(c.getColumnIndex("Badge_Label__c")));
                /*child_ticketids.add(c.getString(c.getColumnIndex("Item_Pool_Id")));
                child_ticketids.add(c.getString(c.getColumnIndex("Badge_Label__c")));*/
                child_ticketids.add(orderItemListHandler);
            }
            c.close();

            return child_ticketids;
        }catch (Exception e){
            return null;
        }
    }

    public String getAttendeeBadgeParentTicketName(String badgeparentid) {
        try {
            String status;
            Cursor c = db.rawQuery("select " + DBFeilds.ATTENDEE_ITEMPOOL_NAME + " from " + DBFeilds.TABLE_ATTENDEE_DETAILS + " where (" + DBFeilds.ATTENDEE_ID + " = '" + badgeparentid + "')", null);
            if (c.moveToFirst()) {
                status = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME));
                c.close();
                return status;
            } else {
                c.close();
            }
            return ITransaction.EMPTY_STRING;
        }catch (Exception e){
            return ITransaction.EMPTY_STRING;
        }
    }
    public String getAttendeeParentTicketBadgeLabel(String ticketid) {
        try {
            String status;
            Cursor c = db.rawQuery("select " + DBFeilds.ATTENDEE_BADGE_LABLE + " from " + DBFeilds.TABLE_ATTENDEE_DETAILS + " where (" + DBFeilds.ATTENDEE_ID + " = '" + ticketid + "')", null);
            if (c.moveToFirst()) {
                status = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE));
                c.close();
                return status;
            } else {
                c.close();
            }
            return ITransaction.EMPTY_STRING;
        }catch (Exception e){
            return ITransaction.EMPTY_STRING;
        }
    }
    public String getAttendeeParentTicketBadgeId(String ticketid) {
        try {
            String status;
            Cursor c = db.rawQuery("select " + DBFeilds.ATTENDEE_BADGEID + " from " + DBFeilds.TABLE_ATTENDEE_DETAILS + " where (" + DBFeilds.ATTENDEE_ID + " = '" + ticketid + "')", null);
            if (c.moveToFirst()) {
                status = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGEID));
                c.close();
                return status;
            } else {
                c.close();
            }
            return ITransaction.EMPTY_STRING;
        }catch (Exception e){
            return ITransaction.EMPTY_STRING;
        }
    }
    public boolean isParentTicketexistsinDB(String ticketid) {
        try {
            Cursor c = db.rawQuery("select " + DBFeilds.ATTENDEE_BADGEID + " from " + DBFeilds.TABLE_ATTENDEE_DETAILS + " where (" + DBFeilds.ATTENDEE_ID + " = '" + ticketid + "')", null);
            if (c.getCount()>0) {
                c.close();
                return true;
            } else {
                c.close();
                return false;
            }
        }catch (Exception e){
            return false;
        }
    }

    public String getAttendeeCompany(String badgeid_or_custombarcode) {
        Cursor c = db.rawQuery("select " + DBFeilds.ATTENDEE_COMPANY + " from " + DBFeilds.TABLE_ATTENDEE_DETAILS + " where (" + DBFeilds.ATTENDEE_BADGEID + " = '" + badgeid_or_custombarcode + "' OR " + DBFeilds.ATTENDEE_CUSTOM_BARCODE + " = '" + badgeid_or_custombarcode + "' OR " + DBFeilds.ATTENDEE_ID + " = '" + badgeid_or_custombarcode + "')", null);
        if (c.moveToFirst()) {
            String email = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_COMPANY));
            c.close();
            return email;
        } else {
            c.close();
        }
        return ITransaction.EMPTY_STRING;
    }

    public String getAttendeeisBadgePrinted(String attendee_id) {
        Cursor c = db.rawQuery("select " + DBFeilds.ATTENDEE_BADGE_PRINTSTATUS + " from " + DBFeilds.TABLE_ATTENDEE_DETAILS + " where " + DBFeilds.ATTENDEE_ID + " = '"+attendee_id+ "'", null);
        if (c.moveToFirst()) {
            String status = Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS)));
            c.close();
            return status;
        } else {
            c.close();
        }
        return ITransaction.EMPTY_STRING;
    }
    public String getAttendeeWorkorMobile(String attendee_id) {
        Cursor c = db.rawQuery("select Attendee_Phone,Attendee_Mobile from " + DBFeilds.TABLE_ATTENDEE_DETAILS + " where " + DBFeilds.ATTENDEE_ORDER_ID + " = '"+attendee_id+"'", null);
        if (c.moveToFirst()) {
            String status = Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_WORK_PHONE)));
            if(status.isEmpty())
                status = Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_MOBILE)));
            c.close();
            return status;
        } else {
            c.close();
        }
        return ITransaction.EMPTY_STRING;
    }
    /*public String getAttendeeisBadgePrinted(String attendee_id) {
        Cursor c = db.rawQuery("select " + DBFeilds.ATTENDEE_BADGEID + " from " + DBFeilds.TABLE_ATTENDEE_DETAILS + " where " + DBFeilds.ATTENDEE_ID + " = '" + attendee_id + "'", null);
        if (c.moveToFirst()) {
            String email = Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)));
            c.close();
            return email;
        } else {
            c.close();
        }
        return ITransaction.EMPTY_STRING;
    }*/

    public String getAttendeeNameWithId(String attendee_id) {
        Cursor c = db.rawQuery("select " + DBFeilds.ATTENDEE_FIRST_NAME + "," + DBFeilds.ATTENDEE_LAST_NAME + " from " + DBFeilds.TABLE_ATTENDEE_DETAILS + " where " + DBFeilds.ATTENDEE_ID + " = '"+attendee_id+ "'", null);
        if (c.moveToFirst()) {
            String name = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " " + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
            ;
            c.close();
            return name;
        } else {
            c.close();
        }
        return ITransaction.EMPTY_STRING;
    }
    public Cursor getAttendeeDataCursorForBuyers(String whereClause) {
        try {
            if (whereClause.contains("where")) {
                whereClause = whereClause.replace("where", ITransaction.EMPTY_STRING);
            } else if (whereClause.contains("Where")) {
                whereClause = whereClause.replace("Where", ITransaction.EMPTY_STRING);
            }
            String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
            Cursor c = this.db.rawQuery("Select * from OrderTicketDetails" + where_condition + whereClause, null);
            c.moveToFirst();
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getAttendeeDataCursorForNonBadgable(String whereClause) {
        try {
            if (whereClause.contains("where")) {
                whereClause = whereClause.replace("where", ITransaction.EMPTY_STRING);
            } else if (whereClause.contains("Where")) {
                whereClause = whereClause.replace("Where", ITransaction.EMPTY_STRING);
            }
            String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
            // Log.i("-------------------Attendee Where Condition--------------", ":" + where_condition + whereClause);
           /* if (!BaseActivity.isEventAdmin()) {
                String items = "";
                int i = 0;
                List<ScannedItems> scanned_item_list = Util.db.getScannedItems(BaseActivity.checkedin_event_record.Events.Id);
                for (ScannedItems scan_items : scanned_item_list) {
                    items = items + DBFeilds.ATTENDEE_ITEM_POOL_ID + "='" + scan_items.BLN_Item_Pool__c + "'";
                    if (i != (scanned_item_list.size() - 1)) {
                        items = items + " OR ";
                    }
                    i++;
                }
                if (!items.isEmpty()) {
                    where_condition = " where (" + items + ") AND " + " ((item_type_name = '" + ITEM_TYPES.Package
                            + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR ("
                            + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS + " != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
                }
            }*/
            Cursor c = this.db.rawQuery("Select * from OrderTicketDetails" + where_condition + whereClause, null);
            c.moveToFirst();
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Cursor getAttendeeswithUnique(String Firstname,String Lastname,String mailId,String Eventid){
        try {
            if(Firstname.contains("'")){
                Firstname=  Firstname.replaceAll("'","''");
            }if(Lastname.contains("'")){
                Lastname=Lastname.replaceAll("'","''");
            }if(mailId.contains("'")){
                mailId= mailId.replaceAll("'","''");
            }
            String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
           /* if (!BaseActivity.isEventAdmin()) {
                String items = "";
                int i = 0;
                List<ScannedItems> scanned_item_list = Util.db.getScannedItems(BaseActivity.checkedin_event_record.Events.Id);
                for (ScannedItems scan_items : scanned_item_list) {
                    items = items + DBFeilds.ATTENDEE_ITEM_POOL_ID + "='" + scan_items.BLN_Item_Pool__c + "'";
                    if (i != (scanned_item_list.size() - 1)) {
                        items = items + " OR ";
                    }
                    i++;
                }
                if (!items.isEmpty()) {
                    where_condition = " where (" + items + ") AND " + " ((item_type_name = '" + ITEM_TYPES.Package
                            + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR ("
                            + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS + " != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
                }
            } */ // Log.i("-------------------Attendee Where Condition--------------", ":" + where_condition + whereClause);
            Cursor c = this.db.rawQuery("Select * from OrderTicketDetails "+where_condition+"" +
                    " Attendee_Email_Id= '"+mailId +"' AND "
                    +DBFeilds.ATTENDEE_FIRST_NAME+"= '"+Firstname +"' AND "+DBFeilds.ATTENDEE_LAST_NAME+"= '"+Lastname+"' AND "+" Event_Id ='"+Eventid+"'",null);//AND item_badgable='B - Badge'*/

            c.moveToFirst();
            return c;
          /*  Cursor cursor=this.db.rawQuery("Select count(*) from OrderTicketDetails where Ticket_Status='Booked' AND Attendee_Email_Id= '"+mailId +"' AND "
                    +"Attendee_First_Name= '"+Firstname +"' AND "+"Attendee_Last_Name= '"+Lastname+"' ",null);//AND item_badgable='B - Badge'*/

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getTStatusCountWithOrder(String order_id, String event_id, String status, String group_id) {
        int status_count = 0;
        try {
            if (BaseActivity.NullChecker(group_id).isEmpty()) {
                group_id = getSwitchedONGroupId(event_id);
            }
            Cursor c = db.rawQuery("SELECT count(*) FROM OrderTicketDetails,TStatus where ((item_type_name = 'Package' AND   Parent_Id != 'null') OR (item_type_name != 'Package ')) AND  (" + DBFeilds.ATTENDEE_TICKET_STATUS + " != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND Order_Id = '"
                    + order_id + "' AND Event_Id = '" + event_id + "' AND Attendee_Id = Ticket__c AND Group_Id = '" + group_id + "' AND Tstatus_name__c = '" + status + "' AND Islatest__c = 'true'", null);
            if (c.moveToFirst()) {
                status_count = c.getInt(0);
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return status_count;
    }

    public int getTstatusCountForItemPoolId(String group_id, String item_pool_id, String event_id, String status, String userID) {
        int tstatus_count = 0;
        try {
            String condition = ITransaction.EMPTY_STRING;
            String query = "Select count(*) From TStatus where (" + condition + ") AND Group_Id = '" + group_id + "' AND Tstatus_name__c = '" + status + "' AND T_Event_Id = '" + event_id + "' AND Islatest__c='true'";
            if (BaseActivity.NullChecker(item_pool_id).isEmpty()) {
                if (userID != null && userID.equals("no permission")) {
                    query = "Select count(*) From TStatus where Group_Id = '" + group_id + "' AND Tstatus_name__c = '" + status + "' AND T_Event_Id = '" + event_id + "' AND Islatest__c='true'";
                } else {
                    query = "Select count(*) From TStatus where Group_Id = '" + group_id + "' AND Tstatus_name__c = '" + status + "' AND T_Event_Id = '" + event_id + "' AND Islatest__c='true' AND T_User_Id = '" + userID + "'";
                }

            } else if (item_pool_id.contains(";")) {
                String[] s = item_pool_id.split(";");
                for (int i = 0; i < s.length; i++) {
                    condition = condition + "BLN_Session_Item__c = '" + s[i] + "'";
                    if (i != (s.length - 1)) {
                        condition = condition + " OR ";
                    }
                }
                if (userID != null && userID.equals("no permission")) {
                    query = "Select count(*) From TStatus where (" + condition + ") AND Group_Id = '" + group_id + "' AND Tstatus_name__c = '" + status + "' AND T_Event_Id = '" + event_id + "' AND Islatest__c='true'";
                } else {
                    query = "Select count(*) From TStatus where (" + condition + ") AND Group_Id = '" + group_id + "' AND Tstatus_name__c = '" + status + "' AND T_Event_Id = '" + event_id + "' AND Islatest__c='true' AND T_User_Id = '" + userID + "'";
                }

            } else {
                condition = "BLN_Session_Item__c = '" + item_pool_id + "'";
                if (userID != null && userID.equals("no permission")) {
                    query = "Select count(*) From TStatus where (" + condition + ") AND Group_Id = '" + group_id + "' AND Tstatus_name__c = '" + status + "' AND T_Event_Id = '" + event_id + "' AND Islatest__c='true'";
                } else {
                    query = "Select count(*) From TStatus where (" + condition + ") AND Group_Id = '" + group_id + "' AND Tstatus_name__c = '" + status + "' AND T_Event_Id = '" + event_id + "' AND Islatest__c='true' AND T_User_Id = '" + userID + "'";
                }
            }

            //Log.i("----------------Tstatus count-------------",":"+query);
            Cursor c = db.rawQuery(query, null);

            if (c.moveToFirst()) {
                tstatus_count = c.getInt(0);
            }
            c.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return tstatus_count;
    }

    public String getBuyerName(String order_id) {
        String name = ITransaction.EMPTY_STRING;
        try {
            Cursor c = db.rawQuery("select " + DBFeilds.USER_FIRST_NAME + "," + DBFeilds.USER_LAST_NAME + " from " + DBFeilds.TABLE_USER + " where " + DBFeilds.ORDER_ORDER_ID + " = '" + order_id + "'", null);
            if (c.moveToFirst()) {
                name = c.getString(c.getColumnIndex(DBFeilds.USER_FIRST_NAME)) + " " + c.getString(c.getColumnIndex(DBFeilds.USER_LAST_NAME));
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return name;
    }


    public int getAttendeeTicketsCount(String attendee_id, String event_id) {
        int attendees = 1;
        String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";

        /*if (!BaseActivity.isEventAdmin()) {
            where_condition=getWhereconditionforNonAdmin();
        }*/
        Cursor c = this.db.rawQuery("select count(*) from OrderTicketDetails "+where_condition+" Attendee_Id='"+attendee_id+"' OR Badge_Parent_Id = '"+attendee_id+"' AND Event_Id = '" + event_id + "'", null);
        if (c == null) {
            return attendees;
        } else if (c.moveToFirst()) {
            attendees = c.getInt(0);
            c.close();
        }

        return attendees;
    }
    public int getAllTicketsCountwithMailId(String mailId,String Firstname,String Lastname,String event_id){
        if(Firstname.contains("'")){
            Firstname=  Firstname.replaceAll("'","''");
        }if(Lastname.contains("'")){
            Lastname=Lastname.replaceAll("'","''");
        }if(mailId.contains("'")){
            mailId= mailId.replaceAll("'","''");
        }
        int count=0;
        String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
        /*if (!BaseActivity.isEventAdmin()) {
            where_condition=getWhereconditionforNonAdmin();
        }*/
        Cursor cursor=this.db.rawQuery("Select count(*) from OrderTicketDetails "+where_condition+DBFeilds.ATTENDEE_EMAIL_ID+" = '"+mailId +"' AND "
                +DBFeilds.ATTENDEE_FIRST_NAME+"= '"+Firstname +"' AND "+DBFeilds.ATTENDEE_LAST_NAME+"= '"+Lastname+"' "+" AND Event_Id = '" + event_id + "'",null);//AND item_badgable='B - Badge'
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    /*public String getWhereconditionforNonAdmin(){
        String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
        if (!BaseActivity.isEventAdmin()) {
            String items = "";
            int i = 0;
            List<ScannedItems> scanned_item_list = Util.db.getScannedItems(BaseActivity.checkedin_event_record.Events.Id);
            for (ScannedItems scan_items : scanned_item_list) {
                items = items + DBFeilds.ATTENDEE_ITEM_POOL_ID + "='" + scan_items.BLN_Item_Pool__c + "'";
                if (i != (scanned_item_list.size() - 1)) {
                    items = items + " OR ";
                }
                i++;
            }
            if (!items.isEmpty()) {
                where_condition = " where (" + items + ") AND " + " ((item_type_name = '" + ITEM_TYPES.Package
                        + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR ("
                        + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS + " != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
            }
        }
        return where_condition;
    }*/
    public int getAttendeeCheckinTicketCountforParent(String attendee_id, String event_id,String sessionid) {
        int checkin_attendees = 0;
        String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
        /*if (!BaseActivity.isEventAdmin()) {
            where_condition=getWhereconditionforNonAdmin();
        } */
        Cursor c = this.db.rawQuery("select Attendee_Id,Item_Pool_Id from OrderTicketDetails"+where_condition +" Attendee_Id = '" + attendee_id + "' OR "
                +DBFeilds.ATTENDEE_BADGE_PARENT_ID +" = '"+attendee_id+"' "+" AND Event_Id = '" + event_id + "'", null);
        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            String tstatus = getTStatusBasedOnSessionId(c.getString(c.getColumnIndex("Attendee_Id")),
                    c.getString(c.getColumnIndex("Item_Pool_Id")), event_id,sessionid);
            if (AppUtils.NullChecker(tstatus).equalsIgnoreCase("true")) {
                checkin_attendees = checkin_attendees + 1;
            }
        }
        c.close();
        return checkin_attendees;
    }
    public int getAttendeeCheckOutTicketCountforParent(String attendee_id, String event_id,String sessionid) {
        int checkin_attendees = 0;
        String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
        /*if (!BaseActivity.isEventAdmin()) {
            where_condition=getWhereconditionforNonAdmin();
        }*/ Cursor c = this.db.rawQuery("select Attendee_Id,Item_Pool_Id from OrderTicketDetails"+where_condition +" Attendee_Id = '" + attendee_id + "' OR "
                +DBFeilds.ATTENDEE_BADGE_PARENT_ID +" = '"+attendee_id+"' "+" AND Event_Id = '" + event_id + "'", null);
        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            String tstatus = getTStatusBasedOnSessionId(c.getString(c.getColumnIndex("Attendee_Id")),
                    c.getString(c.getColumnIndex("Item_Pool_Id")), event_id,sessionid);
            if (AppUtils.NullChecker(tstatus).equalsIgnoreCase("false")) {
                checkin_attendees = checkin_attendees + 1;
            }
        }
        c.close();
        return checkin_attendees;
    }

    /* public int getAttendeeCheckinTicketsCount(String attendee_id, String event_id,String sessionid) {
         int checkin_attendees = 0;
         String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
         if (!BaseActivity.isEventAdmin()) {
             where_condition=getWhereconditionforNonAdmin();
         } Cursor c = this.db.rawQuery("select Attendee_Id,Item_Pool_Id from OrderTicketDetails"+where_condition +" Attendee_Id = '" + attendee_id + "' AND Event_Id = '" + event_id + "'", null);
         for (int i = 0; i < c.getCount(); i++) {
             c.moveToPosition(i);
             String tstatus = getTStatusBasedOnSessionId(c.getString(c.getColumnIndex("Attendee_Id")),
                     c.getString(c.getColumnIndex("Item_Pool_Id")), event_id,sessionid);
             if (AppUtils.NullChecker(tstatus).equalsIgnoreCase("true")) {
                 checkin_attendees = checkin_attendees + 1;
             }
         }
         c.close();
         return checkin_attendees;
     }*/
    public int getAttendeeCheckinsUniqueCount(String mailId,String Firstname,String Lastname,String event_id,String session_id) {
        try {
            int checkin_attendees = 0;
            if (Firstname.contains("'")) {
                Firstname = Firstname.replaceAll("'", "''");
            }
            if (Lastname.contains("'")) {
                Lastname = Lastname.replaceAll("'", "''");//Lastname.replaceAll("'","\'");
            }
            if (mailId.contains("'")) {
                mailId = mailId.replaceAll("'", "''");
            }
            String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
            /*if (!BaseActivity.isEventAdmin()) {
                where_condition = getWhereconditionforNonAdmin();
            }*/
            Cursor c = this.db.rawQuery("select Attendee_Id,Item_Pool_Id from OrderTicketDetails " + where_condition + DBFeilds.ATTENDEE_EMAIL_ID+" = '" + mailId + "' AND "
                    +DBFeilds.ATTENDEE_FIRST_NAME+ "= '" + Firstname + "' AND " + DBFeilds.ATTENDEE_LAST_NAME+"= '" + Lastname + "' ", null);
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                String tstatus = getTStatusBasedOnSessionId(c.getString(c.getColumnIndex("Attendee_Id")), c.getString(c.getColumnIndex("Item_Pool_Id")), event_id, session_id);
                if (AppUtils.NullChecker(tstatus).equalsIgnoreCase("true")) {
                    checkin_attendees = checkin_attendees + 1;
                }
            }

            c.close();
            return checkin_attendees;
        }

        catch (Exception e){
            e.printStackTrace();
            return 1;
        }
    }
    public int getAttendeeCheckoutsUniqueCount(String mailId,String Firstname,String Lastname,String event_id,String session_id) {
        try {
            int checkin_attendees = 0;
            if (Firstname.contains("'")) {
                Firstname = Firstname.replaceAll("'", "''");
            }
            if (Lastname.contains("'")) {
                Lastname = Lastname.replaceAll("'", "''");//Lastname.replaceAll("'","\'");
            }
            if (mailId.contains("'")) {
                mailId = mailId.replaceAll("'", "''");
            }
            String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND (" + DBFeilds.ATTENDEE_TICKET_STATUS +" != 'Cancelled' AND " + DBFeilds.ATTENDEE_TICKET_STATUS +" !='Abandoned' AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " !='Deleted') AND ";
            /*if (!BaseActivity.isEventAdmin()) {
                where_condition = getWhereconditionforNonAdmin();
            }*/
            Cursor c = this.db.rawQuery("select Attendee_Id,Item_Pool_Id from OrderTicketDetails " + where_condition + DBFeilds.ATTENDEE_EMAIL_ID+" = '" + mailId + "' AND "
                    +DBFeilds.ATTENDEE_FIRST_NAME+ "= '" + Firstname + "' AND " +DBFeilds.ATTENDEE_LAST_NAME+ "= '" + Lastname + "' ", null);
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                String tstatus = getTStatusBasedOnSessionId(c.getString(c.getColumnIndex("Attendee_Id")), c.getString(c.getColumnIndex("Item_Pool_Id")), event_id, session_id);
                if (AppUtils.NullChecker(tstatus).equalsIgnoreCase("false")) {
                    checkin_attendees = checkin_attendees + 1;
                }
            }

            c.close();
            return checkin_attendees;
        }

        catch (Exception e){
            e.printStackTrace();
            return 1;
        }
    }

    public Cursor getOrderDetailsCursor(String whereClause) {
        try {
            if (whereClause.contains("where")) {
                whereClause = whereClause.replace("where", ITransaction.EMPTY_STRING);
            } else if (whereClause.contains("Where")) {
                whereClause = whereClause.replace("Where", ITransaction.EMPTY_STRING);
            }
            String where_condition = " where ((item_type_name = '" + ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR (" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + ITEM_TYPES.Package + "')) AND ";
            //Log.i("-------------------Attendee Where Condition--------------", ":" + where_condition + whereClause);
            Cursor c = this.db.rawQuery("Select * from OrderTicketDetails" + where_condition + whereClause, null);
            c.moveToFirst();
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void upadteOrderList(ArrayList<OrderDetailsHandler> order_list, String eventId) {
        try {
            /// SQLiteStatement stmt = db.compileStatement(sql);

            db.execSQL("PRAGMA cache_size=16000");
            db.execSQL("PRAGMA synchronous=OFF");
            db.execSQL("PRAGMA count_changes=OFF");
            this.db.beginTransaction();
            Iterator it = order_list.iterator();

            int i = 0;
            while (it.hasNext()) {
                OrderDetailsHandler orders = (OrderDetailsHandler) it.next();
                InsertAndUpdateAttendee(orders.ticketsInn, Util.NullChecker(orders.orderInn.getOrderName()));//add string for order name search
                InsertAndUpdateAttendee(orders.cancelledTickets, Util.NullChecker(orders.orderInn.getOrderName()));
                InsertAndUpdateMultiplePayment(orders.paymentInnmultiple, eventId);
                InsertAndUpdateOrder(orders.orderInn);
                InsertAndUpdateAttendeeListValues(orders.listValues);
                //orders.orderInn.setPaymentData(orders.paymentInnmultiple);
                InsertAndUpdateGNUser(orders.orderInn);
                InsertAndUpdateOrderPool(orders.orderItemInn, eventId);
                if (orders.compListInner != null && orders.compListInner.size() > 0) {
                    if (orders.ticketTags != null && orders.ticketTags.size() > 0) {
                        InsertAndUpdateAttendeeAddress(orders.compListInner);
                    }
                    InsertAndUpdateTicketTag(orders.ticketTags);
                }
                i++;
                if (HttpPostData.dataloading != null) {
                    HttpPostData.dataloading.onProgressUpdate(i);
                }
                //datakoader.onProgressUpdate(i);
            }
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void InsertAndUpdateOrder(OrderListHandler order_detail) {
        try {
            //Log.i("---ORDER NAME---", order_detail.getOrderName());
            this.datavalues = new ContentValues();
            this.datavalues.put(DBFeilds.ORDER_BUYER_ID, order_detail.getClientGNUserId());
            this.datavalues.put(DBFeilds.ORDER_ORDER_STATUS, order_detail.getOrderStatus());
            this.datavalues.put(DBFeilds.ORDER_ORDER_TOTAL, String.valueOf(order_detail.getOrderTotalAmount()));
            this.datavalues.put(DBFeilds.ORDER_ORDER_DATE, order_detail.getOrderDate());
            this.datavalues.put(DBFeilds.ORDER_ORDER_NAME, order_detail.getOrderName());
            this.datavalues.put(DBFeilds.ORDER_ORDER_SUBTOTAL, String.valueOf(order_detail.getOrderSubTotalAmount()));
            this.datavalues.put(DBFeilds.ORDER_FEE_AMOUNT, String.valueOf(order_detail.getOrderFeeAmount()));
            this.datavalues.put(DBFeilds.ORDER_AMOUNT_PAID, String.valueOf(order_detail.getOrderAmountPaid()));
            this.datavalues.put(DBFeilds.ORDER_AMOUNT_DUE, String.valueOf(order_detail.getOrderAmountDue()));
            this.datavalues.put(DBFeilds.ORDER_ORDER_DISCOUNT, String.valueOf(order_detail.getOrderDiscount()));
            this.datavalues.put(DBFeilds.ORDER_ORDER_TAX, String.valueOf(order_detail.getOrderTaxAmount()));
            this.datavalues.put(DBFeilds.ORDER_ORDER_ID, order_detail.getOrderId());
            this.datavalues.put(DBFeilds.USER_EVENT_ID, order_detail.getEventId());
            this.datavalues.put(DBFeilds.ORDER_REGISTRATION_TYPE, Util.NullChecker(order_detail.getOrderRegistrationType()));

          /*  for(PaymentTypeHandler paymentTypeHandler:order_detail.getPaymentData()){
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ID, paymentTypeHandler.getId());
                this.datavalues.put(DBFeilds.ORDER_PAYKEY, paymentTypeHandler.getPayKey());
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_TYPE, Util.NullChecker(paymentTypeHandler.getPaymentType()));
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_MODE, Util.NullChecker(paymentTypeHandler.getPaymentMode()));
                this.datavalues.put(DBFeilds.ORDER_CHECK_NUMBER, paymentTypeHandler.getCheckNumber());
                this.datavalues.put(DBFeilds.ORDER_CREDIT_CARD_TYPE, paymentTypeHandler.getCreditCardType());
                this.datavalues.put(DBFeilds.ORDER_LAST_4DIGIT, paymentTypeHandler.getlast4degits());
                this.datavalues.put(DBFeilds.ORDER_REGISTRATION_TYPE, paymentTypeHandler.getPaymentType());
                this.datavalues.put(DBFeilds.ORDER_PAYGATEWAY_TYPE_ID, paymentTypeHandler.BLN_Pay_Gateway__r.PGateway_Type__r.Id);
                this.datavalues.put(DBFeilds.ORDER_PAYGATEWAY_TYPE_NAME, paymentTypeHandler.BLN_Pay_Gateway__r.PGateway_Type__r.Name);
                this.datavalues.put(DBFeilds.ORDER_REGISTRATION_TYPE, paymentTypeHandler.getRegistrationType());
            }*/
           /* this.datavalues.put(DBFeilds.ORDER_PAYMENT_ID, order_detail.getPaymentData().get(0).getId());
            this.datavalues.put(DBFeilds.ORDER_PAYKEY, order_detail.getPaymentData().get(0).getPayKey());
            this.datavalues.put(DBFeilds.ORDER_PAYMENT_TYPE, Util.NullChecker(order_detail.getPaymentData().get(0).getPaymentType()));
            this.datavalues.put(DBFeilds.ORDER_PAYMENT_MODE, Util.NullChecker(order_detail.getPaymentData().get(0).getPaymentMode()));
            this.datavalues.put(DBFeilds.ORDER_CHECK_NUMBER, order_detail.getPaymentData().get(0).getCheckNumber());
            this.datavalues.put(DBFeilds.ORDER_CREDIT_CARD_TYPE, order_detail.getPaymentData().get(0).getCreditCardType());
            this.datavalues.put(DBFeilds.ORDER_LAST_4DIGIT, order_detail.getPaymentData().get(0).getlast4degits());
            this.datavalues.put(DBFeilds.ORDER_REGISTRATION_TYPE, order_detail.getPaymentData().get(0).getPaymentType());
            this.datavalues.put(DBFeilds.ORDER_PAYGATEWAY_TYPE_ID, order_detail.getPaymentData().get(0).BLN_Pay_Gateway__r.PGateway_Type__r.Id);
            this.datavalues.put(DBFeilds.ORDER_PAYGATEWAY_TYPE_NAME, order_detail.getPaymentData().get(0).BLN_Pay_Gateway__r.PGateway_Type__r.Name);
            this.datavalues.put(DBFeilds.ORDER_REGISTRATION_TYPE, order_detail.getPaymentData().get(0).getRegistrationType());*/
            if (isUserExists(DBFeilds.TABLE_SALES_ORDER, " where Order_Id = '" + order_detail.getOrderId() + "'" + " AND " + DBFeilds.USER_EVENT_ID + " = '" + order_detail.getEventId() + "'")) {
                this.db.update(DBFeilds.TABLE_SALES_ORDER, this.datavalues, "Order_Id = '" + order_detail.getOrderId() + "'" + " AND " + DBFeilds.USER_EVENT_ID + " = '" + order_detail.getEventId() + "'", null);
                //Log.i("DataBase", "Order record Updated Successfull");
                return;
            }
            this.db.insert(DBFeilds.TABLE_SALES_ORDER, null, this.datavalues);
            //Log.i("DataBase", "Order record Insert Successfull");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    public void InsertAndUpdateMultiplePayment(ArrayList<PaymentTypeHandler> paymenttypehandler, String eventId) {
        try {
            ////Log.i("---Order Pool Size---", order_pool_data.size());
            this.db.beginTransaction();
            Iterator it = paymenttypehandler.iterator();
            while (it.hasNext()) {
                PaymentTypeHandler paymentTypeHandler = (PaymentTypeHandler) it.next();
                this.datavalues = new ContentValues();
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_ID, paymentTypeHandler.Id);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_CREATEDDATE, paymentTypeHandler.CreatedDate);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_SETTLEMENTDATE, Util.NullChecker(paymentTypeHandler.Settlement_Date__c));
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_CREDIT_CARD_4DIGITS, paymentTypeHandler.credit_card_last_4digits__c);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_CREDIT_CARD_TYPE, paymentTypeHandler.credit_card_type__c);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_CURRENCY_CODE, paymentTypeHandler.Currency_Code__c);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_LASTMODIFIEDDATE, paymentTypeHandler.LastModifiedDate);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_NAME, paymentTypeHandler.Name);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_ORDER_ID, paymentTypeHandler.Order__c);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_AMOUNT, paymentTypeHandler.Payment_Amount__c);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE, paymentTypeHandler.Payment_Mode__c);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_REF_NUMBER, paymentTypeHandler.Payment_Ref_Number__c);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS, paymentTypeHandler.Payment_Status__c);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_TYPE, paymentTypeHandler.Payment_Type__c);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_PAY_REG_TYPE, paymentTypeHandler.Pay_reg_type__c);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_PAY_GATEWAY, paymentTypeHandler.BLN_Pay_Gateway__r.PGateway_Type__r.Id);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_PAY_GATEWAY_NAME, paymentTypeHandler.BLN_Pay_Gateway__r.PGateway_Type__r.Name);
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_NOTES, AppUtils.NullChecker(paymentTypeHandler.Note__c));
                this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_EVENTID, eventId);

                if (isUserExists(DBFeilds.TABLE_ORDER_PAYMENT_ITEMS, " where payment_item_id = '" + paymentTypeHandler.Id + "'" + " AND " + DBFeilds.ORDER_PAYMENT_ITEM_EVENTID + " = '" + eventId + "'")) {
                    this.db.update(DBFeilds.TABLE_ORDER_PAYMENT_ITEMS, this.datavalues, "payment_item_id = '" + paymentTypeHandler.Id + "'" + " AND " + DBFeilds.ORDER_PAYMENT_ITEM_EVENTID + " = '" + eventId + "'", null);
                    //Log.i("DataBase", "Order Pool record Updated Successfull");
                } else {
                    this.db.insert(DBFeilds.TABLE_ORDER_PAYMENT_ITEMS, null, this.datavalues);
                    //Log.i("DataBase", "Order Pool record Insert Successfull");
                }
            }
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void InsertAndUpdateOrderPool(ArrayList<OrderItemPoolHandler> order_pool_data, String eventId) {
        try {
            ////Log.i("---Order Pool Size---", order_pool_data.size());
            this.db.beginTransaction();
            Iterator it = order_pool_data.iterator();
            while (it.hasNext()) {
                OrderItemPoolHandler pool_handler = (OrderItemPoolHandler) it.next();
                this.datavalues = new ContentValues();
                this.datavalues.put(DBFeilds.ORDER_ORDER_ID, pool_handler.getOrderId());
                this.datavalues.put(DBFeilds.ORDERITEM_ITEM_ID, pool_handler.getOrderItemId());
                this.datavalues.put(DBFeilds.ORDERITEM_ITEM_NAME, pool_handler.itemType.getItemName());
                this.datavalues.put(DBFeilds.ORDERITEM_ITEM_TYPE, pool_handler.itemType.getItemTypeId());
                this.datavalues.put(DBFeilds.ORDERITEM_ITEM_QTY, pool_handler.getOrderItemQty());
                this.datavalues.put(DBFeilds.ORDERITEM_ITEM_TOTAL_PRICE, String.valueOf(pool_handler.getOrderItemTotalAmount()));
                this.datavalues.put(DBFeilds.ORDERITEM_EACH_ITEM_PRICE, String.valueOf(pool_handler.getOrderEachItemPrice()));
                this.datavalues.put(DBFeilds.ORDERITEM_ITEM_DISCOUNT, String.valueOf(pool_handler.getOrderItemDiscount()));
                this.datavalues.put(DBFeilds.ORDERITEM_PROMO_CODE, ITransaction.EMPTY_STRING);
                this.datavalues.put(DBFeilds.ORDERITEM_POOL_NAME, pool_handler.getOrderPoolName());
                this.datavalues.put(DBFeilds.ORDERITEM_ORDER_ITEM_ID, pool_handler.getOrderItemPoolId());
                this.datavalues.put(DBFeilds.USER_EVENT_ID, eventId);
                this.datavalues.put(DBFeilds.ORDERITEM_CURRENCY, pool_handler.getOrderItemCurrency());
                this.datavalues.put(DBFeilds.ORDERITEM_STATUS, pool_handler.getOrderItemStatus());
                this.datavalues.put(DBFeilds.ORDERITEM_FEE, pool_handler.getOrderItemFee());
                this.datavalues.put(DBFeilds.ORDERITEM_TAX, pool_handler.getOrderItemTax());
                if (isUserExists(DBFeilds.TABLE_SALES_ORDER_ITEM, " where Order_Item_Id = '" + pool_handler.getOrderItemPoolId() + "'" + " AND " + DBFeilds.USER_EVENT_ID + " = '" + eventId + "'")) {
                    this.db.update(DBFeilds.TABLE_SALES_ORDER_ITEM, this.datavalues, "Order_Item_Id = '" + pool_handler.getOrderItemPoolId() + "'" + " AND " + DBFeilds.USER_EVENT_ID + " = '" + eventId + "'", null);
                    //Log.i("DataBase", "Order Pool record Updated Successfull");
                } else {
                    this.db.insert(DBFeilds.TABLE_SALES_ORDER_ITEM, null, this.datavalues);
                    //Log.i("DataBase", "Order Pool record Insert Successfull");
                }
            }
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*public void InsertAndUpdateAttendee(ArrayList<OrderItemListHandler> ticketsInn, String ordername) {
        try {
            this.db.beginTransaction();
            Iterator it = ticketsInn.iterator();
            int i = 0;
            while (it.hasNext()) {
                OrderItemListHandler attendee_info = (OrderItemListHandler) it.next();
                this.datavalues = new ContentValues();
                this.datavalues.put(DBFeilds.ATTENDEE_ID, Util.NullChecker(attendee_info.getId()));
                //newly added
                this.datavalues.put(DBFeilds.ATTENDEE_IMAGE, Util.NullChecker(attendee_info.tkt_profile__r.User_Pic__c));
                this.datavalues.put(DBFeilds.ATTENDEE_COMPANY_LOGO, Util.NullChecker(attendee_info.tkt_profile__r.Company_Logo__c));
                this.datavalues.put(DBFeilds.ATTENDEE_FIRST_NAME, Util.NullChecker(attendee_info.tkt_profile__r.First_Name__c));
                this.datavalues.put(DBFeilds.ATTENDEE_LAST_NAME, Util.NullChecker(attendee_info.tkt_profile__r.Last_Name__c));
                this.datavalues.put(DBFeilds.ATTENDEE_SUFFIX, Util.NullChecker(attendee_info.tkt_profile__r.Suffix__c));
                this.datavalues.put(DBFeilds.ATTENDEE_PREFIX, Util.NullChecker(attendee_info.tkt_profile__r.Prefix__c));
                this.datavalues.put(DBFeilds.ATTENDEE_EMAIL_ID, Util.NullChecker(attendee_info.tkt_profile__r.Email__c));
                this.datavalues.put(DBFeilds.ATTENDEE_COMPANY, Util.NullChecker(attendee_info.tkt_profile__r.TKT_Company__c));
                this.datavalues.put(DBFeilds.ATTENDEE_COMPANY_ID, Util.NullChecker(attendee_info.getCompany_id()));
                this.datavalues.put(DBFeilds.ATTENDEE_JOB_TILE, attendee_info.tkt_profile__r.TKT_Job_Title__c);
                this.datavalues.put(DBFeilds.ATTENDEE_MOBILE, Util.NullChecker(attendee_info.tkt_profile__r.Mobile__c));
                this.datavalues.put(DBFeilds.ATTENDEE_WORK_PHONE, Util.NullChecker(attendee_info.tkt_profile__r.Work_Phone__c));
                this.datavalues.put(DBFeilds.ATTENDEE_WORK_ADDRESS_1, Util.NullChecker(attendee_info.tkt_profile__r.Work_Address__r.Address1__c));
                this.datavalues.put(DBFeilds.ATTENDEE_WORK_ADDRESS_2, Util.NullChecker(attendee_info.tkt_profile__r.Work_Address__r.Address2__c));
                this.datavalues.put(DBFeilds.ATTENDEE_WORK_CITY, Util.NullChecker(attendee_info.tkt_profile__r.Work_Address__r.City__c));
                this.datavalues.put(DBFeilds.ATTENDEE_WORK_STATE, getStateLongName(Util.NullChecker(attendee_info.tkt_profile__r.Work_Address__r.State__r.Name)));
                this.datavalues.put(DBFeilds.ATTENDEE_WORK_COUNTRY, getCountryName(Util.NullChecker(attendee_info.tkt_profile__r.Work_Address__r.Country__r.Name)));
                this.datavalues.put(DBFeilds.ATTENDEE_WORK_ZIPCODE, Util.NullChecker(attendee_info.tkt_profile__r.Work_Address__r.ZipCode__c));
                this.datavalues.put(DBFeilds.ATTENDEE_HOME_PHONE, Util.NullChecker(attendee_info.tkt_profile__r.Home_Phone__c));
                this.datavalues.put(DBFeilds.ATTENDEE_HOME_ADDRESS_1, Util.NullChecker(attendee_info.tkt_profile__r.Home_Address__r.Address1__c));
                this.datavalues.put(DBFeilds.ATTENDEE_HOME_ADDRESS_2, Util.NullChecker(attendee_info.tkt_profile__r.Home_Address__r.Address2__c));
                this.datavalues.put(DBFeilds.ATTENDEE_HOME_CITY, Util.NullChecker(attendee_info.tkt_profile__r.Home_Address__r.City__c));
                this.datavalues.put(DBFeilds.ATTENDEE_HOME_STATE, getStateLongName(Util.NullChecker(attendee_info.tkt_profile__r.Home_Address__r.State__r.Name)));
                this.datavalues.put(DBFeilds.ATTENDEE_HOME_COUNTRY, getCountryName(Util.NullChecker(attendee_info.tkt_profile__r.Home_Address__r.Country__r.Name)));
                this.datavalues.put(DBFeilds.ATTENDEE_HOME_ZIPCODE, Util.NullChecker(attendee_info.tkt_profile__r.Home_Address__r.ZipCode__c));
                this.datavalues.put(DBFeilds.ATTENDEE_BADGE_NAME, Util.NullChecker(attendee_info.getBadgeLabel()).replaceAll("\\<.*?\\>", ""));
                this.datavalues.put(DBFeilds.USER_EVENT_ID, attendee_info.getEventId());
                this.datavalues.put(DBFeilds.ORDERITEM_ITEM_ID, attendee_info.getItemId());
                this.datavalues.put(DBFeilds.ATTENDEE_ITEM_TYPE_ID, attendee_info.getItemTypeId());
                this.datavalues.put(DBFeilds.ATTENDEE_TIKCET_NUMBER, attendee_info.getTicketNumber());
                this.datavalues.put(DBFeilds.ATTENDEE_ORDER_NUMBER, ordername);

                this.datavalues.put(DBFeilds.ATTENDEE_TICKET_SEAT_NUMBER, attendee_info.getSeatno());
                this.datavalues.put(DBFeilds.ORDER_ORDER_ID, attendee_info.getOrderId());
                if (attendee_info.getOrderItemId() != null) {
                    this.datavalues.put(DBFeilds.ORDERITEM_ORDER_ITEM_ID, attendee_info.getOrderItemId());
                } else {
                    this.datavalues.put(DBFeilds.ORDERITEM_ORDER_ITEM_ID, ITransaction.EMPTY_STRING);
                }
                this.datavalues.put(DBFeilds.ATTENDEE_ITEM_POOL_ID, attendee_info.getItemPoolId());
                this.datavalues.put(DBFeilds.ATTENDEE_TICKET_STATUS, attendee_info.getTicketStatus());
                if (attendee_info.getRSVPStatus() != null&&((!"Check In".equalsIgnoreCase(attendee_info.getRSVPStatus()))&&(!"Check Out".equalsIgnoreCase(attendee_info.getRSVPStatus())))) {
                    this.datavalues.put(DBFeilds.ATTENDEE_RSVP_STATUS, Util.NullChecker(attendee_info.getRSVPStatus()));
                }
                this.datavalues.put(DBFeilds.ATTENDEE_BUYER_ID, attendee_info.tkt_profile__r.BLN_GN_User__c);
                this.datavalues.put(DBFeilds.ATTENDEE_CHECKEDINDATE, attendee_info.getChickindate());

                if (!Util.NullChecker(attendee_info.badgeparentid__r.Badge_ID__c).isEmpty()) {
                    this.datavalues.put(DBFeilds.ATTENDEE_BADGEID, attendee_info.badgeparentid__r.Badge_ID__c);
                    this.datavalues.put(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS, Util.NullChecker(attendee_info.Badge_ID__r.Print_status__c));
                } else if (attendee_info.getOrderItemId() != null) {
                    this.datavalues.put(DBFeilds.ATTENDEE_BADGEID, attendee_info.getBadgeId());
                    this.datavalues.put(DBFeilds.ATTENDEE_UNIQUE_NUMBER, attendee_info.Badge_ID__r.Name);
                    this.datavalues.put(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS, Util.NullChecker(attendee_info.Badge_ID__r.Print_status__c));
                } else {
                    this.datavalues.put(DBFeilds.ATTENDEE_BADGEID, ITransaction.EMPTY_STRING);
                }
                this.datavalues.put(DBFeilds.ATTENDEE_BADGE_PARENT_ID, Util.NullChecker(attendee_info.getBadgeParentId()));




                this.datavalues.put(DBFeilds.ATTENDEE_CUSTOM_BARCODE, attendee_info.getCustomBarCode());
                this.datavalues.put(DBFeilds.ATTENDEE_TAG, attendee_info.getTag());
                this.datavalues.put(DBFeilds.ATTENDEE_BADGE_LABLE, attendee_info.getBadgeLabel());
                this.datavalues.put(DBFeilds.ATTENDEE_NOTE, attendee_info.getnote());
                this.datavalues.put(DBFeilds.ATTENDEE_ITEMPOOL_NAME, attendee_info.Item_Pool__r.Item_Pool_Name__c);
                this.datavalues.put(DBFeilds.ATTENDEE_ITEMTYPE_NAME, attendee_info.Item_Type__r.Name);
                this.datavalues.put(DBFeilds.ATTENDEE_ITEM_PARENT_ID, attendee_info.getTicketParentId());
                this.datavalues.put(DBFeilds.ATTENDEE_BADGABLE, attendee_info.Item_Pool__r.Badgable__c);
                this.datavalues.put(DBFeilds.ATTENDEE_SCANID, attendee_info.getScanID());
                //Log.i("-----------------Ticket Name And Badgable--------------", ":" + attendee_info.Item_Pool__r.Item_Pool_Name__c + " -- " + attendee_info.Item_Pool__r.Badgable__c);
                if (isUserExists(DBFeilds.TABLE_ATTENDEE_DETAILS, " where Attendee_Id = '" + attendee_info.getId() + "'" + " AND " + DBFeilds.USER_EVENT_ID + " = '" + attendee_info.getEventId() + "'")) {
                    this.db.update(DBFeilds.TABLE_ATTENDEE_DETAILS, this.datavalues, "Attendee_Id = '" + attendee_info.getId() + "'" + " AND " + DBFeilds.USER_EVENT_ID + " = '" + attendee_info.getEventId() + "'", null);
                    //Log.i("DataBase", "Checkid in date=" + attendee_info.getChickindate());
                    //Log.i("DataBase", "Attendee record Updated Successfull");
                } else {
                    this.db.insert(DBFeilds.TABLE_ATTENDEE_DETAILS, null, this.datavalues);
                    //Log.i("DataBase", "Checkid in date=" + attendee_info.getChickindate());
                    //Log.i("DataBase", "Attendee record Insert Successfull");
                }
                InsertAndUpdateTSTATUS(attendee_info.Tstatus__r.records, attendee_info.getEventId());
            }
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }*/



    public void InsertAndUpdateAttendee(ArrayList<OrderItemListHandler> ticketsInn, String ordername) {
        try {
            this.db.beginTransaction();

            //Log.i("---Attendee Record Size---", ":" + ticketsInn.size());
       /*if(ticketsInn.size() > 1){
          Collections.sort(ticketsInn, new Comparator<OrderItemListHandler>() {

            @Override
            public int compare(OrderItemListHandler lhs, OrderItemListHandler rhs) {
               // TODO Auto-generated method stub
               if(Util.NullChecker(lhs.getCustomBarCode()).isEmpty() || Util.NullChecker(rhs.getCustomBarCode()).isEmpty()){
                 return 0;
               }
               return Util.NullChecker(rhs.getCustomBarCode()).compareToIgnoreCase(Util.NullChecker(lhs.getCustomBarCode()));
            }
         });

          for(OrderItemListHandler attendee_info : ticketsInn){
             Log.i("----------------Ticket Number And Barcode-----------",":"+attendee_info.getTicketNumber()+" :: "+attendee_info.getCustomBarCode());
          }
       }*/


            Iterator it = ticketsInn.iterator();
            int i = 0;
            while (it.hasNext()) {
                OrderItemListHandler attendee_info = (OrderItemListHandler) it.next();
                this.datavalues = new ContentValues();
                // this.datavalues.put(DBFeilds.ATTENDEE_ID, Util.NullChecker(attendee_info.getId()));
                this.datavalues.put(DBFeilds.ATTENDEE_ID, Util.NullChecker(attendee_info.tkt_profile__r.Id));
                //newly added
                this.datavalues.put(DBFeilds.ATTENDEE_IMAGE, Util.NullChecker(attendee_info.tkt_profile__r.User_Pic__c));
                this.datavalues.put(DBFeilds.ATTENDEE_COMPANY_LOGO, Util.NullChecker(attendee_info.tkt_profile__r.Company_Logo__c));
                this.datavalues.put(DBFeilds.ATTENDEE_FIRST_NAME, Util.NullChecker(attendee_info.tkt_profile__r.First_Name__c));
                this.datavalues.put(DBFeilds.ATTENDEE_LAST_NAME, Util.NullChecker(attendee_info.tkt_profile__r.Last_Name__c));
                this.datavalues.put(DBFeilds.ATTENDEE_SUFFIX, Util.NullChecker(attendee_info.tkt_profile__r.Suffix__c));
                this.datavalues.put(DBFeilds.ATTENDEE_PREFIX, Util.NullChecker(attendee_info.tkt_profile__r.Prefix__c));
                this.datavalues.put(DBFeilds.ATTENDEE_EMAIL_ID, Util.NullChecker(attendee_info.tkt_profile__r.Email__c));
                this.datavalues.put(DBFeilds.ATTENDEE_COMPANY, Util.NullChecker(attendee_info.tkt_profile__r.TKT_Company__c)); //Name
                this.datavalues.put(DBFeilds.ATTENDEE_COMPANY_ID, Util.NullChecker(attendee_info.getCompany_id()));
                this.datavalues.put(DBFeilds.ATTENDEE_JOB_TILE, attendee_info.tkt_profile__r.TKT_Job_Title__c);
                //Newly added columns for attendees
                //this.datavalues.put(DBFeilds.ATTENDEE_COMPANY, Util.NullChecker(attendee_info.tkt_profile__r.TKT_Company__c));
                this.datavalues.put(DBFeilds.ATTENDEE_AGE, Util.NullChecker(attendee_info.tkt_profile__r.Age__c));
                this.datavalues.put(DBFeilds.ATTENDEE_GENDER, Util.NullChecker(attendee_info.tkt_profile__r.Gender__c));
                this.datavalues.put(DBFeilds.ATTENDEE_DOB, Util.NullChecker(attendee_info.tkt_profile__r.DOB__c));
                this.datavalues.put(DBFeilds.ATTENDEE_PRIMARY_BUSINESS_CATEGORY, Util.NullChecker(attendee_info.tkt_profile__r.Primary_Business_Category__c));
                this.datavalues.put(DBFeilds.ATTENDEE_SECONDARY_BUSINESS_CATEGORY, Util.NullChecker(attendee_info.tkt_profile__r.Secondary_Business_Category__c));
                this.datavalues.put(DBFeilds.ATTENDEE_NUMBER_OF_EMPLOYEES, Util.NullChecker(attendee_info.tkt_profile__r.Number_Of_Employees__c));
                this.datavalues.put(DBFeilds.ATTENDEE_ESTABLISHED_DATE, Util.NullChecker(attendee_info.tkt_profile__r.Established_Date__c));
                this.datavalues.put(DBFeilds.ATTENDEE_REVENUE, Util.NullChecker(attendee_info.tkt_profile__r.Revenue__c));
                this.datavalues.put(DBFeilds.ATTENDEE_TAX_ID, Util.NullChecker(attendee_info.tkt_profile__r.Tax_Id__c));
                this.datavalues.put(DBFeilds.ATTENDEE_WEBSITE_URL, Util.NullChecker(attendee_info.tkt_profile__r.Company_Website_URL__c));
                this.datavalues.put(DBFeilds.ATTENDEE_DUNS_NUMBER, Util.NullChecker(attendee_info.tkt_profile__r.Duns_Number__c));
                this.datavalues.put(DBFeilds.ATTENDEE_BLOG_URL, Util.NullChecker(attendee_info.tkt_profile__r.Blog_URL__c));
                this.datavalues.put(DBFeilds.ATTENDEE_DESCRIPTION, Util.NullChecker(attendee_info.tkt_profile__r.Company_Description__c));
                this.datavalues.put(DBFeilds.ATTENDEE_KEYWORDS, Util.NullChecker(attendee_info.tkt_profile__r.Keywords__c));
                this.datavalues.put(DBFeilds.ATTENDEE_EXCEPTIONAL_KEYWORDS, Util.NullChecker(attendee_info.tkt_profile__r.Exceptional_Keywords__c));
                this.datavalues.put(DBFeilds.ATTENDEE_DBA, Util.NullChecker(attendee_info.tkt_profile__r.DBA__c));
                this.datavalues.put(DBFeilds.ATTENDEE_BBB_NUMBER, Util.NullChecker(attendee_info.tkt_profile__r.BBB_Number__c));
                this.datavalues.put(DBFeilds.ATTENDEE_GSA_SCHEDULE, Util.NullChecker(attendee_info.tkt_profile__r.GSA_Schedule__c));
                this.datavalues.put(DBFeilds.ATTENDEE_GEOGRAPHICAL_REGION, Util.NullChecker(attendee_info.tkt_profile__r.Geographical_Region__c));



                this.datavalues.put(DBFeilds.ATTENDEE_ETHNICITY, Util.NullChecker(attendee_info.tkt_profile__r.Ethnicity__c));
                this.datavalues.put(DBFeilds.ATTENDEE_BUSINESS_STRUCTURE, Util.NullChecker(attendee_info.tkt_profile__r.Business_Structure__c));
                this.datavalues.put(DBFeilds.ATTENDEE_CAGECODE, Util.NullChecker(attendee_info.tkt_profile__r.CageCode__c));
                this.datavalues.put(DBFeilds.ATTENDEE_DISTRIBUTION_COUNTRY, Util.NullChecker(attendee_info.tkt_profile__r.distribution_Country__c));
                this.datavalues.put(DBFeilds.ATTENDEE_FAXNUMBER, Util.NullChecker(attendee_info.tkt_profile__r.FaxNumber__c));
                this.datavalues.put(DBFeilds.ATTENDEE_MANUFACTURES_COUNTRY, Util.NullChecker(attendee_info.tkt_profile__r.Manufactures_Country__c));
                this.datavalues.put(DBFeilds.ATTENDEE_OUTSIDE_FACILITIES, Util.NullChecker(attendee_info.tkt_profile__r.Outside_Facilities__c));
                this.datavalues.put(DBFeilds.ATTENDEE_REFERENCES1, Util.NullChecker(attendee_info.tkt_profile__r.References1__c));
                this.datavalues.put(DBFeilds.ATTENDEE_REFERENCES2, Util.NullChecker(attendee_info.tkt_profile__r.References2__c));
                this.datavalues.put(DBFeilds.ATTENDEE_SCOPEOFWORK1, Util.NullChecker(attendee_info.tkt_profile__r.ScopeOfWork1__c));
                this.datavalues.put(DBFeilds.ATTENDEE_SCOPEOFWORK2, Util.NullChecker(attendee_info.tkt_profile__r.ScopeOfWork2__c));
                this.datavalues.put(DBFeilds.ATTENDEE_SECONDARY_EMAIL, Util.NullChecker(attendee_info.tkt_profile__r.Secondary_email__c));
                this.datavalues.put(DBFeilds.ATTENDEE_YEAR_IN_BUSINESS, Util.NullChecker(attendee_info.tkt_profile__r.Year_in_business__c));
                /*this.datavalues.put(DBFeilds.ATTENDEE_TABLE_NUMBER, Util.NullChecker(attendee_info.tkt_profile__r.BLN_MM_Report_TKT_Table__c));
                 */this.datavalues.put(DBFeilds.ATTENDEE_LOCATION_ROOM, Util.NullChecker(attendee_info.tkt_profile__r.Location_Room__c));

                this.datavalues.put(DBFeilds.ATTENDEE_SPEAKERLINKEDINID, Util.NullChecker(attendee_info.tkt_profile__r.SpeakerLinkedInId));
                this.datavalues.put(DBFeilds.ATTENDEE_SPEAKERTWITTERID, Util.NullChecker(attendee_info.tkt_profile__r.SpeakerTwitterId));
                this.datavalues.put(DBFeilds.ATTENDEE_SPEAKERBLOGGER, Util.NullChecker(attendee_info.tkt_profile__r.SpeakerBlogger));
                this.datavalues.put(DBFeilds.ATTENDEE_BIOGRAPHY, Util.NullChecker(attendee_info.tkt_profile__r.Biography__c));
                //this.datavalues.put(DBFeilds.ATTENDEE_SPEAKERVIDEO, Util.NullChecker(attendee_info.tkt_profile__r.SpeakerVideo));
                this.datavalues.put(DBFeilds.ATTENDEE_WHATSAPP, Util.NullChecker(attendee_info.tkt_profile__r.WhatsApp__c));
                this.datavalues.put(DBFeilds.ATTENDEE_WECHAT, Util.NullChecker(attendee_info.tkt_profile__r.Wechat__c));
                this.datavalues.put(DBFeilds.ATTENDEE_SKYPE, Util.NullChecker(attendee_info.tkt_profile__r.Skype__c));
                this.datavalues.put(DBFeilds.ATTENDEE_SNAPCHAT, Util.NullChecker(attendee_info.tkt_profile__r.Snapchat__c));
                this.datavalues.put(DBFeilds.ATTENDEE_INSTAGRAM, Util.NullChecker(attendee_info.tkt_profile__r.Instagram__c));
                this.datavalues.put(DBFeilds.ATTENDEE_PHONE, Util.NullChecker(attendee_info.tkt_profile__r.Mobile__c));//Phone__c



                this.datavalues.put(DBFeilds.ATTENDEE_MOBILE, Util.NullChecker(attendee_info.tkt_profile__r.Mobile__c));//Phone__c
                this.datavalues.put(DBFeilds.ATTENDEE_WORK_PHONE, Util.NullChecker(attendee_info.tkt_profile__r.Work_Phone__c));
                this.datavalues.put(DBFeilds.ATTENDEE_WORK_ADDRESS_1, Util.NullChecker(attendee_info.tkt_profile__r.Work_Address__r.Address1__c));
                this.datavalues.put(DBFeilds.ATTENDEE_WORK_ADDRESS_2, Util.NullChecker(attendee_info.tkt_profile__r.Work_Address__r.Address2__c));
                this.datavalues.put(DBFeilds.ATTENDEE_WORK_CITY, Util.NullChecker(attendee_info.tkt_profile__r.Work_Address__r.City__c));
                this.datavalues.put(DBFeilds.ATTENDEE_WORK_STATE, getStateLongName(Util.NullChecker(attendee_info.tkt_profile__r.Work_Address__r.State__r.Name)));
                this.datavalues.put(DBFeilds.ATTENDEE_WORK_COUNTRY, getCountryName(Util.NullChecker(attendee_info.tkt_profile__r.Work_Address__r.Country__r.Name)));
                this.datavalues.put(DBFeilds.ATTENDEE_WORK_ZIPCODE, Util.NullChecker(attendee_info.tkt_profile__r.Work_Address__r.ZipCode__c));
                this.datavalues.put(DBFeilds.ATTENDEE_HOME_PHONE, Util.NullChecker(attendee_info.tkt_profile__r.Home_Phone__c));
                this.datavalues.put(DBFeilds.ATTENDEE_HOME_ADDRESS_1, Util.NullChecker(attendee_info.tkt_profile__r.Home_Address__r.Address1__c));
                this.datavalues.put(DBFeilds.ATTENDEE_HOME_ADDRESS_2, Util.NullChecker(attendee_info.tkt_profile__r.Home_Address__r.Address2__c));
                this.datavalues.put(DBFeilds.ATTENDEE_HOME_CITY, Util.NullChecker(attendee_info.tkt_profile__r.Home_Address__r.City__c));
                this.datavalues.put(DBFeilds.ATTENDEE_HOME_STATE, getStateLongName(Util.NullChecker(attendee_info.tkt_profile__r.Home_Address__r.State__r.Name)));
                this.datavalues.put(DBFeilds.ATTENDEE_HOME_COUNTRY, getCountryName(Util.NullChecker(attendee_info.tkt_profile__r.Home_Address__r.Country__r.Name)));
                this.datavalues.put(DBFeilds.ATTENDEE_HOME_ZIPCODE, Util.NullChecker(attendee_info.tkt_profile__r.Home_Address__r.ZipCode__c));
                this.datavalues.put(DBFeilds.ATTENDEE_BILLING_ADDRESS_1, Util.NullChecker(attendee_info.tkt_profile__r.Billing_Address__r.Address1__c));
                this.datavalues.put(DBFeilds.ATTENDEE_BILLING_ADDRESS_2, Util.NullChecker(attendee_info.tkt_profile__r.Billing_Address__r.Address2__c));
                this.datavalues.put(DBFeilds.ATTENDEE_BILLING_CITY, Util.NullChecker(attendee_info.tkt_profile__r.Billing_Address__r.City__c));
                this.datavalues.put(DBFeilds.ATTENDEE_BILLING_STATE, getStateLongName(Util.NullChecker(attendee_info.tkt_profile__r.Billing_Address__r.State__r.Name)));
                this.datavalues.put(DBFeilds.ATTENDEE_BILLING_COUNTRY, getCountryName(Util.NullChecker(attendee_info.tkt_profile__r.Billing_Address__r.Country__r.Name)));
                this.datavalues.put(DBFeilds.ATTENDEE_BILLING_ZIPCODE, Util.NullChecker(attendee_info.tkt_profile__r.Billing_Address__r.ZipCode__c));
                this.datavalues.put(DBFeilds.ATTENDEE_BADGE_NAME, Util.NullChecker(attendee_info.getBadgeLabel()).replaceAll("\\<.*?\\>", ""));
                this.datavalues.put(DBFeilds.USER_EVENT_ID, attendee_info.getEventId());
                this.datavalues.put(DBFeilds.ORDERITEM_ITEM_ID, attendee_info.getItemId());
                this.datavalues.put(DBFeilds.ATTENDEE_ITEM_TYPE_ID, attendee_info.getItemTypeId());
                this.datavalues.put(DBFeilds.ATTENDEE_TIKCET_NUMBER, attendee_info.getTicketNumber());
                this.datavalues.put(DBFeilds.ATTENDEE_ORDER_NUMBER, ordername);
                // Log.i("---------------Attendee Name And checkin Status--------",":"+Util.NullChecker(attendee_info.tkt_profile__r.First_Name__c)+" : "+attendee_info.checkinhandler.getCheckinstatus());
           /* if(attendee_info.checkinhandler != null){
                this.datavalues.put(DBFeilds.ATTENDEE_ISCHECKIN, String.valueOf(attendee_info.checkinhandler.getCheckinstatus()));
            }*/
                this.datavalues.put(DBFeilds.ATTENDEE_TICKET_SEAT_NUMBER, attendee_info.getSeatno());
                this.datavalues.put(DBFeilds.ORDER_ORDER_ID, attendee_info.getOrderId());
                if (attendee_info.getOrderItemId() != null) {
                    this.datavalues.put(DBFeilds.ORDERITEM_ORDER_ITEM_ID, attendee_info.getOrderItemId());
                } else {
                    this.datavalues.put(DBFeilds.ORDERITEM_ORDER_ITEM_ID, ITransaction.EMPTY_STRING);
                }
                this.datavalues.put(DBFeilds.ATTENDEE_ITEM_POOL_ID, attendee_info.getItemPoolId());
                this.datavalues.put(DBFeilds.ATTENDEE_TICKET_STATUS, attendee_info.getTicketStatus());
                if (attendee_info.getRSVPStatus() != null&&((!"Check In".equalsIgnoreCase(attendee_info.getRSVPStatus()))&&(!"Check Out".equalsIgnoreCase(attendee_info.getRSVPStatus())))) {
                    this.datavalues.put(DBFeilds.ATTENDEE_RSVP_STATUS, Util.NullChecker(attendee_info.getRSVPStatus()));
                }
                this.datavalues.put(DBFeilds.ATTENDEE_BUYER_ID, attendee_info.tkt_profile__r.BLN_GN_User__c);
                this.datavalues.put(DBFeilds.ATTENDEE_CHECKEDINDATE, attendee_info.getChickindate());

                if (!Util.NullChecker(attendee_info.badgeparentid__r.Badge_ID__c).isEmpty()) {
                    this.datavalues.put(DBFeilds.ATTENDEE_BADGEID, attendee_info.badgeparentid__r.Badge_ID__c);
                    this.datavalues.put(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS, Util.NullChecker(attendee_info.Badge_ID__r.Print_status__c));
                } else if (attendee_info.getOrderItemId() != null) {
                    this.datavalues.put(DBFeilds.ATTENDEE_BADGEID, attendee_info.getBadgeId());
                    this.datavalues.put(DBFeilds.ATTENDEE_UNIQUE_NUMBER, attendee_info.Badge_ID__r.Name);
                    this.datavalues.put(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS, Util.NullChecker(attendee_info.Badge_ID__r.Print_status__c));
                } else {
                    this.datavalues.put(DBFeilds.ATTENDEE_BADGEID, ITransaction.EMPTY_STRING);
                }
                this.datavalues.put(DBFeilds.ATTENDEE_BADGE_PARENT_ID, Util.NullChecker(attendee_info.getBadgeParentId()));

            /*if(Util.NullChecker(attendee_info.getCustomBarCode()).isEmpty()){
               if(i>0){
                  this.datavalues.put(DBFeilds.ATTENDEE_CUSTOM_BARCODE, ticketsInn.get(0).getCustomBarCode());
               }
            }else{
               this.datavalues.put(DBFeilds.ATTENDEE_CUSTOM_BARCODE, attendee_info.getCustomBarCode());
            }
            i++;*/

                this.datavalues.put(DBFeilds.ATTENDEE_CUSTOM_BARCODE, attendee_info.getCustomBarCode());
                this.datavalues.put(DBFeilds.ATTENDEE_TAG, attendee_info.getTag());
                this.datavalues.put(DBFeilds.ATTENDEE_BADGE_LABLE, attendee_info.getBadgeLabel());
                this.datavalues.put(DBFeilds.ATTENDEE_NOTE, attendee_info.getnote());
                this.datavalues.put(DBFeilds.ATTENDEE_ITEMPOOL_NAME, attendee_info.Item_Pool__r.Item_Pool_Name__c);
                this.datavalues.put(DBFeilds.ATTENDEE_ITEMTYPE_NAME, attendee_info.Item_Type__r.Name);
                this.datavalues.put(DBFeilds.ATTENDEE_ITEM_PARENT_ID, attendee_info.getTicketParentId());
                this.datavalues.put(DBFeilds.ATTENDEE_BADGABLE, attendee_info.Item_Pool__r.Badgable__c);
                this.datavalues.put(DBFeilds.ATTENDEE_SCANID, attendee_info.getScanID());
                //Log.i("-----------------Ticket Name And Badgable--------------", ":" + attendee_info.Item_Pool__r.Item_Pool_Name__c + " -- " + attendee_info.Item_Pool__r.Badgable__c);
                if (isUserExists(DBFeilds.TABLE_ATTENDEE_DETAILS, " where Attendee_Id = '" + attendee_info.tkt_profile__r.Id + "'" + " AND " + DBFeilds.USER_EVENT_ID + " = '" + attendee_info.getEventId() + "'")) {
                    this.db.update(DBFeilds.TABLE_ATTENDEE_DETAILS, this.datavalues, "Attendee_Id = '" + attendee_info.tkt_profile__r.Id + "'" + " AND " + DBFeilds.USER_EVENT_ID + " = '" + attendee_info.getEventId() + "'", null);
                    //Log.i("DataBase", "Checkid in date=" + ATTENDEE_INFO.getChickindate());
                    //Log.i("DataBase", "Attendee record Updated Successfull");
                } else {
                    this.db.insert(DBFeilds.TABLE_ATTENDEE_DETAILS, null, this.datavalues);
                    //Log.i("DataBase", "Checkid in date=" + attendee_info.getChickindate());
                    //Log.i("DataBase", "Attendee record Insert Successfull");
                }
                InsertAndUpdateTSTATUS(attendee_info.Tstatus__r.records, attendee_info.getEventId());
            }
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    public void InsertAndUpdateTSTATUS(List<TStatus> tstatusList, String event_id) {
        boolean isdeleted=true;
        //delete from TStatus where Ticket__c ='a2L2F0000002kiuUAA' AND Group_Id='a2V2F000000IhGEUA0'
        try {
            this.db.beginTransaction();
            for (TStatus tstatus : tstatusList) {
                ContentValues values = new ContentValues();
                values.put(DBFeilds.T_EVENT_ID, event_id);
                values.put(DBFeilds.T_TICKET_ID, tstatus.Ticket__c);
                values.put(DBFeilds.T_CHECKIN_STATUS, String.valueOf(tstatus.Tstatus_name__c));
                values.put(DBFeilds.T_SESSION_ITEM_POOL, tstatus.BLN_Session_Item__r.BLN_Item_Pool__c);
                values.put(DBFeilds.T_SESSION_USER, tstatus.BLN_Session_user__c);
                values.put(DBFeilds.T_ISLATEST, String.valueOf(tstatus.Islatest__c));
                values.put(DBFeilds.T_GROUP_ID, tstatus.BLN_Session_user__r.BLN_Group__r.Id);
                values.put(DBFeilds.T_GROUP_NAME, tstatus.BLN_Session_user__r.BLN_Group__r.Name);
                values.put(DBFeilds.T_SCAN_TIME, tstatus.scan_time__c);
                values.put(DBFeilds.T_USER_ID, tstatus.BLN_Session_user__r.BLN_GN_User__r.User__c);
                values.put(DBFeilds.T_GNUSER_ID, tstatus.BLN_Session_user__r.BLN_GN_User__r.Id);
                boolean isExists = isRecordExists(DBFeilds.TABLE_TSTATUS, " where " + DBFeilds.T_TICKET_ID + " = '" + tstatus.Ticket__c + "' AND " + DBFeilds.T_EVENT_ID + " = '" + event_id + "' AND " + DBFeilds.T_SESSION_ITEM_POOL + " = '" + tstatus.BLN_Session_Item__r.BLN_Item_Pool__c + "' AND " + DBFeilds.T_GROUP_ID + " = '" + tstatus.BLN_Session_user__r.BLN_Group__r.Id + "'");
                /*if(isExists&&isdeleted){

                }else*/  if (isExists) {
                    db.update(DBFeilds.TABLE_TSTATUS, values, DBFeilds.T_TICKET_ID + " = '" + tstatus.Ticket__c + "' AND " + DBFeilds.T_EVENT_ID + " = '" + event_id + "' AND " + DBFeilds.T_SESSION_ITEM_POOL + " = '" + tstatus.BLN_Session_Item__r.BLN_Item_Pool__c + "' AND " + DBFeilds.T_GROUP_ID + " = '" + tstatus.BLN_Session_user__r.BLN_Group__r.Id + "'", null);
                } else {
                    db.insert(DBFeilds.TABLE_TSTATUS, null, values);
                }
            }
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void InsertAndUpdateTSTATUS(TStatus tstatus, String event_id) {
        try {
            ContentValues values = new ContentValues();
            values.put(DBFeilds.T_EVENT_ID, event_id);
            values.put(DBFeilds.T_TICKET_ID, tstatus.Ticket__c);
            values.put(DBFeilds.T_CHECKIN_STATUS, String.valueOf(tstatus.Tstatus_name__c));
            values.put(DBFeilds.T_SESSION_ITEM_POOL, tstatus.BLN_Session_Item__r.BLN_Item_Pool__c);
            values.put(DBFeilds.T_SESSION_USER, tstatus.BLN_Session_user__c);
            values.put(DBFeilds.T_ISLATEST, String.valueOf(tstatus.Islatest__c));
            values.put(DBFeilds.T_GROUP_ID, tstatus.BLN_Session_user__r.BLN_Group__r.Id);
            values.put(DBFeilds.T_GROUP_NAME, tstatus.BLN_Session_user__r.BLN_Group__r.Name);
            values.put(DBFeilds.T_SCAN_TIME, tstatus.scan_time__c);
            values.put(DBFeilds.T_USER_ID, tstatus.BLN_Session_user__r.BLN_GN_User__r.User__c);
            values.put(DBFeilds.T_GNUSER_ID, tstatus.BLN_Session_user__r.BLN_GN_User__r.Id);

            boolean isExists = isRecordExists(DBFeilds.TABLE_TSTATUS, " where " + DBFeilds.T_TICKET_ID + " = '" + tstatus.Ticket__c + "' AND " + DBFeilds.T_EVENT_ID + " = '" + event_id + "' AND " + DBFeilds.T_SESSION_ITEM_POOL + " = '" + tstatus.BLN_Session_Item__r.BLN_Item_Pool__c + "' AND " + DBFeilds.T_GROUP_ID + " = '" + tstatus.BLN_Session_user__r.BLN_Group__r.Id + "'");
            //Log.i("----------------Is Exists------------",":"+isExists);
            if (isExists) {
                db.update(DBFeilds.TABLE_TSTATUS, values, DBFeilds.T_TICKET_ID + " = '" + tstatus.Ticket__c + "' AND " + DBFeilds.T_EVENT_ID + " = '" + event_id + "' AND " + DBFeilds.T_SESSION_ITEM_POOL + " = '" + tstatus.BLN_Session_Item__r.BLN_Item_Pool__c + "' AND " + DBFeilds.T_GROUP_ID + " = '" + tstatus.BLN_Session_user__r.BLN_Group__r.Id + "'", null);
            } else {
                db.insert(DBFeilds.TABLE_TSTATUS, null, values);
            }


        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
    public String getTStatusBasedOnSessionId(String ticket_id, String Item_poo_id, String event_id,String group_id) {
        // String group_id = getSwitchedONGroupId(event_id);
        String checkin_status = ITransaction.EMPTY_STRING;
        try {
            //Log.i("------------Print Query------",":"+"select Tstatus_name__c from "+DBFeilds.TABLE_TSTATUS+" where "+DBFeilds.T_TICKET_ID+"='"+ticket_id+"' AND "+DBFeilds.T_SESSION_ITEM_POOL+"='"+Item_poo_id+"' AND "+DBFeilds.T_GROUP_ID+"='"+group_id+"' AND "+DBFeilds.T_EVENT_ID+"='"+event_id+"' AND "+DBFeilds.T_ISLATEST+"='true'");
            //  changed for checkin count
            // Cursor c = db.rawQuery("select Tstatus_name__c from " + DBFeilds.TABLE_TSTATUS + " where " + DBFeilds.T_TICKET_ID + "='" + ticket_id + "' AND " + DBFeilds.T_SESSION_ITEM_POOL + "='" + Item_poo_id + "' AND " + DBFeilds.T_GROUP_ID + "='" + group_id + "' AND " + DBFeilds.T_EVENT_ID + "='" + event_id + "' AND " + DBFeilds.T_ISLATEST + "='true'", null);
            Cursor c = db.rawQuery("select Tstatus_name__c from " + DBFeilds.TABLE_TSTATUS + " where " + DBFeilds.T_TICKET_ID + "='" + ticket_id + "'  AND " + DBFeilds.T_GROUP_ID + "='" + group_id + "' AND " + DBFeilds.T_EVENT_ID + "='" + event_id + "' AND " + DBFeilds.T_ISLATEST + "='true'", null);
            if (c!=null&&c.moveToFirst()) {
                checkin_status = c.getString(c.getColumnIndex(DBFeilds.T_CHECKIN_STATUS));
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return checkin_status;
    }

    public String getTStatusBasedOnGroup(String ticket_id, String Item_poo_id, String event_id) {
        String group_id = getSwitchedONGroupId(event_id);
        String checkin_status = ITransaction.EMPTY_STRING;
        try {
            //Log.i("------------Print Query------",":"+"select Tstatus_name__c from "+DBFeilds.TABLE_TSTATUS+" where "+DBFeilds.T_TICKET_ID+"='"+ticket_id+"' AND "+DBFeilds.T_SESSION_ITEM_POOL+"='"+Item_poo_id+"' AND "+DBFeilds.T_GROUP_ID+"='"+group_id+"' AND "+DBFeilds.T_EVENT_ID+"='"+event_id+"' AND "+DBFeilds.T_ISLATEST+"='true'");
            Cursor c = db.rawQuery("select Tstatus_name__c from " + DBFeilds.TABLE_TSTATUS + " where " + DBFeilds.T_TICKET_ID + "='" + ticket_id + "' AND " + DBFeilds.T_SESSION_ITEM_POOL + "='" + Item_poo_id + "' AND " + DBFeilds.T_GROUP_ID + "='" + group_id + "' AND " + DBFeilds.T_EVENT_ID + "='" + event_id + "' AND " + DBFeilds.T_ISLATEST + "='true'", null);

            if (c.moveToFirst()) {
                checkin_status = c.getString(c.getColumnIndex(DBFeilds.T_CHECKIN_STATUS));
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return checkin_status;
    }

    public String getTStatusBasedOnGroup(String ticket_id, String Item_poo_id, String event_id, String group_id) {
        //String group_id = getSwitchedONGroupId(event_id);
        String checkin_status = ITransaction.EMPTY_STRING;
        try {
            //Log.i("------------Print Query------",":"+"select Tstatus_name__c from "+DBFeilds.TABLE_TSTATUS+" where "+DBFeilds.T_TICKET_ID+"='"+ticket_id+"' AND "+DBFeilds.T_SESSION_ITEM_POOL+"='"+Item_poo_id+"' AND "+DBFeilds.T_GROUP_ID+"='"+group_id+"' AND "+DBFeilds.T_EVENT_ID+"='"+event_id+"' AND "+DBFeilds.T_ISLATEST+"='true'");
            Cursor c = db.rawQuery("select Tstatus_name__c from " + DBFeilds.TABLE_TSTATUS + " where " + DBFeilds.T_TICKET_ID + "='" + ticket_id + "' AND " + DBFeilds.T_SESSION_ITEM_POOL + "='" + Item_poo_id + "' AND " + DBFeilds.T_GROUP_ID + "='" + group_id + "' AND " + DBFeilds.T_EVENT_ID + "='" + event_id + "' AND " + DBFeilds.T_ISLATEST + "='true'", null);

            if (c.moveToFirst()) {
                checkin_status = c.getString(c.getColumnIndex(DBFeilds.T_CHECKIN_STATUS));
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return checkin_status;
    }

    public String getScantimeBasedOnGroup(String ticket_id, String Item_poo_id, String event_id) {
        String group_id = getSwitchedONGroupId(event_id);
        String scantime = ITransaction.EMPTY_STRING;
        try {
            Cursor c = db.rawQuery("select T_Scan_Time from " + DBFeilds.TABLE_TSTATUS + " where " + DBFeilds.T_TICKET_ID + "='" + ticket_id + "' AND " + DBFeilds.T_SESSION_ITEM_POOL + "='" + Item_poo_id + "' AND " + DBFeilds.T_GROUP_ID + "='" + group_id + "' AND " + DBFeilds.T_EVENT_ID + "='" + event_id + "' AND Islatest__c = 'true' LIMIT 1", null);
            if (c.moveToFirst()) {
                scantime = c.getString(c.getColumnIndex(DBFeilds.T_SCAN_TIME));
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return scantime;
    }

    public String getScantimeBasedOnGroup(String ticket_id, String Item_poo_id, String group_id, String event_id) {
        //String group_id = getSwitchedONGroupId(event_id);
        String scantime = ITransaction.EMPTY_STRING;
        try {
            Cursor c = db.rawQuery("select T_Scan_Time from " + DBFeilds.TABLE_TSTATUS + " where " + DBFeilds.T_TICKET_ID + "='" + ticket_id + "' AND " + DBFeilds.T_SESSION_ITEM_POOL + "='" + Item_poo_id + "' AND " + DBFeilds.T_GROUP_ID + "='" + group_id + "' AND " + DBFeilds.T_EVENT_ID + "='" + event_id + "' AND Islatest__c = 'true' LIMIT 1", null);
            if (c.moveToFirst()) {
                scantime = c.getString(c.getColumnIndex(DBFeilds.T_SCAN_TIME));
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return scantime;
    }

    public ArrayList<TicketTypeHandler> getItemType(String item_type_id) {
        ArrayList<TicketTypeHandler> type = new ArrayList();
        try {
            String WHERE = "ItemTypeName IN ('Admissions','Items')";
            if (!getItemTypeName(item_type_id).isEmpty()) {
                WHERE = "ItemTypeName IN ('Admissions','Items','" + getItemTypeName(item_type_id) + "')";
            }
            Cursor c = this.db.rawQuery("Select * from ItemType where " + WHERE, null);
            if (c.moveToFirst()) {
                do {
                    TicketTypeHandler item = new TicketTypeHandler();
                    item.setItemTypeId(c.getString(c.getColumnIndex(DBFeilds.ITEM_TYPE_ID)));
                    item.setItemTypeName(c.getString(c.getColumnIndex(DBFeilds.ITEM_TYPE_NAME)));
                    type.add(item);
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return type;
    }
   /* public TicketTypeHandler getItemType(String item_type_id) {
    	TicketTypeHandler item = new TicketTypeHandler();
        try {
            Cursor c = this.db.rawQuery("Select * from ItemType where "+DBFeilds.ITEM_TYPE_ID+"='"+item_type_id+"'", null);
            //Log.i("-------------------Cursor Size----------------",":"+c.getCount()+" : "+item_type_id);
            if (c.moveToFirst()) {
                do {

                    item.setItemTypeId(c.getString(c.getColumnIndex(DBFeilds.ITEM_TYPE_ID)));
                    item.setItemTypeName(c.getString(c.getColumnIndex(DBFeilds.ITEM_TYPE_NAME)));

                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }*/


    public void InsertItemType(List<ItemType> itemTypes) {
        try {
            this.db.beginTransaction();
            for (ItemType type : itemTypes) {
                this.datavalues = new ContentValues();
                this.datavalues.put(DBFeilds.ITEM_TYPE_ID, type.ItemType_Id);
                this.datavalues.put(DBFeilds.ITEM_TYPE_NAME, type.ItemType);
                this.datavalues.put(DBFeilds.ITEM_TYPE_CURRENCY, type.currencyid);
                if (isRecordExists(DBFeilds.TABLE_ITEM_TYPE, " where ItemTypeId = '" + type.ItemType_Id + "'")) {
                    this.db.update(DBFeilds.TABLE_ITEM_TYPE, this.datavalues, "ItemTypeId = '" + type.ItemType_Id + "'", null);
                } else {
                    this.db.insert(DBFeilds.TABLE_ITEM_TYPE, null, this.datavalues);
                }
            }
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
            //Log.i("DATABASE", "ITEM TYPE INSERT");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upadteItemListRecordInDB(List<TicketListResponseHandler> response_handler, String eventId) {
        try {
            for (TicketListResponseHandler handler : response_handler) {
                handler.Items.setImage_url(handler.siteurl + handler.Items.getImage_url());
                handler.Items.setItemFee(Util.NullChecker(handler.Fee));
                InsertAndUpdateItem(handler.Items);
                InsertHideStatus(handler.Items.get_Hide_Visibility__c(),handler.Items.getId(),eventId);
                // deleteItemRegsettings(handler.Items.getId());
                //InsertUpdateRegsettings(handler.Items.Reg_Settings__r.records, true);
                InsertUpdateRegsettings(handler.Items.Reg_Settings__r.records, true);
                //Log.i("-----------------Item Pool Length-------------", ":" + handler.Itempool.size());
                Iterator it = handler.Itempool.iterator();
                while (it.hasNext()) {
                    TicketPoolHandler itempool = (TicketPoolHandler) it.next();
                    InsertAndUpdateItemPool(itempool, eventId);
                    // deleteItemRegsettings(itempool.getPoolId());
                    InsertUpdateRegsettings(itempool.Reg_Settings__r.records, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upadteItemRecordInDB(TicketResponseHandler response_handler, String eventId) {
        try {
            Iterator it = response_handler.Items.iterator();
            while (it.hasNext()) {
                TicketHandler item = (TicketHandler) it.next();
                InsertAndUpdateItem(item);
                InsertHideStatus(item.get_Hide_Visibility__c(),item.getId(),eventId);
                //Log.i("---------------Item Name And Reg settings---------------", ":" + item.getItem_name__c() + " : " + item.Reg_Settings__r.records.size());
                deleteItemRegsettings(item.getId());
                InsertUpdateRegsettings(item.Reg_Settings__r.records, true);
            }
            InsertAndUpdateItemPool(response_handler.Itempool, eventId);
            deleteItemRegsettings(response_handler.Itempool.getPoolId());
            InsertUpdateRegsettings(response_handler.Itempool.Reg_Settings__r.records, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getItemName(String id) {
        String name = ITransaction.EMPTY_STRING;
        try {
            Cursor cursor = this.db.rawQuery("Select * from ItemDetails where ItemId = '" + id + "'", null);
            if (cursor == null) {
                return name;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                name = cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_NAME));
                cursor.close();
            }
            return name;
        } catch (Exception e) {
            e.printStackTrace();
            return name;
        }
    }

    public Cursor getItems(String where) {
        try {
            Cursor cursor = this.db.rawQuery("Select * from ItemDetails" + where, null);
            cursor.moveToFirst();
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getItem_Pool_Name(String item_pool_id, String event_id) {
        String name = ITransaction.EMPTY_STRING;
        try {
            Cursor c = db.rawQuery("select * from " + DBFeilds.TABLE_ITEM_POOL + " where " + DBFeilds.ITEMPOOL_ID + " = '" + item_pool_id + "' AND " + DBFeilds.ITEMPOOL_EVENT_ID + " = '" + event_id + "'", null);
            if (c.moveToFirst()) {
                name = c.getString(c.getColumnIndex(DBFeilds.ITEMPOOL_ITEMPOOLNAME));
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return name;
    }

    public double getTotalItemIncludeFee(String itemid) {
        double total_amount = 0.00;
        double lineitemfee= 0.00,lineitemcount=0.00;
        try {
            Cursor c = Util.db.getTicketPoolCursor(" where "
                    + DBFeilds.ITEMPOOL_ADDON_PARENT + " = '"
                    + itemid + "'");
            //Cursor c = db.rawQuery("Select SUM (Order_Total) form OrderDetails " + whereClause, null);
            if (c != null) {
                c.moveToFirst();
                for (int j = 0; j < c.getCount(); j++) {
                    lineitemfee= Util.db.getItemType_BL_FEE(BaseActivity.checkedin_event_record.Events.Id,c.getString(c.getColumnIndex(DBFeilds.ITEM_TYPE_ID))) ;
                    if (j != (c.getCount() - 1)) {
                        lineitemcount=Double.valueOf(c.getString(c.getColumnIndex(DBFeilds.ITEMPOOL_ADDON_COUNT)));
                    }
                    c.moveToNext();
                    total_amount=(lineitemfee*lineitemcount)+total_amount;
                }
            }
            /*if (c.moveToFirst()) {
                total_amount = c.getDouble(c.getColumnIndex("Order_Total"));
            }*/
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return total_amount;
    }


    public String getItemPoolParentId(String item_pool_id, String event_id){
        String item_parent_id = ITransaction.EMPTY_STRING;
        try {
            Cursor c = db.rawQuery("Select Addon_Parent__c from "+DBFeilds.TABLE_ITEM_POOL+" where "+DBFeilds.ITEMPOOL_ID+" = '"+item_pool_id+"' AND "+DBFeilds.ITEMPOOL_EVENT_ID+" = '"+event_id+"'", null);
            if(c.moveToFirst()){
                item_parent_id = c.getString(c.getColumnIndex(DBFeilds.ITEMPOOL_ADDON_PARENT));
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return item_parent_id;
    }
    public String getItem_Pool_ID(String item_id,String event_id){
        String name = ITransaction.EMPTY_STRING;
        try {
            Cursor c = db.rawQuery("select * from "+DBFeilds.TABLE_ADDED_TICKETS+" where "+DBFeilds.ADDED_ITEM_ID+" = '"+item_id+"' AND "+DBFeilds.ADDED_ITEM_EVENTID+" = '"+event_id+"'", null);
            if(c.moveToFirst()){
                name = c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_POOLID));
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return name;
    }

    public Cursor getGNUser(String where) {
        try {
            if (where.contains("where")) {
                where = where.replace("where", ITransaction.EMPTY_STRING);
            } else if (where.contains("Where")) {
                where = where.replace("Where", ITransaction.EMPTY_STRING);
            }
            Cursor cursor = this.db.rawQuery("Select * from USER_PROFILE where (Order_Status != 'Abandoned' AND Order_Status != 'Cancelled' AND Order_Status != 'Deleted') AND " + where, null);
            //before Order_Status = 'Fully Paid' OR Order_Status = 'Check Not Received'
            cursor.moveToFirst();
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public String getItemTypeName(String id) {
        String name = ITransaction.EMPTY_STRING;
        try {
            Cursor cursor = this.db.rawQuery("Select * from ItemType where ItemTypeId = '" + id + "'", null);
            if (cursor == null) {
                return name;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                name = cursor.getString(cursor.getColumnIndex(DBFeilds.ITEM_TYPE_NAME));
                cursor.close();
            }
            return name;
        } catch (Exception e) {
            e.printStackTrace();
            return name;
        }
    }
    public void InsertAndUpdateItemPool(TicketPoolHandler item_pool, String eventId) {
        try {
            //Log.i("---Item Pool Name---", ":" + item_pool.getPoolItemName());
            this.datavalues = new ContentValues();
            this.datavalues.put(DBFeilds.ITEMPOOL_EVENT_ID, eventId);
            this.datavalues.put(DBFeilds.ITEMPOOL_ITEMPOOLNAME, item_pool.getPoolItemName());
            this.datavalues.put(DBFeilds.ITEM_TYPE_ID, item_pool.getPoolItemType());
            this.datavalues.put(DBFeilds.ITEMPOOL_COUNT, String.valueOf(item_pool.getPoolItemCount()));
            this.datavalues.put(DBFeilds.ITEMPOOL_POOLNAME, item_pool.getPoolName());
            this.datavalues.put(DBFeilds.ITEMPOOL_ID, item_pool.getPoolId());
            this.datavalues.put(DBFeilds.ITEMPOOL_ADDON_COUNT, String.valueOf(item_pool.getAddonCount()));
            this.datavalues.put(DBFeilds.ITEMPOOL_ADDON_PARENT, item_pool.getAddonParentId());
            this.datavalues.put(DBFeilds.ITEMPOOL_BADGABLE, item_pool.getBadgable());
            this.datavalues.put(DBFeilds.ITEMPOOL_BADGE_LABEL, item_pool.getBadgeLabel());
            this.datavalues.put(DBFeilds.ITEMPOOL_TICKET_SETTINGS, item_pool.getItemSettings());
            this.datavalues.put(DBFeilds.ITEMPOOL_ALLOW_TICKETED_SESSION, String.valueOf(item_pool.getTicketed_Sessions__c()));
            if (isUserExists(DBFeilds.TABLE_ITEM_POOL, " where ItemPoolId = '" + item_pool.getPoolId() + "'" + " AND " + DBFeilds.ITEMPOOL_EVENT_ID + " = '" + eventId + "'")) {
                this.db.update(DBFeilds.TABLE_ITEM_POOL, this.datavalues, "ItemPoolId = '" + item_pool.getPoolId() + "'" + " AND " + DBFeilds.ITEMPOOL_EVENT_ID + " = '" + eventId + "'", null);
                //Log.i("DataBase", "Item Pool record Updated Successfull");
                return;
            }
            this.db.insert(DBFeilds.TABLE_ITEM_POOL, null, this.datavalues);
            //Log.i("DataBase", "Item Pool record Insert Successfull");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void InsertAndUpdateItem(TicketHandler item) {
        try {
            this.datavalues = new ContentValues();

            this.datavalues.put(DBFeilds.ITEMPOOL_EVENT_ID, item.getEvent__c());
            this.datavalues.put(DBFeilds.ITEMPOOL_ID, Util.NullChecker(item.getItem_Pool__c()));
            this.datavalues.put(DBFeilds.ADDED_ITEM_NAME, item.getItem_name__c());
            this.datavalues.put(DBFeilds.ADDED_ITEM_PRICE, String.valueOf(item.getPrice__c()));
            this.datavalues.put(DBFeilds.ADDED_ITEM_QUANTITY, String.valueOf(item.getItem_count__c()));
            this.datavalues.put(DBFeilds.ADDED_ITEM_ORDER_MAX_QUANTITY, String.valueOf(item.getMax_per_order__c()));
            this.datavalues.put(DBFeilds.ADDED_ITEM_ORDER_MIN_QUANTITY, String.valueOf(item.getMin_per_order__c()));
            this.datavalues.put(DBFeilds.ADDED_ITEM_SOLDQUANTITY, String.valueOf(item.getAvailable_Tickets__c()));
            this.datavalues.put(DBFeilds.TABLE_ITEM_TYPE, item.getItem_type__c());
            this.datavalues.put(DBFeilds.ADDED_ITEM_SALESSTARTDATE, item.getSale_start__c());
            this.datavalues.put(DBFeilds.ADDED_ITEM_SALESENDDATE, item.getSale_end__c());
            this.datavalues.put(DBFeilds.ADDED_ITEM_IMAGEURL, item.getImage_url());
            if(!Util.NullChecker(item.getItemFee()).isEmpty()) {
                this.datavalues.put(DBFeilds.ADDED_ITEM_FEE, item.getItemFee());
            }else{
                this.datavalues.put(DBFeilds.ADDED_ITEM_FEE, 0.00);
            }
            this.datavalues.put(DBFeilds.ADDED_ITEM_PAYMENTTYPE, item.getPayment__c());
            this.datavalues.put(DBFeilds.ADDED_ITEM_OPTION, item.getTicket_Settings__c());
            this.datavalues.put(DBFeilds.ADDED_ITEM_STATUS, item.getVisibility__c());
            this.datavalues.put(DBFeilds.ADDED_ITEM_SA_VISIBILITY, item.get_SA_Visibility__c());
            if (item.getService_fee__c().equalsIgnoreCase(this.context.getString(R.string.includefee))) {
                this.datavalues.put(DBFeilds.ADDED_ITEM_SERVICEFEE, "false");
            } else {
                this.datavalues.put(DBFeilds.ADDED_ITEM_SERVICEFEE, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
            }
            this.datavalues.put(DBFeilds.ITEM_TYPE_NAME, getItemTypeName(item.getItem_type__c()));
            this.datavalues.put(DBFeilds.ADDED_ITEM_ID, item.getId());
            this.datavalues.put(DBFeilds.ADDED_ITEM_IS_TAX_APPLICABLE, item.getTaxable__c());
            this.datavalues.put(DBFeilds.ADDED_ITEM_BL_FEE, item.item_type__r.BL_Fee_Amt_per_res__c);
            if (isUserExists(DBFeilds.TABLE_ADDED_TICKETS, " where ItemId = '" + item.getId() + "'" + " AND " + DBFeilds.ITEMPOOL_EVENT_ID + " = '" + item.getEvent__c() + "'")) {
                //Log.i("DataBase", "-------ItemName-------" + item.getItem_name__c());
                this.db.update(DBFeilds.TABLE_ADDED_TICKETS, this.datavalues, "ItemId = '" + item.getId() + "'" + " AND " + DBFeilds.ITEMPOOL_EVENT_ID + " = '" + item.getEvent__c() + "'", null);
                //Log.i("DataBase", "Item record Updated Successfull");
                return;
            }
            this.db.insert(DBFeilds.TABLE_ADDED_TICKETS, null, this.datavalues);
            //Log.i("DataBase", "Item record Insert Successfull");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void InsertUpdateRegsettings(ArrayList<RegistrationSettingsController> regsetting_list,boolean isResetSettings) {
        Iterator it = regsetting_list.iterator();
        while (it.hasNext()) {
            RegistrationSettingsController regsetting = (RegistrationSettingsController) it.next();
            //Log.i("------------Label Name--------------", ":" + regsetting.Defaullt_Label__c + ":" + regsetting.Included__c);
            if(!isResetSettings){
                if(regsetting.Setting_Type__c.equalsIgnoreCase("SAappdefault")){
                    continue;
                }
            }
            ContentValues values = new ContentValues();
            values.put(DBFeilds.REG_ID, regsetting.Id);
            //values.put(DBFeilds.REG_ITEM_ID, regsetting.Item__c);
            values.put(DBFeilds.REG_ITEM_ID, String.valueOf(regsetting.Update_Access__c));//attendee settings
            values.put(DBFeilds.REG_COLUMN_NAME, regsetting.Column_Name__c);
            values.put(DBFeilds.REG_DEFAULT_LABEL, regsetting.Defaullt_Label__c);
            values.put(DBFeilds.REG_INCLUDE, String.valueOf(regsetting.Included__c));
            values.put(DBFeilds.REG_LABEL_NAME, regsetting.Label_Name__c);
            values.put(DBFeilds.REG_REQUIRED, String.valueOf(regsetting.Required__c));
            values.put(DBFeilds.REG_EVENT_ID, regsetting.Event__c);
            values.put(DBFeilds.REG_SETTING_TYPE, regsetting.Setting_Type__c);
            values.put(DBFeilds.REG_GROUP_LABELNAME, regsetting.Group_Label__c);
            values.put(DBFeilds.REG_GROUP_NAME, regsetting.Group_Name__c);
            values.put(DBFeilds.REG_FIELDTYPE, regsetting.Custom_Message__c);
            //DBFeilds.USER_EVENT_ID + " = '" + BaseActivity.checkedin_event_record.Events.Id + "'"
            if (isRecordExists(DBFeilds.TABLE_ITEM_REG_SETTINGS, " where reg_id='" + regsetting.Id + "'" + " AND " + DBFeilds.REG_EVENT_ID + " = '" + regsetting.Event__c + "'")) {
                this.db.update(DBFeilds.TABLE_ITEM_REG_SETTINGS, values, "reg_id='" + regsetting.Id + "'"+ " AND " + DBFeilds.REG_EVENT_ID + " = '" + regsetting.Event__c + "'", null);
            } else {
                this.db.insert(DBFeilds.TABLE_ITEM_REG_SETTINGS, null, values);
            }
        }
        //createtemptable();
    }
    /*
        public void InsertandUpadteFeildTypes(TicketProfileController tktfeildtype){
            try{
                this.datavalues = new ContentValues();
                //newly added
                this.datavalues.put(DBFeilds.ATTENDEE_IMAGE, Util.NullChecker(tktfeildtype.User_Pic__c));
                this.datavalues.put(DBFeilds.ATTENDEE_COMPANY_LOGO, Util.NullChecker(tktfeildtype.Company_Logo__c));
                this.datavalues.put(DBFeilds.ATTENDEE_FIRST_NAME, Util.NullChecker(tktfeildtype.First_Name__c));
                this.datavalues.put(DBFeilds.ATTENDEE_LAST_NAME, Util.NullChecker(tktfeildtype.Last_Name__c));
                this.datavalues.put(DBFeilds.ATTENDEE_SUFFIX, Util.NullChecker(tktfeildtype.Suffix__c));
                this.datavalues.put(DBFeilds.ATTENDEE_PREFIX, Util.NullChecker(tktfeildtype.Prefix__c));
                this.datavalues.put(DBFeilds.ATTENDEE_EMAIL_ID, Util.NullChecker(tktfeildtype.Email__c));
                this.datavalues.put(DBFeilds.ATTENDEE_COMPANY, Util.NullChecker(tktfeildtype.TKT_Company__c));
                this.datavalues.put(DBFeilds.ATTENDEE_JOB_TILE, tktfeildtype.TKT_Job_Title__c);
                //Newly added columns
                //this.datavalues.put(DBFeilds.ATTENDEE_COMPANY, Util.NullChecker(tktfeildtype.TKT_Company__c));
                this.datavalues.put(DBFeilds.ATTENDEE_AGE, Util.NullChecker(tktfeildtype.Age__c));
                this.datavalues.put(DBFeilds.ATTENDEE_GENDER, Util.NullChecker(tktfeildtype.Gender__c));
                this.datavalues.put(DBFeilds.ATTENDEE_DOB, Util.NullChecker(tktfeildtype.DOB__c));
                this.datavalues.put(DBFeilds.ATTENDEE_PRIMARY_BUSINESS_CATEGORY, Util.NullChecker(tktfeildtype.Primary_Business_Category__c));
                this.datavalues.put(DBFeilds.ATTENDEE_SECONDARY_BUSINESS_CATEGORY, Util.NullChecker(tktfeildtype.Secondary_Business_Category__c));
                this.datavalues.put(DBFeilds.ATTENDEE_NUMBER_OF_EMPLOYEES, Util.NullChecker(tktfeildtype.Number_Of_Employees__c));
                this.datavalues.put(DBFeilds.ATTENDEE_ESTABLISHED_DATE, Util.NullChecker(tktfeildtype.Established_Date__c));
                this.datavalues.put(DBFeilds.ATTENDEE_REVENUE, Util.NullChecker(tktfeildtype.Revenue__c));
                this.datavalues.put(DBFeilds.ATTENDEE_TAX_ID, Util.NullChecker(tktfeildtype.Tax_Id__c));
                this.datavalues.put(DBFeilds.ATTENDEE_WEBSITE_URL, Util.NullChecker(tktfeildtype.Company_Website_URL__c));
                this.datavalues.put(DBFeilds.ATTENDEE_DUNS_NUMBER, Util.NullChecker(tktfeildtype.Duns_Number__c));
                this.datavalues.put(DBFeilds.ATTENDEE_BLOG_URL, Util.NullChecker(tktfeildtype.Blog_URL__c));
                this.datavalues.put(DBFeilds.ATTENDEE_DESCRIPTION, Util.NullChecker(tktfeildtype.Company_Description__c));
                this.datavalues.put(DBFeilds.ATTENDEE_KEYWORDS, Util.NullChecker(tktfeildtype.Keywords__c));
                this.datavalues.put(DBFeilds.ATTENDEE_EXCEPTIONAL_KEYWORDS, Util.NullChecker(tktfeildtype.Exceptional_Keywords__c));
                this.datavalues.put(DBFeilds.ATTENDEE_DBA, Util.NullChecker(tktfeildtype.DBA__c));
                this.datavalues.put(DBFeilds.ATTENDEE_BBB_NUMBER, Util.NullChecker(tktfeildtype.BBB_Number__c));
                this.datavalues.put(DBFeilds.ATTENDEE_GSA_SCHEDULE, Util.NullChecker(tktfeildtype.GSA_Schedule__c));
                this.datavalues.put(DBFeilds.ATTENDEE_GEOGRAPHICAL_REGION, Util.NullChecker(tktfeildtype.Geographical_Region__c));



                this.datavalues.put(DBFeilds.ATTENDEE_ETHNICITY, Util.NullChecker(tktfeildtype.Ethnicity__c));
                this.datavalues.put(DBFeilds.ATTENDEE_BUSINESS_STRUCTURE, Util.NullChecker(tktfeildtype.Business_Structure__c));
                this.datavalues.put(DBFeilds.ATTENDEE_CAGECODE, Util.NullChecker(tktfeildtype.CageCode__c));
                this.datavalues.put(DBFeilds.ATTENDEE_DISTRIBUTION_COUNTRY, Util.NullChecker(tktfeildtype.distribution_Country__c));
                this.datavalues.put(DBFeilds.ATTENDEE_FAXNUMBER, Util.NullChecker(tktfeildtype.FaxNumber__c));
                this.datavalues.put(DBFeilds.ATTENDEE_MANUFACTURES_COUNTRY, Util.NullChecker(tktfeildtype.Manufactures_Country__c));
                this.datavalues.put(DBFeilds.ATTENDEE_OUTSIDE_FACILITIES, Util.NullChecker(tktfeildtype.Outside_Facilities__c));
                this.datavalues.put(DBFeilds.ATTENDEE_REFERENCES1, Util.NullChecker(tktfeildtype.References1__c));
                this.datavalues.put(DBFeilds.ATTENDEE_REFERENCES2, Util.NullChecker(tktfeildtype.References2__c));
                this.datavalues.put(DBFeilds.ATTENDEE_SCOPEOFWORK1, Util.NullChecker(tktfeildtype.ScopeOfWork1__c));
                this.datavalues.put(DBFeilds.ATTENDEE_SCOPEOFWORK2, Util.NullChecker(tktfeildtype.ScopeOfWork2__c));
                this.datavalues.put(DBFeilds.ATTENDEE_SECONDARY_EMAIL, Util.NullChecker(tktfeildtype.Secondary_email__c));
                this.datavalues.put(DBFeilds.ATTENDEE_YEAR_IN_BUSINESS, Util.NullChecker(tktfeildtype.Year_in_business__c));
                    */
/*this.datavalues.put(DBFeilds.ATTENDEE_TABLE_NUMBER, Util.NullChecker(tktfeildtype.BLN_MM_Report_TKT_Table__c));
                this.datavalues.put(DBFeilds.ATTENDEE_LOCATION_ROOM, Util.NullChecker(tktfeildtype.Location_Room__c));
               *//*

            this.datavalues.put(DBFeilds.ATTENDEE_SPEAKERLINKEDINID, Util.NullChecker(tktfeildtype.SpeakerLinkedInId));
            this.datavalues.put(DBFeilds.ATTENDEE_SPEAKERTWITTERID, Util.NullChecker(tktfeildtype.SpeakerTwitterId));
            this.datavalues.put(DBFeilds.ATTENDEE_SPEAKERBLOGGER, Util.NullChecker(tktfeildtype.SpeakerBlogger));
            this.datavalues.put(DBFeilds.ATTENDEE_BIOGRAPHY, Util.NullChecker(tktfeildtype.Biography__c));
            //this.datavalues.put(DBFeilds.ATTENDEE_SPEAKERVIDEO, Util.NullChecker(tktfeildtype.SpeakerVideo));
            this.datavalues.put(DBFeilds.ATTENDEE_WHATSAPP, Util.NullChecker(tktfeildtype.WhatsApp__c));
            this.datavalues.put(DBFeilds.ATTENDEE_WECHAT, Util.NullChecker(tktfeildtype.Wechat__c));
            this.datavalues.put(DBFeilds.ATTENDEE_SKYPE, Util.NullChecker(tktfeildtype.Skype__c));
            this.datavalues.put(DBFeilds.ATTENDEE_SNAPCHAT, Util.NullChecker(tktfeildtype.Snapchat__c));
            this.datavalues.put(DBFeilds.ATTENDEE_INSTAGRAM, Util.NullChecker(tktfeildtype.Instagram__c));















            this.datavalues.put(DBFeilds.ATTENDEE_PHONE, Util.NullChecker(tktfeildtype.Phone__c));
            this.datavalues.put(DBFeilds.ATTENDEE_WORK_PHONE, Util.NullChecker(tktfeildtype.Work_Phone__c));
            this.datavalues.put(DBFeilds.ATTENDEE_WORK_ADDRESS_1, Util.NullChecker(tktfeildtype.Work_Address__r.Address1__c));
            this.datavalues.put(DBFeilds.ATTENDEE_WORK_ADDRESS_2, Util.NullChecker(tktfeildtype.Work_Address__r.Address2__c));
            this.datavalues.put(DBFeilds.ATTENDEE_WORK_CITY, Util.NullChecker(tktfeildtype.Work_Address__r.City__c));
            this.datavalues.put(DBFeilds.ATTENDEE_WORK_STATE, getStateLongName(Util.NullChecker(tktfeildtype.Work_Address__r.State__r.Name)));
            this.datavalues.put(DBFeilds.ATTENDEE_WORK_COUNTRY, getCountryName(Util.NullChecker(tktfeildtype.Work_Address__r.Country__r.Name)));
            this.datavalues.put(DBFeilds.ATTENDEE_WORK_ZIPCODE, Util.NullChecker(tktfeildtype.Work_Address__r.ZipCode__c));
            this.datavalues.put(DBFeilds.ATTENDEE_HOME_PHONE, Util.NullChecker(tktfeildtype.Home_Phone__c));
            this.datavalues.put(DBFeilds.ATTENDEE_HOME_ADDRESS_1, Util.NullChecker(tktfeildtype.Home_Address__r.Address1__c));
            this.datavalues.put(DBFeilds.ATTENDEE_HOME_ADDRESS_2, Util.NullChecker(tktfeildtype.Home_Address__r.Address2__c));
            this.datavalues.put(DBFeilds.ATTENDEE_HOME_CITY, Util.NullChecker(tktfeildtype.Home_Address__r.City__c));
            this.datavalues.put(DBFeilds.ATTENDEE_HOME_STATE, getStateLongName(Util.NullChecker(tktfeildtype.Home_Address__r.State__r.Name)));
            this.datavalues.put(DBFeilds.ATTENDEE_HOME_COUNTRY, getCountryName(Util.NullChecker(tktfeildtype.Home_Address__r.Country__r.Name)));
            this.datavalues.put(DBFeilds.ATTENDEE_HOME_ZIPCODE, Util.NullChecker(tktfeildtype.Home_Address__r.ZipCode__c));
            this.datavalues.put(DBFeilds.USER_EVENT_ID, BaseActivity.checkedin_event_record.Events.Id);

            //Log.i("-----------------Ticket Name And Badgable--------------", ":" + attendee_info.Item_Pool__r.Item_Pool_Name__c + " -- " + attendee_info.Item_Pool__r.Badgable__c);
            if (isUserExists(DBFeilds.TABLE_ATTENDEE_FEILDTYPES, " where "+ DBFeilds.USER_EVENT_ID + " = '" + BaseActivity.checkedin_event_record.Events.Id + "'")) {
                this.db.update(DBFeilds.TABLE_ATTENDEE_FEILDTYPES, this.datavalues, DBFeilds.USER_EVENT_ID + " = '" + BaseActivity.checkedin_event_record.Events.Id + "'", null);
                //Log.i("DataBase", "Checkid in date=" + attendee_info.getChickindate());
                //Log.i("DataBase", "Attendee record Updated Successfull");
            } else {
                this.db.insert(DBFeilds.TABLE_ATTENDEE_FEILDTYPES, null, this.datavalues);
                //Log.i("DataBase", "Checkid in date=" + attendee_info.getChickindate());
                //Log.i("DataBase", "Attendee record Insert Successfull");
            }}catch (Exception e){
            e.printStackTrace();
        }
    }
*/
    /*public void createtemptable(){
        String columns=Util.db.getRegchildsNameList();
        if(!Util.NullChecker(columns).isEmpty()) {
            final String CREATE_TEMP = "CREATE TABLE IF NOT EXISTS Tempdata (_id integer primary key autoincrement," + columns + " )";
            db.execSQL(CREATE_TEMP);
            this.db = db;
        }
    }*/
    public String getRegchildsNameList() {
        String allchilds="";
        ArrayList<String> reg_settings_list = new ArrayList();
        Cursor c = this.db.rawQuery("select distinct reg_column_name from Item_Reg_Settings where reg_group_name__c !='' AND reg_eventid = '" + BaseActivity.checkedin_event_record.Events.Id+ "'", null);
        if (c != null) {
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                /*RegistrationSettingsController reg_setting = new RegistrationSettingsController();
                reg_setting.Group_Name__c = c.getString(c.getColumnIndex(DBFeilds.REG_GROUP_NAME));*/
                if (i != (c.getCount() - 1)) {
                    allchilds = allchilds +c.getString(c.getColumnIndex(DBFeilds.REG_COLUMN_NAME)).replaceAll(" ","_")+ " TEXT ,";
                }else {
                    allchilds= allchilds+c.getString(c.getColumnIndex(DBFeilds.REG_COLUMN_NAME)).replaceAll(" ","_")+" TEXT ";
                }
            }
        }
        c.close();
        return allchilds;
    }
    public ArrayList<RegistrationSettingsController> getRegSettingsList(String where) {
        ArrayList<RegistrationSettingsController> reg_settings_list = new ArrayList();
        Cursor c = this.db.rawQuery("select * from Item_Reg_Settings " + where, null);
        if (c != null) {
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                RegistrationSettingsController reg_setting = new RegistrationSettingsController();
                reg_setting.Id = c.getString(c.getColumnIndex(DBFeilds.REG_ID));
                reg_setting.Column_Name__c = c.getString(c.getColumnIndex(DBFeilds.REG_COLUMN_NAME));
                reg_setting.Defaullt_Label__c = c.getString(c.getColumnIndex(DBFeilds.REG_DEFAULT_LABEL));
                reg_setting.Included__c = Boolean.valueOf( c.getString(c.getColumnIndex(DBFeilds.REG_INCLUDE)));
                reg_setting.Item__c = c.getString(c.getColumnIndex(DBFeilds.REG_ITEM_ID));
                reg_setting.Label_Name__c = c.getString(c.getColumnIndex(DBFeilds.REG_LABEL_NAME));
                reg_setting.Required__c = Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.REG_REQUIRED)));
                reg_setting.Setting_Type__c = c.getString(c.getColumnIndex(DBFeilds.REG_SETTING_TYPE));
                reg_setting.Event__c = c.getString(c.getColumnIndex(DBFeilds.REG_EVENT_ID));
                reg_settings_list.add(reg_setting);
            }
        }
        c.close();
        return reg_settings_list;
    }
    public ArrayList<String> getRegGroupNameList(String where) {
        ArrayList<String> reg_settings_list = new ArrayList();
        Cursor c = this.db.rawQuery("select distinct reg_group_name__c from Item_Reg_Settings " + where, null);
        if (c != null) {
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                /*RegistrationSettingsController reg_setting = new RegistrationSettingsController();
                reg_setting.Group_Name__c = c.getString(c.getColumnIndex(DBFeilds.REG_GROUP_NAME));*/
                if(!c.getString(c.getColumnIndex(DBFeilds.REG_GROUP_NAME)).isEmpty())
                    reg_settings_list.add(c.getString(c.getColumnIndex(DBFeilds.REG_GROUP_NAME)));
            }
        }
        c.close();
        return reg_settings_list;
    }
    public ArrayList<RegistrationSettingsController> getRegGroupandLabelNameList(String where) {
        ArrayList<RegistrationSettingsController> reg_settings_list = new ArrayList<>();
        Cursor c = this.db.rawQuery("select distinct reg_group_name__c from Item_Reg_Settings " + where, null);
        if (c != null) {
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                RegistrationSettingsController reg_setting = new RegistrationSettingsController();
                if(!c.getString(c.getColumnIndex(DBFeilds.REG_GROUP_NAME)).isEmpty()) {
                    reg_setting.Group_Name__c = c.getString(c.getColumnIndex(DBFeilds.REG_GROUP_NAME));
                    reg_settings_list.add(reg_setting);
                }
            }
        }
        c.close();
        return reg_settings_list;
    }
    public String getRegGroupLabelNamewithGroupName(String where,String groupname) {
        String labelname="";
        Cursor c = this.db.rawQuery("select distinct reg_group_label__c from Item_Reg_Settings " + where, null);
        if (c != null&&c.getCount() > 0) {
            c.moveToFirst();
            if (!c.getString(c.getColumnIndex(DBFeilds.REG_GROUP_LABELNAME)).isEmpty())
                labelname = c.getString(c.getColumnIndex(DBFeilds.REG_GROUP_LABELNAME));
            else {
                labelname = groupname;
            }

        }

        c.close();
        return labelname;
    }
    public void UpdateFieldTypes(){
        Cursor c = this.db.rawQuery("select reg_column_name,reg_id from Item_Reg_Settings where reg_setting_type ='Scan Attendee'",null);
        if (c != null) {
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                String columnname=c.getString(c.getColumnIndex(DBFeilds.REG_COLUMN_NAME));
                String reg_id=c.getString(c.getColumnIndex(DBFeilds.REG_ID));
                if(!columnname.isEmpty()&&!columnname.contains(" ")) {
                    Cursor c1 = this.db.rawQuery("select " + columnname + " from OrderTicketFeildTypes", null);
                    c1.moveToFirst();
                    String fieldtype=c1.getString(c1.getColumnIndex(columnname));
                    ContentValues values = new ContentValues();
                    values.put(DBFeilds.REG_ID, reg_id);
                    values.put(DBFeilds.REG_FIELDTYPE, fieldtype);
                    if (isRecordExists(DBFeilds.TABLE_ITEM_REG_SETTINGS, " where reg_id='" + reg_id + "'")) {
                        this.db.update(DBFeilds.TABLE_ITEM_REG_SETTINGS, values, "reg_id='" + reg_id + "'", null);
                    } else {
                        this.db.insert(DBFeilds.TABLE_ITEM_REG_SETTINGS, null, values);
                    }

                }else {
                    c.moveToNext();
                }


            }
        }
        c.close();
    }
    public Cursor getRegChildswithGroupName(String where) {
        // ArrayList<RegistrationSettingsController> reg_settings_list = new ArrayList();
        Cursor c = this.db.rawQuery("select * from Item_Reg_Settings INNER JOIN OrderTicketDetails  " + where, null);
        //        Cursor c = this.db.rawQuery("select reg_id,reg_include,reg_column_name,reg_default_label,reg_label_name,reg_eventid from Item_Reg_Settings " + where, null);
        if (c != null) {
            return c;
           /* for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                *//*RegistrationSettingsController reg_setting = new RegistrationSettingsController();
                reg_setting.Id = c.getString(c.getColumnIndex(DBFeilds.REG_ID));
                reg_setting.Column_Name__c = c.getString(c.getColumnIndex(DBFeilds.REG_COLUMN_NAME));
                reg_setting.Defaullt_Label__c = c.getString(c.getColumnIndex(DBFeilds.REG_DEFAULT_LABEL));
                reg_setting.Included__c = Boolean.valueOf( c.getString(c.getColumnIndex(DBFeilds.REG_INCLUDE)));
                reg_setting.Label_Name__c = c.getString(c.getColumnIndex(DBFeilds.REG_LABEL_NAME));
               // reg_setting.Required__c = Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.REG_REQUIRED)));
               // reg_setting.Setting_Type__c = c.getString(c.getColumnIndex(DBFeilds.REG_SETTING_TYPE));
                reg_setting.Event__c = c.getString(c.getColumnIndex(DBFeilds.REG_EVENT_ID));
                reg_settings_list.add(reg_setting);*//*
            }*/
        }
        c.close();
        return null;
    }

    public void deleteItemRegsettings(String item_id) {
        this.db.delete(DBFeilds.TABLE_ITEM_REG_SETTINGS, "reg_item_id='" + item_id + "'", null);
    }

   /* public boolean deleteItemType1(String itemid) {
        try {
            this.db.delete(DBFeilds.TABLE_ADDED_ITEMS, "ItemId = '" + itemid + "'", null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }*/

    public boolean deleteTicketType(String itemid, String poolid) {
        try {
            this.db.delete(DBFeilds.TABLE_ADDED_TICKETS, "ItemId = '" + itemid + "'" + " AND " + DBFeilds.ITEMPOOL_ID + " = '" + poolid + "'", null);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

   /* public String getItemName1(String item_id, String event_id) {
        String item = ITransaction.EMPTY_STRING;
        try {
            Cursor c = this.db.rawQuery("Select * from AddedItems where ItemId = '" + item_id + "'" + " AND " + DBFeilds.ITEMPOOL_EVENT_ID + " = '" + event_id + "'", null);
            if (c == null) {
                return item;
            }
            if (c.getCount() > 0) {
                c.moveToFirst();
                item = c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_NAME));
                c.close();
            }
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }
		return item;
    }*/

    public String getItemCollectInfoSettings(String item_id, String event_id) {
        String item = ITransaction.EMPTY_STRING;
        try {
            Cursor c = this.db.rawQuery("Select * from ItemDetails where ItemId = '" + item_id + "'" + " AND " + DBFeilds.ADDED_ITEM_EVENTID + " = '" + event_id + "'", null);

            if (c.moveToFirst()) {
                item = c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_OPTION));
            }
            c.close();
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }
    //sai change
    public String getItemBadgble(String item_id, String event_id) {
        String item = ITransaction.EMPTY_STRING;
        try {
            Cursor c = this.db.rawQuery("Select * from ItemDetails where Item_Id = '" + item_id + "'" + " AND " + DBFeilds.ATTENDEE_BADGABLE + " = '" + event_id + "'", null);

            //Cursor c = this.db.rawQuery("Select * from OrderTicketDetails where Item_Id = '" + item_id + "'" + " AND " + DBFeilds.ATTENDEE_BADGABLE + " = '" + event_id + "'", null);

            if (c.moveToFirst()) {
                item = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE));
            }
            c.close();
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }
   /* public String getItemTypeName(String item_id, String event_id) {
        String item = ITransaction.EMPTY_STRING;
        try {
            Cursor c = this.db.rawQuery("Select * from AddedItems where ItemId = '" + item_id + "'" + " AND " + DBFeilds.ITEMPOOL_EVENT_ID + " = '" + event_id + "'", null);
            if (c == null) {
                return item;
            }
            if (c.getCount() > 0) {
                c.moveToFirst();
                item = c.getString(c.getColumnIndex(DBFeilds.ITEM_TYPE_NAME));
                c.close();
            }
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }
		return item;
    }*/

    public void updateEventCheckedInStatus(String eventid, String isEventStaff, String modifydate) {
        try {
            this.datavalues = new ContentValues();
            this.datavalues.put(DBFeilds.EVENT_ISCHECKEDIN, "Yes");
            this.datavalues.put(DBFeilds.EVENT_ISEVENT_STAFF, isEventStaff);
            this.datavalues.put(DBFeilds.EVENT_LASTMODIFYDATE, modifydate);
            this.db.update(Util.EVENT_DETAILS, this.datavalues, "EventID = '" + eventid + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateEventRefreshDate(String eventid, String modifydate) {
        try {
            this.datavalues = new ContentValues();
            this.datavalues.put(DBFeilds.EVENT_LASTMODIFYDATE, modifydate);
            this.db.update(Util.EVENT_DETAILS, this.datavalues, "EventID = '" + eventid + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteBadgeTable(String eventid) {
        this.db.beginTransaction();
        try {
            this.db.delete(DBFeilds.TABLE_BADGE_TEMPLATE, "EventID = '" + eventid + "'", null);
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateBadgeStatus(String tempName, String whereClause) {
        //Log.i("----Where Clause----------" + whereClause + "-----Badge Name---", ":" + tempName);
        Cursor c = null;
        try {
            c = this.db.rawQuery("Select * from BadgeTemplateNew" + whereClause, null);
            //Log.i(new StringBuilder(String.valueOf(whereClause)).append("-----Template count---").toString(), ":" + c.getCount());
            if (c.getCount() != 0) {
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    this.datavalues = new ContentValues();
                    if (tempName.equalsIgnoreCase(c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_ID)))) {
                        this.datavalues.put(DBFeilds.BADGE_NEW_IS_SELECTED, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
                        this.db.update(DBFeilds.TABLE_BADGE_TEMPLATE_NEW, this.datavalues, "badge_id = '" + c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_ID)) + "'", null);
                        //Log.i(new StringBuilder(String.valueOf(tempName)).append("----isBadgeSelected----").toString(), ": true");
                    } else {
                        this.datavalues.put(DBFeilds.BADGE_NEW_IS_SELECTED, "false");
                        this.db.update(DBFeilds.TABLE_BADGE_TEMPLATE_NEW, this.datavalues, "badge_id = '" + c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_ID)) + "'", null);
                        //Log.i(new StringBuilder(String.valueOf(tempName)).append("----isBadgeSelected----").toString(), ": false");
                    }
                }
            }
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (c != null) {
                c.close();
            }
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
        }
    }

    public void updateEventBadgeName(String name, String eventid) {
        try {
            this.datavalues = new ContentValues();
            this.datavalues.put(DBFeilds.EVENT_BADGE_NAME, name);
            this.db.update(Util.EVENT_DETAILS, this.datavalues, "EventID = '" + eventid + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void InsertAndUpdateBadgeTemplate(ArrayList<BadgeController> badge_handler) {
        try {
            Iterator it = badge_handler.iterator();
            while (it.hasNext()) {
                BadgeController badge_data = (BadgeController) it.next();
                Iterator it2 = badge_data.badge_elm.iterator();
                while (it2.hasNext()) {
                    BadgeElements element = (BadgeElements) it2.next();
                    this.datavalues = new ContentValues();
                    this.datavalues.put(DBFeilds.BADGE_BGCOLOR, Util.NullChecker(badge_data.getBadgebackgroundcolor()));
                    if (Util.NullChecker(element.getElementfontSize()).indexOf("px") > 0) {
                        this.datavalues.put(DBFeilds.BADGE_FONTSIZE, Util.NullChecker(element.getElementfontSize()).replace("px", ITransaction.EMPTY_STRING));
                    } else {
                        this.datavalues.put(DBFeilds.BADGE_FONTSIZE, Util.NullChecker(element.getElementfontSize()));
                    }
                    this.datavalues.put(DBFeilds.BADGE_TEXTALIGN, Util.NullChecker(element.getElementtextAlign()));
                    if (Util.NullChecker(element.getElementmarginTop()).indexOf("px") > 0) {
                        this.datavalues.put(DBFeilds.BADGE_MARGINTOP, Util.NullChecker(element.getElementpositionTop()).replace("px", ITransaction.EMPTY_STRING));
                    } else {
                        this.datavalues.put(DBFeilds.BADGE_MARGINTOP, Util.NullChecker(element.getElementpositionTop()));
                    }
                    this.datavalues.put(DBFeilds.BADGE_MARGINRIGHT, "0");
                    this.datavalues.put(DBFeilds.BADGE_MARGINBOTTOM, "0");
                   /* this.datavalues.put(DBFeilds.BADGE_MARGINRIGHT, AppEventsConstants.EVENT_PARAM_VALUE_NO);
                    this.datavalues.put(DBFeilds.BADGE_MARGINBOTTOM, AppEventsConstants.EVENT_PARAM_VALUE_NO);*/
                    if (Util.NullChecker(element.getElementmarginLeft()).indexOf("px") > 0) {
                        this.datavalues.put(DBFeilds.BADGE_MARGINLEFT, Util.NullChecker(element.getElementpositionLeft()).replace("px", ITransaction.EMPTY_STRING));
                    } else {
                        this.datavalues.put(DBFeilds.BADGE_MARGINLEFT, Util.NullChecker(element.getElementpositionLeft()));
                    }
                    this.datavalues.put(DBFeilds.BADGE_ELEMENTID, element.getElementid());
                    this.datavalues.put(DBFeilds.BADGE_FONTFAMILY, Util.NullChecker(element.getElementfontFamily()));
                    this.datavalues.put(DBFeilds.BADGE_WIDTH, Util.NullChecker(element.getElementwidth()));
                    this.datavalues.put(DBFeilds.BADGE_HEIGHT, Util.NullChecker(element.getElementheight()));
                    this.datavalues.put(DBFeilds.BADGE_BADGETEMPLATE_TYPE, badge_data.getBadgetemplatetype());
                    this.datavalues.put(DBFeilds.BADGE_BGCOLOR, element.getElementbackgroundColor());
                    this.datavalues.put(DBFeilds.BADGE_TEXTCOLOR, element.getElementtextColor());
                    this.datavalues.put(DBFeilds.BADGE_BADGE_WIDTH, badge_data.getBadgewith());
                    this.datavalues.put(DBFeilds.BADGE_BADGE_HEIGHT, badge_data.getBadgeheight());
                    this.datavalues.put(DBFeilds.BADGE_BADGE_NAME, badge_data.getBadgename());
                    this.datavalues.put(DBFeilds.BADGE_BADGE_IMAGE, ITransaction.EMPTY_STRING);
                    this.datavalues.put(DBFeilds.BADGE_BADGE_BGCOLOR, badge_data.getBadgebackgroundcolor());
                    this.datavalues.put(DBFeilds.EVENT_EVENT_ID, badge_data.getEventid());
                    this.datavalues.put(DBFeilds.BADGE_CONTENTTEXT, Util.NullChecker(element.getElementcontentText()));
                    if (isUserExists(DBFeilds.TABLE_BADGE_TEMPLATE, " where BadgeName = '" + badge_data.getBadgename() + "'" + " AND " + DBFeilds.BADGE_ELEMENTID + " = '" + element.getElementid() + "'" + " AND " + DBFeilds.EVENT_EVENT_ID + " = '" + badge_data.getEventid() + "'")) {
                        this.db.update(DBFeilds.TABLE_BADGE_TEMPLATE, this.datavalues, "BadgeName = '" + badge_data.getBadgename() + "'" + " AND " + DBFeilds.BADGE_ELEMENTID + " = '" + element.getElementid() + "'" + " AND " + DBFeilds.EVENT_EVENT_ID + " = '" + badge_data.getEventid() + "'", null);
                        //Log.i("----BadgeTemplate Updated successfully----", ":");
                    } else {
                        this.datavalues.put(DBFeilds.BADGE_ISBADGE_SELECTED, "No");
                        this.db.insert(DBFeilds.TABLE_BADGE_TEMPLATE, null, this.datavalues);
                        //Log.i("----BadgeTemplate inserted successfully----", ":");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateScanAttendeeFee(String eventid, boolean isFeeApplicable) {
        try {
            this.datavalues = new ContentValues();
            if (isFeeApplicable) {
                this.datavalues.put(DBFeilds.EVENT_FEE_APPLICABLE, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
            } else {
                this.datavalues.put(DBFeilds.EVENT_FEE_APPLICABLE, "false");
            }
            this.db.update(Util.EVENT_DETAILS, this.datavalues, "EventID = '" + eventid + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cursor getTicketCursor(String whereClause) {
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("Select * from ItemDetails" + whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public void InsertHideStatus(String hidestatus,String itempool_id,String eventid) {
        try {

            this.datavalues = new ContentValues();

            //this.db.update(DBFeilds.TABLE_HIDEITEMS, this.datavalues, "Item_Poolid = '" + itempool_id + "' AND Event_id = '" + eventid + "'" , null);
            if (!isUserExists(DBFeilds.TABLE_HIDEITEMS, " where Item_Poolid = '" + itempool_id + "'" + " AND Event_id = '" + eventid + "'")) {
                this.datavalues.put(DBFeilds.HIDE_ITEM_EVENTID, eventid);
                this.datavalues.put(DBFeilds.HIDE_ITEMPOOLID, itempool_id);
                this.datavalues.put(DBFeilds.HIDE_ITEMSTATUS, hidestatus);
                this.db.insert(DBFeilds.TABLE_HIDEITEMS, null, this.datavalues);
                //Log.i("----BadgeTemplate inserted successfully----", ":");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void InsertandUpdateHideStatus(String hidestatus,String itempool_id,String eventid) {
        try {

            this.datavalues = new ContentValues();
            this.datavalues.put(DBFeilds.HIDE_ITEM_EVENTID, eventid);
            this.datavalues.put(DBFeilds.HIDE_ITEMPOOLID, itempool_id);
            this.datavalues.put(DBFeilds.HIDE_ITEMSTATUS, hidestatus);
            this.db.update(DBFeilds.TABLE_HIDEITEMS, this.datavalues, "Item_Poolid = '" + itempool_id + "'" + " AND  Event_id = '" + eventid + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void InsertandUpdatePicklistvalues(List<AllPickListValues> pickListValuesArrayList) {
        try {

            this.datavalues = new ContentValues();
            for (int i=0;i<pickListValuesArrayList.size();i++){
                this.datavalues.put(DBFeilds.fieldName, pickListValuesArrayList.get(i).fieldName);
                if(pickListValuesArrayList.get(i).customvalues!=null) {
                    for (int j = 0; j< pickListValuesArrayList.get(i).customvalues.size(); j++) {
                        this.datavalues.put(DBFeilds.CustomvalueName, pickListValuesArrayList.get(i).customvalues.get(j).Name);
                        this.datavalues.put(DBFeilds.List_Description__c, pickListValuesArrayList.get(i).customvalues.get(j).List_Description__c);
                        this.datavalues.put(DBFeilds.List_Code__c, pickListValuesArrayList.get(i).customvalues.get(j).List_Code__c);
                        this.datavalues.put(DBFeilds.CustomvalueID, pickListValuesArrayList.get(i).customvalues.get(j).Id);
                        this.datavalues.put(DBFeilds.Sort_Order__c, pickListValuesArrayList.get(i).customvalues.get(j).Sort_Order__c);
                        this.datavalues.put(DBFeilds.Picklistvalue,"");
                        if (isRecordExists(DBFeilds.TABLE_PICKLISTVALUES, " where Customvalueid = '" + pickListValuesArrayList.get(i).customvalues.get(j).Id + "'")) {
                            this.db.update(DBFeilds.TABLE_PICKLISTVALUES, this.datavalues, "Customvalueid = '" + pickListValuesArrayList.get(i).customvalues.get(j).Id + "'", null);
                            //Log.i("DATABASE CLASS", "---------Attendee Address Updated Successfully");
                        }else {
                            this.db.insert(DBFeilds.TABLE_PICKLISTVALUES, null, this.datavalues);
                        }
                    }
                } else if(pickListValuesArrayList.get(i).picklistvalues!=null) {
                    String stringArray[] = pickListValuesArrayList.get(i).picklistvalues;
                    StringBuffer sb = new StringBuffer();
                    for (int k = 0; k < stringArray.length; k++) {
                        if (k != (stringArray.length - 1))
                            sb.append(stringArray[k]+";");
                        else
                            sb.append(stringArray[k]);
                    }
                    String str = sb.toString();
                    this.datavalues.put(DBFeilds.Picklistvalue,str);
                    if (isRecordExists(DBFeilds.TABLE_PICKLISTVALUES, " where fieldName = '" + pickListValuesArrayList.get(i).fieldName + "'")) {
                        this.db.update(DBFeilds.TABLE_PICKLISTVALUES, this.datavalues, "fieldName = '" + pickListValuesArrayList.get(i).fieldName + "'", null);
                        //Log.i("DATABASE CLASS", "---------Attendee Address Updated Successfully");
                    }else {
                        this.db.insert(DBFeilds.TABLE_PICKLISTVALUES, null, this.datavalues);
                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getPicklistItemId(String DbFeild ,String fieldvalue){
        String id="";
        if(!fieldvalue.equalsIgnoreCase("--None--")){try{
            Cursor c = this.db.rawQuery("Select Customvalueid  from "+DBFeilds.TABLE_PICKLISTVALUES + " where " + DBFeilds.fieldName+" = '"+DbFeild+"' AND "+  DBFeilds.List_Description__c+" = '"+fieldvalue+"'", null);
            if(c != null&&c.getCount()>0) {
                c.moveToFirst();
                id = c.getString(c.getColumnIndex(DBFeilds.CustomvalueID));
            }else if(id.isEmpty()){
                id = fieldvalue;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        }
        return id;
    }
    public List<String> getFiterItems(String DbFeild){
        List<String> Filtervalues=new ArrayList<>();;
        try {
            Cursor c = this.db.rawQuery("Select *  from "+DBFeilds.TABLE_PICKLISTVALUES + " where " + DBFeilds.fieldName+" = '"+DbFeild+"' ORDER BY Sort_Order__c ASC", null);
            Filtervalues.add("--None--");
            if(c != null) {
                if(c.getCount()<=1){
                    c.moveToFirst();
                    String s = c.getString(c.getColumnIndex(DBFeilds.Picklistvalue));
                    if (!s.isEmpty()) {
                        String[] nhv = s.split(";");
                        for (int i = 0; i < nhv.length; i++) {
                            Filtervalues.add(nhv[i]);
                        }
                    }
                }else{
                    for (int i = 0; i < c.getCount(); i++) {
                        c.moveToPosition(i);
                        Filtervalues.add(c.getString(c.getColumnIndex(DBFeilds.List_Description__c)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Filtervalues;
    }
    public Cursor getHideTicketCursor(String whereClause){
        Cursor c=null;
        try {
            c = db.rawQuery("Select * from ItemDetails "+whereClause
                    , null);
        }catch (Exception e){
            e.printStackTrace();
        }
        return c;
    }
    public boolean getHideStatus(String event_id,String itempoolid) {

        boolean isExits = false;
        try {
            Cursor cursor = this.db.rawQuery("Select * from HideItems where " + DBFeilds.HIDE_ITEM_EVENTID+" = '"+event_id+"' AND "+DBFeilds.HIDE_ITEMPOOLID+" = '"+itempoolid+"'", null);
            if(cursor != null){
                if(cursor.getCount()>0){
                    isExits = true;
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isExits;
    }
    public Cursor getSelfCheckinTicketCursor(String whereClause){
        Cursor c=null;
        try {
            c = db.rawQuery("Select * from ItemDetails "+whereClause, null);
        }catch (Exception e){
            e.printStackTrace();
        }
        return c;
    }
    public String getSelfCheckinSettings(String eventid,String column){
        Cursor c=null;
        //boolean include=false;
        String include="";
        try {
            c = db.rawQuery("select reg_column_name,reg_include from Item_Reg_Settings where reg_setting_type ='App Settings' and  reg_eventid='"+eventid+"'  and  reg_column_name='"+column+"'", null);
            if(c != null) {
                c.moveToFirst();
                include = c.getString(c.getColumnIndex(DBFeilds.REG_INCLUDE));
                //                include = Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.REG_INCLUDE)));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return include;
    }
    public List<SelfcheckinColumns> getSelfCheckinSettings(String eventid){
        List<SelfcheckinColumns>  columns=new ArrayList<>();
        Cursor c=null;
        boolean include=false;
        try {
            c = db.rawQuery("select reg_column_name,reg_include from Item_Reg_Settings where reg_setting_type ='App Settings' and  reg_eventid='"+eventid+"'", null);
            for(int i = 0;i<c.getCount();i++){
                SelfcheckinColumns column=new SelfcheckinColumns();
                c.moveToPosition(i);
                column.Allowaddnewattendee = (c.getString(c.getColumnIndex(DBFeilds.REG_COLUMN_NAME)));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return columns;
    }
    public Cursor getEventItems(String eventid){
        Cursor c=null;
        try {
            c = this.db.rawQuery("SELECT * FROM ItemDetails  INNER JOIN ItemPoolDetails where ((ItemDetails.ItemPoolId = ItemPoolDetails.ItemPoolId) OR (ItemPoolDetails.Addon_Parent__c='' AND ItemDetails.ItemTypeName = 'Package'))   AND ItemPoolDetails. ItemPool_Ticketed_Sessions__c='true' AND ItemDetails.EventId ='"+eventid+"' GROUP BY ItemDetails.ItemPoolId", null);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return c;
    }

    public int getItemsCount(String whereClause){
        int count = 0;
        try {
            Cursor cursor = this.db.rawQuery("Select count(*) from ItemDetails" + whereClause, null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        } catch (Exception e) {
            // TODO: handle exception
        }

        return count;
    }
    public HashMap<String, String> getALLItemNameAndId(String event_id){
        HashMap<String, String> free_items = new HashMap<String, String>();
        try {
            //Log.i("-------------Query-------------",":"+"Select * from ItemDetails item,ItemPoolDetails item_pool  where item."+DBFeilds.ADDED_ITEM_POOLID+" = item_pool."+DBFeilds.ITEMPOOL_ID+" AND " + DBFeilds.ADDED_ITEM_PAYMENTTYPE +" = 'Free' AND "+DBFeilds.ADDED_ITEM_EVENTID+"='"+event_id+"' AND item_pool."+DBFeilds.ITEMPOOL_ALLOW_TICKETED_SESSION+"='true'");
            Cursor c = db.rawQuery("Select * from ItemDetails item,ItemPoolDetails item_pool  where item."+DBFeilds.ADDED_ITEM_POOLID+" = item_pool."+DBFeilds.ITEMPOOL_ID+" AND item_pool."+DBFeilds.ITEMPOOL_BADGABLE+"= '"+"B - Badge"+"'"+" AND item." +DBFeilds.ADDED_ITEM_EVENTID+"='"+event_id+"' AND item_pool."+DBFeilds.ITEMPOOL_ALLOW_TICKETED_SESSION+"='true'", null);
            for(int i=0;i<c.getCount();i++){
                c.moveToPosition(i);
                free_items.put(c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_ID)), c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_NAME)));
            }
            c.close();

        } catch (Exception e) {
            // TODO: handle exception
            //Log.i("-----------------Exception------------",":"+e.getMessage());
        }
        return free_items;
    }
    public HashMap<String, String> getFreeItemNameAndId(String event_id){
        HashMap<String, String> free_items = new HashMap<String, String>();
        try {
            //Log.i("-------------Query-------------",":"+"Select * from ItemDetails item,ItemPoolDetails item_pool  where item."+DBFeilds.ADDED_ITEM_POOLID+" = item_pool."+DBFeilds.ITEMPOOL_ID+" AND " + DBFeilds.ADDED_ITEM_PAYMENTTYPE +" = 'Free' AND "+DBFeilds.ADDED_ITEM_EVENTID+"='"+event_id+"' AND item_pool."+DBFeilds.ITEMPOOL_ALLOW_TICKETED_SESSION+"='true'");
            Cursor c = db.rawQuery("Select * from ItemDetails item,ItemPoolDetails item_pool  where item."+DBFeilds.ADDED_ITEM_POOLID+" = item_pool."+DBFeilds.ITEMPOOL_ID+" AND item." + DBFeilds.ADDED_ITEM_PAYMENTTYPE +" = 'Free' AND item."+DBFeilds.ADDED_ITEM_EVENTID+"='"+event_id+"' AND item_pool."+DBFeilds.ITEMPOOL_ALLOW_TICKETED_SESSION+"='true'", null);
            for(int i=0;i<c.getCount();i++){
                c.moveToPosition(i);
                free_items.put(c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_ID)), c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_NAME)));
            }
            c.close();

        } catch (Exception e) {
            // TODO: handle exception
            //Log.i("-----------------Exception------------",":"+e.getMessage());
        }
        return free_items;
    }
    public HashMap<String, String> getSelfCheckinFreeItemNameAndId(String event_id){
        HashMap<String, String> free_items = new HashMap<String, String>();
        try {
            //Log.i("-------------Query-------------",":"+"Select * from ItemDetails item,ItemPoolDetails item_pool  where item."+DBFeilds.ADDED_ITEM_POOLID+" = item_pool."+DBFeilds.ITEMPOOL_ID+" AND " + DBFeilds.ADDED_ITEM_PAYMENTTYPE +" = 'Free' AND "+DBFeilds.ADDED_ITEM_EVENTID+"='"+event_id+"' AND item_pool."+DBFeilds.ITEMPOOL_ALLOW_TICKETED_SESSION+"='true'");
            Cursor c = db.rawQuery("Select * from ItemDetails item,ItemPoolDetails item_pool  where item."
                    +DBFeilds.ADDED_ITEM_POOLID+" = item_pool."+DBFeilds.ITEMPOOL_ID+" AND item."
                    + DBFeilds.ADDED_ITEM_SA_VISIBILITY +"='true' AND item."
                    + DBFeilds.ADDED_ITEM_PAYMENTTYPE +" = 'Free' AND item."
                    +DBFeilds.ADDED_ITEM_EVENTID+"='"+event_id+"' AND item_pool."
                    +DBFeilds.ITEMPOOL_ALLOW_TICKETED_SESSION+"='true'", null);
            for(int i=0;i<c.getCount();i++){
                c.moveToPosition(i);
                free_items.put(c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_ID)), c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_NAME)));
            }
            c.close();

        } catch (Exception e) {
            // TODO: handle exception
            //Log.i("-----------------Exception------------",":"+e.getMessage());
        }
        return free_items;
    }
    public HashMap<String, String> getSelfCheckinPaidItemNameAndId(String event_id){
        HashMap<String, String> free_items = new HashMap<String, String>();
        try {
            //Log.i("-------------Query-------------",":"+"Select * from ItemDetails item,ItemPoolDetails item_pool  where item."+DBFeilds.ADDED_ITEM_POOLID+" = item_pool."+DBFeilds.ITEMPOOL_ID+" AND " + DBFeilds.ADDED_ITEM_PAYMENTTYPE +" = 'Free' AND "+DBFeilds.ADDED_ITEM_EVENTID+"='"+event_id+"' AND item_pool."+DBFeilds.ITEMPOOL_ALLOW_TICKETED_SESSION+"='true'");
            Cursor c = db.rawQuery("Select * from ItemDetails item,ItemPoolDetails item_pool  where item."
                    +DBFeilds.ADDED_ITEM_POOLID+" = item_pool."+DBFeilds.ITEMPOOL_ID+" AND item."
                    + DBFeilds.ADDED_ITEM_SA_VISIBILITY +"='true' AND item."
                    + DBFeilds.ADDED_ITEM_PAYMENTTYPE +" = 'Paid' AND (item."
                    + DBFeilds.ADDED_ITEM_STATUS +" = 'Public' OR item." + DBFeilds.ADDED_ITEM_STATUS+" = 'Private') AND item."
                    +DBFeilds.ADDED_ITEM_EVENTID+"='"+event_id+"' AND item_pool."
                    +DBFeilds.ITEMPOOL_ALLOW_TICKETED_SESSION+"='true'", null);
            for(int i=0;i<c.getCount();i++){
                c.moveToPosition(i);
                free_items.put(c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_ID)), c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_NAME)));
            }
            c.close();

        } catch (Exception e) {
            // TODO: handle exception
            //Log.i("-----------------Exception------------",":"+e.getMessage());
        }
        return free_items;
    }
    public HashMap<String, String> getSelfCheckinFreeandPaidItemNameAndId(String event_id){
        HashMap<String, String> free_items = new HashMap<String, String>();
        try {
            //Log.i("-------------Query-------------",":"+"Select * from ItemDetails item,ItemPoolDetails item_pool  where item."+DBFeilds.ADDED_ITEM_POOLID+" = item_pool."+DBFeilds.ITEMPOOL_ID+" AND " + DBFeilds.ADDED_ITEM_PAYMENTTYPE +" = 'Free' AND "+DBFeilds.ADDED_ITEM_EVENTID+"='"+event_id+"' AND item_pool."+DBFeilds.ITEMPOOL_ALLOW_TICKETED_SESSION+"='true'");
            Cursor c = db.rawQuery("Select * from ItemDetails item,ItemPoolDetails item_pool  where item."
                    +DBFeilds.ADDED_ITEM_POOLID+" = item_pool."+DBFeilds.ITEMPOOL_ID+" AND item."
                    + DBFeilds.ADDED_ITEM_SA_VISIBILITY +"='true' AND item."
                    + DBFeilds.ADDED_ITEM_PAYMENTTYPE +" = 'Free' AND item."
                    + DBFeilds.ADDED_ITEM_PAYMENTTYPE +" = 'Paid' AND item."
                    +DBFeilds.ADDED_ITEM_EVENTID+"='"+event_id+"' AND item_pool."
                    +DBFeilds.ITEMPOOL_ALLOW_TICKETED_SESSION+"='true'", null);
            for(int i=0;i<c.getCount();i++){
                c.moveToPosition(i);
                free_items.put(c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_ID)), c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_NAME)));
            }
            c.close();

        } catch (Exception e) {
            // TODO: handle exception
            //Log.i("-----------------Exception------------",":"+e.getMessage());
        }
        return free_items;
    }

    public boolean isPaidTicketExists(String event_id) {

        boolean isExits = false;
        try {
            Cursor cursor = this.db.rawQuery("Select * from ItemDetails where " + DBFeilds.ADDED_ITEM_PAYMENTTYPE+" = 'Paid' AND "+DBFeilds.ADDED_ITEM_EVENTID+" = '"+event_id+"'", null);
            if(cursor != null){
                if(cursor.getCount()>0){
                    isExits = true;
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isExits;
    }

    public Cursor getTicketPoolCursor(String whereClause) {
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("Select * from ItemPoolDetails" + whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public Cursor getBadgeTemplateCursor() {
        Cursor c = null;
        try {
            c = this.db.rawQuery("Select * from BadgeTemplateNew " , null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }
 /*
    public Cursor getBadgeTemplate(String whereClause) {
        try {
            Cursor c = this.db.rawQuery("Select * from BadgeTemplate " + whereClause, null);
            //Log.i("-----Badge Templae Size now----", ":" + c.getCount());
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getBadgeTemplate() {
        try {
            Cursor c = this.db.rawQuery("Select * from BadgeTemplate", null);
            //Log.i("-----Badge Templae Size now----", ":" + c.getCount());
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/

    public Cursor getOrderDetailPool(String whereClause) {
        try {
            return this.db.rawQuery("Select * from OrderItemDetails" + whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getOrderTicketDetails(String whereClause) {
        Cursor cursor = null;
        //Log.i("Database", whereClause);
        try {
            cursor = this.db.rawQuery("Select * from OrderTicketDetails" + whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public Cursor getPaymentTicketDataCursor(String whereClause) {
        try {
            return this.db.rawQuery("Select * from OrderDetails" + whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getRefundDataCursor(String whereClause) {
        try {
            return this.db.rawQuery("Select attendee.EventID, attendee._id, attendee.AttendeeID,attendee.FirstName, attendee.LastName, payment._id, payment.EventID,payment.StripePaymentID,payment.CheckedInTicketNumber,payment.TicketName,payment.ItemName,payment.AttendeeID,payment.PaymentID,payment.PaymentType,payment.TicketID,payment.ItemID,payment.PaymentStatus,payment.CheckNumber,payment.TicketNumber, payment.PaymentDate, payment.TicketQty, payment.TotalAmount from AttendeeDetails attendee, SalesOrderDetails payment" + whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getPaymentCursor(String whereClause) {
        try {
            return this.db.rawQuery("Select * from OrderDetails INNER JOIN OrderPaymentItemDetails orderpayment ON Order_Id=orderpayment.payment_order_id "
                    + whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public String getOrderStatuswithAttendee(String attendee_order_id,String eventid){
        String orderstatus="";
        try {
            Cursor c = this.db.rawQuery("Select Order_Status from OrderDetails where " + DBFeilds.ORDERITEM_ORDER_ID+" = '"+attendee_order_id+"'AND "+ DBFeilds.ORDERITEM_EVENT_ID+" = '"+eventid+"'", null);
            if(c != null) {
                c.moveToFirst();
                if(!c.getString(c.getColumnIndex("Order_Status")).isEmpty())
                    orderstatus= c.getString(c.getColumnIndex("Order_Status"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderstatus;
    }

    public String getOrderStatuswithAttendeeID(String attendee_id,String eventid){
        String orderstatus="",attendee_order_id="";
        try {
            Cursor cursor = this.db.rawQuery("Select Order_Id from OrderTicketDetails where " + DBFeilds.ATTENDEE_ID+" = '"+attendee_id+"'AND "+ DBFeilds.ATTENDEE_EVENT_ID+" = '"+eventid+"'", null);
            if(cursor != null) {
                cursor.moveToFirst();
                if(!cursor.getString(cursor.getColumnIndex("Order_Id")).isEmpty())
                    attendee_order_id= cursor.getString(cursor.getColumnIndex("Order_Id"));
            }
            Cursor c = this.db.rawQuery("Select Order_Status from OrderDetails where " + DBFeilds.ORDERITEM_ORDER_ID+" = '"+attendee_order_id+"'AND "+ DBFeilds.ORDERITEM_EVENT_ID+" = '"+eventid+"'", null);
            if(c != null) {
                c.moveToFirst();
                if(!c.getString(c.getColumnIndex("Order_Status")).isEmpty())
                    orderstatus= c.getString(c.getColumnIndex("Order_Status"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderstatus;
    }

    public List<String> getEventOrderStatusFiterItems(String eventid,String DbFeild){
        List<String> eventorderstatus=new ArrayList<>();;
        try {
            Cursor c = this.db.rawQuery("Select distinct "+ DbFeild + " from OrderDetails where " + DBFeilds.ORDERITEM_EVENT_ID+" = '"+eventid+"'", null);
            eventorderstatus.add("All");
            if(c != null) {
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    if(!c.getString(c.getColumnIndex(DbFeild)).isEmpty()&&!c.getString(c.getColumnIndex(DbFeild)).equalsIgnoreCase("Deleted")&&!c.getString(c.getColumnIndex(DbFeild)).equalsIgnoreCase("Abandoned"))
                        eventorderstatus.add(c.getString(c.getColumnIndex(DbFeild)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eventorderstatus;
    }
    public List<String> getRSVPStatusFiterItems(String eventid,String DbFeild){
        List<String> RSVPStatus=new ArrayList<>();;
        try {
            Cursor c = this.db.rawQuery("Select distinct "+ DbFeild + " from OrderTicketDetails where " + DBFeilds.ATTENDEE_EVENT_ID+" = '"+eventid+"'", null);
            RSVPStatus.add("ALL");
            if(c != null) {
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    if(!Util.NullChecker(c.getString(c.getColumnIndex(DbFeild))).isEmpty())
                        RSVPStatus.add(c.getString(c.getColumnIndex(DbFeild)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RSVPStatus;
    }

    public List<String> getEventPaymentFiterItems(String eventid,String DbFeild){
        List<String> paymodes=new ArrayList<>();;
        try {
            Cursor c = this.db.rawQuery("Select distinct "+ DbFeild + " from OrderPaymentItemDetails where " + DBFeilds.ORDER_PAYMENT_ITEM_EVENTID+" = '"+eventid+"'", null);
            paymodes.add("All");
            if(c != null) {
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    if(!c.getString(c.getColumnIndex(DbFeild)).isEmpty())
                        paymodes.add(c.getString(c.getColumnIndex(DbFeild)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paymodes;
    }
    public Cursor getPaymentDataCursor(String whereClause) {
        if (whereClause.contains("where")) {
            whereClause = whereClause.replace("where", ITransaction.EMPTY_STRING);
        } else if (whereClause.contains("Where")) {
            whereClause = whereClause.replace("Where", ITransaction.EMPTY_STRING);
        }
        try {
            // return this.db.rawQuery("Select distinct orders.Event_Id, orders._id, orders.Order_Id, orders.Buyer_Id, orders.Payment_Type, orders.Order_Status, orders.Order_Total, orders.Order_Date, orders.PayKey, orders.Order_Name, orders.Order_SubTotal , orders.Fee_Amount, orders.Payment_Mode, orders.Amount_Paid,orders.Amount_Due,orders.Order_Discount, orders.Order_Tax, orders.registration_type,orders.check_number,orders.last_card_degits, orders.paygateway_type_id, orders.paygateway_type_name, orders.card_type, user.UserID, user.GNUserID, user.FirstName, user.LastName, user.Company, user.CompanyId, user.EmailID , user.PhoneNumber ,orderpayment.payment_order_id,orderpayment.payment_mode,orderpayment.payment_item_eventid,orderpayment.payment_pay_gateway_name from OrderPaymentItemDetails orderpayment,USER_PROFILE user , OrderDetails orders where orders.Order_Status != 'Abandoned' AND orders.Order_Status != 'Deleted' AND orders.Order_Id=orderpayment.payment_order_id AND orders.Event_Id=orderpayment.payment_item_eventid AND " + whereClause, null);

            return this.db.rawQuery("Select distinct orders.Event_Id, orders._id, orders.Order_Id, orders.Buyer_Id, orders.Payment_Type, orders.Order_Status, orders.Order_Total, orders.Order_Date, orders.PayKey, orders.Order_Name, orders.registration_type,orders.check_number,orders.last_card_degits, orders.paygateway_type_id, orders.paygateway_type_name, orders.card_type, user.UserID, user.GNUserID, user.FirstName, user.LastName, user.Company, user.CompanyId, user.EmailID , user.PhoneNumber ,orderpayment.payment_order_id,orderpayment.payment_item_eventid,orderpayment.payment_pay_gateway_name from OrderPaymentItemDetails orderpayment,USER_PROFILE user , OrderDetails orders where orders.Order_Status != 'Abandoned' AND orders.Order_Status != 'Deleted' AND orders.Order_Id=orderpayment.payment_order_id AND orders.Event_Id=orderpayment.payment_item_eventid AND " + whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Cursor getOrderDetailDataCursor(String whereClause) {
        if (whereClause.contains("where")) {
            whereClause = whereClause.replace("where", ITransaction.EMPTY_STRING);
        } else if (whereClause.contains("Where")) {
            whereClause = whereClause.replace("Where", ITransaction.EMPTY_STRING);
        }
        try {
            return this.db.rawQuery("Select distinct orders.Event_Id, orders._id, orders.Order_Id, orders.Buyer_Id, orders.Payment_Type, orders.Order_Status, orders.Order_Total, orders.Order_Date, orders.PayKey, orders.Order_Name, orders.Order_SubTotal , orders.Fee_Amount, orders.Payment_Mode, orders.Amount_Paid,orders.Amount_Due,orders.Order_Discount, orders.Order_Tax, orders.registration_type,orders.check_number,orders.last_card_degits, orders.paygateway_type_id, orders.paygateway_type_name, orders.card_type, user.UserID, user.GNUserID, user.FirstName, user.LastName, user.Company, user.CompanyId, user.EmailID , user.PhoneNumber ,orderpayment.payment_order_id,orderpayment.payment_mode,orderpayment.payment_item_eventid,orderpayment.payment_pay_gateway_name from OrderPaymentItemDetails orderpayment,USER_PROFILE user , OrderDetails orders where orders.Order_Status != 'Abandoned' AND orders.Order_Status != 'Deleted' AND orders.Order_Id=orderpayment.payment_order_id AND orders.Event_Id=orderpayment.payment_item_eventid AND " + whereClause, null);

            //return this.db.rawQuery("Select distinct orders.Event_Id, orders._id, orders.Order_Id, orders.Buyer_Id, orders.Payment_Type, orders.Order_Status, orders.Order_Total, orders.Order_Date, orders.PayKey, orders.Order_Name, orders.registration_type,orders.check_number,orders.last_card_degits, orders.paygateway_type_id, orders.paygateway_type_name, orders.card_type, user.UserID, user.GNUserID, user.FirstName, user.LastName, user.Company, user.CompanyId, user.EmailID , user.PhoneNumber ,orderpayment.payment_order_id,orderpayment.payment_item_eventid,orderpayment.payment_pay_gateway_name from OrderPaymentItemDetails orderpayment,USER_PROFILE user , OrderDetails orders where orders.Order_Status != 'Abandoned' AND orders.Order_Status != 'Deleted' AND orders.Order_Id=orderpayment.payment_order_id AND orders.Event_Id=orderpayment.payment_item_eventid AND " + whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /*
        public Cursor getPaymentFilterDataCursor(String whereClause) {
            if (whereClause.contains("where")) {
                whereClause = whereClause.replace("where", ITransaction.EMPTY_STRING);
            } else if (whereClause.contains("Where")) {
                whereClause = whereClause.replace("Where", ITransaction.EMPTY_STRING);
            }
            try {
                return this.db.rawQuery("Select distinct orders.Event_Id, orders._id, orders.Order_Id, orders.Buyer_Id, orders.Payment_Type, orders.Order_Status, orders.Order_Total, orders.Order_Date, orders.PayKey, orders.Order_Name, orders.Order_SubTotal , orders.Fee_Amount, orders.Payment_Mode, orders.Amount_Paid,orders.Amount_Due,orders.Order_Discount, orders.Order_Tax, orders.registration_type,orders.check_number,orders.last_card_degits, orders.paygateway_type_id, orders.paygateway_type_name, orders.card_type, user.UserID, user.GNUserID, user.FirstName, user.LastName, user.Company, user.CompanyId, user.EmailID , user.PhoneNumber ,orderpayment.payment_order_id,orderpayment.payment_mode,orderpayment.payment_item_eventid,orderpayment.payment_pay_gateway_name from OrderPaymentItemDetails orderpayment,USER_PROFILE user , OrderDetails orders where orders.Order_Status != 'Abandoned' AND orders.Order_Status != 'Deleted' AND orders.Order_Id=orderpayment.payment_order_id AND orders.Event_Id=orderpayment.payment_item_eventid AND " + whereClause, null);

                //return this.db.rawQuery("Select distinct orders.Event_Id, orders._id, orders.Order_Id, orders.Buyer_Id, orders.Payment_Type, orders.Order_Status, orders.Order_Total, orders.Order_Date, orders.PayKey, orders.Order_Name, orders.registration_type,orders.check_number,orders.last_card_degits, orders.paygateway_type_id, orders.paygateway_type_name, orders.card_type, user.UserID, user.GNUserID, user.FirstName, user.LastName, user.Company, user.CompanyId, user.EmailID , user.PhoneNumber ,orderpayment.payment_order_id,orderpayment.payment_item_eventid,orderpayment.payment_pay_gateway_name from OrderPaymentItemDetails orderpayment,USER_PROFILE user , OrderDetails orders where orders.Order_Status != 'Abandoned' AND orders.Order_Status != 'Deleted' AND orders.Order_Id=orderpayment.payment_order_id AND orders.Event_Id=orderpayment.payment_item_eventid AND " + whereClause, null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    */
    //public String getOrderID
    public Cursor getPaymentItemDataCursor(String whereClause) {
        try {
            return this.db.rawQuery("Select * from OrderPaymentItemDetails" + whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /*
        public Cursor getPaymentDataCursor(String whereClause) {
            if (whereClause.contains("where")) {
                whereClause = whereClause.replace("where", ITransaction.EMPTY_STRING);
            } else if (whereClause.contains("Where")) {
                whereClause = whereClause.replace("Where", ITransaction.EMPTY_STRING);
            }
            try {
                return this.db.rawQuery("Select orders.Event_Id, orders._id, orders.Order_Id, orders.Buyer_Id, orders.Payment_Type, orders.Order_Status, orders.Order_Total, orders.Order_Date, orders.PayKey, orders.Order_Name, orders.Order_SubTotal , orders.Fee_Amount, orders.Payment_Mode, orders.Amount_Paid, orders.Order_Tax, orders.registration_type,orders.check_number,orders.last_card_degits, orders.paygateway_type_id, orders.paygateway_type_name, orders.card_type, user.UserID, user.GNUserID, user.FirstName, user.LastName, user.Company, user.CompanyId, user.EmailID from USER_PROFILE user , OrderDetails INNER JOIN OrderPaymentItemDetails orders where orders.Order_Status != 'Abandoned' AND " + whereClause, null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    */
    //public String getOrderID
    public Cursor getPaymentModeswithOrderID(String order_id){
        try {
            return this.db.rawQuery("Select * from OrderPaymentItemDetails where payment_order_id='"+order_id+"'", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getTotalAmount(String whereClause){
        double total_amount = 0.00;
        try {

            Cursor c = db.rawQuery("Select SUM (Order_Total) form OrderDetails "+whereClause, null);
            if(c.moveToFirst()){
                total_amount = c.getDouble(c.getColumnIndex("Order_Total"));
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return total_amount;
    }

    public int totalOrderCount(String event_id) {
        int count = 0;
        Cursor c = this.db.rawQuery("Select count(*) from OrderDetails where Event_Id = '" + event_id + "' AND Order_Status != 'Abandoned' AND Order_Status != 'Deleted'", null);
        if(c.moveToFirst()){
            count = c.getInt(0);
        }
        c.close();
        return count;
    }
    public int totalOrderCountwithoutCancelled(String event_id) {
        int count = 0;
        Cursor c = this.db.rawQuery("Select count(*) from OrderDetails where Event_Id = '" + event_id + "' AND Order_Status != 'Abandoned' AND Order_Status != 'Deleted' AND Order_Status != 'Cancelled'", null);
        if(c.moveToFirst()){
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    public String getOrderId(String Ordername) {
        String order_id = ITransaction.EMPTY_STRING;
        Cursor c = this.db.rawQuery("Select * from OrderDetails where Order_Name LIKE '%" + Ordername + "%" + "'", null);
        if (c == null) {
            return order_id;
        }
        if (c.getCount() > 0) {
            c.moveToFirst();
            order_id = c.getString(c.getColumnIndex(DBFeilds.ORDER_ORDER_ID));
            c.close();
        }
        return order_id;
    }
    public String getOrderName(String Orderid) {
        String order_name= ITransaction.EMPTY_STRING;
        Cursor c = this.db.rawQuery("Select * from OrderDetails where "+DBFeilds.ORDER_ORDER_ID+"='"+Orderid+"'", null);
        if (c == null) {
            return order_name;
        }
        if (c.getCount() > 0) {
            c.moveToFirst();
            order_name = c.getString(c.getColumnIndex(DBFeilds.ORDER_ORDER_NAME));
            c.close();
        }
        return order_name;
    }
    public double getPaymentAmount(String whereClause) {
        try {
            double total_amt;
            Cursor c = this.db.rawQuery("Select sum(Order_Total) from OrderDetails" + whereClause, null);
            if (c.moveToFirst()) {
                total_amt = c.getDouble(0);
            } else {
                total_amt = 0.0d;
            }
            c.close();
            return total_amt;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0d;
        }
    }

    public Cursor getRefundCursor(String whereClause) {
        Cursor c = null;
        try {
            c = this.db.rawQuery("Select * from " + TABLE_SALES_REFUND + whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public void updatePayPalEmail(String user_id, String email) {
        try {
            if (this.db.rawQuery("Select * from USER_PROFILE where UserID ='" + user_id + "'", null).getCount() != 0) {
                this.datavalues = new ContentValues();
                this.datavalues.put("PaypalEmail", email);
                this.db.update(DBFeilds.TABLE_USER, this.datavalues, "UserID = '" + user_id + "'", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cursor getTotalTicketSold(String whereClause) {
        Cursor c = null;
        try {
            c = this.db.rawQuery("SELECT * from OrderTicketDetails attendee, SalesOrderDetails payment " + whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

   /* public void updateCheckedInStatus(String attendee_id, String eventId, String time, boolean status) {
        try {
        	 this.datavalues = new ContentValues();
			 //this.datavalues.put(DBFeilds.ATTENDEE_ISCHECKIN, String.valueOf(status));
			 this.datavalues.put(DBFeilds.ATTENDEE_CHECKEDINDATE, time);
			 this.db.update(DBFeilds.TABLE_ATTENDEE_DETAILS, this.datavalues,"Attendee_Id = '" + attendee_id + "'" + " AND " + DBFeilds.USER_EVENT_ID + " = '" + eventId + "'",	null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public void updateCheckedInStatus(TStatus tstatus,String event_id) {
        try {
            this.datavalues = new ContentValues();
            //this.datavalues.put(DBFeilds.ATTENDEE_ISCHECKIN, String.valueOf(status));
            //this.datavalues.put(DBFeilds.ATTENDEE_CHECKEDINDATE, tstatus.scan_time__c);
            //this.db.update(DBFeilds.TABLE_ATTENDEE_DETAILS, this.datavalues,"Attendee_Id = '" + tstatus.Ticket__c + "'" + " AND " + DBFeilds.USER_EVENT_ID + " = '" + event_id + "'",	null);
            InsertAndUpdateTSTATUS(tstatus,event_id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateFailedCheckedInStatus(TStatus tstatus,String event_id) {
        try {
            this.datavalues = new ContentValues();
            //this.datavalues.put(DBFeilds.ATTENDEE_ISCHECKIN, String.valueOf(status));
            this.datavalues.put(DBFeilds.ATTENDEE_CHECKEDINDATE, tstatus.scan_time__c);
            this.db.update(DBFeilds.TABLE_ATTENDEE_DETAILS, this.datavalues,"Attendee_Id = '" + tstatus.Ticket__c + "'" + " AND " + DBFeilds.USER_EVENT_ID + " = '" + event_id + "'",	null);
            InsertAndUpdateTSTATUS(tstatus,event_id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertandupdateAttendeeBadgeIdandPrintstatus(String TicketId,String printstatus) {
        try {
            this.datavalues = new ContentValues();
            this.datavalues.put(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS, printstatus);
            this.db.update(DBFeilds.TABLE_ATTENDEE_DETAILS, this.datavalues, "Attendee_Id = '" + TicketId + "'", null);
            //Log.i("DATABASE", "Badge Id Inserted Successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void insertandupdateAttendeeBadgeIdandBadgeParentID(ArrayList<OrderDetailsHandler.BadgeIDs> ticklist) {
        try {
            for (int i = 0; i < ticklist.size(); i++) {
                this.datavalues = new ContentValues();
                this.datavalues.put(DBFeilds.ATTENDEE_ID, Util.NullChecker(ticklist.get(i).Id));
                if(!Util.NullChecker(ticklist.get(i).badgeparentid__c).isEmpty()){
                    this.datavalues.put(DBFeilds.ATTENDEE_BADGE_PARENT_ID, Util.NullChecker(ticklist.get(i).badgeparentid__c));
                    if (!Util.NullChecker(ticklist.get(i).badgeparentid__r.Badge_ID__c).isEmpty()) {
                        this.datavalues.put(DBFeilds.ATTENDEE_BADGEID, ticklist.get(i).badgeparentid__r.Badge_ID__c);
                        //this.datavalues.put(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS, Util.NullChecker(ticklist.get(i).Badge_ID__r.Print_status__c));
                    }else {
                        this.datavalues.put(DBFeilds.ATTENDEE_BADGEID, Util.NullChecker(ticklist.get(i).Badge_ID__c));
                    }
                }else {
                    this.datavalues.put(DBFeilds.ATTENDEE_BADGEID, Util.NullChecker(ticklist.get(i).Badge_ID__c));
                }
                this.db.update(DBFeilds.TABLE_ATTENDEE_DETAILS, this.datavalues, "Attendee_Id = '" + ticklist.get(i).Id + "'", null);
                //this.datavalues.put(DBFeilds.ATTENDEE_REASON, Util.NullChecker(Reason));
                //this.datavalues.put(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS, Util.NullChecker(printstatus));
            }
            //Log.i("DATABASE", "Badge Id Inserted Successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*
    public void insertandupdateAttendeeBadgeIdandBadgeParentID(String BadgeParentid, String BadgeId, String Reason, String TicketId,String printstatus) {
        try {
            this.datavalues = new ContentValues();
            this.datavalues.put(DBFeilds.ATTENDEE_ID, Util.NullChecker(TicketId));
            this.datavalues.put(DBFeilds.ATTENDEE_BADGE_PARENT_ID, Util.NullChecker(BadgeParentid));
            this.datavalues.put(DBFeilds.ATTENDEE_BADGEID, Util.NullChecker(BadgeId));
            this.datavalues.put(DBFeilds.ATTENDEE_REASON, Util.NullChecker(Reason));
            this.datavalues.put(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS, Util.NullChecker(printstatus));
            this.db.update(DBFeilds.TABLE_ATTENDEE_DETAILS, this.datavalues, "Attendee_Id = '" + TicketId + "'", null);
            //Log.i("DATABASE", "Badge Id Inserted Successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/

    public void InsertandupdateBadgeIdforParentandChild(String badge_parentid, String BadgeId){
        try {
            Cursor c = db.rawQuery("select "+DBFeilds.ATTENDEE_ID +" from "+DBFeilds.TABLE_ATTENDEE_DETAILS+" where "+DBFeilds.ATTENDEE_BADGE_PARENT_ID+" = '"+badge_parentid+"' OR "
                    +DBFeilds.ATTENDEE_ID+" = '"+badge_parentid+"'", null);
            //if(c.getCount()>0){
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                this.datavalues = new ContentValues();
                this.datavalues.put(DBFeilds.ATTENDEE_BADGEID, BadgeId);
                this.db.update(DBFeilds.TABLE_ATTENDEE_DETAILS, this.datavalues, "Attendee_Id = '" + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ID)) + "'", null);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void insertandupdateAttendeeBadgeId(String BadgeLabel, String BadgeId, String Reason, String TicketId,String printstatus,String BadgeParentId) {
        try {
            this.datavalues = new ContentValues();
            this.datavalues.put(DBFeilds.ATTENDEE_ID, TicketId);
            if(!BadgeLabel.equalsIgnoreCase("fromorder")){
                this.datavalues.put(DBFeilds.ATTENDEE_BADGE_NAME, BadgeLabel);
            }
            this.datavalues.put(DBFeilds.ATTENDEE_BADGEID, BadgeId);
            this.datavalues.put(DBFeilds.ATTENDEE_REASON, Reason);
            this.datavalues.put(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS, printstatus);
            this.db.update(DBFeilds.TABLE_ATTENDEE_DETAILS, this.datavalues, "Attendee_Id = '" + TicketId + "'", null);
            if(!Util.NullChecker(BadgeParentId).trim().isEmpty()){
                Util.db.InsertandupdateBadgeIdforParentandChild(BadgeParentId,BadgeId);
            }
            //Log.i("DATABASE", "Badge Id Inserted Successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*
    public void InsertAndUpdateCheckNo(String checkno, String order_id,String payment_status) {
        try {
            this.datavalues = new ContentValues();
            //this.datavalues.put(DBFeilds.ORDER_CHECK_NUMBER, checkno);//TODO PAYMENTITEMS
            this.datavalues.put(DBFeilds.ORDER_ORDER_STATUS, payment_status);
            this.db.update(DBFeilds.TABLE_SALES_ORDER, this.datavalues, "Order_Id = '" + order_id + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/

    public void InsertAndUpdateCheckandpaymentdetails(String checkno, String order_id,String payment_status,String payment_id,String payment_notes,String orderStatus) {
        try {
            this.datavalues = new ContentValues();
            this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_REF_NUMBER, checkno);//TODO PAYMENTITEMS
            this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS, payment_status);
            this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_ID, payment_id);
            this.datavalues.put(DBFeilds.ORDER_PAYMENT_ITEM_NOTES, payment_notes);
            this.db.update(DBFeilds.TABLE_ORDER_PAYMENT_ITEMS, this.datavalues, "payment_order_id = '" + order_id + "'", null);
            ContentValues ordervalues=new ContentValues();
            ordervalues.put(DBFeilds.ORDER_ORDER_STATUS, orderStatus);
            this.db.update(DBFeilds.TABLE_SALES_ORDER, ordervalues, "Order_Id = '" + order_id + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void InsertAndUpdateRefresh(RefreshResponse refresh, String checked_in_eventId,boolean isResetSettings) {
        try {
            InsertAndUpdateEvent(refresh.BLN_ASC_EventsOUTPUT.Events,isResetSettings);
            InsertAndUpdateUserProfile(refresh.BLN_ASC_EventsOUTPUT.Profile);

            if (refresh.BLN_ASC_EventsOUTPUT.PayGateways != null && refresh.BLN_ASC_EventsOUTPUT.PayGateways.size() > 0) {
                deletePayGateway_Keys(ITransaction.EMPTY_STRING);
            }
            PaymentType payment_keys = new PaymentType();
            payment_keys.Paygateway_name__c = this.context.getString(R.string.eventdex_stripe_keys);
            payment_keys.PG_User_Key__c = refresh.BLN_ASC_EventsOUTPUT.StripeEventdexClientID;
            payment_keys.PG_Pass_Secret__c = refresh.BLN_ASC_EventsOUTPUT.StripeEventdexSecretKey;
            InsertAndUpdatePay_GAteway_Key(payment_keys);

            for (PaymentType pay_key : refresh.BLN_ASC_EventsOUTPUT.PayGateways) {
                InsertAndUpdatePay_GAteway_Key(pay_key);
            }
            if(isResetSettings&&refresh.itemlist!=null ) {
                for (int i = 0; i < refresh.itemlist.size(); i++) {
                    InsertandUpdateHideStatus(refresh.itemlist.get(i).Hide_Ticket_SA__c, refresh.itemlist.get(i).Id, BaseActivity.checkedin_event_record.Events.Id);
                }
            }
            //Util.db.InsertCountry(refresh.BLN_ASC_EventsOUTPUT.CountriesList);
            Util.db.InsertItemType(refresh.BLN_ASC_EventsOUTPUT.ItemTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Insert_Update_PGateways(PaymentGateWays paygateway) {
        try {
            //Log.i("------------- Inserted Payment Settings From DB Names------------", ":" + paygateway.Name);
            this.datavalues = new ContentValues();
            if (!paygateway.Name.equalsIgnoreCase("CreditCard")) {
                this.datavalues.put(PaymentTypeDBFeilds.PAYMENT_TYPE_ID, paygateway.Id);
                this.datavalues.put(PaymentTypeDBFeilds.PAYMENT_TYPE_NAME, paygateway.Name);
                if (paygateway.Display_Type__c.isEmpty()) {
                    this.datavalues.put(PaymentTypeDBFeilds.PAYMENT_TYPE_DISPLAY, "Checkbox");
                } else {
                    this.datavalues.put(PaymentTypeDBFeilds.PAYMENT_TYPE_DISPLAY, paygateway.Display_Type__c);
                }
                this.datavalues.put(PaymentTypeDBFeilds.PAYMENT_ADOPTIVE_TYPE, paygateway.Adaptive_Type__c);
                this.datavalues.put(PaymentTypeDBFeilds.PAYMENT_ONLINE_FLAG, paygateway.Online_Flag__c);
                this.datavalues.put(PaymentTypeDBFeilds.PAYMENT_CURRENCY_ID, paygateway.BLN_Currency__c);
                this.datavalues.put(PaymentTypeDBFeilds.PAYMENT_GATEWAY_TYPE_ID, paygateway.BLN_PGateway_Type__c);

                if (isRecordExists(DBFeilds.TABLE_PAYMENT_GATEWAYS, " where payment_type_id='" + paygateway.Id + "' and "+PaymentTypeDBFeilds.PAYMENT_CURRENCY_ID+"='"+paygateway.BLN_Currency__c+"'")) {
                    this.db.update(DBFeilds.TABLE_PAYMENT_GATEWAYS, this.datavalues, "payment_type_id='" + paygateway.Id + "'", null);
                } else {
                    this.db.insert(DBFeilds.TABLE_PAYMENT_GATEWAYS, null, this.datavalues);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void InsertAndUpdateEventPaymentSetting(EventPaymentTypes _setting) {
        try {
            this.datavalues = new ContentValues();
            this.datavalues.put(EventPayamentSettings.EVENT_ID, _setting.Events__c);
            this.datavalues.put(EventPayamentSettings.EVENT_PAY_GATEWAY__C, _setting.Pay_Gateway__c);
            this.datavalues.put(EventPayamentSettings.EVENT_PAY__ID, _setting.Id);
            this.datavalues.put(EventPayamentSettings.EVENT_PGATEWAY_ID, _setting.Pay_Gateway__r.PGateway_Type__c);
            if(!Util.NullChecker(_setting.Pay_Gateway__r.Paygateway_Label__c).isEmpty()) {
                this.datavalues.put(EventPayamentSettings.EVENT_PAYGATEWAY_LABEL_NAME, _setting.Pay_Gateway__r.Paygateway_Label__c);
            }
            this.datavalues.put(EventPayamentSettings.EVENT_PAY_NAME, getPGateway_Name(_setting.Pay_Gateway__r.PGateway_Type__c));
            this.datavalues.put(EventPayamentSettings.EVENT_PAY_ADOPTIVE_TYPE, getPayment_Adoptive_Type_Name(_setting.Pay_Gateway__r.PGateway_Type__c));
            this.datavalues.put(EventPayamentSettings.EVENT_REGISTRATION_TYPE, _setting.Registration_Type__c);

            if (isRecordExists(DBFeilds.TABLE_EVENT_PAYAMENT_SETTINGS, " where event_id = '" + _setting.Events__c + "' AND " + EventPayamentSettings.EVENT_PAY__ID + " = '" + _setting.Id + "'")) {
                this.db.update(DBFeilds.TABLE_EVENT_PAYAMENT_SETTINGS, this.datavalues, "event_id =  '" + _setting.Events__c + "' AND " + EventPayamentSettings.EVENT_PAY__ID + " = '" + _setting.Id + "'", null);
                //Log.i("DATABASE CLASS", "%--------------------Event setting data updated successfully---------------------% of " + _setting.Events__c);

            }else{
                this.db.insert(DBFeilds.TABLE_EVENT_PAYAMENT_SETTINGS, null, this.datavalues);
            }

            //Log.i("DATABASE CLASS", "%--------------------Event setting data inserted successfully---------------------% of " + _setting.Events__c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPGateway_Name(String _pay_type_id) {
        String name = ITransaction.EMPTY_STRING;
        try {
            //Log.i("DATABASE CLASS", "%-------Payment Type ID-------%" + _pay_type_id);
            Cursor c = this.db.rawQuery("Select * from PaymentGateways where payment_type_id = '" + _pay_type_id + "'", null);
            if (c.moveToFirst()) {
                name = c.getString(c.getColumnIndex(PaymentTypeDBFeilds.PAYMENT_TYPE_NAME));
            }
            c.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public void InsertAndUpdatePay_GAteway_Key(PaymentType pay_gateway) {
        try {
            //Log.i("DATABASE CLASS", "---------Pay Gateway Key Updated Successfully------------" + pay_gateway.PGateway_Type__c);
            this.datavalues = new ContentValues();
            this.datavalues.put(EventPayamentSettings.KEY_PAY_GATEWAY_ID, pay_gateway.Id);
            this.datavalues.put(EventPayamentSettings.KEY_PGATEWAY_TYPE_ID, pay_gateway.PGateway_Type__c);
            this.datavalues.put(EventPayamentSettings.KEY_GN_USER, pay_gateway.BLN_GN_User__c);
            this.datavalues.put(EventPayamentSettings.KEY_COMPANY, pay_gateway.Company__c);
            this.datavalues.put(EventPayamentSettings.KEY_PAYGATEWAY_NAME, pay_gateway.Paygateway_name__c);
            this.datavalues.put(EventPayamentSettings.KEY_PAYGATEWAY_LABEL_NAME, pay_gateway.Paygateway_Label__c);
            this.datavalues.put(EventPayamentSettings.KEY_PG_EMAIL_ID, pay_gateway.PG_Email_Id__c);
            this.datavalues.put(EventPayamentSettings.KEY_PG_PASS_SECRET, pay_gateway.PG_Pass_Secret__c);
            this.datavalues.put(EventPayamentSettings.KEY_PG_SIGNATURE, pay_gateway.PG_Signature__c);
            this.datavalues.put(EventPayamentSettings.KEY_PG_USER_KEY, pay_gateway.PG_User_Key__c);
            this.datavalues.put(EventPayamentSettings.KEY_PP_FEE_PAYER, pay_gateway.PP_Fee_Payer__c);
            this.datavalues.put(EventPayamentSettings.KEY_PP_PAYMENT_TYPE, pay_gateway.PP_Payment_Type__c);
            this.datavalues.put(EventPayamentSettings.KEY_SERVICE_FEE, pay_gateway.Service_Fee__c);
            this.datavalues.put(EventPayamentSettings.KEY_CITRUS_PARAM__C, pay_gateway.citrus_param__c);
            if (isRecordExists(DBFeilds.TABLE_PAY_GATEWAY_KEYS, " Where Pay_Gateway_Id__c = '" + pay_gateway.Id + "' AND " + EventPayamentSettings.KEY_PGATEWAY_TYPE_ID + " = '" + pay_gateway.PGateway_Type__c + "'")) {
                this.db.update(DBFeilds.TABLE_PAY_GATEWAY_KEYS, this.datavalues, "Pay_Gateway_Id__c = '" + pay_gateway.Id + "' AND " + EventPayamentSettings.KEY_PGATEWAY_TYPE_ID + " = '" + pay_gateway.PGateway_Type__c + "'", null);
                //Log.i("DATABASE CLASS", "---------Pay Gateway Key Updated Successfully------------");
                return;
            }
            this.db.insert(DBFeilds.TABLE_PAY_GATEWAY_KEYS, null, this.datavalues);
            //Log.i("DATABASE CLASS", "---------Pay Gateway Key Inserted Successfully-----------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getPaymentgatewayLabel(String id){
        Cursor c = this.db.rawQuery("select PayGateway_label__c from PaymentSettings " + " where event_id = '" + BaseActivity.checkedin_event_record.Events.Id + "' AND "+ EventPayamentSettings.EVENT_PGATEWAY_ID  + " = '" + id + "'", null);
        String label="";
        if(c.moveToFirst()){
            label=c.getString(c.getColumnIndex(EventPayamentSettings.EVENT_PAYGATEWAY_LABEL_NAME));
        }
        c.close();
        return label;
    }
    public List<PaymentGateWays> getPGateways(String whereclause) {
        try {
            List<PaymentGateWays> pgateways_list = new ArrayList();
            Cursor c = this.db.rawQuery("select * from PaymentGateways " + whereclause, null);
            if (c == null) {
                return pgateways_list;
            }
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                PaymentGateWays pgateway = new PaymentGateWays();
                pgateway.Id = c.getString(c.getColumnIndex(PaymentTypeDBFeilds.PAYMENT_TYPE_ID));
                pgateway.Name = c.getString(c.getColumnIndex(PaymentTypeDBFeilds.PAYMENT_TYPE_NAME));
                pgateway.Display_Type__c = c.getString(c.getColumnIndex(PaymentTypeDBFeilds.PAYMENT_TYPE_DISPLAY));
                pgateway.Adaptive_Type__c = c.getString(c.getColumnIndex(PaymentTypeDBFeilds.PAYMENT_ADOPTIVE_TYPE));
                //Log.i("-------------Payment Settings From DB Names------------", ":" + pgateway.Name);
                pgateways_list.add(pgateway);
            }
            c.close();
            return pgateways_list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<PaymentType> getPay_Gateway_Key(String Where) {
        List<PaymentType> _pay_gateway_keys = new ArrayList();
        try {
            Cursor c = this.db.rawQuery("SELECT distinct * FROM Pay_Gateway_Keys" + Where, null);
            //Log.i("--------------------get Keys Query---------------", ":" + "SELECT * FROM Pay_Gateway_Keys" + Where);
            if (c != null) {
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    PaymentType pay_keys = new PaymentType();
                    pay_keys.Id = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_PAY_GATEWAY_ID));
                    pay_keys.PGateway_Type__c = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_PGATEWAY_TYPE_ID));
                    pay_keys.Company__c = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_COMPANY));
                    pay_keys.BLN_GN_User__c = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_GN_USER));
                    pay_keys.Paygateway_name__c = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_PAYGATEWAY_NAME));
                    pay_keys.Paygateway_Label__c = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_PAYGATEWAY_LABEL_NAME));
                    pay_keys.PG_Email_Id__c = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_PG_EMAIL_ID));
                    pay_keys.PG_Pass_Secret__c = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_PG_PASS_SECRET));
                    pay_keys.PG_Signature__c = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_PG_SIGNATURE));
                    pay_keys.PG_User_Key__c = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_PG_USER_KEY));
                    pay_keys.PP_Fee_Payer__c = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_PP_FEE_PAYER));
                    pay_keys.PP_Payment_Type__c = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_PP_PAYMENT_TYPE));
                    pay_keys.Service_Fee__c = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_SERVICE_FEE));
                    pay_keys.Name = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_PAYGATEWAY_NAME));
                    pay_keys.citrus_param__c = c.getString(c.getColumnIndex(EventPayamentSettings.KEY_CITRUS_PARAM__C));
                    _pay_gateway_keys.add(pay_keys);
                }
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return _pay_gateway_keys;
    }

    public void deletePayGateway_Keys(String Whereclause) {
        this.db.delete(new StringBuilder(DBFeilds.TABLE_PAY_GATEWAY_KEYS).append(Whereclause).toString(), null, null);
    }

    public int delete_Event_PayGateway_from_PGateway(String where) {
        try {
            return this.db.delete(new StringBuilder(DBFeilds.TABLE_EVENT_PAYAMENT_SETTINGS).append(where).toString(), null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    public void InsertAndUpdateAttendeeListValues(ArrayList<MultiListValues> listvalues) {
        try {
            this.db.beginTransaction();
           /* Iterator it = listvalues.iterator();
            while (it.hasNext()) {*/
            //MultiListValues.BLN_Tkt_List BLN_Tkt_List_Selected__r = (MultiListValues.BLN_Tkt_List) it.next();
            for (MultiListValues.Records records : listvalues.get(0).BLN_Tkt_List_Selected__r.records) {
                //this.datavalues.put(DBFeilds.ATTENDEE_ID, Util.NullChecker(records.BLN_TKT_profile__c));
                if(records.BLN_ListLookUp__r.List_Type__c.equalsIgnoreCase(Util.ATTENDEE_ETHNICITY)||
                        records.BLN_ListLookUp__r.List_Type__c.equalsIgnoreCase(Util.ATTENDEE_GEOGRAPHICAL_REGION)||
                        records.BLN_ListLookUp__r.List_Type__c.equalsIgnoreCase(Util.ATTENDEE_BUSINESS_STRUCTURE)||
                        records.BLN_ListLookUp__r.List_Type__c.equalsIgnoreCase(Util.ATTENDEE_NUMBER_OF_EMPLOYEES)) {
                    this.datavalues = new ContentValues();
                    this.datavalues.put(DBFeilds.ATTENDEE_ID, Util.NullChecker(records.BLN_TKT_profile__c));
                    if (records.BLN_ListLookUp__r.List_Type__c.equalsIgnoreCase(Util.ATTENDEE_ETHNICITY))
                        this.datavalues.put(DBFeilds.ATTENDEE_ETHNICITY, Util.NullChecker(records.BLN_ListLookUp__r.List_Description__c));
                    else if (records.BLN_ListLookUp__r.List_Type__c.equalsIgnoreCase(Util.ATTENDEE_GEOGRAPHICAL_REGION))
                        this.datavalues.put(DBFeilds.ATTENDEE_GEOGRAPHICAL_REGION, Util.NullChecker(records.BLN_ListLookUp__r.List_Description__c));
                    else if (records.BLN_ListLookUp__r.List_Type__c.equalsIgnoreCase(Util.ATTENDEE_BUSINESS_STRUCTURE))
                        this.datavalues.put(DBFeilds.ATTENDEE_BUSINESS_STRUCTURE, Util.NullChecker(records.BLN_ListLookUp__r.List_Description__c));
                    else if (records.BLN_ListLookUp__r.List_Type__c.equalsIgnoreCase(Util.ATTENDEE_NUMBER_OF_EMPLOYEES))
                        this.datavalues.put(DBFeilds.ATTENDEE_NUMBER_OF_EMPLOYEES, Util.NullChecker(records.BLN_ListLookUp__r.List_Description__c));

                    if (isUserExists(DBFeilds.TABLE_ATTENDEE_DETAILS, " where Attendee_Id = '" + records.BLN_TKT_profile__c + "'" + " AND " + DBFeilds.USER_EVENT_ID + " = '" + BaseActivity.checkedin_event_record.Events.Id + "'")) {
                        this.db.update(DBFeilds.TABLE_ATTENDEE_DETAILS, this.datavalues, "Attendee_Id = '" + records.BLN_TKT_profile__c  + "'" + " AND " + DBFeilds.USER_EVENT_ID + " = '" + BaseActivity.checkedin_event_record.Events.Id + "'", null);
                    } else {
                        this.db.insert(DBFeilds.TABLE_ATTENDEE_DETAILS, null, this.datavalues);
                    }
                }else{
                }
                   /* this.datavalues.put(DBFeilds.ATTENDEE_LIST_CODE, Util.NullChecker(records.BLN_ListLookUp__r.List_Code__c));
                    this.datavalues.put(DBFeilds.ATTENDEE_LIST_TYPE, Util.NullChecker(records.BLN_ListLookUp__r.List_Type__c));
                    this.datavalues.put(DBFeilds.ATTENDEE_LIST_DESCRIPTION, Util.NullChecker(records.BLN_ListLookUp__r.List_Description__c));*/

            }

            this.db.setTransactionSuccessful();
            this.db.endTransaction();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    public void InsertAndUpdateAttendeeAddress(ArrayList<AttendeeAddressHandler> addresses) {
        try {
            Iterator it = addresses.iterator();
            while (it.hasNext()) {
                AttendeeAddressHandler attendee_address_haHandler = (AttendeeAddressHandler) it.next();
                for (AttendeeAddressListObject attendee_address : attendee_address_haHandler.Company_Ext__r.records) {
                    this.datavalues = new ContentValues();
                    this.datavalues.put(AddressDBFeilds.ADDRESS_1, attendee_address.Business_Address1__c);
                    this.datavalues.put(AddressDBFeilds.ADDRESS_2, attendee_address.Business_Address2__c);
                    this.datavalues.put(AddressDBFeilds.ADDRESS_TYPE, "Home");
                    this.datavalues.put(AddressDBFeilds.CITY, attendee_address.Business_City__c);
                    this.datavalues.put(AddressDBFeilds.STATE, attendee_address.BLN_Business_State__c);
                    this.datavalues.put(AddressDBFeilds.COUNTRY, attendee_address.BLN_Business_Country__c);
                    this.datavalues.put(AddressDBFeilds.COMPANY_NAME, attendee_address_haHandler.Name);
                    this.datavalues.put(AddressDBFeilds.COMPANY_ID, attendee_address.Company__c);
                    this.datavalues.put(AddressDBFeilds.ZIPCODE, attendee_address.Business_Zipcode__c);
                    this.datavalues.put(AddressDBFeilds.WORK_PHONE, attendee_address.Duns_Number__c);
                    if (isRecordExists(DBFeilds.TABLE_ADDRESS, " where company_id = '" + attendee_address.Company__c + "'")) {
                        this.db.update(DBFeilds.TABLE_ADDRESS, this.datavalues, "company_id = '" + attendee_address.Company__c + "'", null);
                        //Log.i("DATABASE CLASS", "---------Attendee Address Updated Successfully");
                    } else {
                        this.db.insert(DBFeilds.TABLE_ADDRESS, null, this.datavalues);
                        //Log.i("DATABASE CLASS", "---------Attendee Address Inserted Successfully");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cursor getAttendeeAddress(String whereClause) {
        Cursor c = null;
        try {
            c = this.db.rawQuery("Select * from UserAddress" + whereClause, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public void InsertAndUpdateTicketTag(ArrayList<TicketTagHandler> tagHandler) {
        try {
            Iterator it = tagHandler.iterator();
            while (it.hasNext()) {
                TicketTagHandler tag = (TicketTagHandler) it.next();
                this.datavalues = new ContentValues();
                this.datavalues.put(DBFeilds.TAG_ID, tag.Id);
                this.datavalues.put(EventPayamentSettings.EVENT_ID, tag.Event__c);
                this.datavalues.put(Util.TICKET_ID, tag.RowId__c);
                this.datavalues.put(DBFeilds.TAG_TICKET_NAME, tag.Table_Name__c);
                this.datavalues.put(DBFeilds.TAG_NAME, tag.Tag_Text__c);
                if (isRecordExists(DBFeilds.TABLE_TICKET_TAG, " where event_id = '" + tag.Event__c + "' AND " + Util.TICKET_ID + " = '" + tag.RowId__c + "' AND " + DBFeilds.TAG_ID + " = '" + tag.Id + "'")) {
                    this.db.update(DBFeilds.TABLE_TICKET_TAG, this.datavalues, "event_id = '" + tag.Event__c + "' AND " + Util.TICKET_ID + " = '" + tag.RowId__c + "' AND " + DBFeilds.TAG_ID + " = '" + tag.Id + "'", null);
                    //Log.i("DATABASE CLASS", "=========Tags are Successfully Updated========");
                } else {
                    this.db.insert(DBFeilds.TABLE_TICKET_TAG, null, this.datavalues);
                    //Log.i("DATABASE CLASS", "=========Tags are Successfully Inserted========");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cursor getSessionAttendeeCursor(String where_caluse,String where_clause2){
        String query = "select _id AS _id,session_attendee_first_name AS session_attendee_first_name,session_attendee_last_name AS session_attendee_last_name,session_item_pool_id AS session_item_pool_id , session_attendee_ticket_id, session_attendee_email_id, session_attendee_company AS session_attendee_company, session_attendee_checkin_status AS session_attendee_checkin_status, session_attendee_scantime AS session_attendee_scantime,session_event_id AS session_event_id,session_attendee_id AS session_attendee_id,session_group_id AS session_group_id FROM SessionAttendees "+where_caluse
                +" UNION select _id, badge_id AS session_attendee_first_name,offline_attendee_name AS session_attendee_last_name,item_pool_id AS session_item_pool_id, badge_id, user_id AS session_attendee_email_id,badge_status AS session_attendee_company,checkin_status AS session_attendee_checkin_status,scan_time AS session_attendee_scantime,event_id AS session_event_id,error AS session_attendee_id,session_group_id AS session_group_id FROM offline_scan "+where_clause2;
        //Log.i("-----------print Query--------------",":"+query);
        Cursor c = db.rawQuery(query, null);
        return c;
    }


    public Cursor getSessionAttendeeCursor(String where_clause){
        String query=" SELECT * FROM "+DBFeilds.TABLE_SESSION_ATTENDEES+" "+where_clause;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        return c;
    }


    public int getSessionAttendeeCount(String where_clause){
        int count = 0;
        try {
            Cursor c = db.rawQuery("select count(*) from "+DBFeilds.TABLE_SESSION_ATTENDEES+" "+where_clause, null);
            if(c.moveToFirst()){
                count =  c.getInt(0);
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return count;
    }
    public boolean deleteTable(String tableName) {
        try {
            this.db.delete(tableName, null, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteTicketTags(TicketTagHandler tagHandler) {
        try {
            this.db.delete(DBFeilds.TABLE_TICKET_TAG, "ticket_id = '" + tagHandler.RowId__c + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void InsertAndUpdateOrganizerPayInfo(Organaizer organaizer) {
        //Log.i("---------Organizer Id And Name------------", ":" + organaizer.Id + " : " + organaizer.Name + " : " + organaizer.PG_Authorization_Key__c);
        ContentValues values = new ContentValues();
        values.put(DBFeilds.ORGANAIZER_ID, organaizer.Id);
        values.put(DBFeilds.ORGANAIZER_NAME, organaizer.Name);
        values.put(DBFeilds.ORGANAIZER_KEY, organaizer.PG_Authorization_Key__c);
        values.put(DBFeilds.ORGANAIZER_CARD_TYPE, organaizer.PG_Authorization_Card_Type__c);
        values.put(DBFeilds.ORGANAIZER_CARD_DIGITS, organaizer.PG_Authorization_CC_Last_four_Digit__c);
        values.put(DBFeilds.ORGANAIZER_CARD_OVERWRITE, String.valueOf(organaizer.PG_Credit_Card_Overwrite__c));
        if (isRecordExists(DBFeilds.TABLE_ORGANAIZER_INFO, " where organizer_id = '" + organaizer.Id + "'")) {
            this.db.update(DBFeilds.TABLE_ORGANAIZER_INFO, values, "organizer_id = '" + organaizer.Id + "'", null);
        } else {
            this.db.insert(DBFeilds.TABLE_ORGANAIZER_INFO, null, values);
        }
    }

    public Organaizer getEventOrganaizerPayInfo(String organaizer_id) {
        Event event = new Event();
        Organaizer organaizer = event.new Organaizer();
        Cursor c = this.db.rawQuery("select * from Organaizer_Info where organizer_id='" + organaizer_id + "'", null);
        //Log.i("--------------Cursor Size-------------", ":" + c.getCount());
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            organaizer.Id = c.getString(c.getColumnIndex(DBFeilds.ORGANAIZER_ID));
            organaizer.Name = c.getString(c.getColumnIndex(DBFeilds.ORGANAIZER_NAME));
            organaizer.PG_Authorization_Key__c = c.getString(c.getColumnIndex(DBFeilds.ORGANAIZER_KEY));
            organaizer.PG_Authorization_CC_Last_four_Digit__c = c.getString(c.getColumnIndex(DBFeilds.ORGANAIZER_CARD_DIGITS));
            organaizer.PG_Credit_Card_Overwrite__c = Boolean.parseBoolean(c.getString(c.getColumnIndex(DBFeilds.ORGANAIZER_CARD_OVERWRITE)));
            organaizer.PG_Authorization_Card_Type__c = c.getString(c.getColumnIndex(DBFeilds.ORGANAIZER_CARD_TYPE));
        }
        c.close();
        return organaizer;
    }

    public Cursor getTicketTag(String ticket_id) {
        Cursor c = null;
        try {
            c = this.db.rawQuery("Select * from ticket_tag where ticket_id = '" + ticket_id + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public boolean deleteAttendeeInfo(String eventId, String attendeeId) {
        try {
            this.db.delete(DBFeilds.TABLE_ATTENDEE_DETAILS, "Attendee_Id = '" + attendeeId + "' AND " + DBFeilds.USER_EVENT_ID + " = '" + eventId + "'", null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteBadges(String event_id) {
        this.db.delete(DBFeilds.TABLE_BADGE_TEMPLATE_NEW, "event_id='" + event_id + "'", null);
        this.db.delete(DBFeilds.TABLE_BADGE_LAYERS, "event_id='" + event_id + "'", null);
    }

    public void InsertAndUpdateBadgeTemplateNew(BadgeResponseNew badge_response) {
        ContentValues values = new ContentValues();
        values.put(DBFeilds.BADGE_NEW_ID, badge_response.badge.Id);
        values.put(DBFeilds.BADGE_NEW_NAME, badge_response.badge.Name);
        values.put(DBFeilds.BADGE_NEW_DESC, badge_response.badge.Description__c);
        values.put(EventPayamentSettings.EVENT_ID, badge_response.badge.event_id);
        values.put(DBFeilds.BADGE_NEW_MODULE, badge_response.badge.Module__c);
        values.put(DBFeilds.BADGE_NEW_DATA, badge_response.badge.Data__c);
        values.put(DBFeilds.BADGE_NEW_IS_SELECTED, Boolean.valueOf(badge_response.badge.is_selected));
        values.put(DBFeilds.BADGE_NEW_DEFAULT_BADGE, badge_response.badge.ScanAttendeeDefaultBadge__c);
        if (isRecordExists(DBFeilds.TABLE_BADGE_TEMPLATE_NEW, " where badge_id = '" + badge_response.badge.Id + "'")) {
            this.db.update(DBFeilds.TABLE_BADGE_TEMPLATE_NEW, values, "badge_id = '" + badge_response.badge.Id + "'", null);
        } else {
            this.db.insert(DBFeilds.TABLE_BADGE_TEMPLATE_NEW, null, values);
        }
        InsertAndUpdateBadgeLayers(badge_response.layers, badge_response.badge.Id, badge_response.badge.event_id);
    }

    public void InsertAndUpdateBadgeLayers(ArrayList<BadgeLayerNew> layers, String badge_id, String event_id) {
        Iterator it = layers.iterator();
        while (it.hasNext()) {
            BadgeLayerNew layer = (BadgeLayerNew) it.next();
            ContentValues values = new ContentValues();
            values.put(DBFeilds.LAYER_ID, layer.Id);
            values.put(DBFeilds.LAYER_DATA, layer.Data__c);
            values.put(DBFeilds.BADGE_NEW_ID, badge_id);
            values.put(EventPayamentSettings.EVENT_ID, event_id);
            if (isRecordExists(DBFeilds.TABLE_BADGE_LAYERS, " where layer_id='" + layer.Id + "' AND " + DBFeilds.BADGE_NEW_ID + "='" + badge_id + "'")) {
                this.db.update(DBFeilds.TABLE_BADGE_LAYERS, values, "layer_id='" + layer.Id + "' AND " + DBFeilds.BADGE_NEW_ID + "='" + badge_id + "'", null);
            } else {
                this.db.insert(DBFeilds.TABLE_BADGE_LAYERS, null, values);
            }
        }
    }
    public ArrayList<BadgeResponseNew> getBadgeSelectedElseifDefaultbadge(){
        ArrayList<BadgeResponseNew> badges = new ArrayList<BadgeResponseNew>();
        String where_att = " Where " + DBFeilds.BADGE_NEW_EVENT_ID + " = '"
                + BaseActivity.checkedin_event_record.Events.Id + "' AND " + DBFeilds.BADGE_NEW_ID
                + " = '" + BaseActivity.checkedin_event_record.Events.Mobile_Default_Badge__c + "'";
        Cursor c = this.db.rawQuery("select * from BadgeTemplateNew"+where_att , null);
        if (c != null) {
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                BadgeResponseNew badge_parent = new BadgeResponseNew();
                badge_parent.badge.Data__c = c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_DATA));
                badge_parent.badge.Id = c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_ID));
                badge_parent.badge.Name = c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_NAME));
                badge_parent.badge.Module__c = c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_MODULE));
                badge_parent.badge.Description__c = c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_DESC));
                badge_parent.badge.event_id = c.getString(c.getColumnIndex(EventPayamentSettings.EVENT_ID));
                badge_parent.badge.is_selected = Boolean.parseBoolean(c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_IS_SELECTED)));
                badge_parent.badge.ScanAttendeeDefaultBadge__c = c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_DEFAULT_BADGE));
                Cursor layer_cursor = this.db.rawQuery("select * from BadgeLayers where badge_id = '" + badge_parent.badge.Id + "'", null);
                if (layer_cursor != null) {
                    for (int j = 0; j < layer_cursor.getCount(); j++) {
                        layer_cursor.moveToPosition(j);
                        BadgeLayerNew layer = new BadgeLayerNew();
                        layer.Id = layer_cursor.getString(layer_cursor.getColumnIndex(DBFeilds.LAYER_ID));
                        layer.Data__c = layer_cursor.getString(layer_cursor.getColumnIndex(DBFeilds.LAYER_DATA));
                        badge_parent.layers.add(layer);
                    }
                    layer_cursor.close();
                }
                badges.add(badge_parent);
            }
        }
        return badges;
    }
    public ArrayList<BadgeResponseNew> getAllBadges(String where) {
        ArrayList<BadgeResponseNew> badges = new ArrayList<BadgeResponseNew>();
        Cursor c = this.db.rawQuery("select * from BadgeTemplateNew" + where, null);
        if (c != null) {
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                BadgeResponseNew badge_parent = new BadgeResponseNew();
                badge_parent.badge.Data__c = c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_DATA));
                badge_parent.badge.Id = c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_ID));
                badge_parent.badge.Name = c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_NAME));
                badge_parent.badge.Module__c = c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_MODULE));
                badge_parent.badge.Description__c = c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_DESC));
                badge_parent.badge.event_id = c.getString(c.getColumnIndex(EventPayamentSettings.EVENT_ID));
                badge_parent.badge.is_selected = Boolean.parseBoolean(c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_IS_SELECTED)));
                badge_parent.badge.ScanAttendeeDefaultBadge__c = c.getString(c.getColumnIndex(DBFeilds.BADGE_NEW_DEFAULT_BADGE));
                Cursor layer_cursor = this.db.rawQuery("select * from BadgeLayers where badge_id = '" + badge_parent.badge.Id + "'", null);
                if (layer_cursor != null) {
                    for (int j = 0; j < layer_cursor.getCount(); j++) {
                        layer_cursor.moveToPosition(j);
                        BadgeLayerNew layer = new BadgeLayerNew();
                        layer.Id = layer_cursor.getString(layer_cursor.getColumnIndex(DBFeilds.LAYER_ID));
                        layer.Data__c = layer_cursor.getString(layer_cursor.getColumnIndex(DBFeilds.LAYER_DATA));
                        badge_parent.layers.add(layer);
                    }
                    layer_cursor.close();
                }
                badges.add(badge_parent);
            }
            c.close();
        }
        return badges;
    }


    public void InsertAndUpdateScannedItems(List<ScannedItems> scanned_items,String event_id){
        for(ScannedItems scanned_item : scanned_items){
            ContentValues values = new ContentValues();
            values.put(DBFeilds.SCANNED_ID, scanned_item.Id);
            values.put(DBFeilds.SCANNED_EVENT_ID, event_id);
            values.put(DBFeilds.SCANNED_GNUSER_ID, scanned_item.BLN_GN_User__c);
            values.put(DBFeilds.SCANNED_ITEM_POOL_ID, scanned_item.BLN_Item_Pool__c);
            values.put(DBFeilds.SCANNED_STATUS, scanned_item.Status__c);
            values.put(DBFeilds.SCANNED_SWITCH , String.valueOf(scanned_item.DefaultValue__c));
            values.put(DBFeilds.SCANNED_GROUP_ID, scanned_item.BLN_Group__c);
            boolean isExists = isRecordExists(DBFeilds.TABLE_SCANNED_TICKETS, " where "+DBFeilds.SCANNED_ID+"='"+scanned_item.Id+"' AND "+DBFeilds.SCANNED_EVENT_ID+"='"+scanned_item.BLN_Event__c+"' AND "+DBFeilds.SCANNED_GROUP_ID+" = '"+scanned_item.BLN_Group__c+"'");
            if(isExists){
                db.update(DBFeilds.TABLE_SCANNED_TICKETS, values, DBFeilds.SCANNED_ID+"='"+scanned_item.Id+"' AND "+DBFeilds.SCANNED_EVENT_ID+"='"+scanned_item.BLN_Event__c+"' AND "+DBFeilds.SCANNED_GROUP_ID+" = '"+scanned_item.BLN_Group__c+"'", null);
            }else{
                db.insert(DBFeilds.TABLE_SCANNED_TICKETS, null, values);
            }
        }
    }
    public void UpdateScanItemSwitch1(ScannedItems item){
        ContentValues values = new ContentValues();
        values.put(DBFeilds.SCANNED_ID , item.Id);
        values.put(DBFeilds.SCANNED_EVENT_ID , item.BLN_Event__c);
        values.put(DBFeilds.SCANNED_GNUSER_ID , item.BLN_GN_User__c);
        values.put(DBFeilds.SCANNED_ITEM_POOL_ID , item.BLN_Item_Pool__c);
        values.put(DBFeilds.SCANNED_STATUS , item.Status__c);
        values.put(DBFeilds.SCANNED_SWITCH , String.valueOf(item.DefaultValue__c));
        values.put(DBFeilds.SCANNED_GROUP_ID, item.BLN_Group__c);
        db.update(DBFeilds.TABLE_SCANNED_TICKETS, values, DBFeilds.SCANNED_ID+"='"+item.Id+"' AND "+DBFeilds.SCANNED_EVENT_ID+" = '"+item.BLN_Event__c+"'", null);
    }

    public List<ScannedItems> getScannedItems(String Event_Id){
        // String Group_Id = getSwitchedONGroupId(BaseActivity.checkedin_event_record.Events.Id);
        Cursor c = db.rawQuery("select * from "+DBFeilds.TABLE_SCANNED_TICKETS+" where "+DBFeilds.SCANNED_EVENT_ID+"='"+Event_Id+"'", null);
        List<ScannedItems> scanned_items = new ArrayList<ScannedItems>();
        if(c != null){
            for(int i=0;i<c.getCount();i++){
                c.moveToPosition(i);
                ScannedItems item = new ScannedItems();
                item.Id = c.getString(c.getColumnIndex(DBFeilds.SCANNED_ID));
                item.BLN_Event__c = c.getString(c.getColumnIndex(DBFeilds.SCANNED_EVENT_ID));
                item.BLN_GN_User__c = c.getString(c.getColumnIndex(DBFeilds.SCANNED_GNUSER_ID));
                item.BLN_Item_Pool__c = c.getString(c.getColumnIndex(DBFeilds.SCANNED_ITEM_POOL_ID));
                item.Status__c = c.getString(c.getColumnIndex(DBFeilds.SCANNED_STATUS));
                item.DefaultValue__c = Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.SCANNED_SWITCH)));
                item.BLN_Group__c = c.getString(c.getColumnIndex(DBFeilds.SCANNED_GROUP_ID));
                scanned_items.add(item);
            }
            c.close();
        }
        return scanned_items;
    }

    public List<ScannedItems> getScannedItemsGroup(String Event_Id,String Group_Id ){

        Cursor c = db.rawQuery("select * from "+DBFeilds.TABLE_SCANNED_TICKETS+" where "+DBFeilds.SCANNED_EVENT_ID+"='"+Event_Id+"' AND "+DBFeilds.SCANNED_GROUP_ID+" = '"+Group_Id+"'", null);
        List<ScannedItems> scanned_items = new ArrayList<ScannedItems>();
        if(c != null){
            for(int i=0;i<c.getCount();i++){
                c.moveToPosition(i);
                ScannedItems item = new ScannedItems();
                item.Id = c.getString(c.getColumnIndex(DBFeilds.SCANNED_ID));
                item.BLN_Event__c = c.getString(c.getColumnIndex(DBFeilds.SCANNED_EVENT_ID));
                item.BLN_GN_User__c = c.getString(c.getColumnIndex(DBFeilds.SCANNED_GNUSER_ID));
                item.BLN_Item_Pool__c = c.getString(c.getColumnIndex(DBFeilds.SCANNED_ITEM_POOL_ID));
                item.Status__c = c.getString(c.getColumnIndex(DBFeilds.SCANNED_STATUS));
                item.DefaultValue__c = Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.SCANNED_SWITCH)));
                item.BLN_Group__c = c.getString(c.getColumnIndex(DBFeilds.SCANNED_GROUP_ID));
                scanned_items.add(item);
            }
            c.close();
        }
        return scanned_items;
    }
    public int getGroupCount(String event_id){
        int group_count = 0;
        try {
            Cursor c = db.rawQuery("Select count(*) from "+DBFeilds.TABLE_SCANNEDTICKETS_GROUP+" where "+DBFeilds.GROUP_EVENT_ID+" = '"+event_id+"'", null);
            if(c.moveToFirst()){
                group_count = c.getInt(0);
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return group_count;
    }
    public List<SessionGroup> getGroupList(String event_id){
        List<SessionGroup> group_list = new ArrayList<SessionGroup>();
        try {
            Cursor c = db.rawQuery("Select * from "+DBFeilds.TABLE_SCANNEDTICKETS_GROUP+" where "+DBFeilds.GROUP_EVENT_ID+" = '"+event_id+"'", null);

            for(int i=0; i<c.getCount();i++){
                c.moveToPosition(i);
                SessionGroup group = new SessionGroup();
                group.Id = c.getString(c.getColumnIndex(DBFeilds.GROUP_ID));
                group.Name = c.getString(c.getColumnIndex(DBFeilds.GROUP_NAME));
                group.Event__c = c.getString(c.getColumnIndex(DBFeilds.GROUP_EVENT_ID));
                group.Scan_Switch = Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.GROUP_SCAN_SWITCH)));
                group.BLN_Session_Items__r.records = getScannedItemsGroup(group.Event__c, group.Id );
                if(group.BLN_Session_Items__r.records.size() == 0){
                    continue;
                }
                group_list.add(group);
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return group_list;

    }

    public List<SessionGroup> getTicketedGroupList(String event_id){
        List<SessionGroup> group_list = new ArrayList<SessionGroup>();
        try {
            Cursor c = db.rawQuery("Select * from "+DBFeilds.TABLE_SCANNEDTICKETS_GROUP+" where "+DBFeilds.GROUP_EVENT_ID+" = '"+event_id+"'", null);

            for(int i=0; i<c.getCount();i++){
                c.moveToPosition(i);
                SessionGroup group = new SessionGroup();
                group.Id = c.getString(c.getColumnIndex(DBFeilds.GROUP_ID));
                group.Name = c.getString(c.getColumnIndex(DBFeilds.GROUP_NAME));
                group.Event__c = c.getString(c.getColumnIndex(DBFeilds.GROUP_EVENT_ID));
                group.Scan_Switch = Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.GROUP_SCAN_SWITCH)));
                group.BLN_Session_Items__r.records = getScannedItemsGroup(group.Event__c, group.Id );
                if(group.BLN_Session_Items__r.records.size() > 0){
                    if(isItemPoolFreeSession(group.BLN_Session_Items__r.records.get(0).BLN_Item_Pool__c, event_id)){
                        continue;
                    }
                }
                group_list.add(group);
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return group_list;

    }
    public TreeMap<String,String> getAllAttendeeItemPoolsWithNames(String event_id){
        TreeMap<String, String> id_name = new TreeMap<String, String>();
        try {
            Cursor c = db.rawQuery("select Item_Pool_Id, item_pool_name,item_type_name,Parent_Id  from OrderTicketDetails where Event_Id = '"+event_id+"' AND item_badgable='B - Badge' AND ((item_type_name='Package' AND Parent_Id !='null' ) OR (item_type_name !='Package' ) ) GROUP BY  Item_Pool_Id, item_type_name", null);
            for(int i=0;i<c.getCount();i++){
                c.moveToPosition(i);
                if(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMTYPE_NAME)).equalsIgnoreCase(ITEM_TYPES.Package.toString())){
                    String parent_id = getItemPoolParentId(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), event_id);
                    String itempool_parent_name = getItem_Pool_Name(parent_id, event_id);
                    // Log.i("-----------------Item Pool Name With Parent------------------",":"+c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)) +" [ "+itempool_parent_name+" ]");
                    id_name.put(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)) +" [ "+itempool_parent_name+" ]");
                }else{
                    id_name.put(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)));
                }


            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return id_name;
    }

    public TreeMap<String,String> getAllGroupList(String event_id){
        TreeMap<String, String> id_name = new TreeMap<String, String>();
        try {
            Cursor c = db.rawQuery("select Item_Pool_Id, item_pool_name,item_type_name,Parent_Id  from OrderTicketDetails where Event_Id = '"+event_id+"' AND ((item_type_name='Package' AND Parent_Id !='null' ) OR (item_type_name !='Package' ) ) GROUP BY  Item_Pool_Id, item_type_name", null);
            for(int i=0;i<c.getCount();i++){
                c.moveToPosition(i);
                if(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMTYPE_NAME)).equalsIgnoreCase(ITEM_TYPES.Package.toString())){
                    String parent_id = getItemPoolParentId(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), event_id);
                    String itempool_parent_name = getItem_Pool_Name(parent_id, event_id);
                    // Log.i("-----------------Item Pool Name With Parent------------------",":"+c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)) +" [ "+itempool_parent_name+" ]");
                    id_name.put(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)) +" [ "+itempool_parent_name+" ]");
                }else{
                    id_name.put(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)));
                }


            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return id_name;
    }



    public List<ScannedItems> getSwitchedOnScanItem(String Event_Id) {
        String group_id = getSwitchedONGroupId(Event_Id);
        List<ScannedItems> scanned_items = new ArrayList<ScannedItems>();
        Cursor c = db.rawQuery("select * from " + DBFeilds.TABLE_SCANNED_TICKETS + " where " + DBFeilds.SCANNED_EVENT_ID
                + "='" + Event_Id + "' AND " + DBFeilds.SCANNED_SWITCH + "='true' AND "+DBFeilds.SCANNED_GROUP_ID+" = '"+group_id+"'", null);
        //String item_pool_id = ITransaction.EMPTY_STRING;
        if(c != null){
            for(int i=0;i<c.getCount();i++){
                c.moveToPosition(i);
                ScannedItems item = new ScannedItems();
                item.Id = c.getString(c.getColumnIndex(DBFeilds.SCANNED_ID));
                item.BLN_Event__c = c.getString(c.getColumnIndex(DBFeilds.SCANNED_EVENT_ID));
                item.BLN_GN_User__c = c.getString(c.getColumnIndex(DBFeilds.SCANNED_GNUSER_ID));
                item.BLN_Item_Pool__c = c.getString(c.getColumnIndex(DBFeilds.SCANNED_ITEM_POOL_ID));
                item.Status__c = c.getString(c.getColumnIndex(DBFeilds.SCANNED_STATUS));
                item.DefaultValue__c = Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.SCANNED_SWITCH)));
                scanned_items.add(item);
            }
            c.close();
        }

        return scanned_items;
    }

    public boolean isAlowFreeSession(String Event_Id) {
        boolean isAllowSession = false;
        try {
            Cursor c = db.rawQuery("select * from " + DBFeilds.TABLE_SCANNED_TICKETS + " where "
                            + DBFeilds.SCANNED_EVENT_ID + "='" + Event_Id + "' AND " + DBFeilds.SCANNED_SWITCH + "='true'",
                    null);
            // String item_pool_id = ITransaction.EMPTY_STRING;
            if (c != null) {
                if (c.getCount() == 1) {
                    c.moveToFirst();
                    String item_pool_id = c.getString(c.getColumnIndex(DBFeilds.SCANNED_ITEM_POOL_ID));
                    isAllowSession = isItemPoolFreeSession(item_pool_id, Event_Id);
                }
                c.close();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return isAllowSession;
    }

    public boolean isTicketedSessionInOfflineScans(String event_id){
        boolean isTicketedSession = false;
        try {
            Cursor c = db.rawQuery("SELECT item_pool_id FROM offline_scan "+"where "+DBFeilds.OFFLINE_EVENT_ID+"='"+event_id+"' GROUP By item_pool_id", null);
            for(int i=0;i<c.getCount();i++){
                c.moveToPosition(i);
                if(!isItemPoolFreeSession(c.getString(c.getColumnIndex(DBFeilds.OFFLINE_ITEM_POOL_ID)), event_id)){
                    isTicketedSession = true;
                    break;
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return isTicketedSession;
    }

    public boolean isItemPoolFreeSession(String item_pool_id,String event_id){
        try {
            Cursor c = db.rawQuery("select * from "+DBFeilds.TABLE_ITEM_POOL+" where "+DBFeilds.ITEMPOOL_ID+" = '"+item_pool_id+"' AND "+DBFeilds.ITEMPOOL_EVENT_ID+" = '"+event_id+"'", null);
            if(c.moveToFirst()){
                boolean session = Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.ITEMPOOL_ALLOW_TICKETED_SESSION)));
                c.close();
                if(session){
                    session = false;
                }else{
                    session = true;
                }
                return session;
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return false;
    }

    public boolean isItemPoolBadgable(String item_pool_id, String Event_id){
        boolean isBadgable = false;
        try {

            Cursor c = db.rawQuery("select "+DBFeilds.ITEMPOOL_BADGABLE+" from "+DBFeilds.TABLE_ITEM_POOL+" where "+DBFeilds.ITEMPOOL_EVENT_ID+"='"+Event_id+"' AND "+DBFeilds.ITEMPOOL_ID+" = '"+item_pool_id+"'" , null);
            if(c.moveToFirst()){
                String badgable = c.getString(c.getColumnIndex(DBFeilds.ITEMPOOL_BADGABLE));
                if(badgable.equalsIgnoreCase("B - Badge")){
                    isBadgable = true;
                }
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return isBadgable;
    }
    public String getItemPoolBadgeLabel(String item_pool_id, String Event_id){
        String badgelabel = "";
        try {
            Cursor c = db.rawQuery("select "+DBFeilds.ITEMPOOL_BADGE_LABEL+" from "+DBFeilds.TABLE_ITEM_POOL+" where "+DBFeilds.ITEMPOOL_EVENT_ID+"='"+Event_id+"' AND "+DBFeilds.ITEMPOOL_ID+" = '"+item_pool_id+"'" , null);
            if(c.moveToFirst()){
                badgelabel = Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.ITEMPOOL_BADGE_LABEL)));
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return badgelabel;
    }

    public boolean SessionCheckInStatus(String ticket_id, String group_id){
        boolean checkin_status = false;
        try {
            Cursor c = db.rawQuery("select * from "+DBFeilds.TABLE_SESSION_ATTENDEES+" where "+DBFeilds.SESSION_ATTENDEE_TICKET_ID+"='"+ticket_id+"' AND "+DBFeilds.SESSION_GROUP_ID+" = '"+group_id+"'", null);
            if(c.moveToFirst()){
                checkin_status = Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS)));
                //return checkin_status;
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return checkin_status;
    }
    public String SessionCheckInStringStatus(String ticket_id, String group_id){
        String checkin_status = "";
        try {
            Cursor c = db.rawQuery("select * from "+DBFeilds.TABLE_SESSION_ATTENDEES+" where "+DBFeilds.SESSION_ATTENDEE_TICKET_ID+"='"+ticket_id+"' AND "+DBFeilds.SESSION_GROUP_ID+" = '"+group_id+"'", null);
            if(c.moveToFirst()){
                checkin_status = c.getString(c.getColumnIndex(DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS));
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return checkin_status;
    }
    public boolean isItemPoolSwitchON(String Item_Pool_Id, String Event_Id) {
        Cursor c = db.rawQuery("select * from " + DBFeilds.TABLE_SCANNED_TICKETS + " where "
                + DBFeilds.SCANNED_ITEM_POOL_ID + "='" + Item_Pool_Id + "' AND " + DBFeilds.SCANNED_EVENT_ID + " = '"
                + Event_Id + "' AND " + DBFeilds.SCANNED_SWITCH + "='true'", null);
        if (c.moveToFirst()) {
            c.close();
            return true;
        }
        return false;
    }
    public String getSwitchedONGroupId(String event_id){
        String group_id = ITransaction.EMPTY_STRING;
        try {
            Cursor c = db.rawQuery("Select * from "+DBFeilds.TABLE_SCANNEDTICKETS_GROUP+" where "+DBFeilds.GROUP_SCAN_SWITCH+" = 'true' AND "+DBFeilds.GROUP_EVENT_ID+" = '"+event_id+"'", null);
            if(c.moveToFirst()){
                group_id = c.getString(c.getColumnIndex(DBFeilds.GROUP_ID));
            }
            c.close();

        } catch (Exception e) {
            // TODO: handle exception
        }
        return group_id;
    }
    public List<String> getEventIdList(){
        List<String> event_ids = new ArrayList<String>();
        try {
            Cursor c = db.rawQuery("select EventID from "+DBFeilds.TABLE_EVENT_DETAILS, null);
            for(int i = 0;i<c.getCount();i++){
                c.moveToPosition(i);
                event_ids.add(c.getString(c.getColumnIndex("EventID")));
            }
            c.close();

        } catch (Exception e) {
            // TODO: handle exception
        }
        return event_ids;
    }

    public SessionGroup getSwitchedONGroup(String event_id){
        SessionGroup group =new SessionGroup();
        try {
            Cursor c = db.rawQuery("Select * from "+DBFeilds.TABLE_SCANNEDTICKETS_GROUP+" where "+DBFeilds.GROUP_SCAN_SWITCH+" = 'true' AND "+DBFeilds.GROUP_EVENT_ID+" = '"+event_id+"'", null);
            if(c.moveToFirst()){
                group.Id = c.getString(c.getColumnIndex(DBFeilds.GROUP_ID));
                group.Name = c.getString(c.getColumnIndex(DBFeilds.GROUP_NAME));
                group.Event__c = c.getString(c.getColumnIndex(DBFeilds.GROUP_EVENT_ID));
                group.Scan_Switch = Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.GROUP_SCAN_SWITCH)));
            }
            c.close();

        } catch (Exception e) {
            // TODO: handle exception
        }
        return group;
    }

    public SessionGroup getGroup(String event_id,String group_id){
        SessionGroup group =new SessionGroup();
        try {
            Cursor c = db.rawQuery("Select * from "+DBFeilds.TABLE_SCANNEDTICKETS_GROUP+" where "+DBFeilds.GROUP_ID+" = '"+group_id+"' AND "+DBFeilds.GROUP_EVENT_ID+" = '"+event_id+"'", null);
            if(c.moveToFirst()){
                group.Id = c.getString(c.getColumnIndex(DBFeilds.GROUP_ID));
                group.Name = c.getString(c.getColumnIndex(DBFeilds.GROUP_NAME));
                group.Event__c = c.getString(c.getColumnIndex(DBFeilds.GROUP_EVENT_ID));
                group.Scan_Switch = Boolean.valueOf(c.getString(c.getColumnIndex(DBFeilds.GROUP_SCAN_SWITCH)));
            }
            c.close();

        } catch (Exception e) {
            // TODO: handle exception
        }
        return group;
    }
    public void InsertAndUpdateSessionAttendees(List<TStatus> session_attendees,String event_id){
        int i= 0;
        for(TStatus attendee:session_attendees){
            try {
                ContentValues values = new ContentValues();
                values.put(DBFeilds.SESSION_ITEM_POOL_ID, attendee.BLN_Session_Item__r.BLN_Item_Pool__c);
                values.put(DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS, String.valueOf(attendee.Tstatus_name__c));
                values.put(DBFeilds.SESSION_ATTENDEE_SCANTIME, attendee.scan_time__c);
                values.put(DBFeilds.SESSION_ATTENDEE_FIRST_NAME, attendee.Ticket__r.tkt_profile__r.First_Name__c);
                values.put(DBFeilds.SESSION_ATTENDEE_LAST_NAME, attendee.Ticket__r.tkt_profile__r.Last_Name__c);
                values.put(DBFeilds.SESSION_ATTENDEE_EMAIL_ID, attendee.Ticket__r.tkt_profile__r.Email__c);
                values.put(DBFeilds.SESSION_ATTENDEE_COMPANY, attendee.Ticket__r.tkt_profile__r.TKT_Company__c);
                values.put(DBFeilds.SESSION_MOBILE, attendee.Ticket__r.tkt_profile__r.Mobile__c);
                values.put(DBFeilds.SESSION_TKT_JOB_TITLE, attendee.Ticket__r.tkt_profile__r.TKT_Job_Title__c);
                values.put(DBFeilds.SESSION_BADGE_LABEL,attendee.Ticket__r.Badge_Label__c);
                values.put(DBFeilds.SESSION_CUSTOM_BARCODE,attendee.Ticket__r.Custom_Barcode__c);
                values.put(DBFeilds.SESSION_ITEM_TYPE_ID, attendee.Ticket__r.Item_Type__r.Id);
                values.put(DBFeilds.SESSION_ITEM_TYPE_NAME, attendee.Ticket__r.Item_Type__r.Name);
                values.put(DBFeilds.SESSION_ATTENDEE_TICKET_ID, attendee.Ticket__c);
                values.put(DBFeilds.SESSION_EVENT_ID, event_id);
                values.put(DBFeilds.SESSION_ATTENDEE_ID , attendee.Id);
                values.put(DBFeilds.SESSION_GROUP_ID, attendee.BLN_Session_user__r.BLN_Group__c);
                values.put(DBFeilds.SESSION_GROUP_NAME, attendee.BLN_Session_user__r.BLN_Group__r.Name);

                boolean isExists = isRecordExists(DBFeilds.TABLE_SESSION_ATTENDEES, " where "+DBFeilds.SESSION_ATTENDEE_TICKET_ID+"='"+attendee.Ticket__c+"' AND "+DBFeilds.SESSION_ITEM_POOL_ID+" = '"+attendee.BLN_Session_Item__r.BLN_Item_Pool__c+"' AND "+DBFeilds.SESSION_GROUP_ID+" = '"+attendee.BLN_Session_user__r.BLN_Group__c+"'");
                if(isExists){
                    db.update(DBFeilds.TABLE_SESSION_ATTENDEES, values, DBFeilds.SESSION_ATTENDEE_TICKET_ID+"='"+attendee.Ticket__c+"' AND "+DBFeilds.SESSION_ITEM_POOL_ID+" = '"+attendee.BLN_Session_Item__r.BLN_Item_Pool__c+"' AND "+DBFeilds.SESSION_GROUP_ID+" = '"+attendee.BLN_Session_user__r.BLN_Group__c+"'", null);
                }else{
                    db.insert(DBFeilds.TABLE_SESSION_ATTENDEES, null, values);
                }

                i++;
                if(HttpGetMethod.dataloading != null){
                    HttpGetMethod.dataloading.onProgressUpdate(i);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        }


    }

    public void InsertAndUpdateOfflineScans(OfflineScansObject offlineScans){
        try {
            datavalues=new ContentValues();
            datavalues.put(DBFeilds.OFFLINE_ITEM_POOL_ID, offlineScans.item_pool_id);
            datavalues.put(DBFeilds.OFFLINE_BADGE_ID, offlineScans.badge_id);
            datavalues.put(DBFeilds.OFFLINE_EVENT_ID, offlineScans.event_id);
            datavalues.put(DBFeilds.OFFLINE_BADGE_STATUS, offlineScans.badge_status);
            datavalues.put(DBFeilds.OFFLINE_CHECKIN_STATUS, offlineScans.checkin_status);
            datavalues.put(DBFeilds.OFFLINE_SCAN_TIME, offlineScans.scan_date_time);
            datavalues.put(DBFeilds.OFFLINE_USER_ID, offlineScans.user_id);
            datavalues.put(DBFeilds.OFFLINE_ERROR_MSG, offlineScans.error);
            datavalues.put(DBFeilds.OFFLINE_ATTENDEE_NAME, offlineScans.name);
            datavalues.put(DBFeilds.OFFLINE_GROUP_ID, offlineScans.scan_group_id);
            datavalues.put(DBFeilds.OFFLINE_SCANDEVICE_MODE, offlineScans.scandevicemode);
		 	/*if(isRecordExists(DBFeilds.TABLE_OFFLINE_SCANS, " where "+DBFeilds.OFFLINE_BADGE_ID+" = '"+offlineScans.badge_id+"' AND "+DBFeilds.OFFLINE_ITEM_POOL_ID+" = '"+offlineScans.item_pool_id+"' AND "+DBFeilds.OFFLINE_EVENT_ID+" ='"+offlineScans.event_id+"'")){
		 		db.update(DBFeilds.TABLE_OFFLINE_SCANS, datavalues, DBFeilds.OFFLINE_BADGE_ID+" = '"+offlineScans.badge_id+"' AND "+DBFeilds.OFFLINE_ITEM_POOL_ID+" = '"+offlineScans.item_pool_id+"'AND "+DBFeilds.OFFLINE_EVENT_ID+" ='"+offlineScans.event_id+"'", null);
		 	}else{
		 		db.insert(DBFeilds.TABLE_OFFLINE_SCANS, null, datavalues);
		 	}*/

            db.insert(DBFeilds.TABLE_OFFLINE_SCANS, null, datavalues);
        } catch (Exception e) {
            // TODO: handle exception
        }

    }


    public void UpdateOfflineInvalidScans(OfflineScansObject offlineScans){
        try {
            datavalues = new ContentValues();
            datavalues.put(DBFeilds.OFFLINE_ITEM_POOL_ID, offlineScans.item_pool_id);
            datavalues.put(DBFeilds.OFFLINE_BADGE_ID, offlineScans.badge_id);
            datavalues.put(DBFeilds.OFFLINE_EVENT_ID, offlineScans.event_id);
            datavalues.put(DBFeilds.OFFLINE_BADGE_STATUS, offlineScans.badge_status);
            datavalues.put(DBFeilds.OFFLINE_CHECKIN_STATUS, offlineScans.checkin_status);
            datavalues.put(DBFeilds.OFFLINE_SCAN_TIME, offlineScans.scan_date_time);
            datavalues.put(DBFeilds.OFFLINE_USER_ID, offlineScans.user_id);
            datavalues.put(DBFeilds.OFFLINE_ERROR_MSG, offlineScans.error);
            datavalues.put(DBFeilds.OFFLINE_ATTENDEE_NAME, offlineScans.name);
            datavalues.put(DBFeilds.OFFLINE_GROUP_ID, offlineScans.scan_group_id);
            datavalues.put(DBFeilds.OFFLINE_SCANDEVICE_MODE, offlineScans.scandevicemode);

            db.update(DBFeilds.TABLE_OFFLINE_SCANS, datavalues,
                    DBFeilds.OFFLINE_BADGE_ID + " = '" + offlineScans.badge_id + "' AND "
                            + DBFeilds.OFFLINE_GROUP_ID + " = '" + offlineScans.scan_group_id + "'AND "
                            + DBFeilds.OFFLINE_EVENT_ID + " ='" + offlineScans.event_id + "'",
                    null);

            /*
             * if(isRecordExists(DBFeilds.TABLE_OFFLINE_SCANS, " where "
             * +DBFeilds.OFFLINE_BADGE_ID+" = '"+offlineScans.badge_id+"' AND "
             * +DBFeilds.OFFLINE_ITEM_POOL_ID+" = '"+offlineScans.item_pool_id+
             * "' AND "+DBFeilds.OFFLINE_EVENT_ID+" ='"
             * +offlineScans.event_id+"'")){
             * db.update(DBFeilds.TABLE_OFFLINE_SCANS, datavalues,
             * DBFeilds.OFFLINE_BADGE_ID+" = '"+offlineScans.badge_id+"' AND "
             * +DBFeilds.OFFLINE_ITEM_POOL_ID+" = '"+offlineScans.item_pool_id+
             * "'AND "+DBFeilds.OFFLINE_EVENT_ID+" ='"
             * +offlineScans.event_id+"'", null); }else{
             * db.insert(DBFeilds.TABLE_OFFLINE_SCANS, null, datavalues); }
             *
             * db.insert(DBFeilds.TABLE_OFFLINE_SCANS, null, datavalues)
             */;
        } catch (Exception e) {
            // TODO: handle exception
        }

    }
    public  int getOfflinescanscount(String where){
        int i=0;
        Cursor c=db.rawQuery("SELECT * FROM "+DBFeilds.TABLE_OFFLINE_SCANS+where, null);
        if(c!=null){
            i=c.getCount();
        }
        return i;
    }
    public List<OfflineScansObject> getOfflineScans(String where,boolean isFromAttendeeList){
        List<OfflineScansObject> offlineList=new ArrayList<OfflineScansObject>();
        try {
            //Log.i("-----------Query----------",":"+"SELECT * FROM "+DBFeilds.TABLE_OFFLINE_SCANS+where);
            Cursor c=db.rawQuery("SELECT * FROM "+DBFeilds.TABLE_OFFLINE_SCANS+where, null);
            for(int i=0;i<c.getCount();i++){
                c.moveToPosition(i);
                if(isItemPoolFreeSession(c.getString(c.getColumnIndex(DBFeilds.OFFLINE_ITEM_POOL_ID)), c.getString(c.getColumnIndex(DBFeilds.OFFLINE_EVENT_ID))) && isFromAttendeeList){
                    continue;
                }
                OfflineScansObject offlineScan=new OfflineScansObject();
                offlineScan.badge_id=c.getString(c.getColumnIndex(DBFeilds.OFFLINE_BADGE_ID));
                offlineScan.badge_status=c.getString(c.getColumnIndex(DBFeilds.OFFLINE_BADGE_STATUS));
                offlineScan.checkin_status=c.getString(c.getColumnIndex(DBFeilds.OFFLINE_CHECKIN_STATUS));
                offlineScan.event_id=c.getString(c.getColumnIndex(DBFeilds.OFFLINE_EVENT_ID));
                offlineScan.item_pool_id=c.getString(c.getColumnIndex(DBFeilds.OFFLINE_ITEM_POOL_ID));
                offlineScan.user_id=c.getString(c.getColumnIndex(DBFeilds.OFFLINE_USER_ID));
                offlineScan.error=c.getString(c.getColumnIndex(DBFeilds.OFFLINE_ERROR_MSG));
                offlineScan.scan_date_time=c.getString(c.getColumnIndex(DBFeilds.OFFLINE_SCAN_TIME));
                offlineScan.name = c.getString(c.getColumnIndex(DBFeilds.OFFLINE_ATTENDEE_NAME));
                offlineScan.scan_group_id = c.getString(c.getColumnIndex(DBFeilds.OFFLINE_GROUP_ID));
                offlineScan.scandevicemode = c.getString(c.getColumnIndex(DBFeilds.OFFLINE_SCANDEVICE_MODE));
                offlineList.add(offlineScan);
            }
            c.close();
        } catch (Exception e) {
            return offlineList;
        }

        return offlineList;
    }


    public int getTotalOfflineScanCount(String event_id){
        try {
            Cursor c=db.rawQuery("SELECT count(*) FROM "+DBFeilds.TABLE_OFFLINE_SCANS+" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='"+ event_id + "' AND "+DBFeilds.OFFLINE_BADGE_STATUS+" != '"+DBFeilds.STATUS_INVALID+"'", null);
            if(c.moveToFirst()){
                int count = c.getInt(0);
                c.close();
                return count;

            }else{
                c.close();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return 0;
    }


    public int getTotalOfflineScanCount(String event_id,boolean allrecords){
        try {
            Cursor c=db.rawQuery("SELECT count(*) FROM "+DBFeilds.TABLE_OFFLINE_SCANS+" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='"+ event_id + "'", null);
            if(c.moveToFirst()){
                int count = c.getInt(0);
                c.close();
                return count;

            }else{
                c.close();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return 0;
    }

    public int getALLOfflineScanCount(String event_id){
        try {
            Cursor c=db.rawQuery("SELECT count(*) FROM "+DBFeilds.TABLE_OFFLINE_SCANS+" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='"+ event_id + "'", null);
            if(c.moveToFirst()){
                int count = c.getInt(0);
                c.close();
                return count;

            }else{
                c.close();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return 0;
    }

    public void updateTicketSalesTax(String checked_in_eventId, String salex_tax, String isAplicable) {
        this.datavalues = new ContentValues();
        this.datavalues.put(DBFeilds.EVENT_SALESTAX, salex_tax);
        this.datavalues.put(DBFeilds.EVENT_FEE_APPLICABLE, isAplicable);
        this.db.update(Util.EVENT_DETAILS, this.datavalues, "EventID = '" + checked_in_eventId + "'", null);
    }

    public void InsertAndUpdateSEMINAR_AGENDA(List<SeminaAgenda> agenda_list){
        try {
            for(SeminaAgenda agenda:agenda_list){
                ContentValues values = new ContentValues();
                values.put(DBFeilds.SEMINAR_ITEM_POOL_ID, agenda.poolId);
                values.put(DBFeilds.SEMINAR_AGENDA_NAME, agenda.agendaName);
                values.put(DBFeilds.SEMINAR_START_DATE_TIME, agenda.startTime);
                values.put(DBFeilds.SEMINAR_END_DATE_TIME, agenda.endtime);
                values.put(DBFeilds.SEMINAR_ROOM_NAME, agenda.room);
                values.put(DBFeilds.SEMINAR_ROOM_NUMBER, agenda.roomNo);
                values.put(DBFeilds.SEMINAR_AGENDA_DESC, agenda.agendaDescription);
                boolean isExists = isRecordExists(DBFeilds.TABLE_SEMINA_AGENDA, " where "+DBFeilds.SEMINAR_ITEM_POOL_ID+"='"+agenda.poolId+"'");
                if(isExists){
                    db.update(DBFeilds.TABLE_SEMINA_AGENDA, values, DBFeilds.SEMINAR_ITEM_POOL_ID+"='"+agenda.poolId+"'", null);
                }else{
                    db.insert(DBFeilds.TABLE_SEMINA_AGENDA, null, values);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public SeminaAgenda getSeminarAgenda(String where_clause){
        SeminaAgenda agenda = null;
        try {

            Cursor c = db.rawQuery("select * from "+DBFeilds.TABLE_SEMINA_AGENDA+" "+where_clause, null);
            if(c.moveToFirst()){
                agenda = new SeminaAgenda();
                agenda.poolId = c.getString(c.getColumnIndex(DBFeilds.SEMINAR_ITEM_POOL_ID));
                agenda.agendaName = c.getString(c.getColumnIndex(DBFeilds.SEMINAR_AGENDA_NAME));
                agenda.startTime = c.getString(c.getColumnIndex(DBFeilds.SEMINAR_START_DATE_TIME));
                agenda.endtime = c.getString(c.getColumnIndex(DBFeilds.SEMINAR_END_DATE_TIME));
                agenda.room = c.getString(c.getColumnIndex(DBFeilds.SEMINAR_ROOM_NAME));
                agenda.roomNo = c.getString(c.getColumnIndex(DBFeilds.SEMINAR_ROOM_NUMBER));
                agenda.agendaDescription = c.getString(c.getColumnIndex(DBFeilds.SEMINAR_AGENDA_DESC));
            }
            c.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return agenda;
    }

    public void InsertAndUpdateSESSION_GROUP(List<SessionGroup> session_group){
        try {
            for(SessionGroup group : session_group){
                ContentValues values = new ContentValues();
                values.put(DBFeilds.GROUP_ID, group.Id);
                values.put(DBFeilds.GROUP_NAME, group.Name);
                values.put(DBFeilds.GROUP_EVENT_ID, group.Event__c);
                values.put(DBFeilds.GROUP_PRODUCT_TYPE, group.Product_Type__c);
                if(group.BLN_Session_users__r != null){
                    if(group.BLN_Session_users__r.records.size() > 0){
                        values.put(DBFeilds.GROUP_SCAN_SWITCH,String.valueOf(group.BLN_Session_users__r.records.get(0).DefaultValue__c));
                    }
                }
                boolean isExists = isRecordExists(DBFeilds.TABLE_SCANNEDTICKETS_GROUP, " where "+DBFeilds.GROUP_ID+"='"+ group.Id+"' AND "+DBFeilds.GROUP_EVENT_ID+" = '"+group.Event__c+"'");
                if(isExists){
                    db.update(DBFeilds.TABLE_SCANNEDTICKETS_GROUP, values, DBFeilds.GROUP_ID+"='"+ group.Id+"' AND "+DBFeilds.GROUP_EVENT_ID+" = '"+group.Event__c+"'", null);
                }else{
                    db.insert(DBFeilds.TABLE_SCANNEDTICKETS_GROUP, null, values);
                }
                if( group.BLN_Session_Items__r != null){
                    if(group.BLN_Session_users__r.records.size() > 0){
                        for(ScannedItems scan_item : group.BLN_Session_Items__r.records){
                            scan_item.DefaultValue__c = group.BLN_Session_users__r.records.get(0).DefaultValue__c;
                        }
                    }
                    InsertAndUpdateScannedItems( group.BLN_Session_Items__r.records,group.Event__c);
                }

            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void UpdateSESSION_GROUP(SessionGroup session_group){
        try {

            ContentValues values = new ContentValues();
            values.put(DBFeilds.GROUP_ID, session_group.Id);
            values.put(DBFeilds.GROUP_NAME, session_group.Name);
            values.put(DBFeilds.GROUP_EVENT_ID, session_group.Event__c);
            values.put(DBFeilds.GROUP_PRODUCT_TYPE, session_group.Product_Type__c);
            values.put(DBFeilds.GROUP_SCAN_SWITCH,String.valueOf(session_group.Scan_Switch));
            db.update(DBFeilds.TABLE_SCANNEDTICKETS_GROUP, values, DBFeilds.GROUP_ID+"='"+ session_group.Id+"' AND "+DBFeilds.GROUP_EVENT_ID+" = '"+session_group.Event__c+"'", null);
            if (session_group.BLN_Session_Items__r.records.size() > 0) {
                for (ScannedItems scan_item : session_group.BLN_Session_Items__r.records) {
                    scan_item.DefaultValue__c = session_group.Scan_Switch;
                }
            }
            InsertAndUpdateScannedItems(session_group.BLN_Session_Items__r.records, session_group.Event__c);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public int deleteOffLineScans(String where){
        return this.db.delete(DBFeilds.TABLE_OFFLINE_SCANS, where, null);
    }

    public String getItemPoolID(String ticket_id){
        String item_pool_id=ITransaction.EMPTY_STRING;
        try {
            Cursor c = db.rawQuery("select * from "+DBFeilds.TABLE_ATTENDEE_DETAILS+" where "+DBFeilds.ATTENDEE_ID+"='"+ticket_id+"'", null);
            if(c.moveToFirst()){
                item_pool_id = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID));
                c.close();
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
        return item_pool_id;
    }


    public void DownloadFromUrl(String DownloadUrl, String fileName) {
        try {
            File dir = new File(new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath())).append(Util.APP_FOLDER_NAME).toString());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            URL url = new URL(DownloadUrl);
            File file = new File(dir, fileName);
            BufferedInputStream bis = new BufferedInputStream(url.openConnection().getInputStream());
            ByteArrayBuffer baf = new ByteArrayBuffer(5000);
            while (true) {
                int current = bis.read();
                if (current == -1) {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(baf.toByteArray());
                    fos.flush();
                    fos.close();
                    return;
                }
                baf.append((byte) current);
            }
        } catch (IOException e) {
        }
    }

    public Bitmap getBitmap(byte[] bitmap) {
        return BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
    }

    public byte[] getByteArray(Bitmap bitmap) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 100, bos);
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getimagedata(byte[] bitmapImagedata) {
        if (bitmapImagedata != null) {
            try {
                return Base64.encodeToString(bitmapImagedata, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public byte[] getBlobImage(String url) {
        try {
            return EntityUtils.toByteArray(new DefaultHttpClient().execute(new HttpGet(url)).getEntity());
        } catch (Exception e) {
            e.printStackTrace();
            return getByteArray(BitmapFactory.decodeResource(this.context.getResources(), R.drawable.default_image));
        }
    }

    public Bitmap GetImage(String filename) {
        try {
            File imgFile = new File(Environment.getExternalStorageDirectory() + Util.APP_FOLDER_NAME + "/" + filename);
            if (imgFile.exists()) {
                return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            }
            return BitmapFactory.decodeResource(this.context.getResources(), R.drawable.default_image);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        try {
            int width = bm.getWidth();
            int height = bm.getHeight();
            float scaleWidth = ((float) newWidth) / ((float) width);
            float scaleHeight = ((float) newHeight) / ((float) height);
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean isOnline() {
        try {
            NetworkInfo ni = ((ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (ni == null || !ni.isConnected()) {
                return Boolean.valueOf(false);
            }
            return Boolean.valueOf(true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
