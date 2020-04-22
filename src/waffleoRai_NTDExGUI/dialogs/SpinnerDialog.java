package waffleoRai_NTDExGUI.dialogs;

import javax.swing.JDialog;
import javax.swing.JSpinner;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

public class SpinnerDialog extends JDialog{

	private static final long serialVersionUID = 5824112787061162588L;
	
	public static final int WIDTH = 210;
	public static final int HEIGHT = 100;	

	private boolean okay;
	private JSpinner spinner;
	//private int value;
	
	public SpinnerDialog(Frame parent, String title, String label){
		super(parent, true);
		
		setResizable(false);
		getContentPane().setLayout(null);
		
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setTitle(title);
		
		spinner = new JSpinner();
		spinner.setBounds(106, 11, 54, 20);
		getContentPane().add(spinner);
		
		JButton btnOk = new JButton("OK");
		btnOk.setBounds(106, 42, 89, 23);
		getContentPane().add(btnOk);
		btnOk.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				okay = true;
				closeMe();
			}
			
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(10, 42, 89, 23);
		getContentPane().add(btnCancel);
		btnCancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				okay = false;
				closeMe();
			}
			
		});
		
		JLabel lblNewLabel = new JLabel(label + ":");
		lblNewLabel.setBounds(10, 14, 46, 14);
		getContentPane().add(lblNewLabel);
		
	}
	
	public boolean getConfirmed(){
		return okay;
	}
	
	public int getValue(){
		return (Integer)spinner.getValue();
	}
	
	public void closeMe(){
		this.setVisible(false);
	}
	
}
