package swisst.core;

import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import lombok.Data;
import lombok.NonNull;

@Data
public class Round implements Iterable<TournamentInstance> {
	@NonNull
	private List<TournamentInstance> games;
	boolean complete;

	public void setOutcomes(Outcome... outcomes) {
		if (outcomes.length != games.size())
			throw new IllegalArgumentException("Arguments count differs from games count.");
		IntStream.range(0, outcomes.length).forEach(i -> games.get(i).play(outcomes[i]));
	}

	public Round update() {
		complete = !games.stream().anyMatch(g -> g.outcome().isEmpty());
		return this;
	}

	@Override
	public Iterator<TournamentInstance> iterator() {
		return games.iterator();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append("\n");
		for (TournamentInstance game : games) {
			sb.append(game).append("\n");
		}
		return sb.toString();
	}
}
