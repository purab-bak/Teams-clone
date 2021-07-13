
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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
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
/**
    Main Activity to handle video conferencing callbacks
 **/
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

    private RelativeLayout mChatLinearLayout;
    boolean isShowingChat = false;
    MessageAdapter messageAdapter;
    RecyclerView chatRV;
    TextInputEditText messageEt;
    FloatingActionButton sendMessageButton;
    List<Message> messageList;
    DatabaseReference messageRef;

    //disabling video
    boolean isVideoEnabled = true;
    ImageView videoFreezeButton;

    //Notes
    private LinearLayout notesLayout;
    boolean isShowingNotes = false;
    TextInputEditText notesTextEt;
    ExtendedFloatingActionButton saveNotesButton;
    DatabaseReference notesRef;
    TextView notesTitleTV;

    RelativeLayout controlPanel;

    boolean isControlPanelVisible = false;

    RelativeLayout mainLayout;

    ImageView upButton;


    private final IRtcEngineEventHandler mRtcHandler = new IRtcEngineEventHandler() {

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    currentSessionInfo.setUserCount(1);
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
                         }
                });
            }

            if (state == Constants.REMOTE_VIDEO_STATE_REASON_LOCAL_MUTED) {
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

        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isControlPanelVisible = !isControlPanelVisible;
                controlPanel.setVisibility(isControlPanelVisible ? View.VISIBLE : View.INVISIBLE);

                int res = isControlPanelVisible ? R.drawable.ic_baseline_keyboard_arrow_down_24 : R.drawable.ic_baseline_keyboard_arrow_up_24;
                upButton.setImageResource(res);

            }
        });


    }


    private void initUi() {

        channelName = getIntent().getStringExtra("channelName");

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        controlPanel = findViewById(R.id.control_panel);

        activeChannelsRef = FirebaseDatabase.getInstance().getReference().child("active-channels");

        upButton = findViewById(R.id.upButton);
        currentSessionInfo = new SessionInfo(0, channelName);
        mainLayout = findViewById(R.id.mainLayout);

        videoFreezeButton = findViewById(R.id.video_off_button);
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
        notesTitleTV = findViewById(R.id.noteTitleTV);
        notesTitleTV.setText(channelName);
    }



    private void initEngineAndJoinChannel() {

        //init engine
        initializeEngine();

        //setup local video
        setupLocalVideo();

        //join channel
        joinChannel();

    }


    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("Need to check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

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

    //join a video call channel
    private void joinChannel() {
        String token = getString(R.string.agora_access_token);
        if (TextUtils.isEmpty(token)) {
            token = null;
        }

        mRtcEngine.joinChannel(token, channelName, "", 0);

    }


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

    //ask for permissions
    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    private void onRemoteUserLeft(int uid) {
        removeRemoteVideo(uid);
    }

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

    public void onSwitchCameraClicked(View view) {
        mRtcEngine.switchCamera();
    }

    public void onLocalAudioMuteClicked(View view) {
        isMuted = !isMuted;
        mRtcEngine.muteLocalAudioStream(isMuted);
        int res = isMuted ? R.drawable.ic_baseline_mic_off_24 : R.drawable.ic_baseline_mic_on;
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


    public void onVideoButtonClicked(View view) {

        mRtcEngine.enableLocalVideo(!isVideoEnabled);

        isVideoEnabled = !isVideoEnabled;

        int res = isVideoEnabled ? R.drawable.ic_baseline_videocam_24 : R.drawable.ic_baseline_videocam_off_24;
        videoFreezeButton.setImageResource(res);

    }

    public void onChatButtonCLicked(View view) {

        isShowingChat = !isShowingChat;
        mChatLinearLayout.setVisibility(isShowingChat ? View.VISIBLE : View.GONE);

        if (isShowingNotes) {
            isShowingNotes = false;
            notesLayout.setVisibility(View.GONE);
        }

    }

    public void onNotesButtonCLicked(View view) {

        isShowingNotes = !isShowingNotes;
        notesLayout.setVisibility(isShowingNotes ? View.VISIBLE : View.GONE);

        if (isShowingChat) {
            isShowingChat = false;
            mChatLinearLayout.setVisibility(View.GONE);
        }

    }

    private void getNotes() {

        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

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
        notesRef.setValue(note).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showToast("Note saved!");
            }
        });

    }

    private void showToast(String s) {
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    //get message from database
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
        Message message = new Message(messageText, mCurrentUser.getDisplayName(), String.valueOf(mCurrentUser.getPhotoUrl()), mCurrentUser.getUid(), epoch);
        messageEt.setText("");
        messageRef.push().setValue(message);

    }


    //sharing code
    public void onShareButtonClicked(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Meeting Code", "Use the code " + channelName + " to join the meeting.");
        showToast("Meeting code copied to clipboard!");
        clipboard.setPrimaryClip(clip);
    }
}