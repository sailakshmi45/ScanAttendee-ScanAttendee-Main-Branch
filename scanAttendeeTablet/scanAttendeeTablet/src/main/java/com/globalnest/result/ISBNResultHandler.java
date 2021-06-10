
package com.globalnest.result;

import com.globalnest.scanattendee.R;
import com.google.zxing.Result;
import com.google.zxing.client.result.ISBNParsedResult;
import com.google.zxing.client.result.ParsedResult;

import android.app.Activity;


/**
 * Handles books encoded by their ISBN values.
 * 
 * 
 */
public final class ISBNResultHandler extends ResultHandler {

    public ISBNResultHandler(Activity activity, ParsedResult result, Result rawResult) {
        super(activity, result, rawResult);
    }

    @Override
    public CharSequence getDisplayContents() {
        ISBNParsedResult result = (ISBNParsedResult) getResult();
        StringBuilder contents = new StringBuilder(100);
        ParsedResult.maybeAppend(result.getISBN(), contents);
        contents.trimToSize();
        return contents.toString();
    }
    
    @Override
    public int getDisplayTitle() {
        return R.string.result_isbn;
    }
}
