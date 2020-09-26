package waffleoRai_NTDExGUI.nightmode;

import java.awt.Color;

import javax.swing.JMenuBar;
import javax.swing.plaf.basic.BasicBorders.MenuBarBorder;

public class DarkMenuBar extends JMenuBar{

	private static final long serialVersionUID = -177402585864698458L;
	
	public DarkMenuBar(){
		setForeground(Color.LIGHT_GRAY);
		setBackground(Color.DARK_GRAY);
		setBorder(new MenuBarBorder(Color.DARK_GRAY, Color.LIGHT_GRAY));
		
		
	}

}
