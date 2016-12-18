package com.mydemo.videoplayerdemo.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mydemo.videoplayerdemo.Bean.VideoBean;
import com.mydemo.videoplayerdemo.R;
import com.mydemo.videoplayerdemo.utils.Utils;

import java.util.Map;

/**
 * Created by chenlong on 2016/12/16.
 */
public class VideoRecyclerAdapter extends RecyclerView.Adapter
{
    private Context mContext;
    private Cursor mCursor;
    private Map<Integer, String> thumbnailsMap;

    public VideoRecyclerAdapter(Context context, Cursor cursors, Map<Integer, String> thumbnailsMap)
    {
        this.mContext = context;
        this.mCursor = cursors;
        this.thumbnailsMap = thumbnailsMap;
    }

    public void releaseCursor()
    {
        if (mCursor != null)
            mCursor.close();
    }

    /**
     * 接口回调
     */
    public interface OnItemClickListener
    {
        void OnItemClick(VideoBean bean);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View inflateView = inflater.inflate(R.layout.item_video_view, parent, false);
        final VideoBean bean = new VideoBean();     //加final基本等同于全局变量
        VideoViewHolder holder = new VideoViewHolder(inflateView, bean);

        inflateView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (listener != null)
                {
                    listener.OnItemClick(bean);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
        mCursor.moveToPosition(position);               //根据position的位置获取对应的cursor的值
        videoViewHolder.bean.bindBean(mCursor);         //优化复用 给bean赋值
        videoViewHolder.bindData();                     //优化viewHolder绑定数据
    }

    @Override
    public int getItemCount()
    {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    private class VideoViewHolder extends RecyclerView.ViewHolder
    {
        VideoBean bean;
        ImageView icon;
        TextView name;
        TextView duration;
        TextView size;

        public VideoViewHolder(View itemView, VideoBean bean)
        {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            duration = (TextView) itemView.findViewById(R.id.duration);
            name = (TextView) itemView.findViewById(R.id.name);
            size = (TextView) itemView.findViewById(R.id.size);
            this.bean = bean;
        }

        public void bindData()
        {
            name.setText(bean.getName());
            size.setText(Formatter.formatFileSize(mContext, bean.getSize()));
            duration.setText(Utils.formatFileDuration(bean.getDuration()));
            String thumbnailPath = thumbnailsMap.get(bean.getId());

            if (!TextUtils.isEmpty(thumbnailPath))
            {
                Bitmap bitmap = BitmapFactory.decodeFile(thumbnailPath);
                icon.setImageBitmap(bitmap);
            } else
            {
                //手动加载缩略图
                Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(bean.getPath(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                if (videoThumbnail == null)
                {
                    icon.setImageResource(R.mipmap.ic_launcher);
                } else
                {
                    icon.setImageBitmap(videoThumbnail);
                }
            }
        }
    }
}

