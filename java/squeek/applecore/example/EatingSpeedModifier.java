package squeek.applecore.example;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EatingSpeedModifier
{
	// this is a default Forge event
	// normally, doing this will cause the eating animation to not work properly,
	// but AppleCore fixes that
	@SubscribeEvent
	public void onItemUse(LivingEntityUseItemEvent event)
	{
		if (event.getEntity() instanceof EntityPlayer)
		{
			if (event.getItem().getItem() instanceof ItemFood)
			{
				event.setDuration(100);
			}
		}
	}
}