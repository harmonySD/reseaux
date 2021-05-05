CC=gcc
CFLAGS=-Wall -pthread
LDLIBS=-lm

ALL=gestio client

all: $(ALL)

utilisateur1.o: utilisateur1.c utilisateur.h
	$(CC) $(CFLAGS) -c utilisateur1.c

gestionnaire.o: gestionnaire.c gestionnaire.h
	$(CC) $(CFLAGS) -c gestionnaire.c

client:utilisateur1.o 
	$(CC) $(CFLAGS) utilisateur1.o -o client

gestio: gestionnaire.o 
	$(CC) $(CFLAGS) gestionnaire.o -o gestio



cleanall :
	rm -rf *.o main *~

clean:
	rm -rf *~ $(ALL)