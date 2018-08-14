package com.arcsoft.xmp_demo;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.util.Log;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;


public class JpegGenerator {

    private static final int IMAGE_DIMENSION = 512;
    private static final String TEMP_PREFIX = "gen-tmp";
    private static final String JPEG_SUFFIX = ".jpeg";


    private JpegGenerator() { }

    static XMPMeta getXmpBuilder(String filename) {
        XMPMeta xmpMeta = XmpUtil.extractXMPMeta(filename);
        return xmpMeta;
    }

    static File write(ExifWriter exifWriter, XmpBuilder builder,byte[] jpeg, File outputFile) throws
            IOException,
            ImageReadException,
            ImageWriteException {

        XMPMeta xmpMeta;
        try {
            xmpMeta = builder.build();
        } catch (XMPException e) {
            throw new RuntimeException(e);
        }

        File tempFile = File.createTempFile(TEMP_PREFIX, JPEG_SUFFIX);
        try {
            try {
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(jpeg);
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            boolean ok = XmpUtil.writeXMPMeta(tempFile.getAbsolutePath(), xmpMeta);
            if(!ok) {
                Log.d("HJ","writeXMPMeta failed");
            }

            exifWriter.reWrite(tempFile, outputFile);
        } finally {
            if (!tempFile.delete()) {
                System.out.println(String.format("Failed to delete tempFile %s", tempFile));
            }
        }

        return outputFile;
    }

    /**
     * Generates a new Random JPEG File with the given EXIF and XMP and saves it to the given output
     * File.
     */
    static File write(ExifWriter exifWriter, XmpBuilder builder, File outputFile) throws
            IOException,
            ImageReadException,
            ImageWriteException {

        int[] image = new int[IMAGE_DIMENSION * IMAGE_DIMENSION];
        Random random = new Random(System.currentTimeMillis());
        for (int y = 0; y < IMAGE_DIMENSION; y++) {
            for (int x = 0; x < IMAGE_DIMENSION; x++) {
                image[x*IMAGE_DIMENSION + y] =  random.nextInt(256);
            }
        }

        Bitmap bmp = Bitmap.createBitmap(image,IMAGE_DIMENSION,IMAGE_DIMENSION, Bitmap.Config.RGB_565);

        XMPMeta xmpMeta;
        try {
            xmpMeta = builder.build();
        } catch (XMPException e) {
            throw new RuntimeException(e);
        }

        File tempFile = File.createTempFile(TEMP_PREFIX, JPEG_SUFFIX);
        try {
            try {
                FileOutputStream fos = new FileOutputStream(tempFile);
                bmp.compress(Bitmap.CompressFormat.JPEG,80,fos);
                bmp.recycle();
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            XmpUtil.writeXMPMeta(tempFile.getAbsolutePath(), xmpMeta);

            exifWriter.reWrite(tempFile, outputFile);
        } finally {
            if (!tempFile.delete()) {
                System.out.println(String.format("Failed to delete tempFile %s", tempFile));
            }
        }

        return outputFile;
    }

}
