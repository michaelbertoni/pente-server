package fr.epsi.entity;

import lombok.Getter;
import lombok.Setter;
import org.apache.tomcat.jni.Local;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TreeMap;

/**
 * Created by micha on 10/04/2017.
 */
@Getter
@Setter
public class Game {

    private static Game instance = new Game();

    public static Game getInstance() {
        return instance;
    }

    private Game() {
    }

    private Player player1;

    private Player player2;

    private int firstPlayer; // 1:premier joueur à rejoindre, 2:deuxième joueur à rejoindre

    private Player tour; // user du tour en cours

    private GameState etat;

    private boolean prolongtation;

    private int numTour;

    private Player gagnant;

    private WinType typeVictoire;

    private int[][] tableau;

    private TreeMap<Integer,Integer> coups;

    private Timer timerPartie;

    private Timer timerTour;

}
