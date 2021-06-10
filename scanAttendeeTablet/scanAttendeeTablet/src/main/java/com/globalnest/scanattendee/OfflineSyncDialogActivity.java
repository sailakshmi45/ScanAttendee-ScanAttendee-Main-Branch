package com.globalnest.scanattendee;

import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class OfflineSyncDialogActivity extends Activity {


	TextView txt_totalscans,txt_validScans,txt_invalidScans;
	String checkedin_eventId = ITransaction.EMPTY_STRING;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.offlinescans_popup);
		checkedin_eventId = this.getIntent().getStringExtra(Util.INTENT_KEY_1);
				
		String response=getIntent().getStringExtra("SYNCHRESPONSE");	
		String invaliCount=response.split(",")[2];
		String validCount=response.split(",")[1];
		String totalCount=response.split(",")[0];
		
		//final Dialog scanDialog;
		//LayoutInflater inflater = getLayoutInflater();
		//View alertLayout = LayoutInflater.from(LeadsListActivity.this).inflate(R.layout.offlinescans_popup, null);
		txt_totalscans = (TextView) findViewById(R.id.txt_totalLeads);
		txt_validScans = (TextView) findViewById(R.id.txt_validLeads);
		txt_invalidScans = (TextView) findViewById(R.id.txt_invalidLeads);	
		Button btn_done=(Button)findViewById(R.id.btn_done);

		txt_totalscans.setText(totalCount);
		txt_validScans.setText(validCount);
		txt_invalidScans.setText(invaliCount);
		btn_done.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				/*Intent intent = new Intent(OfflineSyncDialogActivity.this, OfflineScanActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);*/
				AppUtils.displayLog("-------------Offline scan Count------------",":"+Util.db.getTotalOfflineScanCount(checkedin_eventId));
				if(Util.db.getTotalOfflineScanCount(checkedin_eventId) > 0){
					Intent intent = new Intent(OfflineSyncDialogActivity.this, OfflineDataSyncActivty.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				}else{
					finish();	
				}
				
				
			}
		});
	}
}
