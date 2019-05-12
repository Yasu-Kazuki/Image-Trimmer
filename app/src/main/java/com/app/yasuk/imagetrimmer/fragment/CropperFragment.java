package com.app.yasuk.imagetrimmer.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.app.yasuk.imagetrimmer.MainActivity;
import com.app.yasuk.imagetrimmer.R;
import com.app.yasuk.imagetrimmer.TrimView;

import java.io.FileDescriptor;
import java.io.IOException;

public class CropperFragment extends Fragment {

    private FrameLayout cropContainer;
    private ImageView croppingView;

    private Button cropButton;

    private TrimView trimView = null;

    private Bitmap originalBmp = null;
    private Bitmap resizedBmp = null;

    private int resizeFraction;
    public static final int RESIZE_DENOMINATOR = 100;

    private static final String BUNDLE_KEY_FILE_PATH = "file_path";

    public CropperFragment() {
        // Required empty public constructor
    }

    public static CropperFragment newInstance(String filePath) {
        CropperFragment fragment = new CropperFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_KEY_FILE_PATH, filePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cropper, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        resizeFraction = 100;

        // view
        cropContainer = view.findViewById(R.id.cropper_container);
        croppingView = view.findViewById(R.id.cropper_imageView);

        trimView = null;

        cropButton = view.findViewById(R.id.crop_button);
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap croppedBmp = Bitmap.createBitmap(resizedBmp, trimView.getStartX(), trimView.getStartY(),
                        trimView.getRectWidth(), trimView.getRectHeight());

                FragmentTransaction transaction = MainActivity.fragmentManager.beginTransaction();
                transaction.replace(R.id.base_container, CropResultFragment.newInstance(croppedBmp, resizeFraction));
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        try {
            originalBmp = getBitmapFromUri(Uri.parse(getArguments().getString(BUNDLE_KEY_FILE_PATH)));

            double resizeWidth = originalBmp.getWidth();
            double resizeHeight = originalBmp.getHeight();
            double tmpResizeWidth;
            double tmpResizeHeight;

            // If Bitmap is small, expand the size on screen
            if(resizeWidth < MainActivity.screenWidth && resizeHeight < MainActivity.screenHeight){
                while(true){
                    resizeFraction = resizeFraction+1;

                    tmpResizeWidth = resizeWidth * resizeFraction / RESIZE_DENOMINATOR;
                    tmpResizeHeight = resizeHeight * resizeFraction / RESIZE_DENOMINATOR;

                    if(tmpResizeWidth > MainActivity.screenWidth || tmpResizeHeight > MainActivity.screenHeight){
                        break;
                    } else{
                        resizeWidth = tmpResizeWidth;
                        resizeHeight = tmpResizeHeight;
                    }
                }
            }else{
                // If Bitmap is larger than screen, shrink the size on screen
                while(resizeFraction > 0){
                    resizeFraction = resizeFraction-1;

                    tmpResizeWidth = resizeWidth * resizeFraction / RESIZE_DENOMINATOR;
                    tmpResizeHeight = resizeHeight * resizeFraction / RESIZE_DENOMINATOR;

                    if(resizeWidth > MainActivity.screenWidth || resizeHeight > MainActivity.screenHeight){
                        resizeWidth = tmpResizeWidth;
                        resizeHeight = tmpResizeHeight;
                    } else {
                        break;
                    }
                }
            }
            resizedBmp = Bitmap.createScaledBitmap(originalBmp, (int)resizeWidth, (int)resizeHeight, false);
            croppingView.setImageBitmap(resizedBmp);

            ViewTreeObserver vto = croppingView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if(trimView == null){
                        trimView = new TrimView(getActivity());
                        trimView.setSize((int)croppingView.getX(), (int)croppingView.getY(), croppingView.getWidth(), croppingView.getHeight());
                        cropContainer.addView(trimView);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getActivity().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

}
