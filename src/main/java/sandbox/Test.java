package sandbox;

import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;

import java.io.IOException;

import net.imagej.ImageJ;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;

public class Test {
    public static void main(String[] args) {
        try {
            ImgOpener opener = new ImgOpener();
            final Img img = opener.openImg(args[0]);

            ImageJ ij = new ImageJ();
            double[] scaleFactors = new double[] {0.5, 0.5};
            Object scaled = ij.op().run("scaleView", img, scaleFactors, new NLinearInterpolatorFactory());
            ij.io().save(scaled, args[1]);
        }
        catch (final ImgIOException|IOException e) {
            e.printStackTrace();
        }
    }
}
