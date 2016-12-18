package com.mydemo.videoplayerdemo;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.mydemo.videoplayerdemo.Bean.VideoBean;
import com.mydemo.videoplayerdemo.adapter.VideoRecyclerAdapter;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements VideoRecyclerAdapter.OnItemClickListener
{

    private RecyclerView mVideoRecyclerView;
    private VideoRecyclerAdapter mVideoAdapter;
    private Map<Integer, String> thumbnailsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initViewData(); //初始化时 拿到数据
        setContentView(R.layout.activity_main);
        mVideoRecyclerView = (RecyclerView) findViewById(R.id.rv);
        //设置布局管理器
        mVideoRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mVideoAdapter != null)
            mVideoAdapter.releaseCursor();
    }

    private void initViewData()
    {
        new GetVideoDataTask().execute(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI);
    }

    @Override
    public void OnItemClick(VideoBean bean)
    {
        Intent intent = new Intent(this, XVideoPlayerActivity.class);
        intent.putExtra("videoBean", bean);
        startActivity(intent);
    }

    private class GetVideoDataTask extends AsyncTask<Uri, Void, Cursor>
    {
        @Override
        protected Cursor doInBackground(Uri... uris)
        {
            Cursor[] cursor = new Cursor[uris.length];
            Uri videoUri = uris[0];
            Uri thumbnailsUri = uris[1];
            cursor[0] = MainActivity.this.getContentResolver().query(videoUri, null, null, null, null);
            cursor[1] = MainActivity.this.getContentResolver().query(thumbnailsUri, null, null, null, null);
            while (cursor[1].moveToNext())
            {
                int videoId = cursor[1].getInt(cursor[1].getColumnIndex(MediaStore.Video.Thumbnails.VIDEO_ID));
                String thumbnailPath = cursor[1].getString(cursor[1].getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                thumbnailsMap.put(videoId, thumbnailPath);
            }
            cursor[1].close();
            return cursor[0];
        }

        @Override
        protected void onPostExecute(Cursor cursors)
        {
            super.onPostExecute(cursors);
            mVideoAdapter = new VideoRecyclerAdapter(MainActivity.this, cursors, thumbnailsMap);
            mVideoRecyclerView.setAdapter(mVideoAdapter);
            mVideoAdapter.setOnItemClickListener(MainActivity.this);
        }

    }
}
