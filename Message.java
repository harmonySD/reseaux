public class Message {
	private final char [] id = new char [8];
	private final char[] data = new char [140];

	@Override 
	public String toString(){
		return this.id+" "+this.data;
	}
	public String getId(){return this.id;}
	public String getText(){return this.data;}
}
