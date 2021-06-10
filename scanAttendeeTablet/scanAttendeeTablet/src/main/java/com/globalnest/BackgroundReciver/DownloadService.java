package com.globalnest.BackgroundReciver;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.BadgeResponseNew;
import com.globalnest.mvc.DashboradHandler;
import com.globalnest.mvc.ItemsListResponse;
import com.globalnest.mvc.RefreshResponse;
import com.globalnest.mvc.SessionGroup;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.network.HttpClientClass;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.retrofit.rest.ApiClient;
import com.globalnest.retrofit.rest.ApiInterface;
import com.globalnest.scanattendee.BaseActivity;
import com.globalnest.scanattendee.EventListActivity;
import com.globalnest.scanattendee.ExternalSettingsActivity;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Ref;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DownloadService extends IntentService {

	public static final int STATUS_RUNNING = 0;
	public static final int STATUS_FINISHED = 1;
	public static final int STATUS_ERROR = 2;
	// public static boolean IS_TICKETS_LOADING = false;
	public static final String ATT_URL = "ATT_URL";
	public static final String TIC_URL = "TIC_URL";
	public static final String BADGE_URL = "BADGE_URL";
	public static final String EVENTID = "EVENTID";
	public static final String REQUESTTYPE = "REQUESTTYPE";
	public static final String ACCESSTOKEN = "ACCESSTOKEN";
	public static final String IS_DOWNLOAD_ALL = "IS_DOWNLOAD_ALL";
	public static final String RECEIVER = "receiver";
	public static final String reload = "Reload";
	public static final String ACTIVITY_NAME = "activity_name";
	public static final String LASTMODIFIEDDATE = "LastModifiedDate";
	public static final String REFRESHBADGECOUNT = "refreshBadgeCount";

	private String requestType = "", accessToken = "", attURL = "", ticURL = "", badgeURL = "", activity_name = "",LastModifiedDate="";
	private static final String TAG = "---DownloadService---";
	private String eventId = "";
	private boolean isReloadService = false;
	private ResultReceiver receiver;
	private int count = 0;
	private int refreshBadgeCount = 1;

	public DownloadService() {
		super(DownloadService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		//Log.d(TAG, "Service Started!");
		receiver = intent.getParcelableExtra("receiver");
		attURL = intent.getStringExtra(ATT_URL);
		ticURL = intent.getStringExtra(TIC_URL);
		badgeURL = intent.getStringExtra(BADGE_URL);
		eventId = intent.getStringExtra(EVENTID);
		requestType = intent.getStringExtra(REQUESTTYPE);
		accessToken = intent.getStringExtra(ACCESSTOKEN);
		activity_name = intent.getStringExtra(ACTIVITY_NAME);
		LastModifiedDate = intent.getStringExtra(LASTMODIFIEDDATE);
		//refreshBadgeCount =intent.getIntExtra(REFRESHBADGECOUNT,1);
		String load = intent.getStringExtra(reload);
		if (load != null && load.equals(reload)) {
			isReloadService = true;
		}
		Bundle bundle = new Bundle();

		if (!TextUtils.isEmpty(ticURL)) {
			/* Update UI: Download Service is Running */
			if (isReloadService) {
				bundle.putInt("Count", count);
				bundle.putString("EVENTID", eventId);
				bundle.putString(DownloadService.reload, DownloadService.reload);
				receiver.send(STATUS_RUNNING, bundle);
			} else {
				receiver.send(STATUS_RUNNING, Bundle.EMPTY);
			}
			try {
				if (requestType.equalsIgnoreCase("Refresh")) {
					doAttendeeRefresh();//changed for speed refreshing
					//downloadData(ticURL, accessToken);
					//new DownloadDataTask(ticURL,accessToken).execute();
				} else if (requestType.equalsIgnoreCase("tickets")) {
					downloadData(ticURL, accessToken);
					//new DownloadDataTask(ticURL,accessToken).execute();
				} else if (requestType.equalsIgnoreCase("orders")) {
					doAttendeeLoadMore();
					/*ApiInterface apiService =
							ApiClient.getClient().create(ApiInterface.class);
					String url[] = attURL.split(WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "?");
					//Call<TotalOrderListHandler> call = apiService.getAttendeesurl(url[1],accessToken);
					Call<TotalOrderListHandler> call = apiService.getAttendees(BaseActivity.checkedin_event_record.Events.Id
							, badgeURL, "", BaseActivity.checkedin_event_record.Events.scan_attendee_limit__c, accessToken);
					call.enqueue(new Callback<TotalOrderListHandler>() {
						@Override
						public void onResponse(Call<TotalOrderListHandler> call, Response<TotalOrderListHandler> response) {
							TotalOrderListHandler totalorderlisthandler = response.body();
							Util.db.upadteOrderList(totalorderlisthandler.TotalLists,eventId);
							if(totalorderlisthandler.TotalLists.size() > 0){
								if(count == 0){
									count = totalorderlisthandler.TotalLists.size();
								}else{
									count = count+totalorderlisthandler.TotalLists.size();
								}
								Util.offset_pref = getSharedPreferences(Util.OFFSET_PREF, MODE_PRIVATE);
								String last_record_id = totalorderlisthandler.TotalLists.get(totalorderlisthandler.TotalLists.size()-1).orderInn.getOrderId();
								Util._saveDataPreference(Util.offset_pref,eventId, last_record_id);
							}
							if((Util.db.totalOrderCount(eventId) < Util.dashboardHandler.totalOrders) &&
									!totalorderlisthandler.TotalLists.isEmpty() &&
									activity_name.equalsIgnoreCase(ExternalSettingsActivity.class.getName())) {
								doAttendeeLoadMore();
							}else {
								Bundle bundle=new Bundle();
								boolean blogTitles = false;
								bundle.putString("RESULT", requestType);
								bundle.putString("EVENTID", eventId);
								receiver.send(STATUS_FINISHED, bundle);
								blogTitles=true;
								Util.sessionCountPref.edit().putBoolean(Util.isExternalDataDownloaded,true).commit();
							}
							Log.e(TAG, "------success-------");
							//Log.d(TAG, "Number of movies received: " + movies.size());
						}

						@Override
						public void onFailure(Call<TotalOrderListHandler> call, Throwable t) {
							// Log error here since request failed
							Log.e(TAG, t.toString());
						}
					});*/
					//downloadData(attURL,accessToken);
				} else if (requestType.equalsIgnoreCase("Statistics")) {
					downloadData(ticURL, accessToken);
				} else {
					requestType = "";
					downloadData(attURL, accessToken);
					//new DownloadDataTask(attURL,accessToken).execute();
				}
			} catch (Exception e) {
				/* Sending error message back to activity */
				e.printStackTrace();
				bundle.putString(Intent.EXTRA_TEXT, e.toString());
				receiver.send(STATUS_ERROR, bundle);
			}
		}
		//Log.d(TAG, "Service Stopping!");
		this.stopSelf();
	}
	/*if((Util.db.totalOrderCount(eventId) < Util.dashboardHandler.totalOrders) &&  activity_name.equalsIgnoreCase(ExternalSettingsActivity.class.getName())){
                receiver.send(STATUS_RUNNING, Bundle.EMPTY);
                //bundle.putInt("Count", count);
                blogTitles=true;
                attURL = attURL.replaceAll("[&?]offset.*?(?=&|\\?|$)", "");
                if(Util.db.totalOrderCountwithoutCancelled(eventId)==0){
                    attURL = attURL+"&"+"offset=";
                }else {
                    attURL = attURL + "&" + "offset=" + Util.offset_pref.getString(eventId, "");
                }
                //attURL = attURL+"&"+"offset="+Util.offset_pref.getString(eventId,"");
                requestType="orders";
                //downloadData(attURL,accessToken);//retrofit
                //new DownloadDataTask(attURL,accessToken).execute();
            }else{
                bundle.putString("RESULT", requestType);
                bundle.putString("EVENTID", eventId);
                receiver.send(STATUS_FINISHED, bundle);
                blogTitles=true;
                Util.sessionCountPref.edit().putBoolean(Util.isExternalDataDownloaded,true).commit();
            }
            */
	private void doAttendeeRefresh() {
		final Bundle bundle=new Bundle();
		final boolean blogTitles = false;
		try {
			String url[] = ticURL.split(WebServiceUrls.SA_REFRESH_URL + "?");
			ApiInterface apiService =
					ApiClient.getClient(url[0]).create(ApiInterface.class);

//badgeURL used as userid
			Call<RefreshResponse> call =apiService.getRefreshAttendees(BaseActivity.checkedin_event_record.Events.Id,
					badgeURL,"",LastModifiedDate,"Itemandattendees",refreshBadgeCount,accessToken);
			/*Call<TotalOrderListHandler> call = apiService.getAttendees(BaseActivity.checkedin_event_record.Events.Id
					, badgeURL, Util.offset_pref.getString(eventId, ""), BaseActivity.checkedin_event_record.Events.scan_attendee_limit__c, accessToken);*/
			Log.e("------RerofitURL-------", "------response started-------");
			String httpurl = url[0] + WebServiceUrls.SA_REFRESH_URL+ "Event_id=" + BaseActivity.checkedin_event_record.Events.Id
					+ "&User_id=" + badgeURL+"&appname="+"&LastModifiedDate="+LastModifiedDate
					+"&Request_Flag=Itemandattendees"+"&startbatch="+refreshBadgeCount;
			if(AppUtils.isLogEnabled) {
				AppUtils.displayLog(call + "------ Url-------", httpurl);
				AppUtils.displayLog("------------- Access_token--------------", ":"+ accessToken);
			}
			call.enqueue(new Callback<RefreshResponse>() {
				@Override
				public void onResponse(Call<RefreshResponse> call, Response<RefreshResponse> response) {
					Log.e("------success-------", "------response started-------");
					if (response.code() == 200) {
						RefreshResponse refresh  = response.body();
						if (refresh.BLN_ASC_ItemsListOUTPUT != null) {
								Util.db.upadteItemListRecordInDB(refresh.BLN_ASC_ItemsListOUTPUT, eventId);
						}
						if (refresh.TotalLists != null) {
							Util.db.upadteOrderList(refresh.TotalLists, eventId);
						}
						if (refresh.ticketTags != null) {
							Util.db.InsertAndUpdateTicketTag(refresh.ticketTags);
						}
						refreshBadgeCount = refreshBadgeCount+1;
						int count = 0;
						if(refresh.noofBatch !=null && !refresh.noofBatch.equals("")){
							count = Integer.parseInt(refresh.noofBatch);
						}

						if(count > 1 && refreshBadgeCount <= count){
							bundle.putString("isFrom", "Refresh");
							receiver.send(STATUS_RUNNING, bundle);
							String url = ticURL.replace("startbatch=1","startbatch="+refreshBadgeCount);
							doAttendeeRefresh();
						}else{
							Util.sessionCountPref.edit().putString(Util.isSessionRefreshed,"true").commit();// To fetch session records from session start time
							Util.lastModifideDate.edit().putString(Util.ITEMSANDATTENDEESLASTMODITIFEDATE,refresh.LastRefreshedDate).commit();
							Util.db.updateEventRefreshDate(eventId, refresh.LastRefreshedDate);
							bundle.putString("isFrom", "Refresh");
							receiver.send(STATUS_FINISHED, bundle);
						}
						Log.e("------success-------", "------inserrtion ended-------");
						//Log.d(TAG, "Number of movies received: " + movies.size());
					}
				}
				@Override
				public void onFailure(Call<RefreshResponse> call, Throwable t) {
					// Log error here since request failed
					Log.e("------failure-------", t.toString());
					receiver.send(STATUS_ERROR, Bundle.EMPTY);
				}
			});
			//downloadData(attURL,accessToken);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	private void doAttendeeLoadMore() {
		final Bundle bundle=new Bundle();
		final boolean blogTitles = false;
		try {
			String url[] = attURL.split(WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "?");
			ApiInterface apiService =
					ApiClient.getClient(url[0]).create(ApiInterface.class);
			//Call<TotalOrderListHandler> call = apiService.getAttendeesurl(url[1],accessToken);
			//badgeURL used as userid
			Call<TotalOrderListHandler> call = apiService.getAttendees(BaseActivity.checkedin_event_record.Events.Id
					, badgeURL, Util.offset_pref.getString(eventId, ""), BaseActivity.checkedin_event_record.Events.scan_attendee_limit__c, accessToken);
			Log.e("------RerofitURL-------", "------response started-------");
			String httpurl = url[0] + WebServiceUrls.SA_ATTENDEE_LOAD_MORE+ "Event_id=" + BaseActivity.checkedin_event_record.Events.Id
					+ "&User_id=" + badgeURL+"&offset="+Util.offset_pref.getString(eventId, "")+"&limit"+BaseActivity.checkedin_event_record.Events.scan_attendee_limit__c;
			if(AppUtils.isLogEnabled) {
				AppUtils.displayLog(call + "------ Url-------", httpurl);
				AppUtils.displayLog("------------- Access_token--------------", ":"+ accessToken);
			}
			call.enqueue(new Callback<TotalOrderListHandler>() {
				@Override
				public void onResponse(Call<TotalOrderListHandler> call, Response<TotalOrderListHandler> response) {
					Log.e("------success-------", "------response started-------");
					if (response.code() == 200) {
							TotalOrderListHandler totalorderlisthandler = response.body();
							if (totalorderlisthandler.TotalLists.size() > 0) {
								Util.db.upadteOrderList(totalorderlisthandler.TotalLists, eventId);
								if (count == 0) {
									count = totalorderlisthandler.TotalLists.size();
								} else {
									count = count + totalorderlisthandler.TotalLists.size();
								}
								Util.offset_pref = getSharedPreferences(Util.OFFSET_PREF, MODE_PRIVATE);
								String last_record_id = totalorderlisthandler.TotalLists.get(totalorderlisthandler.TotalLists.size() - 1).orderInn.getOrderId();
								Util._saveDataPreference(Util.offset_pref, eventId, last_record_id);
							}
							if ((Util.db.totalOrderCount(eventId) < Util.dashboardHandler.totalOrders) &&
									!totalorderlisthandler.TotalLists.isEmpty() &&
									activity_name.equalsIgnoreCase(ExternalSettingsActivity.class.getName())) {
								doAttendeeLoadMore();
								receiver.send(STATUS_RUNNING, Bundle.EMPTY);
								bundle.putInt("Count", count);
								boolean blogTitles = true;
							} else {
								bundle.putString("RESULT", requestType);
								bundle.putString("EVENTID", eventId);
								bundle.putInt("Count", count);
								receiver.send(STATUS_FINISHED, bundle);
								//blogTitles=true;
								Util.sessionCountPref.edit().putBoolean(Util.isExternalDataDownloaded, true).commit();
							}
							Log.e("------success-------", "------inserrtion ended-------");
							//Log.d(TAG, "Number of movies received: " + movies.size());
						}
					}
					@Override
					public void onFailure(Call<TotalOrderListHandler> call, Throwable t) {
						// Log error here since request failed
						Log.e("------failure-------", t.toString());
						receiver.send(STATUS_ERROR, Bundle.EMPTY);
					}
				});
				//downloadData(attURL,accessToken);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		private boolean downloadData(String requestUrl,String _access_token) throws IOException, DownloadException {
			String response="";
			HttpClient client = HttpClientClass.getHttpClient(60000);
			HttpPost postMethod = new HttpPost(requestUrl);

			if(_access_token != null){
				postMethod.addHeader("Authorization", _access_token);
			}
			AppUtils.displayLog(TAG+"-- ---------------URL --", ":" + requestUrl.toString());
			AppUtils.displayLog(TAG+"-- ----------------ACCESS TOKEN --", ":"+ _access_token);
			HttpResponse http_response = client.execute(postMethod);
			int res_code = http_response.getStatusLine().getStatusCode();
			AppUtils.displayLog(TAG+"-- RESPONSE CODE --", ":"+ res_code);
			//boolean results = false;
			if (res_code == 200) {
				response = EntityUtils.toString(http_response.getEntity());
				boolean isUserLoggedin = Util.db.isUserExists(DBFeilds.TABLE_USER, " where UserRole = '" + ITransaction.LOGGEDIN_USER + "'");
				if (isUserLoggedin) {
					return parseResult(response);
				}
				return false;
			} else {
				receiver.send(STATUS_ERROR, Bundle.EMPTY);
				throw new DownloadException("Failed to fetch data!!");
			}
		}

		private boolean parseResult(String result) {

			boolean blogTitles = false;
			Gson gson=new Gson();

			try {
				Bundle bundle=new Bundle();
				AppUtils.displayLog("--------------Response-----------------", "."+result);

				if(requestType.equalsIgnoreCase("tickets")){
					//Log.i("---------------------------Download Service class----", "Saving Tickets....."+result);
					ItemsListResponse item_response =gson.fromJson(result, ItemsListResponse.class);
					//TicketListResponseHandler[] responseHandler = gson.fromJson(result, TicketListResponseHandler[].class);
					Util.db.upadteItemListRecordInDB(item_response.Itemscls_infoList,eventId);
					// For Attendee Settings
                    Util.db.InsertUpdateRegsettings(item_response.settingsForScanAttendee,false);
                    //Util.db.InsertandUpadteFeildTypes(item_response.tktprofilefieldtype);

                    Util.db.InsertAndUpdateSEMINAR_AGENDA(item_response.agndaInfo);
					List<SessionGroup> group_list = Util.db.getGroupList(BaseActivity.checkedin_event_record.Events.Id);
					Util.db.deleteEventScannedTicketsGroup(BaseActivity.checkedin_event_record.Events.Id);
					Util.db.deleteEventScannedTickets(BaseActivity.checkedin_event_record.Events.Id);

					if(!group_list.isEmpty()){
						for(SessionGroup group : group_list){
							for(SessionGroup server_group : item_response.userSessions){
								if(group.Id.equalsIgnoreCase(server_group.Id)){
									if(server_group.BLN_Session_users__r.records.size() > 0){
										server_group.BLN_Session_users__r.records.get(0).DefaultValue__c = group.Scan_Switch;
									}

								}
							}
						}
					}
					Util.db.InsertAndUpdateSESSION_GROUP(item_response.userSessions);

					if(isReloadService){
						bundle.putInt("Count", count);
						bundle.putString("EVENTID", eventId);
						bundle.putString(DownloadService.reload, DownloadService.reload);
						receiver.send(STATUS_RUNNING, bundle);
					}else{
						receiver.send(STATUS_RUNNING, Bundle.EMPTY);
					}
					//IS_TICKETS_LOADING = true;
					if(activity_name.equalsIgnoreCase(EventListActivity.class.getName())){
						requestType="badges";
						downloadData(badgeURL,accessToken);
					}

					//new DownloadDataTask(attURL,accessToken).execute();
				}else if(requestType.equalsIgnoreCase("badges")){
					Type listType = new TypeToken<List<BadgeResponseNew>>() {}.getType();
					List<BadgeResponseNew> badges = (List<BadgeResponseNew>) gson.fromJson(result, listType);
					//Log.i("---------------- parseJsonResponse Badge Size----------", ":" + badges.size());
					Util.db.deleteBadges(eventId);
					for(BadgeResponseNew badge : badges){
						badge.badge.event_id = eventId;
						Util.db.InsertAndUpdateBadgeTemplateNew(badge);
					}
				/*requestType="";
				downloadData(attURL,accessToken);*/
					if(activity_name.equalsIgnoreCase(EventListActivity.class.getName())){
						requestType="orders";
						downloadData(attURL,accessToken);
					}
					//sharedPreferences.edit().clear().commit();
				}else if(requestType.equalsIgnoreCase("Refresh")){


					RefreshResponse refresh = gson.fromJson(result, RefreshResponse.class);
					if (refresh.BLN_ASC_ItemsListOUTPUT != null) {
						// TicketListResponseHandler items[] =
						// refresh.BLN_ASC_ItemsListOUTPUT.toArray(new
						// TicketListResponseHandler[refresh.BLN_ASC_ItemsListOUTPUT.size()]);

						Util.db.upadteItemListRecordInDB(refresh.BLN_ASC_ItemsListOUTPUT, eventId);
					}
					//Util.db.updateEventRefreshDate(checked_in_eventId, refresh.LastRefreshedDate);
					if (refresh.TotalLists != null) {
						Util.db.upadteOrderList(refresh.TotalLists, eventId);
					}
					if (refresh.ticketTags != null) {
						Util.db.InsertAndUpdateTicketTag(refresh.ticketTags);
					}

					refreshBadgeCount = refreshBadgeCount+1;
					int count = 0;
					if(refresh.noofBatch !=null && !refresh.noofBatch.equals("")){
						count = Integer.parseInt(refresh.noofBatch);
					}

					if(count > 1 && refreshBadgeCount <= count){
						bundle.putString("isFrom", "Refresh");
						receiver.send(STATUS_RUNNING, bundle);
						String url = ticURL.replace("startbatch=1","startbatch="+refreshBadgeCount);
						downloadData(url,accessToken);
					}else{
						Util.sessionCountPref.edit().putString(Util.isSessionRefreshed,"true").commit();// To fetch session records from session start time
						Util.lastModifideDate.edit().putString(Util.ITEMSANDATTENDEESLASTMODITIFEDATE,refresh.LastRefreshedDate).commit();
						Util.db.updateEventRefreshDate(eventId, refresh.LastRefreshedDate);
						bundle.putString("isFrom", "Refresh");
						receiver.send(STATUS_FINISHED, bundle);
					}

				}else if(requestType.equalsIgnoreCase("Statistics")){
					DashboradHandler dashboard_data = new DashboradHandler();
					Util.dashboard_data_pref.edit().putString(attURL + eventId, result).commit();
					dashboard_data = gson.fromJson(result, DashboradHandler.class);
					Util.dashboardHandler = dashboard_data;
					bundle.putString("isFrom", "Statistics");
					receiver.send(STATUS_FINISHED, bundle);
				}else{
					//Log.i("---------------------------Download Service class----", "Saving Attendees....."+result);
					TotalOrderListHandler totalorderlisthandler = gson.fromJson(result, TotalOrderListHandler.class);
					Util.db.upadteOrderList(totalorderlisthandler.TotalLists,eventId);
					if(totalorderlisthandler.TotalLists.size() > 0){
						if(count == 0){
							count = totalorderlisthandler.TotalLists.size();
						}else{
							count = count+totalorderlisthandler.TotalLists.size();
						}
						Util.offset_pref = getSharedPreferences(Util.OFFSET_PREF, MODE_PRIVATE);
						String last_record_id = totalorderlisthandler.TotalLists.get(totalorderlisthandler.TotalLists.size()-1).orderInn.getOrderId();
						Util._saveDataPreference(Util.offset_pref,eventId, last_record_id);
					}
					if(totalorderlisthandler.ticketTags != null){
						Util.db.InsertAndUpdateTicketTag(totalorderlisthandler.ticketTags);
					}

					//Log.i("-----Download Service class Total Orders And Downloaded Orders--------------", ":"+Util.dashboardHandler.totalOrders+" : "+Util.db.totalOrderCount(eventId));
					if(isReloadService){
						if(totalorderlisthandler.TotalLists !=null && totalorderlisthandler.TotalLists.size() >0){
							bundle.putInt("Count", count);
							bundle.putString("EVENTID", eventId);
							bundle.putString(DownloadService.reload, DownloadService.reload);
							receiver.send(STATUS_RUNNING, bundle);
							blogTitles=true;
							attURL = attURL.replaceAll("[&?]offset.*?(?=&|\\?|$)", "");
							if(Util.db.totalOrderCountwithoutCancelled(eventId)==0){
								attURL = attURL+"&"+"offset=";
							}else {
								attURL = attURL + "&" + "offset=" + Util.offset_pref.getString(eventId, "");
							}
							requestType="orders";
							downloadData(attURL,accessToken);
						}else{
							bundle.putString("RESULT", requestType);
							bundle.putString("EVENTID", eventId);
							receiver.send(STATUS_FINISHED, bundle);
							blogTitles=true;
							Util.sessionCountPref.edit().putBoolean(Util.isExternalDataDownloaded,true).commit();
						}
					}else{
						if((Util.db.totalOrderCount(eventId) < Util.dashboardHandler.totalOrders) && !totalorderlisthandler.TotalLists.isEmpty() && activity_name.equalsIgnoreCase(ExternalSettingsActivity.class.getName())){
							receiver.send(STATUS_RUNNING, Bundle.EMPTY);
							//bundle.putInt("Count", count);
							blogTitles=true;
							attURL = attURL.replaceAll("[&?]offset.*?(?=&|\\?|$)", "");
							if(Util.db.totalOrderCountwithoutCancelled(eventId)==0){
								attURL = attURL+"&"+"offset=";
							}else {
								attURL = attURL + "&" + "offset=" + Util.offset_pref.getString(eventId, "");
							}
							//attURL = attURL+"&"+"offset="+Util.offset_pref.getString(eventId,"");
							requestType="orders";
							downloadData(attURL,accessToken);//retrofit
							//new DownloadDataTask(attURL,accessToken).execute();
						}else{
							bundle.putString("RESULT", requestType);
							bundle.putString("EVENTID", eventId);
							receiver.send(STATUS_FINISHED, bundle);
							blogTitles=true;
							Util.sessionCountPref.edit().putBoolean(Util.isExternalDataDownloaded,true).commit();
						}
					}

				}
			} catch (Exception e){
				e.printStackTrace();
				receiver.send(STATUS_ERROR, Bundle.EMPTY);
			}

			return blogTitles;
		}

   /* private class DownloadDataTask extends SafeAsyncTask<String>{

		 (non-Javadoc)
		 * @see java.util.concurrent.Callable#call()

    	private String requestUrl=ITransaction.EMPTY_STRING;
    	private String _access_token = ITransaction.EMPTY_STRING;
    	public DownloadDataTask(String requestUrl,String _access_token){
    		this.requestUrl = requestUrl;
    		this._access_token = _access_token;
    	}

		@Override
		public String call() throws Exception {
			// TODO Auto-generated method stub
			return downloadData(requestUrl, _access_token);
		}
    	protected void onSuccess(String result) throws Exception{
    		super.onSuccess(result);
    		boolean isUserLoggedin = Util.db.isUserExists(DBFeilds.TABLE_USER, " where UserRole = '" + ITransaction.LOGGEDIN_USER + "'");
			if (isUserLoggedin) {
				parseResult(result);
			}

    	}
    }*/

		public class DownloadException extends Exception {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public DownloadException(String message) {
				super(message);
			}

			public DownloadException(String message, Throwable cause) {
				super(message, cause);
			}
		}


	}
