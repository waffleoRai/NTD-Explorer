package waffleoRai_NTDExGUI.panels;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.OffsetDateTime;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;

public class DefaultGameOpenButton extends AbstractGameOpenButton{
	
	private static final long serialVersionUID = -4908553170082491360L;
	
	public static final int WIDTH = 220;
	public static final int HEIGHT = 70;
	
	public static final int Y1 = 22;
	public static final int Y2 = 34;
	public static final int Y3 = 46;
	
	public static final int Y1_ALT = 28;
	public static final int Y3_ALT = 40;
	
	private JLabel lblL1;
	private JLabel lblL2;
	private JLabel lblL3;
	
	private static BufferedImage img_off;
	private static BufferedImage img_on;
	
	public DefaultGameOpenButton(){
		super();
		if(img_off == null){
			try{
				img_off = ImageIO.read(NTDProgramFiles.class.getResource("/waffleoRai_NTDExCore/res/general_gradient_btn_2.png"));
				img_on = ImageIO.read(NTDProgramFiles.class.getResource("/waffleoRai_NTDExCore/res/general_gradient_btn_3.png"));
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		super.initGUI();
	}
	
	protected void initSubclassGUI() {
		setBackground(SystemColor.menu);
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setLayout(null);
		
		Dimension sz = new Dimension(WIDTH, HEIGHT);
		setMinimumSize(sz);
		setMaximumSize(sz);
		setPreferredSize(new Dimension(220, 70));
		
		pnlIcon.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		pnlIcon.setBounds(10, 25, 32, 32);
		add(pnlIcon);
		sz = new Dimension(32,32);
		pnlIcon.setMinimumSize(sz);
		pnlIcon.setPreferredSize(sz);
		
		lblGameCode.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblGameCode.setHorizontalAlignment(SwingConstants.CENTER);
		lblGameCode.setBounds(61, 5, 105, 14);
		add(lblGameCode);
		
		lblL1 = new JLabel("ユニコードテストタイトル");
		lblL1.setHorizontalAlignment(SwingConstants.CENTER);
		//lblL1.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblL1.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 10));
		lblL1.setBounds(42, Y1, 146, 12);
		add(lblL1);
		
		lblL2 = new JLabel("Publisher Name (Maybe)");
		lblL2.setHorizontalAlignment(SwingConstants.CENTER);
		lblL2.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 10));
		lblL2.setBounds(42, Y2, 146, 12);
		add(lblL2);
		
		lblL3 = new JLabel("17 Jun 2020 23:41:00 CDT");
		lblL3.setHorizontalAlignment(SwingConstants.CENTER);
		lblL3.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 10));
		lblL3.setBounds(42, Y3, 146, 12);
		add(lblL3);
	}
	
	public BufferedImage getButtonImage(boolean selected) {
		if(selected) return img_on;
		return img_off;
	}
	
	protected void loadLabels(NTDProject data) {
		
		String banner = project.getBannerTitle();
		String[] lines = banner.split("\n");
		
		if(lines.length == 1)
		{
			lblL1.setText(project.getBannerTitle());
			lblL2.setText(project.getPublisherTag());
			OffsetDateTime time = project.getVolumeTime();
			lblL3.setText(NTDProject.getDateTimeString(time));
		}
		else if(lines.length == 2)
		{
			lblL1.setText(lines[0]);
			Rectangle b = lblL1.getBounds();
			b.y = Y1_ALT;
			lblL1.setBounds(b);
			
			lblL3.setText(lines[1]);
			b = lblL3.getBounds();
			b.y = Y3_ALT;
			lblL3.setBounds(b);
			
			lblL2.setText("");
			lblL2.setVisible(false);
		}
		else if(lines.length == 3)
		{
			lblL1.setText(lines[0]);
			lblL2.setText(lines[1]);
			lblL3.setText(lines[2]);
		}
		lblL1.repaint();
		lblL2.repaint();
		lblL3.repaint();
	}

	public void setLabelsDirect(String line1){
		lblL2.setText(line1);
		Rectangle b = lblL2.getBounds();
		b.y = Y2;
		lblL2.setBounds(b);
		
		lblL1.setText("");
		lblL3.setText("");
		
		lblL1.repaint(); lblL2.repaint(); lblL3.repaint();
	}
	
	public void setLabelsDirect(String line1, String line2){
		lblL1.setText(line1);
		Rectangle b = lblL1.getBounds();
		b.y = Y1_ALT;
		lblL1.setBounds(b);
		
		lblL3.setText(line2);
		b = lblL3.getBounds();
		b.y = Y3_ALT;
		lblL3.setBounds(b);
		
		lblL2.setText("");
		
		lblL1.repaint(); lblL2.repaint(); lblL3.repaint();
	}
	
	public void setLabelsDirect(String line1, String line2, String line3){
		lblL1.setText(line1);
		Rectangle b = lblL1.getBounds();
		b.y = Y1;
		lblL1.setBounds(b);
		
		lblL2.setText(line2);
		b = lblL2.getBounds();
		b.y = Y2;
		lblL2.setBounds(b);
		
		lblL3.setText(line3);
		b = lblL3.getBounds();
		b.y = Y3;
		lblL3.setBounds(b);
		
		lblL1.repaint(); lblL2.repaint(); lblL3.repaint();
	}
	
}
