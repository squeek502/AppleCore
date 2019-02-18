package squeek.applecore.asm.module;

import org.objectweb.asm.tree.*;
import squeek.applecore.asm.ASMConstants;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.asmhelper.applecore.ASMHelper;
import squeek.asmhelper.applecore.InsnComparator;
import squeek.asmhelper.applecore.ObfHelper;

import static org.objectweb.asm.Opcodes.*;

public class ModuleExhaustingActions implements IClassTransformerModule
{
	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{ASMConstants.PLAYER, ASMConstants.BLOCK, ASMConstants.BLOCK_CONTAINER, ASMConstants.BLOCK_ICE, ASMConstants.POTION};
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (transformedName.equals(ASMConstants.PLAYER))
		{
			ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

			MethodNode jumpMethod = ASMHelper.findMethodNodeOfClass(classNode, ObfHelper.isObfuscated() ? "func_70664_aZ" : "jump", "()V");
			if (jumpMethod == null)
				throw new RuntimeException("EntityPlayer.jump method not found");
			patchSimpleAddExhaustionCall(classNode, jumpMethod, 1, "NORMAL_JUMP");
			patchSimpleAddExhaustionCall(classNode, jumpMethod, 0, "SPRINTING_JUMP");

			MethodNode damageEntityMethod = ASMHelper.findMethodNodeOfClass(classNode, ObfHelper.isObfuscated() ? "func_70665_d" : "damageEntity", ASMHelper.toMethodDescriptor("V", ASMConstants.DAMAGE_SOURCE, "F"));
			if (damageEntityMethod == null)
				throw new RuntimeException("EntityPlayer.damageEntity method not found");
			patchDamageEntity(damageEntityMethod);

			MethodNode attackEntityMethod = ASMHelper.findMethodNodeOfClass(classNode, ObfHelper.isObfuscated() ? "func_71059_n" : "attackTargetEntityWithCurrentItem", ASMHelper.toMethodDescriptor("V", ASMConstants.ENTITY));
			if (attackEntityMethod == null)
				throw new RuntimeException("EntityPlayer.attackTargetEntityWithCurrentItem method not found");
			patchSimpleAddExhaustionCall(classNode, attackEntityMethod, 0, "ATTACK_ENTITY");

			MethodNode addMovementStatMethod = ASMHelper.findMethodNodeOfClass(classNode, ObfHelper.isObfuscated() ? "func_71000_j" : "addMovementStat", ASMHelper.toMethodDescriptor("V", "D", "D", "D"));
			if (addMovementStatMethod == null)
				throw new RuntimeException("EntityPlayer.addMovementStat method not found");
			patchMovementStat(addMovementStatMethod, 0, "MOVEMENT_DIVE");
			patchMovementStat(addMovementStatMethod, 1, "MOVEMENT_SWIM");
			patchMovementStat(addMovementStatMethod, 2, "MOVEMENT_SPRINT");
			patchMovementStat(addMovementStatMethod, 3, "MOVEMENT_CROUCH");
			patchMovementStat(addMovementStatMethod, 4, "MOVEMENT_WALK");

			return ASMHelper.writeClassToBytes(classNode);
		}
		else if (transformedName.equals(ASMConstants.BLOCK) || transformedName.equals(ASMConstants.BLOCK_CONTAINER) || transformedName.equals(ASMConstants.BLOCK_ICE))
		{
			ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);
			String methodDesc = ASMHelper.toMethodDescriptor(
				"V",
				ASMConstants.WORLD,
				ASMConstants.PLAYER,
				ASMConstants.BLOCK_POS,
				ASMConstants.IBLOCKSTATE,
				ASMConstants.TILE_ENTITY,
				ASMConstants.ITEM_STACK
			);
			MethodNode harvestBlock = ASMHelper.findMethodNodeOfClass(classNode, ObfHelper.isObfuscated() ? "func_180657_a" : "harvestBlock", methodDesc);
			if (harvestBlock == null)
				throw new RuntimeException("Block.harvestBlock method not found in class " + classNode.name + " with desc " + methodDesc);
			patchSimpleAddExhaustionCall(classNode, harvestBlock, 0, "HARVEST_BLOCK");
			return ASMHelper.writeClassToBytes(classNode);
		}
		else if (transformedName.equals(ASMConstants.POTION))
		{
			ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);
			MethodNode performEffect = ASMHelper.findMethodNodeOfClass(classNode, ObfHelper.isObfuscated() ? "func_76394_a" : "performEffect", ASMHelper.toMethodDescriptor("V", ASMConstants.ENTITY_LIVING, "I"));
			if (performEffect == null)
				throw new RuntimeException("Potion.performEffect method not found");

			AbstractInsnNode call = getAddExhaustionCall(performEffect);
			AbstractInsnNode load = ASMHelper.findPreviousInstructionWithOpcode(call, CHECKCAST);
			if (load == null)
				throw new RuntimeException("Unexpected instruction pattern found in Potion.performEffect:\n" + ASMHelper.getInsnListAsString(performEffect.instructions));

			patchAddExhaustionCall(performEffect.instructions, load, call, 1, "HUNGER_POTION");
			// inserted ALOAD needs a CHECKCAST
			performEffect.instructions.insert(load.getNext(), new TypeInsnNode(CHECKCAST, ObfHelper.getInternalClassName(ASMConstants.PLAYER)));
			return ASMHelper.writeClassToBytes(classNode);
		}
		return basicClass;
	}

	// transform
	// search for pattern:
	/*
	ALOAD 0
    LDC 0.2
    INVOKEVIRTUAL net/minecraft/entity/player/EntityPlayer.addExhaustion (F)V
	 */
	// resulting in:
	/*
	ALOAD 0
    ALOAD 0
    GETSTATIC squeek/applecore/api/hunger/ExhaustionEvent$ExhaustingActions.SPRINTING_JUMP : Lsqueek/applecore/api/hunger/ExhaustionEvent$ExhaustingActions;
    LDC 0.2
    INVOKESTATIC squeek/applecore/asm/Hooks.fireExhaustingActionEvent (Lnet/minecraft/entity/player/EntityPlayer;Lsqueek/applecore/api/hunger/ExhaustionEvent$ExhaustingActions;F)F
    INVOKEVIRTUAL net/minecraft/entity/player/EntityPlayer.addExhaustion (F)V
	 */

	/**
	 * Usage note: This will modify the instructions so that the search pattern will not be found in the next call
	 * so doing something like patchAddExhaustionCall(..., 0, ...); patchAddExhaustionCall(..., 1, ...); will fail if there
	 * are only two instances of that pattern in the function. Should either reverse it (so the second gets modified first) or call
	 * it twice with index 0 both times.
	 */
	private void patchSimpleAddExhaustionCall(ClassNode classNode, MethodNode method, int patternIndex, String exhaustingActionEnum)
	{
		AbstractInsnNode start = null;
		AbstractInsnNode haystackStart = method.instructions.getFirst();

		for (int i = 0; i <= patternIndex; i++)
		{
			InsnList needle = new InsnList();
			needle.add(new VarInsnNode(ALOAD, InsnComparator.INT_WILDCARD));
			needle.add(new LdcInsnNode(InsnComparator.WILDCARD));
			needle.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName(ASMConstants.PLAYER), ObfHelper.isObfuscated() ? "func_71020_j" : "addExhaustion", ASMHelper.toMethodDescriptor("V", "F"), false));

			start = ASMHelper.find(haystackStart, needle);
			if (start == null || start.getNext() == null)
				throw new RuntimeException("EntityPlayer.addExhaustion call pattern (index=" + patternIndex + ") not found in " + classNode.name + "." + method.name);

			haystackStart = start.getNext();
		}

		patchAddExhaustionCall(method.instructions, start, start.getNext().getNext(), ((VarInsnNode) start).var, exhaustingActionEnum);
	}

	private void patchAddExhaustionCall(InsnList instructions, AbstractInsnNode loadPoint, AbstractInsnNode callPoint, int playerLoadIndex, String exhaustingAction)
	{
		// load the player
		AbstractInsnNode loadPlayer = new VarInsnNode(ALOAD, playerLoadIndex);
		instructions.insert(loadPoint, loadPlayer);
		// add a GETSTATIC for the enum
		AbstractInsnNode getEnum = new FieldInsnNode(GETSTATIC, ObfHelper.getInternalClassName(ASMConstants.ExhaustionEvent.EXHAUSTING_ACTIONS), exhaustingAction, ASMHelper.toDescriptor(ASMConstants.ExhaustionEvent.EXHAUSTING_ACTIONS));
		instructions.insert(loadPlayer, getEnum);
		// add an INVOKE for the fire event hook before the call
		AbstractInsnNode fireEvent = new MethodInsnNode(INVOKESTATIC, ASMConstants.HOOKS_INTERNAL_CLASS, "fireExhaustingActionEvent", ASMHelper.toMethodDescriptor("F", ASMConstants.PLAYER, ASMConstants.ExhaustionEvent.EXHAUSTING_ACTIONS, "F"), false);
		instructions.insertBefore(callPoint, fireEvent);
	}

	// special case for the call in EntityPlayer.damageEntity
	private void patchDamageEntity(MethodNode method)
	{
		AbstractInsnNode addExhaustionCall = getAddExhaustionCall(method);
		AbstractInsnNode loadPoint = addExhaustionCall.getPrevious().getPrevious().getPrevious();
		patchAddExhaustionCall(method.instructions, loadPoint, addExhaustionCall, 0, "DAMAGE_TAKEN");
	}

	// special case for the calls in EntityPlayer.addMovementStat
	private void patchMovementStat(MethodNode method, int callIndex, String exhaustingAction)
	{
		AbstractInsnNode addExhaustionCall = getAddExhaustionCall(method, callIndex);

		AbstractInsnNode loadPoint = ASMHelper.findPreviousInstructionWithOpcode(addExhaustionCall, ALOAD);
		if (loadPoint == null)
			throw new RuntimeException("No ALOAD found before addExhaustion call (index=" + callIndex + ") in EntityPlayer.addMovementStat");

		patchAddExhaustionCall(method.instructions, loadPoint, addExhaustionCall, 0, exhaustingAction);
	}

	private AbstractInsnNode getAddExhaustionCall(MethodNode method)
	{
		return getAddExhaustionCall(method, 0);
	}

	private AbstractInsnNode getAddExhaustionCall(MethodNode method, int callIndex)
	{
		AbstractInsnNode addExhaustionCall = null;
		AbstractInsnNode haystackStart = method.instructions.getFirst();
		AbstractInsnNode needle = new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName(ASMConstants.PLAYER), ObfHelper.isObfuscated() ? "func_71020_j" : "addExhaustion", ASMHelper.toMethodDescriptor("V", "F"), false);

		for (int i = 0; i <= callIndex && haystackStart != null; i++)
		{
			addExhaustionCall = ASMHelper.find(haystackStart, needle);
			if (addExhaustionCall == null)
				throw new RuntimeException("No addExhaustion call (index=" + callIndex + ") found in " + method.name + ":\n" + ASMHelper.getInsnListAsString(method.instructions));
			haystackStart = addExhaustionCall.getNext();
		}

		return addExhaustionCall;
	}
}
