import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
public class AttendUDP extends Thread{
    int port;
    String add;
    // tailles fixée des données / nom utilisateur
	public static final int idsz=8;
	public static final int datasz=140;


    public AttendUDP(int p, String a){
        port=p;
        add=a;
    }
    public void run(){
        try{
            MulticastSocket mso = new MulticastSocket(port);
            mso.joinGroup(InetAddress.getByName(add));

           // DatagramSocket ds = new DatagramSocket(port);
            byte[]data = new byte[4+1+4+1+idsz+1+datasz+2];
            DatagramPacket paquet = new DatagramPacket(data, data.length);
            while(true){
                //ds.receive(paquet);
                mso.receive(paquet);
                String st = new String(paquet.getData(),0,paquet.getLength());
                System.out.print(st);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
