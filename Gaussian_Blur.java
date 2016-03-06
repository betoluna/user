import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;// include interface Blitter
import ij.plugin.filter.Convolver;

/**
 * CS/ECE545 - WPI, Spring 2016
 * Name: Norberto Luna-Cano
 * Email: nlunacano@wpi.edu
 * Date: 3/4/16
 * Overview Description of Plugin:
 * This plugin makes use of the 1d Gaussian kernel
 * defined by the function makeGaussianKernel1d.
 */

/**
 * Objects of this class are used in Blur_Or_Sharpen plugin
 */
public class Gaussian_Blur implements PlugInFilter {
	protected ImagePlus image;

	private double sigma;
	private double w;

	/**
	 *
	 * @param arg can be specified in plugins.config
	 * @param image is the currently opened image
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	@Override
	public int setup(String arg, ImagePlus image) {
		this.image = image;
		/*
		 *
		 */
		return DOES_8G;
	}

	/**
	 * Description: Please note: ignore the run method. It is not being utilized
	 * here since this plugin is being used only by the Blur_Or_Sharpen class
	 * by the invocation of the makeGaussKernel1d function.
	 * @param ip is the current slice (typically, plugins use
	 * the ImagePlus set above instead).
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(ImageProcessor ip) {
		// sigma = 1;

		// //actually unsharp masking code
		// w = -1;

		// ImageProcessor I = ip.convertToFloat(); // I

		// // create a blurred version of the image
		// ImageProcessor J = I.duplicate(); // I^
		
		// float[] H = makeGaussKernel1d(sigma);
		// Convolver cv = new Convolver();
		// cv.setNormalize(true);

		// //apply Gaussian filter in horizontal and vertical directions
		// cv.convolve(J, H, 1, H.length);
		// cv.convolve(J, H, H.length, 1);

		// I.multiply(1 + w); // I <-  (1 + w) x I
		// J.multiply(w);     // I^ <- w x I^
		// I.copyBits(J, 0, 0, Blitter.SUBTRACT); // I^ <- (1 + w) x I - w x I^

		// //copy result back into original byte image
		// ip.insert(I.convertToByte(false), 0, 0);

	}

	/** one-dimensional Gaussian kernel
	 * @param sigma the standart deviation
	 * @return the Gaussian kernel
	 */	
	public float[] makeGaussKernel1d(double sigma) { 

		// create the kernel
		int center = (int) (3.0 * sigma);
		float[] kernel = new float[2 * center + 1]; // odd size

		// fill the kernel
		double sigma2 = sigma * sigma;
		for (int i = 0; i < kernel.length; i++) {
			double r = center - i;
			kernel[i] = (float) Math.exp(-0.5 * (r * r) / sigma2);
		}

		return kernel;
	}

}




