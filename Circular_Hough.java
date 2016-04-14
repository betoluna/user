import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.util.*;
import ij.gui.*;

/**
 * CS/ECE545 - WPI, Spring 2016
 * Name: Norberto Luna-Cano
 * Email: nlunacano@wpi.edu
 * Date: April 13, 2016
 * Overview Description of Plugin:
 * For coins.png set radius bet 25 - 30
 * For eyes1 it finds right pupil with radius bet 7  - 8, left pupil bet 3 - 5 but separate runs and only 1 circle
 *
 */

/**
 * Burger and Burge Exercise 9.4 (page 171) (30 points): Implement as an ImageJ plugin,
 * called Circular_Hough, the Hough Transform for finding circles with varying radii.
 * Make use of a fast algorithm for generating circles, such as described in sec 9.4,
 * in the accumulator Array. Suitable images to test your algorithm will be posted on
 * the class site. Test at least one image with your submission. Also explain briefly how
 * your algorithm works in your comments in the code.
 *
 * If you class makes use of any predefined variable members, please name them
 * appropriately and provide a short description comment on how it is used or
 * modified and the implications of modifying the variable.
 */
public class Circular_Hough implements PlugInFilter {
    protected ImagePlus image;

    public int width;
    public int height;
    public int radiiSpan;
    byte byteArrayImage[];
    double ACCUMULATOR[][][];
    int lookUpTable[][][];
    ArrayList<Circle> circleList;
    public int minRad;
    public int maxRad;
    public int step;
    public int numCircles;// num of circles to find
    
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
        ip = imageCopy.getProcessor();

        width = ip2.getWidth();
        height = ip2.getHeight();
        byteArrayImage = (byte[]) ip2.getPixels();//array of image pixels

        if (readParameters()) {

            //create a look up table 
            //increment theta each time by some proportion of 360 degrees
            int thetaIncrement = Math.round(minRad * 45);
            lookUpTable = new int[2][thetaIncrement][radiiSpan];
            int tableLen = 0;
            for (int r = minRad; r <= maxRad; r = r + step) {
                tableLen = 0;
                for (int numerator = 0; numerator < thetaIncrement; numerator++) {
                    double angle = (2 * Math.PI * (double) numerator) / (double) thetaIncrement;
                    int radiusIndex = (r - minRad) / step;
                    int rCosTheta = (int) Math.round((double) r * Math.cos(angle));
                    int rSinTheta = (int) Math.round((double) r * Math.sin(angle));
                    if ((rCosTheta != lookUpTable[0][tableLen][radiusIndex]) && (rSinTheta != lookUpTable[1][tableLen][radiusIndex]) || (tableLen == 0)) {
                        lookUpTable[0][tableLen][radiusIndex] = rCosTheta;
                        lookUpTable[1][tableLen][radiusIndex] = rSinTheta;
                        tableLen++;
                    }
                }
            }

            //create and fill Hough Accumulator 
            ACCUMULATOR = new double[width][height][radiiSpan];
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
                                    ACCUMULATOR[a][b][radiusIndex] += 1;//increase accumulator at this index
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
                            if (ACCUMULATOR[x][y][radiusIndex] > counterMax) {
                                counterMax = ACCUMULATOR[x][y][radiusIndex];
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
                            if (Math.pow(j - maxX, 2.0) + Math.pow(ii - maxY, 2.0) < rOver2Squared) {
                                ACCUMULATOR[j][ii][radiusIndex] = 0.0;//replace value with zero
                            }
                        }
                    }
                }
            }

            // draw edges in image with intensity value 127 
            int edgePos = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (byteArrayImage[x + width * y] != 0) {
                        edges[edgePos] = 127;//max positive value for a byte in two's complement 
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

                //plotCircle(x, y, r, edgeImProc);
                plotCircle(x, y, r, ip);//plot white circles on original 8-bit image

            }

            //new ImagePlus(numCircles + " Circles Found", edgeImProc).show();
            new ImagePlus(numCircles + " Circles Found", ip).show();


        }
    }//end run
   
   	/**
	* Given center coordiantes and radius, draw a circle.
	* Own implementation of "A Fast Bresenham Type Algorithm For Drawing Circles"
	* http://web.engr.oregonstate.edu/~sllu/bcircle.pdf
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

    boolean readParameters() {
        GenericDialog gd = new GenericDialog("Hough Parameters", IJ.getInstance());
        gd.addNumericField("Minimum radius (in pixels) :", 10, 0);
        gd.addNumericField("Maximum radius (in pixels)", 20, 0);
        gd.addNumericField("Increment radius (in pixels) :", 2, 0);
        gd.addNumericField("Number of Circles (NC): (enter 0 if using threshold)", 10, 0);

        gd.showDialog();

        if (gd.wasCanceled()) {
            return (false);
        }

        minRad = (int) gd.getNextNumber();
        maxRad = (int) gd.getNextNumber();
        step = (int) gd.getNextNumber();
        radiiSpan = ((maxRad - minRad) / step) + 1;
        numCircles = (int) gd.getNextNumber();

        return true;
    }
}


