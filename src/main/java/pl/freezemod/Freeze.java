package pl.freezemod;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Freeze implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("freezemod");
	public static Map<UUID, FrozenPlayer> frozenPlayers = new HashMap<>();
	public static List<UUID> frozenPlayersToRemove = new ArrayList<>();
	public static Boolean freezeServer;

	@Override
	public void onInitialize() {
		ServerPlayConnectionEvents.JOIN.register(this::joinEvent);
		Commands.register();
		LOGGER.info("Freeze mod initialized");
	}


	private void joinEvent(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
		if (!frozenPlayers.containsKey(handler.player.getUuid()) && handler.player.getFrozenTicks() > 0) {
			handler.player.setFrozenTicks(0);
		}

		if (freezeServer) {
			Commands.addEffect(handler.player);
		}
	}
}