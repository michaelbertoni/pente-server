package fr.epsi.service;

import fr.epsi.entity.Game;
import fr.epsi.entity.GameState;
import fr.epsi.entity.Player;
import fr.epsi.entity.WinType;
import fr.epsi.model.ConnectDto;
import fr.epsi.model.GameDto;
import org.apache.tomcat.jni.Local;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by micha on 10/04/2017.
 */
@Service
public class GameService {

    private static SecureRandom random = new SecureRandom();

    public ConnectDto connectPlayer(String joueurName) {
        Game game = Game.getInstance();
        ConnectDto dto = new ConnectDto();
        if (game.getEtat() == GameState.FIN) {
            resetGame();
        }

        if (game.getPlayer1() != null && game.getPlayer2() != null) {
            dto.setCode(401);
            return dto;
        }

        Player newPlayer = null;
        if (game.getPlayer1() == null) {
            newPlayer = new Player(generatePlayerId(), joueurName, 0);
            game.setPlayer1(newPlayer);
            dto.setNumJoueur(1);
        } else if (game.getPlayer2() == null) {
            newPlayer = new Player(generatePlayerId(), joueurName, 0);
            game.setPlayer2(newPlayer);
            dto.setNumJoueur(2);

            // commencer la partie
            game.setEtat(GameState.ENC);
            game.setNumTour(1);
            game.setTour(game.getFirstPlayer() == 0 ? game.getPlayer1() : game.getPlayer2());

            // Timer de fin de tour
            genererTimerDeFinDeTour();

            // Timer de fin de partie
            Timer timerPartie = new Timer();
            timerPartie.schedule(taskMortSubite(), 1000 * 60 * 10); // 10 minutes
            game.setTimerPartie(timerPartie);
        }

        dto.setCode(200);
        dto.setIdJoueur(newPlayer.getId());
        dto.setNomJoueur(newPlayer.getNom());

        return dto;
    }

    private void genererTimerDeFinDeTour() {
        Game game = Game.getInstance();
        Timer timerTour = new Timer();
        timerTour.schedule(taskTourManque(), 1000 * 10); // 10 secondes
        game.setTimerTour(timerTour);
    }

    private TimerTask taskTourManque() {
        Game game = Game.getInstance();
        return new TimerTask() {
            @Override
            public void run() {
                game.setEtat(GameState.FIN);
                game.setTypeVictoire(WinType.TOUR_MANQUE);
                game.setGagnant(game.getPlayer1() == game.getTour() ? game.getPlayer1() : game.getPlayer2());
                game.getTimerPartie().cancel();
            }
        };
    }

    private TimerTask taskMortSubite() {
        Game game = Game.getInstance();
        return new TimerTask() {
            @Override
            public void run() {
                game.setProlongtation(true);
            }
        };
    }

    private void resetGame() {
        Game newGame = Game.getInstance();
        newGame.setFirstPlayer(ThreadLocalRandom.current().nextInt(0, 2));
        newGame.setPlayer1(null);
        newGame.setPlayer2(null);
        newGame.setTour(null);
        newGame.setEtat(GameState.ATT);
        newGame.setProlongtation(false);
        newGame.setNumTour(0);
        newGame.setGagnant(null);
        newGame.setTypeVictoire(null);
        newGame.setTableau(new int[19][19]);
        newGame.setCoups(new TreeMap<>());
    }

    private GameDto getTurn(String idJoueur) {
        Game game = Game.getInstance();
        GameDto dto = new GameDto();
        if (game.getPlayer1() == null || game.getPlayer2() == null) {
            dto.setCode(503);
            return dto;
        }
        if (game.getPlayer1().getId() != idJoueur && game.getPlayer2().getId() != idJoueur) {
            dto.setCode(401);
            return dto;
        }

        dto.setCode(200);
        dto.setStatus(game.getTour().getId() == idJoueur ? 1 : 0);
        dto.setTableau(game.getTableau());
        dto.setNbTenaillesJ1(game.getPlayer1().getNbTenaille());
        dto.setNbTenaillesJ2(game.getPlayer2().getNbTenaille());
        dto.setDernierCoupX(game.getCoups().lastEntry().getKey());
        dto.setDernierCoupY(game.getCoups().lastEntry().getValue());
        dto.setProlongation(game.isProlongtation());
        dto.setFinPartie(game.getEtat() == GameState.FIN);

        if (dto.isFinPartie()) {
            if (game.getTypeVictoire().equals(WinType.TENAILLE)) {
                if (game.isProlongtation()) {
                    dto.setDetailFinPartie(game.getGagnant().getNom() + " a gagné la partie avec une tenaille en mort subite !");
                } else {
                    dto.setDetailFinPartie(game.getGagnant().getNom() + " a gagné la partie avec 5 tenailles !");
                }
            } else if (game.getTypeVictoire().equals(WinType.PENTE)) {
                if (game.isProlongtation()) {
                    dto.setDetailFinPartie(game.getGagnant().getNom() + " a gagné la partie avec une pente en mort subite !");
                } else {
                    dto.setDetailFinPartie(game.getGagnant().getNom() + " a gagné la partie avec une pente !");
                }
            } else if (game.getTypeVictoire().equals(WinType.TOUR_MANQUE)) {
                Player perdant = game.getGagnant() == game.getPlayer1() ? game.getPlayer2() : game.getPlayer1();
                dto.setDetailFinPartie(game.getGagnant().getNom() + " a gagné car " + perdant.getNom() + " n'a pas joué en 10 secondes !");
            }
        }

        dto.setNumTour(game.getNumTour());
        return dto;
    }

    public Integer placerPion(Integer x, Integer y, String idJoueur) {
        Game game = Game.getInstance();
        if (game.getPlayer1() == null || game.getPlayer2() == null) {
            return 503;
        }
        if (game.getPlayer1().getId() != idJoueur && game.getPlayer2().getId() != idJoueur) {
            return 401;
        }
        if (game.getTour().getId() != idJoueur) {
            return 401;
        }
        if (game.getEtat() != GameState.ENC) {
            return 401;
        }

        Player player = game.getTour();
        if (miseAjourTableau(x - 1, y - 1, player)) {
            game.getTimerTour().cancel();
            genererTimerDeFinDeTour();
            return 200;
        } else {
            return 406;
        }
    }

    private boolean miseAjourTableau(int x, int y, Player player) {
        Game game = Game.getInstance();
        // x hors tableau
        if (x > game.getTableau().length || x < 0) {
            return false;
        }

        // y hors tableau
        if (y > game.getTableau()[x].length || y < 0) {
            return false;
        }

        // pion déjà présent aux coordonnées x y
        if (game.getTableau()[x][y] != 0) {
            return false;
        }

        // Premier tour ? pion au centre
        if (game.getNumTour() == 1 && x != 9 && y != 9) {
            return false;
        }

        // Troisième tour ? pion pas dans le centre 3 * 3
        if (game.getNumTour() == 3 && ((x > 6 && x < 12) && (y > 6 && y < 12))) {
            return false;
        }

        int numPlayer = player == game.getPlayer1() ? 1 : 2;
        game.getTableau()[x][y] = numPlayer;

        // Vérification pente
        int nbPionsHorizontalAlignes = 0;
        int nbPionsVerticalAlignes = 0;
        int nbPionsDiagonal1Alignes = 0;
        int nbPionsDiagonal2Alignes = 0;
        for (int i = -4; i < 5; i++) {
            if (game.getTableau()[x + i][y] == numPlayer) {
                nbPionsHorizontalAlignes++;
            } else {
                nbPionsHorizontalAlignes = 0;
            }
            if (game.getTableau()[x][y + i] == numPlayer) {
                nbPionsVerticalAlignes++;
            } else {
                nbPionsVerticalAlignes = 0;
            }
            if (game.getTableau()[x + i][y + i] == numPlayer) {
                nbPionsDiagonal1Alignes++;
            } else {
                nbPionsDiagonal1Alignes = 0;
            }
            if (game.getTableau()[x + i][y - i] == numPlayer) {
                nbPionsDiagonal2Alignes++;
            } else {
                nbPionsDiagonal2Alignes = 0;
            }

            // Pente réalisée ?
            if (nbPionsHorizontalAlignes == 5 || nbPionsVerticalAlignes == 5 || nbPionsDiagonal1Alignes == 5 || nbPionsDiagonal2Alignes == 5) {
                endGame(player, WinType.PENTE);
                break;
            }
        }

        // Vérification tenailles
        int numOtherPlayer = numPlayer == 1 ? 2 : 1;
        int nbTenaillesRealisees = 0;
        //// gauche
        int pionGauche1 = game.getTableau()[x - 1][y];
        int pionGauche2 = game.getTableau()[x - 2][y];
        int pionGauche3 = game.getTableau()[x - 3][y];
        if (pionGauche1 == numOtherPlayer && pionGauche2 == numOtherPlayer && pionGauche3 == numPlayer) {
            nbTenaillesRealisees++;
        }

        //// gauche haut
        int pionGaucheHaut1 = game.getTableau()[x - 1][y - 1];
        int pionGaucheHaut2 = game.getTableau()[x - 2][y - 2];
        int pionGaucheHaut3 = game.getTableau()[x - 3][y - 3];
        if (pionGaucheHaut1 == numOtherPlayer && pionGaucheHaut2 == numOtherPlayer && pionGaucheHaut3 == numPlayer) {
            nbTenaillesRealisees++;
        }

        //// haut
        int pionHaut1 = game.getTableau()[x][y - 1];
        int pionHaut2 = game.getTableau()[x][y - 2];
        int pionHaut3 = game.getTableau()[x][y - 3];
        if (pionHaut1 == numOtherPlayer && pionHaut2 == numOtherPlayer && pionHaut3 == numPlayer) {
            nbTenaillesRealisees++;
        }

        //// droite haut
        int pionDroiteHaut1 = game.getTableau()[x + 1][y - 1];
        int pionDroiteHaut2 = game.getTableau()[x + 2][y - 2];
        int pionDroiteHaut3 = game.getTableau()[x + 3][y - 3];
        if (pionDroiteHaut1 == numOtherPlayer && pionDroiteHaut2 == numOtherPlayer && pionDroiteHaut3 == numPlayer) {
            nbTenaillesRealisees++;
        }

        //// droite
        int pionDroite1 = game.getTableau()[x + 1][y];
        int pionDroite2 = game.getTableau()[x + 2][y];
        int pionDroite3 = game.getTableau()[x + 3][y];
        if (pionDroite1 == numOtherPlayer && pionDroite2 == numOtherPlayer && pionDroite3 == numPlayer) {
            nbTenaillesRealisees++;
        }

        //// droite bas
        int pionDroiteBas1 = game.getTableau()[x + 1][y + 1];
        int pionDroiteBas2 = game.getTableau()[x + 2][y + 2];
        int pionDroiteBas3 = game.getTableau()[x + 3][y + 3];
        if (pionDroiteBas1 == numOtherPlayer && pionDroiteBas2 == numOtherPlayer && pionDroiteBas3 == numPlayer) {
            nbTenaillesRealisees++;
        }

        //// bas
        int pionBas1 = game.getTableau()[x][y + 1];
        int pionBas2 = game.getTableau()[x][y + 2];
        int pionBas3 = game.getTableau()[x][y + 3];
        if (pionBas1 == numOtherPlayer && pionBas2 == numOtherPlayer && pionBas3 == numPlayer) {
            nbTenaillesRealisees++;
        }

        //// gauche bas
        int pionGaucheBas1 = game.getTableau()[x - 1][y + 1];
        int pionGaucheBas2 = game.getTableau()[x - 2][y + 2];
        int pionGaucheBas3 = game.getTableau()[x - 3][y + 3];
        if (pionGaucheBas1 == numOtherPlayer && pionGaucheBas2 == numOtherPlayer && pionGaucheBas3 == numPlayer) {
            nbTenaillesRealisees++;
        }

        player.setNbTenaille(player.getNbTenaille() + nbTenaillesRealisees);

        if (player.getNbTenaille() > 4 || (game.isProlongtation() && nbTenaillesRealisees > 0)) {
            endGame(player, WinType.TENAILLE);
        }

        return true;
    }

    private void endGame(Player player, WinType typeVictoire) {
        Game game = Game.getInstance();
        game.getTimerTour().cancel();
        game.getTimerPartie().cancel();
        game.setGagnant(player);
        game.setEtat(GameState.FIN);
        game.setTypeVictoire(typeVictoire);
    }

    private String generatePlayerId() {
        char[] CHARSET_AZ_09 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        char[] charResult = new char[8];
        for (int i = 0; i < charResult.length; i++) {
            // picks a random index out of character set > random character
            int randomCharIndex = random.nextInt(CHARSET_AZ_09.length);
            charResult[i] = CHARSET_AZ_09[randomCharIndex];
        }
        String result = new String(charResult);

        if (Game.getInstance().getPlayer1().getId().equals(result) || Game.getInstance().getPlayer2().getId().equals(result)) {
            return generatePlayerId();
        }
        return result;
    }
}
