package activitytest.example.lenovo.handwriting;

import android.app.Application;

import activitytest.example.lenovo.handwriting.operation.provider.MyDataBaseAdapter;

public class HandWriting extends Application {
    public static String PREFS_NAME = "activitytest.example.lenovo.handwriting";
    public static String PICTURE_PATH = "picturepath";

    public MyDataBaseAdapter myDataBaseAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        myDataBaseAdapter = new MyDataBaseAdapter(this);
        myDataBaseAdapter.open();
    }

    @Override
    public void onTerminate() {
        /*
         * if (Log.LOG_ENABLE) Log.e("TimersMe Application onTerminate()");
         */
        super.onTerminate();
        if (myDataBaseAdapter != null) {
            myDataBaseAdapter.close();
        }
    }
}
