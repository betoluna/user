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
 * perform 2 types of flood fill algorithms to fill in each circle found in the image. 
 * Metohds used: recursive, and coherence.
 */

/**
 * Note, for simplicity, not for design, most code is duplicated from Circular_Hough.java 
 * as permitted in the problem specification. 
 */
public class Circle_Flood implements PlugInFilter {
	protected ImagePlus image;

    Stack<Coordinate> stack;
    ArrayList<Circle> circleList;

	//determine whether to use coherence or recursion floodFill
    private boolean useCoherence = true;
    
    private int numCircles;// num of circles to find   
    private int minRad;
    private int maxRad;
    
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
     * class to represent the rightmost
     * coordinate for the coherence method
     */
    static class Coordinate {
        private int x;
        private int y;
        private int leftX;

        public Coordinate(int x, int y, int leftX) {
            this.x = x;
            this.y = y;
            this.leftX = leftX;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getleftX() { return leftX; }
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
        stack = new Stack<Coordinate>();

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

        int width = ip2.getWidth();
        int height = ip2.getHeight();
        byte byteArrayImage[] = (byte[]) ip2.getPixels();//array of image pixels

        if (readParameters()) {

            //create a look up table 
            //increment theta each time by some proportion of 360 degrees
            int thetaIncrement = Math.round(minRad * 45);
            int step = 2;//radius increment  
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

                if (useCoherence) {
                    coherenceFloodFill(x, y, r, edgeImProc);
                } else {
                    recursiveFloodFill(edgeImProc, x, y, r, x, y, 255);
                }     
            }

            new ImagePlus("Circular_Hough", edgeImProc).show();
        }
    }//end run

    /**
    * @param CX and CY the circle center (seed)
    * @param R the radius of the circle
    * @param u and v the new possible point coordinates in circle
    * @param proc the omage processor
    * @param label 
    */
    public void recursiveFloodFill(ImageProcessor proc, int CX, int CY, int R, int u, int v, int label) {   

        //compute if u and v are within circle boundaries by the distance formula 
        int d = (int) Math.sqrt( (double) ((u - CX) * (u - CX) + (v - CY) * (v - CY)) );

        if (d > R) return;       
       
        proc.set(u, v, label);

        recursiveFloodFill(proc, CX, CY, R, u + 1, v, label);
        recursiveFloodFill(proc, CX, CY, R, u, v + 1, label);
        recursiveFloodFill(proc, CX, CY, R, u, v - 1, label);
        recursiveFloodFill(proc, CX, CY, R, u - 1, v, label);
    }

    /**
    * Fill circle by coherence method. CX and CY are the center coordinates, R the radius
    */
    public void coherenceFloodFill(int CX, int CY, int R, ImageProcessor proc) {
        int X = R;
        int Y = 0;
        int xChange = 1 - 2*R;
        int yChange = 1;
        int radiusError = 0;

        while (X >= Y) {
            stack.push(new Coordinate(CX + X, CY + Y, CX - X));
            stack.push(new Coordinate(CX + X, CY - Y, CX - X));
            stack.push(new Coordinate(CX + Y, CY + X, CX - Y));
            stack.push(new Coordinate(CX + Y, CY - X, CX - Y));

            Y++;
            radiusError = radiusError + yChange;
            yChange = yChange + 2;

            if (2 * radiusError + xChange > 0) {
                X--;
                radiusError = radiusError + xChange;
                xChange = xChange + 2;
            }
        }

        while (!stack.empty()) {
            //Pop stack to provide next seed, fill in run defined by seed
            Coordinate c = stack.pop();
            int v = c.getY();
            int leftX = c.getleftX();
            for (int u = c.getX(); u >= leftX; u--) {
                proc.set(u, v, 255);
            }
        }          
    }

    boolean readParameters() {
        GenericDialog gd = new GenericDialog("Hough Parameters", IJ.getInstance());
        gd.addNumericField("Minimum radius (in pixels) :", 10, 0);
        gd.addNumericField("Maximum radius (in pixels)", 20, 0);
        gd.addNumericField("Number of Circles (NC): (enter 0 if using threshold)", 10, 0);

        gd.showDialog();

        if (gd.wasCanceled()) {
            return (false);
        }

        minRad = (int) gd.getNextNumber();
        maxRad = (int) gd.getNextNumber();
        numCircles = (int) gd.getNextNumber();

        return true;
    }

    
}