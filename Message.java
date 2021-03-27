public class Message {
	// tailles fixée des données / nom utilisateur
	public static final int idsz=8;
	public static final int datasz=140;
	
	private final String id ;
	private final String data  ;
	
	public Message(String userid, String message ){
		// stockage id de l'émetteur du message
		if (userid.length() != Message.idsz ){
			if (userid.length() > Message.idsz){this.id = userid.substring(0,Message.idsz);}
			else{this.id = userid+"#".repeat( Message.idsz - userid.length() );}
			}else {this.id=userid;}
		//stockage du message
		if (message.length() != Message.datasz ){
			if (message.length() > Message.datasz){this.data = message.substring(0,Message.datasz);}
			else{this.data = message+"#".repeat( Message.datasz - message.length() );}
			}else {this.data=message;}
	}
	
	@Override 
	public String toString(){
		// modifiée pour faciliter la récupération des données pour l'envoi d'un message sur le réseau
		return this.id+" "+this.data;
	}
	public String getId(){return this.id;}
	public String getText(){return this.data;}
}
