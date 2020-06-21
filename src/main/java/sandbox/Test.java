package sandbox;

import io.scif.Format;
import io.scif.SCIFIO;
import io.scif.bf.BioFormatsFormat;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
import io.scif.services.FormatService;

import java.io.IOException;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.Scale;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.io.location.FileLocation;

public class Test {
    public static void main(String[] args) {
        SCIFIO scifio = new SCIFIO();
        try {
            // turn off all formats except Bio-Formats compatibility format
            FormatService formats = scifio.format();
            for (Format f : formats.getAllFormats()) {
                f.setEnabled(f instanceof BioFormatsFormat);
            }

            // open image from specified file
            ImgOpener opener = new ImgOpener();
            // no openImg(Location) signature
            final Img img = opener.openImgs(new FileLocation(args[0])).get(0);

            // downsample 50% in X and Y
            // tried using the "scaleView" op, but matching is not reliable (maybe due to BioFormatsFormat?)

						final long[] newDims = Intervals.dimensionsAsLongArray(img);
            double[] scaleFactors = new double[newDims.length];
            scaleFactors[0] = 0.5;
            scaleFactors[1] = 0.5;
						for (int i=0; i<newDims.length; i++) {
                if (i >= 2) {
                    scaleFactors[i] = 1.0 / newDims[i];
                }
						    newDims[i] = Math.round(newDims[i] * scaleFactors[i]);
						}

						NLinearInterpolatorFactory interpolator = new NLinearInterpolatorFactory();
						IntervalView interval = Views.interval(Views.raster(RealViews.affineReal(
								Views.interpolate(Views.extendMirrorSingle(img), interpolator),
								new Scale(scaleFactors))), new FinalInterval(newDims));

            // display image
            ImageJFunctions.show(interval);

            // save scaled image
            // TODO: how to convert an IntervalView to an Img?
            //ImgSaver saver = new ImgSaver();
            //saver.saveImg(new FileLocation(args[1]), interval);
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
        finally {
            // context disposal takes several seconds
            /* debug */ System.out.println("shutting down...");
            scifio.context().dispose();
        }
    }
}
