package waffleoRai_NTDExGUI.panels;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.border.EtchedBorder;

import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExGUI.dialogs.OpenDialog;

import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

@Deprecated
public class DSPreviewPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 3784246391258766887L;
	
	public static final int WIDTH = 220;
	public static final int HEIGHT = 70;
	
	private NTDProject project;
	private Timer timer;
	
	private BufferedImage[] iframes;
	private int current_frame;
	
	private JPanel pnlIcon;
	private JLabel lblGameCode;
	private JLabel lblL1;
	private JLabel lblL2;
	private JLabel lblL3;
	
	private List<ActionListener> listeners;
	private boolean selected;
	
	private BufferedImage bkg;

	public DSPreviewPanel()
	{
		listeners = new LinkedList<ActionListener>();
		selected = false;
		initGUI();
		timer = new Timer((int)Math.round(1000.0/60.0), this);
		bkg = OpenDialog.getButtonImage();
	}

	private void initGUI()
	{
		setBackground(SystemColor.menu);
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setLayout(null);
		
		Dimension sz = new Dimension(WIDTH, HEIGHT);
		setMinimumSize(sz);
		setMaximumSize(sz);
		setPreferredSize(new Dimension(220, 70));
		
		pnlIcon = new JPanel()
		{
			public static final long serialVersionUID = 7181179256315196396L;
			
			public void paintComponent(Graphics g) 
			{
		        Graphics2D g2d = (Graphics2D)g;
		        g2d.drawImage(iframes[current_frame], 0, 0, null);
		    }
		};
		pnlIcon.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		pnlIcon.setBounds(10, 25, 32, 32);
		add(pnlIcon);
		sz = new Dimension(32,32);
		pnlIcon.setMinimumSize(sz);
		pnlIcon.setPreferredSize(sz);
		
		lblGameCode = new JLabel("NTR-GAME-UNK");
		lblGameCode.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblGameCode.setHorizontalAlignment(SwingConstants.CENTER);
		lblGameCode.setBounds(61, 5, 105, 14);
		add(lblGameCode);
		
		lblL1 = new JLabel("this is a pretty long test title");
		lblL1.setHorizontalAlignment(SwingConstants.CENTER);
		//lblL1.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblL1.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 10));
		lblL1.setBounds(42, 22, 146, 12);
		add(lblL1);
		
		lblL2 = new JLabel("maybe something else");
		lblL2.setHorizontalAlignment(SwingConstants.CENTER);
		lblL2.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 10));
		lblL2.setBounds(42, 34, 146, 12);
		add(lblL2);
		
		lblL3 = new JLabel("probably Nintendo");
		lblL3.setHorizontalAlignment(SwingConstants.CENTER);
		lblL3.setFont(NTDProgramFiles.getUnicodeFont(Font.PLAIN, 10));
		lblL3.setBounds(42, 46, 146, 12);
		add(lblL3);
		
		this.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {
				onMouseDown();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				onMouseRelease();
			}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
			
		});
	}
	
	public void loadMe(NTDProject data, Timer external_timer)
	{
		project = data;
		
		lblGameCode.setText(project.getGameCode12().replace("_", "-"));
		lblGameCode.repaint();
		
		String banner = project.getBannerTitle();
		String[] lines = banner.split("\n");
		if(lines.length == 1)
		{
			lblL1.setText("");
			lblL3.setText("");
			lblL2.setText(lines[0]);
		}
		else if(lines.length == 2)
		{
			lblL1.setText(lines[0]);
			Rectangle b = lblL1.getBounds();
			b.y = 28;
			lblL1.setBounds(b);
			
			lblL3.setText(lines[1]);
			b = lblL3.getBounds();
			b.y = 40;
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
		
		current_frame = 0;
		//iframes = project.getBannerIcon();
		
		if(iframes.length > 1){
			if(external_timer == null) timer.start();
			else external_timer.addActionListener(this);
		}
		else pnlIcon.repaint();
	}

	public void onMouseDown()
	{
		setBackground(SystemColor.inactiveCaptionBorder);
		bkg = OpenDialog.getSelectedButtonImage();
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		repaint();
	}
	
	public void onMouseRelease()
	{
		setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		selected = !selected;
		if(!selected){
			setBackground(SystemColor.menu);
			bkg = OpenDialog.getButtonImage();
		}
		for(ActionListener l : listeners)l.actionPerformed(new ActionEvent(this, 0, null));
	}
	
	public void addActionListener(ActionListener l)
	{
		//So can notify containing panel when clicked...
		listeners.add(l);
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		pnlIcon.repaint();
		current_frame++;
		if(current_frame >= iframes.length) current_frame = 0;
	}
	
	public void dispose(){
		if(timer.isRunning()) timer.stop();
	}
	
	public boolean isSelected()
	{
		return selected;
	}
	
	public void paintComponent(Graphics g){
		if(bkg == null) return;
		g.drawImage(bkg, 0, 0, null);
	}

}
