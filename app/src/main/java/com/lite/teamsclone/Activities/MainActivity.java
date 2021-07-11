
package com.lite.teamsclone.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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
import com.lite.teamsclone.Adapters.MessageAdapter;
import com.lite.teamsclone.Layouts.GridVideoViewContainer;
import com.lite.teamsclone.Models.AgoraUser;
import com.lite.teamsclone.Models.Message;
import com.lite.teamsclone.Models.Note;
import com.lite.teamsclone.Models.SessionInfo;
import com.lite.teamsclone.R;
import com.lite.teamsclone.utils.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_ID = 22;

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
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

    private boolean isMuted = false;
    private boolean mIsLandscape = false;

    private boolean isLocalCall = true;
    private String localState = Constant.USER_STATE_OPEN;

    ///database
    DatabaseReference mRef;
    private List<String> DBFriend = new ArrayList<>();
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    ////chat

    private LinearLayout mChatLinearLayout;
    boolean isShowingChat = false;
    MessageAdapter messageAdapter;
    RecyclerView chatRV;
    EditText messageEt;
    Button sendMessageButton;
    List<Message> messageList;
    DatabaseReference messageRef;

    //disabling video
    boolean isVideoEnabled = true;

    //Notes
    private LinearLayout notesLayout;
    boolean isShowingNotes = false;
    EditText notesTextEt;
    Button saveNotesButton;
    DatabaseReference notesRef;


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

                    //SurfaceView localView = mUidsList.remove(0);

                    showToast("Channel Joined");

                    agoraUser.setAgoraUid(uid);
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

            if (state == Constants.REMOTE_VIDEO_STATE_STARTING) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("tag", "RemoteVideo Starting, uid" + (uid));
                        currentSessionInfo.setUserCount(currentSessionInfo.getUserCount() + 1);

                        setupRemoteVideo(uid);
                        //userCount = getUserCountFromDatabase();
                        Toast.makeText(MainActivity.this, "User count local " + currentSessionInfo.getUserCount(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (state == Constants.REMOTE_VIDEO_STATE_REASON_LOCAL_MUTED){
                //change here
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //set fullscreen
        setContentView(R.layout.activity_main);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }

        initUi();

        showToast("On Create");

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            //init engine

            initEngineAndJoinChannel();

        }

        getMessages();
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!TextUtils.isEmpty(messageEt.getText().toString())) {
                    sendMessage(messageEt.getText().toString());
                } else {
                    showToast("Message cannot be empty");
                }
            }
        });

        getNotes();
        saveNotesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(notesTextEt.getText().toString())) {
                    saveNote(notesTextEt.getText().toString());
                } else {
                    showToast("Note cannot be empty");
                }
            }
        });

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

        //chat
        mChatLinearLayout = findViewById(R.id.layout_show_chat);
        messageEt = findViewById(R.id.chatMessageET);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        chatRV = findViewById(R.id.chat_recyclerView);
        messageList = new ArrayList<>();
        messageRef = FirebaseDatabase.getInstance().getReference().child("messages-activeChannels").child(channelName).child("messages");

        //notes
        notesLayout = findViewById(R.id.add_note_layout);
        notesTextEt = findViewById(R.id.notesET);
        saveNotesButton = findViewById(R.id.notesSaveBtn);
        notesRef = FirebaseDatabase.getInstance().getReference().child("user-notes").child(mCurrentUser.getUid()).child(channelName);

    }

    private void initEngineAndJoinChannel() {

        //init engine
        initializeEngine();

        //setup video config
        //setupVideoConfig();

        //setup local video
        setupLocalVideo();

        //join channel
        joinChannel();

    }


    //done
    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("Need to check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

//    private void setupVideoConfig(){
//        mRtcEngine.enableVideo();
//
//        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x360,
//                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
//                VideoEncoderConfiguration.STANDARD_BITRATE,
//                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
//                ));
//
//    }


    //done
    private void setupLocalVideo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRtcEngine.enableVideo();
                mRtcEngine.enableInEarMonitoring(true);
                mRtcEngine.setInEarMonitoringVolume(80);

                SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());

                mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));

                // used to place overlays on top of an underlying media surface view
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
        if (TextUtils.isEmpty(token)) {
            token = null;
        }

        mRtcEngine.joinChannel(token, channelName, "", 0);

    }

    //done
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQ_ID: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
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
    private void setupRemoteVideo(int uid) {
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

        mGridVideoViewContainer.initViewContainer(MainActivity.this, agoraUser.getAgoraUid(), mUidsList, mIsLandscape);

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
        if (!mCallEnd) {
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

    public void onEndCallClicked(View view) {
        endCall();
    }

    public void onCallClicked(View view) {

        if (mCallEnd) {
            startCall();
            mCallEnd = false;
            mCallBtn.setImageResource(R.drawable.btn_endcall);
        } else {
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

        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        finish();

    }

    private void removeLocalVideo() {
        if (mLocalView != null) {
            mLocalContainer.removeView(mLocalView);
        }

        mLocalView = null;
    }


    private void showButtons(boolean show) {

        int visibility = show ? View.VISIBLE : View.GONE;

        mMuteBtn.setVisibility(visibility);
        mSwitchCameraBtn.setVisibility(visibility);

    }

    //check
    public void onLockRoomClick(View view) {
        if (isLocalCall) {
            //when the user is in his own room
            if (localState.equals(Constant.USER_STATE_LOCK)) {
                //set the room to public
                localState = Constant.USER_STATE_OPEN;
            } else {
                //set the room to private so that no one can join the room
                localState = Constant.USER_STATE_LOCK;
            }
        } else {
            //when user is joining other people's room
            //leave that room and come back to user's own room
            isLocalCall = true;
            finishCalling();
            channelName = channelName;
            startCalling();
            localState = Constant.USER_STATE_OPEN;
            //update user's room state
        }
    }


    /**
     * This method is pausing video. Checkkk!!!!
     **/
    public void onVideoButtonClicked(View view) {

        //mRtcEngine.muteLocalVideoStream(isVideoEnabled);
        mRtcEngine.enableLocalVideo(!isVideoEnabled);

        isVideoEnabled = !isVideoEnabled;

    }

    public void onChatButtonCLicked(View view) {

        isShowingChat = !isShowingChat;
        mChatLinearLayout.setVisibility(isShowingChat ? View.VISIBLE : View.GONE);

    }

    public void onNotesButtonCLicked(View view) {

        isShowingNotes = ! isShowingNotes;
        notesLayout.setVisibility(isShowingNotes? View.VISIBLE : View.GONE);

    }

    private void getNotes(){

        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                if (snapshot.exists()){

                    Note savedNote = snapshot.getValue(Note.class);
                    notesTextEt.setText(savedNote.getBody());

                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    private void saveNote(String noteBody) {

        Note note = new Note(channelName, noteBody, channelName, System.currentTimeMillis());

        notesRef.setValue(note);

    }


    private void showToast(String s) {
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    private void getMessages() {

        messageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                message.setKey(snapshot.getKey());
                messageList.add(message);
                displayMessages(messageList);

                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                message.setKey(snapshot.getKey());

                List<Message> newMessages = new ArrayList<>();
                for (Message m : messageList) {

                    if (m.getKey().equals(message.getKey())) {
                        newMessages.add(message);
                    } else {
                        newMessages.add(m);
                    }
                }
                messageList = newMessages;
                displayMessages(messageList);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }

    private void displayMessages(List<Message> messageList) {
        chatRV.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        messageAdapter = new MessageAdapter(MainActivity.this, messageList);
        chatRV.setAdapter(messageAdapter);
    }

    private void sendMessage(String messageText) {

        long epoch = System.currentTimeMillis();

        Message message = new Message(messageText, mCurrentUser.getDisplayName(), epoch);
        messageEt.setText("");
        messageRef.push().setValue(message);

    }


    private void updateDatabase(int uid) {

        activeChannelsRef.child(channelName).child("users").child(mCurrentUser.getUid()).setValue(uid);

        //activeChannelsRef.child(channelName).child("users-count").setValue()


        activeChannelsRef.child(channelName).runTransaction(new Transaction.Handler() {
            @NonNull
            @NotNull
            @Override
            public Transaction.Result doTransaction(@NonNull @NotNull MutableData currentData) {
                if (currentData.child("user-count").getValue() != null) {
                    userCount = Integer.parseInt(currentData.child("user-count").getValue().toString()) + 1;
                } else {
                    userCount = 1;
                }

                currentData.child("user-count").setValue(userCount);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, boolean committed, @Nullable @org.jetbrains.annotations.Nullable DataSnapshot currentData) {
                Toast.makeText(MainActivity.this, "Transaction complete", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "user count txn compl: " + userCount, Toast.LENGTH_SHORT).show();

            }
        });

    }

    private int getUserCountFromDatabase() {

        activeChannelsRef.child(channelName).child("user-count").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                userCount = Integer.parseInt(snapshot.getValue().toString());
                Toast.makeText(MainActivity.this, "User count from function call : " + userCount, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        return userCount;
    }


}