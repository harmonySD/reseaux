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

typedef struct{
    char id[8];
    char ip1[15];
    char port1[4];
    char ip2[15];
    char port2[4];
}diffu;

int SIZE_PORT=4;
int SIZE_ID=8;
int SIZE_IP=15;
int SIZE_MESS=140;
diffu annuaire[100];
int nbdiffu=0;

// typedef struct{
//     char port[4];
//     diffu annuaire[100];
// }gestionnaire;


void *recvClient(void *arg){
    int sock=*((int *)arg);
    char list[5];
    int recu=recv(sock,list,4*sizeof(char),0);
    list[recu]='\0';
    // while possible
    if(strcmp(list,"LIST")){
        char numDiff[8];
        strcpy(numDiff,"LINB ");
        // ajout 0 piur nbDiffu
        sprintf(numDiff,"%i",nbdiffu);
        send(sock,numDiff,strlen(numDiff),0);
        for (int i=0;i<nbdiffu;i++){
            char envoieDiffu[4+1+SIZE_ID+1+SIZE_IP+
                1+SIZE_PORT+1+SIZE_IP+1+SIZE_PORT];
            strcpy(envoieDiffu,"ITEM ");
            sprintf(envoieDiffu,"%s %s %s %s %s",
                annuaire[i].id,annuaire[i].ip1,annuaire[i].port1,
                annuaire[i].ip2,annuaire[i].port2);
            send(sock,envoieDiffu,strlen(envoieDiffu),0);
        }
        close(sock);
    }
    else {
        close(sock);
    }
    return NULL;
}

int main(int argc, char**argv){
    if(argc != 2){
        printf("Erreur il faut fournir un numero de port");
        return 0;
    }
    int p=atoi(argv[1]);

    int sock=socket(PF_INET,SOCK_STREAM,0);
    struct sockaddr_in address_sock;
    address_sock.sin_family=AF_INET;
    address_sock.sin_port=htons(p);
    address_sock.sin_addr.s_addr=htonl(INADDR_ANY);
    int r=bind(sock,(struct sockaddr *)&address_sock,sizeof(struct sockaddr_in));
    
    if(r==0){
        r=listen(sock,0);
        if (r==0){
            while (1){
                struct sockaddr_in caller;
                socklen_t size=sizeof(caller);
                int *sock2=(int *)malloc(sizeof(int));
                *sock2=accept(sock,(struct sockaddr *)&caller,&size);
                if(sock2>=0){

                    // Thread avec client
                    pthread_t th;
                    int r2=pthread_create(&th,NULL,recvClient,sock2);
                    if (r2!=0){
                        printf("Erreur thread\n");
                        exit(0);
                    } 
                }
                else printf("Erreur accept\n");
            }
        }
        else printf("Erreur du listen\n");
    }
    else printf("Erreur du bind\n");
    return 0;

}
