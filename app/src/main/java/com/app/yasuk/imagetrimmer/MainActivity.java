package com.app.yasuk.imagetrimmer;

import android.graphics.Point;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

import com.app.yasuk.imagetrimmer.fragment.StartFragment;

public class MainActivity extends AppCompatActivity {

    public static FragmentManager fragmentManager;
    private StartFragment startFragment;

    public static int screenWidth;
    public static int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get screen size
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display dsp = wm.getDefaultDisplay();
        Point size = new Point();
        dsp.getSize(size);

        screenWidth = size.x;
        screenHeight = size.y;

        startFragment = StartFragment.newInstance();

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.add(R.id.base_container, startFragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0 ){
            fragmentManager.popBackStack();
            return;
        } else {
            super.onBackPressed();
        }

        finish();
    }
}
