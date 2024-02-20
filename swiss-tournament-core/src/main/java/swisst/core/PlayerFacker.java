package swisst.core;

import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import com.github.javafaker.Faker;

import lombok.Data;

/**
 * Fake player generator, utilizes the com.github.javafaker library under the
 * hood. See the link {@link https://github.com/DiUS/java-faker} for more info
 * on java-faker .
 *
 * @author lunalobos
 */
@Data
class PlayerFaker {

	private int minElo;
	private int maxElo;
	private Random r;
	private double k;
	private double drawTolerance;

	public PlayerFaker() {
		minElo = 1600;
		maxElo = 1900;
		r = new SecureRandom();
		k = 0.005;
		drawTolerance = 0.2;
	}

	public PlayerFaker(int minElo, int maxElo, Random r, double k, double drawTolerance) {
		this.minElo = minElo;
		this.maxElo = maxElo;
		this.r = r;
		this.k = k;
		this.drawTolerance = drawTolerance;
	}

	private int elo() {
		return minElo + r.nextInt(maxElo - minElo);
	}

	private String name() {
		return new Faker().name().fullName();
	}

	public TournamentPlayer nextPlayer() {
		return new TournamentPlayer(name(), elo());
	}

	public List<TournamentPlayer> createFakePlayers(int amount) {
		return IntStream.range(0, amount).mapToObj(i -> nextPlayer()).collect(LinkedList::new, LinkedList::add,
				LinkedList::addAll);
	}

	public Outcome match(TournamentPlayer white, TournamentPlayer black) {

		double normalizedDiff = (white.elo() - black.elo()) * k;

		double definition = r.nextGaussian(normalizedDiff, 0.5);

		return definition > -drawTolerance ? (definition < drawTolerance ? Outcome.D : Outcome.WW) : Outcome.BW;
	}

}
