import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.plugin.filter.Convolver;
import ij.process.ImageConverter;
import java.util.*;

/**
 * CS/ECE545 - WPI, Spring 2016
 * Name: Norberto Luna-Cano
 * Email: nlunacano@wpi.edu
 * Date: 4/13/16
 * Overview Description of Plugin:
 * calculates the zero crossings in an image. It
 * This class sets a pixel to 255 if it is a zero crossing and 0 otherwise.
 */

/**
 * Instructions: Use this plugin filter as a template for your plugin implementations.
 * Specifically for the run method, replace the description with your own deetailed 
 * description of the implementation of the algorithm you are implementing. 
 * You may include assumptions being made, the algorithms being used, any special 
 * variables referenced, etc.
 * 
 * If you class makes use of any predefined variable members, please name them
 * appropriately and provide a short description comment on how it is used or
 * modified and the implications of modifying the variable.
 */
public class Zero_Crossings implements PlugInFilter {
	protected ImagePlus image;

	private int prev;
	private int zeros;

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
		prev = 1;
		zeros = 0;
		//handle floating point image and convert it to 8-bit grayscale
		ImageConverter orig = new ImageConverter(image);
		orig.convertToGray8();
        ip = image.getProcessor();
		
		ImageProcessor I = ip.duplicate();
		ImageProcessor ipCopy = ip.duplicate();

		// float[] Hlaplace = { 0, 1, 0,
		// 					 1, -4, 1,
		// 					 0, 1, 0 };

		// Convolver cv = new Convolver();
		// cv.convolve(ipCopy, Hlaplace, 3, 3);

		float Hlaplace[] = {1f, -2f, 1f};

		int w = ip.getWidth();
		int h = ip.getHeight();

		convolve(ipCopy, I, Hlaplace, false, w, h);
		convolve(ipCopy, I, Hlaplace, true, w, h);

		new ImagePlus("ipCopy", I).show();
	}

	/* 
	 * Separable convolution routine.
	 */
	public void convolve(ImageProcessor orig, ImageProcessor ipDest, float H[], boolean isYDirection, int w, int h) {
		ArrayList<Integer> neighbors = new ArrayList<Integer>();

		
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
				System.out.println("p: " + p);

				//######################################
				// 1
				// if (p == 0) {
				// 	zeros++;
				// } else { // p is not zero
				// 	if (!sameSign(prev, p)) {
				// 		//update prev to current p
				// 		//prev = p;

				// 		// if we have a run of zeros
				// 		if (zeros > 0) {
				// 			for (int i = 1; i <= zeros; i++) {
				// 				ipDest.set(u - i, v, 255);
				// 			}
				// 			zeros = 0;
				// 			prev = p;
				// 		} else { //opposite sign ints are next to each other
				// 			//compute abs value of opposite sign ints
				// 			int left = Math.abs(prev);
				// 			int right = Math.abs(p);
				// 			if (left > right) {
				// 				ipDest.set(u, v, 255);
				// 			} else if (left < right || left == right) {
				// 				ipDest.set(u - 1, v, 255);
				// 			}
				// 			prev = p;
				// 		}			
				// 	} else { // prev and p have the same sign
				// 		ipDest.set(u, v, 0);
				// 		prev = p;
				// 	}
				// }
				//######################################
				// 2
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



				//######################################		
				// if (p < 0) p = 0;
				// if (p > 255) p = 255;
				// ipDest.set(u, v, p);
			}
		}
	}

	//check if two ints have the same sign
	public static boolean sameSign(int x, int y) {
        return (x >= 0) ^ (y < 0);//consider zero non-negative
    }
}

