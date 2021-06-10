/**
 * Scanner Settings Application
 * (c) 2011 Socket Mobile, inc.
 */
package com.globalnest.scanattendee.socketmobile;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.util.Map;
import java.util.Vector;

import com.globalnest.scanattendee.R;
import com.globalnest.scanattendee.socketmobile.ScanApiHelper.ScanApiHelperNotification;
import com.globalnest.utils.AppUtils;
import com.socketmobile.scanapi.ISktScanDecodedData;
import com.socketmobile.scanapi.ISktScanObject;
import com.socketmobile.scanapi.ISktScanProperty;
import com.socketmobile.scanapi.ISktScanSymbology;
import com.socketmobile.scanapi.ISktScanVersion;
import com.socketmobile.scanapi.SktScan;
import com.socketmobile.scanapi.SktScanApiOwnership;
import com.socketmobile.scanapi.SktScanApiOwnership.Notification;
import com.socketmobile.scanapi.SktScanDeviceType;
import com.socketmobile.scanapi.SktScanErrors;
import com.socketmobile.scanapicore.SktDebug;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;



/**
 * ScannerSettingsApplication is responsible to link this application
 * to ScanAPI.
 * <p>
 * This application class is responsible for the following action:
 * <li>link to ScanAPI using open and close methods
 * <li>ScanAPI ownership management
 * <li>ScanAPI sending and receiving commands through ScanObject
 * <li>Screen rotation - during screen rotation ScanAPI should stay open
 * otherwise the connection to the scanner will drop
 * <p>
 * ScanAPIOwnership is used here because only one application at the time can
 * use ScanAPI. So if an application is currently using ScanAPI, and a second
 * application is launching and willing to use ScanAPI, it claims ScanAPI ownership
 * which cause the first application to receive this notification and release
 * ScanAPI ownership by closing it.
 * Once the second app is done using ScanAPI, it releases its ownership and close
 * ScanAPI which causes the first application to receive a notification indicating
 * it is now OK to claim again the ownership of ScanAPI and it can open ScanAPI at that
 * moment.
 * @author ericg
 *
 */
public class ScannerSettingsApplication extends Application {

	/**
	 * simple synchronized event
	 * @author ericg
	 *
	 */

	/**
	 * Added by Babu durga prasad to resolve android 4.4 crsh and failed to load crashlytics
	 * @param base
	 */
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
	class Event{
		private boolean _set;
		public Event(boolean set){
			_set=set;
		}
		public synchronized void  set()
		{
			_set=true;
			notify();
		}
		public synchronized void reset(){
			_set=false;
		}
		public synchronized boolean waitFor(long timeoutInMilliseconds)
		{
			long t1,t2=0;
			for(;_set==false;){
				t1=System.currentTimeMillis();
				try {
					wait(timeoutInMilliseconds);
				} catch (InterruptedException e) {
					break;
				}
				t2=System.currentTimeMillis();
				if(_set==false)
				{
					if(t2>=(t1+timeoutInMilliseconds))
						break;
					else
						timeoutInMilliseconds=(t1+timeoutInMilliseconds)-t2;
				}
				else
					break;
			}
			return _set;
		}
	}
	interface ImproveAppChoices{
		public final int YES=0;
		public final int NO=1;
	}
	public static final String IMPROVE_APP_ID="3CHH3NWJXYMRSRHTMF54";// Android ScannerSettings Flurry App ID
	public static final String PREFERENCES_NAME="ScannerSettingsPreferences";
	public static final String IMPROVE_APP="ImproveApp";
	
	/**
	 * definition of the notification or Property Operation Complete messages
	 * the various Activities can receive
	 */
	public static final String NOTIFY_SCANAPI_INIT_COMPLETE = ScannerSettingsApplication.class.getName() + ".NotifyScanApiInitComplete";  
	public static final String NOTIFY_SCANNER_ARRIVAL = ScannerSettingsApplication.class.getName() + ".NotifyScannerArrival";   
	public static final String NOTIFY_SCANNER_REMOVAL = ScannerSettingsApplication.class.getName() + ".NotifyScannerRemoval";
	public static final String NOTIFY_DATA_ARRIVAL = ScannerSettingsApplication.class.getName() + ".NotifyDataArrival";

	public static final String NOTIFY_CLOSE_ACTIVITY=ScannerSettingsApplication.class.getName()+".NotifyCloseActivity";
	public static final String NOTIFY_ERROR_MESSAGE=ScannerSettingsApplication.class.getName()+".NotifyErrorMessage";
	
	public static final String GET_SCANAPI_VERSION_COMPLETE=ScannerSettingsApplication.class.getName()+".GetScanApiVersionComplete";
	public static final String GET_DEVICETYPE_COMPLETE = ScannerSettingsApplication.class.getName() + ".GetDeviceTypeComplete";
	public static final String GET_SCANNER_VERSION_COMPLETE = ScannerSettingsApplication.class.getName() + ".GetDeviceVersionComplete";
	public static final String GET_BATTERYLEVEL_COMPLETE = ScannerSettingsApplication.class.getName() + ".GetBatteryLevelComplete";
	public static final String GET_SUFFIX_COMPLETE = ScannerSettingsApplication.class.getName() + ".GetSuffixComplete";
	public static final String SET_SUFFIX_COMPLETE = ScannerSettingsApplication.class.getName() + ".SetSuffixComplete";
	public static final String GET_DECODEACTION_COMPLETE = ScannerSettingsApplication.class.getName() + ".GetDecodeActionComplete";
	public static final String SET_DECODEACTION_COMPLETE = ScannerSettingsApplication.class.getName() + ".SetDecodeActionComplete";
	public static final String GET_RUMBLECAPABILITY_COMPLETE = ScannerSettingsApplication.class.getName() + ".GetRumbleCapabilityComplete";
	public static final String GET_SYMBOLOGY_COMPLETE = ScannerSettingsApplication.class.getName() + ".GetSymbologyComplete";
	public static final String SET_SYMBOLOGY_COMPLETE = ScannerSettingsApplication.class.getName() + ".SetSymbologyComplete";
	public static final String GET_FRIENDLYNAME_COMPLETE = ScannerSettingsApplication.class.getName() + ".GetFriendlyNameComplete";
	public static final String SET_FRIENDLYNAME_COMPLETE = ScannerSettingsApplication.class.getName() + ".SetFriendlyNameComplete";   
	public static final String GET_SOFTSCAN_STATUS_COMPLETE=ScannerSettingsApplication.class.getName()+".GetSoftScanStatusComplete";
	public static final String SET_SOFTSCAN_STATUS_COMPLETE=ScannerSettingsApplication.class.getName()+".SetSoftScanStatusComplete";

	
	public static final String EXTRA_RESULT=ScannerSettingsApplication.class.getName()+".Result";
	public static final String EXTRA_DEVICENAME=ScannerSettingsApplication.class.getName()+".DeviceName";
	public static final String EXTRA_DEVICETYPE=ScannerSettingsApplication.class.getName()+".DeviceType";
	public static final String EXTRA_SYMBOLOGY_ID=ScannerSettingsApplication.class.getName()+".SymbologyId";
	public static final String EXTRA_SYMBOLOGY_NAME=ScannerSettingsApplication.class.getName()+".SymbologyName";
	public static final String EXTRA_SYMBOLOGY_STATE=ScannerSettingsApplication.class.getName()+".SymbologyState";
	public static final String EXTRA_SYMBOLOGY_ENABLED=ScannerSettingsApplication.class.getName()+".SymbologyEnabled";
	public static final String EXTRA_SUFFIX=ScannerSettingsApplication.class.getName()+".Suffix";
	public static final String EXTRA_DECODEACTION=ScannerSettingsApplication.class.getName()+".DecodeAction";
	public static final String EXTRA_RUMBLE=ScannerSettingsApplication.class.getName()+".Rumble";
	public static final String EXTRA_BATTERYLEVEL=ScannerSettingsApplication.class.getName()+".BatteryLevel";
	public static final String EXTRA_DEVICEVERSION=ScannerSettingsApplication.class.getName()+".DeviceVersion";
	public static final String EXTRA_SCANAPIVERSION=ScannerSettingsApplication.class.getName()+".ScanAPIVersion";
	public static final String EXTRA_DECODEDDATA=ScannerSettingsApplication.class.getName()+".DecodedData";
	public static final String EXTRA_ERROR_MESSAGE=ScannerSettingsApplication.class.getName()+".ErrorMessage";
	public static final String EXTRA_SOFTSCANSTATUS=ScannerSettingsApplication.class.getName()+".SoftScanStatus";
	
	private final int CLOSE_SCAN_API=1;
	
	private static ScannerSettingsApplication _singleton;
	private SktScanApiOwnership _scanApiOwnership;// object managing ScanAPI ownership
	private DeviceInfo _currentSelectedDevice;// current device selected
	private ScanApiHelper _scanApiHelper;// Helper to post and manage commands to send to ScanAPI
	private Event _consumerTerminatedEvent;// event to know when the ScanAPI terminate event has been received
//	private Event _scanApiDoneInitEvent;// event to know when ScanAPI is done with initialization
	private int _viewCount;// View counter (each activity increase or decrease this count when created or destroyed respectively)
	private boolean _forceCloseUI;// flag to force to close the UI
	private boolean _unregisterOwnership;// flag to know when to unregister ScanAPI ownership
	private boolean _abortScanApi;// flag to abort ScanAPI when it's just done with initialization
	private static Context context;
	private Handler _handler=new Handler(new Handler.Callback() {
		
		public boolean handleMessage(Message msg) {
			switch(msg.what){
			case CLOSE_SCAN_API:
				Debug.MSG(Debug.kLevelTrace,"Receive a CLOSE SCAN API Message and View Count="+_viewCount+"ScanAPI open:"+_scanApiHelper.isScanApiOpen());
				// if we receive this message and the view count is 0
				// and ScanAPI is open then we should close it
				if(_viewCount==0){
					if(_scanApiHelper.isScanApiOpen()==true){
						_unregisterOwnership=true;
						closeScanApi();
					}
					else{
						if(_scanApiOwnership.hasOwnership()){
							_abortScanApi=true;
						}
						_unregisterOwnership=true;
					}
				}
				break;
			}
			return false;
		}
	});

	/**
	 * initialization of this application
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics()); //TODO ONCRASHES
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
		_singleton=this;
		_viewCount=0;// there is no view created for this application yet
		_forceCloseUI=false;
		_unregisterOwnership=false;
		_abortScanApi=false;
		
		_consumerTerminatedEvent=new Event(true);
//		_scanApiDoneInitEvent=new Event(true);
		ScannerSettingsApplication.context = getApplicationContext();
		Debug.MSG(Debug.kLevelTrace,"Application onCreate");
		
		// create a ScanAPI Helper 
		_scanApiHelper=new ScanApiHelper();
		DeviceInfo init=new DeviceInfo(getString(R.string.please_wait_while_initializing_scanapi_),null,(long)SktScanDeviceType.kSktScanDeviceTypeNone);
		_scanApiHelper.updateDevice(init);
		_scanApiHelper.setNoDeviceText(getString(R.string.scannersettings_nodevice));
		_scanApiHelper.setNotification(_scanApiHelperNotification);
		
		// create a ScanAPI ownership
		_scanApiOwnership=new SktScanApiOwnership(_scanApiOwnershipNotification,
				getString(R.string.app_name));
		
	}
	public static Context getBaseActivityContext(){
		return ScannerSettingsApplication.context;
	}

	/**
	 * in case of Low Memory just close ScanAPI, which
	 * will cause any connected scanner to disconnect
	 * and eventually close the UI
	 */
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Debug.MSG(Debug.kLevelTrace,"Application onLowMemory");
		_forceCloseUI=true;
		_unregisterOwnership=true;
		// close scanApi
		closeScanApi();
	}

	/**
	 * do nothing particular here since this function
	 * is not always called by the system
	 */
	@Override
	public void onTerminate() {
		super.onTerminate();
		Debug.MSG(Debug.kLevelTrace,"Application onTerminated");
	}

	/**
	 * retrieve the current instance of this application
	 * @return
	 */
	public static ScannerSettingsApplication getInstance(){
		return _singleton;
	}
	
	/**
	 * retrieve the reference to the devices list
	 * @param devicesList
	 */	
	public Vector<DeviceInfo> getDevicesList(){
		return _scanApiHelper.getDevicesList();
	}
	
	/**
	 * save the reference of the current selected device
	 * @param device
	 */
	public void setCurrentSelectedDevice(DeviceInfo device){
		_currentSelectedDevice=device;
	}
	
	/**
	 * retrieve the reference to the current selected device
	 * @param devicesList
	 */	
	public DeviceInfo getCurrentDevice(){
		return _currentSelectedDevice;
	}
	
	/**
	 * increase the view count.
	 * <br>this is called typically on each Activity.onCreate
	 * <br>If the view count was 0 then it asks this application object
	 * to register for ScanAPI ownership notification and to open ScanAPI  
	 */
	public void increaseViewCount(){
		if(_scanApiHelper.isScanApiOpen()==false){
			if(_viewCount==0){
				if(registerScanApiOwnership())
					_scanApiOwnership.askForOwnership();
			}
			else{
				Debug.MSG(Debug.kLevelWarning,"There is more View created without ScanAPI opened??");				
			}
		}
		++_viewCount;
		Debug.MSG(Debug.kLevelTrace,"Increase View count, New view count: "+_viewCount);
	}
	
	/**
	 * decrease the view count.
	 * <br> this is called typically on each Activity.onDestroy
	 * <br> If the view Count comes to 0 then it will try to close
	 * ScanAPI and unregister for ScanAPI ownership notification unless
	 * this decreaseViewCount is happening because of a screen rotation
	 */
	public void decreaseViewCount(){
		// if the view count is going to be 0
		// and ScanAPI is open and there hasn't
		// been a screen rotation then close ScanApi
		if(_viewCount==1){

			Debug.MSG(Debug.kLevelTrace,"Post a differed request to close ScanAPI");
			_handler.sendEmptyMessageDelayed(CLOSE_SCAN_API,500);
			/*
			if(_scanApiHelper.isScanApiOpen()==true){
	
				// it's probably OK to close ScanAPI now, but
				// just send a CLOSE_SCAN_API request delayed by .5s
				// to give the View a chance to be recreated 
				// if it was just a screen rotation
				Debug.MSG(Debug.kLevelTrace,"Post a differed request to close ScanAPI");
				_handler.sendEmptyMessageDelayed(CLOSE_SCAN_API,500);
			}
			else{
				// if ScanAPI is not initialized then unregister
				// ScanAPIOwnership
				if(_scanApiDoneInitEvent.waitFor(3000)==false){
					unregisterScanApiOwnership();
				}
				// otherwise, send a close ScanAPI request.
				else
					_handler.sendEmptyMessageDelayed(CLOSE_SCAN_API,500);
			}
			*/
		}
		--_viewCount;
		if(_viewCount<0){
			_viewCount=0;
			Debug.MSG(Debug.kLevelWarning,"try to decrease more view count that possible");
		}
		Debug.MSG(Debug.kLevelTrace,"Decrease View count, New view count: "+_viewCount);
	}
	
	/**
	 * register for ScanAPI ownership
	 */
	private boolean registerScanApiOwnership(){
		boolean success=false;
		int result=_scanApiOwnership.register(this);
		success=SktScanApiOwnership.Error.isSuccessful(result);
//		if(success){
//			if(result==SktScanApiOwnership.Error.kAlreadyDone)
//				success=false;
//		}
		return success;
	}
	
	/**
	 * unregister from ScanAPI ownership
	 */
	private void unregisterScanApiOwnership(){
		_scanApiOwnership.unregister();
		_unregisterOwnership=false;
	}
	
	/**
	 * open ScanAPI by first claiming its ownership
	 * then checking if the previous instance of ScanAPI has
	 * been correctly close. ScanAPI initialization is done in a
	 * separate thread, because it performs some internal testing
	 * that requires some time to complete and we want the UI to be
	 * responsive and present on the screen during that time.
	 */
	private void openScanApi(){
		// check this event to be sure the previous 
		// ScanAPI consumer has been shutdown
		Debug.MSG(Debug.kLevelTrace,"Wait for the previous terminate event to be set");
		
		if(_consumerTerminatedEvent.waitFor(3000)==true){
			Debug.MSG(Debug.kLevelTrace,"the previous terminate event has been set");
			_consumerTerminatedEvent.reset();
//			_scanApiDoneInitEvent.reset();// ScanAPI is not done with initialization yet
			_scanApiHelper.removeCommands(null);// remove all the commands
			_scanApiOwnership.claimOwnership();
			_scanApiHelper.open();
		}
		else{
			Debug.MSG(Debug.kLevelWarning,"the previous terminate event has NOT been set");
			displayError(getString(R.string.scannersettings_unable_to_start_scanapi_because_the_previous_close_hasn_t_been_completed));
		}
	}
	
	/**
	 * close ScanAPI by sending an abort. 
	 * This allows ScanAPI to shutdown 
	 * gracefully by asking to close any Scanner Object if 
	 * they were opened. When ScanAPI is terminated a kSktScanTerminate event
	 * is received and ScanApiHelper is calling the 
	 * notification onScanApiTerminated, and this is where the ScanApiOwnership can
	 * be released
	 */
	private void closeScanApi(){
		_scanApiHelper.close();
	}
	
	 /**
	 * check if there is at least one device connected
	 * @return true if there is at least one device connected false otherwise
	 */
	public boolean isDeviceConnected(){
		return _scanApiHelper.isDeviceConnected();
	}
    

	/**
	 * Notification helping to manage ScanAPI ownership.
	 * Only one application at a time can have access to ScanAPI.
	 * When another application is claiming ScanAPI ownership, this
	 * callback is called with release set to true asking this application
	 * to release scanAPI. When the other application is done with ScanAPI 
	 * it calls releaseOwnership, causing this callback to be called again
	 * but this time with release set to false. At that moment this application
	 * can reclaim the ScanAPI ownership.
	 */
	private Notification _scanApiOwnershipNotification=new Notification() {
		
		public void onScanApiOwnershipChange(Context context, boolean release) {
			if(release==true){
				closeScanApi();
			}
			else{
				if(_viewCount>0)
					openScanApi();
				else{
					SktDebug.DBGSKT_MSG(SktDebug.DBGSKT_LEVEL.DBGSKT_TRACE,"Ownership is granted but we don't have an Activity, so release it");
					_scanApiOwnership.claimOwnership();
					_scanApiOwnership.releaseOwnership();
				}
			}
		}

		public void onScanApiOwnershipFailed(Context context, String applicationNameGettingOwnership) {
			String text=context.getString(R.string.scannersettings_another_application_is_using_the_scanner_please_restart_this_application_);
			displayError(text);
		}
	};
	
	/**
	 *     checkErrorCode
	
	 Check if there is an error, and if that's the case then broadcast an error message to the activity
	 that want to display the message.
	 
	 The flag displayErrorIfNotSupported allows to ignore the  NOT SUPPORTED error. This might be use for
	 properties that can be safely retrieved from the scanner and ignored if they are not supported.
	 
	 There is at least one case where an error is diplayed when the property is not supported, and that when
	 the SoftScan is set to enable. The user needs to know if enabling the softscan feature works or not.
	 
	 */
	boolean checkErrorCode(int propertyStringId, final long nErrorCode, boolean displayErrorIfNotSupported,Context context)
	{
		boolean bResult = true;
		long result=nErrorCode;
		SktDebug.DBGSKT_MSG(SktDebug.DBGSKT_LEVEL.DBGSKT_TRACE,"Check if Error:"+String.valueOf(result));
		if(displayErrorIfNotSupported==false){
			if(result==SktScanErrors.ESKT_NOTSUPPORTED)
				result=SktScanErrors.ESKT_NOERROR;
		}
		
		if (!SktScanErrors.SKTSUCCESS(result))
		{
			int resId=R.string.scannersettings_failed_with_error_;
			if(result==SktScanErrors.ESKT_NOTSUPPORTED)
				resId=R.string.scannersettings_is_not_supported_error_;
			// the property Friendly Name failed with error -32
			String text=getString(R.string.scannersettings_the_property_)+" "+
				getText(propertyStringId)+" "+
				getString(resId)+" "+
				result;
			displayError(text);
    		bResult=false;
		}
		return bResult;
	}
	
	
	/**
	 * Request the ScanAPI Version. The response will be received via the
	 * GET_SCANAPI_VERSION_COMPLETE action
	 */
	public void getScanApiVersion()
	{
		_scanApiHelper.postGetScanAPIVersion(_onScanAPIVersion);
	}
	
	/**
	 * _onScanAPIVersion
	 * Called when the Scan API has returned a "get complete" for the ScanAPI version.
	 * The value is saved in a global string variable.
	 */
	ICommandContextCallback _onScanAPIVersion=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			String scanAPIVersion;
			if (checkErrorCode(R.string.scannersettings_scan_api_version_property, result,false,getApplicationContext()))
			{
				
				// format the string version
				ISktScanVersion version = scanObj.getProperty().getVersion();
				scanAPIVersion=Integer.toHexString(version.getMajor())+"."+
									Integer.toHexString(version.getMiddle())+"."+
									Integer.toHexString(version.getMinor())+" "+
									version.getBuild()+" "+
									display2Digit(version.getMonth())+
									display2Digit(version.getDay())+
									Integer.toHexString(version.getYear());
				
				
			}
			else
			{
				scanAPIVersion=getString(R.string.scannersettings_error_)+result+getString(R.string.scannersettings_retrieving_version);
			}
			
			Intent intentsent = new Intent(GET_SCANAPI_VERSION_COMPLETE);
			intentsent.putExtra(EXTRA_RESULT,result);
        	intentsent.putExtra(EXTRA_SCANAPIVERSION, scanAPIVersion.toString());
			sendBroadcast(intentsent);
			
		}
	};

	/**
	 * Request the SoftScan status. The response will be received via the
	 * GET_SOFTSCAN_STATUS_COMPLETE action
	 */
	public void getSoftScanStatus()
	{
		_scanApiHelper.postGetSoftScanStatus(_onSoftScanStatus);
	}
	
	/**
	 * _onSoftScanStatus
	 * Called when the Scan API has returned a "get complete" for the SoftScan status.
	 * The value is saved in a global string variable.
	 */
	ICommandContextCallback _onSoftScanStatus=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			int status;
			if (checkErrorCode(R.string.scannersettings_softscan_status_property, result,false,getApplicationContext()))
			{
				status = scanObj.getProperty().getByte();
			}
			else
			{
				status=ISktScanProperty.values.enableordisableSoftScan.kSktScanDisableSoftScan;
			}
			
			Intent intentsent = new Intent(GET_SOFTSCAN_STATUS_COMPLETE);
			intentsent.putExtra(EXTRA_RESULT,result);
        	intentsent.putExtra(EXTRA_SOFTSCANSTATUS, status);
			sendBroadcast(intentsent);
			
		}
	};

	/**
	 * request to enable or disable the SoftScan Status
	 */
	public void setSoftScanStatus(int status,boolean bErrorIfNotSupported){
		if(bErrorIfNotSupported==true)
			_scanApiHelper.postSetSoftScanStatus(status,_onSetSoftScanStatusErrorIfNotSupported);
		else
			_scanApiHelper.postSetSoftScanStatus(status,_onSetSoftScanStatus);
	}
	/**
	 * _onSetSoftScanStatus
	 * Check if the result is unsuccessful, and if that's the case display
	 * an error if the error is not NOT SUPPORTED
	 */
	ICommandContextCallback _onSetSoftScanStatus=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			if (checkErrorCode(R.string.scannersettings_softscan_status_property, result,false,getApplicationContext()))
			{
				Intent intentsent = new Intent(SET_SOFTSCAN_STATUS_COMPLETE);
				intentsent.putExtra(EXTRA_RESULT,result);
				sendBroadcast(intentsent);
			}
		}
	};
	
	/**
	 * _onSetSoftScanStatusErrorIfNotSupported
	 * Check if the result is unsuccessful, and if that's the case display
	 * an error even if the error is NOT SUPPORTED
	 */
	ICommandContextCallback _onSetSoftScanStatusErrorIfNotSupported=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			checkErrorCode(R.string.scannersettings_softscan_status_property, result,true,getApplicationContext());

			Intent intentsent = new Intent(SET_SOFTSCAN_STATUS_COMPLETE);
			intentsent.putExtra(EXTRA_RESULT,result);
			sendBroadcast(intentsent);
		}
	};
	
	/**
	 * Request the scanner version. The response will be received through the intent
	 * GET_SCANNER_VERSION_COMPLETE action
	 */
	public void getScannerVersion(){
		if(_currentSelectedDevice!=null)
			_scanApiHelper.postGetFirmware(_currentSelectedDevice, _onScannerVersion);
	}
	
	/**
	 *  _onScannerVersion
	 *  Called when the Scan API has returned a "get complete" for the Scanner Firmware revision.
	 *  The value is displayed in the General dialog.
	 *  To continue the chain of requesting all of the properties we want from the scanner,
	 *  the property for the scanner battery level is requested.
	 * 
	 */
	ICommandContextCallback _onScannerVersion=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			
			Intent intentsent = new Intent(GET_SCANNER_VERSION_COMPLETE);
			intentsent.putExtra(EXTRA_RESULT,result);
			
			CommandContext command=(CommandContext)scanObj.getProperty().getContext();
			if(command!=null)
			{
				DeviceInfo device=command.getDeviceInfo();
				if (checkErrorCode(R.string.scannersettings_scanner_version_property, result,false,getApplicationContext()))
				{
					StringBuffer szVersion=new StringBuffer();
					ISktScanVersion version = scanObj.getProperty().getVersion();
					szVersion.append(Integer.toHexString(version.getMajor()));
					szVersion.append('.');
					szVersion.append(Integer.toHexString(version.getMiddle()));
					szVersion.append('.');
					szVersion.append(Integer.toHexString(version.getMinor()));
					szVersion.append(' ');
					szVersion.append(version.getBuild());
					szVersion.append(' ');
					szVersion.append(display2Digit(version.getMonth()));
					szVersion.append(display2Digit(version.getDay()));
					szVersion.append(Integer.toHexString(version.getYear()));
					device.setVersion(szVersion.toString());
					Debug.MSG(Debug.kLevelTrace,"Device Version:"+device.getVersion());
		        	intentsent.putExtra(EXTRA_DEVICEVERSION, szVersion.toString());
					sendBroadcast(intentsent);
				}
				else
				{
					device.setVersion(getString(R.string.scannersettings_error_)+result+getString(R.string.scannersettings_retrieving_version));
				}
			}
		}
	};

	/**
	 * request the battery level the selected scanner. The response will be received
	 * through the intent action GET_BATTERYLEVEL_COMPLETE
	 */
	public void getBatteryLevel(){
		if(_currentSelectedDevice!=null)
			_scanApiHelper.postGetBattery(_currentSelectedDevice, _onBatteryLevel);
	}
	
	/**
	 * _onBatteryLevel
	 * Called when the Scan API has returned a "get complete" for the Battery Level.
	 * The value is used to update the controls in the Connected Scanner dialog.
	 * 
	 */
	ICommandContextCallback _onBatteryLevel=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			int nCurrent;
	
			if (checkErrorCode(R.string.scannersettings_battery_level_property, result,false,getApplicationContext()))
			{
				Intent intentsent = new Intent(GET_BATTERYLEVEL_COMPLETE);
				intentsent.putExtra(EXTRA_RESULT,result);

				String batteryLevel;
				if(result==SktScanErrors.ESKT_NOTSUPPORTED)
				{
					batteryLevel="Not Supported";
				}
				else
				{
					nCurrent = SktScan.helper.SKTBATTERY_GETCURLEVEL(scanObj.getProperty().getUlong());
					if(nCurrent>0)
						batteryLevel=Integer.toString(nCurrent)+"%";
					else
						batteryLevel="0%";
				}
	        	intentsent.putExtra(EXTRA_BATTERYLEVEL, batteryLevel);
				sendBroadcast(intentsent);
				CommandContext command=(CommandContext)scanObj.getProperty().getContext();
				if(command!=null)
				{
					DeviceInfo device=command.getDeviceInfo();
					device.setBatteryLevel(batteryLevel);
					Debug.MSG(Debug.kLevelTrace,"Device Battery Level:"+device.getBatteryLevel());
				}
			}
		}
	};

	/**
	 * request the decode action information of the current selected scanner. The
	 * response will be received through the intent action GET_DECODEACTION_COMPLETE
	 */
	public void getDecodeAction(){
		if(_currentSelectedDevice!=null)
			_scanApiHelper.postGetDecodeAction(_currentSelectedDevice,_onGetDecodeAction);
	}
	
	/**
	 * onGetDecodeAction
	 * Called when the Scan API has returned a "get complete" for the decode action.
	 * The values are decoded into booleans that can be used to set the GUI controls.
	 * Finally, to continue the chain of requesting all of the properties we want from the scanner,
	 * the property for device capabilities is requested.
	 */
	ICommandContextCallback _onGetDecodeAction=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			if (checkErrorCode(R.string.scannersettings_decode_action_property, result,false,getApplicationContext()))
			{
				Intent intentsent = new Intent(GET_DECODEACTION_COMPLETE);
				intentsent.putExtra(EXTRA_RESULT,result);
				char decodeAction=0;
				if (SktScanErrors.SKTSUCCESS(result))
				{
					decodeAction = scanObj.getProperty().getByte();
				}
				else
				{
					decodeAction |= ISktScanProperty.values.localDecodeAction.kSktScanLocalDecodeActionBeep;
				}

	        	intentsent.putExtra(EXTRA_DECODEACTION, decodeAction);
				sendBroadcast(intentsent);
				CommandContext command=(CommandContext)scanObj.getProperty().getContext();
				if(command!=null)
				{
					DeviceInfo device=command.getDeviceInfo();
					device.setDecodeVal(decodeAction);
				}
			}
		}
	};

	/**
	 * request to change the decode action of the current selected scanner. The result will
	 * be received through the intent action SET_DECODEACTION_COMPLETE
	 * @param decodeAction
	 */
	public void setDecodeAction(int decodeAction){
		if(_currentSelectedDevice!=null)
			_scanApiHelper.postSetDecodeAction(_currentSelectedDevice, decodeAction, _onSetDecodeAction);
	}
	
	/**
	 * onSetDecodeAction
	 * Called when the Scan API has returned a "get complete" for the decode action.
	 * The values are decoded into booleans that can be used to set the GUI controls.
	 * Finally, to continue the chain of requesting all of the properties we want from the scanner,
	 * the property for device capabilities is requested.
	 */
	ICommandContextCallback _onSetDecodeAction=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			if (checkErrorCode(R.string.scannersettings_decode_action_property, result,false,getApplicationContext()))
			{
				Intent intentsent = new Intent(SET_DECODEACTION_COMPLETE);
				intentsent.putExtra(EXTRA_RESULT,result);
				sendBroadcast(intentsent);
			}
		}
	};

	/**
	 * request the current selected scanner rumble capability. The response will be received
	 * in the intent action GET_RUMBLECAPABILITY_COMPLETE
	 */
	public void getRumbleCapability(){
		if(_currentSelectedDevice!=null)
			_scanApiHelper.postGetCapabilitiesDevice(_currentSelectedDevice, _onCapabilitiesDevice);
	}
	/**
	 *  onCapabilitiesDevice
	 *  Called when the Scan API has returned a "get complete" for the device capabilities.
	 *  Used to determine if the scanner supports rumble mode.
	 *  Finally, to continue the chain of requesting all of the properties we want from the scanner,
	 *  the property for device postamble is requested.
	 * 
	 */
	ICommandContextCallback _onCapabilitiesDevice=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			if (checkErrorCode(R.string.scannersettings_device_capabilities_property, result,false,getApplicationContext()))
			{
				Intent intentsent = new Intent(GET_RUMBLECAPABILITY_COMPLETE);
				intentsent.putExtra(EXTRA_RESULT,result);
				boolean bRumble=false;
				if (SktScanErrors.SKTSUCCESS(result))
				{
					bRumble = ((scanObj.getProperty().getUlong()&ISktScanProperty.values.capabilityLocalFunctions.kSktScanCapabilityLocalFunctionRumble) == ISktScanProperty.values.capabilityLocalFunctions.kSktScanCapabilityLocalFunctionRumble);

				}
	        	intentsent.putExtra(EXTRA_RUMBLE, bRumble);
				sendBroadcast(intentsent);
				CommandContext command=(CommandContext)scanObj.getProperty().getContext();
				if(command!=null)
				{
					DeviceInfo device=command.getDeviceInfo();
					device.setRumble(bRumble);
				}
			}
		}
	};

	/**
	 * request the current selected scanner suffix. The response will be received in
	 * the intent action GET_SUFFIX_COMPLETE
	 */
	public void getSuffix(){
		if(_currentSelectedDevice!=null)
			_scanApiHelper.postGetPostambleDevice(_currentSelectedDevice, _onGetPostambleDevice);
	}
	
	/**
	 * onGetPostambleDevice
	 * Called when the Scan API has returned a "get complete" for the postamble setting.
	 * The GUI supports setting the postamble to NEWLINE, TAB, or none.
	 */
	ICommandContextCallback _onGetPostambleDevice=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			if (checkErrorCode(R.string.scannersettings_scanner_postamble_property, result,false,getApplicationContext()))
			{
				Intent intentsent = new Intent(GET_SUFFIX_COMPLETE);
				intentsent.putExtra(EXTRA_RESULT,result);
				if (SktScanErrors.SKTSUCCESS(result))
				{
					String suffix;
					if (scanObj.getProperty().getString().getLength() > 0)
						suffix=scanObj.getProperty().getString().getValue();
					else
						suffix="";
	
		        	intentsent.putExtra(EXTRA_SUFFIX, suffix);
					sendBroadcast(intentsent);
					CommandContext command=(CommandContext)scanObj.getProperty().getContext();
					if(command!=null)
					{
						DeviceInfo device=command.getDeviceInfo();
						device.setSuffix(suffix);
					}
				}
			}
		}
	};

	/**
	 * change the current selected scanner suffix. The result will be received through
	 * the intent action SET_SUFFIX_COMPLETE
	 * @param suffix
	 */
	public void setSuffix(String suffix){
		if(_currentSelectedDevice!=null)
			_scanApiHelper.postSetPostamble(_currentSelectedDevice, suffix, _onSetPostambleDevice);
	}
	
	/**
	 * onSetPostambleDevice
	 * Called when the Scan API has returned a "get complete" for the postamble setting.
	 * The GUI supports setting the postamble to NEWLINE, TAB, or none.
	 */
	ICommandContextCallback _onSetPostambleDevice=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			if (checkErrorCode(R.string.scannersettings_scanner_postamble_property, result,false,getApplicationContext()))
			{
				Intent intentsent = new Intent(SET_SUFFIX_COMPLETE);
				intentsent.putExtra(EXTRA_RESULT,result);
				sendBroadcast(intentsent);
			}
		}
	};

	/**
	 * get all the symbology status of the current selected scanner. Each symbology status will be
	 * received through the intent action GET_SYMBOLOGY_COMPLETE
	 */
	public void getAllSymbology(){
		if(_currentSelectedDevice!=null)
			_scanApiHelper.postGetAllSymbologyInfo(_currentSelectedDevice, _onGetSymbologyInfoComplete);
	}
	/**
	 * retrieve from the current selected scanner the symbology status of the symbology id passed in input.
	 * The result will be received through the intent action GET_SYMBOLOGY_COMPLETE
	 * @param symbologyId
	 */
	public void getSymbology(int symbologyId){
		if(_currentSelectedDevice!=null)
			_scanApiHelper.postGetSymbologyInfo(_currentSelectedDevice, symbologyId, _onGetSymbologyInfoComplete);
	}
	
	/**
	 * _onGetSymbologyInfoComplete
	 * Called when the Scan API has returned a "get complete" for a Symbology property.
	 */ 
	ICommandContextCallback _onGetSymbologyInfoComplete=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			int symbologyId;
			
			if (checkErrorCode(R.string.scannersettings_symbology_status_property, result,false,getApplicationContext()))
			{
				Intent intentsent = new Intent(GET_SYMBOLOGY_COMPLETE);
				intentsent.putExtra(EXTRA_RESULT,result);
				
				if (SktScanErrors.SKTSUCCESS(result))
				{
					symbologyId=scanObj.getProperty().getSymbology().getID();
					int status=scanObj.getProperty().getSymbology().getStatus();
					intentsent.putExtra(EXTRA_SYMBOLOGY_ID, symbologyId);
		        	intentsent.putExtra(EXTRA_SYMBOLOGY_STATE, status);
		        	intentsent.putExtra(EXTRA_SYMBOLOGY_NAME, scanObj.getProperty().getSymbology().getName());
					if(status!=ISktScanSymbology.status.kSktScanSymbologyStatusNotSupported)
					{
			        	// keep the current Symbology state in 
			        	CommandContext command=(CommandContext)scanObj.getProperty().getContext();
						if(command!=null)
						{
							DeviceInfo device=command.getDeviceInfo();
							try{
								device.setSymbologyIndex(symbologyId);
								device.setSymbologyStatus(symbologyId,scanObj.getProperty().getSymbology().getStatus());
								device.setSymbologyName(symbologyId,scanObj.getProperty().getSymbology().getName());
							}catch(Exception e)
							{
								Debug.MSG(Debug.kLevelError,"Error during get Symbology Complete: "+e.getMessage());
								Debug.MSG(Debug.kLevelError,"nIndex="+symbologyId);
							}
						}
					}
				}
				sendBroadcast(intentsent);
			}
		}
	};

	/**
	 * enable or disable the symbology identified of the current selected device. The
	 * response will be received through the intent action SET_SYMBOLOGY_COMPLETE
	 * @param symbologyId
	 * @param enable
	 */
	public void setSymbology(int symbologyId,boolean enable){
		if(_currentSelectedDevice!=null)
			_scanApiHelper.postSetSymbologyInfo(_currentSelectedDevice, symbologyId, enable, _onSetSymbologyInfoComplete);
	}
	
	/**
	 * _onGetSymbologyInfoComplete
	 * Called when the Scan API has returned a "get complete" for a Symbology property.
	 */ 
	ICommandContextCallback _onSetSymbologyInfoComplete=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			int symbologyId=0;
			
			CommandContext command=(CommandContext)scanObj.getProperty().getContext();
			if(command!=null)
			{
				symbologyId=command.getSymbologyId();
			}			
			Intent intentsent = new Intent(SET_SYMBOLOGY_COMPLETE);
			intentsent.putExtra(EXTRA_RESULT, result);
			intentsent.putExtra(EXTRA_SYMBOLOGY_ID, symbologyId);
			sendBroadcast(intentsent);
		}
	};

	/**
	 * request to retrieve the friendly name of the current selected scanner. The response
	 * will be received in the intent action GET_FRIENDLYNAME_COMPLETE
	 */
	public void getFriendlyName(){
		if(_currentSelectedDevice!=null)
			_scanApiHelper.postGetFriendlyName(_currentSelectedDevice, _onGetFriendlyName);
	}
	
	/**
	 *  onGetFriendlyName
	 *  Called when the Scan API has returned a "get complete" for the friendly name.
	 */
	ICommandContextCallback _onGetFriendlyName=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			if (checkErrorCode(R.string.scannersettings_friendly_name_property, result,false,getApplicationContext()))
			{
				if (SktScanErrors.SKTSUCCESS(result))
				{
					String friendlyName;
					if (scanObj.getProperty().getString().getLength() > 0)
						friendlyName=scanObj.getProperty().getString().getValue();
					else
						friendlyName="";
	
					CommandContext command=(CommandContext)scanObj.getProperty().getContext();
					AppUtils.displayLog("----------------Command------------",":"+command);
					if(command!=null)
					{
						AppUtils.displayLog("----------------Device Name------------",":"+command.getName());
						DeviceInfo device=command.getDeviceInfo();
						device.setName(friendlyName);
						Intent intent=new Intent(GET_FRIENDLYNAME_COMPLETE);
						intent.putExtra(EXTRA_RESULT, result);
						intent.putExtra(EXTRA_DEVICENAME,friendlyName);
						sendBroadcast(intent);
					}
				}
			}
		}
	};
	
	/**
	 * request to change the friendly name of the current selected scanner. The response
	 * will be received in the intent action SET_FRIENDLYNAME_COMPLETE
	 * @param newName
	 */
	public void setFriendlyName(String newName){
		if(_currentSelectedDevice!=null){
			_currentSelectedDevice.setName(newName);
			_scanApiHelper.postSetFriendlyName(newName, _currentSelectedDevice, _onSetFriendlyName);
		}
	}
	
	/**
	 * onSetFriendlyName
	 * Response from setting a new friendly name to a device
	 */
	ICommandContextCallback _onSetFriendlyName=new ICommandContextCallback() {
		
		public void run(long result,ISktScanObject scanObj) {
			Intent intentsent = new Intent(SET_FRIENDLYNAME_COMPLETE);
			intentsent.putExtra(EXTRA_RESULT, result);
			sendBroadcast(intentsent);
			
			_scanApiHelper.updateDevice(_currentSelectedDevice);
			Intent intent=new Intent(NOTIFY_SCANNER_ARRIVAL);
			intent.putExtra(EXTRA_DEVICENAME,_currentSelectedDevice.getName());
			sendBroadcast(intent);
		}
	};

	/**
	 * request to change the decode action of the current selected scanner. The result will
	 * be received through the intent action SET_DECODEACTION_COMPLETE
	 * @param decodeAction
	 */
	public void startSoftScanner(){
		if(_currentSelectedDevice!=null)
			_scanApiHelper.postSetTriggerDevice(_currentSelectedDevice, ISktScanProperty.values.trigger.kSktScanTriggerStart,null);
	}
	
	/**
	 * pass to the SoftScan device the Overlay View where the camera will be displayed
	 * @param device
	 * @param frame
	 */
	public void setSoftScanOverlay(DeviceInfo device,Map<String,Object> context){
		_scanApiHelper.postSetOverlayView(device, context, null);
	}
	
	protected String display2Digit(int value) {
		String result=Integer.toHexString(value);
		if(result.length()<2)
			result="0"+result;
		return result;
	}
	
	protected void displayError(String text){
		Intent intent=new Intent(NOTIFY_ERROR_MESSAGE);
		intent.putExtra(EXTRA_ERROR_MESSAGE,text);
		sendBroadcast(intent);
	}

	private ScanApiHelperNotification _scanApiHelperNotification=new ScanApiHelperNotification() {
		/**
		 * receive a notification indicating ScanAPI has terminated,
		 * then send an intent to finish the activity if it is still
		 * running
		 */
		public void onScanApiTerminated() {
			_abortScanApi=false;
			_consumerTerminatedEvent.set();
			_scanApiOwnership.releaseOwnership();
			if(_unregisterOwnership==true)
				unregisterScanApiOwnership();
			if(_forceCloseUI){
				Intent intent=new Intent(NOTIFY_CLOSE_ACTIVITY);
				sendBroadcast(intent);
			}
		}
		/**
		 * ScanAPI is now initialized, if there is an error
		 * then ask the activity to display it
		 */
		public void onScanApiInitializeComplete(long result) {
			// if ScanAPI couldn't be initialized
			// then display an error
			if(!SktScanErrors.SKTSUCCESS(result)){
				_consumerTerminatedEvent.set();
	    		_scanApiOwnership.releaseOwnership();
				String text=getString(R.string.scannersettings_failed_to_initialize_scanapi_error_)+result;
				displayError(text);
			}
			else
			{
//	        	_scanApiDoneInitEvent.set();// now ScanAPI is done with initialization
	        	if(_abortScanApi==true){
	        		_abortScanApi=false;
	        		_scanApiHelper.close();
	        	}
	        	else{
					Intent intent=new Intent(NOTIFY_SCANAPI_INIT_COMPLETE);
		        	sendBroadcast(intent);
	        	}
			}
		}
		/**
		 * ask the activity to display any asynchronous error
		 * received from ScanAPI
		 */
		public void onError(long result) {
			Debug.MSG(Debug.kLevelError,"receive an error:"+result);
			String text=getString(R.string.scannersettings_scanapi_is_reporting_an_error_)+result;
			if(result==SktScanErrors.ESKT_UNABLEINITIALIZE)
				text=getString(R.string.unable_to_initialize_the_scanner_please_power_cycle_the_scanner_);
			displayError(text);
		}
		
		/**
		 * a device has disconnected. Update the UI accordingly
		 */
		public void onDeviceRemoval(DeviceInfo deviceRemoved) {
			if(deviceRemoved==_currentSelectedDevice)
				_currentSelectedDevice=null;
        	Intent intent=new Intent(NOTIFY_SCANNER_REMOVAL);
        	intent.putExtra(EXTRA_DEVICENAME,deviceRemoved.getName());
        	sendBroadcast(intent);
		}
		
		/**
		 * a device is connecting, update the UI accordingly
		 */
		public void onDeviceArrival(long result, DeviceInfo newDevice) {
			if(SktScanErrors.SKTSUCCESS(result)){
				Intent intent=null;
				// if no device is selected then
				// the newly arrived device is selected by default
				if(_currentSelectedDevice==null)
					_currentSelectedDevice=newDevice;
				intent=new Intent(NOTIFY_SCANNER_ARRIVAL);
				intent.putExtra(EXTRA_DEVICENAME,newDevice.getName());
				sendBroadcast(intent);
			}
			else
			{
				String text=getString(R.string.scannersettings_error_)+result+
					getString(R.string.scannersettings_during_device_arrival_notification);
				displayError(text);
			}
		}
		/**
		 * ScanAPI is delivering some decoded data
		 * as the activity to display them
		 */
		public void onDecodedData(DeviceInfo deviceInfo,
				ISktScanDecodedData decodedData) {
			// send the data only coming from the scanner that
			// has been selected
			String s = new String(decodedData.getData());
			AppUtils.displayLog("--------------Device Name------------",":"+deviceInfo.getName()+s.toString());
			if(deviceInfo==_currentSelectedDevice){
				Intent intent=new Intent(NOTIFY_DATA_ARRIVAL);
				intent.putExtra(EXTRA_SYMBOLOGY_NAME,decodedData.getSymbologyName());
				intent.putExtra(EXTRA_DECODEDDATA,decodedData.getData());
				sendBroadcast(intent);
			}
		}
		/**
		 * an error occurs during the retrieval of ScanObject
		 * from ScanAPI, this is critical error and only a restart
		 * can fix this.
		 */
		public void onErrorRetrievingScanObject(long result) {
			String text="Error unable to retrieve ScanAPI message: ";
			text+="("+result+")";
			text+="Please close this application and restart it";
			displayError(text);
		}
	};

}
