package com.globalnest.mvc;

import java.io.Serializable;

/**
 * Created by laxmanamurthy on 1/31/2017.
 */

public class PaytmStatusCheckResponseGson implements Serializable {
    public String TXNID = "",
            BANKTXNID= "",
            ORDERID= "",
            TXNAMOUNT= "",
            STATUS= "",
            TXNTYPE= "",
            GATEWAYNAME= "",
            RESPCODE= "",
            RESPMSG= "",
            BANKNAME= "",
            MID= "",
            PAYMENTMODE= "",
            REFUNDAMT= "",
            TXNDATE= "";
}
