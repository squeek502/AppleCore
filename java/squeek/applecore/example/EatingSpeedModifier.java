package squeek.applecore.example;

import net.minecraft.item.ItemFood;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EatingSpeedModifier
{
	// this is a default Forge event
	// normally, doing this will cause the eating animation to not work properly,
	// but AppleCore fixes that
	@SubscribeEvent
	public void onItemUse(PlayerUseItemEvent.Start event)
	{
		if (event.item.getItem() instanceof ItemFood)
			event.duration = 100;
	}
}
