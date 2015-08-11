package squeek.applecore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import squeek.applecore.ModConfig;
import squeek.applecore.ModInfo;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.food.FoodValues;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HUDOverlayHandler
{
	float flashAlpha = 0f;
	byte alphaDir = 1;

	private static final ResourceLocation modIcons = new ResourceLocation(ModInfo.MODID_LOWER, "textures/icons.png");

	public static void init()
	{
		HUDOverlayHandler hudOverlayHandler = new HUDOverlayHandler();
		FMLCommonHandler.instance().bus().register(hudOverlayHandler);
		MinecraftForge.EVENT_BUS.register(hudOverlayHandler);
	}

	@SubscribeEvent(priority=EventPriority.LOW)
	public void onPreRender(RenderGameOverlayEvent.Pre event)
	{
		if (event.isCanceled())
			return;

		if (event.type != RenderGameOverlayEvent.ElementType.FOOD)
			return;

		if (!ModConfig.SHOW_FOOD_EXHAUSTION_UNDERLAY)
			return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;

		ScaledResolution scale = event.resolution;

		int left = scale.getScaledWidth() / 2 + 91;
		int top = scale.getScaledHeight() - GuiIngameForge.right_height;

		drawExhaustionOverlay(AppleCoreAPI.accessor.getExhaustion(player), mc, left, top, 1f);
	}

	@SubscribeEvent(priority=EventPriority.LOW)
	public void onRender(RenderGameOverlayEvent.Post event)
	{
		if (event.isCanceled())
			return;

		if (event.type != RenderGameOverlayEvent.ElementType.FOOD)
			return;

		if (!ModConfig.SHOW_FOOD_VALUES_OVERLAY && !ModConfig.SHOW_SATURATION_OVERLAY)
			return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		ItemStack heldItem = player.getHeldItem();
		FoodStats stats = player.getFoodStats();

		ScaledResolution scale = event.resolution;

		int left = scale.getScaledWidth() / 2 + 91;
		int top = scale.getScaledHeight() - GuiIngameForge.right_height + 10;

		// saturation overlay
		if (ModConfig.SHOW_SATURATION_OVERLAY)
			drawSaturationOverlay(0, stats.getSaturationLevel(), mc, left, top, 1f);

		if (!ModConfig.SHOW_FOOD_VALUES_OVERLAY || heldItem == null || !AppleCoreAPI.accessor.isFood(heldItem))
		{
			flashAlpha = 0;
			alphaDir = 1;
			return;
		}

		// restored hunger/saturation overlay while holding food
		FoodValues foodValues = FoodValues.get(heldItem, player);
		drawHungerOverlay(foodValues.hunger, stats.getFoodLevel(), mc, left, top, flashAlpha);
		int newFoodValue = stats.getFoodLevel() + foodValues.hunger;
		float newSaturationValue = stats.getSaturationLevel() + foodValues.getSaturationIncrement();
		drawSaturationOverlay(newSaturationValue > newFoodValue ? newFoodValue - stats.getSaturationLevel() : foodValues.getSaturationIncrement(), stats.getSaturationLevel(), mc, left, top, flashAlpha);
	}

	public static void drawSaturationOverlay(float saturationGained, float saturationLevel, Minecraft mc, int left, int top, float alpha)
	{
		if (saturationLevel + saturationGained < 0)
			return;

		int startBar = saturationGained != 0 ? Math.max(0, (int) saturationLevel / 2) : 0;
		int endBar = (int) Math.ceil(Math.min(20, saturationLevel + saturationGained) / 2f);
		int barsNeeded = endBar - startBar;
		mc.getTextureManager().bindTexture(modIcons);

		enableAlpha(alpha);
		for (int i = startBar; i < startBar + barsNeeded; ++i)
		{
			int x = left - i * 8 - 9;
			int y = top;
			float effectiveSaturationOfBar = (saturationLevel + saturationGained) / 2 - i;

			if (effectiveSaturationOfBar >= 1)
				mc.ingameGUI.drawTexturedModalRect(x, y, 27, 0, 9, 9);
			else if (effectiveSaturationOfBar > .5)
				mc.ingameGUI.drawTexturedModalRect(x, y, 18, 0, 9, 9);
			else if (effectiveSaturationOfBar > .25)
				mc.ingameGUI.drawTexturedModalRect(x, y, 9, 0, 9, 9);
			else if (effectiveSaturationOfBar > 0)
				mc.ingameGUI.drawTexturedModalRect(x, y, 0, 0, 9, 9);
		}
		disableAlpha(alpha);

		// rebind default icons
		mc.getTextureManager().bindTexture(Gui.icons);
	}

	public static void drawHungerOverlay(int hungerRestored, int foodLevel, Minecraft mc, int left, int top, float alpha)
	{
		if (hungerRestored == 0)
			return;

		int startBar = foodLevel / 2;
		int endBar = (int) Math.ceil(Math.min(20, foodLevel + hungerRestored) / 2f);
		int barsNeeded = endBar - startBar;
		mc.getTextureManager().bindTexture(Gui.icons);

		enableAlpha(alpha);
		for (int i = startBar; i < startBar + barsNeeded; ++i)
		{
			int idx = i * 2 + 1;
			int x = left - i * 8 - 9;
			int y = top;
			int icon = 16;
			int background = 13;

			if (mc.thePlayer.isPotionActive(Potion.hunger))
			{
				icon += 36;
				background = 13;
			}

			mc.ingameGUI.drawTexturedModalRect(x, y, 16 + background * 9, 27, 9, 9);

			if (idx < foodLevel + hungerRestored)
				mc.ingameGUI.drawTexturedModalRect(x, y, icon + 36, 27, 9, 9);
			else if (idx == foodLevel + hungerRestored)
				mc.ingameGUI.drawTexturedModalRect(x, y, icon + 45, 27, 9, 9);
		}
		disableAlpha(alpha);
	}

	public static void drawExhaustionOverlay(float exhaustion, Minecraft mc, int left, int top, float alpha)
	{
		mc.getTextureManager().bindTexture(modIcons);

		float maxExhaustion = AppleCoreAPI.accessor.getMaxExhaustion(mc.thePlayer);
		float ratio = exhaustion / maxExhaustion;
		int width = (int) (ratio * 81);
		int height = 9;

		enableAlpha(.75f);
		mc.ingameGUI.drawTexturedModalRect(left - width, top, 81 - width, 18, width, height);
		disableAlpha(.75f);

		// rebind default icons
		mc.getTextureManager().bindTexture(Gui.icons);
	}

	public static void enableAlpha(float alpha)
	{
		if (alpha == 1f)
			return;

		GL11.glColor4f(1f, 1f, 1f, alpha);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	public static void disableAlpha(float alpha)
	{
		if (alpha == 1f)
			return;

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glColor4f(1f, 1f, 1f, 1f);
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event)
	{
		if (event.phase != TickEvent.Phase.END)
			return;

		flashAlpha += alphaDir * 0.125f;
		if (flashAlpha >= 1.5f)
		{
			flashAlpha = 1f;
			alphaDir = -1;
		}
		else if (flashAlpha <= -0.5f)
		{
			flashAlpha = 0f;
			alphaDir = 1;
		}
	}
}
