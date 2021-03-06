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


int BSLASH = 1;
int FIN = 2;
int SIZE_ID = 8;
int SIZE_MESS = 140;
int ESP = 1;
int PORT = 4;
int NBMESS = 3;
int IP = 15;
int NUMMESS = 4;
int NUMDIFF = 2;
int  SM = 4;
char *ID;


typedef struct{
    char id[9];
    char ip1[16];
    char port1[5];
    char ip2[16];
    char port2[5];    
}diffu;


char *verif_lenght(char *str, int size){
    char *final_str = str; 
    int size_str = strlen(str);
    //if size of str is not equal to size(aka size wanted)
    if(size_str < size){
        //add # 
        for(int i = size_str-1; i < size-1; i++){
           strcat(final_str,"#");
        } 
    }
    //if size of str bigger than size
    else if (size_str > size){
        char *final_str2 = NULL;
        final_str2=malloc(size);
        for(int i = 0; i < size; i++){
            final_str2[i] = final_str[i];
       }
       final_str = final_str2;
    }
    return final_str;
}


char *verif_lenght_nb(char *str, int size){
    char *nb = str;
    int size_nb = strlen(str);
    if(size_nb < size){
        char *nb2 = NULL;
        nb2 = malloc(size);
        //add some 0 to the left
        for(int i = 0; i<(size - size_nb); i++){
            nb2[i] = '0';
        }
        strcat(nb2, nb);
        nb = nb2;
    }
    //if size of nb bigger than size 
    //we supposed its the max nb of messages 
    else if(size_nb > size){
        nb="999";
    }
    return nb;
}


diffu connection_gestionnaire(char *argv){ 
    int p = atoi(argv);
    struct sockaddr_in adress_sock;
    adress_sock.sin_family = AF_INET;
    adress_sock.sin_port = htons(p);
    inet_aton("127.0.0.1", &adress_sock.sin_addr);
    int descr = socket(PF_INET, SOCK_STREAM, 0);
    int r = connect(descr, (struct sockaddr *)&adress_sock, sizeof(struct sockaddr_in));
    if(r != 1){
        int s = send(descr, "LIST\r\n", SM+FIN, 0);
        if(s<0){
            printf("%s\n", strerror(errno));
        }
        sleep(4);
        char rec[SM+ESP+NUMDIFF+FIN+BSLASH];
        int taille_recu=recv(descr, rec, SM + ESP + NUMDIFF + FIN, 0);
        rec[taille_recu] = '\0';
        //get num_diff
        char *nbstr = strtok(rec, "LINB ");
        int nb = atoi(nbstr);
        diffu tab[nb];
        int i = 0;
        for(i = 0; i < nb; i++){
            //fill the array of diffu 
            char rec[SM + ESP + SIZE_ID + ESP + IP + ESP + PORT + ESP+ IP 
                    + ESP + PORT + FIN + BSLASH];
            int t_recu = recv(descr, rec, SM + ESP + SIZE_ID + ESP + IP + ESP + 
                                PORT + ESP + IP + ESP + PORT + FIN, 0);
            rec[t_recu] = '\0';
            // char *sep="ITEM ";
            // char *tok=strtok(rec,sep);
            // memcpy(tab[i].id,tok,SIZE_ID+1);
            // tok=strtok(NULL," ");
            // memcpy(tab[i].ip1,tok,IP+1);
            // tok=strtok(NULL," ");
            // memcpy(tab[i].port1,tok,PORT+1);
            // printf("port %s",tok);
            // printf("port struct %s",tab[i].port1);
            // tok=strtok(NULL," ");
            // memcpy(tab[i].ip2,tok,IP+1);
            // tok=strtok(NULL,"\r");
            // printf("port %s pppp",tok);

            // memcpy(tab[i].port2,tok,PORT+1);
            // printf("port struct %s\n",tab[i].port2);

            char * tok;
            int j = 0;
            tok = strtok (rec,"ITEM ");
            while (tok != NULL)
            {
                if(j == 0){
                    memcpy(tab[i].id,tok,SIZE_ID+1);
                    tok = strtok (NULL, " \r\n");
                }else if(j == 1){
                    memcpy(tab[i].ip1,tok,IP+1);
                    tok = strtok (NULL, " \r\n");
                }else if(j == 2){
                    memcpy(tab[i].port1,tok,PORT+1);
                    tok = strtok (NULL, " \r\n");
                }else if(j == 3){
                    memcpy(tab[i].ip2,tok,IP+1);
                    tok = strtok (NULL, " \r\n");
                }else if(j == 4){
                    memcpy(tab[i].port2,tok,PORT+1);
                    tok = strtok (NULL, " \r\n");
                }
                j++;  
            }
        }
        srand(time(NULL));
        int pos=rand()%i;
        diffu choix_diffu;
        memmove(&choix_diffu,&tab[pos],sizeof(diffu));
        return choix_diffu;        
    } 
    //no diffuseur 
    diffu choix_diffu;
    strncpy(choix_diffu.id,"",SIZE_ID);
    strncpy(choix_diffu.ip1,"",IP);
    strncpy(choix_diffu.port1,"",PORT);
    strncpy(choix_diffu.ip2,"",IP);
    strncpy(choix_diffu.port2,"",PORT);
    return choix_diffu;
}


void *sendMessage(void *sock_desc) {
    int so = *((int *) sock_desc);
    while (1) {
        char message[SM+FIN];
        scanf("%[^\n]%*c", message);
        fflush(stdin);
        if(strstr(message, "MESS")){
            char messAenv[SIZE_MESS];
            printf("%s", "entree votre message d'au plus 140 characteres: ");
            scanf("%[^\n]%*c", messAenv);
            fflush(stdin);
            char *m = verif_lenght(messAenv, SIZE_MESS);
            char mess[SM + ESP + SIZE_MESS + ESP + SIZE_ID + FIN];
            strcpy(mess, message); 
            strcat(mess, " ");
            strcat(mess, ID);
            strcat(mess, " ");
            strcat(mess, m);
            strcat(mess, "\r\n");
            int test_send = send(so, mess, SM + ESP + SIZE_ID + ESP 
                            + SIZE_MESS + FIN, 0);
            if(test_send < 0){
                printf("test_send =%d\n", test_send);
                printf("%s\n", strerror(errno));
            }
            printf("mess :%s\n", mess);
            char recu[SM + FIN + BSLASH];
            int taille_rec = recv(so, recu, SM + FIN, 0);
            recu[taille_rec] = '\0';
            //printf("recu %s\n",recu); //Verification ACKM send
            if(strstr(recu,"ACKM") == NULL){
                printf("Message non recu par le diffuseur");  
            }
        }else if(strstr(message, "LAST")){
            char nb[SM];
            printf("%s", "afficher combien de message de l'historique : ");
            scanf("%[^\n]%*c", nb);
            fflush(stdin);
            if(atoi(nb) == 0){
                strcpy(nb,"0");
            }
            char *m = verif_lenght_nb(nb, 3);
            printf("m %s", m);
            char mess[SM + ESP + NBMESS + FIN];
            strcpy(mess, message);
            strcat(mess, " ");
            strcat(mess, m);
            strcat(mess, "\r\n");
            send(so, mess, SM + ESP + NBMESS + FIN, 0);
            char recu[SM + ESP + NUMMESS + ESP + SIZE_ID + ESP + SIZE_MESS 
                    + FIN + BSLASH];
            int taille_rec = recv(so, recu, SM + ESP + NUMMESS + ESP + SIZE_ID
                    + ESP + SIZE_MESS + FIN, 0);
            recu[taille_rec] = '\0';
            while (strstr(recu,"ENDM") == NULL){
                printf("message de l'historique :%s\n", recu);
                taille_rec = recv(so, recu, SM + ESP + NUMMESS + ESP + SIZE_ID
                         + ESP + SIZE_MESS + FIN, 0);
                recu[taille_rec] = '\0';
            }
        }
    }
}


void connection_diffuseur(char *port1, char *ip1, char *port2, char *ip2, char *id){
    int sock = socket(PF_INET, SOCK_DGRAM, 0);
    int ok = 1;
    int r = setsockopt(sock, SOL_SOCKET, SO_REUSEPORT, &ok, sizeof(ok));
    struct sockaddr_in address_sock;
    address_sock.sin_family = AF_INET;
    address_sock.sin_port = htons(atoi(port1));//3434 
    address_sock.sin_addr.s_addr = htonl(INADDR_ANY);
    r = bind(sock, (struct sockaddr *)&address_sock, sizeof(struct sockaddr_in));
    
    struct ip_mreq mreq;
    mreq.imr_multiaddr.s_addr  = inet_addr(ip1);//"225.1.2.4"
    mreq.imr_interface.s_addr = htonl(INADDR_ANY);

    r = setsockopt(sock, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq));
   
    if(r == 0){    
        struct sockaddr_in adress_sock2;
        adress_sock2.sin_family = AF_INET;
        adress_sock2.sin_port = htons(atoi(port2));//5757
        inet_aton(ip2, &adress_sock2.sin_addr);//"127.0.0.1"
  
        int descr = socket(PF_INET, SOCK_STREAM, 0);
        int r2 = connect(descr, (struct sockaddr *)&adress_sock2,
                sizeof(struct sockaddr_in));

        if(r2 != -1){
            pthread_t thread;
            pthread_create(&thread, NULL, sendMessage, &descr);
        }else{
            printf("error thread");
        }
        while(1){
            char mess_recu[SM + ESP + NUMMESS + ESP + SIZE_ID + ESP 
                    + SIZE_MESS + FIN + BSLASH]; 
            int rec = recv(sock, mess_recu, SM + ESP + NUMMESS + ESP 
                    + SIZE_ID + ESP + SIZE_MESS + FIN, 0);
            mess_recu[rec] = '\0';
            printf("Message recu :%s\n", mess_recu);
        }
    }
}


int main(int argc, char**argv){
    if(argc != 3){
        printf("Erreur il faut fournir un numero de port puis un pseudo ! ");
        return 0;
    }
    ID=verif_lenght(argv[2], SIZE_ID);

    //connection gestionnaire en mode TCP qui rempli une struture pour sauvegarde 
    //le port et l'adress ip choisit 


    diffu diffuseur=connection_gestionnaire(argv[1]);
    // printf("port1 %s\n",diffuseur.port1);
    // printf("ip1 %s\n",diffuseur.ip1);
    // printf("ip2 %s\n",diffuseur.ip2);
    // printf("port2 %s\n",diffuseur.port2);
    // printf("id %s",ID);

   // connection_diffuseur("3434","225.1.2.4","5757","127.0.0.1",ID);//port et  addresse issue de la structure  PAS SURE 
    connection_diffuseur(diffuseur.port1,diffuseur.ip1,diffuseur.port2,diffuseur.ip2,ID);






}
