/* class to output RGB values to text file for debugging purposes
 * also serves as middle-man to image conversion */

import java.util.ArrayList;
import java.util.List;
import java.awt.Image;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

public class Stream{
	static List<RGB> rgbArray = new ArrayList<RGB>();
	public List<RGB> stream(String fileName) throws IOException{
		rgbArray.clear();
		Image ig = ImageIO.read(new File(fileName)); //TODO name image 
		int w = ig.getWidth(null);
		int h = ig.getHeight(null);
		Conversion imageConverter = new Conversion();
		imageConverter.imageProcessing(ig, 10, 0, w-20, h);
		PrintWriter buffer;
		buffer = new PrintWriter("rgbvalues.txt"); 
		String temp = "";
		System.out.println("Buffer Write Begin"); //%TODO Remove this line
		for (int i = 0; i < rgbArray.size(); i++){
			if(i % 120 == 0)
				buffer.print("\n");
				
			temp = rgbArray.get(i).blueString();
			buffer.print("(" + temp + "," + i + ")");
			//System.out.println(temp);
		}
		buffer.close();
		System.out.println("Buffer Write end"); //%TODO remove this line
		return rgbArray;
	}
}
