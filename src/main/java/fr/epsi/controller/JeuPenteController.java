package fr.epsi.controller;

import fr.epsi.model.ConnectDto;
import fr.epsi.model.PlayDto;
import fr.epsi.model.TurnDto;
import fr.epsi.service.JeuPenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by michael on 11/04/2017.
 */

/**
 * Point d'entrée de l'API du jeu de pente
 */
@RestController
public class JeuPenteController {

    @Autowired
    private JeuPenteService jeuPenteService;

    /**
     * Route pour la méthode de connexion à la partie
     * @param joueurName : nom du joueur
     * @param response
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/connect/{joueurName}", method = RequestMethod.GET)
    public ConnectDto connect(@PathVariable String joueurName, HttpServletResponse response) {
        ConnectDto dto = this.jeuPenteService.connecterJoueur(joueurName);
        System.out.println(dto.toString());
        response.setStatus(dto.getCode());
        return dto;
    }

    /**
     * Route pour la méthode de placement d'un pion sur le plateau de jeu
     * @param x : localisation horizontale du pion
     * @param y : localisation verticale du pion
     * @param idJoueur : id du joueur généré par le serveur à la connexion
     * @param response
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/play/{x}/{y}/{idJoueur}", method = RequestMethod.GET)
    public PlayDto play(@PathVariable Integer x, @PathVariable Integer y, @PathVariable String idJoueur, HttpServletResponse response) {
        PlayDto dto = this.jeuPenteService.placerPion(x, y, idJoueur);
        System.out.println(dto.toString());
        response.setStatus(dto.getCode());
        if (dto.getCode() == 200) {
            this.jeuPenteService.genererTimerFinTour();
        }
        return dto;
    }

    /**
     * Route pour la méthode de récupération du statut de la partie
     * @param idJoueur : id du joueur généré par le serveur à la connexion
     * @param response
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/turn/{idJoueur}", method = RequestMethod.GET)
    public TurnDto turn(@PathVariable String idJoueur, HttpServletResponse response) {
        TurnDto dto = this.jeuPenteService.statutPartieEnCours(idJoueur);
        System.out.println(dto.toString());
        response.setStatus(dto.getCode());
        return dto;
    }
}
