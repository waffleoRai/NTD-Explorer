package waffleoRai_NTDExCore;

public enum EncryptionType {
	
	DSi_MODCRYPT("Modcrypt"),
	WII_AES("AES-128");
	
	private String n;
	
	private EncryptionType(String name)
	{
		n = name;
	}
	
	public String toString()
	{
		return n;
	}

}
