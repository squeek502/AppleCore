package squeek.applecore.example;

import net.minecraft.item.ItemFood;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

// does not work correctly
public class EatingSpeedModifier
{
	@SubscribeEvent
	public void onItemUse(PlayerUseItemEvent.Start event)
	{
		if (event.item.getItem() instanceof ItemFood)
			event.duration = 48;
	}
}
