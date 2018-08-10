package com.arcsoft.xmp_demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        XmpBuilder xmpBuilder = CreateXmpBuilder();
        if (xmpBuilder == null) {
            return;
        }
        requestPermission();
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

    private XmpBuilder CreateXmpBuilder( ){
        XmpBuilder xmpBuilder = new XmpBuilder();
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
