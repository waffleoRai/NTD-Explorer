package waffleoRai_NTDExCore;

import java.util.HashMap;
import java.util.Map;

public enum Console {
	
	UNKNOWN(-1, "UNK"),
	
	PS1(-2, "PSX"),
	
	NES(1, ""),
	SNES(2, ""),
	GB(3, "DMG"),
	GBC(4, "CGB"),
	N64(5, "NUS"),
	GBA(6, "AGB"),
	
	GAMECUBE(7, "DOL"),
	DS(8, "NTR"),
	WII(9, "RVL"),
	DSi(10, "TWL"),
	_3DS(11, "CTR"),
	NEW_3DS(12, ""),
	WIIU(13, "WUP"),
	SWITCH(14, "HAC");
	
	private int int_val;
	private String short_code;
	
	private Console(int ival, String code3)
	{
		int_val = ival;
		short_code = code3;
	}
	
	public int getIntValue()
	{
		return int_val;
	}
	
	public String getShortCode()
	{
		return short_code;
	}
	
	//--------------------------
	
	private static Map<Integer, Console> imap;
	
	public static Console getConsoleFromIntCode(int icode)
	{
		if(imap == null)
		{
			imap = new HashMap<Integer, Console>();
			Console[] vals = Console.values();
			for(Console c : vals) imap.put(c.getIntValue(), c);
		}
		
		return imap.get(icode);
	}
	
}
