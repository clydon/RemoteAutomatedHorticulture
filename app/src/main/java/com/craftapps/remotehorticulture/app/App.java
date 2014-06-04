package com.craftapps.remotehorticulture.app;

import android.app.Application;
        import com.parse.Parse;

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "k9UH8HfBh2kObyiosc1Pu99Sf3L3zEk7mgGLvo3S", "mARU3AN9uEEw8JhuMPE31JDcQUqOhdOdIwPqRmFW");
    }
}