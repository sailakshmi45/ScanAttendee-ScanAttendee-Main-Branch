package com.globalnest.network;

import android.app.ProgressDialog;

import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.MainActivity;
import com.globalnest.scanattendee.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;


@SuppressWarnings("deprecation")
public class HttpPostData extends SafeAsyncTask<String> {
	
	private String _url;
	private String _values, _access_token,_message;
	private int HTTP_OK = 200;
	private MainActivity _baseactivity;
	private ProgressDialog _dialog;
	private AlertDialogCustom alert_dialog;
    public static DataLoadAsyncTask dataloading = null;
	public HttpPostData(String message,String _url, String values,String access_token,MainActivity baseactivity) {
		this._url = _url;
		this._values = values;
		this._access_token = access_token;
		this._baseactivity = baseactivity;
		this._message=message;
		this._dialog = new ProgressDialog(_baseactivity);
		this.alert_dialog = new AlertDialogCustom(_baseactivity);
		_dialog.setCancelable(false);
		
		//this._dialog.setIndeterminateDrawable(_baseactivity.getDrawable(R.drawable.image_for_rotation));
	}

	@Override
	protected void onPreExecute() throws Exception {
		super.onPreExecute();
		if (_dialog != null && !_dialog.isShowing()) {
			if(_message.isEmpty()){
				_message = "Please wait...";
			}
			_dialog.setMessage(_message);
			
			if(!_message.equalsIgnoreCase("Hide")){
				_dialog.show();
			}
			
		}
	}
	@Override
	public String call() throws Exception {
		// TODO Auto-generated method stub
		String response = executeRequest();
		return response;
	}
	protected void onSuccess(String result) throws Exception {
		super.onSuccess(result);
		
		_dialog.dismiss();
		if (result != null) {
			if(!isValidResponse(result)){
				_baseactivity.parseJsonResponse(result);
			}else if (_url.contains(WebServiceUrls.SA_ATTENDEE_LOAD_MORE) || _url.contains(WebServiceUrls.USER_EVENTS)) {
				dataloading = new DataLoadAsyncTask(this._baseactivity, _message, result);
				dataloading.execute();
			} else {
				// _dialog.dismiss();
				_baseactivity.parseJsonResponse(result);
			}
		} else {
			/*Intent i = new Intent(_baseactivity,LoginActivity.class);
			alert_dialog.setParamenters("Error", errorMessage(result), i, null, 1, true);
			alert_dialog.setAlertImage(R.drawable.error,"error");*/
			if(!_url.contains(WebServiceUrls.SA_DEVICE_SESSION) && !_url.contains(WebServiceUrls.SA_EVENT_DASHBOARD)  && !_url.contains(WebServiceUrls.SA_TICKETS_SCAN_URL) && !_url.contains(WebServiceUrls.SA_SCAN_TICKET)){
				alert_dialog.show();	
			}
			
			
		}

	}

	private String executeRequest() {
		String response=null;
		try {
			HttpClient client = HttpClientClass.getHttpClient(40000);
			HttpPost postMethod = new HttpPost(_url);
			
			if(_url.contains("api.stripe.com") || _url.contains("vault.trustcommerce.com") || _url.contains("connect.stripe.com")){
			   postMethod.setHeader(HTTP.CONTENT_TYPE,"application/x-www-form-urlencoded");
			}
			if(_access_token != null){
			   postMethod.addHeader("Authorization", _access_token);
			}
			//postMethod.addHeader("Content-Type", "application/x-www-form-u rlencoded");
			
			if(_values != null){
			    postMethod.setEntity(new StringEntity(_values,"UTF-8"));
			}
			AppUtils.displayLog("--------------Url--------------", ":" + _url.toString());
			AppUtils.displayLog("--------------Values--------------", ":" + _values);
			AppUtils.displayLog("------------- Access_token--------------", ":"+ _access_token);
			/*Log.i("--------------Url--------------", ":" + _url.toString());
			Log.i("--------------Values--------------", ":" + _values);
			Log.i("------------- Access_token--------------", ":"+ _access_token);*/
			
			HttpResponse http_response = client.execute(postMethod);
			int res_code = http_response.getStatusLine().getStatusCode();
			AppUtils.displayLog("------------- Response Code--------------", ":"+ res_code);
			//Log.i("------------- Response Code--------------", ":"+ res_code);
			if (res_code == HTTP_OK) {
				response = EntityUtils.toString(http_response.getEntity(), "UTF-8");
			}else{
				response = EntityUtils.toString(http_response.getEntity(), "UTF-8");
				alert_dialog.setParamenters("Error!", "Sorry! server has encountered an error.Please try again.", null, null, 1, false);
				alert_dialog.setAlertImage(R.drawable.error,"error");
			}
			AppUtils.displayLog("------------- Response--------------", ":"+ response);
			//Log.i("------------- Response--------------", ":"+ response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			_baseactivity.insertDB();
			alert_dialog.setParamenters("Error", "Server is not reachable. Please check your internet connection.", null, null, 1, false);
			alert_dialog.setAlertImage(R.drawable.error,"error");
			//response = String.valueOf(res_code);
				AppUtils.displayLog("--------------IOException----", ":" + e.getMessage());

		}
		return response;
	}
	public  boolean isValidResponse(String response){
		try {
			Object obj = new JSONTokener(response).nextValue();
			
			if(obj instanceof JSONObject){
				JSONObject json_obj = new JSONObject(response);
				if(json_obj.has("error")){
					return false;
				}
				
			}else if(obj instanceof JSONArray){
				JSONArray json_array = new JSONArray(response);
				if(json_array.length() > 0){
					if(json_array.optJSONObject(0).has("errorCode")){
						return false;
					}
				}
				
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		return true;
	}
	
	
}
