package com.globalnest.network;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.security.KeyStore;

public class HttpClientClass {
	public static HttpClient getHttpClient(int timeout) {
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
	            schReg.register(new Scheme("https",  new TLSSocketFactory(), 443));
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
}
