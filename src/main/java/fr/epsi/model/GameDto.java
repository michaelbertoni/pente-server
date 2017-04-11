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

    private Integer status;

    private int[][] tableau;

    private Integer nbTenaillesJ1;

    private Integer nbTenaillesJ2;

    private Integer dernierCoupX;

    private Integer dernierCoupY;

    private boolean prolongation;

    private boolean finPartie;

    private String detailFinPartie;

    private Integer numTour;

    private Integer code;

}
