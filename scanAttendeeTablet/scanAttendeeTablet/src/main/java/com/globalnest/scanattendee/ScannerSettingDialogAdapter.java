//  ScanAttendee Android
//  Created by Ajay on Mar 15, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.scanattendee;

import com.globalnest.classes.QRCodeEncoder;
import com.globalnest.data.Contents;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author mayank
 *
 */

public class ScannerSettingDialogAdapter extends PagerAdapter {

	private final String[] TITLES = {  "RESET","SPP MODE","PAIR" };
	private Context mContext;
	private String mParingCode = "", mResetCode = "";

	/**
	 *
	 */
	public ScannerSettingDialogAdapter(Context context, String paringCode,
									   String resetCode) {
		mContext = context;
		mParingCode = paringCode;
		mResetCode = resetCode;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return TITLES[position];
	}

	@Override
	public int getCount() {
		return TITLES.length;
	}

	@Override
	public Object instantiateItem(View collection, final int position) {
		View view = null;
		AppUtils.displayLog("@@@@@@@@@@@@  Barcode Scan Activity  @@@@@@@@@@@@",
				"@@@@@@@@@  Mac Address @@@@@@@=" + mParingCode);

		if (position == 2) {
			view = LayoutInflater.from(mContext).inflate(R.layout.scanner_paring_page, null);
			// ImageView btn_close = (ImageView)
			// view.findViewById(R.id.softscanclose);
			final ImageView img_1dcode,img_barcode,img_delete;
			final Button status;
			final LinearLayout layout_addBTAddress,layout_show_scancode,lay_imgeditBTAddress;
			TextView btn_save;
			final EditText edt_BTaddress;
			img_1dcode = (ImageView) view.findViewById(R.id.socket1dcodeimg);
			img_barcode = (ImageView) view.findViewById(R.id.socketbarcodeimg);
			img_delete  =(ImageView) view.findViewById(R.id.img_delete);
			status=(Button) view.findViewById(R.id.statusscreen);
			layout_addBTAddress=(LinearLayout) view.findViewById(R.id.layout_add_bluetoothaddress);
			layout_show_scancode=(LinearLayout) view.findViewById(R.id.layout_show_scancode);
			lay_imgeditBTAddress=(LinearLayout) view.findViewById(R.id.lay_imgeditBTAddress);
			//lay_editview=(LinearLayout) view.findViewById(R.id.lay_editview);
			btn_save=(TextView) view.findViewById(R.id.btn_save);
			edt_BTaddress=(EditText)view.findViewById(R.id.edt_BTaddress);
			img_barcode.setVisibility(View.VISIBLE);
			BaseActivity.img_pairBarcode=img_1dcode;
			BaseActivity.img_pairQrcode=img_barcode;
			Bitmap barcode;
			status.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent statusIntent = new Intent();
					statusIntent.setAction("android.intent.action.MAIN");//android.settings.WIFI_IP_SETTINGS
					statusIntent.setClassName("com.android.settings","com.android.settings.Settings$StatusActivity");
					mContext.startActivity(statusIntent);
				}
			});
			if(!Util.devicebluetoothaddress.getString("BluetoothAddress","").isEmpty()){
				edt_BTaddress.setText(Util.devicebluetoothaddress.getString("BluetoothAddress","").replace("#FNI","").replace("#",""));
				edt_BTaddress.setEnabled(false);
			}else if(!Util.NullChecker(mParingCode).isEmpty()){
				edt_BTaddress.setText(Util.NullChecker(mParingCode).replace("#FNI","").replace("#",""));
				edt_BTaddress.setEnabled(false);
			}
			lay_imgeditBTAddress.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					edt_BTaddress.setEnabled(true);
					layout_addBTAddress.setVisibility(View.VISIBLE);
				}
			});
			img_delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Util.devicebluetoothaddress.edit().clear().commit();
					edt_BTaddress.setText("");
					edt_BTaddress.setEnabled(true);
					layout_show_scancode.setVisibility(View.GONE);
					layout_addBTAddress.setVisibility(View.VISIBLE);
				}
			});
			btn_save.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(edt_BTaddress.getText().toString().isEmpty()){
						Toast.makeText(mContext, "Please enter Bluetooth Address ", Toast.LENGTH_LONG).show();

					}else if(edt_BTaddress.getText().toString().length()<12){
						Toast.makeText(mContext, "Please enter Valid Bluetooth Address ", Toast.LENGTH_LONG).show();
					}else {
						SharedPreferences.Editor devicebluetoothaddress=Util.devicebluetoothaddress.edit();
						String BTAddress=Util.NullChecker(edt_BTaddress.getText().toString()).replaceAll(":", "").trim();
						devicebluetoothaddress.putString("BluetoothAddress",BTAddress);
						devicebluetoothaddress.commit();
						layout_show_scancode.setVisibility(View.VISIBLE);
						mParingCode=edt_BTaddress.getText().toString();
						String OneD_pairing_code = "#FNI" + mParingCode + "#";
						String TwoD_pairing_code = "#FNC SPP Initiator " + mParingCode + "#";
						CreateBarcode(OneD_pairing_code, img_1dcode);
						CreateQrcode(TwoD_pairing_code, img_barcode);
						edt_BTaddress.setEnabled(false);
						layout_addBTAddress.setVisibility(View.GONE);
						BaseActivity.showCustomToast(mContext,"Saved and created Successfully", R.drawable.img_like, R.drawable.toast_greenroundededge, false);
						BaseActivity.hideSoftKeyboard((Activity) mContext);
					}
				}
			});

			try {
				if(Util.NullChecker(mParingCode).isEmpty()&&
						Util.devicebluetoothaddress.getString("BluetoothAddress","").isEmpty()){
					layout_addBTAddress.setVisibility(View.VISIBLE);
					layout_show_scancode.setVisibility(View.GONE);
				}else {
					layout_addBTAddress.setVisibility(View.GONE);
					if(!Util.devicebluetoothaddress.getString("BluetoothAddress","").isEmpty())
					{
						mParingCode=Util.devicebluetoothaddress.getString("BluetoothAddress","");}
					layout_show_scancode.setVisibility(View.VISIBLE);
					//mParingCode=Util.devicebluetoothaddress.getString("BluetoothAddress","");
					String OneD_pairing_code = "#FNI" + mParingCode + "#";
					String TwoD_pairing_code = "#FNC SPP Initiator " + mParingCode + "#";
					CreateBarcode(OneD_pairing_code, img_1dcode);
					CreateQrcode(TwoD_pairing_code, img_barcode);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (position == 0) {
			view = LayoutInflater.from(mContext).inflate(
					R.layout.scanner_reset_page, null);
			ImageView img_1dcode = (ImageView) view
					.findViewById(R.id.socket1dcodeimg);
			ImageView img_barcode = (ImageView) view
					.findViewById(R.id.socketbarcodeimg);

			img_barcode.setVisibility(View.VISIBLE);
			BaseActivity.img_resetBarcode=img_1dcode;
			BaseActivity.img_resetQrcode=img_barcode;

			Bitmap barcode;
			try {

				CreateBarcode("#FNB00F0#", img_1dcode);
				CreateQrcode("#FNB 41FB970001#", img_barcode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(position == 1){
			view = LayoutInflater.from(mContext).inflate(
					R.layout.scanner_spp_mode, null);
			ImageView img_1dcode = (ImageView) view
					.findViewById(R.id.socket1dcodeimg);
			ImageView img_barcode = (ImageView) view
					.findViewById(R.id.socketbarcodeimg);

			img_barcode.setVisibility(View.VISIBLE);
			BaseActivity.img_sppBarcode=img_1dcode;
			BaseActivity.img_sppQrcode=img_barcode;
			Bitmap barcode;
			try {
				CreateBarcode("#FNB00F40000#", img_1dcode);
				CreateQrcode("#FNC SPP Acceptor 000000000000#", img_barcode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		((ViewPager) collection).addView(view, 0);
		return view;
	}

	@Override
	public void destroyItem(View collection, int position, Object view) {
		((ViewPager) collection).removeView((View) view);

	}

	@Override
	public boolean isViewFromObject(View view, Object object) {

		return view == ((View) object);
	}

	public void CreateQrcode(String btAddress, ImageView img_barcode) {

		try {
			WindowManager manager = (WindowManager) ((Activity) mContext)
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = manager.getDefaultDisplay();

			@SuppressWarnings("deprecation")
			int width = display.getWidth();// point.x;
			@SuppressWarnings("deprecation")
			int height = display.getHeight();
			int smallerDimension = width < height ? width : height;
			smallerDimension = smallerDimension * 3 / 4;

			QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(btAddress, null,
					Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(),
					smallerDimension);

			DisplayMetrics metrics = new DisplayMetrics();
			((Activity) mContext).getWindowManager().getDefaultDisplay()
					.getMetrics(metrics);

			try {
				Bitmap b = qrCodeEncoder.encodeAsBitmap();
				img_barcode.setImageBitmap(b);
			} catch (WriterException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void CreateBarcode(String btAddress, ImageView img_barcode) {

		try {
			WindowManager manager = (WindowManager) ((Activity) mContext)
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = manager.getDefaultDisplay();

			@SuppressWarnings("deprecation")
			int width = display.getWidth();// point.x;
			@SuppressWarnings("deprecation")
			int height = display.getHeight();
			int smallerDimension = width > height ? width : height;
			smallerDimension = smallerDimension * 2 / 4;
			btAddress=Util.NullChecker(btAddress).replaceAll(":", "").trim();
			QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(btAddress, null,
					Contents.Type.TEXT, BarcodeFormat.CODE_128.toString(),
					smallerDimension);

			DisplayMetrics metrics = new DisplayMetrics();
			((Activity) mContext).getWindowManager().getDefaultDisplay()
					.getMetrics(metrics);

			try {
				Bitmap b = qrCodeEncoder.encodeAsBitmap();
				img_barcode.setImageBitmap(b);
			} catch (WriterException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
