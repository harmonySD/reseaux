NOM_Diffuseur="LeSuperDif" # Nom du diffuseur
MODE_LOCAL="true" # à mettre à true si le client et le Diffuseur se trouvent sur la même machine sinon écrire false 
ADRESSE_MULTICAST="225.10.20.30"
PORT_MULTICAST=5678 
PORT_TCP=1234 #port utilisé pour ceux qui veulent se connecter en TCP au diffuseur
ADRESSE_GESTIONNAIRE="127.0.0.1" # Adresse du gestionnaire
PORT_GESTIONNAIRE="4444" #port pour se connecter au gestionnare
CHEMIN_FICHIER_MESSAGES="phrasesdiffuseur" #chemin vers un fichier contenant au MINIMUM 220 lignes de message à envoyer, 1 ligne par message 





java Diffuseur $NOM_Diffuseur $MODE_LOCAL $PORT_TCP $PORT_MULTICAST $ADRESSE_MULTICAST  $ADRESSE_GESTIONNAIRE $PORT_GESTIONNAIRE <  $CHEMIN_FICHIER_MESSAGES
