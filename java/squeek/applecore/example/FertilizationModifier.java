package squeek.applecore.example;

import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockStem;
import squeek.applecore.api.plants.FertilizationEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class FertilizationModifier
{
	@SubscribeEvent
	public void canFertlize(FertilizationEvent.CanFertilize event)
	{
		if (event.growable instanceof BlockCocoa)
			// disable cocoa fertilization completely
			event.setResult(Result.DENY);
		else if (event.growable instanceof BlockStem)
			// melon/pumpkin can always be fertilized
			event.setResult(Result.ALLOW);
		else
			// normal fertilization conditions for everything else
			event.setResult(Result.DEFAULT);
	}

	@SubscribeEvent
	public void onFertilize(FertilizationEvent.Fertilize event)
	{
		// custom fertilization effect for stems
		if (event.growable instanceof BlockStem)
		{
			// randomize the meta
			event.world.setBlockMetadataWithNotify(event.x, event.y, event.z, event.random.nextInt(15), 1);
			event.setResult(Result.ALLOW);
		}
		else
		{
			AppleCoreExample.Log.info(event.growable + " is about to be fertilized (current meta: " + event.metadata + ")");
		}
	}

	@SubscribeEvent
	public void onFertilized(FertilizationEvent.Fertilized event)
	{
		int currentMetadata = event.world.getBlockMetadata(event.x, event.y, event.z);
		AppleCoreExample.Log.info(event.growable + " was fertilized from meta " + event.previousMetadata + " to " + currentMetadata);
	}
}
