package squeek.applecore.asm.module;

import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.tree.*;
import squeek.applecore.asm.ASMConstants;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.asmhelper.applecore.ASMHelper;
import squeek.asmhelper.applecore.InsnComparator;
import squeek.asmhelper.applecore.ObfHelper;

public class ModulePlantGrowth implements IClassTransformerModule
{
	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{
		"net.minecraft.block.BlockCrops",
		"net.minecraft.block.BlockReed",
		"net.minecraft.block.BlockCactus",
		"net.minecraft.block.BlockCocoa",
		"net.minecraft.block.BlockMushroom",
		"net.minecraft.block.BlockNetherWart",
		"net.minecraft.block.BlockSapling",
		"net.minecraft.block.BlockStem",
		"com.pam.harvestcraft.BlockPamFruit",
		"com.pam.harvestcraft.BlockPamSapling",
		"mods.natura.blocks.crops.BerryBush",
		"mods.natura.blocks.crops.NetherBerryBush",
		"mods.natura.blocks.crops.CropBlock",
		"mods.natura.blocks.crops.Glowshroom"
		};
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

		MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_149674_a", "updateTick", "(Lnet/minecraft/world/World;IIILjava/util/Random;)V");

		if (methodNode == null)
			methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_149674_a", "(Lnet/minecraft/world/World;IIILjava/util/Random;)V");

		if (methodNode == null)
			throw new RuntimeException(classNode.name + ": updateTick method not found");

		if (transformedName.equals("net.minecraft.block.BlockCrops"))
			hookBlockCrops(classNode, methodNode);
		else if (transformedName.equals("net.minecraft.block.BlockReed"))
			hookBlockReed(classNode, methodNode);
		else if (transformedName.equals("net.minecraft.block.BlockCactus"))
			hookBlockCactus(classNode, methodNode);
		else if (transformedName.equals("net.minecraft.block.BlockCocoa"))
			hookBlockCocoa(classNode, methodNode);
		else if (transformedName.equals("net.minecraft.block.BlockMushroom"))
			hookBlockMushroom(classNode, methodNode);
		else if (transformedName.equals("net.minecraft.block.BlockNetherWart"))
			hookBlockNetherWart(classNode, methodNode);
		else if (transformedName.equals("net.minecraft.block.BlockSapling"))
			hookBlockSapling(classNode, methodNode);
		else if (transformedName.equals("net.minecraft.block.BlockStem"))
			hookBlockStem(classNode, methodNode);
		else if (transformedName.equals("com.pam.harvestcraft.BlockPamFruit"))
			hookBlockPamFruit(classNode, methodNode);
		else if (transformedName.equals("com.pam.harvestcraft.BlockPamSapling"))
			hookBlockPamSapling(classNode, methodNode);
		else if (transformedName.equals("mods.natura.blocks.crops.BerryBush") || transformedName.equals("mods.natura.blocks.crops.NetherBerryBush"))
			hookBlockNaturaBerryBush(classNode, methodNode);
		else if (transformedName.equals("mods.natura.blocks.crops.CropBlock"))
			hookNaturaCropBlock(classNode, methodNode);
		else if (transformedName.equals("mods.natura.blocks.crops.Glowshroom"))
			hookBlockMushroom(classNode, methodNode);
		else
			throw new RuntimeException("Unexpected class passed to transformer : " + transformedName);

		return ASMHelper.writeClassToBytes(classNode);
	}

	private void hookBlockCrops(ClassNode classNode, MethodNode method)
	{
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IF_ICMPLT);
		AbstractInsnNode ifStartPoint = ASMHelper.findNextInstruction(ASMHelper.findFirstInstructionWithOpcode(method, INVOKESPECIAL));

		if (ifStartPoint == null)
			throw new RuntimeException("Failed to transform BlockCrops, INVOKESPECIAL instruction not found");

		LabelNode endLabel = ASMHelper.findEndLabel(method);
		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ifStartPoint, endLabel);
		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		ifStartPoint = ASMHelper.findPreviousLabelOrLineNumber((ASMHelper.findNextInstructionWithOpcode(ifJumpInsn, LDC))).getNext();
		ifJumpInsn = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(ifStartPoint, IFNE);

		ifFailedLabel = ifJumpInsn.label;
		ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		LocalVariableNode metadataVar = findStoredMetadataLocalVariable(method);
		int previousMetadataIndex = storeMetadataInNewVariable(method, metadataVar);

		injectOnGrowthEventBefore(method, ifFailedLabel, previousMetadataIndex);
	}

	private void hookBlockReed(ClassNode classNode, MethodNode method)
	{
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IF_ICMPGE);
		LabelNode ifDeniedLabel = ifJumpInsn.label;

		injectNotDeniedCheckBefore(method, ifJumpInsn.getNext(), ifDeniedLabel);

		LocalVariableNode metadataVar = findStoredMetadataLocalVariable(method);
		int previousMetadataIndex = metadataVar.index;

		// need to change the GOTO so that it doesn't skip our added fireEvent call
		LabelNode newGotoLabel = new LabelNode();
		JumpInsnNode gotoInsn = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(ifJumpInsn, GOTO);
		gotoInsn.label = newGotoLabel;

		method.instructions.insertBefore(ifDeniedLabel, newGotoLabel);
		injectOnGrowthEventBefore(method, ifDeniedLabel, previousMetadataIndex);
	}

	// TODO: same as hookBlockReed, should they use a shared method?
	private void hookBlockCactus(ClassNode classNode, MethodNode method)
	{
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IF_ICMPGE);
		LabelNode ifDeniedLabel = ifJumpInsn.label;

		injectNotDeniedCheckBefore(method, ifJumpInsn.getNext(), ifDeniedLabel);

		LocalVariableNode metadataVar = findStoredMetadataLocalVariable(method);
		int previousMetadataIndex = metadataVar.index;

		// need to change the GOTO so that it doesn't skip our added fireEvent call
		LabelNode newGotoLabel = new LabelNode();
		JumpInsnNode gotoInsn = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(ifJumpInsn, GOTO);
		gotoInsn.label = newGotoLabel;

		method.instructions.insertBefore(ifDeniedLabel, newGotoLabel);
		injectOnGrowthEventBefore(method, ifDeniedLabel, previousMetadataIndex);
	}

	private void hookBlockCocoa(ClassNode classNode, MethodNode method)
	{
		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ASMHelper.findFirstInstruction(method), ASMHelper.findEndLabel(method));

		// get second IFNE
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IFNE);
		ifJumpInsn = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(ifJumpInsn, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousLabelOrLineNumber(ifJumpInsn).getNext();

		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		LocalVariableNode metadataVar = findStoredMetadataLocalVariable(method);
		int previousMetadataIndex = metadataVar.index;

		injectOnGrowthEventBefore(method, ifFailedLabel, previousMetadataIndex);
	}

	private void hookBlockMushroom(ClassNode classNode, MethodNode method)
	{
		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ASMHelper.findFirstInstruction(method), ASMHelper.findEndLabel(method));

		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousLabelOrLineNumber(ifJumpInsn).getNext();

		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		fixPrecedingIfsToNotSkipInjectedInstructions(method, ifFailedLabel);
		injectOnGrowthWithoutMetadataChangeEventBefore(method, ifFailedLabel);
	}

	private void hookBlockNetherWart(ClassNode classNode, MethodNode method)
	{
		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ASMHelper.findFirstInstruction(method), ASMHelper.findEndLabel(method));

		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousInstructionWithOpcode(ifJumpInsn, IF_ICMPGE).getNext();

		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		LocalVariableNode metadataVar = findStoredMetadataLocalVariable(method);
		int previousMetadataIndex = storeMetadataInNewVariable(method, metadataVar);

		injectOnGrowthEventBefore(method, ifFailedLabel, previousMetadataIndex);
	}

	private void hookBlockSapling(ClassNode classNode, MethodNode method)
	{
		JumpInsnNode lightValueIf = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IF_ICMPLT);
		JumpInsnNode randomIf = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(lightValueIf, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findNextInstruction(ASMHelper.findFirstInstructionWithOpcode(method, INVOKESPECIAL));

		if (ifStartPoint == null)
			throw new RuntimeException("Failed to transform BlockSapling, INVOKESPECIAL instruction not found");

		LabelNode ifFailedLabel = lightValueIf.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(randomIf, ifAllowedLabel);

		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ifStartPoint, ifFailedLabel);
		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		int previousMetadataIndex = getMetadataAndStoreInNewVariableBefore(method, ifAllowedLabel.getNext(), ifFailedLabel);

		injectOnGrowthEventBefore(method, ifFailedLabel, previousMetadataIndex);
	}

	private void hookBlockStem(ClassNode classNode, MethodNode method)
	{
		JumpInsnNode lightValueIf = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IF_ICMPLT);
		AbstractInsnNode ifStartPoint = ASMHelper.getOrFindInstructionOfType(lightValueIf, AbstractInsnNode.LINE, true).getNext();

		LabelNode ifFailedLabel = lightValueIf.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(lightValueIf, ifAllowedLabel);

		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ifStartPoint, ASMHelper.findEndLabel(method));
		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		JumpInsnNode randomIf = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(lightValueIf, IFNE);
		ifStartPoint = ASMHelper.getOrFindInstructionOfType(randomIf, AbstractInsnNode.LINE, true).getNext();

		ifFailedLabel = randomIf.label;
		ifAllowedLabel = new LabelNode();
		method.instructions.insert(randomIf, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		LocalVariableNode metadataVar = findStoredMetadataLocalVariable(method);
		int previousMetadataIndex = storeMetadataInNewVariable(method, metadataVar);

		// need to change the GOTO so that it doesn't skip our added fireEvent call
		LabelNode newGotoLabel = new LabelNode();
		JumpInsnNode gotoInsn = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(randomIf, GOTO);
		gotoInsn.label = newGotoLabel;

		method.instructions.insertBefore(ifFailedLabel, newGotoLabel);
		fixPrecedingIfsToNotSkipInjectedInstructions(method, ifFailedLabel);
		injectOnGrowthEventBefore(method, ifFailedLabel, previousMetadataIndex);
	}

	private void hookBlockPamFruit(ClassNode classNode, MethodNode method)
	{
		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ASMHelper.findFirstInstruction(method), ASMHelper.findEndLabel(method));

		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousLabelOrLineNumber(ifJumpInsn).getNext();

		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		LocalVariableNode metadataVar = findStoredMetadataLocalVariable(method);
		int previousMetadataIndex = storeMetadataInNewVariable(method, metadataVar);

		injectOnGrowthEventBefore(method, ifFailedLabel, previousMetadataIndex);
	}

	private void hookBlockPamSapling(ClassNode classNode, MethodNode method)
	{
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findLastInstructionWithOpcode(method, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousLabelOrLineNumber(ifJumpInsn).getNext();

		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ifStartPoint, ifFailedLabel);
		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		int previousMetadataIndex = getMetadataAndStoreInNewVariableBefore(method, ifAllowedLabel.getNext(), ifFailedLabel);
		injectOnGrowthEventBefore(method, ifFailedLabel, previousMetadataIndex);
	}

	private void hookBlockNaturaBerryBush(ClassNode classNode, MethodNode method)
	{
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IFNE);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousLabelOrLineNumber(ifJumpInsn).getNext();
		if (!classNode.name.endsWith("NetherBerryBush"))
			ifJumpInsn = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(ifJumpInsn, IF_ICMPLT);

		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ifStartPoint, ifFailedLabel);
		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		LocalVariableNode metadataVar = findStoredMetadataLocalVariable(method);
		int previousMetadataIndex = metadataVar.index;

		fixPrecedingIfsToNotSkipInjectedInstructions(method, ifFailedLabel);
		injectOnGrowthEventBefore(method, ifFailedLabel, previousMetadataIndex);
	}

	private void hookNaturaCropBlock(ClassNode classNode, MethodNode method)
	{
		JumpInsnNode ifJumpInsn = (JumpInsnNode) ASMHelper.findFirstInstructionWithOpcode(method, IF_ICMPLT);
		AbstractInsnNode ifStartPoint = ASMHelper.findPreviousLabelOrLineNumber(ifJumpInsn).getNext();

		LabelNode endLabel = ASMHelper.findEndLabel(method);
		LabelNode ifFailedLabel = ifJumpInsn.label;
		LabelNode ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		int resultIndex = fireAllowGrowthEventAndStoreResultBefore(method, ifStartPoint, endLabel);
		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		ifStartPoint = ASMHelper.findNextInstructionWithOpcode(ifJumpInsn, LDC).getPrevious();
		ifJumpInsn = (JumpInsnNode) ASMHelper.findNextInstructionWithOpcode(ifStartPoint, IFNE);

		ifFailedLabel = ifJumpInsn.label;
		ifAllowedLabel = new LabelNode();
		method.instructions.insert(ifJumpInsn, ifAllowedLabel);

		injectAllowedOrDefaultCheckBefore(method, ifStartPoint, resultIndex, ifAllowedLabel, ifFailedLabel);

		LocalVariableNode metadataVar = findStoredMetadataLocalVariable(method);
		int previousMetadataIndex = storeMetadataInNewVariable(method, metadataVar);

		injectOnGrowthEventBefore(method, ifFailedLabel, previousMetadataIndex);
	}

	private LocalVariableNode findStoredMetadataLocalVariable(MethodNode method)
	{
		InsnList needle = new InsnList();
		needle.add(new VarInsnNode(ALOAD, 1)); // world
		needle.add(new VarInsnNode(ILOAD, 2)); // x
		needle.add(new VarInsnNode(ILOAD, 3)); // y
		needle.add(new VarInsnNode(ILOAD, 4)); // z
		String getBlockMetadataName = ObfHelper.isObfuscated() ? "func_72805_g" : "getBlockMetadata";
		needle.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.world.World"), getBlockMetadataName, "(III)I", false));
		needle.add(new VarInsnNode(ISTORE, InsnComparator.INT_WILDCARD));

		InsnList foundInsns = ASMHelper.findAndGetFoundInsnList(method.instructions.getFirst(), needle);
		if (foundInsns.getFirst() == null)
			throw new RuntimeException("Could not find pattern:\n" + ASMHelper.getInsnListAsString(needle) + "\nin method instructions:\n" + ASMHelper.getInsnListAsString(method.instructions));
		VarInsnNode insnISTORE = (VarInsnNode) foundInsns.getLast();
		for (LocalVariableNode localVar : method.localVariables)
		{
			if (localVar.index == insnISTORE.var)
				return localVar;
		}
		return null;
	}

	private int storeMetadataInNewVariable(MethodNode method, LocalVariableNode metadataVar)
	{
		LabelNode previousMetadataStart = new LabelNode();
		LocalVariableNode previousMetadata = new LocalVariableNode("previousMetadata", "I", null, previousMetadataStart, metadataVar.end, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(previousMetadata);

		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ILOAD, metadataVar.index));
		toInject.add(new VarInsnNode(ISTORE, previousMetadata.index));
		toInject.add(previousMetadataStart);
		method.instructions.insert(metadataVar.start, toInject);

		return previousMetadata.index;
	}

	private int getMetadataAndStoreInNewVariableBefore(MethodNode method, AbstractInsnNode injectPoint, LabelNode endLabel)
	{
		LabelNode previousMetadataStart = new LabelNode();
		LocalVariableNode previousMetadata = new LocalVariableNode("previousMetadata", "I", null, previousMetadataStart, endLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(previousMetadata);

		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 1)); // world
		toInject.add(new VarInsnNode(ILOAD, 2)); // x
		toInject.add(new VarInsnNode(ILOAD, 3)); // y
		toInject.add(new VarInsnNode(ILOAD, 4)); // z
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.world.World"), ObfHelper.isObfuscated() ? "func_72805_g" : "getBlockMetadata", "(III)I", false));
		toInject.add(new VarInsnNode(ISTORE, previousMetadata.index));
		toInject.add(previousMetadataStart);

		method.instructions.insertBefore(injectPoint, toInject);
		return previousMetadata.index;
	}

	private int fireAllowGrowthEventAndStoreResultBefore(MethodNode method, AbstractInsnNode injectPoint, LabelNode endLabel)
	{
		// create allowGrowthResult variable
		LabelNode allowGrowthResultStart = new LabelNode();
		LocalVariableNode allowGrowthResult = new LocalVariableNode("allowGrowthResult", "Lcpw/mods/fml/common/eventhandler/Event$Result;", null, allowGrowthResultStart, endLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(allowGrowthResult);

		InsnList toInject = new InsnList();

		// Result allowGrowthResult = Hooks.fireAllowPlantGrowthEvent(this, p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);
		addFireGrowthEventInsnsToList(toInject);
		toInject.add(new VarInsnNode(ASTORE, allowGrowthResult.index));
		toInject.add(allowGrowthResultStart);

		method.instructions.insertBefore(injectPoint, toInject);

		return allowGrowthResult.index;
	}

	private void addFireGrowthEventInsnsToList(InsnList insnList)
	{
		insnList.add(new VarInsnNode(ALOAD, 0));
		insnList.add(new VarInsnNode(ALOAD, 1));
		insnList.add(new VarInsnNode(ILOAD, 2));
		insnList.add(new VarInsnNode(ILOAD, 3));
		insnList.add(new VarInsnNode(ILOAD, 4));
		insnList.add(new VarInsnNode(ALOAD, 5));
		insnList.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.Hooks), "fireAllowPlantGrowthEvent", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;IIILjava/util/Random;)Lcpw/mods/fml/common/eventhandler/Event$Result;", false));
	}

	private void injectAllowedOrDefaultCheckBefore(MethodNode method, AbstractInsnNode injectPoint, int resultIndex, LabelNode ifAllowedLabel, LabelNode ifFailedLabel)
	{
		InsnList toInject = new InsnList();

		toInject.add(new VarInsnNode(ALOAD, resultIndex));
		toInject.add(new FieldInsnNode(GETSTATIC, "cpw/mods/fml/common/eventhandler/Event$Result", "ALLOW", "Lcpw/mods/fml/common/eventhandler/Event$Result;"));
		toInject.add(new JumpInsnNode(IF_ACMPEQ, ifAllowedLabel));
		toInject.add(new VarInsnNode(ALOAD, resultIndex));
		toInject.add(new FieldInsnNode(GETSTATIC, "cpw/mods/fml/common/eventhandler/Event$Result", "DEFAULT", "Lcpw/mods/fml/common/eventhandler/Event$Result;"));
		toInject.add(new JumpInsnNode(IF_ACMPNE, ifFailedLabel));

		method.instructions.insertBefore(injectPoint, toInject);
	}

	private void injectNotDeniedCheckBefore(MethodNode method, AbstractInsnNode injectPoint, LabelNode ifDeniedLabel)
	{
		InsnList toInject = new InsnList();

		addFireGrowthEventInsnsToList(toInject);
		toInject.add(new FieldInsnNode(GETSTATIC, "cpw/mods/fml/common/eventhandler/Event$Result", "DENY", "Lcpw/mods/fml/common/eventhandler/Event$Result;"));
		toInject.add(new JumpInsnNode(IF_ACMPEQ, ifDeniedLabel));

		method.instructions.insertBefore(injectPoint, toInject);
	}

	private void injectOnGrowthEventBefore(MethodNode method, AbstractInsnNode injectPoint, int previousMetadataVarIndex)
	{
		InsnList toInject = new InsnList();

		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(ILOAD, 2));
		toInject.add(new VarInsnNode(ILOAD, 3));
		toInject.add(new VarInsnNode(ILOAD, 4));
		toInject.add(new VarInsnNode(ILOAD, previousMetadataVarIndex));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.Hooks), "fireOnGrowthEvent", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;IIII)V", false));

		method.instructions.insertBefore(injectPoint, toInject);
	}

	private void injectOnGrowthWithoutMetadataChangeEventBefore(MethodNode method, AbstractInsnNode injectPoint)
	{
		InsnList toInject = new InsnList();

		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(ILOAD, 2));
		toInject.add(new VarInsnNode(ILOAD, 3));
		toInject.add(new VarInsnNode(ILOAD, 4));
		toInject.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.Hooks), "fireOnGrowthWithoutMetadataChangeEvent", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;III)V", false));

		method.instructions.insertBefore(injectPoint, toInject);
	}

	private void fixPrecedingIfsToNotSkipInjectedInstructions(MethodNode method, LabelNode labelJumpedTo)
	{
		LabelNode beforeOnGrowthEvent = new LabelNode();
		method.instructions.insertBefore(labelJumpedTo, beforeOnGrowthEvent);

		AbstractInsnNode curInsn = labelJumpedTo.getPrevious();
		while (curInsn != null)
		{
			boolean isJump = curInsn instanceof JumpInsnNode;
			if (isJump && ((JumpInsnNode) curInsn).label == labelJumpedTo)
				((JumpInsnNode) curInsn).label = beforeOnGrowthEvent;
			else if (isJump)
				break;
			curInsn = curInsn.getPrevious();
		}
	}
}
