package edu.lafayette.bci;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import edu.lafayette.bci.devices.*;
import edu.lafayette.bci.sigproc.*;
import edu.lafayette.bci.utils.*;
import edu.lafayette.bci.nxt.NXTControl;

import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.File;

/**
 * This is the main class for the NXTControlPanel.  It reads data
 * from the Emotiv headset on each sampling frame, pushes the data
 * through the signal processing pipelines, and updates the UI and
 * the NXT commands.
 * 
 * @author Brandon T. Smith
 */
public class NXTControlPanel implements EmotivObserver, KeyListener,
		WatchdogObserver {

	// Threshold constants
	// Note: OCCIPITAL_THRES and BLINK_THRES may have to be calibrated each time the headset
	// is placed on the user's head.  Increasing the threshold will decrease the number of
	// false positives.  Decreasing the threshold will decrease the number of false negatives.
	private /*static*/ final double OCCIPITAL_THRES = 30.0; // Power threshold in (uV)^2
	private static final double NXT_SPEED = 0.5; // Constant fraction of the nxt maximum velocity
	private static final double GYROX_POS_THRES = 2000.0; // Position threshold
	private static final double GYROX_POS_MAX = 10000.0; // Max value for normalizing gyro position
	private /*static*/ final double BLINK_THRES = 130.0; // Blink detection threshold, in uV
	private static final int BLINK_NUM_THRES = 5; // Number of blinks to trigger a detection
	private static final double BLINK_TIME_THRES = 2.0; // Time within which blinks must occur (in secs)
	private static final long CONNECTION_TIMEOUT = 1000; // Timeout for detecting connection loss
	private static final int MOVING_AVG_WINDOW = 1500; // Number of milliseconds in filter window

	// Graph for occipital waves
	private Graph occipital = null;
	private Graph hpfO1 = null;
	private Graph hpfO2 = null;

	// Graph for gyros
	private Graph gyroX = null;

	// Graph for blinking
	private Graph frontal = null;
	private Graph hpfAF3 = null;
	private Graph hpfAF4 = null;

	// End devices
	private Emotiv emotiv = null;
	private NXTControl nxt = null;

	// UI class
	private NXTControlPanelUI ui = null;
	// Set standard bottom label (TODO: Modify if necessary)
	private static final String STANDARD_BOTTOM_LABEL 
		= "Start/Stop: Close Eyes, Toggle Forward/Back: Blink x5, Turn: Rotate Head (Reset Shift+C)";

	// Connection timer
	private Watchdog wd = null;

	// State variables
	private volatile boolean isTurning = false; // Indicates if the nxt is turning or stopped
	private volatile boolean moving = false; // True if the nxt is moving, false if stopped
	private volatile boolean forward = true; // True if the nxt is (or will be) moving forward, false if moving back
	private volatile boolean estop = false; // Emergency stop
	private volatile long timeoutTime = 0;

	// Used to calculate elapsed time
	private long prevTime = 0;
	private long gyroPrevTime = 0;

	// Implementation testing variables - set of these to false to turn off the corresponding DOF
	private static final boolean ENABLE_ALPHA = true;
	private static final boolean ENABLE_BLINK = true;
	private static final boolean ENABLE_GYRO = true;
	
	// An array to hold the speed values generated from the gyroscope
	private ArrayList<Double> allSpeeds = new ArrayList<Double>();
	
	
	/**
	 * NXTControlPanel constructor, initiates communication with end devices.
	 */
	public NXTControlPanel() {
		
		// Create UI
		ui = new NXTControlPanelUI();
		ui.setDirection(NXTControlPanelUI.STOP);
		ui.addKeyListener(this);
		
		// Setup the graphs and pipelines
		occipital = new Graph();
		hpfO1 = new Graph();
		hpfO2 = new Graph();
		gyroX = new Graph();
		frontal = new Graph();
		hpfAF3 = new Graph();
		hpfAF4 = new Graph();

		// Set graph window size (number of points kept)
		occipital.setWindowSize(5);
		hpfO1.setWindowSize(2);
		hpfO2.setWindowSize(2);
		gyroX.setWindowSize(2);
		frontal.setWindowSize(5);
		hpfAF3.setWindowSize(2);
		hpfAF4.setWindowSize(2);

		// Pipelines
		Pipeline pipeOcc = new Pipeline();
		Pipeline pipeHpf1 = new Pipeline();
		Pipeline pipeHpf2 = new Pipeline();
		Pipeline pipeGyroX = new Pipeline();
		Pipeline pipeFront = new Pipeline();
		Pipeline pipeHpf3 = new Pipeline();
		Pipeline pipeHpf4 = new Pipeline();

		// High pass filter to remove the drifting DC bias
		HighPassFilter hpf1 = new HighPassFilter(0.5);
		HighPassFilter hpf2 = new HighPassFilter(0.5);
		pipeHpf1.addAlgorithm(hpf1);
		pipeHpf2.addAlgorithm(hpf2);

		// Butterworth filter between 8-13Hz (alpha band)
		double[] freqs = { 8.0, 13.0 };
		Butterworth butter = new Butterworth(4, freqs, 1 / (Emotiv.SAMPLE_RATE_IN_MS / 1000.0), Butterworth.BPF);
		pipeOcc.addAlgorithm(butter);

		// Power calculation
		Power power = new Power(1 / 11.5, Emotiv.SAMPLE_RATE_IN_MS / 1000.0);
		pipeOcc.addAlgorithm(power);

		// Rolling average filter to smooth the power
		MovingAverage avg = new MovingAverage(MOVING_AVG_WINDOW / Emotiv.SAMPLE_RATE_IN_MS);
		pipeOcc.addAlgorithm(avg);

		// Convert to digital signal using level threshold
		Threshold thres = new Threshold(OCCIPITAL_THRES);
		pipeOcc.addAlgorithm(thres);

		// Perform rising and falling edge detection
		EdgeDetect edge = new EdgeDetect();
		pipeOcc.addAlgorithm(edge);

		// XGyro
		GyroDetect gd = new GyroDetect(GYROX_POS_THRES, false, true);
		pipeGyroX.addAlgorithm(gd);

		// Blinking HPF's
		HighPassFilter hpf3 = new HighPassFilter(0.5);
		HighPassFilter hpf4 = new HighPassFilter(0.5);
		pipeHpf3.addAlgorithm(hpf3);
		pipeHpf4.addAlgorithm(hpf4);

		// Blink detection threshold
		Threshold thres2 = new Threshold(BLINK_THRES);
		pipeFront.addAlgorithm(thres2);

		// Rising edge detection
		EdgeDetect edge2 = new EdgeDetect();
		pipeFront.addAlgorithm(edge2);

		// Pulse counter
		PulseCount cnt = new PulseCount(BLINK_NUM_THRES, BLINK_TIME_THRES);
		pipeFront.addAlgorithm(cnt);

		occipital.addPipeline(pipeOcc);
		hpfO1.addPipeline(pipeHpf1);
		hpfO2.addPipeline(pipeHpf2);
		gyroX.addPipeline(pipeGyroX);
		frontal.addPipeline(pipeFront);
		hpfAF3.addPipeline(pipeHpf3);
		hpfAF4.addPipeline(pipeHpf4);

		// Create the emotiv, watchdog, and the nxt
		nxt = new NXTControl();
		wd = new Watchdog();
		wd.setTimeout(CONNECTION_TIMEOUT);
		wd.addObserver(this);
		emotiv = new Emotiv();
		emotiv.addObserver(this);
		wd.start();

		// Takeoff automatically if blinking is disabled
		if (!ENABLE_BLINK) {
			// Wait to allow filters to settle
			try { Thread.sleep(5000); } catch (Exception e) {}
			// Start NXT
			nxt.forward(NXT_SPEED);
			moving = true;
			forward = true;
			//ui.setMoving(moving, forward);
			ui.setDirection(NXTControlPanelUI.FORWARD);
		}

		// Wait until window is closed
		while (!ui.isWindowClosed()) {
			try { Thread.sleep(250); } catch (Exception e) {}
		}
		
		// Write speeds out to a file
		PrintWriter speedsOut = null;
		try {
			// writes to a specific location on my (B. T. Smith) personal computer.
			speedsOut = new PrintWriter(
					new File("C:\\Users\\Brandon T. Smith\\Documents\\EXCEL\\gyroSpeeds.csv"));
			for(Double d : allSpeeds) { speedsOut.println(d); }
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			if (speedsOut!=null) { speedsOut.close(); }
		}

		// Close device communications
		wd.finish();
		emotiv.close();
		nxt.stop();
	}

	@Override
	public void timeout() {
		// Stop the nxt
		timeoutTime = System.currentTimeMillis();
		nxt.stop();

		// Recalibrate the gyro
		GyroDetect det = (GyroDetect)gyroX.getPipeline().getAlgorithm(0);
		det.calibrateCenter();

		// Set the UI to stop
		ui.setDirection(NXTControlPanelUI.STOP);

		// Indicate connection lost on UI
		ui.setBottomLabel("Connection Lost");
		

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// Recalibrate if Shift+C is pressed
		if (e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_C) {
			GyroDetect det = (GyroDetect)gyroX.getPipeline().getAlgorithm(0);
			det.calibrateCenter();
		} else if (!e.isShiftDown()) {
			// Implement emergency stop if any key is pressed
			estop = !estop;
			nxt.stop();
			moving = false;
			forward = true;
			ui.setDirection(NXTControlPanelUI.STOP);
			//ui.setMoving(false, forward);

			// Set the label appropriately
			if (timeoutTime != 0) {
				ui.setBottomLabel("Connection Lost");
			} else if (estop) {
				ui.setBottomLabel("Estop");
			} else {
				ui.setBottomLabel(STANDARD_BOTTOM_LABEL);
			}
		}
	}
	
	@Override
	public void sensorsChanged(Emotiv e) {
		
		// Initialize time
		if (prevTime == 0) {
			prevTime = System.currentTimeMillis();
		}

		// Get current time, calculate elapsed time
		long currTime = System.currentTimeMillis();

		// Reset watchdog timer
		wd.reset();

		// If timeout occurred, reset the prevTime to account for lost time
		if (timeoutTime != 0) {
			prevTime += currTime - timeoutTime - Emotiv.SAMPLE_RATE_IN_MS;
			gyroPrevTime += currTime - timeoutTime - Emotiv.GYRO_SAMPLE_RATE_IN_MS;
			timeoutTime = 0;
		}

		// Pass data from each electrode through the HPF
		hpfO1.addPoint(new Point((currTime - prevTime) / 1000.0, e.getSensorValue("O1")));
		hpfO2.addPoint(new Point((currTime - prevTime) / 1000.0, e.getSensorValue("O2")));

		// Average signals and pass through occipital pipeline
		Point[] hpfO1data = new Point[hpfO1.getData().size()];
		Point[] hpfO2data = new Point[hpfO2.getData().size()];
		hpfO1.getData().toArray(hpfO1data);
		hpfO2.getData().toArray(hpfO2data);

		// Create a new point with the average of O1 and O2
		Point pt = new Point(hpfO1data[hpfO1data.length - 1].getX(), 
				(hpfO1data[hpfO1data.length - 1].getY() + hpfO2data[hpfO2data.length - 1].getY()) / 2.0);

		// Add to the occipital pipeline
		if (ENABLE_ALPHA) {
			occipital.addPoint(pt);
		} else {
			// Add zero point if alpha disabled for testing
			occipital.addPoint(new Point(hpfO1data[hpfO1data.length - 1].getX(), 0.0));
		}

		// Add AF3/4 data to high pass filters
		hpfAF3.addPoint(new Point((currTime - prevTime) / 1000.0, e.getSensorValue("AF3")));
		hpfAF4.addPoint(new Point((currTime - prevTime) / 1000.0, e.getSensorValue("AF4")));

		// Get high passed data
		Point[] hpfAF3data = new Point[hpfAF3.getData().size()];
		Point[] hpfAF4data = new Point[hpfAF3.getData().size()];
		hpfAF3.getData().toArray(hpfAF3data);
		hpfAF3.getData().toArray(hpfAF4data);

		// Average high passed data
		Point pt2 = new Point(hpfAF3data[hpfAF3data.length - 1].getX(), 
				(hpfAF3data[hpfAF3data.length - 1].getY() + hpfAF4data[hpfAF4data.length - 1].getY() / 2.0));

		// Add to frontal pipeline
		if (ENABLE_BLINK) {
			frontal.addPoint(pt2);
		} else {
			// Add zero point if blink disabled for testing
			frontal.addPoint(new Point(hpfAF3data[hpfAF3data.length - 1].getX(), 0.0));
		}
		
		// Estop logic
		if (estop) {
			nxt.stop();
			moving = false;
			forward = true;
			ui.setDirection(NXTControlPanelUI.STOP);
			ui.setBottomLabel("Estop");
			return;
		} 

		// No estop or connection loss -> reset label to normal value
		ui.setBottomLabel(STANDARD_BOTTOM_LABEL);
		
		// skip linear movement processing if turning.
		if (isTurning) {
			return;
		}
		
		// TODO: Consider whether to use blinks for start/stop
		// reverse  logic
		// TODO Add an indicator to the ControlPanelUI for the "forward" variable.
		Point[] frontalData = new Point[frontal.getData().size()];
		frontal.getData().toArray(frontalData);
		if (frontalData[frontalData.length - 1].getY() == 1.0) {
			// Toggle reverse
			forward = !forward; //for start/stop, moving = !moving;

			// start or stop
			if (moving && forward && !isTurning) {
				nxt.forward(NXT_SPEED);
				ui.setDirection(NXTControlPanelUI.FORWARD);
			} else if (moving && !forward && !isTurning) {
				nxt.backward(NXT_SPEED);
				ui.setDirection(NXTControlPanelUI.BACKWARD);
			}
			
			return;
		}
		
		// TODO: Consider whether to use alpha for direction toggle. 
		// Toggle start/stop
		// Look for alpha posedge/negedge
		Point[] occipitalData = new Point[occipital.getData().size()];
		occipital.getData().toArray(occipitalData);
		Point lastPoint = occipitalData[occipitalData.length - 1];

		if (lastPoint.getY() == 1 && moving) {
			// Rising edge detected while moving, stop nxt
			nxt.stop();
			ui.setDirection(NXTControlPanelUI.STOP);
		} else if (lastPoint.getY() == 1 && !moving) {
			// rising edge detected while not moving, start nxt
			if (forward) {
				nxt.forward(NXT_SPEED);
				ui.setDirection(NXTControlPanelUI.FORWARD);
			} else {
				nxt.backward(NXT_SPEED);
				ui.setDirection(NXTControlPanelUI.BACKWARD);
			}
		} else if (lastPoint.getY() == -1) {
			// Falling edge detected, toggle movement state
			moving = !moving;	
		}

	}

	
	@Override
	public void gyrosChanged(Emotiv e) {
		
		// Initialize time
		if (gyroPrevTime == 0) {
			gyroPrevTime = System.currentTimeMillis();
		}

		// Get current elapsed time
		long currTime = System.currentTimeMillis();

		// Add gyro data to graph
		if (ENABLE_GYRO) {
			gyroX.addPoint(new Point((currTime - gyroPrevTime) / 1000.0, e.getGyroValue("x")));
		} else {
			// Add zero point if disabled for testing
			gyroX.addPoint(new Point((currTime - gyroPrevTime) / 1000.0, 0.0));
		}

		// Get processed data
		Point[] gyroXData = new Point[gyroX.getData().size()];
		gyroX.getData().toArray(gyroXData);
		
		// Determine speed and direction of rotation
		// TODO: Consider whether to change the values against which speed is compared in 
		// 		the if-else statement below
		double speed = gyroXData[gyroXData.length - 1].getY();
		if (speed < 0) {
			nxt.turnLeft(speed * -1 / GYROX_POS_MAX);
			ui.setDirection(NXTControlPanelUI.LEFT);
			isTurning = true;
		} else if (speed != 0) {
			nxt.turnRight(speed / GYROX_POS_MAX);
			ui.setDirection(NXTControlPanelUI.RIGHT);
			isTurning = true;
		} else if (isTurning && speed==0) {
			if (moving && forward) {
				nxt.forward(NXT_SPEED);
				ui.setDirection(NXTControlPanelUI.FORWARD);
			} else if (moving && !forward) {
				nxt.backward(NXT_SPEED);
				ui.setDirection(NXTControlPanelUI.BACKWARD);
			} else if (!moving) {
				nxt.stop();
				ui.setDirection(NXTControlPanelUI.STOP);
			}
			isTurning = false;
			// TODO: Consider recalibrating the gyroscope here, so that it resets every time.
		}
		// Record speed values in an array to output to a file
		allSpeeds.add( new Double(speed) );

	}

	public static void main(String[] args) {
		// TODO Request values for Occipital and Blink thresholds
		// Use GUI that has entry fields for the threshold values
		// to assign values. Check against a NaN error to ensure 
		// that the thresholds are set, then continue execution.
		
		// Create an object of this class
		new NXTControlPanel();

	}
	
	/* ********** Unimplemented Methods ********** */
	// TODO: Add battery, quality indicators
	
	/* (non-Javadoc)
	 * @see EmotivObserver#batteryChanged(Emotiv)
	 */
	@Override
	public void batteryChanged(Emotiv e) {}

	/* (non-Javadoc)
	 * @see EmotivObserver#qualityChanged(Emotiv, java.lang.String, int)
	 */
	@Override
	public void qualityChanged(Emotiv e, String sensor, int quality) {}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyTyped(KeyEvent e) {}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyReleased(KeyEvent e) {}

}
