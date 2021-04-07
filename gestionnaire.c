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

char *verif_lenght_nb(char *str, int size){
    char *nb=str;
    int s=strlen(str);
    if(s<size){
        char *nb2=NULL;
        nb2=malloc(size);
        //ajoute des 0 de 0 a size-s 
        for(int i=0; i<(size-s);i++){
            nb2[i]='0';
        }
        strcat(nb2,nb);
        nb=nb2;
    }else if(s>size){
        nb="999";
    }
    return nb;
}


// donne la liste des diffuseurs au client
void recvClient(int sock){
    int tailleMessDiffu=SIZE_FORME+1+SIZE_IP+1+
                        SIZE_IP+1+SIZE_PORT+1+SIZE_IP+1+SIZE_PORT+2;
    char messNum[SIZE_FORME+1+2+2+1];
    char numDiff[3];
    sprintf(numDiff,"%i",nbDiffu);
    sprintf(numDiff,"%s",verif_lenght_nb(numDiff,2));
    sprintf(messNum,"LINB %s\r\n",numDiff);
    send(sock,messNum,strlen(messNum),0);
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
        // printAnnuraire();
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

int main(int argc, char**argv){
    if(argc != 2){
        printf("Erreur il faut fournir un numero de port");
        return 0;
    }
    int p=atoi(argv[1]);
    choix(p);
    return 0;

}
