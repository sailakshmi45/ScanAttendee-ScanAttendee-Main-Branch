package com.globalnest.printer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.globalnest.brother.ptouch.sdk.printdemo.common.MsgDialog;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.BadgeDataNew;
import com.globalnest.mvc.BadgeResponseNew;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.objects.EventObjects;
import com.globalnest.scanattendee.AttendeeDetailActivity;
import com.globalnest.scanattendee.BaseActivity;
import com.globalnest.scanattendee.GlobalScanActivity;
import com.globalnest.scanattendee.ManageTicketActivity;
import com.globalnest.scanattendee.OrderSucessPrintActivity;
import com.globalnest.scanattendee.PrintAttendeesListActivity;
import com.globalnest.scanattendee.PrintersListActivity;
import com.globalnest.scanattendee.R;
import com.globalnest.scanattendee.SelfCheckinAttendeeDetailActivity;
import com.globalnest.scanattendee.SellOrderActivity;
import com.globalnest.scanattendee.TransperantGlobalScanActivity;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.zebra.android.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.BluetoothConnectionInsecure;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.device.ZebraIllegalArgumentException;
import com.zebra.sdk.graphics.ZebraImageFactory;
import com.zebra.sdk.graphics.ZebraImageI;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveryException;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;
import com.zebra.sdk.printer.discovery.NetworkDiscoverer;
import com.zebra.sdk.settings.SettingsException;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.globalnest.scanattendee.BaseActivity.zebraPrinter;


/**
 * Created by mayank on 20/04/2017.
 */

public class ZebraPrinter {
    public static Map<String, String> allPrinterSettingValues=new HashMap<>();
    public static final String ZEBRA_NAME="zebra_name";
    public static final String ZEBRA_WIFI_IP="address";
    public static final String ZEBRA_PORT="zebra_port";
    public static final String ZEBRA_WIFI_MAC="macAddress";
    public static final String ZEBRA_BT_MAC="macAddress";//zebra_bt_mac
    public static final String PRINTERMODEL="printerModel";
    public static final String FIT_TO_PAPER="fit_to_paper";
    public static final String SELECTED_PRINTER="selected_printer";
    public static final String ZEBRA_PRINT_WIDTH="ezpl.print_width";
    public static final String ZEBRA_LABEL_LENGTH="zpl.label_length";
    public static final String ZEBRA_RESOLUTION="zpl.resolution.in_dpi";
    public static final String ZEBRA_MEDIATYPE="ezpl.media_type";
    private boolean isException=false;
    private static String errorMessage="";
    Context context;
    private static com.zebra.sdk.printer.ZebraPrinter zebraPrinterInstance=null;
    static ZebraPrinterLinkOs linkOsPrinter=null;
    public static TcpConnection tcpConnection=null;
    public static BluetoothConnection bluetoothConnection=null;
    public static Set<String> availableSettings=new HashSet<>();
    private ProgressDialog progressDialog;
    private ExternalSettings ext_settings;
    private   double zebraLabelLength=0;
    private  double zebraPrintWidth=0;

    public static int tempZebraLabelLength=0;
    public static int tempZebraPrintWidth=0;
    /*long startTime = 0,startPrintTime=0,startPrinterStatusTime=0;
    long endTime = 0,endPrinterStatusTime=0;*/
    //public static long  baseStartTime = 0;
    //private Context context;

    public ZebraPrinter(Context context){
        this.context=context;
        progressDialog=new ProgressDialog(context);
    }

    public ZebraPrinter(){
        //this.context=context;
    }

    public void createTCPConnection()throws ConnectionException {
        String ipAddress =PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "");
        String defaultPort=PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PORT, "");
        tcpConnection = new TcpConnection(ipAddress, Integer.parseInt(defaultPort));
        tcpConnection.open();

       /* try {
            int dots= ZebraPrinterFactory.getInstance(tcpConnection).getCurrentStatus().labelLengthInDots;
            AppUtils.displayLog("labelLengthInDots = ",""+dots);
        } catch (ZebraPrinterLanguageUnknownException e) {
            e.printStackTrace();
        }*/
    }

    public void createTCPConnection(Context context)throws ConnectionException {
        openProgressDialog(context);
        String ipAddress =PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "");
        String defaultPort=PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PORT, "");
        tcpConnection = new TcpConnection(ipAddress, Integer.parseInt(defaultPort));
        tcpConnection.open();

        try {
            int dots= ZebraPrinterFactory.getInstance(tcpConnection).getCurrentStatus().labelLengthInDots;
            AppUtils.displayLog("labelLengthInDots = ",""+dots);
        } catch (ZebraPrinterLanguageUnknownException e) {
            e.printStackTrace();
        }
        closeProgressDialog(context);
    }

    public void createTCPConnection(String ipAddress,String defaultPort )throws ConnectionException {
        tcpConnection = new TcpConnection(ipAddress, Integer.parseInt(defaultPort));
        tcpConnection.open();

        /*try {
            int dots= ZebraPrinterFactory.getInstance(tcpConnection).getCurrentStatus().labelLengthInDots;
            AppUtils.displayLog("labelLengthInDots = ",""+dots);
        } catch (ZebraPrinterLanguageUnknownException e) {
            e.printStackTrace();
        }*/
    }

    public void createBTConnection() throws ConnectionException {
        String bluetoothAddress =PrinterDetails.selectedPrinterPrefrences.getString(ZEBRA_BT_MAC, "");
        // bluetoothConnection = new BluetoothConnection(bluetoothAddress);
        if(bluetoothConnection!=null)
            bluetoothConnection.close();

        bluetoothConnection  = new BluetoothConnectionInsecure(bluetoothAddress);

        bluetoothConnection.open();
        bluetoothConnection.setMaxTimeoutForRead(10000);
    }
    public void CloseandReopenBTConnection() throws ConnectionException{
        if(bluetoothConnection!=null)
            bluetoothConnection.close();
        String bluetoothAddress =PrinterDetails.selectedPrinterPrefrences.getString(ZEBRA_BT_MAC, "");
        bluetoothConnection  = new BluetoothConnectionInsecure(bluetoothAddress);
        bluetoothConnection.open();
    }
    public void createBTConnection(String bluetoothAddress) throws ConnectionException {
        //bluetoothConnection = new BluetoothConnection(bluetoothAddress);
        bluetoothConnection  = new BluetoothConnectionInsecure(bluetoothAddress);

        bluetoothConnection.open();
        bluetoothConnection.setMaxTimeoutForRead(10000);
    }

    public TcpConnection getTCPConnection(){
        return tcpConnection;
    }

    public BluetoothConnection getBTConnection(){
        return bluetoothConnection;
    }

    public ZebraPrinterInfo getDeviceInfo(TcpConnection conn,String ipaddress)throws Exception{
        ZebraPrinterInfo zebraPrinterInfo=new ZebraPrinterInfo();
        //conn.open();.
        if (conn.isConnected()) {
            //zebraPrinterInfo.uniqueId = SGD.GET("device.unique_id", conn);
            //zebraPrinterInfo.bluetoothDiscoverable = SGD.GET("bluetooth.discoverable", conn);
            //zebraPrinterInfo.btMacAddr = SGD.GET("bluetooth.short_address", conn);
            //            zebraPrinterInfo.ipAddr = SGD.GET("ip.addr", conn);
            zebraPrinterInfo.uniqueId = "";
            zebraPrinterInfo.btMacAddr = "";
            zebraPrinterInfo.ipAddr = ipaddress;
            zebraPrinterInfo.port= Integer.parseInt("9100");
            // }zebraPrinterInfo.port = Integer.valueOf(SGD.GET("ip.port", conn));
            zebraPrinterInfo.wifiMacAddr = "";
            zebraPrinterInfo.mediatype =   SGD.GET("ezpl.media_type",conn);
            //            zebraPrinterInfo.wifiMacAddr = SGD.GET("wlan.mac_raw",conn);
           /* if(SGD.GET("head.resolution.in_dpi",conn).equalsIgnoreCase("203")
                    ||SGD.GET("head.resolution.in_dpi",conn).equalsIgnoreCase("300")
                    ||SGD.GET("head.resolution.in_dpi",conn).equalsIgnoreCase("600"))*/
            if(!Util.NullChecker(SGD.GET("head.resolution.in_dpi",conn)).equalsIgnoreCase("?"))
                zebraPrinterInfo.resolutiondpi = SGD.GET("head.resolution.in_dpi",conn);
            else
                zebraPrinterInfo.resolutiondpi = "203";
            //To save the badge values as zebra prefrences
          /*  if(Util.NullChecker(zebraPrinterInfo.labelwidth = SGD.GET("ezpl.print_width",conn)).equals("?")
            ||Util.NullChecker(zebraPrinterInfo.labellenth = SGD.GET("zpl.label_length",conn)).equals("?")) {
                EventObjects checkedin_event_record = Util.db.getSelectedEventRecord(BaseActivity.checkedin_event_record.Events.Id);
                String where_att = " Where " + DBFeilds.BADGE_NEW_EVENT_ID + " = '"
                        + checkedin_event_record.Events.Id + "' AND " + DBFeilds.BADGE_NEW_ID
                        + " = '" + checkedin_event_record.Events.Mobile_Default_Badge__c + "'";
                ArrayList<BadgeResponseNew> badge_res = Util.db.getAllBadges(where_att);
                if (badge_res.size() > 0) {
                    BadgeDataNew badge_data = new Gson().fromJson(badge_res.get(0).badge.Data__c, BadgeDataNew.class);
                    if(Util.NullChecker(zebraPrinterInfo.labelwidth = SGD.GET("ezpl.print_width",conn)).equals("?"))
                    zebraPrinterInfo.labelwidth = String.valueOf(badge_data.canvasWidth * Integer.valueOf(zebraPrinterInfo.resolutiondpi));
                    if(Util.NullChecker(zebraPrinterInfo.labellenth = SGD.GET("zpl.label_length",conn)).equals("?"))
                    zebraPrinterInfo.labellenth = String.valueOf(badge_data.canvasHeight * Integer.valueOf(zebraPrinterInfo.resolutiondpi));

                }
            }*/
            //if(!Util.NullChecker(zebraPrinterInfo.labelwidth = SGD.GET("ezpl.print_width",conn)).equals("?"))
            zebraPrinterInfo.labelwidth = SGD.GET("ezpl.print_width",conn);
            // if(!Util.NullChecker(zebraPrinterInfo.labellenth = SGD.GET("zpl.label_length",conn)).equals("?"))
            zebraPrinterInfo.labellenth = SGD.GET("zpl.label_length",conn);


        }
        Thread.sleep(500);
        //conn.close();
        return zebraPrinterInfo;
    }
    public ZebraPrinterInfo getBTDeviceInfo(BluetoothConnection conn)throws Exception{
        ZebraPrinterInfo zebraPrinterInfo=new ZebraPrinterInfo();
        //conn.open();
        if (conn.isConnected()) {
            //zebraPrinterInfo.uniqueId = SGD.GET("device.unique_id", conn);
            //zebraPrinterInfo.bluetoothDiscoverable = SGD.GET("bluetooth.discoverable", conn);
            // zebraPrinterInfo.btMacAddr = SGD.GET("bluetooth.short_address", conn);//"bluetooth.address
            zebraPrinterInfo.btMacAddr = SGD.GET("bluetooth.address", conn);
            //            zebraPrinterInfo.ipAddr = SGD.GET("ip.addr", conn);
            zebraPrinterInfo.uniqueId = "";
            //zebraPrinterInfo.btMacAddr = "";
            zebraPrinterInfo.ipAddr = "";
            zebraPrinterInfo.port= Integer.parseInt("9100");
            zebraPrinterInfo.friendlyname= SGD.GET("bluetooth.friendly_name",conn);
            // }zebraPrinterInfo.port = Integer.valueOf(SGD.GET("ip.port", conn));
            zebraPrinterInfo.wifiMacAddr = "";
            //            zebraPrinterInfo.wifiMacAddr = SGD.GET("wlan.mac_raw",conn);
           /* if(SGD.GET("head.resolution.in_dpi",conn).equalsIgnoreCase("203")
                    ||SGD.GET("head.resolution.in_dpi",conn).equalsIgnoreCase("300")
                    ||SGD.GET("head.resolution.in_dpi",conn).equalsIgnoreCase("600"))*/
            zebraPrinterInfo.labelwidth = SGD.GET("ezpl.print_width",conn);
            if(!Util.NullChecker(zebraPrinterInfo.labellenth = SGD.GET("ezpl.print_width",conn)).isEmpty());
            zebraPrinterInfo.labellenth = SGD.GET("zpl.label_length",conn);
            if(!Util.NullChecker(SGD.GET("head.resolution.in_dpi",conn)).equalsIgnoreCase("?"))
                zebraPrinterInfo.resolutiondpi = SGD.GET("head.resolution.in_dpi",conn);
            else
                zebraPrinterInfo.resolutiondpi = "203";

        }
        Thread.sleep(500);
        //conn.close();
        return zebraPrinterInfo;
    }

    public void print (TcpConnection conn, final Context context, FrameLayout frameLayout)throws Exception {
        //conn.open();
        //startPrintTime=System.currentTimeMillis();
        if (findPrinterStatus(conn,context)) {
            //endPrinterStatusTime=System.currentTimeMillis();
            frameLayout.setDrawingCacheEnabled(true);
            frameLayout.buildDrawingCache(true);
            frameLayout.buildDrawingCache();
            Bitmap tempBitmap = frameLayout.getDrawingCache();
            ZebraImageI zebraImage = ZebraImageFactory.getImage(/*RotateBitmap(*/tempBitmap/*,90)*/);
            if(ZebraPrinter.tempZebraPrintWidth==0 ||ZebraPrinter.tempZebraLabelLength==0) {
                setImageHeightWidth();
                ZebraPrinterFactory.getInstance(conn).printImage(zebraImage, 0, 0, (int)zebraPrintWidth, (int)zebraLabelLength, false);
            }else {
                ZebraPrinterFactory.getInstance(conn).printImage(zebraImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
            }
            closeProgressDialog(context);
            //endTime = System.currentTimeMillis();
            NumberFormat formatter = new DecimalFormat("#0.00000");

            //showPrinterErrorDialog(context, "Printed Completed", true);
                /*showPrinterErrorDialog(context, "Printed Completed "+tempZebraPrintWidth+" = "+tempZebraLabelLength+" total time ="+formatter.format((endTime - startTime) / 1000d)+ " sec Print() time= "+
                        formatter.format((endTime - startPrintTime) / 1000d)+ " sec printerStatus() time= "+formatter.format((endPrinterStatusTime - startPrinterStatusTime) / 1000d)+ " sec "+formatter.format((endTime - baseStartTime) / 1000d),true);*/

            afterPrintComplete(context);
        }
        Thread.sleep(500);
        //conn.close();

    }

    public void print (TcpConnection conn, final Context context, ArrayList<String> mFiles)throws Exception {
        //conn.open();
        if (findPrinterStatus(conn,context)) {
            setImageHeightWidth();
            if(zebraPrintWidth==0||zebraLabelLength==0){
                closeProgressDialog(context);
                showPrinterErrorDialog(context, "Wrong Lable",false);
            }else if(zebraPrintWidth==-1||zebraLabelLength==-1){
                closeProgressDialog(context);
                showPrinterErrorDialog(context, "No Badge Selected",false);
            }else {
                //setPrinterSetting(conn);
                /*if(zebraPrinterInstance.getCurrentStatus().labelLengthInDots!=zebraLabelLength)
                    setPrinterSetting(conn, ZebraPrinter.ZEBRA_LABEL_LENGTH, String.valueOf(zebraLabelLength));*/
                zebraPrinterInstance=ZebraPrinterFactory.getInstance(conn);
                for (String filePath : mFiles) {
                    Bitmap tempBitmap = BitmapFactory.decodeFile(filePath);
                    if(tempBitmap!=null) {
                        ZebraImageI zebraImage = ZebraImageFactory.getImage(tempBitmap);
                        if(ZebraPrinter.tempZebraPrintWidth==0 ||ZebraPrinter.tempZebraLabelLength==0) {
                            setImageHeightWidth();
                            zebraPrinterInstance.printImage(zebraImage, 0, 0, (int)zebraPrintWidth, (int)zebraLabelLength, false);
                            // displayToast("Width-"+zebraPrintWidth+"|"+"Length-"+zebraLabelLength,context);
                        }else {
                            zebraPrinterInstance.printImage(zebraImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                            //displayToast("Width-"+ZebraPrinter.tempZebraPrintWidth+"|"+"Length-"+ZebraPrinter.tempZebraLabelLength,context);
                        }
                    }
                }

                closeProgressDialog(context);
                //showPrinterErrorDialog(context, "Printed Completed",true);
                afterPrintComplete(context);
            }
        }
        Thread.sleep(500);
        deleteDir();
        //conn.close();
    }

    public void print (BluetoothConnection conn, Context context, FrameLayout frameLayout)throws Exception {
        //conn.open();
        if (findPrinterStatus(conn,context)) {
            //endPrinterStatusTime=System.currentTimeMillis();
            frameLayout.setDrawingCacheEnabled(true);
            frameLayout.buildDrawingCache(true);
            frameLayout.buildDrawingCache();
            Bitmap tempBitmap = frameLayout.getDrawingCache();
            ZebraImageI zebraImage = ZebraImageFactory.getImage(/*RotateBitmap(*/tempBitmap/*,90)*/);
            if(ZebraPrinter.tempZebraPrintWidth==0 ||ZebraPrinter.tempZebraLabelLength==0) {
                setImageHeightWidth();
                ZebraPrinterFactory.getInstance(conn).printImage(zebraImage, 0, 0, (int)zebraPrintWidth, (int)zebraLabelLength, false);
            }else {
                ZebraPrinterFactory.getInstance(conn).printImage(zebraImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
            }
            closeProgressDialog(context);
            //endTime = System.currentTimeMillis();
            //showPrinterErrorDialog(context, "Printed Completed", true);
                /*showPrinterErrorDialog(context, "Printed Completed "+tempZebraPrintWidth+" = "+tempZebraLabelLength+" total time ="+formatter.format((endTime - startTime) / 1000d)+ " sec Print() time= "+
                        formatter.format((endTime - startPrintTime) / 1000d)+ " sec printerStatus() time= "+formatter.format((endPrinterStatusTime - startPrinterStatusTime) / 1000d)+ " sec "+formatter.format((endTime - baseStartTime) / 1000d),true);*/

            afterPrintComplete(context);
        }
        Thread.sleep(500);

        //conn.close();
    }

    public void print (BluetoothConnection conn, Context context, ArrayList<String> mFiles)throws Exception {
        //conn.open();
        if (findPrinterStatus(conn,context)) {
            setImageHeightWidth();
            if(zebraPrintWidth==0||zebraLabelLength==0){
                closeProgressDialog(context);
                showPrinterErrorDialog(context, "Wrong Lable",false);
            }else if(zebraPrintWidth==-1||zebraLabelLength==-1){
                closeProgressDialog(context);
                showPrinterErrorDialog(context, "No Badge Selected",false);
            }else {
                //setPrinterSetting(conn, ZebraPrinter.ZEBRA_PRINT_WIDTH, String.valueOf(zebraPrintWidth));
                //setPrinterSetting(conn);
                zebraPrinterInstance=ZebraPrinterFactory.getInstance(conn);
                for (String filePath : mFiles) {
                    Bitmap tempBitmap = BitmapFactory.decodeFile(filePath);
                    if(tempBitmap!=null) {
                        ZebraImageI zebraImage = ZebraImageFactory.getImage(tempBitmap);
                        if(ZebraPrinter.tempZebraPrintWidth==0 ||ZebraPrinter.tempZebraLabelLength==0) {
                            setImageHeightWidth();
                            zebraPrinterInstance.printImage(zebraImage, 0, 0, (int)zebraPrintWidth, (int)zebraLabelLength, false);
                            // displayToast("Width-"+zebraPrintWidth+"|"+"Length-"+zebraLabelLength,context);
                        }else {
                            zebraPrinterInstance.printImage(zebraImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                            //displayToast("Width-"+ZebraPrinter.tempZebraPrintWidth+"|"+"Length-"+ZebraPrinter.tempZebraLabelLength,context);
                        }
                    }
                }

                closeProgressDialog(context);
                //showPrinterErrorDialog(context, "Printed Completed",true);
                afterPrintComplete(context);
                /*zebraPrinterInstance=ZebraPrinterFactory.getInstance(conn);
                for (String filePath : mFiles) {
                    Bitmap tempBitmap = BitmapFactory.decodeFile(filePath);
                    ZebraImageI zebraImage = ZebraImageFactory.getImage(tempBitmap);
                    zebraPrinterInstance.printImage(zebraImage, 0, 0, (int)zebraPrintWidth, (int)zebraLabelLength, false);
                }
                deleteDir();
                closeProgressDialog(context);
                if(((BaseActivity) context) instanceof GlobalScanActivity){
                    ((GlobalScanActivity) context).docheckinprocessafterprint("Print Completed");
                }else {
                    showPrinterErrorDialog(context, "Printed Completed",true);}*/

            }
        }
        Thread.sleep(500);
        //conn.close();
    }

    private boolean findPrinterStatus(final Connection conn, final Context context)throws Exception{
        // startPrinterStatusTime=System.currentTimeMillis();
        SGD.SET("device.languages", "zpl", conn);
        zebraPrinterInstance= /*new ZebraPrinterZpl(conn);//*/ZebraPrinterFactory.getInstance(conn);
        PrinterStatus status=zebraPrinterInstance.getCurrentStatus();
        if(!status.isReadyToPrint) {
            String msg="";
            if (status.isHeadOpen) {
                if(context instanceof GlobalScanActivity){
                    ((GlobalScanActivity) context).docheckinprocessafterprint("ERROR: Printer Head is Open");
                }else {
                    showPrinterErrorDialog(context, "ERROR: Printer Head is Open", false);
                }
                return false;
            } else if (status.isPaused) {
                if(context instanceof GlobalScanActivity){
                    ((GlobalScanActivity) context).docheckinprocessafterprint("ERROR: Printer is Paused");
                }else {
                    showPrinterErrorDialog(context, "ERROR: Printer is Paused", false);
                }
                return false;
            } else if (status.isPaperOut) {
                if(context instanceof GlobalScanActivity){
                    ((GlobalScanActivity) context).docheckinprocessafterprint("ERROR: No Media Detected");
                }else {
                    showPrinterErrorDialog(context, "ERROR: No Media Detected", false);
                }
                return false;
            }
        }else{
            return true;
        }
        //showToast(context,"Status detected true ....");
        return true;
    }

    public static boolean isPrinterReadyToPrint(Connection conn) throws ConnectionException, ZebraPrinterLanguageUnknownException {
        if(conn==null)
            return false;
        return ZebraPrinterFactory.getInstance(conn).getCurrentStatus().isReadyToPrint;
    }

    private void displayToast(final String message,final Context context) {
        try {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast;
                    toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static class discoverbluetooth extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            try {

                BluetoothDiscoverer.findPrinters(PrintersListActivity.baseContext, new DiscoveryHandler() {
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

                        Toast.makeText((PrintersListActivity.baseContext), "Bluetooth printer found - "+bluetoothprinters.size(), Toast.LENGTH_LONG).show();

                        for (DiscoveredPrinter printer : printers) {
                            System.out.println(printer);
                            PrinterDetails printerDetail=new PrinterDetails();
                            printerDetail.macAddress=printer.address;
                        /*if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP,"").equals(printer.address)) {
                            printerDetail.isConnected=true;
                        }*/
                            // printerDetail.setDiscoveredPrinter(printer);
                            printersList.add(printerDetail);
                            System.out.println("Bluetooth Discovered " + bluetoothprinters.size() + " printers.");


                        }
                    }

                    public void discoveryError(String message) {
                        //Error during discovery
                    }
                });
            } catch (ConnectionException e) {
                e.printStackTrace();
            } return null;
        }
    }
    public void discoverZebraPrinter(final ListView listView, final Context context) {
        DiscoveryHandler discoveryHandler = new DiscoveryHandler() {
            List<DiscoveredPrinter> printers = new ArrayList<DiscoveredPrinter>();
            List<PrinterDetails> printersList = new ArrayList<PrinterDetails>();
            public void foundPrinter(DiscoveredPrinter printer) {
                printers.add(printer);
            }

            public void discoveryFinished() {
                for (DiscoveredPrinter printer : printers) {
                    System.out.println(printer);
                    PrinterDetails printerDetail=new PrinterDetails();
                    printerDetail.ip=printer.address;
                    if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP,"").equals(printer.address)) {
                        printerDetail.isConnected=true;
                    }
                    // printerDetail.setDiscoveredPrinter(printer);
                    printersList.add(printerDetail);

                }
                ((PrintersListActivity)context).zebraSearchFinish(printersList);
                System.out.println("Discovered " + printersList.size() + " printers.");
            }

            public void discoveryError(String message) {
                System.out.println("An error occurred during discovery : " + message);
            }
        };
        try {
            System.out.println("Starting printer discovery.");
            NetworkDiscoverer.findPrinters(discoveryHandler);
        } catch (DiscoveryException e) {
            e.printStackTrace();
        }
    }

    public static class ZebraPrinterListAdapter extends BaseAdapter {
        List<PrinterDetails> printers;
        Context context;
        public ZebraPrinterListAdapter(List<PrinterDetails> printers, Context context){
            this.printers=printers;
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
            try{
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView =inflater.inflate(R.layout.printer_list_item,null);
                TextView txt_name= (TextView) convertView.findViewById(R.id.txt_printerName);
                ImageView img_connected=(ImageView)convertView.findViewById(R.id.img_printer_status) ;
                ImageView img_printer_type=(ImageView)convertView.findViewById(R.id.img_printer_type);
                TextView txt_printer_ser_no=(TextView)convertView.findViewById(R.id.txt_printer_ser_no);
                txt_printer_ser_no.setVisibility(View.VISIBLE);
                String savedIPAddress = PrinterDetails.selectedPrinterPrefrences.getString("address", "");
                String savedMACAddress = PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_MAC, "");

            /*if(AppUtils.NullChecker(getItem(position).discoveredPrinter.address).isEmpty()){
                txt_name.setText(getItem(position).getIp());
            }else {*/
                txt_name.setText("Zebra ("+getItem(position).ip+")");

                // }

                if(!getItem(position).ip.isEmpty()){
                    img_printer_type.setVisibility(View.VISIBLE);
                    img_printer_type.setImageResource(R.drawable.img_wifi);
                }else if (!getItem(position).macAddress.isEmpty()) {
                    img_printer_type.setVisibility(View.VISIBLE);
                    img_printer_type.setImageResource(R.drawable.img_bluetooth);
                    txt_printer_ser_no.setText(AppUtils.NullChecker(getItem(position).macAddress));
                } else{
                    img_printer_type.setVisibility(View.GONE);
                    img_printer_type.setImageResource(R.drawable.print);
                }

                if (savedIPAddress.equals(getItem(position).ip) && !savedIPAddress.equals("")) {
                    img_connected.setVisibility(View.VISIBLE);
                    img_connected.setImageResource(R.drawable.green_circle_1);
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

                    txt_printer_ser_no.setText("Length: "+str_length
                            +" | Width: "+str_width
                            +" | DPI: "+dpi
                            +" | Media Type: "+PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_MEDIATYPE, "")
                            +" | IP: "+savedIPAddress);
                }else if (savedMACAddress.equals(getItem(position).macAddress) && !savedMACAddress.equals("")) {
                    img_connected.setVisibility(View.VISIBLE);
                    img_connected.setImageResource(R.drawable.green_circle_1);
                }else {
                    img_connected.setVisibility(View.GONE);
                    img_connected.setImageResource(R.drawable.red_circle_1);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return convertView;
        }

        public List<PrinterDetails> getPrinterList(){
            return  printers;
        }

        public void refreshAdapter(ArrayList<PrinterDetails> printerList){
            printers=printerList;
            notifyDataSetChanged();
        }
    }

    public static String getErrorFromException(Exception e){
        return e.getMessage()+String.valueOf(e.getStackTrace()[0].getLineNumber());
    }

    public void doZebraPrint(final Context context, final FrameLayout print_badge) {
        try {
            //startTime = System.currentTimeMillis();
            //ZebraPrinter zebraPrinter=new ZebraPrinter();
            getExternalSettings();
            openProgressDialog(context);
            if(zebraPrinter!=null) {
                if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP,"").isEmpty()) {
                    if (zebraPrinter.getTCPConnection().isConnected()) {
                        if(!ext_settings.doubleSide_badge)
                            print(getTCPConnection(), context, print_badge);
                        else
                            printMirrorImage(getTCPConnection(),context,print_badge);
                    } else {
                        //showToast(context,"Connecting port  ...."+PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PORT, ""));
                        tcpConnection = new TcpConnection(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, ""), Integer.parseInt(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PORT, "")));
                        tcpConnection.open();
                        if(!ext_settings.doubleSide_badge)
                            print(getTCPConnection(), context, print_badge);
                        else
                            printMirrorImage(getTCPConnection(),context,print_badge);
                    }
                    /*tcpConnection = new TcpConnection(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, ""), Integer.parseInt(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PORT, "")));
                    tcpConnection.open();
                    if (getTCPConnection().isConnected()) {
                        print(getTCPConnection(), context, print_badge);
                    } else {
                        showPrinterErrorDialog(context, "Problem with creating printer connection.");
                    }*/
                }else{
                    doBluetoothZebraPrint(context,print_badge);
                }
            }else{
                closeProgressDialog(context);
                showPrinterErrorDialog(context,"Printer not connected.",false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            zebraPrinterInstance=null;
            linkOsPrinter=null;
            availableSettings=new HashSet<>();
            closeProgressDialog(context);
            errorMessage=getErrorFromException(e);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            errorMessage=sw.toString();
            if(errorMessage.trim().isEmpty()){
                errorMessage=getErrorFromException(e);
            }
            try{
                if (findPrinterStatus(getTCPConnection(),context)) {
                    //showPrinterErrorDialog(context,"Printer not found. Please check your internet connection",false);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            openprinterNotConnectedDialog(context, print_badge, null);
                        }
                    });
                }
            }catch(Exception e1){
                errorMessage=getErrorFromException(e);
                sw = new StringWriter();
                pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                errorMessage=sw.toString();
                if(errorMessage.trim().isEmpty()){
                    errorMessage=getErrorFromException(e);
                }
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openprinterNotConnectedDialog(context, print_badge, null);
                    }
                });

            }
        }
    }

    public void doZebraPrint(final Context context, final ArrayList<String> mFilesPath) {
        try {
            //ZebraPrinter zebraPrinter=new ZebraPrinter();
            getExternalSettings();
            //startTime = System.currentTimeMillis();
            openProgressDialog(context);

            if(zebraPrinter!=null) {
                if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP,"").isEmpty()) {
                    if (zebraPrinter.getTCPConnection().isConnected()) {
                        if(!ext_settings.doubleSide_badge)
                            print(getTCPConnection(), context, mFilesPath);
                        else
                            printMirrorImage(getTCPConnection(), context, mFilesPath);
                    } else {
                        tcpConnection = new TcpConnection(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, ""), Integer.parseInt(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PORT, "")));
                        tcpConnection.open();
                        if(!ext_settings.doubleSide_badge)
                            print(getTCPConnection(), context, mFilesPath);
                        else
                            printMirrorImage(getTCPConnection(), context, mFilesPath);
                    }
                }else{
                    doBluetoothZebraPrint(context,mFilesPath);
                }
            }else{
                closeProgressDialog(context);
                showPrinterErrorDialog(context,"Printer not connected.",false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            tempZebraPrintWidth=0;
            tempZebraLabelLength=0;
            zebraPrinterInstance=null;
            linkOsPrinter=null;
            availableSettings=new HashSet<>();
            closeProgressDialog(context);
            //showPrinterErrorDialog(context, "Something went wrong. Please reprint badge", false);
            errorMessage=getErrorFromException(e);
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    openprinterNotConnectedDialog(context,null,mFilesPath);
                }
            });


        }
    }

    private void doBluetoothZebraPrint(Context context, FrameLayout print_badge) throws Exception {
        //ZebraPrinter zebraPrinter=new ZebraPrinter();
        if (zebraPrinter.getBTConnection()!=null) {
            if(zebraPrinter.getBTConnection().isConnected()){
            /*bluetoothConnection = new BluetoothConnection(PrinterDetails.selectedPrinterPrefrences.getString("macAddress", ""));
            bluetoothConnection.open();*/
                if(!ext_settings.doubleSide_badge)
                    print(bluetoothConnection, context, print_badge);
                else {
                    printBluetoothMirrorImage(bluetoothConnection, context, print_badge);
                }
            }
        }else {
            CloseandReopenBTConnection();
            if (zebraPrinter.getBTConnection() != null) {
                if (zebraPrinter.getBTConnection().isConnected()) {
                    if (!ext_settings.doubleSide_badge)
                        print(bluetoothConnection, context, print_badge);
                    else {
                        printBluetoothMirrorImage(bluetoothConnection, context, print_badge);
                    }
                }
            }
        }

        //showPrinterErrorDialog(context, "Printed Completed");
    }

    private void doBluetoothZebraPrint(Context context, ArrayList<String> mFiles) throws Exception {
        //ZebraPrinter zebraPrinter=new ZebraPrinter();
        if (zebraPrinter.getBTConnection()!=null) {
            if(zebraPrinter.getBTConnection().isConnected()){
                if(!ext_settings.doubleSide_badge)
                    print(bluetoothConnection, context, mFiles);
                else {
                    printBluetoothMirrorImage(bluetoothConnection, context, mFiles);
                }
            }
        }else{
            CloseandReopenBTConnection();
            if (zebraPrinter.getBTConnection()!=null) {
                if (zebraPrinter.getBTConnection().isConnected()) {
                    if (!ext_settings.doubleSide_badge) {
                        print(bluetoothConnection, context, mFiles);
                    } else {
                        printBluetoothMirrorImage(bluetoothConnection, context, mFiles);
                    }
                }
            }
        }

    }



    private static void openProgressDialog(Context context) {
        try {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BaseActivity.baseDialog.setMessage("Printing Badge...");
                    BaseActivity.baseDialog.show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void openProgressDialog(Context context,final String message) {
        try {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BaseActivity.baseDialog.setMessage(message);
                    BaseActivity.baseDialog.show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void closeProgressDialog(Context context) {
        try{
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //BaseActivity.baseDialog.setMessage("Printing Badge...");
                    BaseActivity.baseDialog.dismiss();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void showPrinterErrorDialog(final Context context, final String message, final boolean isFinish){
        try{
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Util.setCustomAlertDialog(context);
                    Util.openCustomDialog("Alert", message);
                    //Util.alert_dialog.setCancelable(false);
                    Util.txt_okey.setText("Ok");
                    Util.txt_dismiss.setVisibility(View.GONE);
                    if(context instanceof PrintAttendeesListActivity){
                        Util.alert_dialog.setCancelable(false);
                    }
                    Util.txt_okey.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            //ShowTicketsDialog();
                            //doPrint();
                            Util.alert_dialog.dismiss();
                            if(context instanceof TransperantGlobalScanActivity||context instanceof AttendeeDetailActivity){
                                ((Activity) context).finish();
                            }else if(context instanceof GlobalScanActivity&& BaseActivity.ordersuccess_popupok_clicked)
                            {
                                Intent i = new Intent(context, ManageTicketActivity.class);
                                BaseActivity.ordersuccess_popupok_clicked=false;
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(i);
                                //activity.finish();

                            } else if(context instanceof PrintAttendeesListActivity){
                                Intent i=null;
                                i = new Intent(context, PrintAttendeesListActivity.class);
                                i.putExtra("fromPrintSucess","printedSuccesssfully");
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(i);
                            }else if(context instanceof SellOrderActivity){
                                Intent i = new Intent(context, ManageTicketActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(i);
                            }else if(context instanceof OrderSucessPrintActivity){
                                ((Activity) context).finish();
                               /* Intent i = new Intent(context, ManageTicketActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(i);*/
                            }else if(context instanceof GlobalScanActivity){
                                if (AppUtils.NullChecker(MsgDialog.intent_value).equalsIgnoreCase(SellOrderActivity.class.getName())) {
                                    Intent i = new Intent(context, ManageTicketActivity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    context.startActivity(i);
                                }
                                ((Activity) context).finish();
                            }
                            if(isFinish)
                                ((Activity) context).finish();
                        }
                    });
                    if(BaseActivity.baseDialog.isShowing())
                        closeProgressDialog(context);

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void displaySettings(Connection c) throws ConnectionException, ZebraPrinterLanguageUnknownException, SettingsException, ZebraIllegalArgumentException {
        com.zebra.sdk.printer.ZebraPrinter genericPrinter = ZebraPrinterFactory.getInstance(c);
        ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(genericPrinter);     //ezpl.print_width :812 , zpl.label_length :620

        if (linkOsPrinter != null) {

            System.out.println("Available Settings for myDevice");
            Set<String> availableSettings = linkOsPrinter.getAvailableSettings();
            for (String setting : availableSettings) {
                System.out.println(setting + ": Range = (" + linkOsPrinter.getSettingRange(setting) + ")");
            }

            System.out.println("\nCurrent Setting Values for myDevice");
            Map<String, String> allSettingValues = linkOsPrinter.getAllSettingValues();
            for (String settingName : allSettingValues.keySet()) {
                System.out.println(settingName + ":" + allSettingValues.get(settingName));
            }

            String darknessSettingId = "print.tone";
            String newDarknessValue = "10.0";
            if (availableSettings.contains(darknessSettingId) &&
                    linkOsPrinter.isSettingValid(darknessSettingId, newDarknessValue) &&
                    linkOsPrinter.isSettingReadOnly(darknessSettingId) == false) {
                linkOsPrinter.setSetting(darknessSettingId, newDarknessValue);
            }

            System.out.println("\nNew " + darknessSettingId + " Value = " + linkOsPrinter.getSettingValue(darknessSettingId));
        }
    }

    public static String getPrinterSettingValue(Connection c, String key) throws ConnectionException, ZebraPrinterLanguageUnknownException, SettingsException {
        String value="";
        /*com.zebra.sdk.printer.ZebraPrinter genericPrinter = ZebraPrinterFactory.getInstance(c);
        ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(genericPrinter);*/     //ezpl.print_width :812 , zpl.label_length :620

        //if (linkOsPrinter != null) {
        // Set<String> availableSettings = linkOsPrinter.getAvailableSettings();
        // Map<String, String> allSettingValues = linkOsPrinter.getAllSettingValues();
        if(allPrinterSettingValues!=null)
            value= allPrinterSettingValues.get(key);
        //}
        if(value==null)
            value="0";
        AppUtils.displayLog("Get Printer Setting Key= "+key," Value = "+value);
        return value;
    }

    public void setPrinterSetting(Connection c) throws SettingsException, ConnectionException, ZebraPrinterLanguageUnknownException {
        if(tempZebraLabelLength==0 || tempZebraPrintWidth==0) {
            if (zebraPrinterInstance == null)
                zebraPrinterInstance = ZebraPrinterFactory.getInstance(c);
            else if (!zebraPrinterInstance.getConnection().isConnected())
                zebraPrinterInstance = ZebraPrinterFactory.getInstance(c);

            linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(zebraPrinterInstance);
            if (linkOsPrinter != null) {
                if (availableSettings.size() == 0)
                    availableSettings = linkOsPrinter.getAvailableSettings();
                // if (availableSettings.contains(key) && linkOsPrinter.isSettingValid(key, value) && !linkOsPrinter.isSettingReadOnly(key)) {
                if(!String.valueOf(zebraPrintWidth).equals("0"))
                    linkOsPrinter.setSetting(ZEBRA_PRINT_WIDTH, String.valueOf(zebraPrintWidth));
                if(!String.valueOf(zebraLabelLength).equals("0"))
                    linkOsPrinter.setSetting(ZEBRA_LABEL_LENGTH, String.valueOf(zebraLabelLength));
                tempZebraPrintWidth=(int)zebraPrintWidth;
                tempZebraLabelLength=(int)zebraLabelLength;
                AppUtils.displayLog("---------Set Printer Setting Key---------- = " + ZEBRA_PRINT_WIDTH, " Value = " + zebraPrintWidth);
                AppUtils.displayLog("---------Set Printer Setting Key---------- = " + ZEBRA_LABEL_LENGTH, " Value = " + zebraLabelLength);

                //}
            }
        }
    }

    public static String getAllPrinterSettingValue(TcpConnection c) throws ConnectionException, ZebraPrinterLanguageUnknownException, SettingsException {
        String value="";
        com.zebra.sdk.printer.ZebraPrinter genericPrinter = ZebraPrinterFactory.getInstance(c);
        ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(genericPrinter);     //ezpl.print_width :812 , zpl.label_length :620

        if (linkOsPrinter != null) {
            // Set<String> availableSettings = linkOsPrinter.getAvailableSettings();
            allPrinterSettingValues = linkOsPrinter.getAllSettingValues();
            for (String settingName : allPrinterSettingValues.keySet()) {
                System.out.println(settingName + ":" + allPrinterSettingValues.get(settingName));
            }
        }
        AppUtils.displayLog("Get All Printer Setting Values"," Size = "+allPrinterSettingValues.size());
        return value;
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap identicalBitmapTwoInOne(Bitmap source, float angle) {

        Bitmap two = source;
        Bitmap bmOverlay = Bitmap.createBitmap(source.getWidth(), (source.getHeight()*2), source.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(source, new Matrix(), null);
        canvas.drawBitmap(two, 0, two.getHeight(), null);

       /* canvas.drawBitmap(source, 0, source.getHeight(), null);
        canvas.drawBitmap(source, source.getWidth(), source.getHeight(), null);*/

        return bmOverlay;

    }
    public static Bitmap rotateBitmapTwoInOne(Bitmap source, float angle) {

        Bitmap bmOverlay = Bitmap.createBitmap(source.getWidth(),(source.getHeight()*2), source.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(source, new Matrix(), null);
        canvas.drawBitmap(rotateBitmap(source,angle), 0, source.getHeight(), null);
        return bmOverlay;

    }


    private void setImageHeightWidth() {
        getExternalSettings();
        EventObjects checkedin_event_record = Util.db.getSelectedEventRecord(BaseActivity.checkedin_event_record.Events.Id);
        String where_att = " Where " + DBFeilds.BADGE_NEW_EVENT_ID + " = '"
                + checkedin_event_record.Events.Id + "' AND " + DBFeilds.BADGE_NEW_ID
                + " = '" + checkedin_event_record.Events.Mobile_Default_Badge__c + "'";
        if(!ext_settings.zebra_settings){
            if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PRINT_WIDTH, "").equalsIgnoreCase("?")
                    ||PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_LABEL_LENGTH, "").equalsIgnoreCase("?")){
                ArrayList<BadgeResponseNew> badge_res = Util.db.getAllBadges(where_att);
                if (badge_res.size() > 0) {
                    BadgeDataNew badge_data = new Gson().fromJson(badge_res.get(0).badge.Data__c, BadgeDataNew.class);
                    if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PRINT_WIDTH, "").equalsIgnoreCase("?"))
                        zebraPrintWidth = (double) (badge_data.canvasWidth * Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_RESOLUTION, "")));
                    else
                        zebraPrintWidth = Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PRINT_WIDTH, ""));
                    if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_LABEL_LENGTH, "").equalsIgnoreCase("?"))
                        zebraLabelLength = (double) (badge_data.canvasHeight * Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_RESOLUTION, "")));
                    else
                        zebraLabelLength = Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_LABEL_LENGTH, ""));
                }
            }else{
                zebraPrintWidth = Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PRINT_WIDTH, ""));
                zebraLabelLength = Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_LABEL_LENGTH, ""));
            }
        }else {
            ArrayList<BadgeResponseNew> badge_res = Util.db.getAllBadges(where_att);
            if (badge_res.size() > 0) {
                BadgeDataNew badge_data = new Gson().fromJson(badge_res.get(0).badge.Data__c, BadgeDataNew.class);
            /*zebraPrintWidth = (int) badge_data.canvasWidth * 203;
            zebraLabelLength = (int) badge_data.canvasHeight * 203;*/
                zebraPrintWidth = (double) (badge_data.canvasWidth * Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_RESOLUTION, "")));
                zebraLabelLength = (double) (badge_data.canvasHeight * Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_RESOLUTION, "")));

            } else {
                zebraPrintWidth = 0;
                zebraLabelLength = 0;
            }
        }
    }

    public static boolean deleteDir() {

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/ScanAttendee/Badges");
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }

        return dir.delete();
    }

    public static void openprinterNotConnectedDialog(final Context context, final FrameLayout badgeLayout, final ArrayList<String> mFiles){
        Util.setCustomAlertDialog(context);
        String alertmsg = "";
        // if (!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
        alertmsg = "Something went wrong, Do you want to reprint."+errorMessage;
       /* } else {
            alertmsg = "Printer is not connected.Do you want to Connect?";
        }*/
        Util.openCustomDialog("Alert", alertmsg);
        Util.txt_okey.setText("Reprint");
        Util.txt_dismiss.setVisibility(View.VISIBLE);
        Util.txt_okey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //ZebraPrinter zebraPrinter=new ZebraPrinter();
                Util.alert_dialog.dismiss();
                if(badgeLayout==null){
                    zebraPrinter.doZebraPrint(context,mFiles);
                }else{
                    zebraPrinter.doZebraPrint(context,badgeLayout);
                }
            }
        });
        Util.txt_dismiss.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Util.alert_dialog.dismiss();
                if(context instanceof TransperantGlobalScanActivity){
                    ((Activity) context).finish();
                }/*else if(context instanceof GlobalScanActivity){
                    ((Activity) context).finish();
                }*/
            }
        });
    }

    public void showToast(final Context context, final String message){
        /*((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });*/
    }
    public void printBluetoothMirrorImage(BluetoothConnection conn, final Context context, FrameLayout frameLayout)throws Exception{
        if (findPrinterStatus(conn,context)) {
            //endPrinterStatusTime=System.currentTimeMillis();
            setImageHeightWidth();
            showToast(context,"Status true....");
            frameLayout.setDrawingCacheEnabled(true);
            frameLayout.buildDrawingCache(true);
            frameLayout.buildDrawingCache();
            Bitmap tempBitmap = frameLayout.getDrawingCache();
            ZebraImageI zebraImage = ZebraImageFactory.getImage(/*RotateBitmap(*/tempBitmap/*,90)*/);
            ZebraImageI zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmap(tempBitmap,180));
            if(ext_settings.identical_doubleSide_badge){
                zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmap(tempBitmap,0));
            }else if(ext_settings.identical_doubleSide_badge_two_in_one){
                zebraMirrorImage = ZebraImageFactory.getImage(identicalBitmapTwoInOne(tempBitmap,0));
            }else if(ext_settings.mirror_doubleSide_badge_two_in_one){
                zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmapTwoInOne(tempBitmap,180));
            }else {
                zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmap(tempBitmap,180));
            }
            if(ZebraPrinter.tempZebraPrintWidth==0 ||ZebraPrinter.tempZebraLabelLength==0) {

                if(ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one){
                    if(ext_settings.zebra_settings){
                        zebraLabelLength=zebraLabelLength*2;
                    }
                    ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, (int)zebraPrintWidth, (int)(zebraLabelLength), false);
                }/*else if(ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one&&!ext_settings.zebra_settings){
                    ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, (int)zebraPrintWidth, (int)(zebraLabelLength), false);
                }*/else{
                    ZebraPrinterFactory.getInstance(conn).printImage(zebraImage, 0, 0, (int)zebraPrintWidth, (int)zebraLabelLength, false);
                    ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, (int)zebraPrintWidth, (int)zebraLabelLength, false);
                }
            }else {
                if(ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one){
                    if(ext_settings.zebra_settings){
                        ZebraPrinter.tempZebraLabelLength=ZebraPrinter.tempZebraLabelLength*2;
                    }
                    ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                }
                else{
                    ZebraPrinterFactory.getInstance(conn).printImage(zebraImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                    ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                }
            }
            closeProgressDialog(context);
            //endTime = System.currentTimeMillis();
            NumberFormat formatter = new DecimalFormat("#0.00000");

            //showPrinterErrorDialog(context, "Printed Completed", true);
                /*showPrinterErrorDialog(context, "Printed Completed "+tempZebraPrintWidth+" = "+tempZebraLabelLength+" total time ="+formatter.format((endTime - startTime) / 1000d)+ " sec Print() time= "+
                        formatter.format((endTime - startPrintTime) / 1000d)+ " sec printerStatus() time= "+formatter.format((endPrinterStatusTime - startPrinterStatusTime) / 1000d)+ " sec "+formatter.format((endTime - baseStartTime) / 1000d),true);*/

            afterPrintComplete(context);
        }
        Thread.sleep(500);
        //conn.close();
    }

    public void printMirrorImage(TcpConnection conn, final Context context, FrameLayout frameLayout)throws Exception{
        if (findPrinterStatus(conn,context)) {
            //endPrinterStatusTime=System.currentTimeMillis();
            setImageHeightWidth();
            showToast(context,"Status true....");
            frameLayout.setDrawingCacheEnabled(true);
            frameLayout.buildDrawingCache(true);
            frameLayout.buildDrawingCache();
            Bitmap tempBitmap = frameLayout.getDrawingCache();
            ZebraImageI zebraImage = ZebraImageFactory.getImage(/*RotateBitmap(*/tempBitmap/*,90)*/);
            ZebraImageI zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmap(tempBitmap,180));
            if(ext_settings.identical_doubleSide_badge){
                zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmap(tempBitmap,0));
            }else if(ext_settings.identical_doubleSide_badge_two_in_one){
                zebraMirrorImage = ZebraImageFactory.getImage(identicalBitmapTwoInOne(tempBitmap,0));
            }else if(ext_settings.mirror_doubleSide_badge_two_in_one){
                zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmapTwoInOne(tempBitmap,180));
            }else {
                zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmap(tempBitmap,180));
            }
            if(ZebraPrinter.tempZebraPrintWidth==0 ||ZebraPrinter.tempZebraLabelLength==0) {

                if(ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one){
                    if(ext_settings.zebra_settings){
                        zebraLabelLength=zebraLabelLength*2;
                    }
                    ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, (int)zebraPrintWidth, (int)(zebraLabelLength), false);
                }/*else if(ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one&&!ext_settings.zebra_settings){
                    ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, (int)zebraPrintWidth, (int)(zebraLabelLength), false);
                }*/else{
                    ZebraPrinterFactory.getInstance(conn).printImage(zebraImage, 0, 0, (int)zebraPrintWidth, (int)zebraLabelLength, false);
                    ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, (int)zebraPrintWidth, (int)zebraLabelLength, false);
                }
            }else {
                if(ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one){
                    if(ext_settings.zebra_settings){
                        ZebraPrinter.tempZebraLabelLength=ZebraPrinter.tempZebraLabelLength*2;
                    }
                    ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                }
                else{
                    ZebraPrinterFactory.getInstance(conn).printImage(zebraImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                    ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                }
            }
            closeProgressDialog(context);
            //endTime = System.currentTimeMillis();
            NumberFormat formatter = new DecimalFormat("#0.00000");

            //showPrinterErrorDialog(context, "Printed Completed", true);
                /*showPrinterErrorDialog(context, "Printed Completed "+tempZebraPrintWidth+" = "+tempZebraLabelLength+" total time ="+formatter.format((endTime - startTime) / 1000d)+ " sec Print() time= "+
                        formatter.format((endTime - startPrintTime) / 1000d)+ " sec printerStatus() time= "+formatter.format((endPrinterStatusTime - startPrinterStatusTime) / 1000d)+ " sec "+formatter.format((endTime - baseStartTime) / 1000d),true);*/

            afterPrintComplete(context);
        }
        Thread.sleep(500);
        //conn.close();
    }
    public void printBluetoothMirrorImage(BluetoothConnection conn, final Context context, ArrayList<String> mFiles)throws Exception {
        //conn.open();
        if (findPrinterStatus(conn,context)) {
            setImageHeightWidth();
            if(zebraPrintWidth==0||zebraLabelLength==0){
                closeProgressDialog(context);
                showPrinterErrorDialog(context, "Wrong Lable",false);
            }else if(zebraPrintWidth==-1||zebraLabelLength==-1){
                closeProgressDialog(context);
                showPrinterErrorDialog(context, "No Badge Selected",false);
            }else {
                //setPrinterSetting(conn);
                /*if(zebraPrinterInstance.getCurrentStatus().labelLengthInDots!=zebraLabelLength)
                    setPrinterSetting(conn, ZebraPrinter.ZEBRA_LABEL_LENGTH, String.valueOf(zebraLabelLength));*/
                zebraPrinterInstance=ZebraPrinterFactory.getInstance(conn);
                for (String filePath : mFiles) {
                    Bitmap tempBitmap = BitmapFactory.decodeFile(filePath);
                    ZebraImageI zebraImage = ZebraImageFactory.getImage(/*RotateBitmap(*/tempBitmap/*,90)*/);
                    ZebraImageI zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmap(tempBitmap,180));
                    if(ext_settings.identical_doubleSide_badge){
                        zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmap(tempBitmap,0));
                    }else if(ext_settings.identical_doubleSide_badge_two_in_one){
                        zebraMirrorImage = ZebraImageFactory.getImage(identicalBitmapTwoInOne(tempBitmap,0));
                    }else if(ext_settings.mirror_doubleSide_badge_two_in_one){
                        zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmapTwoInOne(tempBitmap,180));
                    }else {
                        zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmap(tempBitmap,180));
                    }
                    if(ZebraPrinter.tempZebraPrintWidth==0 ||ZebraPrinter.tempZebraLabelLength==0) {

                        if(ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one&&ext_settings.zebra_settings){
                            if(ext_settings.zebra_settings){
                                zebraLabelLength=zebraLabelLength*2;
                            }
                            ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, (int)zebraPrintWidth, (int)zebraLabelLength, false);
                            //displayToast("Width-"+zebraPrintWidth+"|"+"Length-"+zebraLabelLength*2,context);
                        }/*else if(ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one&&!ext_settings.zebra_settings){
                            ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, (int)zebraPrintWidth, (int)(zebraLabelLength), false);
                        }*/else{
                            ZebraPrinterFactory.getInstance(conn).printImage(zebraImage, 0, 0, (int)zebraPrintWidth, (int)zebraLabelLength, false);
                            ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, (int)zebraPrintWidth,(int) zebraLabelLength, false);
                        }
                    }else {
                        if(ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one){
                            if(ext_settings.zebra_settings){
                                ZebraPrinter.tempZebraLabelLength=ZebraPrinter.tempZebraLabelLength*2;
                            }
                            ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                            //displayToast("Width-"+zebraPrintWidth+"|"+"Length-"+zebraLabelLength*2,context);
                        }
                        else{
                            ZebraPrinterFactory.getInstance(conn).printImage(zebraImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                            ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                        }
                    }
                    /*if(tempBitmap!=null) {
                        ZebraImageI zebraImage = ZebraImageFactory.getImage(tempBitmap);
                        ZebraImageI zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmap(tempBitmap,180));
                        if(ZebraPrinter.tempZebraPrintWidth==0 ||ZebraPrinter.tempZebraLabelLength==0) {
                            setImageHeightWidth();
                            zebraPrinterInstance.printImage(zebraImage, 0, 0, zebraPrintWidth, zebraLabelLength, false);
                            zebraPrinterInstance.printImage(zebraMirrorImage, 0, 0, zebraPrintWidth, zebraLabelLength, false);
                        }else {
                            zebraPrinterInstance.printImage(zebraImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                            zebraPrinterInstance.printImage(zebraMirrorImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                        }
                    }*/
                }

                closeProgressDialog(context);
                //showPrinterErrorDialog(context, "Printed Completed",true);
                afterPrintComplete(context);
            }
        }
        Thread.sleep(500);
        deleteDir();
        //conn.close();
    }
    public void printMirrorImage(TcpConnection conn, final Context context, ArrayList<String> mFiles)throws Exception {
        //conn.open();
        if (findPrinterStatus(conn,context)) {
            setImageHeightWidth();
            if(zebraPrintWidth==0||zebraLabelLength==0){
                closeProgressDialog(context);
                showPrinterErrorDialog(context, "Wrong Lable",false);
            }else if(zebraPrintWidth==-1||zebraLabelLength==-1){
                closeProgressDialog(context);
                showPrinterErrorDialog(context, "No Badge Selected",false);
            }else {
                //setPrinterSetting(conn);
                /*if(zebraPrinterInstance.getCurrentStatus().labelLengthInDots!=zebraLabelLength)
                    setPrinterSetting(conn, ZebraPrinter.ZEBRA_LABEL_LENGTH, String.valueOf(zebraLabelLength));*/
                zebraPrinterInstance=ZebraPrinterFactory.getInstance(conn);
                for (String filePath : mFiles) {
                    Bitmap tempBitmap = BitmapFactory.decodeFile(filePath);
                    ZebraImageI zebraImage = ZebraImageFactory.getImage(/*RotateBitmap(*/tempBitmap/*,90)*/);
                    ZebraImageI zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmap(tempBitmap,180));
                    if(ext_settings.identical_doubleSide_badge){
                        zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmap(tempBitmap,0));
                    }else if(ext_settings.identical_doubleSide_badge_two_in_one){
                        zebraMirrorImage = ZebraImageFactory.getImage(identicalBitmapTwoInOne(tempBitmap,0));
                    }else if(ext_settings.mirror_doubleSide_badge_two_in_one){
                        zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmapTwoInOne(tempBitmap,180));
                    }else {
                        zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmap(tempBitmap,180));
                    }
                    if(ZebraPrinter.tempZebraPrintWidth==0 ||ZebraPrinter.tempZebraLabelLength==0) {

                        if(ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one&&ext_settings.zebra_settings){
                            if(ext_settings.zebra_settings){
                                zebraLabelLength=zebraLabelLength*2;
                            }
                            ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, (int)zebraPrintWidth, (int)zebraLabelLength, false);
                            //displayToast("Width-"+zebraPrintWidth+"|"+"Length-"+zebraLabelLength*2,context);
                        }/*else if(ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one&&!ext_settings.zebra_settings){
                            ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, (int)zebraPrintWidth, (int)(zebraLabelLength), false);
                        }*/else{
                            ZebraPrinterFactory.getInstance(conn).printImage(zebraImage, 0, 0, (int)zebraPrintWidth, (int)zebraLabelLength, false);
                            ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, (int)zebraPrintWidth,(int) zebraLabelLength, false);
                        }
                    }else {
                        if(ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one){
                            if(ext_settings.zebra_settings){
                                ZebraPrinter.tempZebraLabelLength=ZebraPrinter.tempZebraLabelLength*2;
                            }
                            ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                            //displayToast("Width-"+zebraPrintWidth+"|"+"Length-"+zebraLabelLength*2,context);
                        }
                        else{
                            ZebraPrinterFactory.getInstance(conn).printImage(zebraImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                            ZebraPrinterFactory.getInstance(conn).printImage(zebraMirrorImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                        }
                    }
                    /*if(tempBitmap!=null) {
                        ZebraImageI zebraImage = ZebraImageFactory.getImage(tempBitmap);
                        ZebraImageI zebraMirrorImage = ZebraImageFactory.getImage(rotateBitmap(tempBitmap,180));
                        if(ZebraPrinter.tempZebraPrintWidth==0 ||ZebraPrinter.tempZebraLabelLength==0) {
                            setImageHeightWidth();
                            zebraPrinterInstance.printImage(zebraImage, 0, 0, zebraPrintWidth, zebraLabelLength, false);
                            zebraPrinterInstance.printImage(zebraMirrorImage, 0, 0, zebraPrintWidth, zebraLabelLength, false);
                        }else {
                            zebraPrinterInstance.printImage(zebraImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                            zebraPrinterInstance.printImage(zebraMirrorImage, 0, 0, ZebraPrinter.tempZebraPrintWidth, ZebraPrinter.tempZebraLabelLength, false);
                        }
                    }*/
                }

                closeProgressDialog(context);
                //showPrinterErrorDialog(context, "Printed Completed",true);
                afterPrintComplete(context);
            }
        }
        Thread.sleep(500);
        deleteDir();
        //conn.close();
    }

    private void getExternalSettings() {
        if(ext_settings==null) {
            ext_settings = new ExternalSettings();
            if (!Util.external_setting_pref.getString(BaseActivity.user_profile.Userid + BaseActivity.checkedin_event_record.Events.Id, "").isEmpty()) {
                ext_settings = new Gson().fromJson(Util.external_setting_pref.getString(BaseActivity.user_profile.Userid + BaseActivity.checkedin_event_record.Events.Id, ""), ExternalSettings.class);
            }
        }
    }
    private void afterPrintComplete(final Context context) {
        if (Util.getselfcheckinbools(Util.ISPRINTALLOWED)) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    BaseActivity.showCustomToast(context,
                            "Print Completed!",
                            R.drawable.img_like, R.drawable.toast_greenroundededge, true);

                }
            });
            if (Util.getselfcheckinbools(Util.ISAUTOCHECKIN)) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        ((BaseActivity) context).onPrintSuccess(context);
                    }
                });
            } else {
                ((BaseActivity) context).finish();
            }
        } else if (((BaseActivity) context).isNotFromSelfcheckin(context)) {
            if (!Util.external_setting_pref.getString(((BaseActivity) context).sfdcddetails.user_id + ((BaseActivity) context).checked_in_eventId, "").isEmpty()) {
                ((BaseActivity) context).externalSettings = new Gson().fromJson(Util.external_setting_pref.getString(((BaseActivity) context).sfdcddetails.user_id + ((BaseActivity) context).checked_in_eventId, ""), ExternalSettings.class);
                if (((BaseActivity) context).externalSettings.quick_checkin) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {

                            BaseActivity.showCustomToast(context,
                                    "Print Completed!",
                                    R.drawable.img_like, R.drawable.toast_greenroundededge, true);
                            ((BaseActivity) context).onPrintSuccess(context);
                            ((Activity) context).setResult(2017);
                        }
                    }); //((BaseActivity) mContext).finish();
                } else {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            BaseActivity.showCustomToast(context,
                                    "Print Completed!",
                                    R.drawable.img_like, R.drawable.toast_greenroundededge, true);
                            ((BaseActivity) context).updatePrintStatus(context, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
                            ((Activity) context).setResult(2017);
                            //((BaseActivity) context).onPrintSuccess(context);
                        }
                    });


                    //new change for finishing selfcheckinattendeedetail page
                    //openAlertDialog(message, "success", mContext);

                }
            } else if (((BaseActivity) context) instanceof TransperantGlobalScanActivity) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        BaseActivity.showCustomToast(context,
                                "Print Completed!",
                                R.drawable.img_like, R.drawable.toast_greenroundededge, true);
                    }
                });
                ((BaseActivity) context).finish();
            } else if (((BaseActivity) context) instanceof GlobalScanActivity) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        ((GlobalScanActivity) context).docheckinprocessafterprint("Print Completed");
                    }
                });
            } else {
                showPrinterErrorDialog(context, "Printed Completed", true);
            }
        }else if(((BaseActivity) context) instanceof PrintAttendeesListActivity){
            showPrinterErrorDialog(context, "Printed Completed", true);

        }
    }
    public static void googleCloudPrint(Context context,String fileName) {
        File file = new File(Environment
                .getExternalStorageDirectory().getAbsolutePath()
                + "/ScanAttendee/Badges/"+"printPdf.pdf");
        //File file = new File(filePath);  /storage/emulated/0/ScanAttendee/Badges
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null)
            type = "image/png";

        Intent printIntent = new Intent(context,
                PrintDialogActivity.class);
        printIntent.setDataAndType(Uri.fromFile(file), type
                /*"application/pdf"*/);
        printIntent.putExtra("title", "Android print demo");
        context.startActivity(printIntent);
    }


    public static void googleCloudPrintImage(Context context,String fileName) {
        File file = new File(Environment
                .getExternalStorageDirectory().getAbsolutePath()
                + "/ScanAttendee/Badges/"+fileName+".png");
        //File file = new File(filePath);  /storage/emulated/0/ScanAttendee/Badges
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null)
            type = "image/png";

        Intent printIntent = new Intent(context,
                PrintDialogActivity.class);
        printIntent.setDataAndType(Uri.fromFile(file), type
                /*"application/pdf"*/);
        printIntent.putExtra("title", "Android print demo");
        //((Activity)context).startActivityForResult(printIntent,555);
        context.startActivity(printIntent);
    }
}
