package waffleoRai_NTDExGUI.dialogs;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collection;

import javax.swing.JDialog;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import waffleoRai_Files.Converter;
import waffleoRai_GUITools.InExListPair;

import javax.swing.JButton;

public class ConvertDumpDialog extends JDialog{

	private static final long serialVersionUID = -6525002092475893054L;

	public static final int WIDTH = 470;
	public static final int HEIGHT = 300;
	
	private InExListPair<Converter> list_pair;
	private boolean confirm;
	
	public ConvertDumpDialog(Frame parent, Collection<Converter> available)
	{
		super(parent, true);
		
		initGUI(parent, available);
		
	}

	private void initGUI(Frame parent, Collection<Converter> available)
	{
		setTitle("Convert & Dump");
		setResizable(false);
		getContentPane().setLayout(null);
		Dimension sz = new Dimension(WIDTH, HEIGHT);
		this.setMinimumSize(sz);
		this.setMaximumSize(sz);
		this.setLocationRelativeTo(parent);
		
		JLabel lblTheFollowingFormats = new JLabel("The following formats are available for conversion:");
		lblTheFollowingFormats.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblTheFollowingFormats.setBounds(10, 11, 254, 14);
		getContentPane().add(lblTheFollowingFormats);
		
		JScrollPane spIn = new JScrollPane();
		spIn.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		spIn.setBounds(10, 56, 184, 165);
		getContentPane().add(spIn);
		
		JList<Converter> lstIn = new JList<Converter>();
		spIn.setViewportView(lstIn);
		
		JScrollPane spEx = new JScrollPane();
		spEx.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		spEx.setBounds(268, 56, 184, 165);
		getContentPane().add(spEx);
		
		JList<Converter> lstEx = new JList<Converter>();
		spEx.setViewportView(lstEx);
		
		JLabel lblInclude = new JLabel("Include");
		lblInclude.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblInclude.setBounds(10, 36, 46, 14);
		getContentPane().add(lblInclude);
		
		JLabel lblExclude = new JLabel("Exclude");
		lblExclude.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblExclude.setBounds(268, 37, 46, 14);
		getContentPane().add(lblExclude);
		
		JButton btnEx = new JButton("->");
		btnEx.setBounds(203, 94, 55, 23);
		getContentPane().add(btnEx);
		
		
		JButton btnIn = new JButton("<-");
		btnIn.setBounds(204, 128, 55, 23);
		getContentPane().add(btnIn);
		btnIn.setEnabled(false);
		
		JButton btnContinue = new JButton("Continue");
		btnContinue.setBounds(367, 237, 89, 23);
		getContentPane().add(btnContinue);
		btnContinue.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				confirm = true;
				closeMe();
			}
			
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(268, 237, 89, 23);
		getContentPane().add(btnCancel);
		btnCancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				confirm = false;
				closeMe();
			}
			
		});
		
		list_pair = new InExListPair<Converter>(lstEx, lstIn, available, available);
		
		btnEx.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				list_pair.excludeSelected();
				btnEx.setEnabled(!lstIn.isSelectionEmpty());
				btnIn.setEnabled(!lstEx.isSelectionEmpty());
			}
			
		});
		lstIn.addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent e) 
			{
				btnEx.setEnabled(!lstIn.isSelectionEmpty());
			}
			
		});
		btnIn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				list_pair.includeSelected();
				btnEx.setEnabled(!lstIn.isSelectionEmpty());
				btnIn.setEnabled(!lstEx.isSelectionEmpty());
			}
			
		});
		lstEx.addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent e) 
			{
				btnIn.setEnabled(!lstEx.isSelectionEmpty());
			}
			
		});
	}
	
	public void closeMe()
	{
		setVisible(false);
		WindowListener[] listeners = this.getWindowListeners();
		for(WindowListener l : listeners) l.windowClosing(new WindowEvent(this, 0));
		dispose();
	}
	
	public boolean isConfirmed()
	{
		return confirm;
	}
	
	public Collection<Converter> getIncluded()
	{
		return list_pair.getSourceIncludeList();
	}
	
}
