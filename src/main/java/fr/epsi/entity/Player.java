package fr.epsi.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.HashMap;

/**
 * Created by micha on 10/04/2017.
 */
@Getter
@Setter
public class Player {

    private String id;

    private String nom;

    private int nbTenaille;

    public Player(String id, String nom, int nbTenaille) {
        this.id = id;
        this.nom = nom;
        this.nbTenaille = nbTenaille;
    }

    public void incrementNbTenaille() {
        this.nbTenaille++;
    }
}
