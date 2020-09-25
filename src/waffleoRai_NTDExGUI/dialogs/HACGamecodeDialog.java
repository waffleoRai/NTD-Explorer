package waffleoRai_NTDExGUI.dialogs;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JButton;

public class HACGamecodeDialog extends JDialog{

	private static final long serialVersionUID = -2576871515124049189L;
	
	public static final int SELECTION_CANCEL = 0;
	public static final int SELECTION_OKAY = 1;
	public static final int SELECTION_UNKNOWN = 2;
	
	private JTextField txtCode;
	
	private int select;

	public HACGamecodeDialog(Frame parent){
		super(parent, true);
		setResizable(false);
		setLocationRelativeTo(parent);
		
		setTitle("Switch Gamecode");
		getContentPane().setLayout(null);
		
		JLabel lblPleaseEnter = new JLabel("Please enter 5 character Switch software code:");
		lblPleaseEnter.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblPleaseEnter.setHorizontalAlignment(SwingConstants.CENTER);
		lblPleaseEnter.setBounds(67, 11, 267, 14);
		getContentPane().add(lblPleaseEnter);
		
		JLabel lblForPhysicalCopies = new JLabel("For physical copies, this can be found on the cart label and case.");
		lblForPhysicalCopies.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblForPhysicalCopies.setHorizontalAlignment(SwingConstants.CENTER);
		lblForPhysicalCopies.setBounds(40, 30, 324, 14);
		getContentPane().add(lblForPhysicalCopies);
		
		JLabel lblHacP = new JLabel("HAC   P   ");
		lblHacP.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblHacP.setBounds(134, 61, 59, 14);
		getContentPane().add(lblHacP);
		
		txtCode = new JTextField();
		txtCode.setBounds(203, 59, 77, 20);
		getContentPane().add(txtCode);
		txtCode.setColumns(10);
		
		JButton btnOkay = new JButton("Okay");
		btnOkay.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnOkay.setBounds(335, 91, 89, 23);
		getContentPane().add(btnOkay);
		btnOkay.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				select = SELECTION_OKAY;
				closeMe();
			}
			
		});
		
		JButton btnUnknown = new JButton("I Don't Know It");
		btnUnknown.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnUnknown.setBounds(223, 91, 111, 23);
		getContentPane().add(btnUnknown);
		btnUnknown.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				select = SELECTION_UNKNOWN;
				closeMe();
			}
			
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnCancel.setBounds(10, 91, 89, 23);
		getContentPane().add(btnCancel);
		btnCancel.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				select = SELECTION_CANCEL;
				closeMe();
			}
			
		});
		
	}
	
	public int getSelection(){
		return select;
	}
	
	public String getCode(){
		String code = txtCode.getText();
		if(code == null) code = "";
		if(code.length() > 5) code = code.substring(0, 5);
		while(code.length() < 5) code += "0";
		return code;
	}
	
	public void closeMe(){
		this.setVisible(false);
		this.dispose();
	}
	
}
