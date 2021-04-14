CC=gcc
CFLAGS=-Wall -g
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


client:utilisateur.o 
	gcc -pthread -o client utilisateur.o

utilisateur.o: utilisateur1.c utilisateur.h
	gcc -o utilisateur.o -c utilisateur1.c
	

gestio: gestionnaire.o 
	gcc -pthread -o gestio gestionnaire.o

gestionnaire.o: gestionnaire.c gestionnaire.h utilisateur.h
	gcc -o gestionnaire.o -c gestionnaire.c

Utilisateur2: $(CLASSES:.java=.class)


cleanall :
	rm -rf *.o main *~

clean:
	rm -rf *~ $(ALL)
	rm -rf *.class