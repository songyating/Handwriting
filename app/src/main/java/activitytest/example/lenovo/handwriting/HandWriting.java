package activitytest.example.lenovo.handwriting;

import android.app.Application;

import activitytest.example.lenovo.handwriting.operation.provider.MyDataBaseAdapter;

public class HandWriting extends Application{

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
