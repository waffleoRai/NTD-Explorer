package waffleoRai_NTDExGUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class TreePopupMenu extends JPopupMenu{

	private static final long serialVersionUID = -6211839365136568808L;
	
	public static final int MENU_OP_NONE = 0;
	public static final int MENU_OP_RENAME = 1;
	public static final int MENU_OP_NEWDIR = 2; //Directory only
	public static final int MENU_OP_MOVEME = 3;
	public static final int MENU_OP_SPLIT = 4; //File only
	public static final int MENU_OP_EXTRACT = 5; 
	public static final int MENU_OP_EXPORT = 6;
	public static final int MENU_OP_VIEW = 7;
	
	private boolean isDir;
	private int selection;
	
	public TreePopupMenu(){this(false);}
	
	public TreePopupMenu(boolean isDirectory) 
	{
		isDir = isDirectory;
		
		JMenuItem opRename = new JMenuItem("Rename...");
		add(opRename);
		opRename.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				selection = MENU_OP_RENAME;
				closeMe();
			}
			
		});

		JMenuItem opMove = new JMenuItem("Move To...");
		add(opMove);
		opMove.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				selection = MENU_OP_MOVEME;
				closeMe();
			}
			
		});

		JMenuItem opExtract = new JMenuItem("Extract...");
		add(opExtract);
		opExtract.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				selection = MENU_OP_EXTRACT;
				closeMe();
			}
			
		});

		JMenuItem opExport = new JMenuItem("Export...");
		add(opExport);
		opExport.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				selection = MENU_OP_EXPORT;
				closeMe();
			}
			
		});
		
		JMenuItem opView = new JMenuItem("View...");
		add(opView);
		opView.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				selection = MENU_OP_VIEW;
				closeMe();
			}
			
		});

		JSeparator separator = new JSeparator();
		add(separator);
		
		if(isDir)
		{
			JMenuItem opNewDir = new JMenuItem("New Directory...");
			add(opNewDir);
			opNewDir.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) 
				{
					selection = MENU_OP_NEWDIR;
					closeMe();
				}
				
			});
		}
		else
		{
			JMenuItem opSplit = new JMenuItem("Split...");
			add(opSplit);
			opSplit.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) 
				{
					selection = MENU_OP_SPLIT;
					closeMe();
				}
				
			});
		}
	
	}
	
	public int getSelection()
	{
		return selection;
	}
	
	public void closeMe()
	{
		setVisible(false);
	}
	
}
