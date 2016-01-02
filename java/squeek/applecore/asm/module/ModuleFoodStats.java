package squeek.applecore.asm.module;

import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.tree.*;
import squeek.applecore.asm.ASMConstants;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.asmhelper.applecore.ASMHelper;
import squeek.asmhelper.applecore.ObfHelper;

public class ModuleFoodStats implements IClassTransformerModule
{
	public static String foodStatsPlayerField = "entityplayer";

	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{"net.minecraft.entity.player.EntityPlayer", "net.minecraft.util.FoodStats"};
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (!ASMHelper.isCauldron() && transformedName.equals("net.minecraft.entity.player.EntityPlayer"))
		{
			ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "<init>", null);
			if (methodNode != null)
			{
				patchEntityPlayerInit(methodNode);
				return ASMHelper.writeClassToBytes(classNode);
			}
			else
				throw new RuntimeException("EntityPlayer: <init> method not found");
		}
		if (transformedName.equals("net.minecraft.util.FoodStats"))
		{
			ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

			if (!ASMHelper.isCauldron())
			{
				injectFoodStatsPlayerField(classNode);
				injectFoodStatsConstructor(classNode);
			}

			MethodNode addStatsMethodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_75122_a", "addStats", "(IF)V");
			if (addStatsMethodNode != null)
			{
				hookFoodStatsAddition(classNode, addStatsMethodNode);
			}
			else
				throw new RuntimeException("FoodStats: addStats(IF)V method not found");

			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_151686_a", "(Lnet/minecraft/item/ItemFood;Lnet/minecraft/item/ItemStack;)V");
			if (methodNode != null)
			{
				addItemStackAwareFoodStatsHook(classNode, methodNode, ObfHelper.isObfuscated());
			}
			else
				throw new RuntimeException("FoodStats: ItemStack-aware addStats method not found");

			MethodNode updateMethodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_75118_a", "onUpdate", "(Lnet/minecraft/entity/player/EntityPlayer;)V");
			if (updateMethodNode != null)
			{
				hookHealthRegen(classNode, updateMethodNode);
				hookExhaustion(classNode, updateMethodNode);
				hookStarvation(classNode, updateMethodNode);
			}
			else
				throw new RuntimeException("FoodStats: onUpdate method not found");

			return ASMHelper.writeClassToBytes(classNode);
		}
		return basicClass;
	}

	public void patchEntityPlayerInit(MethodNode method)
	{
		// find NEW net/minecraft/util/FoodStats
		AbstractInsnNode targetNode = ASMHelper.find(method.instructions, new TypeInsnNode(NEW, "net/minecraft/util/FoodStats"));

		if (targetNode == null)
		{
			throw new RuntimeException("patchEntityPlayerInit: NEW instruction not found");
		}

		do
		{
			targetNode = targetNode.getNext();
		}
		while (targetNode != null && targetNode.getOpcode() != INVOKESPECIAL);

		if (targetNode == null)
		{
			throw new RuntimeException("patchEntityPlayerInit: INVOKESPECIAL instruction not found");
		}

		method.instructions.insertBefore(targetNode, new VarInsnNode(ALOAD, 0));
		((MethodInsnNode) targetNode).desc = "(Lnet/minecraft/entity/player/EntityPlayer;)V";
	}

	public void injectFoodStatsPlayerField(ClassNode classNode)
	{
		classNode.fields.add(new FieldNode(ACC_PUBLIC, foodStatsPlayerField, ObfHelper.getDescriptor("net.minecraft.entity.player.EntityPlayer"), null, null));
	}

	public void injectFoodStatsConstructor(ClassNode classNode)
	{
		// get the default constructor and copy it
		MethodNode defaultConstructor = ASMHelper.findMethodNodeOfClass(classNode, "<init>", "()V");
		MethodNode constructor = new MethodNode(ACC_PUBLIC, "<init>", "(Lnet/minecraft/entity/player/EntityPlayer;)V", null, null);
		constructor.instructions = ASMHelper.cloneInsnList(defaultConstructor.instructions);

		AbstractInsnNode targetNode = ASMHelper.findLastInstructionWithOpcode(constructor, RETURN);

		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 0)); // this
		toInject.add(new VarInsnNode(ALOAD, 1)); // player param
		toInject.add(new FieldInsnNode(PUTFIELD, classNode.name, foodStatsPlayerField, ObfHelper.getDescriptor("net.minecraft.entity.player.EntityPlayer")));

		constructor.instructions.insertBefore(targetNode, toInject);

		classNode.methods.add(constructor);
	}

	public void addItemStackAwareFoodStatsHook(ClassNode classNode, MethodNode method, boolean isObfuscated)
	{
		// injected code:
		/*
		// added lines
		FoodValues modifiedFoodValues = Hooks.onFoodStatsAdded(this, p_151686_1_, p_151686_2_, this.player);
		int prevFoodLevel = this.foodLevel;
		float prevSaturationLevel = this.foodSaturationLevel;

		// this is a default line that has been altered to use the modified food values
		this.addStats(modifiedFoodValues.hunger, modifiedFoodValues.saturationModifier);

		// added lines
		Hooks.onPostFoodStatsAdded(this, p_151686_1_, p_151686_2_, modifiedFoodValues, this.foodLevel - prevFoodLevel, this.foodSaturationLevel - prevSaturationLevel, this.player);
		*/

		String internalFoodStatsName = ASMHelper.toInternalClassName(classNode.name);

		/*
		 * Modify food values
		 */
		InsnList toInject = new InsnList();
		AbstractInsnNode targetNode = ASMHelper.findFirstInstruction(method);

		// create modifiedFoodValues variable
		LabelNode modifiedFoodValuesStart = new LabelNode();
		LabelNode end = ASMHelper.findEndLabel(method);
		LocalVariableNode modifiedFoodValues = new LocalVariableNode("modifiedFoodValues", ObfHelper.getDescriptor("net.minecraft.util.FoodStats"), null, modifiedFoodValuesStart, end, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(modifiedFoodValues);

		// create prevFoodLevel variable
		LabelNode prevFoodLevelStart = new LabelNode();
		LocalVariableNode prevFoodLevel = new LocalVariableNode("prevFoodLevel", "I", null, prevFoodLevelStart, end, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(prevFoodLevel);

		// create prevSaturationLevel variable
		LabelNode prevSaturationLevelStart = new LabelNode();
		LocalVariableNode prevSaturationLevel = new LocalVariableNode("prevSaturationLevel", "F", null, prevSaturationLevelStart, end, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(prevSaturationLevel);

		// get modifiedFoodValues
		toInject.add(new VarInsnNode(ALOAD, 0));					// this
		toInject.add(new VarInsnNode(ALOAD, 1));					// param 1: ItemFood
		toInject.add(new VarInsnNode(ALOAD, 2));					// param 2: ItemStack
		toInject.add(new VarInsnNode(ALOAD, 0));					// this.player (together with below line)
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, foodStatsPlayerField, ObfHelper.getDescriptor("net.minecraft.entity.player.EntityPlayer")));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HooksInternalClass, "onFoodStatsAdded", "(Lnet/minecraft/util/FoodStats;Lnet/minecraft/item/ItemFood;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;)Lsqueek/applecore/api/food/FoodValues;", false));
		toInject.add(new VarInsnNode(ASTORE, modifiedFoodValues.index));		// modifiedFoodValues = hookClass.hookMethod(...)
		toInject.add(modifiedFoodValuesStart);								// variable scope start

		// save current hunger/saturation levels
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75127_a" : "foodLevel", "I"));
		toInject.add(new VarInsnNode(ISTORE, prevFoodLevel.index));
		toInject.add(prevFoodLevelStart);
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75125_b" : "foodSaturationLevel", "F"));
		toInject.add(new VarInsnNode(FSTORE, prevSaturationLevel.index));
		toInject.add(prevSaturationLevelStart);

		method.instructions.insertBefore(targetNode, toInject);

		/*
		 * Make all calls to getHealAmount/getSaturationModifier use the modified values instead
		 */
		InsnList hungerNeedle = new InsnList();
		hungerNeedle.add(new VarInsnNode(ALOAD, 1));
		hungerNeedle.add(new VarInsnNode(ALOAD, 2));
		hungerNeedle.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.item.ItemFood"), "func_150905_g", "(" + ObfHelper.getDescriptor("net.minecraft.item.ItemStack") + ")I", false));

		InsnList hungerReplacement = new InsnList();
		hungerReplacement.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));
		hungerReplacement.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(ASMConstants.FoodValues), "hunger", "I"));

		InsnList saturationNeedle = new InsnList();
		saturationNeedle.add(new VarInsnNode(ALOAD, 1));
		saturationNeedle.add(new VarInsnNode(ALOAD, 2));
		saturationNeedle.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.item.ItemFood"), "func_150906_h", "(" + ObfHelper.getDescriptor("net.minecraft.item.ItemStack") + ")F", false));

		InsnList saturationReplacement = new InsnList();
		saturationReplacement.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));
		saturationReplacement.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(ASMConstants.FoodValues), "saturationModifier", "F"));

		ASMHelper.findAndReplaceAll(method.instructions, hungerNeedle, hungerReplacement);
		ASMHelper.findAndReplaceAll(method.instructions, saturationNeedle, saturationReplacement);

		/*
		 * onPostFoodStatsAdded
		 */
		targetNode = ASMHelper.findLastInstructionWithOpcode(method, RETURN);
		toInject.clear();

		// this
		toInject.add(new VarInsnNode(ALOAD, 0));

		// par1 (ItemFood)
		toInject.add(new VarInsnNode(ALOAD, 1));

		// par2 (ItemStack)
		toInject.add(new VarInsnNode(ALOAD, 2));

		// modifiedFoodValues
		toInject.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));

		// prevFoodLevel - this.foodLevel
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75127_a" : "foodLevel", "I"));
		toInject.add(new VarInsnNode(ILOAD, prevFoodLevel.index));
		toInject.add(new InsnNode(ISUB));

		// prevSaturationLevel - this.foodSaturationLevel
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75125_b" : "foodSaturationLevel", "F"));
		toInject.add(new VarInsnNode(FLOAD, prevSaturationLevel.index));
		toInject.add(new InsnNode(FSUB));

		// player
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, foodStatsPlayerField, ObfHelper.getDescriptor("net.minecraft.entity.player.EntityPlayer")));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HooksInternalClass, "onPostFoodStatsAdded", "(Lnet/minecraft/util/FoodStats;Lnet/minecraft/item/ItemFood;Lnet/minecraft/item/ItemStack;Lsqueek/applecore/api/food/FoodValues;IFLnet/minecraft/entity/player/EntityPlayer;)V", false));

		method.instructions.insertBefore(targetNode, toInject);
	}

	private void hookFoodStatsAddition(ClassNode classNode, MethodNode method)
	{
		// injected code:
		/*
		if (!Hooks.fireFoodStatsAdditionEvent(player, new FoodValues(p_75122_1_, p_75122_2_)))
		{
		    // default code
		}
		*/

		AbstractInsnNode targetNode = ASMHelper.findFirstInstruction(method);

		LabelNode ifCanceled = new LabelNode();

		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(classNode.name), foodStatsPlayerField, ObfHelper.getDescriptor("net.minecraft.entity.player.EntityPlayer")));
		toInject.add(new TypeInsnNode(NEW, ASMHelper.toInternalClassName(ASMConstants.FoodValues)));
		toInject.add(new InsnNode(DUP));
		toInject.add(new VarInsnNode(ILOAD, 1));
		toInject.add(new VarInsnNode(FLOAD, 2));
		toInject.add(new MethodInsnNode(INVOKESPECIAL, ASMHelper.toInternalClassName(ASMConstants.FoodValues), "<init>", "(IF)V", false));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HooksInternalClass, "fireFoodStatsAdditionEvent", "(Lnet/minecraft/entity/player/EntityPlayer;Lsqueek/applecore/api/food/FoodValues;)Z", false));
		toInject.add(new JumpInsnNode(IFNE, ifCanceled));

		method.instructions.insertBefore(targetNode, toInject);

		targetNode = ASMHelper.findLastInstructionWithOpcode(method, RETURN);

		method.instructions.insertBefore(targetNode, ifCanceled);
	}

	private void hookExhaustion(ClassNode classNode, MethodNode method)
	{
		// exhaustion block replaced with:
		/*
		Result allowExhaustionResult = Hooks.fireAllowExhaustionEvent(player);
		float maxExhaustion = Hooks.fireExhaustionTickEvent(player, foodExhaustionLevel);
		if (allowExhaustionResult == Result.ALLOW || (allowExhaustionResult == Result.DEFAULT && this.foodExhaustionLevel >= maxExhaustion))
		{
			ExhaustionEvent.Exhausted exhaustedEvent = Hooks.fireExhaustionMaxEvent(player, maxExhaustion, foodExhaustionLevel);

			this.foodExhaustionLevel += exhaustedEvent.deltaExhaustion;
			if (!exhaustionMaxEvent.isCanceled())
			{
				this.foodSaturationLevel = Math.max(this.foodSaturationLevel + exhaustedEvent.deltaSaturation, 0.0F);
				this.foodLevel = Math.max(this.foodLevel + exhaustedEvent.deltaHunger, 0);
			}
		}
		*/

		String internalFoodStatsName = ASMHelper.toInternalClassName(classNode.name);
		LabelNode endLabel = ASMHelper.findEndLabel(method);

		InsnList toInject = new InsnList();

		AbstractInsnNode injectPoint = ASMHelper.findFirstInstructionWithOpcode(method, PUTFIELD);
		AbstractInsnNode foodExhaustionIf = ASMHelper.findFirstInstructionWithOpcode(method, IFLE);
		LabelNode foodExhaustionBlockEndLabel = ((JumpInsnNode) foodExhaustionIf).label;

		// remove the entire exhaustion block
		ASMHelper.removeFromInsnListUntil(method.instructions, injectPoint.getNext(), foodExhaustionBlockEndLabel);

		// create allowExhaustionResult variable
		LabelNode allowExhaustionResultStart = new LabelNode();
		LocalVariableNode allowExhaustionResult = new LocalVariableNode("allowExhaustionResult", "Lcpw/mods/fml/common/eventhandler/Event$Result;", null, allowExhaustionResultStart, endLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(allowExhaustionResult);

		// Result allowExhaustionResult = Hooks.fireAllowExhaustionEvent(player);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HooksInternalClass, "fireAllowExhaustionEvent", "(Lnet/minecraft/entity/player/EntityPlayer;)Lcpw/mods/fml/common/eventhandler/Event$Result;", false));
		toInject.add(new VarInsnNode(ASTORE, allowExhaustionResult.index));
		toInject.add(allowExhaustionResultStart);

		// create maxExhaustion variable
		LabelNode maxExhaustionStart = new LabelNode();
		LocalVariableNode maxExhaustion = new LocalVariableNode("maxExhaustion", "F", null, maxExhaustionStart, endLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(maxExhaustion);

		// float maxExhaustion = Hooks.fireExhaustionTickEvent(player, foodExhaustionLevel);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75126_c" : "foodExhaustionLevel", "F"));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HooksInternalClass, "fireExhaustionTickEvent", "(Lnet/minecraft/entity/player/EntityPlayer;F)F", false));
		toInject.add(new VarInsnNode(FSTORE, maxExhaustion.index));
		toInject.add(maxExhaustionStart);

		// if (allowExhaustionResult == Result.ALLOW || (allowExhaustionResult == Result.DEFAULT && this.foodExhaustionLevel >= maxExhaustion))
		toInject.add(new VarInsnNode(ALOAD, allowExhaustionResult.index));
		toInject.add(new FieldInsnNode(GETSTATIC, "cpw/mods/fml/common/eventhandler/Event$Result", "ALLOW", "Lcpw/mods/fml/common/eventhandler/Event$Result;"));
		LabelNode ifAllowed = new LabelNode();
		toInject.add(new JumpInsnNode(IF_ACMPEQ, ifAllowed));
		toInject.add(new VarInsnNode(ALOAD, allowExhaustionResult.index));
		toInject.add(new FieldInsnNode(GETSTATIC, "cpw/mods/fml/common/eventhandler/Event$Result", "DEFAULT", "Lcpw/mods/fml/common/eventhandler/Event$Result;"));
		toInject.add(new JumpInsnNode(IF_ACMPNE, foodExhaustionBlockEndLabel));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75126_c" : "foodExhaustionLevel", "F"));
		toInject.add(new VarInsnNode(FLOAD, maxExhaustion.index));
		toInject.add(new InsnNode(FCMPL));
		toInject.add(new JumpInsnNode(IFLT, foodExhaustionBlockEndLabel));
		toInject.add(ifAllowed);

		// create exhaustedEvent variable
		LabelNode exhaustedEventStart = new LabelNode();
		LocalVariableNode exhaustedEvent = new LocalVariableNode("exhaustionMaxEvent", ASMHelper.toDescriptor(ASMConstants.ExhaustionEvent.Exhausted), null, exhaustedEventStart, foodExhaustionBlockEndLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(exhaustedEvent);

		// FoodEvent.Exhaustion.MaxReached exhaustionMaxEvent = Hooks.fireExhaustionMaxEvent(player, exhaustionTickEvent.maxExhaustionLevel, foodExhaustionLevel);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(FLOAD, maxExhaustion.index));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75126_c" : "foodExhaustionLevel", "F"));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HooksInternalClass, "fireExhaustionMaxEvent", "(Lnet/minecraft/entity/player/EntityPlayer;FF)Lsqueek/applecore/api/hunger/ExhaustionEvent$Exhausted;", false));
		toInject.add(new VarInsnNode(ASTORE, exhaustedEvent.index));
		toInject.add(exhaustedEventStart);

		// this.foodExhaustionLevel += exhaustionMaxEvent.deltaExhaustion;
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(DUP));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75126_c" : "foodExhaustionLevel", "F"));
		toInject.add(new VarInsnNode(ALOAD, exhaustedEvent.index));
		toInject.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(ASMConstants.ExhaustionEvent.Exhausted), "deltaExhaustion", "F"));
		toInject.add(new InsnNode(FADD));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75126_c" : "foodExhaustionLevel", "F"));

		// if (!exhaustionMaxEvent.isCanceled())
		toInject.add(new VarInsnNode(ALOAD, exhaustedEvent.index));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ASMHelper.toInternalClassName(ASMConstants.ExhaustionEvent.Exhausted), "isCanceled", "()Z", false));
		toInject.add(new JumpInsnNode(IFNE, foodExhaustionBlockEndLabel));

		// this.foodSaturationLevel = Math.max(this.foodSaturationLevel + exhaustionMaxEvent.deltaSaturation, 0.0F);
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75125_b" : "foodSaturationLevel", "F"));
		toInject.add(new VarInsnNode(ALOAD, exhaustedEvent.index));
		toInject.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(ASMConstants.ExhaustionEvent.Exhausted), "deltaSaturation", "F"));
		toInject.add(new InsnNode(FADD));
		toInject.add(new InsnNode(FCONST_0));
		toInject.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Math", "max", "(FF)F", false));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75125_b" : "foodSaturationLevel", "F"));

		// this.foodLevel = Math.max(this.foodLevel + exhaustionMaxEvent.deltaHunger, 0);
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75127_a" : "foodLevel", "I"));
		toInject.add(new VarInsnNode(ALOAD, exhaustedEvent.index));
		toInject.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(ASMConstants.ExhaustionEvent.Exhausted), "deltaHunger", "I"));
		toInject.add(new InsnNode(IADD));
		toInject.add(new InsnNode(ICONST_0));
		toInject.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Math", "max", "(II)I", false));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75127_a" : "foodLevel", "I"));

		method.instructions.insert(injectPoint, toInject);
	}

	private void hookHealthRegen(ClassNode classNode, MethodNode method)
	{
		// health regen block replaced with:
		/*
		Result allowRegenResult = Hooks.fireAllowRegenEvent(player);
		if (allowRegenResult == Result.ALLOW || (allowRegenResult == Result.DEFAULT && player.worldObj.getGameRules().getGameRuleBooleanValue("naturalRegeneration") && this.foodLevel >= 18 && player.shouldHeal()))
		{
			++this.foodTimer;

			if (this.foodTimer >= Hooks.fireRegenTickEvent(player))
			{
				FoodEvent.RegenHealth.Regen regenEvent = Hooks.fireRegenEvent(player);
				if (!regenEvent.isCanceled())
				{
					player.heal(regenEvent.deltaHealth);
					this.addExhaustion(regenEvent.deltaExhaustion);
				}
				this.foodTimer = 0;
			}
		}
		else
		{
			this.foodTimer = 0;
		}
		*/

		String internalFoodStatsName = ASMHelper.toInternalClassName(classNode.name);
		LabelNode endLabel = ASMHelper.findEndLabel(method);

		InsnList toInject = new InsnList();

		AbstractInsnNode entryPoint = ASMHelper.find(method.instructions, new LdcInsnNode("naturalRegeneration"));
		AbstractInsnNode injectPoint = entryPoint.getPrevious().getPrevious().getPrevious().getPrevious();
		AbstractInsnNode healthBlockJumpToEnd = ASMHelper.findNextInstructionWithOpcode(entryPoint, GOTO);
		LabelNode healthBlockEndLabel = ((JumpInsnNode) healthBlockJumpToEnd).label;

		// remove the entire health regen block
		ASMHelper.removeFromInsnListUntil(method.instructions, injectPoint.getNext(), healthBlockEndLabel);

		// create allowRegenResult variable
		LabelNode allowRegenResultStart = new LabelNode();
		LocalVariableNode allowRegenResult = new LocalVariableNode("allowRegenResult", "Lcpw/mods/fml/common/eventhandler/Event$Result;", null, allowRegenResultStart, endLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(allowRegenResult);

		// Result allowRegenResult = Hooks.fireAllowRegenEvent(player);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HooksInternalClass, "fireAllowRegenEvent", "(Lnet/minecraft/entity/player/EntityPlayer;)Lcpw/mods/fml/common/eventhandler/Event$Result;", false));
		toInject.add(new VarInsnNode(ASTORE, allowRegenResult.index));
		toInject.add(allowRegenResultStart);

		// if (allowRegenResult == Result.ALLOW || (allowRegenResult == Result.DEFAULT && player.worldObj.getGameRules().getGameRuleBooleanValue("naturalRegeneration") && this.foodLevel >= 18 && player.shouldHeal()))
		toInject.add(new VarInsnNode(ALOAD, allowRegenResult.index));
		toInject.add(new FieldInsnNode(GETSTATIC, "cpw/mods/fml/common/eventhandler/Event$Result", "ALLOW", "Lcpw/mods/fml/common/eventhandler/Event$Result;"));
		LabelNode ifAllowed = new LabelNode();
		toInject.add(new JumpInsnNode(IF_ACMPEQ, ifAllowed));
		toInject.add(new VarInsnNode(ALOAD, allowRegenResult.index));
		toInject.add(new FieldInsnNode(GETSTATIC, "cpw/mods/fml/common/eventhandler/Event$Result", "DEFAULT", "Lcpw/mods/fml/common/eventhandler/Event$Result;"));
		LabelNode elseStart = new LabelNode();
		toInject.add(new JumpInsnNode(IF_ACMPNE, elseStart));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new FieldInsnNode(GETFIELD, ObfHelper.getInternalClassName("net.minecraft.entity.player.EntityPlayer"), ObfHelper.isObfuscated() ? "field_70170_p" : "worldObj", "Lnet/minecraft/world/World;"));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.world.World"), ObfHelper.isObfuscated() ? "func_82736_K" : "getGameRules", "()Lnet/minecraft/world/GameRules;", false));
		toInject.add(new LdcInsnNode("naturalRegeneration"));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.world.GameRules"), ObfHelper.isObfuscated() ? "func_82766_b" : "getGameRuleBooleanValue", "(Ljava/lang/String;)Z", false));
		toInject.add(new JumpInsnNode(IFEQ, elseStart));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75127_a" : "foodLevel", "I"));
		toInject.add(new IntInsnNode(BIPUSH, 18));
		toInject.add(new JumpInsnNode(IF_ICMPLT, elseStart));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.entity.player.EntityPlayer"), ObfHelper.isObfuscated() ? "func_70996_bM" : "shouldHeal", "()Z", false));
		toInject.add(new JumpInsnNode(IFEQ, elseStart));
		toInject.add(ifAllowed);

		// ++this.foodTimer;
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(DUP));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75123_d" : "foodTimer", "I"));
		toInject.add(new InsnNode(ICONST_1));
		toInject.add(new InsnNode(IADD));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75123_d" : "foodTimer", "I"));

		// if (this.foodTimer >= Hooks.fireRegenTickEvent(player))
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75123_d" : "foodTimer", "I"));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HooksInternalClass, "fireRegenTickEvent", "(Lnet/minecraft/entity/player/EntityPlayer;)I", false));
		toInject.add(new JumpInsnNode(IF_ICMPLT, healthBlockEndLabel));

		// create regenEvent variable
		LabelNode regenEventStart = new LabelNode();
		LabelNode regenEventEnd = new LabelNode();
		LocalVariableNode regenEvent = new LocalVariableNode("regenEvent", ASMHelper.toDescriptor(ASMConstants.HealthRegenEvent.Regen), null, regenEventStart, regenEventEnd, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(regenEvent);

		// FoodEvent.RegenHealth.Regen regenEvent = Hooks.fireRegenEvent(player);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HooksInternalClass, "fireRegenEvent", "(Lnet/minecraft/entity/player/EntityPlayer;)Lsqueek/applecore/api/hunger/HealthRegenEvent$Regen;", false));
		toInject.add(new VarInsnNode(ASTORE, regenEvent.index));
		toInject.add(regenEventStart);

		// if (!regenEvent.isCanceled())
		toInject.add(new VarInsnNode(ALOAD, regenEvent.index));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ASMHelper.toInternalClassName(ASMConstants.HealthRegenEvent.Regen), "isCanceled", "()Z", false));
		LabelNode ifCanceled = new LabelNode();
		toInject.add(new JumpInsnNode(IFNE, ifCanceled));

		// player.heal(regenEvent.deltaHealth);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(ALOAD, regenEvent.index));
		toInject.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(ASMConstants.HealthRegenEvent.Regen), "deltaHealth", "F"));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.entity.EntityLivingBase"), ObfHelper.isObfuscated() ? "func_70691_i" : "heal", "(F)V", false));

		// this.addExhaustion(regenEvent.deltaExhaustion);
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, regenEvent.index));
		toInject.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(ASMConstants.HealthRegenEvent.Regen), "deltaExhaustion", "F"));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, internalFoodStatsName, ObfHelper.isObfuscated() ? "func_75113_a" : "addExhaustion", "(F)V", false));

		// this.foodTimer = 0;
		toInject.add(ifCanceled);
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(ICONST_0));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75123_d" : "foodTimer", "I"));
		toInject.add(regenEventEnd);
		toInject.add(new JumpInsnNode(GOTO, healthBlockEndLabel));

		// else
		toInject.add(elseStart);

		// this.foodTimer = 0;
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(ICONST_0));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75123_d" : "foodTimer", "I"));

		method.instructions.insert(injectPoint, toInject);
	}

	private void hookStarvation(ClassNode classNode, MethodNode method)
	{
		// add starveTimer field
		classNode.fields.add(new FieldNode(ACC_PUBLIC, "starveTimer", "I", null, null));

		// injected at the bottom of the function:
		/*
		Result allowStarvationResult = Hooks.fireAllowStarvation(player);
		if (allowStarvationResult == Result.ALLOW || (allowStarvationResult == Result.DEFAULT && this.foodLevel <= 0))
		{
			++this.starveTimer;

			if (this.starveTimer >= Hooks.fireStarvationTickEvent(player))
			{
				FoodEvent.Starvation.Starve starveEvent = Hooks.fireStarveEvent(player);
				if (!starveEvent.isCanceled())
				{
					player.attackEntityFrom(DamageSource.starve, starveEvent.starveDamage);
				}
				this.starveTimer = 0;
			}
		}
		else
		{
			this.starveTimer = 0;
		}
		 */

		String internalFoodStatsName = ASMHelper.toInternalClassName(classNode.name);
		AbstractInsnNode lastReturn = ASMHelper.findLastInstructionWithOpcode(method, RETURN);

		InsnList toInject = new InsnList();

		// create allowStarvationResult variable
		LabelNode allowStarvationResultStart = new LabelNode();
		LabelNode beforeReturn = new LabelNode();
		// for whatever reason, the end label of this variable cant be the actual end label of the method
		// it was throwing ArrayIndexOutOfBoundException in the ClassReader in obfuscated environments
		// not sure why that is the case, but this workaround seems to avoid the issue
		LocalVariableNode allowStarvationResult = new LocalVariableNode("allowStarvationResult", "Lcpw/mods/fml/common/eventhandler/Event$Result;", null, allowStarvationResultStart, beforeReturn, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(allowStarvationResult);

		// Result allowStarvationResult = Hooks.fireAllowStarvation(player);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HooksInternalClass, "fireAllowStarvation", "(Lnet/minecraft/entity/player/EntityPlayer;)Lcpw/mods/fml/common/eventhandler/Event$Result;", false));
		toInject.add(new VarInsnNode(ASTORE, allowStarvationResult.index));
		toInject.add(allowStarvationResultStart);

		// if (allowStarvationResult == Result.ALLOW || (allowStarvationResult == Result.DEFAULT && this.foodLevel <= 0))
		toInject.add(new VarInsnNode(ALOAD, allowStarvationResult.index));
		toInject.add(new FieldInsnNode(GETSTATIC, "cpw/mods/fml/common/eventhandler/Event$Result", "ALLOW", "Lcpw/mods/fml/common/eventhandler/Event$Result;"));
		LabelNode ifAllowed = new LabelNode();
		toInject.add(new JumpInsnNode(IF_ACMPEQ, ifAllowed));
		toInject.add(new VarInsnNode(ALOAD, allowStarvationResult.index));
		LabelNode elseStart = new LabelNode();
		toInject.add(new FieldInsnNode(GETSTATIC, "cpw/mods/fml/common/eventhandler/Event$Result", "DEFAULT", "Lcpw/mods/fml/common/eventhandler/Event$Result;"));
		toInject.add(new JumpInsnNode(IF_ACMPNE, elseStart));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, ObfHelper.isObfuscated() ? "field_75127_a" : "foodLevel", "I"));
		toInject.add(new JumpInsnNode(IFGT, elseStart));
		toInject.add(ifAllowed);

		// ++this.starveTimer;
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(DUP));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, "starveTimer", "I"));
		toInject.add(new InsnNode(ICONST_1));
		toInject.add(new InsnNode(IADD));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, "starveTimer", "I"));

		// if (this.starveTimer >= Hooks.fireStarvationTickEvent(player))
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, "starveTimer", "I"));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HooksInternalClass, "fireStarvationTickEvent", "(Lnet/minecraft/entity/player/EntityPlayer;)I", false));
		toInject.add(new JumpInsnNode(IF_ICMPLT, beforeReturn));

		// create starveEvent variable
		LabelNode starveEventStart = new LabelNode();
		LabelNode starveEventEnd = new LabelNode();
		LocalVariableNode starveEvent = new LocalVariableNode("starveEvent", ASMHelper.toDescriptor(ASMConstants.StarvationEvent.Starve), null, starveEventStart, starveEventEnd, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(starveEvent);

		// FoodEvent.Starvation.Starve starveEvent = Hooks.fireStarveEvent(player);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HooksInternalClass, "fireStarveEvent", "(Lnet/minecraft/entity/player/EntityPlayer;)Lsqueek/applecore/api/hunger/StarvationEvent$Starve;", false));
		toInject.add(new VarInsnNode(ASTORE, starveEvent.index));
		toInject.add(starveEventStart);

		// if (!starveEvent.isCanceled())
		toInject.add(new VarInsnNode(ALOAD, starveEvent.index));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ASMHelper.toInternalClassName(ASMConstants.StarvationEvent.Starve), "isCanceled", "()Z", false));
		LabelNode ifCanceled = new LabelNode();
		toInject.add(new JumpInsnNode(IFNE, ifCanceled));

		// player.attackEntityFrom(DamageSource.starve, starveEvent.starveDamage);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new FieldInsnNode(GETSTATIC, ObfHelper.getInternalClassName("net.minecraft.util.DamageSource"), ObfHelper.isObfuscated() ? "field_76366_f" : "starve", "Lnet/minecraft/util/DamageSource;"));
		toInject.add(new VarInsnNode(ALOAD, starveEvent.index));
		toInject.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(ASMConstants.StarvationEvent.Starve), "starveDamage", "F"));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.entity.player.EntityPlayer"), ObfHelper.isObfuscated() ? "func_70097_a" : "attackEntityFrom", "(Lnet/minecraft/util/DamageSource;F)Z", false));
		toInject.add(new InsnNode(POP));

		// this.starveTimer = 0;
		toInject.add(ifCanceled);
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(ICONST_0));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, "starveTimer", "I"));
		toInject.add(starveEventEnd);
		toInject.add(new JumpInsnNode(GOTO, beforeReturn));

		// else
		toInject.add(elseStart);

		// this.starveTimer = 0;
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(ICONST_0));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, "starveTimer", "I"));

		toInject.add(beforeReturn);

		method.instructions.insertBefore(lastReturn, toInject);
	}

}
