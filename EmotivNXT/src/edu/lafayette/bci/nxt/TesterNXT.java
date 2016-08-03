package edu.lafayette.bci.nxt;
/**
 * A Tester class to implement simple programs on the LEGO NXT. 
 * 
 * @author Brandon T. Smith
 *
 */
public class TesterNXT {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// instantiate nxt
		NXTControl_FullMoveBeta nxt = new NXTControl_FullMoveBeta();
		boolean moving=false;
		boolean forward=false;

		// move forward at half speed for 1 second
		double speed=0;
		forward=true;
		nxt.forward(0.5);
		moving=true;
		try {Thread.sleep(1000);} catch (Exception e){}
		
		// increment left turn speed while going forward
		while (speed<1) {
			nxt.turnLeft(speed, moving, forward);
			try {Thread.sleep(250);} catch (Exception e){}
			speed+=0.1;
		}
		
		// continue forward at half speed for 1 second
		forward=true;
		nxt.forward(0.5);
		moving=true;
		try {Thread.sleep(1000);} catch (Exception e){}
		
		// increment right turn speed while going forward
		speed=0;
		while (speed<1) {
			nxt.turnRight(speed, moving, forward);
			try {Thread.sleep(250);} catch (Exception e){}
			speed+=0.1;
		}
		
		// continue forward at half speed for 1 second, then stop
		forward=true;
		nxt.forward(0.5);
		moving=true;
		try {Thread.sleep(1000);} catch (Exception e){}
		nxt.stop();
		moving=false;
		
		// increment right turn speed while stopped
		speed=0;
		while (speed<1) {
			nxt.turnRight(speed, moving, forward);
			try {Thread.sleep(250);} catch (Exception e){}
			speed+=0.1;
		}
		nxt.stop();
		moving=false;
		
		// move forward at half speed for 1 second, then stop
		forward=true;
		nxt.forward(0.5);
		moving=true;
		try {Thread.sleep(1000);} catch (Exception e){}
		nxt.stop();
		moving=false;
		
		// increment left turn speed while stopped
		speed=0;
		while (speed<1) {
			nxt.turnLeft(speed, moving, forward);
			try {Thread.sleep(250);} catch (Exception e){}
			speed+=0.1;
		}
		nxt.stop();
		moving=false;
		
		// move back at half speed for 1 second
		forward=false;
		nxt.backward(0.5);
		moving=true;
		try {Thread.sleep(1000);} catch (Exception e){}
		
		// increment right turn while moving
		speed=0;
		while (speed<1) {
			nxt.turnRight(speed, moving, forward);
			try {Thread.sleep(250);} catch (Exception e){}
			speed+=0.1;
		}
		
		// continue back at half speed for 1 second
		forward=false;
		nxt.backward(0.5);
		moving=true;
		try {Thread.sleep(1000);} catch (Exception e){}
		
		// increment left turn speed while moving
		speed=0;
		while (speed<1) {
			nxt.turnLeft(speed, moving, forward);
			try {Thread.sleep(500);} catch (Exception e){}
			speed+=0.1;
		}
		
		// continue back at half speed for 1 second, then stop
		forward=false;
		nxt.backward(0.5);
		moving=true;
		try {Thread.sleep(1000);} catch (Exception e){}
		nxt.stop();
		moving=false;
		
		// increment right turn while stopped
		speed=0;
		while (speed<1) {
			nxt.turnRight(speed, moving, forward);
			try {Thread.sleep(250);} catch (Exception e){}
			speed+=0.1;
		}
		
		// continue back at half speed for 1 second, then stop
		forward=false;
		nxt.backward(0.5);
		moving=true;
		try {Thread.sleep(1000);} catch (Exception e){}
		nxt.stop();
		moving=false;
		
		// increment left turn speed while stopped
		speed=0;
		while (speed<1) {
			nxt.turnLeft(speed, moving, forward);
			try {Thread.sleep(250);} catch (Exception e){}
			speed+=0.1;
		}
		
		// continue back at half speed for 1 second, then stop
		forward=false;
		nxt.backward(0.5);
		moving=true;
		try {Thread.sleep(1000);} catch (Exception e){}
		nxt.stop();
		moving=false;
	}

}
