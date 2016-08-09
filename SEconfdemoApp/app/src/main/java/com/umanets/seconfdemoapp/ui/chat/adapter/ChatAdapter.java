package com.umanets.seconfdemoapp.ui.chat.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;
import com.umanets.seconfdemoapp.R;
import com.umanets.seconfdemoapp.Util;
import com.umanets.seconfdemoapp.model.MessageModel;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

/**
 * Created by Евгений on 03.04.2016.
 */
public class ChatAdapter extends FirebaseRecyclerAdapter<MessageModel, ChatAdapter.MyChatViewHolder> {

    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_MSG_IMG = 2;
    private static final int LEFT_MSG_IMG = 3;

    private ChatClickListener mChatClickListener;

    private String nameUser;


    public ChatAdapter(DatabaseReference ref, String nameUser, ChatClickListener mChatClickListener) {
        super(MessageModel.class, R.layout.item_message_left, ChatAdapter.MyChatViewHolder.class, ref);
        this.nameUser = nameUser;
        this.mChatClickListener = mChatClickListener;
    }

    @Override
    public MyChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == RIGHT_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false);
            return new MyChatViewHolder(view);
        } else if (viewType == LEFT_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);
            return new MyChatViewHolder(view);
        } else if (viewType == RIGHT_MSG_IMG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_img, parent, false);
            return new MyChatViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_img, parent, false);
            return new MyChatViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel model = getItem(position);
        if (model.getMapModel() != null) {
            if (model.getUserModel().getName().equals(nameUser)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }
        } else if (model.getUserModel().getName().equals(nameUser)) {
            return RIGHT_MSG;
        } else {
            return LEFT_MSG;
        }
    }

    @Override
    protected void populateViewHolder(MyChatViewHolder viewHolder, MessageModel model, int position) {
        viewHolder.setIvUser(model.getUserModel().getPhoto_profile());
        viewHolder.setTxtMessage(model.getMessage());
        viewHolder.setTvTimestamp(model.getTimeStamp());
        viewHolder.tvIsLocation(View.GONE);
        if (model.getFile() != null) {
            viewHolder.tvIsLocation(View.GONE);
            viewHolder.setIvChatPhoto(model.getFile().getUrl_file());
        } else if (model.getMapModel() != null) {
            viewHolder.setIvChatPhoto(Util.local(model.getMapModel().getLatitude(), model.getMapModel().getLongitude()));
            viewHolder.tvIsLocation(View.VISIBLE);
        }
    }

    public class MyChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTimestamp, tvLocation;
        EmojiconTextView txtMessage;
        ImageView ivUser, ivChatPhoto;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            tvTimestamp = (TextView) itemView.findViewById(R.id.timestamp);
            txtMessage = (EmojiconTextView) itemView.findViewById(R.id.txtMessage);
            tvLocation = (TextView) itemView.findViewById(R.id.tvLocation);
            ivChatPhoto = (ImageView) itemView.findViewById(R.id.img_chat);
            ivUser = (ImageView) itemView.findViewById(R.id.ivUserChat);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            MessageModel model = getItem(position);
            if (model.getMapModel() != null) {
                mChatClickListener.clickImageMapChat(view, position, model.getMapModel().getLatitude(), model.getMapModel().getLongitude());
            } else {
                mChatClickListener.clickImageChat(view, position, model.getUserModel().getName(), model.getUserModel().getPhoto_profile(), model.getFile().getUrl_file());
            }
        }

        public void setTxtMessage(String message) {
            if (txtMessage == null) return;
            txtMessage.setText(message);
        }

        public void setIvUser(String urlPhotoUser) {
            if (ivUser == null) return;
            Picasso.with(ivUser.getContext()).load(urlPhotoUser).centerCrop().resize(40,40).into(ivUser);
        }

        public void setTvTimestamp(String timestamp) {
            if (tvTimestamp == null) return;
            tvTimestamp.setText(converteTimestamp(timestamp));
        }

        public void setIvChatPhoto(String url) {
            if (ivChatPhoto == null) return;
            Picasso.with(ivChatPhoto.getContext()).load(url)
                    .resize(100, 100).centerCrop()
                    .into(ivChatPhoto);
            ivChatPhoto.setOnClickListener(this);
        }

        public void tvIsLocation(int visible) {
            if (tvLocation == null) return;
            tvLocation.setVisibility(visible);
        }

    }

    private CharSequence converteTimestamp(String mileSegundos) {
        return DateUtils.getRelativeTimeSpanString(Long.parseLong(mileSegundos), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }


}
