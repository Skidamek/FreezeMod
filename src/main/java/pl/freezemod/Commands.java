package pl.freezemod;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static pl.freezemod.Freeze.frozenPlayers;

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
        for (Entity entity : targets) {
            if (entity.isPlayer() && frozenPlayers.containsKey(entity.getUuid())) {
                frozenPlayers.remove(entity.getUuid());

                entity.setFrozenTicks(120); // 120 ticks to let the animation go

                source.sendFeedback(TextHelper.literal(entity.getName().getString() + " is no longer frozen").formatted(Formatting.GREEN), true);
            }
        }

        return 1;
    }


    private static int addEffect(ServerCommandSource source, Collection<? extends Entity> targets) throws CommandSyntaxException {
        for (Entity entity : targets) {
            if (entity.isPlayer() && !entity.isSpectator() && !frozenPlayers.containsKey(entity.getUuid())) {

                FrozenPlayer frozenPlayer = new FrozenPlayer(entity.getX(), entity.getY(), entity.getZ());
                frozenPlayers.put(entity.getUuid(), frozenPlayer);

                entity.setFrozenTicks(2147483647);

                ServerPlayerEntity playerEntity = (ServerPlayerEntity) entity;
                Function<Text, Packet<?>> constructor = OverlayMessageS2CPacket::new;

                // action bar message
                CompletableFuture.runAsync(() -> {
                    while (frozenPlayers.containsKey(entity.getUuid())) {
                        try {
                            playerEntity.networkHandler.sendPacket(
                                    constructor.apply(
                                            Texts.parse(
                                                    source,
                                                    TextHelper.literal("You are frozen").formatted(Formatting.AQUA).formatted(Formatting.BOLD),
                                                    playerEntity,
                                                    0
                                            )
                                    ));


                            Thread.sleep(2000);
                        } catch (CommandSyntaxException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });


                // chat message for the command sender
                source.sendFeedback(TextHelper.literal(entity.getName().getString() + " is now frozen").formatted(Formatting.AQUA), true);
            }
        }

        return 1;
    }
}
