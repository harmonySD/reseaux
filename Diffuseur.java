import java.net.*;
import java.util.*;
import java.io.*;
import java.net.InetAddress;


/**
 * classe du diffuseur 
 * **/
public class Diffuseur{

	@SuppressWarnings("unused")
	private final String id ; // nom du diffuseur de la même taille que Message.IDSZ
	private  int rcvPrt; // port de réception de connexion en TCP, généralement pour le client
	private ServerSocket rcvSock; // socket de réception TCP 
	private InetSocketAddress mltcstSA; // Adresse de diffusion Multicast du socket
	private  long frqcy= 1000; // fréquence d'envoi entres deux messages en millisecondes 
	private Holder msgHolder  ; // Holder qui contient tous les messages et l'historique du diffuseur
	private  Thread broadcastThread ; //Thread de la boucle d'émission
	private  boolean broadcastThreadIsWaiting = false; // Variable qui permet de savoir si la boucle d'émission est en pause 
	//en attente de nouveaux meessages
	// le SEUL modifieur de cette variable est broadCastLoop dans le broadcastThread
	private  Thread receiveThread; // Thread de la boucle de réception 
	public final boolean startBroadcast(){this.broadcastThread.start(); return true;}
	public final boolean startListen(){this.receiveThread.start(); return true;}
	public static final int MAXHISTORY = 999; // nombre maximal de messages que quelqu'un peut demander comme historique
	public final boolean localMode; // booléen qui permet de savoir si le diffuseur doit s'enregistrer 
	// auprès de gestionnaire avec l'adresse de la machine (false) ou 127.0.0.0.1 (true)
	
	public Diffuseur(String MultiCasterID,String localMde, int recvPort,  int multiCastPort, String multiCastAddress )
	throws SocketException,UnknownHostException,IOException {
		/**Construit un diffuseur et lance la diffusion et l'écoute sur les ports indiqués 
		 * la boucle d'émission se met immédiatement en attente car elle n'a rien à diffuser.**/
		if(recvPort <0 || multiCastPort <0  || recvPort > 9999|| multiCastPort > 9999 ){ // 9999 car on n'admet que les ports jusqu'à 4 chiffres
			throw new IllegalArgumentException("Port  incorrect");
		}
		if (MultiCasterID.length() != Message.IDSZ ){ // s'assure qu'un Diffuseur aie un pseudo de même longueur qu'un client
			if (MultiCasterID.length() > Message.IDSZ){this.id = MultiCasterID.substring(0,Message.IDSZ);}// cas trop long
			else{this.id = MultiCasterID+("#".repeat( Message.IDSZ - MultiCasterID.length() ));} // cas trop court 
		}else {this.id=MultiCasterID;}// exactement 8 caractères
		this.localMode = Boolean.valueOf​(localMde);

		this.rcvPrt = recvPort ;
		this.mltcstSA = new InetSocketAddress(multiCastAddress,multiCastPort);
		this.msgHolder = new Holder(); // Stockeur de messages 
		
		this.broadcastThread =new Thread( ()->{this.broadcastLoop();} );
		this.receiveThread = new Thread( ()->{this.receiveLoop();} );
		
		this.rcvPrt = recvPort;
		this.rcvSock = new ServerSocket(this.rcvPrt);
		this.rcvSock.setReuseAddress(true);
		
		
		this.startListen();
		this.startBroadcast();
	}
	
	 private void  broadcastLoop (){
		 /**Boucle d'émission en multicast, une par Diffuseur.**/
		DatagramSocket thesender; // Socket pour envoyer les paquets
		DatagramPacket packtosend; // tampon pour les données à envoyer
		try{
			thesender = new DatagramSocket();
			packtosend = new DatagramPacket(
				new byte [Prefixes.DIFF.normalMessLength],
				Prefixes.DIFF.normalMessLength,
				this.mltcstSA
			);   
		}catch(Exception e ){return;}
		try(thesender;){ // pour attrapper demande d'interruption
			while(true){
				try{
					String tosend;
					synchronized(this.msgHolder){// on récupère un message à envoyer du Stockeur
						tosend=Prefixes.DIFF.toString()+" "+this.msgHolder.next()+"\r\n";
					}
					packtosend.setData(tosend.getBytes());
					if (packtosend.getLength() != Prefixes.DIFF.normalMessLength ){
						// une sécurité pour s'assurrer de ne pas émettre de messages de taille incorrecte
						// sur le réseau,on refuse d'envoyer le message
						// problème le message sera quand même ajouté à l'historique à cause de next
						System.err.println("/!\\ Attention le message suivant de taille incorrecte à failli être envoyé"
							+"\nle message :\""+new String(packtosend.getData())
							+"\"\n Longueur attendue " 
							+Prefixes.DIFF.normalMessLength
							+", longueur obtenue :"+packtosend.getLength()+"\n"
						);
						continue;
					}
					thesender.send(packtosend); // envoi du paquet qui est de taille correcte
				}catch(NoSuchElementException ns){ // historique vide, on passe en sommeil
						this.broadcastThreadIsWaiting = true;
						System.out.println("Plus de message à diffuser pour le moment, mise en sommeil de l'émission.");
						synchronized(this.msgHolder){
							msgHolder.wait(); // attente de notification de msgHolder qui signalera que 
						}this.broadcastThreadIsWaiting = false;
						System.out.println("Des messages sont disponibles à la diffusion, reprise de l'émission.");
						continue;
				}catch (IOException ioex){
					System.err.println(ioex.toString());
				}
				Thread.sleep(this.getFrequency()); // délai entre deux envoi de messages
			}
		} catch(InterruptedException end){
			// code particulier  en cas de demande d'interruption du thread 
		}
	}
	
	private void sendMess(Socket so)throws IOException{
		/** fonction qui traite dans la boucle de réception TCP des demandes d'ajout de message
		 * via MESS ou MALL par un client / gestionnaire  rompt la connexion à la fin avec qu'elle se soit ou non bien passée**/
		try(
				so;// pour ne pas avoir à faire close soit même
				//envoie
				PrintWriter out = new PrintWriter(so.getOutputStream());
				//recoit
				BufferedReader in = new BufferedReader(new InputStreamReader(so.getInputStream()));
		)
		{
			char [] buffer =  new char[Prefixes.MESS.normalMessLength - Prefixes.headerSZ];
			//on recoit 
			int  nbrecu= in.read(buffer);
			if(nbrecu != Prefixes.MESS.normalMessLength - Prefixes.headerSZ || in.ready() ||buffer[0]!=' ' || buffer[ 9]!=' ' ) {
				// si la taille ne correspond pas, trop court ou trop long  ou pas d'espaces au bon endroit
				System.err.println("Une erreur est survenue avec "+so.getLocalSocketAddress().toString()+"le contenu était de taille incorrecte;\n"
						+"Ou mal formé\n"
						+"taille attendue: "+(Prefixes.MESS.normalMessLength - Prefixes.headerSZ )
						+"taille obtenue: "+Integer.valueOf(nbrecu)
						+"\n il restait encore des choses à lire: "+Boolean.valueOf(in.ready())
						+"\n Le contenu: '"+new String(buffer)+"'\n"
						);

				return;
			}
			String recu = new String(buffer);
			String id=recu.substring(1,9);
			String mess=recu.substring(9, 150);
			this.addAMessage(new Message(id, mess));
			out.print(Prefixes.ACKM +"\r\n");
			out.flush();
		}finally {}
	}

	private void diffAlive(Socket so)throws IOException{ 
		/**Cette fonction permet à un gestionnaire de savoir si le diffuseur est en ligne,
		 * répond IMOK si le message est bien formé et broadcastThreadIsWaiting à Faux, sinon rompt la connexion.**/
		PrintWriter out = new PrintWriter(so.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(so.getInputStream()));
		char [] charbuf = new char [2];
		in.read(charbuf);
		if(Character.compare(charbuf[0],'\r')!=0 || Character.compare(charbuf[1],'\n')!=0 || in.ready()){
			System.err.println("Une erreur est survenue avec "+so.getLocalSocketAddress().toString()
				+"l'en-tête était de taille incorrecte et/pi étrange\n"
				+"\ntaille attendue: "+Integer.valueOf(Prefixes.RUOK.normalMessLength -Prefixes.headerSZ )
				+"\n il restait encore des choses à lire: "+Boolean.valueOf(in.ready()));
			so.close(); // ferme aussi les buffer
			throw new IOException("Le gestionnaire "+so.getInetAddress()+"a mal commniqué avec le diffuseur lors d'un RUOK ");
		}
		synchronized(this){if(broadcastThreadIsWaiting){
			System.out.println("Le gestionnaire "+so.getInetAddress()+" voulais savoir si le diffuseur était entrain de diffuser mais ce n'était pas le cas,"
			+"en conséquence la connexion va être fermée avec le gestionnaire.");
			so.close(); // ferme aussi les buffer 
			throw new IOException("La connexion a été fermée avec un gestionnaire pour cause de RUOK alors qu'on ne diffusait pas.");
			}
		}
		out.print(Prefixes.IMOK+"\r\n");
		out.flush();
	}

	private void receiveLoop(){ 
		/**Cette fonction écoute les connexion en TCP sur le port rcvPrt et les fournit à un aiguilleur à chaque nouvelle arrivée.**/
		try{
			while(!Thread.interrupted()){ // tant qu'on ne lui a pas dit de s'arrêter d'écouter
				Socket connectedSocket = rcvSock.accept();
				System.out.println("Quelqu'un s'est connecté en TCP au diffuseur :"+connectedSocket.toString());
				Thread t =new Thread(()->{this.receiveLoopSwitchMan(connectedSocket);});
				t.setDaemon(true); // pour faciliter l'arrêt de l'exécution du serveur.
				t.start();
			}
		}catch(Exception e){
			System.err.println("ATTENTION LE DIFFUSEUR ARRETE D'ECOUTER POUR DES CONNEXIONS TCP \n"
			+" A CAUSE DE l'EXCEPTION  SUIVANTE : "+e.toString());}
	}
	
	private void receiveLoopSwitchMan(Socket connSock) {
		/** Cette fonction identifie l'entête envoyé par des clients et appelle la bonne fonction en conséquence
		 *  ou rompt la connexion si l'en-tête n'a pas été reconnu. 
		 * le Socket sera fermé à la fin de l'exécution de cette fonction quoi qu'il arrive**/
		try{
			byte[] gestHeader = new byte[Prefixes.headerSZ];
			if(Prefixes.headerSZ != connSock.getInputStream().read(gestHeader)){
				connSock.close();
				System.out.println("L'entité connecté "+connSock.toString()+" a envoyé un entête trop court,'"+new String(gestHeader)
				+ "' fermeture.");
				return;
			}
			String headerCont = new String(gestHeader);
			if( headerCont.equals(Prefixes.LAST.toString())){
				try{
					System.out.println("L'entité connecté "+connSock.toString()+"souhaite récupérer des messages");
					this.historygiver(connSock);
				}catch(Exception e){
					System.err.println(" Une erreur est apparue lors d'une demande d'historique: "+e.toString());
				}
			}else if(headerCont.equals(Prefixes.MESS.toString()) || headerCont.equals(Prefixes.MALL.toString()) ){
				try{
					System.out.println("L'entité connecté "+connSock.toString()+"souhaite fournir un message à diffuser");
					this.sendMess(connSock);
				}catch(Exception e){
					System.err.println(" Une erreur est apparue lors d'un ajout de message d'un client "+"\n"+e.toString());
				}
			}else{connSock.close();
				System.err.println("Un message avec un en-tête non reconnu a été envoyé, la connexion a été rompue \n l'en-tête :"+headerCont);
			}//rejet de la connexion
		}catch(IOException e){System.err.println(" Une erreur E/S est apparue lors de l'aiguillage "+e.toString());}
		finally{
			try{connSock.close();}catch(IOException e){};
		}
	}
	
private  void historygiver(Socket commSock){
	/**Cette fonction est appelée par l'aiguilleur si un client souhaite obtenir un historique de message
	 * le socket sera fermée à la fin de cette fonction quoi qu'il arrive.
	 * **/
		if(null==commSock){throw new NullPointerException();}
		if(commSock.isClosed()){return;}
		try(
		BufferedReader bfred=new BufferedReader(new InputStreamReader(commSock.getInputStream()));
		commSock; // pour ne pas avoir à fermer le socket nous même
		){
			char[] buffer = new char[Prefixes.LAST.normalMessLength - Prefixes.headerSZ];
			bfred.read(buffer);
			String mssgContent = new String(buffer);
			if(Prefixes.LAST.normalMessLength -Prefixes.headerSZ  != mssgContent.length()
					|| bfred.ready() || buffer[0]!=' '){
				System.err.println("Une erreur est survenue avec "+commSock.getLocalSocketAddress().toString()
						+"le contenu après LAST était de taille incorrecte ou mal formé;\n"
						+"taille attendue: "+Integer.valueOf(Prefixes.LAST.normalMessLength -Prefixes.headerSZ )
						+"taille obtenue: "+Integer.valueOf(mssgContent.length())
						+"\n il restait encore des choses à lire: "+Boolean.valueOf(bfred.ready())
						+"le contenu : '"+ new String(buffer)+"'"
						);
				commSock.close();
				return;
			}
			int howmanyasked;
			try {
				howmanyasked = Integer.valueOf(new String(mssgContent).trim());
			}catch(NumberFormatException ne){ commSock.close(); return;}
			String tosend [] ;
			if(howmanyasked<0) {commSock.close(); return;}
			synchronized(this.msgHolder){
				tosend = this.msgHolder.retrieveHistory(howmanyasked%Diffuseur.MAXHISTORY);
			}
			if (tosend.length == 0){ // si le client veut récupérer un historique de taille 0 ...
				commSock.getOutputStream().write((Prefixes.ENDM.toString()+"\r\n").getBytes());
				commSock.close();
				return;
				}
				OutputStream wheretosend = commSock.getOutputStream();
 			for (String mess : tosend){ //On renvoit tout ce  que le stockeur nous a fourni, inférieur ou égal à ce que le client à demandé
				String finalpack= Prefixes.OLDM.toString()+" "+mess+"\r\n";
				if(finalpack.getBytes().length != Prefixes.OLDM.normalMessLength){
					// sécurité pour éviter d'envoyer des messages d'historiques de taille incorrecte.
					System.err.println("/!\\ Attention le message suivant de taille incorrecte à failli être fourni comme historique à"
						+commSock.toString()
						+"\nle message :\""+finalpack
						+"\"\n Longueur attendue " 
						+Prefixes.OLDM.normalMessLength
						+", longueur obtenue :"+finalpack.getBytes().length+"\n"
					);
					continue;
				}
				wheretosend.write(finalpack.getBytes());	// envoi d'un message de l'historique 
			}//fermeture boucle for
			wheretosend.write((Prefixes.ENDM.toString()+"\r\n").getBytes());
			System.out.println("un historique a été fourni avec succès à l'entité "+commSock.toString());
			return;
		}catch(IOException ioe){
			System.err.println("problème d'envoi d'historique IOE, arrêt");
			ioe.printStackTrace(System.err);return ;}
	}
	
	public void stopServer(){ // pour fermer le serveur.
			this.broadcastThread.interrupt();
			this.receiveThread.interrupt();
		
		}
	public void addAMessage(Message m){// pour ajouter un message.
		synchronized(this.msgHolder){
			if (m== null){return;}
			synchronized (this.msgHolder){
			this.msgHolder.add(m);}
			if (this.broadcastThreadIsWaiting){
				this.msgHolder.notify();
			}
			System.out.println("le message "+ m.toString() +"a été ajouté avec succès.");
			return;
		}
	}
	public void addGestionnaireLink(InetAddress toRegister ,int port){
		/** Cette fonction permet d'ouvrir une ligne de communication entre un gestionnaire et le Diffuseur
		 * essentiellement pour l'envoi de IMOK  **/
			Thread t=new Thread(()->{toGestionnaire(toRegister,port);}); 
			t.setDaemon(true); 
			t.start();
		return;
		}
	
	public void toGestionnaire( InetAddress toRegister ,int port ){
		/** Cette fonction gère l'enregistrement à un gestionnaire donné puis la communication avec lui (RUOK IMOK)**/
		try(
			Socket sock = new Socket (toRegister,port); // pour ne pas avoir à fermer le socket nous même
			OutputStream sendStream = sock.getOutputStream();
			InputStream receiveStream =sock.getInputStream();	
		){
			// au début on s'enregistre auprès du gestionnaire avec un regi 
			System.out.println("Connexion établie avec un gestionnaire :"+sock.getInetAddress().toString()+" tentative d'enregistrement...");
			String tosend = Prefixes.REGI.toString()
				+" "
				+this.id
				+" "
				+Diffuseur.Address4Filler(this.getMulticastAddress().getHostAddress())
				+" "
				+String.format("%04d",this.getMulticastPort())
				+" "
				// ici est fait usage du booléen localMode pour savoir si on doit envoyer une adresse locale comme adresse du serveur ou la vraie
				// adresse de la machien
				+(this.localMode == true ? "127.000.000.001":Diffuseur.Address4Filler(InetAddress.getLocalHost().getHostAddress()))
				+" "
				+String.format("%04d",this.getReceivePort())
				+"\r\n"; //REGI␣id␣ip1␣port1␣ip2␣port2  REGI lediffo# 127.000.001.001 6663 225.010.020.030 6664
				System.out.flush();
			if(tosend.getBytes().length != Prefixes.REGI.normalMessLength ){
				System.err.println("Tentative d'envoi d'un message d'enregistrement erroné à "
					+sock.getInetAddress().toString()
					+"\nle message :'"+tosend
					+"\nlongueur attendue :"
					+Prefixes.REGI.normalMessLength
					+"longueur obtenue :"
					+tosend.getBytes().length 
					+"' \nfermeture de la connexion.");
				return;
			}else{
				sendStream.write(tosend.getBytes());
				sendStream.flush();// envoi  de la demande d'enregistrement
			}
			byte[] checkreok =  sock.getInputStream().readNBytes​(Prefixes.headerSZ +2);
			if(checkreok.length != Prefixes.headerSZ+2 || !new String(checkreok).equals(Prefixes.REOK.toString()+"\r\n")){
				// si le gestionnaire ne renvoit pas REOK \r\n
				System.err.println("Le  Gestionnaire "+ sock.getInetAddress().toString() +" n'a pas confirmé l'enregistrement avec REOK," 
				+"\nil a renvoyé à la place '"+new String(checkreok)
				+"' Deconnexion du gestionnaire.");
				return;
			}
			System.out.println("enregistrement au gestionnaire "+sock.toString()+" réussi !");
			while (true){ // boucle infinie pour communication avec le gestionnaire
				byte[] gestHeader = new byte[Prefixes.headerSZ];
				if(Prefixes.headerSZ != sock.getInputStream().read(gestHeader)){
					System.err.println("Le  Gestionnaire"+ sock.getInetAddress().toString() +"a envoyé un entête trop court. Déconnexion.");
					return;
				}
				String headerCont = new String(gestHeader);
				 if (headerCont.equals(Prefixes.RUOK.toString())){
					try{
						System.out.println("Le Gestionnaire "+sock.toString()+"souhaite savoir si le diffuseur est en ligne");
						this.diffAlive(sock);
					}catch(Exception e){
						System.err.println(" Une erreur est apparue lors d'une Vérification RUOK par un gestionnaire arret :"+e.toString());
						return;
					}
				}else{ // tout ce que peut normalement nous envoyer un gestionnaire sur cette ligne de communication
					// c'est uniquement RUOK
					System.err.println("Le  Gestionnaire"+ sock.getInetAddress().toString() +"a envoyé un entête inconnu.\n" 
						+" l'entête : '"
						+headerCont
						+"' Déconnexion.");
					return;
				}
			}
		}catch(UnknownHostException e){ System.err.println("Exception 'Hôte Inconnu' lors de l'enregistrement à un gestionnaire, arrêt");
		}catch(IOException e){System.err.println("Exception IOE lors de l'enregistrement à un gestionnaire, déconnexion du gestionnaire.");
		}catch(Exception e){System.err.println("Exception '"+e.toString()+ "' lors de la communication avec un gestionnaire, arrêt");
		}	
	}
	
	public synchronized int getMulticastPort(){return this.mltcstSA.getPort();} // accesseur port multicast
	public synchronized InetAddress getMulticastAddress(){return this.mltcstSA.getAddress();} // accesseur adresse multicast
	public synchronized int getReceivePort(){return this.rcvPrt;}// accesseur port de réception TCP
	public synchronized long getFrequency(){return this.frqcy;} // accesseur délai minimal entre deux messages
	public synchronized void setFrequency(long newFreq){this.frqcy = newFreq;}// modifieur  délai minimal entre deux messages
	public static String Address4Filler(String address ){ // cette fonction permet d'avoir 3 chiffres entre chaque point d'une ipv4
		if(address == null){
			return "000.000.000.000";}
		int point1;int point2;int point3;
		point1=address.indexOf​( '.',  0);
		point2=address.indexOf​( '.',  point1+1);
		point3=address.indexOf​( '.',  point2+1);
		return 
		String.format("%03d",Integer.valueOf(address.substring(0,point1)))
		+"."+String.format("%03d",Integer.valueOf(address.substring(point1+1,point2)))
		+"."+String.format("%03d",Integer.valueOf(address.substring(point2+1,point3)))
		+"."+String.format("%03d",Integer.valueOf(address.substring(point3+1,address.length())));
	}

	public static String getLocalAddress() throws UnknownHostException{return InetAddress.getLocalHost().toString();}
	synchronized Holder getHolder(){/** à utiliser uniquement à des fins de débogage !  package private**/return this.msgHolder;}

	public static void main (String [] args)throws Exception {
		//public Diffuseur(String MultiCasterID,String localMde, int recvPort,  int multiCastPort, String multiCastAddress )
		Diffuseur lediff = new Diffuseur(args[0], args[1],Integer.valueOf(args[2]), Integer.valueOf(args[3]),args[4]);
		try(Scanner sc = new Scanner(System.in);){
			String temp;
			for(int i =0;i<220;i++){
				temp = sc.nextLine();
				lediff.addAMessage(new Message(args[0],temp));	
			}
		}catch(NoSuchElementException ns){return;}
		//synchronized(lediff.msgHolder) {
			//lediff.msgHolder.notify(); //pour signaler en premier lieu au gestionnaire qu'il doit se réveiller
			//}
			lediff.addGestionnaireLink(InetAddress.getByName(args[5]),Integer.valueOf(args[6])); //ici l'ajout d'un gestionnaire
		try{
		Object lock= new Object();
		synchronized(lock){
			lock.wait(10000);
			lock.notify();
		}
		}catch(Exception e){}
	}
}

