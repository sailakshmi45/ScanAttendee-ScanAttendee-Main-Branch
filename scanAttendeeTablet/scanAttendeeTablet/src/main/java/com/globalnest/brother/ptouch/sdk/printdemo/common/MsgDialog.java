/**
 * Message Dialog
 *
 * @author Brother Industries, Ltd.
 * @version 2.2
 */

package com.globalnest.brother.ptouch.sdk.printdemo.common;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.globalnest.mvc.ExternalSettings;
import com.globalnest.scanattendee.AttendeeDetailActivity;
import com.globalnest.scanattendee.BaseActivity;
import com.globalnest.scanattendee.GlobalScanActivity;
import com.globalnest.scanattendee.ManageTicketActivity;
import com.globalnest.scanattendee.OrderSucessPrintActivity;
import com.globalnest.scanattendee.PrintAttendeesListActivity;
import com.globalnest.scanattendee.R;
import com.globalnest.scanattendee.SelfCheckinAttendeeDetailActivity;
import com.globalnest.scanattendee.SelfCheckinAttendeeList;
import com.globalnest.scanattendee.SelfcheckinTicketslistActivity;
import com.globalnest.scanattendee.SellOrderActivity;
import com.globalnest.scanattendee.TransperantGlobalScanActivity;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;

import java.io.File;

public class MsgDialog {

	private Dialog progress_dialog;
	private final Context mContext;
	private MsgHandle mHandle;
	Typeface textface;
	TextView txt_loading;
	Activity activity;
	boolean isError = false;
	public static boolean isShowDialog = false;
	public static String intent_value;
	public MsgDialog(Context context, Activity getActivity) {

		mContext = context;
		activity = getActivity;

	}

	/** set handle */
	public void setHandle(MsgHandle handle) {

		mHandle = handle;
	}

	/** show message */
	public void showStartMsgDialog(final String message) {
		isError = false;
		if (progress_dialog != null && progress_dialog.isShowing()) {
			progress_dialog.dismiss();
		}
		textface = Typeface.createFromAsset(mContext.getAssets(),
				"VarelaRound-Regular.ttf");
		progress_dialog = new Dialog(mContext);
		progress_dialog.setCanceledOnTouchOutside(false);
		progress_dialog.setCancelable(false);
		progress_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progress_dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		progress_dialog.setContentView(R.layout.loading_layout);

		// progress_dialog.setCancelable(false);
		txt_loading = (TextView) progress_dialog.findViewById(R.id.txtloading);
		// txt_loading.setTextColor(mContext.getResources().getColor(R.color.green_button_header));
		txt_loading.setTypeface(textface);

		txt_loading.setText(Html.fromHtml("<b>" + message + "<b>"));

		/*
		 *
		 * progress_dialog.setButton(mContext.getString(R.string.button_cancel)
		 * , new DialogInterface.OnClickListener() {
		 *
		 * @Override public void onClick(final DialogInterface dialog, final int
		 * which) { BasePrint.cancel(); Message msg =
		 * mHandle.obtainMessage(Common.MSG_PRINT_CANCEL);
		 * mHandle.sendMessage(msg); } });
		 */
		progress_dialog.show();

	}

	/** show the end message */
	public void showPrintCompleteMsgDialog(final String message) {
		isError = false;
		if (progress_dialog != null && progress_dialog.isShowing()) {
			progress_dialog.dismiss();
		}

		if (message.equals("Android OS is not supported")
				&& message.equals("USB device is not found")
				&& message.equals("ERROR_WRONG_LABEL")) {
			isError = true;
			openAlertDialog(message, "error", mContext);
		} else if (message.equals("Not connected to the printer, please check and retry again.")) {
			isError = true;
			openAlertDialog(message, "error", mContext);
		}else if(message.equals("Your printer has wrong label, please check your printer label.")){
			isError = true;
			openAlertDialog(message, "error", mContext);
		} else {
			isError = false;
			if(!isShowDialog) {
				if (Util.getselfcheckinbools(Util.ISPRINTALLOWED)&&message.equals(mContext.getString(R.string.ErrorMessage_ERROR_NONE))) {
					if (Util.getselfcheckinbools(Util.ISAUTOCHECKIN)) {
						BaseActivity.showCustomToast(mContext,
								"Print Completed!",
								R.drawable.img_like,R.drawable.toast_greenroundededge,true);
						((BaseActivity) mContext).onPrintSuccess(mContext);
					} else {
						BaseActivity.showCustomToast(mContext,
								"Print Completed!",
								R.drawable.img_like,R.drawable.toast_greenroundededge,true);
						((BaseActivity) mContext).onPrintSuccess(mContext);
						//((BaseActivity) mContext).finish();//new change for finishing selfcheckinattendeedetail page
						//openAlertDialog(message, "success", mContext);

					}
				} else if(mContext instanceof GlobalScanActivity){
					((GlobalScanActivity) mContext).docheckinprocessafterprint(message);
				}else if(((BaseActivity) mContext).isNotFromSelfcheckin(mContext)&&message.equals(mContext.getString(R.string.ErrorMessage_ERROR_NONE))){
					if (!Util.external_setting_pref.getString(((BaseActivity) mContext).sfdcddetails.user_id + ((BaseActivity) mContext).checked_in_eventId, "").isEmpty()) {
						((BaseActivity) mContext).externalSettings = new Gson().fromJson(Util.external_setting_pref.getString(((BaseActivity) mContext).sfdcddetails.user_id + ((BaseActivity) mContext).checked_in_eventId, ""), ExternalSettings.class);
						if (((BaseActivity) mContext).externalSettings.quick_checkin) {
							BaseActivity.showCustomToast(mContext,
									"Print Completed!",
									R.drawable.img_like, R.drawable.toast_greenroundededge, true);
							((BaseActivity) mContext).onPrintSuccess(mContext);
							((BaseActivity) mContext).setResult(2017);
							//((BaseActivity) mContext).finish();Ma
						}/*else if(mContext instanceof AttendeeDetailActivity){
							((BaseActivity) mContext).onPrintSuccess(mContext);
						}*/else{
							BaseActivity.showCustomToast(mContext,
									"Print Completed!",
									R.drawable.img_like, R.drawable.toast_greenroundededge, true);
							((BaseActivity) mContext).updatePrintStatus(mContext, SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor);
							((BaseActivity) mContext).setResult(2017);//((BaseActivity) mContext).finish();
						}
						//new change for finishing selfcheckinattendeedetail page
						//openAlertDialog(message, "success", mContext);

					}
				}else {
					openAlertDialog(message, "success", mContext);
				}

			}else {
				if(message.equals(mContext.getString(R.string.ErrorMessage_ERROR_NONE))) {
					isError = false;
					if (AppUtils.NullChecker(intent_value).equalsIgnoreCase(SellOrderActivity.class.getName())) {
						Intent i=null;
						if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
							i = new Intent(activity, SelfCheckinAttendeeList.class);
						}else {
							i = new Intent(activity, ManageTicketActivity.class);
						}
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						activity.startActivity(i);
					}
					activity.finish();
				}else{
					isError = true;
					openAlertDialog(message, "error", mContext);
				}
			}
		}
	}

	/** update complete dialog's message */
	public void setMessage(String msg) {

		if (progress_dialog != null && progress_dialog.isShowing()) {
			txt_loading.setText(Html.fromHtml("<b>" + msg + "<b>"));
		}
	}
	/*public void disableCancel() {
		progress_dialog.getButton(progress_dialog.BUTTON_POSITIVE).setEnabled(
				false);
	}*/
	/** show message */
	public void showMsgNoButton(final String title, final String message) {
		isError = false;
		if (progress_dialog != null && progress_dialog.isShowing()) {
			progress_dialog.dismiss();
		}
		textface = Typeface.createFromAsset(mContext.getAssets(),
				"VarelaRound-Regular.ttf");
		progress_dialog = new Dialog(mContext);
		progress_dialog.setCanceledOnTouchOutside(false);
		progress_dialog.setCancelable(false);
		progress_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progress_dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		progress_dialog.setContentView(R.layout.loading_layout);
		// progress_dialog.setCancelable(false);
		txt_loading = (TextView) progress_dialog.findViewById(R.id.txtloading);
		// txt_loading.setTextColor(mContext.getResources().getColor(R.color.green_button_header));
		txt_loading.setTypeface(textface);
		txt_loading.setText(Html.fromHtml("<b>" + title + "\n\n" + message
				+ "<b>"));
		progress_dialog.show();

	}

	/** close dialog */
	public void close() {

		if (progress_dialog != null && progress_dialog.isShowing()) {
			progress_dialog.dismiss();
		}
	}

	public void openAlertDialog(final String error_title, final String type,
								Context context) {
		TextView title;
		Button btn_okey,btn_cancle;

		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dialog.setContentView(R.layout.theme_alert_dialog);
		dialog.show();
		dialog.setCancelable(false);
		title = (TextView) dialog.findViewById(R.id.txt_message);
		btn_okey = (Button) dialog.findViewById(R.id.btnOK);
		btn_cancle = (Button) dialog.findViewById(R.id.btnCancel);
		btn_cancle.setVisibility(View.GONE);
		title.setTypeface(textface);
		title.setText(error_title);

		btn_okey.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				progress_dialog.dismiss();
				deleteDir();
				if(activity instanceof GlobalScanActivity&& BaseActivity.ordersuccess_popupok_clicked)
				{
					Intent i = new Intent(activity, ManageTicketActivity.class);
					BaseActivity.ordersuccess_popupok_clicked=false;
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					activity.startActivity(i);
					activity.finish();
					//activity.finish();
				}
				else if(activity instanceof TransperantGlobalScanActivity)
				{
					activity.finish();
				}
				else if(activity instanceof AttendeeDetailActivity){
					BaseActivity.fromprintsucess=true;
					/*Intent i=null;
					i = new Intent(activity, AttendeeDetailActivity.class);
					i.putExtra("fromPrintSucess","printedSuccesssfully");
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					activity.startActivity(i);*/
				}
				else if(activity instanceof SellOrderActivity){
					Intent i=null;
					if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
						i = new Intent(activity, SelfCheckinAttendeeList.class);
					}else {
						i = new Intent(activity, ManageTicketActivity.class);
					}
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					activity.startActivity(i);
				}else if(activity instanceof OrderSucessPrintActivity){
					/*Intent i = new Intent(activity, ManageTicketActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					activity.startActivity(i);*/
					activity.finish();
				}else if(activity instanceof PrintAttendeesListActivity&&error_title.equals("Print Completed")){
					Intent i=null;
					i = new Intent(activity, PrintAttendeesListActivity.class);
					i.putExtra("fromPrintSucess","printedSuccesssfully");
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					activity.startActivity(i);
				}else if (isError) {
					if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
						Intent i = new Intent(activity, SelfCheckinAttendeeList.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						activity.startActivity(i);
					}else {
						activity.finish();
					}
				}

			}
		});

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
	/*public void disableCancel() {
		progress_dialog.getButton(progress_dialog.BUTTON_POSITIVE).setEnabled(
				false);
	}*/
}

