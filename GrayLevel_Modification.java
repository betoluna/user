import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class GrayLevel_Modification implements PlugInFilter {
    public int setup(String arg, ImagePlus im) {
        return DOES_8G; // this plugin only works on 8-bit grayscale images
    }

    public void run(ImageProcessor ip) {
        int w = ip.getWidth();
        int h = ip.getHeight();

         
         for (int v = 0; v < h; v++) {
            for (int u = 0; u < w; u++) {
            	int p = ip.getPixel(u, v);
            	int s = (int)(16 * Math.sqrt(p));
            	ip.putPixel(u, v, s);

            }   
        }
    }
}