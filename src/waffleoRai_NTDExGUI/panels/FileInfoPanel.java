package waffleoRai_NTDExGUI.panels;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import waffleoRai_Files.FileTypeNode;
import waffleoRai_Utils.FileNode;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import javax.swing.border.BevelBorder;
import java.awt.Insets;

/*
 * Name
 * /Full/Path/Name
 * Detected Format:
 * Compression:
 * Encryption:
 * File Type: (Group: like archive, sound etc.)
 * 
 * ROM Offset:
 * Size:
 * 
 */

public class FileInfoPanel extends JPanel{

	private static final long serialVersionUID = 8306200207814899162L;
	
	private static final int MIN_WIDTH = 150;
	private static final int MIN_HEIGHT = 145;

	private FileNode loaded_node;
	//private TypeDefinition file_type;
	//private EncryptionType encryption;
	
	public FileInfoPanel(FileNode node)
	{
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		
		JPanel panel = new JPanel()
		{
			private static final long serialVersionUID = 8144654522128846798L;
			
			public void paintComponent(Graphics g) 
			{
				setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
				setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
				super.paintComponent(g);
				paintPanel(g);
			}
		};
		panel.setBackground(Color.WHITE);
		scrollPane.setViewportView(panel);
		
		
		loaded_node = node;
		//file_type = type;
		//encryption = enc;
	
	}
	
	public void paintPanel(Graphics g) 
	{
        Graphics2D g2d = (Graphics2D)g;
        Font myfont = g2d.getFont();
        
        int x_off = 10;
        int y_off = 10;
        
        String str = "<null>";
        if(loaded_node != null) str = loaded_node.getFileName();
        g2d.setFont(new Font("Courier New", Font.PLAIN, 12));
        g2d.drawString(str, x_off, y_off);
        y_off += 20;
        
        str = "<null>";
        if(loaded_node != null) str = loaded_node.getFullPath();
        g2d.setFont(new Font("Courier New", Font.PLAIN, 10));
        g2d.drawString(str, x_off, y_off);
        y_off += 15;
        
        str = "Detected Format: [Unknown]";
        FileTypeNode t = loaded_node.getTypeChainHead();
        if(t != null)
        {
        	while(t.getChild() != null) t = t.getChild();
        	str = "Detected Format: " + t.toString();
        }
        g2d.drawString(str, x_off, y_off);
        y_off += 15;
        
        str = "Compression: None";
        FileTypeNode head = loaded_node.getTypeChainHead();
        if(head != null && head.isCompression())
        {
        	str = "Compression: " + head.toString();
        }
        g2d.drawString(str, x_off, y_off);
        y_off += 15;
        
        str = "Encryption: None";
        if(loaded_node.getEncryption() != null)
        {
        	str = "Encryption: " + loaded_node.getEncryption().getDescription();
        }
        g2d.drawString(str, x_off, y_off);
        y_off += 15;
        
        str = "File Type: None";
        if(t != null && t.getFileClass() != null)
        {
        	str = "File Type: " + t.getFileClass().toString();
        }
        g2d.drawString(str, x_off, y_off);
        y_off += 30;
        
        str = "ROM Offset: <null>";
        if(loaded_node != null)
        {
        	str = "ROM Offset: 0x" + Long.toHexString(loaded_node.getOffset());
        }
        g2d.drawString(str, x_off, y_off);
        y_off += 15;
        
        str = "Size: 0x00 (0 bytes)";
        if(loaded_node != null)
        {
        	str = "Size: 0x" + Long.toHexString(loaded_node.getLength());
        	str += " (" + loaded_node.getLength() + " bytes)";
        }
        g2d.drawString(str, x_off, y_off);
        
        
        g2d.setFont(myfont);
    }
	
}
