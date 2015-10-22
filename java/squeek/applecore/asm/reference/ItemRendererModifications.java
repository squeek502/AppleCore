package squeek.applecore.asm.reference;

import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;

public class ItemRendererModifications
{
	// changed itemstack.getMaxItemUseDuration() to entityclientplayermp.itemInUseMaxDuration
	@SuppressWarnings({"null", "unused"})
	public void renderItemInFirstPerson(float p_78440_1_)
	{
		// dummies to avoid compilation errors
		ItemStack itemstack = null;
		EntityPlayerModifications entityclientplayermp = null;
		float f6;
		float f7;

		// ...

		EnumAction enumaction = itemstack.getItemUseAction();

		// this block is deep in the method
		if (enumaction == EnumAction.eat || enumaction == EnumAction.drink)
		{
			f6 = entityclientplayermp.getItemInUseCount() - p_78440_1_ + 1.0F;
			f7 = 1.0F - f6 / entityclientplayermp.itemInUseMaxDuration;

			// ...
		}

		// ...
	}
}
