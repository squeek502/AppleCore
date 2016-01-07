package squeek.applecore.asm;

import squeek.asmhelper.applecore.ASMHelper;

public class ASMConstants
{
	public static final String Hooks = "squeek.applecore.asm.Hooks";
	public static final String HooksInternalClass = ASMHelper.toInternalClassName(Hooks);
	public static final String FoodValues = "squeek.applecore.api.food.FoodValues";

	public static final class ExhaustionEvent
	{
		public static final String Exhausted = "squeek.applecore.api.hunger.ExhaustionEvent$Exhausted";
	}
	public static final class HealthRegenEvent
	{
		public static final String Regen = "squeek.applecore.api.hunger.HealthRegenEvent$Regen";
		public static final String PeacefulRegen = "squeek.applecore.api.hunger.HealthRegenEvent$PeacefulRegen";
	}
	public static final class StarvationEvent
	{
		public static final String Starve = "squeek.applecore.api.hunger.StarvationEvent$Starve";
	}

	public static final class HarvestCraft
	{
		public static final String BlockPamFruit = "com.pam.harvestcraft.BlockPamFruit";
		public static final String BlockPamSapling = "com.pam.harvestcraft.BlockPamSapling";
	}

	public static final String FoodStats = "net.minecraft.util.FoodStats";
	public static final String Block = "net.minecraft.block.Block";
	public static final String World = "net.minecraft.world.World";
	public static final String BlockPos = "net.minecraft.util.BlockPos";
	public static final String IBlockState = "net.minecraft.block.state.IBlockState";
	public static final String Player = "net.minecraft.entity.player.EntityPlayer";
}