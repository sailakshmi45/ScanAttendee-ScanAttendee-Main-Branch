// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.globalnest.mvc;

import java.util.ArrayList;


// Referenced classes of package com.globalnest.mvc:
//            TicketHandler, TicketPoolHandler

public class TicketListResponseHandler
{

    public String Enddate;
    public ArrayList<TicketPoolHandler> Itempool;
    public TicketHandler Items;
    public String Startdate;
    public String siteurl;
    public String Fee;
    public TicketListResponseHandler()
    {
        Startdate = "";
        Enddate = "";
        Items = new TicketHandler();
        Itempool = new ArrayList<TicketPoolHandler>();
    }
    
    @Override
    public String toString()
    {
        return "TicketListResponseHandler [Startdate="+Startdate +", Enddate="+Enddate+", Items="+Items+
        		", Itempool="+Itempool+", siteurl="+siteurl+"]";
    }
}
