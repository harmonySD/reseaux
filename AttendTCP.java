import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class AttendTCP extends Thread{
    // tailles fixée des données / nom utilisateur
	public static final int idsz = 8;
    public static final int datasz = 140;
    
    int port; 
    String add;
    String pseudo;
    public AttendTCP(int p, String a, String pseu){
        port = p;
        add = a;
        pseudo = pseu;
    }

    public String completeIfNeeded(String src, int wantedsz){
        int manytofill = wantedsz - src.length();
        if(manytofill > 0){
          return src + "#".repeat(manytofill);  
        }
        return src;
    }
    public String completenbIfNeeded(String src, int wantedsz){
        int manytofill = wantedsz - src.length();
        if(manytofill > 0){
            return "0".repeat(manytofill) + src;
        }else if (manytofill < 0){
            return "999";
        }
        return src;
    }
    


    public void run(){
        try(Socket cs = new Socket(add,port);
            //envoie
            PrintWriter out = new PrintWriter(cs.getOutputStream());
             //recoit
             BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
        ){
            Scanner sc = new Scanner(System.in);
            String msg;
            boolean arret=false;
            while(arret==false){
                //envoie
                msg = sc.nextLine();
                if(msg.equals("MESS")){
                    String menv;
                    System.out.println("Entrer votre message : ");
                    msg = sc.nextLine();
                    msg = completeIfNeeded(msg, datasz);
                    menv = "MESS " + pseudo + " " + msg + "\r\n";
                    out.print(menv);
                    out.flush();
                    //recoit
                    String mess = in.readLine();
                    System.out.println(("recu "+mess));
                    if(!mess.equals("ACKM")){
                        System.out.println("message non recu par le diffusuer");
                    }

                }
                else if(msg.equals("LAST")){
                    System.out.println("afficher combien de messages :");
                    msg = sc.nextLine();
                    msg = completenbIfNeeded(msg, 3);
                    String menv = "LAST " + msg + "\r\n";
                    out.print(menv);
                    out.flush();
                    //recoit
                    String mess = in.readLine();
                    System.out.println(mess);
                    while(!mess.equals("ENDM")){
                        mess = in.readLine();
                        System.out.println(mess);
                    }


                }
                else if(msg.equals("q")){
                    arret=true;
                }
                
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
