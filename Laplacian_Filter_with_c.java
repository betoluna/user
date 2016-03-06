import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.plugin.filter.Convolver;

/**
 * CS/ECE545 - WPI, Spring 2016
 * Name: Norberto Luna-Cano
 * Email: nlunacano@wpi.edu
 * Date: 3/4/16
 * Overview Description of Plugin:
 * sharpen an image by computing the Laplacian of the image and adding some amount c of that back.
 * 
 * 
 */

/**
 * The instance variable can be adjusted to obtain different
 * degrees of sharpening (with negative values) or blurring
 * (with positive values). In general values not far from zero
 * yiled better result on some images. 
 */
public class Laplacian_Filter_with_c implements PlugInFilter {
	protected ImagePlus image;

	private float c = -4f;
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
	 * Description: [implementation specific description]
	 *
	 * @param ip is the current slice (typically, plugins use
	 * the ImagePlus set above instead).
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(ImageProcessor ip) {

		//laplacian filter
		// float[] H  = {
		// 	0, 1, 0,
		// 	1, -4, 1,
		// 	0, 1, 0 };

		//filter as shown in the problem description
		float[] H  = {
			0, c/4, 0,
			c/4, 1-c, c/4,
			0, c/4, 0 };	

		Convolver cv = new Convolver();
		//cv.setNormalize(false);
		cv.convolve(ip, H, 3, 3); 
	}

}


