package waffleoRai_NTDExGUI.dialogs.imageinfo;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JDialog;

import waffleoRai_Containers.nintendo.NDS;
import waffleoRai_Containers.nintendo.NDS.DSExeData;

import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import javax.swing.border.BevelBorder;
import java.awt.Insets;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/*
 * 
 * 
 * 
 */

public class DSImageInfoDialog extends JDialog{

	private static final long serialVersionUID = -8254793571383432576L;
	
	public static final int WIDTH = 300;
	public static final int HEIGHT = 350;
	
	private NDS my_image;
	private JPanel pnlInfo;

	public DSImageInfoDialog(Frame parent, NDS image)
	{
		super(parent, true);
		this.setLocationRelativeTo(parent);
		my_image = image;
		initGUI();
	}
	
	private void initGUI()
	{
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		setTitle("DS Game Image Info");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		getContentPane().add(scrollPane, gbc_scrollPane);
		
		pnlInfo = new JPanel()
		{
			private static final long serialVersionUID = 830744516271989334L;

			public void paintComponent(Graphics g) 
			{
				super.paintComponent(g);
				paintInfoPanel((Graphics2D)g);
			}
		};
		pnlInfo.setBackground(Color.WHITE);
		scrollPane.setViewportView(pnlInfo);
	}
	
	private void paintInfoPanel(Graphics2D g2d)
	{
		if(my_image == null) return;
		
		Font myfont = g2d.getFont();
		
		final int y_space = 20;
        
        int x_off = 10;
        int y_off = 10;
        
        String str = "Title ID: " + my_image.getLongGameCode();
        g2d.setFont(new Font("Courier New", Font.PLAIN, 11));
        g2d.drawString(str, x_off, y_off);
        y_off += y_space;
        
        str = "Game Code: " + my_image.getGameCode();
        g2d.drawString(str, x_off, y_off);
        y_off += y_space;
        
        str = "Maker Code: " + my_image.getMakerCodeAsASCII();
        g2d.drawString(str, x_off, y_off);
        y_off += y_space;
        
        str = "DSi Software: " + my_image.hasTWL();
        g2d.drawString(str, x_off, y_off);
        y_off += y_space;
        
        str = "ROM Version: " + my_image.getROMVersion();
        g2d.drawString(str, x_off, y_off);
        y_off += y_space;
        
        if(my_image.hasTWL())
        {
        	str = "Game Revision: ";
            str += String.format("0x%04x", my_image.getGameRevision());
            g2d.drawString(str, x_off, y_off);
            y_off += y_space;
        }
        
        str = "Internal Flags: ";
        str += String.format("0x%04x", my_image.getFlags());
        g2d.drawString(str, x_off, y_off);
        y_off += y_space;
        
        str = "ARM9 Location: ";
        DSExeData arm9 = my_image.getARM9_data();
        if(arm9 != null)
        {
        	str += "0x" + Long.toHexString(arm9.getROMOffset());
        	str += " - 0x" + Long.toHexString(arm9.getROMOffset() + arm9.getSize());
        	g2d.drawString(str, x_off, y_off);
            y_off += y_space;	
            
            str = "ARM9 Load Address: 0x" + Long.toHexString(arm9.getLoadAddress());
            g2d.drawString(str, x_off, y_off);
            y_off += y_space;	
            
            str = "ARM9 Entry Address: 0x" + Long.toHexString(arm9.getEntryAddress());
            g2d.drawString(str, x_off, y_off);
            y_off += y_space;	
        }
        
        str = "ARM7 Location: ";
        DSExeData arm7 = my_image.getARM7_data();
        if(arm7 != null)
        {
        	str += "0x" + Long.toHexString(arm7.getROMOffset());
        	str += " - 0x" + Long.toHexString(arm7.getROMOffset() + arm7.getSize());
        	g2d.drawString(str, x_off, y_off);
            y_off += y_space;	
            
            str = "ARM7 Load Address: 0x" + Long.toHexString(arm7.getLoadAddress());
            g2d.drawString(str, x_off, y_off);
            y_off += y_space;	
            
            str = "ARM7 Entry Address: 0x" + Long.toHexString(arm7.getEntryAddress());
            g2d.drawString(str, x_off, y_off);
            y_off += y_space;	
        }
        
        g2d.setFont(myfont);
        
	}
	
}
