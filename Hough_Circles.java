import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.util.*;
import java.awt.*;

import ij.gui.*;

/**
 * This ImageJ plugin shows the Hough Transform Space and search for
 * circles in a binary image. The image must have been passed through
 * an edge detection module and have edges marked in white (background
 * must be in black).
 */
public class Hough_Circles implements PlugInFilter {
    protected ImagePlus image;
    ArrayList<Circle> circleList;

    public int radiusMin;  // Find circles with radius grater or equal radiusMin
    public int radiusMax;  // Find circles with radius less or equal radiusMax
    public int radiusInc;  // Increment used to go from radiusMin to radiusMax
    public int maxCircles; // Numbers of circles to be found
    public int threshold = -1; // An alternative to maxCircles. All circles with
    // a value in the hough space greater then threshold are marked. Higher thresholds
    // results in fewer circles.
    byte imageValues[]; // Raw image (returned by ip.getPixels())
    double houghValues[][][]; // Hough Space Values
    public int width; // Hough Space width (depends on image width)
    public int height;  // Hough Space heigh (depends on image height)
    public int depth;  // Hough Space depth (depends on radius interval)
    public int offset; // Image Width
    Point centerPoint[]; // Center Points of the Circles Found.
    //private int vectorMaxSize = 500;
    boolean useThreshold = false;
    int lut[][][]; // LookUp Table for rsin e rcos values


    @Override
    public int setup(String arg, ImagePlus image) {
        this.image = image;

        //return DOES_8G + DOES_STACKS + SUPPORTS_MASKING;
        return DOES_ALL;//accept all types of images including rgb, or DOES_RGB;
    }

    @Override
    public void run(ImageProcessor ip) {
        circleList = new ArrayList<Circle>();
        
        //preprocess the image (assume taking an rgb image from the website)
        ImageConverter converter = new ImageConverter(image);
        converter.convertToGray8();//convert to 8-bit grayscale
        ImageProcessor ip2 = image.getProcessor();
        ip2.findEdges();
        ip2.threshold(127);// also ip.autoThreshold();
        
        imageValues = (byte[]) ip2.getPixels();

        width = ip2.getWidth();
        height = ip2.getHeight();
        System.out.println("width: " + width + ", height: " + height);

        if (readParameters()) { // Show a Dialog Window for user input of
            // radius and maxCircles.

            houghTransform(ip2);

            // Create image View for Hough Transform.
            // ImageProcessor newip = new ByteProcessor(width, height);
            // byte[] newpixels = (byte[]) newip.getPixels();
            // createHoughPixels(newpixels);

            // Create image View for Marked Circles.
            ImageProcessor circlesip = new ByteProcessor(width, height);
            byte[] circlespixels = (byte[]) circlesip.getPixels();

            // Mark the center of the found circles in a new image
            getCenterPoints(maxCircles);
            drawCircles(circlespixels);

            for (Circle circle : circleList) {
                System.out.println(circle.getX() + " " + circle.getY() + " " + circle.getRadius());
            }

            //new ImagePlus("Hough Space [r="+radiusMin+"]", newip).show(); // Shows only the hough space for the minimun radius
            new ImagePlus(maxCircles + " Circles Found", circlesip).show();
        }
    }

    /**
     * The parametric equation for a circle centered at (a,b) with
     * radius r is:
     * a = x - r*cos(theta)
     * b = y - r*sin(theta)
     * <p>
     * In order to speed calculations, we first construct a lookup
     * table (lut) containing the rcos(theta) and rsin(theta) values, for
     * theta varying from 0 to 2*PI with increments equal to
     * 1/8*r. As of now, a fixed increment is being used for all
     * different radius (1/8*radiusMin). This should be corrected in
     * the future.
     * <p>
     * Return value = Number of angles for each radius
     */
    private int buildLookUpTable() {

        int i = 0;
        int incDen = Math.round(10 * radiusMin);  // increment denominator

        lut = new int[2][incDen][depth];

        for (int radius = radiusMin; radius <= radiusMax; radius = radius + radiusInc) {
            i = 0;
            for (int incNun = 0; incNun < incDen; incNun++) {
                double angle = (2 * Math.PI * (double) incNun) / (double) incDen;
                int indexR = (radius - radiusMin) / radiusInc;
                int rcos = (int) Math.round((double) radius * Math.cos(angle));
                int rsin = (int) Math.round((double) radius * Math.sin(angle));
                if ((i == 0) || (rcos != lut[0][i][indexR]) && (rsin != lut[1][i][indexR])) {
                    lut[0][i][indexR] = rcos;
                    lut[1][i][indexR] = rsin;
                    i++;
                }
            }
        }

        return i;
    }

    private void houghTransform(ImageProcessor iproc) {

        int lutSize = buildLookUpTable();
        //System.out.println("lutSize: " + lutSize);

        houghValues = new double[width][height][depth];

        int w = width - 1;
        int h = height - 1;

        for (int y = 1; y < h; y++) {
            for (int x = 1; x < w; x++) {
                for (int radius = radiusMin; radius <= radiusMax; radius = radius + radiusInc) {
                    //if (imageValues[(x) + (y) * offset] != 0) {// Edge pixel found
                    if (iproc.get(x, y) == 255) {// potential edge pixel 
                        int indexR = (radius - radiusMin) / radiusInc;
                        for (int i = 0; i < lutSize; i++) {

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

    }

    // Draw the circles found in the original image.
    public void drawCircles(byte[] circlespixels) {

        // Copy original input pixels into output
        // circle location display image and
        // combine with saturation at 100
        int roiaddr = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Copy;
                circlespixels[roiaddr] = imageValues[x + width * y];
                //circlespixels[roiaddr] = imageValues[x];
                // Saturate
                if (circlespixels[roiaddr] != 0)
                    circlespixels[roiaddr] = 127;//max positive value in two's complement for a byte
                else
                    circlespixels[roiaddr] = 0;
                roiaddr++;
            }
        }
        
        for (int l = 0; l < maxCircles; l++) {
            int x = centerPoint[l].x;
            int y = centerPoint[l].y;
            for (int k = -10; k <= 10; ++k) {// put a cross at center of relevant circle.
                if (!outOfBounds(x, y + k))
                    circlespixels[(y + k) * width + x] = -1;//all 1's in the byte
                if (!outOfBounds(x + k, y))
                    circlespixels[y * width + x + k] = -1;
            }
        }
    }

    private boolean outOfBounds(int x, int y) {
        if (x >= width)
            return (true);
        if (x <= 0)
            return (true);
        if (y >= height)
            return (true);
        if (y <= 0)
            return (true);
        return (false);
    }

    /**
     * Search for a fixed number of circles.
     *
     * @param maxCircles The number of circles that should be found.
     */
    private void getCenterPoints(int maxCircles) {

        centerPoint = new Point[maxCircles];
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

            centerPoint[c] = new Point(xMax, yMax);
        
            clearNeighbours(xMax, yMax, rMax);
        }
    }

    /**
     * Clear, from the Hough Space, all the counter that are near (radius/2) a previously found circle C.
     *
     * @param x The x coordinate of the circle C found.
     * @param x The y coordinate of the circle C found.
     * @param x The radius of the circle C found.
     */
    private void clearNeighbours(int x, int y, int radius) {
        // The following code just clean the points around the center of the circle found.
        double halfRadius = radius / 2.0F;
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
                    if (Math.pow(j - x, 2D) + Math.pow(i - y, 2D) < halfSquared) {
                        houghValues[j][i][indexR] = 0.0D;
                    }
                }
            }
        }
    }

    boolean readParameters() {

        GenericDialog gd = new GenericDialog("Hough Parameters", IJ.getInstance());
        gd.addNumericField("Minimum radius (in pixels) :", 10, 0);
        gd.addNumericField("Maximum radius (in pixels)", 20, 0);
        gd.addNumericField("Increment radius (in pixels) :", 2, 0);
        gd.addNumericField("Number of Circles (NC): (enter 0 if using threshold)", 10, 0);
        gd.addNumericField("Threshold: (not used if NC > 0)", 60, 0);

        gd.showDialog();

        if (gd.wasCanceled()) {
            return (false);
        }

        radiusMin = (int) gd.getNextNumber();
        radiusMax = (int) gd.getNextNumber();
        radiusInc = (int) gd.getNextNumber();
        depth = ((radiusMax - radiusMin) / radiusInc) + 1;
        maxCircles = (int) gd.getNextNumber();
        threshold = (int) gd.getNextNumber();
        if (maxCircles > 0) {
            useThreshold = false;
            threshold = -1;
        } else {
            useThreshold = true;
            if (threshold < 0) {
                IJ.showMessage("Threshold must be greater than 0");
                return (false);
            }
        }
        return true;

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

    // Convert Values in Hough Space to an 8-Bit Image Space.
    private void createHoughPixels(byte houghPixels[]) {
        double d = -1D;
        for (int j = 0; j < height; j++) {
            for (int k = 0; k < width; k++)
                if (houghValues[k][j][0] > d) {
                    d = houghValues[k][j][0];
                }

        }

        for (int l = 0; l < height; l++) {
            for (int i = 0; i < width; i++) {
                houghPixels[i + l * width] = (byte) Math.round((houghValues[i][l][0] * 255D) / d);
            }

        }
    }
}
