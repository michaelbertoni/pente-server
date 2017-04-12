package fr.epsi.controller;

import fr.epsi.model.ConnectDto;
import fr.epsi.model.PlayDto;
import fr.epsi.model.TurnDto;
import fr.epsi.service.JeuPenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by micha on 11/04/2017.
 */
@RestController
public class JeuPenteController {

    @Autowired
    private JeuPenteService jeuPenteService;

    @RequestMapping(value = "/connect/{joueurName}", method = RequestMethod.GET)
    public ConnectDto connect(@PathVariable String joueurName, HttpServletResponse response) {
        ConnectDto dto = this.jeuPenteService.connecterJoueur(joueurName);
        response.setStatus(dto.getCode());
        return dto;
    }

    @RequestMapping(value = "/play/{x}/{y}/{idJoueur}", method = RequestMethod.GET)
    public PlayDto play(@PathVariable Integer x, @PathVariable Integer y, @PathVariable String idJoueur, HttpServletResponse response) {
        PlayDto dto = this.jeuPenteService.placerPion(x, y, idJoueur);
        response.setStatus(dto.getCode());
        return dto;
    }

    @RequestMapping(value = "/turn/{idJoueur}", method = RequestMethod.GET)
    public TurnDto turn(@PathVariable String idJoueur, HttpServletResponse response) {
        TurnDto dto = this.jeuPenteService.statusPartieEnCours(idJoueur);
        response.setStatus(dto.getCode());
        return dto;
    }
}
