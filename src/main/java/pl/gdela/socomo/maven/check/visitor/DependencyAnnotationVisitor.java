package pl.gdela.socomo.maven.check.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class DependencyAnnotationVisitor extends AnnotationVisitor {

	private VisitorDataCollector visitorDataCollector;

	public DependencyAnnotationVisitor(VisitorDataCollector visitorDataCollector) {
		super(Opcodes.ASM4);
		this.visitorDataCollector = visitorDataCollector;
	}

	@Override
	public void visit(final String name, final Object value) {
		if (value instanceof Type) {
			visitorDataCollector.addType((Type) value);
		}
	}

	@Override
	public void visitEnum(
			final String name,
			final String desc,
			final String value)
	{
		visitorDataCollector.addDesc(desc);
	}

	@Override
	public AnnotationVisitor visitAnnotation(
			final String name,
			final String desc)
	{
		visitorDataCollector.addDesc(desc);
		return this;
	}

	@Override
	public AnnotationVisitor visitArray(final String name) {
		return this;
	}

	@Override
	public void visitEnd() {
	}
}
