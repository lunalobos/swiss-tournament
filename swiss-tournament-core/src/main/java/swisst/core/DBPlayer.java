package swisst.core;

import java.io.Serializable;

public class DBPlayer implements Player, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -4384347408605008314L;
	private String name;
	private int elo;

	public DBPlayer() {
	}

	public DBPlayer(String name, int elo) {
		this.name = name;
		this.elo = elo;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int elo() {
		return elo;
	}

}
