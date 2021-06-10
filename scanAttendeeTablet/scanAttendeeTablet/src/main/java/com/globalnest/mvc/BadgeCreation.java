//  ScanAttendee Android
//  Created by Ajay on Oct 15, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.mvc;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globalnest.autoresizetextview.AutoResizeTextView;
import com.globalnest.autoresizetextview.AutoResizeTextViewOne;
import com.globalnest.autoresizetextview.RotateLayout;
import com.globalnest.classes.CircleButton;
import com.globalnest.classes.QRCodeEncoder;
import com.globalnest.data.Contents;
import com.globalnest.database.DBFeilds;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.scanattendee.BaseActivity;
import com.globalnest.scanattendee.R;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;
import com.globalnest.utils.Util.FEILDS;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * @author laxmanamurthy
 *
 */
public class BadgeCreation {

    private static  int PIXEL_FOR_INCH = 96;
//	private static  final int PIXEL_FOR_INCH = 96;

    private  Context context;
    private int width=0,height=0;
    public BadgeCreation(Context ctx, int deviceWidth,int deviceHeight){
        this.context = ctx;
        this.width = deviceWidth;
        this.height = deviceHeight;
    }


    public  void createBadgeTemplate(BadgeResponseNew badge_res,FrameLayout badge_layout,Cursor att_cursor,boolean forprint) throws IOException {
        Gson gson = new Gson();
        ////Log.i("-----------------Badge Date---------",":"+badge_res.badge.Data__c);
        BadgeDataNew badge_data = gson.fromJson(badge_res.badge.Data__c, BadgeDataNew.class);
	/*	//Log.i("-----------------Frame Beforev Heght Width---------",
				":" + badge_res.badge.Description__c + ":" + badge_data.canvasWidth + " : " + badge_data.canvasHeight);*/
        if(forprint){
            PIXEL_FOR_INCH=150;
        }else {
            PIXEL_FOR_INCH=96;
        }
        int width_frame = (int) (badge_data.canvasWidth * PIXEL_FOR_INCH);
        int height_frame = (int) (badge_data.canvasHeight * PIXEL_FOR_INCH);
        //FrameLayout badge_layout =  new FrameLayout(context);
        LinearLayout.LayoutParams params_frame = new LinearLayout.LayoutParams(pxToDp(width_frame), pxToDp(height_frame));
        //android.widget.LinearLayout.LayoutParams params_frame = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
        badge_layout.setLayoutParams(params_frame);
        // badge_layout.setPadding(5, 5, 5, 5);
        if(badge_data.backgroundColor.length()==7)
            badge_layout.setBackgroundColor(Color.parseColor(badge_data.backgroundColor));
        if(!AppUtils.NullChecker(badge_data.backgroundImage).isEmpty()){
            ImageView img_bg = new ImageView(context);
            LinearLayout.LayoutParams params_frame_1 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
            img_bg.setLayoutParams(params_frame_1);
			/*img_bg.setX(pxToDp((int) (x * badge_data.canvasWidth)));
			img_bg.setY(pxToDp((int) (y * badge_data.canvasWidth)));*/
            img_bg.setScaleType(ScaleType.FIT_XY);
            String image_url = WebServiceUrls.SA_PRODUCTION + badge_data.backgroundImage;
            Glide.with(context).load(image_url).dontAnimate().into(img_bg);
			/*Picasso.with(context).load(image_url).placeholder(R.drawable.badge_sample_image)
					.error(R.drawable.badge_sample_image).into(img_bg);*/
            badge_layout.addView(img_bg);
        }

        sortLayers(badge_res.layers);
        for (BadgeLayerNew layer : badge_res.layers) {
            BadgeDataNew layer_values = gson.fromJson(layer.Data__c, BadgeDataNew.class);
            if (layer_values.layerType.equalsIgnoreCase("Custom_QR_CODE")) {
                // continue;
                ImageView img_qrcode = new ImageView(context);
                double width = (double) layer_values.width * PIXEL_FOR_INCH;
                double height = (double) layer_values.height * PIXEL_FOR_INCH;
                double x = (double) (layer_values.x * PIXEL_FOR_INCH);
                double y = (double) (layer_values.y * PIXEL_FOR_INCH);
                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                img_qrcode.setLayoutParams(params);
                img_qrcode.setX(pxToDp((int) (x * badge_data.canvasWidth)));
                img_qrcode.setY(pxToDp((int) (y * badge_data.canvasWidth)));
                Bitmap barCode = null;
                if (att_cursor != null) {
                    barCode = encodeQrCode(att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_CUSTOM_BARCODE)), pxToDp((int) (width * badge_data.canvasWidth)), pxToDp((int) (height * badge_data.canvasWidth)), layer_values.layerType);
                } else {
                    barCode = encodeQrCode(layer_values.content, pxToDp((int) (width * badge_data.canvasWidth)), pxToDp((int) (height * badge_data.canvasWidth)), layer_values.layerType);
                }
                if (barCode != null) {
                    //Bitmap resize_bitmap = Bitmap.createScaledBitmap(barCode, pxToDp((int) (width * badge_data.canvasWidth)), pxToDp((int) (height * badge_data.canvasWidth)), true);
                    //Bitmap resize_bitmap = Util.db.getResizedBitmap(barCode,);
                    img_qrcode.setImageBitmap(barCode);
                    img_qrcode.setScaleType(ScaleType.FIT_XY);
                    badge_layout.addView(img_qrcode);
                }

            }else if (layer_values.layerType.equalsIgnoreCase("Custom_Bar_CODE")) {
                // continue;
                ImageView img_qrcode = new ImageView(context);
                double width = (double) layer_values.width * PIXEL_FOR_INCH;
                double height = (double) layer_values.height * PIXEL_FOR_INCH;
                double x = (double) (layer_values.x * PIXEL_FOR_INCH);
                double y = (double) (layer_values.y * PIXEL_FOR_INCH);
                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                img_qrcode.setLayoutParams(params);
                img_qrcode.setX(pxToDp((int) (x * badge_data.canvasWidth)));
                img_qrcode.setY(pxToDp((int) (y * badge_data.canvasWidth)));
                Bitmap barCode = null;
                if (att_cursor != null) {
                    barCode = encodeQrCode(att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_CUSTOM_BARCODE)), pxToDp((int) (width * badge_data.canvasWidth)), pxToDp((int) (height * badge_data.canvasWidth)), layer_values.layerType);
                } else {
                    barCode = encodeQrCode(layer_values.content, pxToDp((int) (width * badge_data.canvasWidth)), pxToDp((int) (height * badge_data.canvasWidth)), layer_values.layerType);
                }
                if (barCode != null) {
                    //Bitmap resize_bitmap = Bitmap.createScaledBitmap(barCode, pxToDp((int) (width * badge_data.canvasWidth)), pxToDp((int) (height * badge_data.canvasWidth)), true);
                    //Bitmap resize_bitmap = Util.db.getResizedBitmap(barCode,);
                    img_qrcode.setImageBitmap(barCode);
                    img_qrcode.setScaleType(ScaleType.FIT_XY);
                    badge_layout.addView(img_qrcode);
                }

            }else if (layer_values.layerType.equalsIgnoreCase("QR_CODE") || layer_values.layerType.equalsIgnoreCase("BAR_CODE")) {
                // continue;
                ImageView img_qrcode = new ImageView(context);
                double width = (double) layer_values.width * PIXEL_FOR_INCH;
                double height = (double) layer_values.height * PIXEL_FOR_INCH;
                double x = (double) (layer_values.x * PIXEL_FOR_INCH);
                double y = (double) (layer_values.y * PIXEL_FOR_INCH);


                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                img_qrcode.setLayoutParams(params);
                img_qrcode.setX(pxToDp((int) (x * badge_data.canvasWidth)));
                img_qrcode.setY(pxToDp((int) (y * badge_data.canvasWidth)));
                Bitmap barCode = null;
                String badgeid="";
                if (att_cursor != null) {
                    if(Util.NullChecker(Util.NullChecker(att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID)))).isEmpty()) {
                        badgeid=Util.NullChecker(att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)));
                    }else if(!(Util.db.getAttendeeParentTicketBadgeId(Util.NullChecker(Util.NullChecker(att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID)))))).isEmpty()) {
                        badgeid=Util.db.getAttendeeParentTicketBadgeId(Util.NullChecker(Util.NullChecker(att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID)))));
                    }else {
                        badgeid=Util.NullChecker(att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)));
                    }
                    barCode = encodeQrCode(badgeid, pxToDp((int) (width * badge_data.canvasWidth)), pxToDp((int) (height * badge_data.canvasWidth)), layer_values.layerType);

                    //barCode = encodeQrCode(att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)), pxToDp((int) (width * badge_data.canvasWidth)), pxToDp((int) (height * badge_data.canvasWidth)), layer_values.layerType);
                } else {
                    barCode = encodeQrCode(layer_values.content, pxToDp((int) (width * badge_data.canvasWidth)), pxToDp((int) (height * badge_data.canvasWidth)), layer_values.layerType);
                }
                if (barCode != null) {
                    //Bitmap resize_bitmap = Bitmap.createScaledBitmap(barCode, pxToDp((int) (width * badge_data.canvasWidth)), pxToDp((int) (height * badge_data.canvasWidth)), true);
                    //Bitmap resize_bitmap = Util.db.getResizedBitmap(barCode,);
                    img_qrcode.setImageBitmap(barCode);
                    img_qrcode.setScaleType(ScaleType.FIT_XY);
                    badge_layout.addView(img_qrcode);
                }

            } else if (layer_values.layerType.equalsIgnoreCase("ATTENDEE_DATA") || layer_values.layerType.equalsIgnoreCase("TEXT")) {
                ////Log.i("-----------------Layer Name---------", ":" + layer_values.layerType);
                //TextView txt_view = new TextView(context);
                if(layer_values.rotation!=0){
                    RotateLayout outter_layout = new RotateLayout(context);
                    AutoResizeTextView txt_view = new AutoResizeTextView(context);
                    //JustifyTextView txt_view = new JustifyTextView(context);
                    double width = (layer_values.width * PIXEL_FOR_INCH);
                    double height = (layer_values.height * PIXEL_FOR_INCH);
                    int x = (int) (layer_values.x * PIXEL_FOR_INCH);
                    int y = (int) (layer_values.y * PIXEL_FOR_INCH);
                    Log.i(" TextView WIDTH ", ":" + pxToDp((int) (width * badge_data.canvasWidth)) + " HEIGHT: " + pxToDp((int) ((height * badge_data.canvasWidth))));
                    //Log.i("TextView in dp Width",":" + pxToDp((int) (width * badge_data.canvasWidth))/displayMetrics.density + " Height:" + pxToDp((int) ((height * badge_data.canvasWidth)))/displayMetrics.density);

                    LayoutParams params = new LayoutParams(pxToDp((int) (height * badge_data.canvasWidth)), pxToDp(40));
                    params = new LayoutParams(pxToDp((int) ((width) * badge_data.canvasWidth)), pxToDp((int) (((height) * badge_data.canvasWidth))));
                    ////Log.i("--------------X, Y Values--------------", ":" + x * badge_data.canvasWidth+ " : " + y * badge_data.canvasWidth);
                    //params.gravity=Gravity.CENTER_VERTICAL;
                    outter_layout.setLayoutParams(params);
                    //outter_layout.setBackgroundColor(context.getResources().getColor(R.color.green_flat_color));
                    outter_layout.setX(pxToDp((int) (x * badge_data.canvasWidth)));
                    outter_layout.setY(pxToDp((int) (y * badge_data.canvasWidth)));


                    if (layer_values.rotation != 0) {
                        txt_view.setGravity(Gravity.VERTICAL_GRAVITY_MASK | Gravity.BOTTOM);
                    } else if (layer_values.align.equalsIgnoreCase("right")) {
                        txt_view.setGravity(Gravity.RIGHT);
                    } else if (layer_values.align.equalsIgnoreCase("left")) {
                        txt_view.setGravity(Gravity.LEFT);
                    } else if (layer_values.align.equalsIgnoreCase("center")) {
                        txt_view.setGravity(Gravity.CENTER);
                    }
                    txt_view.setGravity(Gravity.CENTER);
                    if (layer_values.fontFamily.contains("Arial")) {

                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.arial, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.arial, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.arial, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.arial, Typeface.NORMAL);
                        }

                    } else if (layer_values.fontFamily.contains("Droid Serif")) {
                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.droid_regrular, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.droid_regrular, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.droid_regrular, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.droid_regrular, Typeface.NORMAL);
                        }
                    } else if (layer_values.fontFamily.contains("Raleway")) {
                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.raleway_regular, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.raleway_regular, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.raleway_regular, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.raleway_regular, Typeface.NORMAL);
                        }
                    } else if (layer_values.fontFamily.contains("Futura")) {
                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.futura, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.futura, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.futura, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.futura, Typeface.NORMAL);
                        }
                    } else if (layer_values.fontFamily.contains("Brush Script")) {
                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.brush_scripts, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.brush_scripts, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.brush_scripts, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.brush_scripts, Typeface.NORMAL);
                        }
                    } else if (layer_values.fontFamily.contains("Papyrus")) {
                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.papyrus, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.papyrus, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.papyrus, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.papyrus, Typeface.NORMAL);
                        }
                    } else if (layer_values.fontFamily.contains("Verdana")) {
                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.verdana_bold, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.verdana_bold, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.verdana_regular, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.verdana_regular, Typeface.NORMAL);
                        }
                    } else if (layer_values.fontFamily.contains("Times New Roman")) {
                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.times_roman, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.times_roman, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.times_roman, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.times_roman, Typeface.NORMAL);
                        }
                    }

                    if(att_cursor != null && layer_values.layerType.equalsIgnoreCase("ATTENDEE_DATA")&&layer_values.dummyLabel.equals("Badge Label")&&!Util.NullChecker(layer_values.ticketname).trim().isEmpty()) {
                        List<OrderItemListHandler> childticketids=Util.db.getChildTicketIds(Util.NullChecker(att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID))));
                        String ticketid="";
                        //String[] temp=layer_values.ticketname.split(",");
                        if(layer_values.ticketname.contains(",")) {
                            String[] temp = layer_values.ticketname.split(",");
                            String badgelabel="";
                            //for (int i = 0; i < childticketids.size(); i++) {
                            for (int i = 0; i < temp.length; i++) {
                                for(int j = 0; j < childticketids.size(); j++) {
                                    if (Util.NullChecker(temp[i]).equalsIgnoreCase(childticketids.get(j).getItemPoolId())) {
                                        badgelabel=badgelabel+","+Util.NullChecker(childticketids.get(j).getBadgeLabel());
                                        //txt_view.setText(Util.NullChecker(childticketids.get(j).getBadgeLabel()));
                                    }
                                }
                                //if(Util.NullChecker(layer_values.ticketname).equalsIgnoreCase(childticketids.get(i).getItemPoolId())){
                            }
                            if(Util.NullChecker(badgelabel).length()>1&&Util.NullChecker(badgelabel).contains(",")){
                                if((badgelabel.charAt(badgelabel.length()-1))==','&&(badgelabel.charAt(0)==',')){
                                    badgelabel=badgelabel.substring(1,badgelabel.length()-1);
                                }else if(badgelabel.charAt(0)==','){
                                    badgelabel=badgelabel.substring(1);
                                }else if((badgelabel.charAt(badgelabel.length()-1))==','){
                                    badgelabel= badgelabel.replace(badgelabel.substring(badgelabel.length()-1),"");
                                }
                            }
                            txt_view.setText(Util.NullChecker(badgelabel).replaceAll("\\<.*?\\>", ""));

                        }

                    }else if (att_cursor != null && layer_values.layerType.equalsIgnoreCase("ATTENDEE_DATA")) {
                        setTextAttendee_Data(layer_values.dummyLabel, txt_view, att_cursor, layer_values.transform);
                    } else {
                        txt_view.setText(layer_values.content);
                        if (layer_values.transform.equalsIgnoreCase("uppercase")) {
                            txt_view.setText(layer_values.content.toUpperCase()+" ");
                        } else if (layer_values.transform.equalsIgnoreCase("lowercase")) {
                            txt_view.setText(layer_values.content.toLowerCase());
                        }
                    }

                    //if(layer_values.rotation==90 ||layer_values.rotation==-90) {
                    //textViewParams = new LayoutParams(pxToDp((int) (((height) * badge_data.canvasWidth))), pxToDp((int) ((width) * badge_data.canvasWidth)));
                    //textViewParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
					/*}else{
						textViewParams = new LayoutParams(pxToDp((int) (((height-(height/10)) * badge_data.canvasWidth))), pxToDp((int) (((width)-(width/10)) * badge_data.canvasWidth)));

						//textViewParams = new LayoutParams(pxToDp((int) ((((height+4)/2) * badge_data.canvasWidth))), pxToDp((int) (((width-2)/2) * badge_data.canvasWidth)));
					}*/
                    //textViewParams.gravity=Gravity.CENTER;
                    //LayoutParams textViewParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    //LayoutParams textViewParams=new LayoutParams(pxToDp((int) ((width) * badge_data.canvasWidth)), pxToDp((int) (((height) * badge_data.canvasWidth))));
                    //fill parent because text will cutting
                    LayoutParams textViewParams=new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
                    txt_view.setLayoutParams(textViewParams);
                    //txt_view.setBackgroundColor(R.color.fb_color);
                    int fontsize = (int)(height * badge_data.canvasWidth);
                    int twentyPer=(width_frame*10)/100;
                    int maxtextViewWidth=width_frame-twentyPer;
                    if(pxToDp(((int)height * (int)badge_data.canvasWidth))>maxtextViewWidth)
                        txt_view.setText(" "+txt_view.getText().toString()+" ");
					/*if (layer_values.transform.equalsIgnoreCase("uppercase")) {
						fontsize = fontsize - (fontsize * 10) / 100;
					}*/
                    //txt_view.setText(txt_view.getTxt().toString()+"  ");
                    if(layer_values.color.length()==7)
                        txt_view.setTextColor(Color.parseColor(layer_values.color));
                    txt_view.setSingleLine(true);
                    //txt_view.setEllipsize(TextUtils.TruncateAt.END);
                    txt_view.setTextSize(fontsize+fontsize+(fontsize*10/100));
					/*txt_view.setScaleY((float)1.2);
					txt_view.setScaleX((float)1);*/
                    //txt_view.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
                    //txt_view.getPaint().setMaskFilter(null);
                    txt_view.enableSizeCache(false);
                    txt_view.setPadding(1,0,1,0);
                    //txt_view.setBackgroundColor(context.getResources().getColor(R.color.light_gray));
                    outter_layout.setAngle(-(int)layer_values.rotation);
                    outter_layout.addView(txt_view);
                    badge_layout.addView(outter_layout);
                }else {

                    LinearLayout outter_layout = new LinearLayout(context);
                    //AutoResizeTextView txt_view = new AutoResizeTextView(context);
                    AutoResizeTextView txt_view = new AutoResizeTextView(context);
                    //com.globalnest.autoresizetextview.FontFitTextView txt_view=new FontFitTextView(context);
                    //JustifyTextView txt_view = new JustifyTextView(context);
                    //TextPaint paint = txt_view.getPaint();
                    double width = layer_values.width * PIXEL_FOR_INCH;
                    double height = layer_values.height * PIXEL_FOR_INCH;
                    int x = (int) (layer_values.x * PIXEL_FOR_INCH);
                    int y = (int) (layer_values.y * PIXEL_FOR_INCH);
                    Log.i(" TextView WIDTH ", ":" + pxToDp((int) (width * badge_data.canvasWidth)) + " HEIGHT: " + pxToDp((int) ((height * badge_data.canvasWidth))));
                    //Log.i("TextView in dp Width",":" + pxToDp((int) (width * badge_data.canvasWidth))/displayMetrics.density + " Height:" + pxToDp((int) ((height * badge_data.canvasWidth)))/displayMetrics.density);
                    LayoutParams params;
                    params = new LayoutParams(pxToDp((int) ((width) * badge_data.canvasWidth)), pxToDp((int) (((height) * badge_data.canvasWidth))));

                    outter_layout.setLayoutParams(params);
                    outter_layout.setX(pxToDp((int) (x * badge_data.canvasWidth)));
                    outter_layout.setY(pxToDp((int) (y * badge_data.canvasWidth)));


                    if (layer_values.align.equalsIgnoreCase("right")) {
                        txt_view.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
                    } else if (layer_values.align.equalsIgnoreCase("left")) {
                        txt_view.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
                    } else if (layer_values.align.equalsIgnoreCase("center")) {
                        txt_view.setGravity(Gravity.CENTER|Gravity.CENTER_VERTICAL);
                    }

                    if (layer_values.fontFamily.contains("Arial")) {

                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.arial, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.arial, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.arial, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.arial, Typeface.NORMAL);
                        }

                    } else if (layer_values.fontFamily.contains("Droid Serif")) {
                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.droid_regrular, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.droid_regrular, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.droid_regrular, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.droid_regrular, Typeface.NORMAL);
                        }
                    } else if (layer_values.fontFamily.contains("Raleway")) {
                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.raleway_regular, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.raleway_regular, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.raleway_regular, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.raleway_regular, Typeface.NORMAL);
                        }
                    } else if (layer_values.fontFamily.contains("Futura")) {
                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.futura, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.futura, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.futura, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.futura, Typeface.NORMAL);
                        }
                    } else if (layer_values.fontFamily.contains("Brush Script")) {
                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.brush_scripts, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.brush_scripts, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.brush_scripts, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.brush_scripts, Typeface.NORMAL);
                        }
                    } else if (layer_values.fontFamily.contains("Papyrus")) {
                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.papyrus, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.papyrus, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.papyrus, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.papyrus, Typeface.NORMAL);
                        }
                    } else if (layer_values.fontFamily.contains("Verdana")) {
                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.verdana_bold, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.verdana_bold, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.verdana_regular, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.verdana_regular, Typeface.NORMAL);
                        }
                    } else if (layer_values.fontFamily.contains("Times New Roman")) {
                        if (layer_values.fontWeight.equalsIgnoreCase("700") && layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.times_roman, Typeface.BOLD_ITALIC);
                        } else if (layer_values.fontWeight.equalsIgnoreCase("700")) {
                            txt_view.setTypeface(Util.times_roman, Typeface.BOLD);
                        } else if (layer_values.fontStyle.equalsIgnoreCase("italic")) {
                            txt_view.setTypeface(Util.times_roman, Typeface.ITALIC);
                        } else {
                            txt_view.setTypeface(Util.times_roman, Typeface.NORMAL);
                        }
                    }
                    //String txt_value="";
                    AppUtils.displayLog("----------------Device Width---------------", ":" + this.width);


                    if(att_cursor != null && layer_values.layerType.equalsIgnoreCase("ATTENDEE_DATA")&&layer_values.dummyLabel.equals("Badge Label")&&!Util.NullChecker(layer_values.ticketname).trim().isEmpty()) {
                        List<OrderItemListHandler> childticketids=Util.db.getChildTicketIds(Util.NullChecker(att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID))));
                        String ticketid="";
                        //String[] temp=layer_values.ticketname.split(",");
                        if(layer_values.ticketname.contains(",")) {
                            String[] temp = layer_values.ticketname.split(",");
                            String badgelabel="";
                            //for (int i = 0; i < childticketids.size(); i++) {
                            for (int i = 0; i < temp.length; i++) {
                                for(int j = 0; j < childticketids.size(); j++) {
                                    if (Util.NullChecker(temp[i]).equalsIgnoreCase(childticketids.get(j).getItemPoolId())) {
                                        badgelabel=badgelabel+","+Util.NullChecker(childticketids.get(j).getBadgeLabel());
                                        //txt_view.setText(Util.NullChecker(childticketids.get(j).getBadgeLabel()));
                                    }
                                }
                                //if(Util.NullChecker(layer_values.ticketname).equalsIgnoreCase(childticketids.get(i).getItemPoolId())){
                            }
                            if(Util.NullChecker(badgelabel).length()>1&&Util.NullChecker(badgelabel).contains(",")){
                                if((badgelabel.charAt(badgelabel.length()-1))==','&&(badgelabel.charAt(0)==',')){
                                    badgelabel=badgelabel.substring(1,badgelabel.length()-1);
                                }else if(badgelabel.charAt(0)==','){
                                    badgelabel=badgelabel.substring(1);
                                }else if((badgelabel.charAt(badgelabel.length()-1))==','){
                                    badgelabel= badgelabel.replace(badgelabel.substring(badgelabel.length()-1),"");
                                }
                            }
                            txt_view.setText(Util.NullChecker(badgelabel).replaceAll("\\<.*?\\>", ""));

                        }else {
                            String badgelabel="";
                            for(int j = 0; j < childticketids.size(); j++) {
                                if (Util.NullChecker(layer_values.ticketname).equalsIgnoreCase(childticketids.get(j).getItemPoolId())) {
                                    badgelabel=badgelabel+","+Util.NullChecker(childticketids.get(j).getBadgeLabel());
                                    //txt_view.setText(Util.NullChecker(childticketids.get(j).getBadgeLabel()));
                                }
                            }
                            if(Util.NullChecker(badgelabel).length()>1&&Util.NullChecker(badgelabel).contains(",")){
                                if((badgelabel.charAt(badgelabel.length()-1))==','&&(badgelabel.charAt(0)==',')){
                                    badgelabel=badgelabel.substring(1,badgelabel.length()-1);
                                }else if(badgelabel.charAt(0)==','){
                                    badgelabel=badgelabel.substring(1);
                                }else if((badgelabel.charAt(badgelabel.length()-1))==','){
                                    badgelabel= badgelabel.replace(badgelabel.substring(badgelabel.length()-1),"");
                                }
                            }
                            txt_view.setText(Util.NullChecker(badgelabel).replaceAll("\\<.*?\\>", ""));
                        }

                    }else if (att_cursor != null && layer_values.layerType.equalsIgnoreCase("ATTENDEE_DATA")) {
                        setTextAttendee_Data(layer_values.dummyLabel, txt_view, att_cursor, layer_values.transform);
                    } else {
                        txt_view.setText(layer_values.content);
                        if (layer_values.transform.equalsIgnoreCase("uppercase")) {
                            txt_view.setText(layer_values.content.toUpperCase());
                        } else if (layer_values.transform.equalsIgnoreCase("lowercase")) {
                            txt_view.setText(layer_values.content.toLowerCase());
                        }
                    }
                    double twentyPer=(width_frame*10)/100;
                    Log.i("TextView check ", ":" + pxToDp((int) (width * badge_data.canvasWidth)) + " BadgeLayout: " + (pxToDp(width_frame)));
                    //LayoutParams textViewParams = new LayoutParams(pxToDp((int) (width * badge_data.canvasWidth)), pxToDp((int) (height * badge_data.canvasWidth)));
                    //LayoutParams textViewParams=new LayoutParams(pxToDp((int) ((width) * badge_data.canvasWidth)), pxToDp((int) (((height) * badge_data.canvasWidth))));
                    LayoutParams textViewParams=new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
                    txt_view.setLayoutParams(textViewParams);
                    //txt_view.setBackgroundColor(R.color.fb_color);
                    int fontsize=(int)(height* badge_data.canvasWidth);
                    //int fontsize = (int)(height * badge_data.canvasWidth);
                    //int twentyPer=(width_frame*2)/100;
                    //if (layer_values.transform.equalsIgnoreCase("uppercase"))
                    double maxtextViewWidth=width_frame-twentyPer;
                    //txt_view.setText(txt_view.getText().toString()+"  ");
                    if(pxToDp((int)(width * badge_data.canvasWidth))>maxtextViewWidth&&forprint) {
                        txt_view.setText(" "+txt_view.getText().toString()+" ");
						/*if(txt_view.getText().toString().length()>4&&txt_view.getText().toString().length()<10){
							txt_view.setText(txt_view.getText().toString()+"  ");
						}else if(txt_view.getText().toString().length()>10&&txt_view.getText().toString().length()<20){
							txt_view.setText(txt_view.getText().toString()+"   ");
						}else if(txt_view.getText().toString().length()>20&&txt_view.getText().toString().length()<30){
							txt_view.setText(txt_view.getText().toString()+"    ");
						}else if(txt_view.getText().toString().length()>30) {
							txt_view.setText(txt_view.getText().toString()+"      ");
						}*/

                        fontsize=fontsize-(fontsize*10)/100;
                    }
                    if(layer_values.color.length()==7)
                        txt_view.setTextColor(Color.parseColor(layer_values.color));
                    txt_view.setSingleLine(true);
                    //txt_view.setMaxLines(1);
                    //txt_view.setEllipsize(TextUtils.TruncateAt.END);
                    if (layer_values.transform.equalsIgnoreCase("uppercase")&&forprint) {
						/*if(txt_view.getText().toString().length()>4&&txt_view.getText().toString().length()<10){
							txt_view.setText(txt_view.getText().toString());
						}else if(txt_view.getText().toString().length()>10&&txt_view.getText().toString().length()<20){
							txt_view.setText(txt_view.getText().toString()+" ");
						}else if(txt_view.getText().toString().length()>20&&txt_view.getText().toString().length()<30){
							txt_view.setText(txt_view.getText().toString()+"  ");
						}else if(txt_view.getText().toString().length()>30) {
							txt_view.setText(txt_view.getText().toString()+"    ");
						}*/

                        //fontsize=fontsize-(fontsize*10)/100;

                        if(txt_view.getText().toString().length()>20) {
                            fontsize=fontsize-(fontsize*10)/100;
                            txt_view.setText(" "+txt_view.getText().toString()+"   ");
                        }
                    }else if (layer_values.transform.equalsIgnoreCase("uppercase")) {
                        fontsize = fontsize - (fontsize * 10) / 100;
                        txt_view.setText(txt_view.getText().toString()+" ");
                        if(txt_view.getText().toString().length()>20) {
                            fontsize=fontsize-(fontsize*10)/100;
                            txt_view.setText(txt_view.getText().toString()+"   ");
                        }
                    }
                    txt_view.setTextSize(fontsize+fontsize+(fontsize*10/100));
					/*txt_view.setScaleY((float)1.2);
					txt_view.setScaleY((float)1);*/
                    //txt_view.setTextSize(fontsize);
					/*txt_view.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
					txt_view.getPaint().setMaskFilter(null);*/
                    txt_view.enableSizeCache(false);
                    //txt_view.setPadding(1,0,1,0);

                    int[] a=new int[fontsize+10];
                    //TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(txt_view,1,fontsize,2,TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
					/*TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(txt_view,1,fontsize,2,TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
					TextViewCompat.setTextAppearance(txt_view,TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);*/
                    //TextViewCompat.setAutoSizeTextTypeUniformWithPresetSizes(txt_view,a,TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                    //correctWidth(txt_view,(width*((int)badge_data.canvasWidth)),fontsize );
                    //txt_view.setBackgroundColor(context.getResources().getColor(R.color.light_gray));
                    //outter_layout.setBackgroundColor(R.color.black);
                    outter_layout.addView(txt_view);
                    //resize(txt_view,layer_values.content,pxToDp((int) ((width) * badge_data.canvasWidth)), pxToDp((int) (((height-1) * badge_data.canvasWidth))));
                    badge_layout.addView(outter_layout);


                }

            } else if (layer_values.layerType.equalsIgnoreCase("SHAPE")) {
                if (layer_values.dummyLabel.equalsIgnoreCase("Rectangle")) {
                    FrameLayout rect_layout = new FrameLayout(context);
                    double width = (layer_values.width * PIXEL_FOR_INCH);
                    double height = (layer_values.height * PIXEL_FOR_INCH);
                    int x = (int) (layer_values.x * PIXEL_FOR_INCH);
                    int y = (int) (layer_values.y * PIXEL_FOR_INCH);

                    LayoutParams params = new LayoutParams(pxToDp((int) (width * badge_data.canvasWidth)),
                            pxToDp((int) (height * badge_data.canvasWidth)));
                    ////Log.i("--------------X, Y Values--------------", ":" + x * badge_data.canvasWidth+ " : " + y * badge_data.canvasWidth);
                    rect_layout.setLayoutParams(params);
                    rect_layout.setX(pxToDp((int) (x * badge_data.canvasWidth)));
                    rect_layout.setY(pxToDp((int) (y * badge_data.canvasWidth)));
                    if(layer_values.color.length()==7)
                        rect_layout.setBackgroundColor(Color.parseColor(layer_values.color));
                    badge_layout.addView(rect_layout);
                } else if (layer_values.dummyLabel.equalsIgnoreCase("Circle")) {
                    CircleButton circle = new CircleButton(context);
                    double width = (int) (layer_values.width * PIXEL_FOR_INCH);
                    double height = (int) (layer_values.height * PIXEL_FOR_INCH);
                    int x = (int) (layer_values.x * PIXEL_FOR_INCH);
                    int y = (int) (layer_values.y * PIXEL_FOR_INCH);

                    LayoutParams params = new LayoutParams(pxToDp((int) (width * badge_data.canvasWidth) + 10),
                            pxToDp((int) (height * badge_data.canvasWidth) + 10));
                    circle.setLayoutParams(params);
                    //DrawView view = new DrawView(context, pxToDp((int) (x * badge_data.canvasWidth)), pxToDp((int) (y * badge_data.canvasWidth)), pxToDp((int) (width * badge_data.canvasWidth))/2, layer_values.color);
                    circle.setX(pxToDp((int) (x * badge_data.canvasWidth)));
                    circle.setY(pxToDp((int) (y * badge_data.canvasWidth)));
                    if(layer_values.color.length()==7)
                        circle.setColor(Color.parseColor(layer_values.color));
                    badge_layout.addView(circle);
                }

            }

            if(forprint){
                if (layer_values.layerType.equalsIgnoreCase("IMAGE") && layer_values.image_type.equalsIgnoreCase("static")) {
                    ImageView image = new ImageView(context);
                    int width = (int) (layer_values.width * PIXEL_FOR_INCH);
                    int height = (int) (layer_values.height * PIXEL_FOR_INCH);
                    int x = (int) (layer_values.x * PIXEL_FOR_INCH);
                    int y = (int) (layer_values.y * PIXEL_FOR_INCH);

                    LayoutParams params = new LayoutParams(pxToDp((int) (width * badge_data.canvasWidth)),
                            pxToDp((int) (height * badge_data.canvasWidth)));
                    ////Log.i("--------------X, Y Values--------------", ":" + x * badge_data.canvasWidth+ " : " + y * badge_data.canvasWidth);
                    image.setLayoutParams(params);
                    image.setX(pxToDp((int) (x * badge_data.canvasWidth)));
                    image.setY(pxToDp((int) (y * badge_data.canvasWidth)));
                    image.setScaleType(ScaleType.FIT_XY);
                    //  if(isOnline1()){
                    if(Util.NullChecker(layer_values.image).contains("id=")){
                        String [] as=layer_values.image.split("id=");
                        String imagename=as[1].replace("&o","");

                        File path = Environment.getExternalStorageDirectory();
                        File imgFile = new  File(path+"/ScanAttendee/Badge Images/" +imagename+".png");
                        if(imgFile.exists()){
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            image.setImageBitmap(myBitmap);

                        }else {
                            String image_url = WebServiceUrls.SA_PRODUCTION + layer_values.image;
                            Glide.with(context).load(image_url).dontAnimate().into(image);
                        }
                    }else {
                        String image_url = WebServiceUrls.SA_PRODUCTION + layer_values.image;
                        //String image_url = "https://qa4-eventdex.cs51.force.com/ScanAttendee/servlet/servlet.FileDownload?file=0154B0000000l8rQAA&oid=00D4B000000D0rkUAC";
                        //String image_url = WebServiceUrls.SA_PRODUCTION+layer_values.image;
                        Glide.with(context).load(image_url).dontAnimate().into(image);
                    }
                    //}
                    /*else {
                        String image_url = WebServiceUrls.SA_PRODUCTION + layer_values.image;
                        //String image_url = "https://qa4-eventdex.cs51.force.com/ScanAttendee/servlet/servlet.FileDownload?file=0154B0000000l8rQAA&oid=00D4B000000D0rkUAC";
                        //String image_url = WebServiceUrls.SA_PRODUCTION+layer_values.image;
                        Glide.with(context).load(image_url).dontAnimate().into(image);
					*//*Picasso.with(context).load(image_url)
							.placeholder(R.drawable.badge_sample_image)
							.error(R.drawable.badge_sample_image).into(image);*//*
                    }*/
                    badge_layout.addView(image);

                }
                if(layer_values.layerType.equalsIgnoreCase("IMAGE") && layer_values.image_type.equalsIgnoreCase("Attendee")){
                    ImageView image = new ImageView(context);
                    int width = (int) (layer_values.width * PIXEL_FOR_INCH);
                    int height = (int) (layer_values.height * PIXEL_FOR_INCH);
                    int x = (int) (layer_values.x * PIXEL_FOR_INCH);
                    int y = (int) (layer_values.y * PIXEL_FOR_INCH);

                    LayoutParams params = new LayoutParams(pxToDp((int) (width * badge_data.canvasWidth)),
                            pxToDp((int) (height * badge_data.canvasWidth)));
                    ////Log.i("--------------X, Y Values--------------", ":" + x * badge_data.canvasWidth+ " : " + y * badge_data.canvasWidth);
                    image.setLayoutParams(params);
                    image.setX(pxToDp((int) (x * badge_data.canvasWidth)));
                    image.setY(pxToDp((int) (y * badge_data.canvasWidth)));
                    image.setScaleType(ScaleType.FIT_XY);
                    String[] fullurl= BaseActivity.checkedin_event_record.image.split("&id=");

                    String image_url = fullurl[0]+"&id="+att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_IMAGE));

                    //String image_url = WebServiceUrls.SA_PRODUCTION+layer_values.image;
                    if(att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_IMAGE)).length()>0){
                        Glide.with(context).load(image_url).dontAnimate().into(image);
						/*Picasso.with(context).load(image_url)
								.placeholder(R.drawable.badge_sample_image)
								.error(R.drawable.badge_sample_image).into(image);*/
                        badge_layout.addView(image);
                    }
                    else{
                        //String url = "https://qa4-eventdex.cs51.force.com/ScanAttendee/servlet/servlet.FileDownload?file=0154B0000000l8rQAA&oid=00D4B000000D0rkUAC";
                        String url = WebServiceUrls.SA_PRODUCTION+layer_values.image;
                        //String url = WebServiceUrls.SA_PRODUCTION+layer_values.image;
						/*Picasso.with(context).load(url)
								.placeholder(R.drawable.badge_sample_image)
								.error(R.drawable.badge_sample_image).into(image);*/
                        Glide.with(context).load(image_url).dontAnimate().into(image);
                    }
                    //String image_url = WebServiceUrls.SA_PRODUCTION+layer_values.image;


                }
                if(layer_values.layerType.equalsIgnoreCase("IMAGE") && layer_values.image_type.equalsIgnoreCase("company")){
                    ImageView image = new ImageView(context);
                    int width = (int) (layer_values.width * PIXEL_FOR_INCH);
                    int height = (int) (layer_values.height * PIXEL_FOR_INCH);
                    int x = (int) (layer_values.x * PIXEL_FOR_INCH);
                    int y = (int) (layer_values.y * PIXEL_FOR_INCH);

                    LayoutParams params = new LayoutParams(pxToDp((int) (width * badge_data.canvasWidth)),
                            pxToDp((int) (height * badge_data.canvasWidth)));
                    ////Log.i("--------------X, Y Values--------------", ":" + x * badge_data.canvasWidth+ " : " + y * badge_data.canvasWidth);
                    image.setLayoutParams(params);
                    image.setX(pxToDp((int) (x * badge_data.canvasWidth)));
                    image.setY(pxToDp((int) (y * badge_data.canvasWidth)));
                    image.setScaleType(ScaleType.FIT_XY);
                    String[] fullurl= BaseActivity.checkedin_event_record.image.split("&id=");

                    String image_url = fullurl[0]+"&id="+att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_COMPANY_LOGO));

                    //String image_url = WebServiceUrls.SA_PRODUCTION+layer_values.image;
                    if(att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_COMPANY_LOGO)).length()>0){
						/*Picasso.with(context).load(image_url)
								.placeholder(R.drawable.badge_sample_image)
								.error(R.drawable.badge_sample_image).into(image);*/
                        Glide.with(context).load(image_url).dontAnimate().into(image);
                        badge_layout.addView(image);
                    }
                    else{
                        String url = WebServiceUrls.SA_PRODUCTION+layer_values.image;
                        //String url = "https://qa4-eventdex.cs51.force.com/ScanAttendee/servlet/servlet.FileDownload?file=0154B0000000l8rQAA&oid=00D4B000000D0rkUAC";
                        Glide.with(context).load(image_url).dontAnimate().into(image);
						/*Picasso.with(context).load(url)
								.placeholder(R.drawable.badge_sample_image)
								.error(R.drawable.badge_sample_image).into(image);*/
                    }
                    //String image_url = WebServiceUrls.SA_PRODUCTION+layer_values.image;


                }}
            else if (layer_values.layerType.equalsIgnoreCase("IMAGE")&& !forprint) {
                ImageView image = new ImageView(context);
                int width = (int) (layer_values.width * PIXEL_FOR_INCH);
                int height = (int) (layer_values.height * PIXEL_FOR_INCH);
                int x = (int) (layer_values.x * PIXEL_FOR_INCH);
                int y = (int) (layer_values.y * PIXEL_FOR_INCH);

                LayoutParams params = new LayoutParams(pxToDp((int) (width * badge_data.canvasWidth)),
                        pxToDp((int) (height * badge_data.canvasWidth)));
                ////Log.i("--------------X, Y Values--------------", ":" + x * badge_data.canvasWidth+ " : " + y * badge_data.canvasWidth);
                image.setLayoutParams(params);
                image.setX(pxToDp((int) (x * badge_data.canvasWidth)));
                image.setY(pxToDp((int) (y * badge_data.canvasWidth)));
                image.setScaleType(ScaleType.FIT_XY);
                //String image_url = WebServiceUrls.SA_PRODUCTION+layer_values.image;
                String image_url = WebServiceUrls.SA_PRODUCTION+layer_values.image;
                //String image_url = "https://qa4-eventdex.cs51.force.com/ScanAttendee/servlet/servlet.FileDownload?file=0154B0000000l8rQAA&oid=00D4B000000D0rkUAC";
				/*Glide.with(context).load(image_url).diskCacheStrategy(DiskCacheStrategy.ALL)
						.dontAnimate().into(image);*/
                Glide.with(context).load(image_url)
                        .dontAnimate().into(image);
                badge_layout.addView(image);
                String imagename="";
                try {
                    if(layer_values.image.contains("?id=")){
                        String [] as=layer_values.image.split("id=");
                        imagename=as[1].replace("&o","");
                    }
                    URL url = new URL(image_url);
                    Bitmap bb = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    saveImageToExternal(imagename,bb);
                } catch(IOException e) {
                    System.out.println(e);
                }





/*
				Picasso.with(context).load(image_url)
						.placeholder(R.drawable.badge_sample_image).noFade()
						.error(R.drawable.badge_sample_image).into(image);
				badge_layout.addView(image);*/
            }
        }

        //Bitmap b1 = viewToBitmap(img_badge, width_frame,height_frame);
        //badge_layout.setVisibility(View.INVISIBLE);
		/*File root = android.os.Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath() + "/ScanAttendee/Badges");
		String file_path = dir.toString() + "/" + badge_res.badge.Description__c+badge_res.badge.Name + ".png";
		Bitmap b =  BitmapFactory.decodeFile(file_path);*/

        //img_badge.setImageBitmap(b1);
    }
    public void saveImageToExternal(String imgName, Bitmap bm) throws IOException {
        //Create Path to save Image
        String newFolder = "/Badge Images";
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

        File imageFile = new File(filename , imgName+".png"); // Imagename.png
        FileOutputStream out = new FileOutputStream(imageFile);
        try{
            bm.compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
            out.flush();
            out.close();

            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(context,new String[] { imageFile.getAbsolutePath() }, null,new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                }
            });
        } catch(Exception e) {
            throw new IOException();
        }
    }
    public  int pxToDp(int px) {
		/*DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		Configuration config = context.getResources().getConfiguration();
		int densityDpi = (int)(displayMetrics.density * 160f);
		displayMetrics.densityDpi = densityDpi;
		config.densityDpi = DisplayMetrics.DENSITY_MEDIUM;
		displayMetrics.setTo(displayMetrics);
		config.setTo(config);
		context.getResources().updateConfiguration(config, displayMetrics);*/
        int valueInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px,
                context.getResources().getDisplayMetrics());

		/*DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		Configuration config = contex
		t.getResources().getConfiguration();
		displayMetrics.densityDpi = DisplayMetrics.DENSITY_HIGH;
		config.densityDpi = DisplayMetrics.DENSITY_HIGH;
		displayMetrics.setTo(displayMetrics);
		config.setTo(config);
		context.getResources().updateConfiguration(config, displayMetrics);*/


		/*DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		switch(displayMetrics.densityDpi){
			case DisplayMetrics.DENSITY_LOW:
				//set text-size for low-density devices.
				break;

			case DisplayMetrics.DENSITY_MEDIUM:
				//set text-size for medium-density devices.
				break;
			case DisplayMetrics.DENSITY_HIGH:
				//set text-size for high-density devices.
				break;
		}*/
        return valueInDp;
    }

    public Bitmap encodeQrCode(String data,int width, int height,String layerType) {

        Bitmap b = null;
        try {

            // smallerDimension = smallerDimension * 2;
            // Log.i("--------------Smaller
            // Dimensions--------------",":"+smallerDimension);
            int smallerDimension = width < height ? width : height;
            //int smallerDimension = width > height ? width : height;
            Log.i("----layerType------",""+layerType);
            if (layerType.equalsIgnoreCase("QR_CODE")) {
                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(data, null, Contents.Type.TEXT,
                        BarcodeFormat.QR_CODE.toString(), smallerDimension);
                b = qrCodeEncoder.encodeAsBitmap(data, BarcodeFormat.QR_CODE, width, height);
            }else if(layerType.equalsIgnoreCase("Custom_QR_CODE")){
                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(data, null, Contents.Type.TEXT,
                        BarcodeFormat.QR_CODE.toString(), smallerDimension);
                b = qrCodeEncoder.encodeAsBitmap(data, BarcodeFormat.QR_CODE, width, height);
            }else if(layerType.equalsIgnoreCase("Custom_Bar_CODE")){
                smallerDimension = smallerDimension * 2;
                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(data, null, Contents.Type.TEXT,
                        BarcodeFormat.CODE_128.toString(), smallerDimension);
                b = qrCodeEncoder.encodeAsBitmap(data, BarcodeFormat.CODE_128, width, height);
            }else{
                smallerDimension = smallerDimension * 2;
                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(data, null, Contents.Type.TEXT,
                        BarcodeFormat.CODE_128.toString(), smallerDimension);
                b = qrCodeEncoder.encodeAsBitmap(data, BarcodeFormat.CODE_128, width, height);

            }

        } catch (Exception e) {

            e.printStackTrace();
        }
        return b;
    }

    public static Bitmap viewToBitmap(View view, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /*public class DrawView extends View
    {
        Paint paint;
        float x,y,radius;
        String color;
        public DrawView(Context context, AttributeSet attrs)
        {
            super(context, attrs);
        }

        public DrawView(Context context,float x,float y,float radius,String color)
        {
            super(context);
            paint = new Paint();
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = color;
        }

        *//*@Override
		protected void onDraw(Canvas canvas)
		{
			super.onDraw(canvas);
			paint.setColor(Color.parseColor(color));
			paint.setStrokeWidth(3);
			canvas.drawCircle(x, y, radius, paint);

	       *//**//* canvas.drawRect(30, 30, 80, 80, paint);
	        paint.setStrokeWidth(0);
	        paint.setColor(Color.CYAN);
	        canvas.drawRect(33, 60, 77, 77, paint );
	        paint.setColor(Color.YELLOW);
	        canvas.drawRect(33, 33, 77, 60, paint );*//**//*
		}*//*
	}*/
    public void setTextAttendee_Data(String dummylabel,TextView  txt_view,Cursor attendee_cursor,String transform) {
        String whereClause = " where "
                + DBFeilds.ORDER_EVENT_ID
                + " = '"
                + attendee_cursor.getString(attendee_cursor
                .getColumnIndex(DBFeilds.ATTENDEE_EVENT_ID))
                + "'"
                + " AND "
                + DBFeilds.ORDER_ORDER_ID
                + " = "
                + "'"
                + attendee_cursor.getString(attendee_cursor
                .getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID)) + "'";

        final Cursor orderdetail = Util.db.getPaymentCursor(whereClause);
        orderdetail.moveToFirst();
        if (!dummylabel.replace(" ", "").toString().isEmpty()) {
            FEILDS feild = FEILDS.valueOf(dummylabel.replace(" ", "").toUpperCase());
            String badgeName = "";

            switch (feild) {
                case FULLNAME:
                    String fullname = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_PREFIX))).replace(" ", "").trim() + " "
                            + Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))).replace(" ", "").trim() + " "
                            + Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME))).replace(" ", "").trim()  + " "
                            + Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_SUFFIX))).replace(" ", "").trim();
                    txt_view.setText(fullname);
                    badgeName = fullname;
                    break;
                case FIRSTNAME:
                    txt_view.setText(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))));
                    badgeName = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)));
                    break;
                case LASTNAME:
                    txt_view.setText(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME))));
                    badgeName = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));
                    break;
                case EMAIL:
                    txt_view.setText(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID))));
                    badgeName = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID)));
                    break;
                case COMPANY:
                    txt_view.setText(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_COMPANY))));
                    badgeName = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_COMPANY)));
                    break;
                case TITLE:
                    txt_view.setText(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_JOB_TILE))));
                    badgeName = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_JOB_TILE)));
                    break;
                case TICKETTYPE:
                    String item_type = Util.db.getItemTypeName(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_TYPE_ID))));
                    badgeName = Util.db.getItemTypeName(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_TYPE_ID))));
                    txt_view.setText(item_type);
                    break;

                case BADGELABEL:
                    if(Util.NullChecker(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID)))).isEmpty()) {
                        txt_view.setText(Util.NullChecker(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)))));
                        badgeName = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))).replaceAll("\\<.*?\\>", "");
                    }else if(Util.db.isParentTicketexistsinDB(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID))))){
                        txt_view.setText(Util.db.getAttendeeParentTicketBadgeLabel(Util.NullChecker(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID))))));
                        badgeName = Util.db.getAttendeeParentTicketBadgeLabel(
                                Util.NullChecker(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID))))).replaceAll("\\<.*?\\>", "");
                    }else {

                    }
                    //badgeName = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))).replaceAll("\\<.*?\\>", "");
                    break;
                case BADGESTATUS:
                    if (Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID))).isEmpty()) {
                        txt_view.setText("Not Printed");
                    } else {
                        txt_view.setText("Printed");
                    }
                    badgeName = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)));
                    break;
                case BADGEID:
                    txt_view.setText(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID))));
                    badgeName = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)));
                    break;
                case SEATNO:
                    txt_view.setText(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_TICKET_SEAT_NUMBER))));
                    badgeName = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_TICKET_SEAT_NUMBER)));
                    break;
                case PAYMENTSTATUS:
                    txt_view.setText(Util.NullChecker(orderdetail.getString(orderdetail.getColumnIndex(DBFeilds.ORDER_ORDER_STATUS))));
                    badgeName = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_STATUS)));
                    break;
                case ORDERID:
                    txt_view.setText(Util.NullChecker(orderdetail.getString(orderdetail.getColumnIndex(DBFeilds.ORDER_ORDER_NAME))));
                    badgeName = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_NAME)));
                    break;
                case TICKETID:
                    txt_view.setText(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_TIKCET_NUMBER))));
                    badgeName = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_TIKCET_NUMBER)));
                    break;
                case UNIQUEID:
                    txt_view.setText(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_SCANID))));
                    badgeName = Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_SCANID)));
                    break;
                case BUYERNAME:
                    String order_id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID));
                    Cursor c = Util.db.getGNUser(" " + DBFeilds.ORDER_ORDER_ID + " = '" + order_id + "'");
                    String buyer_name = ITransaction.EMPTY_STRING;
                    if (c.moveToFirst()) {
                        buyer_name = Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.USER_FIRST_NAME))).replace(" ", "").trim() + " " + Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.USER_LAST_NAME))).trim();
                    }
                    c.close();
                    txt_view.setText(buyer_name);
                    if (transform.equalsIgnoreCase("uppercase")) {
                        txt_view.setText(buyer_name.toUpperCase());
                    } else if (transform.equalsIgnoreCase("lowercase")) {
                        txt_view.setText(buyer_name.toLowerCase());
                    }
                    badgeName = buyer_name;
                default:
                    break;

            }

            txt_view.setText(badgeName);
            if (transform.equalsIgnoreCase("uppercase")) {
                txt_view.setText(badgeName.toUpperCase());
            } else if (transform.equalsIgnoreCase("lowercase")) {
                txt_view.setText(badgeName.toLowerCase());
            }else {
                txt_view.setText(badgeName);
            }
        }
    }

    public void sortLayers(ArrayList<BadgeLayerNew> layers){

        Collections.sort(layers, new Comparator<BadgeLayerNew>() {

            @Override
            public int compare(BadgeLayerNew lhs, BadgeLayerNew rhs) {
                // TODO Auto-generated method stub
                Gson gson = new Gson();
                BadgeDataNew data1 = gson.fromJson(lhs.Data__c, BadgeDataNew.class);
                BadgeDataNew data2 = gson.fromJson(rhs.Data__c, BadgeDataNew.class);
                return (int) (data1.zIndex - data2.zIndex);
            }
        });
    }



    public void correctWidth(TextView textView, int desiredWidth,int fontsize)
    {
        Paint paint = new Paint();
        Rect bounds = new Rect();

        paint.setTypeface(textView.getTypeface());
        float textSize = fontsize;
        paint.setTextSize(textSize);
        String text = textView.getText().toString();
        paint.getTextBounds(text, 0, text.length(), bounds);

        while (bounds.width() > desiredWidth)
        {
            textSize--;
            paint.setTextSize(textSize);
            paint.getTextBounds(text, 0, text.length(), bounds);
        }

        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
    }
    public boolean isOnline1() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()){
            try{
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
                HttpConnectionParams.setSoTimeout(httpParameters, 6000);
                HttpClient _httpclient = new DefaultHttpClient(httpParameters);
                HttpGet _httpget = new HttpGet("http://www.google.com");
                HttpResponse _response = _httpclient.execute(_httpget);
                int _responsecode =_response.getStatusLine().getStatusCode();

                if(_responsecode == 200){
                    return new Boolean(true);
                }else{
                    return false;
                }
            }catch(MalformedURLException e1){
                e1.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        return false;
    }

}
