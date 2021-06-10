//  ScanAttendee Android
//  Created by Ajay on May 27, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.classes;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * @author mayank
 *
 */
public class TextWatcherAdapter implements TextWatcher {

public interface TextWatcherListener {

    void onTextChanged(EditText view, String text);

}

private final EditText view;
private final TextWatcherListener listener;

public TextWatcherAdapter(EditText editText, TextWatcherListener listener) {
    this.view = editText;
    this.listener = listener;
}

@Override
public void onTextChanged(CharSequence s, int start, int before, int count) {
    listener.onTextChanged(view, s.toString());
}

@Override
public void beforeTextChanged(CharSequence s, int start, int count,
                              int after) {
    // pass
}

@Override
public void afterTextChanged(Editable s) {
    // pass
}
}