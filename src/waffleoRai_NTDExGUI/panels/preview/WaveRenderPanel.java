package waffleoRai_NTDExGUI.panels.preview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

public class WaveRenderPanel extends JPanel implements MouseListener{
	
	private static final long serialVersionUID = -6576167190786559065L;
	
	private double[] wavedat;
	private int loop_st;
	private int loop_ed;
	
	private int zoom_l;
	private int zoom_r;

	public WaveRenderPanel(int w, int h){
		setMinimumSize(new Dimension(w, h));
		setPreferredSize(new Dimension(w, h));
		setBackground(Color.BLACK);
		setForeground(Color.GREEN);
		
		this.addMouseListener(this); //0.0
	}
	
	public void setData(double[] fullwave){
		wavedat = fullwave;
		zoom_l = 0;
		zoom_r = fullwave.length;
		loop_st = 0;
		loop_ed = zoom_r;
		repaint();
	}
	
	public void setLoop(int st, int ed){
		loop_st = st;
		loop_ed = ed;
		repaint();
	}
	
	public void setZoom(int min, int max){
		if(wavedat == null){
			zoom_l = 0; zoom_r = 0;
			repaint();
			return;
		}
		zoom_l = Math.min(0, min);
		zoom_r = Math.max(wavedat.length, max);
		if(zoom_l >= zoom_r) zoom_l = zoom_r-1;
		repaint();
	}
	
	public void paintComponent(Graphics g){
		//TODO more accurate size retrieval
		
		super.paintComponent(g);
		
		g.setColor(Color.green);
		
		//Horizontal line through
		Dimension size = this.getSize();
		int h = size.height;
		int w = size.width;
		if(h % 2 == 0) h--;
		
		int midpix = h/2;
		g.drawLine(0, midpix, w, midpix);
		
		if(wavedat == null) return;
		
		//Draw wave
		int samps = zoom_r - zoom_l;
		double px_per_samp = (double)w/(double)samps;
		if(px_per_samp < 1){
			//Multiple samples per x
			double samp_per_px = 1.0/px_per_samp;
			int lasty = midpix;
			int lastx = 0;
			double nxt = samp_per_px;
			double sum = 0.0;
			int ctx = 0;
			for(int s = zoom_l; s < zoom_r; s++){
				
				//Count
				sum += wavedat[s];
				if(s == loop_st || s == loop_ed){
					g.setColor(Color.red);
					g.drawLine(lastx+1, 0, lastx+1, h);
					g.setColor(Color.green);
				}
				
				if(++ctx >= nxt){
					nxt += samp_per_px;
					double avg = sum/(double)ctx;
					ctx = 0;
					sum = 0.0;
					
					double off = (midpix-1) * avg;
					int y = midpix + (int)Math.round(off);
					int x = lastx+1;
					
					g.drawLine(lastx, lasty, x, y);
					lastx = x;
					lasty = y;
				}
			}
		}
		else{
			//Multiple x per sample
			int lastx = 0;
			int lasty = midpix;
			int s = zoom_l;
			double nxt = 0.0;
			int ct = 0;
			for(int x = 0; x < w; x++){
				
				if(ct++ >= nxt){
					//Draw sample
					
					if(s == loop_st || s == loop_ed){
						g.setColor(Color.red);
						g.drawLine(x, 0, x, h);
						g.setColor(Color.green);
					}
					
					double val = 0.0;
					if(s < zoom_r) val = wavedat[s++];
					double off = (midpix-1) * val;
					int y = midpix + (int)Math.round(off);
					
					if(x!=0)g.drawLine(lastx, lasty, x, y);
					lastx = x;
					lasty = y;
					
					nxt += px_per_samp;
				}
				
			}
			
			//Draw one more line so it goes off edge
			int x = w;
			while(ct++ < nxt){x++;}
			
			double val = 0.0;
			if(s < zoom_r) val = wavedat[s++];
			double off = (midpix-1) * val;
			int y = midpix + (int)Math.round(off);
			
			if(x!=0)g.drawLine(lastx, lasty, x, y);
			lastx = x;
			lasty = y;
			
			nxt += px_per_samp;
			
		}
		
		
	}

	
	public void mouseClicked(MouseEvent e) {
		//If right click, zoom out 5x
		if(e.getButton() == MouseEvent.BUTTON3){
			int nowzoom = zoom_r - zoom_l;
			int zoomout = nowzoom * 5;
			int side = zoomout/2;
			int midpoint = zoom_r - (nowzoom/2);
			
			int left = midpoint - side;
			int right = midpoint + side;
			setZoom(left, right);
		}
		else if(e.getButton() == MouseEvent.BUTTON1){
			int nowzoom = zoom_r - zoom_l;
			int zoomin = nowzoom/5;
			int side = zoomin/2;
			int midpoint = zoom_r - (nowzoom/2);
			
			int left = midpoint - side;
			int right = midpoint + side;
			setZoom(left, right);
		}
	}


	public void mousePressed(MouseEvent e) {}


	public void mouseReleased(MouseEvent e) {}


	public void mouseEntered(MouseEvent e) {}


	public void mouseExited(MouseEvent e) {}

}
