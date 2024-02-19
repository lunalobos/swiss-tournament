package swisst.core;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public enum Outcome {
	WW("1.0", "0.0", "1-0"), BW("0.0", "1.0", "0-1"), D("0.5", "0.5", "1/2-1/2"), IN_GAME("0.0", "0.0", "in game"),
	SUSPENDED("0.0", "0.0", "suspended");

	private BigDecimal wScore;
	private BigDecimal bScore;
	private String representation;

	private Outcome(String wScore, String bScore, String representation) {
		this.wScore = new BigDecimal(wScore);
		this.bScore = new BigDecimal(bScore);
		this.representation = representation;
	}
}
