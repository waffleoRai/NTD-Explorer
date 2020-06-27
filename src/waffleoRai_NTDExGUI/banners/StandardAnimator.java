package waffleoRai_NTDExGUI.banners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

import waffleoRai_Image.Animation;

public class StandardAnimator implements Animator{

	private ActionListener listener;
	
	private Animation anim;
	private int millis;
	private Timer timer;
	
	private int frame;
	private int left;
	
	public StandardAnimator(Animation a, int millis_per_frame, ActionListener l){
		anim = a;
		listener = l;
		millis = millis_per_frame;
		frame = 0;
		
		left = a.getFrame(0).getLengthInFrames();
		timer = new Timer(millis, new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				onTimerTick();
				listener.actionPerformed(e);
			}
			
		});
	}
	
	public BufferedImage getCurrentImage(){
		return anim.getFrameImage(frame);
	}
	
	private void onTimerTick(){
		if(--left <= 0){
			if(++frame > anim.getNumberFrames()) frame = 0;
			left = anim.getFrame(frame).getLengthInFrames();
		}
	}
	
	public void start() {
		timer.start();
	}
	
	public void stop() {
		timer.stop();
	}

	public Animation getAnimation() {return anim;}

}
