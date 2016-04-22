import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;

import ij.WindowManager;
import java.awt.image.IndexColorModel;

/**
 * CS/ECE545 - WPI, Spring 2016
 * Name: Norberto Luna-Cano
 * Email: nlunacano@wpi.edu
 * Date: May 1st. 2016
 * Overview Description of Plugin:
 * 
 * IJ.log("> ");
 * 
 */

/**
 * shows the color table of an 8-bit indexed image as a new image with 16 × 16 rectangular 
 * color fields. Use the following ImageJ function to label each color with the 
 * frequency and index color value in the image. Mark all unused color table entries with 
 * the value 0. The new image should look similar to the image below but with 16 x 16 squares 
 * (white background, each color represented by a square of that color, and below the colored 
 * square a label with the index # and frequency). Look at Prog. 12.3 p.249 as a starting point.
 * 
 * If you class makes use of any predefined variable members, please name them
 * appropriately and provide a short description comment on how it is used or
 * modified and the implications of modifying the variable.
 */
public class Indextable_Display implements PlugInFilter {
	protected ImagePlus image;
	int rectWidth = 16;
	int rectHeight = 16;
	int step = 16;
	int max = 256;
	int gridWidth = 16; 
	int gridHeight = 16;

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
		 * 
		 */
		return DOES_8G + DOES_8C;
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
		IndexColorModel icm = (IndexColorModel) ip.getColorModel(); 
		//IJ.write("Color Model=" + ip.getColorModel() + " " + ip.isColorLut());
	
		int pixBits = icm.getPixelSize(); 
		int mapSize = icm.getMapSize(); 
		
		//retrieve the current lookup tables (maps) for R,G,B
		byte[] Rmap = new byte[mapSize]; icm.getReds(Rmap);  
		byte[] Gmap = new byte[mapSize]; icm.getGreens(Gmap);  
		byte[] Bmap = new byte[mapSize]; icm.getBlues(Bmap);  
		

		int w = 280; int h = 280; 
		ColorProcessor cip = new ColorProcessor(w, h);
		String[] coord = new String[256];

		//fill an array of origin coordinates, one for each color (256)
		int index = 0; int u = 0;
        for (int y = 0; y < max; y = y + step) {
            for (int x = 0; x < max; x = x + step) {
                coord[index++] = u++ * step + "," + y;
            }
            u = 0;
        }

		//modify the lookup tables	
		//for (int idx = 0; idx < mapSize; idx++) {
		for (int idx = 0; idx < 100; idx = idx + 10) {  
			int r = 0xff & Rmap[idx];//mask to treat as unsigned byte 
			int g = 0xff & Gmap[idx];
			int b = 0xff & Bmap[idx];   

			IJ.log("> Rmap[idx]: " + Rmap[idx]);
			IJ.log("> r: " + r); 
			int c = ((r & 0xff) << 16) | ((g & 0xff) << 8) | b & 0xff;


			String[] coordinate = new String[2];
            coordinate = coord[idx].split(",");
			int x = Integer.parseInt(coordinate[0]);
            int y = Integer.parseInt(coordinate[1]);
			paintRectangle(x, y, c, cip);

			// for (int v = 0; v < h; v++) {
   //   			for (int u = 0; u < w; u++) {
			// 		cip.putPixel(u, v, c);
			// 	}
			// }
			// ImagePlus cimg = new ImagePlus("My New Color Image: " + idx, cip); 
			// cimg.show();
		}
		
		ImagePlus cimg = new ImagePlus("My New Color Image", cip); 
		cimg.show();
	}

	public void paintRectangle(int x, int y, int color, ColorProcessor cip) {
		int xBoundary = x + rectWidth;
		int yBoundary = y + rectHeight;

		for (int v = y; v < yBoundary; v++) {
     		for (int u = x; u < xBoundary; u++) {
				cip.putPixel(u, v, color);
			}
		}
	}

}
