package fr.epsi.controller;

import fr.epsi.model.ConnectDto;
import fr.epsi.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by micha on 11/04/2017.
 */
@RestController
public class GameController {

    @Autowired
    private GameService gameService;

    @RequestMapping(value = "/connect/{joueurName}", method = RequestMethod.GET)
    public ConnectDto connect(@PathVariable String joueurName) {
        return this.gameService.connectPlayer(joueurName);
    }
}
