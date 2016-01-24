package squeek.applecore.api.plants;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Random;

public class PlantGrowthEvent extends Event
{
	/**
	 * Fired each plant update tick to determine whether or not growth is allowed for the {@link #block}.
	 * 
	 * This event is fired in various {@link Block#updateTick} overrides.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event uses the {@link Result}. {@link HasResult}<br>
	 * {@link Result#DEFAULT} will use the vanilla conditionals.
	 * {@link Result#ALLOW} will allow the growth tick without condition.
	 * {@link Result#DENY} will deny the growth tick without condition.
	 */
	@HasResult
	public static class AllowGrowthTick extends PlantGrowthEvent
	{
		public final Block block;
		public final World world;
		public final BlockPos pos;
		public final IBlockState state;
		public final Random random;

		public AllowGrowthTick(Block block, World world, BlockPos pos, IBlockState state, Random random)
		{
			this.block = block;
			this.world = world;
			this.pos = pos;
			this.state = state;
			this.random = random;
		}
	}

	/**
	 * Fired after a plant grows from a growth tick.
	 * 
	 * This event is fired in various {@link Block#updateTick} overrides.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 */
	public static class GrowthTick extends PlantGrowthEvent
	{
		public final Block block;
		public final World world;
		public final BlockPos pos;
		public final IBlockState state;
		public final int previousMetadata;

		public GrowthTick(Block block, World world, BlockPos pos, IBlockState state, int previousMetadata)
		{
			this.block = block;
			this.world = world;
			this.pos = pos;
			this.state = state;
			this.previousMetadata = previousMetadata;
		}

		public GrowthTick(Block block, World world, BlockPos pos, IBlockState state)
		{
			this(block, world, pos, state, state.getBlock().getMetaFromState(state));
		}
	}
}