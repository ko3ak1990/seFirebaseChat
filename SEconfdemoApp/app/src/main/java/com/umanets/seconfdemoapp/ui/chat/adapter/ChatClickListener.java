package com.umanets.seconfdemoapp.ui.chat.adapter;

import android.view.View;

/**
 * Created by ko3ak_zhn on 8/9/16.
 */
public interface ChatClickListener {
    void clickImageChat(View view, int position, String nameUser, String urlPhotoUser, String urlPhotoClick);


    void clickImageMapChat(View view, int position,String latitude,String longitude);
}
