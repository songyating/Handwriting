package activitytest.example.lenovo.handwriting;

import android.app.Application;

import activitytest.example.lenovo.handwriting.operation.provider.MyDataBaseAdapter;

public class HandWriting extends Application {
    public static String PREFS_NAME = "activitytest.example.lenovo.handwriting";
    public static String PICTURE_PATH = "picturepath";
    public static String FIRST_LOGIN = "first_login";
    public static String IS_STORY = "is_story";
    public static String USER_NAME_LIST="user_name_list";

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
        myDataBaseAdapter.deleteAllData();
        if (myDataBaseAdapter != null) {
            myDataBaseAdapter.close();
        }
    }
}
