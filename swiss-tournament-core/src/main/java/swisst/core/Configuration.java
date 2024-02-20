package swisst.core;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

import lombok.Data;

/**
 * This class stores information about tiebreakers and provides static methods
 * to create its own instances. The global instance used in the app is the
 * static property {@code instance}. The main function of this class is to
 * synthesize tiebreaker information into an instance of {@code
 * Comparator<TorunamentPlayer>} that allows creating the player ranking.
 *
 * @author lunalobos
 */
@Data
public class Configuration {

	private static final Logger LOGGER = LogManager.getLogger(Configuration.class);

	public static Configuration defaultConfig() {
		return new Configuration(new LinkedList<>(
				Arrays.asList(Tiebreaker.BUCHHOLZ, Tiebreaker.BLACK_GAMES, Tiebreaker.LINEAR_PERFORMANCE)));
	}

	public static Configuration fromModel(ConfigModel model) {
		List<Tiebreaker> tiebreakers = model.getTiebreakers().stream().map(s -> Tiebreaker.get(s))
				.filter(o -> o.isPresent()).map(o -> o.get()).collect(Collectors.toCollection(LinkedList::new));
		LOGGER.trace(MsgFactory.getMessage("Tiebreakers from model: %s", tiebreakers));
		return new Configuration(tiebreakers);
	}

	public static Configuration instance = defaultConfig();

	private List<Tiebreaker> tiebreakers;

	public Configuration(List<Tiebreaker> tiebreakers) {
		LOGGER.trace(MsgFactory.getMessage("Tiebreakers: %s", tiebreakers));
		this.tiebreakers = tiebreakers;
	}

	public Configuration() {
	}

	/**
	 * This method synthesize tiebreaker information into an instance of {@code
	 * Comparator<TorunamentPlayer>} that allows creating the player ranking.
	 *
	 * @return an instance of {@code Comparator<TorunamentPlayer>} that allows
	 *         creating the player ranking
	 */
	public Comparator<TournamentPlayer> comparator() {
		Comparator<TournamentPlayer> scoreComparator = (p1, p2) -> -p1.getScore().compareTo(p2.getScore());
		return scoreComparator.thenComparing(tiebreakers.stream().map(t -> t.getComparator())
				.reduce((c1, c2) -> c1.thenComparing(c2)).orElseThrow(() -> new IllegalArgumentException()));
	}

	@Override
	public String toString() {
		return new StringBuilder().append("Tiebreakers order:")
				.append(tiebreakers.stream().map(t -> new StringBuilder("\n  ").append(t.getHeader()))
						.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString())
				.toString();
	}
}
