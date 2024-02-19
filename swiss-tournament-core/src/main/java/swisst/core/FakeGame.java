package swisst.core;

import java.util.Optional;

import lombok.Data;
import lombok.NonNull;

@Data
public class FakeGame implements TournamentInstance {
	@NonNull
	private TournamentPlayer lonelyWhitePlayer;
	private Outcome $outcome;

	@Override
	public void play(Outcome outcome) {
		lonelyWhitePlayer.setScore(lonelyWhitePlayer.getScore().add(outcome.getWScore()));
		$outcome = outcome;
	}

	@Override
	public Optional<Outcome> outcome() {
		return Optional.ofNullable($outcome);
	}

	@Override
	public String toString() {
		return new StringBuilder().append("Player ").append(lonelyWhitePlayer.getName())
				.append(" has no match and wins a point by default.").toString();
	}

	@Override
	public Optional<TournamentPlayer> white() {
		return Optional.of(lonelyWhitePlayer);
	}

	@Override
	public Optional<TournamentPlayer> black() {
		return Optional.empty();
	}

}
