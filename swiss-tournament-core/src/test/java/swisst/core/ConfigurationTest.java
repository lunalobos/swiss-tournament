package swisst.core;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class ConfigurationTest {
	private static final Logger LOGGER = LogManager.getLogger(ConfigurationTest.class);

	@Test
	void fromModel() {

		String json = "{\r\n"
				+ "	\"tiebreakers\": [\"sonneborn berger\", \"fide performance\", \"black games\"],\r\n"
				+ "	\"iterations\": 5\r\n" + "}";
		ConfigModel model;
		try {
			model = new ObjectMapper().readValue(json, ConfigModel.class);
			Configuration config = Configuration.fromModel(model);
			assertEquals("Sonneborn-Berger - FIDE Perf - Black Games - ",
					config.getTiebreakers().stream().map(t -> t.getHeader() + " - ").collect(StringBuilder::new,
							StringBuilder::append, StringBuilder::append).toString());
		} catch (JsonProcessingException e) {
			LOGGER.fatal(MsgFactory.getMessage(
					"Fatal error when parsing json config.\nException class: %s\nMessage: %s\nStack trace: %s",
					e.getClass(), e.getMessage(), e.getStackTrace()));
			fail();
		}

	}

}
