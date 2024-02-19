package swisst.core;

import java.util.Optional;

public interface TournamentInstance {
	void play(Outcome outcome);
	Optional<Outcome> outcome();
	Optional<TournamentPlayer> white();
	Optional<TournamentPlayer> black();
}