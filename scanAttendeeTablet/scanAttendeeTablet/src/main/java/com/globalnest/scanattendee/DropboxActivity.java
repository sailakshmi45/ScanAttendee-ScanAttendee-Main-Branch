package com.globalnest.scanattendee;

import com.globalnest.utils.Util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
public class DropboxActivity extends BaseActivity {
	EditText edit_name, edit_email, edit_total_byte, edit_used_byte;
	TextView txt_name, txt_email, txt_total_byte, txt_used_byte;
	TextView btn_delete_acc;
	Dialog progress_dialog;
	String user_name, user_email = "", consumed_memory = "", total_memory = "";
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			progress_dialog = new Dialog(this);
			progress_dialog.setCanceledOnTouchOutside(false);
			progress_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			progress_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			this.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			setCustomContentView(R.layout.dropbox_layout);
			back_layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 
					finish();
				}
			});
			btn_delete_acc.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 
					customAskDialog("Drop box", "Do you want to logout from Dropbox account?");
				}
			});
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	public void customAskDialog(String title, String message) {
		try {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.CustomForDialog);
			alertDialog.setIcon(android.R.drawable.ic_dialog_info);
			alertDialog.setTitle(title);
			alertDialog.setMessage(message);
			alertDialog.setInverseBackgroundForced(true);
			alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 
				}
			});
			alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 
					//Util.mApi.getSession().unlink();
					Util.mLoggedIn = false;
					finish();
				}
			});
			Dialog d = alertDialog.show();
			int textViewId = d.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
			TextView tv = (TextView) d.findViewById(textViewId);
			tv.setTextColor(getResources().getColor(R.color.blue_text_color));
			int dividerId = d.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
			View divider = d.findViewById(dividerId);
			divider.setBackgroundColor(getResources().getColor(R.color.blue_text_color));
		} catch (NotFoundException e) {
			
			e.printStackTrace();
		}
	}
	@Override
	public void doRequest() {
		// 
		new GetDropboxAccount().execute();
	}
	@Override
	public void parseJsonResponse(String response) {
		// 
		
	}
	private class GetDropboxAccount extends AsyncTask<String, Void, String> {
		protected void onPreExecute() {
			try {
				progress_dialog.setContentView(R.layout.loading_layout);
				//progress_dialog.setCancelable(false);
				TextView txt_loading = (TextView) progress_dialog.findViewById(R.id.txtloading);
				txt_loading.setTypeface(Util.roboto_regular);
				txt_loading.setText("Loading account details...");
				progress_dialog.show();
				super.onPreExecute();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		@Override
		protected String doInBackground(String... params) {
			// 
			String result = "";
			/*try {
				//Log.i("---Drop Box User Name--", ":" + Util.mApi.accountInfo().displayName);
				user_name = Util.mApi.accountInfo().displayName;
				user_email = Util.mApi.accountInfo().country;
				consumed_memory = String.valueOf(Util.mApi.accountInfo().quotaNormal);
				total_memory = String.valueOf(Util.mApi.accountInfo().quota);
			} catch (DropboxException e) {
				result = e.getMessage();
				e.printStackTrace();
			}*/
			return result;
		}
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				progress_dialog.dismiss();
				if (result.isEmpty()) {
					edit_name.setText(user_name);
					edit_email.setText(user_email);
					edit_total_byte.setText(total_memory);
					edit_used_byte.setText(consumed_memory);
				} else {
					startErrorAnimation(result, txt_error_msg);
				}
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
	}
	@Override
	public void setCustomContentView(int layout) {
		try {
			// 
			View v = inflater.inflate(layout, null);
			linearview.addView(v);
			txt_title.setText("Account Details");
			img_setting.setVisibility(View.GONE);
			img_menu.setImageResource(R.drawable.back_button);
			event_layout.setVisibility(View.GONE);
			button_layout.setVisibility(View.GONE);
			event_layout.setVisibility(View.VISIBLE);
			txt_name = (TextView) linearview.findViewById(R.id.dropbox_name);
			txt_email = (TextView) linearview.findViewById(R.id.dropbox_email);
			txt_total_byte = (TextView) linearview.findViewById(R.id.txt_ttlbytes);
			txt_used_byte = (TextView) linearview.findViewById(R.id.txt_ttlconbytes);
			edit_name = (EditText) linearview.findViewById(R.id.edit_dropbox_name);
			edit_email = (EditText) linearview.findViewById(R.id.edit_dropbox_email);
			edit_total_byte = (EditText) linearview.findViewById(R.id.edit_dropbox_byte);
			edit_used_byte = (EditText) linearview.findViewById(R.id.edit_dropbox_cbyte);
			btn_delete_acc = (TextView) linearview.findViewById(R.id.btn_deleteacc);
			txt_name.setTypeface(Util.roboto_regular);
			txt_email.setTypeface(Util.roboto_regular);
			txt_total_byte.setTypeface(Util.roboto_regular);
			txt_used_byte.setTypeface(Util.roboto_regular);
			edit_name.setTypeface(Util.roboto_regular);
			edit_email.setTypeface(Util.roboto_regular);
			edit_total_byte.setTypeface(Util.roboto_regular);
			edit_used_byte.setTypeface(Util.roboto_regular);
			if (isOnline())
				doRequest();
			else
				startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
		} catch (NotFoundException e) {
			
			e.printStackTrace();
		}
	}
	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
		// 
	}
}
