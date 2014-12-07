package com.abdennebi.photogift.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.abdennebi.photogift.R;
import com.abdennebi.photogift.api.ApiCallbacks;
import com.abdennebi.photogift.api.ApiClient;
import com.abdennebi.photogift.application.PhotoGift;
import com.abdennebi.photogift.domain.Gift;
import com.abdennebi.photogift.utils.IOUtils;
import com.abdennebi.photogift.utils.IntentsUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import static com.abdennebi.photogift.utils.IntentsUtils.Extra.GIFT_CHAIN_ID;


public class CreateGiftActivity extends Activity {

    /**
     * Activity result code for image capture.
     */
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 6000;

    /**
     * Activity result code for image gallery select.
     */
    private static final int REQUEST_CODE_IMAGE_SELECT = 6001;

    private ApiClient apiClient;

    private EditText titleET;

    private EditText textET;

    private ImageView photoContainer;

    /**
     * The generated image file URI.
     */
    private Uri imageUri;

    /**
     * the path to image to upload
     */
    private String imagePath;

    /**
     * The Chain Id to which we wish add a gift  (can be null).
     */
    private Long gitChainId;

    private ProgressDialog progressDialog;

    private ImageView send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiClient = ((PhotoGift) getApplication()).getApiClient();

        Intent intent = getIntent();

        gitChainId = intent.getLongExtra(GIFT_CHAIN_ID, -1);

        setContentView(R.layout.gift_create);

        titleET = (EditText) findViewById(R.id.gift_create_value_title);
        textET = (EditText) findViewById(R.id.gift_create_value_text);
        photoContainer = (ImageView) findViewById(R.id.gift_create_gift_photo);
        send = (ImageView) findViewById(R.id.gift_create_button);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGift();
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        switch (requestCode) {
            case REQUEST_CODE_IMAGE_CAPTURE:
                if (responseCode == RESULT_OK) {

                    Picasso.with(this).load(imageUri).into(photoContainer);
                }
                break;
            case REQUEST_CODE_IMAGE_SELECT:
                if (responseCode == RESULT_OK && intent != null && intent.getData() != null) {

                    imageUri = intent.getData();
                    Picasso.with(this).load(imageUri).into(photoContainer);
                    if ("content".equals(imageUri.getScheme())) {
                        try {
                            imagePath = IOUtils.copyContent(imageUri, getContentResolver());
                        } catch (IOException e) {
                            Toast.makeText(CreateGiftActivity.this, "Unable de get the image file", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        imagePath = imageUri.getPath();
                    }

                }
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.gift_create_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.gift_create_menu_add_from_camera:
                addFromCamera();
                return true;
            case R.id.gift_create_menu_add_from_gallery:
                addFromGallery();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void update(Gift newGift) {

        Intent intent = new Intent();

        if (newGift != null) {
            intent.putExtra(IntentsUtils.Extra.CREATED_GIFT, newGift);
            setResult(RESULT_OK, intent);

        } else {
            setResult(RESULT_CANCELED, intent);
        }

        finish();
    }


    public void addFromCamera() {
        // http://developer.android.com/reference/android/provider/MediaStore.html
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = IntentsUtils.getOutputMediaFileUri(IntentsUtils.MEDIA_TYPE_IMAGE);
        imagePath = imageUri.getPath();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_CODE_IMAGE_CAPTURE);
    }

    public void addFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), REQUEST_CODE_IMAGE_SELECT);
    }

    public void createGift() {
        String title = titleET.getText().toString();
        String text = textET.getText().toString();
        sendImage(gitChainId, imagePath, title, text);
    }

    private void sendImage(Long gitChainId, String imageUri, String title, String text) {

        progressDialog = ProgressDialog.show(this, "Please wait ...", "Uploading Gift ...", true);

        Long id = gitChainId != -1 ? gitChainId : null;

        apiClient.createGift(id, imageUri, title, text, new ApiCallbacks.Callback<Gift>() {
            @Override
            public void onSuccess(Gift result) {
                Toast.makeText(CreateGiftActivity.this, getString(R.string.upload_success), Toast.LENGTH_LONG).show();
                update(result);
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Exception ex) {
                Toast.makeText(CreateGiftActivity.this, getString(R.string.upload_failure), Toast.LENGTH_LONG).show();
                update(null);
                progressDialog.dismiss();
            }
        });

    }
}
