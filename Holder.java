
import java.util.concurrent.*;
import java.util.Iterator;
import java.util.*;
public class Holder implements Iterator<String> {


	public static final int HOLDSZ = 9999; // nb maximal de messages que contient notre holder
	//si un nouveau arrive, on écrase le plus ancien non envoyé
	private ConcurrentLinkedQueue<Message> toSendQueue= new ConcurrentLinkedQueue<Message>();
	private int toSendOccupation=0; // indique le nombre actuel de messages en attente d'envoi
	private int nextMessageNumber=0;//indique le numéro (Modulo HOLDSZ) qu'aura le prochain message 
	//utile pour l'envoi et pour le stockage dans l'historique ainsi que l'accès depuis l'historique
	private Message [] HistoryQueue= new Message[HOLDSZ];// historique des derniers messages récupérés via next 
	private int historyOccupation =0;// taille actuelle de l'historique, vas jusqu'à HOLDSZ puis chaque nouveau message écrase le plus ancien,
	// grâce à l'utilisation de nextMessageNumber
	
	public Holder(){}// constructeur
	
	public synchronized void add(Message toadd) throws NullPointerException{
		/**Ajoute un Message à la liste des messages à envoyer
		 * lance NullPointerException si l'argument est null  **/
		if (toadd == null){throw new NullPointerException();}
		if (toSendOccupation< HOLDSZ ){ // si la queue n'est pas pleine
			this.toSendQueue.add(toadd);
			toSendOccupation++; // on augmente le compteur
		}else{ // si la queue est pleine on supprime le plus ancien non envoyé
			this.toSendQueue.remove();
			this.toSendQueue.add(toadd);
		}// pas d'augmentation compteur, liste déjà pleine !
	}
	@Override
	public synchronized boolean hasNext() { 
		return toSendOccupation < 1 ? false:true;
	}
	@Override
	public  synchronized String next() throws NoSuchElementException { // renvoit le prochain message à envoyer
		Message temp = toSendQueue.remove(); // lance NSEException si queue vide
		this.toSendOccupation --; //maj occupation file à envoyer
		String toret =String.format("%04d",this.nextMessageNumber)+" "+temp.toString();// ajout du numéro du message pour envoi
		if (this.historyOccupation< HOLDSZ){this.historyOccupation++;}
		this.HistoryQueue[this.nextMessageNumber]=temp;
		this.nextMessageNumber=(this.nextMessageNumber  +1 )%HOLDSZ;
		return toret;// la taille de toret doit être 
	}
		/*
	public synchronized String[] retrieveHistory(int howmany){ // /!\ renvoit un tableau de String avec un null si historique vide !
		// fonction de récupération de l'historique des messages, du plus récent à la position 0 au plus ancien  
		String [] toret;
		if (howmany> this.historyOccupation){//vérifier la condition et remplacer si nécessaire pour avoir renvoit correct si historique plein ou non
			//tableau n'est pas plein et/ou trop de mssages demandés
			toret = new String[this.historyOccupation];
			for (int i = this.historyOccupation-1;i>-1;i--){
				toret[this.historyOccupation-1-i]=Integer.toString(i)+" "+this.HistoryQueue[i].toString();
				//les messages sont rangés dans toret du plus récent au plus ancien
			}
		}
		else if (howmany<=this.nextMessageNumber-1){
			// si pas trop de messages demandés ET on peut
			//juste remonter le tableau de l'historique bêtement sans avoir à faire de saut
		toret = new String[howmany];
			for (int i = howmany-1;i>-1;i--){
				toret[howmany-1-i]=Integer.toString((this.nextMessageNumber-1-i))+" "+this.HistoryQueue[this.nextMessageNumber-1-i].toString();
			}
		}else{// howmany> this.nextMessagenumber-1
		toret = new String[howmany];
			for (int i = howmany-1;i>-1;i--){
				toret[howmany-1-i]=Integer.toString(this.nextMessageNumber-1-i%HOLDSZ)+" "+this.HistoryQueue[this.nextMessageNumber-1-i%HOLDSZ].toString();
			}
		}
			return toret;
	}*/
	
	
	public synchronized String[] retrieveHistory(int howmany){
		if(0>=howmany){return new String[0]; }
	    String [] toret;
	    if((!( howmany> this.nextMessageNumber )) || Holder.HOLDSZ > this.historyOccupation){
			//  nb messages demandés < indice || trops de messages demandés : pas à /impossible boucler en fin de tableau			
			toret = new String[howmany];
	        for (int i = howmany-1;i>-1;i--){
				toret[howmany-1-i]=Integer.toString((this.nextMessageNumber-1-i))+" "+this.HistoryQueue[this.nextMessageNumber-1-i].toString();
			}
	    }else{
			// il faut reboucler sur la fin du tableau 
			int numtoget = howmany > Holder.HOLDSZ ? HOLDSZ:howmany ;
			toret = new String[numtoget];
			for (int i = numtoget;i>0;i--){
				toret[i-numtoget]=Integer.toString((this.nextMessageNumber-1-i)%HOLDSZ)
				+" "
				+this.HistoryQueue[this.nextMessageNumber-1-i%HOLDSZ].toString();
			}
		}
	 return toret;   
	}
}


