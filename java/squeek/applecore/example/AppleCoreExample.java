package squeek.applecore.example;

import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import squeek.applecore.ModInfo;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = ModInfo.MODID + "Example", version = ModInfo.VERSION, dependencies = "")
public class AppleCoreExample
{
	public static final Logger Log = LogManager.getLogger(ModInfo.MODID + "Example");

	@Instance(ModInfo.MODID + "Example")
	public static AppleCoreExample instance;

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new EatingSpeedModifier());
		MinecraftForge.EVENT_BUS.register(new ExhaustionModifier());
		MinecraftForge.EVENT_BUS.register(new FoodEatenResult());
		MinecraftForge.EVENT_BUS.register(new FoodStatsAdditionCanceler());
		MinecraftForge.EVENT_BUS.register(new FoodValuesModifier());
		MinecraftForge.EVENT_BUS.register(new HealthRegenModifier());
		MinecraftForge.EVENT_BUS.register(new StarvationModifier());
		MinecraftForge.EVENT_BUS.register(new FoodValuesTooltipHandler());
	}
}
