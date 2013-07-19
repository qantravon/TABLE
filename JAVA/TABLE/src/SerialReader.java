import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.awt.event.*;import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class SerialReader implements SerialPortEventListener
{
    private InputStream in;
    private byte readChar;
    private boolean dataAvail;
    private boolean timedOut = false;
    private List<Byte> truthVals;
    private SerialPort serialPort;
    
    public SerialReader ( InputStream in , SerialPort sp)
    {
        this.in = in;
        serialPort = sp;
        try
        {
	        serialPort.addEventListener(this);
	        serialPort.notifyOnDataAvailable(true);
        }
        catch(Exception e)
        {
        	System.err.println(e.toString());
        }
        readChar = -1;
        truthVals = new ArrayList<Byte>();
        dataAvail = false;
    }
    
    public boolean isTimedOut()
    {
    	return timedOut;
    }
    
    public void RX()
    {
        byte[] buffer = new byte[1024];
        int len = -1;
        truthVals.clear();
        try
        {
    		long startTime = new Date().getTime();
            while ( truthVals.size() < 64)
            {
    			long nowTime = new Date().getTime();
    			if(nowTime - startTime > 5000)
    			{
    				timedOut = true;
    				return;
    			}
            	if(dataAvail)
            	{
	            	len = this.in.read(buffer);
	            	//System.out.println("Buffer len = " + len);
	            	for(int i = 0; i < len; ++i)
	            	{
	            		truthVals.add(new Byte(buffer[i]));
	            	}
            	}
            	timedOut = false;
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
                    
    }
    
    /* watch for data from serial */
    public synchronized void serialEvent(SerialPortEvent oEvent)
    {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE)
		{
			dataAvail = true;
			System.err.println("Data Available!");
		}
		else
		{
			dataAvail = false;
		}
	}
    
    public List<Byte> getOccupied()
    {
    	RX();
    	List<Byte> rVal = new ArrayList<Byte>();
    	for(int i = 0; i < truthVals.size(); ++i)
    	{
    		rVal.add(truthVals.get(i));
    	}
    	return rVal;
    }
}