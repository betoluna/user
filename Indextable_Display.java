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
		

		int w = 256; int h = 256; int offset = 0; int rise = 16; int run = 16; int step = 0; int yStep = 0;
		ColorProcessor cip = new ColorProcessor(w, h);
		//modify the lookup tables	
		//for (int idx = 0; idx < mapSize; idx++) {
		for (int idx = 0; idx < 100; idx = idx + 10) {  
			int r = 0xff & Rmap[idx];//mask to treat as unsigned byte 
			int g = 0xff & Gmap[idx];
			int b = 0xff & Bmap[idx];   
			// Rmap[idx] = (byte) Math.min(r + 30, 255); 
			// Gmap[idx] = (byte) Math.min(g + 30, 255);
			// Bmap[idx] = (byte) Math.min(b + 30, 255);

			IJ.log("> Rmap[idx]: " + Rmap[idx]);
			IJ.log("> r: " + r); 
			int c = ((r & 0xff) << 16) | ((g & 0xff) << 8) | b & 0xff;

			int origin = 1 + run * step++;
			int xEnd = step * run;
		
			int yEnd = step * rise; 


			for (int v = 0; v < h; v++) {
     			for (int u = 0; u < w; u++) {
					cip.putPixel(u, v, c);
				}
			}
			ImagePlus cimg = new ImagePlus("My New Color Image: " + idx, cip); 
			cimg.show();
		}
		

	}

	// public void paint(int orig, int rise, int run, int color) {

	// 	for (int v = orig; v < rise; v++) {
 //     		for (int u = orig; u < run; u++) {
	// 			cip.putPixel(u, v, color);
	// 		}
	// 	}
	// }


}
