package waffleoRai_NTDExGUI.panels.preview;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import javax.swing.border.BevelBorder;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import waffleoRai_GUITools.CheckeredImagePane;
import waffleoRai_GUITools.ImagePaneDrawer;
import waffleoRai_NTDExCore.NTDValues;

import javax.swing.JLabel;
import javax.swing.JSpinner;

public class TilesetViewPanel extends JPanel{
	
	/*----- Constants -----*/

	private static final long serialVersionUID = -8401843051685024745L;

	/*----- Instance Variables -----*/
	
	private int t_per_row;
	private JLabel lblTileSize;
	private JSpinner spinner;
	
	private CheckeredImagePane pnlImg;
	
	private BufferedImage[] tiles;
	
	
	/*----- Construction -----*/
	
	public TilesetViewPanel(){
		t_per_row = 8;
		
		initGUI();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{35, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlControl = new JPanel();
		pnlControl.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlControl = new GridBagConstraints();
		gbc_pnlControl.insets = new Insets(0, 0, 5, 0);
		gbc_pnlControl.fill = GridBagConstraints.BOTH;
		gbc_pnlControl.gridx = 0;
		gbc_pnlControl.gridy = 0;
		add(pnlControl, gbc_pnlControl);
		GridBagLayout gbl_pnlControl = new GridBagLayout();
		gbl_pnlControl.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlControl.rowHeights = new int[]{0, 0};
		gbl_pnlControl.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlControl.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlControl.setLayout(gbl_pnlControl);
		
		lblTileSize = new JLabel("Tile Size: n x n");
		GridBagConstraints gbc_lblTileSize = new GridBagConstraints();
		gbc_lblTileSize.insets = new Insets(5, 5, 5, 5);
		gbc_lblTileSize.gridx = 0;
		gbc_lblTileSize.gridy = 0;
		pnlControl.add(lblTileSize, gbc_lblTileSize);
		
		JLabel lblTilesPerRow = new JLabel("Tiles per Row:");
		GridBagConstraints gbc_lblTilesPerRow = new GridBagConstraints();
		gbc_lblTilesPerRow.insets = new Insets(5, 0, 5, 5);
		gbc_lblTilesPerRow.gridx = 2;
		gbc_lblTilesPerRow.gridy = 0;
		pnlControl.add(lblTilesPerRow, gbc_lblTilesPerRow);
		
		spinner = new JSpinner();
		spinner.setValue(t_per_row);
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.BOTH;
		gbc_spinner.insets = new Insets(5, 0, 5, 5);
		gbc_spinner.gridx = 3;
		gbc_spinner.gridy = 0;
		pnlControl.add(spinner, gbc_spinner);
		spinner.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				setTilesPerRowInternal((Integer)spinner.getValue());
			}
			
		});
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);
		
		pnlImg = new CheckeredImagePane();
		scrollPane.setViewportView(pnlImg);
	}
	
	/*----- Classes -----*/
	
	private static class TileDrawer implements ImagePaneDrawer{

		private int x_coord;
		private int y_coord;
		private BufferedImage tile;
		
		public TileDrawer(int x, int y, BufferedImage t){
			x_coord = x;
			y_coord = y;
			tile = t;
			//System.err.println("x,y,tile -- " + x + "," + y + "," + (tile != null));
		}
		
		public int getX() {return x_coord;}
		public int getY() {return y_coord;}
		public int getWidth() {return tile.getWidth();}
		public int getHeight() {return tile.getHeight();}

		public void drawMe(Graphics g, int x, int y) {
			g.drawImage(tile, x+x_coord, y+y_coord, null);
		}
		
	}
	
	/*----- Paint -----*/
	
	private void updateMe(){

		pnlImg.clearItems();
		if(tiles == null){
			lblTileSize.setText("Tile Size: 0 x 0");
			lblTileSize.repaint();
			pnlImg.repaint();
			return;
		}
		
		//Tile size
		int tdim = tiles[0].getWidth();
		lblTileSize.setText("Tile Size: " + tdim + " x " + tdim);
		lblTileSize.repaint();
		
		final int gap = 1;
		int tcount = tiles.length;
		int lcount = t_per_row;
		//System.err.println("Tiles per row: " + t_per_row);
		int rows = tcount/lcount + 1;
		pnlImg.setDrawingAreaSize(new Dimension((lcount*(gap + tdim)), (rows*(gap+tdim))));
		
		int x = 0; int y = 0;
		int l = 0;
		for(int t = 0; t < tcount; t++){
			TileDrawer td = new TileDrawer(x, y, tiles[t]);
			pnlImg.addItem(td);
			
			x += tdim + gap;
			if(++l >= lcount){
				l = 0; x = 0;
				y += tdim + gap;
			}
		}
		
		pnlImg.repaint();
	}
	
	/*----- Getters -----*/
	
	/*----- Setters -----*/
	
	public void setTilesPerRow(int tpr){
		if(tpr < 1) tpr = 8;
		spinner.setValue(tpr);
	}
	
	private void setTilesPerRowInternal(int tpr){
		if(tpr <= 0) return;
		t_per_row = tpr;
		NTDValues.setTLE_ExportWidth(tpr);
		updateMe();
	}
	
	public void loadTiles(List<BufferedImage> tilelist){
		if(tilelist == null || tilelist.isEmpty()){
			tiles = null;
			updateMe();
			return;
		}
		
		tiles = new BufferedImage[tilelist.size()];
		int i = 0;
		for(BufferedImage img : tilelist) tiles[i++] = img;
		updateMe();
	}
	
}
