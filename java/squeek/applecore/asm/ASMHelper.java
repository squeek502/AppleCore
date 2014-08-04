package squeek.applecore.asm;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

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
		ClassWriter writer = new ObfRemappingClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public static byte[] writeClassToBytesNoDeobf(ClassNode classNode)
	{
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public static AbstractInsnNode getOrFindInstructionOfType(AbstractInsnNode firstInsnToCheck, int opcode)
	{
		return getOrFindInstructionOfType(firstInsnToCheck, opcode, false);
	}

	public static AbstractInsnNode getOrFindInstructionOfType(AbstractInsnNode firstInsnToCheck, int opcode, boolean reverseDirection)
	{
		for (AbstractInsnNode instruction = firstInsnToCheck; instruction != null; instruction = reverseDirection ? instruction.getPrevious() : instruction.getNext())
		{
			if (instruction.getOpcode() == opcode)
				return instruction;
		}
		return null;
	}

	public static AbstractInsnNode getOrFindInstruction(AbstractInsnNode firstInsnToCheck)
	{
		return getOrFindInstruction(firstInsnToCheck, false);
	}

	public static AbstractInsnNode getOrFindInstruction(AbstractInsnNode firstInsnToCheck, boolean reverseDirection)
	{
		for (AbstractInsnNode instruction = firstInsnToCheck; instruction != null; instruction = reverseDirection ? instruction.getPrevious() : instruction.getNext())
		{
			if (instruction.getType() != AbstractInsnNode.LABEL && instruction.getType() != AbstractInsnNode.LINE)
				return instruction;
		}
		return null;
	}

	public static AbstractInsnNode findFirstInstruction(MethodNode method)
	{
		return getOrFindInstruction(method.instructions.getFirst());
	}

	public static AbstractInsnNode findFirstInstructionOfType(MethodNode method, int opcode)
	{
		return getOrFindInstructionOfType(method.instructions.getFirst(), opcode);
	}

	public static AbstractInsnNode findLastInstructionOfType(MethodNode method, int opcode)
	{
		return getOrFindInstructionOfType(method.instructions.getLast(), opcode, true);
	}

	public static AbstractInsnNode findNextInstruction(AbstractInsnNode instruction)
	{
		return getOrFindInstruction(instruction.getNext());
	}

	public static AbstractInsnNode findNextInstructionOfType(AbstractInsnNode instruction, int opcode)
	{
		return getOrFindInstructionOfType(instruction.getNext(), opcode);
	}

	public static AbstractInsnNode findPreviousInstruction(AbstractInsnNode instruction)
	{
		return getOrFindInstruction(instruction.getPrevious(), true);
	}

	public static AbstractInsnNode findPreviousInstructionOfType(AbstractInsnNode instruction, int opcode)
	{
		return getOrFindInstructionOfType(instruction.getPrevious(), opcode, true);
	}

	public static AbstractInsnNode findFirstInstructionOfTypeWithDesc(MethodNode method, int opcode, Object desc)
	{
		for (AbstractInsnNode instruction = getOrFindInstructionOfType(method.instructions.getFirst(), opcode); instruction != null; instruction = findNextInstructionOfType(instruction, opcode))
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

	public static int removeFromInsnListUntil(InsnList insnList, AbstractInsnNode startInclusive, AbstractInsnNode endNotInclusive)
	{
		AbstractInsnNode insnToRemove = startInclusive;
		int numDeleted = 0;
		while (insnToRemove != null && insnToRemove != endNotInclusive)
		{
			numDeleted++;
			insnToRemove = insnToRemove.getNext();
			insnList.remove(insnToRemove.getPrevious());
		}
		return numDeleted;
	}

	public static InsnList cloneInsnList(InsnList source)
	{
		InsnList clone = new InsnList();

		// used to map the old labels to their cloned counterpart
		Map<LabelNode, LabelNode> labelMap = new HashMap<LabelNode, LabelNode>();

		// build the label map
		for (AbstractInsnNode instruction = source.getFirst(); instruction != null; instruction = instruction.getNext())
		{
			if (instruction instanceof LabelNode)
			{
				labelMap.put(((LabelNode) instruction), new LabelNode());
			}
		}

		for (AbstractInsnNode instruction = source.getFirst(); instruction != null; instruction = instruction.getNext())
		{
			clone.add(instruction.clone(labelMap));
		}

		return clone;
	}

	public static LocalVariableNode findLocalVariableOfMethod(MethodNode method, String varName, String varDesc)
	{
		for (LocalVariableNode localVar : method.localVariables)
		{
			if (localVar.name.equals(varName) && localVar.desc.equals(varDesc))
			{
				return localVar;
			}
		}
		return null;
	}

	private static Printer printer = new Textifier();
	private static TraceMethodVisitor methodprinter = new TraceMethodVisitor(printer);

	public static String getMethodAsString(MethodNode method)
	{
		method.accept(methodprinter);
		StringWriter sw = new StringWriter();
		printer.print(new PrintWriter(sw));
		printer.getText().clear();
		return sw.toString();
	}
}
