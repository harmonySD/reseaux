CC=gcc
CFLAGS=-Wall -g
LDLIBS=-lm

ALL=gestio client

all: $(ALL)

client:utilisateur.o 
	gcc -pthread -o client utilisateur.o

utilisateur.o: utilisateur1.c utilisateur.h
	gcc -o utilisateur.o -c utilisateur1.c

gestio: gestionnaire.o 
	gcc -pthread -o gestio gestionnaire.o

gestionnaire.o: gestionnaire.c gestionnaire.h utilisateur.h
	gcc -o gestionnaire.o -c gestionnaire.c


cleanall :
	rm -rf *.o main *~

clean:
	rm -rf *~ $(ALL)