/*
 *  Copyright 2013-2016 Amazon.com,
 *  Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Amazon Software License (the "License").
 *  You may not use this file except in compliance with the
 *  License. A copy of the License is located at
 *
 *      http://aws.amazon.com/asl/
 *
 *  or in the "license" file accompanying this file. This file is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *  CONDITIONS OF ANY KIND, express or implied. See the License
 *  for the specific language governing permissions and
 *  limitations under the License.
 */

package activitytest.example.lenovo.handwriting.myuserpools;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import activitytest.example.lenovo.handwriting.HandWriting;
import activitytest.example.lenovo.handwriting.R;
import activitytest.example.lenovo.handwriting.operation.PublicFunction;
import activitytest.example.lenovo.handwriting.operation.activity.NewNote;
import activitytest.example.lenovo.handwriting.operation.provider.NoteInfo;
import activitytest.example.lenovo.handwriting.operation.provider.NoteRecycleViewAdapter;

public class UserActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private NavigationView nDrawer;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;
    private AlertDialog userDialog;
    private ProgressDialog waitDialog;
    private RecyclerView mRecycleView;
    private NoteRecycleViewAdapter noteRecycleViewAdapter;
    private FloatingActionButton newBtn;
    private File outputImage;

    // Cognito user objects
    private CognitoUser user;
    private CognitoUserSession session;
    private CognitoUserDetails details;

    // User details
    private String username;

    private SharedPreferences mSharedPreferences;

    public static final int TAKE_PHOTO = 1;
    private Uri imageUri;
    private static HandWriting handWriting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        handWriting = (HandWriting) getApplicationContext();
        mSharedPreferences = getSharedPreferences(
                HandWriting.PREFS_NAME, 0);
        // Set toolbar for this screen
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("");

        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText("笔 迹");
        setSupportActionBar(toolbar);

        newBtn = findViewById(R.id.new_note);
        mRecycleView = findViewById(R.id.main_recycleview);
        noteRecycleViewAdapter = new NoteRecycleViewAdapter();

        // Set navigation drawer for this screen
        mDrawer = (DrawerLayout) findViewById(R.id.user_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        nDrawer = (NavigationView) findViewById(R.id.nav_view);
        setNavDrawer();
        init();
        showNotes();
        mRecycleView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecycleView.setAdapter(noteRecycleViewAdapter);
        setListener();//对点击项进行监听
        newBtnListener();//新建按钮的拖动监听
        View navigationHeader = nDrawer.getHeaderView(0);
        TextView navHeaderSubTitle = (TextView) navigationHeader.findViewById(R.id.textViewNavUserSub);
        navHeaderSubTitle.setText(username);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void newBtnListener() {
        DisplayMetrics dm = getResources().getDisplayMetrics();

        final int screenWidth = dm.widthPixels;
        final int screenHeight = dm.heightPixels - PublicFunction.dip2px(UserActivity.this, 60);


        newBtn.setOnTouchListener(new View.OnTouchListener() {
            int lastX, lastY;
            long startTime = 0;
            long endTime = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();// 获取触摸事件触摸位置的原始X坐标
                        lastY = (int) event.getRawY();
                        startTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        int l = v.getLeft() + dx;
                        int b = v.getBottom() + dy;
                        int r = v.getRight() + dx;
                        int t = v.getTop() + dy;
                        // 下面判断移动是否超出屏幕
                        if (l < 0) {
                            l = 0;
                            r = l + v.getWidth();
                        }
                        if (t < 0) {
                            t = 0;
                            b = t + v.getHeight();
                        }
                        if (r > screenWidth) {
                            r = screenWidth;
                            l = r - v.getWidth();
                        }
                        if (b > screenHeight) {
                            b = screenHeight;
                            t = b - v.getHeight();
                        }
                        v.layout(l, t, r, b);
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        v.postInvalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        endTime = System.currentTimeMillis();
                        break;
                    default:
                        break;
                }
                if (endTime - startTime > 0.1 * 1000L) {
                    return true;
                } else {
                    return false;
                }
            }
        });

    }

    private void setListener() {
        noteRecycleViewAdapter.setOnItemClickListener(new NoteRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent look = new Intent(UserActivity.this, NewNote.class);
                look.putExtra("id", position);
                startActivityForResult(look, 23);
            }

            @Override
            public void onLongItemClick(final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
                builder.setIcon(R.drawable.head);
                builder.setTitle("删除此条笔迹");
                // Add the buttons
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        handWriting.myDataBaseAdapter.deleteData(position);
                        showNotes();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Find which menu item was selected
        int menuItem = item.getItemId();

        // Do the task
        if (menuItem == R.id.user_update_attribute) {
          /*  showWaitDialog("Updating...");
            getDetails();*/
            takePhoto();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void takePhoto() {
        String imageName = System.currentTimeMillis() + ".jpg";
        outputImage = new File(getExternalCacheDir(), imageName);
        try {
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) { //android7.0及以上
            imageUri = FileProvider.getUriForFile(this,
                    "activitytest.example.lenovo.handwriting.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        //启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    mRecycleView.setBackground(getBitmap(bitmap,outputImage.getAbsolutePath()));
                    //保存图片路径
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString(HandWriting.PICTURE_PATH, outputImage.getAbsolutePath());
                    editor.apply();
                }

                break;
            case 20:
                // Settings
                if (resultCode == RESULT_OK) {
                    boolean refresh = data.getBooleanExtra("refresh", true);
                    if (refresh) {
                        showNotes();
                    }
                }
                break;
            case 21:
                // Verify attributes
                if (resultCode == RESULT_OK) {
                    boolean refresh = data.getBooleanExtra("refresh", true);
                    if (refresh) {
                        showNotes();
                    }
                }
                break;
            case 22:
                // 新建笔迹
                if (resultCode == RESULT_OK) {
                    boolean refresh = data.getBooleanExtra("refresh", true);
                    if (refresh) {
                        showNotes();
                    }
                }
                break;
            case 23:
                // 查看笔迹
                if (resultCode == RESULT_OK) {
                    boolean refresh = data.getBooleanExtra("refresh", true);
                    if (refresh) {
                        showNotes();
                    }
                }
                break;
            default:
                break;
        }
    }

    private Drawable getBitmap(Bitmap bitmap,String path) {
        int degree = PublicFunction.getBitmapDegree(path);
        Bitmap bitmap1 = PublicFunction.rotateBitmapByDegree(
                PublicFunction.getRoundedCornerBitmap(bitmap), degree);
        Drawable drawable = new BitmapDrawable(getResources(), bitmap1);
        return drawable;
    }

    // Handle when the a navigation item is selected
    private void setNavDrawer() {
        nDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                performAction(item);
                return true;
            }
        });
    }

    // Perform the action for the selected navigation item
    private void performAction(MenuItem item) {
        // Close the navigation drawer
        mDrawer.closeDrawers();

        // Find which item was selected
        switch (item.getItemId()) {
            case R.id.nav_user_add_attribute:
                // Add a new attribute
                addNote();
                break;

            case R.id.nav_user_change_password:
                // Change password
                changePassword();
                break;
            case R.id.nav_user_settings:
                // Show user settings
                showSettings();
                break;
            case R.id.nav_user_sign_out:
                // Sign out from this account
                signOut();
                break;
            case R.id.nav_user_about:
                // For the inquisitive
                Intent aboutAppActivity = new Intent(this, AboutApp.class);
                startActivity(aboutAppActivity);
                break;
            default:
                break;
        }
    }

    // Get user details from CIP service
    private void getDetails() {
        AppHelper.getPool().getUser(username).getDetailsInBackground(detailsHandler);
    }

    // 显示笔迹列表
    private void showNotes() {
        noteRecycleViewAdapter.clearItems();
        Cursor noteCursor = handWriting.myDataBaseAdapter.fetchAllNoteData();
        Log.d(TAG, "showNotes: " + handWriting.myDataBaseAdapter);
        Log.d(TAG, "showNotes: " + noteCursor.getCount());
        try {
            if (noteCursor != null && noteCursor.moveToFirst()) {
                for (int i = 0; i < noteCursor.getCount(); i++) {
                    noteRecycleViewAdapter.addItem(new NoteInfo(noteCursor));
                    noteCursor.moveToNext();
                }
            }
        } finally {
            if (noteCursor != null) {
                noteCursor.close();
            }
        }
        noteRecycleViewAdapter.notifyDataSetChanged();
    }


    // Show user MFA Settings
    private void showSettings() {
        Intent userSettingsActivity = new Intent(this, SettingsActivity.class);
        startActivityForResult(userSettingsActivity, 20);
    }

    // 新建笔迹
    private void addNote() {
        Intent newNote = new Intent(this, NewNote.class);
        startActivityForResult(newNote, 22);
    }


    // 改变密码
    private void changePassword() {
        Intent changePssActivity = new Intent(this, ChangePasswordActivity.class);
        startActivity(changePssActivity);
    }


    // 退出登录
    private void signOut() {
        user.signOut();
        exit();
    }

    // 初始化
    private void init() {
        // Get the user name
        Bundle extras = getIntent().getExtras();
        username = AppHelper.getCurrUser();

        user = AppHelper.getPool().getUser(username);

        getDetails();
        setBackground();

    }

    private void setBackground() {
        String pictureFile = mSharedPreferences.getString(HandWriting.PICTURE_PATH, "");
        if (pictureFile.length() == 0) {
            mRecycleView.setBackground(ContextCompat.getDrawable(this, R.drawable.background));
        } else {
            Drawable picture = Drawable.createFromPath(pictureFile);
            BitmapDrawable bd = (BitmapDrawable) picture;
            assert bd != null;
            Bitmap bm = bd.getBitmap();
            mRecycleView.setBackground(getBitmap(bm,pictureFile));
        }
    }


    GetDetailsHandler detailsHandler = new GetDetailsHandler() {
        @Override
        public void onSuccess(CognitoUserDetails cognitoUserDetails) {
            closeWaitDialog();
            // Store details in the AppHandler
            AppHelper.setUserDetails(cognitoUserDetails);
            // Trusted devices?
            handleTrustedDevice();
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            showDialogMessage("Could not fetch user details!", AppHelper.formatException(exception), true);
        }
    };

    private void handleTrustedDevice() {
        CognitoDevice newDevice = AppHelper.getNewDevice();
        if (newDevice != null) {
            AppHelper.newDevice(null);
            trustedDeviceDialog(newDevice);
        }
    }

    private void updateDeviceStatus(CognitoDevice device) {
        device.rememberThisDeviceInBackground(trustedDeviceHandler);
    }

    private void trustedDeviceDialog(final CognitoDevice newDevice) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remember this device?");
        //final EditText input = new EditText(UserActivity.this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        //input.setLayoutParams(lp);
        //input.requestFocus();
        //builder.setView(input);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    //String newValue = input.getText().toString();
                    showWaitDialog("Remembering this device...");
                    updateDeviceStatus(newDevice);
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                }
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    // Callback handlers

    UpdateAttributesHandler updateHandler = new UpdateAttributesHandler() {
        @Override
        public void onSuccess(List<CognitoUserCodeDeliveryDetails> attributesVerificationList) {
            // Update successful
            if (attributesVerificationList.size() > 0) {
                showDialogMessage("Updated", "The updated attributes has to be verified", false);
            }
            getDetails();
        }

        @Override
        public void onFailure(Exception exception) {
            // Update failed
            closeWaitDialog();
            showDialogMessage("Update failed", AppHelper.formatException(exception), false);
        }
    };

    GenericHandler trustedDeviceHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            // Close wait dialog
            closeWaitDialog();
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            showDialogMessage("Failed to update device status", AppHelper.formatException(exception), true);
        }
    };

    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.show();
    }

    private void showDialogMessage(String title, String body, final boolean exit) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if (exit) {
                        exit();
                    }
                } catch (Exception e) {
                    // Log failure
                    Log.e(TAG, "Dialog dismiss failed");
                    if (exit) {
                        exit();
                    }
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        } catch (Exception e) {
            //
        }
    }

    private void exit() {
        Intent intent = new Intent();
        if (username == null) {
            username = "";
        }
        intent.putExtra("name", username);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void newNote(View view) {
        addNote();
    }
}
