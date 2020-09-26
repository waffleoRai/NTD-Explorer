package waffleoRai_NTDExGUI.nightmode;

import java.awt.Color;

import javax.swing.JMenu;

public class DarkMenu extends JMenu{

	private static final long serialVersionUID = -5470136040436439871L;
	
	public DarkMenu(String s){
		super(s);
		setForeground(Color.LIGHT_GRAY);
		setBackground(Color.DARK_GRAY);
	}

}
