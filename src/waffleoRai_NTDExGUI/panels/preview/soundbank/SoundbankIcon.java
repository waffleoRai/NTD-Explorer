package waffleoRai_NTDExGUI.panels.preview.soundbank;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import waffleoRai_soundbank.SoundbankNode;

public class SoundbankIcon {
	
private static Map<Integer, Icon> icon_map;
	
	private static void initialize(){
		icon_map = new ConcurrentHashMap<Integer, Icon>();
		
		icon_map.put(SoundbankNode.NODETYPE_BANK, new ImageIcon(SoundbankIcon.class.getResource("/waffleoRai_NTDExGUI/panels/preview/soundbank/res/node_bnk32_bank.png")));
		icon_map.put(SoundbankNode.NODETYPE_PROGRAM, new ImageIcon(SoundbankIcon.class.getResource("/waffleoRai_NTDExGUI/panels/preview/soundbank/res/node_bnk32_prog.png")));
		icon_map.put(SoundbankNode.NODETYPE_TONE, new ImageIcon(SoundbankIcon.class.getResource("/waffleoRai_NTDExGUI/panels/preview/soundbank/res/node_bnk32_tone.png")));
		
	}
	
	public static Icon getTypeIcon(Integer type){
		if(icon_map == null) initialize();
		return icon_map.get(type);
	}
	
}
