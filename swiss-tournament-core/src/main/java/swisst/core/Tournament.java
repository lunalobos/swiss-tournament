package swisst.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.BiFunction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Tournament {

	private static final Logger LOGGER = LogManager.getLogger(Tournament.class);

	private Map<String, TournamentPlayer> $names;
	private int numberOfRounds;
	private List<Round> $rounds;
	private PriorityQueue<TournamentPlayer> players;

	public Tournament() {
		this.players = new PriorityQueue<>();
		$rounds = new LinkedList<>();
		$names = new HashMap<>();
	}

	public boolean update() {
		return $rounds.getLast().update().isComplete();
	}

	public String addGameOutcome(String whiteName, String blackName, Outcome outcome) {
		if ($rounds.isEmpty())
			return "The game has not started.";
		TournamentPlayer white = $names.get(whiteName);
		TournamentPlayer black = $names.get(blackName);
		if (white == null || black == null)
			return "Wrong names.";
		Match game = new Match(white, black);
		if (!$rounds.getLast().getGames().contains(game))
			return "Wrong game definition.";
		int index = $rounds.getLast().getGames().indexOf(game);
		$rounds.getLast().getGames().get(index).play(outcome);
		refreshQueue();
		return this.toString();
	}

	public String addPlayers(Collection<TournamentPlayer> newCommers) {
		newCommers.stream().forEach(nc -> {
			players.add(nc);
			$names.put(nc.getName(), nc);
		});
		return this.toString();
	}

	public String addPlayers(TournamentPlayer... newComers) {
		Arrays.stream(newComers).forEach(nc -> {
			players.add(nc);
			$names.put(nc.getName(), nc);
		});
		return this.toString();
	}

	public String removePlayer(String playerName) {
		if ($rounds.size() == 0) {
			Optional.ofNullable($names.get(playerName)).ifPresent(p -> $names.remove(playerName, p));
			refreshQueue();
		}
		return this.toString();
	}

	public String firstRound() {
		numberOfRounds = (int) (Math.log($names.keySet().size()) / Math.log(2)) + $names.keySet().size() % 2;
		refreshQueue();

		List<TournamentInstance> games = new LinkedList<>();
		while (players.size() > 1) {
			TournamentPlayer white = players.poll();
			TournamentPlayer black = players.poll();
			games.add(new Match(white, black));
		}
		if (players.size() == 1) {
			TournamentPlayer lastPlayer = players.poll();
			FakeGame defualtGame = new FakeGame(lastPlayer);
			defualtGame.play(Outcome.WW);
			games.add(defualtGame);
		}
		refreshQueue();
		$rounds.add(new Round(games));

		return $rounds.getLast().toString();
	}

	public String nextRound() {
		LOGGER.traceEntry("nextRound");
		if (off())
			return "The tournament has ended.";
		refreshQueue();
		final Queue<TournamentPlayer> temporalPlayersQueue = new ArrayDeque<>();
		final List<TournamentInstance> games = new LinkedList<>();
		while (players.size() > 1) {
			LOGGER.trace(MsgFactory.getMessage("Players main queue size: %d.", players.size()));
			final TournamentPlayer white = players.poll();
			LOGGER.trace(MsgFactory.getMessage("White: %s.", white.getName()));
			Optional<TournamentPlayer> black = Optional.of(players.poll());
			LOGGER.trace(MsgFactory.getMessage("Black: %s.", black.get().getName()));
			while (white.getAgainst().contains(black.orElse(new TournamentPlayer("fakePlayer", 1600)))) {
				LOGGER.trace(MsgFactory.getMessage("Player %s already played against %s, looking for another opponent.",
						white.getName(), black.orElse(new TournamentPlayer("fakePlayer", 1600)).getName()));
				black.ifPresentOrElse(v -> temporalPlayersQueue.add(v), () -> {
				});
				black = Optional.ofNullable(players.poll());
				LOGGER.trace(MsgFactory.getMessage("Black: %s.",
						black.orElse(new TournamentPlayer("fakePlayer", 1600)).getName()));
			}
			players.addAll(temporalPlayersQueue);
			temporalPlayersQueue.clear();
			LOGGER.trace(MsgFactory.getMessage("Players main queue size: %d.", players.size()));
			black.ifPresentOrElse(v -> {
				Match game = new Match(white, v);
				games.add(game);
				LOGGER.trace(MsgFactory.getMessage("Game %s added.", game));
			}, () -> {
				FakeGame defualtGame = new FakeGame(white);
				defualtGame.play(Outcome.WW);
				games.add(defualtGame);
			});
		}
		if (players.size() == 1) {
			TournamentPlayer lastPlayer = players.poll();
			FakeGame defualtGame = new FakeGame(lastPlayer);
			defualtGame.play(Outcome.WW);
			games.add(defualtGame);
		}
		refreshQueue();
		$rounds.add(new Round(games));

		return LOGGER.traceExit($rounds.getLast().toString());
	}

	public String redoLastRound() {
		$rounds.removeLast();
		return nextRound();
	}

	public void refreshQueue() {
		players.clear();
		$names.entrySet().stream().filter(e -> e.getValue().isActive()).forEach(e -> players.add(e.getValue()));
	}

	public String lastRound() {
		return $rounds.getLast().toString();
	}

	public boolean off() {
		return numberOfRounds != 0 && $rounds.size() == numberOfRounds && $rounds.getLast().isComplete();
	}

	public Optional<TournamentPlayer> seekPlayer(String name) {
		return Optional.ofNullable($names.get(name));
	}

	public Round currentRound() {
		return $rounds.getLast();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append("\n");
		sb.append("Rounds: ").append(numberOfRounds).append("\n");
		if (!$rounds.isEmpty())
			sb.append("Rounds played: ").append($rounds.size() - ($rounds.getLast().isComplete() ? 1 : 0)).append("\n");
		List<TournamentPlayer> sortedPlayers = new ArrayList<>($names.values());
		Collections.sort(sortedPlayers, Configuration.instance.comparator());
		StringGenerator playerHeader = StringGenerator.builder().function((p, t) -> "Player").arg("%-32s").build();
		StringGenerator scoreHeader = StringGenerator.builder().function((p, t) -> "Score").arg("%-20s").build();
		sb.append(playerHeader.apply(null)).append(scoreHeader.apply(null));
		sb.append(Configuration.instance.getTiebreakers().stream()
				.map(tiebreaker -> StringGenerator.builder().function((p, t) -> t.getHeader()).tiebreaker(tiebreaker)
						.arg("%-20s").build())
				.map(sg -> sg.apply(null)).collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
				.toString());
		sb.append("\n");
		String body = sortedPlayers.stream().map(player -> {
			StringBuilder builder = new StringBuilder().append(String.format("%-32s", player.tournamentName()))
					.append(String.format("%-20s", player.getScore().setScale(1)));
			builder.append(Configuration.instance.getTiebreakers().stream()
					.map(tiebreaker -> StringGenerator.builder().function((p, t) -> t.getValue(p).toString())
							.tiebreaker(tiebreaker).arg("%-20s").build())
					.map(sg -> sg.apply(player))
					.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString());
			return builder.append("\n").toString();
		}).collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
		sb.append(body);
		return sb.toString();
	}

}

@Data
@Builder
class StringGenerator {
	@NonNull
	private BiFunction<TournamentPlayer, Tiebreaker, String> function;
	private Tiebreaker tiebreaker;
	@NonNull
	private String arg;

	public String apply(TournamentPlayer player) {
		return String.format(arg, function.apply(player, tiebreaker));
	}
}
