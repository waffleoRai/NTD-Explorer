package waffleoRai_NTDExGUI.dialogs;

import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JTabbedPane;

import waffleoRai_Compression.definitions.AbstractCompDef;
import waffleoRai_Compression.definitions.CompDefNode;
import waffleoRai_Files.FileClass;
import waffleoRai_Files.FileTypeDefNode;
import waffleoRai_Files.FileTypeDefinition;
import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExGUI.panels.ChainViewPanel;
import waffleoRai_NTDExGUI.panels.TypeBrowserPane;
import waffleoRai_NTDExGUI.panels.TypeCompBrowserPane;

import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;

public class SetTypeDialog extends JDialog{

	private static final long serialVersionUID = -2724065137446577482L;
	
	public static final FileClass[] GROUP_SYSTEM = {FileClass.SYSTEM, FileClass.EXECUTABLE, 
												    FileClass.CODELIB, FileClass.CONFIG_FILE};
	public static final FileClass[] GROUP_ARCHIVE = {FileClass.ARCHIVE, FileClass.SOUND_ARC, 
		    										 FileClass.SOUND_WAVEARC};
	public static final FileClass[] GROUP_CODE = {FileClass.EXECUTABLE, FileClass.CODELIB, 
			 									  FileClass.CODESCRIPT};
	public static final FileClass[] GROUP_AUDIO = {FileClass.SOUND_ARC, FileClass.SOUND_WAVEARC, 
			  					                   FileClass.SOUND_WAVEARC, FileClass.SOUND_WAVE,
			  					                   FileClass.SOUND_STREAM, FileClass.SOUNDBANK,
			  					                   FileClass.SOUND_SEQ};
	public static final FileClass[] GROUP_2DGRAPHIC = {FileClass.IMG_IMAGE, FileClass.IMG_ICON,
													   FileClass.IMG_SPRITE_SHEET, FileClass.IMG_TILE,
													   FileClass.IMG_TILEMAP, FileClass.IMG_TEXTURE,
													   FileClass.IMG_PALETTE, FileClass.IMG_ANIM_2D,
													   FileClass.IMG_FONT, FileClass.MOV_MOVIE,
													   FileClass.MOV_VIDEO};
	public static final FileClass[] GROUP_3DGRAPHIC = {FileClass._3D_MODEL, FileClass._3D_MESH, 
			  										   FileClass._3D_UVMAP, FileClass.IMG_TEXTURE,
			  										   FileClass._3D_MORPH_DAT, FileClass._3D_RIG_DAT,
			  										   FileClass._3D_LIGHTING_DAT, FileClass._3D_ANIM_3D,
			  										   FileClass._3D_MAT_ANIM, FileClass._3D_TXR_ANIM,
			  										   FileClass._3D_UV_ANIM};
	public static final FileClass[] GROUP_TEXT = {FileClass.TEXT_FILE, FileClass.DAT_STRINGTBL, 
			   									  FileClass.CODESCRIPT, FileClass.XML};
	public static final FileClass[] GROUP_DATA = {FileClass.XML, FileClass.DAT_COLLISION, 
													FileClass.DAT_LAYOUT, FileClass.DAT_TABLE,
													FileClass.DAT_STRINGTBL};
	
	private ChainViewPanel pnlChainView;
	
	private boolean approved;
	private LinkedList<FileTypeNode> chain;
	
	public SetTypeDialog(LinkedList<FileTypeNode> initchain){
		chain = initchain;
		if(chain == null) chain = new LinkedList<FileTypeNode>();
		initGUI();
		updateChainGraphic();
	}
	
	private void initGUI(){
		setTitle("Edit Type Chain");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlChainBtn = new JPanel();
		GridBagConstraints gbc_pnlChainBtn = new GridBagConstraints();
		gbc_pnlChainBtn.insets = new Insets(0, 0, 5, 5);
		gbc_pnlChainBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlChainBtn.gridx = 0;
		gbc_pnlChainBtn.gridy = 0;
		getContentPane().add(pnlChainBtn, gbc_pnlChainBtn);
		GridBagLayout gbl_pnlChainBtn = new GridBagLayout();
		gbl_pnlChainBtn.columnWidths = new int[]{0, 0};
		gbl_pnlChainBtn.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlChainBtn.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_pnlChainBtn.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlChainBtn.setLayout(gbl_pnlChainBtn);
		
		JButton btnRemoveTail = new JButton("Remove Tail");
		GridBagConstraints gbc_btnRemoveTail = new GridBagConstraints();
		gbc_btnRemoveTail.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnRemoveTail.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemoveTail.gridx = 0;
		gbc_btnRemoveTail.gridy = 0;
		pnlChainBtn.add(btnRemoveTail, gbc_btnRemoveTail);
		btnRemoveTail.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onClearTail();
			}
		});
		
		JButton btnRemoveHead = new JButton("Remove Head");
		GridBagConstraints gbc_btnRemoveHead = new GridBagConstraints();
		gbc_btnRemoveHead.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnRemoveHead.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemoveHead.gridx = 0;
		gbc_btnRemoveHead.gridy = 1;
		pnlChainBtn.add(btnRemoveHead, gbc_btnRemoveHead);
		btnRemoveHead.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onClearHead();
			}
		});
		
		JButton btnClearAll = new JButton("Clear All");
		GridBagConstraints gbc_btnClearAll = new GridBagConstraints();
		gbc_btnClearAll.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnClearAll.gridx = 0;
		gbc_btnClearAll.gridy = 2;
		pnlChainBtn.add(btnClearAll, gbc_btnClearAll);
		btnClearAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onClearChain();
			}
		});
		
		JPanel pnlExitBtn = new JPanel();
		GridBagConstraints gbc_pnlExitBtn = new GridBagConstraints();
		gbc_pnlExitBtn.gridwidth = 2;
		gbc_pnlExitBtn.insets = new Insets(0, 0, 0, 5);
		gbc_pnlExitBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlExitBtn.gridx = 0;
		gbc_pnlExitBtn.gridy = 2;
		getContentPane().add(pnlExitBtn, gbc_pnlExitBtn);
		GridBagLayout gbl_pnlExitBtn = new GridBagLayout();
		gbl_pnlExitBtn.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlExitBtn.rowHeights = new int[]{0, 0};
		gbl_pnlExitBtn.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlExitBtn.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlExitBtn.setLayout(gbl_pnlExitBtn);
		
		JButton btnApply = new JButton("Apply");
		GridBagConstraints gbc_btnApply = new GridBagConstraints();
		gbc_btnApply.insets = new Insets(5, 5, 5, 5);
		gbc_btnApply.gridx = 1;
		gbc_btnApply.gridy = 0;
		pnlExitBtn.add(btnApply, gbc_btnApply);
		btnApply.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onApply();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlExitBtn.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		
		pnlChainView = new ChainViewPanel();
		GridBagConstraints gbc_pnlChainView = new GridBagConstraints();
		gbc_pnlChainView.fill = GridBagConstraints.BOTH;
		gbc_pnlChainView.gridx = 1;
		gbc_pnlChainView.gridy = 0;
		getContentPane().add(pnlChainView, gbc_pnlChainView);
		
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
		GridBagConstraints gbc_tabs = new GridBagConstraints();
		gbc_tabs.insets = new Insets(0, 0, 5, 0);
		gbc_tabs.gridwidth = 2;
		gbc_tabs.fill = GridBagConstraints.BOTH;
		gbc_tabs.gridx = 0;
		gbc_tabs.gridy = 1;
		getContentPane().add(tabs, gbc_tabs);
		
		//Groups of file classes tabbed
		TypeCompBrowserPane pnlComp = new TypeCompBrowserPane(false);
		tabs.addTab("Compression", null, pnlComp, null);
		pnlComp.setButtonText("Set Head");
		pnlComp.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onCompListApply(pnlComp.getSelected());
			}
		});
		
		TypeBrowserPane pnlSys = new TypeBrowserPane(arr2List(GROUP_SYSTEM), false);
		tabs.addTab("System", null, pnlSys, null);
		pnlSys.setButtonText("Set Tail");
		pnlSys.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onListApply(pnlSys.getSelected());
			}
		});
		
		TypeBrowserPane pnlArc = new TypeBrowserPane(arr2List(GROUP_ARCHIVE), false);
		tabs.addTab("Archive", null, pnlArc, null);
		pnlArc.setButtonText("Set Tail");		
		pnlArc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onListApply(pnlArc.getSelected());
			}
		});
		
		TypeBrowserPane pnlCode = new TypeBrowserPane(arr2List(GROUP_CODE), false);
		tabs.addTab("Code", null, pnlCode, null);
		pnlCode.setButtonText("Set Tail");
		pnlCode.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onListApply(pnlCode.getSelected());
			}
		});
		
		TypeBrowserPane pnlData = new TypeBrowserPane(arr2List(GROUP_DATA), false);
		tabs.addTab("Data", null, pnlData, null);
		pnlData.setButtonText("Set Tail");
		pnlData.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onListApply(pnlData.getSelected());
			}
		});
		
		TypeBrowserPane pnlText = new TypeBrowserPane(arr2List(GROUP_TEXT), false);
		tabs.addTab("Text", null, pnlText, null);
		pnlText.setButtonText("Set Tail");
		pnlText.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onListApply(pnlText.getSelected());
			}
		});
		
		TypeBrowserPane pnlAudio = new TypeBrowserPane(arr2List(GROUP_AUDIO), false);
		tabs.addTab("Audio", null, pnlAudio, null);
		pnlAudio.setButtonText("Set Tail");
		pnlAudio.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onListApply(pnlAudio.getSelected());
			}
		});
		
		TypeBrowserPane pnlG2 = new TypeBrowserPane(arr2List(GROUP_2DGRAPHIC), false);
		tabs.addTab("2D Graphics", null, pnlG2, null);
		pnlG2.setButtonText("Set Tail");
		pnlG2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onListApply(pnlG2.getSelected());
			}
		});
		
		TypeBrowserPane pnlG3 = new TypeBrowserPane(arr2List(GROUP_3DGRAPHIC), false);
		tabs.addTab("3D Graphics", null, pnlG3, null);
		pnlG3.setButtonText("Set Tail");
		pnlG3.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onListApply(pnlG3.getSelected());
			}
		});
		
	}
	
	private static List<FileClass> arr2List(FileClass[] group){
		List<FileClass> list = new ArrayList<FileClass>(group.length);
		for(int i = 0; i < group.length; i++) list.add(group[i]);
		return list;
	}
	
	private void updateChainGraphic(){
		pnlChainView.loadChain(chain);
	}
	
	private void onListApply(FileTypeDefinition applieddef){
		//Set tail, or add to tail
		if(chain.isEmpty()) chain.add(new FileTypeDefNode(applieddef));
		else{
			//Check to see if tail is a compression node
			//If it is, just add new node.
			//If not, replace last node
			FileTypeNode tail = chain.getLast();
			if(!tail.isCompression()) chain.removeLast();
			chain.add(new FileTypeDefNode(applieddef));
		}
		
		updateChainGraphic();
	}
	
	private void onCompListApply(AbstractCompDef def){
		//Just add head
		chain.push(new CompDefNode(def));
		updateChainGraphic();
	}
	
	private void onClearTail(){
		if(chain.isEmpty()) return;
		FileTypeNode tail = chain.getLast();
		if(!tail.isCompression()) chain.removeLast();
		updateChainGraphic();
	}
	
	private void onClearHead(){
		if(chain.isEmpty()) return;
		chain.pop();
		updateChainGraphic();
	}
	
	private void onClearChain(){
		chain.clear();
		updateChainGraphic();
	}
	
	private void onApply(){
		approved = true;
		closeMe();
	}
	
	private void onCancel(){
		approved = false;
		closeMe();
	}
	
	public boolean selectionApproved(){
		return approved;
	}
	
	public FileTypeNode getChain(){
		//Link, then return head
		FileTypeNode head = null;
		FileTypeNode prev = null;
		
		for(FileTypeNode n : chain){
			if(head == null){
				head = n;
				prev = n;
				continue;
			}
			
			prev.setChild(n);
			prev = n;
		}
		
		return head;
	}
	
	private void closeMe(){
		setVisible(false);
		pnlChainView.loadChain(null);
		dispose();
	}
	
}
