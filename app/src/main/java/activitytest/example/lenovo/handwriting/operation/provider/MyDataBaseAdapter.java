package activitytest.example.lenovo.handwriting.operation.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.text.SimpleDateFormat;

/**
 * 该类用于创建数据库，保存日记信息，提供了数据的更新，插入，查询,删除等基本功能
 *
 * @author syt
 */
public class MyDataBaseAdapter {
    public final static String DB_NAME = "Handwriting.db"; // 数据库名称
    private final static String DB_TABLE_NOTE = "NoteInfos"; // 数据库表名
    private final static int DB_VERSION = 1; // 数据库版本
    private final static SimpleDateFormat DB_TIMESTAMP_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    /**
     * @description 日记表列
     */
    public static class NotesColumns implements BaseColumns {
        public static final String _ID = "_id"; // 表中一条数据的id
        public static final String DATE = "date";//日期时间
        public static final String TITLE = "title";//日记标题
        public static final String CONTENT = "content";// 日记内容

        static final String[] NOTES_QUERY_COLUMNS = {_ID, DATE, TITLE, CONTENT};

        public static final int _ID_INDEX = 0;
        public static final int DATE_INDEX = 1;
        public static final int TITLE_INDEX = 2;
        public static final int CONTENT_INDEX = 3;
    }

    // 创建Timer表
    private static final String DB_CREATE_TABLE_NOTE = "CREATE TABLE "
            + DB_TABLE_NOTE + "(" + NotesColumns._ID
            + " INTEGER PRIMARY KEY ," + NotesColumns.DATE + " INTEGER,"
            + NotesColumns.TITLE + " VARCHAR(100)," + NotesColumns.CONTENT
            + " VARCHAR(200));";
    private Context mContext = null; // 本地Context对象


    private static class DatabaseHelper extends SQLiteOpenHelper {
        /**
         * 构造函数-创建一个数据库
         *
         * @param context
         */
        DatabaseHelper(Context context, String dbName, int dbVersion) {
            super(context, dbName, null, dbVersion);
        }

        /**
         * 创建数据库
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_TABLE_NOTE);// 创建表Timer
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    /**
     * 执行open（）打开数据库时，保存返回的数据库对象
     */
    private SQLiteDatabase mSQLiteDatabase = null;

    /**
     * 由SQLiteOpenHelper继承过来
     */
    private DatabaseHelper mDatabaseHelper = null;

    /**
     * 构造函数-取得Context
     *
     * @param context
     */
    public MyDataBaseAdapter(Context context) {
        mContext = context;
    }

    /**
     * 关闭数据库
     */
    public void close() {
        mDatabaseHelper.close();
    }

    /**
     * 获取只读数据库
     *
     * @author CZ
     */
    public SQLiteDatabase getAlarmReadableDatabase() {
        return mSQLiteDatabase;
    }

    /**
     * 获取可写数据库
     *
     * @author CZ
     */
    public SQLiteDatabase getAlarmWriteableDatabase() {
        return mSQLiteDatabase;
    }


    /**
     * 打开数据库，返回数据库对象
     *
     * @throws SQLException
     */
    public void open() throws SQLException {
        Log.d("Main", "open: ");
        if (isOpen()) {
            return;
        } else {
            mDatabaseHelper = new DatabaseHelper(mContext, DB_NAME, DB_VERSION);
            mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
            try {
                mSQLiteDatabase.query(DB_TABLE_NOTE, null, null, null, null,
                        null, null);
            } catch (Exception e) {
                mSQLiteDatabase.execSQL(DB_CREATE_TABLE_NOTE);// 创建表Timer
            }

        }
    }

    /**
     * myDataBaseAdapter判断数据库是否打开
     */
    public boolean isOpen() {
        if (mSQLiteDatabase == null) {
            return false;
        }
        return mSQLiteDatabase.isOpen();
    }

    /**
     * 添加一条数据,按数据在数据库中的列序依次插入
     *
     * @param noteInfo {初始化NoteInfo类,并在函数中调用该类的方法}
     * @return
     */
    public long insertData(NoteInfo noteInfo) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(NotesColumns.CONTENT, noteInfo.getContent());
        initialValues.put(NotesColumns.TITLE, noteInfo.getTitle());
        initialValues.put(NotesColumns.DATE, noteInfo.getDate());
        //initialValues.put(NotesColumns.IMAGE,noteInfo.getImageUri());
        try {
            return mSQLiteDatabase.insert(DB_TABLE_NOTE, null, initialValues);
        } catch (Exception e) {
            mSQLiteDatabase.execSQL(DB_CREATE_TABLE_NOTE);// 创建表Timer
            return mSQLiteDatabase.insert(DB_TABLE_NOTE, null, initialValues);
        }
    }

    /**
     * 查询指定数据
     *
     * @param id 日记的ID,唯一标识
     * @return
     * @throws SQLException
     */
    public Cursor fetchNoteData(int id) {
        Cursor mCursor = null;
        try {
            mCursor = mSQLiteDatabase.query(true, DB_TABLE_NOTE,
                    NotesColumns.NOTES_QUERY_COLUMNS, NotesColumns._ID + "="
                            + id, null, null, null, null, null);
            mCursor.moveToFirst();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mCursor;
    }

    public Cursor fetchAllNoteData() {
        try {
            Cursor cursor = mSQLiteDatabase.query(true, DB_TABLE_NOTE,
                    NotesColumns.NOTES_QUERY_COLUMNS, null, null,
                    null, null, "id desc", null);
            cursor.moveToFirst();
            return cursor;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // mSQLiteDatabase.execSQL(DB_CREATE_TABLE_NOTE);
            Log.d("SSS", "fetchAllNoteData: " + e.toString());
            return null;
        }
    }

    /***
     * 通过各个字段值修改NOTE表数据
     *
     * @param noteID
     * @param noteInfo
     * @return
     */
    public boolean updateColumns(int noteID, NoteInfo noteInfo) {
        ContentValues args = new ContentValues();

        args.put(NotesColumns.DATE, noteInfo.getDate());
        args.put(NotesColumns.TITLE, noteInfo.getTitle());
        args.put(NotesColumns.CONTENT, noteInfo.getContent());

        return mSQLiteDatabase.update(DB_TABLE_NOTE, args, NotesColumns._ID
                + "=" + noteID, null) > 0;
    }

    /**
     * 删除一条数据
     *
     * @param id 日记的ID,唯一标识
     * @return
     */
    public boolean deleteData(int id) {
        return mSQLiteDatabase.delete(DB_TABLE_NOTE, NotesColumns._ID + "="
                + id, null) > 0;
    }

    /**
     * 删除所有数据
     */
    public boolean deleteAllData() {
        return mSQLiteDatabase.delete(DB_TABLE_NOTE, null, null) > 0;
    }
}
