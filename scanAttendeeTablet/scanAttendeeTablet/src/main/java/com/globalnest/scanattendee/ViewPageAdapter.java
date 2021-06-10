package com.globalnest.scanattendee;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.globalnest.classes.RoundedImageView;
import com.globalnest.objects.Event;
import com.globalnest.objects.EventObjects;
import com.globalnest.utils.Util;
import com.squareup.picasso.Picasso;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewPageAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	private List<EventObjects> event_list_data;

	Context context;
	ImageView img_more, img_checkedin, imgeventshare;
	TextView txt_eventName, txt_eventLoc, txt_event_address, txt_event_state, txt_event_day, txt_event_desc,
			txt_txtenddate, txt_time_zone_name;
	FrameLayout layout_address, event_name_layout, event_end_line, frame_desc;

	public ViewPageAdapter(LayoutInflater inflater, List<EventObjects> events_list, Context context) {
		super();
		this.inflater = inflater;
		this.event_list_data = events_list;
		this.context = context;
		// inflater = (LayoutInflater)
		// activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {

		return event_list_data.size();
	}

	@Override
	public Event getItem(int position) {

		return event_list_data.get(position).Events;
	}

	@Override
	public long getItemId(int position) {

		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		String event_city = "";
		String event_address = "";
		View v = inflater.inflate(R.layout.event_list_item_layout, null);
		layout_address = (FrameLayout) v.findViewById(R.id.addresslayout);
		event_name_layout = (FrameLayout) v.findViewById(R.id.eventnamelayout);
		event_end_line = (FrameLayout) v.findViewById(R.id.eventendline);
		frame_desc = (FrameLayout) v.findViewById(R.id.frame_desc);
		final ImageView event_img = (ImageView) v.findViewById(R.id.imgeventbg);
		img_more = (ImageView) v.findViewById(R.id.imgeventmore);
		imgeventshare = (ImageView) v.findViewById(R.id.imgeventshare);
		img_checkedin = (ImageView) v.findViewById(R.id.imgcheckedin);
		txt_eventName = (TextView) v.findViewById(R.id.txteventname);
		txt_eventLoc = (TextView) v.findViewById(R.id.eventloc);
		txt_event_address = (TextView) v.findViewById(R.id.eventaddress);
		txt_event_state = (TextView) v.findViewById(R.id.eventcitystate);
		txt_txtenddate = (TextView) v.findViewById(R.id.txtenddate);
		txt_event_day = (TextView) v.findViewById(R.id.txtstartdate);
		txt_event_desc = (TextView) v.findViewById(R.id.txteventdesc);
		txt_time_zone_name = (TextView) v.findViewById(R.id.txt_time_zone_name);

		txt_eventName.setTypeface(Util.roboto_bold);
		txt_eventLoc.setTypeface(Util.roboto_regular);
		txt_event_address.setTypeface(Util.roboto_regular);
		txt_event_state.setTypeface(Util.roboto_regular);
		txt_time_zone_name.setTypeface(Util.roboto_regular);
		txt_event_desc.setTypeface(Util.roboto_regular);

		String time_zone = event_list_data.get(position).Events.Time_Zone__c;
		TimeZone tz = TimeZone.getTimeZone(time_zone);
		txt_time_zone_name.setText("Timezone: "+tz.getDisplayName(true, TimeZone.SHORT, Locale.getDefault()));
		if (!event_list_data.get(position).image.isEmpty()) {
			Picasso.with(context).load(event_list_data.get(position).image).placeholder(R.drawable.default_image)
					.error(R.drawable.default_image).into(event_img);
		} else {
			event_img.setVisibility(View.GONE);
		}
		/*
		 * if (!event_list_data.get(position).image.isEmpty()) {
		 * imageloder.displayImage(event_list_data.get(position).image,
		 * event_img, options, animateFirstListener); }
		 */
		if (!event_list_data.get(position).Events.Description__c.isEmpty()) {
			txt_event_desc.setText(event_list_data.get(position).Events.Description__c);
		} else {
			txt_event_desc.setVisibility(View.GONE);
		}

		txt_eventName.setText(event_list_data.get(position).Events.Name);
		if (!event_list_data.get(position).Events.Start_Date__c.isEmpty()) {

			try {
				/*
				 * txt_event_day.setText(Util.date_format
				 * .format(Util.db_server_format.parse(event_list_data.get(
				 * position).Events.Start_Date__c)) + " at " +
				 * event_list_data.get(position).Events.Start_Time__c);
				 */

				String event_start_date = Util.change_US_ONLY_DateFormat(
						event_list_data.get(position).Events.Start_Date__c,
						event_list_data.get(position).Events.Time_Zone__c);
				String event_end_date = Util.change_US_ONLY_DateFormat(event_list_data.get(position).Events.End_Date__c,
						event_list_data.get(position).Events.Time_Zone__c);

				txt_event_day.setText(event_start_date);
				/*
				 * txt_txtenddate.setText(Util.date_format
				 * .format(Util.db_server_format.parse(event_list_data.get(
				 * position).Events.End_Date__c)) + " at " +
				 * event_list_data.get(position).Events.End_Time__c);
				 */
				txt_txtenddate.setText(event_end_date);

			} catch (Exception e) {
				// TODO: handle exception
				// Log.i("***********************____________Event Date
				// Exception___________",":"+e.getMessage());
			}

		}
		if (!event_list_data.get(position).Events.Street1__c.isEmpty()
				&& !event_list_data.get(position).Events.Venue_Name__c.isEmpty()) {
			if (!event_list_data.get(position).Events.Street1__c.isEmpty()) {
				event_address = event_address + event_list_data.get(position).Events.Street1__c + "";
			} else if (!event_list_data.get(position).Events.Venue_Name__c.isEmpty()) {
				event_address = event_address + ", " + event_list_data.get(position).Events.Venue_Name__c + "";
			}
			txt_event_address.setText(event_address);
		} else {
			txt_event_address.setVisibility(View.GONE);
		}

		/*
		 * if (!event_list_data.get(position).Events.Street1__c.isEmpty()) {
		 * txt_event_address.setVisibility(View.VISIBLE);
		 * txt_event_address.setText(event_list_data.get(position).Events.
		 * Street1__c); }else{ txt_event_address.setVisibility(View.GONE); }
		 * 
		 * if (!event_list_data.get(position).Events.Venue_Name__c.isEmpty()) {
		 * txt_eventLoc.setVisibility(View.VISIBLE); txt_eventLoc
		 * .setText(event_list_data.get(position).Events.Venue_Name__c); }else{
		 * txt_eventLoc.setVisibility(View.GONE); }
		 */
		if (!event_list_data.get(position).Events.Venue_Name__c.isEmpty()) {
			txt_eventLoc.setVisibility(View.VISIBLE);
			txt_eventLoc.setText(event_list_data.get(position).Events.Venue_Name__c);
		} else {
			txt_eventLoc.setVisibility(View.GONE);
		}

		// Log.i("--------------State Short
		// Name---------",":"+event_list_data.get(position).Events.BLN_State__c);
		if (!event_list_data.get(position).Events.City__c.isEmpty() && !event_list_data.get(position).state.isEmpty()) {
			// layout_address.setVisibility(View.VISIBLE);

			/*
			 * event_city = event_list_data.get(position).Events.City__c + ", "
			 * + event_list_data.get(position).state;
			 */
			event_city = event_list_data.get(position).Events.City__c + ", "
					+ Util.db.getStateName(event_list_data.get(position).Events.BLN_State__c);

		} else if (event_list_data.get(position).Events.City__c.isEmpty()
				&& !event_list_data.get(position).state.isEmpty()) {
			// txteventcity.setVisibility(View.VISIBLE);
			// event_city = event_list_data.get(position).state;
			event_city = Util.db.getStateName(event_list_data.get(position).Events.BLN_State__c);

		} else if (!event_list_data.get(position).Events.City__c.isEmpty()
				&& event_list_data.get(position).state.isEmpty()) {
			// txteventcity.setVisibility(View.VISIBLE);
			event_city = event_list_data.get(position).Events.City__c;
		}

		if (!event_list_data.get(position).Events.ZipCode__c.isEmpty()) {

			event_city = event_city + " " + event_list_data.get(position).Events.ZipCode__c;
		}
		if (!event_city.isEmpty()) {

			txt_event_state.setVisibility(View.VISIBLE);
			txt_event_state.setText(event_city);
		} else {
			txt_event_state.setVisibility(View.GONE);
		}

		if (event_list_data.get(position).Events.Street1__c.isEmpty()
				&& event_list_data.get(position).Events.Venue_Name__c.isEmpty() && event_city.isEmpty()) {
			layout_address.setVisibility(View.GONE);
		} else {
			layout_address.setVisibility(View.VISIBLE);
		}

		if (event_list_data.get(position).Events.Street1__c.isEmpty()
				&& event_list_data.get(position).Events.Venue_Name__c.isEmpty() && event_city.isEmpty()) {
			layout_address.setVisibility(View.GONE);
		}

		if (((BaseActivity) context).checked_in_eventId.equals(event_list_data.get(position).Events.Id))
			img_checkedin.setVisibility(View.VISIBLE);
		else
			img_checkedin.setVisibility(View.GONE);

		imgeventshare.setFocusable(false);
		imgeventshare.setFocusableInTouchMode(false);
		img_more.setFocusable(false);
		img_more.setFocusableInTouchMode(false);

		imgeventshare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent i = new Intent(context, SocialMedia.class);
				i.putExtra(Util.ADDED_EVENT_ID, event_list_data.get(position).Events.Id);
				context.startActivity(i);
			}
		});

		final String selected_event_id = event_list_data.get(position).Events.Id;
		final String selected_event_name = event_list_data.get(position).Events.Name;
		final String selected_event_pic = event_list_data.get(position).image;
		final String selected_event_role = event_list_data.get(position).Events.roles;

		img_more.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (isAdmin(selected_event_role)) {

					BitmapDrawable bmdr = (BitmapDrawable) event_img.getDrawable();
					Bitmap edit_event_logo = bmdr.getBitmap();
					openPopup();
				} else {
					((EventListActivity) context).showEventAlert(event_list_data.get(position).Events.Name);
				}

				// popupwindow.showAsDropDown(v);
			}

			private void openPopup() {
				final Dialog add_key_dialog;
				add_key_dialog = new Dialog(context, R.style.MyCustomTheme);
				add_key_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				add_key_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
				add_key_dialog.setCancelable(true);
				add_key_dialog.setContentView(R.layout.event_list_popup);

				Button edit = (Button) add_key_dialog.findViewById(R.id.btn_eventedit);
				Button delete = (Button) add_key_dialog.findViewById(R.id.btn_eventdelete);
				Button cancle = (Button) add_key_dialog.findViewById(R.id.btn_eventcancle);
				TextView eventName = (TextView) add_key_dialog.findViewById(R.id.txt_event);
				RoundedImageView eventpic = (RoundedImageView) add_key_dialog.findViewById(R.id.img_eventpic);

				eventName.setText(selected_event_name);
				Picasso.with(context).load(selected_event_pic).placeholder(R.drawable.default_image)
						.error(R.drawable.default_image).into(eventpic);
				edit.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						Intent i = new Intent(context, AddEventActivity.class);
						i.putExtra(Util.EVENT_ACTION, AddEventActivity.EDIT_EVENT);
						i.putExtra(Util.EVENT_ID, selected_event_id);
						context.startActivity(i);
						add_key_dialog.dismiss();
					}
				});

				delete.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						customAskDialog(selected_event_name,
								"Would you like to delete the event? All your data will be lost and can not be recovered.");
						add_key_dialog.dismiss();
					}

					public void customAskDialog(String title, String message) {

						AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
						//alertDialog.setIcon(android.R.drawable.ic_dialog_info);
						alertDialog.setTitle(title);
						alertDialog.setMessage(message);
						alertDialog.setInverseBackgroundForced(true);
						alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						});
						alertDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

								if (((BaseActivity) context).isOnline()) {
									/*if (((EventListActivity) context).checked_in_eventId.equalsIgnoreCase(selected_event_id)) {
										Util.clearSharedPreference(Util.eventPrefer);
										((EventListActivity) context).checked_in_eventId = "";
									}*/
									((EventListActivity) context).selected_event_id = selected_event_id;
									((EventListActivity) context).requestType = "Delete";
									((EventListActivity) context).doRequest();

								} else {
									((EventListActivity) context).startErrorAnimation(
											context.getResources().getString(R.string.network_error),
											((EventListActivity) context).txt_error_msg);

								}
							}

						});

						Dialog d = alertDialog.show();

						int textViewId = d.getContext().getResources().getIdentifier("android:id/alertTitle", null,
								null);
						TextView tv = (TextView) d.findViewById(textViewId);
						tv.setTextColor(context.getResources().getColor(R.color.blue_text_color));
						int dividerId = d.getContext().getResources().getIdentifier("android:id/titleDivider", null,
								null);
						View divider = d.findViewById(dividerId);
						divider.setBackgroundColor(context.getResources().getColor(R.color.blue_text_color));
					}
				});

				cancle.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						add_key_dialog.dismiss();
					}
				});

				add_key_dialog.show();
			}

		});

		return v;
	}

	public static boolean isAdmin(String role) {
		if (BaseActivity.NullChecker(role).equalsIgnoreCase("Attendee")) {
			return false;
		} else {
			return true;
		}

	}
}
