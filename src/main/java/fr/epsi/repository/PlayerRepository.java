package fr.epsi.repository;

import fr.epsi.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by micha on 10/04/2017.
 */
public interface PlayerRepository extends JpaRepository<Player, Integer> {
}
