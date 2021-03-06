package cookiedragon.luchadora.mixin.mixins.entity;

import cookiedragon.luchadora.event.api.EventDispatcher;
import cookiedragon.luchadora.event.entity.EntityReachEvent;
import cookiedragon.luchadora.event.entity.ResetBlockDamageEvent;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author cookiedragon234 22/Dec/2019
 */
@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP
{
	@Inject(method = "resetBlockRemoving", at = @At("HEAD"), cancellable = true)
	private void resetBlockWrapper(CallbackInfo ci)
	{
		ResetBlockDamageEvent event = new ResetBlockDamageEvent();
		EventDispatcher.dispatch(event);
		if (event.isCancelled())
		{
			ci.cancel();
		}
	}
	
	@Inject(method = "getBlockReachDistance", at = @At("RETURN"), cancellable = true)
	private void getReachDistanceWrapper(CallbackInfoReturnable<Float> cir)
	{
		EntityReachEvent event = new EntityReachEvent((PlayerControllerMP)(Object)this, cir.getReturnValue());
		EventDispatcher.dispatch(event);
		cir.setReturnValue(event.reachDistance);
	}
	
	@Redirect(method = "processRightClickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;contains(Lnet/minecraft/util/math/BlockPos;)Z"))
	private boolean isInWorldBorder(WorldBorder worldBorder, BlockPos pos)
	{
		return true;
	}
	
	@Redirect(method = "clickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;contains(Lnet/minecraft/util/math/BlockPos;)Z"))
	private boolean isInWorldBorderClick(WorldBorder worldBorder, BlockPos pos)
	{
		return true;
	}
}
