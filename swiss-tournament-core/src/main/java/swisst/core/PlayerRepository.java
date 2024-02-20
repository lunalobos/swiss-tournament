package swisst.core;

import java.io.IOException;
import java.util.Optional;
/**
 * Interface for the basic representation of a player repository.
 *
 * @author lunalobos
 */
public interface PlayerRepository {
	Optional<Player> findByName(String name);
	boolean save(Player... players) throws IOException;
}
