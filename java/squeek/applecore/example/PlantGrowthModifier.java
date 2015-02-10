package squeek.applecore.example;

import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockStem;
import squeek.applecore.api.plants.PlantGrowthEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class PlantGrowthModifier
{
	@SubscribeEvent
	public void growthTickAllowed(PlantGrowthEvent.AllowGrowthTick event)
	{
		if (event.block instanceof BlockCocoa)
			// disable cocoa growth completely
			event.setResult(Result.DENY);
		else if (event.block instanceof BlockStem)
			// normal growth for melon/pumpkin
			event.setResult(Result.DEFAULT);
		else
			// everything else grows as fast as the random ticking allows
			event.setResult(Result.ALLOW);
	}

	@SubscribeEvent
	public void onGrowthTick(PlantGrowthEvent.GrowthTick event)
	{
		int currentMetadata = event.world.getBlockMetadata(event.x, event.y, event.z);
		AppleCoreExample.Log.info(event.block + " grew from a growth tick (from meta " + event.previousMetadata + " to " + currentMetadata + ")");
	}
}
