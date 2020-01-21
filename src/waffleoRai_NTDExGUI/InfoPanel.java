package waffleoRai_NTDExGUI;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.border.SoftBevelBorder;

import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;

import javax.swing.border.BevelBorder;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.SwingConstants;

public class InfoPanel extends JPanel{

	private static final long serialVersionUID = -2849616410459530735L;
	
	public static final int MIN_WIDTH = 450;
	public static final int HEIGHT = 55;
	
	private Image img;

	public InfoPanel(NTDProject proj)
	{
		initGUI(proj);
	}
	
	private void initGUI(NTDProject proj)
	{
		setLayout(null);
		Dimension minsize = new Dimension(MIN_WIDTH, HEIGHT);
		setMinimumSize(minsize);
		setPreferredSize(minsize);
		
		if(proj != null)
		{
			img = proj.getBannerIcon()[0].getScaledInstance(32, 32, BufferedImage.SCALE_SMOOTH);
		}
		if(img == null)
		{
			try {img = NTDProgramFiles.getDefaultImage_unknown().getScaledInstance(32, 32, BufferedImage.SCALE_SMOOTH);} 
			catch (IOException e) {e.printStackTrace();}	
		}
		JPanel pnlImage = new JPanel()
		{
			private static final long serialVersionUID = 4180222039185893441L;

			public void paint(Graphics g) 
			{
		        Graphics2D g2d = (Graphics2D)g;
		        g2d.drawImage(img, 0, 0, null);
		    }
		};		
		pnlImage.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		pnlImage.setBounds(10, 11, 32, 32);
		add(pnlImage);
		
		String defo_str = "NO GAME LOADED";
		JLabel lblCode = new JLabel(defo_str);
		lblCode.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblCode.setBounds(52, 11, 107, 14);
		add(lblCode);
		if(proj != null) lblCode.setText(proj.getGameCode12().replace("_", "-"));
		
		defo_str = "Console Unknown";
		JLabel lblConsole = new JLabel(defo_str);
		lblConsole.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblConsole.setBounds(52, 29, 107, 14);
		add(lblConsole);
		if(proj != null)
		{
			switch(proj.getConsole())
			{
			case DS: lblConsole.setText("Nintendo DS"); break;
			case DSi: lblConsole.setText("Nintendo DS/DSi"); break;
			case GAMECUBE: lblConsole.setText("Nintendo GameCube"); break;
			case GB: lblConsole.setText("Nintendo GameBoy"); break;
			case GBA: lblConsole.setText("GameBoy Advance"); break;
			case GBC: lblConsole.setText("GameBoy Color"); break;
			case N64: lblConsole.setText("Nintendo 64"); break;
			case NES: lblConsole.setText("Nintendo Entertainment System"); break;
			case NEW_3DS: lblConsole.setText("Nintendo New 3DS"); break;
			case SNES: lblConsole.setText("Super Nintendo Entertainment System"); break;
			case SWITCH: lblConsole.setText("Nintendo Switch"); break;
			case UNKNOWN: lblConsole.setText("Console Unknown"); break;
			case WII: lblConsole.setText("Nintendo Wii"); break;
			case WIIU: lblConsole.setText("Nintendo WiiU"); break;
			case _3DS: lblConsole.setText("Nintendo 3DS"); break;
			default: lblConsole.setText("Console Unknown"); break;
			}
		}
		
		JLabel lblTitle1 = new JLabel("No image loaded");
		lblTitle1.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle1.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 10));
		lblTitle1.setBounds(186, 10, 118, 12);
		add(lblTitle1);
		
		JLabel lblTitle2 = new JLabel("");
		lblTitle2.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle2.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 10));
		lblTitle2.setBounds(186, 22, 118, 12);
		add(lblTitle2);
		
		JLabel lblTitle3 = new JLabel("");
		lblTitle3.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle3.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 10));
		lblTitle3.setBounds(186, 34, 118, 12);
		add(lblTitle3);
		
		//28
		if(proj != null)
		{
			String btitle = proj.getBannerTitle();
			String[] lines = btitle.split("\n");
			if(lines.length == 1)
			{
				lblTitle1.setText("");
				lblTitle2.setText(lines[0]);
			}
			else if(lines.length == 2)
			{
				lblTitle1.setText(lines[0]);
				lblTitle3.setText(lines[1]);
				
				Rectangle bounds = lblTitle1.getBounds();
				bounds.y = 16;
				lblTitle1.setBounds(bounds);
				
				bounds = lblTitle3.getBounds();
				bounds.y = 28;
				lblTitle3.setBounds(bounds);
			}
			else if(lines.length >= 3)
			{
				lblTitle1.setText(lines[0]);
				lblTitle2.setText(lines[1]);
				lblTitle3.setText(lines[2]);
			}
		}
		
		JLabel lblEncryption = new JLabel("Encryption:");
		lblEncryption.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblEncryption.setBounds(314, 11, 55, 14);
		add(lblEncryption);
		
		JLabel lblEnc = new JLabel("N/A");
		//lblEnc.setForeground(new Color(0, 128, 0));
		lblEnc.setBounds(371, 11, 79, 14);
		add(lblEnc);
		if(proj != null)
		{
			if(proj.isEncrypted())
			{
				if(proj.getConsole() == Console.WII) lblEnc.setText("AES-128");
				else if(proj.getConsole() == Console.DSi) lblEnc.setText("Modcrypt");
				lblEnc.setForeground(Color.RED);
			}
			else
			{
				lblEnc.setText("None");
				lblEnc.setForeground(Color.GREEN);
			}
		}
	}
	
}
