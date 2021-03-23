public class Holder implements Iterator<Message> {
	
	public Holder(){};
	

	unsigned int memorypos =0;
	unsigned int nextpos=0;
	unsigned int currentsavedoccupation=0;
	private Message mssgholder [] = new Message[999];

	@Override
	boolean 	hasNext() {currentsavedoccupation >0 ? true : false;}
	@Override
	public String next() throws NoSuchElementException {
		//FIXME
		// renvoyer le  toString du message après avoir ajouté son numéro
		string toret = this.nextpos.toString()+" "+this.mssgholder[this.nextpos].toString();
		this.nextpos = (this.nextpos+1)%this.memorypos; // préparation pour le prochain next
		
		return toret;}
	 Message[] retrieveHistory(int howmany){//FIXME a faire
		 };
	}
