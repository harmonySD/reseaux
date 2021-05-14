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

#define BSLASH 1
#define FIN 2
#define NBMESS 3
#define NUMMESS 4
#define NUMDIFF 2
#define  SM 4
#define SIZE_FORME 4
#define SIZE_PORT 4
#define SIZE_ID 8
#define SIZE_IP 15
#define SIZE_MESS 140
#define ESP 1

extern char *verif_lenght(char *str, int size);
extern char *verif_lenght_nb(char *str, int size);
extern void verif_ip(char *ip,char *ipv);
extern diffuseur connection_gestionnaire(char *argv,char *add);
extern void *sendMessage(void *sock_desc);
extern void connection_diffuseur(char *port1, char *ip1, char *port2, char *ip2, char *id, char *tty);

#endif