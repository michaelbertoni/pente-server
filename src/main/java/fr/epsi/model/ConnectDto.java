package fr.epsi.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by micha on 11/04/2017.
 */

/**
 * Objet retourné pour la requête /connect/{nomJoueur}
 */
@Getter
@Setter
@ToString
public class ConnectDto {

    private String idJoueur;

    private Integer code;

    private String nomJoueur;

    private Integer numJoueur;

}
