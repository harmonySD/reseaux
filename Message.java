public class Message {
	public static final idsz=8;
	public static final datasz=140;
	private final String id ;
	private final String data  ;

	@Override 
	public String toString(){
		return this.id+" "+this.data;
	}
	public String getId(){return this.id;}
	public String getText(){return this.data;}
}
