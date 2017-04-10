package fr.epsi.service;

import fr.epsi.entity.Game;
import fr.epsi.entity.GameState;
import fr.epsi.entity.Player;
import fr.epsi.entity.Tenaille;
import fr.epsi.model.GameDto;
import fr.epsi.repository.GameRepository;
import jersey.repackaged.com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by micha on 10/04/2017.
 */
@Service
public class GameService {

    @Inject
    private GameRepository gameRepository;

    private static SecureRandom random = new SecureRandom();

    public GameDto map(Game game) {
        GameDto dto = new GameDto();
        dto.setTour(game.getTour().getId());
        dto.setEtat(game.getEtat());
        if (game.getGagnant() != null) {
            dto.setGagnant(game.getGagnant().getId());
        }
        dto.setGrille(game.getGrille());

        HashMap<String,Integer> tenailles = new HashMap<>();
        game.getTenailles().forEach(t -> {
            tenailles.put(t.getPlayer().getNom(), t.getScore());
        });

        dto.setTenailles(tenailles);
        dto.setMaj(Instant.now().getEpochSecond());
        return dto;
    }

    public void createGame(String firstPlayerName) {
        Game newGame = new Game();
        newGame.setToken(generateGameId());
        newGame.setEtat(GameState.ATT);
        newGame.setGrille(new ArrayList<>());
        newGame.setPlayers(Arrays.asList(new Player(firstPlayerName)));
        newGame.setTenailles(new ArrayList<>());
        this.gameRepository.save(newGame);
    }

    public void joinGame(String secondPlayerName, String token) {

    }

    private String generateGameId() {
        char[] CHARSET_AZ_09 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        char[] charResult = new char[8];
        for (int i = 0; i < charResult.length; i++) {
            // picks a random index out of character set > random character
            int randomCharIndex = random.nextInt(CHARSET_AZ_09.length);
            charResult[i] = CHARSET_AZ_09[randomCharIndex];
        }
        String result = new String(charResult);

        if (this.gameRepository.findAll().stream().map(Game::getToken).anyMatch(s -> s.equals(result))) {
            return generateGameId();
        }

        return result;
    }

}
