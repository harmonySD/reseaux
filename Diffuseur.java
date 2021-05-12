import java.net.*;
import java.util.*;
import java.io.*;
import java.net.InetAddress;


/**
 * classe du diffuseur 
 * **/
public class Diffuseur{

	@SuppressWarnings("unused")
	private final String id ;
	private  int rcvPrt;
	private ServerSocket rcvSock;
	private InetSocketAddress mltcstSA;
	private  long frqcy= 2500;
	private Holder msgHolder  ;
	private  Thread broadcastThread ;
	private  boolean broadcastThreadIsWaiting = false; // le SEUL modifieur est broadCastLoop
	private  Thread receiveThread;
	public final boolean startBroadcast(){this.broadcastThread.start(); return true;}
	public final boolean startListen(){this.receiveThread.start(); return true;}
	public static final int MAXHISTORY = 999;
	
	public Diffuseur(String MultiCasterID, int recvPort,  int multiCastPort, String multiCastAddress ) 
	throws SocketException,UnknownHostException,IOException {
		if(recvPort <0 || multiCastPort <0  || recvPort > 65535|| multiCastPort > 65535 ){
			throw new IllegalArgumentException("Port  incorrect");
		}
		this.id = MultiCasterID; // pas de sécurité max 8 caractères
		this.rcvPrt = recvPort ;
		this.mltcstSA = new InetSocketAddress(multiCastAddress,multiCastPort);
		this.msgHolder = new Holder();
		
		this.broadcastThread =new Thread( ()->{this.broadcastLoop();} );
		this.receiveThread = new Thread( ()->{this.receiveLoop();} );
		
		this.rcvPrt = recvPort;
		this.rcvSock = new ServerSocket(this.rcvPrt);
		this.rcvSock.setReuseAddress(true);
		
		
		this.startListen();
		this.startBroadcast();
	}
	
	 private void  broadcastLoop (){
		DatagramSocket thesender;
		DatagramPacket packtosend;
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
					synchronized(this.msgHolder){
					 tosend=Prefixes.DIFF.toString()+" "+this.msgHolder.next()+"\r\n";
					}
					packtosend.setData(tosend.getBytes());
					if (packtosend.getLength() != Prefixes.DIFF.normalMessLength ){
						System.err.println("/!\\ Attention le message suivant de taille incorrecte à failli être envoyé"
							+"\nle message :\""+new String(packtosend.getData())
							+"\"\n Longueur attendue " 
							+Prefixes.DIFF.normalMessLength
							+", longueur obtenue :"+packtosend.getLength()+"\n"
						);
						continue;
					}
					thesender.send(packtosend);
				}catch(NoSuchElementException ns){ // historique vide, on passe en sommeil
						this.broadcastThreadIsWaiting = true;
						System.out.println("Plus de message à diffuser pour le moment, mise en sommeil.");
						synchronized(this.msgHolder){
							msgHolder.wait();
						}this.broadcastThreadIsWaiting = false;
						System.out.println("Des messages sont disponibles à la diffusion, reprise.");
						continue;
				}catch (IOException ioex){
					System.err.println(ioex.toString());
				}
				Thread.sleep(this.getFrequency());
			}
		} catch(InterruptedException end){
			// code en cas de demande d'interruption du thread 
		}
	}
	
	private void sendMess(Socket so)throws IOException{

		try(//envoie
				PrintWriter out = new PrintWriter(so.getOutputStream());
				//recoit
				BufferedReader in = new BufferedReader(new InputStreamReader(so.getInputStream()));
		)
		{
			char [] buffer =  new char[Prefixes.MESS.normalMessLength - Prefixes.headerSZ];
			//on recoit 
			int  nbrecu= in.read(buffer);
			if(nbrecu != Prefixes.MESS.normalMessLength - Prefixes.headerSZ || in.ready() ) {
				System.err.println("Une erreur est survenue avec "+so.getLocalSocketAddress().toString()+"l'en-tête était de taille incorrecte;\n"
						+"taille attendue: "+(Prefixes.MESS.normalMessLength - Prefixes.headerSZ )
						+"taille obtenue: "+Integer.valueOf(nbrecu)
						+"\n il restait encore des choses à lire: "+Boolean.valueOf(in.ready()));
				so.close();
				return;
			}
			String recu = new String(buffer);
			String id=recu.substring(1,9);
			String mess=recu.substring(9, 150);
			this.addAMessage(new Message(id, mess));
			out.print(Prefixes.ACKM +"\r\n");
			out.flush();
			so.close();

		}finally {}
	}

	private void diffAlive(Socket so)throws IOException{
		PrintWriter out = new PrintWriter(so.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(so.getInputStream()));
		char [] charbuf = new char [2];
		in.read(charbuf);
		if(Character.compare(charbuf[0],'\r')!=0 || Character.compare(charbuf[1],'\n')!=0 || in.ready()){
			System.err.println("Une erreur est survenue avec "+so.getLocalSocketAddress().toString()
				+"l'en-tête était de taille incorrecte et/pi étrange\n"
				+"\ntaille attendue: "+Integer.valueOf(Prefixes.RUOK.normalMessLength -Prefixes.headerSZ )
				+"\n il restait encore des choses à lire: "+Boolean.valueOf(in.ready()));
			so.close();
			throw new IOException("Le gestionnaire "+so.getInetAddress()+"a mal commniqué avec le diffuseur lors d'un RUOK ");
		}
		out.print(Prefixes.IMOK+"\r\n");
		out.flush();
	}

	private void receiveLoop(){
		try{
			while(!Thread.interrupted()){
				Socket connectedSocket = rcvSock.accept();
				System.out.println("Quelqu'un s'est connecté en TCP au diffuseur :"+connectedSocket.toString());
				Thread t =new Thread(()->{this.receiveLoopSwitchMan(connectedSocket);});
				t.setDaemon(true);
				t.start();
			}
		}catch(Exception e){}
	}
	
	private void receiveLoopSwitchMan(Socket connSock) {
		try{
			byte[] gestHeader = new byte[Prefixes.headerSZ];
			if(Prefixes.headerSZ != connSock.getInputStream().read(gestHeader)){
				connSock.close();return;
			}
			String headerCont = new String(gestHeader);
			if( headerCont.equals(Prefixes.LAST.toString())){
				try{
					System.out.println("L'entité connecté "+connSock.toString()+"souhaite récupérer des messages");
					this.historygiver(connSock);
				}catch(Exception e){
					System.err.println(" Une erreur est apparue lors d'une demande d'historique: "+e.toString());
				}
			}else if(headerCont.equals(Prefixes.MESS.toString())){
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
		if(null==commSock){throw new NullPointerException();}
		if(commSock.isClosed()){return;}
		try(BufferedReader bfred=new BufferedReader(new InputStreamReader(commSock.getInputStream()));commSock;){
			char[] buffer = new char[Prefixes.LAST.normalMessLength - Prefixes.headerSZ];
			bfred.read(buffer);
			String mssgContent = new String(buffer);
			if(Prefixes.LAST.normalMessLength -Prefixes.headerSZ  != mssgContent.length()
					|| bfred.ready()){
				System.err.println("Une erreur est survenue avec "+commSock.getLocalSocketAddress().toString()+"le contenu aprè LAST était de taille incorrecte;\n"
						+"taille attendue: "+Integer.valueOf(Prefixes.LAST.normalMessLength -Prefixes.headerSZ )
						+"taille obtenue: "+Integer.valueOf(mssgContent.length())
						+"\n il restait encore des choses à lire: "+Boolean.valueOf(bfred.ready()));
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
			if (tosend.length == 0){
				commSock.getOutputStream().write((Prefixes.ENDM.toString()+"\r\n").getBytes());
				commSock.close();
				return;
				}
				OutputStream wheretosend = commSock.getOutputStream();
 			for (String mess : tosend){
				String finalpack= Prefixes.OLDM.toString()+" "+mess+"\r\n";
				if(finalpack.getBytes().length != Prefixes.OLDM.normalMessLength){
					System.err.println("/!\\ Attention le message suivant de taille incorrecte à failli être fourni comme historique à"
						+commSock.toString()
						+"\nle message :\""+finalpack
						+"\"\n Longueur attendue " 
						+Prefixes.OLDM.normalMessLength
						+", longueur obtenue :"+finalpack.getBytes().length+"\n"
					);
					continue;
				}
				wheretosend.write(finalpack.getBytes());	
			}
			wheretosend.write((Prefixes.ENDM.toString()+"\r\n").getBytes());
			System.out.println("un historique a été fourni avec succès à l'entité "+commSock.toString());
			return;
		}catch(IOException ioe){
			System.err.println("problème d'envoi d'historique IOE, arrêt");
			ioe.printStackTrace(System.err);return ;}
	}
	
	public void stopServer(){
			this.broadcastThread.interrupt();
			this.receiveThread.interrupt();
		
		}
	public void addAMessage(Message m){
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
			Thread t=new Thread(()->{toGestionnaire(toRegister,port);});
			t.setDaemon(true);
			t.start();
		return;
		}
	
	public void toGestionnaire( InetAddress toRegister ,int port ){
		try(
			Socket sock = new Socket (toRegister,port);
			OutputStream sendStream = sock.getOutputStream();
			InputStream receiveStream =sock.getInputStream();	
		){
			System.out.println("Connexion établie avec un gestionnaire :"+sock.getInetAddress().toString()+" tentative d'enregistrement...");
			String tosend = Prefixes.REGI.toString()
				+" "
				+this.id
				+" "
				+Diffuseur.Address4Filler(InetAddress.getLocalHost().getHostAddress())
				+" "+Integer.valueOf(this.getReceivePort())
				+" "
				+Diffuseur.Address4Filler(this.getBroadcastAddress().getHostAddress())
				+" "
				+Integer.valueOf(this.getBroadcastPort())
				+"\r\n"; //REGI␣id␣ip1␣port1␣ip2␣port2  REGI lediffo# 127.000.001.001 6663 225.010.020.030 6664
				System.out.flush();
			if(tosend.getBytes().length != Prefixes.REGI.normalMessLength || false ){
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
				sendStream.flush();
			}
			byte[] checkreok =  sock.getInputStream().readNBytes​(Prefixes.headerSZ +2);
			if(checkreok.length != Prefixes.headerSZ+2 || !new String(checkreok).equals(Prefixes.REOK.toString()+"\r\n")){
				System.err.println("Le  Gestionnaire "+ sock.getInetAddress().toString() +" n'a pas confirmé l'enregistrement avec REOK," 
				+"\nil a renvoyé à la place '"+new String(checkreok)
				+"' Deconnexion du gestionnaire.");
				return;
			}
			System.out.println("enregistrement au gestionnaire "+sock.toString()+" réussi !");
			while (true){
				byte[] gestHeader = new byte[Prefixes.headerSZ];
				if(Prefixes.headerSZ != sock.getInputStream().read(gestHeader)){
					System.err.println("Le  Gestionnaire"+ sock.getInetAddress().toString() +"a envoyé un entête trop court. Déconnexion.");
					return;
				}
				String headerCont = new String(gestHeader);
				if(headerCont.equals(Prefixes.MALL.toString())){
					System.out.println("Le Gestionnaire "+sock.toString()+" souhaite communiquer au diffuseur un message à stocker");
					try{gestionnaireMess(sock);} catch(Exception e){
						System.err.println(" Une erreur est apparue lors d'un ajout de message MALL par un gestionnaire arret :"+e.toString());
						return;
						}
				}else if (headerCont.equals(Prefixes.RUOK.toString())){
					try{
						System.out.println("Le Gestionnaire "+sock.toString()+"souhaite savoir si le diffuseur est en ligne");
						this.diffAlive(sock);
					}catch(Exception e){
						System.err.println(" Une erreur est apparue lors d'une Vérification RUOK par un gestionnaire arret"+e.toString());
						return;
					}
				}else{ 
					System.err.println("Le  Gestionnaire"+ sock.getInetAddress().toString() +"a envoyé un entête inconnu.\n" 
						+" l'entête :"
						+headerCont
						+" Déconnexion.");
					return;
				}
			}
		}catch(UnknownHostException e){ System.err.println("Exception 'Hôte Inconnu' lors de l'enregistrement à un gestionnaire, arrêt");
		}catch(IOException e){System.err.println("Exception IOE lors de l'enregistrement à un gestionnaire, arrêt");
		}catch(Exception e){System.err.println("Exception '"+e.toString()+ "' lors de la communication avec un gestionnaire, arrêt");
		}	
	}
	
	private void gestionnaireMess(Socket so)throws IOException{
		//envoit
		PrintWriter out = new PrintWriter(so.getOutputStream());
		//recoit
		BufferedReader in = new BufferedReader(new InputStreamReader(so.getInputStream()));
		char [] buffer =  new char[Prefixes.MESS.normalMessLength - Prefixes.headerSZ];
		//on recoit 
		int  nbrecu= in.read(buffer);
		if(nbrecu != Prefixes.MALL.normalMessLength - Prefixes.headerSZ || in.ready() ) {
			System.err.println("Une erreur est survenue avec "+so.getLocalSocketAddress().toString()+"l'en-tête était de taille incorrecte;\n"
				+"taille attendue: "+(Prefixes.MESS.normalMessLength - Prefixes.headerSZ )
				+"taille obtenue: "+Integer.valueOf(nbrecu)
				+"\n il restait encore des choses à lire: "+Boolean.valueOf(in.ready()));
				throw new IOException("Le gestionnaire "+so.getInetAddress()+"a mal commniqué avec le diffuseur lors d'un MALL ");
		}
		String recu = new String(buffer);
		String id=recu.substring(1,9);
		String mess=recu.substring(9, 150);
		this.addAMessage(new Message(id, mess));
		out.print(Prefixes.ACKM +"\r\n");
		out.flush();
		
	}
	
	public synchronized int getBroadcastPort(){return this.mltcstSA.getPort();}
	public synchronized InetAddress getBroadcastAddress(){return this.mltcstSA.getAddress();}
	public synchronized int getReceivePort(){return this.rcvPrt;}
	public synchronized long getFrequency(){return this.frqcy;}
	public synchronized void setFrequency(long newFreq){this.frqcy = newFreq;}
	public static String Address4Filler(String address ){
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
		Diffuseur lediff = new Diffuseur(args[0], Integer.valueOf(args[1]), Integer.valueOf(args[2]),args[3]);
		try(Scanner sc = new Scanner(System.in);){
			String temp;
			for(int i =0;i<90;i++){
				temp = sc.nextLine();
				lediff.addAMessage(new Message("M0ral3s",temp));	
			}
		}catch(NoSuchElementException ns){return;}
		synchronized(lediff.msgHolder) {
			lediff.msgHolder.notify();
			}
			lediff.addGestionnaireLink(InetAddress.getByName("127.0.1.1"),6667);
		try{
		Object lock= new Object();
		synchronized(lock){
			lock.wait(10000);
			lock.notify();
		}
		}catch(Exception e){}
	}
}

