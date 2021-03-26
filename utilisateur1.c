#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <time.h>
#include <errno.h>
#include <pthread.h>

int SIZE_ID=8;
int SIZE_MESS=140;
/* un client se connecte a un gestionnaire 
choisit son diffuseuer
se connecte au diffuseur 
interragit avec le diffuseur 
recoit les multicast (aka udp etc)
envoie des messages en TCP */

typedef struct{
    char port[4];
    char ip[15];
}diffu;

typedef struct{
    int *sock;
    char *id;
}thread_arg;



char *verif_lenght(char *str, int size){
    char *id=str; 
    int s=strlen(str);
    //si la taille de l'id n'est pas egal a 8
    if(s < size){
        //on rajoute des # a l'id
        for(int i = s-1; i < size-1; i++){
           strcat(id,"#");
        } 
    }
    //si la taille est plus grande que 8
    else if (s > size){
        //on tronque l'id a 8 caracteres
        char *id2 =NULL;
        id2=malloc(size);
        for(int i =0; i<size; i++){
            id2[i]=id[i];
       }
       id=id2;
    }
    return id;

}
/*diffu connection_gestionnaire(char *argv){ 
    int p=atoi(argv);
    struct sockaddr_in adress_sock;
    adress_sock.sin_family = AF_INET;
    adress_sock.sin_port = htons(p);
    inet_aton("127.0.0.1",&adress_sock.sin_addr);
    int descr = socket(PF_INET,SOCK_STREAM,0);
    int r = connect(descr, (struct sockaddr *)&adress_sock,
                    sizeof(struct sockaddr_in));
    if(r !=1){
        
        // envoie message LIST 
        // doit recevoir  LINB num_diff 
        // creer tableau de taille num diff  de structure diffu 
        // puis boucle sur num_diff 
        //     recoit le message enregistre dans la structure 
        //     dans le tableau mettre la structure a la position i 
        // fin boucle 
        // choisir un nb aleatoire 
        // prendre le diffu a cette place 
        // renvoyer le diffu 
        
    }
}*/


void *sendMessage(void *sock_desc) {
    thread_arg *foo =(thread_arg*)sock_desc;
    int so=*foo->sock;
    char *id=foo->id;
    while (1) {
        char message[4];
        scanf("%[^\n]%*c", message);
        fflush(stdin);

        if(strstr(message,"MESS")){
            char messAenv[SIZE_MESS];
            printf("%s","entree votre message d'au plus 140 characteres: ");
            scanf("%[^\n]%*c", messAenv);
            fflush(stdin);
            char *m=verif_lenght(messAenv,SIZE_MESS);
            char mess[4+SIZE_MESS+SIZE_ID+2];
            strcpy(mess,message);
            strcat(mess," ");
            strcat(mess,foo->id);//id ne marche pas :(
            strcat(mess," ");
            strcat(mess,m);
            send(so,mess,SIZE_ID+4+2+SIZE_MESS,0);
            char recu[5];
            int taille_rec=recv(so,recu,5,0);
            recu[taille_rec]='\0';
            printf("recu %s\n",recu);
            if(strstr(recu,"ACKM")==NULL){
                printf("Message non recu par le diffuseur");  
            }
        }else if(strstr(message,"LAST")){
            char nb[4];
            printf("%s","afficher combien de message de l'historique : ");
            scanf("%[^\n]%*c",nb);
            fflush(stdin);
            //int nbMess=atoi(nb);//si nb pas un nb alors 0
            char *m=verif_lenght(nb,3);
            printf("m %s",m);
            char mess[4+1+4];//3 marhe pas ???
            strcpy(mess,message);
            strcat(mess," ");
            strcat(mess,m);
            printf("%lu",strlen(mess));
            // send(so,mess,4+1+3,0);
            // char recu[5];
            // int taille_rec=recv(so,recu,5,0);
            // recu[taille_rec]='\0';
            // while (strstr(recu,"ENDM")==NULL){
            //     printf("message de l'historique :%s",recu);
            //     int taille_rec=recv(so,recu,5,0);
            //     recu[taille_rec]='\0';
            // }
        }
        //regarder si MESS ou LAST 
        //si MESS 
        //r
        //scaanf ragrder taille 
        //strcpy MEss scan id etc 
        //envoie 
        
    //     if(send(so, message, SIZE_MESS, 0)<0){
    //        puts("error ! not send");
    //    }
    }
}

void connection_diffuseur(char *port1, char *ip1, char *port2, char *ip2, char *id){
    printf("id ici : %s\n",id);
    int sock=socket(PF_INET,SOCK_DGRAM,0);
    int ok=1;
    int r=setsockopt(sock,SOL_SOCKET,SO_REUSEPORT,&ok,sizeof(ok));
    struct sockaddr_in address_sock;
    address_sock.sin_family=AF_INET;
    address_sock.sin_port=htons(atoi(port1));//3434 
    address_sock.sin_addr.s_addr=htonl(INADDR_ANY);
    r=bind(sock,(struct sockaddr *)&address_sock,sizeof(struct sockaddr_in));
    
    struct ip_mreq mreq;
    mreq.imr_multiaddr.s_addr=inet_addr(ip1);//"225.1.2.4"
    mreq.imr_interface.s_addr=htonl(INADDR_ANY);

    r=setsockopt(sock,IPPROTO_IP,IP_ADD_MEMBERSHIP,&mreq,sizeof(mreq));

    //si on veut envoyer un message a l'emetteur (aka diffuseur) MAIS JE CROIS PAS BESOIN 
    struct sockaddr_in emet;
    socklen_t a=sizeof(emet);
   

    if(r==0){    
        struct sockaddr_in adress_sock2;
        adress_sock2.sin_family = AF_INET;
        adress_sock2.sin_port = htons(5757);//5757
        inet_aton("127.0.0.1",&adress_sock2.sin_addr);//"127.0.0.1"
  
        int descr=socket(PF_INET,SOCK_STREAM,0);
        int r2=connect(descr,(struct sockaddr *)&adress_sock2,
                sizeof(struct sockaddr_in));

        if(r2!=-1){
            char *mess="SALUT!\n";
            send(descr,mess,strlen(mess),0);
            pthread_t thread;
            thread_arg t;
            t.id=id;
            printf("id dans struct %s\n",t.id);
            t.sock=&descr;

            pthread_create(&thread,NULL,sendMessage,&t);
        }else{
            printf("rater");
        }
        while(1){
            char mess_recu[SIZE_MESS+4+8+4+3+1]; //+3 pour les espaces +1 pour '\0'
            int rec=recvfrom(sock,mess_recu,SIZE_MESS+4+8+4+3,0,(struct sockaddr *)&emet,&a);
            mess_recu[rec]='\0';
            printf("Message recu :%s\n",mess_recu);
        }

    }


}




int main(int argc, char**argv){
    if(argc != 2){
        printf("Erreur il faut fournir un numero de port puis un pseudo ! ");
        return 0;
    }
    char *id=verif_lenght(argv[1],SIZE_ID);
    //printf("id : %s",id);

    //connection gestionnaire en mode TCP qui rempli une struture pour sauvegarde 
    //le port et l'adress ip choisit 


    //diffu diffuseur=connection_gestionnaire(argv[1]);
    printf("id %s",id);
    connection_diffuseur("3434","225.1.2.4","5757","127.0.0.1",id);//port et  addresse issue de la structure  PAS SURE 







}
