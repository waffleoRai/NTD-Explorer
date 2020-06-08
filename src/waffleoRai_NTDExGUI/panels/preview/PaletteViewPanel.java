package waffleoRai_NTDExGUI.panels.preview;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

import waffleoRai_GUITools.CheckeredImagePane;
import waffleoRai_GUITools.ImagePaneDrawer;
import waffleoRai_Image.Palette;
import waffleoRai_Image.Pixel;
import javax.swing.JLabel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class PaletteViewPanel extends JPanel{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = 1476355455771983101L;
	
	/*----- Instance Variables -----*/

	private Palette[] plts;
	private int plt_idx;
	
	private CheckeredImagePane pnlImg;
	private JTable tblInfo;
	private JComboBox<String> comboBox;
	
	/*----- Construction -----*/
	
	public PaletteViewPanel(){
		initGUI();
		fillTable();
		drawPalette();
	}
	
	public PaletteViewPanel(Palette p){
		plts = new Palette[1];
		plts[0] = p;
		initGUI();
		fillTable();
		drawPalette();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{35, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlControl = new JPanel();
		GridBagConstraints gbc_pnlControl = new GridBagConstraints();
		gbc_pnlControl.gridwidth = 2;
		gbc_pnlControl.insets = new Insets(0, 0, 5, 5);
		gbc_pnlControl.fill = GridBagConstraints.BOTH;
		gbc_pnlControl.gridx = 0;
		gbc_pnlControl.gridy = 0;
		add(pnlControl, gbc_pnlControl);
		GridBagLayout gbl_pnlControl = new GridBagLayout();
		gbl_pnlControl.columnWidths = new int[]{0, 0, 0};
		gbl_pnlControl.rowHeights = new int[]{0, 0, 0};
		gbl_pnlControl.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlControl.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		pnlControl.setLayout(gbl_pnlControl);
		
		comboBox = new JComboBox<String>();
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(5, 5, 5, 150);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;
		pnlControl.add(comboBox, gbc_comboBox);
		comboBox.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				setPaletteIndex(comboBox.getSelectedIndex());
			}
			
		});
		updateCombobox();
		
		JLabel label = new JLabel("");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.gridx = 1;
		gbc_label.gridy = 1;
		pnlControl.add(label, gbc_label);
		
		JScrollPane spImage = new JScrollPane();
		spImage.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spImage = new GridBagConstraints();
		gbc_spImage.weightx = 1.0;
		gbc_spImage.insets = new Insets(0, 0, 0, 5);
		gbc_spImage.fill = GridBagConstraints.BOTH;
		gbc_spImage.gridx = 0;
		gbc_spImage.gridy = 1;
		add(spImage, gbc_spImage);
		
		pnlImg = new CheckeredImagePane();
		spImage.setViewportView(pnlImg);
		
		JScrollPane spInfo = new JScrollPane();
		spInfo.setViewportBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_spInfo = new GridBagConstraints();
		gbc_spInfo.weightx = 0.4;
		gbc_spInfo.fill = GridBagConstraints.BOTH;
		gbc_spInfo.gridx = 1;
		gbc_spInfo.gridy = 1;
		add(spInfo, gbc_spInfo);
		
		tblInfo = new JTable();
		spInfo.setViewportView(tblInfo);
	}

	private void drawPalette(){

		pnlImg.clearItems();
		if(plts == null){
			pnlImg.repaint();
			return;
		}
		
		Palette plt = plts[plt_idx];
		if(plt == null){
			pnlImg.repaint();
			return;
		}
		
		int l = 0;
		int x = 0; int y = 0;
		int rad = 10;
		int circ = rad << 1;
		int gap = 0;
		int dim = 1;
		switch(plt.getBitDepth()){
		case 4:
			//4x4
			dim = (4*(circ + gap) + 10);
			pnlImg.setDrawingAreaSize(new Dimension(dim, dim));
			for(int i = 0; i < 16; i++){
				Pixel px = plt.getPixel(i);
				Color c = new Color(px.getRed(), px.getGreen(), px.getBlue());
				CircleDrawer cd = new CircleDrawer(x, y, rad, c);
				pnlImg.addItem(cd);
				
				x += circ + gap;
				if(l++ >= 3){
					l = 0; x = 0;
					y += circ + gap;
				}
			}
			break;
		case 8:
			//16x16
			rad = 5;
			gap = 1;
			circ = rad << 1;
			dim = (16*(circ + gap) + 10);
			pnlImg.setDrawingAreaSize(new Dimension(dim, dim));
			for(int i = 0; i < 256; i++){
				Pixel px = plt.getPixel(i);
				Color c = new Color(px.getRed(), px.getGreen(), px.getBlue());
				CircleDrawer cd = new CircleDrawer(x, y, rad, c);
				pnlImg.addItem(cd);
				
				x += circ + gap;
				if(l++ >= 15){
					l = 0; x = 0;
					y += circ + gap;
				}
			}
			break;
		}
		
		pnlImg.repaint();
		
	}
	
	private void fillTable(){

		String[] cols = {"Color Index", "Hex Value", "Red", "Green", "Blue", "Alpha"};
		
		if(plts == null || plts[plt_idx] == null){
			String[][] empty = new String[1][cols.length];
			for(int i = 0; i < cols.length; i++)empty[0][i] = "";
			DefaultTableModel mdl = new DefaultTableModel(empty, cols);
			tblInfo.setModel(mdl);
			tblInfo.repaint();
			return;
		}
		
		Palette plt = plts[plt_idx];
		int ccount = 0;
		switch(plt.getBitDepth()){
		case 4: ccount = 16; break;
		case 8: ccount = 256; break;
		}
		
		String[][] dat = new String[ccount][cols.length];
		for(int i = 0; i < ccount; i++){
			dat[i][0] = Integer.toString(i);
			Pixel px = plt.getPixel(i);
			dat[i][1] = "#" + String.format("%06x", (px.getARGB() & 0xFFFFFF));
			dat[i][2] = Integer.toString(px.getRed());
			dat[i][3] = Integer.toString(px.getGreen());
			dat[i][4] = Integer.toString(px.getBlue());
			dat[i][5] = Integer.toString(px.getAlpha());
		}
		
		DefaultTableModel mdl = new DefaultTableModel(dat, cols);
		tblInfo.setModel(mdl);
		tblInfo.repaint();
	}
	
	private void updateCombobox(){
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
		
		if(plts == null){
			comboBox.setModel(model);
			comboBox.setEnabled(false);
			return;
		}
		
		for(int i = 0; i < plts.length; i++){
			model.addElement("Palette " + i);
		}
		
		comboBox.setModel(model);
		comboBox.setEnabled(true);
		comboBox.repaint();
	}
	
	/*----- Classes -----*/
	
	private static class CircleDrawer implements ImagePaneDrawer{

		private int x_coord;
		private int y_coord;
		private int radius;
		private Color color;
		
		public CircleDrawer(int x, int y, int r, Color c){
			x_coord = x;
			y_coord = y;
			radius = r;
			color = c;
		}
		
		public int getX() {return x_coord;}
		public int getY() {return y_coord;}
		public int getWidth() {return radius << 1;}
		public int getHeight() {return radius << 1;}

		public void drawMe(Graphics g, int x, int y) {
			g.setColor(color);
			
			int dim = radius << 1;
			g.fillOval(x + x_coord, y + y_coord, dim, dim);
		}
		
	}
	
	/*----- Getters -----*/
	
	/*----- Setters -----*/
	
	public void setPaletteIndex(int i){
		plt_idx = i;
		fillTable();
		drawPalette();
	}
	
	public void setPalette(Palette p){
		plts = new Palette[1];
		plts[0] = p;
		plt_idx = 0;
		updateCombobox();
		fillTable();
		drawPalette();
	}
	
	public void loadPalettes(List<Palette> palettes){
		if(palettes == null || palettes.isEmpty()){
			plts = null;
			plt_idx = 0;
			updateCombobox();
			fillTable();
			drawPalette();
			return;
		}
		
		plts = new Palette[palettes.size()];
		int i = 0;
		for(Palette p : palettes) plts[i++] = p;
		
		plt_idx = 0;
		updateCombobox();
		fillTable();
		drawPalette();
	}
	
}
