package com.hmmrahul.ocrtranslator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.hmmrahul.ocrtranslator.databinding.ActivityMainBinding;

import io.ak1.OnBubbleClickListener;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;
    private static final int REQUEST_CAMERA_CODE = 100;


    Fragment activefragment;
    final Fragment ocrFragment = new ScanFragment();
    final Fragment manualTranslatorFragment = new ManualTranslatorFragment();
    FragmentManager fragmentManager = getSupportFragmentManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_CODE);
        }

        fragmentManager.beginTransaction().add(mainBinding.mainFrameLayout.getId(), ocrFragment).commit();
        activefragment = ocrFragment;
        fragmentManager.beginTransaction().add(mainBinding.mainFrameLayout.getId(), manualTranslatorFragment).hide(manualTranslatorFragment).commit();
        mainBinding.bubbleTabBar.setSelectedWithId(R.id.ocrTab, false);

        mainBinding.bubbleTabBar.addBubbleListener(new OnBubbleClickListener() {
            @Override
            public void onBubbleClick(int i) {
                switch (i) {
                    case R.id.ocrTab:
                        fragmentManager.beginTransaction().hide(activefragment).show(ocrFragment).commit();
                        activefragment = ocrFragment;
                        break;
                    case R.id.translatorTab:
                        fragmentManager.beginTransaction().hide(activefragment).show(manualTranslatorFragment).commit();
                        activefragment = manualTranslatorFragment;
                        break;
                }
            }
        });


    }


}