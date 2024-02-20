package swisst.core;

import java.util.Optional;

/**
 * Base interface for creating tournament instances (real games or other
 * situations).
 *
 * @author lunalobos
 */
public interface TournamentInstance {
	void play(Outcome outcome);

	Optional<Outcome> outcome();

	Optional<TournamentPlayer> white();

	Optional<TournamentPlayer> black();
}