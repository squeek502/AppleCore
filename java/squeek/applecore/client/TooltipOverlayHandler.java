package squeek.applecore.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import squeek.applecore.AppleCore;
import squeek.applecore.ModConfig;
import squeek.applecore.ModInfo;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.asm.Hooks;
import squeek.applecore.helpers.KeyHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TooltipOverlayHandler
{
	private static ResourceLocation modIcons = new ResourceLocation(ModInfo.MODID_LOWER, "textures/icons.png");

	public static void init()
	{
		FMLCommonHandler.instance().bus().register(new TooltipOverlayHandler());
	}
	//private static final Field guiLeft = ReflectionHelper.findField(GuiContainer.class, ObfuscationReflectionHelper.remapFieldNames(GuiContainer.class.getName(), "guiLeft", "field_147003_i", "i"));
	//private static final Field guiTop = ReflectionHelper.findField(GuiContainer.class, ObfuscationReflectionHelper.remapFieldNames(GuiContainer.class.getName(), "guiTop", "field_147009_r", "r"));
	public static final Field theSlot = ReflectionHelper.findField(GuiContainer.class, ObfuscationReflectionHelper.remapFieldNames(GuiContainer.class.getName(), "theSlot", "field_147006_u", "u"));
	private static Method getStackMouseOver = null;
	private static Field itemPanel = null;
	private static boolean neiLoaded = false;
	private static Class<?> foodJournalGui = null;
	private static Field foodJournalHoveredStack = null;
	static
	{
		try
		{
			neiLoaded = Loader.isModLoaded("NotEnoughItems");
			if (neiLoaded)
			{
				Class<?> LayoutManager = Class.forName("codechicken.nei.LayoutManager");
				itemPanel = LayoutManager.getDeclaredField("itemPanel");
				getStackMouseOver = Class.forName("codechicken.nei.ItemPanel").getDeclaredMethod("getStackMouseOver", int.class, int.class);
			}
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			AppleCore.Log.error("Unable to integrate the food values tooltip overlay with NEI: ");
			e.printStackTrace();
		}

		try
		{
			if (Loader.isModLoaded("SpiceOfLife"))
			{
				foodJournalGui = ReflectionHelper.getClass(TooltipOverlayHandler.class.getClassLoader(), "squeek.spiceoflife.gui.GuiScreenFoodJournal");
				foodJournalHoveredStack = ReflectionHelper.findField(foodJournalGui, "hoveredStack");
			}
		}
		catch (Exception e)
		{
			AppleCore.Log.error("Unable to integrate the food values tooltip overlay with The Spice of Life: ");
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public void onRenderTick(RenderTickEvent event)
	{
		if (event.phase != TickEvent.Phase.END)
			return;

		if ((ModConfig.SHOW_FOOD_VALUES_IN_TOOLTIP && KeyHelper.isShiftKeyDown()) || ModConfig.ALWAYS_SHOW_FOOD_VALUES_TOOLTIP)
		{
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer player = mc.thePlayer;
			GuiScreen curScreen = mc.currentScreen;

			ScaledResolution scale = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

			boolean isFoodJournalGui = foodJournalGui != null && foodJournalGui.isInstance(curScreen);
			boolean isValidContainerGui = curScreen instanceof GuiContainer;
			if (isValidContainerGui)
			{
				Gui gui = curScreen;
				int mouseX = Mouse.getX() * scale.getScaledWidth() / mc.displayWidth;
				int mouseY = scale.getScaledHeight() - Mouse.getY() * scale.getScaledHeight() / mc.displayHeight;
				ItemStack hoveredStack = null;

				// get the hovered stack from the active container
				try
				{
					// try regular container
					Slot hoveredSlot = (Slot) TooltipOverlayHandler.theSlot.get(gui);

					// get the stack
					if (hoveredSlot != null)
						hoveredStack = hoveredSlot.getStack();

					// try NEI
					if (hoveredStack == null && getStackMouseOver != null)
						hoveredStack = (ItemStack) (getStackMouseOver.invoke(itemPanel.get(null), mouseX, mouseY));

					// try FoodJournal
					if (hoveredStack == null && isFoodJournalGui)
						hoveredStack = (ItemStack) foodJournalHoveredStack.get(gui);
				}
				catch (RuntimeException e)
				{
					throw e;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				// if the hovered stack is a food and there is no item being dragged
				if (player.inventory.getItemStack() == null && hoveredStack != null && AppleCoreAPI.accessor.isFood(hoveredStack))
				{
					FoodValues defaultFoodValues = FoodValues.get(hoveredStack);
					FoodValues modifiedFoodValues = FoodValues.get(hoveredStack, player);

					if (defaultFoodValues.equals(modifiedFoodValues) && defaultFoodValues.hunger == 0 && defaultFoodValues.saturationModifier == 0)
						return;

					int biggestHunger = Math.max(defaultFoodValues.hunger, modifiedFoodValues.hunger);
					float biggestSaturationIncrement = Math.max(defaultFoodValues.getSaturationIncrement(), modifiedFoodValues.getSaturationIncrement());

					int barsNeeded = (int) Math.ceil(Math.abs(biggestHunger) / 2f);
					int saturationBarsNeeded = (int) Math.max(1, Math.ceil(Math.abs(biggestSaturationIncrement) / 2f));
					boolean saturationOverflow = saturationBarsNeeded > 10;
					String saturationText = saturationOverflow ? ((defaultFoodValues.saturationModifier < 0 ? -1 : 1) * saturationBarsNeeded) + "x " : null;
					if (saturationOverflow)
						saturationBarsNeeded = 1;

					boolean needsCoordinateShift = !neiLoaded || isFoodJournalGui;
					//int toolTipTopY = Hooks.toolTipY;
					//int toolTipLeftX = Hooks.toolTipX;
					int toolTipBottomY = Hooks.toolTipY + Hooks.toolTipH + 1 + (needsCoordinateShift ? 3 : 0);
					int toolTipRightX = Hooks.toolTipX + Hooks.toolTipW + 1 + (needsCoordinateShift ? 3 : 0);

					boolean shouldDrawBelow = toolTipBottomY + 20 < scale.getScaledHeight() - 3;

					int rightX = toolTipRightX - 3;
					int leftX = rightX - (Math.max(barsNeeded * 9, saturationBarsNeeded * 6 + (int) (mc.fontRenderer.getStringWidth(saturationText) * 0.75f))) - 4;
					int topY = (shouldDrawBelow ? toolTipBottomY : Hooks.toolTipY - 20 + (needsCoordinateShift ? -4 : 0));
					int bottomY = topY + 20;

					boolean wasLightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
					if (wasLightingEnabled)
						GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glDisable(GL11.GL_DEPTH_TEST);

					// bg
					Gui.drawRect(leftX - 1, topY, rightX + 1, bottomY, 0xF0100010);
					Gui.drawRect(leftX, (shouldDrawBelow ? bottomY : topY - 1), rightX, (shouldDrawBelow ? bottomY + 1 : topY), 0xF0100010);
					Gui.drawRect(leftX, topY, rightX, bottomY, 0x66FFFFFF);

					int x = rightX - 2;
					int startX = x;
					int y = bottomY - 19;

					GL11.glColor4f(1f, 1f, 1f, .25f);

					mc.getTextureManager().bindTexture(Gui.icons);

					for (int i = 0; i < barsNeeded * 2; i += 2)
					{
						x -= 9;

						if (modifiedFoodValues.hunger < 0)
							gui.drawTexturedModalRect(x, y, 34, 27, 9, 9);
						else if (modifiedFoodValues.hunger > defaultFoodValues.hunger && defaultFoodValues.hunger <= i)
							gui.drawTexturedModalRect(x, y, 133, 27, 9, 9);
						else if (modifiedFoodValues.hunger > i + 1 || defaultFoodValues.hunger == modifiedFoodValues.hunger)
							gui.drawTexturedModalRect(x, y, 16, 27, 9, 9);
						else if (modifiedFoodValues.hunger == i + 1)
							gui.drawTexturedModalRect(x, y, 124, 27, 9, 9);
						else
							gui.drawTexturedModalRect(x, y, 34, 27, 9, 9);

						GL11.glEnable(GL11.GL_BLEND);
						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
						gui.drawTexturedModalRect(x, y, defaultFoodValues.hunger - 1 == i ? 115 : 106, 27, 9, 9);
						GL11.glDisable(GL11.GL_BLEND);

						if (modifiedFoodValues.hunger > i)
							gui.drawTexturedModalRect(x, y, modifiedFoodValues.hunger - 1 == i ? 61 : 52, 27, 9, 9);
					}

					y += 11;
					x = startX;
					float modifiedSaturationIncrement = modifiedFoodValues.getSaturationIncrement();
					float absModifiedSaturationIncrement = Math.abs(modifiedSaturationIncrement);

					GL11.glPushMatrix();
					GL11.glScalef(0.75F, 0.75F, 0.75F);
					GL11.glColor4f(1f, 1f, 1f, .5f);
					for (int i = 0; i < saturationBarsNeeded * 2; i += 2)
					{
						float effectiveSaturationOfBar = (absModifiedSaturationIncrement - i) / 2f;

						x -= 6;

						boolean shouldBeFaded = absModifiedSaturationIncrement <= i;
						if (shouldBeFaded)
						{
							GL11.glEnable(GL11.GL_BLEND);
							GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
						}

						mc.getTextureManager().bindTexture(Gui.icons);
						gui.drawTexturedModalRect(x * 4 / 3, y * 4 / 3, 16, 27, 9, 9);

						mc.getTextureManager().bindTexture(modIcons);
						gui.drawTexturedModalRect(x * 4 / 3, y * 4 / 3, effectiveSaturationOfBar >= 1 ? 27 : effectiveSaturationOfBar > 0.5 ? 18 : effectiveSaturationOfBar > 0.25 ? 9 : effectiveSaturationOfBar > 0 ? 0 : 36, modifiedSaturationIncrement >= 0 ? 0 : 9, 9, 9);

						if (shouldBeFaded)
							GL11.glDisable(GL11.GL_BLEND);
					}
					if (saturationText != null)
					{
						mc.fontRenderer.drawStringWithShadow(saturationText, x * 4 / 3 - mc.fontRenderer.getStringWidth(saturationText) + 2, y * 4 / 3 + 1, 0xFFFF0000);
					}
					GL11.glPopMatrix();

					GL11.glEnable(GL11.GL_DEPTH_TEST);
					if (wasLightingEnabled)
						GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glColor4f(1f, 1f, 1f, 1f);
				}
			}
		}
	}

}
