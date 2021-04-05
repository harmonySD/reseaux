CC=gcc
CFLAGS=-Wall -g
LDLIBS=-lm

ALL=gestio

all: $(ALL)

gestio: gestionnaire.o gestionnaire.o
	gcc -pthread -o gestio gestionnaire.o

gestionnaire.o: gestionnaire.c gestionnaire.h
	gcc -o gestionnaire.o -c gestionnaire.c

cleanall :
	rm -rf *.o main *~

clean:
	rm -rf *~ $(ALL)