package fr.epsi.model;

import fr.epsi.entity.GameState;
import fr.epsi.entity.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

/**
 * Created by micha on 10/04/2017.
 */
@Getter
@Setter
public class GameDto {

    private Integer tour;

    private GameState etat;

    private Integer gagnant;

    private List<List<Integer>> grille;

    private HashMap<String,Integer> tenailles;

    private long maj;

}
