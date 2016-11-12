package squeek.applecore.api_impl;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import static net.minecraftforge.event.world.BlockEvent.CropGrowEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
		MinecraftForge.EVENT_BUS.register(this);
	}

	// dispatch the Forge events rather than the deprecated AppleCore events here, thus making users of these
	// deprecated methods forwards compatible
	@Override
	public Event.Result validatePlantGrowth(Block block, World world, BlockPos pos, IBlockState state, Random random)
	{
		CropGrowEvent.Pre event = new CropGrowEvent.Pre(world, pos, state);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult();
	}

	@Override
	public void announcePlantGrowth(Block block, World world, BlockPos pos, IBlockState currentState, IBlockState previousState)
	{
		CropGrowEvent.Post event = new CropGrowEvent.Post(world, pos, previousState, currentState);
		MinecraftForge.EVENT_BUS.post(event);
	}

	@Override
	public void announcePlantGrowth(Block block, World world, BlockPos pos, IBlockState previousState)
	{
		CropGrowEvent.Post event = new CropGrowEvent.Post(world, pos, previousState, world.getBlockState(pos));
		MinecraftForge.EVENT_BUS.post(event);
	}

	private static Random random;

	// forward the Forge events to the deprecated AppleCore equivalents, so that
	// any event handlers that use the AppleCore events still work
	@SubscribeEvent
	public void onCropGrowEventPre(CropGrowEvent.Pre event)
	{
		PlantGrowthEvent.AllowGrowthTick appleCoreEvent = new PlantGrowthEvent.AllowGrowthTick(event.getState().getBlock(), event.getWorld(), event.getPos(), event.getState(), random);
		appleCoreEvent.setResult(event.getResult());
		MinecraftForge.EVENT_BUS.post(appleCoreEvent);
		event.setResult(appleCoreEvent.getResult());
	}

	@SubscribeEvent
	public void onCropGrowEventPost(CropGrowEvent.Post event)
	{
		PlantGrowthEvent.GrowthTick appleCoreEvent = new PlantGrowthEvent.GrowthTick(event.getState().getBlock(), event.getWorld(), event.getPos(), event.getState(), event.getOriginalState());
		MinecraftForge.EVENT_BUS.post(appleCoreEvent);
	}
}