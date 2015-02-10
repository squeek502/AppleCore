package squeek.applecore.api.plants;

import java.util.Random;
import net.minecraft.block.IGrowable;
import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

public class FertilizationEvent extends Event
{
	/**
	 * Fired to determine whether or not fertilization is allowed for the {@link IGrowable}.
	 * 
	 * This event is fired in all {@link IGrowable#func_149852_a} implementations.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event uses the {@link Result}. {@link HasResult}<br>
	 * {@link Result#DEFAULT} will use the vanilla conditionals.
	 * {@link Result#ALLOW} will allow fertilization without condition.
	 * {@link Result#DENY} will deny fertilization without condition.
	 */
	@HasResult
	public static class CanFertilize extends FertilizationEvent
	{
		public final IGrowable growable;
		public final World world;
		public final int x;
		public final int y;
		public final int z;
		public final Random random;

		public CanFertilize(IGrowable growable, World world, int x, int y, int z, Random random)
		{
			this.growable = growable;
			this.world = world;
			this.x = x;
			this.y = y;
			this.z = z;
			this.random = random;
		}
	}

	/**
	 * Fired when an IGrowable is about to be fertilized.
	 * 
	 * This event is fired in all {@link IGrowable#func_149853_b} implementations.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event uses the {@link Result}. {@link HasResult}<br>
	 * {@link Result#DEFAULT} will use the default fertilization implementation.
	 * {@link Result#ALLOW} will mark the fertilization as handled and skip the default implementation.
	 * {@link Result#DENY} will cancel the fertilization.
	 */
	@HasResult
	public static class Fertilize extends FertilizationEvent
	{
		public final IGrowable growable;
		public final World world;
		public final int x;
		public final int y;
		public final int z;
		public final Random random;
		public final int metadata;

		public Fertilize(IGrowable growable, World world, int x, int y, int z, Random random, int metadata)
		{
			this.growable = growable;
			this.world = world;
			this.x = x;
			this.y = y;
			this.z = z;
			this.random = random;
			this.metadata = metadata;
		}
	}

	/**
	 * Fired after an IGrowable is fertilized.
	 * 
	 * This event is fired in all {@link IGrowable#func_149853_b} implementations.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 */
	public static class Fertilized extends FertilizationEvent
	{
		public final IGrowable growable;
		public final World world;
		public final int x;
		public final int y;
		public final int z;
		public final int previousMetadata;

		public Fertilized(IGrowable growable, World world, int x, int y, int z, int previousMetadata)
		{
			this.growable = growable;
			this.world = world;
			this.x = x;
			this.y = y;
			this.z = z;
			this.previousMetadata = previousMetadata;
		}
	}
}
