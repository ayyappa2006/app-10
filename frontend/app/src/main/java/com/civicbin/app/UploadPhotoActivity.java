package com.civicbin.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;

public class UploadPhotoActivity extends AppCompatActivity {

    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Void> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);

        ImageView btnBack = findViewById(R.id.btnBack);
        LinearLayout btnGallery = findViewById(R.id.btnGallery);
        Button btnCamera = findViewById(R.id.btnCamera);

        btnBack.setOnClickListener(v -> finish());

        // Setup Gallery Launcher
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                navigateToReport(uri.toString());
            }
        });

        // Setup Camera Launcher (Preview Bitmap)
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
            if (bitmap != null) {
                try {
                    File file = new File(getCacheDir(), "captured_photo.jpg");
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    navigateToReport(Uri.fromFile(file).toString());
                } catch (Exception e) {
                    Toast.makeText(this, "Error saving photo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnCamera.setOnClickListener(v -> cameraLauncher.launch(null));
    }

    private void navigateToReport(String uriString) {
        Intent intent = new Intent(this, ReportPhotoActivity.class);
        intent.putExtra("photoUri", uriString);
        startActivity(intent);
    }
}
