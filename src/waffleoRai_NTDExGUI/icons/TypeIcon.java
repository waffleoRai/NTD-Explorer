package waffleoRai_NTDExGUI.icons;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Utils.DirectoryNode;
import waffleoRai_Utils.FileNode;

public class TypeIcon {
	
	private static Map<FileClass, Icon> icon_map;
	
	private static void initialize(){
		icon_map = new ConcurrentHashMap<FileClass, Icon>();
		
		Icon i = new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_arc.png"));
		icon_map.put(FileClass.ARCHIVE, i);
		
		icon_map.put(FileClass.SYSTEM, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_sys.png")));
		
		icon_map.put(FileClass.EXECUTABLE, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_exe.png")));
		icon_map.put(FileClass.CODELIB, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_lib.png")));
		
		icon_map.put(FileClass.SOUND_ARC, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_sar.png")));
		icon_map.put(FileClass.SOUND_STREAM, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_stm.png")));
		icon_map.put(FileClass.SOUND_WAVE, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_wav.png")));
		icon_map.put(FileClass.SOUNDBANK, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_bnk.png")));
		icon_map.put(FileClass.SOUND_SEQ, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_seq.png")));
		icon_map.put(FileClass.SOUND_WAVEARC, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_war.png")));
		//icon_map.put(FileClass.SOUNDBANK, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_wsd.png")));
	}
	
	public static Icon getTypeIcon(FileClass type){
		if(icon_map == null) initialize();
		return icon_map.get(type);
	}
	
	public static Icon getIconForNode(FileNode node){

		if(node == null) return null;
		if(node instanceof DirectoryNode){
			DirectoryNode dn = (DirectoryNode)node;
			FileClass fc = dn.getFileClass();
			if(fc != null) return getTypeIcon(fc);
			return null;
		}
		FileTypeNode type = node.getTypeChainHead();
		if(type == null) return null;
		while(type.getChild() != null) type = type.getChild(); //Get tail
		FileClass c = type.getFileClass();
		
		return getTypeIcon(c);
	}

}
