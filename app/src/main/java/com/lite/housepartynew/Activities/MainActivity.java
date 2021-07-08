
package com.lite.housepartynew.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
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
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowInsets;
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
import com.lite.housepartynew.Adapters.FriendListRecyclerViewAdapter;
import com.lite.housepartynew.Adapters.MessageAdapter;
import com.lite.housepartynew.Adapters.ShowFriendListRecyclerViewAdapter;
import com.lite.housepartynew.Layouts.GridVideoViewContainer;
import com.lite.housepartynew.Layouts.RecyclerItemClickListener;
import com.lite.housepartynew.Models.AgoraUser;
import com.lite.housepartynew.Models.DBUser;
import com.lite.housepartynew.Models.Message;
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

    private ChildEventListener childEventListener;
    private ChildEventListener joinFriendChildEventListener;
    private boolean isShowingFriend = false;
    private boolean isAddingFriend = false;

    private LinearLayout mAddFriendLinearLayout;
    private LinearLayout mShowFriendLinearLayout;
    private EditText mSearchFriendEditText;

    private List<DBUser> searchFriendList = new ArrayList<>();
    private FriendListRecyclerViewAdapter mFriendListRecyclerViewAdapter;

    private RecyclerView mFriendListRecyclerView;
    private ShowFriendListRecyclerViewAdapter mShowFriendListRecyclerViewAdapter;
    private RecyclerView mShowFriendListRecyclerView;


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

        connectToFireDB(mCurrentUser.getUid());


        mAddFriendLinearLayout = findViewById(R.id.layout_add_friends);
        mShowFriendLinearLayout = findViewById(R.id.layout_show_friends);
        mSearchFriendEditText = findViewById(R.id.et_search_friends);
        mFriendListRecyclerView = findViewById(R.id.rv_friendList);
        mShowFriendListRecyclerView = findViewById(R.id.rv_show_friendList);

        //chat
        mChatLinearLayout = findViewById(R.id.layout_show_chat);
        messageEt = findViewById(R.id.chatMessageET);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        chatRV = findViewById(R.id.chat_recyclerView);
        messageList = new ArrayList<>();
        messageRef = FirebaseDatabase.getInstance().getReference().child("messages-activeChannels").child(channelName).child("messages");

    }

    private void connectToFireDB(final String firebaseUid) {
        mRef = database.getReference("Users");

        //listen to the friend list in the database
        mRef.push();
        mRef.child(firebaseUid).child("friend").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DBFriend = (List<String>) dataSnapshot.getValue();
                if (DBFriend == null) {
                    DBFriend = new ArrayList<>();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRef.child(firebaseUid).setValue(new DBUser(firebaseUid, agoraUser.getAgoraUid(), localState, DBFriend));
            }
        }, 1500);
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

//    //done  but check use
//    private String getUserName() {
//        //return this.userName;
//    }

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

        startActivity(new Intent(MainActivity.this, JoinChannelActivity.class));
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

    public void onLockRoomClick(View view) {
        if (isLocalCall) {
            //when the user is in his own room
            if (localState.equals(Constant.USER_STATE_LOCK)) {
                //set the room to public
                localState = Constant.USER_STATE_OPEN;
                mRef.child(mCurrentUser.getUid()).setValue(new DBUser(mCurrentUser.getUid(), agoraUser.getAgoraUid(), localState, DBFriend));
                Toast.makeText(MainActivity.this, "set to Public room", Toast.LENGTH_SHORT).show();
            } else {
                //set the room to private so that no one can join the room
                localState = Constant.USER_STATE_LOCK;
                mRef.child(mCurrentUser.getUid()).setValue(new DBUser(mCurrentUser.getUid(), agoraUser.getAgoraUid(), localState, DBFriend));
                Toast.makeText(MainActivity.this, " set to Private room", Toast.LENGTH_SHORT).show();

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
            mRef.child(mCurrentUser.getUid()).setValue(new DBUser(mCurrentUser.getUid(), agoraUser.getAgoraUid(), localState, DBFriend));
        }
    }

    public void onAddFriendClick(View view) {
        if (isShowingFriend) {
            isShowingFriend = !isShowingFriend;
            mShowFriendLinearLayout.setVisibility(isShowingFriend ? View.VISIBLE : View.GONE);
        }
        isAddingFriend = !isAddingFriend;
        mAddFriendLinearLayout.setVisibility(isAddingFriend ? View.VISIBLE : View.GONE);
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

    public void onSearchButtonClick(View view) {
        String searchFriendName = mSearchFriendEditText.getText().toString();
        if (searchFriendName == null || searchFriendName.equals("")) {
            Toast.makeText(MainActivity.this, "Name can not be empty!", Toast.LENGTH_SHORT).show();
        } else {
            searchFriends(searchFriendName);
        }
    }

    private void searchFriends(final String searchFriendName) {
        //search for a new friend in the database
        searchFriendList.clear();
        mFriendListRecyclerViewAdapter = new FriendListRecyclerViewAdapter(searchFriendList);
        mFriendListRecyclerViewAdapter.setOnItemClickListener(new FriendListRecyclerViewAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                addFriend(searchFriendList.get(position).getName());
                mSearchFriendEditText.setText("");
                searchFriendList.clear();
                mFriendListRecyclerView.setAdapter(mFriendListRecyclerViewAdapter);
            }
        });
        RecyclerView.LayoutManager manager = new GridLayoutManager(getBaseContext(), 1);
        mFriendListRecyclerView.setLayoutManager(manager);

        mFriendListRecyclerView.setAdapter(mFriendListRecyclerViewAdapter);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                DBUser result = dataSnapshot.getValue(DBUser.class);

                searchFriendList.add(result);
                mRef.orderByChild("name").startAt(searchFriendName).endAt(searchFriendName + "\uf8ff").removeEventListener(childEventListener);

                mFriendListRecyclerViewAdapter = new FriendListRecyclerViewAdapter(searchFriendList);
                mFriendListRecyclerViewAdapter.setOnItemClickListener(new FriendListRecyclerViewAdapter.ClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        addFriend(searchFriendList.get(position).getName());
                        mSearchFriendEditText.setText("");
                        searchFriendList.clear();
                        mFriendListRecyclerView.setAdapter(mFriendListRecyclerViewAdapter);
                    }
                });
                RecyclerView.LayoutManager manager = new GridLayoutManager(getBaseContext(), 1);
                mFriendListRecyclerView.setLayoutManager(manager);

                mFriendListRecyclerView.setAdapter(mFriendListRecyclerViewAdapter);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mRef.orderByChild("name").startAt(searchFriendName).endAt(searchFriendName + "\uf8ff").addChildEventListener(childEventListener);
    }

    public void addFriend(String userName) {
        DBFriend.add(userName);
        mRef.child(mCurrentUser.getUid()).setValue(new DBUser(mCurrentUser.getUid(), agoraUser.getAgoraUid(), localState, DBFriend));
    }

    public void onShowFriendListClick(View view) {
        if (isAddingFriend) {
            isAddingFriend = !isAddingFriend;
            mAddFriendLinearLayout.setVisibility(isAddingFriend ? View.VISIBLE : View.GONE);
        }

        isShowingFriend = !isShowingFriend;
        mShowFriendLinearLayout.setVisibility(isShowingFriend ? View.VISIBLE : View.GONE);

        mShowFriendListRecyclerViewAdapter = new ShowFriendListRecyclerViewAdapter(DBFriend);
        mShowFriendListRecyclerViewAdapter.setOnItemClickListener(new ShowFriendListRecyclerViewAdapter.ClickListener() {
            @Override
            public void onItemClick(final int position, View v) {

                if (v.getId() == R.id.btn_join_friend) {
                    joinFriendChildEventListener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            DBUser result = dataSnapshot.getValue(DBUser.class);
                            if (result.getState().equals(Constant.USER_STATE_OPEN)) {
                                joinFriend(DBFriend.get(position));
                                mShowFriendLinearLayout.setVisibility(View.GONE);
                            } else {
                                showToast(DBFriend.get(position) + "'s room is locked. You can message him to say hi!");
                            }

                            mRef.orderByChild("name").startAt(DBFriend.get(position)).endAt(DBFriend.get(position) + "\uf8ff").removeEventListener(joinFriendChildEventListener);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    };
                    mRef.orderByChild("name").startAt(DBFriend.get(position)).endAt(DBFriend.get(position) + "\uf8ff").addChildEventListener(joinFriendChildEventListener);

                } else if (v.getId() == R.id.btn_chat_friend) {
                    //startMessaging(DBFriend.get(position));
                }
            }
        });
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        mShowFriendListRecyclerView.setLayoutManager(manager);
        mShowFriendListRecyclerView.setAdapter(mShowFriendListRecyclerViewAdapter);
    }

    public void joinFriend(String friendName) {
        channelName = friendName;
        finishCalling();
        startCalling();
        //set the user's room state to private
        localState = Constant.USER_STATE_LOCK;
        mRef.child(mCurrentUser.getUid()).setValue(new DBUser(mCurrentUser.getUid(), agoraUser.getAgoraUid(), localState, DBFriend));
        isLocalCall = false;
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

        Message message = new Message(messageText, mCurrentUser.getDisplayName());
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