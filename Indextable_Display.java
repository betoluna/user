import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * CS/ECE545 - WPI, Spring 2016
 * Name: Norberto Luna-Cano
 * Email: nlunacano@wpi.edu
 * Date: May 1st. 2016
 * Overview Description of Plugin:
 * 
 * 
 * 
 */

/**
 * shows the color table of an 8-bit indexed image as a new image with 16 × 16 rectangular 
 * color fields. Use the following ImageJ function to label each color with the 
 * frequency and index color value in the image. Mark all unused color table entries with 
 * the value 0. The new image should look similar to the image below but with 16 x 16 squares 
 * (white background, each color represented by a square of that color, and below the colored 
 * square a label with the index # and frequency). Look at Prog. 12.3 p.249 as a starting point.
 * 
 * If you class makes use of any predefined variable members, please name them
 * appropriately and provide a short description comment on how it is used or
 * modified and the implications of modifying the variable.
 */
public class Indextable_Display implements PlugInFilter {
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
		 * The current return value accepts all gray-scale
		 * images. If it needs to process indexed
		 * images, setup() should return DOES_8C (8-bit color) see p.248
		 *
		 * It could also be DOES_ALL; you can add "| NO_CHANGES"
		 * to indicate that the current image will not be
		 * changed by this plugin.
		 *
		 * Beware of DOES_STACKS: this will call the run()
		 * method with all slices of the current image
		 * (channels, z-slices and frames, all). Most likely
		 * not what you want.
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
		
	}
}