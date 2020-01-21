package waffleoRai_NTDExCore.importer;

import java.awt.Frame;
import java.io.IOException;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import waffleoRai_NTDExCore.Console;
import waffleoRai_NTDExCore.GameRegion;
import waffleoRai_NTDExCore.NTDProject;

public interface ParserOption {
	
	public void setOptionString(String op);
	public NTDProject generateProject(String path, GameRegion reg, Frame dialog_parent) throws IOException;
	
	public Console getConsole();
	public List<FileFilter> getExtFilters();
}
