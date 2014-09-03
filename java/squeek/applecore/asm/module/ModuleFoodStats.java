package squeek.applecore.asm.module;

import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.hunger.ExhaustionEvent;
import squeek.applecore.api.hunger.HealthRegenEvent;
import squeek.applecore.api.hunger.StarvationEvent;
import squeek.applecore.asm.Hooks;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.applecore.asm.helpers.ASMHelper;
import squeek.applecore.asm.helpers.ObfHelper;
import cpw.mods.fml.common.eventhandler.Event;

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
			boolean isObfuscated = !name.equals(transformedName);

			ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "<init>", null);
			if (methodNode != null)
			{
				patchEntityPlayerInit(methodNode, isObfuscated);
				return ASMHelper.writeClassToBytes(classNode);
			}
			else
				throw new RuntimeException("EntityPlayer: <init> method not found");
		}
		if (transformedName.equals("net.minecraft.util.FoodStats"))
		{
			boolean isObfuscated = !name.equals(transformedName);

			ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

			if (!ASMHelper.isCauldron())
			{
				injectFoodStatsPlayerField(classNode);
				injectFoodStatsConstructor(classNode, isObfuscated);
			}

			MethodNode addStatsMethodNode = ASMHelper.findMethodNodeOfClass(classNode, isObfuscated ? "a" : "addStats", "(IF)V");
			if (addStatsMethodNode != null)
			{
				hookFoodStatsAddition(classNode, addStatsMethodNode, isObfuscated);
			}
			else
				throw new RuntimeException("FoodStats: addStats(IF)V method not found");

			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, isObfuscated ? "a" : "func_151686_a", isObfuscated ? "(Lacx;Ladd;)V" : "(Lnet/minecraft/item/ItemFood;Lnet/minecraft/item/ItemStack;)V");
			if (methodNode != null)
			{
				addItemStackAwareFoodStatsHook(classNode, methodNode, isObfuscated);
			}
			else
				throw new RuntimeException("FoodStats: ItemStack-aware addStats method not found");

			MethodNode updateMethodNode = ASMHelper.findMethodNodeOfClass(classNode, isObfuscated ? "a" : "onUpdate", isObfuscated ? "(Lyz;)V" : "(Lnet/minecraft/entity/player/EntityPlayer;)V");
			if (updateMethodNode != null)
			{
				hookHealthRegen(classNode, updateMethodNode, isObfuscated);
				hookExhaustion(classNode, updateMethodNode, isObfuscated);
				hookStarvation(classNode, updateMethodNode, isObfuscated);
			}
			else
				throw new RuntimeException("FoodStats: onUpdate method not found");

			return ASMHelper.writeClassToBytes(classNode);
		}
		return basicClass;
	}

	public void patchEntityPlayerInit(MethodNode method, boolean isObfuscated)
	{
		// find NEW net/minecraft/util/FoodStats
		AbstractInsnNode targetNode = ASMHelper.find(method.instructions, new TypeInsnNode(NEW, isObfuscated ? "zr" : "net/minecraft/util/FoodStats"));

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
		((MethodInsnNode) targetNode).desc = isObfuscated ? "(Lyz;)V" : "(Lnet/minecraft/entity/player/EntityPlayer;)V";
	}

	public void injectFoodStatsPlayerField(ClassNode classNode)
	{
		classNode.fields.add(new FieldNode(ACC_PUBLIC, foodStatsPlayerField, ObfHelper.getDescriptor("net.minecraft.entity.player.EntityPlayer"), null, null));
	}

	public void injectFoodStatsConstructor(ClassNode classNode, boolean isObfuscated)
	{
		// get the default constructor and copy it
		MethodNode defaultConstructor = ASMHelper.findMethodNodeOfClass(classNode, "<init>", "()V");
		MethodNode constructor = new MethodNode(ACC_PUBLIC, "<init>", isObfuscated ? "(Lyz;)V" : "(Lnet/minecraft/entity/player/EntityPlayer;)V", null, null);
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

		String internalFoodStatsName = classNode.name.replace(".", "/");

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
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "onFoodStatsAdded", "(Lnet/minecraft/util/FoodStats;Lnet/minecraft/item/ItemFood;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;)Lsqueek/applecore/api/food/FoodValues;"));
		toInject.add(new VarInsnNode(ASTORE, modifiedFoodValues.index));		// modifiedFoodValues = hookClass.hookMethod(...)
		toInject.add(modifiedFoodValuesStart);								// variable scope start

		// save current hunger/saturation levels
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "a" : "foodLevel", "I"));
		toInject.add(new VarInsnNode(ISTORE, prevFoodLevel.index));
		toInject.add(prevFoodLevelStart);
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "b" : "foodSaturationLevel", "F"));
		toInject.add(new VarInsnNode(FSTORE, prevSaturationLevel.index));
		toInject.add(prevSaturationLevelStart);

		method.instructions.insertBefore(targetNode, toInject);

		/*
		 * Make all calls to getHealAmount/getSaturationModifier use the modified values instead
		 */
		InsnList hungerNeedle = new InsnList();
		hungerNeedle.add(new VarInsnNode(ALOAD, 1));
		hungerNeedle.add(new VarInsnNode(ALOAD, 2));
		hungerNeedle.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.item.ItemFood"), isObfuscated ? "g" : "func_150905_g", "(" + ObfHelper.getDescriptor("net.minecraft.item.ItemStack") + ")I"));

		InsnList hungerReplacement = new InsnList();
		hungerReplacement.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));
		hungerReplacement.add(new FieldInsnNode(GETFIELD, Type.getInternalName(FoodValues.class), "hunger", "I"));

		InsnList saturationNeedle = new InsnList();
		saturationNeedle.add(new VarInsnNode(ALOAD, 1));
		saturationNeedle.add(new VarInsnNode(ALOAD, 2));
		saturationNeedle.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.item.ItemFood"), isObfuscated ? "h" : "func_150906_h", "(" + ObfHelper.getDescriptor("net.minecraft.item.ItemStack") + ")F"));

		InsnList saturationReplacement = new InsnList();
		saturationReplacement.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));
		saturationReplacement.add(new FieldInsnNode(GETFIELD, Type.getInternalName(FoodValues.class), "saturationModifier", "F"));

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
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "a" : "foodLevel", "I"));
		toInject.add(new VarInsnNode(ILOAD, prevFoodLevel.index));
		toInject.add(new InsnNode(ISUB));

		// prevSaturationLevel - this.foodSaturationLevel
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "b" : "foodSaturationLevel", "F"));
		toInject.add(new VarInsnNode(FLOAD, prevSaturationLevel.index));
		toInject.add(new InsnNode(FSUB));

		// player
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, foodStatsPlayerField, ObfHelper.getDescriptor("net.minecraft.entity.player.EntityPlayer")));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "onPostFoodStatsAdded", "(Lnet/minecraft/util/FoodStats;Lnet/minecraft/item/ItemFood;Lnet/minecraft/item/ItemStack;Lsqueek/applecore/api/food/FoodValues;IFLnet/minecraft/entity/player/EntityPlayer;)V"));
	}

	private void hookFoodStatsAddition(ClassNode classNode, MethodNode method, boolean isObfuscated)
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
		toInject.add(new FieldInsnNode(GETFIELD, classNode.name.replace(".", "/"), foodStatsPlayerField, ObfHelper.getDescriptor("net.minecraft.entity.player.EntityPlayer")));
		toInject.add(new TypeInsnNode(NEW, Type.getInternalName(FoodValues.class)));
		toInject.add(new InsnNode(DUP));
		toInject.add(new VarInsnNode(ILOAD, 1));
		toInject.add(new VarInsnNode(FLOAD, 2));
		toInject.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(FoodValues.class), "<init>", "(IF)V"));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireFoodStatsAdditionEvent", "(Lnet/minecraft/entity/player/EntityPlayer;Lsqueek/applecore/api/food/FoodValues;)Z"));
		toInject.add(new JumpInsnNode(IFNE, ifCanceled));

		method.instructions.insertBefore(targetNode, toInject);

		targetNode = ASMHelper.findLastInstructionWithOpcode(method, RETURN);

		method.instructions.insertBefore(targetNode, ifCanceled);
	}

	private void hookExhaustion(ClassNode classNode, MethodNode method, boolean isObfuscated)
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

		String internalFoodStatsName = classNode.name.replace(".", "/");
		LabelNode endLabel = ASMHelper.findEndLabel(method);

		InsnList toInject = new InsnList();

		AbstractInsnNode injectPoint = ASMHelper.findFirstInstructionWithOpcode(method, PUTFIELD);
		AbstractInsnNode foodExhaustionIf = ASMHelper.findFirstInstructionWithOpcode(method, IFLE);
		LabelNode foodExhaustionBlockEndLabel = ((JumpInsnNode) foodExhaustionIf).label;

		// remove the entire exhaustion block
		ASMHelper.removeFromInsnListUntil(method.instructions, injectPoint.getNext(), foodExhaustionBlockEndLabel);

		// create allowExhaustionResult variable
		LabelNode allowExhaustionResultStart = new LabelNode();
		LocalVariableNode allowExhaustionResult = new LocalVariableNode("allowExhaustionResult", Type.getDescriptor(Event.Result.class), null, allowExhaustionResultStart, endLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(allowExhaustionResult);

		// Result allowExhaustionResult = Hooks.fireAllowExhaustionEvent(player);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireAllowExhaustionEvent", "(Lnet/minecraft/entity/player/EntityPlayer;)Lcpw/mods/fml/common/eventhandler/Event$Result;"));
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
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "c" : "foodExhaustionLevel", "F"));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireExhaustionTickEvent", "(Lnet/minecraft/entity/player/EntityPlayer;F)F"));
		toInject.add(new VarInsnNode(FSTORE, maxExhaustion.index));
		toInject.add(maxExhaustionStart);

		// if (allowExhaustionResult == Result.ALLOW || (allowExhaustionResult == Result.DEFAULT && this.foodExhaustionLevel >= maxExhaustion))
		toInject.add(new VarInsnNode(ALOAD, allowExhaustionResult.index));
		toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "ALLOW", Type.getDescriptor(Event.Result.class)));
		LabelNode ifAllowed = new LabelNode();
		toInject.add(new JumpInsnNode(IF_ACMPEQ, ifAllowed));
		toInject.add(new VarInsnNode(ALOAD, allowExhaustionResult.index));
		toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "DEFAULT", Type.getDescriptor(Event.Result.class)));
		toInject.add(new JumpInsnNode(IF_ACMPNE, foodExhaustionBlockEndLabel));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "c" : "foodExhaustionLevel", "F"));
		toInject.add(new VarInsnNode(FLOAD, maxExhaustion.index));
		toInject.add(new InsnNode(FCMPL));
		toInject.add(new JumpInsnNode(IFLT, foodExhaustionBlockEndLabel));
		toInject.add(ifAllowed);

		// create exhaustedEvent variable
		LabelNode exhaustedEventStart = new LabelNode();
		LocalVariableNode exhaustedEvent = new LocalVariableNode("exhaustionMaxEvent", Type.getDescriptor(ExhaustionEvent.Exhausted.class), null, exhaustedEventStart, foodExhaustionBlockEndLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(exhaustedEvent);

		// FoodEvent.Exhaustion.MaxReached exhaustionMaxEvent = Hooks.fireExhaustionMaxEvent(player, exhaustionTickEvent.maxExhaustionLevel, foodExhaustionLevel);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(FLOAD, maxExhaustion.index));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "c" : "foodExhaustionLevel", "F"));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireExhaustionMaxEvent", "(Lnet/minecraft/entity/player/EntityPlayer;FF)Lsqueek/applecore/api/hunger/ExhaustionEvent$Exhausted;"));
		toInject.add(new VarInsnNode(ASTORE, exhaustedEvent.index));
		toInject.add(exhaustedEventStart);

		// this.foodExhaustionLevel += exhaustionMaxEvent.deltaExhaustion;
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(DUP));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "c" : "foodExhaustionLevel", "F"));
		toInject.add(new VarInsnNode(ALOAD, exhaustedEvent.index));
		toInject.add(new FieldInsnNode(GETFIELD, Type.getInternalName(ExhaustionEvent.Exhausted.class), "deltaExhaustion", "F"));
		toInject.add(new InsnNode(FADD));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, isObfuscated ? "c" : "foodExhaustionLevel", "F"));

		// if (!exhaustionMaxEvent.isCanceled())
		toInject.add(new VarInsnNode(ALOAD, exhaustedEvent.index));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(ExhaustionEvent.Exhausted.class), "isCanceled", "()Z"));
		toInject.add(new JumpInsnNode(IFNE, foodExhaustionBlockEndLabel));

		// this.foodSaturationLevel = Math.max(this.foodSaturationLevel + exhaustionMaxEvent.deltaSaturation, 0.0F);
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "b" : "foodSaturationLevel", "F"));
		toInject.add(new VarInsnNode(ALOAD, exhaustedEvent.index));
		toInject.add(new FieldInsnNode(GETFIELD, Type.getInternalName(ExhaustionEvent.Exhausted.class), "deltaSaturation", "F"));
		toInject.add(new InsnNode(FADD));
		toInject.add(new InsnNode(FCONST_0));
		toInject.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Math", "max", "(FF)F"));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, isObfuscated ? "b" : "foodSaturationLevel", "F"));

		// this.foodLevel = Math.max(this.foodLevel + exhaustionMaxEvent.deltaHunger, 0);
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "a" : "foodLevel", "I"));
		toInject.add(new VarInsnNode(ALOAD, exhaustedEvent.index));
		toInject.add(new FieldInsnNode(GETFIELD, Type.getInternalName(ExhaustionEvent.Exhausted.class), "deltaHunger", "I"));
		toInject.add(new InsnNode(IADD));
		toInject.add(new InsnNode(ICONST_0));
		toInject.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Math", "max", "(II)I"));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, isObfuscated ? "a" : "foodLevel", "I"));

		method.instructions.insert(injectPoint, toInject);
	}

	private void hookHealthRegen(ClassNode classNode, MethodNode method, boolean isObfuscated)
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

		String internalFoodStatsName = classNode.name.replace(".", "/");
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
		LocalVariableNode allowRegenResult = new LocalVariableNode("allowRegenResult", Type.getDescriptor(Event.Result.class), null, allowRegenResultStart, endLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(allowRegenResult);

		// Result allowRegenResult = Hooks.fireAllowRegenEvent(player);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireAllowRegenEvent", "(Lnet/minecraft/entity/player/EntityPlayer;)Lcpw/mods/fml/common/eventhandler/Event$Result;"));
		toInject.add(new VarInsnNode(ASTORE, allowRegenResult.index));
		toInject.add(allowRegenResultStart);

		// if (allowRegenResult == Result.ALLOW || (allowRegenResult == Result.DEFAULT && player.worldObj.getGameRules().getGameRuleBooleanValue("naturalRegeneration") && this.foodLevel >= 18 && player.shouldHeal()))
		toInject.add(new VarInsnNode(ALOAD, allowRegenResult.index));
		toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "ALLOW", Type.getDescriptor(Event.Result.class)));
		LabelNode ifAllowed = new LabelNode();
		toInject.add(new JumpInsnNode(IF_ACMPEQ, ifAllowed));
		toInject.add(new VarInsnNode(ALOAD, allowRegenResult.index));
		toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "DEFAULT", Type.getDescriptor(Event.Result.class)));
		LabelNode elseStart = new LabelNode();
		toInject.add(new JumpInsnNode(IF_ACMPNE, elseStart));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new FieldInsnNode(GETFIELD, ObfHelper.getInternalClassName("net.minecraft.entity.player.EntityPlayer"), isObfuscated ? "o" : "worldObj", isObfuscated ? "Lahb;" : "Lnet/minecraft/world/World;"));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.world.World"), isObfuscated ? "O" : "getGameRules", isObfuscated ? "()Lagy;" : "()Lnet/minecraft/world/GameRules;"));
		toInject.add(new LdcInsnNode("naturalRegeneration"));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.world.GameRules"), isObfuscated ? "b" : "getGameRuleBooleanValue", "(Ljava/lang/String;)Z"));
		toInject.add(new JumpInsnNode(IFEQ, elseStart));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "a" : "foodLevel", "I"));
		toInject.add(new IntInsnNode(BIPUSH, 18));
		toInject.add(new JumpInsnNode(IF_ICMPLT, elseStart));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.entity.player.EntityPlayer"), isObfuscated ? "bR" : "shouldHeal", "()Z"));
		toInject.add(new JumpInsnNode(IFEQ, elseStart));
		toInject.add(ifAllowed);

		// ++this.foodTimer;
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(DUP));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "d" : "foodTimer", "I"));
		toInject.add(new InsnNode(ICONST_1));
		toInject.add(new InsnNode(IADD));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, isObfuscated ? "d" : "foodTimer", "I"));

		// if (this.foodTimer >= Hooks.fireRegenTickEvent(player))
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "d" : "foodTimer", "I"));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireRegenTickEvent", "(Lnet/minecraft/entity/player/EntityPlayer;)I"));
		toInject.add(new JumpInsnNode(IF_ICMPLT, healthBlockEndLabel));

		// create regenEvent variable
		LabelNode regenEventStart = new LabelNode();
		LabelNode regenEventEnd = new LabelNode();
		LocalVariableNode regenEvent = new LocalVariableNode("regenEvent", Type.getDescriptor(HealthRegenEvent.Regen.class), null, regenEventStart, regenEventEnd, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(regenEvent);

		// FoodEvent.RegenHealth.Regen regenEvent = Hooks.fireRegenEvent(player);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireRegenEvent", "(Lnet/minecraft/entity/player/EntityPlayer;)Lsqueek/applecore/api/hunger/HealthRegenEvent$Regen;"));
		toInject.add(new VarInsnNode(ASTORE, regenEvent.index));
		toInject.add(regenEventStart);

		// if (!regenEvent.isCanceled())
		toInject.add(new VarInsnNode(ALOAD, regenEvent.index));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(HealthRegenEvent.Regen.class), "isCanceled", "()Z"));
		LabelNode ifCanceled = new LabelNode();
		toInject.add(new JumpInsnNode(IFNE, ifCanceled));

		// player.heal(regenEvent.deltaHealth);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(ALOAD, regenEvent.index));
		toInject.add(new FieldInsnNode(GETFIELD, Type.getInternalName(HealthRegenEvent.Regen.class), "deltaHealth", "F"));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.entity.EntityLivingBase"), isObfuscated ? "f" : "heal", "(F)V"));

		// this.addExhaustion(regenEvent.deltaExhaustion);
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, regenEvent.index));
		toInject.add(new FieldInsnNode(GETFIELD, Type.getInternalName(HealthRegenEvent.Regen.class), "deltaExhaustion", "F"));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, internalFoodStatsName, isObfuscated ? "a" : "addExhaustion", "(F)V"));

		// this.foodTimer = 0;
		toInject.add(ifCanceled);
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(ICONST_0));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, isObfuscated ? "d" : "foodTimer", "I"));
		toInject.add(regenEventEnd);
		toInject.add(new JumpInsnNode(GOTO, healthBlockEndLabel));

		// else
		toInject.add(elseStart);

		// this.foodTimer = 0;
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(ICONST_0));
		toInject.add(new FieldInsnNode(PUTFIELD, internalFoodStatsName, isObfuscated ? "d" : "foodTimer", "I"));

		method.instructions.insert(injectPoint, toInject);
	}

	private void hookStarvation(ClassNode classNode, MethodNode method, boolean isObfuscated)
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

		String internalFoodStatsName = classNode.name.replace(".", "/");
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
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireAllowStarvation", "(Lnet/minecraft/entity/player/EntityPlayer;)Lcpw/mods/fml/common/eventhandler/Event$Result;"));
		toInject.add(new VarInsnNode(ASTORE, allowStarvationResult.index));
		toInject.add(allowStarvationResultStart);

		// if (allowStarvationResult == Result.ALLOW || (allowStarvationResult == Result.DEFAULT && this.foodLevel <= 0))
		toInject.add(new VarInsnNode(ALOAD, allowStarvationResult.index));
		toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "ALLOW", Type.getDescriptor(Event.Result.class)));
		LabelNode ifAllowed = new LabelNode();
		toInject.add(new JumpInsnNode(IF_ACMPEQ, ifAllowed));
		toInject.add(new VarInsnNode(ALOAD, allowStarvationResult.index));
		LabelNode elseStart = new LabelNode();
		toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "DEFAULT", Type.getDescriptor(Event.Result.class)));
		toInject.add(new JumpInsnNode(IF_ACMPNE, elseStart));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "a" : "foodLevel", "I"));
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
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireStarvationTickEvent", "(Lnet/minecraft/entity/player/EntityPlayer;)I"));
		toInject.add(new JumpInsnNode(IF_ICMPLT, beforeReturn));

		// create starveEvent variable
		LabelNode starveEventStart = new LabelNode();
		LabelNode starveEventEnd = new LabelNode();
		LocalVariableNode starveEvent = new LocalVariableNode("starveEvent", Type.getDescriptor(StarvationEvent.Starve.class), null, starveEventStart, starveEventEnd, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(starveEvent);

		// FoodEvent.Starvation.Starve starveEvent = Hooks.fireStarveEvent(player);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireStarveEvent", "(Lnet/minecraft/entity/player/EntityPlayer;)Lsqueek/applecore/api/hunger/StarvationEvent$Starve;"));
		toInject.add(new VarInsnNode(ASTORE, starveEvent.index));
		toInject.add(starveEventStart);

		// if (!starveEvent.isCanceled())
		toInject.add(new VarInsnNode(ALOAD, starveEvent.index));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(StarvationEvent.Starve.class), "isCanceled", "()Z"));
		LabelNode ifCanceled = new LabelNode();
		toInject.add(new JumpInsnNode(IFNE, ifCanceled));

		// player.attackEntityFrom(DamageSource.starve, starveEvent.starveDamage);
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new FieldInsnNode(GETSTATIC, ObfHelper.getInternalClassName("net.minecraft.util.DamageSource"), isObfuscated ? "field_76366_f" : "starve", isObfuscated ? "Lro;" : "Lnet/minecraft/util/DamageSource;"));
		toInject.add(new VarInsnNode(ALOAD, starveEvent.index));
		toInject.add(new FieldInsnNode(GETFIELD, Type.getInternalName(StarvationEvent.Starve.class), "starveDamage", "F"));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.entity.player.EntityPlayer"), isObfuscated ? "a" : "attackEntityFrom", isObfuscated ? "(Lro;F)Z" : "(Lnet/minecraft/util/DamageSource;F)Z"));
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
