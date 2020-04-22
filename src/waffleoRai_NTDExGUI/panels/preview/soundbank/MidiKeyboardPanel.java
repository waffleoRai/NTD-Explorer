package waffleoRai_NTDExGUI.panels.preview.soundbank;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

public class MidiKeyboardPanel extends JPanel{

	private static final long serialVersionUID = 4706137093758464598L;
	
	public static final int KEYOFFSET_BLACK = -2;
	public static final int KEYWIDTH_BLACK = 4;
	public static final int KEYWIDTH_WHITE = 7;
	public static final int KEYHEIGHT_BLACK = 20;
	public static final int KEYHEIGHT_WHITE = 32;
	
	public static final int TOTAL_WHITEKEYS = 75;
	
	public static final Color COLOR_WHITE_DOWN = new Color(200,200,200);
	public static final Color COLOR_BLACK_DOWN = new Color(64,64,64);
	
	public static final int[] BLACK_KEYS = {1,3,6,8,10};
	
	private BoardKey[] keys;
	private List<KeyboardListener> listeners;
	
	private class BoardKey{
		
		private boolean blackkey;
		
		private int x_min;
		private int x_max;
		private int y_min;
		private int y_max;
		
		private boolean depressed;
		
		public BoardKey(boolean black, Rectangle area){
			blackkey = black;
			x_min = area.x;
			x_max = x_min + area.width;
			y_min = area.y;
			y_max = y_min + area.height;
		}
		
		public void draw(Graphics g){

			int w = x_max - x_min;
			int h = y_max - y_min;
			
			//Draw rectangle
			if(blackkey){
				if(depressed) g.setColor(COLOR_BLACK_DOWN);
				else g.setColor(Color.black);
			}
			else{
				if(depressed) g.setColor(COLOR_WHITE_DOWN);
				else g.setColor(Color.white);
			}
			g.fillRect(x_min, y_min, w, h);
			
			//Draw outline
			g.setColor(Color.black);
			g.drawRect(x_min, y_min, w, h);
			
		}
		
		public void setDepressed(boolean b){
			depressed = b;
		}
		
		public boolean insideKey(int x, int y){
			if (x < x_min) return false;
			if (x > x_max) return false;
			if (y < y_min) return false;
			if (y > y_max) return false;
			return true;
		}
		
		public boolean whiteKey(){
			return !blackkey;
		}
		
	}
	
	public MidiKeyboardPanel(){

		listeners = new LinkedList<KeyboardListener>();
		keys = new BoardKey[128];
		
		int minwidth = TOTAL_WHITEKEYS * KEYWIDTH_WHITE;
		int minheight = KEYHEIGHT_WHITE;
		this.setMinimumSize(new Dimension(minwidth, minheight));
		this.setPreferredSize(new Dimension(minwidth, minheight));
		
		int i = 0;
		int k = 0; //Note in octave
		int X = 0;
		while(i++ < 128){
			boolean black = false;
			for(int j : BLACK_KEYS){
				if(j == k){
					black = true;
					break;
				}
			}
			
			//Determine location of key
			int w = 0;
			int h = 0;
			int x = X;
			int y = 0;
			if(black){
				w = KEYWIDTH_BLACK;
				h = KEYHEIGHT_BLACK;
				x += KEYOFFSET_BLACK;
			}
			else{
				w = KEYWIDTH_WHITE;
				h = KEYHEIGHT_WHITE;
				X+=w;
			}
			
			//Construct key
			keys[i-1] = new BoardKey(black, new Rectangle(x, y, w, h));
			
			k++;
			if(k>=12)k = 0;
		}
		
		this.addListener(new KeyboardListener(){

			@Override
			public void onNotePressed(int note, int vel) {
				//Depress key
				if(note < 0) return;
				keys[note].setDepressed(true);
				repaint();
			}

			@Override
			public void onNoteReleased(int note) {
				//Undepress key
				if(note < 0) return;
				keys[note].setDepressed(false);
				repaint();
			}

			@Override
			public void allNotesOff() {
				//Undepress all keys
				for(int i = 0; i < 128; i++) keys[i].setDepressed(false);
				repaint();
			}
			
		});
		
		this.addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent e) {
				//Find note and turn on
				/*int note = determinePressedKey(e.getX(), e.getY());
				if(note >= 0 && note < 128){
					for(KeyboardListener l : listeners) l.onNotePressed(note, 100);
				}*/
			}

			@Override
			public void mousePressed(MouseEvent e) {
				//System.err.println("Mouse press detected: " + e.getX() + "," + e.getY());
				int note = determinePressedKey(e.getX(), e.getY());
				//System.err.println("Note: " + note);
				if(note >= 0 && note < 128){
					for(KeyboardListener l : listeners) l.onNotePressed(note, 100);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				for(KeyboardListener l : listeners) l.allNotesOff();
			}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {
				for(KeyboardListener l : listeners) l.allNotesOff();
			}
		});
		
		repaint();
	}
	
	public int determinePressedKey(int x, int y){
		//Determine approximate scan start location
		//System.err.println("Mouse press detected: " + x + "," +y);
		int stnote = (x/KEYWIDTH_WHITE);
		if(stnote < 0) stnote = 0;
		stnote = ((stnote/7) * 12) - 1;
		//System.err.println("stnote: " + stnote);
		if(y < KEYHEIGHT_BLACK){
			//Check black keys THEN white keys
			//System.err.println("Black key height");
			for(int i = 0; i < 13; i++){
				int v = stnote+i;
				if(v >= 128) break;
				BoardKey k = keys[v];
				if(k.whiteKey()) continue;
				if(k.insideKey(x, y)) return v;
			}
			for(int i = 0; i < 13; i++){
				int v = stnote+i;
				if(v >= 128) break;
				BoardKey k = keys[v];
				if(!k.whiteKey()) continue;
				if(k.insideKey(x, y)) return v;
			}
		}
		else{
			//Check only white keys
			//System.err.println("Not black key height");
			for(int i = 0; i < 13; i++){
				int v = stnote+i;
				if(v >= 128) break;
				BoardKey k = keys[v];
				if(!k.whiteKey()) continue;
				if(k.insideKey(x, y)) return v;
			}
		}
		
		return -1;
	}
	
	private void drawWhiteKeys(Graphics g){
		for(int i = 0; i < 128; i++){
			BoardKey k = keys[i];
			if(!k.whiteKey()) continue;
			k.draw(g);
		}
	}
	
	private void drawBlackKeys(Graphics g){
		for(int i = 0; i < 128; i++){
			BoardKey k = keys[i];
			if(k.whiteKey()) continue;
			k.draw(g);
		}
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		drawWhiteKeys(g);
		drawBlackKeys(g);
		
	}
	
	public void addListener(KeyboardListener l){
		listeners.add(l);
	}

}
