package com.globalnest.scanattendee;

import com.globalnest.classes.QRCodeEncoder;
import com.globalnest.data.Contents;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;


public class ScannerPairActivity extends Activity {

	//RandomString random = new RandomString(12);
	ImageView image_qrcode;
    String name="",address="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pairing_layout);
		image_qrcode = (ImageView) findViewById(R.id.scan_qrcode);
		getBluetoothMacAddress();
      // address = address.replace(":", "");
		//Log.i("---------------Mac Address------------", ":" + address);
		/*Intent i=new Intent(ScannerSettingsApplication.START_EZ_PAIR);
		// remove the bluetooth address and keep only the device friendly name
		if(name !=null){
			if(name.length()>18)
				name=name.substring(0,name.length()-18);
			
		}
		i.putExtra(ScannerSettingsApplication.EXTRA_EZ_PAIR_DEVICE,name);
		i.putExtra(ScannerSettingsApplication.EXTRA_EZ_PAIR_HOST_ADDRESS,address);
		sendBroadcast(i);*/
		
		address = "#FNC SPP Initiator "+address+"#";
		CreateQrcode(address);
		/*IntentFilter filter = new IntentFilter(ScannerSettingsApplication.NOTIFY_DECODED_DATA);   
	    registerReceiver(this._newItemsReceiver, filter); */
	}

	@SuppressWarnings("deprecation")
	public void CreateQrcode(String data) {
		WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		int width = display.getWidth();// point.x;
		int height = display.getHeight();
		int smallerDimension = width < height ? width : height;
		smallerDimension = smallerDimension * 3 / 4;
		QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(data, null,
				Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(),
				smallerDimension);
		try {
			Bitmap qrcode = qrCodeEncoder.encodeAsBitmap();
			image_qrcode.setImageBitmap(qrcode);

		} catch (WriterException e) {
			e.printStackTrace();
		}
	}

	public void getBluetoothMacAddress() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		// if device does not support Bluetooth
		if (mBluetoothAdapter != null) {
			//Log.d("", "device does not support bluetooth");
			 name = mBluetoothAdapter.getName();
		     address = mBluetoothAdapter.getAddress().replace(":","").trim();
		}
       
		
	}

	/*private final BroadcastReceiver _newItemsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			//Log.i("--------------Intent Action-----------",":"+intent.getAction());
			// ScanAPI is initialized
			if(intent.getAction().equalsIgnoreCase(ScannerSettingsApplication.NOTIFY_DECODED_DATA))
	        {
				char[] data=intent.getCharArrayExtra(ScannerSettingsApplication.EXTRA_DECODEDDATA);
				//Log.i("--------------Blue Tooth Address-----------",":"+new String(data));
				
				
	        }

		}
	};*/
}
