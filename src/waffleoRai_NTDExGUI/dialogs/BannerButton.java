package waffleoRai_NTDExGUI.dialogs;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import waffleoRai_Image.Animation;

public class BannerButton extends JLabel implements ActionListener, MouseListener{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = 6113930795967852306L;
	
	public static final int WIDTH = 64;
	public static final int HEIGHT = 64;
	
	/*----- Instance Variables -----*/
	
	private Animation imgdata; 
	private int frame_millis;
	private Timer anim_timer;
	
	private int now_frame; //Current frame index
	private int frames_left; //# pings left for this frame
	
	private String title;
	
	private List<ActionListener> listeners;
	private boolean isSelected;
	
	private int width;
	private int height;
	
	private int x;
	private int y;
	
	/*----- Construction -----*/
	
	public BannerButton(Animation icon, int frame_len_millis, int w, int h){
		
		listeners = new LinkedList<ActionListener>();
		setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		width = w;
		height = h;
		
		setBackground(SystemColor.menu);
		this.setOpaque(true);
		
		this.setMinimumSize(new Dimension(width, height));
		this.setPreferredSize(new Dimension(width, height));
		
		imgdata = icon;
		frame_millis = frame_len_millis;
		
		if(iconAnimated()){
			anim_timer = new Timer(frame_millis, this);
			anim_timer.setDelay(frame_millis);
		}
		
		now_frame = 0;
		frames_left = imgdata.getFrame(0).getLengthInFrames();
		
		calculateIconCoords();
	}
	
	private void calculateIconCoords(){
		int centerX = width/2;
		int centerY = height/2;
		
		BufferedImage i0 = imgdata.getFrameImage(0);
		int iw = i0.getWidth();
		int ih = i0.getHeight();
		
		x = centerX - (iw/2);
		y = centerY - (ih/2);
	}
	
	/*----- Getters -----*/
	
	public boolean iconAnimated(){
		if(frame_millis <= 0) return false;
		if(imgdata == null) return false;
		
		if(imgdata.getNumberFrames() < 2) return false;
		
		return true;
	}


	public Animation getAnimatedIcon(){
		return imgdata;
	}
	
	public String getBannerTitle(){
		return title;
	}
	
	public boolean isSelected(){
		return isSelected;
	}
	
	public int getInternalWidth(){
		return width;
	}
	
	public int getInternalHeight(){
		return height;
	}
	
	/*----- Setters -----*/
	
	public void setTitleString(String s){
		title = s;
	}
	
	public void setSelected(boolean b){
		isSelected = b;
		if(b){
			setBackground(SystemColor.inactiveCaptionBorder);
			//setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		}
		else{
			setBackground(SystemColor.menu);
			//setBorder(null);
		}
		repaint();
	}
	
	public void startAnimationTimer(){
		if(anim_timer != null) anim_timer.start();
	}
	
	public void stopAnimationTimer(){
		if(anim_timer != null) anim_timer.stop();
	}
	
	public void addActionListener(ActionListener l){
		//So can notify containing panel when clicked...
		listeners.add(l);
	}
	
	/*----- Actions -----*/
	
	public void actionPerformed(ActionEvent e) {
		//For animation timer
		
		if(--frames_left <= 0){
			if(++now_frame >= imgdata.getNumberFrames()){
				now_frame = 0;
			}
			frames_left = imgdata.getFrame(now_frame).getLengthInFrames();
		}
		
		repaint();
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);

		BufferedImage img = imgdata.getFrameImage(now_frame);
		g.drawImage(img, x, y, null);
		
	}

	public void mouseClicked(MouseEvent e) {
		setSelected(true);
		for(ActionListener l : listeners)l.actionPerformed(new ActionEvent(this, this.hashCode(), "Banner Button Clicked"));
	}

	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

}
