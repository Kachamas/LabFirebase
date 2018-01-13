package com.example.kacha.helloworld;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    EditText editTextUserName;
    Button btnSet;
    TextView tvShow;
    Button btnDelete;
    EditText editTextWritePost;
    Button btnPost;
    private static final String CHILD_USERS = "chat-users";
    private static final String CHILD_MESSAGES = "chat";
    private static final String USERID = "id-12345";
    private DatabaseReference mesRoot, mesUser, mesMessage;
    private String mUsername;
    private String mMessage;
    private ValueEventListener mValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        mesRoot = firebaseDatabase.getReference();
        mesUser = mesRoot.child(CHILD_USERS);
        mesMessage = mesRoot.child(CHILD_MESSAGES);

        btnSet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mUsername = editTextUserName.getText().toString();
                if (TextUtils.isEmpty(mUsername)) {
                    editTextUserName.setError("Required");
                } else {
                    mesUser.child(USERID).setValue(mUsername);
                    editTextUserName.setText(null);
                }
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mMessage = editTextWritePost.getText().toString();
                if (TextUtils.isEmpty(mMessage)) {
                    editTextWritePost.setError("Required");
                } else {
                    FriendlyMessage friendlyMessage = new FriendlyMessage(mMessage, mUsername);
                    mesMessage.push().setValue(friendlyMessage);
                    editTextWritePost.setText(null);
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mesMessage.removeValue();
            }
        });


    }

    private void init() {
        editTextUserName = findViewById(R.id.editTextUserName);
        btnSet = findViewById(R.id.btnSet);
        tvShow = findViewById(R.id.tvShow);
        btnDelete = findViewById(R.id.btnDelete);
        editTextWritePost = findViewById(R.id.editTextWritePost);
        btnPost = findViewById(R.id.btnPost);
    }

    //open app start key username..
    @Override
    protected void onStart() {
        super.onStart();
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsername = dataSnapshot.child(CHILD_USERS).child(USERID).getValue(String.class);
                tvShow.setText("Current Username: " + mUsername + "\n");

                Iterable<DataSnapshot> children = dataSnapshot.child(CHILD_MESSAGES).getChildren();
                while (children.iterator().hasNext()) {
                    String key = children.iterator().next().getKey();
                    FriendlyMessage friendlyMessage = dataSnapshot.child(CHILD_MESSAGES).child(key).getValue(FriendlyMessage.class);

                    long now = System.currentTimeMillis();
                    long past = now - (60 * 60 * 24 * 45 * 1000L);
                    String x = DateUtils.getRelativeTimeSpanString(past, now, DateUtils.MINUTE_IN_MILLIS).toString();

                    tvShow.append("username : " + friendlyMessage.getUsername() + " | ");
                    tvShow.append("text : " + friendlyMessage.getText() + " (" + x + ")" + "\n");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mesRoot.addValueEventListener(mValueEventListener);
    }

    //close app (delete all data)
    @Override
    protected void onStop() {
        super.onStop();
        if (mValueEventListener != null) {
            mesRoot.removeEventListener(mValueEventListener);
        }
    }
}
