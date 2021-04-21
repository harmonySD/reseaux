import java.net.*;
import java.util.*;
import java.io.IOException;





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
	private  long frqcy= 5000;
	private Holder msgHolder  ; // ACCES CONCURRENT
	private  Thread broadcastThread ;
	private  boolean broadcastThreadIsWaiting = false; // le SEUL modifieur est broadCastLoop
	private  Thread receiveThread;
	public final boolean startBroadcast(){this.broadcastThread.start(); return true;}
	public final boolean startListen(){this.receiveThread.start(); return true;}
	
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
						}
						this.broadcastThreadIsWaiting = false;
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
	
	private void receiveLoop(){
		Socket connectedSocket =new Socket(); // pour satisfaire Java
		try{
			while(true){
				connectedSocket = rcvSock.accept();
				byte[] mssgHeader = new byte[Prefixes.headerSZ];
				if(Prefixes.headerSZ != connectedSocket.getInputStream().read(mssgHeader)){
					connectedSocket.close();continue;
					
				}
				
				String headerCont = new String(mssgHeader);
				if( headerCont==Prefixes.LAST.toString()){
				}
				else if (headerCont==Prefixes.RUOK.toString()){
					
				}
				else if(Prefixes.MESS.toString() ==Prefixes.MESS.toString() ){
					
				}
				else{connectedSocket.close();}//rejet de la connexion
				
				if(this.receiveThread.isInterrupted()){throw new InterruptedException();}
			}
		}catch(InterruptedException end){ 
			try{rcvSock.close();}catch(IOException e){}
			try{connectedSocket.close();}catch(IOException e){};
			// code en cas de demande d'interruption du thread 
			}catch(IOException e){}
	}
	
	private void historygiver(){
	
	}
	
	public void stopServer(){
			this.broadcastThread.interrupt();
			this.receiveThread.interrupt();
		
		}
	public void addAMessage(Message m){
		if (m== null){return;}
		synchronized (this.msgHolder){
		this.msgHolder.add(m);}
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
			for(int i =0;i<10;i++){
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

	
	
