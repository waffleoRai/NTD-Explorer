package waffleoRai_NTDExGUI.banners;

import java.awt.image.BufferedImage;

import waffleoRai_Image.Animation;
import waffleoRai_Image.SimpleAnimation;

public class Unanimator implements Animator{

	private BufferedImage image;
	
	public Unanimator(BufferedImage img){
		image = img;
	}
	
	public BufferedImage getCurrentImage(){return image;}
	
	public void start() {}
	public void stop() {}

	public Animation getAnimation() {
		Animation a = new SimpleAnimation(1);
		a.setFrame(image, 0);
		return a;
	}

}
