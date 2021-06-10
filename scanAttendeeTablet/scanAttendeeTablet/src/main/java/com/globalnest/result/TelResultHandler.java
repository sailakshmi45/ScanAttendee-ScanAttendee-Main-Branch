
package com.globalnest.result;



import com.globalnest.scanattendee.R;
import com.google.zxing.client.result.ParsedResult;

import android.app.Activity;
import android.telephony.PhoneNumberUtils;


/**
 * Offers relevant actions for telephone numbers.
 * 
 * 
 */
public final class TelResultHandler extends ResultHandler {

    public TelResultHandler(Activity activity, ParsedResult result) {
        super(activity, result);
    }

    // Overriden so we can take advantage of Android's phone number hyphenation routines.
    @Override
    public CharSequence getDisplayContents() {
        String contents = getResult().getDisplayResult();
        contents = contents.replace("\r", "");
        return PhoneNumberUtils.formatNumber(contents);
    }

    @Override
    public int getDisplayTitle() {
        return R.string.result_tel;
    }
}
