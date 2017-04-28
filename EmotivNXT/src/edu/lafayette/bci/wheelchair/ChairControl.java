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
	
	private final static String PORT = "COM5";
	
	private SerialPort arduport = null;
	
	public static void main(String args[]) {
		ChairControl cc = new ChairControl();
		cc.forward();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		cc.backward();
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		cc.turnLeft();
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		cc.turnRight();
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		cc.stop();
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		cc.close();
		System.out.print("finished");
	}

	// Create an instance of ChairControl.
	public ChairControl (){
		// TODO: Determine best/correct COM port for Arduino link
		// Connect to COM port
		arduport = new SerialPort(PORT);
		try {
			System.out.println("Connected: " + arduport.openPort() );
			Thread.sleep(1000);
			arduport.setParams(SerialPort.BAUDRATE_9600, 
					SerialPort.DATABITS_8, 
					SerialPort.STOPBITS_1, 
					SerialPort.PARITY_NONE);
			Thread.sleep(1000);
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, 
					"Could not connect, or could not set parameters for connection.", 
					"Serial Port Exception", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Set up time inerrupted.",  
					"Interrupted Exception", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	// Move the wheelchair forwards
	public void forward() {
		try {
//			arduport.writeBytes("f/n".getBytes());
			System.out.println("Forward sent: " + arduport.writeString("f") );
		} catch (SerialPortException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not transmit 'forward' command.", 
					"Serial Port Exception", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	// Move the wheelchair backwards
	public void backward() {
		try {
			arduport.writeBytes("b".getBytes());
		} catch (SerialPortException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not transmit 'backwards' command.", 
					"Serial Port Exception", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	// Rotate the wheelchair to the left.
	public void turnLeft() {
		try {
			arduport.writeBytes("l".getBytes());
		} catch (SerialPortException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not transmit 'turn left' command.", 
					"Serial Port Exception", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	// Rotate the wheelchair to the right.
	public void turnRight() {
		try {
			arduport.writeBytes("r".getBytes());
		} catch (SerialPortException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not transmit 'turn right' command.", 
					"Serial Port Exception", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	// Stop the wheelchair.
	public void stop() {
		try {
			arduport.writeBytes("s/n".getBytes());
		} catch (SerialPortException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not transmit 'stop' command.", 
					"Serial Port Exception", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	// close connection
	public void close() {
		try {
			arduport.closePort();
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to properly close connection.", 
					"Serial Port Exception", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
}
