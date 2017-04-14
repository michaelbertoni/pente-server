package fr.epsi.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by micha on 12/04/2017.
 */

/**
 * Objet retourné pour la requête /play/{x}/{y}/{idJoueur}
 */
@Getter
@Setter
@ToString
public class PlayDto {

    private Integer code;

    public PlayDto(Integer code) {
        this.code = code;
    }

}
