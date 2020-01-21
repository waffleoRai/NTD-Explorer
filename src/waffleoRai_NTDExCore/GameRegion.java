package waffleoRai_NTDExCore;

import java.util.HashMap;
import java.util.Map;

public enum GameRegion {

	UNKNOWN(-1, "UNK"),
	
	USA(2, "USA"),
	USZ(0x7F, "USZ"),
	NOE(3, "NOE"),
	JPN(1, "JPN");
	
	private int s_val;
	private String code3;
	
	private GameRegion(int val, String code)
	{
		s_val = val;
		code3 = code;
	}
	
	public int getIntValue()
	{
		return s_val;
	}
	
	public String getShortCode()
	{
		return code3;
	}
	
	public String toString()
	{
		return code3;
	}

	//-----------------------------
	
	private static Map<Integer, GameRegion> imap;
	private static Map<String, GameRegion> cmap;
	
	public static GameRegion getRegion(int val)
	{
		if(imap == null)
		{
			imap = new HashMap<Integer, GameRegion>();
			for(GameRegion r : GameRegion.values()) imap.put(r.getIntValue(), r);
		}
		return imap.get(val);
	}
	
	public static GameRegion getRegion(String code3)
	{
		if(cmap == null)
		{
			cmap = new HashMap<String, GameRegion>();
			for(GameRegion r : GameRegion.values()) cmap.put(r.getShortCode(), r);
		}
		return cmap.get(code3);
	}
	
}
