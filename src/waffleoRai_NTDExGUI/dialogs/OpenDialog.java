package waffleoRai_NTDExGUI.dialogs;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExGUI.panels.DefaultGameOpenButton;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import waffleoRai_Containers.nintendo.GCMemCard;

public class OpenDialog extends JDialog{

	private static final long serialVersionUID = 5001732512082598521L;
	
	public static final int SIZE_ADD_NOSCROLLER = 10;
	public static final int SIZE_ADD_SCROLLER = 20;
	public static final int MAX_BLOCKS_HEIGHT = 5;
	
	public static final int GUI_WIDTH_ADD = 15;
	public static final int GUI_HEIGHT_ADD = 65;
	
	public static final int TAB_IDX_GC = 1;
	public static final int TAB_IDX_DS = 2;
	public static final int TAB_IDX_WII = 3;
	public static final int TAB_IDX_3DS = 4;
	public static final int TAB_IDX_WIIU = 5;
	public static final int TAB_IDX_SWITCH = 6;
	public static final int TAB_IDX_PS1 = 0;
	//public static final int TAB_IDX_N64 = 99;
	//public static final int TAB_IDX_GB = 99;
	//public static final int TAB_IDX_GBC = 99;
	//public static final int TAB_IDX_GBA = 99;
	
	//private Timer ds_timer;
	private NTDProject selection;
	private List<ActionListener> listeners;
	
	private JTabbedPane tabbedPane;
	private Dimension[] tabSizes;
	private int[] blockCount;
	
	private BufferedImage pnlbkg;
	private static BufferedImage btnOff;
	private static BufferedImage btnOn;

 	public OpenDialog(Frame parent, Map<Console, Collection<NTDProject>> loadlist) 
	{
		super(parent, true);
		//setLocationRelativeTo(parent);
		setResizable(false);
		listeners = new LinkedList<ActionListener>();
		initGUI(loadlist);
		setLocationRelativeTo(parent);
	}
	
	private void initGUI(Map<Console, Collection<NTDProject>> loadlist)
	{
		setTitle("Open");
		try{
			pnlbkg = ImageIO.read(OpenDialog.class.getResource("/waffleoRai_NTDExCore/res/general_gradient_bkg_1.png"));
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JScrollPane spPSX = new JScrollPane(){
			private static final long serialVersionUID = -5907251596864073016L;
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				g.drawImage(pnlbkg, 0, 0, null);
			}
		};
		spPSX.getViewport().setOpaque(false);
		spPSX.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("PS1", null, spPSX, null);
		tabbedPane.setEnabledAt(TAB_IDX_PS1, false);
		
		JPanel pnlPSX = new JPanel();
		pnlPSX.setOpaque(false);
		spPSX.setViewportView(pnlPSX);
		int gcount_psx = loadPSXTab(pnlPSX, loadlist);
		
		JScrollPane spGC = new JScrollPane(){
			private static final long serialVersionUID = -5907251596864073016L;
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				g.drawImage(pnlbkg, 0, 0, null);
			}
		};
		spGC.getViewport().setOpaque(false);
		spGC.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("GameCube", null, spGC, null);
		tabbedPane.setEnabledAt(TAB_IDX_GC, false);
		
		JPanel pnlGC = new JPanel();
		pnlGC.setOpaque(false);
		spGC.setViewportView(pnlGC);
		int gcount_gc = loadGCTab(pnlGC, loadlist);
		
		JScrollPane spDS = new JScrollPane(){
			
			private static final long serialVersionUID = 2446270250180489468L;

			public void paintComponent(Graphics g){
				super.paintComponent(g);
				if(pnlbkg != null) g.drawImage(pnlbkg, 0, 0, null);
			}
		};
		spDS.getViewport().setOpaque(false);
		spDS.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("DS/DSi", null, spDS, null);
		
		JPanel pnlDS = new JPanel();
		spDS.setViewportView(pnlDS);
		pnlDS.setOpaque(false);
		int gcount_ds = loadDSTab(pnlDS, loadlist);
		
		JScrollPane spWii = new JScrollPane();
		spWii.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("Wii", null, spWii, null);
		tabbedPane.setEnabledAt(TAB_IDX_WII, false);
		
		JScrollPane sp3DS = new JScrollPane();
		sp3DS.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("3DS", null, sp3DS, null);
		tabbedPane.setEnabledAt(TAB_IDX_3DS, false);
		
		JScrollPane spWiiU = new JScrollPane();
		spWiiU.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("Wii U", null, spWiiU, null);
		tabbedPane.setEnabledAt(TAB_IDX_WIIU, false);
		
		JScrollPane spSwitch = new JScrollPane();
		spSwitch.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("Switch", null, spSwitch, null);
		tabbedPane.setEnabledAt(TAB_IDX_SWITCH, false);
		
		int tabcount = tabbedPane.getTabCount();
		blockCount = new int[tabcount];
		tabSizes = new Dimension[tabcount];
		setSPDimension(gcount_ds, TAB_IDX_DS, DefaultGameOpenButton.WIDTH, DefaultGameOpenButton.HEIGHT, spDS);
		setSPDimension(gcount_psx, TAB_IDX_PS1, DefaultGameOpenButton.WIDTH, DefaultGameOpenButton.HEIGHT, spPSX);
		setSPDimension(gcount_gc, TAB_IDX_GC, DefaultGameOpenButton.WIDTH, DefaultGameOpenButton.HEIGHT, spGC);
		
		for(int i = 0; i < blockCount.length; i++)
		{
			if(blockCount[i] > 0){
				tabbedPane.setSelectedIndex(i);
				break;
			}
		}
		
		tabbedPane.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				onTabChange();
			}
			
		});
		
		adjustMySizeToTab();
	}
	
	private void setSPDimension(int count, int tabidx, int bwidth, int bheight, JScrollPane sp){
		Dimension dim = new Dimension();
		if(count == 0) tabbedPane.setEnabledAt(tabidx, false);
		else if(count < MAX_BLOCKS_HEIGHT)
		{
			tabbedPane.setEnabledAt(tabidx, true);
			dim.width = bwidth + SIZE_ADD_NOSCROLLER + 5;
			dim.height = bheight * count + 20;
		}
		else
		{
			tabbedPane.setEnabledAt(tabidx, true);
			dim.width = bwidth + SIZE_ADD_SCROLLER;
			dim.height = bheight * MAX_BLOCKS_HEIGHT;
		}
		sp.setMinimumSize(dim);
		sp.setPreferredSize(dim);
		
		tabSizes[tabidx] = dim; blockCount[tabidx] = count;
	}
	
	private int loadDSTab(JPanel panel, Map<Console, Collection<NTDProject>> loadlist)
	{
		GridBagLayout gbl_pnlDS = new GridBagLayout();
		panel.setLayout(gbl_pnlDS);
		
		List<NTDProject> addlist = new LinkedList<NTDProject>();
		Collection<NTDProject> ntr = loadlist.get(Console.DS);
		if(ntr != null) addlist.addAll(ntr);
		Collection<NTDProject> twl = loadlist.get(Console.DSi);
		if(twl != null) addlist.addAll(twl);
		Collections.sort(addlist);
		
		int row = 0;
		for(NTDProject proj : addlist)
		{
			DefaultGameOpenButton gamepnl = new DefaultGameOpenButton();
			int millis = 0;
			if(proj.getBannerIcon() != null && proj.getBannerIcon().length > 1){
				millis = (int)Math.round(1000.0/30.0);
			}
			gamepnl.loadMe(proj, millis);
			gamepnl.addActionListener(new ClickyListener(proj));
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridwidth = 1; gbc.gridheight = 1;
			gbc.gridx = 0; gbc.gridy = row;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.insets = new Insets(1,0,1,0);
			row++;
			panel.add(gamepnl, gbc);
		}
		if(addlist.size() < MAX_BLOCKS_HEIGHT){
			//Add a dummy gridbag row
			int rcount = row+1;
			gbl_pnlDS.rowWeights = new double[rcount+1];
			gbl_pnlDS.rowHeights = new int[rcount+1];
			gbl_pnlDS.rowWeights[rcount] = Double.MIN_VALUE;
			gbl_pnlDS.rowWeights[rcount-1] = 1.0;
		}
		
		return addlist.size();
	}
	
	private int loadPSXTab(JPanel panel, Map<Console, Collection<NTDProject>> loadlist){
		
		GridBagLayout gbl_pnl = new GridBagLayout();
		panel.setLayout(gbl_pnl);
		
		List<NTDProject> addlist = new LinkedList<NTDProject>();
		Collection<NTDProject> ps1 = loadlist.get(Console.PS1);
		if(ps1 != null) addlist.addAll(ps1);
		Collections.sort(addlist);
		
		int row = 0;
		for(NTDProject proj : addlist)
		{
			DefaultGameOpenButton gamepnl = new DefaultGameOpenButton();
			int millis = 0;
			if(proj.getBannerIcon() != null){
				if(proj.getBannerIcon().length == 2){
					millis = (int)Math.round((16.0/50.0) * 1000.0);
				}
				else if(proj.getBannerIcon().length == 3){
					millis = (int)Math.round((11.0/50.0) * 1000.0);
				}
			}
			gamepnl.loadMe(proj, millis);
			gamepnl.addActionListener(new ClickyListener(proj));
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.insets = new Insets(1,0,1,0);
			row++;
			panel.add(gamepnl, gbc);
		}
		if(addlist.size() < MAX_BLOCKS_HEIGHT){
			//Add a dummy gridbag row
			int rcount = row+1;
			gbl_pnl.rowWeights = new double[rcount+1];
			gbl_pnl.rowHeights = new int[rcount+1];
			gbl_pnl.rowWeights[rcount] = Double.MIN_VALUE;
			gbl_pnl.rowWeights[rcount-1] = 1.0;
		}
		
		return addlist.size();
	}

	private int loadGCTab(JPanel panel, Map<Console, Collection<NTDProject>> loadlist){
		GridBagLayout gbl_pnl = new GridBagLayout();
		panel.setLayout(gbl_pnl);
		
		List<NTDProject> addlist = new LinkedList<NTDProject>();
		Collection<NTDProject> projs = loadlist.get(Console.GAMECUBE);
		if(projs != null) addlist.addAll(projs);
		Collections.sort(addlist);
		
		int row = 0;
		for(NTDProject proj : addlist)
		{
			DefaultGameOpenButton gamepnl = new DefaultGameOpenButton();
			int millis = GCMemCard.ICO_FRAME_MILLIS * 4;
			gamepnl.loadMe(proj, millis);
			gamepnl.addActionListener(new ClickyListener(proj));
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.insets = new Insets(1,0,1,0);
			row++;
			panel.add(gamepnl, gbc);
		}
		if(addlist.size() < MAX_BLOCKS_HEIGHT){
			//Add a dummy gridbag row
			int rcount = row+1;
			gbl_pnl.rowWeights = new double[rcount+1];
			gbl_pnl.rowHeights = new int[rcount+1];
			gbl_pnl.rowWeights[rcount] = Double.MIN_VALUE;
			gbl_pnl.rowWeights[rcount-1] = 1.0;
		}
		
		return addlist.size();
	}
	
	private class ClickyListener implements ActionListener
	{
		private NTDProject myproj;
		
		public ClickyListener(NTDProject p)
		{
			myproj = p;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			//System.err.println("Project clicked: " + myproj.getGameCode12());
			selection = myproj;
			for(ActionListener l : listeners) l.actionPerformed(new ActionEvent(this, 0, "proj" + myproj.getGameCode12() + "_select"));
		}	
	}

	public NTDProject getSelection()
	{
		return this.selection;
	}
	
	public void addActionListener(ActionListener l)
	{
		listeners.add(l);
	}
	
	public void closeMe()
	{
		setVisible(false);
		WindowListener[] listeners = this.getWindowListeners();
		for(WindowListener l : listeners) l.windowClosing(new WindowEvent(this, 0));
		dispose();
	}
	
	public void onTabChange(){
		//System.err.println("Tab change heard!");
		adjustMySizeToTab();
	}
	
	public void adjustMySizeToTab()
	{
		int tidx = tabbedPane.getSelectedIndex();
		Dimension d = tabSizes[tidx];
		Dimension dim = new Dimension(DefaultGameOpenButton.WIDTH + GUI_WIDTH_ADD, GUI_HEIGHT_ADD);
		if(d != null){
			dim = new Dimension(d.width + GUI_WIDTH_ADD, d.height + GUI_HEIGHT_ADD);
		}
		
		setMinimumSize(dim);
		setPreferredSize(dim);
		setMaximumSize(dim);
		
		tabbedPane.setMinimumSize(dim);
		tabbedPane.setPreferredSize(dim);
		//tabbedPane.setMaximumSize(dim);
		
		this.repaint();
	}
	
	public static BufferedImage getButtonImage(){
		if(btnOff == null){
			try{
				btnOff = ImageIO.read(NTDProgramFiles.class.getResource("/waffleoRai_NTDExCore/res/general_gradient_btn_2.png"));
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		return btnOff;
	}
	
	public static BufferedImage getSelectedButtonImage(){
		if(btnOn == null){
			try{
				btnOn = ImageIO.read(NTDProgramFiles.class.getResource("/waffleoRai_NTDExCore/res/general_gradient_btn_3.png"));
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		return btnOn;
	}
	
}
