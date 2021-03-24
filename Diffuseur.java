import 
import
import 
/**
 * classe du diffuseur 
 * 
 * 
 * **/
public class Diffuseur{
	public static final int idsz = 8;
	private final String id ;
	private unsigned int rcvprt;
	private unsigned int brdcstprt;
	private unsigned int frqcy= 5000;
	private T dataholder ;// ACCES CONCURRENT
	private  Thread broadcastthread ;
	private  Thread receivethread;
	public boolean startBroadcast(){}
	public boolean startListen()throws{}
	
	public Diffuseur(unsigned int recvPort, unsigned int multiCastPort , String id){
	}
	
	private broadcastloop (){
	}
	
	private receiveloop(){
	}
	
	private historygiver(){
	}
}
	
	
