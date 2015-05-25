package pl.gdela.socomo.maven.check.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;


class DependencyFieldVisitor extends FieldVisitor {

	private VisitorDataCollector visitorDataCollector;

	public DependencyFieldVisitor(VisitorDataCollector visitorDataCollector) {
		super(Opcodes.ASM4);
		this.visitorDataCollector = visitorDataCollector;
	}

	@Override
	public AnnotationVisitor visitAnnotation(
			final String desc,
			final boolean visible)
	{
		visitorDataCollector.addDesc(desc);
		return new DependencyAnnotationVisitor(visitorDataCollector);
	}

	@Override
	public void visitAttribute(Attribute attribute) {

	}

	@Override
	public void visitEnd() {

	}
}
