
package com.globalnest.scanattendee;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.globalnest.brother.ptouch.sdk.printdemo.common.MsgDialog;
import com.globalnest.brother.ptouch.sdk.printdemo.printprocess.ImagePrint;
import com.globalnest.classes.MultiDirectionSlidingDrawer;
import com.globalnest.classes.NonSwipeableViewPager;
import com.globalnest.cropimage.CropImage;
import com.globalnest.cropimage.CropUtil;
import com.globalnest.database.DBFeilds;
import com.globalnest.database.EventPayamentSettings;
import com.globalnest.mvc.BlockTicketListController;
import com.globalnest.mvc.BuyerInfoHandler;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.mvc.ItemTypeController;
import com.globalnest.mvc.OrderDetailsHandler;
import com.globalnest.mvc.OrderItemListHandler;
import com.globalnest.mvc.PaymentObject;
import com.globalnest.mvc.TicketTypeContoller;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.printer.PrinterDetails;
import com.globalnest.printer.ZebraPrinter;
import com.globalnest.retrofit.rest.ApiClient;
import com.globalnest.retrofit.rest.ApiInterface;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Sailakshmi
 *
 */
public class SelfcheckinCollectOrderInfo extends BaseActivity implements OnClickListener{

    private MultiDirectionSlidingDrawer forms_drawer;
    private NonSwipeableViewPager list_forms;
    private FormsAdapter adapter;
    private FormNameAdapter names_adapter;
    private int form_count = 1;
    private ListView list_form_names;
    private HashMap<Integer, String> index_name = new HashMap<Integer, String>();
    private ArrayList<TicketTypeContoller> _ticketcart;
    private BuyerInfoHandler buyerInfo = new BuyerInfoHandler();
    private ArrayList<ItemTypeController> items_list = new ArrayList<ItemTypeController>();
    private ArrayList<OrderItemListHandler> order_line_items = new ArrayList<OrderItemListHandler>();
    private ArrayList<OrderItemListHandler> buyer_order_line_items = new ArrayList<OrderItemListHandler>();
    private ArrayList<OrderItemListHandler> package_order_line_items = new ArrayList<OrderItemListHandler>();
    private ArrayList<BlockTicketListController> ticketname_for_packageitems = new ArrayList<BlockTicketListController>();
    //OrderItemListHandler buyer_order_line_item = new OrderItemListHandler();
    private OrderDetailsHandler order_detail = new OrderDetailsHandler();
    private String access_key = "collect";//custombarcode_scanned_valule="";
    private int slected_check = -1;
    //String item_id;
    private HashMap<String, ArrayList<BlockTicketListController>> block_ticket_map;
    //private ArrayList<ArrayList<String>> _tarraylis = new ArrayList<ArrayList<String>>();
    //private ArrayList<ArrayList<String>> _tarraylis2 = new ArrayList<ArrayList<String>>();
    private double total = 0.0, servicefee=0,servicetax=0;
    private int index = 0;
    //int i = 0;
    private String ticket_setting = "",requestType="";
    boolean is_collectinfofromBuyer=false;
    private ExternalSettings ext_settings;
    private boolean isErrorBarcode=false;
    ArrayList<Bitmap> bitmapArrayList=new ArrayList<Bitmap>();
    int formPosition =0;
    Bitmap attendee_photo;
    ArrayList<AttendeeImage> listImagAttendees = new ArrayList<AttendeeImage>();
    ImageView img_attendee;
    public static String BuyerAttendeeImage="";
    public boolean buyerimage=false;
    ProgressDialog progressDialog;

    @Override
    public void onClick(View v) {
        if (v == img_search) {
            Intent i = new Intent(SelfcheckinCollectOrderInfo.this, SearchAttendeeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(Util.ACCESS_KEY_COLLECT_ORDER_INFO, Util.COLLECT_ORDER_INFO);
            i.putExtra(Util.CART_TICKETS, _ticketcart);
            i.putExtra(Util.TOTAL, total);
            i.putExtra(Util.SERVICE_FEE, servicefee);
            i.putExtra(Util.SERVICE_TAX, servicetax);
            i.putExtra(Util.INTENT_KEY_1, buyerInfo);
            //For Data transfer
            String json_string = new Gson().toJson(order_line_items);
            String json_items = new Gson().toJson(items_list);
            Util.order_request.edit().putString(Util.ORDER_REQUEST_STRING, json_string).commit();
            Util.order_Items.edit().putString(Util.ORDER_ITEMS_STRING, json_items).commit();
            //i.putExtra(Util.INTENT_KEY_2, order_line_items);
            //i.putExtra(Util.INTENT_KEY_3, items_list);
            i.putExtra(Util.INTENT_KEY_4, buyer_order_line_items);
            i.putExtra(Util.FORMCOUNT, form_count);
            i.putExtra(Util.COPY_VALUES, index_name);
            i.putExtra(Util.TICKETSETTING, ticket_setting);
            i.putExtra(Util.INDEX, list_forms.getCurrentItem());
            i.putExtra(Util.BLOCK_TICKET_MAP, block_ticket_map);
            i.putExtra(Util.BLOCK_PACKAGE_TICKET, ticketname_for_packageitems);
            i.putExtra(Util.ISCOLLECT_INFO_FROM_BUYER, is_collectinfofromBuyer);
            i.putExtra("FormPosition", formPosition);
            startActivity(i);
            finish();
        } /*else if (v == img_attendee) {
			openTakeFromDialg(CollectOrderInfo.this);
		}*/
    }

    class AttendeeImage{
        ImageView img;
        Bitmap bitmap;
    }
    Cursor attendee_cursor;
    private ArrayList<String> mFiles = new ArrayList<String>();
    private String transaction_id = "",card_last_4="", check_number = "",order_status="",order_id="",payment_gateway_name="",order_name="";
    private TotalOrderListHandler totalorderlisthandler;
    int sellticketscount=0;
    //private boolean issingleticket=false,copybuyerinfofor1stform=false;
    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.collect_order_info);
        _ticketcart = (ArrayList<TicketTypeContoller>) this.getIntent().getSerializableExtra(Util.CART_TICKETS);
        //issingleticket =this.getIntent().getBooleanExtra(Util.ONLY1FORM,false);
        //copybuyerinfofor1stform =this.getIntent().getBooleanExtra(Util.COPY1STFORM,false);
        access_key = this.getIntent().getStringExtra(Util.ACCESS_KEY_ATTENDEE_DETAIL);
		/*if(Util.NullChecker(access_key).equals(Util.ATTENDEE_DETAIL)){
			formPosition=this.getIntent().getExtras().getInt("FormPosition");
		}*/
        total = this.getIntent().getDoubleExtra(Util.TOTAL, 0.0);
        Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
        ext_settings = new ExternalSettings();
        if(!Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, "").isEmpty()){
            ext_settings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, ""), ExternalSettings.class);
        }

        servicefee = this.getIntent().getDoubleExtra(Util.SERVICE_FEE, 0.0);
        servicetax = this.getIntent().getDoubleExtra(Util.SERVICE_TAX, 0.0);

        index = this.getIntent().getIntExtra(Util.INDEX, 0);
        ticket_setting = this.getIntent().getStringExtra(Util.TICKETSETTING);
        block_ticket_map = (HashMap<String, ArrayList<BlockTicketListController>>) this.getIntent().getSerializableExtra(Util.BLOCK_TICKET_MAP);
        ticketname_for_packageitems = (ArrayList<BlockTicketListController>)this.getIntent().getSerializableExtra(Util.BLOCK_PACKAGE_TICKET);
        is_collectinfofromBuyer = this.getIntent().getBooleanExtra(Util.ISCOLLECT_INFO_FROM_BUYER, false);
        for(BlockTicketListController block_ticket : ticketname_for_packageitems){

            OrderItemListHandler order_line_item = new OrderItemListHandler();
            order_line_item.setItemId(block_ticket.Item__c);
            order_line_item.setItemPoolId(block_ticket.Item_Pool__c);
            order_line_item.setItemTypeId(block_ticket.Item_Pool__r.Item_Type__r.Id);

            // //Log.i("---------Collect Order
            // Info------------",": Block ticket item=
            // "+_tarraylis.get(i)+"Total quentity="+qty);
            order_line_item.setTicketNumber(block_ticket.Name);
            order_line_item.setItemTypeName(block_ticket.Item_Pool__r.Item_Pool_Name__c);
            if(TextUtils.isEmpty(block_ticket.Parent_ID__c)){
                order_line_item.setPackageTicketName(block_ticket.Name);
                order_line_item.setTicketStatus(ticket_setting);
            }else{
                order_line_item.setTicketStatus(block_ticket.Item_Pool__r.Ticket_Settings__c);
            }
            order_line_item.setTicketParentId(block_ticket.Parent_ID__c);
            order_line_item.Item_Type__r.Name = block_ticket.Item_Type__r.Name;
            order_line_item.Item_Pool__r.Ticket_Settings__c = block_ticket.Item_Pool__r.Ticket_Settings__c;
            order_line_item.Item_Pool__r.Badgable__c = block_ticket.Item_Pool__r.Badgable__c;
            order_line_item.setId(block_ticket.Id);
            package_order_line_items.add(order_line_item);
        }

        if (Util.NullChecker(access_key).equals(Util.ATTENDEE_DETAIL)) {

            String json_string = Util.order_request.getString(Util.ORDER_REQUEST_STRING, "");
            String json_items = Util.order_Items.getString(Util.ORDER_ITEMS_STRING, "");
            if(!json_string.isEmpty()){
                Type type = new TypeToken<List<OrderItemListHandler>>(){}.getType();
                order_line_items = new Gson().fromJson(json_string, type);
            }
            if(!json_items.isEmpty()){
                Type type = new TypeToken<List<ItemTypeController>>(){}.getType();
                items_list = new Gson().fromJson(json_items, type);
            }
            //order_line_items = (ArrayList<OrderItemListHandler>) this.getIntent().getSerializableExtra(Util.INTENT_KEY_2);
            //items_list = (ArrayList<ItemTypeController>) this.getIntent().getSerializableExtra(Util.INTENT_KEY_3);
            buyerInfo=(BuyerInfoHandler) this.getIntent().getSerializableExtra(Util.INTENT_KEY_1);
            buyer_order_line_items=(ArrayList<OrderItemListHandler>) this.getIntent().getSerializableExtra(Util.INTENT_KEY_4);
            form_count = this.getIntent().getIntExtra(Util.FORMCOUNT, 0);
            index_name = (HashMap<Integer, String>) this.getIntent().getSerializableExtra(Util.COPY_VALUES);
            formPosition=this.getIntent().getExtras().getInt("FormPosition");
        } else {
            for (TicketTypeContoller ticket_controller : _ticketcart) {

                String where_condition = " where EventId='"	+ checked_in_eventId + "' AND ItemId='" + ticket_controller.getTicketsId() + "'";
                // //Log.i("----------------Where Condition---------------",":"+where_condition);
                Cursor c = Util.db.getTicketCursor(where_condition);
                //Log.i("----------------Cursor---------------",":"+c.getCount());
                if (c != null) {
                    c.moveToFirst();
                    ItemTypeController item = new ItemTypeController();
                    item.setItemId(c.getString(c.getColumnIndex("ItemId")));
                    item.setItemName(c.getString(c.getColumnIndex("ItemName")));
                    item.setItemPrice(c.getDouble(c.getColumnIndex("ItemPrice")));
                    // item.setItemQuantity(c.getInt(c.getColumnIndex("ItemQuantity")));
                    item.setItemQuantity(Integer.parseInt(ticket_controller.getSelectedTickets()));
                    item.setItemPoolId(c.getString(c.getColumnIndex("ItemPoolId")));
                    item.setItemTypeName(c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_TYPENAME)));
                    item.setItemTypeId(c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_TYPE)));
                    item.setItemFeeSetting(ticket_controller.getTicketFeeSetting());
                    item.setItemPaidType(ticket_controller.getTicketPaymentType());
					/*String parent_id = Util.db.getItemPoolParentId(item_pool_id, checked_in_eventId);
					if(!NullChecker(parent_id).isEmpty()) {}*/
                    if(ticket_controller.getTicketFeeSetting().equals("false")&&(ticket_controller.getTicketPaymentType().equalsIgnoreCase("Paid") || ticket_controller.getTicketPaymentType().equalsIgnoreCase("Donation"))){
                        Double ti=(Double.valueOf(Double.toString(c.getDouble(c.getColumnIndex("ItemPrice")))))-Double.valueOf(Double.toString(Util.db.getItemFee(checked_in_eventId,c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_ID)))));
                        item.setBL_Fee(Double.toString(Double.valueOf(Double.toString(Util.db.getItemFee(checked_in_eventId,c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_ID)))))));
                    }else {
                        item.setBL_Fee(Double.toString(Util.db.getItemFee(checked_in_eventId,c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_ID)))));
                        //item.setBL_Fee(ticket_controller.getBLFeeAmount());
                    }
                    items_list.add(item);
                    ticket_setting = c.getString(c.getColumnIndex("ItemOption"));

                    if(ticket_setting.equalsIgnoreCase(getString(R.string.infofrombuyer))){
                        for(BlockTicketListController block_ticket : block_ticket_map.get(item.getItemId())){
                            //Log.i("----------------Order Item Name---------------", ":"+block_ticket.Name);
                            is_collectinfofromBuyer=true;
                            OrderItemListHandler buyer_order_line_item = new OrderItemListHandler();
                            buyer_order_line_item.setItemId(item.getItemId());
                            buyer_order_line_item.setItemPoolId(item.getItemPoolId());
                            buyer_order_line_item.setItemTypeId(block_ticket.Item_Pool__r.Item_Type__r.Id);
                            buyer_order_line_item.setTicketStatus(ticket_setting);
                            // //Log.i("---------Collect Order Info------------",": Block ticket item= "+_tarraylis.get(i)+"Total quentity="+qty);
                            buyer_order_line_item.setTicketNumber(block_ticket.Name);
                            buyer_order_line_item.setTicketParentId(block_ticket.Parent_ID__c);
                            buyer_order_line_item.Item_Type__r.Name = block_ticket.Item_Type__r.Name;
                            buyer_order_line_item.Item_Pool__r.Ticket_Settings__c = block_ticket.Item_Pool__r.Ticket_Settings__c;
                            buyer_order_line_item.Item_Pool__r.Badgable__c = block_ticket.Item_Pool__r.Badgable__c;
                            buyer_order_line_items.add(buyer_order_line_item);
                        }

                    }else if(ticket_setting.equalsIgnoreCase(getString(R.string.infofromattendee))){

                        if (!item.getItemTypeName().equalsIgnoreCase("Package")) {
                            for (BlockTicketListController block_ticket : block_ticket_map.get(item.getItemId())) {
                                //if(!issingleticket){
                                form_count = form_count + 1;
                                //}
                                OrderItemListHandler order_line_item = new OrderItemListHandler();
                                order_line_item.setItemId(item.getItemId());
                                order_line_item.setItemPoolId(item.getItemPoolId());
                                order_line_item.setItemTypeId(item.getItemName());
                                order_line_item.setTicketStatus(ticket_setting);
                                // //Log.i("---------Collect Order
                                // Info------------",": Block ticket item=
                                // "+_tarraylis.get(i)+"Total quentity="+qty);
                                order_line_item.setTicketNumber(block_ticket.Name);
                                order_line_item.setTicketParentId(block_ticket.Parent_ID__c);
                                order_line_item.Item_Type__r.Name = block_ticket.Item_Type__r.Name;
                                order_line_item.Item_Pool__r.Ticket_Settings__c = block_ticket.Item_Pool__r.Ticket_Settings__c;
                                //order_line_item.Item_Pool__r.BadgeStatus="false";
                                order_line_item.Item_Pool__r.Badgable__c = block_ticket.Item_Pool__r.Badgable__c;
                                order_line_items.add(order_line_item);
                            }
                        }else{
                            //form_count = form_count+Integer.valueOf(ticket_controller.getSelectedTickets());
                            for (BlockTicketListController block_ticket : block_ticket_map.get(item.getItemId())) {
                                form_count = form_count + 1;
                                OrderItemListHandler order_line_item = new OrderItemListHandler();
                                order_line_item.setItemId(item.getItemId());
                                order_line_item.setItemPoolId(item.getItemPoolId());
                                order_line_item.setItemTypeId(block_ticket.Item_Pool__r.Item_Type__r.Id);

                                // //Log.i("---------Collect Order
                                // Info------------",": Block ticket item=
                                // "+_tarraylis.get(i)+"Total quentity="+qty);
                                order_line_item.setTicketNumber(block_ticket.Name);
                                order_line_item.setItemTypeName(item.getItemTypeName());
                                if(TextUtils.isEmpty(block_ticket.Parent_ID__c)){
                                    order_line_item.setPackageTicketName(block_ticket.Name);
                                    order_line_item.setTicketStatus(ticket_setting);
                                }else{
                                    order_line_item.setTicketStatus(block_ticket.Item_Pool__r.Ticket_Settings__c);
                                }
                                order_line_item.setTicketParentId(block_ticket.Parent_ID__c);
                                order_line_item.Item_Type__r.Name = block_ticket.Item_Type__r.Name;
                                order_line_item.Item_Pool__r.Ticket_Settings__c = block_ticket.Item_Pool__r.Ticket_Settings__c;
                                order_line_item.Item_Pool__r.Badgable__c = block_ticket.Item_Pool__r.Badgable__c;
                                order_line_item.setId(block_ticket.Id);
                                order_line_items.add(order_line_item);
                            }
                        }
                    }//sai change
                    else if(ticket_setting.equalsIgnoreCase(getString(R.string.donotinfofromattendee))){
                        if (item.getItemTypeName().equalsIgnoreCase("Package")) {
                            for (BlockTicketListController block_ticket : block_ticket_map.get(item.getItemId())) {
                                if(block_ticket.Item_Pool__r.Ticket_Settings__c.equalsIgnoreCase(getString(R.string.infofrombuyer))){
                                    is_collectinfofromBuyer=true;
                                    OrderItemListHandler buyer_order_line_item = new OrderItemListHandler();
                                    buyer_order_line_item.setItemId(item.getItemId());
                                    buyer_order_line_item.setItemPoolId(item.getItemPoolId());
                                    buyer_order_line_item.setItemTypeId(block_ticket.Item_Pool__r.Item_Type__r.Id);
                                    buyer_order_line_item.setTicketStatus(block_ticket.Item_Pool__r.Ticket_Settings__c);
                                    // //Log.i("---------Collect Order Info------------",": Block ticket item= "+_tarraylis.get(i)+"Total quentity="+qty);
                                    buyer_order_line_item.setTicketNumber(block_ticket.Name);
                                    buyer_order_line_item.setTicketParentId(block_ticket.Parent_ID__c);
                                    buyer_order_line_item.Item_Type__r.Name = block_ticket.Item_Type__r.Name;
                                    buyer_order_line_item.Item_Pool__r.Ticket_Settings__c = block_ticket.Item_Pool__r.Ticket_Settings__c;
                                    buyer_order_line_item.Item_Pool__r.Badgable__c = block_ticket.Item_Pool__r.Badgable__c;
                                    //buyer_order_line_item.setBadgeLabel(block_ticket.Item_Pool__r.Badgable__c);
                                    buyer_order_line_items.add(buyer_order_line_item);

									/*OrderItemListHandler buyer_order_line_item = new OrderItemListHandler();
									buyer_order_line_item.setItemId(item.getItemId());
									buyer_order_line_item.setItemPoolId(item.getItemPoolId());
									buyer_order_line_item.setItemTypeId(item.getItemName());
									buyer_order_line_item.setTicketStatus(block_ticket.Item_Pool__r.Ticket_Settings__c);
									// //Log.i("---------Collect Order Info------------",": Block ticket item= "+_tarraylis.get(i)+"Total quentity="+qty);
									buyer_order_line_item.setTicketNumber(block_ticket.Name);
									buyer_order_line_items.add(buyer_order_line_item);*/

                                }else if(block_ticket.Item_Pool__r.Ticket_Settings__c.equalsIgnoreCase(getString(R.string.infofromattendee))){
                                    form_count = form_count + 1;
                                    OrderItemListHandler order_line_item = new OrderItemListHandler();
                                    order_line_item.setItemId(item.getItemId());
                                    order_line_item.setItemPoolId(item.getItemPoolId());
                                    order_line_item.setItemTypeId(block_ticket.Item_Pool__r.Item_Type__r.Id);
                                    order_line_item.setTicketNumber(block_ticket.Name);
                                    order_line_item.setPackageTicketName(block_ticket.Item_Pool__r.Item_Pool_Name__c);
                                    order_line_item.setItemTypeName(item.getItemTypeName());
                                    order_line_item.setTicketStatus(ticket_setting);
                                    order_line_item.setTicketParentId(block_ticket.Parent_ID__c);
                                    order_line_item.Item_Type__r.Name = block_ticket.Item_Type__r.Name;
                                    order_line_item.Item_Pool__r.Ticket_Settings__c = block_ticket.Item_Pool__r.Ticket_Settings__c;
                                    order_line_item.Item_Pool__r.Badgable__c = block_ticket.Item_Pool__r.Badgable__c;

                                    // //Log.i("---------Collect Order Info------------",": Block ticket item= "+_tarraylis.get(i)+"Total quentity="+qty);
                                    //order_line_item.setTicketNumber(block_ticket.Item_Pool__r.Item_Pool_Name__c);
                                    order_line_items.add(order_line_item);
                                }else if(block_ticket.Item_Pool__r.Ticket_Settings__c.equalsIgnoreCase(getString(R.string.donotinfofromattendee))){
                                    is_collectinfofromBuyer=true;
                                    OrderItemListHandler buyer_order_line_item = new OrderItemListHandler();
                                    buyer_order_line_item.setItemId(item.getItemId());
                                    buyer_order_line_item.setItemPoolId(item.getItemPoolId());
                                    buyer_order_line_item.setItemTypeId(block_ticket.Item_Pool__r.Item_Type__r.Id);
                                    buyer_order_line_item.setTicketStatus(block_ticket.Item_Pool__r.Ticket_Settings__c);
                                    // //Log.i("---------Collect Order Info------------",": Block ticket item= "+_tarraylis.get(i)+"Total quentity="+qty);
                                    buyer_order_line_item.setTicketNumber(block_ticket.Name);
                                    buyer_order_line_item.setTicketParentId(block_ticket.Parent_ID__c);
                                    buyer_order_line_item.Item_Type__r.Name = block_ticket.Item_Type__r.Name;
                                    buyer_order_line_item.Item_Pool__r.Ticket_Settings__c = block_ticket.Item_Pool__r.Ticket_Settings__c;
                                    buyer_order_line_item.Item_Pool__r.Badgable__c = block_ticket.Item_Pool__r.Badgable__c;
                                    //buyer_order_line_item.setBadgeLabel(block_ticket.Item_Pool__r.Badgable__c);
                                    buyer_order_line_items.add(buyer_order_line_item);}

                            }
                        }
                    }
                    c.close();
                    // }
                }

                //position++;
            }
        }
        adapter = new FormsAdapter(form_count);

        for(int i =0 ; i<form_count ; i++){
            listImagAttendees.add(null);
        }
        list_forms.setEnabled(false);
        list_forms.setAdapter(adapter);

        list_forms.setCurrentItem(index,false);
        names_adapter = new FormNameAdapter();
        list_form_names.setAdapter(names_adapter);

        list_forms.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrolled(int position, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }
        });


        list_form_names.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //
                // //Log.i("----------------on Item Click------------",":"+position);

                int current_page = list_forms.getCurrentItem();
                if (position == 0) {
                    access_key = "null";
                    // //Log.i("----------------on Item Click------------",":"+"if"+buyerInfo.getFirstName());

                    order_line_items.get(current_page - 1).setFirstName(buyerInfo.getFirstName());
                    order_line_items.get(current_page - 1).setLastName(buyerInfo.getLastName());
                    order_line_items.get(current_page - 1).setEmail(buyerInfo.getEmail());
                    order_line_items.get(current_page - 1).setCompanyName(buyerInfo.getCompany());
                    order_line_items.get(current_page-1).setTag(buyerInfo.getTag());
                    order_line_items.get(current_page-1).setMobile(buyerInfo.getMobile());
                    order_line_items.get(current_page-1).setBadgeLabel(buyerInfo.getBadge_lable());
                    order_line_items.get(current_page-1).setSeatno("");
                    order_line_items.get(current_page-1).setDesignation(buyerInfo.getDesignation());
                    order_line_items.get(current_page-1).setnote(buyerInfo.getnote());
                    if(is_collectinfofromBuyer){
                        order_line_items.get(current_page - 1).setAttendeeImage(order_line_items.get(position).getUserImage());
                    }
                    // adapter.notifyDataSetChanged();
                } else {
                    // //Log.i("----------------on Item Click------------",":"+"else");
                    access_key = "null";

                    order_line_items.get(current_page - 1).setAttendeeImage(order_line_items.get(position - 1).getUserImage());
                    //Util.db.getimagedata(Util.db.getByteArray(attendee_photo))
                    order_line_items.get(current_page - 1).setFirstName(order_line_items.get(position - 1).getFirstName());
                    order_line_items.get(current_page - 1).setLastName(order_line_items.get(position - 1).getLastName());
                    order_line_items.get(current_page - 1).setEmail(order_line_items.get(position - 1).getEmail());
                    order_line_items.get(current_page - 1).setCompanyName(order_line_items.get(position - 1).getCompanyName());
                    order_line_items.get(current_page-1).setTag(order_line_items.get(position - 1).getTag());
                    order_line_items.get(current_page-1).setMobile(order_line_items.get(position - 1).getMobile());
                    order_line_items.get(current_page-1).setBadgeLabel(order_line_items.get(position - 1).getBadgeLabel());
                    order_line_items.get(current_page-1).setSeatno("");
                    order_line_items.get(current_page-1).setDesignation(order_line_items.get(position - 1).getDesignation());
                    order_line_items.get(current_page-1).setnote(order_line_items.get(position - 1).getnote());
                    // adapter.notifyDataSetChanged();
                }
                slected_check = position;
                list_forms.setAdapter(adapter);
                list_forms.setCurrentItem(current_page);
                names_adapter.notifyDataSetChanged();
                forms_drawer.animateClose();

            }
        });
        back_layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openWaringAlert();
            }
        });

        //forms_drawer.setonlock();


    }

    /*
     * (non-Javadoc)
     *
     * @see com.globalnest.scanattendee.BaseActivity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
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
        // back_layout.setOnClickListener(this);
        txt_title.setText("Order Info");
        img_menu.setImageResource(R.drawable.back_button);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        img_search.setVisibility(View.VISIBLE);
        list_forms = (NonSwipeableViewPager) linearview.findViewById(R.id.list_forms);
        //list_forms.setS
        list_forms.setOverScrollMode(ViewPager.OVER_SCROLL_NEVER);
        forms_drawer = (MultiDirectionSlidingDrawer) linearview.findViewById(R.id.forms_drawer);
        list_form_names = (ListView) forms_drawer.findViewById(R.id.list_form_names);
        img_search.setOnClickListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.globalnest.network.IPostResponse#doRequest()
     */

    private class FormsAdapter extends PagerAdapter {

        View v;
        int form_count = 1;
        String fname = "", lname = "", email = "", company = "",attendeeimg="";

        String str_designation="",str_tag="",str_phone="",str_badge_label="",str_seat_no="",srt_note="",str_custom_barcode="";

        public FormsAdapter(int no_forms) {
            this.form_count = no_forms;
        }

        @Override
        public int getCount() {

            return form_count;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(View collection, final int position) {

            try{
                // //Log.i("---------Collect Order Info------------",":instantiateItem Position= "+position+" Index= "+index);
                v = inflater.inflate(R.layout.selfcheckin_form_item, null);
                TextView txt_title = (TextView) v.findViewById(R.id.self_txt_form_title);
                final ExpandablePanel extand_panel = (ExpandablePanel) v.findViewById(R.id.expand_panel);
                extand_panel.Expanded(true);
                AttendeeImage attendeeImg = new AttendeeImage();
                attendeeImg.img = (ImageView) v.findViewById(R.id.self_imgattendeepic);
                listImagAttendees.set(position, attendeeImg);
                img_attendee = (ImageView) v.findViewById(R.id.self_imgattendeepic);
                final TextView txttotal_payable = (TextView) v.findViewById(R.id.txttotal_payable);
                final EditText self_edt_fname = (EditText) v.findViewById(R.id.self_edt_form_fname);
                final EditText self_edt_lname = (EditText) v.findViewById(R.id.self_edt_form_lname);
                final EditText self_edt_email = (EditText) v.findViewById(R.id.self_edt_form_email);
                final EditText self_edt_company = (EditText) v.findViewById(R.id.self_edt_form_company);
                final EditText self_edt_designation = (EditText) v.findViewById(R.id.self_edt_att_designation);
                final EditText self_edt_phone = (EditText) v.findViewById(R.id.self_edt_att_phone);
                //final EditText self_edt_tag = (EditText) v.findViewById(R.id.self_edt_att_tag);
                final EditText self_edt_seat_no = (EditText) v.findViewById(R.id.self_edt_att_seatno);
                final EditText self_edt_badge_lable = (EditText) v.findViewById(R.id.self_edt_att_badge);
                final FrameLayout frame_barcode = (FrameLayout) v.findViewById(R.id.frame_barcode);
                final ScrollView scrollView = (ScrollView) v.findViewById(R.id.formscrollView);
                Double totalvalue=total+servicefee+servicetax;
                //Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+String.format("%.2f",totalvalue)
                txttotal_payable.setText("Price Total :"+Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+String.format("%.2f",totalvalue));
                scrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //replace this line to scroll up or down
                        scrollView.fullScroll(ScrollView.FOCUS_UP);
                    }
                }, 100L);
                //scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                frame_barcode.setVisibility(View.GONE);
                // final EditText self_edt_custombarcode = (EditText) v.findViewById(R.id.self_edt_form_custombarcode);
                ////Log.i("------------Custom Barcode in adapter-------------",":"+custombarcode_scanned_valule);
                //self_edt_custombarcode.setText(custombarcode_scanned_valule);
                //self_edt_badge_lable.setText(getString(R.string.attendee));
                final TextView txt_badge_info = (TextView) v.findViewById(R.id.txt_badge_info);
                //final EditText self_edt_note = (EditText) v.findViewById(R.id.self_edt_attreg_note);
                Button btn_previous = (Button) v.findViewById(R.id.btnself_form_previous);
                Button btn_next = (Button) v.findViewById(R.id.btnself_form_next);
                Button btn_copy = (Button) v.findViewById(R.id.btn_copy);
                Button btn_barcode = (Button) v.findViewById(R.id.btn_barcode);
                //ExpandablePanel panel=new ExpandablePanel(SelfcheckinCollectOrderInfo.this);


                // index=position;

                if (position == 0) {

                    //Log.i("---------Collect Order Info------------",":else buyer info Position= " + position+ " Index= " + index);
                    extand_panel.setVisibility(View.GONE);
                    btn_copy.setVisibility(View.GONE);
                    txt_title.setText("Buyer Info");
                    btn_previous.setVisibility(View.GONE);
                    //img_attendee.setVisibility(View.GONE);
                    self_edt_fname.setText(buyerInfo.getFirstName());
                    self_edt_lname.setText(buyerInfo.getLastName());
                    self_edt_email.setText(buyerInfo.getEmail());
                    self_edt_company.setText(buyerInfo.getCompany());
                    self_edt_phone.setText(buyerInfo.getMobile());
                    //self_edt_custombarcode.setText(buyerInfo.getCustomBarcode());
                    //if(is_collectinfofromBuyer||issingleticket){
                    if(is_collectinfofromBuyer){
                        self_edt_designation.setText(buyerInfo.getDesignation());
                    }
					/*self_edt_fname.setText(user_profile.Profile.First_Name__c);
					self_edt_lname.setText(user_profile.Profile.Last_Name__c);
					self_edt_email.setText(user_profile.Profile.Email__c);
					self_edt_company.setText(user_profile.Profile.Company_Name__c);
					self_edt_phone.setText(user_profile.Profile.Mobile__c);
					self_edt_custombarcode.setText(buyerInfo.getCustomBarcode());
					self_edt_designation.setText(user_profile.designation);*/

                    //newly added badgeable fields
					/*self_edt_tag.setText(buyerInfo.getTag());
					if(!NullChecker(buyerInfo.getBadge_lable()).isEmpty()){
					  self_edt_badge_lable.setText(buyerInfo.getBadge_lable());
					}
					self_edt_seat_no.setText(buyerInfo.getSeatno());
					self_edt_designation.setText(buyerInfo.getDesignation());
					self_edt_note.setText(buyerInfo.getnote());*/
                    //if (is_collectinfofromBuyer||issingleticket) {
                    if (is_collectinfofromBuyer) {
                        img_attendee.setVisibility(View.VISIBLE);
                        extand_panel.setVisibility(View.VISIBLE);
                        self_edt_badge_lable.setText(NullChecker(Util.db.getItemPoolBadgeLabel(buyer_order_line_items.get(position).getItemPoolId(),checked_in_eventId)));

                        if (ext_settings.custom_barcode) {
                            frame_barcode.setVisibility(View.VISIBLE);
                        }
                    } else {
                        img_attendee.setVisibility(View.GONE);
                        extand_panel.setVisibility(View.GONE);
                        frame_barcode.setVisibility(View.GONE);
                    }
                } else {
                    extand_panel.setVisibility(View.VISIBLE);
                    if (ext_settings.custom_barcode) {
                        frame_barcode.setVisibility(View.VISIBLE);
                    }


                    if (order_line_items.get(position - 1).getItemTypeName().equalsIgnoreCase("Package")) {
                        if (!TextUtils.isEmpty(order_line_items.get(position - 1).getPackageTicketName())) {
                            if (order_line_items.get(position - 1).Item_Pool__r.Badgable__c.equals("B - Badge")) {
                                img_attendee.setVisibility(View.VISIBLE);
                                self_edt_badge_lable.setText(Util.db.getItemPoolBadgeLabel(order_line_items.get(position - 1).getItemPoolId(),checked_in_eventId));
                            } else {
                                img_attendee.setVisibility(View.GONE);
                                self_edt_badge_lable.setVisibility(View.GONE);
                            }
                            String item_pool_name = Util.db.getItem_Pool_Name(order_line_items.get(position - 1).getItemPoolId(), checked_in_eventId);

                            txt_title.setText(item_pool_name + "\nAttendee Info  ("
                                    + order_line_items.get(position - 1).getPackageTicketName() + ")");
                        } else {
                            for (OrderItemListHandler order_items : order_line_items) {
                                if (order_line_items.get(position - 1).getTicketParentId().equalsIgnoreCase(order_items.getId())) {
                                    if (order_line_items.get(position - 1).Item_Pool__r.Badgable__c.equals("B - Badge")) {
                                        img_attendee.setVisibility(View.VISIBLE);
                                        self_edt_badge_lable.setText(Util.db.getItemPoolBadgeLabel(order_line_items.get(position - 1).getItemPoolId(),checked_in_eventId));
                                    } else {
                                        img_attendee.setVisibility(View.GONE);
                                        self_edt_badge_lable.setVisibility(View.GONE);
                                    }
                                    String item_pool_name = Util.db.getItem_Pool_Name(order_items.getItemPoolId(), checked_in_eventId);
                                    txt_title.setText(item_pool_name + "\nAttendee Info  ("
                                            + order_items.getPackageTicketName() + ")");

                                    break;
                                }
                            }
                        }
                    } else {
                        String[] _tname = order_line_items.get(position - 1).getTicketNumber().split(" ");
                        for (int k = 0; k < order_line_items.size(); k++) {
                            if (order_line_items.get(position - 1).getTicketStatus()
                                    .equalsIgnoreCase(getString(R.string.infofromattendee))) {
                                for (int i = 0; i < _tname.length; i++) {
                                    // //Log.i("---------Collect Order
                                    // Info------------","Ticket name of
                                    // position"+order_line_items.get(position-1).getTicketNumber()+"
                                    // Ticket name is="+_tname[i]);
                                    if (order_line_items.get(position - 1).Item_Pool__r.Badgable__c.equals("B - Badge")) {
                                        img_attendee.setVisibility(View.VISIBLE);
                                        self_edt_badge_lable.setText(Util.db.getItemPoolBadgeLabel(order_line_items.get(position - 1).getItemPoolId(),checked_in_eventId));

                                    } else {
                                        img_attendee.setVisibility(View.GONE);
                                        self_edt_badge_lable.setVisibility(View.GONE);
                                    }
                                    txt_title.setText(order_line_items.get(position - 1).getItemTypeId()
                                            + " Attendee Info  (" + _tname[i] + ")");
                                }
                            }
                        }
                    }

                    String url[] = sfdcddetails.instance_url.split("/");
                    Picasso.with(SelfcheckinCollectOrderInfo.this).load(order_line_items.get(position - 1).getUserImage())
                            .placeholder(R.drawable.default_image)
                            .error(R.drawable.default_image).into(img_attendee);
                    //img_attendee.setImageBitmap(order_line_items.get(position-1).);
					/*if(formPosition==1&&copybuyerinfofor1stform) {
						self_edt_fname.setText(order_line_items.get(position - 1).getFirstName());
						self_edt_lname.setText(order_line_items.get(position - 1).getLastName());
						self_edt_email.setText(order_line_items.get(position - 1).getEmail());
						self_edt_company.setText(order_line_items.get(position - 1).getCompanyName());
					}*/
                    self_edt_fname.setText(order_line_items.get(position - 1).getFirstName());
                    self_edt_lname.setText(order_line_items.get(position - 1).getLastName());
                    self_edt_email.setText(order_line_items.get(position - 1).getEmail());
                    self_edt_company.setText(order_line_items.get(position - 1).getCompanyName());
                    self_edt_phone.setText(order_line_items.get(position - 1).getMobile());
                    // self_edt_custombarcode.setText(order_line_items.get(position - 1).getCustomBarCode());

                }
                //img_attendee.setOnClickListener(SelfcheckinCollectOrderInfo.this);
                btn_next.setTypeface(Util.roboto_regular);
                btn_previous.setTypeface(Util.roboto_regular);
                txt_title.setTypeface(Util.roboto_regular);
                self_edt_fname.setTypeface(Util.roboto_regular);
                self_edt_lname.setTypeface(Util.roboto_regular);
                self_edt_email.setTypeface(Util.roboto_regular);
                self_edt_company.setTypeface(Util.roboto_regular);

                self_edt_designation.setTypeface(Util.roboto_regular);
                self_edt_phone.setTypeface(Util.roboto_regular);
                //self_edt_tag.setTypeface(Util.roboto_regular);
                self_edt_badge_lable.setTypeface(Util.roboto_regular);
                self_edt_seat_no.setTypeface(Util.roboto_regular);
                // self_edt_custombarcode.setTypeface(Util.roboto_regular);


                self_edt_fname.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                            if (motionEvent.getX()>(view.getWidth()-50)){
                                clearText(self_edt_fname);
                                // hideSoftKeyboard(SelfCheckinAttendeeList.this);
                            }
                        }
                        return false;
                    }
                });
                self_edt_lname.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                            if (motionEvent.getX()>(view.getWidth()-50)){
                                clearText(self_edt_lname);
                                // hideSoftKeyboard(SelfCheckinAttendeeList.this);
                            }
                        }
                        return false;
                    }
                });
                self_edt_company.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                            if (motionEvent.getX()>(view.getWidth()-50)){
                                clearText(self_edt_company);
                                // hideSoftKeyboard(SelfCheckinAttendeeList.this);
                            }
                        }
                        return false;
                    }
                });
                self_edt_email.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                            if (motionEvent.getX()>(view.getWidth()-50)){
                                clearText(self_edt_email);
                                // hideSoftKeyboard(SelfCheckinAttendeeList.this);
                            }
                        }
                        return false;
                    }
                });
                self_edt_phone.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                            if (motionEvent.getX()>(view.getWidth()-50)){
                                clearText(self_edt_phone);
                                // hideSoftKeyboard(SelfCheckinAttendeeList.this);
                            }
                        }
                        return false;
                    }
                });
                self_edt_seat_no.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                            if (motionEvent.getX()>(view.getWidth()-50)){
                                clearText(self_edt_seat_no);
                                // hideSoftKeyboard(SelfCheckinAttendeeList.this);
                            }
                        }
                        return false;
                    }
                });
                self_edt_badge_lable.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                            if (motionEvent.getX()>(view.getWidth()-50)){
                                clearText(self_edt_badge_lable);
                                // hideSoftKeyboard(SelfCheckinAttendeeList.this);
                            }
                        }
                        return false;
                    }
                });
                self_edt_designation.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                            if (motionEvent.getX()>(view.getWidth()-50)){
                                clearText(self_edt_designation);
                                // hideSoftKeyboard(SelfCheckinAttendeeList.this);
                            }
                        }
                        return false;
                    }
                });
                /*self_edt_note.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                            if (motionEvent.getX()>(view.getWidth()-50)){
                                clearText(self_edt_note);
                                // hideSoftKeyboard(SelfCheckinAttendeeList.this);
                            }
                        }
                        return false;
                    }
                });*/

                img_attendee.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openTakeFromDialg(SelfcheckinCollectOrderInfo.this);
                    }
                });
                btn_copy.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            hideSoftKeyboard(SelfcheckinCollectOrderInfo.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (forms_drawer.isOpened()) {
                            forms_drawer.animateClose();
                        } else {
                            forms_drawer.animateOpen();
                        }
                    }
                });
                btn_previous.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //
                        formPosition = position - 1;
                        slected_check = -1;
                        names_adapter.notifyDataSetChanged();
                        list_forms.setCurrentItem(position - 1, true);

                    }
                });

                btn_barcode.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (AppUtils.isCamPermissionGranted(SelfcheckinCollectOrderInfo.this)) {
                            // TODO Auto-generated method stub
                            fname = self_edt_fname.getText().toString().trim();
                            lname = self_edt_lname.getText().toString().trim();
                            email = self_edt_email.getText().toString().trim();
                            company = self_edt_company.getText().toString().trim();

                            str_designation = self_edt_designation.getText().toString().trim();
                            str_phone = self_edt_phone.getText().toString().trim();
                            str_badge_label = self_edt_badge_lable.getText().toString().trim();
                            //str_tag = self_edt_tag.getText().toString().trim();
                            str_seat_no = self_edt_seat_no.getText().toString().trim();
                            //srt_note = self_edt_note.getText().toString().trim();
                            //str_custom_barcode = self_edt_custombarcode.getText().toString().trim();

                            if (position == 0) {
                                //Log.i("Putting data to the buyer info", "");
                                buyerInfo.setFirstName(fname);
                                buyerInfo.setLastName(lname);
                                buyerInfo.setEmail(email);
                                buyerInfo.setCompany(company);

                                //newly added badgeable fields
                                buyerInfo.setTag(str_tag);
                                buyerInfo.setMobile(str_phone);
                                buyerInfo.setBadge_lable(str_badge_label);
                                buyerInfo.setSeatno(str_seat_no);
                                buyerInfo.setDesignation(str_designation);
                                buyerInfo.setnote(srt_note);
                                buyerInfo.setCustomBarcode(str_custom_barcode);
                            } else {
                                order_line_items.get(position - 1).setAttendeeImage(Util.db.getimagedata(Util.db.getByteArray(listImagAttendees.get(position).bitmap)));
                                //Util.db.getimagedata(Util.db.getByteArray(attendee_photo)));
                                order_line_items.get(position - 1).setFirstName(fname);
                                order_line_items.get(position - 1).setLastName(lname);
                                order_line_items.get(position - 1).setEmail(email);
                                order_line_items.get(position - 1).setCompanyName(company);
                                order_line_items.get(position - 1).setItemId(order_line_items.get(position - 1).getItemId());

                                //newly added badgeable fields

                                order_line_items.get(position - 1).setTag(str_tag);
                                order_line_items.get(position - 1).setMobile(str_phone);
                                order_line_items.get(position - 1).setBadgeLabel(str_badge_label);
                                order_line_items.get(position - 1).setSeatno(str_seat_no);
                                order_line_items.get(position - 1).setDesignation(str_designation);
                                order_line_items.get(position - 1).setnote(srt_note);
                                order_line_items.get(position - 1).setCustomBarCode(str_custom_barcode);

                                //Log.i("-------------Item Type ------------",":"+order_line_items.get(position-1).getItemTypeName()+" : "+position);
                                if (order_line_items.get(position - 1).getItemTypeName().equalsIgnoreCase("Package")) {
                                    //Log.i("-------------Order Line Items Settings-------------",":"+order_line_items.get(position-1).Item_Pool__r.Ticket_Settings__c);
                                    if (order_line_items.get(position - 1).Item_Pool__r.Ticket_Settings__c.equalsIgnoreCase(getString(R.string.donotinfofromattendee))) {
                                        //Log.i("-------------------Order Line Items size------------",":"+order_line_items.size());
                                        for (OrderItemListHandler order_item : package_order_line_items) {
                                            //Log.i("-------------Order Line Items-------------",":"+(order_line_items.get(position-1).getId().equalsIgnoreCase(order_item.Parent_ID__c)));
                                            if ((order_line_items.get(position - 1).getId().equalsIgnoreCase(order_item.getTicketParentId()))) {
                                                //Log.i("-------------Ticket number First Name-----------",":"+order_item.getTicketNumber()+" "+fname+" "+order_item.getItemTypeId());

                                                order_item.setFirstName(fname);
                                                order_item.setLastName(lname);
                                                order_item.setEmail(email);
                                                order_item.setCompanyName(company);
                                                order_item.setItemId(order_item.getItemId());

                                                //newly added badgeable fields

                                                order_item.setTag(str_tag);
                                                order_item.setMobile(str_phone);
                                                order_item.setBadgeLabel(str_badge_label);
                                                order_item.setSeatno(str_seat_no);
                                                order_item.setDesignation(str_designation);
                                                order_item.setnote(srt_note);
                                                order_item.setCustomBarCode(str_custom_barcode);
                                            }

                                        }
                                    }
                                }

                                // order_line_items.get(position-1).setTicketNumber(t)
                            }
                            Intent i = new Intent(SelfcheckinCollectOrderInfo.this, BarCodeScanActivity.class);
                            i.putExtra(Util.INTENT_KEY_1, CollectOrderInfo.class.getName());
                            i.putExtra(Util.INTENT_KEY_2, position);
                            startActivityForResult(i, 100);
                        }
                        else {
                            AppUtils.giveCampermission(SelfcheckinCollectOrderInfo.this);
                        }
                    }

                });

                btn_next.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //

                        // index++;
                        formPosition = position + 1;
                        slected_check = -1;
                        //attendeeimg = Util.db.getimagedata(Util.db.getByteArray(attendee_photo));
                        fname = self_edt_fname.getText().toString().trim();
                        lname = self_edt_lname.getText().toString().trim();
                        email = self_edt_email.getText().toString().trim();
                        company = self_edt_company.getText().toString().trim();

                        str_designation = self_edt_designation.getText().toString().trim();
                        str_phone = self_edt_phone.getText().toString().trim();
                        str_badge_label = self_edt_badge_lable.getText().toString().trim();
                        //str_tag = self_edt_tag.getText().toString().trim();
                        str_seat_no = self_edt_seat_no.getText().toString().trim();
                        // srt_note = self_edt_note.getText().toString().trim();
                        // str_custom_barcode = self_edt_custombarcode.getText().toString().trim();
                        //Log.i("---------Collect Order Info----", "Name="+ fname + lname);
                        //Log.i("---------Collect Order Info----", "Name="+ email);
                        //Log.i("---------Collect Order Info----", "Name="+ company);
                        //Log.i("---------Collect Order Info----", "Position="+ position + "index=" + index);
                        if (fname.isEmpty()) {
                            self_edt_fname.setError(getResources().getString(R.string.fname_alert));
                            self_edt_fname.requestFocus();
                            //Util.openCustomDialog("Alert","Please enter first name.");
                        } else if (lname.isEmpty()) {
                            self_edt_lname.setError(getResources().getString(R.string.lname_alert));
                            self_edt_lname.requestFocus();
                            //Util.openCustomDialog("Alert","Please enter last name.");
                        } else if (email.isEmpty()) {
                            self_edt_email.setError(getResources().getString(R.string.email_alert));
                            self_edt_email.requestFocus();
                        } else if (!Validation.isEmailAddress(self_edt_email, true, "Please enter valid email")) {
                            self_edt_email.setError("Please enter valid email");
                        } /*else if (frame_barcode.isShown() && str_custom_barcode.isEmpty()) {
                            self_edt_custombarcode.setError(getResources().getString(R.string.barcode_alert));
                            self_edt_custombarcode.requestFocus();
                        }*/ else {

                            if (!index_name.containsKey(position)) {
                                if (position == 0) {
                                    //Log.i("Collect Order Info","Buyer Info Index name=" + position);
                                    index_name.put(position, "Copy Buyer Info");
                                } else {
                                    //Log.i("Collect Order Info","order_line_items Index name="+ position+ "Order Name= "+ order_line_items.get(position - 1).getItemTypeId());
                                    if (order_line_items.get(position - 1).getItemTypeName().equalsIgnoreCase("Package")) {
                                        String item_pool_name = Util.db.getItem_Pool_Name(order_line_items.get(position - 1).getItemPoolId(), checked_in_eventId);
                                        index_name.put(position, "Copy " + item_pool_name + "(" + order_line_items.get(position - 1).getPackageTicketName() + ")");
                                    } else {
                                        String item_pool_name = Util.db.getItem_Pool_Name(order_line_items.get(position - 1).getItemPoolId(), checked_in_eventId);
                                        index_name.put(position, "Copy " + item_pool_name);
                                    }
                                }
                            }

                            if (position == 0) {
                                //Log.i("Putting data to the buyer info", "");
                                buyerInfo.setFirstName(fname);
                                buyerInfo.setLastName(lname);
                                buyerInfo.setEmail(email);
                                buyerInfo.setCompany(company);

                                //newly added badgeable fieldsy
                                buyerInfo.setTag(str_tag);
                                buyerInfo.setMobile(str_phone);
                                buyerInfo.setBadge_lable(str_badge_label);
                                buyerInfo.setSeatno(str_seat_no);
                                buyerInfo.setDesignation(str_designation);
                                buyerInfo.setnote(srt_note);
                                buyerInfo.setCustomBarcode(str_custom_barcode);
                                if(order_line_items.size()>0) {
                                    order_line_items.get(position).setFirstName(fname);
                                    order_line_items.get(position).setLastName(lname);
                                    order_line_items.get(position).setEmail(email);
                                    order_line_items.get(position).setCompanyName(company);
                                    order_line_items.get(position).setMobile(str_phone);
                                }
                            } else {
                                //if (attendee_photo != null){
                                order_line_items.get(position - 1).setAttendeeImage(Util.db.getimagedata(Util.db.getByteArray(listImagAttendees.get(position).bitmap)));
                                //}
                                order_line_items.get(position - 1).setFirstName(fname);
                                order_line_items.get(position - 1).setLastName(lname);
                                order_line_items.get(position - 1).setEmail(email);
                                order_line_items.get(position - 1).setCompanyName(company);
                                order_line_items.get(position - 1).setItemId(order_line_items.get(position - 1).getItemId());

                                //newly added badgeable fields

                                order_line_items.get(position - 1).setTag(str_tag);
                                order_line_items.get(position - 1).setMobile(str_phone);
                                order_line_items.get(position - 1).setBadgeLabel(str_badge_label);
                                order_line_items.get(position - 1).setSeatno(str_seat_no);
                                order_line_items.get(position - 1).setDesignation(str_designation);
                                order_line_items.get(position - 1).setnote(srt_note);
                                order_line_items.get(position - 1).setCustomBarCode(str_custom_barcode);

                                //Log.i("-------------Item Type ------------",":"+order_line_items.get(position-1).getItemTypeName()+" : "+position);
                                if (order_line_items.get(position - 1).getItemTypeName().equalsIgnoreCase("Package")) {
                                    //Log.i("-------------Order Line Items Settings-------------",":"+order_line_items.get(position-1).Item_Pool__r.Ticket_Settings__c);
                                    if (order_line_items.get(position - 1).Item_Pool__r.Ticket_Settings__c.equalsIgnoreCase(getString(R.string.donotinfofromattendee))) {
                                        //Log.i("-------------------Order Line Items size------------",":"+order_line_items.size());
                                        for (OrderItemListHandler order_item : package_order_line_items) {
                                            //Log.i("-------------Order Line Items-------------",":"+(order_line_items.get(position-1).getId().equalsIgnoreCase(order_item.Parent_ID__c)));
                                            if ((order_line_items.get(position - 1).getId().equalsIgnoreCase(order_item.getTicketParentId()))) {
                                                //Log.i("-------------Ticket number First Name-----------",":"+order_item.getTicketNumber()+" "+fname+" "+order_item.getItemTypeId());

                                                order_item.setFirstName(fname);
                                                order_item.setLastName(lname);
                                                order_item.setEmail(email);
                                                order_item.setCompanyName(company);
                                                order_item.setItemId(order_item.getItemId());

                                                //newly added badgeable fields

                                                order_item.setTag(str_tag);
                                                order_item.setMobile(str_phone);
                                                order_item.setBadgeLabel(str_badge_label);
                                                order_item.setSeatno(str_seat_no);
                                                order_item.setDesignation(str_designation);
                                                order_item.setnote(srt_note);
                                                order_item.setCustomBarCode(str_custom_barcode);
                                            }

                                        }
                                    }
                                }

                                // order_line_items.get(position-1).setTicketNumber(t)
                            }
                            //Log.i("---------Collect Order Info----1","Position=" + position + "index=" + index);
                            names_adapter.notifyDataSetChanged();
                            //Log.i("---------Collect Order Info----2","Position=" + position + "index=" + index);
                            list_forms.setCurrentItem(position + 1);
                            list_forms.getAdapter().notifyDataSetChanged();
                            //Log.i("---------Collect Order Info---3","Position=" + position + "index=" + index);
                            if (position == (form_count - 1)) {
                                if (!isErrorBarcode) {
                                    order_line_items.addAll(package_order_line_items);
                                    for (int i = 0; i < buyer_order_line_items.size(); i++) {
                                        order_line_items.add(buyer_order_line_items.get(i));
                                    }
                                }
                                String json_string = new Gson().toJson(order_line_items);
                                String json_items = new Gson().toJson(items_list);
                                Util.order_request.edit().putString(Util.ORDER_REQUEST_STRING, json_string).commit();
                                Util.order_Items.edit().putString(Util.ORDER_ITEMS_STRING, json_items).commit();
                                if (total== 0) {
                                    callSellTickets();
                                   /* requestType = WebServiceUrls.SA_SELL_TICKET;
                                    doRequest();*/
                                } else {
                                    callSellTickets();
                                    //To call sellTickets in last form only
                                  /*  Intent i = new Intent(SelfcheckinCollectOrderInfo.this, SelfcheckinPaymentActivity.class);
                                    i.putExtra(Util.INTENT_KEY_1, buyerInfo);
                                    //	i.putExtra("FirstAttendeeImage",BuyerAttendeeImage);
                                    i.putExtra("FirstAttendeeImage", buyerimage);
                                    //i.putExtra(Util.INTENT_KEY_2, order_line_items);
                                    //i.putExtra(Util.INTENT_KEY_3, items_list);
                                    i.putExtra(Util.TOTAL, total);
                                    i.putExtra(Util.SERVICE_TAX, servicetax);
                                    i.putExtra(Util.SERVICE_FEE, servicefee);
                                    i.putExtra(Util.TICKETSETTING, ticket_setting);
                                    //startActivity(i);
                                    startActivityForResult(i, 200);
*/
                                }
                            }
                        }
                        // index++;
                    }
                });


				 /*if(isLargeTablet(context)|| isXLargeTablet(context))
					{
						//final ExpandablePanalTablet extand_panel=(ExpandablePanalTablet) v.findViewById(R.id.expand_panel);
					// mExpanded=true;
					 //extand_panel.Expanded(true);
					 //extand_panel.measure(widthMeasureSpec, heightMeasureSpec)
					 //extand_panel.setCollapsedHeight(100);
					 extand_panel.set(100);
					}else{
						// mExpanded=false;
					}*/

                extand_panel.setOnExpandListener(new ExpandablePanel.OnExpandListener() {
                    public void onCollapse(View handle, View content) {

                        txt_badge_info.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.plus_small, 0);
                        // extand_panel.setCollapsedHeight(100);
                    }

                    public void onExpand(View handle, View content) {
                        txt_badge_info.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.minus_small, 0);
                    }
                });


            }catch (Exception e){
                e.printStackTrace();
            }
            ((ViewPager) collection).addView(v, 0);

            return v;
        }

        @Override
        public void destroyItem(View collection, int position, Object view) {
            ((ViewPager) collection).removeView((View) view);

        }

        @Override
        public boolean isViewFromObject(View view, Object object) {

            return view == ((View) object);
        }

        @Override
        public void finishUpdate(View arg0) {

        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {

            return null;
        }

        @Override
        public void startUpdate(View arg0) {

        }


    }
    private void clearText(EditText editText){
        editText.setText("");
    }
    private void callSellTickets() {
        try {
            sellticketscount++;
            progressDialog =new ProgressDialog(SelfcheckinCollectOrderInfo.this);
            progressDialog.setMessage("Processing your order...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            ApiInterface apiService = ApiClient.getClient(sfdcddetails.instance_url).create(ApiInterface.class);
            // Call<Void> jsonbody= apiService.setSurveys(setSellJsonBody());
            Call<TotalOrderListHandler> call = apiService.getSellTicketAttendees(checked_in_eventId,sfdcddetails.user_id,""
                    ,setSellJsonBody().toString(),sfdcddetails.token_type + " "+ sfdcddetails.access_token);
            call.enqueue(new Callback<TotalOrderListHandler>() {
                @Override
                public void onResponse(Call<TotalOrderListHandler> call, Response<TotalOrderListHandler> response) {
                    Log.e(call+"------success-------", "------response started-------");
                    try {
                        if (!isValidResponse(response.toString())) {
                            dismissProgressDialog();
                            openSessionExpireAlert(errorMessage(response.toString()));
                        }else if (response.code() == 200) {
                            totalorderlisthandler = response.body();
                            if (!NullChecker(totalorderlisthandler.errorMsg).isEmpty()&&totalorderlisthandler.errorMsg.equalsIgnoreCase("Resend Json Body")&&sellticketscount<=3) {
                                if(sellticketscount<=2) {
                                    dismissProgressDialog();
                                    callSellTickets();
                                }else{
                                    dismissProgressDialog();
                                    openCardErrorDialog("Alert","Something went wrong, Please discard and try again!");
                                }
                            }else if (!NullChecker(totalorderlisthandler.errorMsg).isEmpty()) {
                                playSound(R.raw.somethingwentwrong);
                                openDuplicateBarcodeAlert(totalorderlisthandler.errorMsg);
                                dismissProgressDialog();
                            } else {
                                Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
                                String totalorderlist_string = new Gson().toJson(response.body());
                                //Log.i("---ORDER ARRAY SIZE---","Order id===>"+ totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId());
                                Util.totalorderlisthandler.edit().putString("TTT", totalorderlist_string).commit();
                                order_id = totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId();
                                order_name = totalorderlisthandler.TotalLists.get(0).orderInn.getOrderName();// sending this order name to paytm custid
                                dismissProgressDialog();
                                if (total == 0) {
                                    requestType = WebServiceUrls.SA_PAYMENT_UPDATE;
                                    doRequest();
                                }else if(!order_id.isEmpty()) {
                                    Intent i = new Intent(SelfcheckinCollectOrderInfo.this, SelfcheckinPaymentActivity.class);
                                    i.putExtra(Util.INTENT_KEY_1, buyerInfo);
                                    //	i.putExtra("FirstAttendeeImage",BuyerAttendeeImage);
                                    i.putExtra("FirstAttendeeImage", buyerimage);
                                    //i.putExtra(Util.INTENT_KEY_2, order_line_items);
                                    //i.putExtra(Util.INTENT_KEY_3, items_list);
                                    i.putExtra("ORDERID", order_id);
                                    i.putExtra("ORDERNAME", order_name);
                                    i.putExtra(Util.TOTAL, total);
                                    i.putExtra(Util.SERVICE_TAX, servicetax);
                                    i.putExtra(Util.SERVICE_FEE, servicefee);
                                    i.putExtra(Util.TICKETSETTING, ticket_setting);
                                    //startActivity(i);
                                    startActivityForResult(i, 200);
                                }else if(sellticketscount<=2){
                                    callSellTickets();
                                }else {
                                    dismissProgressDialog();
                                    openCardErrorDialog("Alert","Something went wrong, Please discard and try again!");
                                }
                            }
                        }
                        else {
                            dismissProgressDialog();
                            openSessionExpireAlert(errorMessage(response.errorBody().string()));
                        }
                    }catch (Exception e){
                        dismissProgressDialog();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<TotalOrderListHandler> call, Throwable t) {
                    // Log error here since request failed
                    Log.e("------failure-------", t.toString());
                    dismissProgressDialog();
                }
            });
        }catch (Exception e){
            dismissProgressDialog();
            e.printStackTrace();
        }
    }
    private void dismissProgressDialog() {
        if(progressDialog!=null) {
            if(progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    private class FormNameAdapter extends BaseAdapter {

        /*
         * (non-Javadoc)
         *
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            //
            //Log.i("Size of the names map", ":" + index_name.size());
            return index_name.size();
        }

        /*
         * (non-Javadoc)
         *
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            //
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position) {
            //
            return 0;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.widget.Adapter#getView(int, android.view.View,
         * android.view.ViewGroup)
         */
        @Override
        public View getView(final int position, View convertView,ViewGroup parent) {
            //
            View v = inflater.inflate(R.layout.text_layout, null);
            TextView txt_name = (TextView) v.findViewById(R.id.text_view);
            CheckBox check_box = (CheckBox) v.findViewById(R.id.check_box);
            //Log.i("Collect Order Info","text name=" + index_name.get(position)+ "position of name adapter= " + position);
            txt_name.setText(index_name.get(position));
            if (slected_check == position) {
                check_box.setChecked(true);
            } else {
                check_box.setChecked(false);
            }
            return v;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (forms_drawer.isOpened()) {
                forms_drawer.animateClose();
            } else {
                openWaringAlert();
            }
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void doRequest() {
        //
        String access_token = sfdcddetails.token_type+" "+sfdcddetails.access_token;
        if(requestType.equalsIgnoreCase(WebServiceUrls.SA_SELL_TICKET)){
            String url = sfdcddetails.instance_url + WebServiceUrls.SA_SELL_TICKET+ "eveid=" + checked_in_eventId + "&userid=" + sfdcddetails.user_id;
            postMethod = new HttpPostData("Processing your order...",url,setSellJsonBody().toString(), access_token, SelfcheckinCollectOrderInfo.this);
            postMethod.execute();
        }else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_PAYMENT_UPDATE)){
            String url = sfdcddetails.instance_url + WebServiceUrls.SA_PAYMENT_UPDATE;
            postMethod = new HttpPostData("Processing your payment...",url,setPaymentJSON().toString(), access_token, SelfcheckinCollectOrderInfo.this);
            postMethod.execute();
        }

    }
    private JSONObject setSellJsonBody() {
        // String where=" where ";
        JSONObject parent = new JSONObject();
        try {
            Cursor card_pay_setting= Util.db.getEvent_Card_PGateway(checked_in_eventId);
            card_pay_setting.moveToFirst();
            JSONObject buyer = new JSONObject();
            JSONArray order_array = new JSONArray();
            // set buyer info body
            buyer.put("AmountPayable", total+"");
            buyer.put("AfterDiscountPrice", String.valueOf(total));
            buyer.put("Company", buyerInfo.getCompany());
            buyer.put("CurrencyCode", Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Name);
            buyer.put("DiscountedPrice", "0");
            buyer.put("EMailId", buyerInfo.getEmail().toLowerCase());
            //newly added
            //buyer.put("AttendeePic", buyer_info.getUserPic());
            buyer.put("FirstName", buyerInfo.getFirstName());
            buyer.put("LastName", buyerInfo.getLastName());
            //	buyer.put("buyerImage", BuyerAttendeeImage);
            buyer.put("PhoneNumber", buyerInfo.getMobile());
            buyer.put("OrderStatus", "Fully  Paid".replaceAll(" ", "%20"));
            // buyer.put("OrderTaxes", String.format("%.2f", 0.00));
            buyer.put("OrderTaxes", String.format("%.2f", servicetax));
            buyer.put("OrderTotal", String.valueOf(total));
            buyer.put("Note",buyerInfo.getnote());
            buyer.put("PayKey", "");
            //	buyer.put("PayKey", transaction_id);
            buyer.put("PaymentType", "");
            buyer.put("PaygateWayType","");
            buyer.put("ChargeType", "");
            buyer.put("cardno","");
            buyer.put("FeeAmount", String.valueOf(servicefee));
            buyer.put("keyname","");
            buyer.put("CustomBarcode", NullChecker(buyerInfo.getCustomBarcode()));
            buyer.put("Designation", NullChecker(buyerInfo.getDesignation()));


            // set order body
            for (int i = 0; i < items_list.size(); i++) {
                String where_condition = " where EventId='"	+ checked_in_eventId + "' AND ItemId='" + items_list.get(i).getItemId() + "'";

                String where = " where EventId='"	+ checked_in_eventId + "' AND Item_Pool_Id='" + items_list.get(i).getItemPoolId() + "'";

                Cursor c = Util.db.getTicketCursor(where_condition);

                ////Log.i("----------------Cursor Count---------------",":" + c.getCount());
                c.moveToFirst();
                String ticket_setting1 = c.getString(c.getColumnIndex("ItemOption"));
                // sai change
                //String badgeable=Util.db.getItemBadgble(checked_in_eventId,items_list.get(i).getItemId());
                ////Log.i("----------------Cursor Count---------------", ":"+ ticket_setting1);
                JSONObject item_object = new JSONObject();
                JSONArray item_line_array = new JSONArray();
                item_object.put("ItemDiscount", "0.00");
                //double item_type_price = Util.db.getItemType_BL_FEE(checked_in_eventId, items_list.get(i).getItemTypeId());
                //if(!Boolean.parseBoolean(items_list.get(i).getItemFeeSetting()) && (items_list.get(i).getItemPaidType().equalsIgnoreCase("Paid") || items_list.get(i).getItemPaidType().equalsIgnoreCase("Donation"))) {
                item_object.put("FeeAmount", items_list.get(i).getBL_Fee());
                //}
                item_object.put("ItemId", items_list.get(i).getItemId());
                item_object.put("ItemName", items_list.get(i).getItemName());
                item_object.put("ItemPoolId", items_list.get(i).getItemPoolId());
                item_object.put("ItemPrice",String.valueOf(items_list.get(i).getItemPrice()));
                item_object.put("ItemQuantity",String.valueOf(items_list.get(i).getItemQuantity()));
                item_object.put("ItemTotal", String.valueOf(items_list.get(i).getItemQuantity()*items_list.get(i).getItemPrice()));
                Boolean imageset=false;
                if (ticket_setting1.trim().equalsIgnoreCase(getString(R.string.infofrombuyer))) {
                    for (int j = 0; j < order_line_items.size(); j++) {
                        if (order_line_items.get(j).getItemId().equals(items_list.get(i).getItemId())) {
                            JSONObject line_item_object = new JSONObject();
							/*if(j==0&&BuyerAttendeeImage.length()>0){
								//String s=items_list.get(i).get
								line_item_object.put("AttendeeImage", BuyerAttendeeImage);
							}*/
                            if((imageset.equals(true)) &&((order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge"))){
                                line_item_object.put("AttendeeImage", "");
                            }else if((buyerimage==true) &&(order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge")){
                                imageset=true;
                                line_item_object.put("AttendeeImage", BuyerAttendeeImage);
                            }else{
                                line_item_object.put("AttendeeImage", "");

                            }
                            line_item_object.put("Company",
                                    buyerInfo.getCompany());
                            line_item_object.put("EmailId",
                                    buyerInfo.getEmail().toLowerCase());
                            line_item_object.put("FirstName",
                                    buyerInfo.getFirstName());
                            line_item_object.put("LastName",
                                    buyerInfo.getLastName());
                            line_item_object.put("TicketId", order_line_items.get(j).getTicketNumber());
                            line_item_object.put("Phoneno", buyerInfo.getMobile());
                            if(!buyerInfo.getBadge_lable().trim().isEmpty()){
                                line_item_object.put("BadgeLabel", buyerInfo.getBadge_lable());
                            }else{
                                line_item_object.put("BadgeLabel", Util.db.getItemPoolBadgeLabel(order_line_items.get(j).getItemPoolId(),checked_in_eventId));
                            }
                            line_item_object.put("Tag",buyerInfo.getTag().toString().replace(" ", ","));
                            line_item_object.put("Seatno",buyerInfo.getSeatno());
                            line_item_object.put("Designation", buyerInfo.getDesignation());
                            line_item_object.put("Note",buyerInfo.getnote());
                            //line_item_object.put("CustomBarcode", buyer_info.getCustomBarcode());
                            item_line_array.put(line_item_object);
                        }
                    }
                } else if(ticket_setting1.trim().equalsIgnoreCase(getString(R.string.infofromattendee))){
                    for (int j = 0; j < order_line_items.size(); j++) {
                        if (order_line_items.get(j).getItemId().equals(items_list.get(i).getItemId())) {
                            JSONObject line_item_object = new JSONObject();
                            if((imageset.equals(true)) &&((order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge"))) {
                                line_item_object.put("AttendeeImage", order_line_items.get(j).getUserImage());
                            }
                            else if((order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge")){
                                imageset=true;
                                line_item_object.put("AttendeeImage", BuyerAttendeeImage);
                            }else{
                                line_item_object.put("AttendeeImage", "");

                            }
                            line_item_object.put("Company", order_line_items.get(j).getCompanyName());
                            line_item_object.put("EmailId", order_line_items.get(j).getEmail().toLowerCase());
                            line_item_object.put("FirstName", order_line_items.get(j).getFirstName());
                            line_item_object.put("LastName", order_line_items.get(j).getLastName());
                            line_item_object.put("TicketId", order_line_items.get(j).getTicketNumber());
                            line_item_object.put("Phoneno", order_line_items.get(j).getMobile());
                            line_item_object.put("BadgeLabel", order_line_items.get(j).getBadgeLabel());
                            line_item_object.put("Tag", order_line_items.get(j).getTag().toString().trim().replace(" ", ","));
                            line_item_object.put("Seatno", order_line_items.get(j).getSeatno());
                            line_item_object.put("Designation", order_line_items.get(j).getDesignation());
                            line_item_object.put("Note", order_line_items.get(j).getnote());
                            line_item_object.put("CustomBarcode", order_line_items.get(j).getCustomBarCode());
                            item_line_array.put(line_item_object);
							/*if(j==0&&BuyerAttendeeImage.length()>0){
								//String s=items_list.get(i).get
								line_item_object.put("AttendeeImage", BuyerAttendeeImage);
							}*/
                           /* if((imageset.equals(true)) &&((order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge"))){
                                line_item_object.put("AttendeeImage", "");
                            }else if((order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge")){
                                imageset=true;
                                line_item_object.put("AttendeeImage", BuyerAttendeeImage);
                            }else{
                                line_item_object.put("AttendeeImage", "");

                            }
                            line_item_object.put("Company",
                                    buyerInfo.getCompany());
                            line_item_object.put("EmailId",
                                    buyerInfo.getEmail().toLowerCase());
                            line_item_object.put("FirstName",
                                    buyerInfo.getFirstName());
                            line_item_object.put("LastName",
                                    buyerInfo.getLastName());
                            line_item_object.put("TicketId", order_line_items.get(j).getTicketNumber());
                            line_item_object.put("Phoneno", buyerInfo.getMobile());
                            if(!buyerInfo.getBadge_lable().trim().isEmpty()){
                                line_item_object.put("BadgeLabel", buyerInfo.getBadge_lable());
                            }else{
                                line_item_object.put("BadgeLabel", Util.db.getItemPoolBadgeLabel(order_line_items.get(j).getItemPoolId(),checked_in_eventId));
                            }
                            line_item_object.put("Tag",buyerInfo.getTag().toString().replace(" ", ","));
                            line_item_object.put("Seatno",buyerInfo.getSeatno());
                            line_item_object.put("Designation", buyerInfo.getDesignation());
                            line_item_object.put("Note",buyerInfo.getnote());
                            //line_item_object.put("CustomBarcode", buyer_info.getCustomBarcode());
                            item_line_array.put(line_item_object);*/
                        }
                    }
                }else {
                    for (int j = 0; j < order_line_items.size(); j++) {
                        JSONObject line_item_object = new JSONObject();
                        if (order_line_items.get(j).getItemId().equals(items_list.get(i).getItemId())) {
                            line_item_object.put("Company", order_line_items.get(j).getCompanyName());
                            line_item_object.put("EmailId", order_line_items.get(j).getEmail().toLowerCase());
                            // newly Added
                            line_item_object.put("AttendeeImage", order_line_items.get(j).getUserImage());
                            line_item_object.put("FirstName", order_line_items.get(j).getFirstName());
                            line_item_object.put("LastName", order_line_items.get(j).getLastName());
                            line_item_object.put("TicketId", order_line_items.get(j).getTicketNumber());
                            line_item_object.put("Phoneno", order_line_items.get(j).getMobile());
                            line_item_object.put("BadgeLabel", order_line_items.get(j).getBadgeLabel());
                            line_item_object.put("Tag",order_line_items.get(j).getTag().toString().trim().replace(" ", ","));
                            line_item_object.put("Seatno",order_line_items.get(j).getSeatno());
                            line_item_object.put("Designation", order_line_items.get(j).getDesignation());
                            line_item_object.put("Note",order_line_items.get(j).getnote());
                            line_item_object.put("CustomBarcode",order_line_items.get(j).getCustomBarCode());
                            item_line_array.put(line_item_object);
                        }
                    }
                }
                item_object.put("OrderLineItems", item_line_array);
                order_array.put(item_object);
            }
            parent.put("BuyerInfo", buyer);
            parent.put("Orders", order_array);
            parent.put("SessionAbntime", checkedin_event_record.sessiontime);
            //Log.i("----json Date---", ":"+parent.toString());

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
            //Log.i("----json Date Exception---------------", ":"+e.getMessage());
        }

        return parent;
    }

    private JSONObject setPaymentJSON(){
        JSONObject main_obj = new JSONObject();
        try {
            Cursor card_pay_setting= Util.db.getEvent_Card_PGateway(checked_in_eventId);
            if(card_pay_setting.moveToFirst()){
                //card_pay_setting.moveToFirst();
                payment_gateway_name = card_pay_setting.getString(card_pay_setting.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME));
            }


            main_obj.put("eventId", checked_in_eventId);
            main_obj.put("orderId", order_id);
            main_obj.put("isTransactionSuccess", "Success");
            main_obj.put("errorMessage", "");

            main_obj.put("cardType", "");
            main_obj.put("cardLastDigits", card_last_4);
            main_obj.put("RegType", "ScanAttendee");

			/*if (requestType.equals(Util.CHECK)) {
				main_obj.put("Payment_Mode", Util.CHECK);
				main_obj.put("transactionId", check_number);
				main_obj.put("OrderStatus", order_status);

			}else if(requestType.equals(Util.CASH)){
				main_obj.put("Payment_Mode", Util.CASH);
			}else if(requestType.equals(Util.EXTERNAL_PAY)){
				main_obj.put("Payment_Mode", Util.EXTERNAL_PAY);
			}else */
            if(total==0){
                main_obj.put("PaymentType", "");
                main_obj.put("Payment_Mode", "Free");
            }else{
                main_obj.put("transactionId", transaction_id);
                main_obj.put("Payment_Mode", Util.CRADITCARD);
                main_obj.put("PaymentType ", card_pay_setting.getString(card_pay_setting.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME)));
                main_obj.put("paygatewayid", card_pay_setting.getString(card_pay_setting.getColumnIndex(EventPayamentSettings.EVENT_PAY_GATEWAY__C)));
            }

            card_pay_setting.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return main_obj;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
     */
    @Override
    public void parseJsonResponse(String response) {
        try {
            if(!isValidResponse(response)){
                openSessionExpireAlert(errorMessage(response));
            }
            gson = new Gson();
            if(requestType.equalsIgnoreCase(WebServiceUrls.SA_SELL_TICKET)){
                totalorderlisthandler = new Gson().fromJson(response, TotalOrderListHandler.class);
                //Log.i("---ORDER ARRAY SIZE---", "Server"+ totalorderlisthandler.TotalLists.size());

                if (!NullChecker(totalorderlisthandler.errorMsg).isEmpty()) {
					/*MediaPlayer mediaPlayer = MediaPlayer.create(SellOrderActivity.this, R.raw.somethingwentwrong);
					mediaPlayer.start();*/
                    playSound(R.raw.somethingwentwrong);
                    openDuplicateBarcodeAlert(totalorderlisthandler.errorMsg);
                } else {
                    Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
                    //Log.i("---ORDER ARRAY SIZE---","Order id===>"+ totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId());
                    order_id = totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId();
                    order_name = totalorderlisthandler.TotalLists.get(0).orderInn.getOrderName();// sending this order name to paytm custid
                    if (total==0) {
                        requestType=WebServiceUrls.SA_PAYMENT_UPDATE;
                        doRequest();
                    }
                }
            }else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_PAYMENT_UPDATE)){
                //totalorderlisthandler = gson.fromJson(response, TotalOrderListHandler.class);
                ////Log.i("s---ORDER ARRAY SIZE---", "Server"+ totalorderlisthandler.TotalLists.size());
                if (!isValidResponse(response)) {
                    openSessionExpireAlert(errorMessage(response));
                }
                JSONObject obj = new JSONObject(response);
                String status = obj.optString("status");
                String orderStatus = NullChecker(obj.optString("orderStatus"));
                if (status.toLowerCase().equalsIgnoreCase("booked")) {
                    OrderDetailsHandler orderDetailsHandler = new Gson().fromJson(response, OrderDetailsHandler.class);
                    totalorderlisthandler.TotalLists.get(0).orderInn.setOrderStatus(orderStatus);
                    totalorderlisthandler.TotalLists.get(0).setPaymentInn(orderDetailsHandler.paymentInnmultiple);
                    //TODO PAYMENTITEMS
                   /* totalorderlisthandler.TotalLists.get(0).orderInn.setOrderStatus("Fully Paid");
                    totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setCheckNumber(check_number);
                    totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setId(payment.Id);
                    totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setCreditCardType(payment.credit_card_type__c);
                    totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setlast4degits(payment.credit_card_last_4digits__c);
                    totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setCheckNumber(payment.Payment_Ref_Number__c);
                    totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setRegistrationType(payment.Registration_Type__c);
                    totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setPaymentMode(payment.Payment_Mode__c);

                    if (payment.BLN_Pay_Gateway__c != null) {
                        totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).BLN_Pay_Gateway__r.PGateway_Type__r.Id = payment.BLN_Pay_Gateway__c;
                        totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).BLN_Pay_Gateway__r.PGateway_Type__r.Name = payment_gateway_name;
                    }
*/
                    for (OrderItemListHandler ticket : totalorderlisthandler.TotalLists.get(0).ticketsInn) {
                        ticket.setTicketStatus(status);
                    }
                    order_id = totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId();
                    Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
                    Util.db.insertandupdateAttendeeBadgeIdandBadgeParentID(orderDetailsHandler.ticklist);

                    /*for (int i = 0; i < orderDetailsHandler.ticklist.size(); i++){
                        String BadgeId = orderDetailsHandler.ticklist.get(i).Badge_ID__c;
                        String TicketId = orderDetailsHandler.ticklist.get(i).Id;
                        String BadgeParentId = NullChecker(orderDetailsHandler.ticklist.get(i).badgeparentid__c);
                        String printstatus = "";//Printed
                        Util.db.insertandupdateAttendeeBadgeIdandBadgeParentID(BadgeParentId,
                                BadgeId, "", TicketId,printstatus);
                    }*/
                    attendee_cursor=Util.db.getBadgeableTicketOrderDetails(order_id);
                    //attendee_cursor.moveToFirst();
                    String msg="";
                   /* if(attendee_cursor!=null&&attendee_cursor.getCount()>=1&&((PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Brother"))||(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")))){
                        msg = "Do you want to Print Badge?";
                    }*/
                    //if(attendee_cursor!=null&&attendee_cursor.getCount()>=1){
                    if(attendee_cursor!=null&&attendee_cursor.getCount()>=1&&
                            !(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").isEmpty())&&
                            Util.getselfcheckinbools(Util.ISPRINTALLOWED)) {
                        msg = "Do you want to Print Badge?";
                    }else{
                        msg = "";
                    }
                    Util.setCustomAlertDialog(SelfcheckinCollectOrderInfo.this);
                    String alert = "Your payment for Order ID: " + totalorderlisthandler.TotalLists.get(0).orderInn.getOrderName() + " was Successful!";
					/*if (payment.Payment_Mode__c.equalsIgnoreCase(Util.CASH)) {
						double change = entered_cash_amount - (total + service_tax + servicefee);
						if (change > 0) {
							msg = "Please return " + String.format("%.2f", change) + ". " + msg;
						}
					}*/
                    Util.openCustomDialog(alert, msg);
                    if(attendee_cursor!=null&&attendee_cursor.getCount()>=1){
                        Util.txt_dismiss.setVisibility(View.VISIBLE);
                    }else{
                        Util.txt_dismiss.setVisibility(View.GONE);
                    }
                    Util.txt_okey.setText("OK");
                    Util.alert_dialog.setCancelable(false);
                    Util.txt_okey.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            Util.alert_dialog.dismiss();
                            //if(attendee_cursor!=null&&attendee_cursor.getCount()>=1) {
                           /* if(attendee_cursor!=null&&attendee_cursor.getCount()>=1&&
                                    ((PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Brother"))
                                            ||(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")))){

*/

                            if(attendee_cursor!=null&&attendee_cursor.getCount()>=1&&
                                    !(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").isEmpty())
                                    &&Util.getselfcheckinbools(Util.ISPRINTALLOWED)) {

                               /* if((PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Brother"))
                                        ||(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra"))){

                                }*/
                                if(attendee_cursor.getCount()==1){
                                    Intent endintent = new Intent(SelfcheckinCollectOrderInfo.this, SelfCheckinAttendeeList.class);
                                    endintent.putExtra(Util.ORDER_ID, order_id);
                                    endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(endintent);}
                                else {
                                    BaseActivity.ordersuccess_popupok_clicked=true;
                                    Intent global_intent = new Intent(SelfcheckinCollectOrderInfo.this, GlobalScanActivity.class);
                                    global_intent.putExtra(Util.SCANDATA, order_id.toCharArray());
                                    global_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    global_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(global_intent);
                                    finish();
                                }
                                //finish();
                                //doBadgePrint(attendee_cursor);
                            }else{
                                Intent endintent = new Intent(SelfcheckinCollectOrderInfo.this, SelfCheckinAttendeeList.class);
                                endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(endintent);
								/*Intent endintent = new Intent(SelfcheckinCollectOrderInfo.this, OrderSucessPrintActivity.class);
								endintent.putExtra(Util.ORDER_ID, order_id);
								endintent.putExtra(Util.ORDER_CLOUD_PRINT,true);
								endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(endintent);*/
                            }
                            //finish();
                        }
                    });
                    Util.txt_dismiss.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Util.alert_dialog.dismiss();
                            Intent endintent = new Intent(SelfcheckinCollectOrderInfo.this, SelfCheckinAttendeeList.class);
                            endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(endintent);
                        }
                    });
					/*Intent startentent = new Intent(SelfcheckinCollectOrderInfo.this, GlobalScanActivity.class);
					ordersuccess_popupok_clicked=true;
					startentent.putExtra(Util.SCANDATA, order_id.toCharArray());
					startentent.putExtra(Util.ISFINISH, true);
					startentent.putExtra(Util.INTENT_KEY_1, SellOrderActivity.class.getName());
					startentent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


					Intent endintent = new Intent(SelfcheckinCollectOrderInfo.this, ManageTicketActivity.class);
					endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					AlertDialogCustom ask_dialog = new AlertDialogCustom(SelfcheckinCollectOrderInfo.this);
					//Your payment for Order ID: 0-23444 was Successful. Do you want to print a Badge?
					//String msg = "Do you want to Print/Checkin Badges?";
					String msg = "Do you want to Print Badges?";
					String alert = "Your payment for Order ID: " + totalorderlisthandler.TotalLists.get(0).orderInn.getOrderName() + " was Successful!";
					ask_dialog.setParamenters(alert, msg, startentent, endintent, 2, true);
					ask_dialog.setAlertImage(R.drawable.success, "success");
					ask_dialog.show();*/

                } else {
                    //startErrorAnimation("Payment Process Failed.", txt_error_msg);
                    openCardErrorDialog("Error", "Your payment is declined please check you card details or discard the order?");
                }

            }else {
                Util.db.upadteOrderList(totalorderlisthandler.TotalLists,
                        checked_in_eventId);

                Intent startentent = new Intent(SelfcheckinCollectOrderInfo.this, GlobalScanActivity.class);
                startentent.putExtra(Util.SCANDATA, totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId().toCharArray());
                startentent.putExtra(Util.ISFINISH, true);
                startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Intent endintent = new Intent(SelfcheckinCollectOrderInfo.this, SelfCheckinAttendeeList.class);
                endintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                AlertDialogCustom ask_dialog = new AlertDialogCustom(SelfcheckinCollectOrderInfo.this);
                ask_dialog.setParamenters("Alert", "Do u want to Print/Checkin Tickets ?", startentent, endintent, 2, true);
                ask_dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Log.i("----------------Free Ticket Resposne--------------",":"+e.getMessage());
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

    private void openWaringAlert() {
        Util.setCustomAlertDialog(SelfcheckinCollectOrderInfo.this);
        Util.txt_dismiss.setVisibility(View.VISIBLE);
        Util.setCustomDialogImage(R.drawable.error);
        Util.txt_dismiss.setText("CANCEL");
        Util.txt_okey.setText("OK");

        Util.txt_okey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();
                Intent startentent = new Intent(SelfcheckinCollectOrderInfo.this,SelfCheckinAttendeeList.class);
                startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(startentent);
                finish();
            }
        });
        Util.txt_dismiss.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();

            }
        });
        Util.openCustomDialog("Alert", "Are you sure do you want leave the page. The given data will be lost?");
    }

    public boolean onTouchEvent(android.view.MotionEvent ev) {
        if (super.onTouchEvent(ev)) {
            return false;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.i("--------------Request and Result Code---------",":"+requestCode+" : "+resultCode);
        {
            try {
                if ((requestCode == REQUEST_CODE_CROP_IMAGE) && (data != null)) {

                    String path = data.getStringExtra(CropImage.IMAGE_PATH);

                    if ((path == null) || (TextUtils.isEmpty(path))) {
                        return;
                    }
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
				/*if(bitmapArrayList.size()<formPosition)
					bitmapArrayList.add(bitmap);
				else
					bitmapArrayList.set(formPosition,bitmap);*/
                    //img_attendee.setImageBitmap(bitmap);
                    //attendee_photo = bitmap;
                    //if (is_collectinfofromBuyer||issingleticket) {
                    if (is_collectinfofromBuyer) {
                        listImagAttendees.get(formPosition).bitmap = bitmap;
                        listImagAttendees.get(formPosition).img.setImageBitmap(bitmap);
                        BuyerAttendeeImage = "";
                        BuyerAttendeeImage = Util.db.getimagedata(Util.db.getByteArray(listImagAttendees.get(formPosition).bitmap));
                        if (!NullChecker(BuyerAttendeeImage).isEmpty()) {
                            buyerimage = true;
                        } else {
                            buyerimage = false;
                        }
                    }else {
                        BuyerAttendeeImage = "";
                        listImagAttendees.get(formPosition).bitmap = bitmap;
                        listImagAttendees.get(formPosition).img.setImageBitmap(bitmap);
                    }

                    if (mFileTemp != null) {
                        mFileTemp.delete();
                    }
                } else if ((requestCode == PICK_FROM_CAMERA) && (resultCode == RESULT_OK)) {
                    if (data != null) {

                        //doCrop();
                    } else {
                        File mediaStorageDir = new File(
                                Environment.getExternalStorageDirectory(),
                                "ScanAttendee");
                        if (!mediaStorageDir.exists()) {
                            mediaStorageDir.mkdir();
                        }
                        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                                + "IMG_1.jpg");
                        mImageCaptureUri = Uri.fromFile(mediaFile);


                        if (mImageCaptureUri != null) {
                            try {
                                startCropImage(SelfcheckinCollectOrderInfo.this);

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
                        if (!mediaStorageDir.exists()) {
                            mediaStorageDir.mkdir();
                        }
                        mFileTemp = new File(mediaStorageDir.getPath() + File.separator
                                + "IMG_1.jpg");
                        InputStream inputStream = getContentResolver().openInputStream(data.getData());
                        FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
                        CropUtil.copyStream(inputStream, fileOutputStream);
                        mediaFile = mFileTemp;
                        startCropImage(SelfcheckinCollectOrderInfo.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (requestCode == CROP_FROM_CAMERA) {

                    Bundle extras = data.getExtras();

                    if (extras != null) {
                        attendee_photo = extras.getParcelable("data");
                        img_attendee.setImageBitmap(attendee_photo);

                    }
                } else if (requestCode == FINISH_RESULT) {
                    startActivity(new Intent(SelfcheckinCollectOrderInfo.this, SplashActivity.class));
                    finish();
                }
                if (requestCode == 100 && resultCode == 100) {
                    int position = data.getIntExtra(Util.INTENT_KEY_2, 0);
                    //Log.i("--------------Custom Barcode----------",":"+position);
                    if (position == 0) {
                        //custombarcode_scanned_valule = data.getStringExtra(Util.INTENT_KEY_1);
                        buyerInfo.setCustomBarcode(data.getStringExtra(Util.INTENT_KEY_1));
                    } else {
                        order_line_items.get(position - 1).setCustomBarCode(data.getStringExtra(Util.INTENT_KEY_1));
                        //custombarcode_scanned_valule = data.getStringExtra(Util.INTENT_KEY_1);
                    }

                    adapter.notifyDataSetChanged();
                } else if (requestCode == 200 && resultCode == 200) {
                    isErrorBarcode = true;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void openCardErrorDialog(String alert, String msg){
        Util.setCustomAlertDialog(SelfcheckinCollectOrderInfo.this);
        Util.alert_dialog.setCancelable(false);
        Util.txt_dismiss.setVisibility(View.VISIBLE);
        Util.setCustomDialogImage(R.drawable.error);
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.txt_okey.setText("DISCARD");
        Util.txt_okey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();
                Intent startentent = new Intent(SelfcheckinCollectOrderInfo.this,SelfCheckinAttendeeList.class);
                startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(startentent);
                finish();            }
        });

        Util.openCustomDialog(alert,msg);
    }

    private void openDuplicateBarcodeAlert(String msg){
        Util.setCustomAlertDialog(SelfcheckinCollectOrderInfo.this);
        Util.alert_dialog.setCancelable(false);
        Util.txt_dismiss.setVisibility(View.VISIBLE);
        Util.setCustomDialogImage(R.drawable.error);
        //Util.txt_dismiss.setText("DISCARD");
        //Util.txt_okey.setText("CHANGE");
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.txt_okey.setText("Ok");
        Util.txt_okey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();
                Intent i = new Intent();
                setResult(200, i);
                finish();
            }
        });

        Util.openCustomDialog("Error",msg);
    }

}
