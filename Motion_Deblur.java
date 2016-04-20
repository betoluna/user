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
* Motion Deblur via Deconvolution: Read the following paper “COMPARISON OF MOTION DEBLUR ALGORITHMS “ 
* found on the class site. Then implement an imageJ plugin named Motion_Deblur that performs 
* the direct deblurring method in this paper (Section 3.1 in paper). Assume there is no noise, 
* which simplifies the method. Assume the motion is 2D. Make sure to pay attention to padding. 
* Your job is to find the blur kernel that best represents the motion blur by searching the 
* kernel’s space and repeatedly de-convolving the image with the filter. Use the methods from 
* the previous question to implement your plugin. In your search, use the error metric 
* Peak signal-to-noise ratio (PSNR) defined in Section 4.3. Search should terminate when you 
* reach a suitable error (see Table 2 in paper for possible values).
* Write all methods from scratch (ie don’t use predefined frequency domain methods in imageJ), 
* except of course the Java math functions of the math functions in the imageProcessor class.
*
 * If you class makes use of any predefined variable members, please name them
 * appropriately and provide a short description comment on how it is used or
 * modified and the implications of modifying the variable.
 */
public class Motion_Deblur implements PlugInFilter {
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
		 * images (if you access the pixels with ip.getf(x, y)
		 * anyway, that works quite well.
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