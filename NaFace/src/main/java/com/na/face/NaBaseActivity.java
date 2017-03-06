package com.na.face;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class NaBaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
    }
}
