package com.globalnest.payments;

import android.app.ProgressDialog;
import android.os.StrictMode;

import com.globalnest.network.HttpClientClass;
import com.globalnest.network.SafeAsyncTask;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.trustmanager.EasySSLSocketFactory;
import com.globalnest.utils.AppUtils;
import com.globalnest.MainActivity;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PaypalDirectTask extends SafeAsyncTask<String>{

	private MainActivity _activity;
	private ProgressDialog _dialog;
	private HashMap<String, String> _values;
	private String _url;
	public PaypalDirectTask(String url,HashMap<String, String> values,MainActivity activity){
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
		return executeRequestNew();
	}

	protected void onSuccess(String result) throws Exception{
		super.onSuccess(result);
		_dialog.dismiss();
		_activity.parseJsonResponse(result);
	}



	private List<NameValuePair> getValuePairs(HashMap<String, String> values){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for(String key:values.keySet()){
			if(!key.equalsIgnoreCase(WebServiceUrls.WEBSERVICE_CODE)){
				params.add(new BasicNameValuePair(key, values.get(key)));
			}
		}
		return params;
	}

	private static HttpClient getHttpClient(int timeout) {
		HttpClient mHttpClient=null;
		if (null == mHttpClient) {

			try {
				KeyStore trustStore = KeyStore.getInstance(KeyStore
						.getDefaultType());
				trustStore.load(null, null);
				//SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
				//sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

				HttpParams params = new BasicHttpParams();

				HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
				HttpProtocolParams.setContentCharset(params,
						HTTP.DEFAULT_CONTENT_CHARSET);
				HttpProtocolParams.setUseExpectContinue(params, true);


				ConnManagerParams.setTimeout(params, timeout);

				HttpConnectionParams.setConnectionTimeout(params, timeout);

				HttpConnectionParams.setSoTimeout(params, timeout);


				SchemeRegistry schReg = new SchemeRegistry();
				schReg.register(new Scheme("http", PlainSocketFactory
						.getSocketFactory(), 80));
				schReg.register(new Scheme("https", new EasySSLSocketFactory(), 443));
				ClientConnectionManager conManager = new ThreadSafeClientConnManager(
						params, schReg);
				mHttpClient = new DefaultHttpClient(conManager, params);
			} catch (Exception e) {
				e.printStackTrace();
				return new DefaultHttpClient();
			}
		}
		return mHttpClient;
	}
	
	/*private String executeRequest()
	{
	    String responce = null;
	    int error = 0;
	    try {

	    	//_values.add(new BasicNameValuePair("timestamp", String.valueOf(SystemClock.currentThreadTimeMillis())));
	    	
	    	URL url = new URL(_url);
	    	////Log.i("---------------------Url--------------",":"+url.toString());
	    	//HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    	
	    	HttpsURLConnection conn = NetCipher.getHttpsURLConnection(url);
	    	conn.setReadTimeout(30000);
	    	conn.setConnectTimeout(30000);
	    	conn.setRequestMethod("POST");
	    	conn.setDoInput(true);
	    	conn.setDoOutput(true);
	    	conn.addRequestProperty(HTTP.CONTENT_TYPE,"application/x-www-form-urlencoded");
	    	OutputStream os = conn.getOutputStream();
	    	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
	    	////Log.i("---------------------Values--------------",":"+getValuePairs(_values));
	    	writer.write(getQuery(getValuePairs(_values)));
	    	
	    	writer.flush();
	    	writer.close();
	    	os.close();

	    	conn.connect();
	    	error = conn.getResponseCode();
	    	if(error == 200){
	    		InputStream content = (InputStream) conn.getContent();
	            responce = IOUtils.toString(content);
	           
	    	}
          conn.disconnect();
	    } catch (Exception e) {
	    	//Log.i("-------------Exception----",":"+e.getMessage());
	        responce = NullChecker(e.getMessage());
	        //Log.i("--------------Exception In POst Method----",":"+e.getMessage());
	        if(responce.contains("failed to connect to www.boothleads.com")){
	        	executeRequest();
	        }
	        
	    }

	    return responce;
	}*/


	private String executeRequestNew()
	{
		String response = null;

		try {
			AppUtils.displayLog("---------------Url--------------", ":"+ _url);
			HttpClient client = HttpClientClass.getHttpClient(120000);
			HttpPost postMethod = new HttpPost(_url);
			AppUtils.displayLog("---------------Values--------------", ":"+ getValuePairs(_values));
			postMethod.setHeader(HTTP.CONTENT_TYPE,"application/x-www-form-urlencoded");
			postMethod.setEntity(new UrlEncodedFormEntity(getValuePairs(_values)));
			HttpResponse http_response = client.execute(postMethod);

			int res_code = http_response.getStatusLine().getStatusCode();
			AppUtils.displayLog("------------- Response Code--------------", ":"+ res_code);
			if (res_code == 200) {
				response = EntityUtils.toString(http_response.getEntity());
			}else{
				response = EntityUtils.toString(http_response.getEntity());
			}
			AppUtils.displayLog("------------- Response--------------", ":"+ response);
		} catch (Exception e) {
			AppUtils.displayLog("----------------Expetion Paypal-------------",":"+e.getMessage());
		}

		return response;
	}

	private String getQuery(List<NameValuePair> params)
	{
		StringBuilder result = new StringBuilder();
		try {
			boolean first = true;

			for (NameValuePair pair : params) {
				if (first)
					first = false;
				else
					result.append("&");

				result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
				result.append("=");
				result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result.toString();
	}



}
