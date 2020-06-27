package waffleoRai_NTDExGUI.banners;

import java.awt.image.BufferedImage;

import waffleoRai_Image.Animation;

public interface Animator {
	
	public void start();
	public void stop();
	public Animation getAnimation();
	public BufferedImage getCurrentImage();

}
