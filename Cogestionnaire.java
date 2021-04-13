import java.io.*;
import java.net.Socket;
public class Cogestionnaire extends Thread{
        // tailles fixée des données / nom utilisateur
	public static final int idsz = 8;
    public static final int datasz = 140;
    int port;


    public Cogestionnaire(int p){
        port=p;
     
    }

    public void run(){
        try(Socket cs = new Socket("127.0.0.1",port);
        //envoie
        PrintWriter out = new PrintWriter(cs.getOutputStream());
        //recoit
        BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));){

            String menv= "LIST\r\n";
            out.print(menv);
            out.flush();
            //recoit
            String mess = in.readLine();
            System.out.println(("recu "+mess));
            String nbstr=mess.substring(5, mess.length());
            //System.out.println(nbstr);
            int nb = Integer.parseInt(nbstr);
            for(int i= 0; i< nb; i++){
                String diff = in.readLine();
                System.out.println((diff));
                System.out.print("**********************\n");

            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
