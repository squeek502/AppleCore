package squeek.applecore.asm.module;

import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.asm.Hooks;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.asmhelper.applecore.ASMHelper;
import squeek.asmhelper.applecore.InsnComparator;
import squeek.asmhelper.applecore.ObfHelper;

public class ModuleBlockFood implements IClassTransformerModule
{
	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{"net.minecraft.block.BlockCake"};
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

		MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_150036_b", "func_150036_b", "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;)V");

		if (methodNode != null)
		{
			addOnBlockFoodEatenHook(classNode, methodNode);
			return ASMHelper.writeClassToBytes(classNode);
		}
		else
			throw new RuntimeException("BlockCake: eatCakeSlice (func_150036_b) method not found");
	}

	private void addOnBlockFoodEatenHook(ClassNode classNode, MethodNode method)
	{
		boolean isObfuscated = method.name.startsWith("func_");
		// default p_150036_5_.getFoodStats().addStats call replaced with:
		/*
		FoodValues modifiedFoodValues = Hooks.onBlockFoodEaten(this, p_150036_1_, p_150036_5_);
		int prevFoodLevel = p_150036_5_.getFoodStats().getFoodLevel();
		float prevSaturationLevel = p_150036_5_.getFoodStats().getSaturationLevel();

		p_150036_5_.getFoodStats().addStats(modifiedFoodValues.hunger, modifiedFoodValues.saturationModifier);

		Hooks.onPostBlockFoodEaten(this, modifiedFoodValues, prevFoodLevel, prevSaturationLevel, p_150036_5_);
		*/

		AbstractInsnNode ifCanEat = ASMHelper.findFirstInstructionWithOpcode(method, IFEQ);

		if (ifCanEat == null)
			throw new RuntimeException("IFEQ instruction not found in " + classNode.name + "." + method.name);

		LabelNode ifEndLabel = ((JumpInsnNode) ifCanEat).label;

		/*
		 * Modify food values
		 */
		InsnList toInject = new InsnList();
		AbstractInsnNode targetNode = ASMHelper.findNextInstruction(ifCanEat);

		// create modifiedFoodValues variable
		LabelNode modifiedFoodValuesStart = new LabelNode();
		LocalVariableNode modifiedFoodValues = new LocalVariableNode("modifiedFoodValues", Type.getDescriptor(FoodValues.class), null, modifiedFoodValuesStart, ifEndLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(modifiedFoodValues);

		// FoodValues modifiedFoodValues = Hooks.onBlockFoodEaten(this, p_150036_1_, p_150036_5_);
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(ALOAD, 5));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "onBlockFoodEaten", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lsqueek/applecore/api/food/FoodValues;", false));
		toInject.add(new VarInsnNode(ASTORE, modifiedFoodValues.index));
		toInject.add(modifiedFoodValuesStart);

		// create prevFoodLevel variable
		LabelNode prevFoodLevelStart = new LabelNode();
		LocalVariableNode prevFoodLevel = new LocalVariableNode("prevFoodLevel", "I", null, prevFoodLevelStart, ifEndLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(prevFoodLevel);

		// int prevFoodLevel = p_150036_5_.getFoodStats().getFoodLevel();
		toInject.add(new VarInsnNode(ALOAD, 5));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.entity.player.EntityPlayer"), isObfuscated ? "func_71024_bL" : "getFoodStats", "()Lnet/minecraft/util/FoodStats;", false));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.util.FoodStats"), isObfuscated ? "func_75116_a" : "getFoodLevel", "()I", false));
		toInject.add(new VarInsnNode(ISTORE, prevFoodLevel.index));
		toInject.add(prevFoodLevelStart);

		// create prevSaturationLevel variable
		LabelNode prevSaturationLevelStart = new LabelNode();
		LocalVariableNode prevSaturationLevel = new LocalVariableNode("prevSaturationLevel", "F", null, prevSaturationLevelStart, ifEndLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(prevSaturationLevel);

		// float prevSaturationLevel = p_150036_5_.getFoodStats().getSaturationLevel();
		toInject.add(new VarInsnNode(ALOAD, 5));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.entity.player.EntityPlayer"), isObfuscated ? "func_71024_bL" : "getFoodStats", "()Lnet/minecraft/util/FoodStats;", false));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.util.FoodStats"), isObfuscated ? "func_75115_e" : "getSaturationLevel", "()F", false));
		toInject.add(new VarInsnNode(FSTORE, prevSaturationLevel.index));
		toInject.add(prevSaturationLevelStart);

		method.instructions.insertBefore(targetNode, toInject);

		/*
		 * Replace 2/0.1F with the modified values
		 */
		InsnList hungerNeedle = new InsnList();
		hungerNeedle.add(new InsnNode(ICONST_2));

		InsnList hungerReplacement = new InsnList();
		hungerReplacement.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));
		hungerReplacement.add(new FieldInsnNode(GETFIELD, Type.getInternalName(FoodValues.class), "hunger", "I"));

		InsnList saturationNeedle = new InsnList();
		saturationNeedle.add(new LdcInsnNode(0.1f));

		InsnList saturationReplacement = new InsnList();
		saturationReplacement.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));
		saturationReplacement.add(new FieldInsnNode(GETFIELD, Type.getInternalName(FoodValues.class), "saturationModifier", "F"));

		ASMHelper.findAndReplace(method.instructions, hungerNeedle, hungerReplacement, targetNode);
		ASMHelper.findAndReplace(method.instructions, saturationNeedle, saturationReplacement, targetNode);

		/*
		 * onPostBlockFoodEaten
		 */
		AbstractInsnNode targetNodeAfter = ASMHelper.find(targetNode, new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.world.World"), InsnComparator.WILDCARD, "(III)I", false));
		targetNodeAfter = ASMHelper.findPreviousLabelOrLineNumber(targetNodeAfter).getNext();
		InsnList toInjectAfter = new InsnList();

		// Hooks.onPostBlockFoodEaten(this, modifiedFoodValues, prevFoodLevel, prevSaturationLevel, p_150036_5_);
		toInjectAfter.add(new VarInsnNode(ALOAD, 0));
		toInjectAfter.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));
		toInjectAfter.add(new VarInsnNode(ILOAD, prevFoodLevel.index));
		toInjectAfter.add(new VarInsnNode(FLOAD, prevSaturationLevel.index));
		toInjectAfter.add(new VarInsnNode(ALOAD, 5));
		toInjectAfter.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "onPostBlockFoodEaten", "(Lnet/minecraft/block/Block;Lsqueek/applecore/api/food/FoodValues;IFLnet/minecraft/entity/player/EntityPlayer;)V", false));

		method.instructions.insertBefore(targetNodeAfter, toInjectAfter);
	}
}
