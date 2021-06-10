package com.globalnest.printer;

import android.content.Context;
import android.content.SharedPreferences;

import com.brother.ptouch.sdk.NetPrinter;
import com.brother.ptouch.sdk.Printer;
import com.globalnest.brother.ptouch.sdk.printdemo.common.Common;
import com.globalnest.scanattendee.BaseActivity;
import com.globalnest.scanattendee.ExternalSettingsActivity;
import com.globalnest.scanattendee.R;
import com.globalnest.utils.AppUtils;

import java.util.ArrayList;

/**
 * Created by mayank on 11/05/2017.
 */

public class BrotherPrinter extends BaseActivity{

   private NetPrinter[] mNetPrinter;
    private String mModelName="";
    private Context mContext;

    public void getPrinter(String modelName, Context context){
        mModelName=modelName;
        mContext=context;
        new SearchThread().start();
    }

    public class SearchThread extends Thread {
        /* search for the printer for 10 times until printer has been found. */
        @Override
        public void run() {
            try {
                // search for net printer.
                if (netPrinterList(5)) {
                    isPrinter = true;
                    //msgDialog.close();
                } else {
                     //msgDialog.close();
                }

                if(mNetPrinter.length>0){
                    PrinterDetails.selectedPrinterPrefrences.edit().putBoolean("isConnected",true).commit();
                }else{
                    PrinterDetails.selectedPrinterPrefrences.edit().putBoolean("isConnected",false).commit();
                }
                ((ExternalSettingsActivity)mContext).setPrinterStatus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean netPrinterList(int times ) {
        boolean searchEnd = false;
        try {
            // clear the item list
            if (mItems != null) {
                mItems.clear();
            }
            // get net printers of the particular model
            mItems = new ArrayList<String>();
            Printer myPrinter = new Printer();
            myPrinter.getLabelInfo();
            AppUtils.displayLog("-Printer Model Name-",":"+mModelName.replace("_","-"));
            mNetPrinter = myPrinter.getNetPrinters(mModelName.replace("_","-"));//"QL-720NW"
            final int netPrinterCount = mNetPrinter.length;
            // when find printers,set the printers' information to the list.
            if (netPrinterCount > 0) {
                searchEnd = true;
            } else if (netPrinterCount == 0
                    && times == (Common.SEARCH_TIMES - 1)) { // when no printer is found
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
            SharedPreferences.Editor selectedPrinterPref = PrinterDetails.selectedPrinterPrefrences.edit();
            selectedPrinterPref.putString(ZebraPrinter.SELECTED_PRINTER, "Brother");
            selectedPrinterPref.putString(ZebraPrinter.PRINTERMODEL, mModelName);//QL_720NW , QL_820NWB
            selectedPrinterPref.putString("printer", mNetPrinter.modelName);
            selectedPrinterPref.putBoolean("isConnected",true);
            selectedPrinterPref.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCustomContentView(int layout) {

    }

    @Override
    public void doRequest() {

    }

    @Override
    public void parseJsonResponse(String response) {

    }

    @Override
    public void insertDB() {

    }
}
