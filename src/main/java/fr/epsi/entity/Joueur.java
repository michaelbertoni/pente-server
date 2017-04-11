package fr.epsi.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by micha on 10/04/2017.
 */
@Getter
@Setter
public class Joueur {

    private String id;

    private String nom;

    private int nbTenaille;

    public Joueur(String id, String nom, int nbTenaille) {
        this.id = id;
        this.nom = nom;
        this.nbTenaille = nbTenaille;
    }

}
