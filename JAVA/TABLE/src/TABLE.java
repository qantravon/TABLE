/**
 * @(#)TABLE.java
 *
 * TABLE application
 *
 * @author 
 * @version 1.00 2013/5/10
 */

import java.util.*;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import static java.nio.file.StandardWatchEventKinds.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.GraphicsDevice;
import java.awt.DisplayMode;
import java.awt.Polygon;
import javax.swing.JFrame;
import gnu.io.SerialPort;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.awt.event.*;import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
 
public class TABLE extends JFrame implements Runnable, KeyListener{
	private boolean graphEnabled = true;
	private int baudRate = 230400;	
	private final int NUM_BUFFERS = 2;
	private boolean timedOut = false;
	
	private Timer timer;
	
	private Image dbImage;
	private Image background;
	private MediaTracker mt;
	private Graphics gScr;
    private GraphicsDevice gd;
    private BufferStrategy bufferStrategy;
    private Graphics dbg;
    private Applet a;
    private Thread th;
    private boolean running;
	
    public static boolean[][] grid = new boolean[8][8];
    public static int[] xcorner = new int[81];
    public static int[] ycorner = new int[81];
	public static ArrayList activeSquares = new ArrayList();
    
    public static final String outFile = "output.TXT";
    public static final String cornerFile = "corners.TXT";
    
    private final String imageFile = "TABLE.jpg";
    private java.util.List<RGB> rgbArray;
    
    private SerialPort serialPort;
    private SerialReader serialReader;
    private SerialWriter serialWriter;
    
    void connect() throws Exception //connects to the Serial COM port
    {
    	String portName = "COM5";
    	System.out.println("Connecting to " + portName);
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(baudRate,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                
                serialReader = new SerialReader(in, serialPort);
                serialWriter = new SerialWriter(out, serialPort);
            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    private void processImage() //takes the image and converts it to an array of RGB values
    {
    	System.err.print("Converting image: ");
    	final Stream imgStream = new Stream();
		try
		{
			System.err.println(imageFile);
			rgbArray = imgStream.stream(imageFile); 	//Stream call
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		System.err.println("Image conversion complete");
    }
    
    public void keyPressed(KeyEvent keyevent)
    {
        int i = keyevent.getKeyCode();
        if(i == 27)
        {
        	System.out.println("Exiting...");
        	System.exit(0);
        }
        keyevent.consume();
    }

    public void keyReleased(KeyEvent keyevent)
    {
        keyevent.consume();
    }

    public void keyTyped(KeyEvent keyevent)
    {
        keyevent.consume();
    }
    
    private void setBufferStrategy() //double-buffering for display output
    {
        try
        {
            EventQueue.invokeAndWait(new  Runnable() {
            	public void run()
            	{ createBufferStrategy(NUM_BUFFERS);}
            });
    	}
        catch(Exception exception)
        {
            System.out.println("Error while creating buffer strategy");
            System.exit(0);
        }
        try
        {
            Thread.sleep(500);
        }
        catch(InterruptedException interruptedexception) { }
        bufferStrategy = getBufferStrategy();
    }
    
    public void start()
    {
        th = new Thread(this);
        th.start();
    }

    public void stop()
    {
    	th.stop();
    }
    
    public void run() //primary thread
    {
        Thread.currentThread().setPriority(1);
        for(; running; Thread.currentThread().setPriority(10))
        {
        	processImage();
    		transmitData();
    		getGrid();
    		if(!serialReader.isTimedOut()) //do not continue if serial is waiting
    		{
	    		if(!graphEnabled)
	    			printGrid();
	    			generatePolys();
	    		if(graphEnabled)
	            	screenUpdate();
	    		monitorFilesInDirectory("C:\\TABLE\\classes");
	    		while(monitorFilesInDirectoryWithTimeout("C:\\TABLE\\classes", 2000));
    		}
            try
            {
                Thread.sleep(20L);
            }
            catch(InterruptedException interruptedexception2) { }
        }
    }
    
    private void screenUpdate()
    {
        try
        {
            gScr = bufferStrategy.getDrawGraphics();
            paint(gScr);
            gScr.dispose();
            if(!bufferStrategy.contentsLost())
                bufferStrategy.show();
            else
                System.out.println("Contents Lost");
            Toolkit.getDefaultToolkit().sync();
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            running = false;
        }
    }
        
    public void init()
    {
    	timer = new Timer();
    	background = getToolkit().getImage("board.jpg");
    	a = new Applet();
    	running = true;
        addKeyListener(this);
    	mt = new MediaTracker(this);
    	mt.addImage(background, 0);
    	rgbArray = new ArrayList<RGB>();
    	try
        {
            mt.waitForID(0);
        }
        catch(Exception exception)
        {
            System.out.println(exception.getMessage());
        }
    	getCorners();
    	try
    	{
    		connect();
    	}
    	catch(Exception e)
    	{
            e.printStackTrace();
    	}
    	if(graphEnabled)
    		initFullScreen();
    	run();
    }
    
    public void initFullScreen()
    {
        System.out.println("Activating full-screen...");
        GraphicsEnvironment graphicsenvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        gd = graphicsenvironment.getDefaultScreenDevice();
        setUndecorated(true);
        setIgnoreRepaint(true);
        setResizable(false);
        if(!gd.isFullScreenSupported())
        {
            System.out.println("Full-screen exclusive mode not supported");
            System.exit(0);
        }
        gd.setFullScreenWindow(this);
        setDisplayMode(1024, 768, 16);
        setBufferStrategy();
    }

    private boolean isDisplayModeAvailable(int i, int j, int k)
    {
        DisplayMode adisplaymode[] = gd.getDisplayModes();
        for(int l = 0; l < adisplaymode.length; l++)
            if(i == adisplaymode[l].getWidth() && j == adisplaymode[l].getHeight() && k == adisplaymode[l].getBitDepth())
                return true;

        return false;
    }
    
    private void setDisplayMode(int i, int j, int k)
    {
        if(!gd.isDisplayChangeSupported())
        {
            System.out.println("Display mode changing not supported");
            return;
        }
        if(!isDisplayModeAvailable(i, j, k))
        {
            System.out.println((new StringBuilder()).append("Display mode (").append(i).append(",").append(j).append(",").append(k).append(") not available").toString());
            return;
        }
        DisplayMode displaymode = new DisplayMode(i, j, k, 0);
        try
        {
            gd.setDisplayMode(displaymode);
            System.out.println((new StringBuilder()).append("Display mode set to: (").append(i).append(",").append(j).append(",").append(k).append(")").toString());
        }
        catch(IllegalArgumentException illegalargumentexception)
        {
            System.out.println((new StringBuilder()).append("Error setting Display mode (").append(i).append(",").append(j).append(",").append(k).append(")").toString());
        }
        try
        {
            Thread.sleep(1000L);
        }
        catch(InterruptedException interruptedexception) { }
    }

    
    public void getCorners() //index calculated locations of squares in display image
    {
    	Scanner s = new Scanner(System.in);
    	try
    	{
    		s = new Scanner(new File(cornerFile));
    	}
    	catch(FileNotFoundException e)
    	{
    		System.err.println("Error: File not found!");
    		System.exit(1);
    	}
    	for(int i = 0; i < 81; ++i)
    	{
    		if(s.hasNextInt())
    		{
    			xcorner[i] = s.nextInt();
    		}
    		else
    		{
    			System.err.println("Error: invalid file format");
    			System.exit(1);
    		}
    		if(s.hasNextInt())
    		{
    			ycorner[i] = s.nextInt();
    		}
    		else
    		{
    			System.err.println("Error: invalid file format");
    			System.exit(1);
    		}
    	}
    }
	
	public void generatePolys() //generate polygons for output squares
	{
		//System.err.println("Entering generatePolys");
		activeSquares.clear();
		for(int i = 0; i < grid.length; ++i)
		{
			for(int j = 0; j < grid[i].length; ++j)
			{
				//System.err.println("Checking at " + i + " " + j);
				if(grid[j][i])
				{
					//System.err.println("Found square at (" + i + "," + j + ")");
					int[] xcoords = new int[4];
					int[] ycoords = new int[4];
					xcoords[0] = xcorner[(i+(j*9))];
				  	ycoords[0] = ycorner[(i+(j*9))];
				  	xcoords[1] = xcorner[(i+(j*9))+1];
				  	ycoords[1] = ycorner[(i+(j*9))+1];
				  	xcoords[2] = xcorner[(i+(j*9))+10];
				  	ycoords[2] = ycorner[(i+(j*9))+10];
				  	xcoords[3] = xcorner[(i+(j*9))+9];
				  	ycoords[3] = ycorner[(i+(j*9))+9];
					Polygon square = new Polygon(xcoords, ycoords, 4);
					activeSquares.add(square);
				}
			}
		}
	}
	
	public void transmitData() //send blue vals to PSOC board
	{
		System.err.println("Sending data to PSOC");
		int temp;
		int[] tempArray = new int[16800];
		for(int i = 0; i < 16800; ++i)
		{
			RGB pixel = rgbArray.get(i);
			temp = pixel.getBlue();
			if(temp < 0 || temp > 255)
			{

				System.err.println("Byte overflow");
			}
			tempArray[i] = temp;
			//System.err.println("transmitting " + temp);
			//serialWriter.sendBlue(temp);
		}
		serialWriter.pushArray(tempArray);
		System.err.println("Done sending");
	}
    
    public void getGrid() //get return output from PSOC
    {
    	System.err.println("Getting PSOC output");
    	java.util.List<Byte> occupied = serialReader.getOccupied();
    	if(serialReader.isTimedOut())
    		return;
    	for(int i = 0; i < 8; ++i)
    	{
    		for(int j = 0; j < 8; ++j)
    		{
    			byte a = occupied.get(i+(j*8)).byteValue();
    			//System.err.print(a);
    			grid[i][j] = (a == (byte)1);
    		}
    		//System.err.println();
    	}
    }
    
    public static void printGrid() //text output mode for debugging
    {
    	for(int i = 0; i < 8; ++i)
    	{
    		for(int j = 0; j < 8; ++j)
    		{
    			if(grid[i][j])
    			{
    				System.out.print("1");
    			}
    			else
    			{
    				System.out.print("0");
    			}
    		}
    		System.out.println();
    	}
    	System.out.println("--------");
    }
    
    public static boolean monitorFilesInDirectoryWithTimeout(String directory, int timeout)
	{
		Date myDate = new Date();
		long startTime = myDate.getTime();
		long nowTime;
		try
		{
			//create a WatcherService object for monitoring directories
			WatchService watchService = FileSystems.getDefault().newWatchService();
			//getting the path object for the directory given by users
			Path path = Paths.get(directory);
			//register the events to be notified by the program
			path.register(watchService, ENTRY_MODIFY);
			while(true)
			{
				nowTime = myDate.getTime();
				//infinite loop will take events
				WatchKey key = watchService.poll(timeout, TimeUnit.MILLISECONDS);
				if(key == null)
					return false;
				else
					startTime = myDate.getTime();
				for (WatchEvent<?> watchEvent : key.pollEvents())
				{
					//getting the type of the event
					WatchEvent.Kind<?> kind = watchEvent.kind();
					//getting the file name from the WatchEvent object
					Path file = (Path) watchEvent.context();
					return true;
				}
				//reset the current event and wait for other events
				key.reset();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}

	//found at http://tunatore.wordpress.com/2011/09/30/how-to-monitor-file-changes-using-java-nio-jdk-7-api-watcherservice-example/    
	public static boolean monitorFilesInDirectory(String directory)
	{
		try
		{
			//create a WatcherService object for monitoring directories
			WatchService watchService = FileSystems.getDefault().newWatchService();
			//getting the path object for the directory given by users
			Path path = Paths.get(directory);
			//register the events to be notified by the program
			path.register(watchService, ENTRY_MODIFY);
			while(true)
			{
				//infinite loop will take events
				WatchKey key = watchService.take();
				for (WatchEvent<?> watchEvent : key.pollEvents())
				{
					//getting the type of the event
					WatchEvent.Kind<?> kind = watchEvent.kind();
					//getting the file name from the WatchEvent object
					Path file = (Path) watchEvent.context();
					return true;
				}
				//reset the current event and wait for other events
				key.reset();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}
	
	public void drawGrid(Graphics g)
	{
		//System.err.println("Entering drawGrid");
		g.drawImage(background, 0, 0, null);
		//System.err.println("Exiting drawGrid");
	}
	
	public void drawPolys(Graphics g)
	{
		//System.err.println("Entering drawPolys");
		//System.err.println("Size of activeSquares: " + activeSquares.size());
		g.setColor(Color.YELLOW);
		for(Object poly: activeSquares)
		{
			Polygon p = (Polygon)poly;
			/*System.err.print("Drawing polygon with coords ");
			for(int i = 0; i < p.npoints; ++i)
				System.err.print("(" + p.xpoints[i] + "," + p.ypoints[i] + ") , ");
			System.err.println();*/
			g.fillPolygon(p);
		}
		//System.err.println("Exiting drawPolys");
	}
	
	public void paint(Graphics g)
	{
		super.paint(g);
		drawGrid(g);
		drawPolys(g);
	}
	
    public void update(Graphics g) //update graphics output
    {
        if(dbImage == null)
        {
            dbImage = createImage(getSize().width, getSize().height);
            dbg = dbImage.getGraphics();
        }
        dbg.setColor(getBackground());
        dbg.fillRect(0, 0, getSize().width, getSize().height);
        dbg.setColor(getForeground());
        paint(dbg);
        g.drawImage(dbImage, 0, 0, this);
    }
	
	public TABLE()
	{
		init();
	}
     
    public static void main(String[] args)
    {
    	new TABLE();
    }
}
