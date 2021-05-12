import java.text.Normalizer;
import java.text.Normalizer.Form;

public class Message {
	// tailles fixée des données / nom utilisateur
	public static final int IDSZ=8;
	public static final int DATASZ=140;
	
	private final String id ;
	private final String data  ;
	
	public Message(String useraw, String messageraw ){
		
		
		
		
		String userid = Normalizer.normalize(useraw, Form.NFD) .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");// cette ligne de code a été récupérée sur internet
		String message = Normalizer.normalize(messageraw, Form.NFD) .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");// cette ligne de code a été récupérée sur internet
		// ces lignes ont ete recuperes sur https://www.drillio.com/en/2011/java-remove-accent-diacritic/
		
		// stockage id de l'émetteur du message
		if (userid.length() != Message.IDSZ ){
			if (userid.length() > Message.IDSZ){this.id = userid.substring(0,Message.IDSZ);}
			else{this.id = userid+("#".repeat( Message.IDSZ - userid.length() ));}
		}else {this.id=userid;}
		//stockage du message
		if (message.length() != Message.DATASZ ){
			if (message.length() > Message.DATASZ){this.data = message.substring(0,Message.DATASZ);}
			else{this.data = message+("#".repeat( Message.DATASZ - message.length() ));}
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

