package pl.freezemod;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Freeze implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("freezemod");
	public static Map<UUID, FrozenPlayer> frozenPlayers = new HashMap<>();
	public static List<UUID> frozenPlayersToRemove = new ArrayList<>();

	@Override
	public void onInitialize() {

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (!frozenPlayers.containsKey(handler.player.getUuid()) && handler.player.getFrozenTicks() > 0) {
				handler.player.setFrozenTicks(0);
			}
		});

		Commands.register();
		LOGGER.info("Freeze mod initialized");
	}
}