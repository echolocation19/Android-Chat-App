package com.echolocation19.test2.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.echolocation19.test2.Model.User;
import com.echolocation19.test2.R;

import java.util.List;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.ViewHolder> {

  private List<User> users;
  private OnUserClickListener listener;

  public UsersListAdapter(List<User> users) {
    this.users = users;
  }

  public interface OnUserClickListener {
    void onUserClick(int position);
  }

  public void setOnUserClickListener(OnUserClickListener listener) {
    this.listener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_row, null);
    return new ViewHolder(view, listener);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    User user = users.get(position);
    holder.name.setText(user.getName());
  }

  @Override
  public int getItemCount() {
    return users.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    private TextView name;

    public ViewHolder(@NonNull View itemView, final OnUserClickListener listener) {
      super(itemView);
      name = itemView.findViewById(R.id.usernameRow);
      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (listener != null) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
              listener.onUserClick(position);
            }
          }
        }
      });
    }
  }
}
