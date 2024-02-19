package swisst.core;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import lombok.Data;
import lombok.NonNull;

@Data
public class MainPlayerRepository implements PlayerRepository{

	@SuppressWarnings("unchecked")
	public static Optional<MainPlayerRepository> loadDB(String path) {
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
			return Optional.of(new MainPlayerRepository((Map<String,Player>)ois.readObject(),path));
		} catch (IOException | ClassNotFoundException e) {
			return Optional.empty();
		}
	}

	@NonNull
	private Map<String,Player> players;
	@NonNull
	private String path;

	@Override
	public Optional<Player> findByName(String name) {
		return Optional.ofNullable(players.get(name));
	}

	@Override
	public boolean save(Player... players) throws IOException{
		Arrays.asList(players).stream().forEach(p -> this.players.put(p.name(), p));;
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
		oos.writeObject(this.players);
		oos.close();
		return true;
	}


}
