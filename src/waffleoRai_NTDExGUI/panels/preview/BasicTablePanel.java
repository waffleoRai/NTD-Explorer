package waffleoRai_NTDExGUI.panels.preview;

import waffleoRai_NTDExGUI.DisposableJPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.Insets;
import javax.swing.JTable;
import java.awt.Font;

public class BasicTablePanel extends DisposableJPanel{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = 3148536415652505394L;
	
	/*----- Instance Variables -----*/
	
	private JTable table;
	
	/*----- Construction -----*/
	
	public BasicTablePanel(){
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
		
		table = new JTable();
		table.setFont(new Font("Courier New", Font.PLAIN, 11));
		table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);
	}
	
	/*----- Getters -----*/
	
	public static String[] genDefaultColNames(int columns){
		//Excel style alphabet
		if(columns < 1) return null;
		String[] arr = new String[columns];
		String prefix = "";
		char c = 'A';
		for(int i = 0; i < columns; i++){
			arr[i] = prefix + c;
			
			if(++c > 'Z'){
				int popped = 1;
				
				//Pop the last letter off the prefix and see if it can be incremented
				while(prefix.length() > 0){
					char c2 = prefix.charAt(prefix.length()-1);
					prefix = prefix.substring(0, prefix.length()-1);
					
					if(++c2 > 'Z') popped++;
					else{
						//We good. Break.
						prefix += c2;
						break;
					}
				}
				
				for(int j = 0; j < popped; j++) prefix += 'A';
			}
			
		}
		
		return arr;
	}
	
	public JTable getTable(){
		return table;
	}
	
	/*----- Setters -----*/
	
	public void setTable(Object[] cols, Object[][] dat){
		DefaultTableModel mdl = new DefaultTableModel(dat, cols);
		table.setModel(mdl);
		
		repaint();
	}
	
	/*----- UI Management -----*/
	
	/*----- Clean up -----*/

	public void dispose() {
		//table.setModel(null);
	}

}
