CC=gcc
CFLAGS=-Wall -pthread
LDLIBS=-lm
JC=javac
.SUFFIXES: .java .class
.java.class: 
	$(JC) $*.java

CLASSES =\
		Utilisateur2.java \
		AttendUDP.java \
		AttendTCP.java \
		Cogestionnaire.java

ALL=gestio client Utilisateur2

all: $(ALL)

utilisateur1.o: utilisateur1.c utilisateur.h
	$(CC) $(CFLAGS) -c utilisateur1.c

client:utilisateur1.o 
	$(CC) $(CFLAGS) utilisateur1.o -o client

gestionnaire.o: gestionnaire.c gestionnaire.h
	$(CC) $(CFLAGS) -c gestionnaire.c

gestio: gestionnaire.o 
	$(CC) $(CFLAGS) gestionnaire.o -o gestio


Utilisateur2: $(CLASSES:.java=.class)


cleanall :
	rm -rf *.o main *~

clean:
	rm -rf *~ $(ALL)
	rm -rf *.class