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

int SIZE_ID=8;
int SIZE_MESS=140;

typedef struct{
    char port[4];
    diffu annuaire[100];
}gestionnaire;

typedef struct{
    char id[8];
    char ip1[15];
    char port1[4];
    char ip2[15];
    char port2[4];
}diffu;


int main(int argc, char**argv){
    if(argc != 2){
        printf("Erreur il faut fournir un numero de port puis un pseudo ! ");
        return 0;
    }


}
