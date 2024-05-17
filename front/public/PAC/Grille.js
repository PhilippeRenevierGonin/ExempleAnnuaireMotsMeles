class Lettre {
    constructor(valeur, indice) {
        this.valeur = valeur;
        this.indice = indice;
    }
}

class AbsGrille extends Abs {


    static NODIRECTION = -1;

    static NORD = 0;
    static NORDEST = 1;
    static EST = 2;
    static SUDEST = 3;
    static SUD = 4;
    static SUDOUEST = 5;
    static OUEST = 6;
    static NORDOUEST = 7;
    static DIRECTIONS = [AbsGrille.NORD, AbsGrille.NORDEST, AbsGrille.EST, AbsGrille.SUDEST, AbsGrille.SUD, AbsGrille.SUDOUEST, AbsGrille.OUEST, AbsGrille.NORDOUEST];

    constructor() {
        super();
        this.taille = 0;
        this.lettres = [];
    }


    /**
     * méthode pour recevoir les messages du controleur
     */
    reçoitMessage(message, piecejointe) {
        let result = "";
        if (message == MESSAGE.SELECTION_MOT) {
            if (piecejointe.length > 0) {
                result = this.getToutesLesPossibilites(piecejointe);
            }

        }
        else if (message == MESSAGE.INIT) {
            let grilleInitiale="";
            if (piecejointe && piecejointe.cases) {
                for (let i = 0; i < piecejointe.cases.length; i++) {
                    for (let j = 0; j < piecejointe.cases[i].length; j++) {
                        grilleInitiale = grilleInitiale+piecejointe.cases[i][j];
                    }
                    grilleInitiale+"\n";
                }
            } else {
                grilleInitiale=`trukkanaksktcob
            paresseruopyvos
            saneveuezutueil
            cchbennkrpageul
            rdojepmbcejuifx
            awunomarepandue
            mvrpredominance
            prrvasemetteurs
            ebasurveillants
            rueuqrotxechaos
            aaaaaaaaaaaaaaa
            aaaaaaaaaaaaaaa
            aaaaaaaaaaaaaaa
            aaaaaaaaaaaaaaa
            aaaaaaaaaaaaaaa`;
            }

            let grilles = grilleInitiale.split("\n").map(s => s.trim()).join("");
            let lettres = [];
            for (let i = 0; i < grilles.length; i++) {

                lettres.push(grilles.charAt(i).toUpperCase());
            }
            this.setLettres(lettres);
        }
        else if (message == MESSAGE.VERIF_SELECTION) {
            result = this.verification(piecejointe);
        }
        else {
            result = super.reçoitMessage(message, piecejointe);
        }
        return result;
    }


    /**
     *  le nombre de lettres doit être un carré (4,9,16,25, etc.)
     *  à appeler que si l'abstraction est associée à un contrôleur
     */
    setLettres(listeDeLettres) {
        if (this.ctrl) {
            this.lettres = [];
            for (let i = 0; i < listeDeLettres.length; i++) {
                this.lettres.push(new Lettre(listeDeLettres[i], i));
            }
            this.taille = Math.sqrt(this.lettres.length);

            for(let ligne = 0; ligne < this.taille; ligne++) {
                let ligneDeMots = [];
                for(let col = 0; col < this.taille; col++) {
                    ligneDeMots.push(listeDeLettres[ligne*this.taille+col]);
                }
                this.ctrl.reçoitMessageDeLAbstraction(MESSAGE.AJOUTER_LIGNE, ligneDeMots, ligne); // ajout de la coordonnée y

            }
            this.ctrl.reçoitMessageDeLAbstraction(MESSAGE.FIN_INIT, this.taille); // pour que le controleur puisse convertir les coordonnées

        }

    }


    /**
     * pour trouver la lettre suivante, en partant d'une lettre dans une direction donnée
     * @param lettre : la lettre d'où on part
     * @param direction : la direction à suivre
     * @return la lettre si elle existe (on ne sort pas du cadre) ou null sinon
     */
    getLettreSuivante(lettre, direction) {
        let i = this.getIndiceSuivant(lettre.indice, direction);
        let result = null;
        if (i >= 0) result = this.lettres[i];
        return result;
    }

    /**
     * pour obtenir l'indice suivant (d'une lettre), en partant dans une direction
     * @param indice
     * @param direction
     * @returns {number} -1 si l'indice n'est pas possible (hors du tableau) ou
     */
    getIndiceSuivant(indice, direction) {
        let indicePotentiel = indice;
        let modulo = indicePotentiel % this.taille;
        let division = Math.floor(indicePotentiel / this.taille);
        switch (direction) {
            case AbsGrille.NORD:
                if (division > 0) indicePotentiel = indicePotentiel - this.taille;
                else indicePotentiel = -1;
                break;
            case AbsGrille.NORDEST:
                if ((division > 0) && (modulo < (this.taille - 1))) {
                    indicePotentiel = indicePotentiel - this.taille + 1;
                } else indicePotentiel = -1;
                break;
            case AbsGrille.EST:
                if (modulo < (this.taille - 1)) {
                    indicePotentiel = indicePotentiel + 1;
                } else indicePotentiel = -1;
                break;
            case AbsGrille.SUDEST:
                if ((modulo < (this.taille - 1)) && (division < (this.taille - 1))) {
                    indicePotentiel = indicePotentiel + this.taille + 1;
                } else indicePotentiel = -1;
                break;
            case AbsGrille.SUD:
                if (division < (this.taille - 1)) {
                    indicePotentiel = indicePotentiel + this.taille;
                } else indicePotentiel = -1;
                break;
            case AbsGrille.SUDOUEST:
                if ((division < (this.taille - 1)) && (modulo > 0)) {
                    indicePotentiel = indicePotentiel + this.taille - 1;
                } else indicePotentiel = -1;
                break;
            case AbsGrille.OUEST:
                if (modulo > 0) {
                    indicePotentiel = indicePotentiel - 1;
                } else indicePotentiel = -1;
                break;
            case AbsGrille.NORDOUEST:
                if ((modulo > 0) && (division > 0)) {
                    indicePotentiel = indicePotentiel - this.taille - 1;
                } else indicePotentiel = -1;
                break;
            default:
                indicePotentiel = -1;
                break;
        }
        return indicePotentiel;
    }


    /**
     * recherche toutes les suites d'indice qui correspondent au mot recherché
     * @param motrecherché
     * @returns {[]} un tableau d'indice s'il n'y a qu'une lettre ou un tableau de tableau d'indice s'il y a 2 lettres ou plus
     */
    getToutesLesPossibilites(motrecherché) {
        let liste = [];
        let premiereLettre = motrecherché.charAt(0);
        let candidats = this.lettres.filter(lettre => lettre.valeur == premiereLettre);

        // pour chaque lettre, il faut explorer toutes les directions
        for (let c = 0; c < candidats.length; c++) {
            let lettreCourante = candidats[c];
            // pour chacune des directions
            if (motrecherché.length > 1) {
                for (let dir = 0; dir < AbsGrille.DIRECTIONS.length; dir++) {

                    lettreCourante = candidats[c];
                    let chemin = [];
                    chemin.push(lettreCourante);

                    let direction = AbsGrille.DIRECTIONS[dir];
                    let motCourant = motrecherché.substring(1);

                    while (motCourant.length > 0) {

                        lettreCourante = this.getLettreSuivante(lettreCourante, direction);
                        if ((lettreCourante) && (lettreCourante.valeur == motCourant.charAt(0))) {
                            chemin.push(lettreCourante);
                        } else {
                            chemin = [];
                            break;

                        }
                        motCourant = motCourant.substring(1);
                    }

                    if (chemin.length > 0) liste.push(chemin)

                }
            } else {
                liste.push(lettreCourante);
            }
        }

        return liste;
    }

    verification(listeCoord) {
        console.log("verif");
        let result = true;

        if (listeCoord.length > 1) {
            // il faut trouver la direction
            let direction = AbsGrille.NODIRECTION;
            let delta = listeCoord[0] - listeCoord[1];
            switch (delta) {
                case 1:             direction = AbsGrille.OUEST;    break;
                case -1:            direction = AbsGrille.EST;      break;
                case this.taille:   direction = AbsGrille.NORD;     break;
                case this.taille-1: direction = AbsGrille.NORDEST;  break;
                case this.taille+1: direction = AbsGrille.NORDOUEST;break;
                case -this.taille:  direction = AbsGrille.SUD;      break;
                case -this.taille-1:direction = AbsGrille.SUDEST;   break;
                case -this.taille+1:direction = AbsGrille.SUDOUEST; break;
                default:            result    = false;
            }
            if (direction != AbsGrille.NODIRECTION) {
                for(let i = 1; i < listeCoord.length; i++) {
                    result = result && (this.getIndiceSuivant(listeCoord[i-1], direction) == listeCoord[i]);
                    if (! result) break;
                }
            }
        }

        return result;
    }
}


class PresGrille extends Pres {
    constructor() {
        super();
        this.lettres = null;

        this.input = document.createElement("input");
        this.input.id = "mot";

        let nav = document.createElement("nav");
        nav.appendChild(this.input);
        document.body.appendChild(nav);

        this.grille = document.createElement("div");
        this.grille.id = "motsmeles"
        document.body.appendChild(this.grille);

        this.nouveauMot = () => { // l'evenement n'interesse pas
            // todo, si spanSelectionnesParClic n'est pas vide, il faut la vider
            this.clear();
            let mot = this.input.value.toUpperCase();
            if (mot.length > 0) this.ctrl.reçoitMessageDeLaPresentation(MESSAGE.MOT_TAPE, mot);
        };

        this.spanSelectionnesParClic = [];
        /* this.selectionParClic = (e) => {
            // todo si spanSelectionnesParClic est vide et si input avec du texte, clear
            console.log(e.target.textContent);
            let span = e.target;
            this.spanSelectionnesParClic.push(span);
            this.ctrl.reçoitMessageDeLaPresentation(MESSAGE.SELECTION_MOT_PAR_CLIC, this.spanSelectionnesParClic);
        }; */
    }

    reçoitMessage(message, piecejointe) {
        let result = "";
        if (message == MESSAGE.AJOUTER_LIGNE) {
            this.contruireLigne(piecejointe, arguments[2]); // le 3e param, c'est le n° de la ligne
        }
        else if (message == MESSAGE.FIN_INIT) {
            // ajout de listener que quand on est pret
            this.lettres = document.querySelectorAll("div#motsmeles > div > span");
            this.input.addEventListener("keyup", this.nouveauMot);
        }
        else if (message == MESSAGE.SELECTION_CASE) {
            if (piecejointe.length > 0) {
                this.select(piecejointe);
            }
        }
        else if (message == MESSAGE.BARRER_SELECTION) {
            let spanSelectionnes = document.querySelectorAll("div#motsmeles > div > span.selected");
            for(let i = 0; i < spanSelectionnes.length; i++) {
                let span = spanSelectionnes.item(i);
                if (! span.classList.contains("trouve"))  span.classList.toggle("trouve");
            }
        }
        else if (message == MESSAGE.RESULTAT_VERIF) {
            console.log("pres : messsage reçu "+message+" / "+piecejointe+" / "+this.spanSelectionnesParClic.length);
            let valide = piecejointe;
            if (valide) {
                this.selectSpan(this.spanSelectionnesParClic);
            }
            else {
                this.clear();
            }
        }
        else if (message == MESSAGE.CLEAR) {
            this.clear();
            this.input.value="";
        }
        else {
            result = super.reçoitMessage(message, piecejointe);
        }

        return result;
    }


    clear() {
        this.spanSelectionnesParClic = [];  // pour avoir un reset complet
        this.lettres.forEach(function (span) {
            if (span.classList.contains("selected")) span.classList.toggle("selected");
        });
    }

    select(liste) {
        liste.forEach((indiceSpan) => {
            if (! this.lettres.item(indiceSpan).classList.contains("selected")) this.lettres.item(indiceSpan).classList.toggle("selected");
        });
    }

    selectSpan(listeSpan) {
        listeSpan.forEach((span) => {
            console.log("on selecte "+span.textContent);
            if (! span.classList.contains("selected")) span.classList.toggle("selected");
        });
    }

    contruireLigne(mot, nbLigne) {
        let ligne = document.createElement("div");
        for(let i = 0; i < mot.length; i++) {
            let span = document.createElement("span");
            span.coordGrilleY = nbLigne;
            span.coordGrillex = i;
            span.innerHTML=mot[i].toUpperCase();
            // span.addEventListener("click", this.selectionParClic);
            ligne.appendChild(span);
        }
        this.grille.appendChild(ligne);
    }
}


class CtrlGrille extends Ctrl{
    constructor(abs, pres) {
        super(abs, pres);
    }

    reçoitMessageDeLAbstraction(message, piecejointe) {
        if (message == MESSAGE.AJOUTER_LIGNE) {
            this.pres.reçoitMessage(MESSAGE.AJOUTER_LIGNE, piecejointe, arguments[2]);  // le 3e param, c'est le n° de la ligne
        } else if (message == MESSAGE.FIN_INIT) {
            this.tailleLigne = piecejointe;
            this.pres.reçoitMessage(MESSAGE.FIN_INIT);
        } else super.reçoitMessageDeLAbstraction(message, piecejointe);
    }

    reçoitMessageDuParent(message, piecejointe) {
        if (message == MESSAGE.INIT) {
            this.abs.reçoitMessage(message, piecejointe);
        }
        else if (message == MESSAGE.MOT_TROUVE) {
            // this.pres.reçoitMessage(MESSAGE.BARRER_SELECTION);
            // this.pres.reçoitMessage(MESSAGE.CLEAR);
        }
        else super.reçoitMessageDuParent(message, piecejointe);
    }

    reçoitMessageDeLaPresentation(message, piecejointe) {
        if (message == MESSAGE.MOT_TAPE) {
            this.nouvelleSelection(piecejointe);
            this.parent.reçoitMessageDUnEnfant(message, piecejointe, this);
        } else if (message == MESSAGE.SELECTION_MOT_PAR_CLIC) {
            let selection = piecejointe;
            let listCoord = [];
            selection.forEach(span => {
                let num = span.coordGrillex+span.coordGrilleY*this.tailleLigne;
                listCoord.push(num);
            });
            let valide = this.abs.reçoitMessage(MESSAGE.VERIF_SELECTION, listCoord);
            this.pres.reçoitMessage(MESSAGE.RESULTAT_VERIF, valide);
            if (valide) {
                let mot = "";
                selection.forEach(span => {
                    mot = mot+span.textContent;
                });
                this.parent.reçoitMessageDUnEnfant(MESSAGE.MOT_TAPE, mot, this);
            }

        }
        else super.reçoitMessageDeLaPresentation(message, piecejointe);
    }


    nouvelleSelection(mot) {
        let listes = this.abs.reçoitMessage(MESSAGE.SELECTION_MOT, mot);
        let indicesDesCases = [];
        for (let i = 0; i < listes.length; i++) {
            // indicesDesCases[i] est un tableau ou un int
            let chemin = listes[i];
            if (Array.isArray(chemin)) {
                for(let j = 0; j < chemin.length; j++) indicesDesCases.push(chemin[j].indice);
            }
            else {
                indicesDesCases.push(chemin.indice);
            }
        }
        this.pres.reçoitMessage(MESSAGE.SELECTION_CASE, indicesDesCases);
    }
}
