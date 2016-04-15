import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.plugin.filter.Convolver;
import ij.process.ImageConverter;

/**
 * CS/ECE545 - WPI, Spring 2016
 * Name: Norberto Luna-Cano
 * Email: nlunacano@wpi.edu
 * Date: 4/16/16
 * Overview Description of Plugin:
 * calculates the zero crossings in an image. It
 * This class sets a pixel to 255 if it is a zero crossing and 0 otherwise.
 */

/**
 * This class makes use of a one dimensional gaussian filter
 * with sigma = 1 for smoothing: makeGaussKernel1d(sigma)
 * as implemented in the textbook.
 */
public class Zero_Crossings implements PlugInFilter {
	protected ImagePlus image;

	// private int prev;
	// private int zeros;
	public int sigma;
	public float Hlaplace[] = {1f, -2f, 1f};

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
		 * handles all types of images
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
		// prev = 1;
		// zeros = 0;
		sigma = 1;

		//handle floating point image and convert it to 8-bit grayscale
		ImageConverter orig = new ImageConverter(image);
		orig.convertToGray8();
        ip = image.getProcessor();
		
		ImageProcessor I = ip.duplicate();
		ImageProcessor ipCopy = ip.duplicate();
		
		//use ImageJ smooth()
		//ipCopy.smooth();
		
		//or alternatively use a gaussian smoothing filter with sigma = 1
		// Gaussian_Blur gb = new Gaussian_Blur();
		// float[] H = gb.makeGaussKernel1d(sigma);
		// Convolver cv = new Convolver();
		// cv.convolve(ipCopy, H, 1, H.length);
		// cv.convolve(ipCopy, H, H.length, 1);

		ipCopy.threshold(127);

		//float Hlaplace[] = {1f, -2f, 1f};

		int w = ip.getWidth();
		int h = ip.getHeight();

		process(ipCopy, I, Hlaplace, false, w, h);
		process(ipCopy, I, Hlaplace, true, w, h);

		new ImagePlus("ipCopy", I).show();
	}

	/* 
	 * This method does separable convolution plus comptes the zero-crossings.
	 */
	public void process(ImageProcessor orig, ImageProcessor ipDest, float H[], boolean isYDirection, int w, int h) {
		int prev = 1;
		int zeros = 0;
		for (int v = 1; v <= h-2; v++) {
			for (int u = 1; u <= w-2; u++) {
				double sum = 0;
				int idx = 0;

				//for each value in 3x3 filter H...
				for (int i = -1; i <= 1; i++) {
					int temp;
					if (isYDirection) {
						temp = orig.getPixel(u, v+i);//x coord stays constant, y moves
					} else {
						temp = orig.getPixel(u+i, v);//y coord stays constant, x moves
					}

					sum = sum + temp * H[idx++];
				}
				int p = (int) Math.round(sum);
				//System.out.println("p: " + p);
				
				// compute the actual zero-crossings here:
				// when 'p' (which results from convolving the laplacian
				// with each pixel) changes sign, put 255 at zero-crossing
				// otherwise put 0.
				if (p != 0) {
					if (!sameSign(prev, p)) {
						int left = Math.abs(prev);
						int right = Math.abs(p);
						if (left > right) {
							ipDest.set(u, v, 255);
						} else if (left < right || left == right) {
							ipDest.set(u - 1, v, 255);
						}
						prev = p;
					} else { // prev and p have the same sign
						ipDest.set(u, v, 0);
						prev = p;
					}
				} else { //p = 0
					ipDest.set(u, v, 0);
					prev = p;
				}

			}
		}
	}

	//check if two ints have the same sign
	public static boolean sameSign(int x, int y) {
        return (x >= 0) ^ (y < 0);//consider zero non-negative
    }
}

