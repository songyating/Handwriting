package activitytest.example.lenovo.handwriting.operation.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import activitytest.example.lenovo.handwriting.HandWriting;
import activitytest.example.lenovo.handwriting.R;
import activitytest.example.lenovo.handwriting.operation.provider.NoteInfo;
import butterknife.BindView;
import butterknife.ButterKnife;


public class NewNote extends AppCompatActivity {


    public static HandWriting handWriting;
    @BindView(R.id.title_look_view)
    TextView titleLookView;
    @BindView(R.id.title_view)
    RelativeLayout titleView;
    @BindView(R.id.content_look_view)
    TextView contentLookView;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.take_voice)
    ImageView takeVoice;
    @BindView(R.id.background)
    LinearLayout viewBackground;
    @BindView(R.id.title_content)
    EditText titleContent;
    @BindView(R.id.note_content)
    EditText noteContent;
    @BindView(R.id.save)
    FloatingActionButton save;
    @BindView(R.id.scroll)
    ScrollView scroll;

    private Context mContext;
    private int dataId;//数据的id,用来获取所查看数据的id
    private boolean isNew = true;//是否要新建笔迹，true：新建笔迹  flase：查看笔迹
    private AlertDialog dialog;
    private NoteInfo noteInfo;
    private Intent intent;
    private String titleContentInfo;
    private String noteContentInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        ButterKnife.bind(this);

        mContext = NewNote.this;
        handWriting = (HandWriting) getApplicationContext();
        //语音转汉字的初始化
        SpeechUtility.createUtility(mContext, SpeechConstant.APPID + "=5b5fdab3");

        intent = getIntent();
        dataId = intent.getIntExtra("id", 0);
        if (dataId != 0) {
            isNew = false;//查看笔迹
            init(dataId);//初始化控件内容
        }
    }

    /**
     * 查看笔迹
     *
     * @param id 对应笔迹的
     */

    private void init(int id) {
        Cursor cursor = handWriting.myDataBaseAdapter.fetchNoteData(id);
        noteInfo = new NoteInfo(cursor);
        titleContentInfo = noteInfo.getTitle();
        noteContentInfo = noteInfo.getContent();
        //原先标题隐藏
        titleView.setVisibility(View.GONE);
        //原先笔迹隐藏
        noteContent.setVisibility(View.GONE);
        //保存按钮隐藏
        save.setVisibility(View.GONE);
        //将显示图片更改为编辑
        takeVoice.setImageResource(R.drawable.ic_editor);

        title.setText("笔 迹");
        //显示标题
        titleLookView.setVisibility(View.VISIBLE);
        //显示笔迹
        contentLookView.setVisibility(View.VISIBLE);
        //标题
        titleLookView.setText(titleContentInfo);
        //笔记内容
        contentLookView.setText(noteContentInfo);
        scroll.setVisibility(View.VISIBLE);
    }

    /**
     * 保存内容
     *
     * @param view 为无用的参数
     */
    public void save(View view) {
        String title = titleContent.getText().toString();
        String note = noteContent.getText().toString();
        if (title.length() == 0) {
            if (isNew) {
                displayToast("标题不能为空");
                titleContent.requestFocus();
            } else {
                titleContent.setText("我的笔迹");
            }
        } else if (note.length() == 0) {
            displayToast("笔迹内容不能为空");
            noteContent.requestFocus();
        } else {
            NoteInfo noteInfo = new NoteInfo();
            noteInfo.setContent(note);
            noteInfo.setDate(System.currentTimeMillis());
            noteInfo.setTitle(title);
            if (isNew) {
                handWriting.myDataBaseAdapter.insertData(noteInfo);
            } else {
                handWriting.myDataBaseAdapter.updateColumns(dataId, noteInfo);
            }
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
        if (noteContent.getText().toString().length() > 0) {
            if (isAlert()) {
                displayAlert();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private boolean isAlert() {

        if (isNew) {
            Log.d("SSS", "isAlert: 是新建模式");
            return true;
        } else {
            if (!titleContent.getText().toString().equals(titleContentInfo)
                    || !noteContent.getText().toString().equals(noteContentInfo)) {
                Log.d("SSS", "isAlert: 是查看模式");
                return true;
            }
            return false;
        }
    }

    private void displayAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("是否要保存编辑");
        // Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                save(null);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    public void takeVoice(View view) {
        //新建笔迹——调用录音
        if (isNew) {
            RecognizerDialog dialog = new RecognizerDialog(mContext, null);
            dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            dialog.setParameter(SpeechConstant.ACCENT, "mandarin");

            dialog.setListener(new RecognizerDialogListener() {
                @Override
                public void onResult(RecognizerResult recognizerResult, boolean b) {
                    printResult(recognizerResult);
                }

                @Override
                public void onError(SpeechError speechError) {
                }
            });
            dialog.show();
            displayToast("请开始说话");
        } else {
            //查看笔迹——切换到编辑模式
            titleLookView.setVisibility(View.GONE);
            contentLookView.setVisibility(View.GONE);
            scroll.setVisibility(View.GONE);
            noteContent.setVisibility(View.VISIBLE);
            titleView.setVisibility(View.VISIBLE);
            titleContent.setText(noteInfo.getTitle());
            noteContent.setText(noteInfo.getContent());
            titleContent.requestFocus();
            save.setVisibility(View.VISIBLE);
            takeVoice.setImageResource(R.drawable.ic_voice);
        }
    }

    private void printResult(RecognizerResult result) {
        String text = parseIatResult(result.getResultString());
        //填写的位置
        if (titleContent.hasFocus()) {
            titleContent.setText(text);
        } else {
            noteContent.setText(text);
        }
    }

    public static String parseIatResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
           /* case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        viewBackground.setBackground(drawable);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;*/
            default:
                break;
        }


    }
}
