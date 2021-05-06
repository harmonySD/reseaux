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

#include "utilisateur.h"

#ifndef GESTIONNAIRE_H
#define GESTIONNAIRE_H

extern void printDiffu(diffuseur diffu);
extern void printAnnuraire();
extern char *verif_lenght_nb(char *str, int size);
extern void recvClient(int sock);
extern void actionDiffuseur(int sock,char *newDiffu);
extern void miseAJour(int sock,diffuseur *diffu);
extern void enleverDiffu(diffuseur *diffu);
extern void mallDiffu(int sock, char *mess);
extern void *choixDiscussion(void *arg);
extern void choix(int p);

#endif