package dev.nick.app.automachine;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import dev.nick.logger.LoggerManager;

public class Machine extends Application {

    public static final String PLAN_DIR = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "MachinePlans";

    @Override
    public void onCreate() {
        super.onCreate();
        LoggerManager.setTagPrefix("Nick");
        LoggerManager.setDebugLevel(Log.VERBOSE);
    }
}
