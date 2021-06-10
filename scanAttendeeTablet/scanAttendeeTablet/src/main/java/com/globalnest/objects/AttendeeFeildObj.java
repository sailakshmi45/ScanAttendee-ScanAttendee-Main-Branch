package com.globalnest.objects;

import java.util.ArrayList;

public class AttendeeFeildObj {

   // public ArrayList<Fields> fields=new ArrayList<>();
   public String group_display_name="",field_api_name="",field_data="";//,field_type=""

    /*{
        "fields": [
        {
            "group_display_name": "Basic Information",
                "field_type": "STRING",
                "field_api_name": "TKT_Company__c",
                "field_data": "Microsoft"
        },
        {
            "group_display_name": "Basic Information",
                "field_type": "STRING",
                "field_api_name": "tkt_job_title__c",
                "field_data": "Sr Developer"
        }
  ]
    } */
    public class Fields{
        public String group_display_name="",field_type="",field_api_name="",field_data="";
    }
}
