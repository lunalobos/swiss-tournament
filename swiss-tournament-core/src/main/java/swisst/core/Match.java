package swisst.core;

import java.math.BigDecimal;
import java.util.Optional;

import lombok.Data;
import lombok.NonNull;

@Data
public class Match implements TournamentInstance {

	private static final double K = 32.0;

	private static void calculateElo(TournamentPlayer white, TournamentPlayer black, Outcome outcome) {

		double wExponent = (double) (white.getCurrentElo() - black.getCurrentElo()) / 400;
		double wMitigator = (1 / (1 + (Math.pow(10, wExponent))));

		int newWElo = (int) Math.round(white.getCurrentElo() + K * (outcome.getWScore().doubleValue() - wMitigator));

		double bExponent = (double) (black.getCurrentElo() - white.getCurrentElo()) / 400;
		double bMitigator = (1 / (1 + (Math.pow(10, bExponent))));

		int newBElo = (int) Math.round(white.getCurrentElo() + K * (outcome.getBScore().doubleValue() - bMitigator));

		white.setCurrentElo(newWElo);
		black.setCurrentElo(newBElo);
	}

	@NonNull
	private TournamentPlayer white;

	@NonNull
	private TournamentPlayer black;

	private Outcome $outcome;

	@Override
	public void play(Outcome outcome) {
		$outcome = outcome;
		if (outcome != Outcome.SUSPENDED) {
			white.getAgainst().add(black);
			black.getAgainst().add(white);
			black.addAsBlackGame();
			calculateElo(white, black, outcome);
		}
		white.setScore(white.getScore().add(outcome.getWScore()));
		black.setScore(black.getScore().add(outcome.getBScore()));
		white.acumulateScore();
		black.acumulateScore();
		white.getMatches().add(this);
		black.getMatches().add(this);
	}

	@Override
	public Optional<Outcome> outcome() {
		return Optional.ofNullable($outcome);
	}

	@Override
	public String toString() {
		return new StringBuilder().append(white.getName()).append(" (").append(white.getCurrentElo()).append(") - ")
				.append(black.getName()).append(" (").append(black.getCurrentElo()).append(") ")
				.append(outcome().orElse(Outcome.IN_GAME).getRepresentation()).toString();
	}

	public BigDecimal sonnebornBergerPartialScore(TournamentPlayer player) {
		if (white.equals(player) && outcome().isPresent()) {
			switch (outcome().get()) {
			case WW:
				return black.getScore();
			case D:
				return black.getScore().divide(new BigDecimal("2"));
			default:
				return new BigDecimal("0.0");
			}
		} else if (black.equals(player) && outcome().isPresent()) {
			switch (outcome().get()) {
			case BW:
				return white.getScore();
			case D:
				return white.getScore().divide(new BigDecimal("2"));
			default:
				return new BigDecimal("0.0");
			}
		} else
			return new BigDecimal("0.0");
	}

	@Override
	public Optional<TournamentPlayer> white() {
		return Optional.ofNullable(white);
	}

	@Override
	public Optional<TournamentPlayer> black() {
		return Optional.ofNullable(black);
	}

}
