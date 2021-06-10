package com.globalnest.scanattendee.socketmobile;

import com.socketmobile.scanapi.ISktScanDevice;
import com.socketmobile.scanapi.ISktScanObject;
import com.socketmobile.scanapi.SktScanErrors;

/**
 * ICommandContextCallback defines the interface for
 * a Command complete callback. The ScanObj passed as input
 * of the callback corresponds to the ScanObj received by
 * the application when the command has completed
 */
interface ICommandContextCallback{
	void run(long result,ISktScanObject scanObj);
}

/**
 * CommandContext is a class that allows the application
 * to stack up the commands that need to be sent to the device
 * and when a command sent is completed it calls the callback.
 * 
 * Only one command can be sent at a time. Before sending the
 * next command the previous one must be first completed.
 */
class CommandContext {
	public static final int statusReady=1;
	public static final int statusNotCompleted=2;
	public static final int statusCompleted=3;
	
	private ICommandContextCallback _callback=null;
	private boolean _getOperation=false;
	private ISktScanObject _scanObj;
	private int _status;
	private ISktScanDevice _scanDevice;
	private int _retries;
	private DeviceInfo _deviceInfo;
	private int _symbologyId;
	private String _name;
	
	public CommandContext(String name,boolean getOperation,ISktScanObject scanObj,ISktScanDevice scanDevice,DeviceInfo deviceInfo,ICommandContextCallback callback){
		this._getOperation=getOperation;
		scanObj.getProperty().setContext(this);
		this._scanObj=scanObj;
		this._callback=callback;
		this._status=statusReady;
		this._scanDevice=scanDevice;
		this._retries=0;
		this._deviceInfo=deviceInfo;
		this._symbologyId=0;
		this._name=name;
	}
	
	public boolean getOperation(){
		return _getOperation;
	}
	
	public ISktScanObject getScanObject(){
		return _scanObj;
	}
	
	public int getStatus(){
		return _status;
	}
	public int getRetries(){
		return _retries;
	}
	public void setStatus(int status){
		this._status=status;
	}
	
	public ISktScanDevice getScanDevice(){
		return _scanDevice;
	}
	public DeviceInfo getDeviceInfo(){
		return _deviceInfo;
	}
	public void doCallback(long result,ISktScanObject scanObj){
		_status=statusCompleted;
		if(_callback!=null)
			_callback.run(result,scanObj);
	}
	public void setSymbologyId(int symbology) {
		_symbologyId=symbology;
	}
	public int getSymbologyId() {
		return _symbologyId;
	}

	public String getName(){
		return _name;
	}
	public long DoGetOrSetProperty() {
		long result=SktScanErrors.ESKT_NOERROR;
		if(getScanDevice()==null)
			result=SktScanErrors.ESKT_INVALIDPARAMETER;
		
		if(SktScanErrors.SKTSUCCESS(result)){
			if(getOperation()==true){
//				System.out.println("About to do a get for ID:0x"+Integer.toHexString(getScanObject().getProperty().getID())+"\n");
				System.out.println("About to do a get for "+_name+"\n");
				result=getScanDevice().GetProperty(getScanObject());
			}
			else{
//				System.out.println("About to do a set for ID:0x"+Integer.toHexString(getScanObject().getProperty().getID())+"\n");
				System.out.println("About to do a set for "+_name+"\n");
				result=getScanDevice().SetProperty(getScanObject());
			}
		}
		_retries++;
		if(SktScanErrors.SKTSUCCESS(result)){
			_status=statusNotCompleted;
		}
		return result;
	}

}
