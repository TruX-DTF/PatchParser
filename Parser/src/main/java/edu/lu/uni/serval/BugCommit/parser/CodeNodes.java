package edu.lu.uni.serval.BugCommit.parser;

import java.util.ArrayList;
import java.util.List;

public class CodeNodes {
	
	public static List<String> stmtTypes = new ArrayList<>();
	public static List<String> elements = new ArrayList<>();
	public static List<String> expressions = new ArrayList<>();
	
	static {
		stmtTypes.add("AnonymousClassDeclaration");
		stmtTypes.add("AssertStatement");// exp && msg
		stmtTypes.add("BreakStatement"); // label
		stmtTypes.add("CatchClause");    // SingleVariableDeclaration
		stmtTypes.add("ConstructorInvocation"); // arguments:Exp
		stmtTypes.add("ContinueStatement");     // label
		stmtTypes.add("DoStatement");           // exp
		stmtTypes.add("ExpressionStatement");   //exp
		stmtTypes.add("FieldDeclaration"); // modifier, Type, VariableDeclarationFragment
		stmtTypes.add("ForStatement");     // initializers (exp or VariableDeclarationExpression); exp; updaters(exp).
		stmtTypes.add("IfStatement");      // exp
		stmtTypes.add("ImportDeclaration");
		stmtTypes.add("Initializer");      // stmts
		stmtTypes.add("LabeledStatement"); // label, stmts
		stmtTypes.add("PackageDeclaration");
		stmtTypes.add("ReturnStatement");  // exp
		stmtTypes.add("SuperConstructorInvocation"); // arguments:Exp
		stmtTypes.add("SwitchCase");       // exp
		stmtTypes.add("SwitchStatement");  // exp, stmts
		stmtTypes.add("SynchronizedStatement"); // exp, stmts
		stmtTypes.add("ThrowStatement");   // exp
		stmtTypes.add("TryStatement");     // resources:Exp, stmts
		stmtTypes.add("TypeDeclarationStatement");
		stmtTypes.add("VariableDeclarationStatement"); // modifiers, type, VariableDeclarationFragment
		stmtTypes.add("WhileStatement");       // exp
		stmtTypes.add("EnhancedForStatement"); // SingleVariableDeclaration, exp
		stmtTypes.add("TypeDeclaration");
		stmtTypes.add("MethodDeclaration");// modifier, type, name, arguments:Exp
		stmtTypes.add("EnumDeclaration");      // modifiers, type
		stmtTypes.add("EnumConstantDeclaration");
		stmtTypes.add("AnnotationTypeDeclaration");
		stmtTypes.add("AnnotationTypeMemberDeclaration");
	
		elements.add("Modifier");
		elements.add("TypeParameter"); // Types
		elements.add("PrimitiveType");
		elements.add("SimpleType");
		elements.add("ArrayType"); // Type
		elements.add("ParameterizedType"); // Type
		elements.add("QualifiedType");
		elements.add("WildcardType");
		elements.add("UnionType");
		elements.add("IntersectionType");
		elements.add("NameQualifiedType");
		elements.add("SingleVariableDeclaration");  // modifier, type, exp
		elements.add("VariableDeclarationFragment");// simpleName, exp:Initializer
		elements.add("Dimension");
		elements.add("Instanceof");
		elements.add("Operator");
	
		expressions.add("NormalAnnotation");
		expressions.add("MarkerAnnotation");
		expressions.add("SingleMemberAnnotation");
		expressions.add("ArrayAccess");      // arrayExpression, indexExpression
		expressions.add("ArrayCreation");    // ArrayType, ArrayInitializer
		expressions.add("ArrayInitializer"); // exps.
		expressions.add("Assignment");       // leftHandExp, operator, rightHandExp
		expressions.add("BooleanLiteral");
		expressions.add("CastExpression");   // Type, exp
		expressions.add("CharacterLiteral");
		expressions.add("ClassInstanceCreation"); // Type, arguments:Exp, AnonymousClassDeclaration
		expressions.add("ConditionalExpression"); // conditionalExp, thenExp, elseExp
		expressions.add("CreationReference");
		expressions.add("ExpressionMethodReference");
		expressions.add("FieldAccess");     // Exp, SimpleName:identifier
		expressions.add("InfixExpression"); // leftExp, operator, rightExp, extendedOperands
		expressions.add("InstanceofExpression"); // exp, instanceof, type,
		expressions.add("LambdaExpression"); // parameters:SingleVariableDeclaration/VariableDeclarationFragment
		expressions.add("MethodInvocation"); // Name, MethodName, arguments:Exp
		expressions.add("MethodReference");
		expressions.add("NullLiteral");
		expressions.add("NumberLiteral");
		expressions.add("ParenthesizedExpression"); // Exp
		expressions.add("PostfixExpression"); // Exp, operator
		expressions.add("PrefixExpression");  // operator, Exp
		expressions.add("QualifiedName");     // Name, simpleName
		expressions.add("SimpleName");
		expressions.add("StringLiteral");
		expressions.add("SuperFieldAccess");      // Name, identifier,
		expressions.add("SuperMethodInvocation"); // MethodName, argurments:Exp
		expressions.add("SuperMethodReference");
		expressions.add("ThisExpression");
		expressions.add("TypeLiteral"); 
		expressions.add("TypeMethodReference");
		expressions.add("VariableDeclarationExpression"); // modifiers, VariableDeclarationFragment
	}
	
	/*
		map.put(65, "TagElement");
		map.put(66, "TextElement");
		map.put(67, "MemberRef");
		map.put(68, "MethodRef");
		map.put(69, "MethodRefParameter");
		map.put(80, "MemberValuePair");
	 */
}
