/* class to convert image to RGB values */

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;

/*Obtain RGB values for each pixel*/
public class Conversion {
	
	/* method to get RGB value from image at given x,y */
	public void singlePixel(int x, int y, int pixelValue){
		int alpha = (pixelValue >> 24) & 0xff;
		int red = (pixelValue >> 16) & 0xff;
		int green = (pixelValue >> 8) & 0xff;
		int blue = (pixelValue ) & 0xff;
		RGB value;
		value = new RGB(alpha, red, green, blue);
		Stream.rgbArray.add(value);
	}
	public void imageProcessing(Image img, int x, int y, int w, int h) {
		int[] pixelsArray = new int[w * h]; //initialize to width/height
		PixelGrabber pg = new PixelGrabber(img, x, y, w, h, pixelsArray, 0, w); 
		try {
			System.out.println("Grab Pixels Start"); //%TODO Remove this line
			pg.grabPixels();
			System.out.println("Grab Pixels end"); //%TODO Remove this line
		}
		catch (InterruptedException e){
			System.out.println(e);
			return;
		}
		if ((pg.getStatus() & ImageObserver.ABORT) != 0){
			System.out.println("Image Fetch Failed");
			return;
		}
		System.out.println("singlePixel function calls begin"); // %TODO Remove this line
		for (int i = 0; i < h; i++){
			for (int j = 0; j < w; j++){
				singlePixel(x+j, y+i, pixelsArray[i*w + j]);
			}
		}
		System.out.println("DONE!");
	}
}
