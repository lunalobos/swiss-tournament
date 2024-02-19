package swisst.core;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

import lombok.Data;

@Data
public class Configuration {

	private static final Logger LOGGER = LogManager.getLogger(Configuration.class);

	public static Configuration defaultConfig() {
		return new Configuration(
				new LinkedList<>(Arrays.asList(Tiebreaker.BUCHHOLZ, Tiebreaker.BLACK_GAMES, Tiebreaker.LINEAR_PERFORMANCE)),
				5);
	}

	public static Configuration fromModel(ConfigModel model) {
		List<Tiebreaker> tiebreakers = model.getTiebreakers().stream().map(s -> Tiebreaker.get(s))
				.filter(o -> o.isPresent()).map(o -> o.get()).collect(Collectors.toCollection(LinkedList::new));
		LOGGER.trace(MsgFactory.getMessage("Tiebreakers from model: %s", tiebreakers));
		LOGGER.trace(MsgFactory.getMessage("Iterations from model: %s", model.getIterations()));
		return new Configuration(tiebreakers, model.getIterations());
	}

	public static Configuration instance = defaultConfig();

	private List<Tiebreaker> tiebreakers;
	private int iterations;

	public Configuration(List<Tiebreaker> tiebreakers, int iterations) {
		LOGGER.trace(MsgFactory.getMessage("Tiebreakers: %s", tiebreakers));
		LOGGER.trace(MsgFactory.getMessage("Iterations: %s", iterations));
		this.tiebreakers = tiebreakers;
		this.iterations = iterations;
	}

	public Configuration() {
	}

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
				.append("\nIterations: ").append(iterations).toString();
	}
}
