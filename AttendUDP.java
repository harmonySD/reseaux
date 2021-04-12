import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import javax . swing .* ;
import javax.swing.text.DefaultCaret; 


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
            JFrame fenetre = new JFrame("bjr");
            //fenetre.getContentPane().add((new JLabel("bonjour")));
            //JLabel label = new JLabel("heyhey",JLabel.LEFT);
            //fenetre.add(label);
            fenetre.setSize(500,500);
            fenetre.setVisible(true);
            fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JTextArea label2=new JTextArea();
            label2.setEditable(false);
            JScrollPane scroll= new JScrollPane(label2);
            DefaultCaret caret=  (DefaultCaret)label2.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            //scroll.add(label2);
            fenetre.add(scroll);
            while(true){
                //ds.receive(paquet);
                mso.receive(paquet);
                String st = new String(paquet.getData(),0,paquet.getLength());
                //System.out.print(st);
                label2.append(st);
                //fenetre.setLayout(new FlowLayout())
                fenetre.setVisible(true);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
