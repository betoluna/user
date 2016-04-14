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
 * 
 * 
 * 
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
		//handle floating point image and convert it to 8-bit grayscale
		ImageConverter orig = new ImageConverter(image);
		orig.convertToGray8();
        ip = image.getProcessor();
		
		ImageProcessor I = ip.duplicate();
		ImageProcessor ipCopy = ip.duplicate();

		int w = ip.getWidth();
		int h = ip.getHeight();

		//use the separable convolution routine from Gradient_Magnitude
		Gradient_Magnitude gm = new Gradient_Magnitude();
		gm.convolve(ipCopy, I, Hlaplace, false, w, h);
		gm.convolve(ipCopy, I, Hlaplace, true, w, h);
		
		new ImagePlus("this is I", I).show();
	}

}