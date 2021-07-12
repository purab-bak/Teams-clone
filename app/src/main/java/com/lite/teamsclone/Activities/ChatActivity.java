package com.lite.teamsclone.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lite.teamsclone.Adapters.MessageAdapter;
import com.lite.teamsclone.Models.Message;
import com.lite.teamsclone.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    TextView channelNameTV;
    String channelName;

    MessageAdapter messageAdapter;
    RecyclerView chatRV;
    TextInputEditText messageEt;
    FloatingActionButton sendMessageButton;
    List<Message> messageList;
    DatabaseReference messageRef;

    FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initUI();

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

    private void showToast(String s) {
        Toast.makeText(ChatActivity.this, s, Toast.LENGTH_SHORT).show();
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
        chatRV.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
        messageAdapter = new MessageAdapter(ChatActivity.this, messageList);
        chatRV.setAdapter(messageAdapter);
    }

    private void sendMessage(String messageText) {
        long epoch = System.currentTimeMillis();
        Message message = new Message(messageText, mCurrentUser.getDisplayName(),String.valueOf(mCurrentUser.getPhotoUrl()),mCurrentUser.getUid(),epoch);
        messageEt.setText("");
        messageRef.push().setValue(message);

    }

    private void initUI() {
        channelNameTV = findViewById(R.id.channelNameTV);
        channelName = getIntent().getStringExtra("channelName");

        channelNameTV.append(channelName);

        messageEt = findViewById(R.id.chatMessageET);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        chatRV = findViewById(R.id.chatRV);
        messageList = new ArrayList<>();
        messageRef = FirebaseDatabase.getInstance().getReference().child("messages-activeChannels").child(channelName).child("messages");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void onBackClicked(View view) {
        super.onBackPressed();
    }
}