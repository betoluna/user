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
 * 
 * 
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
    public int depth; 
	int lut[][][]; // LookUp Table for rsin e rcos values
	ArrayList<Circle> circleList;
    public int radiusMin;  
    public int radiusMax;  
    public int radiusInc;  
    public int maxCircles; 
    byte imageValues[]; 
    double houghValues[][][]; 

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
		 * The current return value accepts all gray-scale
		 * images (if you access the pixels with ip.getf(x, y)
		 * anyway, that works quite well.
		 *
		 * It could also be DOES_ALL; you can add "| NO_CHANGES"
		 * to indicate that the current image will not be
		 * changed by this plugin.
		 *
		 * Beware of DOES_STACKS: this will call the run()
		 * method with all slices of the current image
		 * (channels, z-slices and frames, all). Most likely
		 * not what you want.
		 */
		return DOES_ALL;
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

		//preprocess the image (assume taking an rgb image from the website)
        ImageConverter converter = new ImageConverter(image);
        converter.convertToGray8();//convert to 8-bit grayscale
        ImageProcessor ip2 = image.getProcessor();
        ip2.findEdges();
        ip2.threshold(127);// could try ip.autoThreshold();

        width = ip2.getWidth();
        height = ip2.getHeight();
        System.out.println("width: " + width + ", height: " + height);
        imageValues = (byte[]) ip2.getPixels();

        if (readParameters()) { 
	        
	        //create a look up table ******************************
	        int incDen = Math.round(10 * radiusMin);  // increment denominator
	        lut = new int[2][incDen][depth];
	        int lenLut = 0;
	        for (int radius = radiusMin; radius <= radiusMax; radius = radius + radiusInc) {
	            lenLut = 0;
	            for (int incNun = 0; incNun < incDen; incNun++) {
	                double angle = (2 * Math.PI * (double) incNun) / (double) incDen;
	                int indexR = (radius - radiusMin) / radiusInc;
	                int rcos = (int) Math.round((double) radius * Math.cos(angle));
	                int rsin = (int) Math.round((double) radius * Math.sin(angle));
	                if ((lenLut == 0) || (rcos != lut[0][lenLut][indexR]) && (rsin != lut[1][lenLut][indexR])) {
	                    lut[0][lenLut][indexR] = rcos;
	                    lut[1][lenLut][indexR] = rsin;
	                    lenLut++;
	                }
	            }
	        }
	        //System.out.println("lenLut: " + lutSize);
	        //end look up table **********************************

	        //Build the Hough Transform **************************
	        houghValues = new double[width][height][depth];
	        for (int y = 1; y < height - 1; y++) {
	            for (int x = 1; x < width - 1; x++) {
	                for (int radius = radiusMin; radius <= radiusMax; radius = radius + radiusInc) {
	                    //if potential edge pixel 
	                    if (ip2.get(x, y) == 255) {
	                        int indexR = (radius - radiusMin) / radiusInc;
	                        for (int i = 0; i < lenLut; i++) {
	                            int a = x + lut[1][i][indexR];
	                            int b = y + lut[0][i][indexR];
	                            if ((b >= 0) && (b < height) && (a >= 0) && (a < width)) {
	                                houghValues[a][b][indexR] += 1;
	                            }
	                        }
	                    }
	                }
	            }
	        }
	        //end building the Hough *****************************

	        // Create image View for Marked Circles.
	        ImageProcessor circlesip = new ByteProcessor(width, height);
	        byte[] circlespixels = (byte[]) circlesip.getPixels();

	        // getCenterPoints ***********************************
	        int xMax = 0;
	        int yMax = 0;
	        int rMax = 0;

	        for (int c = 0; c < maxCircles; c++) {
	            double counterMax = -1;
	            for (int radius = radiusMin; radius <= radiusMax; radius = radius + radiusInc) {
	                int indexR = (radius - radiusMin) / radiusInc;
	                for (int y = 0; y < height; y++) {
	                    for (int x = 0; x < width; x++) {
	                        if (houghValues[x][y][indexR] > counterMax) {
	                            counterMax = houghValues[x][y][indexR];
	                            xMax = x;
	                            yMax = y;
	                            rMax = radius;
	                        }
	                    }
	                }
	            }
	            System.out.println("x: " + xMax + ", y: " + yMax + ", rMax: " + rMax);
	            circleList.add(new Circle(xMax, yMax, rMax));
       
	            // clean neighbors ******************************
	            clearNeighbours(xMax, yMax, rMax);
	            // clean neighbors ******************************
	        }
	        // End getCenterPoints *******************************

			// draw circles now ********************************* 
	        int roiaddr = 0;
	        for (int y = 0; y < height; y++) {
	            for (int x = 0; x < width; x++) {
	                // Copy;
	                circlespixels[roiaddr] = imageValues[x + width * y];
	                // Saturate
	                if (circlespixels[roiaddr] != 0)
	                    circlespixels[roiaddr] = 127;//max positive value in two's complement for a byte
	                else
	                    circlespixels[roiaddr] = 0;
	                roiaddr++;
	            }
	        }
	      
	        for (Circle circle : circleList) {
	            int x = circle.getX();
	            int y = circle.getY();
	            for (int k = -10; k <= 10; ++k) {// put a cross at center of relevant circle.
	                if (x < width && x >= 0 && (y + k) < height && (y + k) >= 0)
	                    circlespixels[(y + k) * width + x] = -1;//all 1's in the byte
	                if (x < width && x >= 0 && (y + k) < height && (y + k) >= 0)
	                    circlespixels[y * width + x + k] = -1;
	            }
	        }
	        // End draw circles ********************************* 

	        for (Circle circle : circleList) {
	            System.out.println(circle.getX() + " " + circle.getY() + " " + circle.getRadius());
	        }

	        //new ImagePlus("Hough Space [r="+radiusMin+"]", newip).show(); // Shows only the hough space for the minimun radius
	        new ImagePlus(maxCircles + " Circles Found", circlesip).show();
       
        
		}//end readparameters
	}//end run

    /**
     * Clear, from the Hough Space, all the counter that are near (radius/2) a previously found circle C.
     *
     * @param x The x coordinate of the circle C found.
     * @param x The y coordinate of the circle C found.
     * @param x The radius of the circle C found.
     */
    private void clearNeighbours(int x, int y, int radius) {
        // The following code just clean the points around the center of the circle found.
        double halfRadius = radius / 2.0;
        double halfSquared = halfRadius * halfRadius;

        int y1 = (int) Math.floor((double) y - halfRadius);
        int y2 = (int) Math.ceil((double) y + halfRadius) + 1;
        int x1 = (int) Math.floor((double) x - halfRadius);
        int x2 = (int) Math.ceil((double) x + halfRadius) + 1;

        if (y1 < 0)
            y1 = 0;
        if (y2 > height)
            y2 = height;
        if (x1 < 0)
            x1 = 0;
        if (x2 > width)
            x2 = width;

        for (int r = radiusMin; r <= radiusMax; r = r + radiusInc) {
            int indexR = (r - radiusMin) / radiusInc;
            for (int i = y1; i < y2; i++) {
                for (int j = x1; j < x2; j++) {
                    if (Math.pow(j - x, 2.0) + Math.pow(i - y, 2.0) < halfSquared) {
                        houghValues[j][i][indexR] = 0.0;
                    }
                }
            }
        }
    }

    class Circle {
        private int x;
        private int y;
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

        radiusMin = (int) gd.getNextNumber();
        radiusMax = (int) gd.getNextNumber();
        radiusInc = (int) gd.getNextNumber();
        depth = ((radiusMax - radiusMin) / radiusInc) + 1;
        maxCircles = (int) gd.getNextNumber();
        
        return true;
    }
}


