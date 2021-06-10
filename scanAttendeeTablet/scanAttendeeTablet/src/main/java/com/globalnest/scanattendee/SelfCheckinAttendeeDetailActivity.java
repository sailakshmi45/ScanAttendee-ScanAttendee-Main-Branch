package com.globalnest.scanattendee;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.globalnest.cropimage.CropImage;
import com.globalnest.cropimage.CropUtil;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.BadgeResponseNew;
import com.globalnest.mvc.PrintDetails;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.RegistrationSettingsController;
import com.globalnest.printer.PrinterDetails;
import com.globalnest.retrofit.rest.ApiClient;
import com.globalnest.retrofit.rest.ApiInterface;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sailakshmi on 29-06-2017.
 */


public class SelfCheckinAttendeeDetailActivity extends BaseActivity implements OnClickListener {
    ScrollView selfcheckedinlayout;
    FrameLayout print_badge, frame_transparentbadge,frame_barcode,frame_attendeeimg;
    EditText s_firstname,s_lastname,s_company,s_emailid,s_jobtitle,s_phonenumber;
    String requestType="",whereClause = "";
    Cursor payment_cursor;
    public static Cursor selfcheckin_payment_cursor;
    String attendee_id = "", event_id = "", order_id = "", reason = "";
    String att_fname ="",att_lname = "", att_email = "",att_mobile="",att_badge_lable = "",
            att_work_mobile = "",att_job_title = "",att_company="";
    boolean selfcheckinSaveandPrint =false,isReasonEmpty=false;
    private AlertDialog.Builder print_dialog;
    ImageView img_attendee;
    Bitmap attendee_photo;
    TextView txt_image;
    int i=0;
    ProgressDialog progressDialog;
    private TotalOrderListHandler totalorderlisthandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.selfcheckin_attendeedetail);
        setCustumViewData();
        txtprint_selfcheckin.setEnabled(true);
        selfcheckinonlysave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline()){
                    if (isSelfcheckinColumsEdited()) {
                        if (isValidAttendeeRecord()) {
                            saveAttendeeRequest();
                        }
                    }
                        else{
                            showCustomToast(SelfCheckinAttendeeDetailActivity.this,"No changes Updated!",R.drawable.img_like,R.drawable.toast_greenroundededge,false);
                        }

                }else{
                    startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
                }
            }
        });
        txtprint_selfcheckin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline()){
                   /* if(Util.getselfcheckinbools(Util.ISPRINTALLOWED)&&(Util.getselfcheckinbools(Util.ISDATAEDITABLE))&&!isBadgeSelected()) {
                        if(isSelfcheckinColumsEdited()) {
                            if(isValidAttendeeRecord()) {
                                saveAttendeeRequest();
                            }
                        }else{
                            showCustomToast(SelfCheckinAttendeeDetailActivity.this,"No changes Updated!",R.drawable.img_like,R.drawable.toast_greenroundededge,false);
                        }
                    }else */

                    /*BaseActivity.baseDialog.setMessage("Please wait...");
                    BaseActivity.baseDialog.show();
                    BaseActivity.baseDialog.setCancelable(true);*/
                    if(Util.getselfcheckinbools(Util.ISPRINTALLOWED)) {
                        if (AppUtils.isStoragePermissionGranted(SelfCheckinAttendeeDetailActivity.this)&&isSelfcheckinColumsEdited()) {
                            printAttendeeRequest();
                        }else if (isSelfcheckinColumsEdited()) {
                            if(isValidAttendeeRecord()) {
                                saveAttendeeRequest();
                            }
                            else{
                                showCustomToast(SelfCheckinAttendeeDetailActivity.this,"No changes Updated!",R.drawable.img_like,R.drawable.toast_greenroundededge,false);
                            }
                        }
                        else if(!AppUtils.isStoragePermissionGranted(SelfCheckinAttendeeDetailActivity.this)){
                            AppUtils.giveStoragermission(SelfCheckinAttendeeDetailActivity.this);
                        }else{
                            printAttendeeRequest();
                        }
                    }
                    else{
                        if(isSelfcheckinColumsEdited()) {
                            if(isValidAttendeeRecord()) {
                                saveAttendeeRequest();
                            }
                        }else{
                            showCustomToast(SelfCheckinAttendeeDetailActivity.this,"No changes Updated!",R.drawable.img_like,R.drawable.toast_greenroundededge,false);
                        }
                    }
                }else{
                    startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
                }
            }
        });
        txtcheckin_selfcheckin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String sessionid = Util.db.getSwitchedONGroupId(checked_in_eventId);
                String status=Util.db.getTStatusBasedOnGroup(payment_cursor.
                                getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)),
                        payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                boolean ischeckin = Boolean.valueOf(NullChecker(status));
               /* boolean ischeckinforfreesession = Util.db.SessionCheckInStatus(
                        payment_cursor.getString(payment_cursor.getColumnIndex("Attendee_Id")),
                        Util.db.getSwitchedONGroupId(checked_in_eventId));*/
                if(ischeckin){
                    showCustomToast(SelfCheckinAttendeeDetailActivity.this,
                            " Already Checked In",
                            R.drawable.img_like,R.drawable.toast_redrounded,false);
                }else if(!isOnline()){
                    startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
                }else{
                    checkinbutton_clicked=true;
                    ticketCheckin(SelfCheckinAttendeeDetailActivity.this,payment_cursor,sessionid);
                }
            }
        });
    }

    @Override
    public void setCustomContentView(int layout) {
        activity = this;
        View v = inflater.inflate(layout, null);
        linearview.addView(v);
        txt_title.setText("Attendee");
        img_menu.setImageResource(R.drawable.back_button);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        event_layout.setVisibility(View.GONE);
        button_layout.setVisibility(View.GONE);
        event_layout.setVisibility(View.VISIBLE);
        back_layout.setOnClickListener(this);
        selfcheckedinlayout = (ScrollView) linearview.findViewById(R.id.selfcheckedinlayout);
        print_badge = (FrameLayout) linearview.findViewById(R.id.frame_attdetailqrcodebadge);
        frame_transparentbadge = (FrameLayout) linearview.findViewById(R.id.frame_transparentbadge);
        frame_attendeeimg =(FrameLayout) linearview.findViewById(R.id.frm_attendeeimg);
        img_attendee = (ImageView) linearview.findViewById(R.id.attendeedetailpic);
        txt_image  = (TextView) linearview.findViewById(R.id.txt_image);
        s_firstname = (EditText) linearview.findViewById(R.id.s_firstname);
        s_lastname = (EditText) linearview.findViewById(R.id.s_lastname);
        s_emailid = (EditText) linearview.findViewById(R.id.s_emailid);
        s_phonenumber = (EditText) linearview.findViewById(R.id.s_phonenumber);
        s_company = (EditText) linearview.findViewById(R.id.s_company);
        //  s_badgelabel = (EditText) linearview.findViewById(R.id.s_badgelabel);
        //s_workphno = (EditText) linearview.findViewById(R.id.s_workphno);
        s_jobtitle = (EditText) linearview.findViewById(R.id.s_jobtitle);
        if(Util.getselfcheckinbools(Util.ISCHECKINALLOWED))
        {
            txtcheckin_selfcheckin.setVisibility(View.VISIBLE);
        }

        if(Util.getselfcheckinbools(Util.ISPRINTALLOWED)) {
            txtprint_selfcheckin.setVisibility(View.VISIBLE);
            txtprint_selfcheckin.setText("Print Badge");
            if(Util.getselfcheckinbools(Util.ISDATAEDITABLE)){
                selfcheckinonlysave.setVisibility(View.VISIBLE);
            }
        }else if(Util.getselfcheckinbools(Util.ISDATAEDITABLE)){
            txtprint_selfcheckin.setVisibility(View.VISIBLE);
            txtprint_selfcheckin.setText(" Save ");
        }else {
            txtprint_selfcheckin.setVisibility(View.GONE);
        }
        img_socket_scanner.setVisibility(View.GONE);
        img_scanner_base.setVisibility(View.GONE);
        if(Util.getselfcheckinbools(Util.ISAUTOCHECKIN)){
            requestType = "Check in";
            selfcheckin_payment_cursor=null;
            selfcheckin_payment_cursor =payment_cursor;
        }
        if (Util.getselfcheckinbools(Util.ISDATAEDITABLE)) {
            //s_workphno.setFocusable(true);
            frame_attendeeimg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    openTakeFromDialg(SelfCheckinAttendeeDetailActivity.this);
                }
            });
            s_emailid.setFocusable(true);
            s_firstname.setFocusable(true);
            s_lastname.setFocusable(true);
            s_phonenumber.setFocusable(true);
            //  s_badgelabel.setFocusable(true);
            s_jobtitle.setFocusable(true);
            s_company.setFocusable(true);
        } else {
            // s_workphno.setFocusable(false);
            frame_attendeeimg.setEnabled(false);
            s_emailid.setFocusable(false);
            s_firstname.setFocusable(false);
            s_lastname.setFocusable(false);
            s_phonenumber.setFocusable(false);
            // s_badgelabel.setFocusable(false);
            s_jobtitle.setFocusable(false);
            s_company.setFocusable(false);
        }
    }
    public void setCustumViewData() {
        try{
            Intent attendee_intent = getIntent();
            attendee_id = attendee_intent.getStringExtra("ATTENDEE_ID");
            event_id = attendee_intent.getStringExtra("EVENT_ID");
            order_id = attendee_intent.getStringExtra("ORDER_ID");
            //badge_id= attendee_intent.getStringExtra("BADGE_ID");
            whereClause = " where " + DBFeilds.ATTENDEE_EVENT_ID + " = '"
                    + checked_in_eventId + "'" + " AND " + DBFeilds.ATTENDEE_ID
                    + " = " + "'" + attendee_id + "'" + " AND "
                    + DBFeilds.ATTENDEE_ORDER_ID + " = " + "'" + order_id + "'";
            payment_cursor = Util.db.getAttendeeDetails(whereClause);
            payment_cursor.moveToFirst();
            s_firstname.setTypeface(Util.sanfrancisco_iphonefont);
            s_lastname.setTypeface(Util.sanfrancisco_iphonefont);
            s_company.setTypeface(Util.sanfrancisco_iphonefont);
            s_emailid.setTypeface(Util.sanfrancisco_iphonefont);
            s_jobtitle.setTypeface(Util.sanfrancisco_iphonefont);
            s_phonenumber.setTypeface(Util.sanfrancisco_iphonefont);
            if (NullChecker(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_IMAGE))).isEmpty()) {
                if (!getDbValue(DBFeilds.ATTENDEE_FIRST_NAME).isEmpty()
                        &&!getDbValue(DBFeilds.ATTENDEE_LAST_NAME).isEmpty()){
                    img_attendee.setVisibility(View.GONE);
                    txt_image.setVisibility(View.VISIBLE);
                    txt_image.setText(getDbValue(DBFeilds.ATTENDEE_FIRST_NAME).substring(0, 1).toUpperCase()
                            +getDbValue(DBFeilds.ATTENDEE_LAST_NAME).substring(0, 1).toUpperCase());
                }else {
                    img_attendee.setImageResource(R.drawable.default_image);
                }
            }else{
                img_attendee.setVisibility(View.VISIBLE);
                String[] fullurl = checkedin_event_record.image.split("&id=");
                String url = fullurl[0];
                Picasso.with(SelfCheckinAttendeeDetailActivity.this).load(url + "&id=" + payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_IMAGE)))
                        .placeholder(R.drawable.default_image)
                        .error(R.drawable.default_image).into(img_attendee);

            }
            s_firstname.setText(getDbValue(DBFeilds.ATTENDEE_FIRST_NAME).substring(0,1).toUpperCase()+getDbValue(DBFeilds.ATTENDEE_FIRST_NAME).substring(1));
            s_lastname.setText(getDbValue(DBFeilds.ATTENDEE_LAST_NAME).substring(0,1).toUpperCase()+getDbValue(DBFeilds.ATTENDEE_LAST_NAME).substring(1));
            s_emailid.setText(getDbValue(DBFeilds.ATTENDEE_EMAIL_ID));
            if(NullChecker(getDbValue(DBFeilds.ATTENDEE_COMPANY)).isEmpty()){
                s_company.setText(getDbValue(DBFeilds.ATTENDEE_COMPANY));
            }else{
                s_company.setText(getDbValue(DBFeilds.ATTENDEE_COMPANY).substring(0, 1).toUpperCase() + getDbValue(DBFeilds.ATTENDEE_COMPANY).substring(1));
            }
            // s_company.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            //  s_badgelabel.setText(getDbValue(DBFeilds.ATTENDEE_BADGE_LABLE));
            s_phonenumber.setText(getDbValue(DBFeilds.ATTENDEE_MOBILE));
            if(NullChecker(getDbValue(DBFeilds.ATTENDEE_JOB_TILE)).isEmpty()){
                s_jobtitle.setText(getDbValue(DBFeilds.ATTENDEE_JOB_TILE));
            }else {
                s_jobtitle.setText(getDbValue(DBFeilds.ATTENDEE_JOB_TILE).substring(0, 1).toUpperCase() + getDbValue(DBFeilds.ATTENDEE_JOB_TILE).substring(1));
            }
            //  s_workphno.setText(getDbValue(DBFeilds.ATTENDEE_WORK_PHONE));


        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public boolean isSelfcheckinColumsEdited(){
        boolean isedited=false;
        att_fname = s_firstname.getText().toString().trim();
        att_lname = s_lastname.getText().toString().trim();
        att_email = s_emailid.getText().toString().toLowerCase().trim();
        att_company = s_company.getText().toString().trim();
        att_mobile = s_phonenumber.getText().toString().trim();
        // att_work_mobile = s_workphno.getText().toString().trim();
        att_job_title = s_jobtitle.getText().toString().trim();
        // att_badge_lable = s_badgelabel.getText().toString().trim();
        String image=NullChecker(Util.db.getimagedata(Util.db.getByteArray(attendee_photo)));
        String dbMobile=getDbValue(DBFeilds.ATTENDEE_MOBILE).trim().replaceAll("\\p{P}","").replaceAll("-","").replaceAll(" ","");
        String dbworknumber=getDbValue(DBFeilds.ATTENDEE_WORK_PHONE).
                trim().replaceAll("\\p{P}","").replaceAll("-","").replaceAll(" ","");

        if (!att_fname.equalsIgnoreCase(getDbValue(DBFeilds.ATTENDEE_FIRST_NAME))) {
            return true;
        } else if (!att_lname.equalsIgnoreCase(getDbValue(DBFeilds.ATTENDEE_LAST_NAME))) {
            return true;
        } else if (!att_email.equalsIgnoreCase(getDbValue(DBFeilds.ATTENDEE_EMAIL_ID))) {
            return true;
        } else if (!att_company.equalsIgnoreCase(getDbValue(DBFeilds.ATTENDEE_COMPANY))) {
            return true;
        } else if (!att_mobile.replaceAll("\\p{P}","").replaceAll("-","").replaceAll(" ","").trim().equalsIgnoreCase(dbMobile.trim())) {
            return true;
        } else if (!att_job_title.equalsIgnoreCase(getDbValue(DBFeilds.ATTENDEE_JOB_TILE))) {
            return true;
        }else if(!image.isEmpty()){
            return true;
        }/* else if (!att_badge_lable.equalsIgnoreCase(getDbValue(DBFeilds.ATTENDEE_BADGE_LABLE))) {
            return true;
        }
        else if (!att_work_mobile.replaceAll("\\p{P}","").replaceAll("-","").replaceAll(" ","").trim().equalsIgnoreCase(dbworknumber.trim())) {
            return true;
        }*/

        return isedited;
    }
    public boolean isValidAttendeeRecord() {
        if (att_fname.equals("")) {
            s_firstname.setError(getResources().getString(R.string.fname_alert));
            s_firstname.requestFocus();
            return false;
        } else if (att_lname.equals("")) {
            s_lastname.setError(getResources().getString(R.string.lname_alert));
            s_lastname.requestFocus();
            return false;
        } else if (!Pattern.matches(Validation.EMAIL_REGEX, att_email)) {
            s_emailid.setError(getResources().getString(R.string.email_alert));
            s_emailid.requestFocus();
            return false;
        } else {
            return true;
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//		Log.i("---------------onActivity Result------------", ":" + requestCode + " : " + data.getStringExtra(Util.INTENT_KEY_1));

        if((requestCode == REQUEST_CODE_CROP_IMAGE)&& (data!=null)){

            String path = data.getStringExtra(CropImage.IMAGE_PATH);

            if ((path == null)||(TextUtils.isEmpty(path))) {
                return;
            }
            Bitmap bitmap = BitmapFactory.decodeFile(path);
				/*if(bitmapArrayList.size()<formPosition)
					bitmapArrayList.add(bitmap);
				else
					bitmapArrayList.set(formPosition,bitmap);*/
            img_attendee.setVisibility(View.VISIBLE);
            img_attendee.setImageBitmap(bitmap);
            attendee_photo = bitmap;

            if(mFileTemp!=null){
                mFileTemp.delete();
            }
        }else if ((requestCode == PICK_FROM_CAMERA) && (resultCode == RESULT_OK)) {
            if (data != null) {

                //doCrop();
            } else {
                File mediaStorageDir = new File(
                        Environment.getExternalStorageDirectory(),
                        "ScanAttendee");
                if(!mediaStorageDir.exists()){
                    mediaStorageDir.mkdir();
                }
                mediaFile = new File(mediaStorageDir.getPath() + File.separator
                        + "IMG_1.jpg");
                mImageCaptureUri = Uri.fromFile(mediaFile);


                if (mImageCaptureUri != null) {
                    try {
                        startCropImage(SelfCheckinAttendeeDetailActivity.this);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

        } else if (requestCode == PICK_FROM_FILE && data != null
                && data.getData() != null) {

            mImageCaptureUri = data.getData();
            //mediaFile = new File(getRealPathFromURI(mImageCaptureUri));
            try {
                File mediaStorageDir = new File(
                        Environment.getExternalStorageDirectory(),
                        "ScanAttendee");
                if(!mediaStorageDir.exists()){
                    mediaStorageDir.mkdir();
                }
                mFileTemp = new File(mediaStorageDir.getPath() + File.separator
                        + "IMG_1.jpg");
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
                CropUtil.copyStream(inputStream, fileOutputStream);
                mediaFile = mFileTemp;
                startCropImage(SelfCheckinAttendeeDetailActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == CROP_FROM_CAMERA) {

            Bundle extras = data.getExtras();

            if (extras != null) {
                attendee_photo = extras.getParcelable("data");
                img_attendee.setVisibility(View.VISIBLE);
                img_attendee.setImageBitmap(attendee_photo);

            }
        }else if (requestCode == FINISH_RESULT) {
            startActivity(new Intent(SelfCheckinAttendeeDetailActivity.this, SplashActivity.class));
            finish();
        }

    }

    public void printAttendeeRequest() {
        txtprint_selfcheckin.setEnabled(false);
        if(Util.getselfcheckinbools(Util.ISDATAEDITABLE)&&isSelfcheckinColumsEdited()){
            selfcheckinSaveandPrint=true;
        }else{
            selfcheckinSaveandPrint=false;
        }
        whereClause = " where " + DBFeilds.ATTENDEE_EVENT_ID + " = '"
                + checked_in_eventId + "'" + " AND " + DBFeilds.ATTENDEE_ID
                + " = " + "'" + attendee_id + "'" + " AND "
                + DBFeilds.ATTENDEE_ORDER_ID + " = " + "'" + order_id + "'";
        payment_cursor = Util.db.getAttendeeDetails(whereClause);
        payment_cursor.moveToFirst();

        if (isBadgeSelected()) {
            if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                if(!Util.getselfcheckinbools(Util.ISREPRINTALLOWED)&& !Util.getselfcheckinbools(Util.ISDATAEDITABLE)) {
                    if(!getDbValue(DBFeilds.ATTENDEE_BADGEID).isEmpty()&&getDbValue(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS).equalsIgnoreCase("Printed")){
                        showBadgeAlreadyPrinted();
                    }else {//if(getDbValue(DBFeilds.ATTENDEE_BADGEID).isEmpty())
                        callBadgeid();
                    }
                }
                else if (Util.getselfcheckinbools(Util.ISREPRINTALLOWED) && !Util.getselfcheckinbools(Util.ISDATAEDITABLE)) {
                    if (getDbValue(DBFeilds.ATTENDEE_BADGEID).isEmpty()) {
                        callBadgeid();
                    } else if (!getDbValue(DBFeilds.ATTENDEE_BADGEID)
                            .isEmpty()) {
                        isprinterconnectedopendialog();
                    }
                } else if(!Util.getselfcheckinbools(Util.ISREPRINTALLOWED)&& Util.getselfcheckinbools(Util.ISDATAEDITABLE)){
                    if (!getDbValue(DBFeilds.ATTENDEE_BADGEID).isEmpty()&&getDbValue(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS).equalsIgnoreCase("Printed")) {
                        if (selfcheckinSaveandPrint) {
                            if(isValidAttendeeRecord()){
                                saveAttendeeRequest();
                            }
                        }else{
                            showCustomToast(SelfCheckinAttendeeDetailActivity.this,"No changes Updated!",R.drawable.img_like,R.drawable.toast_greenroundededge,false);
                            //Toast.makeText(SelfCheckinAttendeeDetailActivity.this, "No changes Updated", Toast.LENGTH_LONG).show();
                            showBadgeAlreadyPrinted();
                        }
                    } else  {
                        if (selfcheckinSaveandPrint) {
                            if(isValidAttendeeRecord()){
                                saveAttendeeRequest();
                            }
                        }else{
                            callBadgeid();
                        }
                    }
                }else if (Util.getselfcheckinbools(Util.ISREPRINTALLOWED)&&Util.getselfcheckinbools(Util.ISDATAEDITABLE)) {
                    if (getDbValue(DBFeilds.ATTENDEE_BADGEID).isEmpty()) {
                        if (selfcheckinSaveandPrint) {
                            if(isValidAttendeeRecord()) {
                                saveAttendeeRequest();
                            }
                        }else {
                            callBadgeid();
                        }
                    } else if (!getDbValue(DBFeilds.ATTENDEE_BADGEID).isEmpty()) {
                        if (selfcheckinSaveandPrint) {
                            if(isValidAttendeeRecord()){
                                saveAttendeeRequest();
                            }
                        }else {
                            isprinterconnectedopendialog();
                        }
                    }
                }
            }
        }else {
            if (selfcheckinSaveandPrint) {
                if(isValidAttendeeRecord()) {
                    saveAttendeeRequest();
                }
            }else {
                if (isOnline()) {
                    requestType = Util.LOAD_BADGE;
                    doRequest();
                } else {
                    startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                }
            }
        }
    }

    public void openprintDialog() {
        try {
            txtprint_selfcheckin.setEnabled(true);
            print_dialog = new AlertDialog.Builder(SelfCheckinAttendeeDetailActivity.this);
            LayoutInflater li = LayoutInflater
                    .from(SelfCheckinAttendeeDetailActivity.this);
            View promptsView = li.inflate(R.layout.print_dialog_layout, null);
            print_dialog.setView(promptsView);
            final EditText edit_reason = (EditText) promptsView.findViewById(R.id.edit_reason);
            final TextView txt_message=(TextView) promptsView.findViewById(R.id.txt_message);
            final TextView txt_top=(TextView) promptsView.findViewById(R.id.textView1);
            edit_reason.setText("");
            if(getDbValue(DBFeilds.ATTENDEE_BADGEID).isEmpty()){
                txt_message.setVisibility(View.VISIBLE);
                edit_reason.setVisibility(View.GONE);
            }else{
                txt_top.setText("Badge is already printed. Do you want to reprint ?\n" +
                        "The previous badge will become invalid.");
                txt_message.setVisibility(View.GONE);
                edit_reason.setVisibility(View.VISIBLE);
            }
            print_dialog
                    .setCancelable(false)
                    .setPositiveButton("Reprint",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    reason = edit_reason.getText().toString();
                                    if(getDbValue(DBFeilds.ATTENDEE_BADGEID).isEmpty()) {
                                        callBadgeid();
                                    }else if(!getDbValue(DBFeilds.ATTENDEE_BADGEID).isEmpty()){
                                        if (!reason.equalsIgnoreCase("") ) {
                                            callBadgeid();
                                        }else if(reason.trim().isEmpty()){
                                            isReasonEmpty=true;
                                            edit_reason.setFocusable(true);
                                            edit_reason.setError("Reason should not be empty");

                                        }
                                    }
                                }})
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isReasonEmpty=false;
                            hideSoftKeyboard(SelfCheckinAttendeeDetailActivity.this);
                            dialog.dismiss();

                        }
                    });


            // create alert dialog
            final AlertDialog alertDialog = print_dialog.create();

            alertDialog.show();

            alertDialog
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hideSoftKeyboard(SelfCheckinAttendeeDetailActivity.this);
                            if (isReasonEmpty) {
                                alertDialog.dismiss();
                                alertDialog.show();
                                edit_reason.setError("Reason should not be empty");
                                edit_reason.requestFocus();
                            } else {
                                return;
                            }

                        }
                    });
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
            txt_message.setText("Do you want to print the badge?");




        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void saveAttendeeRequest() {
        if (isOnline()) {
            requestType = Util.ATTENDEE_DETAIL;
            doRequest();
        } else {
            startErrorAnimation(getResources().getString(R.string.network_error),
                    txt_error_msg);
        }
        txtprint_selfcheckin.setEnabled(true);
    }
    public void  callBadgeid(){
        /*if(BaseActivity.baseDialog!=null) {
            if(BaseActivity.baseDialog.isShowing())
                BaseActivity.baseDialog.dismiss();
        }*/
        if(Util.getselfcheckinbools(Util.ISPRINTALLOWED)) {
            if (!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
                //if(isprinterconnected()){
                if (isOnline()) {
                    try {
                        if(isBadgeSelected()) {
                            if(Util.getselfcheckinbools(Util.ISPRINTALLOWED)&&AppUtils.isStoragePermissionGranted(SelfCheckinAttendeeDetailActivity.this)){
                                txtprint_selfcheckin.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_orange));
                                txtprint_selfcheckin.setText(" Printing...");
                            }
                            doSaveAndPrint(attendee_id);
                        }else{
                            BaseActivity.showSingleButtonDialog("Alert",
                                    "No Badge Selected, Please contact your Event Organizer!",this);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    /*requestType = Util.GET_BADGE_ID;
                    doRequest();*/
                } else {
                    startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                }
            } else {
                String msg = "";
                if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
                    if (PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
                        msg = "Printer is not connected.Please contact event Organizer!";
                    } else {
                        msg = "Printer is disconnected.Please contact event Organizer!";
                    }
                    txtprint_selfcheckin.setEnabled(true);
                    Util.setCustomAlertDialog(SelfCheckinAttendeeDetailActivity.this);
                    Util.openCustomDialog("Alert", msg);
                    Util.txt_okey.setText("OK");
                    Util.alert_dialog.setCancelable(false);
                    Util.txt_dismiss.setVisibility(View.GONE);
                    Util.txt_okey.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            Util.alert_dialog.dismiss();
                            finish();
                        }
                    });
                }
            }
        }
    }
    @Override
    public void doRequest() {

        String access_token = sfdcddetails.token_type + " "
                + sfdcddetails.access_token;
        String url = sfdcddetails.instance_url
                + WebServiceUrls.SA_ATTENDEE_DETAIL + "eventId="
                + checked_in_eventId;
        if (requestType.equalsIgnoreCase(Util.LOAD_BADGE)) {
            String _url = sfdcddetails.instance_url + WebServiceUrls.SA_GET_BADGE_TEMPLATE_NEW + "Event_Id=" + checked_in_eventId;
            postMethod = new HttpPostData("Loading Badges...", _url, null, access_token, SelfCheckinAttendeeDetailActivity.this);
            postMethod.execute();
        } else if (requestType.equalsIgnoreCase(Util.ATTENDEE_DETAIL)) {
            progressDialog =new ProgressDialog(SelfCheckinAttendeeDetailActivity.this);
            progressDialog.setMessage("Saving Attendee Info...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            ApiInterface apiService = ApiClient.getClient(sfdcddetails.instance_url).create(ApiInterface.class);
            // Call<Void> jsonbody= apiService.setSurveys(setSellJsonBody());
            Call<TotalOrderListHandler> call = apiService.getAttendeeDetailvalues(checked_in_eventId,setAttendeeJsonBody().toString(),sfdcddetails.token_type + " "+ sfdcddetails.access_token);
            if(AppUtils.isLogEnabled) {
                AppUtils.displayLog(call + "------ Url-------", url);
                AppUtils.displayLog(call + "------JSON Retrofit-------", setAttendeeJsonBody().toString());
            }
            call.enqueue(new Callback<TotalOrderListHandler>() {
                @Override
                public void onResponse(Call<TotalOrderListHandler> call, Response<TotalOrderListHandler> response) {
                    Log.e(call+"------success-------", "------response started-------");
                    if(AppUtils.isLogEnabled){AppUtils.displayLog(call+"------JSON Response-------", response.toString());}
                    try {
                        if (!isValidResponse(response.toString())) {
                            dismissProgressDialog();
                            openSessionExpireAlert(errorMessage(response.toString()));
                        } else if (response.code() == 200) {
                            totalorderlisthandler = response.body();
                            if (!NullChecker(totalorderlisthandler.errorMsg).isEmpty()) {
                                AlertDialogCustom dialog = new AlertDialogCustom(
                                        SelfCheckinAttendeeDetailActivity.this);
                                dialog.setParamenters("Alert",
                                        AppUtils.NullChecker(totalorderlisthandler.errorMsg), null, null,
                                        1, false);
                                dialog.show();
                            } else {
                                if (totalorderlisthandler.TotalLists.size() > 0) {
                                    Util.db.upadteOrderList(
                                            totalorderlisthandler.TotalLists,
                                            checked_in_eventId);
                                }
                                if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                                    if (!selfcheckinSaveandPrint) {
                                        showCustomToast(SelfCheckinAttendeeDetailActivity.this,"Saved Successfully!",R.drawable.img_like,R.drawable.toast_greenroundededge,true);
                                        finish();
                                        // Toast.makeText(SelfCheckinAttendeeDetailActivity.this, "Saved Successfully", Toast.LENGTH_LONG).show();
                                    } else if(Util.getselfcheckinbools(Util.ISREPRINTALLOWED)&&selfcheckinSaveandPrint){
                                        showCustomToast(SelfCheckinAttendeeDetailActivity.this,"Saved Successfully!",R.drawable.img_like,R.drawable.toast_greenroundededge,true);
                                        if(!getDbValue(DBFeilds.ATTENDEE_BADGEID).isEmpty()){
                                            selfcheckinSaveandPrint = false;
                                            isprinterconnectedopendialog();
                                        } else {
                                            selfcheckinSaveandPrint = false;
                                            callBadgeid();
                                        }
                                    }else if(!Util.getselfcheckinbools(Util.ISREPRINTALLOWED)&&selfcheckinSaveandPrint){
                                        selfcheckinSaveandPrint = false;
                                        showCustomToast(SelfCheckinAttendeeDetailActivity.this,"Saved Successfully!",R.drawable.img_like,R.drawable.toast_greenroundededge,true);

                                        // Toast.makeText(SelfCheckinAttendeeDetailActivity.this, "Saved Successfully", Toast.LENGTH_LONG).show();
                                        if(!getDbValue(DBFeilds.ATTENDEE_BADGEID).isEmpty()&&getDbValue(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS).equalsIgnoreCase("Printed")){
                                            showBadgeAlreadyPrinted();
                                        }else {
                                            callBadgeid();
                                        }
                                    }
                                }
                            }
                            dismissProgressDialog();
                        }
                        txtprint_selfcheckin.setEnabled(true);
                    }catch (Exception e) {
                        e.printStackTrace();
                        startErrorAnimation(
                                getResources().getString(R.string.network_error1),
                                txt_error_msg);
                    }
                }




                @Override
                public void onFailure(Call<TotalOrderListHandler> call, Throwable t) {
                    // Log error here since request failed
                    Log.e("------failure-------", t.toString());
                    dismissProgressDialog();
                    txtprint_selfcheckin.setEnabled(true);
                }
            });
			/*String url = sfdcddetails.instance_url
					+ WebServiceUrls.SA_ATTENDEE_DETAIL + "eventId="
					+ checked_in_eventId;
			postMethod = new HttpPostData("Saving Attendee Info...", url,
					setAttendeeJsonBody().toString(), access_token,
					AttendeeDetailActivity.this);
			postMethod.execute();*/
        }
        /*else if(requestType.equalsIgnoreCase(Util.GET_BADGE_ID)){
            postMethod = new HttpPostData("Getting Badge Id...",
                    sfdcddetails.instance_url + WebServiceUrls.SA_BADGE_PRINT, setPrintBadgeBody().toString(),
                    access_token, SelfCheckinAttendeeDetailActivity.this);
            postMethod.execute();
        }*/

    }
    private JSONArray setPrintBadgeBody() {
        /*String where_att = " Where EventID = '" + event_id
                + "' AND isBadgeSelected = 'Yes'";
        Cursor updated_badge1 = Util.db.getBadgeTemplate(where_att);
        updated_badge1.moveToFirst();*/
        JSONArray badgearray = new JSONArray();
        JSONObject obj = new JSONObject();
        try {
            obj.put("TicketId", payment_cursor.getString(payment_cursor
                    .getColumnIndex(DBFeilds.ATTENDEE_ID)));
            obj.put("BadgeLabel", NullChecker(payment_cursor.getString(payment_cursor
                    .getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))));
            obj.put("Reason", NullChecker(reason));
            badgearray.put(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return badgearray;
    }
    private void dismissProgressDialog() {
        if(progressDialog!=null) {
            if(progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }
    private String getDbValue(String Dbcolumn){
        if(payment_cursor==null){
            getAttendeeCursor();
        }
        String DbValue=Util.NullChecker(payment_cursor.getString(payment_cursor
                .getColumnIndex(Dbcolumn))).trim();
        return DbValue;
    }

    public void getAttendeeCursor(){
        //badge_id= attendee_intent.getStringExtra("BADGE_ID");
        whereClause = " where " + DBFeilds.ATTENDEE_EVENT_ID + " = '"
                + checked_in_eventId + "'" + " AND " + DBFeilds.ATTENDEE_ID
                + " = " + "'" + attendee_id + "'" + " AND "
                + DBFeilds.ATTENDEE_ORDER_ID + " = " + "'" + order_id + "'";
        payment_cursor = Util.db.getAttendeeDetails(whereClause);
        payment_cursor.moveToFirst();
    }
    private JSONObject setAttendeeJsonBody() {
        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put("fn", att_fname);
            json.put("ln", att_lname);
            json.put("email", att_email);
            json.put("comp", att_company);
            if (att_company.equals(getDbValue(DBFeilds.ATTENDEE_COMPANY)))
                json.put("compid", getDbValue(DBFeilds.ATTENDEE_COMPANY_ID));
            else
                json.put("compid", "");
            json.put("mobile", att_mobile);
            json.put("BPhone", getDbValue(DBFeilds.ATTENDEE_WORK_PHONE));
            json.put("phone", getDbValue(DBFeilds.ATTENDEE_HOME_PHONE));
            json.put("title", att_job_title);
            json.put("badgelabel", getDbValue(DBFeilds.ATTENDEE_BADGE_LABLE));
            json.put("CustomBarcode", getDbValue(DBFeilds.ATTENDEE_CUSTOM_BARCODE));
            String image="";
            if(attendee_photo!=null)
             image=NullChecker(Util.db.getimagedata(Util.db.getByteArray(attendee_photo)));
            if(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_IMAGE)).length()>0&&NullChecker(image).length()==0){
                json.put("UserPic",payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_IMAGE)));
            }
            else if(NullChecker(image).length()>0){
                json.put("AttendeeImage",image);
            }
            else {
                json.put("AttendeeImage","");
            }
           /* //String image=Util.db.getimagedata(Util.db.getByteArray(attendee_photo));
            if(getDbValue(DBFeilds.ATTENDEE_IMAGE).length()>0){
                json.put("UserPic",getDbValue(DBFeilds.ATTENDEE_IMAGE));
            }
            *//*else if(NullChecker(image).length()>0){
                json.put("AttendeeImage",image);
            }*//*
            else {
                json.put("AttendeeImage","");
            }*/
            json.put("tag", "");
            json.put("seatno", getDbValue(DBFeilds.ATTENDEE_TICKET_SEAT_NUMBER));
            json.put("note", getDbValue(DBFeilds.ATTENDEE_NOTE));
            String item_id = getDbValue(DBFeilds.ATTENDEE_ITEM_ID);
            ArrayList<RegistrationSettingsController> reg_setting_list = Util.db.getRegSettingsList("where "+DBFeilds.REG_ITEM_ID+" = '"+item_id+"'");
            for(RegistrationSettingsController each_setting : reg_setting_list){
                if(each_setting.Column_Name__c.equalsIgnoreCase(getString(R.string.work_address)) && Boolean.valueOf(each_setting.Included__c)){
                    json.put("BAddress1", getDbValue(DBFeilds.ATTENDEE_WORK_ADDRESS_1));
                    json.put("BAddress2", getDbValue(DBFeilds.ATTENDEE_WORK_ADDRESS_2));
                    json.put("BCity", getDbValue(DBFeilds.ATTENDEE_WORK_CITY));
                    json.put("BZipcode", getDbValue(DBFeilds.ATTENDEE_WORK_ZIPCODE));

                    if (!Util.db.getStateId(getDbValue(DBFeilds.ATTENDEE_WORK_STATE).trim()).isEmpty())
                        json.put("BState", Util.db.getStateId(getDbValue(DBFeilds.ATTENDEE_WORK_STATE).trim()));
                    if (!Util.db.getCountryId(getDbValue(DBFeilds.ATTENDEE_WORK_COUNTRY).trim()).isEmpty())
                        json.put("BCountry", Util.db.getCountryId(NullChecker(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_WORK_COUNTRY))).trim()));
                }else if(each_setting.Column_Name__c.equalsIgnoreCase(getString(R.string.home_address)) && Boolean.valueOf(each_setting.Included__c)){
                    json.put("add1", getDbValue(DBFeilds.ATTENDEE_HOME_ADDRESS_1));
                    json.put("add2", getDbValue(DBFeilds.ATTENDEE_HOME_ADDRESS_2));
                    json.put("city", getDbValue(DBFeilds.ATTENDEE_HOME_CITY));
                    if (!Util.db.getStateId(getDbValue(DBFeilds.ATTENDEE_HOME_STATE).trim()).isEmpty())
                        json.put("state", Util.db.getStateId(getDbValue(DBFeilds.ATTENDEE_HOME_STATE).trim()));
                    if (!Util.db.getCountryId(getDbValue(DBFeilds.ATTENDEE_HOME_COUNTRY).trim()).isEmpty())
                        json.put("country", Util.db.getCountryId(getDbValue(DBFeilds.ATTENDEE_HOME_COUNTRY).trim()));
                    json.put("zipcode", getDbValue(DBFeilds.ATTENDEE_HOME_ZIPCODE));
                }
            }

            json.put("tid", getDbValue(DBFeilds.ATTENDEE_ID));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        try {
            if(txtprint_selfcheckin!=null)
                txtprint_selfcheckin.setEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public void parseJsonResponse(String response) {
        txtprint_selfcheckin.setEnabled(true);
        if(!isValidResponse(response)){
            openSessionExpireAlert(errorMessage(response));
        }
        if(requestType.equalsIgnoreCase(Util.LOAD_BADGE)){
            Type listType = new TypeToken<List<BadgeResponseNew>>() {}.getType();
            List<BadgeResponseNew> badges =  new Gson().fromJson(response, listType);
            AppUtils.displayLog("---------------- parseJsonResponse Badge Size----------", ":"+checkedin_event_record.Events.Mobile_Default_Badge__c+" : " + response);
            Util.db.deleteBadges(checked_in_eventId);
            sharedPreferences.edit().clear().commit(); //TODO
            for(BadgeResponseNew badge : badges){
                badge.badge.event_id = checked_in_eventId;
                Util.db.InsertAndUpdateBadgeTemplateNew(badge);
            }

            if(isBadgeSelected()){
                // printAttendeeRequest();
            }else{
                showSingleButtonDialog("Alert",
                        "No Badge Selected, Please contact your Event Organizer!",this);

            }

        } else if (requestType.equals(Util.ATTENDEE_DETAIL)) {
            try {
                gson = new Gson();
                TotalOrderListHandler totalorderlisthandler = gson
                        .fromJson(response, TotalOrderListHandler.class);

                if (totalorderlisthandler.TotalLists.size() > 0) {
                    Util.db.upadteOrderList(
                            totalorderlisthandler.TotalLists,
                            checked_in_eventId);
                }
                if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                    if (!selfcheckinSaveandPrint) {
                        showCustomToast(SelfCheckinAttendeeDetailActivity.this,"Saved Successfully!",R.drawable.img_like,R.drawable.toast_greenroundededge,true);
                        finish();
                        // Toast.makeText(SelfCheckinAttendeeDetailActivity.this, "Saved Successfully", Toast.LENGTH_LONG).show();
                    } else if(Util.getselfcheckinbools(Util.ISREPRINTALLOWED)&&selfcheckinSaveandPrint){
                        showCustomToast(SelfCheckinAttendeeDetailActivity.this,"Saved Successfully!",R.drawable.img_like,R.drawable.toast_greenroundededge,true);
                        if(!getDbValue(DBFeilds.ATTENDEE_BADGEID).isEmpty()){
                            selfcheckinSaveandPrint = false;
                            isprinterconnectedopendialog();
                        } else {
                            selfcheckinSaveandPrint = false;
                            callBadgeid();
                        }
                    }else if(!Util.getselfcheckinbools(Util.ISREPRINTALLOWED)&&selfcheckinSaveandPrint){
                        selfcheckinSaveandPrint = false;
                        showCustomToast(SelfCheckinAttendeeDetailActivity.this,"Saved Successfully!",R.drawable.img_like,R.drawable.toast_greenroundededge,true);

                        // Toast.makeText(SelfCheckinAttendeeDetailActivity.this, "Saved Successfully", Toast.LENGTH_LONG).show();
                        if(!getDbValue(DBFeilds.ATTENDEE_BADGEID).isEmpty()&&getDbValue(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS).equalsIgnoreCase("Printed")){
                            showBadgeAlreadyPrinted();
                        }else {
                            callBadgeid();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                startErrorAnimation(
                        getResources().getString(R.string.network_error1),
                        txt_error_msg);
            }
        }
    }

    @Override
    public void insertDB() {

    }

    @Override
    public void onClick(View v) {
        if(v == back_layout){
            print_badge.setVisibility(View.GONE);
            frame_transparentbadge.setVisibility(View.GONE);
            Intent i = new Intent();
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            setResult(2017,i);
            finish();
        }
    }
    public void isprinterconnectedopendialog(){
        if(isBadgeSelected()) {
            if(!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
                if(isValidate_badge_reg_settings&&getDbValue(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS).equalsIgnoreCase("Printed")){
                    openprintDialog();
                }
                else{
                    callBadgeid();
                }

            }
            else{
                txtprint_selfcheckin.setEnabled(true);
                Util.setCustomAlertDialog(SelfCheckinAttendeeDetailActivity.this);
                Util.openCustomDialog("Alert", "Printer is not connected.Please contact event Organizer!");
                Util.txt_okey.setText("OK");
                Util.alert_dialog.setCancelable(false);
                Util.txt_dismiss.setVisibility(View.GONE);
                Util.txt_okey.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Util.alert_dialog.dismiss();
                        finish();
                    }
                });
            }
        }else{
            BaseActivity.showSingleButtonDialog("Alert",
                    "No Badge Selected, Please contact your Event Organizer!",this);
        }
    }
    private void doSaveAndPrint(String attendeeid) throws SQLException {

        PrintAndCheckin printT= new PrintAndCheckin();
        PrintDetails printDetails=new PrintDetails();
        printDetails.attendeeId=attendeeid;
        printDetails.checked_in_eventId=checked_in_eventId;
        printDetails.frame_transparentbadge=frame_transparentbadge;
        printDetails.order_id=order_id;
        printDetails.print_badge=print_badge;
        printDetails.sfdcddetails=sfdcddetails;
        printDetails.reason=reason;
        printDetails.isselfCheckinbool=Util.getselfcheckinbools(Util.ISSELFCHECKIN);
        requestType = "Check in";
        printT.doSaveAndPrint(SelfCheckinAttendeeDetailActivity.this,printDetails);


    }
    private void  showBadgeAlreadyPrinted(){
        Util.setCustomAlertDialog(SelfCheckinAttendeeDetailActivity.this);
        Util.alert_dialog.setCancelable(false);
        Util.openCustomDialog("Alert",
                "Your Badge is Already Printed.Please contact Event Organizer!");
        Util.txt_okey.setText("Ok");
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.alert_dialog.setCancelable(false);
        Util.txt_okey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Util.alert_dialog.dismiss();
                finish();
            }
        });
        txtprint_selfcheckin.setEnabled(true);
    }
}
