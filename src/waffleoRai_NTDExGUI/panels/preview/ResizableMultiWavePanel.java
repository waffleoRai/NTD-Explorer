package waffleoRai_NTDExGUI.panels.preview;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

public class ResizableMultiWavePanel extends JPanel{

	private static final long serialVersionUID = -1921154913820868763L;
	
	private WaveRenderPanel[] panels;
	
	public ResizableMultiWavePanel(int chcount){
		changeChannelCount(chcount);
	}
	
	public void changeChannelCount(int chcount){
		panels = new WaveRenderPanel[chcount];
		this.removeAll();
		
		GridBagLayout gbl_pnlWav = new GridBagLayout();
		gbl_pnlWav.columnWidths = new int[]{0};
		gbl_pnlWav.rowHeights = new int[]{0};
		gbl_pnlWav.columnWeights = new double[]{1.0};
		gbl_pnlWav.rowWeights = new double[panels.length];
		for(int i = 0; i < panels.length; i++) gbl_pnlWav.rowWeights[i] = 1.0;
		setLayout(gbl_pnlWav);
		
		int inset = 2;
		//Dimension mysize = getSize();
		//int w = mysize.width;
		//int h = (mysize.height/panels.length) - (inset << 1);
		int w = 100; //Default
		int h = 20; //Default
		for(int i = 0; i < panels.length; i++){
			panels[i] = new WaveRenderPanel(w, h);
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = i;
			gbc.insets = new Insets(inset, 0, inset, 0);
			add(panels[i], gbc);
			
		}
	}
	
	public void loadWaveData(int chidx, double[] dat){
		panels[chidx].setData(dat);
	}
	
	public int getChannelCount(){
		return panels.length;
	}
	
	public void clearData(){
		for(int i = 0; i < panels.length; i++){
			panels[i].clearData();
		}
	}

}
