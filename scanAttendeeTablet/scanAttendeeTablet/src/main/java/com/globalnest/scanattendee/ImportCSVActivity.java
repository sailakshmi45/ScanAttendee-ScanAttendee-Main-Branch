package com.globalnest.scanattendee;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.globalnest.mvc.AttendeeDetailsController;
import com.globalnest.mvc.CollectionController;
import com.globalnest.mvc.TicketTypeContoller;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.utils.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVReader;

@SuppressLint("NewApi")
public class ImportCSVActivity extends BaseActivity{
	ArrayList<String> dropbox_data = new ArrayList<String>();
	ArrayList<String> dropbox_path = new ArrayList<String>();
	List<String[]> questionList;
	HashMap<Integer,String> colum_map=new HashMap<Integer,String>();
	ArrayList<String[]> failed_record_array = new ArrayList<String[]>();
	ArrayList<AttendeeDetailsController> attendee_controller = new ArrayList<AttendeeDetailsController>();
	String colum_name = "", colum_email = "", colum_ticketName = "",
			colum_ticketPrice = "", colum_ticetQty = "",
			colum_paymentStatus = "", colum_company = "",
			colum_designation = "", colum_soldQty = "",
			colum_paymentType = "", colum_workNum = "", colum_city = "",
			colum_state = "", colum_address = "", colum_zipcode = "",
			colum_barcode = "", seatNumber="";
	
	private int index=0,failed_record=0, total_count=0;
	boolean isError=false;
	String fileName="",path="";
	ListView dropbox_view;
	TextView txt_no_folder;
	ProgressBar loading;
	String FOLDER_DIR = "/";
	
	
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setCustomContentView(R.layout.import_csv_layout);
			 Util.setCustomAlertDialog(this);
			 back_layout.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
					/*	if(index > 0){
							
							FOLDER_DIR= dropbox_path.get(index-1);
							dropbox_path.remove(index);
							index--;
						
						dropbox_data.clear();
						Entry entries;

						try {
							entries = Util.mApi.metadata(FOLDER_DIR, 100,
									null, true, null);
						
						for (Entry e : entries.contents) {
							if (!e.isDeleted) {
								
								if (e.isDir) {
									dropbox_data.add(String.valueOf(e
											.fileName()));
								} else if (e.fileName().lastIndexOf(".csv") > 0) {
									dropbox_data.add(String.valueOf(e
											.fileName()));
								}

								// dropbox_data.add(String.valueOf(e.isDir));

							}
						}
						dropbox_view.setAdapter(new FolderAdaper());
						txt_no_folder.setVisibility(View.GONE);
						
						} catch (DropboxException e1) {
							
							e1.printStackTrace();
						}
						}else{
							finish();
						}*/
					}
				});
			 Util.txt_okey.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						Util.alert_dialog.dismiss();
						new sendFailedRecord().execute();
						
						finish();
					}
				});
			 
			 dropbox_view.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
							long arg3) {
						
						/*try {
							isError =false;

							if (dropbox_data.get(arg2).lastIndexOf(".csv") > 0) {
								txt_loading.setText(Html.fromHtml("<b>" + "Importing, please wait..."
										+ "<b>"));
								
								
								new GetDropboxFile().execute("/"+ dropbox_data.get(arg2));
								
							} else {
								
								FOLDER_DIR += "/" + dropbox_data.get(arg2);
								//dropbox_path.add(FOLDER_DIR);
								
								index = index+1;
								dropbox_path.add(index, FOLDER_DIR);
								
								dropbox_data.clear();
								Entry entries;

								entries = Util.mApi.metadata(FOLDER_DIR, 100,
										null, true, null);
								for (Entry e : entries.contents) {
									if (!e.isDeleted) {
										

										if (e.isDir) {
											dropbox_data.add(String.valueOf(e
													.fileName()));
										} else if (e.fileName().lastIndexOf(".csv") > 0) {
											dropbox_data.add(String.valueOf(e
													.fileName()));
										}

										
									}
								}
								dropbox_view.setAdapter(new FolderAdaper());
								if (dropbox_data.size() == 0) {

									txt_no_folder.setVisibility(View.VISIBLE);

								}

							}

						} catch (Exception ex) {
							ex.printStackTrace();
							
						}*/

					}

				});
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		 
		 
		
	}

	private class GetDropboxFile extends AsyncTask<String, Void, String> {

		protected void onPreExecute() {
			try {
				progress_dialog.show();
				txt_loading.setText("Downloading file...");
				super.onPreExecute();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}

		@Override
		protected String doInBackground(String... params) {
			
			String response = "";
			try {

				File root = android.os.Environment
						.getExternalStorageDirectory();
				File dir = new File(root.getAbsolutePath() + "/ScanAttendee/");
				if (dir.exists() == false) {
					dir.mkdirs();
				}
				// params[0] = params[0].substring(1, params[0].length());
				File file = new File(dir, params[0]);
				path = file.getAbsolutePath();

				fileName = params[0];
				//FileOutputStream outputStream = new FileOutputStream(file);
				//DropboxFileInfo info = Util.mApi.getFile(FOLDER_DIR + params[0], null, outputStream, null);

			} catch (final Exception ex) {
				ex.printStackTrace();

				path = "";
				startErrorAnimation(getResources().getString(R.string.network_error1), txt_error_msg);
				

			}

			if (!path.isEmpty())
			response = readCsv(ImportCSVActivity.this, path);

			return response;
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				progress_dialog.dismiss();
				if (!result.isEmpty()) {

					//parseJsonResponse(result,progress_dialog);
				}else{
					startErrorAnimation(getResources().getString(R.string.network_error1), txt_error_msg);
				}
			} catch (NotFoundException e) {
				
				e.printStackTrace();
			}

		}
	}
	
	
	public String readCsv(Context context, String path) {

		String  server_response="";
		 questionList = new ArrayList<String[]>();
		//AssetManager assetManager = context.getAssets();
		InputStream csvStream = null;
       // String ticket_price="", sold_qty="0", ticketName="", barcode="";
		InputStreamReader csvStreamReader = null;
		CSVReader csvReader = null;
		//boolean isCSV = true, isAttendeeExist = true, isSoldQty=true, isPaymentStatus=true,isPaymentType=true, isTicketPrice=true, isTicketQty=true;
		//String att_ticketYpes = "", att_seatNumber = "", att_barcode = "";
		//double att_ticketPrice = 0;
		//int totalsoldtickets = 0, sold_tickets = 0;
		try {
        csvStream = new FileInputStream(new File(path));
			
			//csvStreamReader = new InputStreamReader(csvStream);
			//csvReader = new CSVReader(csvStreamReader);
			String[] line=null;

			// throw away the header

			while ((line = csvReader.readNext()) != null) {

				questionList.add(line);
			}
			
		
			
			for(int i=0; i < questionList.get(0).length; i++){
				////Log.i(i+"----------Name of the Colum-------",":"+ questionList.get(0)[i]);
				colum_map.put(i, questionList.get(0)[i].trim());
				
			}
			if(questionList.size() > 201){
				
				isError = true;
				progress_dialog.dismiss();
				startErrorAnimation("ScanAttendee supports import of only 200 records.If you need to import more records." +
						" Please contact support at 1 (800) 492-1289", txt_error_msg);
				
				
			}else{
			
			if((colum_map.containsValue("First Name")||colum_map.containsValue("first name") ||colum_map.containsValue("FirstName"))&& 
					(colum_map.containsValue("Last Name")||colum_map.containsValue("last name") || colum_map.containsValue("LastName")) &&
					(colum_map.containsValue("Email Id")|| colum_map.containsValue("Email") || colum_map.containsValue("Email id") 
							|| colum_map.containsValue("email id") || colum_map.containsValue("EmailId") 
							|| colum_map.containsValue("Emailid"))){
				
				//TicketTypeContoller guest_ticket = Util.db.getGuestTicketInfo(checked_in_eventId);
				
				JSONObject parent = new JSONObject();
				JSONArray attendeeArray = new JSONArray();
				
				 
				String[] colum_header = { "First Name","Last Name", "Email Id", "Company",
						"Phone", "Address", "City", "State", "Zip Code","Ticket Type",
						"Ticket Name", "Ticket Price", "Ticket Quantity","Payment Status",
						"Sold Quantity", "Payment Type","Seat Numbers","Job Title","Attendee Category"};
				failed_record_array.add(colum_header);
			for(int j=1; j < questionList.size(); j++){
				//ticketNumber="";
				//ticketName="";
				//att_ticketPrice=0;
				//isSoldQty = true; isPaymentStatus = true; isPaymentType=true;isTicketPrice=true; isTicketQty=true;
				JSONArray ticketArray = new JSONArray();
				JSONObject obj_attndee_info = new JSONObject();
				JSONObject obj_ticket_info = new JSONObject();
				////Log.i("----Column size----",":"+ questionList.get(j).length);
				
				AttendeeDetailsController controller = new AttendeeDetailsController();
				CollectionController coll_controller = new CollectionController();
				TicketTypeContoller tickets_data = new TicketTypeContoller();
				controller.setAttendeeEventId(checked_in_eventId);
				//attendee controller is working 
			
				controller.setAttendeeItemTypes("");
				
				for(int key=0; key < questionList.get(j).length; key++){
					 
					if(colum_map.get(key).toLowerCase().trim().equals("first name") || colum_map.get(key).toLowerCase().trim().equals("firstname")){
			        	   controller.setAttendeeName(questionList.get(j)[key]);
			        	  
					}
					if(colum_map.get(key).toLowerCase().trim().equals("last name") || colum_map.get(key).toLowerCase().trim().equals("lastname")){
			        	   controller.setAttendeeLName(questionList.get(j)[key]);
			        	
					}
					if(colum_map.get(key).toLowerCase().trim().equals("attendee category")){
						 if(!NullChecker(questionList.get(j)[key]).isEmpty())
			        	   controller.setAttendeeCategory(questionList.get(j)[key]);
						 else
							 controller.setAttendeeCategory("");
			        	
					}
					if(colum_map.get(key).toLowerCase().trim().equals("job title")){
						 if(!NullChecker(questionList.get(j)[key]).isEmpty())
			        	   controller.setAttendeeDesignation(questionList.get(j)[key]);
						 else
							 controller.setAttendeeDesignation("");
			        	
					}
					if(colum_map.get(key).toLowerCase().trim().equals("email id") || colum_map.get(key).toLowerCase().trim().equals("emailid")){
						controller.setAttendeeEmail(questionList.get(j)[key]);
					}
				  if(colum_map.get(key).toLowerCase().trim().equals("company") || colum_map.get(key).toLowerCase().equals("company name"))
							controller.setAttendeeCompany(NullChecker(questionList.get(j)[key]));
						
				 
						
				  if(colum_map.get(key).toLowerCase().trim().equals("phone")|| colum_map.get(key).toLowerCase().equals("work phone")){
					  if(!NullChecker(questionList.get(j)[key]).isEmpty()){
						
						  if(isPhoneNumberValid(questionList.get(j)[key])){
							  controller.setAttendeeMobile(NullChecker(questionList.get(j)[key]));
						  }else{
							  controller.setAttendeeMobile(String.format("(%s) %s-%s", questionList.get(j)[key].substring(0, 3), 
									  questionList.get(j)[key].substring(3, 6),  questionList.get(j)[key].substring(6, 10)));
						  }
					  }
					
				  }
				  if(colum_map.get(key).toLowerCase().trim().equals("address") || colum_map.get(key).toLowerCase().contains("work address"))
							controller.setAttendeeAddress(NullChecker(questionList.get(j)[key]));
						
				  if(colum_map.get(key).toLowerCase().trim().equals("city") || colum_map.get(key).toLowerCase().contains("city"))
					controller.setAttendeeCity(NullChecker(questionList.get(j)[key]));
						
				  if(colum_map.get(key).toLowerCase().trim().equals("state") || colum_map.get(key).toLowerCase().contains("state"))
							controller.setAttendeeState(NullChecker(questionList.get(j)[key]));
						
				  if(colum_map.get(key).toLowerCase().trim().equals("zip code") || colum_map.get(key).toLowerCase().contains("zip code")){
					       if(!NullChecker(questionList.get(j)[key]).isEmpty()){
					    	   if(questionList.get(j)[key].length() <= 4 && !questionList.get(j)[key].startsWith("0")){
					    		   
					    		   controller.setAttendeeZipCode("0"+questionList.get(j)[key]);
					    	   }else{
							controller.setAttendeeZipCode(questionList.get(j)[key]);
					    	   }
					       }
				  }
				  
				
				  
			}//colum loop has been finished
				
				
				if(!controller.getAttendeeName().isEmpty() && !controller.getAttendeeLName().isEmpty() 
						&& !controller.getAttendeeEmail().isEmpty() && Pattern.matches(Validation.EMAIL_REGEX, controller.getAttendeeEmail())){
					
					attendee_controller.add(controller);
				obj_attndee_info.put("fname", capitalizeFirstLetter(controller.getAttendeeName()));
				obj_attndee_info.put("lname", capitalizeFirstLetter(controller.getAttendeeLName()));
				obj_attndee_info.put("email", controller.getAttendeeEmail().toLowerCase());
				obj_attndee_info.put("company", capitalizeFirstLetter(controller.getAttendeeCompany()));
				obj_attndee_info.put("wphone", controller.getAttendeeMobile());
				obj_attndee_info.put("Designation", controller.getAttendeeDesignation());
				obj_attndee_info.put("AttendeeCategory", controller.getAttendeeCategory());
				obj_attndee_info.put("city", controller.getAttendeeCity());
				obj_attndee_info.put("state", controller.getAttendeeState());
				obj_attndee_info.put("zip", controller.getAttendeeZipCode());
				obj_attndee_info.put("waddress", controller.getAttendeeAddress());
				
				/*//Log.i("----Ticket Name---",":"+tickets_data.getTicketTypeName()+"\n Ticket Type: "+tickets_data.getTicketType()
						+"\n Ticket price: "+tickets_data.getTicketPrice()+"\n Ticket Quantity: "+tickets_data.getTicketQuantity()+
						"\n Sold Tickets: "+tickets_data.getSoldTickets()+"\n Payment Status: "+coll_controller.getAttendeePaymentStatus()
						+"\n Payment Type: "+coll_controller.getAttendeePaymentType()+"\n isSOldQty: "+isSoldQty+"\nispaymentStatus: "+isPaymentStatus
						+"\n isPaymentType: "+isPaymentType+"\nisTicketPrice: "+isTicketPrice+"\nisTicketQty: "+isTicketQty);*/
	
					//tickets_data.setTicketTypeName(guest_ticket.getTicketTypeName());
					 tickets_data.setTicketPrice(0.0);
					tickets_data.setTicketQuantity(1000);
					tickets_data.setSoldTickets(1);
					tickets_data.setTicketType("Free");
					coll_controller.setAttendeePaymentType("Guest");
					coll_controller.setAttendeePaymentStatus("Paid");
				
			
				obj_ticket_info.put("ProductName", tickets_data.getTicketTypeName());
				obj_ticket_info.put("totalQuantity", tickets_data.getTicketQuantity());
				obj_ticket_info.put("UnitPrice", tickets_data.getTicketPrice());
				obj_ticket_info.put("Quantity", tickets_data.getSoldTickets());
				//obj_ticket_info.put("TicketType", tickets_data.getTicketType());
				obj_ticket_info.put("PaymentType", coll_controller.getAttendeePaymentType());
				obj_ticket_info.put("PaymentStatus", coll_controller.getAttendeePaymentStatus());
				obj_ticket_info.put("seatnumbers", coll_controller.getAttendeeSeatNum());
				
				ticketArray.put(obj_ticket_info);
				
				obj_attndee_info.put("lineItems", ticketArray);
				attendeeArray.put(obj_attndee_info);
				}else{
					 try {
						
						   failed_record_array.add(questionList.get(j));
						

					} catch (Exception e) {
						
						e.printStackTrace();
					}
				}
			}
			
			parent.put("AttendeeInfo", attendeeArray);
			
			server_response = syncCSVFile(parent);
			
			
			}else{
				
				progress_dialog.dismiss();
				startErrorAnimation("CSV format does not matched with Scan Attendee database", txt_error_msg);
				
			}
			}
		}catch(Exception ex){
			
			ex.printStackTrace();
			
			progress_dialog.dismiss();
			startErrorAnimation(getResources().getString(R.string.network_error1), txt_error_msg);
			 
		}
			
		return server_response;
	}
	
	public String syncCSVFile(JSONObject body_data){
		String url="", response="";
		boolean isCheckin=false;
		HttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse;
		
		HttpPost postMethod;
		try {
			
				 
					 url = sfdcddetails.instance_url+WebServiceUrls.SA_SYNC_CSV+"eventId="+checked_in_eventId;
				// //Log.i("-------------TICKET CHECKED IN INFO URL---------",":"+url+" "+body_data.toString());
				 postMethod = new HttpPost(url);
				 postMethod.addHeader("Authorization",sfdcddetails.token_type+" " +sfdcddetails.access_token);
				 StringEntity se = new StringEntity(body_data.toString());
				 postMethod.setEntity(se);
				  httpResponse = client.execute(postMethod);
					response = EntityUtils.toString(httpResponse.getEntity());
			
		////Log.i("-------------ATTENDEE SCAN SYNC CSV RESPONSE---------",":"+response);
			
		} catch (Exception e) {
			
		}
		return response;
	}
	
	@Override
	public void doRequest() {
		
		
	}

	@Override
	public void parseJsonResponse(String response) {
	
		String attendee_id="", attendee_name="", ticket_num="", checked_ticket_num="", seat_num="";
		ArrayList<CollectionController> collection_list = new ArrayList<CollectionController>();
		ArrayList<TicketTypeContoller> tickets_controller = new ArrayList<TicketTypeContoller>();
		attendee_controller.clear();
		try{
		
			JSONObject res_object = new JSONObject(response);
			
			JSONArray ticketArray = res_object.optJSONArray("ticketInformations");
			JSONArray attendeeArray = res_object.optJSONArray("attendeeInformations");
			////Log.i(ticketArray.length()+"----Size of Attendee Json Array----",":"+attendeeArray.length());
			if(ticketArray.length() > 0 && attendeeArray.length() > 0){
			for(int t_count=0; t_count <ticketArray.length(); t_count++ ){
				
				if(!ticketArray.optJSONObject(t_count).optString("id").isEmpty()){
					TicketTypeContoller ticket_data = new TicketTypeContoller();
					ticket_data.setTicketsEventId(checked_in_eventId);
					ticket_data.setTicketTypeName(ticketArray.optJSONObject(t_count).optString("ticketName"));
					ticket_data.setTicketId(ticketArray.optJSONObject(t_count).optString("id"));
					ticket_data.setTicketPrice(ticketArray.optJSONObject(t_count).optInt("ticketPrice"));
					int total_qty = ticketArray.optJSONObject(t_count).optInt("availableTickets")
							+ticketArray.optJSONObject(t_count).optInt("totalSoldTickets");
					ticket_data.setTicketQuantity(total_qty);
					ticket_data.setSoldTickets(ticketArray.optJSONObject(t_count).optInt("totalSoldTickets"));
					ticket_data.setTicketType(ticketArray.optJSONObject(t_count).optString("ticketType"));
					
					tickets_controller.add(ticket_data);
				}
				
			}
			////Log.i("Size of server ticket controller is",":"+tickets_controller.size());
			
			for(int i=0; i<attendeeArray.length(); i++){
				if(!attendeeArray.getJSONObject(i).optString("Email").isEmpty()){
					AttendeeDetailsController att_controller = new AttendeeDetailsController();
					att_controller.setAttendeeEventId(checked_in_eventId);
					attendee_id = attendeeArray.getJSONObject(i).optString("attendeeId");
					att_controller.setAttendeeId(attendee_id);
					att_controller.setAttendeeEmail(attendeeArray.getJSONObject(i).optString("Email"));
					att_controller.setAttendeeName(capitalizeFirstLetter(attendeeArray.getJSONObject(i).optString("FirstName")));
					att_controller.setAttendeeLName(capitalizeFirstLetter(attendeeArray.getJSONObject(i).optString("LastName")));
					if(!attendeeArray.getJSONObject(i).optString("Company").equals("null"))
					att_controller.setAttendeeCompany(capitalizeFirstLetter(attendeeArray.getJSONObject(i).optString("Company")));
					else
						att_controller.setAttendeeCompany("");
					
					if(!attendeeArray.getJSONObject(i).optString("Designation").equals("null"))
						att_controller.setAttendeeDesignation(capitalizeFirstLetter(attendeeArray.getJSONObject(i).optString("Designation")));
						else
							att_controller.setAttendeeDesignation("");
					
					if(!attendeeArray.getJSONObject(i).optString("AttendeeCategory").equals("null"))
						att_controller.setAttendeeCategory(capitalizeFirstLetter(attendeeArray.getJSONObject(i).optString("AttendeeCategory")));
						else
							att_controller.setAttendeeCategory("");
					
					att_controller.setAttendeeQrCodeId(attendeeArray.getJSONObject(i).optString("QrCode"));
					
					if(!attendeeArray.getJSONObject(i).optString("Address").equals("null"))
					att_controller.setAttendeeAddress(attendeeArray.getJSONObject(i).optString("Address"));
					else
						att_controller.setAttendeeAddress("");
					
					if(!attendeeArray.getJSONObject(i).optString("Zip").equals("null"))
						att_controller.setAttendeeZipCode(attendeeArray.getJSONObject(i).optString("Zip"));
						else
							att_controller.setAttendeeZipCode("");
					
					if(!attendeeArray.getJSONObject(i).optString("Phone").equals("null"))
					att_controller.setAttendeeMobile(attendeeArray.getJSONObject(i).optString("Phone"));
					else
						att_controller.setAttendeeMobile("");
					if(!attendeeArray.getJSONObject(i).optString("City").equals("null"))
					att_controller.setAttendeeCity(attendeeArray.getJSONObject(i).optString("City"));
					else
						att_controller.setAttendeeCity("");
					if(!attendeeArray.getJSONObject(i).optString("State").equals("null"))
					att_controller.setAttendeeState(attendeeArray.getJSONObject(i).optString("State"));
					else
						att_controller.setAttendeeState("");
					attendee_controller.add(att_controller);
					attendee_name = attendeeArray.getJSONObject(i).optString("FirstName");
					if(!attendeeArray.getJSONObject(i).optString("LastName").equals("null"))
						attendee_name = attendee_name+" " +attendeeArray.getJSONObject(i).optString("LastName");
						
					JSONArray regInfo_array = attendeeArray.getJSONObject(i).getJSONArray("registrationInfo");
					
					
					for(int k=0; k<regInfo_array.length(); k++){
						if(!regInfo_array.getJSONObject(k).optString("TicketId").isEmpty()){
							ticket_num =""; checked_ticket_num = ""; seat_num = "";
							
								
							JSONArray ind_ticket_array = regInfo_array.getJSONObject(k).optJSONArray("individualTicketInformation");
							
							for(int j=0; j<ind_ticket_array.length(); j++){
								CollectionController payment_data = new CollectionController();
								
								   payment_data.setTicketQty(1);
									payment_data.setAmount(regInfo_array.getJSONObject(k).optInt("ticketsPrice")/
											regInfo_array.getJSONObject(k).optInt("totalSoldQuantity"));
								
									payment_data.setTicketId(regInfo_array.getJSONObject(k).optString("TicketId"));
									payment_data.setTicketName(regInfo_array.getJSONObject(k).optString("ticketName"));
									payment_data.setAttendeeId(attendee_id);
									payment_data.setAttendeePaymentStatus(regInfo_array.getJSONObject(k).optString("PaymentStatus"));
									payment_data.setAttendeePaymentType(regInfo_array.getJSONObject(k).optString("PaymentType"));
									payment_data.setEventId(checked_in_eventId);
									payment_data.setOrderId(regInfo_array.getJSONObject(k).optString("TransactionId"));
									payment_data.setOrderType("TICKET");
									String time = ind_ticket_array.optJSONObject(j).optString("PaymentTime");
											  
								payment_data.setPaymentTime(Util.db_date_format.format(Util.db_date_format1
										.parse(time.replace("am", "AM").replace("pm", "PM"))));
								
								payment_data.setAttendeeTicketNum(ind_ticket_array.optJSONObject(j).optString("name"));
								
								if(!ind_ticket_array.optJSONObject(j).optString("Seat").equals("null") && 
										!ind_ticket_array.optJSONObject(j).optString("Seat").isEmpty()){
									payment_data.setAttendeeSeatNum(ind_ticket_array.optJSONObject(j).optString("Seat"));
									
									
								}
								if(ind_ticket_array.optJSONObject(j).optBoolean("isCheckdin"))
									payment_data.setAttendeeCheckedTicketNum(ind_ticket_array.optJSONObject(j).optString("name"));
								collection_list.add(payment_data);
								}
							
							}
							
							
							
							
							
						
					}//attendee registration info parsing is done and stored into collection controller
					
				}
			}// attendee controller is set
			
			//Util.db.InsertAndUpdateTicket(tickets_controller,false);
			
			
			/*Util.db.InsertAndUpdateAttendee(attendee_controller);
			Util.db.InsertAndUpdatePaymentInfo(collection_list,true);*/
			
			progress_dialog.dismiss();
			total_count = questionList.size()-1;
			 failed_record = total_count - attendee_controller.size();
			
			Util.openCustomDialog("Success","CSV file " +fileName +" imported successfully,Total records:"+total_count
					+",Imported:"+attendee_controller.size()+",Failed:"+failed_record+",Sent log file to "+
					user_profile.Profile.Email__c);
			
			
			}else{
				isError=true;
				progress_dialog.dismiss();
				startErrorAnimation(getResources().getString(R.string.network_error1), txt_error_msg);
				
			}
			
		}catch(final Exception e){
			e.printStackTrace();
			
			isError = true;
			progress_dialog.dismiss();
			startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
			
		}
		
	}

	@Override
	public void setCustomContentView(int layout) {
		
		try {
			View v = inflater.inflate(layout, null);
			linearview.addView(v);
			txt_title.setText("Dropbox");
			img_setting.setVisibility(View.GONE);
			img_menu.setImageResource(R.drawable.back_button);
			event_layout.setVisibility(View.GONE); 
			button_layout.setVisibility(View.GONE);
			event_layout.setVisibility(View.VISIBLE);
			
			dropbox_view = (ListView) linearview.findViewById(R.id.importcsvView);
			txt_no_folder = (TextView) linearview.findViewById(R.id.nofolder);
			loading = (ProgressBar) linearview.findViewById(R.id.dropboxloading);
			
			if(isOnline())
			new GetDropboxFolder().execute();
			else
				startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
		} catch (NotFoundException e) {
			
			e.printStackTrace();
		}
		
	}

	
	private class GetDropboxFolder extends AsyncTask<String, Void, String> {

		protected void onPreExecute() {
			super.onPreExecute();
			try {
				loading.setVisibility(View.VISIBLE);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}

		@Override
		protected String doInBackground(String... params) {
			
		/*	Entry entries, folderenteries;
			
			try {

				entries = Util.mApi.metadata(FOLDER_DIR, 100, null, true,
						null);
				for (Entry e : entries.contents) {
					if (!e.isDeleted) {
						////Log.i("Is Folder", String.valueOf(e.isDir));
						if (e.isDir) {
							dropbox_data.add(String.valueOf(e.fileName()));
						} else if (e.fileName().lastIndexOf(".csv") > 0) {
							dropbox_data.add(String.valueOf(e.fileName()));
						}

						// dropbox_data.add(String.valueOf(e.isDir));

					}
				}

			} catch (DropboxException e1) {
				
				e1.printStackTrace();
			}
*/
			return "";
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				loading.setVisibility(View.GONE);
				
				if (new FolderAdaper().isEmpty()) {
					txt_no_folder.setVisibility(View.VISIBLE);
				} else {
					dropbox_view.setVisibility(View.VISIBLE);
					dropbox_view.setAdapter(new FolderAdaper());
				}
			} catch (Exception e) {
				
				e.printStackTrace();
			}

		}
	}
	
	private class FolderAdaper extends BaseAdapter {

		View v;
		TextView folderNmae;
        ImageView folderImg;
		@Override
		public int getCount() {
			
			return dropbox_data.size();
		}

		@Override
		public Object getItem(int arg0) {
			
			return dropbox_data.get(arg0);

		}

		@Override
		public long getItemId(int arg0) {
			
			return 0;
		}

		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {
			
			try {
				v = inflater.inflate(R.layout.import_csv_list_item, null);

				folderNmae = (TextView) v.findViewById(R.id.folderpath);
				folderImg = (ImageView) v.findViewById(R.id.foldericon);
				folderNmae.setTypeface(Util.roboto_regular);
				folderNmae.setText(dropbox_data.get(arg0));
				   if(dropbox_data.get(arg0).indexOf(".csv") > 0){
					   folderImg.setImageResource(R.drawable.more);  
				   }else{
					   folderImg.setImageResource(R.drawable.folder);  
				   }
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			return v;
		}
	}
	
	private class sendFailedRecord extends AsyncTask<String, Void, String>
	{

	protected void onPreExecute() {
		super.onPreExecute();
		
	}

	@Override
	protected String doInBackground(String... params) {
		
		try {
			fileName = fileName.substring(1, fileName.indexOf(".csv"))+"_FailedRecords.csv";
			StringBuilder builder = new StringBuilder();
			
			for(int i=0; i< failed_record_array.size(); i++){
				String str="";
				if(i == 0){
					for(String s: failed_record_array.get(i)){
					     str = str+s+",";
						/*builder.append("\""+s+"\"");
						builder.append(",");*/
					}
					builder.append(str.substring(0, str.length()-1));
					builder.append("\n");
				}else{
				
				for(String s: failed_record_array.get(i)){
				     str = str+ "\""+s+"\""+",";
					/*builder.append("\""+s+"\"");
					builder.append(",");*/
				}
				
				builder.append(str.substring(0, str.length()-1));
				builder.append("\n");
				}
			}
			
			return makeConnection(builder.toString());
		} catch (Exception e) {
			
			e.printStackTrace();
		}return null;
		
	}

	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
		
		
	}
	}
          private String makeConnection(String data){
		
		String response="";
		try {
			
			String url = sfdcddetails.instance_url+WebServiceUrls.SA_SENDIMPORTEMAIL+"Recipients_EmailID="+user_profile.Profile.Email__c+"&TotalRecords="+total_count+
					"&TotalFailedRecords="+failed_record+"&CSVName="+fileName+"&TotalSuccessRecords="+attendee_controller.size();
			
			HttpClient client = new DefaultHttpClient();
			HttpPost postMethod = new HttpPost(url);
			postMethod.addHeader("Authorization",sfdcddetails.token_type+" " +sfdcddetails.access_token);
			
			StringEntity se = new StringEntity(data);
			postMethod.setEntity(se);
			
			HttpResponse httpResponse = client.execute(postMethod);
			response = EntityUtils.toString(httpResponse.getEntity());
			
		
			
		} catch (Exception e) {
			
		}
		
		return "";
	}

		/* (non-Javadoc)
		 * @see com.globalnest.network.IPostResponse#insertDB()
		 */
		@Override
		public void insertDB() {
			
			
		}
	
}
