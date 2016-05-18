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
	public static final class HungerRegenEvent
	{
		public static final String PeacefulRegen = "squeek.applecore.api.hunger.HungerRegenEvent$PeacefulRegen";
	}
	public static final class StarvationEvent
	{
		public static final String Starve = "squeek.applecore.api.hunger.StarvationEvent$Starve";
	}

	//Java
	public static final String List = "java.util.List";
	public static final String Random = "java.util.Random";
	public static final String String = "java.lang.String";

	//Minecraft
	public static final String AbstractClientPlayer = "net.minecraft.client.entity.AbstractClientPlayer";
	public static final String Block = "net.minecraft.block.Block";
	public static final String BlockPos = "net.minecraft.util.math.BlockPos";
	public static final String DamageSource = "net.minecraft.util.DamageSource";
	public static final String EntityLiving = "net.minecraft.entity.EntityLivingBase";
	public static final String FontRenderer = "net.minecraft.client.gui.FontRenderer";
	public static final String FoodStats = "net.minecraft.util.FoodStats";
	public static final String GameRules = "net.minecraft.world.GameRules";
	public static final String Hand = "net.minecraft.util.EnumHand";
	public static final String HandSide = "net.minecraft.util.EnumHandSide";
	public static final String IBlockState = "net.minecraft.block.state.IBlockState";
	public static final String IGrowable = "net.minecraft.block.IGrowable";
	public static final String ItemFood = "net.minecraft.item.ItemFood";
	public static final String ItemRenderer = "net.minecraft.client.renderer.ItemRenderer";
	public static final String Minecraft = "net.minecraft.client.Minecraft";
	public static final String Player = "net.minecraft.entity.player.EntityPlayer";
	public static final String PlayerSP = "net.minecraft.client.entity.EntityPlayerSP";
	public static final String Stack = "net.minecraft.item.ItemStack";
	public static final String StatBase = "net.minecraft.stats.StatBase";
	public static final String StatList = "net.minecraft.stats.StatList";
	public static final String World = "net.minecraft.world.World";

	//FML & Forge
	public static final String EventResult = "net.minecraftforge.fml.common.eventhandler.Event$Result";
	public static final String GuiUtils = "net.minecraftforge.fml.client.config.GuiUtils";

	//Blocks
	public static final String Cactus =	"net.minecraft.block.BlockCactus";
	public static final String Cake = "net.minecraft.block.BlockCake";
	public static final String Cocoa = "net.minecraft.block.BlockCocoa";
	public static final String Crops = "net.minecraft.block.BlockCrops";
	public static final String Mushroom = "net.minecraft.block.BlockMushroom";
	public static final String NetherWart = "net.minecraft.block.BlockNetherWart";
	public static final String Reed = "net.minecraft.block.BlockReed";
	public static final String Sapling = "net.minecraft.block.BlockSapling";
	public static final String Stem = "net.minecraft.block.BlockStem";
}