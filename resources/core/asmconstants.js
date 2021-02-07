var ASMConstants = {
	HOOKS: "squeek.applecore.asm.Hooks",
	FOOD_VALUES: "squeek.applecore.api.food.FoodValues",
	IAPPLECOREFOODSTATS: "squeek.applecore.asm.util.IAppleCoreFoodStats",
	IEDIBLEBLOCK: "squeek.applecore.api.food.IEdibleBlock",
	IEDIBLE: "squeek.applecore.api.food.IEdible",

	ExhaustionEvent:
	{
		EXHAUSTED: "squeek.applecore.api.hunger.ExhaustionEvent$Exhausted",
		EXHAUSTING_ACTIONS: "squeek.applecore.api.hunger.ExhaustionEvent$ExhaustingActions",
	},
	HealthRegenEvent:
	{
		REGEN: "squeek.applecore.api.hunger.HealthRegenEvent$Regen",
		PEACEFUL_REGEN: "squeek.applecore.api.hunger.HealthRegenEvent$PeacefulRegen",
	},
	HungerRegenEvent:
	{
		PEACEFUL_REGEN: "squeek.applecore.api.hunger.HungerRegenEvent$PeacefulRegen",
	},
	StarvationEvent:
	{
		STARVE: "squeek.applecore.api.hunger.StarvationEvent$Starve",
	},

	//Java
	RANDOM: "java.util.Random",

	//Minecraft
	BLOCK: "net.minecraft.block.Block",
	BLOCK_CONTAINER: "net.minecraft.block.BlockContainer",
	BLOCK_ICE: "net.minecraft.block.BlockIce",
	BLOCK_POS: "net.minecraft.util.math.BlockPos",
	DAMAGE_SOURCE: "net.minecraft.util.DamageSource",
	ENTITY: "net.minecraft.entity.Entity",
	ENTITY_LIVING: "net.minecraft.entity.EntityLivingBase",
	FOOD_STATS: "net.minecraft.util.FoodStats",
	HAND: "net.minecraft.util.EnumHand",
	HAND_SIDE: "net.minecraft.util.EnumHandSide",
	IBLOCKSTATE: "net.minecraft.block.state.IBlockState",
	IGROWABLE: "net.minecraft.block.IGrowable",
	ITEM_FOOD: "net.minecraft.item.ItemFood",
	ITEM_RENDERER: "net.minecraft.client.renderer.ItemRenderer",
	MINECRAFT: "net.minecraft.client.Minecraft",
	PLAYER: "net.minecraft.entity.player.PlayerEntity",
	PLAYER_SP: "net.minecraft.client.entity.PlayerEntitySP",
	POTION: "net.minecraft.potion.Potion",
	ITEM_STACK: "net.minecraft.item.ItemStack",
	TILE_ENTITY: "net.minecraft.tileentity.TileEntity",
	STAT_BASE: "net.minecraft.stats.StatBase",
	STAT_LIST: "net.minecraft.stats.StatList",
	WORLD: "net.minecraft.world.World",

	//FML & Forge
	GUI_INGAME_FORGE: "net.minecraftforge.client.GuiIngameForge",

	//Blocks
	CAKE: "net.minecraft.block.BlockCake",
}