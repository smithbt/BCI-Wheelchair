/**
 * 
 */
package edu.lafayette.bci;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import edu.lafayette.bci.devices.Emotiv;
import edu.lafayette.bci.devices.EmotivObserver;
import edu.lafayette.bci.sigproc.*;
import edu.lafayette.bci.utils.Watchdog;
import edu.lafayette.bci.utils.WatchdogObserver;
import edu.lafayette.bci.nxt.NXTControl;

/**
 * @author Brandon T. Smith
 *
 */
public class SimpleNXTControlPanel implements EmotivObserver, KeyListener,
		WatchdogObserver {

	// End devices
	private Emotiv emotiv = null;
	private NXTControl nxt = null;
	private NXTControlPanelUI ui = null;

	// Connection timer
	private Watchdog wd = null;
	private final static int CONNECTION_TIMEOUT = 1000; // Connection loss timeout (in milliseconds)
	
	// data processing pipelines and pre-average HPF
	private Pipeline occipital = null;
	private Pipeline frontal = null;
	private Pipeline gyroX = null;
	private HighPassFilter hpf = new HighPassFilter(0.5);
	
	// Threshold fields
	private final double BLINK_THRES; // Blink detection threshold, in uV (User-specific)
	private static final int BLINK_NUM_THRES = 5; // Number of blinks to trigger a detection
	private static final double BLINK_TIME_THRES = 2.0; // Time within which blinks must occur (in secs)
	private final double OCCIPITAL_THRES;  // Power threshold in (uV)^2
	private static final double GYROX_POS_THRES = 2000.0; // Position threshold
	private static final double GYROX_POS_MAX = 10000.0; // Max value for normalizing gyro position
	
	// time values for processing
	private long gyroPrevTime = 0;
	private long sensPrevTime = 0;
	
	// state variables for controlled device
	private static final double NXT_SPEED = 0.5;
	private volatile boolean isTraveling = false;
	private volatile boolean isTurning = false;
	private volatile boolean eStop = false;
	

	// Set standard bottom label (TODO: Modify if necessary)
	private static final String STANDARD_BOTTOM_LABEL 
		= "Start/Stop: Blink x5, Toggle Forward/Back: Close Eyes, Turn: Rotate Head (Reset Shift+C)";
	
	/**
	 * Constructor. Initializes processing pipelines and end devices
	 */
	public SimpleNXTControlPanel(double blink, double occip) {
		
		// Sets final threshold values
		BLINK_THRES = blink;
		OCCIPITAL_THRES = occip;
		
		// Creates UI
		ui = new NXTControlPanelUI();
		ui.setDirection(NXTControlPanelUI.STOP);
		ui.addKeyListener(this);
		
		// Establishes processing pipelines
		// Occipital (eye closure, alpha wave pattern) Algorithms
		// Butterworth filter between 8-13Hz (alpha band)
		double[] freqs = { 8.0, 13.0 }; 
		Butterworth occipButter = new Butterworth(4, freqs, 1 / (Emotiv.SAMPLE_RATE_IN_MS / 1000.0), Butterworth.BPF);
		occipital.addAlgorithm(occipButter);
		// Power calculation
		Power occipPower = new Power(1 / 11.5, Emotiv.SAMPLE_RATE_IN_MS / 1000.0); 
		occipital.addAlgorithm(occipPower);
		// TODO: Determine if Moving Average is necessary 
		// Convert to digital signal using level threshold
		Threshold occipThres = new Threshold(OCCIPITAL_THRES); 
		occipital.addAlgorithm(occipThres);
		
		// Frontal (Blinking) Algorithms
		Threshold blinkThres = new Threshold(BLINK_THRES);
		frontal.addAlgorithm(blinkThres);
		// tracks rising edge
		EdgeDetect blinkEdge = new EdgeDetect(); 
		frontal.addAlgorithm(blinkEdge);
		// counts number of pulses
		PulseCount blinkCount = new PulseCount(BLINK_NUM_THRES, BLINK_TIME_THRES); 
		frontal.addAlgorithm(blinkCount);
		
		// Lateral (X) Gyroscope
		GyroDetect gd = new GyroDetect(GYROX_POS_THRES, false, true);
		gyroX.addAlgorithm(gd);
		
		// Initiates end device connections
		nxt = new NXTControl();
		wd = new Watchdog();
		wd.setTimeout(CONNECTION_TIMEOUT);
		wd.addObserver(this);
		emotiv = new Emotiv();
		emotiv.addObserver(this);
		
		// TODO: wait until UI is closed
		while (!ui.isWindowClosed()) {
			try { Thread.sleep(250); } catch (Exception e) {}
		}
		
		// terminate connections
		wd.finish();
		emotiv.close();
		nxt.stop();
	}
	
	/* (non-Javadoc)
	 * @see edu.lafayette.bci.utils.WatchdogObserver#timeout()
	 */
	@Override
	public void timeout() {
		// Set the NXT and UI to stop
		nxt.stop();
		ui.setDirection(NXTControlPanelUI.STOP);

		// Indicate connection lost on UI
		ui.setBottomLabel("Connection Lost");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		nxt.stop();
		ui.setDirection(NXTControlPanelUI.STOP);
		isTraveling = false;
		isTurning = false;
		
		// determine cause and action
		if (eStop) {
			ui.setBottomLabel("Estop");
		} else {
			ui.setBottomLabel(STANDARD_BOTTOM_LABEL);
		}

	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.lafayette.bci.devices.EmotivObserver#batteryChanged(edu.lafayette.bci.devices.Emotiv)
	 */
	@Override
	public void batteryChanged(Emotiv e) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.lafayette.bci.devices.EmotivObserver#qualityChanged(edu.lafayette.bci.devices.Emotiv, java.lang.String, int)
	 */
	@Override
	public void qualityChanged(Emotiv e, String sensor, int quality) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.lafayette.bci.devices.EmotivObserver#sensorsChanged(edu.lafayette.bci.devices.Emotiv)
	 */
	@Override
	public void sensorsChanged(Emotiv e) {
		// Initialize time
		if (sensPrevTime == 0) {
			sensPrevTime = System.currentTimeMillis();
		}

		// calculate time (in seconds) from start
		long currTime = System.currentTimeMillis();
		double time = (currTime - sensPrevTime) / 1000.0;
		
		// Average occipital points, then process
		Point o1 = hpf.process( new Point(time, e.getSensorValue("O1")) );
		Point o2 = hpf.process( new Point(time, e.getSensorValue("O2")) );
		Point ppO = new Point(o1.getX(), (o1.getY() + o2.getY()) / 2.0 );
		Point occip = occipital.pushPoint(ppO);
		
		// Average frontal points, then process
		Point af3 = hpf.process( new Point(time, e.getSensorValue("AF3")) );
		Point af4 = hpf.process( new Point(time, e.getSensorValue("AF4")) );
		Point ppAF = new Point(af3.getX(), (af3.getY() + af4.getY()) / 2.0 );
		Point front = frontal.pushPoint(ppAF);
		
		// TODO: Interpret the results from the two channels (frontal and occipital)
		if (front.getY() == 1.0) { // if 5 blinks detected
			if (!isTraveling) { // Start device if stopped
				nxt.forward(NXT_SPEED);
				ui.setDirection(NXTControlPanelUI.FORWARD);
			} 
			else { // Stop device if running
				nxt.stop();
				ui.setDirection(NXTControlPanelUI.STOP);
			}
			isTraveling = !isTraveling;
			
		}
	}

	/* (non-Javadoc)
	 * @see edu.lafayette.bci.devices.EmotivObserver#gyrosChanged(edu.lafayette.bci.devices.Emotiv)
	 */
	@Override
	public void gyrosChanged(Emotiv e) {
		// Initialize time
		if (gyroPrevTime == 0) {
			gyroPrevTime = System.currentTimeMillis();
		}

		// calculate time (in seconds) from start
		long currTime = System.currentTimeMillis();
		double time = (currTime - gyroPrevTime) / 1000.0;
		
		// create and process new gyroscope point
		Point p = gyroX.pushPoint( new Point(time, e.getGyroValue("x")) );
		
		// Interpret result
		double turnResult = p.getY();
		// TODO: Determine what kind of motion this result corresponds to and enact it.
		if (turnResult == 0) {
			// Stop Turning
			if (isTurning) {
				isTurning = !isTurning;
			}
			
		}
		
	}

}
