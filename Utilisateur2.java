import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Utilisateur2{
    public static String completeIfNeeded(String src, int wantedsz){
        int manytofill = wantedsz - src.length();
        if(manytofill>0){
          return src + "#".repeat(manytofill);  
        }
        return src;
    }

    public static void main(String[] args){
        String add = args[0];
        int porttcp = Integer.parseInt(args[1]);
        String addm = args[2];
        int portudp = Integer.parseInt(args[3]);
        String pseudo = completeIfNeeded(args[4],8);
        Scanner sc=new Scanner(System.in);

        try{
            while(true){
                System.out.println("diffuseur ou gestionnaire");
                String choix = sc.nextLine();
                if(choix.equals("diffuseur")){
                    System.out.println("*********UTILISATER2*********\r\n"+
                                        "Id : "+pseudo+"\r\n"+
                                        "******************************");
                    AttendUDP audp = new AttendUDP(portudp,addm);
                    AttendTCP atcp = new AttendTCP(porttcp, add, pseudo);
                    audp.start();
                    atcp.start();
                    while(atcp.isAlive()){}

                    audp.stops();
                    System.out.println("grrr");
                }
                // else if(choix.equals("gestionnaire")){
                //     System.out.println("port :");
                //     choix = sc.nextLine();
                //     Cogestionnaire cg = new Cogestionnaire(Integer.parseInt(choix));
                //     cg.start();
                // }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}