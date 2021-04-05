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

#ifndef GESTIONNAIRE_H
#define GESTIONNAIRE_H

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

extern void printDiffu(diffuseur diffu);
extern void printAnnuraire();
extern char *verif_lenght_nb(char *str, int size);
extern void recvClient(int sock);
extern void actionDiffuseur(int sock,char *newDiffu);
extern void *choixDiscussion(void *arg);
extern void choix(int p);

#endif