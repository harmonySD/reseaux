import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

public class Cogestionnaire extends Thread{
        // tailles fixée des données / nom utilisateur
	public static final int idsz = 8;
    public static final int datasz = 140;
    int port;
    String address;
    boolean messageall;
    String pseudo;

    public static String completeIfNeeded(String src, int wantedsz){
        int manytofill = wantedsz - src.length();
        if(manytofill>0){
          return src + "#".repeat(manytofill);  
        }
        else if(manytofill<0){
            return src.substring(0, wantedsz);
        }
       
        System.out.println(src);
        return src;
    }


    public Cogestionnaire(int p, String add,Boolean ma,String pseudo){
        this.port=p;
        this.address=add;
        this.messageall=ma;
        this.pseudo=pseudo;
    }

    public void run(){
        try(Socket cs = new Socket(address,port);
        //envoie
        PrintWriter out = new PrintWriter(cs.getOutputStream());
        //recoit
        BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));){
            if(messageall==false){
                String menv= "LIST\r\n";
                out.print(menv);
                out.flush();
                //recoit
                String mess = in.readLine();
                System.out.println("recu "+mess+"de "+cs.toString());
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
            }else if(messageall==true){
                System.out.println("Votre message a diffuser par tout les diffuseurs: ");
                Scanner scn=new Scanner(System.in);
                String messall=scn.nextLine();
                String mes=completeIfNeeded(messall,140);
                System.out.println(mes);
                String menv2="MALL "+this.pseudo+" "+mes;
                out.print(menv2+"\r\n");
                out.flush();
                //recu confiration ?
                String mess = in.readLine();
                System.out.println("recu "+mess+"de "+cs.toString());
                if(mess.equals("RALL")){System.out.println("Message bien recu par le gestionnaire");}


            }
            

        }catch (ConnectException e){
            System.out.print("aucun gestionnaire connecter a cette addresse :( \n");
        } 
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
