package sandbox;

import java.io.IOException;

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

            // downsample 50% in X and Y

            final long[] dims = Intervals.dimensionsAsLongArray(img);
            final long[] newDims = new long[dims.length];
            double[] scaleFactors = new double[newDims.length];
            scaleFactors[0] = 0.5;
            scaleFactors[1] = 0.5;
            for (int i=0; i<newDims.length; i++) {
                if (i >= 2) {
                    newDims[i] = dims[i];
                }
                else {
                    newDims[i] = Math.round(dims[i] * scaleFactors[i]);
                }
            }

            NLinearInterpolatorFactory interpolator = new NLinearInterpolatorFactory();
            IntervalView interval = Views.interval(Views.raster(RealViews.affineReal(
                Views.interpolate(Views.extendMirrorSingle(img), interpolator),
                new Scale(scaleFactors))), new FinalInterval(newDims));

            // save scaled image

            int[] blockSize = new int[] {512, 512, 1};
            N5Writer n5 = new N5FSWriter(args[2]);
            Compression compression = new RawCompression();

            N5Utils.save(interval, n5, "0", blockSize, compression);
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
        finally {
        }
    }
}
