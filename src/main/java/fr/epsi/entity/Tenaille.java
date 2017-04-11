package fr.epsi.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.ManyToOne;

/**
 * Created by micha on 11/04/2017.
 */
@Getter
@Setter
public class Tenaille {

    private Player player;

    private Integer score;
}
