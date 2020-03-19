package waffleoRai_NTDExCore;

import java.util.LinkedList;
import java.util.List;

import waffleoRai_Files.EncryptionDefinition;

public class EncryptionRegion {
	
	private EncryptionDefinition def;
	private long offset;
	private long size;
	
	private String decrypt_path;
	
	private List<byte[]> keydata;
	
	public EncryptionRegion()
	{
		def = null;
		offset = -1;
		size = 0;
		decrypt_path = null;
		keydata = new LinkedList<byte[]>();
	}
	
	public EncryptionRegion(EncryptionDefinition definition, long off, long len, String bufferPath)
	{
		def = definition;
		offset = off;
		size = len;
		decrypt_path = bufferPath;
		keydata = new LinkedList<byte[]>();
	}
	
	public EncryptionDefinition getDefintion(){return def;}
	public long getOffset(){return offset;}
	public long getSize(){return size;}
	public String getDecryptBufferPath(){return decrypt_path;}
	public List<byte[]> getKeyData(){return keydata;}
	
	public void setDefintion(EncryptionDefinition d){def = d;}
	public void setOffset(long off){offset = off;}
	public void setSize(long len){size = len;}
	public void setDecryptBufferPath(String path){decrypt_path = path;}
	public void addKeyData(byte[] data){keydata.add(data);}

	public long getApproximateSerializedSize(boolean includeVLS)
	{
		long sz = 4+8+8;
		if(includeVLS) sz += 3 + (decrypt_path.length() << 1);
		sz += 2;
		for(byte[] key : keydata) sz += 2 + key.length;
		
		return sz;
	}
	
	public boolean inRegion(long off, long len)
	{
		long edoff = off+len;
		long myed = offset + size;
		
		if(edoff <= offset) return false;
		if(off >= myed) return false;
		
		return true;
	}
	
}
