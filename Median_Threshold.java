import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * This plugin threshols an input 8-bit grayscale image by
 * setting the threshold to the median of the histogram.
 * @Author Norberto
 */
public class Median_Threshold implements PlugInFilter {

    /**
	 * This method gets called by ImageJ / Fiji to determine
	 * whether the current image is of an appropriate type.
	 *
	 * @param arg can be specified in plugins.config
	 * @param image is the currently opened image
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
    @Override
    public int setup(String arg, ImagePlus im) {
        return DOES_8G; // this plugin only works on 8-bit grayscale images
    }

    /**
	 * This method is run when the current image was accepted.
	 * @param ip is the current slice (typically, plugins use
	 * the ImagePlus set above instead).
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
    @Override
    public void run(ImageProcessor ip) {
    	//ip.autoThreshold();
    	//ip.threshold(127);
        
    	int w = ip.getWidth();
        int h = ip.getHeight();
        double median = 255 / 2;
        double threshold = median;

        for (int v = 0; v < h; v++) {
            for (int u = 0; u < w; u++) {
            	int p = ip.getPixel(u, v);
            	if(p < threshold) { // pixel value <= 127
            		ip.putPixel(u, v, 0);
            	} else { //pixel value >= 128
            		ip.putPixel(u, v, 255);
            	}
            }
        }
    }
}