import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;// include interface Blitter
import ij.plugin.filter.Convolver;
import ij.plugin.CanvasResizer;

import ij.gui.Roi;
/**
 * CS/ECE545 - WPI, Spring 2016
 * Name: Norberto Luna-Cano
 * Email: nlunacano@wpi.edu
 * Date: 3/4/16
 * Overview Description of Plugin:
 * Blurres of Sharpens an image depending on the
 * value of the parameter W (-1 to 1)
 */

/**
 * This class blurres or sharpens an image. When W = 1 the image is sharpened
 * when W = -1 the image is blurred.
 */
public class Blur_Or_Sharpen implements PlugInFilter {
	protected ImagePlus image;
	private double sigma = 1; // standard deviation
	int radius = (int)(3 * sigma);//the radius of the one-dimensional kernel
	private double W = -1; // the parameter to blur or sharpen

	/**
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
	 * Description: [implementation specific description]
	 *
	 * @param ip is the current slice (typically, plugins use
	 * the ImagePlus set above instead).
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(ImageProcessor ip) {
		ImageProcessor I = ip.convertToFloat();

		ImageProcessor ipCopy = I.duplicate();
		int width = ipCopy.getWidth();
		int height = ipCopy.getHeight();
		ImageProcessor ipNew = addBorderPadding(ipCopy,  width + 2 * radius, height + 2 * radius, radius, radius);
		Roi roi = new Roi(radius, radius, width, height);//define region of interest
		
		int wNew = ipNew.getWidth();
		int hNew = ipNew.getHeight();
		//set region of interest (border) to neutral intensity value
		for (int v = 0; v < hNew; v++) {
			for (int u = 0; u < wNew; u++) {
				if (!roi.contains(u,v)) {
					ipNew.set(u, v, 127);//set to 127 intensity value
				}
			}
		}

		//create a Gaussian_Blur object
		Gaussian_Blur gb = new Gaussian_Blur();
		float[] H = gb.makeGaussKernel1d(sigma);

		Convolver cv = new Convolver();
		cv.setNormalize(true);

		//convolve the new padded image with the kernel in the x and y directions
		cv.convolve(ipNew, H, 1, H.length);
		cv.convolve(ipNew, H, H.length, 1);
		ImageProcessor J = ipNew.crop();

		I.multiply(1 + W);
		J.multiply(W); // ipNew <- w x ipNew * Gaussian1d 
		I.copyBits(J, 0, 0, Blitter.SUBTRACT);

		//copy result back into original byte image
		ip.insert(I.convertToByte(false), 0, 0);

		//new ImagePlus("padded copy", ipNew).show();

	}

	/**
	* @param ipOld the original ImageProcessor
	* @param wNew the new desired width 
	* @param hNew the new desider height
	* @param xOff the x-coordinate offset
	* @param yOff the y-coordinate offset
	* @return a new padded ImageProcessor
	*/
	public ImageProcessor addBorderPadding(ImageProcessor ipOld, int wNew, int hNew, int xOff, int yOff) {
		ImageProcessor ipNew = ipOld.createProcessor(wNew, hNew);
		//ipNew.fill();		
		ipNew.insert(ipOld, xOff, yOff);

		return ipNew;
	}

}


