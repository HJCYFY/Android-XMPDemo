package com.arcsoft.xmp_demo;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ExifWriter {

    private final String model;
    private final String make;

    private ExifWriter(Builder builder) {
        this.make = builder.make;
        this.model = builder.model;
    }

    /**
     * Writes EXIF into the headers of the JPEG image stored in the input File and saves a copy
     * in the given output File.
     */
    public void reWrite(File imageFile, File outputFile) throws
            ImageWriteException,
            IOException,
            ImageReadException {

        JpegImageMetadata jpegImageMetadata = (JpegImageMetadata) Imaging.getMetadata(imageFile);
        TiffOutputSet outputSet = null;
        if (jpegImageMetadata != null) {
            TiffImageMetadata exif = jpegImageMetadata.getExif();
            if (exif != null) {
                outputSet = exif.getOutputSet();
            }
        }
        if (outputSet == null) {
            outputSet = new TiffOutputSet();
        }
        TiffOutputDirectory exifDirectory = outputSet.getOrCreateRootDirectory();
        exifDirectory.add(TiffTagConstants.TIFF_TAG_MAKE, make);
        exifDirectory.add(TiffTagConstants.TIFF_TAG_MODEL, model);

        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(outputFile));
            new ExifRewriter().updateExifMetadataLossy(imageFile, os, outputSet);
            os.close();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) { }
            }
        }
    }


    String prettyPrint(String prefix) {
        StringBuilder sb = new StringBuilder();
        if (make != null) {
            sb.append(prefix)
                    .append("Make: ")
                    .append(make)
                    .append('\n');
        }
        if (model != null) {
            sb.append(prefix)
                    .append("Model: ")
                    .append(model)
                    .append('\n');
        }

        return sb.toString();
    }


    static final class Builder {
        private String make;
        private String model;

        Builder setMake(String make) {
            this.make = make;
            return this;
        }

        Builder setModel(String model) {
            this.model = model;
            return this;
        }

        ExifWriter build() {
            return new ExifWriter(this);
        }
    }
}
