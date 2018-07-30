package activitytest.example.lenovo.handwriting.operation;

import android.content.Context;
import android.widget.Toast;

public class PublicFunction {

    public static void displayToast(Context context,String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
