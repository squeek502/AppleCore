package squeek.applecore.api_impl;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.IAppleCoreDispatcher;
import squeek.applecore.api.plants.PlantGrowthEvent;

import java.util.Random;

public enum AppleCoreDispatcherImpl implements IAppleCoreDispatcher
{
	INSTANCE;

	private AppleCoreDispatcherImpl()
	{
		AppleCoreAPI.dispatcher = this;
	}

	@Override
	public Event.Result validatePlantGrowth(Block block, World world, BlockPos pos, IBlockState state, Random random)
	{
		PlantGrowthEvent.AllowGrowthTick event = new PlantGrowthEvent.AllowGrowthTick(block, world, pos, state, random);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult();
	}

	@Override
	public void announcePlantGrowth(Block block, World world, BlockPos pos, IBlockState state, int previousMetadata)
	{
		PlantGrowthEvent.GrowthTick event = new PlantGrowthEvent.GrowthTick(block, world, pos, state, previousMetadata);
		MinecraftForge.EVENT_BUS.post(event);
	}

	@Override
	public void announcePlantGrowthWithoutMetadataChange(Block block, World world, BlockPos pos, IBlockState state)
	{
		announcePlantGrowth(block, world, pos, state, state.getBlock().getMetaFromState(state));
	}
}