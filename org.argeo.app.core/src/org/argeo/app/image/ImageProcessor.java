package org.argeo.app.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

public class ImageProcessor {
	private Callable<InputStream> inSupplier;
	private Callable<OutputStream> outSupplier;

	public ImageProcessor(Callable<InputStream> inSupplier, Callable<OutputStream> outSupplier) {
		super();
		this.inSupplier = inSupplier;
		this.outSupplier = outSupplier;
	}

	public void process() {
		try {
			ImageMetadata metadata = null;
			Integer orientation = null;
			try (InputStream in = inSupplier.call()) {
				metadata = Imaging.getMetadata(in, null);
				orientation = getOrientation(metadata);
			}
			try (InputStream in = inSupplier.call()) {
				if (orientation != null && orientation != TiffTagConstants.ORIENTATION_VALUE_HORIZONTAL_NORMAL) {
					BufferedImage sourceImage = ImageIO.read(in);
					AffineTransform transform = getExifTransformation(orientation, sourceImage.getWidth(),
							sourceImage.getHeight());
					BufferedImage targetImage = transformImage(sourceImage, orientation, transform);
					Path temp = Files.createTempFile("image", ".jpg");
					try {
						try (OutputStream out = Files.newOutputStream(temp)) {
							ImageIO.write(targetImage, "jpeg", out);
						}
						copyWithMetadata(() -> Files.newInputStream(temp), metadata);
					} finally {
						Files.deleteIfExists(temp);
					}
				} else {
//					try (OutputStream out = outSupplier.call()) {
					copyWithMetadata(() -> in, metadata);
//					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Cannot process image", e);
		}
	}

	protected void copyWithMetadata(Callable<InputStream> inSupplier, ImageMetadata metadata) {
		try (InputStream in = inSupplier.call(); OutputStream out = outSupplier.call();) {
			TiffOutputSet outputSet = null;
			if (metadata != null && metadata instanceof JpegImageMetadata) {
				final TiffImageMetadata exif = ((JpegImageMetadata) metadata).getExif();

				if (null != exif) {
					outputSet = exif.getOutputSet();
//					outputSet.getInteroperabilityDirectory().removeField(TiffTagConstants.TIFF_TAG_ORIENTATION);

					for (TiffOutputDirectory dir : outputSet.getDirectories()) {
//						TiffOutputField field = dir.findField(TiffTagConstants.TIFF_TAG_ORIENTATION);
//						if (field != null) {
						dir.removeField(TiffTagConstants.TIFF_TAG_ORIENTATION);
						dir.removeField(TiffTagConstants.TIFF_TAG_IMAGE_WIDTH);
						dir.removeField(TiffTagConstants.TIFF_TAG_IMAGE_LENGTH);
						dir.removeField(ExifTagConstants.EXIF_TAG_EXIF_IMAGE_WIDTH);
						dir.removeField(ExifTagConstants.EXIF_TAG_EXIF_IMAGE_LENGTH);
//							System.out.println("Removed orientation from " + dir.description());
//						}
					}
				}
			}

			if (null == outputSet) {
				outputSet = new TiffOutputSet();
			}
			new ExifRewriter().updateExifMetadataLossless(in, out, outputSet);
		} catch (Exception e) {
			throw new RuntimeException("Could not update EXIF metadata", e);
		}

	}

	public static BufferedImage transformImage(BufferedImage image, Integer orientation, AffineTransform transform)
			throws Exception {

		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);

		int width = image.getWidth();
		int height = image.getHeight();
		switch (orientation) {
		case TiffTagConstants.ORIENTATION_VALUE_ROTATE_180:
		case TiffTagConstants.ORIENTATION_VALUE_MIRROR_HORIZONTAL_AND_ROTATE_270_CW:
		case TiffTagConstants.ORIENTATION_VALUE_ROTATE_90_CW:
		case TiffTagConstants.ORIENTATION_VALUE_MIRROR_HORIZONTAL_AND_ROTATE_90_CW:
		case TiffTagConstants.ORIENTATION_VALUE_ROTATE_270_CW:
			width = image.getHeight();
			height = image.getWidth();
			break;
		}

		BufferedImage destinationImage = new BufferedImage(width, height, image.getType());

		Graphics2D g = destinationImage.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
		destinationImage = op.filter(image, destinationImage);
		return destinationImage;
	}

	public static int getOrientation(ImageMetadata metadata) {
		if (metadata instanceof JpegImageMetadata) {
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			TiffField field = jpegMetadata.findExifValue(TiffTagConstants.TIFF_TAG_ORIENTATION);
			if (field == null)
				return TiffTagConstants.ORIENTATION_VALUE_HORIZONTAL_NORMAL;
			try {
				return field.getIntValue();
			} catch (ImagingException e) {
				throw new IllegalStateException(e);
			}
		} else {
			throw new IllegalArgumentException("Unsupported metadata format " + metadata.getClass());
		}
	}

	public static AffineTransform getExifTransformation(Integer orientation, int width, int height) {

		AffineTransform t = new AffineTransform();

		switch (orientation) {
		case TiffTagConstants.ORIENTATION_VALUE_HORIZONTAL_NORMAL:
			return null;
		case TiffTagConstants.ORIENTATION_VALUE_MIRROR_HORIZONTAL: // Flip X
			t.scale(-1.0, 1.0);
			t.translate(-width, 0);
			break;
		case TiffTagConstants.ORIENTATION_VALUE_ROTATE_180: // PI rotation
			t.translate(width, height);
			t.rotate(Math.PI);
			break;
		case TiffTagConstants.ORIENTATION_VALUE_MIRROR_VERTICAL: // Flip Y
			t.scale(1.0, -1.0);
			t.translate(0, -height);
			break;
		case TiffTagConstants.ORIENTATION_VALUE_MIRROR_HORIZONTAL_AND_ROTATE_270_CW: // - PI/2 and Flip X
			t.rotate(-Math.PI / 2);
			t.scale(-1.0, 1.0);
			break;
		case TiffTagConstants.ORIENTATION_VALUE_ROTATE_90_CW: // -PI/2 and -width
			t.translate(height, 0);
			t.rotate(Math.PI / 2);
			break;
		case TiffTagConstants.ORIENTATION_VALUE_MIRROR_HORIZONTAL_AND_ROTATE_90_CW: // PI/2 and Flip
			t.scale(-1.0, 1.0);
			t.translate(-height, 0);
			t.translate(0, width);
			t.rotate(3 * Math.PI / 2);
			break;
		case TiffTagConstants.ORIENTATION_VALUE_ROTATE_270_CW: // PI / 2
			t.translate(0, width);
			t.rotate(3 * Math.PI / 2);
			break;
		}

		return t;
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 2)
			throw new IllegalArgumentException(
					"Usage: " + ImageProcessor.class.getSimpleName() + " <source image> <target image>");
		Path imagePath = Paths.get(args[0]);
		Path targetPath = Paths.get(args[1]);

		System.out.println("## Source metadata:");
		try (InputStream in = Files.newInputStream(imagePath)) {
			metadataExample(in, null);
		}

		ImageProcessor imageProcessor = new ImageProcessor(() -> Files.newInputStream(imagePath),
				() -> Files.newOutputStream(targetPath));
		imageProcessor.process();

		System.out.println("## Target metadata:");
		try (InputStream in = Files.newInputStream(targetPath)) {
			metadataExample(in, null);
		}

	}

	public static void metadataExample(InputStream in, String fileName) throws ImagingException, IOException {
		// get all metadata stored in EXIF format (ie. from JPEG or TIFF).
		final ImageMetadata metadata = Imaging.getMetadata(in, fileName);

		// System.out.println(metadata);

		if (metadata instanceof JpegImageMetadata) {
			final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

			// Jpeg EXIF metadata is stored in a TIFF-based directory structure
			// and is identified with TIFF tags.
			// Here we look for the "x resolution" tag, but
			// we could just as easily search for any other tag.
			//
			// see the TiffConstants file for a list of TIFF tags.

			// System.out.println("file: " + file.getPath());

			// print out various interesting EXIF tags.
			printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_XRESOLUTION);
			printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME);
			printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
			printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
			printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO);
			printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
			printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
			printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_BRIGHTNESS_VALUE);
			printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
			printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE);
			printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
			printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE);

			System.out.println();

			// simple interface to GPS data
			final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
			if (null != exifMetadata) {
				final TiffImageMetadata.GpsInfo gpsInfo = exifMetadata.getGpsInfo();
				if (null != gpsInfo) {
					final String gpsDescription = gpsInfo.toString();
					final double longitude = gpsInfo.getLongitudeAsDegreesEast();
					final double latitude = gpsInfo.getLatitudeAsDegreesNorth();

					System.out.println("    " + "GPS Description: " + gpsDescription);
					System.out.println("    " + "GPS Longitude (Degrees East): " + longitude);
					System.out.println("    " + "GPS Latitude (Degrees North): " + latitude);
				}
			}

			// more specific example of how to manually access GPS values
			final TiffField gpsLatitudeRefField = jpegMetadata
					.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
			final TiffField gpsLatitudeField = jpegMetadata
					.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE);
			final TiffField gpsLongitudeRefField = jpegMetadata
					.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
			final TiffField gpsLongitudeField = jpegMetadata
					.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE);
			if (gpsLatitudeRefField != null && gpsLatitudeField != null && gpsLongitudeRefField != null
					&& gpsLongitudeField != null) {
				// all of these values are strings.
				final String gpsLatitudeRef = (String) gpsLatitudeRefField.getValue();
				final RationalNumber[] gpsLatitude = (RationalNumber[]) (gpsLatitudeField.getValue());
				final String gpsLongitudeRef = (String) gpsLongitudeRefField.getValue();
				final RationalNumber[] gpsLongitude = (RationalNumber[]) gpsLongitudeField.getValue();

				final RationalNumber gpsLatitudeDegrees = gpsLatitude[0];
				final RationalNumber gpsLatitudeMinutes = gpsLatitude[1];
				final RationalNumber gpsLatitudeSeconds = gpsLatitude[2];

				final RationalNumber gpsLongitudeDegrees = gpsLongitude[0];
				final RationalNumber gpsLongitudeMinutes = gpsLongitude[1];
				final RationalNumber gpsLongitudeSeconds = gpsLongitude[2];

				// This will format the gps info like so:
				//
				// gpsLatitude: 8 degrees, 40 minutes, 42.2 seconds S
				// gpsLongitude: 115 degrees, 26 minutes, 21.8 seconds E

				System.out.println("    " + "GPS Latitude: " + gpsLatitudeDegrees.toDisplayString() + " degrees, "
						+ gpsLatitudeMinutes.toDisplayString() + " minutes, " + gpsLatitudeSeconds.toDisplayString()
						+ " seconds " + gpsLatitudeRef);
				System.out.println("    " + "GPS Longitude: " + gpsLongitudeDegrees.toDisplayString() + " degrees, "
						+ gpsLongitudeMinutes.toDisplayString() + " minutes, " + gpsLongitudeSeconds.toDisplayString()
						+ " seconds " + gpsLongitudeRef);

			}

			System.out.println();

			final List<ImageMetadataItem> items = jpegMetadata.getItems();
			for (final ImageMetadataItem item : items) {
				System.out.println("    " + "item: " + item);

			}

			System.out.println();
		}
	}

	private static void printTagValue(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
		final TiffField field = jpegMetadata.findExifValueWithExactMatch(tagInfo);
		if (field == null) {
			System.out.println(tagInfo.name + ": " + "Not Found.");
		} else {
			System.out.println(tagInfo.name + ": " + field.getValueDescription());
		}
	}

}
