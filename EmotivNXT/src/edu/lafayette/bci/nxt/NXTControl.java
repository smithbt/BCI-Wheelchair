package edu.lafayette.bci.nxt;

import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;

/**
 * This class provides control methods for the LEGO NXT robot.  
 * The commands are transmitted via Bluetooth, so there will be some
 * delay before the NXT responds. Bluetooth must be enabled on both
 * the computer and the NXT for this class to work.
 * 
 * @author Brandon T. Smith
 *
 */
public class NXTControl {
	
	// Constant Variables for wheel diameter and track width (in mm).
	// Also defines a DifferenialPilot object to control the robot. 
	private static final float WHEEL_DIAMETER=37f;
	private static final float TRACK_WIDTH=129f;
	private DifferentialPilot robot = null;

	/**
	 * Initializes DifferentialPilot robot
	 */
	public NXTControl() {
		robot = new DifferentialPilot(WHEEL_DIAMETER, TRACK_WIDTH, Motor.B, Motor.A);		
	}
	
	/**
	 * Causes the robot to start moving forward.
	 * 
	 * @param speed The percentage value of the maximum speed at which the robot should travel.
	 */
	public void forward(double speed) {
		robot.setTravelSpeed(limiter(speed, 0, 1) * robot.getMaxTravelSpeed());
		robot.forward();
	}
	
	/**
	 * Causes the robot to start moving backward.
	 * 
	 * @param speed The percentage value of the maximum speed at which the robot should travel.
	 */
	public void backward(double speed) {
		robot.setTravelSpeed(limiter(speed, 0, 1) * robot.getMaxTravelSpeed());
		robot.backward();
	}
	
	/**
	 * Causes the robot to turn to the left at a given speed. 
	 * 
	 * @param speed The percentage value of the maximum speed at which the robot should turn.
	 */
	public void turnLeft(double speed) {
		robot.setRotateSpeed(limiter(speed, 0, 1) * robot.getMaxRotateSpeed());
		robot.rotateLeft();
	}

	/**
	 * Causes the robot to turn to the right at a given speed.
	 * 
	 * @param speed The percentage value of the maximum speed at which the robot should turn.
	 */
	public void turnRight(double speed) {
		robot.setRotateSpeed(limiter(speed, 0, 1) * robot.getMaxRotateSpeed());
		robot.rotateRight();
	}
	
	/**
	 * Causes the robot to stop moving.
	 */
	public void stop() {
		robot.stop();
	}
	
	/**
	 * A helper function that limits a value to the min and max values.
	 * 
	 * @param value The value to limit
	 * @param min The maximum allowable value
	 * @param max The minimum allowable value
	 * @return The limited value
	 */
	private double limiter(double value, double min, double max) {
		return (value < min) ? min : (value > max) ? max : value;
	}

}
