package sandbox;

import java.io.IOException;
import java.util.Arrays;

import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.Scale;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
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
            final Img img = N5Utils.open(reader, dataset);

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

            final long[] dims = Intervals.dimensionsAsLongArray(img);
            final long[] topLeft = new long[dims.length];
            Arrays.fill(topLeft, 0);
            final long[] croppedSize = new long[dims.length];
            Arrays.fill(croppedSize, 1);

            topLeft[0] = dims[0] / 4;
            topLeft[1] = dims[1] / 4;
            croppedSize[0] = dims[0] / 2;
            croppedSize[1] = dims[1] / 2;

            final long[] bottomRight = new long[dims.length];
            for (int i=0; i<bottomRight.length; i++) {
                bottomRight[i] = (topLeft[i] + croppedSize[i]) - 1;
            }

            IntervalView interval = Views.interval(img, topLeft, bottomRight);

            /*
            // downsample 50% in X and Y

            double[] scaleFactors = new double[] {0.5, 0.5, 1, 1, 1};

            NLinearInterpolatorFactory interpolator = new NLinearInterpolatorFactory();
            interval = Views.interval(Views.raster(RealViews.affineReal(
                Views.interpolate(Views.extendMirrorSingle(interval), interpolator),
                new Scale(scaleFactors))), new FinalInterval(2000, 2000, 1, 1, 1));
            */


            // save scaled image

            int[] blockSize = new int[dims.length];
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
