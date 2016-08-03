package edu.lafayette.bci;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * @author Brandon T. Smith
 *
 */
public class NXTControlPanelUI implements WindowListener {

	// Constant values for directional movement
	public static final int FORWARD = 1;
	public static final int BACKWARD = 1 << 1;
	public static final int LEFT = 1 << 2; // Counterclockwise turn
	public static final int RIGHT = 1 << 3; // Clockwise turn
	public static final int STOP = 0;

	// Keeps track of current state
	private int currDirection = STOP;
	
	// Window and window closed flags
	private JFrame window = null;
	private boolean closed = false;

	// UI components
	private JLabel image = null;
	private JLabel topLabel = null;
	private JLabel bottomLabel = null;

	/**
	 * Creates a new NXTControlPanelUI
	 */
	public NXTControlPanelUI() {
		// Create the window
		window = new JFrame("NXTControlPanel");
		window.setSize(800, 800);
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.addWindowListener(this);
		
		// Set the background color and border
		window.getContentPane().setBackground(new Color(195, 195, 195));

		// Set layout manager
		BoxLayout b = new BoxLayout(window.getContentPane(), BoxLayout.PAGE_AXIS);
		window.setLayout(b);

		// Add space to top
		window.getContentPane().add(Box.createVerticalStrut(20));

		// Add the top label
		topLabel  = new JLabel("NXT Control Panel");
		topLabel.setFont(new Font("Marker Felt", Font.TRUETYPE_FONT, 30));
		topLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		window.getContentPane().add(topLabel);
		
		// assign NXT model to 'icon'
		ImageIcon icon = new ImageIcon(this.getClass().getResource("resources/NXT.png"));
		//ImageIcon icon = new ImageIcon("resources/NXT.png");
		image = new JLabel(icon);
		image.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		window.getContentPane().add(image);
		
		// Add the bottom label 
		bottomLabel = new JLabel("Start/Stop: Close Eyes, Toggle Forward/Back: Blink x5, Turn: Rotate Head (Reset Shift+C)");
		bottomLabel.setFont(new Font("Marker Felt", Font.TRUETYPE_FONT, 18));
		bottomLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		window.getContentPane().add(bottomLabel);
		
		window.setVisible(true);
	}
	
	/**
	 * Gets the current direction of movement from the UI panel
	 */
	public int getDirection() {
		return currDirection;
	}
	
	/**
	 * Sets the direction of movement in the UI panel
	 * 
	 * @param The constant value for the direction of movement
	 */
	public void setDirection(int direction) {
		// Don't set a direction if we aren't moving
		if (direction == currDirection) {
			return;
		}

		// Set the direction image
		switch(direction) {
			case FORWARD: 
				image.setIcon(new ImageIcon(this.getClass().getResource("resources/NXT_F.png"))); break;
			case BACKWARD: 
				image.setIcon(new ImageIcon(this.getClass().getResource("resources/NXT_B.png"))); break;
			case LEFT: 
				image.setIcon(new ImageIcon(this.getClass().getResource("resources/NXT_L.png"))); break;
			case RIGHT: 
				image.setIcon(new ImageIcon(this.getClass().getResource("resources/NXT_R.png"))); break;
			case STOP: // Same as default 
			default: 
				image.setIcon(new ImageIcon(this.getClass().getResource("resources/NXT.png")));
		}

		currDirection = direction;
	}
	
	/**
	 * Sets the text on the UI's bottom label.
	 * 
	 * @param text The new label text.
	 */
	public void setBottomLabel(String text) {
		bottomLabel.setText(text);
	}
	
	/**
	 * Sets the text on the UI's top label.
	 * 
	 * @param text The new label text.
	 */
	public void setTopLabel(String text) {
		topLabel.setText(text);
	}
	
	/**
	 * Adds a key listener to the window
	 * 
	 * @param k A class implementing the KeyListener interface
	 */
	public void addKeyListener(KeyListener k) {
		window.addKeyListener(k);
	}
	
	/**
	 * Indicates whether or not the window has been closed
	 * 
	 * @return
	 */
	public boolean isWindowClosed() {
		return closed;
	}

	/**************** WindowListener Methods *****************/

	/**
	 * Invoked when a window has been closed as the result of calling 
	 * dispose on the window.  This method sets the close flag to true.
	 */
	public void windowClosed(WindowEvent e) {
		closed = true;
	}

	/************* Unused Methods ***************/

	/**
	 * Unused
	 */
	public void windowActivated(WindowEvent e) {}

	/**
	 * Unused
	 */
	public void windowClosing(WindowEvent e) {}

	/**
	 * Unused
	 */
	public void windowDeactivated(WindowEvent e) {}

	/**
	 * Unused
	 */
	public void windowDeiconified(WindowEvent e) {}

	/**
	 * Unused
	 */
	public void windowIconified(WindowEvent e) {}

	/**
	 * Unused
	 */
	public void windowOpened(WindowEvent e) {}

}
