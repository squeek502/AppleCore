package squeek.applecore.example;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import squeek.applecore.ModInfo;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = ModInfo.MODID + "Example", version = ModInfo.VERSION, dependencies = "required-after:AppleCore")
public class AppleCoreExample
{
	public static final Logger Log = LogManager.getLogger(ModInfo.MODID + "Example");

	@Instance(ModInfo.MODID + "Example")
	public static AppleCoreExample instance;

	public static Item testFood;
	public static Item testMetadataFood;
	public static Block testBlockCrops;
	public static Block testBlockEdible;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		testFood = new ItemNonStandardFood().setUnlocalizedName("testNonStandardFood");
		GameRegistry.registerItem(testFood, "testNonStandardFood");

		testMetadataFood = new ItemMetadataFood(new int[]{1, 10}, new float[]{2f, 0.1f}).setUnlocalizedName("testMetadataFood");
		GameRegistry.registerItem(testMetadataFood, "testMetadataFood");

		testBlockCrops = new BlockCropsExample();
		GameRegistry.registerBlock(testBlockCrops, "testBlockCrops");

		testBlockEdible = new BlockEdibleExample();
		GameRegistry.registerBlock(testBlockEdible, "testBlockEdible");
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		// only add in the test modifications if we are 'alone' in the dev environment
		boolean otherDependantModsExist = false;
		for (ModContainer mod : Loader.instance().getActiveModList())
		{
			if (mod.getMod() == this)
				continue;

			for (ArtifactVersion dependency : mod.getRequirements())
			{
				if (dependency.getLabel().equals(ModInfo.MODID))
				{
					otherDependantModsExist = true;
					break;
				}
			}
			if (otherDependantModsExist)
				break;
		}
		if (!otherDependantModsExist)
		{
			MinecraftForge.EVENT_BUS.register(new EatingSpeedModifier());
			MinecraftForge.EVENT_BUS.register(new ExhaustionModifier());
			MinecraftForge.EVENT_BUS.register(new FoodEatenResult());
			MinecraftForge.EVENT_BUS.register(new FoodStatsAdditionCanceler());
			MinecraftForge.EVENT_BUS.register(new FoodValuesModifier());
			MinecraftForge.EVENT_BUS.register(new HealthRegenModifier());
			MinecraftForge.EVENT_BUS.register(new StarvationModifier());
			MinecraftForge.EVENT_BUS.register(new PlantGrowthModifier());
		}
		if (event.getSide() == Side.CLIENT)
			MinecraftForge.EVENT_BUS.register(new FoodValuesTooltipHandler());
	}
}
