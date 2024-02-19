package swisst.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class TournamentTest {

	@Test
	void blackProbabilityDistribution() {
		List<Integer> blackSample = new LinkedList<>();
		List<Integer> totalSample = new LinkedList<>();
		for (int i = 0; i < 100; i++) {
			List<TournamentPlayer> players = new PlayerFaker().createFakePlayers(45);
			Tournament tournament = new Tournament();
			tournament.addPlayers(players);
			tournament.firstRound();
			while (!tournament.off()) {
				tournament.currentRound().getGames().stream().forEach(game -> {
					PlayerFaker f = new PlayerFaker();
					if (game.white().isPresent() && game.black().isPresent())
						game.play(f.match(game.white().get(), game.black().get()));
				});
				tournament.currentRound().setComplete(true);
				tournament.refreshQueue();
				tournament.nextRound();
			}
			blackSample.addAll(tournament.getPlayers().stream().map(TournamentPlayer::getBlackScore)
					.collect(Collectors.toCollection(LinkedList::new)));
			totalSample.addAll(tournament.getPlayers().stream().map(t -> t.getAgainst().size())
					.collect(Collectors.toCollection(LinkedList::new)));
		}

		double standardDeviation = Math.sqrt(blackSample.stream().mapToDouble(i -> i).map(i -> Math.pow(i - 3, 2))
				.reduce((a, b) -> a + b).getAsDouble() / (double) blackSample.size());
		assertEquals(true, standardDeviation < 1.2525);
	}

}
