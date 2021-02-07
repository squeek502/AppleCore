// Import all Java classes allowed by Forge into the global namespace
var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var AbstractInsnNode = Java.type("org.objectweb.asm.tree.AbstractInsnNode");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
var FrameNode = Java.type("org.objectweb.asm.tree.FrameNode");
var IincInsnNode = Java.type("org.objectweb.asm.tree.IincInsnNode");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var IntInsnNode = Java.type("org.objectweb.asm.tree.IntInsnNode");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var InvokeDynamicInsnNode = Java.type("org.objectweb.asm.tree.InvokeDynamicInsnNode");
var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
var LdcInsnNode = Java.type("org.objectweb.asm.tree.LdcInsnNode");
var LineNumberNode = Java.type("org.objectweb.asm.tree.LineNumberNode");
var LocalVariableAnnotationNode = Java.type("org.objectweb.asm.tree.LocalVariableAnnotationNode");
var LocalVariableNode = Java.type("org.objectweb.asm.tree.LocalVariableNode");
var LookupSwitchInsnNode = Java.type("org.objectweb.asm.tree.LookupSwitchInsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var MultiANewArrayInsnNode = Java.type("org.objectweb.asm.tree.MultiANewArrayInsnNode");
var TableSwitchInsnNode = Java.type("org.objectweb.asm.tree.TableSwitchInsnNode");
var TryCatchBlockNode = Java.type("org.objectweb.asm.tree.TryCatchBlockNode");
var TypeAnnotationNode = Java.type("org.objectweb.asm.tree.TypeAnnotationNode");
var TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");
var MethodNode = Java.type("org.objectweb.asm.tree.MethodNode");
var ParameterNode = Java.type("org.objectweb.asm.tree.ParameterNode");
var Attribute = Java.type("org.objectweb.asm.Attribute");
var Handle = Java.type("org.objectweb.asm.Handle");
var Label = Java.type("org.objectweb.asm.Label");
var Type = Java.type("org.objectweb.asm.Type");
var TypePath = Java.type("org.objectweb.asm.TypePath");
var TypeReference = Java.type("org.objectweb.asm.TypeReference");
