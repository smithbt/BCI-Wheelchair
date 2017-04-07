/**
 * 
 */
package edu.lafayette.bci.wheelchair;

import jssc.*;

import javax.swing.*;

/**
 * @author Brandon T. Smith
 *
 */
public class ChairControl {
	
	SerialPort arduport = null;

	// Create an instance of ChairControl.
	public ChairControl (){
		// TODO: Determine best/correct COM port for Arduino link
		// Connect to COM port
		arduport = new SerialPort("COM1");
		try {
			arduport.setParams(9600, 8, 1, 0);
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			showErrorMessage("Could not set parameters for connection.");
		}
		
	}
	
	// Move the wheelchair forwards
	public void forward() {
		try {
			arduport.writeBytes("f".getBytes());
		} catch (SerialPortException e) {
			e.printStackTrace();
			showErrorMessage("Could not transmit command.");
		}
	}
	
	// Move the wheelchair backwards
	public void backward() {
		try {
			arduport.writeBytes("b".getBytes());
		} catch (SerialPortException e) {
			e.printStackTrace();
			showErrorMessage("Could not transmit command.");
		}
	}
	
	// Rotate the wheelchair to the left.
	public void turnLeft() {
		try {
			arduport.writeBytes("l".getBytes());
		} catch (SerialPortException e) {
			e.printStackTrace();
			showErrorMessage("Could not transmit command.");
		}
	}
	
	// Rotate the wheelchair to the right.
	public void turnRight() {
		try {
			arduport.writeBytes("r".getBytes());
		} catch (SerialPortException e) {
			e.printStackTrace();
			showErrorMessage("Could not transmit command.");
		}
	}
	
	// Stop the wheelchair.
	public void stop() {
		try {
			arduport.writeBytes("s".getBytes());
		} catch (SerialPortException e) {
			e.printStackTrace();
			showErrorMessage("Could not transmit command.");
		}
	}
	
	// close connection
	public void close() {
		try {
			arduport.closePort();
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			showErrorMessage("Failed to properly close connection.");
		}
	}
	
	private static void showErrorMessage(String message) {
		JOptionPane.showMessageDialog(null, message, 
				"Serial Port Exception", JOptionPane.ERROR_MESSAGE);
	}
}
