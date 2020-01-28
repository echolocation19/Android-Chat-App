package com.echolocation19.test2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.echolocation19.test2.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAcctActivity extends AppCompatActivity implements View.OnClickListener {

  private EditText nameEditText;
  private AutoCompleteTextView emailEditText;
  private EditText passwordEditText;
  private EditText confirmPasswordEditText;
  private ProgressBar progressBar;
  private Button loginButton;
  private Button createAcctButton;
  private Button showPasswordButton;
  private Button showConfirmButton;

  private boolean isShownPass;
  private boolean isShownConfPass;

  private FirebaseAuth mAuth = FirebaseAuth.getInstance();
  private FirebaseFirestore db = FirebaseFirestore.getInstance();
  private CollectionReference userRef = db.collection("users");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_acct);

    progressBar = findViewById(R.id.progressBarCreateAcct);
    nameEditText = findViewById(R.id.nameCreateAcct);
    emailEditText = findViewById(R.id.emailCreateAcct);
    passwordEditText = findViewById(R.id.passwordCreateAcct);
    confirmPasswordEditText = findViewById(R.id.confirmPasswordCreateAcct);

    showPasswordButton = findViewById(R.id.showPasswordButton);
    showConfirmButton = findViewById(R.id.showConfirmPasswordButton);

    showPasswordButton.setOnClickListener(this);
    showConfirmButton.setOnClickListener(this);

    loginButton = findViewById(R.id.loginButton1);
    loginButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(CreateAcctActivity.this, LoginActivity.class));
      }
    });

    createAcctButton = findViewById(R.id.createAcctButton1);
    createAcctButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        checkUsersData();
      }
    });
  }

  private void checkUsersData() {
    String name = nameEditText.getText().toString().trim();
    String email = emailEditText.getText().toString().trim();
    String password = passwordEditText.getText().toString().trim();
    String confirmPassword = confirmPasswordEditText.getText().toString().trim();

    if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()) {
      if (password.equals(confirmPassword)) {
        if (!(password.length() < 7)) {
          createNewUser(name, email, password);
        } else {
          Toast.makeText(CreateAcctActivity.this, "Password must be at least 7 symbols", Toast.LENGTH_SHORT).show();
        }
      } else {
        Toast.makeText(CreateAcctActivity.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
      }
    } else {
      Toast.makeText(CreateAcctActivity.this, "Empty fields not allowed", Toast.LENGTH_SHORT).show();
    }
  }

  private void createNewUser(final String name, String email, String password) {
    progressBar.setVisibility(View.VISIBLE);
    mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                  FirebaseUser user = mAuth.getCurrentUser();
                  assert user != null;
                  final String currentUserId = user.getUid();
                  Map<String, String> userObj = new HashMap<>();
                  userObj.put("userId", currentUserId);
                  userObj.put("name", name);
                  userRef.add(userObj)
                          .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                              documentReference.get()
                                      .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                          if (task.getResult().exists()) {
                                            progressBar.setVisibility(View.GONE);
                                            User user = new User();
                                            user.setId(currentUserId);
                                            user.setName(name);
                                            Intent intent = new Intent(CreateAcctActivity.this, UserListActivity.class);
                                            intent.putExtra("userId", currentUserId);
                                            intent.putExtra("name", name);
                                            startActivity(intent);
                                          } else {
                                            progressBar.setVisibility(View.GONE);
                                          }
                                        }
                                      });
                            }
                          })
                          .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                              progressBar.setVisibility(View.GONE);
                              Toast.makeText(CreateAcctActivity.this, "Can't save user", Toast.LENGTH_SHORT).show();
                            }
                          });
                } else {
                  progressBar.setVisibility(View.GONE);
                  Toast.makeText(CreateAcctActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
              }
            })
            .addOnFailureListener(this, new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CreateAcctActivity.this, "Oops, something went wrong", Toast.LENGTH_SHORT).show();
              }
            });

  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.showPasswordButton:
        showHidePassword();
        break;
      case R.id.showConfirmPasswordButton:
        showHideConfirmPassword();
        break;
    }
  }

  private void showHidePassword() {
    if (isShownPass) {
      passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
      showPasswordButton.setBackground(ContextCompat.getDrawable(CreateAcctActivity.this, R.drawable.ic_visibility_white_24dp));
      isShownPass = false;
    } else {
      passwordEditText.setTransformationMethod(null);
      showPasswordButton.setBackground(ContextCompat.getDrawable(CreateAcctActivity.this, R.drawable.ic_visibility_off_white_24dp));
      isShownPass = true;
    }
  }

  private void showHideConfirmPassword() {
    if (isShownConfPass) {
      confirmPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
      showConfirmButton.setBackground(ContextCompat.getDrawable(CreateAcctActivity.this, R.drawable.ic_visibility_white_24dp));
      isShownConfPass = false;
    } else {
      confirmPasswordEditText.setTransformationMethod(null);
      showConfirmButton.setBackground(ContextCompat.getDrawable(CreateAcctActivity.this, R.drawable.ic_visibility_off_white_24dp));
      isShownConfPass = true;
    }
  }
}
