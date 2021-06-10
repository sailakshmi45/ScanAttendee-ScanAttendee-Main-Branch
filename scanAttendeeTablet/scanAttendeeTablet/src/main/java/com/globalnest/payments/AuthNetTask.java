package com.globalnest.payments;

import android.app.ProgressDialog;
import android.os.StrictMode;

import com.globalnest.network.HttpClientClass;
import com.globalnest.network.SafeAsyncTask;
import com.globalnest.utils.AppUtils;
import com.globalnest.MainActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.HashMap;

@SuppressWarnings("deprecation")
public class AuthNetTask extends SafeAsyncTask<String>{
	private MainActivity _activity;
	private ProgressDialog _dialog;
	private HashMap<String, String> _values;
	private String _url;
	public AuthNetTask(String url,HashMap<String, String> values,MainActivity activity){
		this._values = values;
		this._activity = activity;
		this._url = url;
		this._dialog = new ProgressDialog(_activity);
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

		}
	}
	protected void onPreExecute() throws Exception{
		super.onPreExecute();
		_dialog.setMessage("Sending Payment...");
		_dialog.setCancelable(false);
		_dialog.show();
	}
	@Override
	public String call() throws Exception {
		// TODO Auto-generated method stub
		
		String responce = "";
		try {
			HttpClient httpClient = HttpClientClass.getHttpClient(30000);
			HttpPost httpPost = new HttpPost(_url);
			httpPost.setHeader(HTTP.CONTENT_TYPE,"application/x-www-form-urlencoded");
				//httpPost.addHeader("Content-Type", "text/plain");
			AppUtils.displayLog("----------------Values----------",":"+AppUtils.prepareRequest(_values));
				StringEntity entity = new StringEntity(AppUtils.prepareRequest(_values));
				httpPost.setEntity(entity);
			    HttpResponse httpResponse = httpClient.execute(httpPost);

			if(httpResponse.getStatusLine().getStatusCode() == 200){
				responce = EntityUtils.toString(httpResponse.getEntity());
			}else {
				responce = httpResponse.getStatusLine().getStatusCode()+"server error";
			}
		AppUtils.displayLog("------------- Response--------------", ":"+ responce);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			responce = "MalformedURLException";
			//Log.i("--------------MalformedURLException----",":"+e.getMessage());
		} catch (ProtocolException e) {
			e.printStackTrace();
			responce = "ProtocolException";
			//Log.i("--------------ProtocolException----",":"+e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			responce = "IOException";
			//Log.i("--------------IOException----",":"+e.getMessage());
		}  catch(Exception e){
			responce = "Server error";
			e.printStackTrace();
		}
		return responce;
	}

    protected void onSuccess(String response) throws Exception{
    	super.onSuccess(response);
    	 _dialog.dismiss();
    	 _activity.parseJsonResponse(response);
    }
    

	
}
