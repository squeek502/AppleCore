package squeek.applecore.asm.reference;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

public class ItemRendererModifications
{
	// changed itemToRender.getMaxItemUseDuration() to clientPlayer.itemInUseMaxDuration
	private void transformEatFirstPerson(float p_187454_1_, EnumHandSide p_187454_2_, ItemStack p_187454_3_)
	{
		Object mc_thePlayer = null;
		float f = (float)this.mc.player.getItemInUseCount() - p_187454_1_ + 1.0F;
		// this is actually this.mc.thePlayer; a dummy is used here to show which field we are accessing
		float f1 = f / (float) ((EntityPlayerModifications) mc_thePlayer).itemInUseMaxDuration;
	}

	private Minecraft mc;
}
