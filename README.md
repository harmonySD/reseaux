# NetRadio
## projet de programmation reseaux

### EQUIPE 15
- Chagnon Sarah, n° etu: **71800911** 
- Sabri Alexandre, n° etu: **21986486**
- Simon-Duchatel, n° etu: **71802485**

### Compilation et exécution 

```
> make
```
***pour le gestionnaire :***
```
>./gestio port
```
*Avec* 
- `port` : port du gestionnaire 

***pour le diffuseur***
Utilisez le Script Shell Diffuseur.sh, contentez vous simplement de renseigner les variables au début du fichier
puis de l'exécuter APRES le lancement du gestionnaire(sinon pas de communcation avec le gestionnaire)
***pour le gestionnaire :***
```
>./Diffuseur.sh
```

***pour l'utilisateur***

*le client en C faire :*

```
>./client port adressegestio tty pseudo 
```
*Avec*
- `port` : port d'un gestionnaire
- `adressgestio` : adresse d'un gestionnaire *(sur les machines de l'ufr mettre 70 pas 070)*
- `tty`: lien d'un fichier **IO** d'un autre terminale, pour y afficher les messages recu d'un diffuseur
- `pseudo` : le pseudo de votre choix 
***
Pour envoyer un message taper `MESS`, puis `Entrez` puis votre message !

Pour demander l'historique taper `LAST`, puis `Entrez` puis le nombre de messages voulu !
****

*le client en Java faire :*
```
>java Utilisateur2 pseudo
```
*Avec*

- `pseudo` : le pseudo de votre choix 
****
Choix a faire entre gestionnaire ou diffuseur.
- Si diffuseur taper `diffuseur` puis les informations concernant ce diffuseur.
- Si  gestionnaire taper `gestionnaire` puis un port, l'addresse ou se trouve le gestionnaire *(sur les machines de l'ufr mettre 070 pas 70)*. Pour envoyer un message au gestionnaire afin qu'il le diffuse a tous ses diffuseurs, repondre `y`. Sinon repondre `n` pour avoir la liste des diffuseurs enregistrés.


Pour envoyer un message taper `MESS`, puis `Entrez` puis votre message !

Pour demander l'historique taper `LAST`, puis `Entrez` puis le nombre de messages voulu !

Pour quitter un diffuseur taper `q`!
****

***
***Diffuseur***

`Utilisation du Diffuseur`

le Diffuseur est complètement autonome dans son utilisation. 
Une fois démarré il gère automatiquement 
- les connexions de client
- les connexions de Gestionnaire pour MALL

Il supporte la connexion concurrentielle de plusieurs clients/ gestionnaire pour MALL, LAST, MESS.

***Gestionnnaire***

`Utilisation du Gestionnaire`

le Gestionnaire est complètement autonome dans son utilisation. 
Une fois démarré il gère automatiquement 
- les connexions de client
- les connexions de diffuseur

Il supporte la connexion concurrentielle de plusieurs clients et diffuseurs pour MALL, LIST, REGI.

***Utilisateur***

En C:

Le client est semi interactif. Une fois démarré il gère automatiquement 
- la connexion au gestionnnaire
- la connexion a un diffuseur (choisis aléatoirement)


En java : 

Le client est totalement intéractif.


