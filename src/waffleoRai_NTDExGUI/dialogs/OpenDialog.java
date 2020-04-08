package waffleoRai_NTDExGUI.dialogs;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.NTDProject;
import waffleoRai_NTDExGUI.panels.DSPreviewPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class OpenDialog extends JDialog{

	private static final long serialVersionUID = 5001732512082598521L;
	
	public static final int SIZE_ADD_NOSCROLLER = 10;
	public static final int SIZE_ADD_SCROLLER = 20;
	public static final int MAX_BLOCKS_HEIGHT = 5;
	
	public static final int GUI_WIDTH_ADD = 10;
	public static final int GUI_HEIGHT_ADD = 65;
	
	public static final int TAB_IDX_GC = 0;
	public static final int TAB_IDX_DS = 1;
	public static final int TAB_IDX_WII = 2;
	public static final int TAB_IDX_3DS = 3;
	public static final int TAB_IDX_WIIU = 4;
	public static final int TAB_IDX_SWITCH = 5;
	//public static final int TAB_IDX_PS1 = 99;
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

 	public OpenDialog(Frame parent, Map<Console, Collection<NTDProject>> loadlist) 
	{
		super(parent, true);
		setLocationRelativeTo(parent);
		setResizable(false);
		listeners = new LinkedList<ActionListener>();
		initGUI(loadlist);
	}
	
	private void initGUI(Map<Console, Collection<NTDProject>> loadlist)
	{
		setTitle("Open");
		
		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JScrollPane spGC = new JScrollPane();
		spGC.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("GameCube", null, spGC, null);
		tabbedPane.setEnabledAt(TAB_IDX_GC, false);
		
		JScrollPane spDS = new JScrollPane();
		spDS.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("DS/DSi", null, spDS, null);
		
		JPanel pnlDS = new JPanel();
		spDS.setViewportView(pnlDS);
		int gcount_ds = loadDSTab(pnlDS, loadlist);
		Dimension dim_ds = new Dimension();
		if(gcount_ds == 0) tabbedPane.setEnabledAt(TAB_IDX_DS, false);
		else if(gcount_ds < MAX_BLOCKS_HEIGHT)
		{
			tabbedPane.setEnabledAt(TAB_IDX_DS, true);
			dim_ds.width = DSPreviewPanel.WIDTH + SIZE_ADD_NOSCROLLER;
			dim_ds.height = DSPreviewPanel.HEIGHT * gcount_ds;
		}
		else
		{
			tabbedPane.setEnabledAt(TAB_IDX_DS, true);
			dim_ds.width = DSPreviewPanel.WIDTH + SIZE_ADD_SCROLLER;
			dim_ds.height = DSPreviewPanel.HEIGHT * MAX_BLOCKS_HEIGHT;
		}
		spDS.setMinimumSize(dim_ds);
		spDS.setPreferredSize(dim_ds);
		
		JScrollPane spWii = new JScrollPane();
		spWii.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("Wii", null, spWii, null);
		tabbedPane.setEnabledAt(TAB_IDX_WII, false);
		
		JScrollPane sp3DS = new JScrollPane();
		sp3DS.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tabbedPane.addTab("3DS", null, sp3DS, null);
		tabbedPane.setEnabledAt(TAB_IDX_3DS, false);
		
		int tabcount = tabbedPane.getTabCount();
		blockCount = new int[tabcount];
		tabSizes = new Dimension[tabcount];
		tabSizes[TAB_IDX_DS] = dim_ds; blockCount[TAB_IDX_DS] = gcount_ds;
		
		for(int i = 0; i < blockCount.length; i++)
		{
			if(blockCount[i] > 0)
			{
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
	
	private int loadDSTab(JPanel panel, Map<Console, Collection<NTDProject>> loadlist)
	{
		GridBagLayout gbl_pnlDS = new GridBagLayout();
		//gbl_pnlDS.columnWidths = new int[] {0};
		//gbl_pnlDS.rowHeights = new int[] {0};
		//gbl_pnlDS.columnWeights = new double[]{0.0};
		//gbl_pnlDS.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_pnlDS);
		
		List<NTDProject> addlist = new LinkedList<NTDProject>();
		Collection<NTDProject> ntr = loadlist.get(Console.DS);
		if(ntr != null) addlist.addAll(ntr);
		Collection<NTDProject> twl = loadlist.get(Console.DSi);
		if(twl != null) addlist.addAll(twl);
		Collections.sort(addlist);
		
		final int pnl_width = DSPreviewPanel.WIDTH;
		final int pnl_height = DSPreviewPanel.HEIGHT;
		
		int row = 0;
		for(NTDProject proj : addlist)
		{
			//System.err.println("Adding proj panel: " + proj.getGameCode12());
			DSPreviewPanel gamepnl = new DSPreviewPanel();
			gamepnl.loadMe(proj, null);
			gamepnl.addActionListener(new ClickyListener(proj));
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.gridx = 0;
			gbc.gridy = row;
			gbc.anchor = GridBagConstraints.NORTH;
			row++;
			panel.add(gamepnl, gbc);
		}
		
		//Set panel size...
		Dimension sz = new Dimension(pnl_width, (pnl_height * addlist.size()));
		panel.setMinimumSize(sz);
		//panel.setMaximumSize(sz);
		panel.setPreferredSize(sz);
		
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
	
	public void onTabChange()
	{
		adjustMySizeToTab();
	}
	
	public void adjustMySizeToTab()
	{
		int tidx = tabbedPane.getSelectedIndex();
		Dimension d = tabSizes[tidx];
		Dimension dim = new Dimension(DSPreviewPanel.WIDTH + GUI_WIDTH_ADD, GUI_HEIGHT_ADD);
		if(d != null){
			dim = new Dimension(d.width + GUI_WIDTH_ADD, d.height + GUI_HEIGHT_ADD);
		}
		
		setMinimumSize(dim);
		setPreferredSize(dim);
		
		this.repaint();
	}
	
}
