package waffleoRai_NTDExGUI.panels;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;

public abstract class AbstractGameOpenButton extends JPanel implements ActionListener{

	private static final long serialVersionUID = -4575905221510492285L;
	
	protected NTDProject project;
	private Timer timer;
	
	private BufferedImage[] iframes;
	private int current_frame;
	
	protected JPanel pnlIcon;
	protected JLabel lblGameCode;
	
	private List<ActionListener> listeners;
	private boolean selected;
	
	private BufferedImage bkg;
	private static BufferedImage icon_bkg;
	
	public AbstractGameOpenButton(){
		listeners = new LinkedList<ActionListener>();
		selected = false;
		//initGUI();
		bkg = getButtonImage(false);
		if(icon_bkg == null){
			//Load from disk
			try{
				icon_bkg = ImageIO.read(NTDProgramFiles.class.getResource("/waffleoRai_NTDExCore/res/general_gradient_bkg_3.png"));
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	protected void initGUI(){
		pnlIcon = new JPanel(){
			public static final long serialVersionUID = 7181179256315196396L;
			
			public void paintComponent(Graphics g) {
		        if(icon_bkg!=null) g.drawImage(icon_bkg, 0, 0, null);
		        g.drawImage(iframes[current_frame], 0, 0, null);
		    }
		};
		
		lblGameCode = new JLabel("CSL-GAME-REG");
		lblGameCode.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblGameCode.setHorizontalAlignment(SwingConstants.CENTER);
		
		bkg = getButtonImage(false);
		if(icon_bkg == null){
			//Load from disk
			try{
				icon_bkg = ImageIO.read(NTDProgramFiles.class.getResource("/waffleoRai_NTDExCore/res/general_gradient_bkg_3.png"));
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		
		this.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {onMouseDown();}
			public void mouseReleased(MouseEvent e) {onMouseRelease();}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			
		});
		
		initSubclassGUI();
	}
	
	protected abstract void initSubclassGUI();
	public abstract BufferedImage getButtonImage(boolean selected);
	
	public void paintComponent(Graphics g){
		if(bkg == null) return;
		g.drawImage(bkg, 0, 0, null);
	}
	
	public void loadMe(NTDProject data, int timer_millis){
		if(timer_millis > 0){
			timer = new Timer(timer_millis, this);
			timer.start();	
		}
		loadMe(data, timer);
	}
	
	public void loadMe(NTDProject data, Timer external_timer){
		project = data;
		
		lblGameCode.setText(project.getGameCode12().replace("_", "-"));
		lblGameCode.repaint();
		
		current_frame = 0;
		iframes = project.getBannerIcon();
		
		if(iframes.length > 1){
			if(external_timer != null){
				external_timer.addActionListener(this);
			}
		}
		else pnlIcon.repaint();
		
		loadLabels(data);
	}
	
	protected abstract void loadLabels(NTDProject data);

	public void onMouseDown(){
		setBackground(SystemColor.inactiveCaptionBorder);
		bkg = getButtonImage(true);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		repaint();
	}
	
	public void onMouseRelease(){
		setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		selected = !selected;
		if(!selected){
			setBackground(SystemColor.menu);
			bkg = getButtonImage(false);
		}
		for(ActionListener l : listeners)l.actionPerformed(new ActionEvent(this, 0, null));
	}
	
	public void addActionListener(ActionListener l){
		//So can notify containing panel when clicked...
		listeners.add(l);
	}

	public void actionPerformed(ActionEvent e) {
		pnlIcon.repaint();
		current_frame++;
		if(current_frame >= iframes.length) current_frame = 0;
	}
	
	public void dispose(){
		if(timer.isRunning()) timer.stop();
	}
	
	public boolean isSelected(){
		return selected;
	}
	
}
