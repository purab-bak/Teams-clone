package com.lite.housepartynew.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lite.housepartynew.R;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_ID = 22;

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };
    private static final String TAG = "tag";

    private RtcEngine mRtcEngine;

    private FrameLayout mLocalContainer;
    private RelativeLayout mRemoteContainer;
    private SurfaceView mLocalView;
    private SurfaceView mRemoteView;

    private ImageView mCallBtn, mMuteBtn, mSwitchCameraBtn;

    private boolean mCallEnd, mMuted;



    private final IRtcEngineEventHandler mRtcHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("tag", "Join channel success, uid" + (uid));
                }
            });

        }

        @Override
        public void onUserOffline(int uid, int reason) {
            super.onUserOffline(uid, reason);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("tag", "User offline, uid" + (uid));
                    removeRemoteVideo();
                }
            });
        }

        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed);

            if (state == Constants.REMOTE_VIDEO_STATE_STARTING){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("tag", "RemoteVideo Starting, uid" + (uid));
                        setupRemoteVideo(uid);
                    }
                });
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUi();

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            //init engine

            initEngineAndJoinChannel();

        }
    }

    private void initUi() {

        mLocalContainer = findViewById(R.id.local_video_view_container);
        mRemoteContainer = findViewById(R.id.remote_video_view_container);

        mCallBtn = findViewById(R.id.btn_call);
        mMuteBtn = findViewById(R.id.btn_mute);
        mSwitchCameraBtn = findViewById(R.id.btn_switch_camera);
    }

    private void initEngineAndJoinChannel() {

        //init engine
        initializeEngine();

        //setup video config
        setupVideoConfig();

        //setup local video
        setupLocalVideo();

        //join channel
        joinChanel();

    }

    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcHandler);
            Toast.makeText(MainActivity.this, "Initialized Engine", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("Need to check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig(){
        mRtcEngine.enableVideo();

        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
                ));

        Toast.makeText(MainActivity.this, "Setup Video config", Toast.LENGTH_SHORT).show();

    }

    private void setupLocalVideo(){
        mRtcEngine.enableVideo();

        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        mLocalContainer.addView(mLocalView);

        VideoCanvas localVideoCanvas = new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        mRtcEngine.setupLocalVideo(localVideoCanvas);

        Toast.makeText(MainActivity.this, "Setup Local Video", Toast.LENGTH_SHORT).show();

    }



    private void setupRemoteVideo(int uid){

        int count = mRemoteContainer.getChildCount();

        View view = null;

        for (int i=0; i<count; i++){
            View v = mRemoteContainer.getChildAt(i);
            if (v.getTag() instanceof Integer && ((int) v.getTag()) == uid){
                view = v;
            }
        }

        if (view != null){
            return;
        }

        mRemoteView = RtcEngine.CreateRendererView(getBaseContext());
        mRemoteContainer.addView(mRemoteView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        mRemoteView.setTag(uid);
    }

    private void removeRemoteVideo() {

        if (mRemoteView != null){
            mRemoteContainer.removeView(mRemoteView);
        }

        mRemoteView = null;

    }


    private void joinChanel() {
        String token = getString(R.string.agora_access_token);
        if (TextUtils.isEmpty(token)){
            token = null;
        }

        mRtcEngine.joinChannel(token, "demoChannel1", "", 0);

        Toast.makeText(MainActivity.this, "Joined channel successfully", Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_REQ_ID:{
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED){
                    break;
                }
                initEngineAndJoinChannel();
                break;
            }
        }
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    public void onLocalAudioMuteClicked(View view) {

        mMuted = !mMuted;
        mRtcEngine.muteLocalAudioStream(mMuted);
        int res = mMuted?R.drawable.btn_mute : R.drawable.btn_unmute;

        mMuteBtn.setImageResource(res);
    }


    public void onSwitchCameraClicked(View view) {
        mRtcEngine.switchCamera();
    }

    public void onCallClicked(View view) {

        if (mCallEnd){
            startCall();
            mCallEnd = false;
            mCallBtn.setImageResource(R.drawable.btn_endcall);
        }
        else {
            endCall();
            mCallEnd = true;
            mCallBtn.setImageResource(R.drawable.btn_startcall);
        }

        showButtons(!mCallEnd);
    }

    private void startCall() {
        setupLocalVideo();
        joinChanel();
    }

    private void endCall() {

        removeLocalVideo();
        removeRemoteVideo();
        leaveChannel();

    }

    private void removeLocalVideo() {
        if (mLocalView != null){
            mLocalContainer.removeView(mLocalView);
        }

        mLocalView = null;
    }


    private void showButtons(boolean show) {

        int visibility = show? View.VISIBLE : View.GONE;

        mMuteBtn.setVisibility(visibility);
        mSwitchCameraBtn.setVisibility(visibility);

    }

    private void leaveChannel() {

        mRtcEngine.leaveChannel();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mCallEnd){
            leaveChannel();
        }

        RtcEngine.destroy();
    }
}