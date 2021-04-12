import java.io.*;
import java.net.*;

public class Utilisateur2{
    public static String completeIfNeeded(String src, int wantedsz){
        int manytofill = wantedsz - src.length();
        if(manytofill>0){
          return src + "#".repeat(manytofill);  
        }
        return src;
    }

    public static void main(String[] args){
        //recuperer pseudo port add
        String add=args[0];
        int porttcp=Integer.parseInt(args[1]);
        String addm=args[2];
        int portudp=Integer.parseInt(args[3]);
        String pseudo=completeIfNeeded(args[4],8);

        try{
            System.out.println("*********UTILISATER2*********\r\n"+
                                "Id : "+pseudo+"\r\n"+
                                "******************************");
            AttendUDP audp= new AttendUDP(portudp,addm);
            AttendTCP atcp= new AttendTCP(porttcp, add,pseudo);
            audp.start();
            atcp.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}