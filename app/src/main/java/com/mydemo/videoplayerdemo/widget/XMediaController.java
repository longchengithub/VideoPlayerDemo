package com.mydemo.videoplayerdemo.widget;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.mydemo.videoplayerdemo.Bean.VideoBean;
import com.mydemo.videoplayerdemo.R;
import com.mydemo.videoplayerdemo.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static rx.Observable.timer;

/**
 * Created by chenlong on 2016/12/17.
 */

public class XMediaController extends LinearLayout implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener
{
    @BindView(R.id.tv_controller_title)
    TextView tvControllerTitle;
    @BindView(R.id.tv_controller_time)
    TextView tvControllerTime;
    @BindView(R.id.iv_controller_battery)
    ImageView ivControllerBattery;
    @BindView(R.id.iv_controller_mute)
    ImageView ivControllerMute;
    @BindView(R.id.sb_controller_volume)
    SeekBar sbControllerVolume;
    @BindView(R.id.ll_controller_top)
    LinearLayout llControllerTop;
    @BindView(R.id.tv_controller_passed_time)
    TextView tvControllerPassedTime;
    @BindView(R.id.sb_controller_position)
    SeekBar sbControllerPosition;
    @BindView(R.id.tv_controller_total_time)
    TextView tvControllerTotalTime;
    @BindView(R.id.iv_controller_exit)
    ImageView ivControllerExit;
    @BindView(R.id.iv_controller_prev)
    ImageView ivControllerPrev;
    @BindView(R.id.iv_controller_play)
    ImageView ivControllerPlay;
    @BindView(R.id.iv_controller_next)
    ImageView ivControllerNext;
    @BindView(R.id.iv_controller_full_screen)
    ImageView ivControllerFullScreen;
    @BindView(R.id.ll_controller_bottom)
    LinearLayout llControllerBottom;

    private Context mContext;
    private BatteryReceiver batteryReceiver;
    private VideoView vv;
    private VideoBean bean;
    private CompositeSubscription compositeSubscription;
    private Subscription subscription;
    private AudioManager am;

    int[] batteryIcons = new int[]{
            R.mipmap.ic_battery_0,
            R.mipmap.ic_battery_10,
            R.mipmap.ic_battery_20,
            R.mipmap.ic_battery_20,
            R.mipmap.ic_battery_40,
            R.mipmap.ic_battery_40,
            R.mipmap.ic_battery_60,
            R.mipmap.ic_battery_60,
            R.mipmap.ic_battery_80,
            R.mipmap.ic_battery_80,
            R.mipmap.ic_battery_100};
    private VolumeReceiver volumeReceiver;
    private Subscription videoSubscription;
    private Subscription hideControllerSubscription;

    public XMediaController(Context context)
    {
        this(context, null);
    }

    public XMediaController(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public XMediaController(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        mContext = context;
        View.inflate(context, R.layout.view_controller, this);
        ButterKnife.bind(this);

        //注册广播接受者
        initRegister();
    }

    /**
     * 注册广播
     */
    private void initRegister()
    {
        IntentFilter batteryFilter = new IntentFilter();
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        batteryReceiver = new BatteryReceiver();
        mContext.registerReceiver(batteryReceiver, batteryFilter);

        IntentFilter volumeFilter = new IntentFilter();
        volumeFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        volumeReceiver = new VolumeReceiver();
        mContext.registerReceiver(volumeReceiver, volumeFilter);
    }

    /**
     * 准备视频播放完毕
     *
     * @param mediaPlayer
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        mediaPlayer.start();        //准备完毕后设置一些默认的图标 跟 文本状态等
        tvControllerTitle.setText(bean.getName());
        ivControllerPlay.setImageResource(R.drawable.btn_pause_selector);
        tvControllerTotalTime.setText(Utils.formatFileDuration(bean.getDuration()));
        sbControllerPosition.setMax((int) bean.getDuration());
        hideController();
    }

    boolean isHideController;

    /**
     * 延时3秒隐藏
     */
    private void delayHide()
    {
        hideControllerSubscription = Observable.timer(3, TimeUnit.SECONDS, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>()
                {
                    @Override
                    public void call(Long aLong)
                    {
                        if (!isHideController)
                        {
                            hideController();
                        }
                    }
                });

        compositeSubscription.add(hideControllerSubscription);
    }

    /**
     * 隐藏控制器动画
     */
    private void hideController()
    {
        ObjectAnimator bottomAnimator = ObjectAnimator.ofFloat(llControllerBottom,
                "translationY", 0, llControllerBottom.getHeight());
        bottomAnimator.setDuration(300);
        bottomAnimator.start();

        ObjectAnimator topAnimator = ObjectAnimator.ofFloat(llControllerTop,
                "translationY", 0, -llControllerTop.getHeight());
        topAnimator.setDuration(300);
        topAnimator.start();

        isHideController = true;
    }

    /**
     * 播放完毕
     *
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp)
    {
        //播放完毕后 改变状态
        ivControllerPlay.setImageResource(R.drawable.btn_play_selector);
        sbControllerPosition.setProgress(0);
        tvControllerPassedTime.setText("00:00:00");
        compositeSubscription.remove(videoSubscription);
    }

    /**
     * 广播监听电量变化
     */
    private class BatteryReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            int battery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            ivControllerBattery.setImageResource(batteryIcons[battery / 10]);
        }
    }

    /**
     * 广播监听音量变化
     */
    private class VolumeReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            sbControllerVolume.setProgress(currentVolume);
            if (currentVolume == 0)
            {
                ivControllerMute.setImageResource(R.mipmap.volume_off);
            } else
            {
                ivControllerMute.setImageResource(R.mipmap.volume_up);
            }
        }
    }

    public void setVideoViewAndData(VideoView videoView, VideoBean videoBean)
    {
        vv = videoView;
        bean = videoBean;

        vv.setVideoPath(videoBean.getPath());
        vv.setOnPreparedListener(this);
        vv.setOnCompletionListener(this);

        /**
         * 设置显示时间同步每秒更新一次
         */
        setControllerTime();

        /**
         * seekBar同步改变音量的大小
         */
        setSeekBarThings();

        /**
         * 禁音键
         */
        setMute();

        /**
         * 播放键
         */
        setPlay();

        /**
         * 设置video进度条同步进度
         */
        setPassed();                //同步更新进度条
        setVideoSeekBarThings();    //移动监听

        /**
         * 退出键
         */
        setBackPressed();

        /**
         * 全屏切换
         */
        setFullScreen();
    }

    /**
     * 分发事件阶段控制显示或隐藏或延时
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if (isHideController)
                {
                    showController();
                    delayHide();
                } else
                {
                    compositeSubscription.remove(hideControllerSubscription);
                }
                break;
            case MotionEvent.ACTION_UP:
                delayHide();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 显示控制条
     */

    private void showController()
    {
        ObjectAnimator topAnimator = ObjectAnimator.ofFloat(llControllerTop, "translationY", llControllerTop.getTranslationY(), 0);
        ObjectAnimator bottomAnimator = ObjectAnimator.ofFloat(llControllerBottom, "translationY", llControllerBottom.getTranslationY(), 0);
        topAnimator.setDuration(300);
        bottomAnimator.setDuration(300);
        topAnimator.start();
        bottomAnimator.start();

        isHideController = false;
    }

    boolean isFullScreen;

    /**
     * 全屏键
     */
    private void setFullScreen()
    {
        ivControllerFullScreen.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isFullScreen)
                {
                    isFullScreen = false;
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    vv.setLayoutParams(layoutParams);
                } else
                {
                    isFullScreen = true;
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) vv.getLayoutParams();
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                }
            }
        });
    }

    /**
     * 接口回调 退出键接口
     */
    public interface OnExitVideoListener
    {
        void OnExit();
    }

    private OnExitVideoListener listener;

    public void setOnExitVideoListener(OnExitVideoListener listener)
    {
        this.listener = listener;
    }

    /**
     * 退出键
     */
    private void setBackPressed()
    {
        ivControllerExit.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (listener != null)
                {
                    listener.OnExit();
                }
            }
        });
    }

    /**
     * 视频进度条拖动
     */
    private void setVideoSeekBarThings()
    {
        sbControllerPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (fromUser)
                {
                    tvControllerPassedTime.setText(Utils.formatFileDuration(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                compositeSubscription.remove(videoSubscription);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                vv.seekTo(seekBar.getProgress());
                setPassed();
            }
        });
    }

    /**
     * 视频进度条
     */
    private void setPassed()
    {    //每隔200毫秒重复执行一段代码
        videoSubscription = timer(200, TimeUnit.MILLISECONDS)
                .repeat(Integer.MAX_VALUE, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>()
                {
                    @Override
                    public void call(Long aLong)
                    {
                        int currentPosition =  vv.getCurrentPosition();
                        sbControllerPosition.setProgress(currentPosition);
                        tvControllerPassedTime.setText(Utils.formatFileDuration(currentPosition));
                    }
                });
        compositeSubscription.add(videoSubscription);
    }

    /**
     * 播放键
     */
    private void setPlay()
    {
        ivControllerPlay.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (vv.isPlaying())
                {
                    vv.pause();     //暂停
                    ivControllerPlay.setImageResource(R.drawable.btn_play_selector);
                    compositeSubscription.remove(videoSubscription);  //移除更新进度条
                    compositeSubscription.remove(hideControllerSubscription);
                } else
                {
                    vv.start();     //开始
                    ivControllerPlay.setImageResource(R.drawable.btn_pause_selector);
                    setPassed();    //开启更新进度条
                    delayHide();
                }
            }
        });
    }

    //记录禁音前的音量
    private int lastVolume;

    /**
     * 禁音键
     */
    private void setMute()
    {
        ivControllerMute.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (currentVolume == 0)
                {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, lastVolume, 0);
                } else
                {
                    lastVolume = currentVolume;         //点击禁音记录之前的音量
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                }
            }
        });
    }

    /**
     * 音量键
     */
    private void setSeekBarThings()
    {
        am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        //同步seekBar与音量键的最大值
        sbControllerVolume.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        //回显音量大小
        int streamVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        sbControllerVolume.setProgress(streamVolume);
        if (streamVolume == 0)  //如果为0 就显示静音的图片
            ivControllerMute.setImageResource(R.mipmap.volume_off);

        //seekBar的滑动监听
        sbControllerVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
    }

    /**
     * 设置时间
     */
    private void setControllerTime()
    {
        compositeSubscription = new CompositeSubscription();
        subscription = timer(1, TimeUnit.SECONDS)
                .repeat(Integer.MAX_VALUE, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>()
                {
                    @Override
                    public void call(Long aLong)
                    {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                        tvControllerTime.setText(dateFormat.format(new Date()));
                    }
                });
        compositeSubscription.add(subscription);
    }

    /**
     * 解除广播
     */
    public void onDestroy()
    {
        mContext.unregisterReceiver(batteryReceiver);
        mContext.unregisterReceiver(volumeReceiver);
        compositeSubscription.remove(subscription);
    }
}

