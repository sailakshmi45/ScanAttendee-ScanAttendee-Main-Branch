//  ScanAttendee Android
//  Created by Ajay on Oct 13, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.scanattendee;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.globalnest.classes.QRCodeEncoder;
import com.globalnest.data.Contents;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.BadgeCreation;
import com.globalnest.mvc.BadgeDataNew;
import com.globalnest.mvc.BadgeResponseNew;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.EventObjects;
import com.globalnest.printer.GetZebraPrinterConfigTask;
import com.globalnest.printer.PrinterDetails;
import com.globalnest.printer.ZebraPrinter;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.PullToRefreshListView;
import com.globalnest.utils.PullToRefreshListView.OnRefreshListener;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author laxmanamurthy
 *
 */
public class BadgeTemplateNewActivity extends BaseActivity {
    PullToRefreshListView badge_listView;
    LayoutParams lp;
    LinearLayout loadbadges, linear_badge;
    FrameLayout badgelayout, linear_badge_parent;;
    String whereClause = "", badgeId = "", server_start_date = "", server_end_date = "", requestType = "";
    BadgeAdapter _badgeadapter;
    HttpPostData postMethod;
    EventObjects event_data = new EventObjects();
    List<BadgeResponseNew> badges;
    BadgeCreation badge_creator;

    // ArrayList<LinearLayout> badges_layout_list = new
    // ArrayList<LinearLayout>();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setCustomContentView(R.layout.badge_template_layout);

            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();

            width = display.getWidth();// point.x;
            height = display.getHeight();

            badge_creator = new BadgeCreation(this, width, height);
            event_data = Util.db.getSelectedEventRecord(checked_in_eventId);
            if (!NullChecker(event_data.Events.Mobile_Default_Badge__c).isEmpty()) {
                Util.db.updateBadgeStatus(event_data.Events.Mobile_Default_Badge__c, " Where " + DBFeilds.BADGE_NEW_EVENT_ID + " = '" + checked_in_eventId + "'");
            }

            badges = Util.db.getAllBadges(" where " + DBFeilds.BADGE_NEW_EVENT_ID + "='" + checked_in_eventId + "'");
            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/ScanAttendee/.BadgeSamples");
            if (!dir.exists() && dir != null && badges.size() > 0) {
                createTemplates();
            }
            _badgeadapter = new BadgeAdapter();
            badge_listView.setAdapter(_badgeadapter);
            if (AppUtils.isStoragePermissionGranted(BadgeTemplateNewActivity.this)) {
                if (isOnline() && badges.size() == 0)//&&badges.size()==0
                    doRequest();
			/*else {
				startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
			}*/
            } else {
                AppUtils.giveStoragermission(BadgeTemplateNewActivity.this);
            }

            //createTemplates();
            img_setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestType = "";
                    doRequest();
                }
            });
		/*if (badges.size()==0) {

		}*/

            back_layout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    /*
                     * Intent i = new Intent(BadgeTemplateNewActivity.this,
                     * AttendeeListActivity.class); i.putExtra("Badge_Res",
                     * badges.get(0)); i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     * startActivity(i);
                     */
                    finish();
                }
            });

            badge_listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //requestType= Util.UPDATE_BADGE;

                    //print_template.clear();
				/*Cursor c1 = _badgeadapter.getCursor();
				c1.moveToPosition(position);
			
				print_template.put(c1.getString(c1.getColumnIndex("BadgeName")), true);*/
                    badgeId = badges.get(position).badge.Id;
                    String where = " Where " + DBFeilds.BADGE_NEW_EVENT_ID + " = '" + checked_in_eventId + "'";
                    Util.db.updateBadgeStatus(badgeId, where);
                    Util.db.updateEventBadgeName(badgeId, checked_in_eventId);
//				txt_title.setText("Select Template"+"("+badges.size()+")");
//				badges.clear();
//				badges = Util.db.getAllBadges(where);
                    checkedin_event_record = Util.db.getSelectedEventRecord(checked_in_eventId);
                    _badgeadapter.notifyDataSetChanged();
                    sharedPreferences.edit().clear().commit();

                    if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equals("Zebra")) {
                        //new GetZebraPrinterConfigTask(BadgeTemplateNewActivity.this).execute();
                        //Toast.makeText(this,"Badge Template Successfully Updated!",Toast.LENGTH_LONG).show();
                    } else {
                        AlertDialogCustom dialog = new AlertDialogCustom(BadgeTemplateNewActivity.this);
                        dialog.setParamenters("Alert", "Badge Template Successfully Updated!", null, null, 1, true);
                        dialog.show();
                    }
				/*
				if(isOnline())
				  doRequest();
				else
					startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);*/

                }
            });

            badge_listView.setOnRefreshListener(new OnRefreshListener() {

                @Override
                public void onRefresh() {
                    // TODO Auto-generated method stub
                    requestType = "";
                    doRequest();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            createTemplates();
        }
    }

    protected void onResume() {
        super.onResume();
		/*ZebraPrinter.tempZebraPrintWidth=0;
		ZebraPrinter.tempZebraLabelLength=0;*/
    }

    public void onPause() {
        super.onPause();

    }



    /*
     * (non-Javadoc)
     *
     * @see com.globalnest.network.IPostResponse#doRequest()
     */
    @Override
    public void doRequest() {
        // TODO Auto-generated method stub
        String access_token = sfdcddetails.token_type + " " + sfdcddetails.access_token;

        if (requestType.equals(Util.UPDATE_BADGE)) {

            String _url = sfdcddetails.instance_url + WebServiceUrls.SA_EVENT_SETTING + "EventId=" + checked_in_eventId
                    + "&BadgeId=" + badgeId;
            postMethod = new HttpPostData("Setting Badge...", _url, null, access_token, BadgeTemplateNewActivity.this);
            postMethod.execute();
            // BLN_ASC_EventSettings?BadgeName=%@&EventId=
        } else {

            String _url = sfdcddetails.instance_url + WebServiceUrls.SA_GET_BADGE_TEMPLATE_NEW + "Event_Id="+ checked_in_eventId;
            postMethod = new HttpPostData("Loading Badges...", _url, null, access_token, BadgeTemplateNewActivity.this);
            postMethod.execute();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
     */
    @Override
    public void parseJsonResponse(String response) {
        // TODO Auto-generated method stub
        if(badge_listView.isRefreshing()){
            badge_listView.onRefreshComplete();
        }

        loadbadges.setVisibility(View.GONE);
        try {
            if(!isValidResponse(response)){
                openSessionExpireAlert(errorMessage(response));
                return;
            }
            gson = new Gson();
            if(requestType.equals(Util.UPDATE_BADGE))
            {
                String where=" Where "+DBFeilds.BADGE_NEW_EVENT_ID+" = '" + checked_in_eventId +"'";
                Util.db.updateBadgeStatus(badgeId, where);
                Util.db.updateEventBadgeName(badgeId, checked_in_eventId);
                txt_title.setText("Select Template"+"("+badges.size()+")");
                badges.clear();
                badges = Util.db.getAllBadges(where);
                checkedin_event_record = Util.db.getSelectedEventRecord(checked_in_eventId);
                _badgeadapter.notifyDataSetChanged();
                sharedPreferences.edit().clear().commit();

                if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER,"").equals("Zebra")) {
                    //new GetZebraPrinterConfigTask(this).execute();
                    Toast.makeText(this,"Badge Template Successfully Updated!",Toast.LENGTH_LONG).show();
                }else{
                    AlertDialogCustom dialog=new AlertDialogCustom(BadgeTemplateNewActivity.this);
                    dialog.setParamenters("Alert","Badge Template Successfully Updated!", null, null, 1, true);
                    dialog.show();
                }
            }else{

                Type listType = new TypeToken<List<BadgeResponseNew>>() {}.getType();
                badges = (List<BadgeResponseNew>) gson.fromJson(response, listType);
                //Log.i("---------------- parseJsonResponse Badge Size----------", ":" + badges.size());
                Util.db.deleteBadges(checked_in_eventId);
                for(BadgeResponseNew badge : badges){
                    badge.badge.event_id = checked_in_eventId;
                    Util.db.InsertAndUpdateBadgeTemplateNew(badge);
                }
                txt_title.setText("Select Template"+"("+badges.size()+")");
                badges.clear();
                sharedPreferences.edit().clear().commit();
                String where=" Where "+DBFeilds.BADGE_NEW_EVENT_ID+" = '" + checked_in_eventId +"'";
                badges = Util.db.getAllBadges(where);
                createTemplates();
            }
            // new BadgesCreationTask().execute();

            // createTemplates();
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
            //Log.i("----------------Exception----------", ":" + e.getMessage());
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see com.globalnest.network.IPostResponse#insertDB()
     */
    @Override
    public void insertDB() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see com.globalnest.scanattendee.BaseActivity#setCustomContentView(int)
     */
    @Override
    public void setCustomContentView(int layout) {
        // TODO Auto-generated method stub
        activity =this;
        View v = inflater.inflate(layout, null);
        linearview.addView(v);
        txt_title.setText("Select Template");
        img_setting.setVisibility(View.VISIBLE);
        img_setting.setImageResource(R.drawable.dashboardrefresh);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        img_menu.setImageResource(R.drawable.back_button);
        event_layout.setVisibility(View.GONE);
        button_layout.setVisibility(View.GONE);
        event_layout.setVisibility(View.VISIBLE);
        loadbadges = (LinearLayout) linearview.findViewById(R.id.loadbadges);
        badge_listView = (PullToRefreshListView) linearview.findViewById(R.id.badgelistview);
        linear_badge_parent = (FrameLayout) linearview.findViewById(R.id.linear_badge_parent);

    }

    public class BadgeAdapter extends BaseAdapter {

        // LinearLayout linear_badge;

        /*
         * (non-Javadoc)
         *
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return badges.size();
        }

        /*
         * (non-Javadoc)
         *
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public BadgeResponseNew getItem(int position) {
            // TODO Auto-generated method stub
            return badges.get(position);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.widget.Adapter#getView(int, android.view.View,
         * android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View v = inflater.inflate(R.layout.badge_template_item, null);
            // FrameLayout badge_layout = (FrameLayout)
            // v.findViewById(R.id.badgelayout);

            // ;badge_layout.setVisibility(View.INVISIBLE);
            // holder.img_slected = (ImageView) v.findViewById(R.id.imgcheck1);
            TextView txt_badgename = (TextView) v.findViewById(R.id.txttemplatename);
            txt_badgename.setTypeface(Util.roboto_regular);
            ImageView img_slected = (ImageView) v.findViewById(R.id.badgeselected);

            img_slected.setVisibility(View.GONE);
            ImageView img_badge = (ImageView) v.findViewById(R.id.badge_image);
            BadgeResponseNew badgeres = getItem(position);
            //Log.i("----------------Badge Is Selected------------",":"+(checkedin_event_record.Events.badge_name.equalsIgnoreCase(badgeres.badge.Id)));
            if(checkedin_event_record.Events.Mobile_Default_Badge__c.equalsIgnoreCase(badgeres.badge.Id)){
                img_slected.setVisibility(View.VISIBLE);
            }
            BadgeDataNew badge_data = new Gson().fromJson(badgeres.badge.Data__c, BadgeDataNew.class);
            txt_badgename.setText(badgeres.badge.Description__c+" ("+badge_data.canvasWidth+" x "+badge_data.canvasHeight+")");
            // //Log.i("-----------------Badge Data At
            // Position---------",":"+position);

            // badges_layout_list.add(linear_badge);
            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/ScanAttendee/.BadgeSamples");
            String file_path = dir.toString() + "/" + badgeres.badge.Id + badgeres.badge.Name + ".png";
            Bitmap b = BitmapFactory.decodeFile(file_path);
            //Log.i("----------------Getting Bitmap------------",":"+b);
            img_badge.setImageBitmap(b);
            return v;
        }


    }

    public Bitmap encodeQrCode(String data) {

        Bitmap b = null;
        try {
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3 / 4;
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(data, null, Contents.Type.TEXT,
                    BarcodeFormat.QR_CODE.toString(), smallerDimension);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            try {
                b = qrCodeEncoder.encodeAsBitmap();
            } catch (WriterException e) {

                e.printStackTrace();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
        return b;
    }

    private boolean createTemplates() {
        // TODO Auto-generated method stub

        ArrayList<LinearLayout> badge_layouts_list = new ArrayList<LinearLayout>();

        for (final BadgeResponseNew badge_res : badges) {

            final View v = inflater.inflate(R.layout.badge_sample_layout, null);
            linear_badge = (LinearLayout) v.findViewById(R.id.linear_badge);
            badgelayout = (FrameLayout) v.findViewById(R.id.badgelayout);
            linear_badge.setVisibility(View.INVISIBLE);
            try {
                badge_creator.createBadgeTemplate(badge_res, badgelayout,null,false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            linear_badge_parent.addView(v);
            badge_layouts_list.add(linear_badge);
        }
        // _badgeadapter.notifyDataSetChanged();
        int position=0;
        for(final LinearLayout layout : badge_layouts_list){
            final String name = badges.get(position).badge.Id + badges.get(position).badge.Name;
            //Log.i("-----------------Badge Name---------", ":" + name);
            layout.post(new Runnable() {

                @Override
                public void run() {
                    try {


                        // TODO Auto-generated method stub
                        Bitmap bitmap = Bitmap.createBitmap(layout.getChildAt(0).getWidth(), layout.getChildAt(0).getHeight(),
                                Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        layout.draw(canvas);

// TODO Auto-generated method stub
/*layout.setDrawingCacheEnabled(true);
layout.setDrawingCacheQuality(LinearLayout.DRAWING_CACHE_QUALITY_HIGH);
layout.buildDrawingCache(true);*/
                        if(saveBitmap(bitmap, name))
                            _badgeadapter.notifyDataSetChanged();
					/*layout.setDrawingCacheEnabled(true);
					layout.setDrawingCacheQuality(LinearLayout.DRAWING_CACHE_QUALITY_HIGH);
					layout.buildDrawingCache(true);
					if(saveBitmap(layout.getDrawingCache(), name))
						_badgeadapter.notifyDataSetChanged();*/
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            });

            position++;
        }

        return true;
    }

    public boolean saveBitmap(Bitmap bitmap, String name) {
        String newFolder = "/.BadgeSamples";
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/ScanAttendee");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File filename = new File(dir + newFolder);
        // Create a name for the saved image
        if (!filename.exists()) {
            filename.mkdir();
        }
        File file = new File(filename, name + ".png");// myimage.png
        //Log.i("Attendee Detail", "Save image path" + dir.toString() + newFolder);
        // Where to save it
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            // boolean success =
            // bitmap.compress(CompressFormat.PNG, 100,
            // out);
            //Log.i("------------------Bitmap Date-----------", ":" + bitmap);
            if (bitmap != null) {
                //bitmap=BitmapFactory.decodeStream(getAssets().open("1024x768.jpg"));
                bitmap.compress(CompressFormat.PNG, 100, out);
                //Log.i("--------------------------Save Bitmap", "Bitmap is:: " + isSaves);
            } else {
                //Log.i("--------------------------Attendeee Detail", "Bitmap is null");
            }
            out.flush();
            out.close();
            return true;
            // Toast.makeText(getApplicationContext(), "File is Saved in " +
            // filename, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * private class BadgesCreationTask extends SafeAsyncTask<Boolean> {
     *
     * ProgressDialog dialog;
     *
     * (non-Javadoc)
     *
     * @see java.util.concurrent.Callable#call()
     *
     * protected void onPreExecute() throws Exception{ super.onPreExecute();
     * dialog = new ProgressDialog(BadgeTemplateNewActivity.this);
     * dialog.setMessage("Creating Badges..."); dialog.show(); }
     *
     * @Override public Boolean call() throws Exception { // TODO Auto-generated
     * method stub
     *
     * return createTemplates(); } protected void onSuccess(Boolean result)
     * throws Exception{ super.onSuccess(result); //dialog.dismiss();
     * if(result){ badge_listView.setAdapter(_badgeadapter); } }
     *
     * }
     */

}
