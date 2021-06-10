package com.globalnest.scanattendee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.OfflineScansObject;
import com.globalnest.mvc.OfflineSyncFailuerObject;
import com.globalnest.mvc.OfflineSyncResController;
import com.globalnest.mvc.OfflineSyncSuccessObject;
import com.globalnest.mvc.TStatus;
import com.globalnest.network.HttpClientClass;
import com.globalnest.network.SafeAsyncTask;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.UserProfile;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.SFDCDetails;
import com.globalnest.utils.Util;
import com.google.gson.Gson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import static com.globalnest.scanattendee.BaseActivity.isOfflineSyncRunning;

public class OfflineSyncBroadCast extends BroadcastReceiver {
	SFDCDetails sfdc_details;
	UserProfile user;
	Context context;
	List<OfflineScansObject> offlineScans;
	Toast toast;
	String event_id= ITransaction.EMPTY_STRING;
   // ProgressDialog dialog;
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			//Log.i("-------------Base Activity-------------",	":" + BaseActivity.getBaseActivity());
			/*if(BaseActivity.getBaseActivity() != null){
				dialog = new ProgressDialog(BaseActivity.getBaseActivity());
			}
			*/
			if(!isOfflineSyncRunning&&BaseActivity.OFFLINESCANS>0) {// temp fix
				if (isOnline(context)) {
					if (BaseActivity.layout_offline_tag != null) {
						BaseActivity.layout_offline_tag.setVisibility(View.GONE);
					}
					BaseActivity.initializeDB(context);

					Util.eventPrefer = context.getSharedPreferences(Util.eventpref, Context.MODE_PRIVATE);
					event_id = Util._getPreference(Util.eventPrefer, Util.EVENT_CHECKIN_ID);
					AppUtils.displayLog("--------------Checked In Event Record--------------", ":" + event_id);
					offlineScans = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + event_id + "' AND " + DBFeilds.OFFLINE_BADGE_STATUS + " != '" + DBFeilds.STATUS_INVALID + "'", false);
					this.context = context;
					sfdc_details = Util.db.getSFDCDDETAILS();
					toast = Toast.makeText(this.context.getApplicationContext(), "Syncing offline data...", Toast.LENGTH_LONG);
					if(offlineScans.size()>0){
						new GetAccessToken().execute();
					}
				} else {
					if (BaseActivity.layout_offline_tag != null) {
						BaseActivity.layout_offline_tag.setVisibility(View.VISIBLE);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String getValues() {
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("scannedby", sfdc_details.user_id));
		values.add(new BasicNameValuePair("eventId", event_id));
		values.add(new BasicNameValuePair("source", "Offline"));
		values.add(new BasicNameValuePair("DeviceType", Util.getDeviceNameandAppVersion()));
		return AppUtils.getQuery(values);
	}

	private String getJsonBody() {
		JSONArray ticketarray = new JSONArray();
		for (OfflineScansObject scanObj : offlineScans) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("TicketId", scanObj.badge_id);
				obj.put("sTime", Util.getOfflineSyncServerFormat(scanObj.scan_date_time));
				obj.put("isCHeckIn", true);
				obj.put("freeSemPoolId", scanObj.item_pool_id);
				obj.put("device", "ANDROID");
				obj.put("sessionid", scanObj.scan_group_id);
				ticketarray.put(obj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ticketarray.toString();
	}

	public static Boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null && ni.isConnected())
			return true;
		else
			return false;
	}
	
	private class GetAccessToken extends SafeAsyncTask<String>{

		protected void onPreExecute() throws Exception{
			super.onPreExecute();
			//toast.setText("Syncing offline data...");
			
			
		}
		@Override
		public String call() throws Exception {
			// TODO Auto-generated method stub
			return getAccessTokenString(sfdc_details.refresh_token);
		}
		
		protected void onSuccess(String result )throws Exception{
			super.onSuccess(result);
			AppUtils.displayLog("-------------Offline Get Access Token Response------------",":"+result);
			try {
				
				if(BaseActivity.NullChecker(result).isEmpty()){
					if(offlineScans.size() > 0){
						toast.setText("Error occured while syncing offline data please export all offline data...");
						toast.show();
					}
				}else if(!BaseActivity.isValidResponse(result)){
					if(!AppUtils.NullChecker(sfdc_details.user_id).isEmpty()){
						Intent i = new Intent(context,AlertDialogActivity.class);
						i.putExtra(Util.INTENT_KEY_1, BaseActivity.errorMessage(result));
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(i);
					}
					//openSessionExpireAlert(BaseActivity.errorMessage(result));
				}else{
					SFDCDetails current_sfdc= new Gson().fromJson(result, SFDCDetails.class);
					sfdc_details.access_token = current_sfdc.access_token;
					Util.db.InsertAndUpdateSFDCDDETAILS(sfdc_details);
					sfdc_details = Util.db.getSFDCDDETAILS();
					AppUtils.displayLog("---------------Offline Broadcast----------",":"+offlineScans.size());
					if (isOnline(context) && offlineScans.size() > 0) {
						//Log.i("---OfflineSyncBrosdCast----", " ---in the Service---");
						/*String _url = sfdc_details.instance_url + WebServiceUrls.SA_TICKETS_SCAN_URL + getValues();
						String access_token = sfdc_details.token_type + " " + sfdc_details.access_token;
						ServiceCall postdataMethod = new ServiceCall(_url, getJsonBody().toString(), access_token);
						postdataMethod.execute();*/
						Intent i = new Intent(context, OfflineDataSyncActivty.class);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(i);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				/*toast.setText("Error occured while syncing please export all offline data..."+e.getMessage());
				toast.cancel();*/
			}
		}
		
		private String getAccessTokenString(String refresh_token) {
			String token = "";
			//Log.i("-----------------ORG Name---------------",":"+AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF));
			try {
				HttpPost post;
				HttpClient client = HttpClientClass.getHttpClient(60000);
				//Log.i("---------------------Authorization Url----------------", ":"+ WebServiceUrls.REFRESH_TOKEN + refresh_token);
				if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("DEV")){
				     post = new HttpPost(WebServiceUrls.SA_DEV_DOMAIN+WebServiceUrls.REFRESH_TOKEN + refresh_token);
				}else if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("QA")){
					 post = new HttpPost(WebServiceUrls.SA_QA_DOMAIN+WebServiceUrls.REFRESH_TOKEN + refresh_token);
				}else if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("PRODUCTION")){
					 post = new HttpPost(WebServiceUrls.SA_PRODUCTION+WebServiceUrls.REFRESH_TOKEN + refresh_token);
					// Log.i("------------Access Token Url----------------",":"+WebServiceUrls.SA_PRODUCTION+WebServiceUrls.REFRESH_TOKEN + refresh_token);
				}else{
					 post = new HttpPost(WebServiceUrls.SA_DEV_DOMAIN+WebServiceUrls.REFRESH_TOKEN + refresh_token);
				}
				//Log.i("------------Access Token Url----------------",":"+WebServiceUrls.SA_QA_DOMAIN_2+WebServiceUrls.REFRESH_TOKEN + refresh_token);
				HttpResponse response = client.execute(post);
				token = EntityUtils.toString(response.getEntity());
				//Log.i("-----------Response--------------", ":" + token);
			} catch (Exception e) {

			}
			return token;
		}
	}

	private class ServiceCall extends SafeAsyncTask<String> {
		private final String _url;
		private String _access_token;
		private String _json_data;
		
		public ServiceCall(String _url, String jsonData, String access_token) {
			this._url = _url;
			this._access_token = access_token;
			this._json_data = jsonData;
		}
		
		protected void onPreExecute() throws Exception{
			super.onPreExecute();
			toast.show();
			//Log.i("-------------Base Activity-------------",	":" + dialog);
			/*if (dialog != null) {
				dialog.setMessage("Please wait syncing offline data....");
				dialog.show();
			}*/
		}
		/* (non-Javadoc)
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public String call() throws Exception {
			// TODO Auto-generated method stub
			return executeRequest();
		}
		protected void onSuccess(String result) throws Exception{
			super.onSuccess(result);
			try {
				/*if (dialog != null) {
					dialog.dismiss();
				}*/
				
				if(BaseActivity.NullChecker(result).isEmpty()){
					toast.setText("Error occured while syncing please export all offline data...");
					toast.show();
				}else if(!BaseActivity.isValidResponse(result)){
					//toast.cancel();
					//openSessionExpireAlert(BaseActivity.errorMessage(result));
					Intent i = new Intent(context,AlertDialogActivity.class);
					i.putExtra(Util.INTENT_KEY_1, BaseActivity.errorMessage(result));
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(i);
				}else{
					//toast.cancel();
					Gson gson = new Gson();
					OfflineSyncResController offlineResponse = gson.fromJson(result,OfflineSyncResController.class);
					
					if (offlineResponse != null) {
                        int invalid_scans = 0;
                        HashMap<String, String> duplicate_barcodes = new HashMap<>();
						for (OfflineSyncFailuerObject syncObj : offlineResponse.FailureTickets) {
							AppUtils.displayLog("-------------Offline Invalid-----------------",":"+syncObj.tbup.ticketId+" : "+isDuplicateFailureRecord(syncObj, offlineResponse.SuccessTickets));
						   if(isDuplicateFailureRecord(syncObj, offlineResponse.SuccessTickets)){
							   continue;
						   }else if(duplicate_barcodes.containsKey(syncObj.tbup.ticketId)){
							   if(duplicate_barcodes.get(syncObj.tbup.ticketId).equalsIgnoreCase(syncObj.tbup.sessionid)){
								   continue;
							   }else{
								   duplicate_barcodes.put(syncObj.tbup.ticketId, syncObj.tbup.sessionid);
								   invalid_scans++;
							   }
						   }else{
							   duplicate_barcodes.put(syncObj.tbup.ticketId, syncObj.tbup.sessionid);
							   invalid_scans++;
						   }
						   
							
							OfflineScansObject scanObject = new OfflineScansObject();
							scanObject.badge_id = syncObj.tbup.ticketId;
							scanObject.badge_status = DBFeilds.STATUS_INVALID;
							scanObject.event_id = event_id;
							scanObject.item_pool_id = syncObj.tbup.freeSemPoolId;
							scanObject.scan_group_id = syncObj.tbup.sessionid;
							scanObject.scan_date_time = Util.getOfflineSyncClientFormat(syncObj.tbup.sTime,BaseActivity.checkedin_event_record.Events.Time_Zone__c);
							scanObject.error = syncObj.msg;
							Util.db.UpdateOfflineInvalidScans(scanObject);
						}

						for (OfflineSyncSuccessObject syncObj : offlineResponse.SuccessTickets) {
							if(Util.db.isItemPoolFreeSession(syncObj.STicketId.BLN_Session_Item__r.BLN_Item_Pool__c, BaseActivity.checkedin_event_record.Events.Id)){
								List<TStatus> session_attendees = new ArrayList<TStatus>();
								session_attendees.add(syncObj.STicketId);
								Util.db.InsertAndUpdateSessionAttendees(session_attendees,event_id);
							}else{
								Util.db.updateCheckedInStatus(syncObj.STicketId,event_id);
							}
							Util.db.deleteOffLineScans("("+ DBFeilds.OFFLINE_BADGE_ID + " = '"
									        + syncObj.STicketId.Ticket__r.Id + "' OR "+DBFeilds.OFFLINE_BADGE_ID+" = '"+syncObj.STicketId.Ticket__r.Badge_ID__c
									        +"' OR "+DBFeilds.OFFLINE_BADGE_ID+" = '"+syncObj.STicketId.Ticket__r.Custom_Barcode__c+"') AND " 
									        + DBFeilds.OFFLINE_GROUP_ID + " = '"
											+ syncObj.STicketId.BLN_Session_user__r.BLN_Group__c + "' AND "
											+ DBFeilds.OFFLINE_EVENT_ID + " = '"
											+ event_id + "'");
						}

						Intent i = new Intent(context, OfflineSyncDialogActivity.class);
						String totalScans = String.valueOf(offlineScans.size());///offlineResponse.FailureTickets.size() + offlineResponse.SuccessTickets.size());
						String successScans = String.valueOf(offlineResponse.SuccessTickets.size());
						String faluerScans = String.valueOf(invalid_scans);
						i.putExtra("SYNCHRESPONSE", totalScans + "," + successScans + "," + faluerScans);
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(i);
					}
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				toast.setText("Error occured while syncing please export all offline data..."+e.getMessage());
				toast.show();
			}
		}

		private String executeRequest() {
			String response=ITransaction.EMPTY_STRING;
			try {
				// int _responsecode;
				// byte[] image_data=null;
				HttpParams params = new BasicHttpParams();
				int timeoutconnection = 30000;
				HttpConnectionParams.setConnectionTimeout(params, timeoutconnection);
				int sockettimeout = 30000;
				HttpConnectionParams.setSoTimeout(params, sockettimeout);
				HttpResponse _httpresponse;
				HttpClient _httpclient = HttpClientClass.getHttpClient(100000);
				HttpPost _httppost = new HttpPost(_url);
				//Log.i("----------------OFFLINE Post Url-------------", ":" + _url);
				 // Log.i("----------------Post Url-------------",":"+_url);
					_httppost.addHeader("Authorization", _access_token);
				AppUtils.displayLog("-----BEARER TOKEN---", ":" + _access_token);
					// Log.i("-----BEARER TOKEN---",":OAuth " + _access_token);
					if (_json_data != null) {
						//Log.i("------JSON Object Data--------------", ":" + _json_data.toString());
						_httppost.setEntity(new StringEntity(_json_data.toString(), "UTF-8"));
					}
					_httpresponse = _httpclient.execute(_httppost);
					response = EntityUtils.toString(_httpresponse.getEntity());
				AppUtils.displayLog("--------------OFFLINE Method Response -----------", ":" + response);

			} catch (Exception e) {
				e.printStackTrace();
				AppUtils.displayLog("--------------OFFLINE Sync Exception -----------", ":" + e.getMessage());
			}

			return response;
		}
	}

	private boolean isDuplicateFailureRecord(OfflineSyncFailuerObject syncObj,List<OfflineSyncSuccessObject> SuccessTickets){
		
		boolean isBoolean = false;
		for(OfflineSyncSuccessObject success : SuccessTickets){
			if(syncObj.tbup.sessionid.equalsIgnoreCase(success.STicketId.BLN_Session_user__r.BLN_Group__c) && (syncObj.tbup.ticketId.equalsIgnoreCase(Util.NullChecker(success.STicketId.Ticket__r.Badge_ID__c)) || 
					syncObj.tbup.ticketId.equalsIgnoreCase(Util.NullChecker(success.STicketId.Ticket__r.Custom_Barcode__c)) || syncObj.tbup.ticketId.equalsIgnoreCase(Util.NullChecker(success.STicketId.Ticket__r.Id)))){
				isBoolean = true;
				break;
			}
		}
		return isBoolean;
	}
}
