/* Data type to store RGB values from image */

public class RGB {
	private int alpha, red, green, blue;
	public RGB(int a, int r, int g, int b){
		alpha = a;
		red = r; 
		green = g; 
		blue = b; 
	}
	public String toString(){
		String aString = Integer.toString(alpha);
		String rString = Integer.toString(red);
		String gString = Integer.toString(green);
		String bString = Integer.toString(blue);
		String ret = aString + " " + rString + " " + gString + " " + bString;
		return ret;
	}
	
	public String blueString()
	{
		return Integer.toString(blue) + " ";
	}
	
	public char getBlue()
	{
		return (char)blue;
	}
}
