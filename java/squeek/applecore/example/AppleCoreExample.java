package squeek.applecore.example;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import squeek.applecore.ModInfo;

@Mod(modid = ModInfo.MODID + "Example", version = ModInfo.VERSION, dependencies = "required-after:AppleCore")
public class AppleCoreExample
{
	public static final Logger LOG = LogManager.getLogger(ModInfo.MODID + "Example");

	@Instance(ModInfo.MODID + "Example")
	public static AppleCoreExample instance;

	public static Item testFood;
	public static Item testMetadataFood;
	public static Block testBlockCrops;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		testFood = new ItemNonStandardFood().setUnlocalizedName("testNonStandardFood");
		GameRegistry.register(testFood, new ResourceLocation(ModInfo.MODID + "Example", "testNonStandardFood"));

		testMetadataFood = new ItemMetadataFood(new int[]{1, 10}, new float[]{2f, 0.1f}).setUnlocalizedName("testMetadataFood");
		GameRegistry.register(testMetadataFood, new ResourceLocation(ModInfo.MODID + "Example", "testMetadataFood"));

		testBlockCrops = new BlockCropsExample();
		GameRegistry.register(testBlockCrops, new ResourceLocation(ModInfo.MODID + "Example", "testBlockCrops"));
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
			MinecraftForge.EVENT_BUS.register(new HungerRegenModifier());
			MinecraftForge.EVENT_BUS.register(new StarvationModifier());
			MinecraftForge.EVENT_BUS.register(new PlantGrowthModifier());
		}
		if (event.getSide() == Side.CLIENT)
			MinecraftForge.EVENT_BUS.register(new FoodValuesTooltipHandler());
	}
}