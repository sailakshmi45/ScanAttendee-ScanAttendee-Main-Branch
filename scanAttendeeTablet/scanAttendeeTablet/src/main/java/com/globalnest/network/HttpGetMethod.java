package com.globalnest.network;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.MainActivity;
import com.globalnest.scanattendee.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

@SuppressWarnings("deprecation")
public class HttpGetMethod extends AsyncTask<String, Void, String> {
	String url;
	MainActivity baseactivty;
	ProgressDialog dialog;
	String _access_token,_token_type;
	private AlertDialogCustom alert_dialog;
	public static DataLoadAsyncTask dataloading = null;
	public HttpGetMethod(String url,String token_type,String access_token, MainActivity baseactivty) { 
		this._access_token = access_token;
		this._token_type=token_type;
		this.url = url;
		this.baseactivty = baseactivty;
		this.dialog = new ProgressDialog(baseactivty);
		//dialog.setCancelable(false);
		this.alert_dialog = new AlertDialogCustom(this.baseactivty);
	}

	protected void onPreExecute() {
		super.onPreExecute();
		dialog.setMessage("Please wait...");
		dialog.setCancelable(true);
		dialog.show();
	}
	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		String response = GetMethod();
		return response;
	}

	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		dialog.dismiss();
		//baseactivty.parseJsonResponse(result);
		if (result != null) {
			if (url.contains(WebServiceUrls.SA_GETSESSIONCHECKINS)) {
				dataloading = new DataLoadAsyncTask(this.baseactivty,"Please wait....", result);
				dataloading.execute();
			} else {
				baseactivty.parseJsonResponse(result);
			}
		} else {
			alert_dialog.show();
		}
		/*if(result != null){
			dataloading = new DataLoadingTask(this.baseactivty, dialog, result);
			dataloading.execute();
			baseactivity.parseJsonResponse(result);
			
			//baseactivty.parseJsonResponse(result);
		}else{
			dialog.dismiss();
		}*/
		
	}

	private String GetMethod() {
		String response = "";
		try {
			HttpParams params = new BasicHttpParams();
			int timeoutconnection = 30000;
			HttpConnectionParams.setConnectionTimeout(params, timeoutconnection);
			int sockettimeout = 30000;
			HttpConnectionParams.setSoTimeout(params, sockettimeout);
			HttpClient _httpclient = HttpClientClass.getHttpClient(30000);
			HttpGet _httpget = new HttpGet(url);
			_httpget.addHeader("Authorization",_token_type+" " + _access_token);

			AppUtils.displayLog("-----HTTP GET URL------",":"+url);
			AppUtils.displayLog("-----OAUTH ACCESS TOKEN---",":"+_access_token);
			/*Log.i("-----HTTP GET URL------",":"+url);
		  	Log.i("-----OAUTH ACCESS TOKEN---",":"+_access_token);*/
			
			HttpResponse _response = _httpclient.execute(_httpget);
			int _responsecode =_response.getStatusLine().getStatusCode();

			AppUtils.displayLog("--------------Get Method Response Code-----------",":"+_responsecode);
			//Log.i("--------------Get Method Response Code-----------",":"+_responsecode);
			if (_responsecode == 200) {
				response = EntityUtils.toString(_response.getEntity(), "UTF-8");
			}
			AppUtils.displayLog("--------------Get Method Response-----------",":"+response);
			//Log.i("--------------Get Method Response-----------",":"+response);
		} catch (Exception e) {
			AppUtils.displayLog("--------------Get Method Exception-----------",":"+e.getMessage());
			//Log.i("--------------Get Method Exception-----------",":"+e.getMessage());
			alert_dialog.setParamenters("Error", "Server is not reachable. Please check your internet connection.", null, null, 1, false);
			alert_dialog.setAlertImage(R.drawable.error,"error");
		}
		return response;
	}
}
