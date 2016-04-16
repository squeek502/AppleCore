package squeek.applecore.asm.reference;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.math.MathHelper;

public class ItemRendererModifications
{
	// changed itemToRender.getMaxItemUseDuration() to clientPlayer.itemInUseMaxDuration
	private void func_178104_a(AbstractClientPlayer clientPlayer, float p_178104_2_)
	{
		Object entityclientplayermp = null;
		float f = (float) clientPlayer.getItemInUseCount() - p_178104_2_ + 1.0F;
		// this is actually the first parameter; a dummy is used here to show which field we are accessing
		float f1 = f / (float) ((EntityPlayerModifications) entityclientplayermp).itemInUseMaxDuration;
		float f2 = MathHelper.abs(MathHelper.cos(f / 4.0F * 3.1415927F) * 0.1F);
	}
}