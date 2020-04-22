package waffleoRai_NTDExGUI.panels.preview;

import waffleoRai_NTDExGUI.DisposableJPanel;
import waffleoRai_NTDExGUI.panels.preview.soundbank.SoundbankKeyboardPanel;
import waffleoRai_NTDExGUI.panels.preview.soundbank.SoundbankTreeListener;
import waffleoRai_NTDExGUI.panels.preview.soundbank.SoundbankTreePanel;
import waffleoRai_SoundSynth.SynthBank;
import waffleoRai_soundbank.SoundbankNode;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleSoundbankTreePanel extends DisposableJPanel implements SoundbankTreeListener{
	
	private static final long serialVersionUID = -6215799642357691538L;
	
	private SoundbankTreePanel pnlTree;
	private StringmapInfoPanel pnlInfo;
	private SoundbankKeyboardPanel pnlPlay;
	
	public SimpleSoundbankTreePanel(SoundbankNode root){
		initGUI(root);
	}
	
	private void initGUI(SoundbankNode root){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 0.0};
		setLayout(gridBagLayout);
		
		pnlTree = new SoundbankTreePanel(root);
		GridBagConstraints gbc_pnlTree = new GridBagConstraints();
		gbc_pnlTree.insets = new Insets(0, 0, 5, 5);
		gbc_pnlTree.fill = GridBagConstraints.BOTH;
		gbc_pnlTree.gridx = 0;
		gbc_pnlTree.gridy = 0;
		add(pnlTree, gbc_pnlTree);
		pnlTree.addListener(this);
		
		pnlInfo = new StringmapInfoPanel();
		GridBagConstraints gbc_pnlInfo = new GridBagConstraints();
		gbc_pnlInfo.insets = new Insets(0, 0, 5, 0);
		gbc_pnlInfo.fill = GridBagConstraints.BOTH;
		gbc_pnlInfo.gridx = 1;
		gbc_pnlInfo.gridy = 0;
		add(pnlInfo, gbc_pnlInfo);
		loadInfo(root);
		
		pnlPlay = new SoundbankKeyboardPanel();
		GridBagConstraints gbc_pnlPlay = new GridBagConstraints();
		gbc_pnlPlay.gridwidth = 2;
		gbc_pnlPlay.fill = GridBagConstraints.BOTH;
		gbc_pnlPlay.gridx = 0;
		gbc_pnlPlay.gridy = 1;
		add(pnlPlay, gbc_pnlPlay);
	}
	
	public void loadPlayer(SynthBank bank, Collection<Integer> usableBanks, Collection<Integer> usableProgs){
		pnlPlay.loadBank(bank, usableBanks, usableProgs);
	}
	
	public void startPlayer(){
		pnlPlay.openPlayChannel();
	}
	
	public void stopPlayer(){
		pnlPlay.closePlayChannel();
	}
	
	public void loadInfo(SoundbankNode node){
		List<String> keys = node.getMetadataKeys();
		Map<String, String> map = new HashMap<String, String>();
		
		for(String k : keys){
			map.put(k, node.getMetadataValue(k));
		}
		
		pnlInfo.loadData(map, keys);
	}

	
	public void onDoubleClickSelection(SoundbankNode selection) {
		//System.err.println("Double click!");
		loadInfo(selection);
	}

	public void dispose(){
		stopPlayer();
	}
	
}
