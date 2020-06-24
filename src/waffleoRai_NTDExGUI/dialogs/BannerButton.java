package waffleoRai_NTDExGUI.dialogs;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import waffleoRai_Image.Animation;
import waffleoRai_NTDExCore.NTDProgramFiles;

public class BannerButton extends JLabel implements ActionListener, MouseListener{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = 6113930795967852306L;
	
	public static final int[] SIZE_DIMS = {64, 96, 128, 144};
	
	public static final int SIZE_SMALL = 0;
	public static final int SIZE_MEDSMALL = 1;
	public static final int SIZE_MEDIUM = 2;
	public static final int SIZE_LARGE = 3;
	
	public static final String COLOR_GREY = "grey";
	public static final String COLOR_BLUE = "blue";
	public static final String COLOR_GREEN = "green";
	public static final String COLOR_PURPLE = "purple";
	
	public static final int WIDTH = 64;
	public static final int HEIGHT = 64;
	
	/*----- Static Variables -----*/
	
	private static Map<String, BufferedImage[][]> imgmap;
	
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
	
	private BufferedImage icoimg_off;
	private BufferedImage icoimg_on;
	
	/*----- Construction -----*/
	
	public BannerButton(){
		this(null, -1, SIZE_SMALL, COLOR_GREY);
	}
	
	public BannerButton(int size_enum){
		this(null, -1, size_enum, COLOR_GREY);
	}
	
	public BannerButton(Animation icon, int frame_len_millis, int size_enum){
		this(icon, frame_len_millis, size_enum, COLOR_GREY);
	}
	
	public BannerButton(Animation icon, int frame_len_millis, int size_enum, String color){
		listeners = new LinkedList<ActionListener>();
		
		width = WIDTH;
		height = HEIGHT;
		
		if(size_enum >= 0 && size_enum < SIZE_DIMS.length){
			width = SIZE_DIMS[size_enum];
			height = SIZE_DIMS[size_enum];
		}
		this.setMinimumSize(new Dimension(width, height));
		this.setPreferredSize(new Dimension(width, height));
		
		//See if we can retrieve the icon background images...
		icoimg_off = getIconImage(color, size_enum, false);
		icoimg_on = getIconImage(color, size_enum, true);
		if(icoimg_off != null && icoimg_on != null){
			this.setOpaque(false);
		}
		else{
			//One or both was not loaded
			icoimg_off = null; icoimg_on = null;
			setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			setBackground(SystemColor.menu);
			this.setOpaque(true);
		}
		
		imgdata = icon;
		frame_millis = frame_len_millis;
		
		if(iconAnimated()){
			anim_timer = new Timer(frame_millis, this);
			anim_timer.setDelay(frame_millis);
		}
		
		now_frame = 0;
		frames_left = imgdata.getFrame(0).getLengthInFrames();
		
		calculateIconCoords();
		this.addMouseListener(this);
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
	
	/*----- Icon Graphics -----*/
	
	private static BufferedImage getIconImage(String color, int szenum, boolean on){
		if(szenum < 0 | szenum >= SIZE_DIMS.length) return null;
		
		if(imgmap == null) imgmap = new HashMap<String, BufferedImage[][]>();
		BufferedImage[][] imgs = imgmap.get(color);
		if(imgs == null){
			imgs = new BufferedImage[SIZE_DIMS.length][2];
			imgmap.put(color, imgs);
		}
		
		int onidx = 0; 
		if(on) onidx=1;
		if(imgs[szenum][onidx] == null){
			String onoff = "off";
			if(on)onoff = "on";
			String fname = "icobtn_" + SIZE_DIMS[szenum] + "_" + color + "_" + onoff + ".png";
			try{
				imgs[szenum][onidx] = ImageIO.read(NTDProgramFiles.class.getResource("/waffleoRai_NTDExCore/res/" + fname));
			}
			catch(IOException x){
				x.printStackTrace();
				return null;
			}
		}
		
		return imgs[szenum][onidx];
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);

		if(isSelected && icoimg_on != null){
			g.drawImage(icoimg_on, 0, 0, null);
		}
		else if(!isSelected && icoimg_off != null){
			g.drawImage(icoimg_off, 0, 0, null);
		}
		
		BufferedImage img = imgdata.getFrameImage(now_frame);
		g.drawImage(img, x, y, null);
		
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
	
	public void mousePressed(MouseEvent e) {
		setSelected(!isSelected);
		for(ActionListener l : listeners)l.actionPerformed(new ActionEvent(this, this.hashCode(), "Banner Button Clicked"));
	}
	
	public void mouseClicked(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

}
