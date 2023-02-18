package pl.freezemod.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.freezemod.Freeze;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
    public void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (source.equals(DamageSource.FREEZE) && Freeze.frozenPlayers.containsKey(((PlayerEntity) (Object) this).getUuid())) {
            ci.cancel();
        }
    }


    // FIXME: This doesn't work, we want to not allow player to break/place/modify blocks while frozen
    @Inject(method = "canModifyBlocks", at = @At("HEAD"))
    public void canModifyBlocks(CallbackInfoReturnable<Boolean> cir) {
        if (Freeze.frozenPlayers.containsKey(((PlayerEntity) (Object) this).getUuid())) {
            LOGGER.warn("Player " + ((PlayerEntity) (Object) this).getGameProfile().getName() + " tried to modify blocks while frozen");
            cir.cancel();
        }
    }
}
