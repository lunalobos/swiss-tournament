package swisst.core;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import lombok.Data;

@Data
public class TournamentPlayer implements Comparable<TournamentPlayer>, Serializable, Player {
	/**
	 *
	 */
	private static final long serialVersionUID = -4110809027637737453L;
	private String name;
	private Integer initialElo;
	private Integer currentElo;
	private BigDecimal score;
	private Integer blackScore;
	private List<TournamentPlayer> against;
	private boolean $active;
	private List<BigDecimal> roundScores;
	private List<Match> matches;

	public TournamentPlayer(String name, int initialElo) {
		this.name = name;
		this.initialElo = initialElo;
		currentElo = initialElo;
		score = new BigDecimal(0);
		blackScore = 0;
		against = new LinkedList<>();
		$active = true;
		roundScores = new LinkedList<>();
		matches = new LinkedList<>();
	}

	public TournamentPlayer(Player player) {
		this.name = player.name();
		this.initialElo = player.elo();
		currentElo = player.elo();
		score = new BigDecimal(0);
		blackScore = 0;
		against = new LinkedList<>();
		$active = true;
		roundScores = new LinkedList<>();
		matches = new LinkedList<>();
	}

	public void acumulateScore() {
		roundScores.add(new BigDecimal(score.toString()));
	}

	public void addAsBlackGame() {
		blackScore++;
	}

	@Override
	public int compareTo(TournamentPlayer o) {
		int c;
		if((c = score.compareTo(o.score)) != 0)
			return -c;
		else if ((c = Integer.compare(blackScore, o.blackScore)) != 0 )
			return -c;
		else
			return -Integer.compare(initialElo, o.getInitialElo());
	}

	public String tournamentName() {
		return name + " (" + currentElo + ")";
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int elo() {
		return initialElo;
	}

	public boolean isActive() {
		return $active;
	}

	public void disqualify() {
		$active = false;
	}

	public void enable() {
		$active = true;
	}
}
