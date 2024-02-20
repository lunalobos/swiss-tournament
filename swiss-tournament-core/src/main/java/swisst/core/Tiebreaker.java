package swisst.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

@Getter
public enum Tiebreaker {
	// @formatter:off
	BLACK_GAMES (p -> new BigDecimal(p.getBlackScore()), 0, "Black Games"),
	BUCHHOLZ (p -> TiebreakMetrics.buchholz(p), 1, "Buchholz"),
	ELO (p -> new BigDecimal(p.getCurrentElo()), 0, "Elo"),
	LINEAR_PERFORMANCE(p -> TiebreakMetrics.linearPerformance(p), 0, "Linear Perf"),
	PROGRESSIVE(p -> TiebreakMetrics.progressive(p), 1, "Progressive"),
	FIDE_PERFORMANCE(p -> TiebreakMetrics.fidePerformance(p), 0, "FIDE Perf"),
	SONNEBORN_BERGER(p -> TiebreakMetrics.sonnebornBerger(p), 1, "Sonneborn-Berger");
	// @formatter:on
	private static final Logger LOGGER = LogManager.getLogger(Tiebreaker.class);

	public static Optional<Tiebreaker> get(String input) {
		input = input.toUpperCase();
		input = input.replace(' ', '_');
		try {
			return Optional.ofNullable(Tiebreaker.valueOf(input));
		} catch (IllegalArgumentException e) {
			LOGGER.error(MsgFactory.getMessage(
					"Error parsing tiebreaker %s.\nException class: %s\nMessage:%s\nStack trace: %s", input,
					e.getClass(), e.getMessage(), e.getStackTrace()));
			return Optional.empty();
		}
	}

	private Comparator<TournamentPlayer> comparator;
	private Function<TournamentPlayer, BigDecimal> $function;
	private int $scale;
	private String header;

	private Tiebreaker(Function<TournamentPlayer, BigDecimal> function, int scale, String header) {
		$function = function;
		comparator = (p1, p2) -> -$function.apply(p1).compareTo($function.apply(p2));
		$scale = scale;
		this.header = header;
	}

	public BigDecimal getValue(TournamentPlayer player) {
		return $function.apply(player).setScale($scale, RoundingMode.UP);
	}
}

class TiebreakMetrics {

	public static int iterations = 5;

	public static BigDecimal linearPerformance(TournamentPlayer player) {
		double average = player.getAgainst().stream().mapToInt(p -> p.elo()).average().orElse(player.elo());

		return new BigDecimal(average).add(player.getScore().multiply(new BigDecimal(400))).add(new BigDecimal(-400));
	}

	public static BigDecimal fidePerformance(TournamentPlayer player) {

		int[] dp = new int[] { 0, 7, 14, 21, 29, 36, 43, 50, 57, 65, 72, 80, 87, 95, 102, 110, 117, 125, 133, 141, 149,
				158, 166, 175, 184, 193, 202, 211, 220, 230, 240, 251, 262, 273, 284, 296, 309, 322, 336, 351, 366, 383,
				401, 422, 444, 470, 501, 538, 589, 677, 800 };

		int percentage = player.getAgainst().size() == 0 ? 0
				: (int) Math.round(50 * player.getScore().doubleValue() / player.getAgainst().size());
		double average = player.getAgainst().stream().mapToInt(p -> p.elo()).average().orElse(player.elo());

		if (percentage >= 50)
			return new BigDecimal(Math.round(average + dp[percentage - 50]));
		else
			return new BigDecimal(Math.round(average - dp[50 - percentage]));
	}

	public static BigDecimal buchholz(TournamentPlayer player) {
		List<TournamentPlayer> auxList = new LinkedList<>(player.getAgainst());
		Collections.sort(auxList, (p1, p2) -> p1.getScore().compareTo(p2.getScore()));
		Deque<TournamentPlayer> auxDeque = new LinkedList<>(auxList);
		auxDeque.pollLast();
		auxDeque.pollFirst();
		return auxDeque.stream().map(p -> p.getScore()).reduce((a, b) -> a.add(b)).orElse(new BigDecimal(0));
	}

	public static BigDecimal progressive(TournamentPlayer player) {
		return player.getRoundScores().stream().reduce((a, b) -> a.add(b)).orElse(new BigDecimal(0));
	}

	public static BigDecimal sonnebornBerger(TournamentPlayer player) {
		return player.getMatches().stream().map(m -> m.sonnebornBergerPartialScore(player)).reduce((a, b) -> a.add(b))
				.orElse(new BigDecimal(0));
	}

}
