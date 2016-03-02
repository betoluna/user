import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.Opener;
import java.util.ArrayList;

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
public class Histogram_spec_averages implements PlugInFilter {
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
        Opener op = new Opener();
        op.open();
        ImagePlus im = IJ.getImage(); 
        ImageProcessor ipA = im.getProcessor();
        // the target image IA to be modified
        //ImageProcessor ipA = "/Users/beto/Desktop/runaway.jpg";
        // ImageProcessor ipR1 =
        // ImageProcessor ipR2 =
	}

	/**
     * compute the mapping function fhs() to be applied to image IA
     * @param hA histogram hA of target image IA
     * @param histograms list of reference image histograms to be averaged
     * @return the mapping function fhs()
     */
	int[] matchHistograms(int[] hA, ArrayList<int[]> histograms) {
        int K = hA.length;
        int[] hR = computeAvgHistogram(histograms);
        double[] PA = Cdf(hA);
        double[] PR = Cdf(hR);
        int[] F = new int[K];

        // compute mapping function fhs ()
        for (int a = 0; a < K; a++) {
            int j = K - 1;
            do {
                F[a] = j;
                j--;
            } while (j >= 0 && PA[a] <= PR[j]);
        }

        return F;
    }

	/**
     * assume all histogram array references in arrays
     * are of same length
     * @param listOfHistograms the ArrayList of
     * histograms from the set of images
     * @return the result average array histogram
     */
    int[] computeAvgHistogram(ArrayList<int[]> listOfHistograms) {
        int len = listOfHistograms.get(0).length;
        int K = listOfHistograms.size();
        int[] avgHistogramRef = new int[len];

        //assume at least 2 arrays
        for (int i = 0; i < len; i++) {

            //int sum = 0;
            double sum = 0;
            for (int j = 0; j < K; j++) {
                int currEntry = listOfHistograms.get(j)[i];
                sum += currEntry;
            }

            //int avg = sum / K;
            int avg = (int)Math.round(sum / K);
            avgHistogramRef[i] = avg;
        }

        return avgHistogramRef;
    }

    /**
     * compute the cumulative distribution function (cdf)
     * for a given histogram.
     * @param h the original histogram
     * @return the computed cdf histogram
     */
    double[] Cdf(int[] h) {
        // returns the cumulative distribution function for histogram
        int K = h.length;
        int n = 0;
        for (int i = 0; i < K; i++) {
            n += h[i];
        }

        double[] P = new double[K];
        int c = h[0];
        P[0] = (double) c / n;
        for (int i = 1; i < K; i++) {
            c += h[i];
            P[i] = (double) c / n;
        }

        return P;
    }
}

