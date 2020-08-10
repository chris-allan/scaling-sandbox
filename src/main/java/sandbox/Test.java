package sandbox;

import java.util.Arrays;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.img.Img;
//import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.*;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.Scale;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.janelia.saalfeldlab.n5.Compression;
import org.janelia.saalfeldlab.n5.N5FSReader;
import org.janelia.saalfeldlab.n5.N5FSWriter;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.saalfeldlab.n5.RawCompression;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;

public class Test {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java Test <input path> <input dataset> <output path>");
            System.exit(1);
        }
        try {
            String dataset = args[1];
            N5Reader reader =  new N5FSReader(args[0]);
            // lots of different open*(*) signatures to choose from
            // can specify disk caching, cache size, etc.
            Img img = N5Utils.open(reader, dataset);

            final long[] dims = Intervals.dimensionsAsLongArray(img);

            // crop to a particular region
            // for now, choose a centered region with X and Y 50% of the original size:
            //
            //  ----------
            //  |        |
            //  |        |
            //  |  xxxx  |
            //  |  xxxx  |
            //  |  xxxx  |
            //  |  xxxx  |
            //  |        |
            //  |        |
            //  ----------

            final long[] topLeft = new long[dims.length];
            Arrays.fill(topLeft, 0);
            final long[] croppedSize = new long[dims.length];
            Arrays.fill(croppedSize, 1);

            /*
            topLeft[0] = dims[0] / 4;
            topLeft[1] = dims[1] / 4;
            croppedSize[0] = dims[0] / 2;
            croppedSize[1] = dims[1] / 2;
            */
            topLeft[0] = 0;
            topLeft[1] = 0;
            croppedSize[0] = dims[0];
            croppedSize[1] = dims[1];

            RandomAccessibleInterval interval = Views.offsetInterval(img, topLeft, croppedSize);

            // downsample 50% in X and Y

            double[] scaleFactors = new double[dims.length];
            Arrays.fill(scaleFactors, 1.0);
            scaleFactors[0] = 0.5;
            scaleFactors[1] = 0.5;

            long[] scaledSize = new long[scaleFactors.length];
            for (int i=0; i<scaledSize.length; i++) {
                scaledSize[i] = Math.round(scaleFactors[i] * croppedSize[i]);
            }

            NLinearInterpolatorFactory interpolator = new NLinearInterpolatorFactory();
            //FloorInterpolatorFactory interpolator = new FloorInterpolatorFactory();
            //LanczosInterpolatorFactory interpolator = new LanczosInterpolatorFactory();
            //NearestNeighborInterpolatorFactory interpolator = new NearestNeighborInterpolatorFactory();
            //ClampingNLinearInterpolatorFactory interpolator = new ClampingNLinearInterpolatorFactory();

            // can choose other extension (out of bounds) strategies, but some have
            // limitations on the dimension size
            interval = Views.dropSingletonDimensions(Views.interval(Views.raster(RealViews.affineReal(
                Views.interpolate(Views.extendZero(interval), interpolator), new Scale(scaleFactors))),
                new FinalInterval(scaledSize)));

            // TODO: quickly runs out of memory, even when the downsampled image is smaller than 1000x1000
            double sigma = 3.0;
            Gauss3.gauss(sigma, interval, interval);

            // save scaled image

            int[] blockSize = new int[interval.numDimensions()];
            for (int i=0; i<blockSize.length; i++) {
                blockSize[i] = i < 2 ? 500 : 1;
            }
            N5Writer n5 = new N5FSWriter(args[2]);
            Compression compression = new RawCompression();

            N5Utils.save(interval, n5, "0/0", blockSize, compression);
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
        finally {
        }
    }
}
