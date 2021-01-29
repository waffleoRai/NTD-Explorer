package waffleoRai_NTDExGUI.icons;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;

public class TypeIcon {
	
	private static Map<FileClass, Icon> icon_map;
	
	private static void initialize(){
		icon_map = new ConcurrentHashMap<FileClass, Icon>();
		
		Icon i = new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_arc.png"));
		icon_map.put(FileClass.ARCHIVE, i);
		
		icon_map.put(FileClass.EMPTY_FILE, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_0_emptyfile.png")));
		icon_map.put(FileClass.EMPTY_DIR, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico032_emptydir.png")));
		
		icon_map.put(FileClass.SYSTEM, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_sys.png")));
		icon_map.put(FileClass.CONFIG_FILE, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_cfg.png")));
		icon_map.put(FileClass.TEXT_FILE, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_txt.png")));
		icon_map.put(FileClass.XML, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_xml.png")));
		icon_map.put(FileClass.MARKUP_SCRIPT, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_mud.png")));
		
		icon_map.put(FileClass.EXECUTABLE, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_exe.png")));
		icon_map.put(FileClass.CODELIB, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_lib.png")));
		icon_map.put(FileClass.CODESCRIPT, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_scr.png")));
		
		icon_map.put(FileClass.SOUND_ARC, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_sar.png")));
		icon_map.put(FileClass.SOUND_STREAM, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_stm.png")));
		icon_map.put(FileClass.SOUND_WAVE, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_wav.png")));
		icon_map.put(FileClass.SOUNDBANK, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_bnk.png")));
		icon_map.put(FileClass.SOUND_SEQ, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_seq.png")));
		icon_map.put(FileClass.SOUND_WAVEARC, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_war.png")));
		//icon_map.put(FileClass.SOUNDBANK, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_wsd.png")));
		
		icon_map.put(FileClass.IMG_ANIM_2D, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_2an.png")));
		icon_map.put(FileClass.IMG_FONT, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_fnt.png")));
		icon_map.put(FileClass.IMG_ICON, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_ico.png")));
		icon_map.put(FileClass.IMG_IMAGE, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_img.png")));
		icon_map.put(FileClass.IMG_PALETTE, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_plt.png")));
		icon_map.put(FileClass.IMG_SPRITE_SHEET, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_spr.png")));
		icon_map.put(FileClass.IMG_TEXTURE, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_txr.png")));
		icon_map.put(FileClass.IMG_TILE, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_tle.png")));
		icon_map.put(FileClass.IMG_TILEMAP, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_tlm.png")));
		
		icon_map.put(FileClass._3D_ANIM_3D, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_3an.png")));
		icon_map.put(FileClass._3D_LIGHTING_DAT, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_lit.png")));
		icon_map.put(FileClass._3D_MESH, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_msh.png")));
		icon_map.put(FileClass._3D_MODEL, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_mdl.png")));
		icon_map.put(FileClass._3D_MORPH_DAT, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_mph.png")));
		icon_map.put(FileClass._3D_RIG_DAT, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_rig.png")));
		icon_map.put(FileClass._3D_UVMAP, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_uvm.png")));
		icon_map.put(FileClass._3D_MAT_ANIM, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_msa.png")));
		icon_map.put(FileClass._3D_TXR_ANIM, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_tsa.png")));
		icon_map.put(FileClass._3D_UV_ANIM, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_uva.png")));
		
		icon_map.put(FileClass.DAT_COLLISION, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_cls.png")));
		icon_map.put(FileClass.DAT_LAYOUT, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_lay.png")));
		icon_map.put(FileClass.DAT_STRINGTBL, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_str.png")));
		icon_map.put(FileClass.DAT_TABLE, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_tbl.png")));
		icon_map.put(FileClass.DAT_BANNER, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_bnr.png")));
		icon_map.put(FileClass.DAT_HASHTABLE, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_hsh.png")));
		
		icon_map.put(FileClass.MOV_MOVIE, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_mov.png")));
		icon_map.put(FileClass.MOV_VIDEO, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_vid.png")));
		icon_map.put(FileClass.MOV_MULTIMEDIA_STR, new ImageIcon(TypeIcon.class.getResource("/waffleoRai_NTDExGUI/icons/res/node_ico32_mms.png")));
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
