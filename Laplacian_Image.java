import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ImageConverter;

/**
 * CS/ECE545 - WPI, Spring 2016
 * Name: Norberto Luna-Cano
 * Email: nlunacano@wpi.edu
 * Date: 4/13/16
 * Overview Description of Plugin:
 */

/**
 * this class computes the Laplacian of an image. I makes use of
 * the separable convolution routine defined in Gradient_Magnitude.java
 * modified and the implications of modifying the variable.
 */
public class Laplacian_Image implements PlugInFilter {
	protected ImagePlus image;

	private float Hlaplace[] = {1f, -2f, 1f};
	
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
		 * DOES_32 handles float images 
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
		//handle floating point image and convert it to 8-bit grayscale
		ImageConverter orig = new ImageConverter(image);
		orig.convertToGray8();
        ip = image.getProcessor();
		
		ImageProcessor I = ip.duplicate();
		ImageProcessor ipCopy = ip.duplicate();
		//ipCopy.smooth();
		//ipCopy.threshold(80);

		int w = ip.getWidth();
		int h = ip.getHeight();

		//use the separable convolution routine from Gradient_Magnitude
		Gradient_Magnitude gm = new Gradient_Magnitude();
		gm.convolve(ipCopy, I, Hlaplace, false, w, h);
		gm.convolve(ipCopy, I, Hlaplace, true, w, h);
		
		new ImagePlus("this is I", I).show();
	}

}