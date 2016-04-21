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
 * IJ.log("> ");
 * 
 */

/**
 * Implements the Reinhard et al. 2001 paper (IEEE Computer Graphics and Applications).
 * Please read the paper thoroughly before attempting the implement it. Test your 
 * plugin using the images on the class site of “Vincent van Gogh’s Cafe Terrace on 
 * the Place du Forum” as the color profile to apply to the fuller labs photo, which 
 * obviously needs some color.
 * 
 * If you class makes use of any predefined variable members, please name them
 * appropriately and provide a short description comment on how it is used or
 * modified and the implications of modifying the variable.
 */
public class Color_Transfer implements PlugInFilter {
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
		return DOES_8G + DOES_8C;
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