package com.globalnest.scanattendee;

import java.text.ParseException;

import com.globalnest.utils.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class RefundHistoryActivity extends BaseActivity{

	
	TextView total_amt, no_refund;
	GridView refund_view;
	String whereClause="";
	Cursor refund_cursor;
	double refund_total_amt=0;
	RefundAdapter adapter;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		whereClause =  " where EventID = '" + checked_in_eventId+ "' order by RefundDate DESC";
		refund_cursor = Util.db.getRefundCursor(whereClause);
		
		whereClause =  " where EventID = '" + checked_in_eventId+ "' order by RefundDate DESC";
		//refund_total_amt = Util.db.getRefundAmount(whereClause);
//		//Log.i("---Refund Order Cursor Size---",":"+refund_cursor.getCount());
		setCustomContentView(R.layout.refund_history_layout);
		
		back_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 
				finish();
			}
		});
		
		 img_close.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// 
					whereClause =  " where EventID = '" + checked_in_eventId+ "' order by RefundDate DESC";
					refund_cursor = Util.db.getRefundCursor(whereClause);
					
					whereClause =  " where EventID = '" + checked_in_eventId+ "' order by RefundDate DESC";
					//refund_total_amt = Util.db.getRefundAmount(whereClause);
					//Log.i("---Refund Order Cursor Size---",":"+refund_cursor.getCount());
					refund_cursor = Util.db.getRefundCursor(whereClause);
					
					adapter = new RefundAdapter(RefundHistoryActivity.this, refund_cursor);
					refund_view.setAdapter(adapter);
					
					//refund_total_amt = Util.db.getRefundAmount(whereClause);
					
					total_amt.setText("Total Amount: " +Util.nf.format(refund_total_amt));

					if(refund_cursor.getCount() == 0){
						no_refund.setVisibility(View.VISIBLE);
						total_amt.setVisibility(View.GONE);
						img_search.setVisibility(View.GONE);
					}else{
						no_refund.setVisibility(View.GONE);
						total_amt.setVisibility(View.VISIBLE);
						img_search.setVisibility(View.VISIBLE);
					}
					
					back_layout.setVisibility(View.VISIBLE);
					search_layout.setVisibility(View.GONE);
				}
			});
		
		search_view.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 
				
				refund_total_amt=0;
				SortFunction(R.id.editsearchrecord);
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// 

			}

			@Override
			public void afterTextChanged(Editable s) {
				// 

			}
		});
		
	}
	
	@Override
	public void doRequest() {
		// 
		
	}

	@Override
	public void parseJsonResponse(String response) {
		// 
		
	}
	private void SortFunction(int id) {

		if (refund_cursor != null) {
			refund_cursor.close();
		}
		if (id == R.id.editsearchrecord) {
			whereClause =  " where EventID = '" + checked_in_eventId+ "' AND " + "RefundAttendeeName like '" + "%" + 
					search_view.getText().toString().toLowerCase() + "%" + "'" + " OR " + "RefundTicketName like '" +"%" 
							+ search_view.getText().toString().toLowerCase() + "%" + "'" +" OR " + "RefundItemName like '" +"%" 
							+ search_view.getText().toString().toLowerCase() + "%" + "'" +" COLLATE NOCASE order by RefundDate DESC";
		}
		
		refund_cursor = Util.db.getRefundCursor(whereClause);
		
		adapter = new RefundAdapter(this, refund_cursor);
		refund_view.setAdapter(adapter);
		
		//refund_total_amt = Util.db.getRefundAmount(whereClause);
		
		total_amt.setText("Total Amount: " +Util.nf.format(refund_total_amt));
		
		if(refund_cursor.getCount() == 0){
			no_refund.setVisibility(View.VISIBLE);
			total_amt.setVisibility(View.GONE);
			img_search.setVisibility(View.GONE);
		}else{
			no_refund.setVisibility(View.GONE);
			total_amt.setVisibility(View.VISIBLE);
			img_search.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void setCustomContentView(int layout) {
		// 
		View v = inflater.inflate(layout, null);

		linearview.addView(v);
		txt_title.setText("Refund History");
		img_setting.setVisibility(View.GONE);
		img_menu.setImageResource(R.drawable.back_button);
		event_layout.setVisibility(View.GONE); 
		button_layout.setVisibility(View.GONE);
		event_layout.setVisibility(View.VISIBLE);
		img_refund_history.setVisibility(View.GONE);
		
		
		no_refund  = (TextView)linearview.findViewById(R.id.norefundfound);
		total_amt = (TextView)linearview.findViewById(R.id.txtrefundtotal);
		refund_view = (GridView)linearview.findViewById(R.id.refundgridview);
		total_amt.setTypeface(Util.roboto_regular);
		no_refund.setTypeface(Util.roboto_regular);
		adapter = new RefundAdapter(this, refund_cursor);
		refund_view.setAdapter(adapter);
		total_amt.setText("Total Amount: " +Util.nf.format(refund_total_amt));
		
		if (refund_cursor != null) {
			if (refund_cursor.getCount() == 0) {
				no_refund.setVisibility(View.VISIBLE);
				total_amt.setVisibility(View.GONE);
				img_search.setVisibility(View.GONE);
			} else {
				no_refund.setVisibility(View.GONE);
				total_amt.setVisibility(View.VISIBLE);
				img_search.setVisibility(View.VISIBLE);
			}
		}
	
	}

	
	
	private class RefundAdapter extends CursorAdapter{
		TextView txt_pname, txt_orderid, txt_ticketid, txt_tname, txt_date;
		Button btn_price;
		FrameLayout refundline;
		@SuppressWarnings("deprecation")
		public RefundAdapter(Context context, Cursor c) {
			super(context, c);
		}
		@Override
		public void bindView(View v, Context arg1, Cursor c) {
			// 
			refundline  = (FrameLayout)v.findViewById(R.id.refundline);
			txt_pname = (TextView) v.findViewById(R.id.refundpersonName);
			txt_orderid = (TextView) v.findViewById(R.id.refundorderid);
			txt_ticketid = (TextView) v.findViewById(R.id.refundticketid);
			txt_tname = (TextView) v.findViewById(R.id.refundticketname);
			txt_date  = (TextView) v.findViewById(R.id.refunddate);
			btn_price = (Button) v.findViewById(R.id.refundamt);
	
			txt_pname.setTypeface(Util.roboto_regular);
			txt_orderid.setTypeface(Util.roboto_regular);
			txt_ticketid.setTypeface(Util.roboto_regular);
			txt_tname.setTypeface(Util.roboto_regular);
			txt_date.setTypeface(Util.roboto_regular);
			btn_price.setTypeface(Util.roboto_regular);
			
			
			txt_orderid.setText("Order Id : "+c.getString(c.getColumnIndex("RefundOrderID")));
			txt_pname.setText(Html.fromHtml("<b>"+c.getString(c.getColumnIndex("RefundAttendeeName"))+"</b>"));
			if(c.getString(c.getColumnIndex("RefundTicketName")).isEmpty())
				txt_tname.setText(c.getString(c.getColumnIndex("RefundItemName")));
			else
				txt_tname.setText(c.getString(c.getColumnIndex("RefundTicketName")));
			txt_ticketid.setText("Ticket Id : "+c.getString(c.getColumnIndex("RefundTicketNumber")));
			try {
				txt_date.setText(Util.db_date_format1.format(Util.db_date_format.parse(c.getString(c.getColumnIndex("RefundDate")))));
			} catch (ParseException e) {
				
				e.printStackTrace();
			}
			btn_price.setText(
			Util.nf.format(Double.parseDouble(c.getString(c.getColumnIndex("RefundAmount")))));
			
			if(c.getPosition() % 2 == 0){
			
				refundline.setBackgroundColor(getResources().getColor(R.color.green_button_color));
				btn_price.setBackgroundResource(R.drawable.green_price_bg);
			}else{
				refundline.setBackgroundColor(getResources().getColor(R.color.orange_bg));
				btn_price.setBackgroundResource(R.drawable.red_price_bg);
			}
			
		}

		@Override
		public View newView(Context arg0, Cursor arg1, ViewGroup parent) {
			// 
			View v = inflater.inflate(R.layout.refund_history_item_layout, parent,false);
			return v;
		}
		
	}



	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
		// 
		
	}
	
}
