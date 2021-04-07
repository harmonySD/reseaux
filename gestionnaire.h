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

#include "utilisateur.h"

#ifndef GESTIONNAIRE_H
#define GESTIONNAIRE_H

extern void printDiffu(diffuseur diffu);
extern void printAnnuraire();
extern void recvClient(int sock);
extern void actionDiffuseur(int sock,char *newDiffu);
extern void *choixDiscussion(void *arg);
extern void choix(int p);

#endif