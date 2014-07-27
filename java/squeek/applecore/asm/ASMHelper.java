package squeek.applecore.asm;

import static org.objectweb.asm.Opcodes.GETFIELD;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class ASMHelper
{
	public static ClassNode readClassFromBytes(byte[] bytes)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		return classNode;
	}

	public static byte[] writeClassToBytes(ClassNode classNode)
	{
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public static byte[] writeClassToBytesSkipFrames(ClassNode classNode)
	{
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public static AbstractInsnNode findFirstInstruction(MethodNode method)
	{
		for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; instruction = instruction.getNext())
		{
			if (instruction.getType() != AbstractInsnNode.LABEL && instruction.getType() != AbstractInsnNode.LINE)
				return instruction;
		}
		return null;
	}

	public static AbstractInsnNode findFirstInstructionOfType(MethodNode method, int opcode)
	{
		return findNextInstructionOfType(method.instructions.getFirst(), opcode);
	}

	public static AbstractInsnNode findNextInstructionOfType(AbstractInsnNode instruction, int opcode)
	{
		for (instruction = instruction.getNext(); instruction != null; instruction = instruction.getNext())
		{
			if (instruction.getOpcode() == opcode)
				return instruction;
		}
		return null;
	}

	public static AbstractInsnNode findPreviousInstructionOfType(AbstractInsnNode instruction, int opcode)
	{
		for (instruction = instruction.getPrevious(); instruction != null; instruction = instruction.getPrevious())
		{
			if (instruction.getOpcode() == opcode)
				return instruction;
		}
		return null;
	}

	public static AbstractInsnNode findFirstInstructionOfTypeWithDesc(MethodNode method, int opcode, Object desc)
	{
		for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; instruction = instruction.getNext())
		{
			if (instruction.getOpcode() == opcode)
			{
				boolean descMatches = false;
				switch (instruction.getType())
				{
					case AbstractInsnNode.TYPE_INSN:
						descMatches = desc.equals(((TypeInsnNode) instruction).desc);
						break;
					case AbstractInsnNode.LDC_INSN:
						descMatches = desc.equals(((LdcInsnNode) instruction).cst);
						break;
					default:
						break;
				}
				if (descMatches)
					return instruction;
			}
		}
		return null;
	}

	public static AbstractInsnNode findField(MethodNode method, String field, String type, int timeFound)
	{
		int found = 0;
		for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; instruction = instruction.getNext())
		{
			if (instruction.getOpcode() == GETFIELD)
			{
				FieldInsnNode fieldNode = (FieldInsnNode) instruction;
				if (fieldNode.name.equals(field) && fieldNode.desc.equals(type))
				{
					++found;
					if (found == timeFound)
					{
						return instruction;
					}
				}
			}
		}
		return null;
	}

	public static MethodNode findMethodNodeOfClass(ClassNode classNode, String methodName, String methodDesc)
	{
		for (MethodNode method : classNode.methods)
		{
			if (method.name.equals(methodName) && (methodDesc == null || method.desc.equals(methodDesc)))
			{
				return method;
			}
		}
		return null;
	}

	public static LabelNode findEndLabel(MethodNode method)
	{
		for (AbstractInsnNode instruction = method.instructions.getLast(); instruction != null; instruction = instruction.getPrevious())
		{
			if (instruction instanceof LabelNode)
				return (LabelNode) instruction;
		}
		return null;
	}

	public static AbstractInsnNode findLastInstructionOfType(MethodNode method, int bytecode)
	{
		for (AbstractInsnNode instruction = method.instructions.getLast(); instruction != null; instruction = instruction.getPrevious())
		{
			if (instruction.getOpcode() == bytecode)
				return instruction;
		}
		return null;
	}

	public static void removeNodesFromMethodUntil(MethodNode method, AbstractInsnNode startInclusive, AbstractInsnNode endNotInclusive)
	{
		AbstractInsnNode insnToRemove = startInclusive;
		while (insnToRemove != null && insnToRemove != endNotInclusive)
		{
			insnToRemove = insnToRemove.getNext();
			method.instructions.remove(insnToRemove.getPrevious());
		}
	}
}
