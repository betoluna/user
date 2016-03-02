import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
*Burger and Burge problem number 4.3 (page 51). For this problem, 
*you should create a plugin called NonLinear_Binning that solves the 
*problem described. Create a table of 10 arbitrary ranges and pick 
*appropriate intervals. It should be possible for the ranges you choose 
*in your program to be changed and the program recompiled. See p 48
*
* Nonlinear binning assuming 8-bit grayscale images. Using a logarithmic 
* function to assign values to the bins.
* X^B = 256 -> lg X^B = lg 256 -> B lg X = 8 -> lg X = 8 / B -> 2^(8/B) = X 
* so to find a bin for any pixel value X, compute lg (base 2^(8/B)) (X)
* @Author Norberto
*/
public class NonLinear_Binning implements PlugInFilter {
	protected ImagePlus image;

	private double B = 10; // the number of desired bins. Change as needed.
	private int PIXEL_VALUE;// the current location pixel value
	private double BIT_DEPTH = 8; //grayscale image bit-depth
	private int BASE = 2; //used logarithmic base 

	/**
	 * @param image is the currently opened image
	 */
	@Override
	public int setup(String arg, ImagePlus image) {
		this.image = image;
		//The current return value accepts 8-bit gray-scale images 
		return DOES_8G;
	}

	/**
	 * 
	 * @param 
	*/	
	@Override
	public void run(ImageProcessor ip) {
		int[] nonLinearBinnedH = nonLinearBinnedHistogram(ip);

		//ip.applyTable(nonLinearBinnedH);
	}

	//compute the nonLinear bining histogram
	private int[] nonLinearBinnedHistogram(ImageProcessor ip) {
		int[] H = new int[(int) B];

		int w = ip.getWidth();
		int h = ip.getHeight();

		for (int v = 0; v < h; v++) {
            for (int u = 0; u < w; u++) {
            	PIXEL_VALUE = ip.getPixel(u, v);
            	if(PIXEL_VALUE > 0) {
            		//using a nonlinear log function on the pixel value to calculate an index for the bin.
            		int i = (int) (Math.log(PIXEL_VALUE) / Math.log( Math.pow(BASE, BIT_DEPTH / B)));
            		H[i] = H[i] + 1;
            	} else {
            		H[0] = H[0] + 1;
            	}
            	
            }
        }

        //binned histogram
        return H;
	}
}