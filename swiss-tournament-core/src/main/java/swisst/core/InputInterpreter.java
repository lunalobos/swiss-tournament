package swisst.core;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.NonNull;

@Data
public class InputInterpreter {

	private static final Logger LOGGER = LogManager.getLogger(InputInterpreter.class);

	private static final Pattern ADD_PLAYER_PATTERN = Pattern.compile("(add player: )(?<name>.+)");

	private static final Pattern REMOVE_PLAYER_PATTERN = Pattern.compile("(remove player: )(?<name>.+)");
	private static final Pattern ENABLE_PLAYER_PATTERN = Pattern.compile("(enable player: )(?<name>.+)");
	private static final Pattern DISQUALIFY_PLAYER_PATTERN = Pattern.compile("(disqualify player: )(?<name>.+)");
	private static final Pattern START_PATTERN = Pattern.compile("start");
	private static final Pattern OUTCOME_PATTERN = Pattern
			.compile("game: (?<white>.+) - (?<black>.+) => (?<outcome>1-0|0-1|1/2-1/2|suspended)");
	private static final Pattern CURRENT_ROUND_PATTERN = Pattern.compile("current round");
	private static final Pattern SCORE_PATTERN = Pattern.compile("score");
	private static final Pattern REDO_PATTERN = Pattern.compile("redo");
	private static final Pattern HELP_PATTERN = Pattern.compile("help");

	private static final Pattern TIEBREAKER_PATTERN = Pattern
			.compile("tiebreakers: (?<begining>(black games, |buchholz, "
					+ "|elo, |linear performance, |progressive, |fide performance, |sonneborn berger, )*)"
					+ "(?<end>black games|buchholz|elo|linear performance|progressive|fide performance|sonneborn berger)");

	private static final Pattern ITERATIONS_PATTERN = Pattern.compile("iterations: (?<iterations>\\d{1,2})");

	private static final Pattern CONFIG_PATTERN = Pattern.compile("(config: )(?<config>.+)");

	private static final Pattern EXAMPLE_PATTERN = Pattern.compile("example");

	private static Outcome matchOutcome(String representation) {
		return Arrays.stream(Outcome.values()).filter(o -> o.getRepresentation().equals(representation)).findAny()
				.orElseThrow(() -> new IllegalArgumentException("invalid outcome"));
	}

	@NonNull
	private BufferedReader reader;
	@NonNull
	private PlayerRepository playersDB;
	@NonNull
	private Tournament tournament;

	public String readInput() throws IOException {
		String input = reader.readLine();
		if (input == null || input.equals(""))
			return "";

		MatcherExecutor player = new MatcherExecutor(ADD_PLAYER_PATTERN.matcher(input),
				matcher -> player(matcher.group("name")));
		MatcherExecutor outcome = new MatcherExecutor(OUTCOME_PATTERN.matcher(input),
				matcher -> outcome(matcher.group("white"), matcher.group("black"),
						matchOutcome(matcher.group("outcome"))));
		MatcherExecutor start = new MatcherExecutor(START_PATTERN.matcher(input), matcher -> start());
		MatcherExecutor currentRound = new MatcherExecutor(CURRENT_ROUND_PATTERN.matcher(input),
				matcher -> tournament.lastRound());
		MatcherExecutor score = new MatcherExecutor(SCORE_PATTERN.matcher(input), matcher -> tournament.toString());
		MatcherExecutor remove = new MatcherExecutor(REMOVE_PLAYER_PATTERN.matcher(input),
				matcher -> tournament.removePlayer(matcher.group("name")));
		MatcherExecutor enable = new MatcherExecutor(ENABLE_PLAYER_PATTERN.matcher(input), matcher -> {
			tournament.seekPlayer(matcher.group("name")).ifPresent(p -> p.enable());
			return tournament.toString();
		});
		MatcherExecutor disqualify = new MatcherExecutor(DISQUALIFY_PLAYER_PATTERN.matcher(input), matcher -> {
			tournament.seekPlayer(matcher.group("name")).ifPresent(p -> p.disqualify());
			return tournament.toString();
		});
		MatcherExecutor redo = new MatcherExecutor(REDO_PATTERN.matcher(input), matcher -> tournament.redoLastRound());
		MatcherExecutor help = new MatcherExecutor(HELP_PATTERN.matcher(input), matcher -> help());

		MatcherExecutor tiebreaker = new MatcherExecutor(TIEBREAKER_PATTERN.matcher(input), matcher -> {
			List<String> tiebreakerSpecifications = Arrays.stream(matcher.group("begining").split(", "))
					.collect(Collectors.toCollection(LinkedList::new));
			tiebreakerSpecifications.add(matcher.group("end"));
			Configuration.instance.setTiebreakers(tiebreakerSpecifications.stream().map(spec -> Tiebreaker.get(spec))
					.filter(o -> o.isPresent()).map(o -> o.get()).collect(Collectors.toCollection(LinkedList::new)));
			return Configuration.instance.toString();
		});

		MatcherExecutor iterations = new MatcherExecutor(ITERATIONS_PATTERN.matcher(input), matcher -> {
			Configuration.instance.setIterations(Integer.parseInt(matcher.group("iterations")));
			return Configuration.instance.toString();
		});

		MatcherExecutor example = new MatcherExecutor(EXAMPLE_PATTERN.matcher(input), matcher -> {
			exampleTournament();
			return "";
		});

		MatcherExecutor config = new MatcherExecutor(CONFIG_PATTERN.matcher(input), matcher -> {
			Main.loadConfig(matcher.group("config")).ifPresent(json -> {
				try {
					Configuration.instance = Configuration
							.fromModel(new ObjectMapper().readValue(json, ConfigModel.class));
				} catch (JsonMappingException e) {
					LOGGER.error(MsgFactory.getMessage("Exception Class: %s, Message: %s, Stack trace:\n%s",
							e.getClass(), e.getMessage(), e.getStackTrace()));
				} catch (JsonProcessingException e) {
					LOGGER.error(MsgFactory.getMessage("Exception Class: %s, Message: %s, Stack trace:\n%s",
							e.getClass(), e.getMessage(), e.getStackTrace()));
				}
			});
			return Configuration.instance.toString();
		});

		return Stream
				.of(player, outcome, start, currentRound, score, remove, enable, disqualify, redo, help, tiebreaker,
						iterations, config, example)
				.unordered().collect(StringBuilder::new, new Accumulator(), StringBuilder::append).toString();
	}

	private String outcome(String white, String black, Outcome outcome) {
		StringBuilder sb = new StringBuilder(tournament.addGameOutcome(white, black, outcome));
		if (tournament.update()) {
			sb.append("\n");
			sb.append("Round outcomes:\n");
			sb.append(tournament.lastRound());
			sb.append("\n");
			sb.append("Next round:\n");
			sb.append(tournament.nextRound());
			sb.append("\n");
		}
		return sb.toString();
	}

	private String start() {
		System.out.println("\nBeginning...\n");
		return tournament.firstRound();
	}

	private String player(String name) {
		return tournament
				.addPlayers(new TournamentPlayer(playersDB.findByName(name).orElse(new TournamentPlayer(name, 1600))));
	}

	private void exampleTournament() {
		List<Tiebreaker> tiebreakers = IntStream.range(0, 2 + new Random().nextInt(2))
				.mapToObj(i -> Tiebreaker.values()[new Random().nextInt(7)]).distinct()
				.collect(Collectors.toCollection(LinkedList::new));
		int iterations = 3 + new Random().nextInt(7);
		Configuration.instance.setTiebreakers(tiebreakers);
		Configuration.instance.setIterations(iterations);

		List<TournamentPlayer> players = new PlayerFaker().createFakePlayers(45);
		Tournament tournament = new Tournament();
		tournament.addPlayers(players);
		System.out.println(tournament.toString());
		tournament.firstRound();
		while (!tournament.off()) {
			tournament.currentRound().getGames().stream().forEach(game -> {
				PlayerFaker f = new PlayerFaker();
				if (game.white().isPresent() && game.black().isPresent())
					game.play(f.match(game.white().get(), game.black().get()));
			});
			System.out.println(tournament.lastRound());
			System.out.println(tournament.toString());
			tournament.currentRound().setComplete(true);
			tournament.refreshQueue();
			tournament.nextRound();
		}
		System.out.println("Example tournament complete");
		Configuration.instance = Configuration.defaultConfig();
	}

	public String help() {
		try (InputStream is = new BufferedInputStream(
				this.getClass().getClassLoader().getResource("help.txt").openStream())) {
			return new String(is.readAllBytes());
		} catch (IOException e) {
			LOGGER.error(MsgFactory.getMessage(
					"Error when loading the help text file.\n" + "Exception class: %s\nMessage: %s\nStack trace:%s",
					e.getClass(), e.getMessage(), e.getStackTrace()));
			return "Help message is not available.";
		}
	}

}

@Data
class MatcherExecutor {
	@NonNull
	private Matcher matcher;
	@NonNull
	private Function<Matcher, String> function;

	public String output() {
		return function.apply(matcher);
	}
}

class Accumulator implements BiConsumer<StringBuilder, MatcherExecutor> {
	private boolean finded;

	@Override
	public void accept(StringBuilder t, MatcherExecutor u) {
		if (!finded) {
			if (finded = u.getMatcher().find())
				t.append(u.output());
		}
	}
}
