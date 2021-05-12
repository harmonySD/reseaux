#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <fcntl.h>
#include <poll.h>

#include <pthread.h>
#include <time.h>
#include <errno.h>

#include "gestionnaire.h"
#include "utilisateur.h"


// gcc -Wall -pthread -o gestio gestionnaire.c
// scp * chagnons@nivose.informatique.univ-paris-diderot.fr:/info/nouveaux/chagnons/Documents/ProgReseau/Projet

pthread_mutex_t verrou= PTHREAD_MUTEX_INITIALIZER;
diffuseur annuaire[100];
int nbDiffu=0;

void printDiffu(diffuseur diffu){
    printf("id %s, ip1 %s, port1 %s, ip2 %s, port2 %s\n",
        diffu.id,diffu.ip1,diffu.port1,diffu.ip2,diffu.port2);
}

void printAnnuraire(){
    for(int i=0;i<nbDiffu;i++){
        printDiffu(annuaire[i]);
    }
}

// ajout de 0 au debut de str
char *verif_lenght_nb(char *str, int size){
    char *nb = str;
    int size_nb = strlen(str);
    if(size_nb < size){
        char *nb2 = NULL;
        nb2 = malloc(size);
        for(int i = 0; i<(size - size_nb); i++){
            nb2[i] = '0';
        }
        strcat(nb2, nb);
        nb = nb2;
    }
    //si la taille du nombre est plus grande que size 
    //on suppose que cest le max de nombre de diffuseur
    else if(size_nb > size){
        nb="99";
    }
    return nb;
}

// donne la liste des diffuseurs au client
void recvClient(int sock){
    int tailleMessDiffu=SIZE_FORME+1+SIZE_ID+1+
                        SIZE_IP+1+SIZE_PORT+1+SIZE_IP+1+SIZE_PORT+FIN;
    char messNum[SIZE_FORME+1+NUMDIFF+FIN+1];
    // envoie le nombre de message
    char numDiff[3];
    sprintf(numDiff,"%i",nbDiffu);
    sprintf(numDiff,"%s",verif_lenght_nb(numDiff,2));
    sprintf(messNum,"LINB %s\r\n",numDiff);
    send(sock,messNum,strlen(messNum),0);
    // envoie les infos de chaque diffuseur
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
        pthread_mutex_lock(&verrou);

        // ajoute le diffuseur dans l'annuaire
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
        pthread_mutex_unlock(&verrou);
        printf("de ");
        printDiffu(*diffu);
        printf("\n");
        char *mess="REOK\r\n";
        send(sock,mess,strlen(mess),0);

        // verifie si le diffuseur diffuse toujours
        miseAJour(sock, diffu);
    }
    else {
        char *mess="RENO\r\n";
        send(sock,mess,strlen(mess),0);
        close(sock);   
    }
}

// demande si le diffuseur diffuse toujours 
// et si non, l'enleve de la liste annuaire
void miseAJour(int sock,diffuseur *diffu){
    int drapeau = 1;
    struct timeval tv;
    tv.tv_sec=15;
    tv.tv_usec=0;
    while(drapeau){
        // le gestionnnaire envoie RUOK toutes les 30s
        sleep(30);
        char *mess="RUOK\r\n";
        send(sock,mess,strlen(mess),0);
        char imok[SIZE_FORME+FIN+1];

        fcntl(sock,F_SETFL,O_NONBLOCK);
        fd_set initial;
        FD_ZERO(&initial);
        FD_SET(sock,&initial);
        fd_set rdfs;
        memcpy(&rdfs,&initial,sizeof(fd_set));
        // le diffuseur a 15s pour repondre
        int ret=select(sock+1,&rdfs,NULL,NULL,&tv);
        while(ret>0){
            if(FD_ISSET(sock,&rdfs)){
                int rec=recv(sock,imok,SIZE_FORME+3,0);
                if(rec>=0){
                    imok[rec]='\0';
                }
                printf("Message recu %s",imok);
                printf("de ");
                printDiffu(*diffu);
                printf("\n");
                ret--;
            }
        }
        // reponse incorrecte ou trop longue
        if(strcmp(imok,"IMOK\r\n")!=0){
            enleverDiffu(diffu);
            close(sock);
            drapeau=0;
        }
        strcpy(imok,"");
    }
}

// enlever le diffuseur de la liste (annuaire)
void enleverDiffu(diffuseur *diffu){
    char id[9];
    int i=0;
    strcpy(id,annuaire[i].id);
    // trouver le diffuseur a enlever
    while(strcmp(id,diffu->id)!=0){
        i++;
        strcpy(id,annuaire[i].id);
    }
    // decaler tous les diffuseurs de 1 cran
    for(int j=i; j<nbDiffu;j++){
        pthread_mutex_lock(&verrou);
        memcpy(&annuaire[j],&annuaire[j+1],sizeof(diffuseur));
        pthread_mutex_unlock(&verrou);
    }
    pthread_mutex_lock(&verrou);
    nbDiffu-=1;
    pthread_mutex_unlock(&verrou);
}

// envoie de mess a tous les diffuseurs de annuaire
void mallDiffu(int sock, char *mess){
    char *messR="RALL\r\n";
    send(sock,messR,strlen(messR),0);
    close(sock);

    for(int i=0;i<nbDiffu;i++){
        // se connecte en tant que client et envoie au diffuseur
        struct sockaddr_in adress_sock;
        adress_sock.sin_family = AF_INET;
        int p = atoi(annuaire[i].port2);
        adress_sock.sin_port = htons(p);
        inet_aton(annuaire[i].ip2,&adress_sock.sin_addr);

        int sockD=socket(PF_INET,SOCK_STREAM,0);
        int r=connect(sockD,(struct sockaddr *)&adress_sock,
                sizeof(struct sockaddr_in));
        if(r!=-1){
            send(sockD,mess,strlen(mess),0);

            char rall[SIZE_FORME+1+FIN];
            int size_rec=recv(sockD,rall,sizeof(char)*(SIZE_FORME+1+FIN),0);
            rall[size_rec]='\0';
            printf("Message recu %s",rall);
            printf("de ");
            printDiffu(annuaire[i]);
            printf("\n");
        }
    }
}

// en fonction du message recu appelle la bonne fonction
void *choixDiscussion(void *arg){
    int sock=*((int *)arg);
    int tailleMessDiffu=SIZE_FORME + 1 + SIZE_ID + 1 + 
            SIZE_MESS + FIN;
    char newDiffu[tailleMessDiffu+1];
    int recu=recv(sock,newDiffu,tailleMessDiffu*sizeof(char),0);
    newDiffu[recu]='\0';
    printf("Message recu %s",newDiffu);

    // enregistremetn d'un diffuseur
    if(newDiffu[0]=='R' && newDiffu[1]=='E' && 
        newDiffu[2]=='G' && newDiffu[3]=='I' &&
        newDiffu[recu-1]=='\n' &&
        newDiffu[recu-2]=='\r' ){
        actionDiffuseur(sock,newDiffu);
    }
    // list des diffuseur au client
    else if(strcmp(newDiffu,"LIST\r\n")==0){
        recvClient(sock);
    }
    // message du client a transmettre a tous les diffuseurs
    else if(newDiffu[0]=='M' && newDiffu[1]=='A' && 
        newDiffu[2]=='L' && newDiffu[3]=='L' &&
        newDiffu[recu-1]=='\n' &&
        newDiffu[recu-2]=='\r'){
        mallDiffu(sock,newDiffu);
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
                    pthread_t th;
                    int r1=pthread_create(&th,NULL,choixDiscussion,sock2);
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

// main
int main(int argc, char**argv){
    if(argc != 2){
        printf("Erreur il faut fournir un numero de port");
        return 0;
    }
    int p=atoi(argv[1]);
    choix(p);
    return 0;
}