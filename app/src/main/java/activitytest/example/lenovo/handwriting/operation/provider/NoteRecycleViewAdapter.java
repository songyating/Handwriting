package activitytest.example.lenovo.handwriting.operation.provider;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import activitytest.example.lenovo.handwriting.R;
import butterknife.BindView;

public class NoteRecycleViewAdapter extends RecyclerView.Adapter<NoteRecycleViewAdapter.ViewHolder> {

    public Context mContext;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.date)
    TextView date;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.card_view)
    CardView cardView;
    private OnItemClickListener mOnItemClickListener;
    private List<NoteInfo> mNoteInfos = new ArrayList<NoteInfo>();

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * 为Activity提供设置OnItemClickListener的接口
     *
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleview_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final NoteInfo noteInfo = (NoteInfo) getItem(position);
        bindData(noteInfo);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void bindData(NoteInfo noteInfo) {
       title.setText(noteInfo.getTitle());
       content.setText(noteInfo.getContent());
       date.setText(Math.toIntExact(noteInfo.getDate()));
    }

    /**
     * 获取对应位置上的position信息
     *
     * @param position
     * @return
     */
    public Object getItem(int position) {
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


    @Override
    public int getItemCount() {
        return mNoteInfos.size();
    }
    /**
     * 清除所有
     */
    public void clearItems() {
        mNoteInfos.clear();
    }

}
