import java.net.*;

/**
 * classe du diffuseur 
 * 
 * 
 * **/
public class Diffuseur{

	private final String id ;
	private unsigned int rcvprt;
	private DatagramSocket mltcstSock;
	private  long frqcy= 10000; 
	private Holder msgholder  ; // ACCES CONCURRENT
	private  Thread broadcastThread ;
	private  Thread receivethread;
	public final boolean startBroadcast()throws{}
	public final boolean startListen()throws{}
	
	public Diffuseur(String id, int recvPort,  int multiCastPort, String multiCastAddress ) throws SocketException{
		if(recvPort <0 || multiCastPort <0  || recvPort > 65535|| multiCastPort > 65535 ){throw new IllegalArgumentException("Port  incorrect");}
		this.rcvprt = recvPort ;
		this.mltcstSock = new DatagramSocket(multiCastPort,InetAdress.getByName(broadCastAddress)); // peut lancer SocketException
		this.mltcstSock.setReuseAddress​(true); // pour se faciliter la vie par la suite 
		
		this.broadCastThread =new Thread( ()->{this.broadcastLoop();} );
		this.receiveThread = new Thread( ()->{this.receiveLoop();} );
		this.broadCastThread.startListen();
		this.broadCastThread.startBroadcast;
	}
	
	private broadcastLoop (){
		Message tosend ;
		try{
			while(true){
				try{this.msgholder.
					}catch(NoSuchElementException ns){
						this.wait();
						continue;
					}
				Thread.sleep(this.getFrequency());
			}
		} catch(InterruptedException end){
			// code en cas de demande d'interruption du thread 
			}
	}
	
	private receiveLoop(){
		try(){
			while(true){
				String uidrec;
				String mssgrec;
				this.Holder.add(new Message(uidrec,mssgrec));
				this.broadcastThread().notify();
			}
		}catch(InterruptedException end){
			}
	}
	
	private historygiver(){
	
	}
	public void stopServer(){
		
		
		}
		
		
	public synchronized int getBroadcastPort(){return this.brdcstprt;}
	public synchronized int getReceivePort(){return this.rcvprt;}
	public synchronized long getFrequency(){return this.frqcy;}
	public synchronized long setFrequency(long newFreq){this.frqcy = newFreq;}
	private synchronized DatagramSocket getBroadcastSock(){return this.mltcstSock;}
	public static String getLocalAdress(){return InetAdress.getLocalHost.toString();}
	package private synchronized Holder getHolder(){/** à utiliser uniquement à des fins de débogage ! **/return this.Holder;}
	 
}

	
	
