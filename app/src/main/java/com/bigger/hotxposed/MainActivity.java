package com.bigger.hotxposed;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Looper;

import java.nio.charset.Charset;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application application = getApplication();
        Character.getDirectionality('A');
    }
}
