package swisst.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

	private static final Logger LOGGER = LogManager.getLogger(Main.class);

	public static void main(String[] args) {
		PlayerRepository playersDB = MainPlayerRepository.loadDB(args.length >= 1 ? args[0] : "")
				.orElse(new MainPlayerRepository(new HashMap<>(), "players_db.bin"));
		if (args.length == 2) {
			loadConfig(args[1]).ifPresent(jsonString -> {
				try {
					Configuration.instance = Configuration
							.fromModel(new ObjectMapper().readValue(jsonString, ConfigModel.class));
				} catch (JsonMappingException e) {
					LOGGER.error(MsgFactory.getMessage("Exception Class: %s, Message: %s, Stack trace:\n%s",
							e.getClass(), e.getMessage(), e.getStackTrace()));
				} catch (JsonProcessingException e) {
					LOGGER.error(MsgFactory.getMessage("Exception Class: %s, Message: %s, Stack trace:\n%s",
							e.getClass(), e.getMessage(), e.getStackTrace()));
				}
			});
		}
		try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in))) {

			InputInterpreter interpreter = new InputInterpreter(inputReader, playersDB, new Tournament());
			System.out.println(interpreter.help());
			while (!interpreter.getTournament().off()) {
				System.out.println(interpreter.readInput());
			}
			playersDB.save(interpreter.getTournament().getPlayers().stream()
					.map(p -> new DBPlayer(p.getName(), p.getCurrentElo()))
					.collect(Collectors.toCollection(ArrayList::new)).toArray(new Player[] {}));
		} catch (IOException | IllegalArgumentException e) {
			LOGGER.error(MsgFactory.getMessage("Error (%s): %s\nStack trace:\n%s", e.getClass(), e.getMessage(),
					e.getStackTrace()));
		}

	}

	public static Optional<String> loadConfig(String path) {
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
			return Optional.of(reader.lines().collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
					.toString());
		} catch (IOException e) {
			return Optional.empty();
		}
	}

}
