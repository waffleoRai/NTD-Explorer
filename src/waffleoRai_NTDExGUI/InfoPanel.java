package waffleoRai_NTDExGUI;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;

import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExGUI.dialogs.OpenDialog;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

public class InfoPanel extends JPanel{

	private static final long serialVersionUID = -2849616410459530735L;
	
	public static final int MIN_WIDTH = 575;
	public static final int MAX_WIDTH = 1080;
	public static final int HEIGHT = 75;
	
	public static final int ICO_Y1 = 10;
	public static final int ICO_Y2 = 22;
	public static final int ICO_Y3 = 34;
	
	public static final int ICO_Y1_ALT = 16;
	public static final int ICO_Y3_ALT = 28;
	
	public static final int BANNER_FONT_SIZE = 10;
	
	private static BufferedImage pnlbkg;
	private static BufferedImage pnlbnr;
	
	private String bnr1;
	private String bnr2;
	private String bnr3;
	
	private Image img;

	public InfoPanel(NTDProject proj){
		
		if(pnlbkg == null){
			try{
				pnlbkg = ImageIO.read(OpenDialog.class.getResource("/waffleoRai_NTDExCore/res/topbar.png"));
				pnlbnr = ImageIO.read(OpenDialog.class.getResource("/waffleoRai_NTDExCore/res/bannerplaque.png"));
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		
		initGUI(proj);
	}
	
	private void initGUI(NTDProject proj){
		setBackground(Color.DARK_GRAY);
		
		setLayout(null);
		Dimension minsize = new Dimension(MIN_WIDTH, HEIGHT);
		setMinimumSize(minsize);
		setPreferredSize(new Dimension(660, 75));
		
		if(proj != null){
			img = proj.getIconImage(48, 48);
		}
		if(img == null){
			try {img = NTDProgramFiles.getDefaultImage_unknown().getScaledInstance(48, 48, BufferedImage.SCALE_DEFAULT);} 
			catch (IOException e) {e.printStackTrace();}	
		}
		
		JPanel pnlImage = new JPanel()
		{
			private static final long serialVersionUID = 4180222039185893441L;

			public void paintComponent(Graphics g) 
			{
				super.paintComponent(g);
		        Graphics2D g2d = (Graphics2D)g;
		        g2d.drawImage(img, 2, 2, null);
		        
		        
		        
		    }
		};		
		pnlImage.setOpaque(false);
		pnlImage.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		pnlImage.setBounds(10, 11, 52, 52);
		add(pnlImage);
		
		String defo_str = "NO GAME LOADED";
		
		JPanel pnlBanner = new JPanel(){
			private static final long serialVersionUID = -169340384323115201L;

			public void paintComponent(Graphics g){
				super.paintComponent(g);
				if(pnlbnr != null) g.drawImage(pnlbnr, 0, 0, null);
				//drawBannerText(g);
			}
		};
		pnlBanner.setOpaque(false);
		//pnlBanner.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		pnlBanner.setBackground(Color.DARK_GRAY);
		pnlBanner.setBounds(250, 7, 180, 60);
		add(pnlBanner);
		pnlBanner.setLayout(null);
		
		/*JLabel lblTitle2 = new JLabel("testtesttest");
		lblTitle2.setBounds(2, 24, 176, 12);
		pnlBanner.add(lblTitle2);
		lblTitle2.setForeground(Color.LIGHT_GRAY);
		lblTitle2.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle2.setFont(new Font("Arial Unicode MS", Font.PLAIN, 10));
		
		JLabel lblTitle1 = new JLabel("No image loaded");
		lblTitle1.setBounds(2, 12, 176, 12);
		pnlBanner.add(lblTitle1);
		lblTitle1.setForeground(Color.LIGHT_GRAY);
		lblTitle1.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle1.setFont(new Font("Arial Unicode MS", Font.PLAIN, 10));
		
		JLabel lblTitle3 = new JLabel("testtesttest");
		lblTitle3.setBounds(2, 36, 176, 12);
		pnlBanner.add(lblTitle3);
		lblTitle3.setForeground(Color.LIGHT_GRAY);
		lblTitle3.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle3.setFont(new Font("Arial Unicode MS", Font.PLAIN, 10));*/
		
		JLabel lblCode = new JLabel(defo_str);
		lblCode.setHorizontalAlignment(SwingConstants.CENTER);
		lblCode.setForeground(Color.LIGHT_GRAY);
		lblCode.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblCode.setBounds(68, 12, 120, 14);
		add(lblCode);
		if(proj != null) lblCode.setText(proj.getGameCode12().replace("_", "-"));
		
		defo_str = "Console Unknown";
		JLabel lblConsole = new JLabel(defo_str);
		lblConsole.setHorizontalAlignment(SwingConstants.CENTER);
		lblConsole.setForeground(Color.LIGHT_GRAY);
		lblConsole.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblConsole.setBounds(68, 48, 120, 14);
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
			case WIIU: lblConsole.setText("Nintendo Wii U"); break;
			case _3DS: lblConsole.setText("Nintendo 3DS"); break;
			case PS1: lblConsole.setText("Sony PlayStation 1"); break;
			default: lblConsole.setText("Console Unknown"); break;
			}
		}
		
		//28
		/*if(proj != null)
		{
			String[] lines = proj.getBannerLines();
			if(lines.length == 1){
				//lblTitle1.setText("");
				//lblTitle2.setText(lines[0]);
				lblTitle1.setText(lines[0]);
				
				OffsetDateTime time = proj.getVolumeTime();
				if(time == null){
					lblTitle3.setText(proj.getPublisherTag());
				}
				else{
					lblTitle2.setText(proj.getPublisherTag());
					lblTitle3.setText(NTDProject.getDateTimeString(proj.getVolumeTime()));
				}

			}
			else if(lines.length == 2){
				lblTitle1.setText(lines[0]);
				lblTitle3.setText(lines[1]);
				
				Rectangle bounds = lblTitle1.getBounds();
				bounds.y = ICO_Y1_ALT;
				lblTitle1.setBounds(bounds);
				
				bounds = lblTitle3.getBounds();
				bounds.y = ICO_Y3_ALT;
				lblTitle3.setBounds(bounds);
			}
			else if(lines.length >= 3){
				lblTitle1.setText(lines[0]);
				lblTitle2.setText(lines[1]);
				lblTitle3.setText(lines[2]);
			}
		}*/
		
		if(proj != null){
			bnr1 = null; bnr2 = null; bnr3 = null;
			String[] lines = proj.getBannerLines();
			if(lines != null){
				if(lines.length >= 1) bnr1 = lines[0];
				if(lines.length >= 2) bnr2 = lines[1];
				if(lines.length >= 3) bnr3 = lines[2];
			}
		}
		else{
			bnr1 = "No image loaded";
			bnr2 = null;
			bnr3 = null;
		}
		
		JLabel lblVersion = new JLabel("Version 0.0.0");
		lblVersion.setHorizontalAlignment(SwingConstants.CENTER);
		lblVersion.setForeground(Color.LIGHT_GRAY);
		lblVersion.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblVersion.setBounds(68, 28, 120, 14);
		add(lblVersion);
		if(proj != null) lblVersion.setText("Version " + proj.getCurrentVersionString());
		
		/*if(proj != null){
			if(proj.isEncrypted()){
				if(proj.getConsole() == Console.WII || proj.getConsole() == Console.WIIU) lblEnc.setText("AES-128-CBC");
				else if(proj.getConsole() == Console.DSi) lblEnc.setText("Modcrypt");
				else if(proj.getConsole() == Console._3DS) lblEnc.setText("AES-128-CTR");
				else if(proj.getConsole() == Console.SWITCH) lblEnc.setText("NX AES-CTR/XTSAES");
				//lblEnc.setForeground(Color.RED);
			}
			else{
				lblEnc.setText("None");
				//lblEnc.setForeground(Color.GREEN);
			}
		}*/
		
		int x = 2;
		if(bnr3 == null && bnr2 != null){
			//2 lines
			int y = 18;
			pnlBanner.add(genBannerLabel(x, y, bnr1, false));
			pnlBanner.add(genBannerLabel(x, y, bnr1, true));
			
			y += 12;
			pnlBanner.add(genBannerLabel(x, y, bnr2, false));
			pnlBanner.add(genBannerLabel(x, y, bnr2, true));
		}
		else{
			//1 or 3 lines
			if(bnr2 == null){
				//1 line
				pnlBanner.add(genBannerLabel(x, 24, bnr1, false));
				pnlBanner.add(genBannerLabel(x, 24, bnr1, true));
			}
			else{
				int y = 12;
				pnlBanner.add(genBannerLabel(x, y, bnr1, false));
				pnlBanner.add(genBannerLabel(x, y, bnr1, true));
				
				y += 12;
				pnlBanner.add(genBannerLabel(x, y, bnr2, false));
				pnlBanner.add(genBannerLabel(x, y, bnr2, true));
				
				y += 12;
				pnlBanner.add(genBannerLabel(x, y, bnr3, false));
				pnlBanner.add(genBannerLabel(x, y, bnr3, true));
			}
		}
		
	}
	
	private JLabel genBannerLabel(int x, int y, String s, boolean shadow){
		if(shadow){
			x+=2; y+=2;
		}
		
		JLabel lblBanner = new JLabel(s);
		lblBanner.setBounds(x, y, 176, 12);
		if(shadow) lblBanner.setForeground(Color.darkGray);
		else lblBanner.setForeground(Color.LIGHT_GRAY);
		lblBanner.setHorizontalAlignment(SwingConstants.CENTER);
		lblBanner.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, BANNER_FONT_SIZE));
		
		return lblBanner;
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		if(pnlbkg != null) g.drawImage(pnlbkg, 0, 0, null);
	}
	
}
