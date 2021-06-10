
package com.globalnest.result;



import java.util.Locale;

import com.globalnest.scanattendee.R;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.URIParsedResult;

import android.app.Activity;


/**
 * Offers appropriate actions for URLS.
 * 
 *  
 */
public final class URIResultHandler extends ResultHandler {
    // URIs beginning with entries in this array will not be saved to history or
    // copied to the clipboard for security.
    private static final String[] SECURE_PROTOCOLS = { "otpauth:" };

    public URIResultHandler(Activity activity, ParsedResult result) {
        super(activity, result);
    }

    @Override
    public CharSequence getDisplayContents() {
        URIParsedResult uriResult = (URIParsedResult) getResult();
        String uri = uriResult.getURI().toLowerCase(Locale.ENGLISH);
        StringBuilder contents = new StringBuilder(100);
        ParsedResult.maybeAppend(uri, contents);
        contents.trimToSize();
        return contents.toString();
    }
    
    @Override
    public int getDisplayTitle() {
        return R.string.result_uri;
    }

    @Override
    public boolean areContentsSecure() {
        URIParsedResult uriResult = (URIParsedResult) getResult();
        String uri = uriResult.getURI().toLowerCase(Locale.ENGLISH);
        for (String secure : SECURE_PROTOCOLS) {
            if (uri.startsWith(secure)) {
                return true;
            }
        }
        return false;
    }
}
