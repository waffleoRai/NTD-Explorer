package waffleoRai_NTDExCore;


public class TypeLoadTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//System.err.println(NTDTypeLoader.class.getResource(".."));
		try{
			NTDTypeLoader.registerTypes(null, true);
		}
		catch(Exception x){
			x.printStackTrace();
			System.exit(1);
		}
	}

}
