public enum Prefixes {
	// liste uniformisée de tous les 
	// préfixe admissible sur le réseau
	 DIFF(161)
	,MESS(154)
	,ACKM(4)
	,LAST(8)
	,OLDM(160)
	,ENDM(1417) //FIXME quelle taille attendue ici ?
	,REGI(1417) // FIXME quelle taille attendue ici ?
	,REOK(4)
	,RENO(4)
	,RUOK(4)
	,IMOK(4)
	,LIST(4)
	,LINB(7)
	,ITEM(1417); // FIXME quelle taille attendue ici ?
	Prefixes(int normallyWaitedMessagelength){ this.normalMessLength =normallyWaitedMessagelength;}
	public static final int headerSZ = 4;
	public final int normalMessLength;
	@Override 
	public String toString(){ return this.name();}
}
