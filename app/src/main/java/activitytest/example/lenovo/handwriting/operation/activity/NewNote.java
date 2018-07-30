package activitytest.example.lenovo.handwriting.operation.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import activitytest.example.lenovo.handwriting.HandWriting;
import activitytest.example.lenovo.handwriting.R;
import activitytest.example.lenovo.handwriting.operation.provider.NoteInfo;
import butterknife.BindView;
import butterknife.ButterKnife;

public class NewNote extends AppCompatActivity {


    public static HandWriting handWriting;
    public static final int TAKE_PHOTO = 1;
    private Uri imageUri;
    private Context mContext;

    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.take_photo)
    ImageView takePhoto;
    @BindView(R.id.title_content)
    EditText titleContent;
    @BindView(R.id.note_content)
    EditText noteContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        ButterKnife.bind(this);
        mContext = NewNote.this;
        handWriting = (HandWriting) getApplicationContext();

    }

    public void save(View view) {
        String title = titleContent.getText().toString();
        String note = noteContent.getText().toString();
        if (title.length() == 0) {
            displayToast("标题不能为空");
            titleContent.requestFocus();
        } else if (note.length() == 0) {
            displayToast("笔迹内容不能为空");
            noteContent.requestFocus();
        } else {
            NoteInfo noteInfo = new NoteInfo();
            noteInfo.setContent(note);
            noteInfo.setDate(System.currentTimeMillis());
            noteInfo.setTitle(title);
            handWriting.myDataBaseAdapter.insertData(noteInfo);
            exit(true);
        }

    }

    private void exit(boolean refresh) {
        Intent intent = new Intent();
        intent.putExtra("refresh", refresh);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void displayToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public void back(View view) {
        finish();
    }

    public void takePhoto(View view) {
        String imageName = System.currentTimeMillis() + ".jpg";
        File outputImage = new File(getExternalCacheDir(), imageName);
        try {
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) { //android7.0及以上
            imageUri = FileProvider.getUriForFile(this,
                    "com.example.cameratest.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        //启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }
}
