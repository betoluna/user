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
 * A conditional smoothing algorithm which only smooths the image 
 * if there is no edge present at the current pixel I(u,v). 
 * using the sobel operator.
 */
public class Edge_preserve_blur implements PlugInFilter {
	protected ImagePlus image;

	// A threshold variable for the edge strength. If the 
	// edge strength computed at the pixel is less than 
	// this threshold, apply smoothing.
	private double thres = 200;

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
	 * Description: 
	 *
	 * @param ip is the current slice (typically, plugins use
	 * the ImagePlus set above instead).
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(ImageProcessor ip) {
		//ip.findEdges();

		ImageProcessor Dx = ip.duplicate();
		ImageProcessor Dy = ip.duplicate();

		// Sobel x
		float[] HSobel_x = {
			-1, 0, 1,
			-2, 0, 2,
			-1, 0, 1 }; 

		// Sobel y
		float[] HSobel_y = {
			-1, -2, -1,
			0, 0, 0,
			1, 2, 1 };

		//Gaussian somoothing filter
		double[][] smoothingFilter = {
			{0.075, 0.125, 0.075},
			{0.125, 0.200, 0.125},
			{0.075, 0.125, 0.075}
		};
		
		Convolver cv = new Convolver();
		cv.convolve(Dx, HSobel_x, 3, 3);
		cv.convolve(Dy, HSobel_y, 3, 3);

		// now calculate the Edge strength. eq 7.13 p. 122 in textbook
		// for each pixel (u, v) in Dx and Dy calculate E(u, v)
		int w = ip.getWidth();
		int h = ip.getHeight();

		for (int v = 1; v <= h - 2; v++) {
			for (int u = 1; u <= w - 2; u++) {
				// get the pixel values at (u, v) for both, Dx and Dy
				int d_x = Dx.getPixel(u, v);
				int d_y = Dy.getPixel(u, v);
				double edgeStrength = Math.sqrt( (double)((d_x * d_x) + (d_y * d_y)) );//eq 7.13
				//System.out.println("edgeStrength: " + edgeStrength);// in debug mode

				// if edgeStrength is less than some threshold, apply smoothing to the pixel
				if (edgeStrength < thres) {
					double sum = 0;
					for (int j = -1; j <= 1; j++) {
						for (int i = -1; i <= 1; i++) {
							int p = ip.getPixel(u+i, v+j);
							// get the corresponding filter coefficient:
							double c = smoothingFilter[j+1][i+1];
							sum = sum + c * p;
						}
					}

					int q = (int) Math.round(sum);
					ip.putPixel(u, v, q);
				}
			}
		}


		//**************************************************************	
		// PLEASE IGNORE CODE BELOW. SEE README. THIS WAS AN ATTEMPT TO REUSE 
		// CODE FROM PART 3 and LEFT FOR FUTURE WORK.
				
		// int width = ip.getWidth();
		// int height = ip.getHeight();
		
		// Blur_Or_Sharpen bos = new Blur_Or_Sharpen();
		// // Gaussian_Blur gb = new Gaussian_Blur();
		// // float[] H = gb.makeGaussKernel1d(sigma);

		// ImageProcessor I = bos.addBorderPadding(ip,  width + 2 * radius, height + 2 * radius, radius, radius);
		// ImageProcessor Dx = I.duplicate();
		// ImageProcessor Dy = I.duplicate();

		// int w = I.getWidth();
		// int h = I.getHeight();
		
		// Convolver cv = new Convolver();
		// cv.convolve(Dx, HSobel_x, 3, 3);
		// cv.convolve(Dy, HSobel_y, 3, 3);

		// for (int v = 1; v <= h - 2; v++) {
		// 	for (int u = 1; u <= w - 2; u++) {
		// 		// get the pixel values at (u, v) for both, Dx and Dy
		// 		int d_x = Dx.getPixel(u, v);
		// 		int d_y = Dy.getPixel(u, v);
		// 		double edgeStrength = Math.sqrt( (double)((d_x * d_x) + (d_y * d_y)) );//eq 7.13
		// 		//System.out.println("edgeStrength: " + edgeStrength);// in debug mode

		// 		// if edgeStrength is less than some threshold, apply smoothing to the pixel
		// 		if (edgeStrength < thres) {
		// 			double sum = 0;
		// 			for (int j = -1; j <= 1; j++) {
		// 				for (int i = -1; i <= 1; i++) {
		// 					int p = I.getPixel(u+i, v+j);
		// 					// get the corresponding filter coefficient:
		// 					double c = smoothingFilter[j+1][i+1];
		// 					sum = sum + c * p;
		// 				}
		// 			}

		// 			int q = (int) Math.round(sum);
		// 			I.putPixel(u, v, q);
		// 		}
		// 	}
		// }

		// ip.insert(I.convertToByte(false), 0, 0);

	} // end run

}
