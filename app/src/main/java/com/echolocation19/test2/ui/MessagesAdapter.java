package com.echolocation19.test2.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.echolocation19.test2.Model.Message;
import com.echolocation19.test2.R;

import java.util.List;

public class MessagesAdapter extends ArrayAdapter<Message> {

  private List<Message> messages;
  private Activity activity;

  public MessagesAdapter(Activity context, int resource,
                               List<Message> messages) {
    super(context, resource, messages);

    this.messages = messages;
    this.activity = context;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    ViewHolder viewHolder;
    LayoutInflater layoutInflater =
            (LayoutInflater)activity.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

    Message message = getItem(position);
    int layoutResource = 0;
    int viewType = getItemViewType(position);

    if (viewType == 0) {
      layoutResource = R.layout.left_message;
    } else {
      layoutResource = R.layout.right_message;
    }

    if (convertView != null) {
      viewHolder = (ViewHolder) convertView.getTag();
    } else {
      convertView = layoutInflater.inflate(
              layoutResource, parent, false
      );
      viewHolder = new ViewHolder(convertView);
      convertView.setTag(viewHolder);
    }

    boolean isText = message.getImageUrl() == null;

    if (isText) {
      viewHolder.messageTextView.setVisibility(View.VISIBLE);
      viewHolder.photoImageView.setVisibility(View.GONE);
      viewHolder.messageTextView.setText(message.getMessage());
    } else {
      viewHolder.messageTextView.setVisibility(View.GONE);
      viewHolder.photoImageView.setVisibility(View.VISIBLE);
      Glide.with(viewHolder.photoImageView.getContext())
              .load(message.getImageUrl())
              .into(viewHolder.photoImageView);
    }

    return convertView;
  }

  @Override
  public int getItemViewType(int position) {

    int flag;
    Message message = messages.get(position);
    if (message.isMine()) {
      flag = 0;
    } else {
      flag = 1;
    }

    return flag;
  }

  @Override
  public int getViewTypeCount() {
    return 2;
  }

  private class ViewHolder {

    private ImageView photoImageView;
    private TextView messageTextView;

    public ViewHolder(View view) {
      photoImageView = view.findViewById(R.id.photoImageView);
      messageTextView = view.findViewById(R.id.messageTextView);
    }

  }
}
