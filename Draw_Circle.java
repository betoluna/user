import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class Draw_Circle implements PlugInFilter {
	protected ImagePlus image;

	@Override
	public int setup(String arg, ImagePlus image) {
		this.image = image;
		
		return DOES_ALL;
	}


	@Override
	public void run(ImageProcessor ip) {
		int cx = 130;//center x
		int cy = 130;//center y
		int rad = 70;//radius

		plotCircle(cx, cy, rad, ip);

	}

	/**
	* given center coordiantes and radius, draw a circle
	* implementation of "A Fast Bresenham Type Algorithm For Drawing Circles"
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
			proc.putPixel(CX + X, CY + Y, 0);
			proc.putPixel(CX - X, CY + Y, 0);
			proc.putPixel(CX - X, CY - Y, 0);
			proc.putPixel(CX + X, CY - Y, 0);
			proc.putPixel(CX + Y, CY + X, 0);
			proc.putPixel(CX - Y, CY + X, 0);
			proc.putPixel(CX - Y, CY - X, 0);
			proc.putPixel(CX + Y, CY - X, 0);

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