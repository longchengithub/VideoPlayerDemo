package com.mydemo.videoplayerdemo;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

import com.mydemo.videoplayerdemo.Bean.VideoBean;
import com.mydemo.videoplayerdemo.widget.XMediaController;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenlong on 2016/12/16.
 */
public class XVideoPlayerActivity extends AppCompatActivity implements XMediaController.OnExitVideoListener
{
    @BindView(R.id.vv)
    VideoView vv;
    @BindView(R.id.xmc)
    XMediaController xmc;
    private VideoBean bean;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);

        bean = (VideoBean) getIntent().getSerializableExtra("videoBean");
        if (bean == null)
        {
            Uri data = getIntent().getData();
            Cursor cursor = getContentResolver().query(data, null, null, null, null);
            cursor.moveToNext();
            bean = new VideoBean();
            bean.bindBean(cursor);
            cursor.close();
        }
        xmc.setVideoViewAndData(vv, bean);
        xmc.setOnExitVideoListener(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        xmc.onDestroy();
    }

    @Override
    public void OnExit()
    {
        finish();
    }

}
