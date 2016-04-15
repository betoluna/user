import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ImageConverter;

/**
 * CS/ECE545 - WPI, Spring 2016
 * Name: Norberto Luna-Cano
 * Email: nlunacano@wpi.edu
 * Date: 4/16/16
 */

/**
 * This class makes use of Gradient_Magnitude, Lapacian_Image,
 * and Zero Crossings plugins to perform edge detection.
 * output is a binary image with 255 at edges and zero elsewhere.
 */
public class Laplacian_Edge_Detection implements PlugInFilter {
	protected ImagePlus image;

	/**
	 * This method gets called by ImageJ / Fiji to determine
	 * whether the current image is of an appropriate type.
	 *
	 * @param arg can be specified in plugins.config
	 * @param image is the currently opened image
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	@Override
	public int setup(String arg, ImagePlus image) {
		this.image = image;
		/*
		 * Handles any type of image
 		 */
		return DOES_ALL;
	}

	/**
	 * Description: [implementation specific description]
	 *
	 * @param ip is the current slice (typically, plugins use
	 * the ImagePlus set above instead).
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(ImageProcessor ip) {
		ImageConverter orig = new ImageConverter(image);
		orig.convertToGray8();
        ip = image.getProcessor();
		
		ImageProcessor I = ip.duplicate();
		ImageProcessor J = ip.duplicate();
		ImageProcessor ipCopy = ip.duplicate();

		int w = ip.getWidth();
		int h = ip.getHeight();

		//use the separable convolution routine from Gradient_Magnitude
		Gradient_Magnitude gm = new Gradient_Magnitude();

		gm.convolve(ipCopy, I, gm.Hone, true, w, h);
		gm.convolve(ipCopy, I, gm.Hzero, false, w, h);
		gm.convolve(ipCopy, J, gm.Hone, false, w, h);
		gm.convolve(ipCopy, J, gm.Hzero, true, w, h);

		gm.computeEdgeStrength(I, J, ipCopy, w, h);

		//now use the ouput of Gradient_magnitude stored in ipCopy
		ipCopy.threshold(40);
		new ImagePlus("ipCopy", ipCopy).show();

		//ImageProcessor ip2 = new ImageProcessor();
		Zero_Crossings zc = new Zero_Crossings();
		ImageProcessor K = ip.duplicate();
		zc.process(ipCopy, K, zc.Hlaplace, false, w, h);
		zc.process(ipCopy, K, zc.Hlaplace, true, w, h);

		new ImagePlus("Laplacian Edge Detection", K).show();
	}
}