package waffleoRai_NTDExGUI.panels.preview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

public class WaveRenderPanel extends JPanel implements MouseListener{
	
	private static final long serialVersionUID = -6576167190786559065L;
	
	private double[] wavedat;
	private int loop_st;
	private int loop_ed;
	
	private int zoom_l;
	private int zoom_r;
	
	private double normalize_factor;
	
	//private int mywidth;
	//private int myheight;

	public WaveRenderPanel(int w, int h){
		setInternalSize(new Dimension(w,h));
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
		calculateNormalization();
		repaint();
	}
	
	private void calculateNormalization(){
		double max = 0.0;
		for(double d : wavedat){
			double abs = Math.abs(d);
			if(abs > max){
				max = abs;
			}
		}
		
		normalize_factor = 1.0/max;
		//System.err.println("Max: " + max);
		//System.err.println("Normalize factor: " + normalize_factor);
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
		zoom_l = Math.max(0, min);
		zoom_r = Math.min(wavedat.length, max);
		if(zoom_l >= zoom_r) zoom_l = zoom_r-1;
		repaint();
	}
	
	public static double sampleAverage(Collection<Double> samps){
		double sum = 0.0;
		double ct = 0.0;
		for(Double d : samps){sum += d; ct++;}
		
		return sum/ct;
	}
	
	public static double sampleMax(Collection<Double> samps, boolean ab){
		double max = 0.0;
		boolean neg = false;
		for(Double d : samps){
			double abs = Math.abs(d);
			if(abs > max){
				max = abs;
				if(d < 0.0) neg = true;
				else neg = false;
			}
		}
		
		double val = max;
		if(ab && neg) val *= -1.0;
		
		return val;
	}
	
	public static double sampleMedian(List<Double> samps){
		Collections.sort(samps);
		int sz = samps.size();
		int mid = sz/2;
		
		return samps.get(mid);
	}
	
	public static double sampleAbsMedian(List<Double> samps){
		List<Double> nsamps = new LinkedList<Double>();
		for(Double d : samps){
			if(d >= 0.0) nsamps.add(d);
			else nsamps.add(d * -1.0);
		}
		Collections.sort(nsamps);
		int sz = nsamps.size();
		int mid = sz/2;
		
		return nsamps.get(mid);
	}
	
	public static double sampleAbsAverage(Collection<Double> samps){
		double sum = 0.0;
		double ct = 0.0;
		for(Double d : samps){sum += Math.abs(d); ct++;}
		
		return sum/ct;
	}
	
	public double generateValue(List<Double> samps){
		//return sampleAverage(samps);
		return sampleMax(samps, true);
		//return sampleMedian(samps);
		//return sampleAbsMedian(samps);
		//return sampleAbsAverage(samps);
	}
	
	public void paintComponent(Graphics g){
		
		super.paintComponent(g);
		
		g.setColor(Color.green);
		
		//Horizontal line through
		Dimension size = this.getSize();
		//int h = myheight;
		//int w = mywidth;
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
			//System.err.println("Multi x per sample");
			//Multiple samples per x
			double samp_per_px = (double)samps/(double)w;
			int lasty = midpix;
			int lastx = 0;
			double nxt = samp_per_px;
			List<Double> slist = new LinkedList<Double>();
			for(int s = zoom_l; s < zoom_r; s++){
				
				//Count
				slist.add(wavedat[s]);
				if(s == loop_st || s == loop_ed){
					g.setColor(Color.red);
					g.drawLine(lastx+1, 0, lastx+1, h);
					g.setColor(Color.green);
				}
				
				if(s >= nxt){
					nxt += samp_per_px;

					int x = lastx+1;
					double val = generateValue(slist);
					double off = (midpix-1) * val * normalize_factor;
					if(x % 2 == 0) off *= -1.0;
					int y = midpix + (int)Math.round(off);
					
					g.drawLine(lastx, lasty, x, y);
					lastx = x;
					lasty = y;
					//System.err.println(x + "," + y);
					slist.clear();
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
	
	public void setInternalSize(Dimension d){
		//mywidth = d.width;
		//myheight = d.height;
		this.setMinimumSize(d);
		this.setPreferredSize(d);
	}

	public void setSize(Dimension d){
		super.setSize(d);
		setInternalSize(d);
	}
	
	public void clearData(){
		setData(new double[1]);
	}
	
	public void mouseClicked(MouseEvent e) {
		//If right click, zoom out 5x
		if(e.getButton() == MouseEvent.BUTTON3){
			//System.err.println("Right click");
			int nowzoom = zoom_r - zoom_l;
			int zoomout = nowzoom * 5;
			int side = zoomout/2;
			int midpoint = zoom_r - (nowzoom/2);
			
			int left = midpoint - side;
			int right = midpoint + side;
			setZoom(left, right);
		}
		else if(e.getButton() == MouseEvent.BUTTON1){
			//System.err.println("Left click");
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
