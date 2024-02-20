package swisst.core;

import java.util.List;

import lombok.Data;

/**
 * Model class intended to be a container where the configuration in json format
 * is converted to then create an instance of {@code Configuration}.
 *
 * @author lunalobos
 */
@Data
public class ConfigModel {
	private List<String> tiebreakers;
}
