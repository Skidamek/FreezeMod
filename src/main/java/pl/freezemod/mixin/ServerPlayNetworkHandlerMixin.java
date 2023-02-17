package pl.freezemod.mixin;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.freezemod.FrozenPlayer;

import java.util.UUID;

import static pl.freezemod.Freeze.frozenPlayers;

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
                requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYaw(), this.player.getPitch());
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
}
