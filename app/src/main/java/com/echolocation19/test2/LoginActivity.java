package com.echolocation19.test2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.echolocation19.test2.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

  private ProgressBar progressBar;
  private AutoCompleteTextView emailEditText;
  private EditText passwordEditText;
  private Button loginButton;
  private Button createAccountButton;

  private FirebaseAuth mAuth = FirebaseAuth.getInstance();
  private FirebaseFirestore db = FirebaseFirestore.getInstance();
  private CollectionReference usersRef = db.collection("users");
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    
    progressBar = findViewById(R.id.progressBarLogin);
    emailEditText = findViewById(R.id.emailLoginEditText);
    passwordEditText = findViewById(R.id.passwordLoginEditText);
    loginButton = findViewById(R.id.loginButton2);
    createAccountButton = findViewById(R.id.createAcctButton2);
    
    createAccountButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(LoginActivity.this, CreateAcctActivity.class));
      }
    });

    loginButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (!email.isEmpty() && !password.isEmpty()) {
          loginWithEmailAndPassword(email, password);
        } else {
          Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
        }
      }
    });
    
  }

  private void loginWithEmailAndPassword(String email, String password) {
    progressBar.setVisibility(View.VISIBLE);
    mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                  progressBar.setVisibility(View.GONE);
                  final User user = new User();
                  FirebaseUser currentUser = mAuth.getCurrentUser();

                  assert currentUser != null;

                  usersRef.whereEqualTo("userId", currentUser.getUid())
                          .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                              assert queryDocumentSnapshots != null;
                              if (!queryDocumentSnapshots.isEmpty()) {
                                progressBar.setVisibility(View.GONE);
                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                  user.setId(snapshot.getString("userId"));
                                  user.setName(snapshot.getString("name"));
                                  startActivity(new Intent(LoginActivity.this, UserListActivity.class));
                                }
                              }
                            }
                          });
                } else {
                  progressBar.setVisibility(View.GONE);
                  Toast.makeText(LoginActivity.this, "Can't sign in", Toast.LENGTH_SHORT).show();
                }
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
              }
            });
  }
}
