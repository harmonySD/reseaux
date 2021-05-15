public enum Prefixes {
	// liste uniformisée de tous les 
	// préfixe admissible sur le réseau
	 DIFF(161)
	,MESS(156)
	,MALL(156)
	,ACKM(6)
	,LAST(10)
	,OLDM(161)
	,ENDM(4) 
	,REGI(57)//longueur du REGI = REGI␣id␣ip1␣port1␣ip2␣port2\r\n
	,REOK(6)
	,RENO(6)
	,RUOK(6)
	,IMOK(6)
	,LIST(6)
	,LINB(9)
	,ITEM(1417); // FIXME quelle taille attendue ici ?
	Prefixes(int normallyWaitedMessagelength){ this.normalMessLength =normallyWaitedMessagelength;}
	public static final int headerSZ = 4;
	public final int normalMessLength;
	@Override 
	public String toString(){ return this.name();}
}

