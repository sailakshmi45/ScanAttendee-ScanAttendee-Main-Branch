package com.globalnest.scanattendee;

import android.os.Bundle;
import android.view.View;

public class Registrationclass extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.registration_form);
    }

    @Override
    public void setCustomContentView(int layout) {
        activity = this;
        View v = inflater.inflate(layout, null);
        linearview.addView(v);
    }

    @Override
    public void doRequest() {

    }

    @Override
    public void parseJsonResponse(String response) {

    }

    @Override
    public void insertDB() {

    }
}
