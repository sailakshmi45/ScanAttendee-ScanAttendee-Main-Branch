package com.globalnest.mvc;

import java.util.ArrayList;

public class MultiListValues {
    public BLN_Tkt_List BLN_Tkt_List_Selected__r=new BLN_Tkt_List();

    public class BLN_Tkt_List{
        public ArrayList<Records> records = new ArrayList<Records>();
    }
    public class Records {
        public BLN_ListLookUp BLN_ListLookUp__r=new BLN_ListLookUp();
        public String Id="",BLN_TKT_profile__c="";

        public class BLN_ListLookUp{
            public String Id="",List_Code__c="",List_Description__c="",List_Type__c="";
        }
    }
}
