class AbsCiment extends Abs {
    constructor() {
        super();
        this.nbTouchesTapees = 0;
    }

    /**
     * un seul message est traité par l'abstraction de ciment : MOT_TAPE, pour compter le nombre de touches tapées
     * @param message : le message traité est MESSAGE.MOT_TAPE
     * @param piecejointe : non utilisé ici
     * @returns {*} si le message est le bon, retourne le nombre de tentative
     */
    reçoitMessage(message, piecejointe) {
        let result = "";
        if (message == MESSAGE.MOT_TAPE) {
            this.nbTouchesTapees += 1;
            result = this.nbTouchesTapees;
        }
        else {
            result = super.reçoitMessage(message, piecejointe);
        }
        return result;
    }
}


class PresCiment extends Pres {
    constructor() {
        super();
        this.spanCpt = document.createElement("span");
        this.spanCpt.innerHTML = "-";
        let aside = document.createElement("aside");
        // ici on peut mettre du style, ou alors dans le css
        aside.innerHTML="Nb touche(s) tapée(s) : ";
        aside.appendChild(this.spanCpt);
        document.body.appendChild(aside);
    }

    /**
     * La présentation de Ciment ne fait que mettre à jour le nombre de tentative, soit le texte de la span qui a été crée dans le constructeur
     * @param message le message traité est MESSAGE.MOT_TAPE
     * @param piecejointe : contient le nombre de tentative
     * @returns {*} (ne retourne rien)
     */
    reçoitMessage(message, piecejointe) {
        let result = "";
        if (message == MESSAGE.MOT_TAPE) {
            this.spanCpt.innerHTML = piecejointe;
            result = piecejointe;
        }
        else {
            result = super.reçoitMessage(message, piecejointe);
        }
        return result;
    }

}

class CtrlCiment extends Ctrl {
    constructor(abs, pres) {
        super(abs, pres);
    }

    /**
     * pour lancer l'initialisation dans la hierarchie
     */
    init(grille) {
        this.enfants.forEach(e => e.reçoitMessageDuParent(MESSAGE.INIT, grille));
    }

    /**
     * Aiguillage des messages entre les enfants de Ciment
     * @param message : 2 messages sont traités MESSAGE.MOT_TAPE (de grille vers listemots) et MESSAGE.MOT_TROUVE (de listemots vers grille)
     * @param piecejointe : le mot tapé pour MESSAGE.MOT_TAPE ; et rien pour MESSAGE.MOT_TROUVE
     * @param ctrl : l'enfant qui appelle reçoitMessageDUnEnfant
     */
    reçoitMessageDUnEnfant(message, piecejointe, ctrl) {
        if (message == MESSAGE.MOT_TAPE) {
            let nb = this.abs.reçoitMessage(message, piecejointe);
            this.pres.reçoitMessage(message, nb);
            this.enfants.forEach(e => {
                if(e != ctrl) e.reçoitMessageDuParent(MESSAGE.MOT_TAPE, piecejointe);
            } );

        }
        else if (message == MESSAGE.MOT_TROUVE) {
            // todo factoriser transmettreAuxEnfants
            this.enfants.forEach(e => {
                if(e != ctrl) e.reçoitMessageDuParent(MESSAGE.MOT_TROUVE, piecejointe);
            } );
        }
        else super.reçoitMessageDUnEnfant(message, piecejointe);
    }
}
