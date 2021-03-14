public class Holder implements Iterator<Message> {
	
	public Holder(){};
	
	private boolean lastAmended = false;
	unsigned int memorypos =0;
	unsigned int nextpos=0;
	unsigned int currentsavedoccupation=0;
	unsigned int currentsavedhistory=0;
	unsigned int nexthistorypos=0;
	private Message mssgholder [] = new Message[999];
	private Message history[] = new Message[999];
	
	
	@Override
	boolean 	hasNext() {currentsavedoccupation >0 ? true : false;}
	@Override
	Message next(){
		//FIXME
		// renvoyer un message ? une copie du message ? la version toString ?
		Message toret =this.mssgholder[this.nextpos];
		this.nextpos = (this.nextpos+1)%this.memorypos; // préparation pour le prochain next
		this.lastAmended = false;// on peut à nouveau amender si l'on souhaite
		return toret;}
	 Message[] retrieveHistory{//FIXME a faire};
	}
