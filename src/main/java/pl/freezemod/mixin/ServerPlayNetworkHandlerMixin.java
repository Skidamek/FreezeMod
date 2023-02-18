package pl.freezemod.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.freezemod.FrozenPlayer;
import pl.freezemod.TextHelper;

import java.util.UUID;
import java.util.function.Function;

import static pl.freezemod.Freeze.frozenPlayers;
import static pl.freezemod.Freeze.frozenPlayersToRemove;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public void requestTeleport(double x, double y, double z, float yaw, float pitch) { }

    @Inject(method = "onPlayerMove", at = @At("HEAD"), cancellable = true)
    public void onWrite(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        UUID uuid = this.player.getUuid();
        if (frozenPlayers.containsKey(uuid)) {

            FrozenPlayer frozenPlayer = frozenPlayers.get(uuid);

            if (!SimilarCords(frozenPlayer, packet)) {
                Entity entity = this.player; // Needed to get the yaw and pitch
                requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), entity.getYaw(), entity.getPitch());
            }

            ci.cancel();
        }
    }

    private boolean SimilarCords(FrozenPlayer frozenPlayer, PlayerMoveC2SPacket packet) {
        double x = frozenPlayer.getX();
        double y = frozenPlayer.getY();
        double z = frozenPlayer.getZ();
        double playerX = packet.getX(x);
        double playerY = packet.getY(y);
        double playerZ = packet.getZ(z);

        // Compare the coordinates directly and return true if all three match exactly
        return x == playerX && y == playerY && z == playerZ;
    }

    private int tick = 0;
    private int allTicks = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) throws CommandSyntaxException {
        UUID uuid = this.player.getUuid();

        if (frozenPlayers.containsKey(uuid)) {
            allTicks++;
            this.player.setFrozenTicks(allTicks);

            if (tick++ % 20 == 0) {
                tick = 0;
                sendActionBarMessage("You are frozen", Formatting.AQUA);
            }

        } else if (frozenPlayersToRemove.contains(uuid)) {
            this.player.setFrozenTicks(140); // To make animation let go!
            sendActionBarMessage("You are no longer frozen", Formatting.GREEN);
            frozenPlayersToRemove.remove(uuid);
        }
    }


    private void sendActionBarMessage(String message, Formatting color) throws CommandSyntaxException {
        Function<Text, Packet<?>> constructor = OverlayMessageS2CPacket::new;
        ServerCommandSource source = this.player.getCommandSource();
        this.player.networkHandler.sendPacket(
                constructor.apply(
                        Texts.parse(
                                source,
                                TextHelper.literal(message).formatted(color).formatted(Formatting.BOLD),
                                this.player,
                                0
                        )
                ));
    }
}
