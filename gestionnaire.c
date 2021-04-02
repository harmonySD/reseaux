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

// gcc -Wall -pthread -o gestio gestionnaire.c


typedef struct{
    char id[9];
    char ip1[16];
    char port1[5];
    char ip2[16];
    char port2[5];
}diffuseur;

int SIZE_FORME=4;
int SIZE_PORT=4;
int SIZE_ID=8;
int SIZE_IP=15;
int SIZE_MESS=140;
diffuseur annuaire[100];
int nbDiffu=0;

// typedef struct{
//     char port[4];
//     diffu annuaire[100];
// }gestionnaire;

void printDiffu(diffuseur diffu){
    printf("id %s, ip1 %s, port1 %s, ip2 %s, port2 %s\n",
        diffu.id,diffu.ip1,diffu.port1,diffu.ip2,diffu.port2);
}

void printAnnuraire(){
    for(int i=0;i<nbDiffu;i++){
        printDiffu(annuaire[i]);
    }
}

// donne la liste des diffuseurs au client
void recvClient(int sock){
    int tailleMessDiffu=SIZE_FORME+1+SIZE_IP+1+
                        SIZE_IP+1+SIZE_PORT+1+SIZE_IP+1+SIZE_PORT+2;
    // LINB nb=2octet \r\n\0
    char numDiff[SIZE_FORME+1+2+2+1];
    strcpy(numDiff,"LINB ");
    // ajout 0 pour nbDiffu
    sprintf(numDiff,"%i\r\n",nbDiffu);
    send(sock,numDiff,strlen(numDiff),0);
    for (int i=0;i<nbDiffu;i++){
        char envoieDiffu[tailleMessDiffu+1];
        strcpy(envoieDiffu,"ITEM ");
        sprintf(envoieDiffu,"%s %s %s %s %s\r\n",
            annuaire[i].id,annuaire[i].ip1,annuaire[i].port1,
            annuaire[i].ip2,annuaire[i].port2);
        send(sock,envoieDiffu,strlen(envoieDiffu),0);
    }
    close(sock);
}

// enregistre un diffuseur
void actionDiffuseur(int sock,char *newDiffu){
    if(nbDiffu<99){
        diffuseur *diffu=malloc(sizeof(diffuseur));

        char *strToken=strtok(newDiffu," ");
        strToken=strtok(NULL," ");
        strcpy(diffu->id,strToken);
        strToken=strtok(NULL," ");
        strcpy(diffu->ip1,strToken);
        strToken=strtok(NULL," ");
        strcpy(diffu->port1,strToken);
        strToken=strtok(NULL," ");
        strcpy(diffu->ip2,strToken);
        strToken=strtok(NULL," \r\n");
        strcpy(diffu->port2,strToken);
        memcpy(&annuaire[nbDiffu],diffu,sizeof(diffuseur));

        nbDiffu+=1;
        printAnnuraire();
        char *mess="REOK\r\n";
        send(sock,mess,strlen(mess),0);
    }
    else {
        char *mess="RENO\r\n";
        send(sock,mess,strlen(mess),0);
        close(sock);   
    }
}

// en fonction du message recu appelle la bonne fonction
void *choixDiscussion(void *arg){
    int sock=*((int *)arg);
    int tailleMessDiffu=SIZE_FORME+1+SIZE_IP+1+
                        SIZE_IP+1+SIZE_PORT+1+SIZE_IP+1+SIZE_PORT+2;
    char newDiffu[tailleMessDiffu+1];
    int recu=recv(sock,newDiffu,tailleMessDiffu*sizeof(char),0);
    newDiffu[recu]='\0';
    printf("Message recu %s",newDiffu);
    if(strstr(newDiffu,"REGI")){
        actionDiffuseur(sock,newDiffu);
    }
    else if(strstr(newDiffu,"LIST")){
        recvClient(sock);
    }
    else {
        close(sock);
    }
    return NULL;
}

// lance les thread, accept les demandes
void choix(int p){
    // connection 
    int sock1=socket(PF_INET,SOCK_STREAM,0);
    struct sockaddr_in address_sock;
    address_sock.sin_family=AF_INET;
    address_sock.sin_port=htons(p);
    address_sock.sin_addr.s_addr=htonl(INADDR_ANY);
    int r=bind(sock1,(struct sockaddr *)&address_sock,sizeof(struct sockaddr_in));

    if(r==0){
        r=listen(sock1,0);
        if (r==0){
            while (1){
                struct sockaddr_in caller;
                socklen_t size=sizeof(caller);
                int *sock2=(int *)malloc(sizeof(int));
                *sock2=accept(sock1,(struct sockaddr *)&caller,&size);
                if(*sock2>=0){
                    pthread_t th1;
                    // printf("nouvelle connection\n");
                    int r1=pthread_create(&th1,NULL,choixDiscussion,sock2);
                    if (r1!=0){
                        perror("Erreur thread utilisateur\n");
                        exit(0);
                    }
                }
                else perror("Erreur accept diffu\n");
            }
        }
        else perror("Erreur du listen diffu\n");
    }
    else perror("Erreur du bind diffu\n");
}

// void demandeClient(int p){
//     // connection 
//     int sock1=socket(PF_INET,SOCK_STREAM,0);
//     struct sockaddr_in address_sock;
//     address_sock.sin_family=AF_INET;
//     address_sock.sin_port=htons(p);
//     address_sock.sin_addr.s_addr=htonl(INADDR_ANY);
//     int r=bind(sock1,(struct sockaddr *)&address_sock,sizeof(struct sockaddr_in));

//     if(r==0){
//         r=listen(sock1,0);
//         if (r==0){
//             while (1){
//                 struct sockaddr_in caller;
//                 socklen_t size=sizeof(caller);
//                 int *sock2=(int *)malloc(sizeof(int));
//                 *sock2=accept(sock1,(struct sockaddr *)&caller,&size);
//                 if(*sock2>=0){
//                     pthread_t th1;
//                     printf("nouvelle connection diffu\n");
//                     int r1=pthread_create(&th1,NULL,recvClient,sock2);
//                     // printf("apres th\n");
//                     if (r1!=0){
//                         perror("Erreur thread utilisateur\n");
//                         exit(0);
//                     }
//                 }
//                 else perror("Erreur accept diffu\n");
//             }
//         }
//         else perror("Erreur du listen diffu\n");
//     }
//     else perror("Erreur du bind diffu\n");
// }


int main(int argc, char**argv){
    if(argc != 2){
        printf("Erreur il faut fournir un numero de port");
        return 0;
    }
    int p=atoi(argv[1]);
    // int pU=atoi(argv[2]);
    choix(p);
    // demandeClient(p);

    // // connection avec diffuseur
    // int sock1=socket(PF_INET,SOCK_STREAM,0);
    // struct sockaddr_in address_sock;
    // address_sock.sin_family=AF_INET;
    // address_sock.sin_port=htons(p);
    // address_sock.sin_addr.s_addr=htonl(INADDR_ANY);
    // int r=bind(sock1,(struct sockaddr *)&address_sock,sizeof(struct sockaddr_in));

    // // int sockU1=socket(PF_INET,SOCK_STREAM,0);
    // // struct sockaddr_in address_sockU;
    // // address_sockU.sin_family=AF_INET;
    // // address_sockU.sin_port=htons(pU);
    // // address_sockU.sin_addr.s_addr=htonl(INADDR_ANY);
    // // int rU=bind(sockU1,(struct sockaddr *)&address_sockU,sizeof(struct sockaddr_in));
    // // int tailleMessDiffu=SIZE_FORME+1+SIZE_IP+1+
    // //                     SIZE_IP+1+SIZE_PORT+1+SIZE_IP+1+SIZE_PORT+2;
    // if(r==0 ){
    //     // printf("bind\n");
    //     r=listen(sock1,0);
    //     // int rU=listen(sock1,0);
    //     if (r==0 ){
    //         // printf("listen\n");
    //         while (1){
    //             // printf("while\n");
    //             struct sockaddr_in callerD;
    //             socklen_t sizeD=sizeof(callerD);
    //             int *sockD2=(int *)malloc(sizeof(int));
    //             *sockD2=accept(sock1,(struct sockaddr *)&callerD,&sizeD);
    //             if(*sockD2>=0){
    //                 pthread_t th1;
    //                 printf("nouvelle connection diffu\n");
    //                 int r1=pthread_create(&th1,NULL,actionDiffuseur,sockD2);
    //                 // printf("apres th\n");
    //                 if (r1!=0){
    //                     perror("Erreur thread utilisateur\n");
    //                     exit(0);
    //                 }
    //                 printf("nouvelle connection client\n");
    //                 // pthread_t th2;
    //                 int r2=pthread_create(&th1,NULL,recvClient,sockD2);
    //                 if (r2!=0){
    //                     printf("Erreur thread diffuseur\n");
    //                     exit(0);
    //                 }
    //             }
    //             // struct sockaddr_in callerU;
    //             // socklen_t sizeU=sizeof(callerU);
    //             // int *sockU2=(int *)malloc(sizeof(int));
    //             // *sockU2=accept(sock1,(struct sockaddr *)&callerU,&sizeU);
    //             // if(*sockU2>=0){
    //             //     printf("nouvelle connection client\n");
    //             //     pthread_t th2;
    //             //     int r2=pthread_create(&th2,NULL,recvClient,sockU2);
    //             //     if (r2!=0){
    //             //         printf("Erreur thread diffuseur\n");
    //             //         exit(0);
    //             //     }
    //                 // pthread_join(th, NULL);


    //             // }
    //             else perror("Erreur accept diffu\n");
    //         }
    //     }
    //     else perror("Erreur du listen diffu\n");
    // }
    // else perror("Erreur du bind diffu\n");


    // pthread_t th;
    // printf("avant th\n");
    // int r1=pthread_create(&th,NULL,actionDiffuseur,&p);
    // printf("apres th\n");
    // if (r1!=0){
    //     printf("Erreur thread utilisateur\n");
    //     exit(0);
    // }
    // int r2=pthread_create(&th,NULL,recvClient,&p);
    // if (r2!=0){
    //     printf("Erreur thread diffuseur\n");
    //     exit(0);
    // }
    // pthread_join(th, NULL);

    // pthread_t thUtilisateur;
    // int r1=pthread_create(&thUtilisateur, NULL, recvClient, &p);
    // if (r1!=0){
    //     printf("Erreur thread utilisateur\n");
    //     exit(0);
    // }
    // pthread_t thDiffuseur;
    // int r2=pthread_create(&thDiffuseur, NULL,actionDiffuseur,&p);
    // if (r2!=0){
    //     printf("Erreur thread diffuseur\n");
    //     exit(0);
    // }

    // pthread_join(th, NULL);

    
    return 0;

}
