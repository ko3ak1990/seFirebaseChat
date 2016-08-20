package com.umanets.seconfdemoapp.ui.chat.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * Created by Евгений on 03.04.2016.
 */
public class ChatAdapter extends FirebaseRecyclerAdapter<MessageModel, ChatAdapter.MyChatViewHolder> {

    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private ChatClickListener mChatClickListener;

    private String nameUser;


    public ChatAdapter(DatabaseReference ref, String nameUser, ChatClickListener mChatClickListener) {
        super(MessageModel.class, R.layout.item_mine_msg, ChatAdapter.MyChatViewHolder.class, ref);
        this.nameUser = nameUser;
        this.mChatClickListener = mChatClickListener;
    }

    @Override
    public MyChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == RIGHT_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mine_msg, parent, false);
            return new MyChatViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_other_msg, parent, false);
            return new MyChatViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel model = getItem(position);
        if (model != null) {
            if (model.getUserModel().getName().equals(nameUser)) {
                return RIGHT_MSG;
            } else {
                return LEFT_MSG;
            }
        }
        return LEFT_MSG;
    }

    @Override
    protected void populateViewHolder(MyChatViewHolder viewHolder, MessageModel model, int position) {
        viewHolder.setUserImgView(model.getUserModel().getPhoto_profile());
        viewHolder.setMessageTexView(model.getMessage());
        viewHolder.setTimestampTexView(model.getTimeStamp());
        viewHolder.setTitleTextView(model.getUserModel().getName());
        viewHolder.setLocationImgVisibility(View.GONE);
        viewHolder.setChatImgVisibility(View.GONE);
        if (model.getFile() != null) {
            viewHolder.setLocationImgVisibility(View.GONE);
            viewHolder.setChatMsgImgView(model.getFile().getUrl_file());
        } else if (model.getMapModel() != null) {
            viewHolder.setChatMsgImgView(Util.local(model.getMapModel().getLatitude(), model.getMapModel().getLongitude()));
            viewHolder.setLocationImgVisibility(View.VISIBLE);
        }
    }

    public class MyChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTimestampTexView, mLocationTexView;
        TextView mMessageTexView;
        TextView mTitleTextView;
        ImageView mUserImgView, mChatMsgImgView;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            mTimestampTexView = (TextView) itemView.findViewById(R.id.timestamp);
            mTitleTextView = (TextView) itemView.findViewById(R.id.title_tv);
            mMessageTexView = (TextView) itemView.findViewById(R.id.chat_msg_text_tv);
            mChatMsgImgView = (ImageView) itemView.findViewById(R.id.img_chat);
            mUserImgView = (ImageView) itemView.findViewById(R.id.image_ava);
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

        public void setTitleTextView(String message) {
            if (mTitleTextView == null) return;
            mTitleTextView.setText(message);
        }


        public void setMessageTexView(String message) {
            if (mMessageTexView == null) return;
            mMessageTexView.setText(message);
        }

        public void setUserImgView(String urlPhotoUser) {
            if (mUserImgView == null) return;
            Picasso.with(mUserImgView.getContext()).load(urlPhotoUser).centerCrop().resize(40, 40)
                    .transform(new CropCircleTransformation()).into(mUserImgView);
        }

        public void setTimestampTexView(String timestamp) {
            if (mTimestampTexView == null) return;
            mTimestampTexView.setText(converteTimestamp(timestamp));
        }

        public void setChatMsgImgView(String url) {
            if (mChatMsgImgView == null) return;
            if (!TextUtils.isEmpty(url)) {
                Picasso.with(mChatMsgImgView.getContext()).load(url)
                        .resize(100, 100).centerCrop()
                        .into(mChatMsgImgView);
                mChatMsgImgView.setOnClickListener(this);
                mChatMsgImgView.setVisibility(View.VISIBLE);
            } else {
                mChatMsgImgView.setVisibility(View.GONE);
            }
        }

        public void setLocationImgVisibility(int visible) {
            if (mLocationTexView == null) return;
            mLocationTexView.setVisibility(visible);
        }

        public void setChatImgVisibility(int visible) {
            if (mChatMsgImgView == null) return;
            mChatMsgImgView.setVisibility(visible);
        }

    }

    private CharSequence converteTimestamp(String mileSegundos) {
        CharSequence charSequence = DateUtils.getRelativeTimeSpanString(Long.parseLong(mileSegundos),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        return charSequence.equals("0")?"1":charSequence;
    }


}
