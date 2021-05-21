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
***pour le diffuseur***
```
> Utilisez le Script Shell Diffuseur.sh, contentez vous simplement de renseigner les variables au début du fichier
> puis de l'exécuter APRES le lancement du gestionnaire(sinon pas de communcation avec le gestionnaire)
```
***pour le gestionnaire :***
```
>./gestio port
```
*Avec* 
- `port` : port du gestionnaire 

***pour l'utilisateur***

*le client en C faire :*

```
>./client port tty pseudo 
```
*Avec*
- `port` : port d'un gestionnaire
- `tty`: lien d'un fichier **IO** d'un autre terminale, pour y afficher les messages recu d'un diffuseur
- `pseudo` : le pseudo de votre choix 
***
Pour envoyer un message taper `MESS` puis votre message !

Pour demander l'historique taper `LAST` puis le nombre de messages voulu !
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
- Si  gestionnaire taper `gestionnaire` puis un port.

Pour envoyer un message taper `MESS`puis votre message !

Pour demander l'historique taper `LAST`puis le nombre de messages voulu !

Pour quitter un diffuseur taper `q`!
****

***
***Diffuseur***
`Utilisation du Diffuseur`
le Diffuseur est complètement autonome dans son utilisation. 
Une fois démarré il gère automatiquement 
\- les connexions de client
\- les connexions de Gestionnaire pour MALL
\- 
Il supporte la connexion concurrentielle de plusieurs clients/ gestionnaire pour MALL, LAST, MESS.
***Gestionnnaire***

***Utilisateur***

`connection_gestionnaire`

Se connecte a un gestionnaire (*grâce au port donner en argument*), et demande la liste des diffuseur.
Enregistre toutes les informations recues sur les diffuseurs dans la structure `diffu`. Puis choisis *aleatoirement* un diffuseur parmis la liste de structure. 
Renvoie une structure avec les info sur le diffuseur choisi.

`connection_diffuseur`

Se connecte a un diffuseur, creation d'un thread pour l'interaction entre le client et le diffuseur. Pendant ce temps les messages recu du diffuseur sont afficher dans le terminale donner par tty. 
