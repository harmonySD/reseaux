#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <time.h>
#include <errno.h>

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

void connection_diffuseur(char *port, char *ip, char *id){
    int sock=socket(PF_INET,SOCK_DGRAM,0);
    int ok=1;
    int r=setsockopt(sock,SOL_SOCKET,SO_REUSEPORT,&ok,sizeof(ok));
    struct sockaddr_in address_sock;
    address_sock.sin_family=AF_INET;
    address_sock.sin_port=htons(3434);
    address_sock.sin_addr.s_addr=htonl(INADDR_ANY);
    r=bind(sock,(struct sockaddr *)&address_sock,sizeof(struct sockaddr_in));
    struct ip_mreq mreq;
    mreq.imr_multiaddr.s_addr=inet_addr("225.1.2.4");
    mreq.imr_interface.s_addr=htonl(INADDR_ANY);
    r=setsockopt(sock,IPPROTO_IP,IP_ADD_MEMBERSHIP,&mreq,sizeof(mreq));

    //si on veut envoyer un message a l'emetteur (aka diffuseur) MAIS JE CROIS PAS BESOIN 
    struct sockaddr_in emet;
    socklen_t a=sizeof(emet);
   

    if(r==0){
        while(1){//while recv !=0 ?
            //recevoir et peut etre envoyer mais comment faire si on veut envoyer un seule mess
            //et continuer a recevoir le diffu ?
            char mess[4]; //MESS
            /*if (fgets(mess,4,stdin)!=NULL){
                if(strcmp(mess,"MESS")){
                    //envoie message choisit au hasard
                    // verifier bien recu ACKM apres 
                }
                else if(strstr(mess,"LAST")){
                    //envoie mess (normalement contient LAST 6) par exemple 
                    //boucle sur nombre de mess etc 
                }
            }*/
            char mess_recu[SIZE_MESS+4+8+4+3+1]; //+3 pour les espaces +1 pour '\0'
            int rec=recvfrom(sock,mess_recu,SIZE_MESS+4+8+4+3,0,(struct sockaddr *)&emet,&a);
            mess_recu[rec]='\0';
            printf("Message recu :%s\n",mess_recu);
        }

    }


}



char *verif_id(char *str){
    char *id=str; 
    int s=strlen(str);
    //si la taille de l'id n'est pas egal a 8
    if(s < SIZE_ID){
        //on rajoute des # a l'id
        for(int i = s-1; i < SIZE_ID-1; i++){
           strcat(id,"#");
        } 
    }
    //si la taille est plus grande que 8
    else if (s > SIZE_ID){
        //on tronque l'id a 8 caracteres
        char *id2 =NULL;
        id2=malloc(SIZE_ID);
        for(int i =0; i<SIZE_ID; i++){
            id2[i]=id[i];
       }
       id=id2;
    }
    return id;

}

int main(int argc, char**argv){
    if(argc != 2){
        printf("Erreur il faut fournir un numero de port puis un pseudo ! ");
        return 0;
    }
    char *id=verif_id(argv[1]);
    //printf("id : %s",id);

    //connection gestionnaire en mode TCP qui rempli une struture pour sauvegarde 
    //le port et l'adress ip choisit 


    //diffu diffuseur=connection_gestionnaire(argv[1]);
    printf("id %s",id);
    connection_diffuseur("3434","127.0.0.1",id);//port et  addresse issue de la structure  PAS SURE 







}