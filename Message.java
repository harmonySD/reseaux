import java.text.Normalizer;
import java.text.Normalizer.Form;

public class Message {

	public static final int IDSZ=8; // taille fixe du nom d'utilisateur
	public static final int DATASZ=140; // taille fixe des données d'un message
	
	private final String id ;  // stocke le nom de l'utilisateur
	private final String data  ; // stocke son message 
	
	public Message(String useraw, String messageraw ){
		/**Ce constructeur accepte des chaînes trop longues ou trop courtes et les comblera / les  rabotera. 
		 * Il essaye d'enlever les éventuels accents que porterait le message pour qu'il fasse bien 140 octets de long.
		 * **/
		
		
		
		String userid = Normalizer.normalize(useraw, Form.NFD) .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");// CETTE LIGNE DE CODE A ETE RECUPEREE SUR INTERNET
		String message = Normalizer.normalize(messageraw, Form.NFD) .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");// CETTE LIGNE DE CODE A ETE RECUPEREE SUR INTERNET
		// ces deux  lignes ont ete recuperes sur https://www.drillio.com/en/2011/java-remove-accent-diacritic/
		
		// stockage id de l'émetteur du message
		if (userid.length() != Message.IDSZ ){
			if (userid.length() > Message.IDSZ){this.id = userid.substring(0,Message.IDSZ);}
			else{this.id = userid+("#".repeat( Message.IDSZ - userid.length() ));}
		}else {this.id=userid;}
		//stockage du message
		if (message.length() != Message.DATASZ ){
			if (message.length() > Message.DATASZ){this.data = message.substring(0,Message.DATASZ);}
			else{this.data = message+("#".repeat( Message.DATASZ - message.length() ));}
		}else{this.data=message;}
		
	}
	
	@Override 
	public String toString(){
		/**Cette fonction a été redéfinie pour permettre de récupérer un Message facilement au format requis.**/
		return this.id+" "+this.data;
	}
	public String getId(){return this.id;}
	public String getText(){return this.data;}
}

