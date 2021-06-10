package com.globalnest.scanattendee;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.brother.ptouch.sdk.NetPrinter;
import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterStatus;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zebra.android.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.globalnest.classes.MultiDirectionSlidingDrawer.LOG_TAG;
import static com.globalnest.printer.ZebraPrinter.ZEBRA_BT_MAC;
import static com.globalnest.printer.ZebraPrinter.ZEBRA_WIFI_IP;

/**
 * Created by babu on 27-04-2017.
 */

public class PrintersListActivity extends BaseActivity {
    public static PrinterDetails savedprinterDetails;
    private String defaultIP = "", defaultPort = "9100";
    String mPrinterName = "";
    SearchThread search_thread;
    ArrayList<String> brotherPrintersList = new ArrayList<String>();
    private ArrayList<BadgeResponseNew> badge_res = new ArrayList<BadgeResponseNew>();
    ArrayList<PrinterDetails> allPrintersList = new ArrayList<PrinterDetails>();
    ArrayList<PrinterDetails> btPrintersList = new ArrayList<PrinterDetails>();
    ListView brother_listview, zebra_listview,listView_bluetooth;

    private ProgressBar progress_bar;
    //    private ProgressBar progress_zebra, progress_brother;
    //private TextView txt_ZebraPrinterFound, txt_BrotherPrinterFound;
    private TextView txt_PrinterFound,txt_on_off_other_print,txt_on_off_cloud_print,txt_on_off_bluetooth_print;
    private PrinterDetails discoveredPrinter;
    private ToggleButton toggle_cloud_print,toggle_wifi_print,toggle_bluetooth_print;
    // private RadioButton radio_googleprinters,radio_wifiprinters,radio_bluetoothprinters;
    private LinearLayout lay_FitToPaper;
    private CheckBox check_FitToPaper;
    ImageView img_statistics, img_ticket_sale;
    String order_id="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setCustomContentView(R.layout.printer_list_temp_layout);
            order_id=getIntent().getStringExtra(Util.ORDER_ID);
            if(PrinterDetails.buttonprefrences.getString("iswifion","").equalsIgnoreCase("yes")) {
                toggle_wifi_print.setChecked(true);
            }else if(PrinterDetails.buttonprefrences.getString("iswifion","").equalsIgnoreCase("no")){
                toggle_bluetooth_print.setChecked(true);
            }else if(PrinterDetails.buttonprefrences.getString("iswifion","").equalsIgnoreCase("cloud")){
                toggle_cloud_print.setChecked(true);
            }else{
                toggle_wifi_print.setChecked(true);
            }
            img_addticket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(PrinterDetails.buttonprefrences.getString("iswifion","").equalsIgnoreCase("yes")) {
                        toggle_wifi_print.setChecked(true);
                        setWifiorBluetoothON(true);
                    }else if(PrinterDetails.buttonprefrences.getString("iswifion","").equalsIgnoreCase("no")){
                        toggle_bluetooth_print.setChecked(true);
                        setWifiorBluetoothON(false);
                    }else if(PrinterDetails.buttonprefrences.getString("iswifion","").equalsIgnoreCase("cloud")){
                        toggle_cloud_print.setChecked(true);
                    }else{
                        toggle_wifi_print.setChecked(true);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            //AppUtils.showError(PrinterListActivity.this,"Could not connect to printer");
            String message ="Something went wrong. Please try again";
            //AppUtils.showError(PrintersListActivity.this, message,e);
        }
    }

    @Override
    public void setCustomContentView(int layout) {
        try {
            View v = inflater.inflate(layout, null);
            linearview.addView(v);
            txt_title.setText("Printers");
            img_menu.setImageResource(R.drawable.back_button);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            img_addticket.setVisibility(View.VISIBLE);
            img_addticket.setImageResource(R.drawable.dashboardrefresh);
            img_socket_scanner.setEnabled(false);
            back_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSelectedRadiobuttonStatus();
                    finish();
                }
            });

            brother_listview = (ListView) findViewById(R.id.listView_brother);
            progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
            txt_PrinterFound = (TextView) findViewById(R.id.txt_PrinterFound);

            zebra_listview = (ListView) findViewById(R.id.listView_zebra);
           /* progress_zebra = (ProgressBar) findViewById(R.id.progress_zebra);
            txt_ZebraPrinterFound = (TextView) findViewById(R.id.txt_ZebraPrinterFound);*/

            listView_bluetooth = (ListView) findViewById(R.id.listView_bluetooth);

            img_statistics = (ImageView) linearview.findViewById(R.id.img_statistics);
            img_ticket_sale = (ImageView) linearview.findViewById(R.id.img_ticket_sales);
            txt_on_off_bluetooth_print = (TextView) linearview.findViewById(R.id.txt_on_off_bluetooth_print);
            toggle_cloud_print=(ToggleButton)linearview.findViewById(R.id.toggle_cloud_print);
            txt_on_off_cloud_print=(TextView) linearview.findViewById(R.id.txt_on_off_cloud_print);
            toggle_wifi_print=(ToggleButton)linearview.findViewById(R.id.toggle_wifi_print);
            //radio_bluetoothprinters =(RadioButton) linearview.findViewById(R.id.radio_bluetoothprinters);
            toggle_bluetooth_print =(ToggleButton) linearview.findViewById(R.id.toggle_bluetooth_print);
            //radio_wifiprinters =(RadioButton) linearview.findViewById(R.id.radio_wifiprinters);
            txt_on_off_other_print=(TextView) linearview.findViewById(R.id.txt_on_off_other_print);
            lay_FitToPaper =(LinearLayout) linearview.findViewById(R.id.lay_fitToPage);
            check_FitToPaper=(CheckBox) linearview.findViewById(R.id.check_fitToPage);
          /*  if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER,"").equals("Cloud Print")) {
                doIsGoogleCloudPrint(true);
            } else {
                doIsGoogleCloudPrint(false);
            }
            // SetToggleStatus();
            toggle_cloud_print.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    doIsGoogleCloudPrint(isChecked);
                    isCloudPrintingON=isChecked;
                }
            });


            toggle_wifi_print.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isCloudPrintingON=!isChecked;
                    doIsGoogleCloudPrint(!isChecked);
                }
            });*/
            toggle_wifi_print.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sharedPreferences.edit().clear().commit();
                    PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
                    PrinterDetails.buttonprefrences.edit().putString("iswifion", "yes").commit();
                }
            });
            toggle_bluetooth_print.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sharedPreferences.edit().clear().commit();
                    PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (bluetoothAdapter != null) {
                        if (!bluetoothAdapter.isEnabled()) {
                            PrinterDetails.buttonprefrences.edit().putString("iswifion", "No").commit();
                            isBlutoothPrintingON = true;
                        }
                    }
                }
            });
            toggle_cloud_print.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        progress_bar.setVisibility(View.GONE);
                        txt_PrinterFound.setVisibility(View.GONE);
                        toggle_wifi_print.setChecked(false);
                        toggle_bluetooth_print.setChecked(false);
                        setCloudPrefereces(false);
                        txt_on_off_cloud_print.setVisibility(View.VISIBLE);
                    }else{
                        //toggle_wifi_print.setChecked(true);
                        txt_on_off_cloud_print.setVisibility(View.INVISIBLE);
                    }
                }
            });

            toggle_wifi_print.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    try {
                       /* sharedPreferences.edit().clear().commit();
                        PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();*/
                        if(isChecked) {
                            toggle_cloud_print.setChecked(false);
                            toggle_bluetooth_print.setChecked(false);
                            isCloudPrintingON = false;
                            isBlutoothPrintingON = false;
                            progress_bar.setVisibility(View.VISIBLE);
                            txt_PrinterFound.setVisibility(View.VISIBLE);
                            txt_PrinterFound.setText("Searching Wifi Printers...");
                            //  check_FitToPaper.setChecked(false);
                            zebra_listview.setVisibility(View.VISIBLE);
                            brother_listview.setVisibility(View.VISIBLE);
                            listView_bluetooth.setVisibility(View.GONE);
                            new SearchThread().start();
                            if (zebraPrinter == null)
                                zebraPrinter = new ZebraPrinter();
                            zebraPrinter.discoverZebraPrinter(zebra_listview, PrintersListActivity.this);
                            txt_on_off_other_print.setVisibility(View.VISIBLE);
                        }else {
                            txt_on_off_other_print.setVisibility(View.INVISIBLE);
                            brother_listview.setVisibility(View.GONE);
                            zebra_listview.setVisibility(View.GONE);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            toggle_bluetooth_print.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    try {
                        if (isChecked) {
                           /* sharedPreferences.edit().clear().commit();
                            PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();*/
                            toggle_cloud_print.setChecked(false);
                            toggle_wifi_print.setChecked(false);
                            isBlutoothPrintingON=true;
                            brother_listview.setVisibility(View.GONE);
                            zebra_listview.setVisibility(View.GONE);
                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (bluetoothAdapter != null) {
                                if (!bluetoothAdapter.isEnabled()) {
                                    Intent enableBtIntent = new Intent(
                                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(enableBtIntent);
                                } else {
                                    if (AppUtils.isLocationPermissionGranted(PrintersListActivity.this)) {
                                        isCloudPrintingON = false;
                                        discoverbluetoothPrinters();
                                        txt_on_off_bluetooth_print.setVisibility(View.VISIBLE);
                                    } else {
                                        txt_on_off_bluetooth_print.setVisibility(View.INVISIBLE);
                                        isPermissionAllowedorDenied(PrintersListActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
                                        //AppUtils.giveLocationpermission(PrintersListActivity.this);
                                    }
                                }
                            }
                        }else{
                            // toggle_wifi_print.setChecked(true);
                            txt_on_off_bluetooth_print.setVisibility(View.INVISIBLE);
                            listView_bluetooth.setVisibility(View.GONE);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }}
            });
           /* radio_bluetoothprinters.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new ZebraPrinter.discoverbluetooth().execute();
                }
            });*/
           /* check_FitToPaper.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    check_FitToPaper.setChecked(isChecked);
                    setCloudPrefereces(false);
                }
            });*/
        /*layout_brother_click.postDelayed(new Runnable() {

            @Override
            public void run() {
                layout_brother_click.performClick();
            }
        }, 500);*/

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
//added newly
            brotherPrintersList.add("QL-1100");
            brotherPrintersList.add("QL-1110NWB");
            brotherPrintersList.add("QL-1115NWB");
            /*if(!isCloudPrintingON) {
                setDialog();
                search_thread = new SearchThread();
                search_thread.start();
                //  setSavedPrinterList();

                try {
                    new Thread().sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/
           /* BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                if (bluetoothAdapter.isEnabled()) {
                    discoverbluetoothPrinters();
                }
            }*/
            // discoverbluetoothPrinters();


            zebra_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    discoveredPrinter = (PrinterDetails) zebra_listview.getAdapter().getItem(position);
                    defaultIP = discoveredPrinter.ip;
                    new Thread(new Runnable() {
                        public void run() {

                            try {
                                if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equals("Zebra") && defaultIP.equals(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, ""))) {
                                    printerDisconnectAlert(discoveredPrinter);
                                    return;
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        baseDialog.setMessage("Connecting...");
                                        baseDialog.show();
                                        Toast.makeText(PrintersListActivity.this," connecting with..."+discoveredPrinter.ip+"",Toast.LENGTH_LONG).show();
                                    }
                                });



                                zebraPrinter.createTCPConnection(discoveredPrinter.ip, defaultPort);
                              /* if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_MAC, "").isEmpty())
                                   zebraPrinter.createBTConnection();*/
                                if (zebraPrinter.getTCPConnection().isConnected())//||zebraPrinter.getBTConnection().isConnected()) {
                                { /* ZebraPrinterInfo printerInfo =new ZebraPrinterInfo();
                                    printerInfo.ipAddr=discoveredPrinter.ip;
                                    printerInfo.port=Integer.parseInt(defaultPort);
                                    printerInfo.btMacAddr="";
                                    printerInfo.wifiMacAddr="";
                                    printerInfo.uniqueId="";
                                    printerInfo.resolutiondpi="300";*/
                                    //setZebraPrefereces(printerInfo);
                                   /* if(zebraPrinter.getBTConnection().isConnected()&&NullChecker(defaultIP).isEmpty()){
                                        setZebraPrefereces(zebraPrinter.getBTDeviceInfo(zebraPrinter.getBTConnection()));
                                    }else {*/
                                    setZebraPrefereces(zebraPrinter.getDeviceInfo(zebraPrinter.getTCPConnection(), discoveredPrinter.ip));
                                    //}
                                  /*  String where_att = " Where " + DBFeilds.BADGE_NEW_EVENT_ID + " = '"
                                            + checkedin_event_record.Events.Id + "' AND " + DBFeilds.BADGE_NEW_ID
                                            + " = '" + checkedin_event_record.Events.Mobile_Default_Badge__c + "'";
                                    ArrayList<BadgeResponseNew> badge_res = Util.db.getAllBadges(where_att);

                                    if (badge_res.size() > 0) {
                                        BadgeDataNew badge_data = new Gson().fromJson(badge_res.get(0).badge.Data__c, BadgeDataNew.class);
                                        badgeLabelWidth = (double) badge_data.canvasWidth ;
                                        badgeLabelLength = (double) badge_data.canvasHeight;
                                        final double length=Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_LABEL_LENGTH, ""));
                                        final int dpi=Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_RESOLUTION, ""));

                                        //width=Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PRINT_WIDTH, ""));

                                        if(badgeLabelLength>(length/dpi)){
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Util.setCustomAlertDialog(PrintersListActivity.this);
                                                    Util.openCustomDialog("Warning :",
                                                            "We found the connected printer calibrated values and selected Badge Template values are different.\n" +
                                                                    "Please fix your Badge Template length to match with Printer value!\n"+
                                                                    "Printer Length : "+String.format("%.2f",length/dpi)+"''"+" ("+length+")"+"\n" +
                                                                    "Selected Template Length : "+String.format("%.2f",badgeLabelLength)+"''"+" ("+String.format("%.2f",badgeLabelLength*dpi)+")");
                                                    Util.txt_okey.setText("OK");
                                                    Util.txt_dismiss.setVisibility(View.GONE);
                                                    Util.txt_okey.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Util.alert_dialog.dismiss();
                                                        }
                                                    });
                                                    Util.alert_dialog.setCancelable(true);
                                                }
                                            });
                                        }
                                    }*/
                                    ArrayList<PrinterDetails> PrinterLists = (ArrayList<PrinterDetails>) ((ZebraPrinter.ZebraPrinterListAdapter) zebra_listview.getAdapter()).getPrinterList();
                                    final ArrayList<PrinterDetails> tempPrinterLists = new ArrayList<PrinterDetails>();
                                    for (PrinterDetails printer : PrinterLists) {
                                        if (printer.ip.equals(discoveredPrinter.ip)) {
                                            printer.isConnected = true;
                                        }
                                        tempPrinterLists.add(printer);
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            baseDialog.dismiss();
                                            ((ZebraPrinter.ZebraPrinterListAdapter) zebra_listview.getAdapter()).notifyDataSetChanged();//refreshAdapter(tempPrinterLists);
                                            ((PrinterListAdapter) brother_listview.getAdapter()).notifyDataSetChanged();//refreshAdapter(removeDuplicatePrinters(allPrintersList));
                                            // new GetZebraPrinterConfigTask(PrintersListActivity.this).execute();
                                        }
                                    });

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                //AppUtils.showError(PrinterListActivity.this,"Could not connect to printer");
                                openUIErrorDialog(e);
                            }

                        }
                    }).start();
                }
            });
        }catch(Exception e){
            e.printStackTrace();
            String message ="Something went wrong. Please try again";
            //AppUtils.showError(PrintersListActivity.this, message);
            // openUIErrorDialog(e);
        }
    }
    private void openUIErrorDialog(final Exception e){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String errorMessage=getErrorFromException(e);
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                errorMessage=sw.toString();
                if(errorMessage.trim().isEmpty()){
                    errorMessage=getErrorFromException(e);
                }
                String message ="Something went wrong. Please try again "+errorMessage;
                AppUtils.showError(PrintersListActivity.this, message,e);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* if(PrinterDetails.buttonprefrences.getString("iswifion","").equalsIgnoreCase("yes")) {
            toggle_wifi_print.setChecked(true);
        }else if(PrinterDetails.buttonprefrences.getString("iswifion","").equalsIgnoreCase("no")){
            toggle_bluetooth_print.setChecked(true);
        }else if(PrinterDetails.buttonprefrences.getString("iswifion","").equalsIgnoreCase("cloud")){
            toggle_cloud_print.setChecked(true);
        }else{
            toggle_wifi_print.setChecked(true);
        }*/



       /* if(!isCloudPrintingON) {
            zebraPrinter = new ZebraPrinter();
            if (brotherPrintersList != null) {
                if (brotherPrintersList.size() > 0) {
                   *//* progress_brother.setVisibility(View.VISIBLE);
                    txt_BrotherPrinterFound.setText("Searching...");*//*
                    search_thread = new SearchThread();
                    search_thread.start();
                    // setSavedPrinterList();
                }
            }





            if(zebra_listview!=null) {
               *//* progress_zebra.setVisibility(View.VISIBLE);
                txt_ZebraPrinterFound.setText("Searching...");*//*
                zebraPrinter.discoverZebraPrinter(zebra_listview, PrintersListActivity.this);
            }
        }else{
            //toggle_cloud_print.setChecked(isCloudPrintingON);//TODO
        }*/
    }

    public String getErrorFromException(Exception e) {
        return e.getMessage() + String.valueOf(e.getStackTrace()[0].getLineNumber());
    }
    private void bluetoothSearchFinished(final int count){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress_bar.setVisibility(View.GONE);
                //zebra_listview.setAdapter(new ZebraPrinter.ZebraPrinterListAdapter(printersList,PrintersListActivity.this));
                txt_PrinterFound.setText("Discovered " + btPrintersList.size() + " printers.");
                if(btPrintersList.size()==0) {
                    txt_PrinterFound.setText("No Bluetooth Printer Found ");
                }else{
                    txt_PrinterFound.setVisibility(View.GONE);
                }
            }
        });
    }
    private void brotherSearchFinished(final int count) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               /* progress_brother.setVisibility(View.GONE);
                txt_BrotherPrinterFound.setVisibility(View.GONE);*/
                /*if (count > 0)
                    txt_BrotherPrinterFound.setVisibility(View.GONE);
                else {
                    if(!progress_zebra.isShown()) {
                        txt_BrotherPrinterFound.setText("No Printer Found.");
                    }else{
                        txt_BrotherPrinterFound.setVisibility(View.GONE);
                    }
                }*/
            }
        });
    }

    public void zebraSearchFinish(final List<PrinterDetails> printersList){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress_bar.setVisibility(View.GONE);
                zebra_listview.setAdapter(new ZebraPrinter.ZebraPrinterListAdapter(printersList,PrintersListActivity.this));
                txt_PrinterFound.setText("Discovered " + (printersList.size()+allPrintersList.size()) + " printers.");
                if(printersList.size()+allPrintersList.size()==0) {
                    txt_PrinterFound.setText("No Printer Found in "+Util.getWifiName(PrintersListActivity.this));
                }else{
                    txt_PrinterFound.setVisibility(View.GONE);
                }
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
                    for (NetPrinter plist : mNetPrinter) {
                        PrinterDetails objDetails = new PrinterDetails();
                        objDetails.ip = plist.ipAddress;
                        objDetails.model = plist.modelName;
                        objDetails.printerName = plist.modelName;
                        objDetails.mNetPrinter = plist;
                        if (plist.modelName.contains(PrinterDetails.selectedPrinterPrefrences.getString("printer", "")) && !PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
                            objDetails.isConnected = true;
                            PrinterDetails.selectedPrinterPrefrences.edit().putBoolean("isConnected", true).commit();
                        } else {
                            objDetails.isConnected = false;
                        }
                        allPrintersList.add(objDetails);
                    }

                    isPrinter = true;
                    //msgDialog.close();
                    brotherSearchFinished(allPrintersList.size());
                    if (allPrintersList.size() > 0) {
                        //brother_listview.setVisibility(View.VISIBLE);
                        setBrotherPrinterListAdapter();
                    } else {
                        brother_listview.setVisibility(View.GONE);
                        PrinterDetails.selectedPrinterPrefrences.edit().putBoolean("isConnected", false).commit();
                        allPrintersList.clear();
                        setBrotherPrinterListAdapter();
                    }
                } else {
                    brotherSearchFinished(allPrintersList.size());
                    if (allPrintersList.size() > 0) {
                        setBrotherPrinterListAdapter();
                    } else {
                        PrinterDetails.selectedPrinterPrefrences.edit().putBoolean("isConnected", false).commit();
                        allPrintersList.clear();
                        setBrotherPrinterListAdapter();
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
            //myPrinter.getLabelInfo();
            String[] printerListArray = new String[brotherPrintersList.size()];
            int i = 0;
            for (String brotherPrinter : brotherPrintersList) {
                printerListArray[i] = brotherPrinter;
                i++;
                AppUtils.displayLog("--Brother Printer Name--", "" + brotherPrinter);

            }
            mNetPrinter = myPrinter.getNetPrinters(printerListArray);

            final int netPrinterCount = mNetPrinter.length;
            // when find printers,set the printers' information to the list.
            if (netPrinterCount > 0) {
                searchEnd = true;
                //setPrefereces(mNetPrinter[0]);
            } else if (netPrinterCount == 0 && times == (Common.SEARCH_TIMES - 1)) { // when no printer is found
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
    public void setBluetoothPrefereces(String selectedPrinter,String printerName,String macAddress,Boolean isconnected) {
        try {
            sharedPreferences.edit().clear().commit();
            PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
            SharedPreferences.Editor selectedPrinterPref = PrinterDetails.selectedPrinterPrefrences.edit();
            selectedPrinterPref.putString(ZebraPrinter.SELECTED_PRINTER, selectedPrinter);
            selectedPrinterPref.putString(ZebraPrinter.PRINTERMODEL, mPrinterName);//QL_720NW , QL_820NWB
            selectedPrinterPref.putString("printer", printerName);
            selectedPrinterPref.putString("macAddress", macAddress);
            selectedPrinterPref.putBoolean("isConnected", isconnected);
            selectedPrinterPref.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setBrotherPrefereces(NetPrinter mNetPrinter) {
        try {
            // initialization for print
			/*PrinterInfo printerInfo = new PrinterInfo();
			Printer printer = new Printer();
			printerInfo = printer.getPrinterInfo();*/
            if (sharedPreferences.getString("printerModel", "").equals("")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                // editor.putString("printerModel", PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.PRINTERMODEL,"QL_720NW"));
                editor.putString("printerModel", mNetPrinter.modelName);//changed because of socket execption

                if(!mNetPrinter.ipAddress.isEmpty())
                    editor.putString("port", "NET");
                else
                    editor.putString("port", "BLUETOOTH");

                editor.putString("address", mNetPrinter.ipAddress);
                editor.putString("macAddress", mNetPrinter.macAddress);
                editor.putString("printer", mNetPrinter.modelName);
                editor.putString("paperSize", "W62H100");
                editor.putString("serNo", mNetPrinter.serNo);
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

    public void setPrefereces(NetPrinter mNetPrinter,String selectedPrinter) {
        try {
            sharedPreferences.edit().clear().commit();
            PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
            SharedPreferences.Editor selectedPrinterPref = PrinterDetails.selectedPrinterPrefrences.edit();
            selectedPrinterPref.putString(ZebraPrinter.SELECTED_PRINTER, selectedPrinter);
            selectedPrinterPref.putString(ZebraPrinter.PRINTERMODEL, mPrinterName);//QL_720NW , QL_820NWB
            selectedPrinterPref.putString("printer", mNetPrinter.modelName);
            selectedPrinterPref.putString("address", mNetPrinter.ipAddress.toString());
            selectedPrinterPref.putString("macAddress", mNetPrinter.macAddress);
            selectedPrinterPref.putString("serNo", mNetPrinter.serNo);
            selectedPrinterPref.putBoolean("isConnected", true);
            selectedPrinterPref.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCloudPrefereces(boolean isClear) {
        try {
            sharedPreferences.edit().clear().commit();
            PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
            zebra_listview.setVisibility(View.GONE);
            brother_listview.setVisibility(View.GONE);
            listView_bluetooth.setVisibility(View.GONE);
            if(!isClear) {
                isBlutoothPrintingON = false;
                SharedPreferences.Editor selectedPrinterPref = PrinterDetails.selectedPrinterPrefrences.edit();
                selectedPrinterPref.putString(ZebraPrinter.SELECTED_PRINTER, "Cloud Print");
                selectedPrinterPref.putBoolean(ZebraPrinter.FIT_TO_PAPER, check_FitToPaper.isChecked());
                selectedPrinterPref.commit();
                isCloudPrintingON=true;
                String sai= PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            getSelectedRadiobuttonStatus();
        }
        return super.onKeyDown(keyCode, event);
    }
    public void getSelectedRadiobuttonStatus(){
        if(toggle_bluetooth_print.isChecked()){
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                if (bluetoothAdapter.isEnabled()) {
                    PrinterDetails.buttonprefrences.edit().putString("iswifion", "No").commit();
                    isBlutoothPrintingON = true;
                }else {
                    PrinterDetails.buttonprefrences.edit().putString("iswifion", "Yes").commit();
                }
            }
        }else if(toggle_cloud_print.isChecked()){
            PrinterDetails.buttonprefrences.edit().putString("iswifion", "cloud").commit();
            isCloudPrintingON=true;
        }else {
            PrinterDetails.buttonprefrences.edit().putString("iswifion", "Yes").commit();
        }
    }
    /* public void getToggleStatus(){
         if(toggle_cloud_print.isChecked()){
             isCloudPrintingON=true;
         }else{
             isCloudPrintingON=false;
         }
     }*/
    @Override
    public void doRequest() {
        String access_token = sfdcddetails.token_type + " " + sfdcddetails.access_token;
        String _url = sfdcddetails.instance_url + WebServiceUrls.SA_GET_BADGE_TEMPLATE_NEW + "Event_Id="+ checked_in_eventId;
        postMethod = new HttpPostData("Loading Badges...", _url, null, access_token, PrintersListActivity.this);
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
    public class BluetoothPrinterListAdapter extends BaseAdapter {
        List<PrinterDetails> printers;
        Context context;
        public BluetoothPrinterListAdapter(Context context ,ArrayList<PrinterDetails> printersList ){
            this.printers=printersList;
            this.context=context;
        }

        @Override
        public int getCount() {
            return printers.size();
        }

        @Override
        public PrinterDetails getItem(int position) {
            return printers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView=null;
            final Holder holder =new Holder();
            try{
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.printer_list_item, null);
                holder.bluetooth_item_layout = (LinearLayout) rowView.findViewById(R.id.total_layout_printer);
                holder.printerName = (TextView) rowView.findViewById(R.id.txt_printerName);
                holder.txt_printer_ser_no = (TextView) rowView.findViewById(R.id.txt_printer_ser_no);
                holder.img_connected = (ImageView) rowView.findViewById(R.id.img_printer_status);
                holder.img_printer_type = (ImageView) rowView.findViewById(R.id.img_printer_type);
                holder.printerName.setText(getItem(position).printerName);
                if (!getItem(position).macAddress.isEmpty()) {
                    holder.img_printer_type.setVisibility(View.VISIBLE);
                    holder.img_printer_type.setImageResource(R.drawable.img_bluetooth);
                    holder.txt_printer_ser_no.setText(AppUtils.NullChecker(getItem(position).macAddress));
                } else{
                    holder.img_printer_type.setVisibility(View.GONE);
                    holder.img_printer_type.setImageResource(R.drawable.print);
                }

                if(AppUtils.NullChecker(getItem(position).macAddress).equals(PrinterDetails.selectedPrinterPrefrences.getString(ZEBRA_BT_MAC,""))){
                    holder.img_connected.setVisibility(View.VISIBLE);
                    holder.img_connected.setImageResource(R.drawable.green_circle_1);
                    if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER,"").isEmpty()
                            &&PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER,"").contains("Brother"))
                    {
                        holder.txt_printer_ser_no.setText(AppUtils.NullChecker(getItem(position).macAddress));
                    }else {
                        String str_length,str_width;
                        double length,width;
                        int dpi;
                        dpi=Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_RESOLUTION, ""));
                       /* length=Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_LABEL_LENGTH, ""));
                        width=Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PRINT_WIDTH, ""));
                       */
                       if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_LABEL_LENGTH, "").equalsIgnoreCase("?")) {
                            length = Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_LABEL_LENGTH, ""));
                            str_length=String.format("%.2f",length/dpi)+"''"+" ("+(int)length+")";
                        }
                        else
                            str_length=PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_LABEL_LENGTH, "");
                        if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PRINT_WIDTH, "").equalsIgnoreCase("?"))
                        {  width=Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PRINT_WIDTH, ""));
                            str_width= String.format("%.2f",width/dpi)+"''"+" ("+(int)width+")";
                        }
                        else
                            str_width=PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PRINT_WIDTH, "");

                        holder.txt_printer_ser_no.setText("Length: "+str_length
                                +" | Width: "+str_width
                                +" | DPI: "+dpi
                                +" | Media Type: "+PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_MEDIATYPE, "")
                                +" | MAC: " + PrinterDetails.selectedPrinterPrefrences.getString(ZEBRA_BT_MAC, ""));
                    }
                }else {
                    holder.img_connected.setVisibility(View.GONE);
                    holder.img_connected.setImageResource(R.drawable.red_circle_1);
                }
                listView_bluetooth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        try {
                            if (!NullChecker(getItem(position).macAddress).isEmpty() && !NullChecker(getItem(position).macAddress).equalsIgnoreCase(PrinterDetails.selectedPrinterPrefrences.getString(ZEBRA_BT_MAC, ""))) {
                                new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            // int position = (int) holder.bluetooth_item_layout.getTag();

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    baseDialog.setMessage("Connecting...");
                                                    baseDialog.show();
                                                }
                                            });
                                            for (String pName : brotherPrintersList) {
                                                if (getItem(position).printerName.contains(pName)) {
                                                    mPrinterName = pName.replace("-", "_");
                                                    break;
                                                }
                                            }
                                            if (!NullChecker(getItem(position).macAddress).isEmpty() && NullChecker(getItem(position).ip).isEmpty()) {
                                                // Toast.makeText(PrintersListActivity.this, "Not Implemented", Toast.LENGTH_LONG).show();
                                                PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
                                                if (!mPrinterName.isEmpty()) {
                                                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                                    myPrint.setBluetoothAdapter(bluetoothAdapter);
                                                    NetPrinter printer = new NetPrinter();
                                                    printer.modelName = NullChecker(mPrinterName);
                                                    printer.serNo = "";
                                                    printer.ipAddress = "";
                                                    printer.macAddress = NullChecker(getItem(position).macAddress);
                                                    setBrotherPrefereces(printer);
                                                    myPrint.setPrinterInfo();
                                                    Printer mPrinter = new Printer();
                                                    mPrinter.getPrinterInfo();
                                                    PrinterStatus printerStatus = mPrinter.getPrinterStatus();
                                                    if (printerStatus.errorCode.name().equalsIgnoreCase("ERROR_BROTHER_PRINTER_NOT_FOUND")
                                                            || printerStatus.errorCode.name().equalsIgnoreCase("ERROR_CONNECT_SOCKET_FAILED")) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                baseDialog.dismiss();
                                                                openpairingerrorpopup(PrintersListActivity.this);
                                                            }
                                                        });
                                                    } else {
                                                        PrinterDetails.selectedPrinterPrefrences.edit().putString(ZebraPrinter.SELECTED_PRINTER, "Brother").commit();
                                                        PrinterDetails.selectedPrinterPrefrences.edit().putBoolean("isConnected", true).commit();
                                                        PrinterDetails.selectedPrinterPrefrences.edit().putString("macAddress", NullChecker(getItem(position).macAddress)).commit();
                                                        PrinterDetails.selectedPrinterPrefrences.edit().putString("printer", NullChecker(mPrinterName)).commit();
                                                        PrinterDetails.selectedPrinterPrefrences.edit().putString(ZebraPrinter.PRINTERMODEL, NullChecker(mPrinterName)).commit();
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                baseDialog.dismiss();
                                                                ((BluetoothPrinterListAdapter) listView_bluetooth.getAdapter()).notifyDataSetChanged();//refreshAdapter(removeDuplicatePrinters(allPrintersList));
                                                                // new GetZebraPrinterConfigTask(PrintersListActivity.this).execute();
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    BaseActivity.zebraPrinter = new ZebraPrinter();
                                                    try {
                                                        BaseActivity.zebraPrinter.createBTConnection(getItem(position).macAddress);
                                                        if(BaseActivity.zebraPrinter.getBTConnection()!=null)
                                                            if (BaseActivity.zebraPrinter.getBTConnection().isConnected()) {
                                                                setZebraPrefereces(zebraPrinter.getBTDeviceInfo(zebraPrinter.getBTConnection()));
                                                                PrinterDetails.selectedPrinterPrefrences.edit().putString(ZebraPrinter.SELECTED_PRINTER, "zebra").commit();
                                                                PrinterDetails.selectedPrinterPrefrences.edit().putBoolean("isConnected", true).commit();
                                                                PrinterDetails.selectedPrinterPrefrences.edit().putString("macAddress", NullChecker(getItem(position).macAddress)).commit();
                                                                PrinterDetails.selectedPrinterPrefrences.edit().putString("printer", NullChecker(getItem(position).printerName)).commit();
                                                                //setPrefereces(getItem(position).mNetPrinter, "Zebra");
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        baseDialog.dismiss();
                                                                        ((BluetoothPrinterListAdapter) listView_bluetooth.getAdapter()).notifyDataSetChanged();//refreshAdapter(removeDuplicatePrinters(allPrintersList));
                                                                        // new GetZebraPrinterConfigTask(PrintersListActivity.this).execute();
                                                                    }
                                                                });
                                                            } else {
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            zebraPrinter.CloseandReopenBTConnection();
                                                                            baseDialog.dismiss();
                                                                            ((BluetoothPrinterListAdapter) listView_bluetooth.getAdapter()).notifyDataSetChanged();//refreshAdapter(removeDuplicatePrinters(allPrintersList));
                                                                        } catch (ConnectionException e) {
                                                                            e.printStackTrace();
                                                                            openpairingerrorpopup(PrintersListActivity.this);
                                                                        }
                                                                        // new GetZebraPrinterConfigTask(PrintersListActivity.this).execute();
                                                                    }
                                                                });
                                                                // BaseActivity.zebraPrinter.createBTConnection(getItem(position).macAddress);
                                                            }

                                                    } catch (ConnectionException e) {
                                                        e.printStackTrace();
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                openpairingerrorpopup(PrintersListActivity.this);
                                                            }
                                                        });
                                                    }

                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    openpairingerrorpopup(PrintersListActivity.this);
                                                }
                                            });
                                        }
                                    }
                                }).start();
                            } else if (!NullChecker(getItem(position).macAddress).isEmpty()) {
                                printerDisconnectAlert(getItem(position));
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                /*holder.bluetooth_item_layout.setTag(position);
                holder.bluetooth_item_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {

                            int position = (int) holder.bluetooth_item_layout.getTag();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    baseDialog.setMessage("Connecting...");
                                    baseDialog.show();
                                }
                            });
                            for (String pName : brotherPrintersList) {
                                if (getItem(position).printerName.contains(pName)) {
                                    mPrinterName = pName.replace("-", "_");
                                    break;
                                }
                            }
                            if(!NullChecker(getItem(position).macAddress).isEmpty()&&NullChecker(getItem(position).ip).isEmpty()){
                                // Toast.makeText(PrintersListActivity.this, "Not Implemented", Toast.LENGTH_LONG).show();
                                PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
                                if (!mPrinterName.isEmpty())
                                {
                                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                    myPrint.setBluetoothAdapter(bluetoothAdapter);
                                    NetPrinter printer=new NetPrinter();
                                    printer.modelName=NullChecker(mPrinterName);
                                    printer.serNo="";
                                    printer.ipAddress="";
                                    printer.macAddress=NullChecker(getItem(position).macAddress);
                                    setBrotherPrefereces(printer);
                                    myPrint.setPrinterInfo();
                                    Printer mPrinter = new Printer();
                                    mPrinter.getPrinterInfo();
                                    PrinterStatus printerStatus = mPrinter.getPrinterStatus();
                                    if(printerStatus.errorCode.name().equalsIgnoreCase("ERROR_BROTHER_PRINTER_NOT_FOUND")
                                            ||printerStatus.errorCode.name().equalsIgnoreCase("ERROR_CONNECT_SOCKET_FAILED"))
                                    {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                baseDialog.dismiss();
                                                Util.setCustomAlertDialog(PrintersListActivity.this);
                                                String msg="Printer is not paired please pair from Phone Settings -->Bluetooth\n" +
                                                        "If already in paired devices please unpair and pair again!";
                                                Util.openCustomDialog("Alert", msg);
                                                Util.txt_okey.setText("OK");
                                                Util.txt_dismiss.setVisibility(View.GONE);
                                                Util.txt_okey.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Util.alert_dialog.dismiss();
                                                    }
                                                });

                                            }
                                        });
                                    }else{
                                        PrinterDetails.selectedPrinterPrefrences.edit().putString(ZebraPrinter.SELECTED_PRINTER, "Brother").commit();
                                        PrinterDetails.selectedPrinterPrefrences.edit().putBoolean("isConnected", true).commit();
                                        PrinterDetails.selectedPrinterPrefrences.edit().putString("macAddress", NullChecker(getItem(position).macAddress)).commit();
                                        PrinterDetails.selectedPrinterPrefrences.edit().putString("printer",NullChecker(mPrinterName)).commit();
                                        PrinterDetails.selectedPrinterPrefrences.edit().putString(ZebraPrinter.PRINTERMODEL,NullChecker(mPrinterName)).commit();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                baseDialog.dismiss();
                                                ((BluetoothPrinterListAdapter) listView_bluetooth.getAdapter()).notifyDataSetChanged();//refreshAdapter(removeDuplicatePrinters(allPrintersList));
                                                // new GetZebraPrinterConfigTask(PrintersListActivity.this).execute();
                                            }
                                        });
                                    }}
                                else{
                                    BaseActivity.zebraPrinter = new ZebraPrinter();
                                    try {
                                        BaseActivity.zebraPrinter.createBTConnection(getItem(position).macAddress);
                                        if(BaseActivity.zebraPrinter.getBTConnection().isConnected()){
                                            setZebraPrefereces(zebraPrinter.getBTDeviceInfo(zebraPrinter.getBTConnection()));
                                            PrinterDetails.selectedPrinterPrefrences.edit().putString(ZebraPrinter.SELECTED_PRINTER, "zebra").commit();
                                            PrinterDetails.selectedPrinterPrefrences.edit().putBoolean("isConnected", true).commit();
                                            PrinterDetails.selectedPrinterPrefrences.edit().putString("macAddress", NullChecker(getItem(position).macAddress)).commit();
                                            PrinterDetails.selectedPrinterPrefrences.edit().putString("printer",NullChecker(getItem(position).printerName)).commit();
                                            //setPrefereces(getItem(position).mNetPrinter, "Zebra");
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    baseDialog.dismiss();
                                                    ((BluetoothPrinterListAdapter) listView_bluetooth.getAdapter()).notifyDataSetChanged();//refreshAdapter(removeDuplicatePrinters(allPrintersList));
                                                    // new GetZebraPrinterConfigTask(PrintersListActivity.this).execute();
                                                }
                                            });
                                        }else{
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    baseDialog.dismiss();
                                                    ((BluetoothPrinterListAdapter) listView_bluetooth.getAdapter()).notifyDataSetChanged();//refreshAdapter(removeDuplicatePrinters(allPrintersList));
                                                    // new GetZebraPrinterConfigTask(PrintersListActivity.this).execute();
                                                }
                                            });
                                            // BaseActivity.zebraPrinter.createBTConnection(getItem(position).macAddress);
                                        }

                                    } catch (ConnectionException e) {
                                        e.printStackTrace();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                baseDialog.dismiss();
                                            }
                                        });
                                    }

                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } });*/

            }catch (Exception e){
                e.printStackTrace();
            }

            return rowView;
        }
        public boolean createBond(BluetoothDevice btDevice)
                throws Exception
        {
            Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
            Method createBondMethod = class1.getMethod("createBond");
            Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
            return returnValue.booleanValue();
        }
        public List<PrinterDetails> getPrinterList(){
            return  printers;
        }

        public void refreshAdapter(ArrayList<PrinterDetails> printerList){
            printers=printerList;
            notifyDataSetChanged();
        }
        public class Holder {
            TextView printerName,txt_printer_ser_no;
            ImageView img_connected,img_printer_type;
            LinearLayout bluetooth_item_layout;
        }
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
            final Holder holder=new Holder();
            View rowView=null;
            try {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                rowView = inflater.inflate(R.layout.printer_list_item, null);
                holder.total_layout_printer = (LinearLayout) rowView.findViewById(R.id.total_layout_printer);
                holder.printerName = (TextView) rowView.findViewById(R.id.txt_printerName);
                holder.printerSerNo = (TextView) rowView.findViewById(R.id.txt_printer_ser_no);
                holder.printerImage = (ImageView) rowView.findViewById(R.id.img_printer_status);
                holder.networkTypeImage = (ImageView) rowView.findViewById(R.id.img_printer_type);
                holder.printerName.setText(getItem(position).printerName);
                String savedIPAddress = PrinterDetails.selectedPrinterPrefrences.getString("address", "");
                String savedMACAddress = PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_MAC, "");
                holder.printerImage.setVisibility(View.GONE);

                //holder.printerImage.setImageResource(R.drawable.red_circle_1);
                if (!getItem(position).ip.isEmpty()) {
                    holder.networkTypeImage.setVisibility(View.VISIBLE);
                    holder.networkTypeImage.setImageResource(R.drawable.img_wifi);
                    holder.printerSerNo.setText(AppUtils.NullChecker(getItem(position).mNetPrinter.ipAddress));

                } else if (!getItem(position).macAddress.isEmpty()) {
                    holder.networkTypeImage.setVisibility(View.VISIBLE);
                    holder.networkTypeImage.setImageResource(R.drawable.img_bluetooth);
                    holder.printerSerNo.setText(AppUtils.NullChecker(getItem(position).macAddress));
                } else {
                    holder.printerSerNo.setText("Connecting...");
                    holder.printerSerNo.setTextColor(getResources().getColor(R.color.orange_bg));
                }
                if (savedIPAddress.equals(getItem(position).ip) && !savedIPAddress.equals("")) {
                    holder.printerImage.setVisibility(View.VISIBLE);
                    holder.printerImage.setImageResource(R.drawable.green_circle_1);
                    PrinterDetails.selectedPrinterPrefrences.edit().putBoolean("isConnected", true).commit();
                    holder.printerSerNo.setText(AppUtils.NullChecker(getItem(position).mNetPrinter.ipAddress));

                }else if (savedMACAddress.equals(getItem(position).macAddress) && !savedMACAddress.equals("")) {
                    holder.printerImage.setVisibility(View.VISIBLE);
                    holder.printerImage.setImageResource(R.drawable.green_circle_1);
                    holder.printerSerNo.setText(AppUtils.NullChecker(getItem(position).macAddress));
                } else {
                    // holder.printerImage.setVisibility(View.GONE);
                    // holder.printerImage.setImageResource(R.drawable.red_circle_1);
                }

                holder.printerSerNo.setTextColor(getResources().getColor(R.color.gray_color));



            }catch (Exception e){
                e.printStackTrace();
                String message ="Something went wrong. Please try again";
                AppUtils.showError(PrintersListActivity.this, message,e);
            } holder.total_layout_printer.setTag(position);
            holder.total_layout_printer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int position = (int) holder.total_layout_printer.getTag();
                        for (String pName : brotherPrintersList) {
                            if (getItem(position).printerName.contains(pName)) {
                                mPrinterName = pName.replace("-", "_");
                                break;
                            }
                        }

                        if (!getItem(position).ip.isEmpty()) {
                            String printerName = PrinterDetails.selectedPrinterPrefrences.getString("printer", "");
                            if (printerName != null && printerName.equals(getItem(position).printerName)) {
                                if (getItem(position).isConnected) {
                                    printerDisconnectAlert(getItem(position));
                                } else {
                                    if (allPrintersList.size() > 0) {
                                        PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
                                        setPrefereces(getItem(position).mNetPrinter, "Brother");
                                        allPrintersList.get(position).isConnected = true;
                                        setBrotherPrinterListAdapter();
                                    }
                                }
                            } else {
                                PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
                                setPrefereces(getItem(position).mNetPrinter, "Brother");
                                allPrintersList.get(position).isConnected = true;
                                setBrotherPrinterListAdapter();
                            }
                        } else if(!getItem(position).macAddress.isEmpty()){
                            // Toast.makeText(PrintersListActivity.this, "Not Implemented", Toast.LENGTH_LONG).show();
                            PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
                            if (!mPrinterName.isEmpty())
                                setPrefereces(getItem(position).mNetPrinter, "Brother");
                            else {
                                BaseActivity.zebraPrinter = new ZebraPrinter();
                                try {
                                    setPrefereces(getItem(position).mNetPrinter, "Zebra");
                                    BaseActivity.zebraPrinter.createBTConnection();
                                    if(zebraPrinter.getBTConnection().isConnected()){
                                        setZebraPrefereces(zebraPrinter.getBTDeviceInfo(zebraPrinter.getBTConnection()));
                                    }

                                } catch (ConnectionException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } });
            return rowView;
        }

        public void refreshAdapter(ArrayList<PrinterDetails> pList) {
            try {
                printersList = pList;
                notifyDataSetChanged();
            }catch (Exception e){
                e.printStackTrace();
                String message ="Something went wrong. Please try again";
                AppUtils.showError(PrintersListActivity.this, message,e);
            }
        }

        public class Holder {
            TextView printerName,printerSerNo;
            ImageView printerImage,networkTypeImage;
            LinearLayout total_layout_printer;
        }
    }

    private void printerDisconnectAlert(final PrinterDetails selectedPrinter) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Util.setCustomAlertDialog(PrintersListActivity.this);
                    Util.openCustomDialog("Alert", "Do you want to disconnect the printer?");
                    Util.txt_okey.setText("DISCONNECT");
                    Util.txt_dismiss.setVisibility(View.VISIBLE);
                    Util.txt_dismiss.setText("CANCEL");
                    Util.txt_okey.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            //ShowTicketsDialog();
                            PrinterDetails printer = selectedPrinter;
                            printer.selectedPrinter = PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "");
                            printer.macAddress = PrinterDetails.selectedPrinterPrefrences.getString(ZEBRA_BT_MAC, "");
                            printer.ip = PrinterDetails.selectedPrinterPrefrences.getString(ZEBRA_WIFI_IP, "");
                            printer.isConnected = false;
                            PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
                            sharedPreferences.edit().clear().commit();
                            ArrayList pList = new ArrayList<PrinterDetails>();
                            pList.add(printer);
                            // allPrintersList.clear();
                            // allPrintersList.add(printer);
                            ZebraPrinter.allPrinterSettingValues.clear();
                            if(!printer.selectedPrinter.equalsIgnoreCase("Brother")&&!printer.ip.isEmpty()) {
                                ((PrinterListAdapter) brother_listview.getAdapter()).notifyDataSetChanged();//refreshAdapter(removeDuplicatePrinters(allPrintersList));
                            }else if(!printer.selectedPrinter.equalsIgnoreCase("zebra")&&!printer.ip.isEmpty()) {
                                ((ZebraPrinter.ZebraPrinterListAdapter)zebra_listview.getAdapter()).notifyDataSetChanged();//refreshAdapter(allPrintersList);
                            }else if(!printer.macAddress.isEmpty()){
                                ((BluetoothPrinterListAdapter) listView_bluetooth.getAdapter()).notifyDataSetChanged();
                            }
                            try {
                                if (zebraPrinter.getTCPConnection() != null)
                                    zebraPrinter.getTCPConnection().close();
                                if (zebraPrinter.getBTConnection() != null)
                                    zebraPrinter.getBTConnection().close();
                            } catch (Exception e) {
                                e.printStackTrace();
                                String message ="Something went wrong. Please try again";
                                AppUtils.showError(PrintersListActivity.this, message,e);
                            }
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
        }catch (Exception e){
            e.printStackTrace();
            String message ="Something went wrong. Please try again";
            // AppUtils.showError(PrintersListActivity.this, message,e);
        }
    }

    public void setZebraPrefereces(ZebraPrinterInfo mNetPrinter) {
        try {
            //initialization for print
            ZebraPrinter.tempZebraPrintWidth=0;
            ZebraPrinter.tempZebraLabelLength=0;
            PrinterDetails.selectedPrinterPrefrences.edit().clear().commit();
            SharedPreferences.Editor editor = PrinterDetails.selectedPrinterPrefrences.edit();
            editor.putString(ZebraPrinter.SELECTED_PRINTER, "Zebra");
            editor.putString(ZebraPrinter.ZEBRA_NAME, mNetPrinter.uniqueId);
            editor.putString(ZebraPrinter.ZEBRA_PORT, String.valueOf(mNetPrinter.port));
            editor.putString(ZebraPrinter.ZEBRA_WIFI_IP, mNetPrinter.ipAddr);
            editor.putString(ZebraPrinter.ZEBRA_WIFI_MAC, mNetPrinter.wifiMacAddr);
            editor.putString(ZEBRA_BT_MAC, mNetPrinter.btMacAddr);
            editor.putString(ZebraPrinter.ZEBRA_RESOLUTION, NullChecker(mNetPrinter.resolutiondpi));
            editor.putString(ZebraPrinter.ZEBRA_LABEL_LENGTH, NullChecker(mNetPrinter.labellenth));
            editor.putString(ZebraPrinter.ZEBRA_PRINT_WIDTH, NullChecker(mNetPrinter.labelwidth));
            editor.putString(ZebraPrinter.ZEBRA_MEDIATYPE, NullChecker(mNetPrinter.mediatype));
            if(!NullChecker(mNetPrinter.btMacAddr).isEmpty())
                editor.putString("printer", mNetPrinter.btMacAddr);
            else
                editor.putString("printer", mNetPrinter.ipAddr);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public PrinterDetails setSavedPrinterList(){
        PrinterDetails printerDetails=new PrinterDetails();
        final ArrayList<PrinterDetails> pList;
        printerDetails.isConnected= false;
        printerDetails.selectedPrinter= PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER,"");
        printerDetails.ip= PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP,"");
        printerDetails.macAddress= PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_MAC,"");
        printerDetails.printerName= PrinterDetails.selectedPrinterPrefrences.getString("printer","QL-720NW");

        NetPrinter mnetPrinter=new NetPrinter();
        mnetPrinter.macAddress=printerDetails.macAddress;
        mnetPrinter.ipAddress=printerDetails.ip;
        mnetPrinter.serNo=PrinterDetails.selectedPrinterPrefrences.getString("serNo","");
        mnetPrinter.modelName=PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.PRINTERMODEL,"");
        printerDetails.mNetPrinter=mnetPrinter;

        if(!printerDetails.ip.isEmpty()) {
            if (printerDetails.selectedPrinter.equalsIgnoreCase("Brother")) {
                pList = new ArrayList<>();
                pList.add(printerDetails);
                brother_listview.setAdapter(new PrinterListAdapter(PrintersListActivity.this, pList));
            } else if (printerDetails.selectedPrinter.equalsIgnoreCase("Zebra")) {
                pList = new ArrayList<>();
                pList.add(printerDetails);
                zebra_listview.setAdapter(new ZebraPrinter.ZebraPrinterListAdapter(pList, PrintersListActivity.this));
            }
        }
        return  printerDetails;
    }

    public PrinterDetails getSavedPrinterList(){
        PrinterDetails printerDetails=new PrinterDetails();
        final ArrayList<PrinterDetails> pList;
        printerDetails.isConnected= true;
        printerDetails.selectedPrinter= PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER,"");
        printerDetails.ip= PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP,"");
        printerDetails.printerName= PrinterDetails.selectedPrinterPrefrences.getString("printer","QL-720NW");
        return  printerDetails;
    }


//for bluetooth paired printer devices

    /**
     * get paired printers
     */
   /* private void getpaireddevices(){
        // get the BluetoothAdapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(enableBtIntent);
            }
        }else{
            return;
        }

        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        BluetoothDiscoverer.findPrinters(PrintersListActivity.this, new DiscoveryHandler() {
                            public Context context;
                            List<DiscoveredPrinterBluetooth> bluetoothprinters = new ArrayList<DiscoveredPrinterBluetooth>();
                            List<DiscoveredPrinter> printers = new ArrayList<DiscoveredPrinter>();
                            List<PrinterDetails> printersList = new ArrayList<PrinterDetails>();
                            public void foundPrinter(DiscoveredPrinter printer) {
                                String macAddress = printer.address;
                                printers.add(printer);
                                //I found a printer! I can use the properties of a Discovered printer (address) to make a Bluetooth Connection
                            }

                            public void discoveryFinished() {
                                for(DiscoveredPrinterBluetooth printer :  bluetoothprinters){
                                    System.out.println(printer);
                                }  //Discovery is done

                                // Toast.makeText(PrintersListActivity.this, "Bluetooth printer found - "+bluetoothprinters.size(), Toast.LENGTH_LONG).show();

                                for (DiscoveredPrinter printer : printers) {
                                    System.out.println(printer);
                                    PrinterDetails printerDetail=new PrinterDetails();
                                    printerDetail.macAddress=printer.address;
                        *//*if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP,"").equals(printer.address)) {
                            printerDetail.isConnected=true;
                        }*//*
                                    // printerDetail.setDiscoveredPrinter(printer);
                                    printersList.add(printerDetail);
                                    System.out.println("Bluetooth Discovered " + printers.size() + " printers.");


                                }
                            }

                            public void discoveryError(String message) {
                                //Error during discovery
                            }
                        });
                    } catch (ConnectionException e) {
                        e.printStackTrace();
                    }
                }
            });
          *//* new Thread(new Runnable() {
                @Override
                public void run() {
                    Printer printer = new Printer();
                    List<BLEPrinter> printerList = printer.getBLEPrinters(BluetoothAdapter.getDefaultAdapter(), 30);
                    for (BLEPrinter blprinter: printerList) {
                        Log.d("TAG", "Local Name: " + blprinter.localName);
                    }
                }
            }).start();
*//*
     *//*
     * if the paired devices exist, set the paired devices else set the
     * string of "No Bluetooth Printer."
     *//*
            btPrintersList.clear();
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter != null ? bluetoothAdapter.getBondedDevices() : null;
            if ((pairedDevices != null ? pairedDevices.size() : 0) > 0) {
                NetPrinter[] mBluetoothPrinter = new NetPrinter[pairedDevices.size()];
                int i = 0;
                for (BluetoothDevice device : pairedDevices) {
                    NetPrinter mnetPrinter=new NetPrinter();
                    PrinterDetails objDetails = new PrinterDetails();
                    objDetails.ip="";
                    objDetails.model=device.getName();
                    objDetails.printerName=device.getName();
                    objDetails.macAddress=device.getAddress();

                    mnetPrinter.macAddress=device.getAddress();
                    mnetPrinter.ipAddress="";
                    mnetPrinter.serNo="";
                    mnetPrinter.modelName=getModelName(device.getName());
                    objDetails.mNetPrinter=mnetPrinter;
                    Log.d(LOG_TAG, device.getName()+" : "+device.getAddress()+" : "+device.getType()+" : "+device.getBondState());

                    btPrintersList.add(objDetails);
                    i++;
                }
            }
            *//*this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    brother_listview.setAdapter(new PrinterListAdapter(PrintersListActivity.this,allPrintersList));
                }
            });*//*
            if(zebra_listview!=null) {
               *//* progress_zebra.setVisibility(View.VISIBLE);
                txt_ZebraPrinterFound.setText("Searching...");*//*
                zebraPrinter.discoverZebraPrinter(zebra_listview, PrintersListActivity.this);
            }
            setBTPrinterListAdapter();
        } catch (Exception e) {

        }
    }*/
    private void discoverbluetoothPrinters() {
        //btPrintersList.clear();
        progress_bar.setVisibility(View.VISIBLE);
        txt_PrinterFound.setVisibility(View.VISIBLE);
        txt_PrinterFound.setText("Searching Bluetooth Printers...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BluetoothDiscoverer.findPrinters(PrintersListActivity.this, new DiscoveryHandler() {
                        List<DiscoveredPrinter> printers = new ArrayList<DiscoveredPrinter>();
                        public void foundPrinter(DiscoveredPrinter printer) {
                            String macAddress = printer.address;
                            String name=printer.getDiscoveryDataMap().get("FRIENDLY_NAME");
                            printers.add(printer);
                            //I found a printer! I can use the properties of a Discovered printer (address) to make a Bluetooth Connection
                        }

                        public void discoveryFinished() {
                            for(DiscoveredPrinter printer :  printers){
                                PrinterDetails printerDetail=new PrinterDetails();
                                printerDetail.macAddress=printer.address;
                                printerDetail.printerName=printer.getDiscoveryDataMap().get("FRIENDLY_NAME");
                                btPrintersList.add(printerDetail);
                                System.out.println(printer);
                            }  //Discovery is done

                          /*  runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                   *//* baseDialog.setMessage("Connecting...");
                                    baseDialog.show();*//*
                                    Toast.makeText(PrintersListActivity.this, "Bluetooth printer found - "+printers.size(), Toast.LENGTH_LONG).show();
                                }
                            });*/
                            setBTPrinterListAdapter();
                        }
                        public void discoveryError(String message) {
                            //Error during discovery
                        }
                    });
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }}
        }).start();

    }

    private ArrayList<PrinterDetails> removeDuplicatePrinters(ArrayList<PrinterDetails> printers){
        ArrayList<PrinterDetails> pList=new ArrayList<>();
        ArrayList<PrinterDetails> wifiPList=new ArrayList<>();
        ArrayList<PrinterDetails> btPList=new ArrayList<>();
        for(int i=0;i<printers.size();i++){
            if(!printers.get(i).ip.isEmpty()) {
                wifiPList.add(printers.get(i));
            }else if(!printers.get(i).macAddress.isEmpty()){
                btPList.add(printers.get(i));
            }
        }

        pList.addAll(removeDuplicatesWIFIPrinters(wifiPList));
        pList.addAll(removeDuplicatesBTPrinters(btPList));
        allPrintersList.clear();
        allPrintersList.addAll(pList);
        return pList;
        //return printers;
    }

    /*private ArrayList<BLN_Member_c> removeDuplicateMembers(ArrayList<BLN_Member_c> childList){
        try{
            int count = childList.size();
            for (int i = 0; i < count; i++){
                for (int j = i + 1; j < count; j++){
                    if (childList.get(i).Email__c.equals(childList.get(j).Email__c)){
                        childList.remove(j--);
                        count--;
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return childList;
    }*/

    /*private ArrayList<BLN_Member_c> removeDuplicateMembers(ArrayList<BLN_Member_c> childList){
        try{
            int count = childList.size();
            for (int i = count; i > 0; i--){
                for (int j = i - 1; j > 0; j--){
                    if (childList.get(i).id.equals(childList.get(j).id)){
                        childList.remove(j++);
                        count++;
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return childList;
    }*/

    private ArrayList<PrinterDetails> removeDuplicatesWIFIPrinters(ArrayList<PrinterDetails> childList){
        try{
            int count = childList.size();
            for (int i = 0; i < count; i++){
                for (int j = i + 1; j < count; j++){
                    if (childList.get(i).ip.equals(childList.get(j).ip)){
                        childList.remove(j--);
                        count--;
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return childList;
    }

    private ArrayList<PrinterDetails> removeDuplicatesBTPrinters(ArrayList<PrinterDetails> childList){
        try{
            int count = childList.size();
            for (int i = 0; i < count; i++){
                for (int j = i + 1; j < count; j++){
                    if (childList.get(i).macAddress.equals(childList.get(j).macAddress)){
                        childList.remove(j--);
                        count--;
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return childList;
    }

    private void setBrotherPrinterListAdapter() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(toggle_wifi_print.isChecked()) {
                        listView_bluetooth.setVisibility(View.GONE);
                        brother_listview.setAdapter(new PrinterListAdapter(PrintersListActivity.this, removeDuplicatePrinters(allPrintersList)));
                        if (zebra_listview.getAdapter() != null)
                            ((ZebraPrinter.ZebraPrinterListAdapter) zebra_listview.getAdapter()).notifyDataSetChanged();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            //AppUtils.showError(PrinterListActivity.this,"Could not connect to printer");
            String message ="Something went wrong. Please try again";
            AppUtils.showError(PrintersListActivity.this, message,e);
        }
    }

    private void setBTPrinterListAdapter() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(toggle_bluetooth_print.isChecked()) {
                        if (btPrintersList.size() > 0) {
                            listView_bluetooth.setVisibility(View.VISIBLE);
                            //  txt_bluetooth.setVisibility(View.VISIBLE);
                        }
                        brother_listview.setVisibility(View.GONE);
                        listView_bluetooth.setVisibility(View.VISIBLE);
                        //if(listView_bluetooth.getAdapter()!=null)
                        listView_bluetooth.setAdapter(new BluetoothPrinterListAdapter(PrintersListActivity.this, removeDuplicatePrinters(btPrintersList)));
                        ((BluetoothPrinterListAdapter) listView_bluetooth.getAdapter()).notifyDataSetChanged();
                        bluetoothSearchFinished(btPrintersList.size());
                    }
                    /*else {
                        BluetoothPrinterListAdapter BluetoothPrinterListAdapter=
                                new BluetoothPrinterListAdapter(PrintersListActivity.this, removeDuplicatePrinters(btPrintersList));
                        listView_bluetooth.setAdapter(BluetoothPrinterListAdapter);
                        BluetoothPrinterListAdapter.notifyDataSetChanged();
                        listView_bluetooth.setVisibility(View.VISIBLE);
                    }*/
                    // bluetoothSearchFinished(btPrintersList.size());
                    /* if(zebra_listview.getAdapter()!=null)
                        ((ZebraPrinter.ZebraPrinterListAdapter) zebra_listview.getAdapter()).notifyDataSetChanged();*/
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            //AppUtils.showError(PrinterListActivity.this,"Could not connect to printer");
            String message ="Something went wrong. Please try again";
            //AppUtils.showError(PrintersListActivity.this, message,e);
        }
    }

    private String getModelName(String deviceName){
        for(String str:brotherPrintersList){
            if(deviceName.contains(str)){
                return str;
            }
        }
        return deviceName;//"QL-820NWB";
    }
    private void isPermissionAllowedorDenied(final Context context, String[] permission) {
        Dexter.withActivity(this)
                /*.withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
               */ .withPermissions(permission)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            discoverbluetoothPrinters();
                        }
                        // check for permanent denial of any permission

                        if (report.isAnyPermissionPermanentlyDenied()||report.getGrantedPermissionResponses().isEmpty()) {
                            // show alert dialog navigating to Settings
                            Util.setCustomAlertDialog(PrintersListActivity.this);
                            //app needs permission to use this feature. You can grant them in app settings.
                            Util.openCustomDialog("Alert", "ScanAttendee app needs this permission to enable printer search. Please grant them in app settings.");
                            Util.txt_okey.setText("App Settings");
                            Util.txt_dismiss.setText("Dismiss");
                            Util.txt_dismiss.setVisibility(View.VISIBLE);
                            Util.txt_okey.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View arg0) {
                                    Util.alert_dialog.dismiss();
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, 101);
                                }
                            });
                            Util.txt_dismiss.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Util.alert_dialog.dismiss();
                                    toggle_bluetooth_print.setChecked(false);
                                    toggle_wifi_print.setChecked(true);
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
    }


    /*private void doIsGoogleCloudPrint(boolean isChecked){

        if(isChecked) {
            txt_on_off_cloud_print.setText("Turned ON");
            txt_on_off_other_print.setText("Turned OFF");
            txt_on_off_other_print.setTextColor(getResources().getColor(R.color.gray_color));
            txt_on_off_cloud_print.setTextColor(getResources().getColor(R.color.green_connected));
            //lay_FitToPaper.setVisibility(View.VISIBLE);
            zebra_listview.setVisibility(View.GONE);
            brother_listview.setVisibility(View.GONE);
            //listView_bluetooth.setVisibility(View.GONE);
            toggle_cloud_print.setChecked(true);
            toggle_wifi_print.setChecked(false);
            txt_PrinterFound.setVisibility(View.GONE);
            progress_bar.setVisibility(View.GONE);
            check_FitToPaper.setChecked(PrinterDetails.selectedPrinterPrefrences.getBoolean(ZebraPrinter.FIT_TO_PAPER,false));
            setCloudPrefereces(false);
        } else {
            txt_on_off_cloud_print.setText("Turned OFF");
            txt_on_off_other_print.setText("Turned ON");
            txt_on_off_other_print.setTextColor(getResources().getColor(R.color.green_connected));
            txt_on_off_cloud_print.setTextColor(getResources().getColor(R.color.gray_color));
            // lay_FitToPaper.setVisibility(View.GONE);
            progress_bar.setVisibility(View.VISIBLE);
            txt_PrinterFound.setVisibility(View.VISIBLE);txt_PrinterFound
            txt_PrinterFound.setText("Searching...");
            check_FitToPaper.setChecked(false);
            zebra_listview.setVisibility(View.VISIBLE);
            brother_listview.setVisibility(View.VISIBLE);
            listView_bluetooth.setVisibility(View.VISIBLE);
            toggle_wifi_print.setChecked(true);
            toggle_cloud_print.setChecked(false);
            new SearchThread().start();

            //setCloudPrefereces(true);
        }
    }*/
    public void setWifiorBluetoothON(boolean iswifi){
        try {
            if(iswifi){
                toggle_cloud_print.setChecked(false);
                toggle_bluetooth_print.setChecked(false);
                isCloudPrintingON = false;
                isBlutoothPrintingON = false;
                progress_bar.setVisibility(View.VISIBLE);
                txt_PrinterFound.setVisibility(View.VISIBLE);
                txt_PrinterFound.setText("Searching Wifi Printers...");
                //  check_FitToPaper.setChecked(false);
                zebra_listview.setVisibility(View.VISIBLE);
                brother_listview.setVisibility(View.VISIBLE);
                listView_bluetooth.setVisibility(View.GONE);
                new SearchThread().start();
                if (zebraPrinter == null)
                    zebraPrinter = new ZebraPrinter();
                zebraPrinter.discoverZebraPrinter(zebra_listview, PrintersListActivity.this);
                txt_on_off_other_print.setVisibility(View.VISIBLE);

            }else {
                toggle_cloud_print.setChecked(false);
                toggle_wifi_print.setChecked(false);
                isBlutoothPrintingON=true;
                brother_listview.setVisibility(View.GONE);
                zebra_listview.setVisibility(View.GONE);
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(
                                BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(enableBtIntent);
                    } else {
                        if (AppUtils.isLocationPermissionGranted(PrintersListActivity.this)) {
                            isCloudPrintingON = false;
                            discoverbluetoothPrinters();
                            txt_on_off_bluetooth_print.setVisibility(View.VISIBLE);
                        } else {
                            txt_on_off_bluetooth_print.setVisibility(View.INVISIBLE);
                            isPermissionAllowedorDenied(PrintersListActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
                            //AppUtils.giveLocationpermission(PrintersListActivity.this);
                        }
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void openpairingerrorpopup(Context context){
        baseDialog.dismiss();
        Util.setCustomAlertDialog(context);
        String msg="Printer is not paired. Please pair from the device Settings --> Connections --> Bluetooth \n" +
                "If it is already paired with this device, Please unpair and pair it again!";
        Util.openCustomDialog("Alert", msg);
        Util.txt_okey.setText("OK");
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.txt_okey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.alert_dialog.dismiss();
            }
        });

    }
}

