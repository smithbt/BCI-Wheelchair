package edu.lafayette.bci.nxt;

public class TestNXTSpeed {

	public static void main(String args[]) {
		NXTControl robot = new NXTControl();
		
		// forward 5 seconds
		robot.forward(0);
		try{ Thread.sleep(5000); }
		catch(Exception e){ e.printStackTrace(); };
		
		robot.stop();
		
		// turn right 2 seconds
		robot.turnRight(0);
		try{ Thread.sleep(2000); }
		catch(Exception e){ e.printStackTrace(); };
		
		robot.stop();
		
		// back 5 seconds
		robot.backward(0);
		try{ Thread.sleep(5000); }
		catch(Exception e){ e.printStackTrace(); };
		
		robot.stop();
		
		// turn right 2 seconds
		robot.turnLeft(0);
		try{ Thread.sleep(2000); }
		catch(Exception e){ e.printStackTrace(); };
		
		robot.stop();
	}
	
}