//ScanAttendee Android
//Created by Ajay on 08-Jan-2015
//This class is used to get the all the orders and tickets information from backend.
//Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.database;

public class DBFeilds {

    // Object which stores the path of database to be copied from the assets
    public static final  String DB_PATH = "data/data/com.globalnest.scanattendee/databases/";

    // Variable which defines the Datbase name
    public static final  String DATABASE_NAME = "ScanAttendee";

    // Variable which defines the Database version
    public static final int DATABASE_VERSION = 28;

    // Variables which defines the table name



    public static final String TABLE_SFDCD_DETAILS = "sfdcd_details";
    public static final String TABLE_USER = "USER_PROFILE";
    public static final String TABLE_EVENT_DETAILS = "EventDetails";
    public static final String TABLE_ADDED_TICKETS = "ItemDetails";
    public static final String TABLE_ITEM_POOL = "ItemPoolDetails";
    public static final String TABLE_ITEM_TYPE = "ItemType";
    //public static final String TABLE_ADDED_ITEMS = "AddedItems";
    public static final String TABLE_ATTENDEE_DETAILS = "OrderTicketDetails";
    public static final String TABLE_ATTENDEE_FEILDTYPES = "OrderTicketFeildTypes";
    public static final String TABLE_SALES_ORDER = "OrderDetails";
    public static final String TABLE_SALES_ORDER_ITEM = "OrderItemDetails";
    public static final String TABLE_ORDER_PAYMENT_ITEMS = "OrderPaymentItemDetails";
    public static final String TABLE_SALES_REFUND = "RefundOrderDetails";
    public static final String TABLE_BADGE_TEMPLATE = "BadgeTemplate";
    public static final String TABLE_BADGE_TEMPLATE_NEW = "BadgeTemplateNew";
    public static final String TABLE_BADGE_LAYERS = "BadgeLayers";
    public static final String TABLE_COUNTRY = "Country";
    public static final String TABLE_STATE = "State";
    public static final String TABLE_PAYMENT_GATEWAYS = "PaymentGateways";
    public static final String TABLE_EVENT_PAYAMENT_SETTINGS = "PaymentSettings";
    public static final String TABLE_ADDRESS = "UserAddress";
    public static final String TABLE_PAY_GATEWAY_KEYS="Pay_Gateway_Keys";
    public static final String TABLE_TICKET_TAG="ticket_tag";
    public static final String TABLE_ORGANAIZER_INFO="Organaizer_Info";
    public static final String TABLE_EVENTDEX = "Eventdex_Credentials";
    public static final String TABLE_EVENT_PRICING = "Event_Pricing";
    public static final String TABLE_ITEM_REG_SETTINGS = "Item_Reg_Settings";
    public static final String TABLE_SCANNED_TICKETS ="ScannedTickets";
    public static final String TABLE_CURRENCY = "Currency";
    public static final String TABLE_SESSION_ATTENDEES= "SessionAttendees";
    public static final String TABLE_OFFLINE_SCANS="offline_scan";
    public static final String TABLE_SEMINA_AGENDA = "seminar_agenda";
    public static final String TABLE_SCANNEDTICKETS_GROUP = "ScannedTickets_Group";
    public static final String TABLE_TSTATUS = "TStatus";
    public static final String TABLE_HIDEITEMS = "HideItems";
    public static final String TABLE_PICKLISTVALUES = "PicklistValues";

    // SFDC Details table fields
    public static final String INSTANCE_URL = "instance_url";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String USER_ID = "user_id";
    public static final String ACCESS_TOKEN_TYPE = "token_type";
    public static final String ACCESS_TOKEN_TIME = "token_time";

    // User table fields

    public static final String USER_USERID = "UserID";
    public static final String USER_GNUSER_ID = "GNUserID";
    public static final String USER_COMPANY_ID = "CompanyId";
    public static final String USER_IMAGE_URL = "ImageURL";
    public static final String USER_IMAGE_BLOB = "ImageBlob";
    public static final String USER_FIRST_NAME = "FirstName";
    public static final String USER_LAST_NAME = "LastName";
    public static final String USER_COMPANY = "Company";
    public static final String USER_EMAIL_ID = "EmailID";
    public static final String USER_PHONE_NUMBER = "PhoneNumber";
    public static final String USER_CITY = "City";
    public static final String USER_STATE = "State";
    public static final String USER_PASSWORD = "Password";
    public static final String USER_USER_ROLE = "UserRole";
    public static final String USER_COUNTRY = "Country";
    public static final String USER_DESIGNATION = "designation";
    public static final String USER_EVENT_ID = "Event_Id";
    public static final String LASTREFRESH_DATE = "LastRefreshedDate";

    // State table fields
    public static final String STATE_COUNTRY_ID = "CountryId";
    public static final String STATE_LONG_NAME = "State_Long_Name";
    public static final String STATE_ID = "StateId";
    public static final String STATE_SHORT_NAME = "State_Short_Name";

    // Country table fields
    public static final String COUNTRY_ID = "CountryId";
    public static final String COUNTRY_LONG_NAME = "Country_Long_Name";
    public static final String COUNTRY_SHORT_NAME = "Country_Short_Name";
    public static final String CURRENCY_NAME = "currency_name";
    public static final String CURRENCY_ID = "currency_id";
    public static final String CURRENCY_FULL_NAME = "currency_full_name";
    public static final String CURRENCY_SYMBOL="currencey_symbol";


    // Event table fields

    public static final String EVENT_NAME = "EventName";
    public static final String EVENT_REGISTRATION_LINK="registration_link";
    public static final String EVENT_START_DATE = "StartDate";
    //public static final String EVENT_START_TIME = "StartTime";
    public static final String EVENT_END_DATE = "EndDate";
    //public static final String EVENT_END_TIME = "EndTime";
    public static final String EVENT_ZEBRADPI= "ZebraDpi__c";
    public static final String EVENT_ISCHECKEDIN = "isCheckedIn";
    public static final String EVENT_EVENT_ID = "EventID";
    public static final String EVENT_ADDRESS = "Address";
    public static final String EVENT_LOCATION = "Location";
    public static final String EVENT_CITY = "City";
    public static final String EVENT_COUNTRY = "Country";
    public static final String EVENT_COUNTRY_ID = "Country_Id";
    public static final String EVENT_COUNTRY_SHORT_NAME = "Country_Short";
    public static final String EVENT_STATE = "State";
    public static final String EVENT_STATE_ID = "State_Id";
    public static final String EVENT_STATE_SHORT = "State_Short";
    public static final String EVENT_PHONE = "Phone";
    public static final String EVENT_ZIPCODE = "ZipCode";
    public static final String EVENT_CATEGORY = "EventCategory";
    public static final String EVENT_TOTAL_SEATS_AVAILABLE = "TotalSeatsAvailable";
    public static final String EVENT_ISEVENT_STAFF = "isEventStaff";
    public static final String EVENT_IMAGE_URL = "EventImageUrl";
    public static final String EVENT_SA_SELFCHECKIN_IMAGE_URL = "SADashboardimgUrl";
    public static final String EVENT_SA_SELFCHECKIN_BACKGROUNDCOLOUR = "selfcheckinbgcolor";
    public static final String EVENT_SA_SELFCHECKIN_LOGO = "selfcheckineventlogo";
    public static final String EVENT_BADGE_NAME = "EventBadgeName";
    public static final String EVENT_LASTMODIFYDATE = "LastModifyDate";
    public static final String EVENT_FEE_APPLICABLE = "FeeApplicable";
    public static final String EVENT_IMAGE_BLOB = "ImageBlob";
    public static final String EVENT_SALESTAX = "EventSalesTax";
    public static final String EVENT_DESCRIPTION = "EventDescription";
    public static final String EVENT_STATUS = "EventStatus";
    public static final String EVENT_TICKET_TYPES = "TicketTypes";
    public static final String EVENT_TIME_ZONE = "TimeZone";
    public static final String EVENT_SCANATTENDEE_LIMIT = "scan_attendee_limit__c";
    public static final String EVENT_TOTAL_ATTENDEES_COUNT = "event_total_attendee_count";
    public static final String EVENT_TOTAL_ORDERS_COUNT = "event_total_orders_count";
    // public static final String EVENT_ORGANAIZER_ID = "organaizer_id";
    public static final String EVENT_ORGANAIZER_NAME = "organaizer_name";
    public static final String EVENT_ORDERS_COUNT = "orders_count";
    public static final String EVENT_ORGANIZER_ID="organizer_id__c";
    public static final String EVENT_ORGANIZER_EMAIL="organizer_email";
    public static final String EVENT_SESSION_TIME = "sessiontime";
    public static final String EVENT_USER_ROLE = "user_role";
    public static final String EVENT_SELECTED_COUNTRY_CODES = "country_codes";
    public static final String EVENT_ALLOW_SESSION = "AllowNoticketSessions__c";
    public static final String EVENT_CURRENCY = "event_currency";
    public static final String EVENT_DASHBOARDVISIBILITY = "event_dashboardvisibity";

    // Itempool data fields

    public static final String ITEMPOOL_EVENT_ID = "EventId";
    public static final String ITEMPOOL_ID = "ItemPoolId";
    public static final String ITEMPOOL_ITEMPOOLNAME = "ItemPoolName";
    public static final String ITEMPOOL_ITEMTYPE_ID = "ItemTypeId";
    public static final String ITEMPOOL_COUNT = "ItemPoolCount";
    public static final String ITEMPOOL_POOLNAME = "PoolName";
    public static final String ITEMPOOL_ADDON_COUNT = "Addon_Count__c";
    public static final String ITEMPOOL_ADDON_PARENT = "Addon_Parent__c";
    public static final String ITEMPOOL_BADGABLE="Badgable__c";
    public static final String ITEMPOOL_TICKET_SETTINGS = "Ticket_Settings__c";
    public static final String ITEMPOOL_ALLOW_TICKETED_SESSION = "ItemPool_Ticketed_Sessions__c";
    public static final String ITEMPOOL_BADGE_LABEL="Badge_Label__c";//added new by sai TODO

    // ITEM TYPE data fields

    public static final String ITEM_TYPE_ID = "ItemTypeId";
    public static final String ITEM_TYPE_NAME = "ItemTypeName";
    public static final String ITEM_TYPE_CURRENCY = "ItemTypeCurrency";

    // BADGE TEMPLATES data fields

    public static final String BADGE_EVENT_ID = "EventID";
    public static final String BADGE_ISBADGE_SELECTED = "isBadgeSelected";
    public static final String BADGE_BADGE_WIDTH = "BadgeWidth";
    public static final String BADGE_BADGE_HEIGHT = "BadgeHeight";
    public static final String BADGE_BADGE_NAME = "BadgeName";
    public static final String BADGE_BADGE_IMAGE = "BadgeImage";
    public static final String BADGE_BADGE_BGCOLOR = "BadgeBGColor";
    public static final String BADGE_CONTENTTEXT = "ContentText";
    public static final String BADGE_BADGETEMPLATE_TYPE = "BadgeTemplateType";
    public static final String BADGE_WIDTH = "Width";
    public static final String BADGE_HEIGHT = "Height";
    public static final String BADGE_BGCOLOR = "BGColor";
    public static final String BADGE_FONTSIZE = "FontSize";
    public static final String BADGE_TEXTALIGN = "TextAlign";
    public static final String BADGE_TEXTCOLOR = "TextColor";
    public static final String BADGE_MARGINTOP = "MarginTop";
    public static final String BADGE_MARGINRIGHT = "MarginRight";
    public static final String BADGE_MARGINBOTTOM = "MarginBottom";
    public static final String BADGE_MARGINLEFT = "MarginLeft";
    public static final String BADGE_ELEMENTID = "ElementID";
    public static final String BADGE_FONTFAMILY = "FontFamily";

    // ORDER DETAILS data fields

    public static final String ORDER_EVENT_ID = "Event_Id";
    public static final String ORDER_ORDER_ID = "Order_Id";
    public static final String ORDER_BUYER_ID = "Buyer_Id";
    public static final String ORDER_PAYMENT_TYPE = "Payment_Type";
    public static final String ORDER_ORDER_STATUS = "Order_Status";
    public static final String ORDER_ORDER_TOTAL = "Order_Total";
    public static final String ORDER_ORDER_DATE = "Order_Date";
    public static final String ORDER_FEE_AMOUNT = "Fee_Amount";
    public static final String ORDER_AMOUNT_PAID = "Amount_Paid";
    public static final String ORDER_AMOUNT_DUE = "Amount_Due";
    public static final String ORDER_ORDER_DISCOUNT = "Order_Discount";
    public static final String ORDER_ORDER_TAX = "Order_Tax";
    public static final String ORDER_ORDER_NAME = "Order_Name";
    public static final String ORDER_ORDER_SUBTOTAL = "Order_SubTotal";
    public static final String ORDER_PAYKEY = "PayKey";
    public static final String ORDER_PAYMENT_MODE = "Payment_Mode";
    public static final String ORDER_CHECK_NUMBER = "check_number";
    public static final String ORDER_CREDIT_CARD_TYPE = "card_type";
    public static final String ORDER_LAST_4DIGIT = "last_card_degits";
    public static final String ORDER_REGISTRATION_TYPE = "registration_type";
    public static final String ORDER_PAYGATEWAY_TYPE_ID = "paygateway_type_id";
    public static final String ORDER_PAYGATEWAY_TYPE_NAME = "paygateway_type_name";
    public static final String ORDER_PAYMENT_ID = "payment_id";
    // ORDER ITEM DETAILS data fields
//
   /* CreatedDate="",credit_card_last_4digits__c="",credit_card_type__c="",Currency_Code__c="",Id="",
    LastModifiedDate="",Name="",Order__c="",Payment_Amount__c="",Payment_Mode__c="",Payment_Ref_Number__c="",
    Payment_Status__c="",Payment_Type__c="",Pay_reg_type__c="";*/


    //OrderPaymentItemDetails Db fields for Mutliple Partial Payments Table newly added
    public static final String ORDER_PAYMENT_ITEM_ID = "payment_item_id";
    public static final String ORDER_PAYMENT_ITEM_CREATEDDATE = "payment_createddate";
    public static final String ORDER_PAYMENT_ITEM_SETTLEMENTDATE = "payment_settlementdate";
    public static final String ORDER_PAYMENT_ITEM_CREDIT_CARD_4DIGITS = "payment_credit_card_4digits";
    public static final String ORDER_PAYMENT_ITEM_CREDIT_CARD_TYPE = "payment_credit_card_type";
    public static final String ORDER_PAYMENT_ITEM_CURRENCY_CODE = "currency_code";
    public static final String ORDER_PAYMENT_ITEM_LASTMODIFIEDDATE = "payment_lastmodifieddate";
    public static final String ORDER_PAYMENT_ITEM_NAME = "payment_name";
    public static final String ORDER_PAYMENT_ITEM_ORDER_ID = "payment_order_id";
    public static final String ORDER_PAYMENT_ITEM_PAYMENT_AMOUNT = "payment_amount";
    public static final String ORDER_PAYMENT_ITEM_PAYMENT_MODE = "payment_mode";
    public static final String ORDER_PAYMENT_ITEM_PAYMENT_REF_NUMBER = "payment_ref_number";
    public static final String ORDER_PAYMENT_ITEM_PAYMENT_STATUS = "payment_status";
    public static final String ORDER_PAYMENT_ITEM_PAYMENT_TYPE = "payment_type";
    public static final String ORDER_PAYMENT_ITEM_PAY_REG_TYPE = "pay_reg_type";
    public static final String ORDER_PAYMENT_ITEM_PAY_GATEWAY = "payment_pay_gateway_id";
    public static final String ORDER_PAYMENT_ITEM_PAY_GATEWAY_NAME = "payment_pay_gateway_name";
    public static final String ORDER_PAYMENT_ITEM_EVENTID = "payment_item_eventid";
    public static final String ORDER_PAYMENT_ITEM_NOTES = "payment_notes";




    public static final String ORDERITEM_ORDER_ITEM_ID = "Order_Item_Id";
    public static final String ORDERITEM_ORDER_ID = "Order_Id";
    public static final String ORDERITEM_POOL_NAME = "Pool_Name";
    public static final String ORDERITEM_ITEM_ID = "Item_Id";
    public static final String ORDERITEM_ITEM_NAME = "Item_Name";
    public static final String ORDERITEM_ITEM_TYPE = "Item_Type";
    public static final String ORDERITEM_EVENT_ID = "Event_Id";
    public static final String ORDERITEM_ITEM_QTY = "Item_Qty";
    public static final String ORDERITEM_ITEM_TOTAL_PRICE = "Item_Total_Price";
    public static final String ORDERITEM_EACH_ITEM_PRICE = "Each_Item_Price";
    public static final String ORDERITEM_ITEM_DISCOUNT = "Item_Discount";
    public static final String ORDERITEM_PROMO_CODE = "Promo_Code";
    public static final String ORDERITEM_STATUS = "Order_Item_Status";
    public static final String ORDERITEM_FEE = "Order_Item_Fee";
    public static final String ORDERITEM_TAX = "Order_Item_Tax";
    public static final String ORDERITEM_CURRENCY = "Order_Item_Currency";

    // ORDER TICKET DETAILS data fields

    // new ORDER TICKET DETAILS data fields
    //newly added image feild
     public static final String ATTENDEE_ID = "Attendee_Id";

   public static final String ATTENDEE_PREFIX = "Prefix__c";
    public static final String ATTENDEE_FIRST_NAME = "First_Name__c";
    public static final String ATTENDEE_LAST_NAME = "Last_Name__c";
    public static final String ATTENDEE_SUFFIX = "Suffix__c";
    public static final String ATTENDEE_JOB_TILE = "Title__c";
    public static final String ATTENDEE_MOBILE = "Mobile__c";
    public static final String ATTENDEE_PHONE = "Phone__c";
    public static final String ATTENDEE_AGE="Age__c";
    public static final String ATTENDEE_GENDER="Gender__c";
    public static final String ATTENDEE_DOB="DOB__c";
    public static final String ATTENDEE_PRIMARY_BUSINESS_CATEGORY="Primary_Business_Category__c";
    public static final String ATTENDEE_SECONDARY_BUSINESS_CATEGORY="Secondary_Business_Category__c";
    public static final String ATTENDEE_NUMBER_OF_EMPLOYEES="Number_Of_Employees__c";
    public static final String ATTENDEE_ESTABLISHED_DATE="Established_Date__c";
    public static final String ATTENDEE_REVENUE="Revenue__c";
    public static final String ATTENDEE_TAX_ID="Tax_Id__c";
    public static final String ATTENDEE_WEBSITE_URL="Website_URL__c";//reg table column name
    public static final String ATTENDEE_DUNS_NUMBER="Duns_Number__c";
    public static final String ATTENDEE_BLOG_URL="Blog_URL__c";
    public static final String ATTENDEE_DESCRIPTION="Description__c";//reg table column name
    public static final String ATTENDEE_KEYWORDS="Keywords__c";
    public static final String ATTENDEE_EXCEPTIONAL_KEYWORDS="Exceptional_Keywords__c";
    public static final String ATTENDEE_DBA="DBA__c";
    public static final String ATTENDEE_BBB_NUMBER="BBB_Number__c";
    public static final String ATTENDEE_GSA_SCHEDULE="GSA_Schedule__c";
    public static final String ATTENDEE_GEOGRAPHICAL_REGION="Geographical_Region__c";
    public static final String ATTENDEE_ETHNICITY="Ethnicity__c";
    public static final String ATTENDEE_BUSINESS_STRUCTURE="Business_Structure__c";
    public static final String ATTENDEE_CAGECODE="CageCode__c";
    public static final String ATTENDEE_DISTRIBUTION_COUNTRY="distribution_Country__c";
    public static final String ATTENDEE_FAXNUMBER="FaxNumber__c";
    public static final String ATTENDEE_MANUFACTURES_COUNTRY="Manufactures_Country__c";
    public static final String ATTENDEE_OUTSIDE_FACILITIES="Outside_Facilities__c";
    public static final String ATTENDEE_REFERENCES1="References1__c";
    public static final String ATTENDEE_REFERENCES2="References2__c";
    public static final String ATTENDEE_SCOPEOFWORK1="ScopeOfWork1__c";
    public static final String ATTENDEE_SCOPEOFWORK2="ScopeOfWork2__c";
    public static final String ATTENDEE_SECONDARY_EMAIL="Secondary_email__c";
    public static final String ATTENDEE_YEAR_IN_BUSINESS="Year_in_business__c";
    public static final String ATTENDEE_TABLE_NUMBER="Table_Number__c";
    public static final String ATTENDEE_LOCATION_ROOM="Location_Room__c";
    public static final String ATTENDEE_COMPANY_LOGO = "Company_Logo__c";
    public static final String ATTENDEE_SPEAKERLINKEDINID="SpeakerLinkedInId";
    public static final String ATTENDEE_SPEAKERTWITTERID="SpeakerTwitterId";
    public static final String ATTENDEE_SPEAKERBLOGGER="SpeakerBlogger";
    public static final String ATTENDEE_BIOGRAPHY="Biography__c";
    public static final String ATTENDEE_SPEAKERVIDEO="SpeakerVideo";
    public static final String ATTENDEE_WHATSAPP="WhatsApp__c";
    public static final String ATTENDEE_WECHAT="Wechat__c";
    public static final String ATTENDEE_SKYPE="Skype__c";
    public static final String ATTENDEE_SNAPCHAT="Snapchat__c";
    public static final String ATTENDEE_INSTAGRAM="Instagram__c";
    public static final String ATTENDEE_LIST_TYPE="List_Type__c";
    public static final String ATTENDEE_LIST_DESCRIPTION="List_Description__c";
    public static final String ATTENDEE_LIST_CODE="List_Code__c";
















    public static final String ATTENDEE_IMAGE = "User_Pic__c";



    public static final String ATTENDEE_EMAIL_ID = "Email__c";
    public static final String ATTENDEE_COMPANY_ID = "Attendee_Company_Id";
    public static final String ATTENDEE_COMPANY = "TKT_Company__c";//"Attendee_Company";
    public static final String ATTENDEE_TICKET_STATUS = "Ticket_Status__c";
    public static final String ATTENDEE_RSVP_STATUS = "RSVP__c";




    public static final String ATTENDEE_WORK_PHONE = "Work_Phone__c";
    public static final String ATTENDEE_WORK_ADDRESS_1 = "Address1__c";
    public static final String ATTENDEE_WORK_ADDRESS_2 = "Address2__c";
    public static final String ATTENDEE_WORK_CITY = "Attendee_City";
    public static final String ATTENDEE_WORK_STATE = "Attendee_State";
    public static final String ATTENDEE_WORK_COUNTRY = "Attendee_Country";
    public static final String ATTENDEE_WORK_ZIPCODE = "Attendee_Zipcode";

    public static final String ATTENDEE_HOME_PHONE = "Home_Phone__c";
    public static final String ATTENDEE_HOME_ADDRESS_1 = "Attendee_Home_Address_1";
    public static final String ATTENDEE_HOME_ADDRESS_2 = "Attendee_Home_Address_2";
    public static final String ATTENDEE_HOME_CITY = "Attendee_Home_City";
    public static final String ATTENDEE_HOME_STATE = "Attendee_Home_State";
    public static final String ATTENDEE_HOME_COUNTRY = "Attendee_Home_Country";
    public static final String ATTENDEE_HOME_ZIPCODE = "Attendee_Home_Zipcode";

    public static final String ATTENDEE_BILLING_PHONE = "Billing_Phone__c";
    public static final String ATTENDEE_BILLING_ADDRESS_1 = "Attendee_Billing_Address_1";
    public static final String ATTENDEE_BILLING_ADDRESS_2 = "Attendee_Billing_Address_2";
    public static final String ATTENDEE_BILLING_CITY = "Attendee_Billing_City";
    public static final String ATTENDEE_BILLING_STATE = "Attendee_Billing_State";
    public static final String ATTENDEE_BILLING_COUNTRY = "Attendee_Billing_Country";
    public static final String ATTENDEE_BILLING_ZIPCODE = "Attendee_Billing_Zipcode";

    public static final String ATTENDEE_CHECKEDINDATE = "CheckedInDate";
    public static final String ATTENDEE_EVENT_ID = "Event_Id";
    public static final String ATTENDEE_BADGE_NAME = "Attendee_Badge_Name";
    public static final String ATTENDEE_BADGEID = "BadgeId";
    public static final String ATTENDEE_BADGE_PARENT_ID = "Badge_Parent_Id";
    public static final String ATTENDEE_REASON = "Reason";
    public static final String ATTENDEE_ITEM_ID = "Item_Id";
    public static final String ATTENDEE_ITEM_NAME = "Item_Name";
    public static final String ATTENDEE_ITEM_TYPE_ID = "Item_Type_Id";
    public static final String ATTENDEE_ITEM_POOL_ID = "Item_Pool_Id";
    public static final String ATTENDEE_TIKCET_NUMBER = "Tikcet_Number";
    public static final String ATTENDEE_ORDER_NUMBER= "Order_Id_Number";

    // public static final String ATTENDEE_ISCHECKIN = "isCheckin";
    public static final String ATTENDEE_TICKET_SEAT_NUMBER = "Ticket_Seat_Number";
    public static final String ATTENDEE_ORDER_ID = "Order_Id";
    public static final String ATTENDEE_ORDER_ITEM_ID = "Order_Item_Id";
    public static final String ATTENDEE_BUYER_ID = "buyer_id";
    public static final String ATTENDEE_TAG = "tag";
    public static final String ATTENDEE_NOTE = "note";
    // public static final String ATTENDEE_DESIGNATION = "designation";
    public static final String ATTENDEE_BADGE_LABLE = "BadgeLable";
    public static final String ATTENDEE_ITEM_PARENT_ID = "Parent_Id";
    public static final String ATTENDEE_ITEMPOOL_NAME = "item_pool_name";
    public static final String ATTENDEE_ITEMTYPE_NAME = "item_type_name";
    public static final String ATTENDEE_BADGABLE = "item_badgable";
    public static final String ATTENDEE_CUSTOM_BARCODE = "custom_barcode";
    public static final String ATTENDEE_UNIQUE_NUMBER = "unique_number";
    public static final String ATTENDEE_BADGE_PRINTSTATUS = "print_status";
    public static final String ATTENDEE_SCANID = "scan_id";




//Old Attendee Table
   /* public static final String ATTENDEE_ID = "Attendee_Id";
    //newly added image feild
    public static final String ATTENDEE_IMAGE = "Attendee_Image";
    public static final String ATTENDEE_COMPANY_LOGO = "Attendee_Company_Logo";
    public static final String ATTENDEE_FIRST_NAME = "Attendee_First_Name";
    public static final String ATTENDEE_LAST_NAME = "Attendee_Last_Name";
    public static final String ATTENDEE_SUFFIX = "Attendee_Suffix";
    public static final String ATTENDEE_PREFIX = "Attendee_Prefix";
    public static final String ATTENDEE_EMAIL_ID = "Attendee_Email_Id";
    public static final String ATTENDEE_COMPANY_ID = "Attendee_Company_Id";
    public static final String ATTENDEE_COMPANY = "Attendee_Company";
    public static final String ATTENDEE_TICKET_STATUS = "Ticket_Status";
    public static final String ATTENDEE_RSVP_STATUS = "RSVP_Status";

    public static final String ATTENDEE_JOB_TILE = "Attendee_Job_Tile";
    public static final String ATTENDEE_MOBILE = "Attendee_Mobile";

    public static final String ATTENDEE_WORK_PHONE = "Attendee_Phone";
    public static final String ATTENDEE_WORK_ADDRESS_1 = "Attendee_Address_1";
    public static final String ATTENDEE_WORK_ADDRESS_2 = "Attendee_Address_2";
    public static final String ATTENDEE_WORK_CITY = "Attendee_City";
    public static final String ATTENDEE_WORK_STATE = "Attendee_State";
    public static final String ATTENDEE_WORK_COUNTRY = "Attendee_Country";
    public static final String ATTENDEE_WORK_ZIPCODE = "Attendee_Zipcode";

    public static final String ATTENDEE_HOME_PHONE = "Attendee_Home_Phone";
    public static final String ATTENDEE_HOME_ADDRESS_1 = "Attendee_Home_Address_1";
    public static final String ATTENDEE_HOME_ADDRESS_2 = "Attendee_Home_Address_2";
    public static final String ATTENDEE_HOME_CITY = "Attendee_Home_City";
    public static final String ATTENDEE_HOME_STATE = "Attendee_Home_State";
    public static final String ATTENDEE_HOME_COUNTRY = "Attendee_Home_Country";
    public static final String ATTENDEE_HOME_ZIPCODE = "Attendee_Home_Zipcode";


    public static final String ATTENDEE_CHECKEDINDATE = "CheckedInDate";
    public static final String ATTENDEE_EVENT_ID = "Event_Id";
    public static final String ATTENDEE_BADGE_NAME = "Attendee_Badge_Name";
    public static final String ATTENDEE_BADGEID = "BadgeId";
    public static final String ATTENDEE_BADGE_PARENT_ID = "Badge_Parent_Id";
    public static final String ATTENDEE_REASON = "Reason";
    public static final String ATTENDEE_ITEM_ID = "Item_Id";
    public static final String ATTENDEE_ITEM_NAME = "Item_Name";
    public static final String ATTENDEE_ITEM_TYPE_ID = "Item_Type_Id";
    public static final String ATTENDEE_ITEM_POOL_ID = "Item_Pool_Id";
    public static final String ATTENDEE_TIKCET_NUMBER = "Tikcet_Number";
    public static final String ATTENDEE_ORDER_NUMBER= "Order_Id_Number";

    // public static final String ATTENDEE_ISCHECKIN = "isCheckin";
    public static final String ATTENDEE_TICKET_SEAT_NUMBER = "Ticket_Seat_Number";
    public static final String ATTENDEE_ORDER_ID = "Order_Id";
    public static final String ATTENDEE_ORDER_ITEM_ID = "Order_Item_Id";
    public static final String ATTENDEE_BUYER_ID = "buyer_id";
    public static final String ATTENDEE_TAG = "tag";
    public static final String ATTENDEE_NOTE = "note";
    // public static final String ATTENDEE_DESIGNATION = "designation";
    public static final String ATTENDEE_BADGE_LABLE = "BadgeLable";
    public static final String ATTENDEE_ITEM_PARENT_ID = "Parent_Id";
    public static final String ATTENDEE_ITEMPOOL_NAME = "item_pool_name";
    public static final String ATTENDEE_ITEMTYPE_NAME = "item_type_name";
    public static final String ATTENDEE_BADGABLE = "item_badgable";
    public static final String ATTENDEE_CUSTOM_BARCODE = "custom_barcode";
    public static final String ATTENDEE_UNIQUE_NUMBER = "unique_number";
    public static final String ATTENDEE_BADGE_PRINTSTATUS = "print_status";
    public static final String ATTENDEE_SCANID = "scan_id";*/


    //Added Ticket/item Details data fields

    public static final String ADDED_ITEM_EVENTID = "EventId";
    public static final String ADDED_ITEM_ID = "ItemId";
    public static final String ADDED_ITEM_NAME = "ItemName";
    public static final String ADDED_ITEM_PRICE = "ItemPrice";
    public static final String ADDED_ITEM_POOLID = "ItemPoolId";
    public static final String ADDED_ITEM_QUANTITY = "ItemQuantity";
    public static final String ADDED_ITEM_ORDER_MAX_QUANTITY = "Item_max_per_order__c";
    public static final String ADDED_ITEM_ORDER_MIN_QUANTITY = "Item_min_per_order__c";
    public static final String ADDED_ITEM_SOLDQUANTITY = "ItemSoldQuantity";
    public static final String ADDED_ITEM_TYPE = "ItemType";
    public static final String ADDED_ITEM_SALESSTARTDATE = "ItemSalesStartDate";
    public static final String ADDED_ITEM_IMAGEURL = "ItemImageUrl";
    public static final String ADDED_ITEM_FEE = "ItemFee";
    public static final String ADDED_ITEM_PAYMENTTYPE = "ItemPaymentType";
    public static final String ADDED_ITEM_SALESENDDATE = "ItemSalesEnddate";
    public static final String ADDED_ITEM_OPTION = "ItemOption";
    public static final String ADDED_ITEM_TYPENAME = "ItemTypeName";
    public static final String ADDED_ITEM_STATUS = "ItemStatus";
    public static final String ADDED_ITEM_SERVICEFEE = "ServiceFee";
    public static final String ADDED_ITEM_IS_TAX_APPLICABLE="isTaxApplicable";
    public static final String ADDED_ITEM_BL_FEE = "Item_BL_Fee";
    public static final String ADDED_ITEM_SA_VISIBILITY = "Item_SA_Visibility";

    public static final String ADDED_ITEM_BADGABLESTATUS = "Item_Badge_status";
    //public static final String ADDED_ITEM_VISIBILITY="Visibility";

    //Ticket Tag data fields

    public static final String TAG_NAME = "tag_name";
    public static final String TAG_TICKET_IT = "ticket_id";//attendee id same as ticket id
    public static final String TAG_ID = "tag_id";
    public static final String TAG_EVENT_ID = "event_id";
    public static final String TAG_TICKET_NAME = "ticket_name";


    //Organizer Payment Details
    public static final String ORGANAIZER_ID = "organizer_id";
    public static final String ORGANAIZER_NAME= "organizer_name";
    public static final String ORGANAIZER_KEY = "PG_Authorization_Key__c";
    public static final String ORGANAIZER_CARD_DIGITS = "PG_Authorization_CC_Last_four_Digit__c";
    public static final String ORGANAIZER_CARD_TYPE = "PG_Authorization_Card_Type__c";
    public static final String ORGANAIZER_CARD_OVERWRITE = "PG_Credit_Card_Overwrite__c";


    //Event Pricing Details
    public static final String PRICING_EVENT_ID = "event_id_pricing";
    public static final String PRICING_ID = "pricing_id";
    public static final String PRICING_NAME = "pricing_name";
    public static final String PRICING_FEE = "pricing_fee";
    public static final String PRICING_PERCENTAGE = "pricing_per";
    public static final String PRICING_MAX_FEE = "pricing_maxfee";
    public static final String PRICING_ITEM_TYPE = "pricing_item_type";
    public static final String PRICING_IS_PRODUCT = "pricing_is_product";


    //Badge Template New Details
    public static final String BADGE_NEW_ID="badge_id";
    public static final String BADGE_NEW_NAME="badge_name";
    public static final String BADGE_NEW_DESC = "badge_desc";
    public static final String BADGE_NEW_DATA ="badge_data";
    public static final String BADGE_NEW_MODULE = "module";
    public static final String BADGE_NEW_EVENT_ID="event_id";
    public static final String BADGE_NEW_IS_SELECTED="badge_isselcted";
    public static final String BADGE_NEW_DEFAULT_BADGE="badge_defaultbadge_selected";

    //Badge Template New Layers
    public static final String LAYER_ID = "layer_id";
    public static final String LAYER_DATA = "layer_data";
    public static final String BADGE_ID = "badge_id";

    //Item Registration Settings
    public static final String REG_ITEM_ID = "reg_item_id";
    public static final String REG_ID = "reg_id";
    public static final String REG_COLUMN_NAME = "reg_column_name";
    public static final String REG_DEFAULT_LABEL = "reg_default_label";
    public static final String REG_INCLUDE = "reg_include";
    public static final String REG_LABEL_NAME = "reg_label_name";
    public static final String REG_REQUIRED = "reg_required";
    public static final String REG_EVENT_ID = "reg_eventid";
    public static final String REG_SETTING_TYPE = "reg_setting_type";
    public static final String REG_GROUP_NAME = "reg_group_name__c";
    public static final String REG_GROUP_LABELNAME = "reg_group_label__c";
    public static final String REG_FIELDTYPE = "reg_fieldtype";

    //public String Group_Name__c="",Group_Label__c="";
    //Scanned Tickets
    public static final String SCANNED_ID = "Id";
    public static final String SCANNED_EVENT_ID = "Event_Id";
    public static final String SCANNED_GNUSER_ID = "GNUser_Id";
    public static final String SCANNED_ITEM_POOL_ID = "Item_Pool_Id";
    public static final String SCANNED_STATUS = "Status";
    public static final String SCANNED_SWITCH = "ScanSwitch";
    public static final String SCANNED_GROUP_ID = "Scan_Group_Id";

    //FREE Seminar Attendees
    public static final String SESSION_ATTENDEE_ID = "session_attendee_id";
    public static final String SESSION_ITEM_POOL_ID = "session_item_pool_id";
    public static final String SESSION_ATTENDEE_TICKET_ID = "session_attendee_ticket_id";
    public static final String SESSION_ATTENDEE_FIRST_NAME = "session_attendee_first_name";
    public static final String SESSION_ATTENDEE_LAST_NAME = "session_attendee_last_name";
    public static final String SESSION_ATTENDEE_EMAIL_ID = "session_attendee_email_id";
    public static final String SESSION_ATTENDEE_COMPANY = "session_attendee_company";
    public static final String SESSION_ATTENDEE_CHECKIN_STATUS = "session_attendee_checkin_status";
    public static final String SESSION_ATTENDEE_SCANTIME = "session_attendee_scantime";
    public static final String SESSION_EVENT_ID = "session_event_id";  		  //extra added fields
    public static final String SESSION_BADGE_LABEL="session_badge_label";
    public static final String SESSION_CUSTOM_BARCODE="session_custom_barcode";
    public static final String SESSION_TKT_JOB_TITLE="session_tkt_job_title";
    public static final String SESSION_MOBILE="session_mobile";
    public static final String SESSION_ITEM_TYPE_NAME="session_item_type_name";
    public static final String SESSION_ITEM_TYPE_ID="session_item_type_id";
    public static final String SESSION_GROUP_ID = "session_group_id";
    public static final String SESSION_GROUP_NAME = "session_group_name";


    //offline scan
    //public static final String OFFLINE_ID="Id";
    public static final String OFFLINE_ITEM_POOL_ID="item_pool_id";
    public static final String OFFLINE_SCAN_TIME="scan_time";
    public static final String OFFLINE_BADGE_ID="badge_id";
    public static final String OFFLINE_EVENT_ID="event_id";
    public static final String OFFLINE_BADGE_STATUS="badge_status";
    public static final String OFFLINE_CHECKIN_STATUS="checkin_status";
    public static final String OFFLINE_USER_ID="user_id";
    public static final String OFFLINE_ERROR_MSG="error";
    public static final String STATUS_OFFLINE="offline";
    public static final String STATUS_ONLINE="online";
    public static final String STATUS_INVALID="invalid";
    public static final String OFFLINE_ATTENDEE_NAME = "offline_attendee_name";
    public static final String OFFLINE_GROUP_ID = "session_group_id";
    public static final String OFFLINE_SCANDEVICE_MODE = "scandevice_mode";


    //Seminar Agenda
    public static final String SEMINAR_ITEM_POOL_ID = "seminar_item_pool_id";
    public static final String SEMINAR_AGENDA_NAME= "seminar_agenda_name";
    public static final String SEMINAR_START_DATE_TIME= "seminar_start_date_time";
    public static final String SEMINAR_END_DATE_TIME= "seminar_end_date_time";
    public static final String SEMINAR_ROOM_NAME= "seminar_room_name";
    public static final String SEMINAR_ROOM_NUMBER= "seminar_room_number";
    public static final String SEMINAR_AGENDA_DESC = "seminar_agenda_desc";

    public static final String SEMINAR="Seminars";

    //Scanned Tickets Group
    public static final String GROUP_ID = "group_id";
    public static final String GROUP_NAME = "group_name";
    public static final String GROUP_EVENT_ID = "group_event_id";
    public static final String GROUP_PRODUCT_TYPE  = "group_product_type";
    public static final String GROUP_SCAN_SWITCH = "group_Scan_Switch";


    //Tstatus
    public static final String T_EVENT_ID = "T_Event_Id";
    public static final String T_TICKET_ID = "Ticket__c";
    public static final String T_SESSION_ITEM_POOL = "BLN_Session_Item__c";
    public static final String T_CHECKIN_STATUS = "Tstatus_name__c";
    public static final String T_SESSION_USER = "BLN_Session_user__c";
    public static final String T_GROUP_ID = "Group_Id";
    public static final String T_GROUP_NAME = "Group_Name";
    public static final String T_ISLATEST = "Islatest__c";
    public static final String T_SCAN_TIME = "T_Scan_Time";
    public static final String T_USER_ID = "T_User_Id";
    public static final String T_GNUSER_ID = "T_GNUSER_ID";


    //Hide_Items Table
    public static final String HIDE_ITEMPOOLID = "Item_Poolid";
    public static final String HIDE_ITEMSTATUS = "Hide_Status";
    public static final String HIDE_ITEM_EVENTID = "Event_id";


    //Picklistvalues Table
    public static final String Picklistvalue = "Picklistvalue";
    public static final String CustomvalueName = "CustomvalueName";
    public static final String List_Description__c = "List_Description__c";
    public static final String List_Code__c = "List_Code__c";
    public static final String CustomvalueID= "Customvalueid";
    public static final String Sort_Order__c = "Sort_Order__c";
    public static final String fieldName = "fieldName";

}
