package com.globalnest.retrofit.rest;

import com.globalnest.mvc.RefreshResponse;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.objects.LoginResponse;

import org.json.JSONObject;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by sailakshmi on 17-04-2018.
 */

public interface ApiInterface {


    @POST("BLN_ASC_AttendeeLoadMore")
    Call<TotalOrderListHandler> getAttendees(@Query("Event_id") String eventid,
                                             @Query("User_id") String userid,
                                             @Query("offset") String offset,
                                             @Query("limit") int limit,
                                             @Header("Authorization") String accesstoken);
    @POST("BLN_ASC_AttendeeLoadMore")
    Call<RefreshResponse> getRefreshAttendees(@Query("Event_id") String eventid,
                                              @Query("User_id") String userid,
                                              @Query("appname") String appname,
                                              @Query("LastModifiedDate") String LastModifiedDate,
                                              @Query("Request_Flag") String Request_Flag,
                                              @Query("startbatch") int startbatch,
                                              @Header("Authorization") String accesstoken);


    @POST("BLN_ASC_Events")
    Call<LoginResponse> getEvents(@Query("User_id") String userid,
                                  @Query("appname") String appname,
                                  @Header("Authorization") String accesstoken);
    /* @POST("BLN_ASC_AttendeeLoadMore{url}")
     Call<TotalOrderListHandler> getAttendeesurl(@Path("url") String url, @Header("Authorization") String accesstoken);*/
    @Headers({"Content-Type: application/json", "Cache-Control: max-age=640000" ,"charset: UTF-8"})
    @POST("BLN_ASC_WS_SellTickets")
    Call<TotalOrderListHandler> getSellTicketAttendees(@Query("eveid") String eveid,
                                                       @Query("userid") String userid,
                                                       @Query("PromoCode") String promocode,
                                                       @Body String jsonbody,
                                                       @Header("Authorization") String accesstoken);
    @Headers({"Content-Type: application/json", "Cache-Control: max-age=640000" ,"charset: UTF-8"})
    @POST("BLN_ASC_SearchAttendee")
    Call<TotalOrderListHandler> getSearchAttendees(@Query("Event_id") String eveid,
                                                   @Query("User_id") String userid,
                                                   @Query("search_string") String searchstring,
                                                   @Header("Authorization") String accesstoken);
    @POST("BLN_ASC_AttendeeDetails")
    Call<TotalOrderListHandler> getAttendeeDetailvalues(@Query("eventId") String eveid,
                                                        @Body String jsonbody,
                                                        @Header("Authorization") String accesstoken);
    @POST("surveys")
    Call<Void> setSurveys(@Body JSONObject json);

    @GET("www.google.com")
    Call<String> getIsNetAvailable();
}
