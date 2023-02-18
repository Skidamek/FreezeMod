package pl.freezemod;

import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

import java.util.Collection;

import static pl.freezemod.Freeze.frozenPlayers;
import static pl.freezemod.Freeze.frozenPlayersToRemove;

public class Commands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                CommandManager.literal("freeze")
                    .requires(source -> source.hasPermissionLevel(3))
                    .then(
                            CommandManager.literal("give")
                                    .then(
                                            CommandManager.argument("targets", EntityArgumentType.entities())
                                                    .executes(
                                                            context -> addEffect(
                                                                    context.getSource(), EntityArgumentType.getEntities(context, "targets")
                                                            )
                                                    )
                                    )
                    )
                    .then(
                    CommandManager.literal("remove")
                            .executes(
                                    context -> removeEffect(
                                            context.getSource(), ImmutableList.of(context.getSource().getEntityOrThrow())
                                    )
                            )
                            .then(
                                    CommandManager.argument("targets", EntityArgumentType.entities())
                                            .executes(
                                                    context -> removeEffect(
                                                            context.getSource(), EntityArgumentType.getEntities(context, "targets")
                                                    )
                                            )
                            )
                    )

        ));
    }

    private static int removeEffect(ServerCommandSource source, Collection<? extends Entity> targets) {
        for (Entity player : targets) {
            if (player.isPlayer() && frozenPlayers.containsKey(player.getUuid())) {
                frozenPlayers.remove(player.getUuid());
                frozenPlayersToRemove.add(player.getUuid()); // This will be removed in next tick (ServerPlayNetworkHandlerMixin), to make sure that everything is properly synchronized
                source.sendFeedback(TextHelper.literal(player.getName().getString() + " is no longer frozen").formatted(Formatting.GREEN), true);
            }
        }

        return 0;
    }


    private static int addEffect(ServerCommandSource source, Collection<? extends Entity> targets) {
        for (Entity player : targets) {
            if (player.isPlayer() && !player.isSpectator() && !frozenPlayers.containsKey(player.getUuid())) {
                FrozenPlayer frozenPlayer = new FrozenPlayer(player.getX(), player.getY(), player.getZ());
                frozenPlayers.put(player.getUuid(), frozenPlayer);
                // chat message for the command sender
                source.sendFeedback(TextHelper.literal(player.getName().getString() + " is now frozen").formatted(Formatting.AQUA), true);
            }
        }

        return 0;
    }
}
