package activitytest.example.lenovo.handwriting.operation.provider;

import android.database.Cursor;
import android.util.Log;

import activitytest.example.lenovo.handwriting.HandWriting;
import activitytest.example.lenovo.handwriting.operation.provider.MyDataBaseAdapter.NotesColumns;

/**
 * @author syt
 * <p>
 * Note实体类：包含一条Note的全部信息
 */
public class NoteInfo {
    private HandWriting handWriting;
    private int id;
    private long date;
    private String title;
    private String content;

    public NoteInfo( Cursor cursor){
        this.id = cursor.getInt(NotesColumns._ID_INDEX);
        this.date = cursor.getLong(NotesColumns.DATE_INDEX);
        this.content = cursor.getString(NotesColumns.CONTENT_INDEX);
        this.title = cursor.getString(NotesColumns.TITLE_INDEX);
        Log.d("Main", "NoteInfo: "+id+" "+date+" "+content+" "+title);
    }
    public NoteInfo(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
