package com.shubham.photoeditor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CAMERA_REQUEST_CODE = 13254;
    private static final int PICK_IMAGE_CODE =648 ;
    private static final int EDITOR_CODE =135 ;
    private static final int CAMERA_PERMISSION = 564;
    private static final int STORAGE_PERMISSION = 897 ;
    private ImageView preview ;
    private Button capture;
    private Button upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview = findViewById(R.id.image_preview);
        capture = findViewById(R.id.btn_capture_image);
        upload = findViewById(R.id.btn_upload_img);

        capture.setOnClickListener(this);
        upload.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_capture_image:
                if (checkCameraPermission()){
                    startCamera();
                }else{
                    requestCameraPermissions();
                }
                break;
            case R.id.btn_upload_img:
                if (checkStoragePermission()){
                    pickImage();
                }else{
                    requestStoragePermission();
                }
                break;
        }
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION);
    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this,  new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }


    private void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION && grantResults.length>0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startCamera();
        }else if (requestCode == STORAGE_PERMISSION && grantResults.length>0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            pickImage();
        }
        else {
            Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode ==RESULT_OK){
            assert data != null;
            Uri uri = data.getData();
            if(requestCode == CAMERA_REQUEST_CODE){
//                Uri camerauri = data.getData();
                Bitmap bit = (Bitmap) data.getExtras().get("data");
                editor(getImageUri(MainActivity.this, bit));
            }else if ( requestCode == PICK_IMAGE_CODE) {
               editor(uri);
            }
            else if(requestCode == EDITOR_CODE){
                preview.setImageURI(uri);
                Toast.makeText(MainActivity.this, "Photo Saved.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void editor(Uri uri) {
        Intent ds = new Intent( MainActivity.this, DsPhotoEditorActivity.class);
        ds.setData(uri);
        ds.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "Images");
        ds.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE,
                new int[]{DsPhotoEditorActivity.TOOL_WARMTH,
                        DsPhotoEditorActivity.TOOL_STICKER,
                        DsPhotoEditorActivity.TOOL_TEXT,
                        DsPhotoEditorActivity.TOOL_VIGNETTE,
                        DsPhotoEditorActivity.TOOL_DRAW,
                        DsPhotoEditorActivity.TOOL_CONTRAST,
                        DsPhotoEditorActivity.TOOL_PIXELATE});
        startActivityForResult(ds, EDITOR_CODE);
    }

    private Uri getImageUri(Context context, Bitmap image){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100,bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),image, "img", null);
        return Uri.parse(path);
    }
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_CODE);
    }
}