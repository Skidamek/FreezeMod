package pl.freezemod.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.freezemod.Freeze;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
    public void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (Freeze.frozenPlayers.containsKey(((PlayerEntity) (Object) this).getUuid())) {
            ci.cancel();
        }
    }
}
