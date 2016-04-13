import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.util.*;
import ij.gui.*;


public class Preprocess_Filter implements PlugInFilter {
	protected ImagePlus image;

	
	@Override
	public int setup(String arg, ImagePlus image) {
		this.image = image;
		
		return DOES_ALL;
	}


	@Override
	public void run(ImageProcessor ip) {
		//preprocess the image (assume taking an rgb image from the website)
        ImageConverter orig = new ImageConverter(image);
        orig.convertToGray8();//convert to 8-bit grayscale
        ImageProcessor ip2 = image.getProcessor();
        // ip2.smooth();
        // ip2.findEdges();
        // ip2.threshold(127);

	}
}