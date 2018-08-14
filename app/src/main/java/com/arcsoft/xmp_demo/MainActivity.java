package com.arcsoft.xmp_demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPProperty;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final boolean Debug = false;
    private static final int Panorama = 0;
    private static final int Depthmap = 1;

    String mEncodeImg = null;
    String mFilename = null;

    Button getDepthBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();

        getDepthBtn = findViewById(R.id.button3);
    }

    public void requestPermission(){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            boolean result= ((ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) ||
                    (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED));
            if(result){
            }else{
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},0);
            }
        }
    }

    public void createPanorama(View view){
        XmpBuilder xmpBuilder = CreateXmpBuilder(Panorama);
        if (xmpBuilder == null) {
            return;
        }
        String fileName = Environment.getExternalStorageDirectory().getPath() +"/"+UUID.randomUUID().toString();

        ExifWriter exifWriter = createExifWriter();

        File outputFile = new File(fileName + ".jpeg");

        File created ;
        try {
            created = JpegGenerator.write(exifWriter, xmpBuilder, outputFile);
        } catch (IOException | ImageReadException | ImageWriteException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Created file: " + created.getAbsolutePath());
        System.out.println("With EXIF:\n" + exifWriter.prettyPrint("    "));
        System.out.println("With XMP:\n" + xmpBuilder.prettyPrint("    "));
    }

    public void createDepthmap(View view) {

        byte[] colorImg = null;
        byte[] depthImg = null;

        try {
            InputStream isc = getResources().getAssets().open("example_color.jpg");
            int lengthc = isc.available();
            colorImg  = new byte[lengthc];
            isc.read(colorImg);
            isc.close();

            InputStream isd = getResources().getAssets().open("example_depthmap.jpg");
            int lengthd = isd.available();
            depthImg  = new byte[lengthd];
            isd.read(depthImg);
            isd.close();
        }catch (IOException e){
            e.printStackTrace();
            return;
        }

        Log.d("HJ","read success");

        Log.d("HJ","depthImg length = "+depthImg.length);

        mEncodeImg = Base64.encodeToString(depthImg,Base64.DEFAULT);

        if(Debug){
            byte[] decodeImg = Base64.decode(mEncodeImg,Base64.DEFAULT);
            Log.d("HJ","decodeImg length = "+decodeImg.length);
        }

        XmpBuilder xmpBuilder = CreateXmpBuilder(Depthmap);
        if (xmpBuilder == null) {
            return;
        }
        mFilename = Environment.getExternalStorageDirectory().getPath() +"/"+UUID.randomUUID().toString();

        ExifWriter exifWriter = createExifWriter();

        File outputFile = new File(mFilename + ".jpeg");

        File created ;
        try {
            created = JpegGenerator.write(exifWriter, xmpBuilder,colorImg, outputFile);
        } catch (IOException | ImageReadException | ImageWriteException e) {
            throw new RuntimeException(e);
        }
//        System.out.println("Created file: " + created.getAbsolutePath());
//        System.out.println("With EXIF:\n" + exifWriter.prettyPrint("    "));
//        System.out.println("With XMP:\n" + xmpBuilder.prettyPrint("    "));

        getDepthBtn.setVisibility(View.VISIBLE);
    }

    public void getDepthImage(View view) {
        XMPMeta xmpMeta = JpegGenerator.getXmpBuilder(mFilename+".jpeg");
        if(xmpMeta == null) {
            Log.d("HJ","xmpMeta is null");
            return;
        }
        String decodeImg;
        try{
            XMPProperty property = xmpMeta.getProperty("http://ns.google.com/photos/1.0/depthmap/","Data");
            decodeImg = property.getValue();
        }catch (XMPException e) {
            int error = e.getErrorCode();
            Log.e("HJ","error code :"+error);
            return;
        }

        byte[] depthImg = Base64.decode(decodeImg,Base64.DEFAULT);

        String depthImgName = Environment.getExternalStorageDirectory().getPath()+"/depth_image.jpg";

        File file = new File(depthImgName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(depthImg);
            fos.close();
        }catch (FileNotFoundException e ){
            e.printStackTrace();
            return;
        }catch (IOException e) {
            e.printStackTrace();
            return;
        }

        getDepthBtn.setVisibility(View.GONE);
    }

    private XmpBuilder CreateXmpBuilder(int imageType){
        XmpBuilder xmpBuilder = new XmpBuilder();
        switch (imageType) {
            case Panorama:{
                xmpBuilder.setNamespace("http://ns.google.com/photos/1.0/panorama/","GPano");

                xmpBuilder.addMetaData("UsePanoramaViewer","True");
                xmpBuilder.addMetaData("CaptureSoftware","Photo Sphere");
                xmpBuilder.addMetaData("StitchingSoftware","Photo Sphere");
                xmpBuilder.addMetaData("ProjectionType","equirectangular");
                xmpBuilder.addMetaData("PoseHeadingDegrees","350.0");
                xmpBuilder.addMetaData("InitialViewHeadingDegrees","90.0");
                xmpBuilder.addMetaData("InitialViewPitchDegrees","0.0");
                xmpBuilder.addMetaData("InitialViewRollDegrees","0.0");
                xmpBuilder.addMetaData("InitialHorizontalFOVDegrees","75.0");

                xmpBuilder.addMetaData("CroppedAreaLeftPixels","0");
                xmpBuilder.addMetaData("CroppedAreaTopPixels","0");
                xmpBuilder.addMetaData("CroppedAreaImageWidthPixels","512");
                xmpBuilder.addMetaData("CroppedAreaImageHeightPixels","512");
                xmpBuilder.addMetaData("FullPanoWidthPixels","512");
                xmpBuilder.addMetaData("FullPanoHeightPixels","512");
                xmpBuilder.addMetaData("FirstPhotoDate","2012-11-07T21:03:13.465Z");
                xmpBuilder.addMetaData("LastPhotoDate","2012-11-07T21:03:13.465Z");
                xmpBuilder.addMetaData("SourcePhotosCount","50");
                xmpBuilder.addMetaData("ExposureLockUsed","False");
            }
            break;
            case Depthmap: {
                xmpBuilder.setNamespace("http://ns.google.com/photos/1.0/depthmap/","GDepth");

                xmpBuilder.addMetaData("Mime","image/jpeg");
                xmpBuilder.addMetaData("Format","RangeLinear");
                xmpBuilder.addMetaData("Near","0");
                xmpBuilder.addMetaData("Far","255");
                xmpBuilder.addMetaData("Data",mEncodeImg);
            }
            break;
        }

        return xmpBuilder;
    }

    private static ExifWriter createExifWriter() {
        ExifWriter.Builder builder = new ExifWriter.Builder();

        String make;
        make = "make";

        builder.setMake(make);

        String model;
        model = "model";
        builder.setModel(model);

        return builder.build();
    }
}
