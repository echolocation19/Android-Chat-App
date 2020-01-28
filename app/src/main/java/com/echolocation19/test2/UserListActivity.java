package com.echolocation19.test2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.echolocation19.test2.Model.User;
import com.echolocation19.test2.ui.UsersListAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

  private RecyclerView recyclerView;
  private List<User> users;

  private UsersListAdapter adapter;

  private FirebaseUser currentUser;

  private String currentUserName;

  private FirebaseAuth mAuth = FirebaseAuth.getInstance();
  private FirebaseFirestore db = FirebaseFirestore.getInstance();
  private CollectionReference usersRef = db.collection("users");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_list);

    users = new ArrayList<>();

    currentUser = mAuth.getCurrentUser();

    recyclerView = findViewById(R.id.recyclerView);
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
            DividerItemDecoration.VERTICAL));
  }

  @Override
  protected void onStart() {
    super.onStart();
    usersRef.get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
              @Override
              public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                  users.clear();
                  for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    if (!documentSnapshot.getString("userId").equals(currentUser.getUid())) {
                      User user = documentSnapshot.toObject(User.class);
                      user.setId(documentSnapshot.getString("userId"));
                      users.add(user);
                    } else {
                      currentUserName = documentSnapshot.getString("name");
                    }
                  }

                  adapter = new UsersListAdapter(users);
                  recyclerView.setAdapter(adapter);
                  adapter.notifyDataSetChanged();
                  adapter.setOnUserClickListener(new UsersListAdapter.OnUserClickListener() {
                    @Override
                    public void onUserClick(int position) {
                      goToChat(position);
                    }
                  });
                }
              }
            });
  }

  private void goToChat(int position) {
    Intent intent = new Intent(UserListActivity.this, ChatActivity.class);
    intent.putExtra("recipientId", users.get(position).getId());
    intent.putExtra("recipientUserName", users.get(position).getName());
    intent.putExtra("userName", currentUserName);
    startActivity(intent);
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
        startActivity(new Intent(UserListActivity.this, LoginActivity.class));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

}
