public class Message {
	private final char [] id = new char [8];
	private final char[] data = new char [140];
	private unsigned int number;
	
	@Override 
	public String toString(){
		return this.number.toString() +" "+ this.id+" "+this.data;
	}
	
	public void incrementNumber(){this.number+=1;}
	public String getId(){return this.id;}
	public String getText(){return this.data;}
	public String getNumber(){return this.number;}
}
