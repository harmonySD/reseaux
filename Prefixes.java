public enum Prefixes {
	// liste uniformisée de tous les 
	// préfixe admissible sur le réseau
	// Le paramètre est la taille standard d'un message muni d'\r\n
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
	,ITEM(53); 
	Prefixes(int normallyWaitedMessagelength){ this.normalMessLength =normallyWaitedMessagelength;}
	public static final int headerSZ = 4; // taille d'un entête , toujours 4 octets /4 caractères
	public final int normalMessLength; 
	@Override 
	public String toString(){ return this.name();} // pour obtenir le format du mot clé à envoyer
}

