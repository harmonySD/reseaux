import java.net.InetAddress;
import java.net.MulticastSocket;
import java.awt.Color;
import java.net.DatagramPacket;
import javax . swing .* ;
import javax.swing.text.DefaultCaret;



public class AttendUDP extends Thread{
    int port;
    String add;
    JFrame fenetre = new JFrame("les messages recu !");
    JTextArea label2=new JTextArea();
    // tailles fixée des données / nom utilisateur
	public static final int idsz = 8;
	public static final int datasz = 140;


    public AttendUDP(int p, String a){
        port = p;
        add = a;
    }
    public void run(){
        try(MulticastSocket mso = new MulticastSocket(port);){ 
            mso.joinGroup(InetAddress.getByName(add));
            byte[]data = new byte[4 + 1 + 4 + 1 + idsz + 1 + datasz + 2];
            DatagramPacket paquet = new DatagramPacket(data, data.length);
            fenetre.setSize(500, 500);
            fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            label2.setEditable(false);
            label2.setBackground(Color.BLACK);
            label2.setForeground(Color.GREEN);
            JScrollPane scroll = new JScrollPane(label2);
            DefaultCaret caret = (DefaultCaret)label2.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            fenetre.add(scroll);
            fenetre.setVisible(true);
            while(true){
                mso.receive(paquet);
                String st = new String(paquet.getData(),0,paquet.getLength());
                label2.append(st);
                fenetre.setVisible(true);
            }
        } 
        catch (Exception e){
            e.printStackTrace();
        }
    
    }
    
    public void stops(){
        System.out.println("okoko");
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                fenetre.setVisible(false);
                fenetre.dispose();
                label2.setText(null);//vide le texte de la jtexte area
            }
        } );
    }
}
