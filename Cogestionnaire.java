import java.io.*;
import java.net.Socket;
public class Cogestionnaire extends Thread{
        // tailles fixée des données / nom utilisateur
	public static final int idsz = 8;
    public static final int datasz = 140;
    int port;
    String address;


    public Cogestionnaire(int p, String add){
        port=p;
        address=add;
    }

    public void run(){
        try(Socket cs = new Socket(address,port);
        //envoie
        PrintWriter out = new PrintWriter(cs.getOutputStream());
        //recoit
        BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));){

            String menv= "LIST\r\n";
            out.print(menv);
            out.flush();
            //recoit
            String mess = in.readLine();
            //System.out.println(("recu "+mess));
            String nbstr=mess.substring(5, mess.length());
            int nb = Integer.parseInt(nbstr);
            if(nb==0){
                System.out.println("Aucun diffuseur enregistré !");
            }else{
                for(int i= 0; i< nb; i++){
                    String diff = in.readLine();
                    System.out.println((diff));
                    System.out.print("**********************\n");

                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
