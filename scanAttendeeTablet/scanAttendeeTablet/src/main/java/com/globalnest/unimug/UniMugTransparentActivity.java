package com.globalnest.unimug;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.globalnest.scanattendee.BaseActivity;
import com.globalnest.scanattendee.R;
import com.globalnest.scanattendee.SelfcheckinPaymentActivity;
import com.globalnest.scanattendee.SellOrderActivity;
import com.globalnest.scanattendee.UILApplication;
import com.globalnest.unimug.ProfileDatabase;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import IDTech.MSR.XMLManager.StructConfigParameters;
import IDTech.MSR.uniMag.Common;
import IDTech.MSR.uniMag.StateList;
import IDTech.MSR.uniMag.UniMagTools.uniMagReaderToolsMsg;
import IDTech.MSR.uniMag.UniMagTools.uniMagSDKTools;
import IDTech.MSR.uniMag.uniMagReader;
import IDTech.MSR.uniMag.uniMagReader.ReaderType;
import IDTech.MSR.uniMag.uniMagReaderMsg;

import static android.content.Context.MODE_PRIVATE;
/*
 *
 * File name: 	UniMugActivity.java
 * Author:		Eric.Yang
 * Time:		2011.10.21
 *
 * Modified by Jimmy Mo on 2012.09.21
 * to make demo application more readable
 *
 */

// interface uniMagReaderMsg should be implemented
// if firmware download is supported, uniMagReaderToolsMsg also needs to be implemented
public class UniMugTransparentActivity implements uniMagReaderMsg,uniMagReaderToolsMsg {

    // declaring the instance of the uniMagReader;

    private uniMagSDKTools firmwareUpdateTool = null;
    private boolean isReaderConnected = false;
    private boolean isExitButtonPressed = false;
    private boolean isWaitingForCommandResult=false;
    private boolean isSaveLogOptionChecked = false;
    private boolean isUseAutoConfigProfileChecked = false;
    //	private boolean isConnectWithCommand = true;
    private int readerType = -1; // 0: UniMag, 1: UniMag II

    //update the powerup status
    private int percent = 0;
    private long beginTime = 0;
    private long beginTimeOfAutoConfig = 0;
    private byte[] challengeResponse = null;

    private String popupDialogMsg = null;
    private boolean enableSwipeCard =false;
    private boolean autoconfig_running = false;

    private String strMsrData = null;
    private byte[] msrData = null;
    private String statusText = null;
    private int challengeResult = 0;



    /*****************************************
     CREATE TABLE profiles (
     search_date DATETIME,
     direction_output_wave INTEGER,
     input_frequency INTEGER,
     output_frequency INTEGER,
     record_buffer_size INTEGER,
     read_buffer_size INTEGER,
     wave_direction INTEGER,
     _low INTEGER,
     _high INTEGER,
     __low INTEGER,
     __high INTEGER,
     high_threshold INTEGER,
     low_threshold INTEGER,
     device_apm_base INTEGER,
     min INTEGER,
     max INTEGER,
     baud_rate INTEGER,
     preamble_factor INTEGER,
     set_default INTEGER)

     )
     *****************************************/

    static private final int REQUEST_GET_XML_FILE = 1;
    static private final int REQUEST_GET_BIN_FILE = 2;
    static private final int REQUEST_GET_ENCRYPTED_BIN_FILE = 3;

    //property for the menu item.
    static final private int START_SWIPE_CARD 	= Menu.FIRST;
    static final private int SETTINGS_ITEM 		= Menu.FIRST + 2;
    static final private int SUB_SAVE_LOG_ITEM 	= Menu.FIRST + 3;
    static final private int SUB_USE_AUTOCONFIG_PROFILE = Menu.FIRST + 4;
    static final private int SUB_SELECT_READER = Menu.FIRST + 5;
    static final private int SUB_LOAD_XML 		= Menu.FIRST + 6;
    static final private int SUB_LOAD_BIN 		= Menu.FIRST + 7;
    static final private int SUB_START_AUTOCONFIG= Menu.FIRST + 8;
    static final private int SUB_STOP_AUTOCONFIG = Menu.FIRST + 10;
    static final private int SUB_ATTACHED_TYPE 	= Menu.FIRST + 103;
    static final private int SUB_SUPPORT_STATUS	= Menu.FIRST + 104;
    static final private int DELETE_LOG_ITEM 	= Menu.FIRST + 11;
    static final private int ABOUT_ITEM 		= Menu.FIRST + 12;
    static final private int EXIT_IDT_APP 		= Menu.FIRST + 13;
    static final private int SUB_LOAD_ENCRYPTED_BIN = Menu.FIRST + 14;

    private MenuItem itemStartSC = null;
    private MenuItem itemSubSaveLog = null;
    private MenuItem itemSubUseAutoConfigProfile = null;
    private MenuItem itemSubSelectReader = null;
    private MenuItem itemSubLoadXML = null;
    private MenuItem itemSubStartAutoConfig = null;
    private MenuItem itemSubStopAutoConfig = null;
    private MenuItem itemDelLogs = null;
    private MenuItem itemAbout = null;
    private MenuItem itemExitApp = null;

    private SubMenu sub = null;

    private UniMagTopDialog dlgTopShow = null ;
    private UniMagTopDialog dlgSwipeTopShow = null ;
    private UniMagTopDialogYESNO dlgYESNOTopShow = null ;

    private StructConfigParameters profile = null;
    private ProfileDatabase profileDatabase = null;
    private Handler handler = new Handler();

    private uniMagReader myUniMagReader = null;
    private boolean isDialogShown=false;
    private Context context;
    public void onCreate(Context savedInstanceState,boolean isDialogNotShown) {
        //super.onCreate(savedInstanceState);
        context=savedInstanceState;
        this.isDialogShown=isDialogNotShown;
        if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
            myUniMagReader = ((SelfcheckinPaymentActivity) context).myUniMagReader;
        }else {
            myUniMagReader = ((SellOrderActivity) context).myUniMagReader;
        }
        //setContentView(R.layout.activity_unimug);
        profileDatabase = new ProfileDatabase(savedInstanceState);
        profileDatabase.initializeDB();
        isUseAutoConfigProfileChecked = profileDatabase.getIsUseAutoConfigProfile();

        //openReaderSelectDialog();
        //initializeUI();
        /*if(myUniMagReader!=null) {
            if(myUniMagReader.isReaderConnected()){
                swipeCard();
            }else {
                initializeReader(ReaderType.SHUTTLE);
            }
        }else{*/
        initializeReader(ReaderType.SHUTTLE);
        //}

        // to prevent screen timeout
        //((Activity)context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    void openReaderSelectDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select a reader:");
        builder.setCancelable(true);
        builder.setItems(R.array.reader_type, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                switch(which) {
                    case 0:
                        readerType = 0;
                        initializeReader(ReaderType.UM_OR_PRO);
                        Toast.makeText(context.getApplicationContext(), "UniMag / UniMag Pro selected", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        readerType = 1;
                        initializeReader(ReaderType.SHUTTLE);
                        Toast.makeText(context.getApplicationContext(), "UniMag II / Shuttle selected", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        readerType = 1;
                        initializeReader(ReaderType.SHUTTLE);
                        Toast.makeText(context.getApplicationContext(), "UniMag II / Shuttle selected", Toast.LENGTH_SHORT).show();
                        break;
                }
                showAboutInfo();
            }
        });
        builder.create().show();
    }

    /*@Override
    protected void onPause() {
        if(BaseActivity.myUniMagReader!=null)
        {
            //stop swipe card when the application go to background
            BaseActivity.myUniMagReader.stopSwipeCard();
        }
        hideTopDialog();
        hideSwipeTopDialog();
        super.onPause();
    }
    @Override
    protected void onResume() {
        if(myUniMagReader!=null)
        {
            if(isSaveLogOptionChecked==true)
                myUniMagReader.setSaveLogEnable(true);
            else
                myUniMagReader.setSaveLogEnable(false);
        }
        if(itemStartSC!=null)
            itemStartSC.setEnabled(true);
        isWaitingForCommandResult=false;
        super.onResume();
    }*/

    public void onDestroy() {
        if (myUniMagReader != null) {
            if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                ((SelfcheckinPaymentActivity) context).myUniMagReader.registerListen();
                ((SelfcheckinPaymentActivity) context).myUniMagReader.release();
            }else {
                ((SellOrderActivity) context).myUniMagReader.registerListen();
                ((SellOrderActivity) context).myUniMagReader.release();
            }
            myUniMagReader.unregisterListen();
            myUniMagReader.release();

        }

        if (profileDatabase != null)
            profileDatabase.closeDB();

        if (isExitButtonPressed) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
            SelfcheckinPaymentActivity.isUnimagReaderConnected=false;
        }else {
            SellOrderActivity.isUnimagReaderConnected = false;
        }
        myUniMagReader=null;
        if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
            ((SelfcheckinPaymentActivity) context).myUniMagReader = null;
        }else {
            ((SellOrderActivity) context).myUniMagReader = null;
        }
    }

    private void swipeCard(){
        if (myUniMagReader!=null)
        {
            if (!isWaitingForCommandResult)
            {
                beginTime = getCurrentTime();

                if(myUniMagReader.startSwipeCard())
                {

                    Log.d("Demo Info >>>>>","to startSwipeCard");
                }
                else
                    Log.d("Demo Info >>>>>","cannot startSwipeCard");
            }
        }
    }

    public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {

        if (resultCode == Activity.RESULT_OK) {

            String strTmpFileName = "";//data.getStringExtra(FileDialog.RESULT_PATH);;
            if (requestCode == REQUEST_GET_XML_FILE) {

                String file_path = null;
                File root = Environment.getExternalStorageDirectory();
                File dir = new File(root.getAbsolutePath() + "/XML");
                //for (int i = 0; i < tickets_registerfor_print.size(); i++) {
                file_path = dir.toString() + "/" + "idt_unimagcfg_default.xml";
                //Log.i("Attendee Detail", "Image Path=" + file_path);
                //mFiles.add(file_path);
                //}
                if(!isFileExist(file_path)){
                    Log.i("--- UniMagIIDemo ----"," no file found "+file_path);
                }

                if(!isFileExist(strTmpFileName))
                {

                    return  ;
                }
                if (!strTmpFileName.endsWith(".xml")){

                    return  ;
                }

                /////////////////////////////////////////////////////////////////////////////////
                // loadingConfigurationXMLFile() method may connect to server to download xml file.
                // Network operation is prohibited in the UI Thread if target API is 11 or above.
                // If target API is 11 or above, please use AsyncTask to avoid errors.
                myUniMagReader.setXMLFileNameWithPath(file_path);
                if (myUniMagReader.loadingConfigurationXMLFile(false)) {

                }
                else {

                }
            }
            else if (requestCode == REQUEST_GET_BIN_FILE)
            {
                if(!isFileExist(strTmpFileName))
                {

                    return  ;
                }
                //set BIN file
                if(true==firmwareUpdateTool.setFirmwareBINFile(strTmpFileName))
                {

                }
                else
                {

                }
            }
            else if(requestCode == REQUEST_GET_ENCRYPTED_BIN_FILE)
            {

                if(!isFileExist(strTmpFileName))
                {

                    return  ;
                }
                //set BIN file
                if(true==firmwareUpdateTool.setFirmwareEncryptedBINFile(strTmpFileName))
                {

                }
                else
                {

                }
            }
        }
    }






    private void initializeReader(ReaderType type) {
        try{ //myUniMagReader.registerListen();
            if(myUniMagReader!=null) {
                myUniMagReader.unregisterListen();
                myUniMagReader.release();
                myUniMagReader = null;
            }
            myUniMagReader = new uniMagReader(this,context,type);

            if (myUniMagReader == null)
                return;

            myUniMagReader.setVerboseLoggingEnable(true);
            myUniMagReader.registerListen();

            //load the XML configuratin file
            String fileNameWithPath = getConfigurationFileFromRaw();
            if(!isFileExist(fileNameWithPath)) {
                fileNameWithPath = null;
            }

            if (isUseAutoConfigProfileChecked) {
                if (profileDatabase.updateProfileFromDB()) {
                    this.profile = profileDatabase.getProfile();
                    Toast.makeText(context, "AutoConfig profile has been loaded.", Toast.LENGTH_LONG).show();
                    handler.post(doConnectUsingProfile);
                }
                else {
                    Toast.makeText(context, "No profile found. Please run AutoConfig first.", Toast.LENGTH_LONG).show();
                }
            } else {
                /////////////////////////////////////////////////////////////////////////////////
                // Network operation is prohibited in the UI Thread if target API is 11 or above.
                // If target API is 11 or above, please use AsyncTask to avoid errors.
                //fileNameWithPath ="android.resource://" + getPackageName() + "/" + R.raw.idt_unimagcfg_default;
                File xmlFile = null;
                try {
                    // AssetManager am = getAssets();
                    xmlFile = new File(new URI(("file:///android_assets/idt_unimagcfg_default.xml")));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                myUniMagReader.setXMLFileNameWithPath(xmlFile.getPath());
                myUniMagReader.loadingConfigurationXMLFile(true);
                //new LoadXml(fileNameWithPath).execute();
                /////////////////////////////////////////////////////////////////////////////////
            }
            // Toast.makeText(context, "Please connect a reader first.", Toast.LENGTH_LONG).show();

            //Initializing SDKTool for firmware update
        /*firmwareUpdateTool = new uniMagSDKTools(this,this);
        firmwareUpdateTool.setUniMagReader(myUniMagReader);
        myUniMagReader.setSDKToolProxy(firmwareUpdateTool.getSDKToolProxy());*/
            if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                ((SelfcheckinPaymentActivity) context).myUniMagReader = myUniMagReader;
            }else {
                ((SellOrderActivity) context).myUniMagReader = myUniMagReader;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void startAutoConfig() {
        isDialogShown=true;
        String fileNameWithPath = getAutoConfigProfileFileFromRaw();
        if(!isFileExist(fileNameWithPath)) {
            fileNameWithPath = null;
        }
        boolean startAcRet = myUniMagReader.startAutoConfig(fileNameWithPath,true);
        popupDialogMsg = "Configuring Card Reader....";
        handler.post(doShowTopDlg);
        if (startAcRet) {
            strProgressInfo=null;
            handler.post(doUpdateAutoConfigProgressInfo);
            percent = 0;
            beginTime = getCurrentTime();
            autoconfig_running = true;
        }
    }

    private class LoadXml extends AsyncTask {
        String fileName="";
        public LoadXml(String fileName){
            this.fileName=fileName;
        }
        @Override
        protected Object doInBackground(Object[] params) {
            String file_path = null;
            File root = Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/XML");
            //for (int i = 0; i < tickets_registerfor_print.size(); i++) {
            file_path = dir.toString() + "/" + "idt_unimagcfg_default.xml";
            //Log.i("Attendee Detail", "Image Path=" + file_path);
            //mFiles.add(file_path);
            //}
            if(!isFileExist(file_path)){
                Log.i("--- UniMagIIDemo ----"," no file found "+file_path);
            }
            myUniMagReader.setXMLFileNameWithPath(file_path);
            myUniMagReader.loadingConfigurationXMLFile(true);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            /*firmwareUpdateTool = new uniMagSDKTools(UniMugActivity.this,UniMugActivity.this);
            firmwareUpdateTool.setUniMagReader(myUniMagReader);
            myUniMagReader.setSDKToolProxy(firmwareUpdateTool.getSDKToolProxy());*/

        }
    }

    private void initializeReader()
    {
/*
		if(myUniMagReader!=null){
			myUniMagReader.unregisterListen();
			myUniMagReader.release();
			myUniMagReader = null;
		}
//		if (isConnectWithCommand)
			myUniMagReader = new uniMagReader(this,this,true);
//		else
//		myUniMagReader = new uniMagReader(this,this);
*/
        if (myUniMagReader == null)
            return;

        myUniMagReader.setVerboseLoggingEnable(true);
        myUniMagReader.registerListen();

        //load the XML configuratin file
        String fileNameWithPath = getConfigurationFileFromRaw();
        if(!isFileExist(fileNameWithPath)) {
            fileNameWithPath = null;
        }

        if (isUseAutoConfigProfileChecked) {
            if (profileDatabase.updateProfileFromDB()) {
                this.profile = profileDatabase.getProfile();
                Toast.makeText(context, "AutoConfig profile has been loaded.", Toast.LENGTH_LONG).show();
                handler.post(doConnectUsingProfile);
            }
            else {
                Toast.makeText(context, "No profile found. Please run AutoConfig first.", Toast.LENGTH_LONG).show();
            }
        } else {
            /////////////////////////////////////////////////////////////////////////////////
            // Network operation is prohibited in the UI Thread if target API is 11 or above.
            // If target API is 11 or above, please use AsyncTask to avoid errors.
            myUniMagReader.setXMLFileNameWithPath(fileNameWithPath);
            myUniMagReader.loadingConfigurationXMLFile(false);
            /////////////////////////////////////////////////////////////////////////////////
        }
        //Initializing SDKTool for firmware update
        firmwareUpdateTool = new uniMagSDKTools(this,context);
        firmwareUpdateTool.setUniMagReader(myUniMagReader);
        myUniMagReader.setSDKToolProxy(firmwareUpdateTool.getSDKToolProxy());
    }


    private String getConfigurationFileFromRaw( ){
        return getXMLFileFromRaw("idt_unimagcfg_default.xml");
        //return getXMLFileFromRaw("umcfg.xml");
    }
    private String getAutoConfigProfileFileFromRaw( ){
        //share the same copy with the configuration file
        return getXMLFileFromRaw("umcfg.xml");
    }

    // If 'idt_unimagcfg_default.xml' file is found in the 'raw' folder, it returns the file path.
    private String getXMLFileFromRaw(String fileName ){
        //the target filename in the application path
        String fileNameWithPath = null;
        fileNameWithPath = fileName;

        try {
            InputStream in = ((Activity)context).getResources().openRawResource(R.raw.idt_unimagcfg_default);//getAssets().open("idt_unimagcfg_default.xml");//
            int length = in.available();
            byte [] buffer = new byte[length];
            in.read(buffer);
            in.close();
            context.deleteFile(fileNameWithPath);
            FileOutputStream fout = context.openFileOutput(fileNameWithPath, MODE_PRIVATE);
            fout.write(buffer);
            fout.close();

            // to refer to the application path
            File fileDir = context.getFilesDir();
            fileNameWithPath = fileDir.getParent() + File.separator + fileDir.getName();
            fileNameWithPath += File.separator+"idt_unimagcfg_default.xml";

        } catch(Exception e) {
            e.printStackTrace();
            fileNameWithPath = null;
        }
        return fileNameWithPath;
    }

   /* @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if ((keyCode == KeyEvent.KEYCODE_BACK||KeyEvent.KEYCODE_HOME==keyCode||KeyEvent.KEYCODE_SEARCH==keyCode)){
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
   	if ((keyCode == KeyEvent.KEYCODE_BACK||KeyEvent.KEYCODE_HOME==keyCode||KeyEvent.KEYCODE_SEARCH==keyCode)){

			return false;
		}	return super.onKeyMultiple(keyCode, repeatCount, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if ((keyCode == KeyEvent.KEYCODE_BACK||KeyEvent.KEYCODE_HOME==keyCode||KeyEvent.KEYCODE_SEARCH==keyCode)){
 			return false;
		}
    	return super.onKeyUp(keyCode, event);
	}*/

    // Returns reader name based on abbreviations
    private String getReaderName(ReaderType rt){
        switch(rt){
            case UM:
                return "UniMag";
            case UM_PRO:
                return "UniMag Pro";
            case UM_II:
                return "UniMag II";
            case SHUTTLE:
                return "Shuttle";
            case UM_OR_PRO:
                return "UniMag or UniMag Pro";
        }
        return "Unknown";

    }
    //for uniMagReader.getAttachedReaderType()
    public ReaderType getAttachedReaderType(int uniMagUnit) {
        switch (uniMagUnit) {
            case StateList.uniMag2G3GPro:
                return ReaderType.UM_OR_PRO;
            case StateList.uniMagII:
                return ReaderType.UM_II;
            case StateList.uniMagShuttle:
                return ReaderType.SHUTTLE;
            case StateList.uniMagUnkown:
            default:
                return ReaderType.UNKNOWN;
        }
    }
    private void showAboutInfo()
    {
        String strManufacture = myUniMagReader.getInfoManufacture();
        String strModel = myUniMagReader.getInfoModel();
        String strDevice = android.os.Build.DEVICE;
        String strSDKVerInfo = myUniMagReader.getSDKVersionInfo();
        String strXMLVerInfo = myUniMagReader.getXMLVersionInfo();
        String selectedReader;
        if (readerType == 0)
            selectedReader = "UniMag/UniMag Pro";
        else if (readerType == 1)
            selectedReader = "UniMag II/Shuttle";
        else
            selectedReader = "Unknown";


        String strOSVerInfo = android.os.Build.VERSION.RELEASE;
//		Log.e("Device", android.os.Build.DEVICE);
//		Log.e("Product", android.os.Build.PRODUCT);
//		Log.e("Brand", android.os.Build.BRAND);
//		Log.e("Board", android.os.Build.BOARD);
//		Log.e("Manufacturer", android.os.Build.MANUFACTURER);
//		Log.e("Model", android.os.Build.MODEL);
//		Log.e("ID", android.os.Build.ID);

    }
    private Runnable doShowTimeoutMsg = new Runnable()
    {
        public void run()
        {
            if(itemStartSC!=null&&enableSwipeCard==true)
                itemStartSC.setEnabled(true);
            enableSwipeCard = false;
            showDialog(popupDialogMsg);
        }

    };
    // shows messages on the popup dialog
    private void showDialog(String strTitle)
    {
        try
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Card Reader Initializing....");
            builder.setMessage(strTitle);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    };

    private Runnable doShowTopDlg = new Runnable()
    {
        public void run()
        {
            showTopDialog(popupDialogMsg);
        }
    };
    private Runnable doHideTopDlg = new Runnable()
    {
        public void run()
        {
            hideTopDialog( );
        }

    };
    private Runnable doShowSwipeTopDlg = new Runnable()
    {
        public void run()
        {
            showSwipeTopDialog( );
        }
    };
    private Runnable doShowYESNOTopDlg = new Runnable()
    {
        public void run()
        {
            showYesNoDialog( );
        }
    };
    private Runnable doHideSwipeTopDlg = new Runnable()
    {
        public void run()
        {
            hideSwipeTopDialog( );
        }
    };
    // displays result of commands, autoconfig, timeouts, firmware update progress and results.
    private Runnable doUpdateStatus = new Runnable()
    {
        public void run()
        {
            try {
                //textAreaTop.setText(statusText);
                //headerTextView.setText("Command Info");
                if(msrData!=null) {
                    StringBuffer hexString = new StringBuffer();

                    hexString.append("<");
                    String fix = null;
                    for (int i = 0; i < msrData.length; i++) {
                        fix = Integer.toHexString(0xFF & msrData[i]);
                        if(fix.length()==1)
                            fix = "0"+fix;
                        hexString.append(fix);
                        if((i+1)%4==0&&i!=(msrData.length-1))
                            hexString.append(' ');
                    }
                    hexString.append(">");
                    //textAreaBottom.setText(hexString.toString());
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    };
    // displays result of commands, autoconfig, timeouts, firmware update progress and results.
    private Runnable doUpdateAutoConfigProgress = new Runnable() {
        public void run()
        {
            try {
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    };
    String strProgressInfo = "";
    // displays result of commands, autoconfig, timeouts, firmware update progress and results.
    private Runnable doUpdateAutoConfigProgressInfo = new Runnable() {
        public void run() {
            try {
                //Toast.makeText(context,AppUtils.NullChecker(strProgressInfo),Toast.LENGTH_LONG).show();
                if(isReaderConnected && isDialogShown) {
                    handler.post(doHideTopDlg);
                    myUniMagReader.startSwipeCard();
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    };
    // displays result of get challenge command
    private Runnable doUpdateChallengeData = new Runnable()
    {
        public void run()
        {
            try {
                if(cmdGetChallenge_Succeed_WithChallengData==challengeResult) {

                }
                else if (cmdGetChallenge_Succeed_WithFileVersion==challengeResult) {

                }

            }
            catch(Exception ex)
            {
                ex.printStackTrace();

            }
        }
    };
    // displays data from card swiping
    private Runnable doUpdateTVS = new Runnable()
    {
        public void run()
        {
            try
            {
                //CardData cd = new CardData(msrData);
                if(itemStartSC!=null)
                    itemStartSC.setEnabled(true);
                String s = new String(strMsrData);
                String result = strMsrData.replaceAll("[\\-\\+\\.\\^:,]","");
                //textAreaTop.setText(strMsrData+"  \n \n "+result);
                Log.i("---- Card Data ----", " "+strMsrData+" \n "+s);

                StringBuffer hexString = new StringBuffer();
                hexString.append("<");
                String fix = null;
                for (int i = 0; i < msrData.length; i++) {
                    fix = Integer.toHexString(0xFF & msrData[i]);
                    if(fix.length()==1)
                        fix = "0"+fix;
                    hexString.append(fix);
                    if((i+1)%4==0&&i!=(msrData.length-1))
                        hexString.append(' ');
                }
                hexString.append(">");
                adjustTextView();
                myUniMagReader.WriteLogIntoFile(hexString.toString());
                if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                    ((SelfcheckinPaymentActivity) context).onSwipMessageReceived(strMsrData);
                }else {
                    ((SellOrderActivity) context).onSwipMessageReceived(strMsrData);
                }
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
    };
    private void adjustTextView()
    {
        /*int height = (textAreaTop.getHeight()+ textAreaBottom.getHeight())/2;
        textAreaTop.setHeight(height);
        textAreaBottom.setHeight(height);*/
    }
    // displays a connection status of UniMag reader
    private Runnable doUpdateTV = new Runnable()
    {
        public void run()
        {
            /*if(!isReaderConnected)
                connectStatusTextView.setText("DISCONNECTED");
            else
                connectStatusTextView.setText("CONNECTED");*/
        }
    };
    private Runnable doUpdateToast = new Runnable()
    {
        public void run()
        {
            try{
                Context context1 = context.getApplicationContext();
                String msg = null;//"To start record the mic.";
                if(isReaderConnected)
                {
                    msg = "Reader CONNECTED";
                    int duration = Toast.LENGTH_SHORT ;
                    Toast.makeText(context1, msg, duration).show();
                    if(itemStartSC!=null)
                        itemStartSC.setEnabled(true);
                    AppUtils.displayLog("---UniMugTransparentActivity---","isDialogShown--"+isDialogShown);
                    if(isDialogShown)
                        swipeCard();
                }
            }catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
    };
    private Runnable doConnectUsingProfile = new Runnable()
    {
        public void run() {
            if (myUniMagReader != null)
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                myUniMagReader.connectWithProfile(profile);
            }
        }
    };

    /***
     * Class: UniMagTopDialog
     * Author: Eric Yang
     * Date: 2010.10.12
     * Function: to show the dialog on the top of the desktop.
     *
     * *****/
    private class UniMagTopDialog extends Dialog {

        public UniMagTopDialog(Context context) {
            super(context);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if ((keyCode == KeyEvent.KEYCODE_BACK|| KeyEvent.KEYCODE_HOME==keyCode|| KeyEvent.KEYCODE_SEARCH==keyCode)){
                return false;
            }
            return super.onKeyDown(keyCode, event);
        }

        @Override
        public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
            if ((keyCode == KeyEvent.KEYCODE_BACK|| KeyEvent.KEYCODE_HOME==keyCode|| KeyEvent.KEYCODE_SEARCH==keyCode)){

                return false;
            }	return super.onKeyMultiple(keyCode, repeatCount, event);
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if ((keyCode == KeyEvent.KEYCODE_BACK|| KeyEvent.KEYCODE_HOME==keyCode|| KeyEvent.KEYCODE_SEARCH==keyCode)){
                return false;
            }
            return super.onKeyUp(keyCode, event);
        }
    }

    private class UniMagTopDialogYESNO extends Dialog {

        public UniMagTopDialogYESNO(Context context) {
            super(context);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if ((keyCode == KeyEvent.KEYCODE_BACK|| KeyEvent.KEYCODE_HOME==keyCode|| KeyEvent.KEYCODE_SEARCH==keyCode)){
                return false;
            }
            return super.onKeyDown(keyCode, event);
        }

        @Override
        public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
            if ((keyCode == KeyEvent.KEYCODE_BACK|| KeyEvent.KEYCODE_HOME==keyCode|| KeyEvent.KEYCODE_SEARCH==keyCode)){

                return false;
            }	return super.onKeyMultiple(keyCode, repeatCount, event);
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if ((keyCode == KeyEvent.KEYCODE_BACK|| KeyEvent.KEYCODE_HOME==keyCode|| KeyEvent.KEYCODE_SEARCH==keyCode)){
                return false;
            }
            return super.onKeyUp(keyCode, event);
        }
    }

    private void showTopDialog(String strTitle) {
        hideTopDialog();
        if(dlgTopShow==null)
            dlgTopShow = new UniMagTopDialog(context);
        try
        {
            Window win = dlgTopShow.getWindow();
            win.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            win.requestFeature(Window.FEATURE_NO_TITLE);
            dlgTopShow.setTitle("Card Reader Initializing.....");
            dlgTopShow.setContentView(R.layout.dlgtopview );
            TextView myTV = (TextView)dlgTopShow.findViewById(R.id.TView_Info);

            myTV.setText(popupDialogMsg);
            dlgTopShow.setOnKeyListener(new OnKeyListener(){
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event) {
                    if ((keyCode == KeyEvent.KEYCODE_BACK)){
                        return false;
                    }
                    return true;
                }
            });
            dlgTopShow.show();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            dlgTopShow = null;
        }
    };
    private void hideTopDialog( )
    {
        if(dlgTopShow!=null)
        {
            try{
                dlgTopShow.hide();
                dlgTopShow.dismiss();
            }
            catch(Exception ex)
            {

                ex.printStackTrace();
            }
            dlgTopShow = null;
        }
    };

    private void showSwipeTopDialog() {
        hideSwipeTopDialog();
        try{
            long currentTime=getCurrentTime();
            if(dlgSwipeTopShow==null)
                dlgSwipeTopShow = new UniMagTopDialog(context);

            Window win = dlgSwipeTopShow.getWindow();
            win.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            win.requestFeature(Window.FEATURE_NO_TITLE);
            dlgSwipeTopShow.setTitle("Swipe Card");
            dlgSwipeTopShow.setContentView(R.layout.dlgswipetopview );
            final TextView myTV = (TextView)dlgSwipeTopShow.findViewById(R.id.TView_Info);
            Button myBtn = (Button)dlgSwipeTopShow.findViewById(R.id.btnCancel);
            ProgressBar progress_swipecard=(ProgressBar)dlgSwipeTopShow.findViewById(R.id.progress_swipecard);
            myTV.setText(popupDialogMsg);
            if(popupDialogMsg.contains("Waiting for Swipe.....!")) {
                progress_swipecard.setVisibility(View.VISIBLE);
                myTV.setVisibility(View.VISIBLE);
            }else{
                myTV.setVisibility(View.VISIBLE);
                progress_swipecard.setVisibility(View.GONE);
            }

            myBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if(itemStartSC!=null)
                        itemStartSC.setEnabled(true);
                    //stop swipe
                    myUniMagReader.stopSwipeCard();
                    if (readerType == 2)
                        isWaitingForCommandResult = true;

                    if (dlgSwipeTopShow != null) {
                        statusText = "Swipe card cancelled.";
                        msrData = null;
                        handler.post(doUpdateStatus);
                        dlgSwipeTopShow.dismiss();
                    }
                }
            });
            dlgSwipeTopShow.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                        return false;
                    }
                    return true;
                }
            });

            dlgSwipeTopShow.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if(itemStartSC!=null)
                        itemStartSC.setEnabled(true);
                    //stop swipe
                    myUniMagReader.stopSwipeCard();
                    if (readerType == 2)
                        isWaitingForCommandResult = true;

                    if (dlgSwipeTopShow != null) {
                        statusText = "Swipe card cancelled.";
                        msrData = null;
                        handler.post(doUpdateStatus);
                        dlgSwipeTopShow.dismiss();
                    }
                }
            });
            dlgSwipeTopShow.show();
        }

        catch(Exception ex) {
            ex.printStackTrace();
        }
    };
    private void showYesNoDialog() {
        hideSwipeTopDialog();
        try {

            if(dlgYESNOTopShow==null)
                dlgYESNOTopShow = new UniMagTopDialogYESNO(context);

            Window win = dlgYESNOTopShow.getWindow();
            win.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            dlgYESNOTopShow.setTitle("Warning");

            dlgYESNOTopShow.setContentView(R.layout.dlgtopview2bnt );
            TextView myTV = (TextView)dlgYESNOTopShow.findViewById(R.id.TView_Info);
            myTV.setTextColor(0xFF0FF000);
            Button myBtnYES = (Button)dlgYESNOTopShow.findViewById(R.id.btnYes);
            Button myBtnNO = (Button)dlgYESNOTopShow.findViewById(R.id.btnNo);

            //myTV.setText("Warrning, Now will Update Firmware if you press 'YES' to update, or 'No' to cancel");
            myTV.setText("Upgrading the firmware might cause the device to not work properly. \nAre you sure you want to continue? ");

            myBtnYES.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    updateFirmware_exTools();
                    dlgYESNOTopShow.dismiss();
                }
            });

            myBtnNO.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dlgYESNOTopShow.dismiss();
                }
            });

            dlgYESNOTopShow.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event) {
                    if ((keyCode == KeyEvent.KEYCODE_BACK)){
                        return false;
                    }
                    return true;
                }
            });
            dlgYESNOTopShow.show();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    };

    private void hideSwipeTopDialog() {
        try {
            if(dlgSwipeTopShow!=null) {
                dlgSwipeTopShow.hide();
                dlgSwipeTopShow.dismiss();
                dlgSwipeTopShow = null;
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    };

    // implementing a method onReceiveMsgCardData, defined in uniMagReaderMsg interface
    // receiving card data here
    public void onReceiveMsgCardData(byte flagOfCardData,byte[] cardData) {

        String s1 = Arrays.toString(cardData);
        //String s2 = new String(cardData);
        String[] byteValues = s1.substring(1, s1.length() - 1).split(",");
        byte[] bytes = new byte[byteValues.length];

        for (int i=0, len=bytes.length; i<len; i++) {
            bytes[i] = Byte.parseByte(byteValues[i].trim());
        }

        String str = new String(cardData);
        String str1 = new String(bytes);
        Log.i("---String Card Data---"," ==== "+str);
        Log.i("---String Card Data1---"," ==== "+str1);
        if (cardData.length > 5)
            if (cardData[0] == 0x25 && cardData[1] == 0x45) {
                statusText = "Swipe error. Please try again.";
                msrData = new byte[cardData.length];
                System.arraycopy(cardData, 0, msrData, 0, cardData.length);
                enableSwipeCard = true;
                handler.post(doHideSwipeTopDlg);
                handler.post(doUpdateStatus);
                return;
            }

        byte flag = (byte) (flagOfCardData&0x04);
//		Log.d("Demo Info >>>>> onReceive flagOfCardData="+flagOfCardData,"CardData="+ getHexStringFromBytes(cardData));

        if(flag==0x00)
            strMsrData = new String(cardData);
        if(flag==0x04) {
            //You need to decrypt the data here first.
            strMsrData = new String(cardData);
        }
        msrData = new byte[cardData.length];
        System.arraycopy(cardData, 0, msrData, 0, cardData.length);
        enableSwipeCard = true;
        handler.post(doHideTopDlg);
        handler.post(doHideSwipeTopDlg);
        handler.post(doUpdateTVS);
    }

    // implementing a method onReceiveMsgConnected, defined in uniMagReaderMsg interface
    // receiving a message that the uniMag device has been connected
    public void onReceiveMsgConnected() {

        isReaderConnected = true;
        if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
            SelfcheckinPaymentActivity.isUnimagReaderConnected = true;
        }else {
            SellOrderActivity.isUnimagReaderConnected = true;
        }
        if(percent==0) {
            if(profile!=null) {
                if(profile.getModelNumber().length()>0)
                    statusText = "Now the UniMag Unit is connected.("+getTimeInfoMs(beginTime)+"s, with profile "+profile.getModelNumber()+")";
                else
                    statusText = "Now the UniMag Unit is connected.("+getTimeInfoMs(beginTime)+"s)";
            }
            else
                statusText = "Now the UniMag Unit is connected."+" ("+getTimeInfoMs(beginTime)+"s)";
        }
        else {
            if(profile!=null)
                statusText = "Now the UniMag Unit is connected.("+getTimeInfoMs(beginTime)+"s, "+"Profile found at "+percent +"% named "+profile.getModelNumber()+",auto config last " +getTimeInfoMs(beginTimeOfAutoConfig)+"s)";
            else
                statusText = "Now the UniMag Unit is connected."+" ("+getTimeInfoMs(beginTime)+"s, "+"Profile found at "+percent +"%,auto config last " +getTimeInfoMs(beginTimeOfAutoConfig)+"s)";
            percent = 0;
        }
        handler.post(doHideTopDlg);
        handler.post(doHideSwipeTopDlg);
        handler.post(doUpdateTV);
        handler.post(doUpdateToast);
        msrData = null;
        handler.post(doUpdateStatus);
        handler.post(doUpdateAutoConfigProgressInfo);

    }

    // implementing a method onReceiveMsgDisconnected, defined in uniMagReaderMsg interface
    // receiving a message that the uniMag device has been disconnected
    public void onReceiveMsgDisconnected() {
        percent=0;
        strProgressInfo=null;
        if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
            SelfcheckinPaymentActivity.isUnimagReaderConnected=false;
        }else {
            SellOrderActivity.isUnimagReaderConnected = false;
        }
        isReaderConnected = false;
        isWaitingForCommandResult=false;
        autoconfig_running=false;
        handler.post(doHideTopDlg);
        handler.post(doHideSwipeTopDlg);
        handler.post(doUpdateTV);
        showAboutInfo();
        Toast.makeText(context,"Reader Disconnected",Toast.LENGTH_LONG).show();
    }
    // implementing a method onReceiveMsgTimeout, defined in uniMagReaderMsg inteface
    // receiving a timeout message for powering up or card swipe
    public void onReceiveMsgTimeout(String strTimeoutMsg) {
        isWaitingForCommandResult=false;
        enableSwipeCard = true;
        handler.post(doHideTopDlg);
        handler.post(doHideSwipeTopDlg);
        statusText = strTimeoutMsg+"("+getTimeInfo(beginTime)+")";
        msrData = null;
        handler.post(doUpdateStatus);
    }
    // implementing a method onReceiveMsgToConnect, defined in uniMagReaderMsg interface
    // receiving a message when SDK starts powering up the UniMag device
    public void onReceiveMsgToConnect(){
        beginTime = System.currentTimeMillis();
        handler.post(doHideTopDlg);
        handler.post(doHideSwipeTopDlg);
        popupDialogMsg = "Powering up Card Reader.....";
        if(isDialogShown)
            handler.post(doShowTopDlg);
    }
    // implementing a method onReceiveMsgToSwipeCard, defined in uniMagReaderMsg interface
    // receiving a message when SDK starts recording, then application should ask user to swipe a card
    public void onReceiveMsgToSwipeCard() {
        //textAreaTop.setText("");
        //popupDialogMsg = "Waiting for Swipe.....! ("+getTimeInfoMs(beginTime)+" sec)";
        popupDialogMsg = "Waiting for Swipe.....!";
        handler.post(doHideTopDlg);
        handler.post(doHideSwipeTopDlg);
        handler.post(doShowSwipeTopDlg);
    }
    // implementing a method onReceiveMsgProcessingCardData, defined in uniMagReaderMsg interface
    // receiving a message when SDK detects data coming from the UniMag reader
    // The main purpose is to give early notification to user to wait until SDK finishes processing card data.
    public void onReceiveMsgProcessingCardData() {
        statusText = "Card data is being processed. Please wait.";
        msrData = null;
        handler.post(doUpdateStatus);
    }

    public void onReceiveMsgToCalibrateReader() {
        statusText = "Reader needs to be calibrated. Please wait.";
        msrData = null;
        handler.post(doUpdateStatus);
    }
    // this method has been depricated, and will not be called in this version of SDK.
    public void onReceiveMsgSDCardDFailed(String strSDCardFailed)
    {
        popupDialogMsg = strSDCardFailed;
        handler.post(doHideTopDlg);
        handler.post(doHideSwipeTopDlg);
        handler.post(doShowTimeoutMsg);
    }
    // Setting a permission for user
    public boolean getUserGrant(int type, String strMessage) {
        AppUtils.displayLog("Demo Info >>>>> getUserGrant:",strMessage);
        boolean getUserGranted = false;
        switch(type)
        {
            case uniMagReaderMsg.typeToPowerupUniMag:
                //pop up dialog to get the user grant
                getUserGranted = true;
                break;
            case uniMagReaderMsg.typeToUpdateXML:
                //pop up dialog to get the user grant
                getUserGranted = true;
                break;
            case uniMagReaderMsg.typeToOverwriteXML:
                //pop up dialog to get the user grant
                getUserGranted = true;
                break;
            case uniMagReaderMsg.typeToReportToIdtech:
                //pop up dialog to get the user grant
                getUserGranted = true;
                break;
            default:
                getUserGranted = false;
                break;
        }
        return getUserGranted;
    }
    // implementing a method onReceiveMsgFailureInfo, defined in uniMagReaderMsg interface
    // receiving a message when SDK could not find a profile of the phone
    public void onReceiveMsgFailureInfo(int index, String strMessage) {
        isWaitingForCommandResult = false;

        // If AutoConfig found a profile before and saved into db, then retreive it and connect.
        if (profileDatabase.updateProfileFromDB()) {
            this.profile = profileDatabase.getProfile();
            showAboutInfo();
            handler.post(doConnectUsingProfile);
        } else {
            statusText = "Failure index: "+index+", message: "+strMessage;
            msrData = null;
            handler.post(doUpdateStatus);
        }
        //Cannot support current phone in the XML file.
        //start to Auto Config the parameters
        if(myUniMagReader.startAutoConfig(false)==true) {
            beginTime = getCurrentTime();
        }
    }
    // implementing a method onReceiveMsgCommandResult, defined in uniMagReaderMsg interface
    // receiving a message when SDK is able to parse a response for commands from the reader
    public void onReceiveMsgCommandResult(int commandID, byte[] cmdReturn) {
        AppUtils.displayLog("Demo Info >>>>> onReceive commandID="+commandID,",cmdReturn="+ getHexStringFromBytes(cmdReturn));
        isWaitingForCommandResult = false;

        if (cmdReturn.length > 1){
            if (6==cmdReturn[0]&&(byte)0x56==cmdReturn[1])
            {
                statusText = "Failed to send command. Attached reader is in boot loader mode. Format:<"+getHexStringFromBytes(cmdReturn)+">";
                handler.post(doUpdateStatus);
                return;
            }
        }

        switch(commandID)
        {
            case uniMagReaderMsg.cmdGetNextKSN:
                if(0==cmdReturn[0])
                    statusText = "Get Next KSN timeout.";
                else if(6==cmdReturn[0])
                    statusText = "Get Next KSN Succeed.";
                else
                    statusText = "Get Next KSN failed.";
                break;
            case uniMagReaderMsg.cmdEnableAES:
                if(0==cmdReturn[0])
                    statusText = "Turn on AES timeout.";
                else if(6==cmdReturn[0])
                    statusText = "Turn on AES Succeed.";
                else
                    statusText = "Turn on AES failed.";
                break;
            case uniMagReaderMsg.cmdEnableTDES:
                if(0==cmdReturn[0])
                    statusText = "Turn on TDES timeout.";
                else if(6==cmdReturn[0])
                    statusText = "Turn on TDES Succeed.";
                else
                    statusText = "Turn on TDES failed.";
                break;
            case uniMagReaderMsg.cmdGetVersion:
                if(0==cmdReturn[0])
                    statusText = "Get Version timeout.";
                else if (cmdReturn.length <= 3)
                    statusText = "Get Version: "+new String(cmdReturn);
                else if(6==cmdReturn[0]&&2==cmdReturn[1]&&3==cmdReturn[cmdReturn.length-2])
                {
                    statusText = null;
                    byte cmdDataX[]  = new byte[cmdReturn.length-4];
                    System.arraycopy(cmdReturn, 2, cmdDataX, 0, cmdReturn.length-4);
                    statusText = "Get Version:"+new String(cmdDataX);
                }
                else
                {
                    statusText = "Get Version failed, Error Format:<"+ getHexStringFromBytes(cmdReturn)+">";
                }
                break;
            case uniMagReaderMsg.cmdGetSerialNumber:
                if(0==cmdReturn[0])
                    statusText = "Get Serial Number timeout.";
                else if (cmdReturn.length <= 3)
                    statusText = "Get SerialNumber: "+new String(cmdReturn);
                else if(cmdReturn.length>3 && 6==cmdReturn[0]&&2==cmdReturn[1]&&3==cmdReturn[cmdReturn.length-2])
                {
                    statusText = null;
                    byte cmdDataX[]  = new byte[cmdReturn.length-4];
                    System.arraycopy(cmdReturn, 2, cmdDataX, 0, cmdReturn.length-4);
                    statusText = "Get Serial Number:"+new String(cmdDataX);
                }
                else
                {
                    statusText = "Get Serial Number failed, Error Format:<"+ getHexStringFromBytes(cmdReturn)+">";
                }
                break;
            case uniMagReaderMsg.cmdGetAttachedReaderType:
                int readerType = cmdReturn[0];
                ReaderType art = getAttachedReaderType(readerType);
                statusText = "Attached Reader:\n   "+getReaderName(art) ;
                msrData = null;
                handler.post(doUpdateStatus);
                return;

            case uniMagReaderMsg.cmdGetSettings:
                if(0==cmdReturn[0])
                    statusText = "Get Setting timeout.";
                else if (cmdReturn.length <= 3)
                    statusText = "Get Settings: "+new String(cmdReturn);
                else if(6==cmdReturn[0]&&2==cmdReturn[1]&&3==cmdReturn[cmdReturn.length-2])
                {
                    byte cmdDataX[]  = new byte[cmdReturn.length-4];
                    System.arraycopy(cmdReturn, 2, cmdDataX, 0, cmdReturn.length-4);
                    statusText = "Get Setting:"+ getHexStringFromBytes(cmdDataX);
                    cmdDataX=null;
                }
                else
                {
                    statusText = "Get Setting failed, Error Format:<"+ getHexStringFromBytes(cmdReturn)+">";
                }
                break;
            case uniMagReaderMsg.cmdCalibrate:
                if (6==cmdReturn[0] && (cmdReturn[1] & 0xFF) != 0xEE)
                    statusText = "Calibration succeeded.";
                else
                    statusText = "Calibration failed.";
                break;
            case uniMagReaderMsg.cmdGetBatteryLevel:
                if (cmdReturn.length >=3 && cmdReturn[0] == 6)
                    statusText = "Battery Status: "+ Common.getHexStringFromBytes(new byte[]{cmdReturn[1], cmdReturn[2]});
                else
                    statusText = "Failed to check battery level";
                break;
            default:
                break;
        }
        msrData = null;
        msrData = new byte[cmdReturn.length];
        System.arraycopy(cmdReturn, 0, msrData, 0, cmdReturn.length);
        handler.post(doUpdateStatus);
    }
    // implementing a method onReceiveMsgChallengeResult, defined in uniMagReaderToolsMsg interface
    // receiving a message when SDK is able to parse a response for get challenge command from the reader
    public void onReceiveMsgChallengeResult(int returnCode,byte[] data) {
        isWaitingForCommandResult = false;
        switch(returnCode)
        {
            case uniMagReaderToolsMsg.cmdGetChallenge_Succeed_WithChallengData:
                challengeResult = cmdGetChallenge_Succeed_WithChallengData;
                //show the challenge data and enable edit the hex text view

                if (readerType == 2 && 6==data[0]&&2==data[1]&&3==data[data.length-2]){
                    byte cmdChallengeData[]  = new byte[8];
                    System.arraycopy(data, 2, cmdChallengeData, 0, 8);
                    statusText = "Challenge Data:<"+
                            getHexStringFromBytes(cmdChallengeData)+"> "+"\n"+
                            "please enter "+firmwareUpdateTool.getRequiredChallengeResponseLength()+"-byte challenge response below, as hex, then update firmware.";
                }

                else if(6==data[0]&&2==data[1]&&3==data[data.length-2])
                {
                    statusText = null;
                    byte cmdChallengeData[]  = new byte[8];
                    System.arraycopy(data, 2, cmdChallengeData, 0, 8);
                    byte cmdChallengeData_encyption[]  = new byte[8];
                    System.arraycopy(data, 2, cmdChallengeData_encyption, 0, 8);

                    byte cmdChallengeData_KSN[]  = new byte[10];
                    System.arraycopy(data, 10, cmdChallengeData_KSN, 0, 10);
                    statusText = "Challenge Data:<"+
                            getHexStringFromBytes(cmdChallengeData)+"> "+"\n"+"KSN:<"+
                            getHexStringFromBytes(cmdChallengeData_KSN)+">"+"\n"+
                            "please enter "+firmwareUpdateTool.getRequiredChallengeResponseLength()+"-byte challenge response below, as hex, then update firmware.";
                }
                else {
                    statusText = "Get Challenge failed, Error Format:<"+ getHexStringFromBytes(data)+">";
                }

                break;
            case uniMagReaderToolsMsg.cmdGetChallenge_Succeed_WithFileVersion:
                challengeResult = cmdGetChallenge_Succeed_WithFileVersion;
                if(6==data[0]&&((byte)0x56)==data[1] )
                {
                    statusText = null;
                    byte cmdFileVersion[]  = new byte[2];
                    System.arraycopy(data, 2, cmdFileVersion, 0, 2);
                    char fileVersionHigh=(char) cmdFileVersion[0];
                    char fileVersionLow=(char) cmdFileVersion[1];

                    statusText = "Already in boot load mode, and the file version is "+fileVersionHigh+"."+fileVersionLow+"\n" +
                            "Please update firmware directly.";
                } else
                {
                    statusText = "Get Challenge failed, Error Format:<"+ getHexStringFromBytes(data)+">";
                }

                break;
            case uniMagReaderToolsMsg.cmdGetChallenge_Failed:
                statusText = "Get Challenge failed, please try again.";

                break;
            case uniMagReaderToolsMsg.cmdGetChallenge_NeedSetBinFile:
                statusText = "Get Challenge failed, need to set BIN file first.";
                break;
            case uniMagReaderToolsMsg.cmdGetChallenge_Timeout:
                statusText = "Get Challenge timeout.";
                break;
            default:
                break;
        }
        msrData = null;
        handler.post(doUpdateChallengeData);

    }
    // implementing a method onReceiveMsgUpdateFirmwareProgress, defined in uniMagReaderToolsMsg interface
    // receiving a message of firmware update progress
    public void onReceiveMsgUpdateFirmwareProgress(int progressValue) {
        AppUtils.displayLog("Demo Info >>>>> UpdateFirmwareProgress" ,"v = "+progressValue);
        statusText = "Updating firmware, "+progressValue+"% finished.";
        msrData = null;
        handler.post(doUpdateStatus);

    }
    // implementing a method onReceiveMsgUpdateFirmwareResult, defined in uniMagReaderToolsMsg interface
    // receiving a message when firmware update has been finished
    public void onReceiveMsgUpdateFirmwareResult(int result) {
        isWaitingForCommandResult = false;

        switch(result)
        {
            case uniMagReaderToolsMsg.cmdUpdateFirmware_Succeed:
                statusText = "Update firmware succeed.";
                break;
            case uniMagReaderToolsMsg.cmdUpdateFirmware_NeedSetBinFile:
                statusText = "Update firmware failed, need to set BIN file first";
                break;
            case uniMagReaderToolsMsg.cmdUpdateFirmware_NeedGetChallenge:
                statusText = "Update firmware failed, need to get challenge first.";
                break;
            case uniMagReaderToolsMsg.cmdUpdateFirmware_Need8BytesData:
                statusText = "Update firmware failed, need input 8 bytes data.";
                break;
            case uniMagReaderToolsMsg.cmdUpdateFirmware_Need24BytesData:
                statusText = "Update firmware failed, need input 24 bytes data.";
                break;
            case uniMagReaderToolsMsg.cmdUpdateFirmware_EnterBootloadModeFailed:
                statusText = "Update firmware failed, cannot enter boot load mode.";
                break;
            case uniMagReaderToolsMsg.cmdUpdateFirmware_DownloadBlockFailed:
                statusText = "Update firmware failed, cannot download block data.";
                break;
            case uniMagReaderToolsMsg.cmdUpdateFirmware_EndDownloadBlockFailed:
                statusText = "Update firmware failed, cannot end download block.";
                break;
            case uniMagReaderToolsMsg.cmdUpdateFirmware_Timeout:
                statusText = "Update firmware timeout.";
                break;
        }
        AppUtils.displayLog("Demo Info >>>>> UpdateFirmwareResult" ,"v = "+result);
        msrData = null;
        handler.post(doUpdateStatus);

    }
    // implementing a method onReceiveMsgAutoConfigProgress, defined in uniMagReaderMsg interface
    // receiving a message of Auto Config progress
    public void onReceiveMsgAutoConfigProgress(int progressValue) {
        AppUtils.displayLog("Demo Info >>>>> AutoConfigProgress" ,"v = "+progressValue);
        percent = progressValue;
        statusText = "Searching the configuration automatically, "+progressValue+"% finished."+"("+getTimeInfo(beginTime)+")";
        msrData = null;
        beginTimeOfAutoConfig = beginTime;
        handler.post(doUpdateAutoConfigProgress);
    }
    public void onReceiveMsgAutoConfigProgress(int percent, double result,
                                               String profileName) {
        if(strProgressInfo==null)
            strProgressInfo="("+profileName+ ") <"+percent+"%>,Result="+ Common.getDoubleValue(result);
        else
            strProgressInfo+="\n("+profileName+ ") <"+percent+"%>,Result="+ Common.getDoubleValue(result);
        Log.d("**__@__**","demo = "+strProgressInfo);
        handler.post(doUpdateAutoConfigProgressInfo);
    }

    public void onReceiveMsgAutoConfigCompleted(StructConfigParameters profile) {
        AppUtils.displayLog("Demo Info >>>>> AutoConfigCompleted" ,"A profile has been found, trying to connect...");
        autoconfig_running = false;
        beginTimeOfAutoConfig = beginTime;
        this.profile = profile;
        profileDatabase.setProfile(profile);
        profileDatabase.insertResultIntoDB();
        handler.post(doConnectUsingProfile);
    }

    public void getChallenge()
    {
        getChallenge_exTools();
    }
    public void updateFirmware()
    {
        if (isReaderConnected)
            handler.post(doShowYESNOTopDlg);
        else
            Toast.makeText(context, "Please connect a reader first.", Toast.LENGTH_SHORT).show();
    }
    private void getChallenge_exTools()
    {
        if (firmwareUpdateTool != null)
        {
            if (firmwareUpdateTool.getChallenge() == true)
            {
                isWaitingForCommandResult = true;
                // show to get challenge
                statusText = " To Get Challenge, waiting for response.";
                msrData = null;
                handler.post(doUpdateStatus);
            }
        }
    }
    private void updateFirmware_exTools()
    {

        if (firmwareUpdateTool != null)
        {
            String strData ="";// textAreaBottom.getText().toString();

            if(strData.length()>0)
            {
                challengeResponse = getBytesFromHexString(strData);
                if(challengeResponse==null)
                {
                    statusText = "Invalidate challenge data, please input hex data.";
                    msrData = null;
                    handler.post(doUpdateStatus);
                    return;
                }
            }
            else
                challengeResponse=null;

            isWaitingForCommandResult = true;
            if (firmwareUpdateTool.updateFirmware(challengeResponse) == true)
            {
                statusText = " To Update Firmware, waiting for response.";
                msrData = null;
                handler.post(doUpdateStatus);
            }
        }
    }
    public void prepareToSendCommand(int cmdID)
    {
        isWaitingForCommandResult = true;
        switch(cmdID)
        {
            case uniMagReaderMsg.cmdGetNextKSN:
                statusText = " To Get Next KSN, wait for response.";
                break;
            case uniMagReaderMsg.cmdEnableAES:
                statusText = " To Turn on AES, wait for response.";
                break;
            case uniMagReaderMsg.cmdEnableTDES:
                statusText = " To Turn on TDES, wait for response.";
                break;
            case uniMagReaderMsg.cmdGetVersion:
                statusText = " To Get Version, wait for response.";
                break;
            case uniMagReaderMsg.cmdGetSettings:
                statusText = " To Get Setting, wait for response.";
                break;
            case uniMagReaderMsg.cmdGetSerialNumber:
                statusText = " To Get Serial Number, wait for response.";
                break;
            case uniMagReaderMsg.cmdGetBatteryLevel:
                statusText = " To Check battery level, wait for response.";
                break;

            default:
                break;
        }
        msrData = null;
        handler.post(doUpdateStatus);
    }
    private String getHexStringFromBytes(byte []data)
    {
        if(data.length<=0)
            return null;
        StringBuffer hexString = new StringBuffer();
        String fix = null;
        for (int i = 0; i < data.length; i++) {
            fix = Integer.toHexString(0xFF & data[i]);
            if(fix.length()==1)
                fix = "0"+fix;
            hexString.append(fix);
        }
        fix = null;
        fix = hexString.toString();
        return fix;
    }
    public byte[] getBytesFromHexString(String strHexData)
    {
        if (1==strHexData.length()%2) {
            return null;
        }
        byte[] bytes = new byte[strHexData.length()/2];
        try{
            for (int i=0;i<strHexData.length()/2;i++) {
                bytes[i] = (byte) Integer.parseInt(strHexData.substring(i*2, (i+1)*2) , 16);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
        return bytes;
    }
    static private String getMyStorageFilePath( ) {
        String path = null;
        if(isStorageExist())
            path = Environment.getExternalStorageDirectory().toString();
        return path;
    }
    private boolean isFileExist(String path) {
        if(path==null)
            return false;
        File file = new File(path);
        if (!file.exists()) {
            return false ;
        }
        return true;
    }
    static private boolean isStorageExist() {
        //if the SD card exists
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        return sdCardExist;
    }
    private long getCurrentTime(){
        return System.currentTimeMillis();
    }
    private String getTimeInfo(long timeBase){
        int time = (int)(getCurrentTime()-timeBase)/1000;
        int hour = (int) (time/3600);
        int min = (int) (time/60);
        int sec= (int) (time%60);
        return  hour+":"+min+":"+sec;
    }
    private String getTimeInfoMs(long timeBase){
        float time = (float)((getCurrentTime()-timeBase)/1000) ;
        String strtime = String.format("%03f",time);
        return  strtime;
    }

    public boolean isReaderConnected(){

        if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
            return SelfcheckinPaymentActivity.isUnimagReaderConnected;
        }else {
            return SellOrderActivity.isUnimagReaderConnected;
        }

    }
}