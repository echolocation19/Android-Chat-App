package com.echolocation19.test2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.echolocation19.test2.Model.Message;
import com.echolocation19.test2.ui.MessagesAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

  private static final int GET_IMAGE_CODE = 111;

  private FirebaseAuth mAuth = FirebaseAuth.getInstance();
  private FirebaseUser currentUser = mAuth.getCurrentUser();
  private FirebaseStorage storage = FirebaseStorage.getInstance();
  private StorageReference chatImagesRef = storage.getReference().child("chat_images");

  private ChildEventListener messagesChildEventListener;
  private FirebaseDatabase database;
  private DatabaseReference messagesDatabaseReference;

  private ImageButton sendImageButton;
  private Button sendMessageButton;
  private EditText messageEditText;

  private ListView messagesListView;

  private String username;
  private String recipientName;
  private String recipientId;

  private MessagesAdapter adapter;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);

    if (getIntent() != null) {
      username = getIntent().getStringExtra("userName");
      recipientName = getIntent().getStringExtra("recipientUserName");
      recipientId = getIntent().getStringExtra("recipientId");
      setTitle("Chat with " + recipientName);
    }

    database = FirebaseDatabase.getInstance();
    messagesDatabaseReference = database.getReference().child("messages");

    messagesListView = findViewById(R.id.messagesListView);
    List<Message> messages = new ArrayList<>();
    adapter = new MessagesAdapter(this, R.layout.message_item, messages);
    messagesListView.setAdapter(adapter);

    sendMessageButton = findViewById(R.id.sendMessageButton);

    messageEditText = findViewById(R.id.messageEditText);
    messageEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().trim().length() > 0) {
          sendMessageButton.setEnabled(true);
        } else {
          sendMessageButton.setEnabled(false);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    });

    messageEditText.setFilters(new InputFilter[]{
            new InputFilter.LengthFilter(500)
    });

    sendMessageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Message message = new Message();
        message.setMessage(messageEditText.getText().toString());
        message.setUsername(username);
        message.setSenderId(currentUser.getUid());
        message.setRecipientId(recipientId);
        message.setImageUrl(null);
        messagesDatabaseReference.push().setValue(message);
        messageEditText.setText("");
      }
    });

    sendImageButton = findViewById(R.id.sendImageButton);
    sendImageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Select an image"), GET_IMAGE_CODE);
      }
    });

    messagesChildEventListener = new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Message message = dataSnapshot.getValue(Message.class);

        if (message.getSenderId().equals(mAuth.getCurrentUser().getUid())
                && message.getRecipientId().equals(recipientId)) {
          message.setMine(true);
          adapter.add(message);
        } else if (message.getRecipientId().equals(mAuth.getCurrentUser().getUid())
                && message.getSenderId().equals(recipientId)) {
          message.setMine(false);
          adapter.add(message);
        }
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
    messagesDatabaseReference.addChildEventListener(messagesChildEventListener);
  }



  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == GET_IMAGE_CODE && resultCode == RESULT_OK) {
      Uri selectedImageUri;
      if (data != null) {
        selectedImageUri = data.getData();
        final StorageReference imageReference = chatImagesRef.child(selectedImageUri.getLastPathSegment());
        UploadTask uploadTask = imageReference.putFile(selectedImageUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
          @Override
          public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
            if (!task.isSuccessful()) {
              throw task.getException();
            }

            // Continue with the task to get the download URL
            return imageReference.getDownloadUrl();
          }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
          @Override
          public void onComplete(@NonNull Task<Uri> task) {
            if (task.isSuccessful()) {
              Uri downloadUri = task.getResult();
              Message message = new Message();
              message.setImageUrl(downloadUri.toString());
              message.setUsername(username);
              message.setSenderId(currentUser.getUid());
              message.setRecipientId(recipientId);
              messagesDatabaseReference.push().setValue(message);
            } else {
              Toast.makeText(ChatActivity.this, "Can't load image", Toast.LENGTH_SHORT).show();
            }
          }
        });
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.sign_out:
        mAuth.signOut();
        startActivity(new Intent(ChatActivity.this, LoginActivity.class));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}