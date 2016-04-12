import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.io.Opener;

/**
 * CS/ECE545 - WPI, Spring 2016
 * Name: Norberto Luna-Cano
 * Email: nlunacano@wpi.edu
 * Date: 
 * Overview Description of Plugin:
 * 
 * 
 * 
 */

/**
 * Use any of the previous questions implementations (or none) to help design 
 * an algorithm that uses Morphology to measure the size of the pupils of the 
 * two images on the class website (50 points). NOTE: A detailed description of 
 * this problem has been left out on purpose to force you think about how one might 
 * solve this problem. Measurement the pupil size will be based on the radius of 
 * pupil detected. For this assignment, we will define the pupil as simply the opening 
 * of the eye for which light enters the inner part of the eye (https://en.wikipedia.org/wiki/Pupil). 
 * For simplicity (not reality) provide the measurement in terms of the number of pixels. slide 27 lec 7 hit or miss
 * 
 * If you class makes use of any predefined variable members, please name them
 * appropriately and provide a short description comment on how it is used or
 * modified and the implications of modifying the variable.
 */
public class Pupil_Measure implements PlugInFilter {
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
		 * take any image (including RGB images from the class website)
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
		Opener opener = new Opener();
		Circular_Hough hough = new Circular_Hough();

		ImagePlus iplus;
		for (int i = 3; i <= 9; i++) {
			for (int j = i - 2; j < i; j++) {
				iplus = opener.openImage("/Users/beto/Downloads/spine.jpg");
				ip = iplus.getProcessor();
				ip.threshold(127);
				new ImagePlus("j: " + j + ", i: " + i, ip).show();
			}
		}
	}
}

