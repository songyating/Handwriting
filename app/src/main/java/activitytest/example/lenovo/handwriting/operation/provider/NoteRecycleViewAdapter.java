package activitytest.example.lenovo.handwriting.operation.provider;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import activitytest.example.lenovo.handwriting.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class NoteRecycleViewAdapter extends RecyclerView.Adapter<NoteRecycleViewAdapter.ViewHolder> {

    public Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private List<NoteInfo> mNoteInfos = new ArrayList<NoteInfo>();

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onLongItemClick(int position);
    }

    /**
     * 为Activity提供设置OnItemClickListener的接口
     *
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.content)
        TextView content;
        @BindView(R.id.card_view)
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick( getItemIdNum(getAdapterPosition()));
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onLongItemClick( getItemIdNum(getAdapterPosition()));
                    return false;
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void bindData(NoteInfo noteInfo) {
            title.setText(noteInfo.getTitle());
            content.setText(noteInfo.getContent());
            long time = noteInfo.getDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String sd = sdf.format(new Date(Long.parseLong(String.valueOf(time))));      // 时间戳转换成时间
            date.setText(sd);
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("Main", "onCreateViewHolder: ");
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleview_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("Main", "onBindViewHolder: ");
        NoteInfo noteInfo = (NoteInfo) getItem(position);
        holder.bindData(noteInfo);
    }

    /**
     * 获取对应位置上的position信息
     *
     * @param position
     * @return
     */
    private Object getItem(int position) {
        return mNoteInfos.get(position);
    }

    /**
     * 添加项
     *
     * @param alarmInfo
     */
    public void addItem(NoteInfo alarmInfo) {
        mNoteInfos.add(alarmInfo);
    }


    @Override
    public long getItemId(int position) {
        return mNoteInfos.get(position).getId();
    }

    public int getItemIdNum(int pos){
        return mNoteInfos.get(pos).getId();
    }


    @Override
    public int getItemCount() {
        Log.d("Main", "getItemCount: " + mNoteInfos.size());
        return mNoteInfos.size();
    }

    /**
     * 清除所有
     */
    public void clearItems() {
        mNoteInfos.clear();
    }

}
