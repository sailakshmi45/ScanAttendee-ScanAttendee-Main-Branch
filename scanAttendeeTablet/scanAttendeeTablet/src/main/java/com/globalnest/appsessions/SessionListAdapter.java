package com.globalnest.appsessions;

import java.util.ArrayList;
import java.util.List;

import com.globalnest.scanattendee.EventListActivity;
import com.globalnest.scanattendee.R;
import com.globalnest.utils.Util;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SessionListAdapter extends BaseAdapter {

	private List<DeviceSessionId> other_session_ids = new ArrayList<DeviceSessionId>();
	private Activity mContext;
	private Dialog alert_dialog;
	
	public SessionListAdapter(List<DeviceSessionId> other_session_ids, Activity context,Dialog alert_dialog) {
		this.other_session_ids = other_session_ids;
		this.mContext = context;
		this.alert_dialog = alert_dialog;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return other_session_ids.size();
	}

	@Override
	public DeviceSessionId getItem(int pos) {
		// TODO Auto-generated method stub
		return other_session_ids.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		// TODO Auto-generated method stub
		return pos;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewgroup) {
		
			view = LayoutInflater.from(mContext).inflate(R.layout.session_list_item, null);
			
			View view_line = (View) view.findViewById(R.id.view_line);
			TextView txt_deviceType = (TextView) view
					.findViewById(R.id.txt_deviceType);
			TextView txt_lastAccessed = (TextView) view
					.findViewById(R.id.txt_lastAccessed);
			TextView txt_status = (TextView) view.findViewById(R.id.txt_status);
			final ImageView img_delete=(ImageView) view.findViewById(R.id.img_delete);
			txt_deviceType.setText(getItem(position).DeviceType__c);
			txt_lastAccessed.setText(Util.sessionDateTimeFormat(getItem(position).LastModifiedDate));
			txt_status.setText(getItem(position).Status__c);
			

			if ((position+1) % 2 == 0) {
				view_line.setBackgroundColor(mContext.getResources().getColor(R.color.green_button_color));//;Color(R.color.green_button_color);
			} else {
				view_line.setBackgroundColor(mContext.getResources().getColor(R.color.orange_bg));
			}
			img_delete.setFocusable(false);
			img_delete.setTag(position);
			img_delete.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//Toast.makeText(mContext, "Session Removed", Toast.LENGTH_LONG).show();
					//((BaseActivity) mContext).deleteSessionsFromServer(mContext,mResponse.sessionsTocancel.get((Integer) img_delete.getTag()),mMainResponse, mListener);
					//alert_dialog.dismiss();
					((EventListActivity) mContext).requestForKillSession((Integer) img_delete.getTag());
				}
			});
			
			return view;
	}
}
