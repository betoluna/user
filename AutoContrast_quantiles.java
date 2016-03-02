import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/** 
* Burger and Burge problem number 5.1 (page 83). Implement the auto-contrast
* operation as defined in Eqns. (5.8)- (5.10) as an ImageJ plugIn fo an 8-bit 
* grayscale image. Set the quantile s of pixels to be staurated at both ends of 
* the intensity range (0 and 255) to s_low = s_high = 1%.
*
* This plugin implements Autocontrast as per the problem specification.
* @Author Norberto
*/
public class AutoContrast_quantiles implements PlugInFilter {
	protected ImagePlus image;

	private int a_low, a_high, aHat_low, aHat_high;
	private final int A_MIN = 0, A_MAX = 255;// the extreme values of the target range
	private int[] EXPANDED_HISTOGRAM;

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

		int[] hist = ip.getHistogram();

		//find a_low
		int i = 0;
		while(hist[i] == 0) {
			i++;
		}
		a_low = i;

		//find a_high
		i = 255;
		while(hist[i] == 0) {
			i--;
		}
		a_high = i;

		//calculate 1% for s_low, and s_high
		int range = a_high - a_low;
		int offset = (int) Math.round(range * 0.01); 
		aHat_low = a_low + offset;
		aHat_high = a_high - offset;

		/* 
		 * saturate quantile s_low, and s_high. This is done by mapping
		 * the values in the quantiles to the extreme values of the target range.
		 */
		EXPANDED_HISTOGRAM = new int[256];
		i = a_low;
		while(i <= aHat_low) {
			EXPANDED_HISTOGRAM[A_MIN] = EXPANDED_HISTOGRAM[A_MIN] + 1;
			i++;
		}

		i = a_high;
		while(i >= aHat_high) {
			EXPANDED_HISTOGRAM[A_MAX] = EXPANDED_HISTOGRAM[A_MAX] + 1;
			i--;
		}

		//now map intermediate values to range [A_MIN, A_MAX]
		for(int j = aHat_low + 1; j < aHat_high; j++) {
			int index = (j - a_low) * 255 / range; 
			EXPANDED_HISTOGRAM[index] = hist[j];
		}	

		ip.applyTable(EXPANDED_HISTOGRAM);	
	}

	
}