package fr.epsi.entity;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Generated;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

/**
 * Created by micha on 10/04/2017.
 */
@Entity
@Getter
@Setter
public class Game {

    @Id
    @GeneratedValue
    private Integer id;

    private String token;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Player> players;

    @ManyToOne
    private Player tour; // user du tour en cours

    private GameState etat;

    @ManyToOne
    private Player gagnant;

    private List<List<Integer>> grille;

    @ManyToOne
    private List<Tenaille> tenailles;

}
