package squeek.applecore.example;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import squeek.applecore.ModInfo;

// TODO
//@Mod.EventBusSubscriber
//@Mod(ModInfo.MODID + "example")
public class AppleCoreExample
{
	public static final Logger LOG = LogManager.getLogger(ModInfo.MODID + "example");

	public AppleCoreExample()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
	}

	/* TODO
	public static Item testFood;

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		testFood = new ItemNonStandardFood().setUnlocalizedName("testNonStandardFood");
		testFood.setRegistryName(new ResourceLocation(ModInfo.MODID + "example", "testNonStandardFood"));
		event.getRegistry().register(testFood);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event)
	{
		ModelLoader.setCustomModelResourceLocation(testFood, 0, new ModelResourceLocation("potato"));
		ModelLoader.setCustomModelResourceLocation(testMetadataFood, 0, new ModelResourceLocation("potato"));
		ModelLoader.setCustomModelResourceLocation(testMetadataFood, 1, new ModelResourceLocation("potato"));
	}
	 */

	public void init(FMLCommonSetupEvent event)
	{
		// only add in the test modifications if we are 'alone' in the dev environment
		boolean otherDependantModsExist = false;

		for (net.minecraftforge.fml.loading.moddiscovery.ModInfo mod : ModList.get().getMods())
		{
			if (mod.getModId() == ModInfo.MODID + "example")
				continue;

			for (IModInfo.ModVersion dependency : mod.getDependencies())
			{
				if (dependency.getModId().equals(ModInfo.MODID))
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
	}

	public void initClient(FMLClientSetupEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new FoodValuesTooltipHandler());
	}
}