import 
import
import 
/**
 * classe du diffuseur 
 * 
 * 
 * **/
public class Diffuseur<T>{
	private char [] id = new char[8];
	private unsigned int rcvprt;
	private unsigned int brdcstprt;
	private unsigned int frqcy= 5000;
	private T dataholder ;
	private  Thread broadcastthread ;
	private  Thread receivethread;
	public boolean startBroadcast(){}
	public boolean startListen()throws{}
	
	public Diffuseur(unsigned int recvport, unsigned int brdcastThread , String id){
	}
	
	private broadcastloop (){
	}
	
	private receiveloop(){
	}
	
	private historygiver(){
	}
}
	
	
