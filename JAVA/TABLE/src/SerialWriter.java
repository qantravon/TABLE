import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.awt.event.*;import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialWriter
{
	private final int numPoints = 16800;
    private OutputStream out;
    private int sendVal;
    private byte[] sendArray;
    private SerialPort serialPort;
    
    
    public SerialWriter ( OutputStream out , SerialPort sp)
    {
        this.out = out;
        serialPort = sp;
        sendVal = -1;
        sendArray = new byte[19200];
    }
    
    public void TX()
    {
        try
        {
            this.out.write(sendArray);
            System.err.println("\tComplete!");
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
    
    public void pushArray(int[] ar)
    {
    	try
    	{
    		this.out.flush();
    	}
    	catch(IOException e)
    	{
    		e.printStackTrace();
    	}
    	boolean overflowed = false;
    	for(int i = 0; i < numPoints; ++i)
    	{
    		sendArray[i] = (byte)(ar[i]/2);
    		if((sendArray[i] < 0 || sendArray[i] > 255) && !overflowed)
    		{
    			System.err.println("Byte overflow (pusharray)");
    			overflowed = true;
    		}
    	}
    	TX();
    }
    
    public void sendBlue(int blue)
    {
    	while(sendVal > -1);
    	sendVal = blue;
    	TX();
    }
}