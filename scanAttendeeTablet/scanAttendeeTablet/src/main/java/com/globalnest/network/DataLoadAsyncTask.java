package com.globalnest.network;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.globalnest.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class DataLoadAsyncTask extends AsyncTask<Void, Integer, Boolean> {

	private MainActivity _baseactivity;
	private ProgressDialog _dialog;
	private String response;
    public static int progress=0;
    public DataLoadAsyncTask(){
    	
    }
	public DataLoadAsyncTask(MainActivity baseactivity, String message, String resposne) {
		// TODO Auto-generated constructor stub
		this._dialog = new ProgressDialog(baseactivity);
		this._dialog.setMessage(message);
		this._dialog.setCancelable(false);
		this.response = resposne;
		this._baseactivity = baseactivity;

	}

	protected void onPreExecute(){
		super.onPreExecute();
		_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		setBarSetMaxValue();
		_dialog.setCancelable(false);
		_dialog.setProgress(0);
		_dialog.show();
	}
	@Override
	protected Boolean doInBackground(Void... params) {
		// TODO Auto-generated method stub
		_baseactivity.parseJsonResponse(response);
		return true;
	}

	public void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		_dialog.setProgress(values[0]);
	}

	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		////Log.i("--------------Data Loading On success----------",":"+response);
		try {
			_baseactivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(_dialog!=null &_dialog.isShowing())
					_dialog.dismiss();
				}
			});
		}catch (Exception e){
			if(_dialog!=null &_dialog.isShowing())
				_dialog.dismiss();
			e.printStackTrace();
		}
	}
	
	private void setBarSetMaxValue() {
		try {
			JSONObject res_obj = new JSONObject(response);
			if(res_obj.has("Events")){
				JSONArray array = res_obj.optJSONArray("Events");
				//LoginResponse login_response = new Gson().fromJson(response,LoginResponse.class);
				_dialog.setMax(array.length());
			}else if(res_obj.has("TotalLists")){
				JSONArray array = res_obj.optJSONArray("TotalLists");
				_dialog.setMax(array.length());
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			try {
				JSONArray json_array = new JSONArray(response);
				_dialog.setMax(json_array.length());
			} catch (Exception e2) {
				// TODO: handle exception
			}
			
		}
		
	}
	
	
}
