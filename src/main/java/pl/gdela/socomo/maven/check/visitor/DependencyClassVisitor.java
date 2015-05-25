package pl.gdela.socomo.maven.check.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


public class DependencyClassVisitor extends ClassVisitor {


	private VisitorDataCollector dataCollector;

	public DependencyClassVisitor(VisitorDataCollector visitorDataCollector) {
		super(Opcodes.ASM4);
		this.dataCollector = visitorDataCollector;
	}

	@Override
	public void visit(
			final int version,
			final int access,
			final String name,
			final String signature,
			final String superName,
			final String[] interfaces)
	{
		dataCollector.collect(name, superName, interfaces, signature);
	}

	@Override
	public void visitSource(String s, String s2) {

	}

	@Override
	public void visitOuterClass(String s, String s2, String s3) {

	}

	@Override
	public AnnotationVisitor visitAnnotation(
			final String desc,
			final boolean visible)
	{
		dataCollector.addDesc(desc);
		return new DependencyAnnotationVisitor(dataCollector);
	}

	@Override
	public void visitAttribute(Attribute attribute) {

	}

	@Override
	public void visitInnerClass(String s, String s2, String s3, int i) {

	}

	@Override
	public FieldVisitor visitField(
			final int access,
			final String name,
			final String desc,
			final String signature,
			final Object value)
	{
		if (signature == null) {
			dataCollector.addDesc(desc);
		} else {
			dataCollector.addTypeSignature(signature);
		}
		if (value instanceof Type) {
			dataCollector.addType((Type) value);
		}
		return new DependencyFieldVisitor(dataCollector);
	}


	@Override
	public MethodVisitor visitMethod(
			final int access,
			final String name,
			final String desc,
			final String signature,
			final String[] exceptions)
	{
		if (signature == null) {
			dataCollector.addMethodDesc(desc);
		} else {
			dataCollector.addSignature(signature);
		}
		dataCollector.addInternalNames(exceptions);
		return new DependencyMethodVisitor(dataCollector);
	}

	@Override
	public void visitEnd() {

	}
}
