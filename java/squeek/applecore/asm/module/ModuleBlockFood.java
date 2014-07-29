package squeek.applecore.asm.module;

import static org.objectweb.asm.Opcodes.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import org.objectweb.asm.tree.*;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.asm.ASMHelper;
import squeek.applecore.asm.Hooks;
import squeek.applecore.asm.IClassTransformerModule;
import org.objectweb.asm.Type;

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
		boolean isObfuscated = !name.equals(transformedName);
		ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

		MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, isObfuscated ? "b" : "func_150036_b", isObfuscated ? "(Lahb;IIILyz;)V" : "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;)V");

		if (methodNode != null)
		{
			addOnBlockFoodEatenHook(classNode, methodNode, isObfuscated);
			return ASMHelper.writeClassToBytes(classNode);
		}
		else
			throw new RuntimeException("BlockCake: eatCakeSlice (func_150036_b) method not found");
	}

	private void addOnBlockFoodEatenHook(ClassNode classNode, MethodNode method, boolean isObfuscated)
	{
		// default p_150036_5_.getFoodStats().addStats call replaced with:
		/*
		FoodValues modifiedFoodValues = Hooks.onBlockFoodEaten(this, p_150036_1_, p_150036_5_);
		int prevFoodLevel = p_150036_5_.getFoodStats().getFoodLevel();
		float prevSaturationLevel = p_150036_5_.getFoodStats().getSaturationLevel();

		p_150036_5_.getFoodStats().addStats(modifiedFoodValues.hunger, modifiedFoodValues.saturationModifier);

		Hooks.onPostBlockFoodEaten(this, modifiedFoodValues, prevFoodLevel, prevSaturationLevel, p_150036_5_);
		*/

		AbstractInsnNode deleteStartPoint = ASMHelper.findFirstInstructionOfType(method, IFEQ);
		LabelNode ifEndLabel = null;

		if (deleteStartPoint == null)
			throw new RuntimeException("IFEQ instruction not found in " + classNode.name + "." + method.name);
		else
		{
			ifEndLabel = ((JumpInsnNode) deleteStartPoint).label;
		}

		deleteStartPoint = ASMHelper.findNextInstruction(deleteStartPoint);

		if (deleteStartPoint == null)
			throw new RuntimeException("Unexpected instruction pattern found in " + classNode.name + "." + method.name);

		AbstractInsnNode targetNode = ASMHelper.findNextInstructionOfType(deleteStartPoint, INVOKEVIRTUAL);
		while (targetNode != null && !((MethodInsnNode) targetNode).owner.equals(Type.getInternalName(FoodStats.class)))
		{
			targetNode = ASMHelper.findNextInstructionOfType(targetNode, INVOKEVIRTUAL);
		}

		if (targetNode == null)
			throw new RuntimeException("FoodStats.addStats invoke instruction not found in " + classNode.name + "." + method.name);

		ASMHelper.removeFromInsnListUntil(method.instructions, deleteStartPoint, targetNode);

		InsnList toInject = new InsnList();

		// create modifiedFoodValues variable
		LabelNode modifiedFoodValuesStart = new LabelNode();
		LocalVariableNode modifiedFoodValues = new LocalVariableNode("modifiedFoodValues", Type.getDescriptor(FoodValues.class), null, modifiedFoodValuesStart, ifEndLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(modifiedFoodValues);

		// FoodValues modifiedFoodValues = Hooks.onBlockFoodEaten(this, p_150036_1_, p_150036_5_);
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(ALOAD, 5));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "onBlockFoodEaten", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lsqueek/applecore/api/food/FoodValues;"));
		toInject.add(new VarInsnNode(ASTORE, modifiedFoodValues.index));
		toInject.add(modifiedFoodValuesStart);

		// create prevFoodLevel variable
		LabelNode prevFoodLevelStart = new LabelNode();
		LocalVariableNode prevFoodLevel = new LocalVariableNode("prevFoodLevel", "I", null, prevFoodLevelStart, ifEndLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(prevFoodLevel);

		// int prevFoodLevel = p_150036_5_.getFoodStats().getFoodLevel();
		toInject.add(new VarInsnNode(ALOAD, 5));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EntityPlayer.class), isObfuscated ? "bQ" : "getFoodStats", isObfuscated ? "()Lzr;" : "()Lnet/minecraft/util/FoodStats;"));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(FoodStats.class), isObfuscated ? "a" : "getFoodLevel", "()I"));
		toInject.add(new VarInsnNode(ISTORE, prevFoodLevel.index));
		toInject.add(prevFoodLevelStart);

		// create prevSaturationLevel variable
		LabelNode prevSaturationLevelStart = new LabelNode();
		LocalVariableNode prevSaturationLevel = new LocalVariableNode("prevSaturationLevel", "F", null, prevSaturationLevelStart, ifEndLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(prevSaturationLevel);

		// float prevSaturationLevel = p_150036_5_.getFoodStats().getSaturationLevel();
		toInject.add(new VarInsnNode(ALOAD, 5));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EntityPlayer.class), isObfuscated ? "bQ" : "getFoodStats", isObfuscated ? "()Lzr;" : "()Lnet/minecraft/util/FoodStats;"));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(FoodStats.class), isObfuscated ? "e" : "getSaturationLevel", "()F"));
		toInject.add(new VarInsnNode(FSTORE, prevSaturationLevel.index));
		toInject.add(prevSaturationLevelStart);

		// p_150036_5_.getFoodStats().addStats(modifiedFoodValues.hunger, modifiedFoodValues.saturationModifier);
		toInject.add(new VarInsnNode(ALOAD, 5));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EntityPlayer.class), isObfuscated ? "bQ" : "getFoodStats", isObfuscated ? "()Lzr;" : "()Lnet/minecraft/util/FoodStats;"));
		toInject.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));
		toInject.add(new FieldInsnNode(GETFIELD, Type.getInternalName(FoodValues.class), "hunger", "I"));
		toInject.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));
		toInject.add(new FieldInsnNode(GETFIELD, Type.getInternalName(FoodValues.class), "saturationModifier", "F"));
		// targetNode is the INVOKEVIRTUAL addStats(IF)V instruction

		method.instructions.insertBefore(targetNode, toInject);
		
		AbstractInsnNode targetNodeAfter = ASMHelper.findNextInstruction(targetNode);
		InsnList toInjectAfter = new InsnList();

		// Hooks.onPostBlockFoodEaten(this, modifiedFoodValues, prevFoodLevel, prevSaturationLevel, p_150036_5_);
		toInjectAfter.add(new VarInsnNode(ALOAD, 0));
		toInjectAfter.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));
		toInjectAfter.add(new VarInsnNode(ILOAD, prevFoodLevel.index));
		toInjectAfter.add(new VarInsnNode(FLOAD, prevSaturationLevel.index));
		toInjectAfter.add(new VarInsnNode(ALOAD, 5));
		toInjectAfter.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "onPostBlockFoodEaten", "(Lnet/minecraft/block/Block;Lsqueek/applecore/api/food/FoodValues;IFLnet/minecraft/entity/player/EntityPlayer;)V"));

		method.instructions.insertBefore(targetNodeAfter, toInjectAfter);
	}
}
