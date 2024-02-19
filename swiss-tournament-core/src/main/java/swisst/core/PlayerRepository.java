package swisst.core;

import java.io.IOException;
import java.util.Optional;

public interface PlayerRepository {
	Optional<Player> findByName(String name);
	boolean save(Player... players) throws IOException;
}
