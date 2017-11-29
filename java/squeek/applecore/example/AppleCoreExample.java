package squeek.applecore.example;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import squeek.applecore.ModInfo;

@Mod.EventBusSubscriber
@Mod(modid = ModInfo.MODID + "example", version = ModInfo.VERSION, dependencies = "required-after:applecore")
public class AppleCoreExample
{
	public static final Logger LOG = LogManager.getLogger(ModInfo.MODID + "example");

	@Instance(ModInfo.MODID + "example")
	public static AppleCoreExample instance;

	public static Item testFood;
	public static Item testMetadataFood;

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		testFood = new ItemNonStandardFood().setUnlocalizedName("testNonStandardFood");
		testFood.setRegistryName(new ResourceLocation(ModInfo.MODID + "example", "testNonStandardFood"));
		event.getRegistry().register(testFood);

		testMetadataFood = new ItemMetadataFood(new int[]{1, 10}, new float[]{2f, 0.1f}).setUnlocalizedName("testMetadataFood");
		testMetadataFood.setRegistryName(new ResourceLocation(ModInfo.MODID + "example", "testMetadataFood"));
		event.getRegistry().register(testMetadataFood);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event)
	{
		ModelLoader.setCustomModelResourceLocation(testFood, 0, new ModelResourceLocation("potato"));
		ModelLoader.setCustomModelResourceLocation(testMetadataFood, 0, new ModelResourceLocation("potato"));
		ModelLoader.setCustomModelResourceLocation(testMetadataFood, 1, new ModelResourceLocation("potato"));
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
			MinecraftForge.EVENT_BUS.register(new MaxHungerModifier());
		}
		if (event.getSide() == Side.CLIENT)
			MinecraftForge.EVENT_BUS.register(new FoodValuesTooltipHandler());
	}
}