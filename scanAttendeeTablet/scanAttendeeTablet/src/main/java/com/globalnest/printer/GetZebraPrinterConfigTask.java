package com.globalnest.printer;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.BadgeDataNew;
import com.globalnest.mvc.BadgeResponseNew;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.objects.EventObjects;
import com.globalnest.scanattendee.BaseActivity;
import com.globalnest.stripe.android.compat.AsyncTask;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;
import com.zebra.sdk.settings.SettingsException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by mayank on 16/08/2017.
 */

public class GetZebraPrinterConfigTask extends AsyncTask<String,Void,String> {

    Context mContext;
    private int zebraLabelLength=0;
    private int zebraPrintWidth=0;
    private double zebrabadgeLabelLength=0;
    private double zebrabadgePrintWidth=0;
    public GetZebraPrinterConfigTask(Context context){
        mContext=context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        BaseActivity.baseDialog.setMessage("Setting Printer Config...");
        BaseActivity.baseDialog.show();
    }

    @Override
    protected String doInBackground(String[] params) {
        getZebraPrinterSetting();
        return "";
    }

    @Override
    protected void onPostExecute(String str) {
        super.onPostExecute(str);
        BaseActivity.baseDialog.dismiss();
        /*((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ZebraPrinter.showPrinterErrorDialog(mContext,"Printer settings updated Successfully. "*//*\n PRINTER LABEL WIDTH= "+ZebraPrinter.tempZebraPrintWidth+" and PRINTER LABEL HEIGHT=  "+ZebraPrinter.tempZebraLabelLength+" --> "+zebraPrintWidth+", --> "+zebraLabelLength
                        +" ("+badgeDataNew.canvasWidth+badgeDataNew.canvasHeight+")"*//*,true);
            }
        });*/
    }

    public void getZebraPrinterSetting() {
        try {
            ZebraPrinter zebraPrinter;
            //if(zebraPrinter==null) {
                zebraPrinter=new ZebraPrinter();
            //}
            if (PrinterDetails.selectedPrinterPrefrences != null) {

                if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equals("Zebra")) {
                    ExternalSettings ext_settings  = new ExternalSettings();
                    if (!Util.external_setting_pref.getString(BaseActivity.user_profile.Userid + BaseActivity.checkedin_event_record.Events.Id, "").isEmpty()) {
                        ext_settings = new Gson().fromJson(Util.external_setting_pref.getString(BaseActivity.user_profile.Userid + BaseActivity.checkedin_event_record.Events.Id, ""), ExternalSettings.class);
                    }
                    if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP,"").isEmpty()) {
                       // if (zebraPrinter.getTCPConnection() == null) {
                            try {
                                zebraPrinter.createTCPConnection();
                                SGD.SET("device.languages", "zpl", zebraPrinter.getTCPConnection());
                               setImageHeightWidth();
                                //setPrinterSetting(zebraPrinter.getTCPConnection(),String.valueOf(zebraPrintWidth),String.valueOf(zebraLabelLength));


                                /*if(ext_settings!=null && ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one){
                                    setPrinterSetting(zebraPrinter.getTCPConnection(),String.valueOf(zebraPrintWidth),String.valueOf(zebraLabelLength*2));
                                }*/
                                if(ext_settings!=null && ext_settings.zebra_settings){
                                    if(ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one){
                                        zebrabadgeLabelLength=zebrabadgeLabelLength*2;}
                                    //displayToast("Width-"+zebrabadgePrintWidth+"|"+"Length-"+zebrabadgeLabelLength*2,mContext);
                                    setPrinterSetting(zebraPrinter.getTCPConnection(),String.valueOf((int)zebrabadgePrintWidth),String.valueOf((int)zebrabadgeLabelLength));
                                }else{
                                    //displayToast("Width-"+zebraPrintWidth+"|"+"Length-"+zebraLabelLength,mContext);
                                    setPrinterSetting(zebraPrinter.getTCPConnection(),String.valueOf((int)zebraPrintWidth),String.valueOf((int)zebraLabelLength));
                                }
                            } catch (final Exception e) {
                                /*StringWriter sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);
                                e.printStackTrace(pw);
                                final String errorMessage=sw.toString();
                                ((Activity)mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(errorMessage.isEmpty())
                                            AppUtils.showError(mContext, ZebraPrinter.getErrorFromException(e));
                                        else
                                            AppUtils.showError(mContext, errorMessage);
                                    }
                                });*/

                                e.printStackTrace();
                            }
                        }else if (!zebraPrinter.getBTConnection().isConnected()) {
                            zebraPrinter.createTCPConnection();
                            setImageHeightWidth();
                        if(ext_settings!=null && ext_settings.zebra_settings){
                            if(ext_settings.mirror_doubleSide_badge_two_in_one || ext_settings.identical_doubleSide_badge_two_in_one){
                                zebrabadgeLabelLength=zebrabadgeLabelLength*2;}
                            setPrinterSetting(zebraPrinter.getBTConnection(),String.valueOf(zebrabadgePrintWidth),String.valueOf(zebrabadgeLabelLength));
                        }else {
                            setPrinterSetting(zebraPrinter.getBTConnection(),String.valueOf(zebraPrintWidth),String.valueOf(zebraLabelLength));
                        }
                    }else {
                            setImageHeightWidth();
                            setPrinterSetting(zebraPrinter.getTCPConnection(),String.valueOf(zebraPrintWidth),String.valueOf(zebraLabelLength));

                        }
                        BaseActivity.zebraPrinter=zebraPrinter;
                    }

                }

            //}
        }catch (final Exception e) {
            e.printStackTrace();
            //getZebraPrinterSetting();
            /*StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            final String errorMessage=sw.toString();
            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(errorMessage.isEmpty())
                        AppUtils.showError(mContext, ZebraPrinter.getErrorFromException(e));
                    else
                        AppUtils.showError(mContext, errorMessage);
                }
            });*/

        }
    }

    public void setPrinterSetting(Connection c, String zebraWidth, String zebraLength) throws SettingsException, ConnectionException, ZebraPrinterLanguageUnknownException {
           com.zebra.sdk.printer.ZebraPrinter zebraPrinterInstance = ZebraPrinterFactory.getInstance(c);
        ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(zebraPrinterInstance);     //ezpl.print_width :812 , zpl.label_length :620
        /*if(Integer.parseInt(zebraWidth)==0){
            zebraWidth="812";
        }
        if(Integer.parseInt(zebraLength)==0){
            zebraLength="609";
        }*/
        //showToast(mContext,"width = "+zebraWidth+" height = "+zebraLength);

        if (linkOsPrinter != null) {
            Set<String> availableSettings = linkOsPrinter.getAvailableSettings();
        //showToast(mContext,"availableSettings = "+availableSettings.size()+" "+availableSettings.contains(ZebraPrinter.ZEBRA_PRINT_WIDTH)+" , "+availableSettings.contains(ZebraPrinter.ZEBRA_LABEL_LENGTH));
         if (availableSettings.contains(ZebraPrinter.ZEBRA_PRINT_WIDTH) && linkOsPrinter.isSettingValid(ZebraPrinter.ZEBRA_PRINT_WIDTH, zebraWidth) && !linkOsPrinter.isSettingReadOnly(ZebraPrinter.ZEBRA_PRINT_WIDTH)) {
             //showToast(mContext,"Setting width = "+zebraWidth);
             linkOsPrinter.setSetting(ZebraPrinter.ZEBRA_PRINT_WIDTH, zebraWidth);
             ZebraPrinter.tempZebraPrintWidth=Integer.parseInt(zebraWidth);
         }if (availableSettings.contains(ZebraPrinter.ZEBRA_LABEL_LENGTH) && linkOsPrinter.isSettingValid(ZebraPrinter.ZEBRA_LABEL_LENGTH, zebraLength) && !linkOsPrinter.isSettingReadOnly(ZebraPrinter.ZEBRA_LABEL_LENGTH)) {
            //showToast(mContext,"Setting height = "+zebraLength);
            linkOsPrinter.setSetting(ZebraPrinter.ZEBRA_LABEL_LENGTH, zebraLength);
            ZebraPrinter.tempZebraLabelLength = Integer.parseInt(zebraLength);
        }
        }
    }
    BadgeDataNew badgeDataNew=new BadgeDataNew();
    private void setImageHeightWidth() {
        EventObjects checkedin_event_record = Util.db.getSelectedEventRecord(BaseActivity.checkedin_event_record.Events.Id);
        String where_att = " Where " + DBFeilds.BADGE_NEW_EVENT_ID + " = '"
                + checkedin_event_record.Events.Id + "' AND " + DBFeilds.BADGE_NEW_ID
                + " = '" + checkedin_event_record.Events.Mobile_Default_Badge__c + "'";
        ArrayList<BadgeResponseNew> badge_res = Util.db.getAllBadges(where_att);

        if (badge_res.size() > 0) {
            BadgeDataNew badge_data = new Gson().fromJson(badge_res.get(0).badge.Data__c, BadgeDataNew.class);
            zebrabadgePrintWidth = (double) badge_data.canvasWidth * Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_RESOLUTION, ""));
            zebrabadgeLabelLength = (double) (badge_data.canvasHeight * Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_RESOLUTION, "")));
            if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PRINT_WIDTH, "").isEmpty()&&
                    PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PRINT_WIDTH, "").equals("?"))
                zebraPrintWidth = (int) badge_data.canvasWidth * Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_RESOLUTION, ""));
            //zebraPrintWidth = Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_PRINT_WIDTH, ""));
            if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_LABEL_LENGTH, "").isEmpty()&&
                    PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_LABEL_LENGTH, "").equals("?"))
                zebraLabelLength = (int) badge_data.canvasHeight * Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_RESOLUTION, ""));
            //zebraLabelLength =Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_LABEL_LENGTH, ""));
           /* else
                zebraLabelLength = (int) badge_data.canvasHeight * Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_RESOLUTION, ""));
           *//* zebraPrintWidth = (int) badge_data.canvasWidth * 203;
            zebraLabelLength = (int) badge_data.canvasHeight * 203;*/
            /*zebraPrintWidth = (int) badge_data.canvasWidth * Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_RESOLUTION, ""));
            zebraLabelLength = (int) badge_data.canvasHeight * Integer.valueOf(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_RESOLUTION, ""));
           */
            badgeDataNew=badge_data;
        }else{
            zebraPrintWidth =0;
            zebraLabelLength =0;
        }
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
    /*public void showToast(final Context context, final String message){
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }*/
}
