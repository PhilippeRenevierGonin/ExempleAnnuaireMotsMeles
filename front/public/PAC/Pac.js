class MESSAGE {
    static SELECTION_MOT = "selection mot";
    static MOT_TAPE = "mot tapé";
    static SELECTION_CASE = "section des cases";
    static INIT ="initialisation";
    static AJOUTER_LIGNE = "ajouter une ligne de span";
    static FIN_INIT = "initialisation finie";
    static LISTE = "liste de mots";
    static MOT_TROUVE = "mot trouvé";
    static MOT_NON_TROUVE = "mot qui n'a pas été trouvé";
    static CLEAR = "remettre à zéro";
    static BARRER_SELECTION = "barrer la sélection courante";
    static SELECTION_MOT_PAR_CLIC = "sélection de mots par clic";
    static VERIF_SELECTION = "vérification de la selection de mots";
    static RESULTAT_VERIF = "le résultat de la vérification de la sélection du mot";
}

class Abs {
    setCtrl(ctrl) {
        this.ctrl = ctrl;
    }

    reçoitMessage(message, piecejointe) {
        console.error("reçoitMessage de Abs pas encore implémentée : "+message);
    }
}



class Pres {
    setCtrl(ctrl) {
        this.ctrl = ctrl;

    }

    reçoitMessage(message, piecejointe) {
        console.error("reçoitMessage de Pres pas encore implémentée : "+message);
    }

}


class Ctrl  {
    constructor(abs, pres) {
        this.abs = abs;
        this.abs.setCtrl(this);
        this.pres = pres;
        this.pres.setCtrl(this);

        this.parent = null;
        this.enfants = [];
    }

    reçoitMessageDeLAbstraction(message, piecejointe) {
        console.error("reçoitMessageDeLAbstraction non impl : "+message);
    }

    reçoitMessageDUnEnfant(message, piecejointe, ctrl) {
        console.error("reçoitMessageDUnEnfant non impl : "+message);
    }

    reçoitMessageDuParent(message, piecejointe) {
        console.error("reçoitMessageDuParent non impl : "+message);
    }

    reçoitMessageDeLaPresentation(message, piecejointe) {
        console.error("reçoitMessageDeLaPresentation non impl : "+message);
    }

    addEnfant(controleur) {
        this.enfants.push(controleur);
        controleur.setParent(this);
    }

    removeEnfant(controleur) {
        this.enfants = this.enfants.filter(pac => pac !== controleur);
    }

    setParent(controleur) {
        this.parent = controleur;
    }

}
