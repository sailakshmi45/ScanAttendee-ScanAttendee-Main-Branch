package com.globalnest.objects;

import java.io.Serializable;
import java.util.ArrayList;

public class AllPickListValues implements Serializable {
    private static final long serialVersionUID = 1L;
    public String fieldName="";
    public String[] picklistvalues = new String[]{};
    public ArrayList<Customvalues> customvalues=new ArrayList<>();


    public class picklistvalues {
        public int value=0;
    }

    public class Customvalues {
            public String Id="",BLN_Events__c="",List_Description__c="",Name="",List_Code__c="";
            public int Sort_Order__c=0;
    }
}
