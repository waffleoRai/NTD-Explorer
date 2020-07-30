package waffleoRai_NTDExCore.multilang;

public enum NTDLanguage {
	
	ENGLISH("english", "eng"),
	;

	private String display_name;
	private String code3;
	
	private NTDLanguage(String name, String code){
		display_name = name;
		code3 = code;
	}
	
	public String toString(){return display_name;}
	public String getDisplayName(){return display_name;}
	public String getCode(){return code3;}
	
	public static NTDLanguage getFromCode(String code){
		for(NTDLanguage l : NTDLanguage.values()){
			if(l.code3.equalsIgnoreCase(code)) return l;
		}
		return null;
	}
	
}
