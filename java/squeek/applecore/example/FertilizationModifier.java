package squeek.applecore.example;

import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockStem;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.applecore.api.plants.FertilizationEvent;

public class FertilizationModifier
{
	@SubscribeEvent
	public void onFertilize(FertilizationEvent.Fertilize event)
	{
		// disable cocoa fertilization completely
		if (event.block instanceof BlockCocoa)
		{
			AppleCoreExample.LOG.info(event.block + " fertilization denied");
			event.setResult(Result.DENY);
		}
		// custom fertilization effect for stems
		else if (event.block instanceof BlockStem)
		{
			// randomize the meta
			AppleCoreExample.LOG.info("randomizing meta of " + event.block + " using custom fertilization handling");
			IBlockState state = event.state.withProperty(BlockCrops.AGE, event.random.nextInt(7));
			event.world.setBlockState(event.pos, state, 1);
			// mark as handled
			event.setResult(Result.ALLOW);
		}
		else
		{
			AppleCoreExample.LOG.info(event.block + " is about to be fertilized (" + event.state.toString() + ")");
		}
	}

	@SubscribeEvent
	public void onFertilized(FertilizationEvent.Fertilized event)
	{
		AppleCoreExample.LOG.info(event.block + " was fertilized from " + event.previousState.toString() + " to " + event.currentState.toString());
	}
}