package waffleoRai_NTDExGUI.icons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultTreeCellRenderer;

import waffleoRai_Files.tree.DirectoryNode;
import waffleoRai_Files.tree.FileNode;

public class TypeIconTreeRenderer extends DefaultTreeCellRenderer{

	private static final long serialVersionUID = 6176536740976257978L;
	
	private static final Font font = new Font("Tahoma", Font.PLAIN, 11);
	private static final Font font_bold = new Font("Tahoma", Font.BOLD, 11);
	private static final Color sel_color = new Color(198, 209, 218);
	private static final Color sel_border_color = new Color(136, 148, 158);
	
	private JLabel label;
	
	public TypeIconTreeRenderer(){
		label = new JLabel();
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		//this.setText("<html>" +value.toString()+"</html>");
		if(value instanceof FileNode){
			Icon i = TypeIcon.getIconForNode((FileNode)value);
			if(i != null){
				label.setIcon(i);
			}
			else{
				if(leaf) label.setIcon(this.getDefaultLeafIcon());
				else{
					if(expanded) label.setIcon(this.getDefaultOpenIcon());
					else label.setIcon(this.getDefaultClosedIcon());
				}
			}
			label.setToolTipText(((FileNode)value).getLocationString());
		}
		else{
			if(leaf) label.setIcon(this.getDefaultLeafIcon());
			else{
				if(expanded) label.setIcon(this.getDefaultOpenIcon());
				else label.setIcon(this.getDefaultClosedIcon());
			}
		}
		if(value instanceof DirectoryNode){label.setFont(font_bold);}
		else label.setFont(font);
		label.setText(value.toString() + " ");
		
		if(sel){
			label.setOpaque(true);
			label.setBackground(sel_color);
			label.setBorder(new LineBorder(sel_border_color));
			
		}
		else{
			label.setOpaque(false);
			label.setBackground(Color.white);
			label.setBorder(new LineBorder(Color.white));
		}

		return label;
	}

}
