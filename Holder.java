import java.util.concurrent;

public class Holder implements Iterator<Message> {
	// FIXME penser à modifier la docu ! 
	public Holder(){};
	public static final int HOLDSZ = 999;
	private ConcurrentLinkedDeque<Message> toSendQueue= new ConcurrentLinkedDeque();
	int toSendOccupation=0;
	int nextMessageNumber=0;
	private ConcurrentLinkedDeque<Message> HistoryQueue= new ConcurrentLinkedDeque();
	int historyOccupation =0;
	
	
	public void add(Message toadd) throws NullPointerException{
		if (toadd == null){throw new NullPointerException();}
		if (tosendOccupation< HOLDSZ ){
			this.toSendQueue.add(toadd);
			toSendOccupation++;
		}else{
			this.toSendQueue.removeLast();
			this.toSendQueue.add(toadd);
			}
	}
	@Override
	public synchronized boolean hasNext() { 
		return toSendOccupation <= 0 ? false:true;
	}
		
	@Override
	public  synchronized String next() throws NoSuchElementException {
		Message temp = toSendQueue.removeLast(); // lance NSEException si queue vide
		this.toSendOccupation --;
		String toret = this.nextMessageNumber.+temp.toString();
		this.nextMessageNumber=(this.nextMessage  +1 )% HOLDSZ;
		
		//si occupation maximale est atteinte on enlève le plus ancien élément et on en ajoute un nouveau
		if(this.historyOccupation>=HOLDSZ){this.History.removeLast();this.HistoryQueue.add(temp);}
		else{this.History.add(temp); this.historyOccupation++;}
		
		return toret;
	}
		
	 public synchronized String[] retrieveHistory(int howmany){
		
	};
}
