import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Utilisateur2{
    public static String completeIfNeeded(String src, int wantedsz){
        int manytofill = wantedsz - src.length();
        if(manytofill>0){
          return src + "#".repeat(manytofill);  
        }
        else if(manytofill<0){
            return src.substring(0, wantedsz);
        }
        return src;
    }


    public static void main(String[] args){
        String pseudo = completeIfNeeded(args[0],8);
        Scanner sc=new Scanner(System.in);

        try{
            while(true){
                System.out.println("diffuseur ou gestionnaire");
                String choix = sc.nextLine();
                if(choix.equals("diffuseur")){
                    //demander ip port etc
                    System.out.println("info du diffuseur (id ip1 port2 ip2 port2) : ");
                    // parser la reponse
                    String diff = sc.nextLine();
                    String id = diff.substring(0,8);
                    String ip1 = diff.substring(9,24);
                    String port1 = diff.substring(25, 29);
                    String ip2 = diff.substring(30, 45);
                    String port2 = diff.substring(46, 50);
                    System.out.println("*********UTILISATER2*********\r\n"+
                                        "Id : "+pseudo+"\r\n"+
                                        "******************************");
                    AttendTCP atcp = new AttendTCP(Integer.parseInt(port2), ip2, pseudo);
                    AttendUDP audp = new AttendUDP(Integer.parseInt(port1),ip1);
                    
                    atcp.start();
                    audp.start();
                    //while(atcp.isAlive()){}
                    atcp.join();
                    audp.join();
                    audp.stops();
                }
                else if(choix.equals("gestionnaire")){
                    System.out.println("port : ");
                    String port = sc.nextLine();
                    System.out.println("addresse : ");
                    String add = sc.nextLine();
                    System.out.println("Message pour tout les diffuseurs ? y/n :");
                    String rep= sc.nextLine();
                    if(rep.equals("n")){
                         Cogestionnaire cg = new Cogestionnaire(Integer.parseInt(port),add,false,pseudo);
                         cg.start();
                         while(cg.isAlive()){}
                    }
                    else if (rep.equals("y")){
                         Cogestionnaire cg = new Cogestionnaire(Integer.parseInt(port), add, true,pseudo);
                         cg.start();
                         while(cg.isAlive()){}
    
                    }

                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}