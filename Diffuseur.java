package mpdrdebog;
import java.net.*;
import java.util.*;
import java.io.IOException;
import java.io.*;


/**
 * classe du diffuseur 
 * **/
public class Diffuseur{

	@SuppressWarnings("unused")
	private final String id ;
	private  int rcvPrt;
	private ServerSocket rcvSock;
	private InetSocketAddress mltcstSA;
	private DatagramSocket mltcstSock;
	private  long frqcy= 500;
	private Holder msgHolder  ; // ACCES CONCURRENT
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
		this.id = MultiCasterID;
		this.rcvPrt = recvPort ;
		this.mltcstSock = new DatagramSocket(); // peut lancer SocketException
		this.mltcstSock.setReuseAddress(true); // pour se faciliter la vie par la suite 
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
	
	 private synchronized void  broadcastLoop (){
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
		try{ // pour attrapper demande d'interruption
			while(true){
				try{
					String tosend=Prefixes.DIFF.toString()+" "+this.msgHolder.next();
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
						synchronized(this.msgHolder){
							msgHolder.wait();
						}this.broadcastThreadIsWaiting = false;
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
		BufferedReader in = new BufferedReader(new InputStreamReader(so.getInputStream()));)
		{
			//on recoit 
			String recu=in.readLine();
			System.out.println("recuuuu "+recu);
			String id=recu.substring(0,8);
			System.out.println("id "+id);
			String mess=recu.substring(9, 140);
			System.out.println("mess "+mess);
			this.addAMessage(new Message(id, mess));
			out.print("ACKM\n\r");

			out.flush();
			so.close();

		}finally {}
	}

	private void diffAlive(Socket so)throws IOException{
		try (
			PrintWriter out = new PrintWriter(so.getOutputStream());
		){
			out.print("IMOK\n\r");
			out.flush();
			so.close();
		}finally {}
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
			byte[] mssgHeader = new byte[Prefixes.headerSZ];
			if(Prefixes.headerSZ != connSock.getInputStream().read(mssgHeader)){
				connSock.close();return;
			}
			String headerCont = new String(mssgHeader);
			if( headerCont.equals(Prefixes.LAST.toString())){
				try{
					System.out.println("L'entité connecté "+connSock.toString()+"souhaite récupérer des messages");
					this.historygiver(connSock);
				}catch(Exception e){
					System.err.println(" Une erreur est apparue lors d'une demande d'historique: "+e.toString());
				}
			}else if (headerCont.equals(Prefixes.RUOK.toString())){
				try{
					System.out.println("L'entité connecté "+connSock.toString()+"souhaite savoir si le diffuseur est en ligne");
					this.diffAlive(connSock);
				}catch(Exception e){
					System.err.println(" Une erreur est apparue lors d'une Vérification RUOK par un gestionnaire "+e.toString());
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
			String mssgContent = bfred.readLine();
			if(Prefixes.LAST.normalMessLength -Prefixes.headerSZ  != mssgContent.length()
					|| bfred.ready()){
				commSock.close();return;
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
				commSock.getOutputStream().write(Prefixes.ENDM.toString().getBytes());
				commSock.close();
				return;}
				String entete = Prefixes.OLDM.toString();
				OutputStream wheretosend = commSock.getOutputStream();
 			for (String mess : tosend){
				String finalpack= Prefixes.OLDM.toString()+" "+mess;
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
			wheretosend.write(Prefixes.ENDM.toString().getBytes());
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
		if (m== null){return;}
		synchronized (this.msgHolder){
		this.msgHolder.add(m);}
		if (!this.broadcastThreadIsWaiting){
			this.msgHolder.notify();
		}
		System.out.println("le message "+ m.toString() +"a été ajouté avec succès.");
		return;
	}
	
	public synchronized int getBroadcastPort(){return this.mltcstSock.getLocalPort();}
	public synchronized int getReceivePort(){return this.rcvPrt;}
	public synchronized long getFrequency(){return this.frqcy;}
	public synchronized void setFrequency(long newFreq){this.frqcy = newFreq;}
	private synchronized DatagramSocket getMulticastSock(){return this.mltcstSock;}
	public static String getLocalAddress() throws UnknownHostException{return InetAddress.getLocalHost().toString();}
	synchronized Holder getHolder(){/** à utiliser uniquement à des fins de débogage !  package private**/return this.msgHolder;}

	public static void main (String [] args)throws Exception {
		Diffuseur lediff = new Diffuseur(args[0], Integer.valueOf(args[1]), Integer.valueOf(args[2]),args[3]);
		try{
			Scanner sc = new Scanner(System.in);
			String temp;
			for(int i =0;i<15;i++){
				temp = sc.nextLine();
				System.out.println("Ajout du message : "+temp);
				lediff.addAMessage(new Message("M0ral3s",temp));	
			}
		}catch(NoSuchElementException ns){return;}
		synchronized(lediff.msgHolder) {
			lediff.msgHolder.notify();}
		try{
		Object lock= new Object();
		synchronized(lock){
			lock.wait(10000);
			lock.notify();
		}
		}catch(Exception e){}
	}
}
