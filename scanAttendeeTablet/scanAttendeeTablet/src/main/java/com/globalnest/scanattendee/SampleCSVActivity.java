package com.globalnest.scanattendee;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.globalnest.network.HttpGetMethod;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.utils.Util;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class SampleCSVActivity extends BaseActivity {
	TextView txt_note, txt_note5, txt_fname, txt_lname, txt_email, txt_phone, txt_comp, txt_jobtitle, txt_city, txt_address,
	txt_state, txt_zipcode, txt_category, txt_note1, txt_note2, txt_note3, txt_note4;
	Button btn_export;
	Dialog instructiondialog, send_dialog;
	RecipientAdapter adapter = new RecipientAdapter();
	ArrayList<EmailRecipient> recpientlist = new ArrayList<EmailRecipient>();
	EmailRecipient recipient_email;
	String recipientEmail="";
	HttpGetMethod getMethod;
	final int NEW_DIALOG =0;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.sample_csv_layout);
		Util.setCustomAlertDialog(this);
		showDialog(NEW_DIALOG, savedInstanceState);
		
		 back_layout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					finish();
				}
			});
		
          Util.txt_okey.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Util.alert_dialog.dismiss();
				
			}
		});
		
	}
	@Override
	public void doRequest() {
		// TODO Auto-generated method stub
		getMethod = new HttpGetMethod(sfdcddetails.instance_url+WebServiceUrls.SA_SEND_CSV+"Sender_EmailID="+user_profile.Profile.Email__c+
				"&Recipients_EmailID="+recipientEmail, sfdcddetails.token_type,sfdcddetails.access_token,SampleCSVActivity.this);
		getMethod.execute();
	}

	@Override
	public void parseJsonResponse(String response) {
		// TODO Auto-generated method stub
		
		try{
			JSONObject res_object = new JSONObject(response);
			if(res_object.optString("Message").equals("Success")){
				displayToast("The sample CSV file is sent to "+recipientEmail);
				send_dialog.dismiss();
				finish();
				
			}else{
				displayToast(res_object.optString("Message"));
				
			}
			
			
					
		}catch(Exception e){
			
			displayToast(getResources().getString(R.string.connection_error));
		}
	}
	public class EmailRecipient {
		private String email_id = "";
		

		public void setRecipientEmail(String email) {
			this.email_id = email;
		}

		public String getRecipientEmai() {
			return email_id;
		}
     }
	private void displayToast(String msg){
		Toast error = Toast.makeText(SampleCSVActivity.this, msg, Toast.LENGTH_LONG);
		error.setGravity(Gravity.CENTER, 0, 0);
		error.show();
		
	}
	private void openSendCSVDialog(){
		send_dialog = new Dialog(SampleCSVActivity.this, R.style.DialogBottomSlideAnim);
		send_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		send_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(R.color.b1_transparent));
		send_dialog.setContentView(R.layout.send_sample_csv_layout);
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			Window window = send_dialog.getWindow();
			lp.copyFrom(window.getAttributes());
			lp.gravity = Gravity.BOTTOM;
			// This makes the dialog take up the full width
			lp.width = WindowManager.LayoutParams.FILL_PARENT;
			lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
			send_dialog.getWindow().setAttributes(lp);
			send_dialog.setCanceledOnTouchOutside(true);
		
			send_dialog.show();
			
			ImageView img_add = (ImageView) send_dialog.findViewById(R.id.addrecipeint);
			TextView txt_add = (TextView) send_dialog.findViewById(R.id.txtaddrecipient);
			ListView list_recipient = (ListView) send_dialog.findViewById(R.id.list_email_recipient);
			list_recipient.setAdapter(adapter);
			Button btn_cancel = (Button) send_dialog.findViewById(R.id.btnsendcancel);
			Button btn_send = (Button) send_dialog.findViewById(R.id.btnsendcsv);
			
			btn_send.setTypeface(Util.roboto_regular);
			btn_cancel.setTypeface(Util.roboto_regular);
			txt_add.setTypeface(Util.roboto_regular);
			
			recipient_email = new EmailRecipient();
	        recipient_email.setRecipientEmail(user_profile.Profile.Email__c);
			recpientlist.add(recipient_email);
			

			img_add.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(recpientlist.size() >= 3){
						displayToast("You can add up to 3 recipient only.");
						
					}else{
						 recipient_email = new EmailRecipient();
			               recipient_email.setRecipientEmail("");
							recpientlist.add(recipient_email);
							adapter.notifyDataSetChanged();
					}
	              
				}
			});
			
			btn_send.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					
					for(int i=0; i < recpientlist.size(); i++){
						EmailRecipient email = (EmailRecipient) recpientlist.get(i);
						if(!Pattern.matches(Validation.EMAIL_REGEX,email.getRecipientEmai())){
							displayToast(getResources().getString(R.string.email_alert));
						}else{
							recipientEmail += email.getRecipientEmai()+",";
						}
						
					}
					
					if(!recipientEmail.isEmpty()){
						recipientEmail  = recipientEmail.substring(0, recipientEmail.length()-1);
						if(isOnline())
						doRequest();
						else
							displayToast(getResources().getString(R.string.network_error));
						
					}
					
				}
			});
						
				btn_cancel.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								recpientlist.clear();
								send_dialog.dismiss();
							}
						});
	}
	
private class RecipientAdapter extends BaseAdapter{

		
		EditText edit_email;
		Button btn_delete;
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return recpientlist.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return recpientlist.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			 View v = inflater.inflate(R.layout.csv_recipient_item, null);
			   edit_email = (EditText) v.findViewById(R.id.editrecipient);
			btn_delete = (Button) v.findViewById(R.id.btndeleterecipient);
			
			edit_email.setTypeface(Util.roboto_regular);
			edit_email.setText(recpientlist.get(position).getRecipientEmai());
			
			edit_email.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// TODO Auto-generated method stub
                   
					
					////Log.i(s.toString()+"---Array size---",":"+recpientlist.size());
					if(!s.toString().isEmpty() && !recpientlist.isEmpty()){
						recpientlist.get(position).setRecipientEmail(s.toString());
					}
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					
				}
			});
			
			
			btn_delete.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					////Log.i(recpientlist.size()+"----Position of row---",":"+position);
					
					recpientlist.remove(position);
					adapter.notifyDataSetChanged();
				}
			});
			
			return v;
		}
		
		
	}
	
@Override
protected Dialog onCreateDialog(int id, Bundle bundle) {
	
    switch (id) {
        case NEW_DIALOG :
            return openInstructionDialog();
        default:
            return null;
    }
}
@Override
protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
 // TODO Auto-generated method stub
 super.onPrepareDialog(id, dialog, bundle);
 
 switch(id) {
    case NEW_DIALOG:
    
        break;
    }
  }
	private Dialog openInstructionDialog() {
		 instructiondialog = new Dialog(SampleCSVActivity.this,R.style.PopupSlideAnim);
		instructiondialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		instructiondialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		instructiondialog.setContentView(R.layout.csv_instruction_layout);
		/*WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		Window window = instructiondialog.getWindow();
		lp.copyFrom(window.getAttributes());
		lp.gravity = Gravity.TOP;
		
		lp.width = WindowManager.LayoutParams.FILL_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		instructiondialog.getWindow().setAttributes(lp);*/
		instructiondialog.setCanceledOnTouchOutside(true);
		//instructiondialog.show();
		
		return instructiondialog;

	}

	@Override
	public void setCustomContentView(int layout) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(layout, null);
		linearview.addView(v);
		txt_title.setText("Sample CSV");
		img_setting.setVisibility(View.GONE);
		img_menu.setImageResource(R.drawable.back_button);
		event_layout.setVisibility(View.GONE); 
		button_layout.setVisibility(View.GONE);
		event_layout.setVisibility(View.VISIBLE);
		
		txt_fname = (TextView) linearview.findViewById(R.id.txtsamplefname);
		txt_lname = (TextView) linearview.findViewById(R.id.txtsamplelname);
		txt_email = (TextView) linearview.findViewById(R.id.txtsampleemail);
		txt_phone = (TextView) linearview.findViewById(R.id.txtsamplephone);
		txt_comp = (TextView) linearview.findViewById(R.id.txtsamplecompany);
		txt_jobtitle = (TextView) linearview.findViewById(R.id.txtsamplejobtitle);
		txt_address = (TextView) linearview.findViewById(R.id.txtsampleaddress);
		txt_city = (TextView) linearview.findViewById(R.id.txtsamplecity);
		txt_state = (TextView) linearview.findViewById(R.id.txtsamplestate);
		txt_zipcode = (TextView) linearview.findViewById(R.id.txtsamplezipcode);
		
		txt_category = (TextView) linearview.findViewById(R.id.txtsampleattcategory);
		
		btn_export= (Button) linearview.findViewById(R.id.btnexportcsv);
		txt_note = (TextView) linearview.findViewById(R.id.txtcsvnote);
		txt_note1 = (TextView) linearview.findViewById(R.id.txtcsvnote1);
		txt_note2 = (TextView) linearview.findViewById(R.id.txtcsvnote2);
		txt_note3 = (TextView) linearview.findViewById(R.id.txtcsvnote3);
		txt_note4 = (TextView) linearview.findViewById(R.id.txtcsvnote4);
		
		txt_note5 = (TextView) linearview.findViewById(R.id.txtcsvnote5);
		btn_export.setTypeface(Util.roboto_regular);
		txt_note.setTypeface(Util.roboto_regular);
		txt_note1.setTypeface(Util.roboto_regular);
		txt_note2.setTypeface(Util.roboto_regular);
		txt_note3.setTypeface(Util.roboto_regular);
		txt_note4.setTypeface(Util.roboto_regular);
		txt_note5.setTypeface(Util.roboto_regular);
		
		
		txt_fname.setText(Html.fromHtml("First Name <small><font color=#FF0000> (required) </font></small>"));
		txt_lname.setText(Html.fromHtml("Last Name <small><font color=#FF0000> (required) </font></small>"));
		txt_email.setText(Html.fromHtml("Email Id <small><font color=#FF0000> (required) </font></small>"));
		txt_phone.setText(Html.fromHtml("Phone <small><font color=#707070> (optional) </font></small>"));
		txt_comp.setText(Html.fromHtml("Company <small><font color=#707070> (optional) </font></small>"));
		txt_jobtitle.setText(Html.fromHtml("Job Title <small><font color=#707070> (optional) </font></small>"));
		txt_address.setText(Html.fromHtml("Address <small><font color=#707070> (optional) </font></small>"));
		txt_city.setText(Html.fromHtml("City <small><font color=#707070> (optional) </font></small>"));
		txt_state.setText(Html.fromHtml("State <small><font color=#707070> (optional) </font></small>"));
		txt_zipcode.setText(Html.fromHtml("Zip Code <small><font color=#707070> (optional) </font></small>"));
		
		txt_category.setText(Html.fromHtml("Attendee Category <small><font color=#707070> (optional) </font></small>"));
		
		
		txt_note.setText(Html.fromHtml("<b>" +"Note : " +"</b>"));
		
		btn_export.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openSendCSVDialog();
			}
		});
		
		
		
	}
	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
		// TODO Auto-generated method stub
		
	}
	
	

}
