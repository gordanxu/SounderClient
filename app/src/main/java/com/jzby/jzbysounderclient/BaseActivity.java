package com.jzby.jzbysounderclient;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    protected void showTextToast(String text)
    {
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }
}
