package squeek.applecore.example;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EatingSpeedModifier
{
	// this is a default Forge event
	// normally, doing this will cause the eating animation to not work properly,
	// but AppleCore fixes that
	@SubscribeEvent
	public void onItemUse(LivingEntityUseItemEvent.Start event)
	{
		if (event.getEntity() instanceof PlayerEntity)
		{
			if (event.getItem().getItem().isFood())
			{
				event.setDuration(100);
			}
		}
	}
}