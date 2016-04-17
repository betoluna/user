import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.*;
import ij.io.Opener;
import java.util.*;
import ij.process.Blitter;

/* test code for first idea about solving the problem
		Opener opener = new Opener();
		Circular_Hough hough = new Circular_Hough();

		ImagePlus iplus;
		for (int i = 3; i <= 9; i++) {
			for (int j = i - 2; j < i; j++) {
				iplus = opener.openImage("/Users/beto/Downloads/spine.jpg");
				ip = iplus.getProcessor();
				ip.threshold(127);
				new ImagePlus("j: " + j + ", i: " + i, ip).show();
			}
		}
		*/

/**
 * CS/ECE545 - WPI, Spring 2016
 * Name: Norberto Luna-Cano
 * Email: nlunacano@wpi.edu
 * Date: 
 * Overview Description of Plugin:
 * This class looks at measuring the size of pupils in a picture of a face.
 * It includes for simplicity all the code from Circular_Hough.java to find
 * circles (pupils) first, and then uses morphological dilation to confirm 
 * the results of the sizes of pupils found by Hough. Measure of pupil size
 * is based on the radius of the pupil detected, a value that has been previously
 * found by Circular_Hough.
 * For 'Eyes Data set 1' image, it finds right pupil by setting radii bet 7  - 8, and left pupil bet 3 - 5 
 * pixels but it does so in separate runs and setting numCircles variable to 1 (please also see README).
 * Next I try to use morphology by using dilation() (see method below) as implemented in the
 * book to confirm the size of the pupils.
 */
public class Pupil_Measure implements PlugInFilter {
	protected ImagePlus image;

	private ArrayList<Circle> circleList;

    private int numCircles = 1;
    private int minRad = 3;
    private int maxRad = 5;

    //the 2 dimensional disk shaped structuring element
    private int[][] H = {
    	{0,1,1,1,0},
    	{1,1,1,1,1},
    	{1,1,2,1,1},
    	{1,1,1,1,1},
    	{0,1,1,1,0}
    };

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
		 * take any image (including RGB images from the class website)
		 */
		return DOES_ALL;
	}

	/**
	* does morphological dilation, assumes hotspot is at center of H
	* @param I the binary image with values 0 and 255
	* @param H the 2 dimensional structuring element
	*/
	public void dilate(ImageProcessor I, int[][] H){
		//assume that the hot spot of H is at its center (ic,jc):
		int ic = (H[0].length-1)/2;
		int jc = (H.length-1)/2;

		//create a temporary (empty) image:
		ImageProcessor tmp = I.createProcessor(I.getWidth(),I.getHeight());

		for (int j=0; j<H.length; j++){
			for (int i=0; i<H[j].length; i++){
				if (H[j][i] > 0) { // this pixel is set 
				//copy image into position (i-ic,j-jc): 
				tmp.copyBits(I,i-ic,j-jc,Blitter.MAX);
				} 
			}
		}

		//copy the temporary result back to original image
  		I.copyBits(tmp,0,0,Blitter.COPY);
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

        circleList = new ArrayList<Circle>();
        ImagePlus imageCopy = image.duplicate();

        //preprocess the image (assume taking an rgb image from the website)
        ImageConverter orig = new ImageConverter(image);
        orig.convertToGray8();//convert to 8-bit grayscale
        ImageProcessor ip2 = image.getProcessor();
        ip2.smooth();
        ip2.findEdges();
        ip2.threshold(127);

        //preprocess a copy
        ImageConverter copy = new ImageConverter(imageCopy);
        copy.convertToGray8();
        ip = imageCopy.getProcessor();//ip now gets an 8-bit grayscale copy  

        int width = ip2.getWidth();
        int height = ip2.getHeight();
        byte byteArrayImage[] = (byte[]) ip2.getPixels();//array of image pixels

        //create a look up table 
        //increment theta each time by some proportion of 360 degrees
        int thetaIncrement = Math.round(minRad * 45);
        int step = 2;// radius increment
        int radiiSpan = ((maxRad - minRad) / step) + 1;
        int lookUpTable[][][] = new int[2][thetaIncrement][radiiSpan];
        int tableLen = 0;
        for (int r = minRad; r <= maxRad; r = r + step) {
            tableLen = 0;
            for (int numerator = 0; numerator < thetaIncrement; numerator++) {
                double theta = (2 * Math.PI * (double) numerator) / (double) thetaIncrement;
                int radiusIndex = (r - minRad) / step;
                int rCosTheta = (int) Math.round((double) r * Math.cos(theta));
                int rSinTheta = (int) Math.round((double) r * Math.sin(theta));
                if ((rCosTheta != lookUpTable[0][tableLen][radiusIndex]) && (rSinTheta != lookUpTable[1][tableLen][radiusIndex]) || (tableLen == 0)) {
                    lookUpTable[0][tableLen][radiusIndex] = rCosTheta;
                    lookUpTable[1][tableLen][radiusIndex] = rSinTheta;
                    tableLen++;
                }
            }
        }

        //create and fill Hough Accumulator 
        double ACC[][][] = new double[width][height][radiiSpan];
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                for (int r = minRad; r <= maxRad; r = r + step) {
                    //if potential edge pixel
                    if (ip2.get(x, y) == 255) {
                        int radiusIndex = (r - minRad) / step;
                        for (int i = 0; i < tableLen; i++) {
                            int a = x + lookUpTable[1][i][radiusIndex];
                            int b = y + lookUpTable[0][i][radiusIndex];
                            if ((b >= 0) && (b < height) && (a >= 0) && (a < width)) {
                                ACC[a][b][radiusIndex] += 1;//increase accumulator at this index
                            }
                        }
                    }
                }
            }
        }

        ImageProcessor edgeImProc = new ByteProcessor(width, height);
        byte[] edges = (byte[]) edgeImProc.getPixels();

        // compute circle origin coordiantes and radius
        int maxX = 0, maxY = 0, maxR = 0;
        for (int i = 0; i < numCircles; i++) {
            double counterMax = -1;
            for (int r = minRad; r <= maxRad; r = r + step) {
                int radiusIndex = (r - minRad) / step;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        if (ACC[x][y][radiusIndex] > counterMax) {
                            counterMax = ACC[x][y][radiusIndex];
                            maxX = x;
                            maxY = y;
                            maxR = r;
                        }
                    }
                }
            }
            circleList.add(new Circle(maxX, maxY, maxR));// add this circle

            //remove unwanted neighbor values (less than maxima) from the accumulator space
            //from center maxima to a distance of approx maximum radius divided by 2  
            double rOver2 = maxR / 2.0;
            int x1, x2, y1, y2;            
            if ((x1 = (int) Math.floor((double) maxX - rOver2)) < 0)
                x1 = 0;
            if ((x2 = (int) Math.ceil((double) maxX + rOver2) + 1) > width)
                x2 = width;
            if ((y1 = (int) Math.floor((double) maxY - rOver2)) < 0)
                y1 = 0;
            if ((y2 = (int) Math.ceil((double) maxY + rOver2) + 1) > height)
                y2 = height;

            double rOver2Squared = rOver2 * rOver2;
            for (int r = minRad; r <= maxRad; r = r + step) {
                int radiusIndex = (r - minRad) / step;
                for (int ii = y1; ii < y2; ii++) {
                    for (int j = x1; j < x2; j++) {
                        if (rOver2Squared > (j - maxX) * (j - maxX) + (ii - maxY) * (ii - maxY)) {
                            ACC[j][ii][radiusIndex] = 0;//replace value with zero
                        }
                    }
                }
            }
        }

        // draw edges in image with intensity value 127 (max positive value for a byte in two's complement)
        int edgePos = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (byteArrayImage[x + width * y] != 0) {
                    edges[edgePos] = 127; 
                } else {
                    edges[edgePos] = 0;
                }

                edgePos++;
            }
        }

        for (Circle circle : circleList) {
            int x = circle.getX();
            int y = circle.getY();
            int r = circle.getRadius();

            plotCircle(x, y, r, ip);//plot white circles on 8-bit copy of original image

        }

        /*********************************************
        * Attempt to use dilation
        * Note: comment out the the call to dilate() to
        * see the pupil found by Circular_Hough code with
        * parameters set as explained above in this file
        * as well as in the README file. 
        * Unfortunately dilation needs to be further improved
        * to produce desired results. For future work.
        *********************************************/
        dilate(ip, H);

        new ImagePlus("Pupil Measure", ip).show();

		
	}

	// class for circle objects
    class Circle {
        private int x;//center x coordinate
        private int y;//center y coordinate
        private int radius;

        public Circle(int x, int y, int radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getRadius() { return radius; }

        public void setX(int x) { this.x = x; }
        public void setY(int y) { this.y = y; }
        public void setRadius(int r) { radius = r; }

    }

    /**
    * Given center coordinates and radius, draw a circle.
    * Own implementation of "A Fast Bresenham Type Algorithm For Drawing Circles"
    * see paper at: http://web.engr.oregonstate.edu/~sllu/bcircle.pdf
    * @param CX the center x coordinate
    * @param CY the center y coordinate
    * @param R the circle's radius
    */
    public void plotCircle(int CX, int CY, int R, ImageProcessor proc) {
        int X = R;
        int Y = 0;
        int xChange = 1 - 2*R;
        int yChange = 1;
        int radiusError = 0;

        while (X >= Y) {
            proc.set(CX + X, CY + Y, 255);//use faster set() instead of putPixel()
            proc.set(CX - X, CY + Y, 255);
            proc.set(CX - X, CY - Y, 255);
            proc.set(CX + X, CY - Y, 255);
            proc.set(CX + Y, CY + X, 255);
            proc.set(CX - Y, CY + X, 255);
            proc.set(CX - Y, CY - X, 255);
            proc.set(CX + Y, CY - X, 255);

            Y++;
            radiusError = radiusError + yChange;
            yChange = yChange + 2;

            if (2 * radiusError + xChange > 0) {
                X--;
                radiusError = radiusError + xChange;
                xChange = xChange + 2;
            }
        }
    }
}

