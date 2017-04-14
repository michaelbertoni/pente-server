package fr.epsi.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Timer;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by micha on 10/04/2017.
 */

/**
 * Singleton de la partie de pente
 */
@Getter
@Setter
public class JeuPente {

    private static JeuPente instance = new JeuPente();

    public static JeuPente getInstance() {
        return instance;
    }

    private JeuPente() {
        setPremierJoueur(ThreadLocalRandom.current().nextInt(0, 2));
        setEtat(Etat.ATTENTE);
        setNumTour(0);
        setTableau(new int[19][19]);
        setCoups(new TreeMap<>());
    }

    private Joueur joueur1;

    private Joueur joueur2;

    private int premierJoueur; // 1:premier joueur à rejoindre, 2:deuxième joueur à rejoindre

    private Joueur tour; // user du tour en cours

    private Etat etat;

    private boolean prolongtation;

    private int numTour; // compteur de tour

    private Joueur gagnant;

    private TypeVictoire typeVictoire;

    private int[][] tableau;

    private TreeMap<Integer,Integer> coups; // historique des coups joués

    private Timer timerProlongation;

    private Timer timerTour;

    public void incrementerTour() {
        numTour++;
    }

}
