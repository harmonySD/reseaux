import java.util.concurrent;

public class Holder implements Iterator<Message> {
	// FIXME penser à modifier la docu ! 
	public Holder(){};
	public static final int HOLDSZ = 999;
	private ConcurrentLinkedDeque<Message> toSendQueue= new ConcurrentLinkedDeque();
	int toSendOccupation=0;
	int nextMessageNumber=0;
	private Message [] HistoryQueue= new Message[HOLDSZ];
	int historyOccupation =0;
	
	public synchronized void add(Message toadd) throws NullPointerException{
		/**Ajoute un Message à la liste des messages à envoyer
		 * lance NullPointerException si l'argument est null  **/
		if (toadd == null){throw new NullPointerException();}
		if (tosendOccupation< HOLDSZ ){ // si la queue n'est pas pleine
			this.toSendQueue.add(toadd);
			toSendOccupation++; // on augmente le compteur
		}else{ // si la queue est pleine on supprimme le plus ancien non envoyé
			this.toSendQueue.removeLast();
			this.toSendQueue.add(toadd);
		}// pas d'augmentation compteur, liste déjà pleine !
	}
	
	@Override
	public synchronized boolean hasNext() { 
		return toSendOccupation < 1 ? false:true;
	}
		
	@Override
	public  synchronized String next() throws NoSuchElementException { // renvoit le prochain message à envoyer
		Message temp = toSendQueue.removeLast(); // lance NSEException si queue vide
		this.toSendOccupation --; //maj occupation file à envoyer
		String toret = this.nextMessageNumber.toString()+" "+temp.toString();// ajout du numéro du message pour envoi
		if (this.historyOccupation< HOLDSZ){this.historyOccupation++;}
		this.HistoryQueue[this.nextMessageNumber]=temp;
		this.nextMessageNumber=(this.nextMessage  +1 )%HOLDSZ;
		return toret;
	}
		
	public synchronized String[] retrieveHistory(int howmany){
		String [] toret;
		if (howmany> this.historyOccupation){
			toret = new String[this.historyOccupation];
			for (int i = this.historyOccupation-1;i>-1;i--){
				toret[this.historyOccupation-i]=this.HistoryQueue[i];
			}
		}
		else if (howmany<=this.nextMessageNumber-1){
		toret = new String[howmany];
			for (int i = this.howmany-1;i>-1;i--){
				toret[i]=this.HistoryQueue[this.nextMessageNumber-i];
			}
		}
		else{// howmany> this.nextMessagenumber-1
		toret = new String[howmany];
			for (int i = this.historyOccupation;i>-1;i--){
				toret[i]=this.HistoryQueue[this.nextMessageNumber-i];
			}
		}

			return toret;
	}
}
