package waffleoRai_NTDExGUI.panels.preview;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;
import java.awt.Font;

public class StringmapInfoPanel extends JPanel{

	private static final long serialVersionUID = 7801701104177791390L;

	
	private Map<String, String> string_map;
	private List<String> key_order;
	
	private JScrollPane scrollPane;
	private JTextPane textPane;
	
	public StringmapInfoPanel(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		
		textPane = new JTextPane();
		textPane.setFont(new Font("Courier New", Font.PLAIN, 11));
		scrollPane.setViewportView(textPane);
	}
	
	public void loadData(Map<String, String> smap, List<String> keyorder){
		string_map = smap;
		key_order = keyorder;
		
		refreshMe();
	}
	
	public void refreshMe(){
		if(string_map == null){
			textPane.setText("");
			
			textPane.repaint();
			scrollPane.repaint();
			return;
		}
		
		if(key_order == null){
			key_order = new ArrayList<String>(string_map.size()+1);
			key_order.addAll(string_map.keySet());
			Collections.sort(key_order);
		}
		
		StringBuilder sb = new StringBuilder(256 * string_map.size());
		
		for(String k : key_order){
			sb.append(k + ": ");
			sb.append(string_map.get(k) + "\n");
		}
		
		textPane.setText(sb.toString());
		
		textPane.repaint();
		scrollPane.repaint();
	}
	
}
