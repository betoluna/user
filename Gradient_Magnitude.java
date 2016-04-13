import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.Convolver;
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
 * computes the gradient magnitude of an image. Filter should work with floating point image as input
 * 
 * If you class makes use of any predefined variable members, please name them
 * appropriately and provide a short description comment on how it is used or
 * modified and the implications of modifying the variable.
 */
public class Gradient_Magnitude implements PlugInFilter {
	protected ImagePlus image;

	private int w;
	private int h;

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

	/* 
	 * Separable convolution routine.
	 * Convolve the image with one separable Sobel filter at a time
	 */
	public void convolve(ImageProcessor orig, ImageProcessor ipDest, float H[], boolean isDirectionY) {
		
		for (int v = 1; v <= h-2; v++) {
			for (int u = 1; u <= w-2; u++) {
				double sum = 0;
				int idx = 0;

				//for each value in 3x3 filter H...
				for (int i = -1; i <= 1; i++) {
					int temp;
					if (isDirectionY) {
						temp = orig.getPixel(u, v+i);//x coord stays constant
					} else {
						temp = orig.getPixel(u+i, v);//y coord stays constant
					}

					sum = sum + temp * H[idx++];
				}
				int p = (int) Math.round(sum);
				if (p < 0) p = 0;

				ipDest.putPixel(u, v, p);
			}
		}
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
		ImageProcessor J = ip.duplicate();
		ImageProcessor ipCopy = ip.duplicate();

		w = ip.getWidth();
		h = ip.getHeight();

		//separable Sobel filters see textbook p.122
		float Hzero[] = {-1f, 0f, 1f};
		float Hone[]  = {1f, 2f, 1f};

		//convolve the image
		convolve(ipCopy, I, Hone, true);
		convolve(ipCopy, I, Hzero, false);
		convolve(ipCopy, J, Hone, false);
		convolve(ipCopy, J, Hzero, true);

		// Compute E(u, v), the gradient magnitude see book p.122
		for (int v = 0; v < h; v++) {
         	for (int u = 0; u < w; u++) {
         		double DxSqred = I.getPixel(u, v) * I.getPixel(u, v);
         		double DySqred = J.getPixel(u, v) * J.getPixel(u, v);
         		int Euv = (int) Math.sqrt(DxSqred + DySqred);
         		if (Euv > 255) {
         			//System.out.println("out of bounds: " + Euv + ", clamping Euv to 255");
         			Euv = 255;
         		}
	
         		ipCopy.set(u, v, Euv);
            }
        }

		new ImagePlus("this is ipCopy", ipCopy).show();

		//************************************************

		// Using a Convolver object
		// float HSx[] = {-1, 0, 1,
		// 			   -2, 0, 2,
		// 			   -1, 0, 1};

		// float HSy[] = {-1, -2, -1,
		// 			    0, 0, 0,
		// 			    1, 2, 1};

		// Convolver cv = new Convolver();
		// cv.convolve(I, HSx, 3, 3);
		// cv.convolve(J, HSy, 3, 3);

		// int w = ip.getWidth();
		// int h = ip.getHeight();
	
  //       for (int v = 0; v < h; v++) {
  //        	for (int u = 0; u < w; u++) {
  //        		double DxSqred = I.getPixel(u, v) * I.getPixel(u, v);
  //        		double DySqred = J.getPixel(u, v) * J.getPixel(u, v);
  //        		int Euv = (int) Math.sqrt(DxSqred + DySqred);
  //        		if (Euv > 255) {
  //        			System.out.println("out of bounds: " + Euv + ", clamping Euv to 255");
  //        			Euv = 255;
  //        		}
	
  //        		ipCopy.set(u, v, Euv);
  //           }
  //       }

		// new ImagePlus("this is ipCopy", ipCopy).show();

	}//end run()
}