package waffleoRai_NTDExGUI.dialogs;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.NTDProgramFiles;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExGUI.panels.AbstractGameOpenButton;
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

public class OpenDialog extends JDialog{

	private static final long serialVersionUID = 5001732512082598521L;
	
	public static final int SIZE_ADD_NOSCROLLER = 10;
	public static final int SIZE_ADD_SCROLLER = 20;
	public static final int MAX_BLOCKS_HEIGHT = 5;
	
	public static final int GUI_WIDTH_ADD = 15;
	public static final int GUI_HEIGHT_ADD = 65;
	
	public static final int TAB_IDX_GC = 3;
	public static final int TAB_IDX_DS = 4;
	public static final int TAB_IDX_WII = 5;
	public static final int TAB_IDX_3DS = 6;
	public static final int TAB_IDX_WIIU = 7;
	public static final int TAB_IDX_SWITCH = 8;
	public static final int TAB_IDX_PS1 = 0;
	public static final int TAB_IDX_N64 = 1;
	//public static final int TAB_IDX_GB = 99;
	//public static final int TAB_IDX_GBC = 99;
	public static final int TAB_IDX_GBA = 2;
	
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
		int gcount_psx = loadTab(new Console[]{Console.PS1}, pnlPSX, loadlist);
		
		JScrollPane spNUS = new JScrollPane(){
			private static final long serialVersionUID = -5907251996864073016L;
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				g.drawImage(pnlbkg, 0, 0, null);
			}
		};
		spNUS.getViewport().setOpaque(false);
		spNUS.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("N64", null, spNUS, null);
		tabbedPane.setEnabledAt(TAB_IDX_N64, false);
		
		JPanel pnlNUS = new JPanel();
		pnlNUS.setOpaque(false);
		spNUS.setViewportView(pnlNUS);
		int gcount_nus = loadTab(new Console[]{Console.N64}, pnlNUS, loadlist);
		
		JScrollPane spAGB = new JScrollPane(){
			private static final long serialVersionUID = -5907251596864073056L;
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				g.drawImage(pnlbkg, 0, 0, null);
			}
		};
		spAGB.getViewport().setOpaque(false);
		spAGB.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("GBA", null, spAGB, null);
		tabbedPane.setEnabledAt(TAB_IDX_GBA, false);
		
		JPanel pnlAGB = new JPanel();
		pnlAGB.setOpaque(false);
		spAGB.setViewportView(pnlAGB);
		int gcount_agb = loadTab(new Console[]{Console.GBA}, pnlAGB, loadlist);
		
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
		int gcount_gc = loadTab(new Console[]{Console.GAMECUBE}, pnlGC, loadlist);
		
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
		int gcount_ds = loadTab(new Console[]{Console.DS, Console.DSi}, pnlDS, loadlist);
		
		JScrollPane spWii = new JScrollPane(){
			private static final long serialVersionUID = -3566291281042523295L;

			public void paintComponent(Graphics g){
				super.paintComponent(g);
				if(pnlbkg != null) g.drawImage(pnlbkg, 0, 0, null);
			}
		};
		spWii.getViewport().setOpaque(false);
		spWii.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("Wii", null, spWii, null);
		tabbedPane.setEnabledAt(TAB_IDX_WII, false);
		
		JPanel pnlWii = new JPanel();
		spWii.setViewportView(pnlWii);
		pnlWii.setOpaque(false);
		int gcount_wii = loadTab(new Console[]{Console.WII}, pnlWii, loadlist);
		
		JScrollPane sp3DS = new JScrollPane(){
			private static final long serialVersionUID = -3566291281042523295L;

			public void paintComponent(Graphics g){
				super.paintComponent(g);
				if(pnlbkg != null) g.drawImage(pnlbkg, 0, 0, null);
			}
		};
		sp3DS.getViewport().setOpaque(false);
		sp3DS.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("3DS", null, sp3DS, null);
		tabbedPane.setEnabledAt(TAB_IDX_3DS, false);
		
		JPanel pnl3DS = new JPanel();
		sp3DS.setViewportView(pnl3DS);
		pnl3DS.setOpaque(false);
		int gcount_3ds = loadTab(new Console[]{Console._3DS}, pnl3DS, loadlist);
		
		JScrollPane spWiiU = new JScrollPane(){
			private static final long serialVersionUID = -3566291281042523295L;

			public void paintComponent(Graphics g){
				super.paintComponent(g);
				if(pnlbkg != null) g.drawImage(pnlbkg, 0, 0, null);
			}
		};
		spWiiU.getViewport().setOpaque(false);
		spWiiU.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("Wii U", null, spWiiU, null);
		tabbedPane.setEnabledAt(TAB_IDX_WIIU, false);
		
		JPanel pnlWiiU = new JPanel();
		spWiiU.setViewportView(pnlWiiU);
		pnlWiiU.setOpaque(false);
		int gcount_wiiu = loadTab(new Console[]{Console.WIIU}, pnlWiiU, loadlist);
		
		JScrollPane spSwitch = new JScrollPane(){
			private static final long serialVersionUID = -3566291281042523295L;

			public void paintComponent(Graphics g){
				super.paintComponent(g);
				if(pnlbkg != null) g.drawImage(pnlbkg, 0, 0, null);
			}
		};
		spSwitch.getViewport().setOpaque(false);
		spSwitch.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("Switch", null, spSwitch, null);
		tabbedPane.setEnabledAt(TAB_IDX_SWITCH, false);
		
		JPanel pnlSwitch = new JPanel();
		spSwitch.setViewportView(pnlSwitch);
		pnlSwitch.setOpaque(false);
		int gcount_switch = loadTab(new Console[]{Console.SWITCH}, pnlSwitch, loadlist);
		
		int tabcount = tabbedPane.getTabCount();
		blockCount = new int[tabcount];
		tabSizes = new Dimension[tabcount];
		setSPDimension(gcount_ds, TAB_IDX_DS, DefaultGameOpenButton.WIDTH, DefaultGameOpenButton.HEIGHT, spDS);
		setSPDimension(gcount_psx, TAB_IDX_PS1, DefaultGameOpenButton.WIDTH, DefaultGameOpenButton.HEIGHT, spPSX);
		setSPDimension(gcount_gc, TAB_IDX_GC, DefaultGameOpenButton.WIDTH, DefaultGameOpenButton.HEIGHT, spGC);
		setSPDimension(gcount_wii, TAB_IDX_WII, DefaultGameOpenButton.WIDTH, DefaultGameOpenButton.HEIGHT, spWii);
		setSPDimension(gcount_3ds, TAB_IDX_3DS, DefaultGameOpenButton.WIDTH, DefaultGameOpenButton.HEIGHT, sp3DS);
		setSPDimension(gcount_wiiu, TAB_IDX_WIIU, DefaultGameOpenButton.WIDTH, DefaultGameOpenButton.HEIGHT, spWiiU);
		setSPDimension(gcount_switch, TAB_IDX_SWITCH, DefaultGameOpenButton.WIDTH, DefaultGameOpenButton.HEIGHT, spSwitch);
		setSPDimension(gcount_nus, TAB_IDX_N64, DefaultGameOpenButton.WIDTH, DefaultGameOpenButton.HEIGHT, spNUS);
		setSPDimension(gcount_agb, TAB_IDX_GBA, DefaultGameOpenButton.WIDTH, DefaultGameOpenButton.HEIGHT, spAGB);
		
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
			dim.height = bheight * count + 40;
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
	
	private int loadTab(Console[] consoles, JPanel panel, Map<Console, Collection<NTDProject>> loadlist){
		GridBagLayout gbl_pnl = new GridBagLayout();
		panel.setLayout(gbl_pnl);
		
		List<NTDProject> addlist = new LinkedList<NTDProject>();
		for(Console c : consoles){
			Collection<NTDProject> coll = loadlist.get(c);
			if(coll != null) addlist.addAll(coll);
		}
		Collections.sort(addlist);
		
		int row = 0;
		for(NTDProject proj : addlist)
		{
			AbstractGameOpenButton gamepnl = proj.generateOpenButton();
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
