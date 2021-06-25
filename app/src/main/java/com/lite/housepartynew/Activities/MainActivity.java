package com.lite.housepartynew.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.lite.housepartynew.Layouts.GridVideoViewContainer;
import com.lite.housepartynew.Layouts.RecyclerItemClickListener;
import com.lite.housepartynew.Models.AgoraUser;
import com.lite.housepartynew.Models.SessionInfo;
import com.lite.housepartynew.Models.UserStatusData;
import com.lite.housepartynew.R;
import com.lite.housepartynew.utils.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private RelativeLayout mRemoteContainer2, mRemoteContainer3;
    private SurfaceView mLocalView;
    private SurfaceView mRemoteView;
    private SurfaceView mRemoteView2, mRemoteView3;

    private ImageView mCallBtn, mMuteBtn, mSwitchCameraBtn;

    private boolean mCallEnd, mMuted;

    String channelName;

    FirebaseUser mCurrentUser;
    FirebaseAuth mAuth;

    DatabaseReference activeChannelsRef;
    int userCount;

    SessionInfo currentSessionInfo;

    //multiple users feature
    private final HashMap<Integer, SurfaceView> mUidsList = new HashMap<>();
    private AgoraUser agoraUser;
    private GridVideoViewContainer mGridVideoViewContainer;

    public static final int LAYOUT_TYPE_DEFAULT = 0;
    public int mLayoutType = LAYOUT_TYPE_DEFAULT;

    private String userName="lul";
    private boolean isMuted = false;


    private final IRtcEngineEventHandler mRtcHandler = new IRtcEngineEventHandler() {

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    currentSessionInfo.setUserCount(1);

                    Toast.makeText(MainActivity.this, "My UID : " + uid, Toast.LENGTH_SHORT).show();
                    //updateDatabase(uid);

                    //multipleUsers
                    SurfaceView localView = mUidsList.remove(0);
                    mUidsList.put(uid, localView);

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
                    onRemoteUserLeft(uid);
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
                        currentSessionInfo.setUserCount(currentSessionInfo.getUserCount()+1);

                        setupRemoteVideo(uid);
                        //userCount = getUserCountFromDatabase();

                        Toast.makeText(MainActivity.this, "User count local "+currentSessionInfo.getUserCount(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set fullscreen
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
            }
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        initUi();

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            //init engine

            initEngineAndJoinChannel();

        }

    }

    private void initUi() {

        channelName = getIntent().getStringExtra("channelName");

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        activeChannelsRef = FirebaseDatabase.getInstance().getReference().child("active-channels");

        currentSessionInfo = new SessionInfo(0, channelName);

        ///multiple users
        agoraUser = new AgoraUser();
        //mCallBtn = findViewById(R.id.start_call_end_call_btn);
        mMuteBtn = findViewById(R.id.audio_mute_audio_unmute_btn);
        mSwitchCameraBtn = findViewById(R.id.switch_camera_btn);
        mGridVideoViewContainer = findViewById(R.id.grid_video_view_container);

    }

    private void initEngineAndJoinChannel() {

        //init engine
        initializeEngine();

        //setup video config
        setupVideoConfig();

        //setup local video
        setupLocalVideo();

        //join channel
        joinChannel();

    }


    //done
    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcHandler);
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

    }


    //done
    private void setupLocalVideo(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRtcEngine.enableVideo();
                mRtcEngine.enableInEarMonitoring(true);
                mRtcEngine.setInEarMonitoringVolume(80);

                SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
                mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
                surfaceView.setZOrderOnTop(false);
                surfaceView.setZOrderMediaOverlay(false);

                mUidsList.put(0, surfaceView);

                mGridVideoViewContainer.initViewContainer(MainActivity.this, 0, mUidsList, false);
            }
        });

    }

    //done
    private void joinChannel() {
        String token = getString(R.string.agora_access_token);
        if (TextUtils.isEmpty(token)){
            token = null;
        }

        mRtcEngine.joinChannel(token, channelName, "", 0);

    }

    //done
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

    //done
    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    //done
    private void onRemoteUserLeft(int uid) {
        removeRemoteVideo(uid);
    }


    //done
    private void removeRemoteVideo(final int uid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Object target = mUidsList.remove(uid);
                if (target == null) {
                    return;
                }
                switchToDefaultVideoView();
            }
        });

    }


    //done
    private void setupRemoteVideo(int uid){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SurfaceView mRemoteView = RtcEngine.CreateRendererView(getApplicationContext());

                mUidsList.put(uid, mRemoteView);
                mRemoteView.setZOrderOnTop(true);
                mRemoteView.setZOrderMediaOverlay(true);
                mRtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));

                switchToDefaultVideoView();
            }
        });

    }


    //done
    private void switchToDefaultVideoView() {

        mGridVideoViewContainer.initViewContainer(MainActivity.this, agoraUser.getAgoraUid(), mUidsList, false);

        boolean setRemoteUserPriorityFlag = false;

        mLayoutType = LAYOUT_TYPE_DEFAULT;

        int sizeLimit = mUidsList.size();
        if (sizeLimit > 5) {
            sizeLimit = 5;
        }

        for (int i = 0; i < sizeLimit; i++) {
            int uid = mGridVideoViewContainer.getItem(i).mUid;
            if (agoraUser.getAgoraUid() != uid) {
                if (!setRemoteUserPriorityFlag) {
                    setRemoteUserPriorityFlag = true;
                    mRtcEngine.setRemoteUserPriority(uid, Constants.USER_PRIORITY_HIGH);
                } else {
                    mRtcEngine.setRemoteUserPriority(uid, Constants.USER_PRIORITY_NORMAL);
                }
            }
        }
    }

    //done
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mCallEnd){
            leaveChannel();
        }

        RtcEngine.destroy();
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    //done
    private void finishCalling() {
        leaveChannel();
        mUidsList.clear();
    }

    //done
    private void startCalling() {
        setupLocalVideo();
        joinChannel();
    }

    //done  but check use
    private String getUserName() {
        return this.userName;
    }

    //done
    public void onSwitchCameraClicked(View view) {
        mRtcEngine.switchCamera();
    }

    //done
    public void onLocalAudioMuteClicked(View view) {
        isMuted = !isMuted;
        mRtcEngine.muteLocalAudioStream(isMuted);
        int res = isMuted ? R.drawable.btn_mute : R.drawable.btn_unmute;
        mMuteBtn.setImageResource(res);

    }

    public void onEndCallClicked(View view){
        endCall();
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
        joinChannel();
    }

    private void endCall() {

        removeLocalVideo();
        //removeRemoteVideo();
        leaveChannel();

        startActivity(new Intent(MainActivity.this, JoinChannelActivity.class));
        finish();

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

    private void updateDatabase(int uid){

        activeChannelsRef.child(channelName).child("users").child(mCurrentUser.getUid()).setValue(uid);

        //activeChannelsRef.child(channelName).child("users-count").setValue()


        activeChannelsRef.child(channelName).runTransaction(new Transaction.Handler() {
            @NonNull
            @NotNull
            @Override
            public Transaction.Result doTransaction(@NonNull @NotNull MutableData currentData) {
                if (currentData.child("user-count").getValue() != null){
                    userCount = Integer.parseInt(currentData.child("user-count").getValue().toString()) + 1;
                }
                else {
                    userCount = 1;
                }

                currentData.child("user-count").setValue(userCount);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, boolean committed, @Nullable @org.jetbrains.annotations.Nullable DataSnapshot currentData) {
                Toast.makeText(MainActivity.this, "Transaction complete", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "user count txn compl: " +userCount, Toast.LENGTH_SHORT).show();

            }
        });

    }

    private int getUserCountFromDatabase(){

        activeChannelsRef.child(channelName).child("user-count").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                userCount = Integer.parseInt(snapshot.getValue().toString());
                Toast.makeText(MainActivity.this, "User count from function call : "+userCount, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        return userCount;
    }

    //implement later

//    public void onLockRoomClick(View view) {
//        if (isLocalCall) {
//            //when the user is in his own room
//            if (localState.equals(Constant.USER_STATE_LOCK)) {
//                //set the room to public
//                localState = Constant.USER_STATE_OPEN;
//                mRef.child(this.userName).setValue(new DBUser(this.userName, agoraUser.getAgoraUid(), localState, DBFriend));
//                showToast("Room set to public");
//            }else {
//                //set the room to private so that no one can join the room
//                localState = Constant.USER_STATE_LOCK;
//                mRef.child(this.userName).setValue(new DBUser(this.userName, agoraUser.getAgoraUid(), localState, DBFriend));
//                showToast("Room set to private");
//            }
//        }else {
//            //when user is joining other people's room
//            //leave that room and come back to user's own room
//            isLocalCall = true;
//            finishCalling();
//            channelName = userName;
//            startCalling();
//            localState = Constant.USER_STATE_OPEN;
//            //update user's room state
//            mRef.child(userName).setValue(new DBUser(userName, agoraUser.getAgoraUid(), localState, DBFriend));
//        }
//    }
}