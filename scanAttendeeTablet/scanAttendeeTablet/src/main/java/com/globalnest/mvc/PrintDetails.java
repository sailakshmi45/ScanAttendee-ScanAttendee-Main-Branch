package com.globalnest.mvc;

import android.widget.FrameLayout;

import com.globalnest.utils.SFDCDetails;

/**
 * Created by saila_000 on 23-05-2017.
 */

public class PrintDetails {

    public String attendeeId="",order_id="",checked_in_eventId="";
    public SFDCDetails sfdcddetails;
    public FrameLayout print_badge,frame_transparentbadge;
    public boolean isselfCheckinbool=false;
    public String attendeeWhereClause="";
    public boolean isOrderScaneed=false;
    public String qrCode="";
    public String reason="";

}
