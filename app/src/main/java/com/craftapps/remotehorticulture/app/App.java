package com.craftapps.remotehorticulture.app;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.PushService;

import java.util.HashMap;

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "k9UH8HfBh2kObyiosc1Pu99Sf3L3zEk7mgGLvo3S", "mARU3AN9uEEw8JhuMPE31JDcQUqOhdOdIwPqRmFW");

        PushService.setDefaultPushCallback(this, MainActivity.class);

        PushService.subscribe(getBaseContext(), "Test", MainActivity.class);

        try {
            ParseCloud.callFunction("ControlHubOnline", new HashMap<String, Object>());
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

}