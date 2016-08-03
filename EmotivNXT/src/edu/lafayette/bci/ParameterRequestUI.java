package edu.lafayette.bci;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * A GUI class to display a simple window requesting values for the occipital 
 * alpha threshold and the frontal blink threshold.
 * 
 * @author Brandon T. Smith
 *
 */
public class ParameterRequestUI extends JDialog {
	
	// Threshold values
	private double blinkVal = Double.NaN;
	private double occipVal = Double.NaN;

	private final JPanel contentPanel = new JPanel();
	private JTextField blinkField;
	private JTextField occipField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ParameterRequestUI dialog = new ParameterRequestUI();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ParameterRequestUI() {
		setTitle("Enter Thresholds");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblEnterBlinkThreshold = new JLabel("Enter Blink Threshold");
			GridBagConstraints gbc_lblEnterBlinkThreshold = new GridBagConstraints();
			gbc_lblEnterBlinkThreshold.anchor = GridBagConstraints.EAST;
			gbc_lblEnterBlinkThreshold.insets = new Insets(0, 0, 5, 5);
			gbc_lblEnterBlinkThreshold.gridx = 0;
			gbc_lblEnterBlinkThreshold.gridy = 0;
			contentPanel.add(lblEnterBlinkThreshold, gbc_lblEnterBlinkThreshold);
		}
		{
			blinkField = new JTextField();
			GridBagConstraints gbc_blinkVal = new GridBagConstraints();
			gbc_blinkVal.insets = new Insets(0, 0, 5, 0);
			gbc_blinkVal.fill = GridBagConstraints.HORIZONTAL;
			gbc_blinkVal.gridx = 1;
			gbc_blinkVal.gridy = 0;
			contentPanel.add(blinkField, gbc_blinkVal);
			blinkField.setColumns(10);
		}
		{
			JLabel lblEnterOccipitalThreshold = new JLabel("Enter Occipital Threshold");
			GridBagConstraints gbc_lblEnterOccipitalThreshold = new GridBagConstraints();
			gbc_lblEnterOccipitalThreshold.anchor = GridBagConstraints.EAST;
			gbc_lblEnterOccipitalThreshold.insets = new Insets(0, 0, 5, 5);
			gbc_lblEnterOccipitalThreshold.gridx = 0;
			gbc_lblEnterOccipitalThreshold.gridy = 1;
			contentPanel.add(lblEnterOccipitalThreshold, gbc_lblEnterOccipitalThreshold);
		}
		{
			occipField = new JTextField();
			GridBagConstraints gbc_occipVal = new GridBagConstraints();
			gbc_occipVal.insets = new Insets(0, 0, 5, 0);
			gbc_occipVal.fill = GridBagConstraints.HORIZONTAL;
			gbc_occipVal.gridx = 1;
			gbc_occipVal.gridy = 1;
			contentPanel.add(occipField, gbc_occipVal);
			occipField.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// Display Warning if either field is null. 
						// Otherwise, assign values from fields.
						if (blinkField.getText() == null || occipField.getText() == null) {
							//TODO: Add warning. And stop button from closing window? 
						} 
						else {
							blinkVal = Double.parseDouble( blinkField.getText() );
							occipVal = Double.parseDouble( occipField.getText() );
						}
						
						// TODO: Add code to define behavior when "OK" button is clicked.
						// hide contentPanel
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public double getBlinkVal() {
		return blinkVal;
	}
	
	public double getOccipVal() {
		return occipVal;
	}

}
