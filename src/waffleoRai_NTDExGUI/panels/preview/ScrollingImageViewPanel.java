package waffleoRai_NTDExGUI.panels.preview;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.border.BevelBorder;

import waffleoRai_GUITools.CheckeredImagePane;

public class ScrollingImageViewPanel extends JPanel{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = 2190729810749695840L;
	
	/*----- Instance Variables -----*/
	
	private CheckeredImagePane pnlImg;
	
	/*----- Construction -----*/
	
	public ScrollingImageViewPanel(){
		initGUI();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		
		pnlImg = new CheckeredImagePane();
		scrollPane.setViewportView(pnlImg);
	}
	
	/*----- Getters -----*/
	
	public CheckeredImagePane getImagePanel(){
		return pnlImg;
	}

}
