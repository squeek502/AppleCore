package squeek.applecore.asm.module;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import squeek.applecore.asm.ASMConstants;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.asmhelper.applecore.ASMHelper;
import squeek.asmhelper.applecore.ObfHelper;

import static org.objectweb.asm.Opcodes.*;

public class ModuleFoodStats implements IClassTransformerModule
{
	public static String foodStatsPlayerField = "entityplayer";
	public static String foodStatsStarveTimerField = "starveTimer";

	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{ASMConstants.PLAYER, ASMConstants.FOOD_STATS};
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (transformedName.equals(ASMConstants.PLAYER))
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
		if (transformedName.equals(ASMConstants.FOOD_STATS))
		{
			ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

			injectFoodStatsPlayerField(classNode);
			injectFoodStatsConstructor(classNode);

			// add starveTimer field
			classNode.fields.add(new FieldNode(ACC_PUBLIC, foodStatsStarveTimerField, "I", null, null));

			// IAppleCoreFoodStats implementation
			classNode.interfaces.add(ASMHelper.toInternalClassName(ASMConstants.IAPPLECOREFOODSTATS));
			tryAddFieldGetter(classNode, "getFoodTimer", ObfHelper.isObfuscated() ? "field_75123_d" : "foodTimer", "I");
			tryAddFieldSetter(classNode, "setFoodTimer", ObfHelper.isObfuscated() ? "field_75123_d" : "foodTimer", "I");
			tryAddFieldGetter(classNode, "getStarveTimer", foodStatsStarveTimerField, "I");
			tryAddFieldSetter(classNode, "setStarveTimer", foodStatsStarveTimerField, "I");
			tryAddFieldGetter(classNode, "getPlayer", foodStatsPlayerField, ASMHelper.toDescriptor(ASMConstants.PLAYER));
			tryAddFieldSetter(classNode, "setPrevFoodLevel", ObfHelper.isObfuscated() ? "field_75124_e" : "prevFoodLevel", "I");
			tryAddFieldGetter(classNode, "getExhaustion", ObfHelper.isObfuscated() ? "field_75126_c" : "foodExhaustionLevel", "F");
			tryAddFieldSetter(classNode, "setExhaustion", ObfHelper.isObfuscated() ? "field_75126_c" : "foodExhaustionLevel", "F");
			tryAddFieldSetter(classNode, "setSaturation", ObfHelper.isObfuscated() ? "field_75125_b" : "foodSaturationLevel", "F");

			MethodNode addStatsMethodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_75122_a", "addStats", ASMHelper.toMethodDescriptor("V", "I", "F"));
			if (addStatsMethodNode != null)
			{
				hookFoodStatsAddition(classNode, addStatsMethodNode);
			}
			else
				throw new RuntimeException("FoodStats: addStats(IF)V method not found");

			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_151686_a", "addStats", ASMHelper.toMethodDescriptor("V", ASMConstants.ITEM_FOOD, ASMConstants.STACK));
			if (methodNode != null)
			{
				addItemStackAwareFoodStatsHook(classNode, methodNode, ObfHelper.isObfuscated());
			}
			else
				throw new RuntimeException("FoodStats: ItemStack-aware addStats method not found");

			MethodNode updateMethodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_75118_a", "onUpdate", ASMHelper.toMethodDescriptor("V", ASMConstants.PLAYER));
			if (updateMethodNode != null)
			{
				hookUpdate(classNode, updateMethodNode);
			}
			else
				throw new RuntimeException("FoodStats: onUpdate method not found");

			MethodNode needFoodMethodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_75121_c", "needFood", ASMHelper.toMethodDescriptor("Z"));
			if (needFoodMethodNode != null)
			{
				hookNeedFood(classNode, needFoodMethodNode);
			}
			else
				throw new RuntimeException("FoodStats: needFood method not found");

			MethodNode addExhaustionMethod = ASMHelper.findMethodNodeOfClass(classNode, "func_75113_a", "addExhaustion", ASMHelper.toMethodDescriptor("V", "F"));
			if (addExhaustionMethod != null)
			{
				hookAddExhaustion(classNode, addExhaustionMethod);
			}
			else
				throw new RuntimeException("FoodStats: addExhaustion method not found");

			return ASMHelper.writeClassToBytes(classNode);
		}
		return basicClass;
	}

	public void patchEntityPlayerInit(MethodNode method)
	{
		// find NEW net/minecraft/util/FoodStats
		AbstractInsnNode targetNode = ASMHelper.find(method.instructions, new TypeInsnNode(NEW, ASMHelper.toInternalClassName(ASMConstants.FOOD_STATS)));

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
		((MethodInsnNode) targetNode).desc = ASMHelper.toMethodDescriptor("V", ASMConstants.PLAYER);
	}

	public void injectFoodStatsPlayerField(ClassNode classNode)
	{
		classNode.fields.add(new FieldNode(ACC_PUBLIC, foodStatsPlayerField, ASMHelper.toDescriptor(ASMConstants.PLAYER), null, null));
	}

	public void injectFoodStatsConstructor(ClassNode classNode)
	{
		// get the default constructor, apply max hunger patches, and copy it
		MethodNode defaultConstructor = ASMHelper.findMethodNodeOfClass(classNode, "<init>", ASMHelper.toMethodDescriptor("V"));

		if (defaultConstructor == null)
			throw new RuntimeException("FoodStats.<init>() not found");

		InsnList foodLevelNeedle = new InsnList();
		foodLevelNeedle.add(new IntInsnNode(BIPUSH, 20));

		InsnList foodLevelReplacement = new InsnList();
		foodLevelReplacement.add(new VarInsnNode(ALOAD, 0));
		foodLevelReplacement.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.HOOKS), "getMaxHunger", ASMHelper.toMethodDescriptor("I", ASMConstants.FOOD_STATS), false));

		int numReplacements = ASMHelper.findAndReplaceAll(defaultConstructor.instructions, foodLevelNeedle, foodLevelReplacement);

		if (numReplacements < 2)
			throw new RuntimeException("FoodStats.<init>() replaced " + numReplacements + " (BIPUSH 20) instructions, expected >= 2");

		MethodNode constructor = new MethodNode(ACC_PUBLIC, "<init>", ASMHelper.toMethodDescriptor("V", ASMConstants.PLAYER), null, null);
		constructor.instructions = ASMHelper.cloneInsnList(defaultConstructor.instructions);

		AbstractInsnNode targetNode = ASMHelper.findFirstInstructionWithOpcode(constructor, INVOKESPECIAL);

		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 0)); // this
		toInject.add(new VarInsnNode(ALOAD, 1)); // player param
		toInject.add(new FieldInsnNode(PUTFIELD, classNode.name, foodStatsPlayerField, ASMHelper.toDescriptor(ASMConstants.PLAYER)));

		constructor.instructions.insert(targetNode, toInject);

		classNode.methods.add(constructor);
	}

	public void addItemStackAwareFoodStatsHook(ClassNode classNode, MethodNode method, boolean isObfuscated)
	{
		String internalFoodStatsName = ASMHelper.toInternalClassName(classNode.name);

		/*
		 * Modify food values
		 */
		InsnList toInject = new InsnList();
		AbstractInsnNode targetNode = ASMHelper.findFirstInstruction(method);

		// create modifiedFoodValues variable
		LabelNode modifiedFoodValuesStart = new LabelNode();
		LabelNode end = ASMHelper.findEndLabel(method);
		LocalVariableNode modifiedFoodValues = new LocalVariableNode("modifiedFoodValues", ASMHelper.toDescriptor(ASMConstants.FOOD_STATS), null, modifiedFoodValuesStart, end, method.maxLocals);
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
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, foodStatsPlayerField, ASMHelper.toDescriptor(ASMConstants.PLAYER)));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HOOKS_INTERNAL_CLASS, "onFoodStatsAdded", ASMHelper.toMethodDescriptor(ASMConstants.FOOD_VALUES, ASMConstants.FOOD_STATS, ASMConstants.ITEM_FOOD, ASMConstants.STACK, ASMConstants.PLAYER), false));
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
		hungerNeedle.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName(ASMConstants.ITEM_FOOD), ObfHelper.isObfuscated() ? "func_150905_g" : "getHealAmount" , ASMHelper.toMethodDescriptor("I", ASMHelper.toDescriptor(ASMConstants.STACK)), false));

		InsnList hungerReplacement = new InsnList();
		hungerReplacement.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));
		hungerReplacement.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(ASMConstants.FOOD_VALUES), "hunger", "I"));

		InsnList saturationNeedle = new InsnList();
		saturationNeedle.add(new VarInsnNode(ALOAD, 1));
		saturationNeedle.add(new VarInsnNode(ALOAD, 2));
		saturationNeedle.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.item.ItemFood"), ObfHelper.isObfuscated() ? "func_150906_h" : "getSaturationModifier", ASMHelper.toMethodDescriptor("F", ASMHelper.toDescriptor(ASMConstants.STACK)), false));

		InsnList saturationReplacement = new InsnList();
		saturationReplacement.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));
		saturationReplacement.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(ASMConstants.FOOD_VALUES), "saturationModifier", "F"));

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
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, foodStatsPlayerField, ASMHelper.toDescriptor(ASMConstants.PLAYER)));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HOOKS_INTERNAL_CLASS, "onPostFoodStatsAdded",  ASMHelper.toMethodDescriptor("V", ASMConstants.FOOD_STATS, ASMConstants.ITEM_FOOD, ASMConstants.STACK, ASMConstants.FOOD_VALUES, "I", "F", ASMConstants.PLAYER), false));

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
		toInject.add(new FieldInsnNode(GETFIELD, ASMHelper.toInternalClassName(classNode.name), foodStatsPlayerField, ASMHelper.toDescriptor(ASMConstants.PLAYER)));
		toInject.add(new TypeInsnNode(NEW, ASMHelper.toInternalClassName(ASMConstants.FOOD_VALUES)));
		toInject.add(new InsnNode(DUP));
		toInject.add(new VarInsnNode(ILOAD, 1));
		toInject.add(new VarInsnNode(FLOAD, 2));
		toInject.add(new MethodInsnNode(INVOKESPECIAL, ASMHelper.toInternalClassName(ASMConstants.FOOD_VALUES), "<init>", ASMHelper.toMethodDescriptor("V", "I", "F"), false));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMConstants.HOOKS_INTERNAL_CLASS, "fireFoodStatsAdditionEvent", ASMHelper.toMethodDescriptor("Z", ASMConstants.PLAYER, ASMConstants.FOOD_VALUES), false));
		toInject.add(new JumpInsnNode(IFNE, ifCanceled));

		method.instructions.insertBefore(targetNode, toInject);

		targetNode = ASMHelper.findLastInstructionWithOpcode(method, RETURN);

		method.instructions.insertBefore(targetNode, ifCanceled);

		// BIPUSH 20 replaced with GetMaxHunger lookup
		InsnList needle = new InsnList();
		needle.add(new IntInsnNode(BIPUSH, 20));
		InsnList replacement = new InsnList();
		replacement.add(new VarInsnNode(ALOAD, 0));
		replacement.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.HOOKS), "getMaxHunger", ASMHelper.toMethodDescriptor("I", ASMConstants.FOOD_STATS), false));

		ASMHelper.findAndReplaceAll(method.instructions, needle, replacement);
	}

	private void hookUpdate(ClassNode classNode, MethodNode updateMethodNode)
	{
		LabelNode ifSkipReturn = new LabelNode();

		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.HOOKS), "onAppleCoreFoodStatsUpdate", ASMHelper.toMethodDescriptor("Z", ASMConstants.FOOD_STATS, ASMConstants.PLAYER), false));
		toInject.add(new JumpInsnNode(IFEQ, ifSkipReturn));
		toInject.add(new LabelNode());
		toInject.add(new InsnNode(RETURN));
		toInject.add(ifSkipReturn);

		updateMethodNode.instructions.insertBefore(ASMHelper.findFirstInstruction(updateMethodNode), toInject);
	}

	private void hookNeedFood(ClassNode classNode, MethodNode needFoodMethodNode)
	{
		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.HOOKS), "needFood", ASMHelper.toMethodDescriptor("Z", ASMConstants.FOOD_STATS), false));
		toInject.add(new InsnNode(IRETURN));

		needFoodMethodNode.instructions.clear();
		needFoodMethodNode.instructions.insert(toInject);
	}

	private void hookAddExhaustion(ClassNode classNode, MethodNode addExhaustionMethodNode)
	{
		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(FLOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.HOOKS), "onExhaustionAdded", ASMHelper.toMethodDescriptor("F", ASMConstants.FOOD_STATS, "F"), false));
		toInject.add(new VarInsnNode(FSTORE, 1));

		addExhaustionMethodNode.instructions.insertBefore(ASMHelper.findFirstInstruction(addExhaustionMethodNode), toInject);
	}

	private boolean tryAddFieldGetter(ClassNode classNode, String methodName, String fieldName, String fieldDescriptor)
	{
		String methodDescriptor = ASMHelper.toMethodDescriptor(fieldDescriptor);
		if (ASMHelper.findMethodNodeOfClass(classNode, methodName, methodDescriptor) != null)
			return false;

		MethodVisitor mv = classNode.visitMethod(ACC_PUBLIC, methodName, methodDescriptor, null, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, ASMHelper.toInternalClassName(classNode.name), fieldName, fieldDescriptor);
		mv.visitInsn(Type.getType(fieldDescriptor).getOpcode(IRETURN));
		mv.visitMaxs(0, 0);
		return true;
	}

	private boolean tryAddFieldSetter(ClassNode classNode, String methodName, String fieldName, String fieldDescriptor)
	{
		String methodDescriptor = ASMHelper.toMethodDescriptor("V", fieldDescriptor);
		if (ASMHelper.findMethodNodeOfClass(classNode, methodName, methodDescriptor) != null)
			return false;

		MethodVisitor mv = classNode.visitMethod(ACC_PUBLIC, methodName, methodDescriptor, null, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(Type.getType(fieldDescriptor).getOpcode(ILOAD), 1);
		mv.visitFieldInsn(PUTFIELD, ASMHelper.toInternalClassName(classNode.name), fieldName, fieldDescriptor);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		return true;
	}
}