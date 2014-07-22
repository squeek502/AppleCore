package squeek.applecore.asm.module;

import static org.objectweb.asm.Opcodes.*;
import java.util.List;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import squeek.applecore.Hooks;
import squeek.applecore.api.FoodValues;
import squeek.applecore.asm.ASMHelper;
import squeek.applecore.asm.IClassTransformerModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.DamageSource;

public class ModuleFoodStats implements IClassTransformerModule
{

	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{"net.minecraft.entity.player.EntityPlayer", "net.minecraft.util.FoodStats"};
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (transformedName.equals("net.minecraft.entity.player.EntityPlayer"))
		{
			boolean isObfuscated = !name.equals(transformedName);

			ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "<init>", null);
			if (methodNode != null)
			{
				patchEntityPlayerInit(methodNode, isObfuscated);
				// computing frames here causes a ClassNotFoundException in ClassWriter.getCommonSuperClass
				// in an obfuscated environment, so skip computing them as a workaround
				// see: http://stackoverflow.com/a/11605942
				return ASMHelper.writeClassToBytesSkipFrames(classNode);
			}
			else
				throw new RuntimeException("EntityPlayer: <init> method not found");
		}
		if (transformedName.equals("net.minecraft.util.FoodStats"))
		{
			boolean isObfuscated = !name.equals(transformedName);

			ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

			injectFoodStatsPlayerField(classNode);
			injectFoodStatsConstructor(classNode, isObfuscated);

			MethodNode addStatsMethodNode = ASMHelper.findMethodNodeOfClass(classNode, isObfuscated ? "a" : "addStats", "(IF)V");
			if (addStatsMethodNode != null)
			{
				addHungerLossRateCheck(addStatsMethodNode);
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
				addMinHungerToHeal(updateMethodNode, isObfuscated);
				addConfigurableHungerLoss(classNode, updateMethodNode, isObfuscated);
				addSeparateStarveTimer(classNode, updateMethodNode, isObfuscated);
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
		AbstractInsnNode targetNode = ASMHelper.findFirstInstructionOfTypeWithDesc(method, NEW, isObfuscated ? "zr" : "net/minecraft/util/FoodStats");

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
		classNode.fields.add(new FieldNode(ACC_PUBLIC, "player", Type.getDescriptor(EntityPlayer.class), null, null));
	}

	public void injectFoodStatsConstructor(ClassNode classNode, boolean isObfuscated)
	{
		MethodNode constructor = new MethodNode(ACC_PUBLIC, "<init>", isObfuscated ? "(Lyz;)V" : "(Lnet/minecraft/entity/player/EntityPlayer;)V", null, null);

		constructor.visitVarInsn(ALOAD, 0);
		constructor.visitMethodInsn(INVOKESPECIAL, classNode.superName, "<init>", "()V");

		constructor.visitVarInsn(ALOAD, 0); // this
		constructor.visitVarInsn(ALOAD, 1); // player param
		constructor.visitFieldInsn(PUTFIELD, classNode.name, "player", Type.getDescriptor(EntityPlayer.class));

		constructor.visitInsn(RETURN);
		constructor.visitMaxs(2, 2);
		constructor.visitEnd();

		classNode.methods.add(constructor);
	}

	public void addItemStackAwareFoodStatsHook(ClassNode classNode, MethodNode method, boolean isObfuscated)
	{
		String internalFoodStatsName = classNode.name.replace(".", "/");
		// injected code:
		/*
		FoodValues modifiedFoodValues;
		if ((modifiedFoodValues = Hooks.onFoodStatsAdded(this, par1, par2, this.player)) != null)
		{
			int prevFoodLevel = this.foodLevel;
			float prevSaturationLevel = this.foodSaturationLevel;
			
			this.addStats(modifiedFoodValues.hunger, modifiedFoodValues.saturationModifier);
			
			Hooks.onPostFoodStatsAdded(this, modifiedFoodValues, this.foodLevel - prevFoodLevel, this.foodSaturationLevel - prevSaturationLevel, this.player);
			return;
		}
		*/

		AbstractInsnNode targetNode = ASMHelper.findFirstInstruction(method);

		InsnList toInject = new InsnList();

		// create modifiedFoodValues variable
		LabelNode modifiedFoodValuesStart = new LabelNode();
		LabelNode end = ASMHelper.findEndLabel(method);
		LocalVariableNode modifiedFoodValues = new LocalVariableNode("modifiedFoodValues", Type.getDescriptor(FoodValues.class), null, modifiedFoodValuesStart, end, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(modifiedFoodValues);

		LabelNode ifJumpLabel = new LabelNode();

		// create prevFoodLevel variable
		LabelNode prevFoodLevelStart = new LabelNode();
		LocalVariableNode prevFoodLevel = new LocalVariableNode("prevFoodLevel", "I", null, prevFoodLevelStart, ifJumpLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(prevFoodLevel);

		// create prevSaturationLevel variable
		LabelNode prevSaturationLevelStart = new LabelNode();
		LocalVariableNode prevSaturationLevel = new LocalVariableNode("prevSaturationLevel", "F", null, prevSaturationLevelStart, ifJumpLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(prevSaturationLevel);

		// get modifiedFoodValues
		toInject.add(new VarInsnNode(ALOAD, 0));					// this
		toInject.add(new VarInsnNode(ALOAD, 1));					// param 1: ItemFood
		toInject.add(new VarInsnNode(ALOAD, 2));					// param 2: ItemStack
		toInject.add(new VarInsnNode(ALOAD, 0));					// this.player (together with below line)
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, "player", Type.getDescriptor(EntityPlayer.class)));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "onFoodStatsAdded", "(Lnet/minecraft/util/FoodStats;Lnet/minecraft/item/ItemFood;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;)Liguanaman/hungeroverhaul/api/FoodValues;"));
		toInject.add(new InsnNode(DUP));
		toInject.add(new VarInsnNode(ASTORE, modifiedFoodValues.index));		// modifiedFoodValues = hookClass.hookMethod(...)
		toInject.add(modifiedFoodValuesStart);								// variable scope start
		toInject.add(new JumpInsnNode(IFNULL, ifJumpLabel));		// if (modifiedFoodValues != null)

		// if true
		// save current hunger/saturation levels
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "a" : "foodLevel", "I"));
		toInject.add(new VarInsnNode(ISTORE, prevFoodLevel.index));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, isObfuscated ? "b" : "foodSaturationLevel", "F"));
		toInject.add(new VarInsnNode(FSTORE, prevSaturationLevel.index));

		// call this.addStats(IF)V with the modified values
		toInject.add(new VarInsnNode(ALOAD, 0));					// this
		toInject.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));		// modifiedFoodValues
		toInject.add(new FieldInsnNode(GETFIELD, Type.getInternalName(FoodValues.class), "hunger", "I"));
		toInject.add(new VarInsnNode(ALOAD, modifiedFoodValues.index));		// modifiedFoodValues
		toInject.add(new FieldInsnNode(GETFIELD, Type.getInternalName(FoodValues.class), "saturationModifier", "F"));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, internalFoodStatsName, isObfuscated ? "a" : "addStats", "(IF)V"));

		/*
		 * Start onPostFoodStatsAdded call
		 */
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
		toInject.add(new FieldInsnNode(GETFIELD, internalFoodStatsName, "player", Type.getDescriptor(EntityPlayer.class)));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "onPostFoodStatsAdded", "(Lnet/minecraft/util/FoodStats;Lnet/minecraft/item/ItemFood;Lnet/minecraft/item/ItemStack;Liguanaman/hungeroverhaul/api/FoodValues;IFLnet/minecraft/entity/player/EntityPlayer;)V"));
		/*
		 * End onPostFoodStatsAdded call
		 */

		// return
		toInject.add(new InsnNode(RETURN));
		toInject.add(ifJumpLabel);			// if hook returned null, will jump here

		method.instructions.insertBefore(targetNode, toInject);
	}

	private void addHungerLossRateCheck(MethodNode method)
	{
		// injected code:
		/*
		if(IguanaConfig.hungerLossRatePercentage > 0)
		{
		    // default code
		}
		*/

		AbstractInsnNode targetNode = ASMHelper.findFirstInstruction(method);

		LabelNode ifGreaterThan = new LabelNode();

		InsnList toInject = new InsnList();
		//toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(IguanaConfig.class), "hungerLossRatePercentage", "I"));
		toInject.add(new JumpInsnNode(IFLE, ifGreaterThan));

		method.instructions.insertBefore(targetNode, toInject);

		targetNode = ASMHelper.findLastInstructionOfType(method, PUTFIELD).getNext().getNext();

		method.instructions.insertBefore(targetNode, ifGreaterThan);
	}

	private void addMinHungerToHeal(MethodNode method, boolean isObfuscated)
	{
		// modified code:
		/*
		this.foodLevel >= 18
			modified to:
		this.foodLevel >= IguanaConfig.minHungerToHeal && IguanaConfig.healthRegenRatePercentage > 0
		*/

		AbstractInsnNode targetNode = ASMHelper.findField(method, isObfuscated ? "a" : "foodLevel", "I", 3).getNext().getNext();

		method.instructions.remove(targetNode.getPrevious());
		//method.instructions.insertBefore(targetNode, new FieldInsnNode(GETSTATIC, Type.getInternalName(IguanaConfig.class), "minHungerToHeal", "I"));

		LabelNode ifLabel = null;
		if (targetNode.getOpcode() == IF_ICMPLT)
			ifLabel = ((JumpInsnNode) targetNode).label;
		else
			throw new RuntimeException("IF_ICMPLT node not found");

		InsnList ifRegenRateInstructions = new InsnList();
		//ifRegenRateInstructions.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(IguanaConfig.class), "healthRegenRatePercentage", "I"));
		ifRegenRateInstructions.add(new JumpInsnNode(IFLE, ifLabel));

		method.instructions.insert(targetNode, ifRegenRateInstructions);

		while (targetNode != null && targetNode.getOpcode() != BIPUSH)
		{
			targetNode = targetNode.getNext();
		}
		targetNode = targetNode.getNext();

		// remove BIPUSH
		method.instructions.remove(targetNode.getPrevious());

		InsnList toInject = new InsnList();
		toInject.add(new InsnNode(I2F));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "getHealthRegenPeriod", "(Lnet/minecraft/entity/player/EntityPlayer;)F"));
		toInject.add(new InsnNode(FCMPL));

		// change to IFLT
		((JumpInsnNode) targetNode).setOpcode(IFLT);

		method.instructions.insertBefore(targetNode, toInject);

		while (targetNode != null && targetNode.getOpcode() != LDC)
		{
			targetNode = targetNode.getNext();
		}

		LabelNode ifHungerDrainDisabled = new LabelNode();

		toInject.clear();
		//toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(IguanaConfig.class), "disableHealingHungerDrain", "Z"));
		toInject.add(new JumpInsnNode(IFNE, ifHungerDrainDisabled));

		method.instructions.insertBefore(targetNode.getPrevious(), toInject);
		method.instructions.insert(targetNode.getNext(), ifHungerDrainDisabled);

		// remove the else if block by removing all the way until hitting the GOTO
		AbstractInsnNode insnToRemove = ifLabel.getNext();
		while (insnToRemove != null && insnToRemove.getOpcode() != GOTO)
		{
			insnToRemove = insnToRemove.getNext();
			method.instructions.remove(insnToRemove.getPrevious());
		}
		// remove the GOTO as well
		if (insnToRemove != null)
			method.instructions.remove(insnToRemove);
	}

	private void addConfigurableHungerLoss(ClassNode classNode, MethodNode method, boolean isObfuscated)
	{
		// code modified to:
		/*
		if (IguanaConfig.hungerLossRatePercentage == 0)
		{
			foodExhaustionLevel = 0.0F;
			foodSaturationLevel = 0.0F;
			foodLevel = 19;
		}
		else if (this.foodExhaustionLevel > Hooks.getMaxExhaustion(player))
		{
		    this.foodExhaustionLevel = 0.0F;

			// default code
		}
		 */

		AbstractInsnNode firstInjectPoint = ASMHelper.findFirstInstructionOfType(method, PUTFIELD);
		AbstractInsnNode maxExhaustionReplacePoint = ASMHelper.findFirstInstructionOfType(method, LDC);

		LabelNode afterFoodExhaustionCheck = null;
		AbstractInsnNode ifLE = maxExhaustionReplacePoint.getNext().getNext();
		if (ifLE.getOpcode() == IFLE)
			afterFoodExhaustionCheck = ((JumpInsnNode) ifLE).label;

		if (firstInjectPoint == null || maxExhaustionReplacePoint == null || ifLE == null || afterFoodExhaustionCheck == null)
			throw new RuntimeException("Unexpected instructions found in FoodStats.onUpdate");

		InsnList toInject = new InsnList();

		LabelNode ifHungerLossPercentageNotZero = new LabelNode();

		//toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(IguanaConfig.class), "hungerLossRatePercentage", "I"));
		toInject.add(new JumpInsnNode(IFNE, ifHungerLossPercentageNotZero));

		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(FCONST_0));
		toInject.add(new FieldInsnNode(PUTFIELD, classNode.name.replace(".", "/"), isObfuscated ? "c" : "foodExhaustionLevel", "F"));

		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(FCONST_0));
		toInject.add(new FieldInsnNode(PUTFIELD, classNode.name.replace(".", "/"), isObfuscated ? "b" : "foodSaturationLevel", "F"));

		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(BIPUSH, 19));
		toInject.add(new FieldInsnNode(PUTFIELD, classNode.name.replace(".", "/"), isObfuscated ? "a" : "foodLevel", "I"));

		toInject.add(new JumpInsnNode(GOTO, afterFoodExhaustionCheck));
		toInject.add(ifHungerLossPercentageNotZero);

		method.instructions.insert(firstInjectPoint, toInject);

		toInject.clear();
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "getMaxExhaustion", "(Lnet/minecraft/entity/player/EntityPlayer;)F"));

		method.instructions.insertBefore(maxExhaustionReplacePoint, toInject);
		method.instructions.remove(maxExhaustionReplacePoint);

		AbstractInsnNode setToZeroTarget = ifLE.getNext();
		while (setToZeroTarget != null && setToZeroTarget.getOpcode() != DUP)
		{
			setToZeroTarget = setToZeroTarget.getNext();
		}

		if (setToZeroTarget == null)
			throw new RuntimeException("Failed to find DUP instruction after IFLE");

		method.instructions.insertBefore(setToZeroTarget, new InsnNode(FCONST_0));

		AbstractInsnNode insnToRemove = setToZeroTarget;
		while (insnToRemove != null && insnToRemove.getOpcode() != PUTFIELD)
		{
			insnToRemove = insnToRemove.getNext();
			method.instructions.remove(insnToRemove.getPrevious());
		}
	}

	private void addSeparateStarveTimer(ClassNode classNode, MethodNode method, boolean isObfuscated)
	{
		// add starveTimer field
		classNode.fields.add(new FieldNode(ACC_PUBLIC, "starveTimer", "I", null, null));

		// injected code:
		/*
		if (this.foodLevel <= 0)
		{
		    ++this.starveTimer;

		    if (this.starveTimer >= 80)
		    {
		        player.attackEntityFrom(DamageSource.starve, IguanaConfig.damageOnStarve);
		        this.starveTimer = 0;
		    }
		}
		else
		{
			this.starveTimer = 0;
		}
		 */

		AbstractInsnNode lastReturn = ASMHelper.findLastInstructionOfType(method, RETURN);

		LabelNode ifFoodLevelNotLEZero = new LabelNode();
		LabelNode afterElse = new LabelNode();

		InsnList toInject = new InsnList();

		// if foodLevel <= 0
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, classNode.name.replace(".", "/"), isObfuscated ? "a" : "foodLevel", "I"));
		toInject.add(new JumpInsnNode(IFGT, ifFoodLevelNotLEZero));

		// increment starveTimer
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(DUP));
		toInject.add(new FieldInsnNode(GETFIELD, classNode.name.replace(".", "/"), "starveTimer", "I"));
		toInject.add(new InsnNode(ICONST_1));
		toInject.add(new InsnNode(IADD));
		toInject.add(new FieldInsnNode(PUTFIELD, classNode.name.replace(".", "/"), "starveTimer", "I"));

		// if starveTimer >= 80 then do starve damage
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new FieldInsnNode(GETFIELD, classNode.name.replace(".", "/"), "starveTimer", "I"));
		toInject.add(new VarInsnNode(BIPUSH, 80));
		toInject.add(new JumpInsnNode(IF_ICMPLT, afterElse));
		toInject.add(new VarInsnNode(ALOAD, 1)); // player
		toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(DamageSource.class), isObfuscated ? "f" : "starve", Type.getDescriptor(DamageSource.class)));
		//toInject.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(IguanaConfig.class), "damageOnStarve", "I"));
		toInject.add(new InsnNode(I2F));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EntityPlayer.class), isObfuscated ? "a" : "attackEntityFrom", isObfuscated ? "(Lro;F)Z" : "(Lnet/minecraft/util/DamageSource;F)Z"));
		toInject.add(new InsnNode(POP));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(ICONST_0));
		toInject.add(new FieldInsnNode(PUTFIELD, classNode.name.replace(".", "/"), "starveTimer", "I"));

		toInject.add(new JumpInsnNode(GOTO, afterElse));
		toInject.add(ifFoodLevelNotLEZero);

		// else set starveTimer to 0
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(ICONST_0));
		toInject.add(new FieldInsnNode(PUTFIELD, classNode.name.replace(".", "/"), "starveTimer", "I"));

		toInject.add(afterElse);

		method.instructions.insertBefore(lastReturn, toInject);
	}

}
