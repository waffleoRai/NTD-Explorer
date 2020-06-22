package waffleoRai_NTDExGUI.panels;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;
import java.awt.SystemColor;
import java.util.List;
import java.awt.Color;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import waffleoRai_Files.FileTypeNode;
import waffleoRai_NTDExGUI.icons.TypeIcon;

public class ChainViewPanel extends JPanel{

	private static final long serialVersionUID = -6762495982680816700L;

	public static final int VGAP_SIZE = 20;

	public ChainViewPanel(){
		setBackground(SystemColor.window);
	}
	
	private void resetGridbag(int nodecount){
		int rows = (nodecount << 1) + 1;
		int[] rheights = new int[rows+1];
		//Set VGAP_SIZE for all padding rows. Padding is #nodes - 1
		int pcount = nodecount-1;
		if(pcount > 0){
			int r = 2;
			for(int i = 0; i < pcount; i++){
				rheights[r] = VGAP_SIZE;
				r+=2;
			}
		}
		
		//Set row weights to 0 on all middle and 1 on all edges...
		double[] rweights = new double[rows+1];
		rweights[0] = 1.0;
		rweights[rweights.length-1] = Double.MIN_VALUE;
		rweights[rweights.length-2] = 1.0;
		
		GridBagLayout gbl = new GridBagLayout();
		gbl.columnWidths = new int[]{0, 0, 0, 0};
		gbl.rowHeights = rheights;
		gbl.columnWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl.rowWeights = rweights;
		setLayout(gbl);
		
	}
	
	private void addLabel(FileTypeNode node, int chainpos){
		if(node == null) return;
		String str = "";
		if(node.isCompression()) str = node.getCompressionDefinition().toString();
		else str = node.getTypeDefinition().toString();
		
		JLabel nlbl = new JLabel(str + " ");
		nlbl.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		nlbl.setHorizontalAlignment(SwingConstants.CENTER);
		nlbl.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		if(node.isCompression()) nlbl.setForeground(Color.GRAY);
		else nlbl.setIcon(TypeIcon.getTypeIcon(node.getFileClass()));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = (chainpos << 1) + 1;
		add(nlbl, gbc);
		
	}
	
	public void loadChain(List<FileTypeNode> chain){
		this.removeAll();
		if(chain == null){
			repaint(); return;
		}
		
		int ncount = chain.size();
		resetGridbag(ncount);
		
		int i = 0; 
		for(FileTypeNode node : chain) addLabel(node, i++);
		
		repaint();
	}
	
}
