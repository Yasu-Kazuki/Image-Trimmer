package com.app.yasuk.imagetrimmer.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.yasuk.imagetrimmer.R;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CropResultFragment extends Fragment {

    private ImageView resultView;
    private Bitmap resizedResultBmp = null;

    private Button resultButton;

    private static final String BUNDLE_KEY_BMP_DATA = "bmp_data";
    private static final String BUNDLE_KEY_RATIO_DATA = "ratio";

    private static final int REQUEST_SAVE_IMAGE = 200;

    public CropResultFragment() {
        // Required empty public constructor
    }

    public static CropResultFragment newInstance(Bitmap bmp, int ratio) {
        CropResultFragment fragment = new CropResultFragment();
        Bundle args = new Bundle();
        args.putParcelable(BUNDLE_KEY_BMP_DATA, bmp);
        args.putInt(BUNDLE_KEY_RATIO_DATA, ratio);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Bitmap resultBitmap = getArguments().getParcelable(BUNDLE_KEY_BMP_DATA);

        // view
        resultView = view.findViewById(R.id.result_imageView);
        resultView.setImageBitmap(resultBitmap);

        resultButton = view.findViewById(R.id.save_button);
        resultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // initial file name
                Date date = new Date();
                SimpleDateFormat initialFileName = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String saveFileName = initialFileName.format(date);

                // Resize result image to original scale
                int ratio = getArguments().getInt(BUNDLE_KEY_RATIO_DATA);
                double resizeRatio = (double)CropperFragment.RESIZE_DENOMINATOR / ratio;
                double resizedResultWidth = resultBitmap.getWidth() * resizeRatio;
                double resizedResultHeight = resultBitmap.getHeight() * resizeRatio;
                resizedResultBmp = Bitmap.createScaledBitmap(resultBitmap, (int)resizedResultWidth, (int)resizedResultHeight, false);

                // save file
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_TITLE, saveFileName + ".jpg");

                startActivityForResult(intent, REQUEST_SAVE_IMAGE);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_crop_result, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_SAVE_IMAGE && resultCode == getActivity().RESULT_OK) {
            if(resultData.getData() != null){
                Uri uri = resultData.getData();

                try(OutputStream outputStream =
                            getActivity().getContentResolver().openOutputStream(uri)) {
                    resizedResultBmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                    Toast ts = Toast.makeText(getActivity(), R.string.save_result_success_toast  , Toast.LENGTH_LONG);
                    ts.show();
                } catch(Exception e){
                    Toast ts = Toast.makeText(getActivity(), R.string.save_result_failed_toast, Toast.LENGTH_LONG);
                    ts.show();

                    e.printStackTrace();
                }
            }
        }
    }
}
