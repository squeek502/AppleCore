package squeek.applecore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import squeek.applecore.AppleCore;
import squeek.applecore.ModConfig;
import squeek.applecore.ModInfo;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.asm.Hooks;
import squeek.applecore.helpers.KeyHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@SideOnly(Side.CLIENT)
public class TooltipOverlayHandler
{
	private static ResourceLocation modIcons = new ResourceLocation(ModInfo.MODID_LOWER, "textures/icons.png");

	public static void init()
	{
		MinecraftForge.EVENT_BUS.register(new TooltipOverlayHandler());
	}

	public static final Field THE_SLOT = ReflectionHelper.findField(GuiContainer.class, ObfuscationReflectionHelper.remapFieldNames(GuiContainer.class.getName(), "theSlot", "field_147006_u", "u"));
	private static Method getStackMouseOver = null;
	private static Field itemPanel = null;
	private static boolean neiLoaded = false;
	private static Method jeiGetRuntime = null;
	private static Class<?> JeiRuntime = null;
	private static Method jeiGetItemListOverlay = null;
	private static Method jeiGetRecipesGui = null;
	private static Class<?> RecipesGui = null;
	private static Method jeiGetFocusUnderMouse = null;
	private static Method jeiRecipesGetFocusUnderMouse = null;
	private static Field jeiFocus_itemStack = null;
	private static Field jeiRecipeLayouts = null;
	private static Field jeiRecipesGui_guiLeft = null;
	private static Field jeiRecipesGui_guiTop = null;
	private static Field jeiRecipeLayoutPosX = null;
	private static Field jeiRecipeLayoutPosY = null;
	private static Method jeiRecipeLayout_getFocusUnderMouse = null;
	private static boolean jeiLoaded = false;
	private static Class<?> foodJournalGui = null;
	private static Field foodJournalHoveredStack = null;
	static
	{
		try
		{
			jeiLoaded = Loader.isModLoaded("JEI");
			if (jeiLoaded)
			{
				Class<?> jeiInternal = Class.forName("mezz.jei.Internal");
				jeiGetRuntime = jeiInternal.getDeclaredMethod("getRuntime");
				JeiRuntime = Class.forName("mezz.jei.JeiRuntime");
				jeiGetItemListOverlay = JeiRuntime.getDeclaredMethod("getItemListOverlay");
				jeiGetRecipesGui = JeiRuntime.getDeclaredMethod("getRecipesGui");

				Class<?> ItemListOverlay = Class.forName("mezz.jei.gui.ItemListOverlay");
				jeiGetFocusUnderMouse = ItemListOverlay.getDeclaredMethod("getFocusUnderMouse", int.class, int.class);

				RecipesGui = Class.forName("mezz.jei.gui.RecipesGui");
				jeiRecipesGetFocusUnderMouse = RecipesGui.getDeclaredMethod("getFocusUnderMouse", int.class, int.class);

				jeiRecipeLayouts = RecipesGui.getDeclaredField("recipeLayouts");
				jeiRecipeLayouts.setAccessible(true);
				jeiRecipesGui_guiLeft = RecipesGui.getDeclaredField("guiLeft");
				jeiRecipesGui_guiLeft.setAccessible(true);
				jeiRecipesGui_guiTop = RecipesGui.getDeclaredField("guiTop");
				jeiRecipesGui_guiTop.setAccessible(true);
				Class<?> RecipeLayout = Class.forName("mezz.jei.gui.RecipeLayout");
				jeiRecipeLayoutPosX = RecipeLayout.getDeclaredField("posX");
				jeiRecipeLayoutPosX.setAccessible(true);
				jeiRecipeLayoutPosY = RecipeLayout.getDeclaredField("posY");
				jeiRecipeLayoutPosY.setAccessible(true);
				jeiRecipeLayout_getFocusUnderMouse = RecipeLayout.getDeclaredMethod("getFocusUnderMouse", int.class, int.class);

				Class<?> Focus = Class.forName("mezz.jei.gui.Focus");
				jeiFocus_itemStack = Focus.getDeclaredField("stack");
				jeiFocus_itemStack.setAccessible(true);
			}
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			AppleCore.Log.error("Unable to integrate the food values tooltip overlay with JEI: ");
			e.printStackTrace();
		}

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

			ScaledResolution scale = new ScaledResolution(mc);

			boolean isFoodJournalGui = foodJournalGui != null && foodJournalGui.isInstance(curScreen);
			boolean isJEIRecipesGui = RecipesGui != null && RecipesGui.isInstance(curScreen);
			boolean isValidContainerGui = curScreen instanceof GuiContainer;
			if (isValidContainerGui || isJEIRecipesGui)
			{
				Gui gui = curScreen;
				int mouseX = Mouse.getX() * scale.getScaledWidth() / mc.displayWidth;
				int mouseY = scale.getScaledHeight() - Mouse.getY() * scale.getScaledHeight() / mc.displayHeight;
				ItemStack hoveredStack = null;

				// get the hovered stack from the active container
				try
				{
					// try JEI recipe handler
					if (jeiFocus_itemStack != null)
					{
						Object jeiRuntime = jeiGetRuntime.invoke(null);

						// try to get the hovered stack from the current recipe if possible
						Object recipesGui = isJEIRecipesGui ? curScreen : jeiGetRecipesGui.invoke(jeiRuntime);
						if (isJEIRecipesGui)
						{
							Object recipesFocus = jeiRecipesGetFocusUnderMouse.invoke(curScreen, mouseX, mouseY);
							if (recipesFocus != null)
								hoveredStack = (ItemStack) jeiFocus_itemStack.get(recipesFocus);
						}

						// next try to get the hovered stack from the right-hand item list
						if (hoveredStack == null)
						{
							Object itemList = jeiGetItemListOverlay.invoke(jeiRuntime);
							Object listFocus = jeiGetFocusUnderMouse.invoke(itemList, mouseX, mouseY);
							if (listFocus != null)
								hoveredStack = (ItemStack) jeiFocus_itemStack.get(listFocus);
						}
						else
						{
							// ::gross code alert::
							// when the hoveredStack is in the RecipesGui,
							// tooltips are drawn using a translated Gl matrix, so
							// we need to turn the relative x/y coords back into absolute ones
							// unfortunately, we have to recalculate which RecipeLayout is hovered
							// in order to do this
							Object hoveredLayout = null;
							int guiLeft = jeiRecipesGui_guiLeft.getInt(recipesGui);
							int guiTop = jeiRecipesGui_guiTop.getInt(recipesGui);
							int recipeMouseX = mouseX - guiLeft;
							int recipeMouseY = mouseY - guiTop;
							List<Object> recipeLayouts = (List<Object>) jeiRecipeLayouts.get(recipesGui);
							for (Object recipeLayout : recipeLayouts)
							{
								if (jeiRecipeLayout_getFocusUnderMouse.invoke(recipeLayout, recipeMouseX, recipeMouseY) != null)
								{
									hoveredLayout = recipeLayout;
									break;
								}
							}

							if (hoveredLayout != null)
							{
								Hooks.toolTipX += jeiRecipeLayoutPosX.getInt(hoveredLayout) + guiLeft;
								Hooks.toolTipY += jeiRecipeLayoutPosY.getInt(hoveredLayout) + guiTop;
							}
						}
					}

					// RecipesGui is not a GuiContainer, so any code beyond this point is not applicable
					if (hoveredStack == null && isJEIRecipesGui)
						return;

					// try regular container
					if (hoveredStack == null)
					{
						Slot hoveredSlot = (Slot) TooltipOverlayHandler.THE_SLOT.get(gui);

						// get the stack
						if (hoveredSlot != null)
							hoveredStack = hoveredSlot.getStack();
					}

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
					int leftX = rightX - (Math.max(barsNeeded * 9, saturationBarsNeeded * 6 + (int) (mc.fontRendererObj.getStringWidth(saturationText) * 0.75f))) - 4;
					int topY = (shouldDrawBelow ? toolTipBottomY : Hooks.toolTipY - 20 + (needsCoordinateShift ? -4 : 0));
					int bottomY = topY + 20;

					GlStateManager.disableLighting();
					GlStateManager.disableDepth();

					// bg
					Gui.drawRect(leftX - 1, topY, rightX + 1, bottomY, 0xF0100010);
					Gui.drawRect(leftX, (shouldDrawBelow ? bottomY : topY - 1), rightX, (shouldDrawBelow ? bottomY + 1 : topY), 0xF0100010);
					Gui.drawRect(leftX, topY, rightX, bottomY, 0x66FFFFFF);

					// drawRect disables blending and modifies color, so reset them
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
					GlStateManager.enableBlend();
					GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

					int x = rightX - 2;
					int startX = x;
					int y = bottomY - 19;

					mc.getTextureManager().bindTexture(Gui.ICONS);

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

						GlStateManager.color(1.0F, 1.0F, 1.0F, .25F);
						gui.drawTexturedModalRect(x, y, defaultFoodValues.hunger - 1 == i ? 115 : 106, 27, 9, 9);
						GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

						if (modifiedFoodValues.hunger > i)
							gui.drawTexturedModalRect(x, y, modifiedFoodValues.hunger - 1 == i ? 61 : 52, 27, 9, 9);
					}

					y += 11;
					x = startX;
					float modifiedSaturationIncrement = modifiedFoodValues.getSaturationIncrement();
					float absModifiedSaturationIncrement = Math.abs(modifiedSaturationIncrement);

					GlStateManager.pushMatrix();
					GlStateManager.scale(0.75F, 0.75F, 0.75F);
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
					for (int i = 0; i < saturationBarsNeeded * 2; i += 2)
					{
						float effectiveSaturationOfBar = (absModifiedSaturationIncrement - i) / 2f;

						x -= 6;

						boolean shouldBeFaded = absModifiedSaturationIncrement <= i;
						if (shouldBeFaded)
							GlStateManager.color(1.0F, 1.0F, 1.0F, .5F);

						mc.getTextureManager().bindTexture(Gui.ICONS);
						gui.drawTexturedModalRect(x * 4 / 3, y * 4 / 3, 16, 27, 9, 9);

						mc.getTextureManager().bindTexture(modIcons);
						gui.drawTexturedModalRect(x * 4 / 3, y * 4 / 3, effectiveSaturationOfBar >= 1 ? 27 : effectiveSaturationOfBar > 0.5 ? 18 : effectiveSaturationOfBar > 0.25 ? 9 : effectiveSaturationOfBar > 0 ? 0 : 36, modifiedSaturationIncrement >= 0 ? 0 : 9, 9, 9);

						if (shouldBeFaded)
							GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
					}
					if (saturationText != null)
					{
						mc.fontRendererObj.drawStringWithShadow(saturationText, x * 4 / 3 - mc.fontRendererObj.getStringWidth(saturationText) + 2, y * 4 / 3 + 1, 0xFFFF0000);
					}
					GlStateManager.popMatrix();

					GlStateManager.disableBlend();
					GlStateManager.enableDepth();
					GlStateManager.enableLighting();
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				}
			}
		}
	}

}
