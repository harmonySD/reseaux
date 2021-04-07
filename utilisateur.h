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
#include <fcntl.h>

#ifndef UTILISATEUR_H
#define UTILISATEUR_H

typedef struct{
    char id[9];
    char ip1[16];
    char port1[5];
    char ip2[16];
    char port2[5];
}diffuseur;

int BSLASH = 1;
int FIN = 2;
int NBMESS = 3;
int NUMMESS = 4;
int NUMDIFF = 2;
int  SM = 4;
int SIZE_FORME=4;
int SIZE_PORT=4;
int SIZE_ID=8;
int SIZE_IP=15;
int SIZE_MESS=140;
int ESP = 1;

extern char *verif_lenght(char *str, int size);
extern char *verif_lenght_nb(char *str, int size);
extern diffuseur connection_gestionnaire(char *argv);
extern void *sendMessage(void *sock_desc);
extern void connection_diffuseur(char *port1, char *ip1, char *port2, char *ip2, char *id, char *tty);

#endif