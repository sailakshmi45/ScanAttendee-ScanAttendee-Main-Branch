package com.globalnest.scanattendee;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brother.ptouch.sdk.NetPrinter;
import com.brother.ptouch.sdk.Printer;
import com.globalnest.brother.ptouch.sdk.printdemo.common.Common;
import com.globalnest.mvc.BadgeResponseNew;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.printer.PrinterDetails;
import com.globalnest.printer.ZebraPrinter;
import com.globalnest.printer.ZebraPrinterInfo;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by babu on 27-04-2017.
 */

 public class PrintersListActivityOld extends BaseActivity {
    public static  PrinterDetails savedprinterDetails;
    private String defaultIP="",defaultPort="6101";
    String mPrinterName = "";
    SearchThread search_thread;
    ArrayList<String> brotherPrintersList = new ArrayList<String>();
    private ArrayList<BadgeResponseNew> badge_res = new ArrayList<BadgeResponseNew>();
    ArrayList<PrinterDetails> allPrintersList  = new ArrayList<PrinterDetails>();
    ListView brother_listview,zebra_listview;

    private ProgressBar progress_zebra,progress_brother;
    private TextView txt_ZebraPrinterFound,txt_BrotherPrinterFound;
    private PrinterDetails discoveredPrinter;
    ExpandablePanel panel_statistics, panel_ticket_sales;
    FrameLayout layout_brother_click,layout_zebra_click;
    ImageView img_statistics, img_ticket_sale;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.printer_list_temp_layout);

        panel_statistics.setOnExpandListener(new ExpandablePanel.OnExpandListener() {

            @Override
            public void onExpand(View handle, View content) {
                // TODO Auto-generated method stub
                img_statistics.setImageResource(R.drawable.minus_green);
                //panel_statistics.Expanded(true);

            }

            @Override
            public void onCollapse(View handle, View content) {
                // TODO Auto-generated method stub
                img_statistics.setImageResource(R.drawable.plus_green);
                //panel_statistics.Expanded(false);
                //handle.performClick();
            }
        });

        panel_ticket_sales.setOnExpandListener(new ExpandablePanel.OnExpandListener() {

            @Override
            public void onExpand(View handle, View content) {
                // TODO Auto-generated method stub
                img_ticket_sale.setImageResource(R.drawable.minus_green);
            }

            @Override
            public void onCollapse(View handle, View content) {
                // TODO Auto-generated method stub
                img_ticket_sale.setImageResource(R.drawable.plus_green);
            }
        });

        img_addticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress_brother.setVisibility(View.VISIBLE);
                txt_BrotherPrinterFound.setText("Searching...");
                new SearchThread().start();
            }
        });
    }

    @Override
    public void setCustomContentView(int layout) {
        View v = inflater.inflate(layout, null);
        linearview.addView(v);
        txt_title.setText("Printers");
        img_menu.setImageResource(R.drawable.back_button);
        img_addticket.setVisibility(View.VISIBLE);
        img_addticket.setImageResource(R.drawable.refresh);
        img_socket_scanner.setEnabled(false);
        back_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        brother_listview=(ListView) findViewById(R.id.listView_brother);
//        progress_brother=(ProgressBar)findViewById(R.id.progress_brother);
//        txt_BrotherPrinterFound=(TextView) findViewById(R.id.txt_BrotherPrinterFound);
        progress_brother=(ProgressBar)findViewById(R.id.progress_bar);
        txt_BrotherPrinterFound=(TextView) findViewById(R.id.txt_PrinterFound);

        zebra_listview=(ListView) findViewById(R.id.listView_zebra);
        progress_zebra=(ProgressBar)findViewById(R.id.progress_zebra);
        txt_ZebraPrinterFound=(TextView) findViewById(R.id.txt_ZebraPrinterFound);

        panel_statistics = (ExpandablePanel) linearview.findViewById(R.id.expand_event_statistics);
        panel_ticket_sales = (ExpandablePanel) linearview.findViewById(R.id.expand_ticket_sales);

        layout_brother_click = (FrameLayout) linearview.findViewById(R.id.layout_event_statistics);
        layout_zebra_click = (FrameLayout) linearview.findViewById(R.id.layout_ticket_sales);

        img_statistics = (ImageView) linearview.findViewById(R.id.img_statistics);
        img_ticket_sale = (ImageView) linearview.findViewById(R.id.img_ticket_sales);

        /*layout_brother_click.performClick();
        layout_zebra_click.performClick();*/

        layout_brother_click.postDelayed(new Runnable() {

            @Override
            public void run() {
                layout_brother_click.performClick();
            }
        }, 500);

        layout_zebra_click.postDelayed(new Runnable() {

            @Override
            public void run() {
                layout_zebra_click.performClick();
            }
        }, 500);

       /* String where_att = " Where " + DBFeilds.BADGE_NEW_EVENT_ID + " = '" + checked_in_eventId + "' AND " + DBFeilds.BADGE_NEW_ID + " = '" + checkedin_event_record.Events.badge_name + "'";
        badge_res = Util.db.getAllBadges(where_att);*/

        /*if(badge_res.size()==0) {
            doRequest();
        }*/
        brotherPrintersList.add("QL-720NW");
        brotherPrintersList.add("QL-820NWB");
        brotherPrintersList.add("QL-710W");
        brotherPrintersList.add("QL-800");
        brotherPrintersList.add("QL-810W");
        brotherPrintersList.add("PT-E550W");
        brotherPrintersList.add("PT-P750W");
        brotherPrintersList.add("PT-D800W");
        brotherPrintersList.add("PT-E800W");
        brotherPrintersList.add("PT-E850TKW");
        brotherPrintersList.add("PT-P900W");
        brotherPrintersList.add("PT-P950NW");

        setDialog();
        search_thread = new SearchThread();
        search_thread.start();
        setSavedPrinterList();
        /*savedprinterDetails=getSavedPrinterDetail();
        if(savedprinterDetails!=null){
            ArrayList<PrinterDetails> lis=new ArrayList<>();
            lis.add(savedprinterDetails);
            ZebraPrinter.ZebraPrinterListAdapter adapter=new ZebraPrinter.ZebraPrinterListAdapter(lis,this);
            zebra_listview.setAdapter(adapter);
        }*/

        zebraPrinter=new ZebraPrinter();
        //zebraPrinter.discoverZebraPrinter(zebra_listview,progress_zebra,txt_ZebraPrinterFound,this);
        zebraPrinter.discoverZebraPrinter(zebra_listview,this);

        zebra_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                discoveredPrinter= (PrinterDetails) zebra_listview.getAdapter().getItem(position);
                defaultIP=discoveredPrinter.ip;
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER,"").equals("Zebra")){
                                printerDisconnectAlert();
                                return;
                            }
                            zebraPrinter.createTCPConnection(discoveredPrinter.ip,defaultPort);
                           if(zebraPrinter.getTCPConnection().isConnected()) {
                                setZebraPrefereces(zebraPrinter.getDeviceInfo(zebraPrinter.getTCPConnection(),defaultIP));
                                ArrayList<PrinterDetails> PrinterLists = (ArrayList<PrinterDetails>) ((ZebraPrinter.ZebraPrinterListAdapter) zebra_listview.getAdapter()).getPrinterList();
                                final ArrayList<PrinterDetails> tempPrinterLists = new ArrayList<PrinterDetails>();
                                for (PrinterDetails printer : PrinterLists) {
                                    if (printer.ip.equals(discoveredPrinter.ip)) {
                                        printer.isConnected=true;
                                    }
                                    tempPrinterLists.add(printer);
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((ZebraPrinter.ZebraPrinterListAdapter) zebra_listview.getAdapter()).refreshAdapter(tempPrinterLists);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //AppUtils.showError(PrinterListActivity.this,"Could not connect to printer");
                            AppUtils.showError(PrintersListActivityOld.this,getErrorFromException(e));
                        }

                    }
                }).start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(brotherPrintersList!=null){
            if(brotherPrintersList.size()>0){
                progress_brother.setVisibility(View.VISIBLE);
                txt_BrotherPrinterFound.setText("Searching...");
                search_thread = new SearchThread();
                search_thread.start();
                setSavedPrinterList();
            }
        }
    }

    public String getErrorFromException(Exception e){
        return e.getMessage()+String.valueOf(e.getStackTrace()[0].getLineNumber());
    }

    private void brotherSearchFinished(final int count){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress_brother.setVisibility(View.GONE);
                if(count>0)
                    txt_BrotherPrinterFound.setVisibility(View.GONE);
                else
                    txt_BrotherPrinterFound.setText("No Printer Found.");
            }
        });

    }

    public class SearchThread extends Thread {
        /* search for the printer for 10 times until printer has been found. */
        @Override
        public void run() {
            try {
                // search for net printer.
                if (netPrinterList(5)) {
                    allPrintersList.clear();
                    for(NetPrinter plist:mNetPrinter){
                        PrinterDetails objDetails = new PrinterDetails();
                        objDetails.ip=plist.ipAddress;
                        objDetails.model=plist.modelName;
                        objDetails.printerName=plist.modelName;
                        if(plist.modelName.contains(PrinterDetails.selectedPrinterPrefrences.getString("printer","")) && !PrinterDetails.selectedPrinterPrefrences.getString("printer","").isEmpty()) {
                            objDetails.isConnected = true;
                            PrinterDetails.selectedPrinterPrefrences.edit().putBoolean("isConnected",true).commit();
                        }else {
                            objDetails.isConnected = false;
                        }
                        allPrintersList.add(objDetails);
                    }

                    isPrinter = true;
                    //msgDialog.close();
                    brotherSearchFinished(allPrintersList.size());
                    if(allPrintersList.size()>0){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                brother_listview.setAdapter(new PrinterListAdapter(PrintersListActivityOld.this,allPrintersList));
                            }
                        });

                        //Util.setListViewHeightBasedOnChildren(brother_listview);
                    }else{
                            PrinterDetails.selectedPrinterPrefrences.edit().putBoolean("isConnected",false).commit();
                            /*PrinterDetails pDetails=getSavedPrinterList();
                            pDetails.isConnected=false;
                            allPrintersList.add(pDetails);*/
                        allPrintersList.clear();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    PrinterListAdapter adapter = new PrinterListAdapter(PrintersListActivityOld.this,allPrintersList);
                                    brother_listview.setAdapter(adapter);
                                }
                            });
                    }

                } else {
                   // msgDialog.close();
                    brotherSearchFinished(allPrintersList.size());
                    /*PrinterDetails objDetails = new PrinterDetails();
                    objDetails.setIp("");
                    objDetails.setModel("");
                    objDetails.setPrinterName("TestPrinter");
                    objDetails.setConnected(false);
                    allPrintersList.add(objDetails);*/
                    if(allPrintersList.size()>0){
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                PrinterListAdapter adapter = new PrinterListAdapter(PrintersListActivityOld.this,allPrintersList);
                                brother_listview.setAdapter(adapter);
                                //Util.setListViewHeightBasedOnChildren(brother_listview);
                            }
                        });

                    }else{
                        PrinterDetails.selectedPrinterPrefrences.edit().putBoolean("isConnected",false).commit();
                        /*PrinterDetails pDetails=getSavedPrinterList();
                        pDetails.isConnected=false;
                        allPrintersList.add(pDetails);*/
                        allPrintersList.clear();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                PrinterListAdapter adapter = new PrinterListAdapter(PrintersListActivityOld.this,allPrintersList);
                                brother_listview.setAdapter(adapter);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setDialog() {
        try {
            //msgDialog.showMsgNoButton(getString(R.string.netPrinterListTitle_label), getString(R.string.search_printer));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean netPrinterList1(int times) {
        boolean searchEnd = false;
        try {
            // clear the item list
            if (mItems != null) {
                mItems.clear();
            }
            // get net printers of the particular model
            mItems = new ArrayList<String>();
            Printer myPrinter = new Printer();
            //Log.i("-----------Net Printer Info----------------",":"+myPrinter.getPrinterInfo());
            mNetPrinter = myPrinter.getNetPrinters("QL-720NW");
            final int netPrinterCount = mNetPrinter.length;

            // when find printers,set the printers' information to the list.
            if (netPrinterCount > 0) {
                searchEnd = true;
                setPrefereces(mNetPrinter[0]);
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
            String[] printerListArray=new String[brotherPrintersList.size()];
            int i=0;
            for(String brotherPrinter: brotherPrintersList){
                printerListArray[i]=brotherPrinter;
                i++;
                AppUtils.displayLog("--Brother Printer Name--",""+brotherPrinter);

            }
            mNetPrinter = myPrinter.getNetPrinters(printerListArray);

            final int netPrinterCount = mNetPrinter.length;
            // when find printers,set the printers' information to the list.
            if (netPrinterCount > 0) {
                searchEnd = true;
                //setPrefereces(mNetPrinter[0]);
            } else if (netPrinterCount == 0 && times == (Common.SEARCH_TIMES - 1)) { // when
                // no
                // printer
                // is
                // found
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

    public void setPrefereces(NetPrinter mNetPrinter) {
        try {
            // initialization for print
            /*PrinterInfo printerInfo = new PrinterInfo();
            Printer printer = new Printer();
            printerInfo = printer.getPrinterInfo();
            PrinterDetails.selectedPrinterPrefrences= PreferenceManager.getDefaultSharedPreferences(this);
            if (sharedPreferences.getString("printerModel", "").equals("")) {*/
                sharedPreferences.edit().clear().commit();
                PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
                SharedPreferences.Editor selectedPrinterPref = PrinterDetails.selectedPrinterPrefrences.edit();
                selectedPrinterPref.putString(ZebraPrinter.SELECTED_PRINTER, "Brother");
                selectedPrinterPref.putString(ZebraPrinter.PRINTERMODEL, mPrinterName);//QL_720NW , QL_820NWB
                selectedPrinterPref.putString("printer", mNetPrinter.modelName);
                selectedPrinterPref.putString("address", mNetPrinter.ipAddress.toString());
                selectedPrinterPref.putString("serNo", mNetPrinter.serNo);
                selectedPrinterPref.putBoolean("isConnected",true);
                selectedPrinterPref.commit();

                /*SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(ZebraPrinter.SELECTED_PRINTER, "Brother");
                editor.putString("printerModel", mPrinterName);//QL_720NW , QL_820NWB
                editor.putString("port", "NET");
                editor.putString("address", printerInfo.ipAddress.toString());
                editor.putString("macAddress", printerInfo.macAddress.toString());
                editor.putString("address", mNetPrinter.ipAddress);
                editor.putString("macAddress", mNetPrinter.macAddress);
                editor.putString("printer", mNetPrinter.modelName);
                editor.putString("paperSize", "W62H100");
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
                    }else{
                        editor.putString("paperSize", "W62");
                    }
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
                editor.commit();*/
           // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doRequest() {
        String access_token = sfdcddetails.token_type + " " + sfdcddetails.access_token;
        String _url = sfdcddetails.instance_url + WebServiceUrls.SA_GET_BADGE_TEMPLATE_NEW + "Event_Id="+ checked_in_eventId;
        postMethod = new HttpPostData("Loading Badges...", _url, null, access_token, PrintersListActivityOld.this);
        postMethod.execute();
    }

    @Override
    public void parseJsonResponse(String response) {
        if(!isValidResponse(response)){
            openSessionExpireAlert(errorMessage(response));
            return;
        }
        gson = new Gson();
        Type listType = new TypeToken<List<BadgeResponseNew>>() {}.getType();
        List<BadgeResponseNew> badges = (List<BadgeResponseNew>) gson.fromJson(response, listType);
        AppUtils.displayLog("----------------PrinterList Badge Response----------", ":" + badges.size());
        Util.db.deleteBadges(checked_in_eventId);
        for(BadgeResponseNew badge : badges){
            badge.badge.event_id = checked_in_eventId;
            Util.db.InsertAndUpdateBadgeTemplateNew(badge);
        }
        badges.clear();
        //sharedPreferences.edit().clear().commit();
    }

    @Override
    public void insertDB() {

    }

    private class PrinterListAdapter extends BaseAdapter{
        Context context;
        ArrayList<PrinterDetails> printersList ;

      private  PrinterListAdapter(Context context ,ArrayList<PrinterDetails> printersList ){
            this.context = context;
            this.printersList = printersList;
        }
        @Override
        public int getCount() {
            return printersList.size();
        }

        @Override
        public PrinterDetails getItem(int position) {
            return printersList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Holder holder=new Holder();
            View rowView;
            LayoutInflater inflater=( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.printer_list_item, null);
            holder.total_layout_printer = (LinearLayout) rowView.findViewById(R.id.total_layout_printer);
            holder.printerName=(TextView) rowView.findViewById(R.id.txt_printerName);
            holder.printerImage=(ImageView) rowView.findViewById(R.id.img_printer_status);
            holder.printerName.setText(getItem(position).printerName);
            if(getItem(position).isConnected) {
                holder.printerImage.setVisibility(View.VISIBLE);
                holder.printerImage.setImageResource(R.drawable.green_circle_1);
            }else {
                holder.printerImage.setVisibility(View.GONE);
                holder.printerImage.setImageResource(R.drawable.red_circle_1);
            }
            holder.total_layout_printer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    for(String pName:brotherPrintersList) {
                        if (getItem(position).printerName.contains(pName)) {
                            mPrinterName = pName.replace("-", "_");
                        }
                    }
                    String printerName = PrinterDetails.selectedPrinterPrefrences.getString("printer", "");
                    if (printerName!=null && printerName.equals(getItem(position).printerName)){
                        if( getItem(position).isConnected){
                            printerDisconnectAlert();
                        }else{
                            PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
                            setPrefereces(mNetPrinter[position]);
                            allPrintersList.get(position).isConnected = true;
                            brother_listview.setAdapter(new PrinterListAdapter(PrintersListActivityOld.this,allPrintersList));
                            //Util.setListViewHeightBasedOnChildren(brother_listview);
                        }
                    }else{
                        PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
                        setPrefereces(mNetPrinter[position]);
                        allPrintersList.get(position).isConnected = true;
                        brother_listview.setAdapter(new PrinterListAdapter(PrintersListActivityOld.this,allPrintersList));
                        //Util.setListViewHeightBasedOnChildren(brother_listview);
                    }
                }
            });

            return rowView;
        }

        public void refreshAdapter(ArrayList<PrinterDetails> pList) {
            printersList=pList;
            notifyDataSetChanged();
        }

        public class Holder
        {
            TextView printerName;
            ImageView printerImage;
            LinearLayout total_layout_printer;
        }




    }

    private void printerDisconnectAlert() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

        Util.setCustomAlertDialog(PrintersListActivityOld.this);
        Util.openCustomDialog("Alert", "Do you want to disconnect the printer?");
        Util.txt_okey.setText("DISCONNECT");
        Util.txt_dismiss.setVisibility(View.VISIBLE);
        Util.txt_dismiss.setText("CANCEL");
        Util.txt_okey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // ShowTicketsDialog();
                PrinterDetails printer=new PrinterDetails();
                printer.selectedPrinter=PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER,"");
                printer.printerName=PrinterDetails.selectedPrinterPrefrences.getString("printer","");
                printer.isConnected=false;
                PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
                sharedPreferences.edit().clear().commit();
                ArrayList pList= new ArrayList<PrinterDetails>();
                pList.add(printer);
                ((PrinterListAdapter)brother_listview.getAdapter()).refreshAdapter(pList);
                try {
                    if(zebraPrinter.getTCPConnection()!=null)
                        zebraPrinter.getTCPConnection().close();
                } catch (Exception e) {
                    AppUtils.showError(PrintersListActivityOld.this,ZebraPrinter.getErrorFromException(e));
                }
                   /*img_printer.setImageResource(R.drawable.red_circle_1);
                    txt_on_off_printer.setText("Not Connected");
                    txt_on_off_printer.setTextColor(getResources().getColor(R.color.gray_color));*/
                Util.alert_dialog.dismiss();
            }
        });
        Util.txt_dismiss.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Util.alert_dialog.dismiss();
            }
        });
            }
        });
    }

    public void setZebraPrefereces(ZebraPrinterInfo mNetPrinter) {
        try {
            // initialization for print
            PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
            if (PrinterDetails.selectedPrinterPrefrences.getString("printerModel", "").equals("")) {
                SharedPreferences.Editor editor = PrinterDetails.selectedPrinterPrefrences.edit();
                editor.putString(ZebraPrinter.SELECTED_PRINTER, "Zebra");
                editor.putString(ZebraPrinter.ZEBRA_NAME, mNetPrinter.uniqueId);
                editor.putString(ZebraPrinter.ZEBRA_PORT, String.valueOf(mNetPrinter.port));
                editor.putString(ZebraPrinter.ZEBRA_WIFI_IP, mNetPrinter.ipAddr);
                editor.putString(ZebraPrinter.ZEBRA_WIFI_MAC, mNetPrinter.wifiMacAddr);
                editor.putString(ZebraPrinter.ZEBRA_BT_MAC, mNetPrinter.btMacAddr);
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public PrinterDetails setSavedPrinterList(){
        PrinterDetails printerDetails=new PrinterDetails();
        final ArrayList<PrinterDetails> pList;
            printerDetails.isConnected= true;
            printerDetails.selectedPrinter=PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER,"");
            printerDetails.ip=PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP,"");
            printerDetails.printerName=PrinterDetails.selectedPrinterPrefrences.getString("printer","QL-720NW");
        if(printerDetails.selectedPrinter.equalsIgnoreCase("Brother")){
            pList=new ArrayList<>();
            pList .add(printerDetails);
            brother_listview.setAdapter(new PrinterListAdapter(PrintersListActivityOld.this,pList));
        }else if(printerDetails.selectedPrinter.equalsIgnoreCase("Zebra")){
            pList=new ArrayList<>();
            pList .add(printerDetails);
            zebra_listview.setAdapter(new ZebraPrinter.ZebraPrinterListAdapter(pList,PrintersListActivityOld.this));
        }
        return  printerDetails;
    }

    public PrinterDetails getSavedPrinterList(){
        PrinterDetails printerDetails=new PrinterDetails();
        final ArrayList<PrinterDetails> pList;
        printerDetails.isConnected= true;
        printerDetails.selectedPrinter=PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER,"");
        printerDetails.ip=PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP,"");
        printerDetails.printerName=PrinterDetails.selectedPrinterPrefrences.getString("printer","QL-720NW");
        return  printerDetails;
    }
}

