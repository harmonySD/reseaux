public class Holder implements Iterator<Message> {
	// modifier la classe message pour stocker si un message a été envoyé ou pas ?
	// ou rajouter un tableau ici ? 
	// penser à modifier la docu ! 
	public Holder(){};
	public static final int HOLDSZ = 999;

	private int nextpos=0;// de quel indice dans le tableau sera récupéré le prochain message à envoyer
	private int memorypos=0; // 
	private boolean alreadySentBeyond=true; // si est à vrai, il n'y a plus rien à envoyer pour le moment
	private Message mssgholder [] = new Message[HOLDSZ];
	private Message msgghistory [] = new Message[HOLDSZ];
	@Override
	synchronized boolean 	hasNext() { return ! alreadySentBeyond;
		}
	@Override
	public  synchronized String next() throws NoSuchElementException {
		//FIXME
		// renvoyer le  toString du message après avoir ajouté son numéro
		if(alreadySentBeyond){throw new NoSuchElementException();}
			//récup message et adjonction chiffre compteur
		String toret = this.nextpos.toString()+" "+this.mssgholder[this.nextpos].toString();
		
		this.nextpos = (this.nextpos+1)%this.HOLDSZ; // préparation pour le prochain next
		if (this.nextpos == ths.memorypos){this.}
		return toret;}
	 public synchronized String[] retrieveHistory(int howmany){//FIXME a faire
		 };
	}
