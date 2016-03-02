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
public class Laplacian_Filter_with_c implements PlugInFilter {
	protected ImagePlus image;

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
		float[] H  = {
			0, 1, 0,
			1, -4, 1,
			0, 1, 0 };

		// float[] H  = {
		// 	0, 0.125f, 0,
		// 	0.125f, -0.5f, 0.125f,
		// 	0, 0.125f, 0 };

		float c = -0.5f;
		float[] HC = {
			0, c / 4, 0,
			c / 4, 1 - c, c / 4,
			0, c / 4, 0 };

		Convolver cv = new Convolver();
		cv.setNormalize(true);
		cv.convolve(ip, H, 3, 3); //apply the laplacian filter H to image ip
		// cv.convolve(ip, H, 3, 3);
		cv.convolve(ip, HC, 3, 3);


		// int w = ip.getWidth();
  //       int h = ip.getHeight();

         
  //        for (int v = 1; v <= h-2; v++) {
  //           for (int u = 1; u <= w-2; u++) {

  //           	double sum = 0;
  //           	for (int j = -1; j <= 1; j++) {
  //           		for (int i = -1; i <= 1; i++) {

  //           		}
  //           	}
  //           	int p = ip.getPixel(u, v);
  //           	int s = (int)(16 * Math.sqrt(p));
  //           	ip.putPixel(u, v, s);

  //           }   
  //       }

	}

}


