package com.umanets.seconfdemoapp.ui.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.umanets.seconfdemoapp.R;
import com.umanets.seconfdemoapp.Util;
import com.umanets.seconfdemoapp.model.FileModel;
import com.umanets.seconfdemoapp.model.MapModel;
import com.umanets.seconfdemoapp.model.MessageModel;
import com.umanets.seconfdemoapp.model.UserModel;
import com.umanets.seconfdemoapp.ui.chat.adapter.ChatAdapter;
import com.umanets.seconfdemoapp.ui.chat.adapter.ChatClickListener;
import com.umanets.seconfdemoapp.ui.login.LoginActivity;

import java.io.File;
import java.util.Calendar;
import java.util.Date;



public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, ChatClickListener {

    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;
    private static final int PLACE_PICKER_REQUEST = 3;

    static final String TAG = MainActivity.class.getSimpleName();
    //DB name
    static final String CHAT_REFERENCE = "messageModel";
    public static final String AVA_URL_DEFAULT = "https://cdn4.iconfinder.com/data/icons/gray-user-management/512/rounded-512.png";

    //Firebase and GoogleApiClient
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mFirebaseDatabaseReference;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    //CLass Model
    private UserModel userModel;

    //Views UI
    private RecyclerView rvListMessage;
    private LinearLayoutManager mLinearLayoutManager;
    private View btSendMessage;
    private EditText edMessage;
    private View contentRoot;


    //File
    private File filePathImageCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!Util.checkConnection(this)) {
            Util.initToast(this, getString(R.string.check_connection));
            finish();
        } else {
            bindViews();
            verifyUserLoggined();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API)
                    .build();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE).child(Util.FOLDER_STORAGE_IMG);

        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    sendFileFirebase(storageRef, selectedImageUri);
                }
            }
        } else if (requestCode == IMAGE_CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (filePathImageCamera != null && filePathImageCamera.exists()) {
                    StorageReference imageCameraRef = storageRef.child(filePathImageCamera.getName() + "_camera");
                    sendFileFirebase(imageCameraRef, filePathImageCamera);
                }
            }
        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                if (place != null) {
                    LatLng latLng = place.getLatLng();
                    MapModel mapModel = new MapModel(latLng.latitude + "", latLng.longitude + "");
                    MessageModel messageModel = new MessageModel(userModel, Calendar.getInstance().getTime().getTime() + "", mapModel);
                    mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(messageModel);
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sendPhoto:
                photoCameraIntent();
                break;
            case R.id.sendPhotoGallery:
                photoGalleryIntent();
                break;
            case R.id.sendLocation:
                locationPlacesIntent();
                break;
            case R.id.sign_out:
                signOut();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Util.initToast(this, "Google Play Services error.");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonMessage:
                sendMessageFirebase();
                break;
        }
    }

    @Override
    public void clickImageChat(View view, int position, String nameUser, String urlPhotoUser, String urlPhotoClick) {
//        Intent intent = new Intent(this,FullScreenImageActivity.class);
//        intent.putExtra("nameUser",nameUser);
//        intent.putExtra("urlPhotoUser",urlPhotoUser);
//        intent.putExtra("urlPhotoClick",urlPhotoClick);
//        startActivity(intent);
    }

    @Override
    public void clickImageMapChat(View view, int position, String latitude, String longitude) {
        String uri = String.format("geo:%s,%s?z=17&q=%s,%s", latitude, longitude, latitude, longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }


    /**
     * Launch Upload Task via URI
     */
    private void sendFileFirebase(StorageReference storageReference, final Uri file) {
        if (storageReference != null) {
            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
            StorageReference imageGalleryRef = storageReference.child(name + "_gallery");
            UploadTask uploadTask = imageGalleryRef.putFile(file);
            uploadTask.addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
                }
            }).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "onSuccess sendFileFirebase");
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    if(downloadUrl!=null) {
                        FileModel fileModel = new FileModel("img", downloadUrl.toString(), name, "");
                        MessageModel messageModel = new MessageModel(userModel, "", Calendar.getInstance().getTime().getTime() + "", fileModel);
                        mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(messageModel);
                    }
                }
            });
        } else {
            //OOPS URI IS NULL
        }

    }

    /**
     * Launch Upload Task via File
     */
    private void sendFileFirebase(StorageReference storageReference, final File file) {
        if (storageReference != null) {
            UploadTask uploadTask = storageReference.putFile(Uri.fromFile(file));
            uploadTask.addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
                }
            }).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "onSuccess sendFileFirebase");
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    if (downloadUrl != null) {
                        FileModel fileModel = new FileModel("img", downloadUrl.toString(), file.getName(), file.length() + "");
                        MessageModel chatModel = new MessageModel(userModel, "", Calendar.getInstance().getTime().getTime() + "", fileModel);
                        mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                    }
                }
            });
        } else {
            //IS NULL
        }

    }

    /**
     * launch Place Picker
     */
    private void locationPlacesIntent() {
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Launch Counter
     */
    private void photoCameraIntent() {
        String nomeFoto = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
        filePathImageCamera = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), nomeFoto + "camera.jpg");
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(filePathImageCamera));
        startActivityForResult(it, IMAGE_CAMERA_REQUEST);
    }

    /**
     * Launch Gallery
     */
    private void photoGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_title)), IMAGE_GALLERY_REQUEST);
    }

    /**
     * Enviar msg de texto simples para chat
     */
    private void sendMessageFirebase() {
        MessageModel model = new MessageModel(userModel, edMessage.getText().toString(), Calendar.getInstance().getTime().getTime() + "", null);
        mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(model);
        edMessage.setText(null);
        Util.hideKeyboard(this);
    }

    /**
     * Read collections chatmodel Firebase
     */
    private void readMessagensFirebase() {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://setestapp-7b495.firebaseio.com/");
        final ChatAdapter firebaseAdapter = new ChatAdapter(mFirebaseDatabaseReference.child(CHAT_REFERENCE), userModel.getName(), this);
        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    rvListMessage.scrollToPosition(positionStart);
                }
            }
        });
        rvListMessage.setLayoutManager(mLinearLayoutManager);
        rvListMessage.setAdapter(firebaseAdapter);
    }

    /**
     * Verify User Loggined
     */
    private void verifyUserLoggined() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            String avaUrl = AVA_URL_DEFAULT;
            if (mFirebaseUser.getPhotoUrl() != null) {
                avaUrl = mFirebaseUser.getPhotoUrl().toString();
            }
            userModel = new UserModel(mFirebaseUser.getUid(),mFirebaseUser.getDisplayName(), avaUrl);
            readMessagensFirebase();
        }
    }

    /**
     * Use Butterknife
     */
    private void bindViews() {
        contentRoot = findViewById(R.id.contentRoot);
        edMessage = (EditText) findViewById(R.id.mesageEditText);
        btSendMessage = findViewById(R.id.buttonMessage);
        btSendMessage.setOnClickListener(this);
        rvListMessage = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
    }

    /**
     * Sign Out
     */
    private void signOut() {
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}