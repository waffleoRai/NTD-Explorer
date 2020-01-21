package waffleoRai_NTDExGUI.dialogs;

import javax.swing.JFrame;

import waffleoRai_GUITools.GUITools;
import waffleoRai_NTDExCore.NTDProgramFiles;

import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.Dimension;
import java.awt.Font;

public class IntroDialog extends JFrame{

	private static final long serialVersionUID = 5406100749555765665L;
	
	public static final int WIDTH = 275;
	public static final int HEIGHT = 270;
	
	public static final int SEL_NONE = 0;
	public static final int SEL_OPEN = 1;
	public static final int SEL_IMPORT = 2;
	public static final int SEL_MOVEINSTALL = 3;
	public static final int SEL_UNINSTALL = 4;
	public static final int SEL_EXIT = 5;
	
	private int selection;
	
	public IntroDialog()
	{	
		selection = SEL_NONE;
		initGUI();
	}
	
	private void initGUI()
	{
		Dimension sz = new Dimension(WIDTH, HEIGHT);
		setMinimumSize(sz);
		setPreferredSize(sz);
		setLocation(GUITools.getScreenCenteringCoordinates(this));
		
		setTitle("Welcome!");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {259};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JButton btnOpen = new JButton("Open Image");
		btnOpen.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_btnOpen = new GridBagConstraints();
		gbc_btnOpen.fill = GridBagConstraints.BOTH;
		gbc_btnOpen.insets = new Insets(5, 5, 5, 5);
		gbc_btnOpen.gridx = 0;
		gbc_btnOpen.gridy = 0;
		getContentPane().add(btnOpen, gbc_btnOpen);
		btnOpen.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selection = SEL_OPEN;
				closeMe();
			}
		});
		
		JButton btnImport = new JButton("Import ROM");
		btnImport.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_btnImport = new GridBagConstraints();
		gbc_btnImport.fill = GridBagConstraints.BOTH;
		gbc_btnImport.insets = new Insets(5, 5, 5, 5);
		gbc_btnImport.gridx = 0;
		gbc_btnImport.gridy = 1;
		getContentPane().add(btnImport, gbc_btnImport);
		btnImport.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selection = SEL_IMPORT;
				closeMe();
			}
		});
		
		JButton btnMove = new JButton("Move Installation");
		btnMove.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_btnMove = new GridBagConstraints();
		gbc_btnMove.fill = GridBagConstraints.BOTH;
		gbc_btnMove.insets = new Insets(5, 5, 5, 5);
		gbc_btnMove.gridx = 0;
		gbc_btnMove.gridy = 2;
		getContentPane().add(btnMove, gbc_btnMove);
		btnMove.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selection = SEL_MOVEINSTALL;
				closeMe();
			}
		});
		
		JButton btnUninstall = new JButton("Uninstall");
		btnUninstall.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_btnUninstall = new GridBagConstraints();
		gbc_btnUninstall.insets = new Insets(5, 5, 5, 5);
		gbc_btnUninstall.fill = GridBagConstraints.BOTH;
		gbc_btnUninstall.gridx = 0;
		gbc_btnUninstall.gridy = 3;
		getContentPane().add(btnUninstall, gbc_btnUninstall);
		btnUninstall.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selection = SEL_UNINSTALL;
				closeMe();
			}
		});
		
		JButton btnExit = new JButton("Exit");
		btnExit.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_btnExit = new GridBagConstraints();
		gbc_btnExit.insets = new Insets(5, 5, 5, 5);
		gbc_btnExit.fill = GridBagConstraints.BOTH;
		gbc_btnExit.gridx = 0;
		gbc_btnExit.gridy = 4;
		getContentPane().add(btnExit, gbc_btnExit);
		btnExit.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selection = SEL_EXIT;
				closeMe();
			}
		});
	
		if(NTDProgramFiles.getProjectMap().isEmpty()) btnOpen.setEnabled(false);
	}

	public void render()
	{
		pack();
		setVisible(true);
	}
	
	public void closeMe()
	{
		setVisible(false);
		WindowListener[] listeners = this.getWindowListeners();
		for(WindowListener l : listeners) l.windowClosing(new WindowEvent(this, 0));
		dispose();
	}
	
	public int getSelection(){return this.selection;}
	
}
