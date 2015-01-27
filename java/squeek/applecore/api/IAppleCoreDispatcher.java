package squeek.applecore.api;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import squeek.applecore.api.plants.PlantGrowthEvent;
import cpw.mods.fml.common.eventhandler.Event;

public interface IAppleCoreDispatcher
{
	/**
	 * Fires a {@link PlantGrowthEvent.AllowGrowthTick} event and returns the result.
	 * 
	 * See {@link PlantGrowthEvent.AllowGrowthTick} for how to use the result.
	 */
	public Event.Result validatePlantGrowth(Block block, World world, int x, int y, int z, Random random);

	/**
	 * Fires a {@link PlantGrowthEvent.GrowthTick} event.
	 */
	public void announcePlantGrowth(Block block, World world, int x, int y, int z);
}
