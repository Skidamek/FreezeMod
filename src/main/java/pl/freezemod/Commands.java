package pl.freezemod;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

import java.util.List;

import static pl.freezemod.Freeze.*;

public class Commands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                CommandManager.literal("freeze")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(
                            CommandManager.argument("targets", EntityArgumentType.entities())
                                    .executes(
                                            context -> {
                                                for (Entity player : EntityArgumentType.getEntities(context, "targets")) {
                                                    ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
                                                    context.getSource().sendFeedback(TextHelper.literal(player.getName().getString() + " is now frozen").formatted(Formatting.AQUA), true);
                                                    return addEffect(serverPlayerEntity);
                                                }

                                                return 1;
                                            }
                                    )
                    )
                    .then(
                            CommandManager.literal("server")
                                    .executes(
                                            context -> freezeServer(
                                                    context.getSource()
                                            )
                                    )
                    )
                    .then(
                    CommandManager.literal("remove")
                            .executes(
                                    context -> removeEffect(
                                            context.getSource().getPlayerOrThrow()
                                    )
                            )
                            .then(
                                    CommandManager.argument("targets", EntityArgumentType.entities())
                                            .executes(
                                                    context -> {
                                                        for (Entity player : EntityArgumentType.getEntities(context, "targets")) {
                                                            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
                                                            context.getSource().sendFeedback(TextHelper.literal(player.getName().getString() + " is no longer frozen").formatted(Formatting.GREEN), true);
                                                            return removeEffect(serverPlayerEntity);
                                                        }

                                                        return 1;
                                                    }
                                            )
                            )

                            .then(
                                    CommandManager.literal("server")
                                            .executes(
                                                    context -> unFreezeServer(
                                                            context.getSource()
                                                    )
                                            )
                            )
                    )
        ));
    }

    public static int addEffect(ServerPlayerEntity player) {
        if (player.isPlayer() && !player.isSpectator() && !frozenPlayers.containsKey(player.getUuid())) {
            FrozenPlayer frozenPlayer = new FrozenPlayer(player.getX(), player.getY(), player.getZ());
            frozenPlayers.put(player.getUuid(), frozenPlayer);
        }

        return 1;
    }

    private static int removeEffect(ServerPlayerEntity player) {
        if (player.isPlayer() && frozenPlayers.containsKey(player.getUuid())) {
            frozenPlayers.remove(player.getUuid());
            frozenPlayersToRemove.add(player.getUuid()); // This will be removed in next tick (ServerPlayNetworkHandlerMixin), to make sure that everything is properly synchronized
        }

        return 1;
    }

    private static int freezeServer(ServerCommandSource source) {
        freezeServer = true;

        List<ServerPlayerEntity> players = source.getWorld().getPlayers();
        for (ServerPlayerEntity player : players) {
            addEffect(player);
        }

        source.sendFeedback(TextHelper.literal("All players are now frozen").formatted(Formatting.AQUA), true);

        return 1;
    }

    private static int unFreezeServer(ServerCommandSource source) {
        freezeServer = false;
        frozenPlayers.clear();

        List<ServerPlayerEntity> players = source.getWorld().getPlayers();
        for (ServerPlayerEntity player : players) {
            if (frozenPlayers.containsKey(player.getUuid())) {
                frozenPlayersToRemove.add(player.getUuid()); // This will be removed in next tick (ServerPlayNetworkHandlerMixin), to make sure that everything is properly synchronized
            }
        }

        source.sendFeedback(TextHelper.literal("All players are no longer frozen").formatted(Formatting.GREEN), true);

        return 1;
    }
}
