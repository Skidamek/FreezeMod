package pl.freezemod;

import it.unimi.dsi.fastutil.Hash;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Freeze implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("freezemod");
	public static Map<UUID, FrozenPlayer> frozenPlayers = new HashMap<>();

	@Override
	public void onInitialize() {
		Commands.register();
		LOGGER.info("Freeze mod initialized");
	}
}