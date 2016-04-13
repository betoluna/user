import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.Convolver;
import ij.process.ImageProcessor;

/**
 * CS/ECE545 - WPI, Spring 2016
 * Name: Norberto Luna-Cano
 * Email: nlunacano@wpi.edu
 * Date: 4/13/16
 * Overview Description of Plugin:
 * 
 * 
 * 
 */

/**
 * computes the gradient magnitude of an image. Filter should work with floating point image as input
 * 
 * If you class makes use of any predefined variable members, please name them
 * appropriately and provide a short description comment on how it is used or
 * modified and the implications of modifying the variable.
 */
public class Gradient_Magnitude implements PlugInFilter {
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
		 * 
 		 */
		return DOES_8G | DOES_16 | DOES_32;
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
		ImageProcessor J = I.duplicate();
 
		float HSx[] = {-1, 0, 1,
					   -2, 0, 2,
					   -1, 0, 1};

		float HSy[] = {-1, -2, -1,
					    0, 0, 0,
					    1, 2, 1};

	}
}