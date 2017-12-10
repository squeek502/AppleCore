package squeek.applecore.asm;

import squeek.asmhelper.applecore.ASMHelper;

public class ASMConstants
{
	public static final String HOOKS = "squeek.applecore.asm.Hooks";
	public static final String HOOKS_INTERNAL_CLASS = ASMHelper.toInternalClassName(HOOKS);
	public static final String FOOD_VALUES = "squeek.applecore.api.food.FoodValues";
	public static final String IAPPLECOREFOODSTATS = "squeek.applecore.asm.util.IAppleCoreFoodStats";
	public static final String IEDIBLEBLOCK = "squeek.applecore.api.food.IEdibleBlock";
	public static final String IEDIBLE = "squeek.applecore.api.food.IEdible";

	public static final class ExhaustionEvent
	{
		public static final String EXHAUSTED = "squeek.applecore.api.hunger.ExhaustionEvent$Exhausted";
	}
	public static final class HealthRegenEvent
	{
		public static final String REGEN = "squeek.applecore.api.hunger.HealthRegenEvent$Regen";
		public static final String PEACEFUL_REGEN = "squeek.applecore.api.hunger.HealthRegenEvent$PeacefulRegen";
	}
	public static final class HungerRegenEvent
	{
		public static final String PEACEFUL_REGEN = "squeek.applecore.api.hunger.HungerRegenEvent$PeacefulRegen";
	}
	public static final class StarvationEvent
	{
		public static final String STARVE = "squeek.applecore.api.hunger.StarvationEvent$Starve";
	}

	//Java
	public static final String LIST = "java.util.List";
	public static final String RANDOM = "java.util.Random";
	public static final String STRING = "java.lang.String";

	//Minecraft
	public static final String BLOCK = "net.minecraft.block.Block";
	public static final String BLOCK_POS = "net.minecraft.util.math.BlockPos";
	public static final String DAMAGE_SOURCE = "net.minecraft.util.DamageSource";
	public static final String ENTITY_LIVING = "net.minecraft.entity.EntityLivingBase";
	public static final String FONT_RENDERER = "net.minecraft.client.gui.FontRenderer";
	public static final String FOOD_STATS = "net.minecraft.util.FoodStats";
	public static final String GAME_RULES = "net.minecraft.world.GameRules";
	public static final String HAND = "net.minecraft.util.EnumHand";
	public static final String HAND_SIDE = "net.minecraft.util.EnumHandSide";
	public static final String IBLOCKSTATE = "net.minecraft.block.state.IBlockState";
	public static final String IGROWABLE = "net.minecraft.block.IGrowable";
	public static final String ITEM_FOOD = "net.minecraft.item.ItemFood";
	public static final String ITEM_RENDERER = "net.minecraft.client.renderer.ItemRenderer";
	public static final String MINECRAFT = "net.minecraft.client.Minecraft";
	public static final String PLAYER = "net.minecraft.entity.player.EntityPlayer";
	public static final String PLAYER_SP = "net.minecraft.client.entity.EntityPlayerSP";
	public static final String STACK = "net.minecraft.item.ItemStack";
	public static final String STAT_BASE = "net.minecraft.stats.StatBase";
	public static final String STAT_LIST = "net.minecraft.stats.StatList";
	public static final String WORLD = "net.minecraft.world.World";

	//FML & Forge
	public static final String EVENT_RESULT = "net.minecraftforge.fml.common.eventhandler.Event$Result";
	public static final String GUI_INGAME_FORGE = "net.minecraftforge.client.GuiIngameForge";

	//Blocks
	public static final String CACTUS =	"net.minecraft.block.BlockCactus";
	public static final String CAKE = "net.minecraft.block.BlockCake";
	public static final String COCOA = "net.minecraft.block.BlockCocoa";
	public static final String CROPS = "net.minecraft.block.BlockCrops";
	public static final String MUSHROOM = "net.minecraft.block.BlockMushroom";
	public static final String NETHER_WART = "net.minecraft.block.BlockNetherWart";
	public static final String REED = "net.minecraft.block.BlockReed";
	public static final String SAPLING = "net.minecraft.block.BlockSapling";
	public static final String STEM = "net.minecraft.block.BlockStem";
}