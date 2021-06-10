package com.globalnest.printer;

import android.content.SharedPreferences;

import com.brother.ptouch.sdk.NetPrinter;

import java.io.Serializable;

/**
 * Created by TrangHo on 10-05-2015.
 */
public class PrinterDetails implements Serializable{
    public static SharedPreferences selectedPrinterPrefrences;
    public static SharedPreferences buttonprefrences;
    public String printerName="";
    public String ip="",macAddress="";
    public String model="",selectedPrinter="";
    public boolean isConnected=false;
    //public static boolean isPrinterSearched;
    public  NetPrinter mNetPrinter;

    /*public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }


    *//*public void setDiscoveredPrinter(DiscoveredPrinter discoveredPrinter){
        this.discoveredPrinter=discoveredPrinter;
    }

    public DiscoveredPrinter getDiscoveredPrinter(){
        return discoveredPrinter;
    }*//*

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }*/
}
