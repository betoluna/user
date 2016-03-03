import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.io.Opener;// if using ImagePlus openImage(java.lang.String path)
import ij.WindowManager;
import java.util.ArrayList;

/**
 * CS/ECE545 - WPI, Spring 2016
 * Name: Norberto Luna-Cano
 * Email: nlunacano@wpi.edu
 * Date: 3/4/16
 * Overview Description of Plugin:
 * Using a histogram specification for adjusting an image to a
 * computed average reference histogram from a set of images.
 */

/**
 * list is an arrayList of histograms of opened images
 * to be used as reference for the target image once the
 * histograms are averaged. The assumption is that at leas
 * two images are open.
 */
public class Histogram_spec_averages implements PlugInFilter {
	protected ImagePlus image;
    ArrayList<int[]> list = new ArrayList<int[]>();

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
     * Description: one or more image histograms are averaged and the computed
     * averaged histogram is used as reference to do histogram matching.
     * @param ip is the current slice (typically, plugins use
     * the ImagePlus set above instead).
     * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
     */
	@Override
	public void run(ImageProcessor ip) {
        /* alternative to work with several images using the Opener class */
        // Opener op = new Opener();
        // ImagePlus impl1 = op.openImage("/Users/beto/Google Drive/classes/DIP545/images/Baboon.jpg"); 
        // ImagePlus impl2 = op.openImage("/Users/beto/Google Drive/classes/DIP545/images/Peppers.jpg"); 
        // ImageProcessor ip1 = impl1.getProcessor();
        // ImageProcessor ip2 = impl2.getProcessor();
        
        int[] windowList = WindowManager.getIDList(); 
        ImagePlus im;

        //if we have at least two images opened
        if(windowList.length > 1) {
            //make windowList[windowList.length - 1] (the last one opened) your target image 
            //and average (add to list) only images from i = 0 to windowList.length - 2
            for (int i = 0; i < windowList.length - 1; i++) {
                im = WindowManager.getImage(windowList[i]);

                //run ImageJ in debug mode: Edit->Options->Misc->Debug
                System.out.println("i: " + i + " id: " + windowList[i] + " name: " + im.getTitle());

                ip = im.getProcessor();
                int[] hist = ip.getHistogram();
                list.add(hist);
                
            }
            
            //now process the target image
            im = WindowManager.getImage(windowList[windowList.length - 1]);
            ip = im.getProcessor();

            //in debug mode
            System.out.println("Now processing: " + im.getTitle() + " ....");

            int[] F = matchHistograms(ip.getHistogram(), list);
            ip.applyTable(F);
        }
        
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
     * assume all histogram array references in listOfHistograms
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

