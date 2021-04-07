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
>
```
***pour le gestionnaire :***
```
>./gestionnaire port
```
*Avec* 
- `port` : port du gestionnaire 

***pour l'utilisateur***
```
>./utilisateur port tty pseudo 
```
*Avec*
- `port` : port d'un gestionnaire
- `tty`: lien d'un fichier **IO** d'un autre terminale, pour y afficher les messages recu d'un diffuseur
- `pseudo` : le pseudo de votre choix 

***
***Diffuseur***

***Gestionnnaire***

***Utilisateur***

`connection_gestionnaire`

Se connecte a un gestionnaire (*grâce au port donner en argument*), et demande la liste des diffuseur.
Enregistre toutes les informations recues sur les diffuseurs dans la structure `diffu`. Puis choisis *aleatoirement* un diffuseur parmis la liste de structure. 
Renvoie une structure avec les info sur le diffuseur choisi.

`connection_diffuseur`

Se connecte a un diffuseur, creation d'un thread pour l'interaction entre le client et le diffuseur. Pendant ce temps les messages recu du diffuseur sont afficher dans le terminale donner par tty. 
