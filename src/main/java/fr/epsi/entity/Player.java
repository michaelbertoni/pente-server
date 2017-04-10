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
@Entity
@Getter
@Setter
public class Player {

    @Id
    @GeneratedValue
    private Integer id;

    private String nom;

    public Player(String nom) {
        this.nom = nom;
    }

}
