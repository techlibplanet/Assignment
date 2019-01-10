package appdesign.example.com.assignment

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class AssignmentApplication : Application() {


    override fun onCreate() {
        super.onCreate()

        FacebookSdk.sdkInitialize(applicationContext);
        AppEventsLogger.activateApp(this);
    }
}