package squeek.applecore.api_impl;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.IAppleCoreDispatcher;
import squeek.applecore.api.plants.PlantGrowthEvent;
import cpw.mods.fml.common.eventhandler.Event;

public enum AppleCoreDispatcherImpl implements IAppleCoreDispatcher
{
	INSTANCE;

	private AppleCoreDispatcherImpl()
	{
		AppleCoreAPI.dispatcher = this;
	}

	@Override
	public Event.Result validatePlantGrowth(Block block, World world, int x, int y, int z, Random random)
	{
		PlantGrowthEvent.AllowGrowthTick event = new PlantGrowthEvent.AllowGrowthTick(block, world, x, y, z, random);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult();
	}

	@Override
	public void announcePlantGrowth(Block block, World world, int x, int y, int z)
	{
		PlantGrowthEvent.GrowthTick event = new PlantGrowthEvent.GrowthTick(block, world, x, y, z);
		MinecraftForge.EVENT_BUS.post(event);
	}
}
