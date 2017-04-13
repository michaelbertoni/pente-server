package fr.epsi.service;

import fr.epsi.entity.JeuPente;
import fr.epsi.entity.Etat;
import fr.epsi.entity.Joueur;
import fr.epsi.entity.TypeVictoire;
import fr.epsi.model.ConnectDto;
import fr.epsi.model.PlayDto;
import fr.epsi.model.TurnDto;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by micha on 10/04/2017.
 */
@Service
public class JeuPenteService {

    private static SecureRandom random = new SecureRandom();

    public ConnectDto connecterJoueur(String nomJoueur) {
        JeuPente jeuPente = JeuPente.getInstance();
        ConnectDto dto = new ConnectDto();
        // Si la partie est terminée et qu'un joueur se connecte, démarrer une nouvelle partie
        if (jeuPente.getEtat() == Etat.FIN) {
            resetGame();
        }

        if (jeuPente.getJoueur1() != null && jeuPente.getJoueur2() != null) {
            dto.setCode(401);
            return dto;
        }

        Joueur newJoueur = null;
        if (jeuPente.getJoueur1() == null) {
            newJoueur = new Joueur(genererIdJoueur(), nomJoueur, 0);
            jeuPente.setJoueur1(newJoueur);
            dto.setNumJoueur(1);
        } else if (jeuPente.getJoueur2() == null) {
            newJoueur = new Joueur(genererIdJoueur(), nomJoueur, 0);
            jeuPente.setJoueur2(newJoueur);
            dto.setNumJoueur(2);

            // commencer la partie
            jeuPente.setEtat(Etat.EN_COURS);
            jeuPente.setTour(jeuPente.getPremierJoueur() == 0 ? jeuPente.getJoueur1() : jeuPente.getJoueur2());
            genererTimerFinTour();

            // Timer de fin de partie
            Timer timerPartie = new Timer();
            timerPartie.schedule(taskMortSubite(), 1000 * 60 * 10); // 10 minutes
            jeuPente.setTimerProlongation(timerPartie);
        }

        dto.setCode(200);
        dto.setIdJoueur(newJoueur.getId());
        dto.setNomJoueur(newJoueur.getNom());

        return dto;
    }

    public void genererTimerFinTour() {
        JeuPente jeuPente = JeuPente.getInstance();
        stopTimerFinTour();
        Timer timerTour = new Timer();
        timerTour.schedule(taskTourManque(), 1000 * 10 * 10); // 10 secondes
        System.out.println("Timer de fin de tour : start " + LocalDateTime.now().toString());
        jeuPente.setTimerTour(timerTour);
    }

    private void stopTimerFinTour() {
        JeuPente jeuPente = JeuPente.getInstance();
        if (jeuPente.getTimerTour() != null) {
            jeuPente.getTimerTour().cancel();
            System.out.println("Timer de fin de tour : stop " + LocalDateTime.now().toString());
        }
    }

    private TimerTask taskTourManque() {
        JeuPente jeuPente = JeuPente.getInstance();
        return new TimerTask() {
            @Override
            public void run() {
                System.out.println("Timer de fin de tour : terminé ! " + LocalDateTime.now().toString());
                jeuPente.setEtat(Etat.FIN);
                jeuPente.setTypeVictoire(TypeVictoire.TOUR_MANQUE);
                jeuPente.setGagnant(jeuPente.getJoueur1() == jeuPente.getTour() ? jeuPente.getJoueur2() : jeuPente.getJoueur1());
                jeuPente.getTimerProlongation().cancel();
            }
        };
    }

    private TimerTask taskMortSubite() {
        JeuPente jeuPente = JeuPente.getInstance();
        return new TimerTask() {
            @Override
            public void run() {
                jeuPente.setProlongtation(true);
            }
        };
    }

    private void resetGame() {
        JeuPente jeuPente = JeuPente.getInstance();
        jeuPente.setPremierJoueur(ThreadLocalRandom.current().nextInt(0, 2));
        jeuPente.setJoueur1(null);
        jeuPente.setJoueur2(null);
        jeuPente.setTour(null);
        jeuPente.setEtat(Etat.ATTENTE);
        jeuPente.setProlongtation(false);
        jeuPente.setNumTour(0);
        jeuPente.setGagnant(null);
        jeuPente.setTypeVictoire(null);
        jeuPente.setTableau(new int[19][19]);
        jeuPente.setCoups(new TreeMap<>());
    }

    public TurnDto statusPartieEnCours(String idJoueur) {
        JeuPente jeuPente = JeuPente.getInstance();
        TurnDto dto = new TurnDto();
        if (jeuPente.getJoueur1() == null || jeuPente.getJoueur2() == null) {
            dto.setCode(503);
            return dto;
        }
        if (jeuPente.getJoueur1().getId().equals(idJoueur) && jeuPente.getJoueur2().getId().equals(idJoueur)) {
            dto.setCode(401);
            return dto;
        }

        dto.setCode(200);
        dto.setStatus(jeuPente.getTour().getId().equals(idJoueur) ? 1 : 0);
        dto.setTableau(jeuPente.getTableau());
        dto.setNbTenaillesJ1(jeuPente.getJoueur1().getNbTenaille());
        dto.setNbTenaillesJ2(jeuPente.getJoueur2().getNbTenaille());
        if (!jeuPente.getCoups().isEmpty()) {
            dto.setDernierCoupX(jeuPente.getCoups().lastEntry().getKey());
            dto.setDernierCoupY(jeuPente.getCoups().lastEntry().getValue());
        }
        dto.setProlongation(jeuPente.isProlongtation());
        dto.setFinPartie(jeuPente.getEtat() == Etat.FIN);

        if (dto.isFinPartie()) {
            if (jeuPente.getTypeVictoire() == TypeVictoire.TENAILLE) {
                if (jeuPente.isProlongtation()) {
                    dto.setDetailFinPartie(jeuPente.getGagnant().getNom() + " a gagné la partie avec une tenaille en mort subite !");
                } else {
                    dto.setDetailFinPartie(jeuPente.getGagnant().getNom() + " a gagné la partie avec 5 tenailles !");
                }
            } else if (jeuPente.getTypeVictoire() == TypeVictoire.PENTE) {
                if (jeuPente.isProlongtation()) {
                    dto.setDetailFinPartie(jeuPente.getGagnant().getNom() + " a gagné la partie avec une pente en mort subite !");
                } else {
                    dto.setDetailFinPartie(jeuPente.getGagnant().getNom() + " a gagné la partie avec une pente !");
                }
            } else if (jeuPente.getTypeVictoire() == TypeVictoire.TOUR_MANQUE) {
                Joueur perdant = jeuPente.getGagnant() == jeuPente.getJoueur1() ? jeuPente.getJoueur2() : jeuPente.getJoueur1();
                dto.setDetailFinPartie(jeuPente.getGagnant().getNom() + " a gagné car " + perdant.getNom() + " n'a pas joué en 10 secondes !");
            }
        }

        dto.setNumTour(jeuPente.getNumTour());
        return dto;
    }

    public PlayDto placerPion(Integer x, Integer y, String idJoueur) {
        JeuPente jeuPente = JeuPente.getInstance();
        // joueurs créés ?
        if (jeuPente.getJoueur1() == null || jeuPente.getJoueur2() == null) {
            return new PlayDto(503);
        }

        // idJoueur non reconnu
        if (!(jeuPente.getJoueur1().getId().equals(idJoueur) || jeuPente.getJoueur2().getId().equals(idJoueur))) {
            return new PlayDto(401);
        }

        // pas le tour du joueur
        if (!jeuPente.getTour().getId().equals(idJoueur)) {
            return new PlayDto(401);
        }

        // partie pas en cours
        if (jeuPente.getEtat() != Etat.EN_COURS) {
            return new PlayDto(401);
        }

        // x hors tableau
        if (x > jeuPente.getTableau().length || x < 0) {
            return new PlayDto(406);
        }

        // y hors tableau
        if (y > jeuPente.getTableau()[x].length || y < 0) {
            return new PlayDto(406);
        }

        // pion déjà présent aux coordonnées x y
        if (jeuPente.getTableau()[x][y] != 0) {
            return new PlayDto(406);
        }

        // Premier tour ? pion au centre
        if (jeuPente.getNumTour() == 0 && !(x == 9 && y == 9)) {
            return new PlayDto(406);
        }

        // Troisième tour ? pion pas dans le centre 3 * 3
        if (jeuPente.getNumTour() == 2 && ((x > 5 && x < 13) && (y > 5 && y < 13))) {
            return new PlayDto(406);
        }

        stopTimerFinTour();

        Joueur joueur = jeuPente.getTour();
        miseAjourTableau(x, y, joueur);
        jeuPente.setTour(joueur == jeuPente.getJoueur1() ? jeuPente.getJoueur2() : jeuPente.getJoueur1());
        jeuPente.incrementerTour();
        return new PlayDto(200);
    }

    private void miseAjourTableau(int x, int y, Joueur joueur) {
        JeuPente jeuPente = JeuPente.getInstance();

        int numPlayer = joueur == jeuPente.getJoueur1() ? 1 : 2;
        jeuPente.getTableau()[x][y] = numPlayer;

        // Vérification pente
        int nbPionsHorizontalAlignes = 0;
        int nbPionsVerticalAlignes = 0;
        int nbPionsDiagonal1Alignes = 0;
        int nbPionsDiagonal2Alignes = 0;
        for (int i = -4; i < 5; i++) {
            if (x + i >= 0 && x + i <= 18) {
                if (jeuPente.getTableau()[x + i][y] == numPlayer) {
                    nbPionsHorizontalAlignes++;
                } else {
                    nbPionsHorizontalAlignes = 0;
                }
            }
            if (y + i >= 0 && y + i <= 18) {
                if (jeuPente.getTableau()[x][y + i] == numPlayer) {
                    nbPionsVerticalAlignes++;
                } else {
                    nbPionsVerticalAlignes = 0;
                }
            }
            if (x + i >= 0 && x + i <= 18 && y + i >= 0 && y + i <= 18) {
                if (jeuPente.getTableau()[x + i][y + i] == numPlayer) {
                    nbPionsDiagonal1Alignes++;
                } else {
                    nbPionsDiagonal1Alignes = 0;
                }
            }
            if (x + i >= 0 && x + i <= 18 && y - i >= 0 && y - i <= 18) {
                if (jeuPente.getTableau()[x + i][y - i] == numPlayer) {
                    nbPionsDiagonal2Alignes++;
                } else {
                    nbPionsDiagonal2Alignes = 0;
                }
            }

            // Pente réalisée ?
            if (nbPionsHorizontalAlignes == 5 || nbPionsVerticalAlignes == 5 || nbPionsDiagonal1Alignes == 5 || nbPionsDiagonal2Alignes == 5) {
                endGame(joueur, TypeVictoire.PENTE);
            }
        }

        // Vérification tenailles
        int numOtherPlayer = numPlayer == 1 ? 2 : 1;
        int nbTenaillesRealisees = 0;
        //// gauche
        int pionGauche1 = x - 1 >= 0 && x - 1 <= 18 ? jeuPente.getTableau()[x - 1][y] : 0;
        int pionGauche2 = x - 2 >= 0 && x - 2 <= 18 ? jeuPente.getTableau()[x - 2][y] : 0;
        int pionGauche3 = x - 3 >= 0 && x - 3 <= 18 ? jeuPente.getTableau()[x - 3][y] : 0;
        if (pionGauche1 == numOtherPlayer && pionGauche2 == numOtherPlayer && pionGauche3 == numPlayer) {
            jeuPente.getTableau()[x - 1][y] = 0;
            jeuPente.getTableau()[x - 2][y] = 0;
            nbTenaillesRealisees++;
        }

        //// gauche haut
        int pionGaucheHaut1 = x - 1 >= 0 && x - 1 <= 18 && y - 1 >= 0 && y - 1 <= 18 ? jeuPente.getTableau()[x - 1][y - 1] : 0;
        int pionGaucheHaut2 = x - 2 >= 0 && x - 2 <= 18 && y - 2 >= 0 && y - 2 <= 18 ? jeuPente.getTableau()[x - 2][y - 2] : 0;
        int pionGaucheHaut3 = x - 3 >= 0 && x - 3 <= 18 && y - 3 >= 0 && y - 3 <= 18 ? jeuPente.getTableau()[x - 3][y - 3] : 0;
        if (pionGaucheHaut1 == numOtherPlayer && pionGaucheHaut2 == numOtherPlayer && pionGaucheHaut3 == numPlayer) {
            jeuPente.getTableau()[x - 1][y - 1] = 0;
            jeuPente.getTableau()[x - 2][y - 2] = 0;
            nbTenaillesRealisees++;
        }

        //// haut
        int pionHaut1 = y - 1 >= 0 && y - 1 <= 18 ? jeuPente.getTableau()[x][y - 1] : 0;
        int pionHaut2 = y - 2 >= 0 && y - 2 <= 18 ? jeuPente.getTableau()[x][y - 2] : 0;
        int pionHaut3 = y - 3 >= 0 && y - 3 <= 18 ? jeuPente.getTableau()[x][y - 3] : 0;
        if (pionHaut1 == numOtherPlayer && pionHaut2 == numOtherPlayer && pionHaut3 == numPlayer) {
            jeuPente.getTableau()[x][y - 1] = 0;
            jeuPente.getTableau()[x][y - 2] = 0;
            nbTenaillesRealisees++;
        }

        //// droite haut
        int pionDroiteHaut1 = x + 1 >= 0 && x + 1 <= 18 && y - 1 >= 0 && y - 1 <= 18 ? jeuPente.getTableau()[x + 1][y - 1] : 0;
        int pionDroiteHaut2 = x + 2 >= 0 && x + 2 <= 18 && y - 2 >= 0 && y - 2 <= 18 ? jeuPente.getTableau()[x + 2][y - 2] : 0;
        int pionDroiteHaut3 = x + 3 >= 0 && x + 3 <= 18 && y - 3 >= 0 && y - 3 <= 18 ? jeuPente.getTableau()[x + 3][y - 3] : 0;
        if (pionDroiteHaut1 == numOtherPlayer && pionDroiteHaut2 == numOtherPlayer && pionDroiteHaut3 == numPlayer) {
            jeuPente.getTableau()[x + 1][y - 1] = 0;
            jeuPente.getTableau()[x + 2][y - 2] = 0;
            nbTenaillesRealisees++;
        }

        //// droite
        int pionDroite1 = x + 1 >= 0 && x + 1 <= 18 ? jeuPente.getTableau()[x + 1][y] : 0;
        int pionDroite2 = x + 2 >= 0 && x + 2 <= 18 ? jeuPente.getTableau()[x + 2][y] : 0;
        int pionDroite3 = x + 3 >= 0 && x + 3 <= 18 ? jeuPente.getTableau()[x + 3][y] : 0;
        if (pionDroite1 == numOtherPlayer && pionDroite2 == numOtherPlayer && pionDroite3 == numPlayer) {
            jeuPente.getTableau()[x + 1][y] = 0;
            jeuPente.getTableau()[x + 2][y] = 0;
            nbTenaillesRealisees++;
        }

        //// droite bas
        int pionDroiteBas1 = x + 1 >= 0 && x + 1 <= 18 && y + 1 >= 0 && y + 1 <= 18 ? jeuPente.getTableau()[x + 1][y + 1] : 0;
        int pionDroiteBas2 = x + 2 >= 0 && x + 2 <= 18 && y + 2 >= 0 && y + 2 <= 18 ? jeuPente.getTableau()[x + 2][y + 2] : 0;
        int pionDroiteBas3 = x + 3 >= 0 && x + 3 <= 18 && y + 3 >= 0 && y + 3 <= 18 ? jeuPente.getTableau()[x + 3][y + 3] : 0;
        if (pionDroiteBas1 == numOtherPlayer && pionDroiteBas2 == numOtherPlayer && pionDroiteBas3 == numPlayer) {
            jeuPente.getTableau()[x + 1][y + 1] = 0;
            jeuPente.getTableau()[x + 2][y + 2] = 0;
            nbTenaillesRealisees++;
        }

        //// bas
        int pionBas1 = y + 1 >= 0 && y + 1 <= 18 ? jeuPente.getTableau()[x][y + 1] : 0;
        int pionBas2 = y + 2 >= 0 && y + 2 <= 18 ? jeuPente.getTableau()[x][y + 2] : 0;
        int pionBas3 = y + 3 >= 0 && y + 3 <= 18 ? jeuPente.getTableau()[x][y + 3] : 0;
        if (pionBas1 == numOtherPlayer && pionBas2 == numOtherPlayer && pionBas3 == numPlayer) {
            jeuPente.getTableau()[x][y + 1] = 0;
            jeuPente.getTableau()[x][y + 2] = 0;
            nbTenaillesRealisees++;
        }

        //// gauche bas
        int pionGaucheBas1 = x - 1 >= 0 && x - 1 <= 18 && y + 1 >= 0 && y + 1 <= 18 ? jeuPente.getTableau()[x - 1][y + 1] : 0;
        int pionGaucheBas2 = x - 2 >= 0 && x - 2 <= 18 && y + 2 >= 0 && y + 2 <= 18 ? jeuPente.getTableau()[x - 2][y + 2] : 0;
        int pionGaucheBas3 = x - 3 >= 0 && x - 3 <= 18 && y + 3 >= 0 && y + 3 <= 18 ? jeuPente.getTableau()[x - 3][y + 3] : 0;
        if (pionGaucheBas1 == numOtherPlayer && pionGaucheBas2 == numOtherPlayer && pionGaucheBas3 == numPlayer) {
            jeuPente.getTableau()[x - 1][y + 1] = 0;
            jeuPente.getTableau()[x - 2][y + 2] = 0;
            nbTenaillesRealisees++;
        }

        joueur.setNbTenaille(joueur.getNbTenaille() + nbTenaillesRealisees);

        // Conditions victoire tenailles
        if (joueur.getNbTenaille() > 4 || (jeuPente.isProlongtation() && nbTenaillesRealisees > 0)) {
            endGame(joueur, TypeVictoire.TENAILLE);
        }

        // Ajout du coup dans l'historique
        jeuPente.getCoups().put(x, y);
    }

    private void endGame(Joueur joueur, TypeVictoire typeVictoire) {
        JeuPente jeuPente = JeuPente.getInstance();
        stopTimerFinTour();
        jeuPente.getTimerProlongation().cancel();
        jeuPente.setGagnant(joueur);
        jeuPente.setEtat(Etat.FIN);
        jeuPente.setTypeVictoire(typeVictoire);
    }

    private String genererIdJoueur() {
        char[] CHARSET_AZ_09 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        char[] charResult = new char[8];
        for (int i = 0; i < charResult.length; i++) {
            // picks a random index out of character set > random character
            int randomCharIndex = random.nextInt(CHARSET_AZ_09.length);
            charResult[i] = CHARSET_AZ_09[randomCharIndex];
        }
        String result = new String(charResult);

        JeuPente jeuPente = JeuPente.getInstance();
        if ((jeuPente.getJoueur1() != null && jeuPente.getJoueur1().getId().equals(result))
                || (jeuPente.getJoueur2() != null && jeuPente.getJoueur2().getId().equals(result))) {
            return genererIdJoueur();
        }
        return result;
    }
}
