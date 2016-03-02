import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * This plugInperforms a power law transformation on an image. Works on
 * 8‐bit grayscale images.
 *
 * runaway.jpg: c = 1, gamma = 2.8, resulted in improved clarity and more contrast,  which resulted in a more balanced histogram.
 * changing values for c (e.g.0.5, 1.3) did not shield good results.
 * spine.jpg: gamma = 0.5 yield best results revealing more detail on the left darker side of the figure. Values above 1 were unuseful since 
 * the picture was darkened on the same side hiding more detail than the original. Other useful setting was to raise c to c = 1.3
 * and lower gamma to 0.7
 * My gamma is set to 2.2 which is the standard Mac OS gamma value setting. Still I will point out that the two halves of the
 * circle were not clearly seen, but instead they could be seen very faintly with the lighter gray half being slightly more perceptible.
 * I need to do more research to be able to describe how this effects my viewing in regards to my choice of gamma in my plugIn. 
 * @Author Norberto
 */
public class Power_Transform implements PlugInFilter {
	protected ImagePlus image;

	private int maxPixelValue = 255;
	private double c = 1.3;
    private double GAMMA = 0.7;

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
		 * The current return value accepts 8-bit gray-scale images 
		 */
		return DOES_8G;
	}

	/**
	 * See textbook p. 77
	 * @param 
	*/	
	@Override
	public void run(ImageProcessor ip) {

		int w = ip.getWidth();
        int h = ip.getHeight();

        //using formula s = c*r^gamma   
        for (int v = 0; v < h; v++) {
            for (int u = 0; u < w; u++) {
            	int r = ip.getPixel(u, v);
            	//scale r to be in the range [0, 1]
            	double aa = (double) r / maxPixelValue;
            	// apply gamma function to scaled pixel value
            	double bb = Math.pow(aa, GAMMA);
            	// scale value back to [0, 255]
            	int s = (int) Math.round(c * bb * maxPixelValue);
            	ip.putPixel(u, v, s);
            }
        }
	}
}

