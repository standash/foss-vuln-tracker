package it.unitn.repoman.core.lang.wrappers.java;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unitn.repoman.core.lang.wrappers.generic.Wrapper;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import it.unitn.repoman.core.lang.traversals.java.JavaMethodCallTraversal;
import it.unitn.repoman.core.lang.traversals.generic.MethodCallTraversal;
import it.unitn.repoman.core.lang.parsers.java.JavaParser;

public class JavaWrapper extends Wrapper {
	
	public JavaWrapper() {
		this.restrictedKeywords = new String[]{"true", "false", "return", "null", "new", "this"};
	}
	
	@Override
	public boolean isStatement(ParseTree node) {
		return node.getClass().equals(JavaParser.StatementExpressionContext.class) ||
			   node.getClass().equals(JavaParser.LocalVariableDeclarationContext.class) || 
			   node.getClass().equals(JavaParser.FieldDeclarationContext.class);
	}
	
	@Override
	public boolean isReturnStatement(ParseTree node) {
		return (node.getClass().equals(JavaParser.StatementContext.class) && node.getChild(0).getText().equals("return"));
	}
	
	@Override
	public boolean isConditionalExpression(ParseTree node) {
		return node.getClass().equals(JavaParser.ParExpressionContext.class);
	}

	@Override
	public boolean isMethodDeclaration(ParseTree node) {
		return node.getClass().equals(JavaParser.MethodDeclarationContext.class);
	}
	
	@Override
	public boolean isClassDeclaration(ParseTree node) {
		return node.getClass().equals(JavaParser.ClassDeclarationContext.class);
	}
	
	@Override
	public boolean isTypeDeclaration(ParseTree node) {
		return node.getClass().equals(JavaParser.TypeDeclarationContext.class);
	}
	
	@Override
	public boolean isFieldDeclaration(ParseTree node) {
		return node.getClass().equals(JavaParser.FieldDeclarationContext.class);
	}
	
	@Override 
	public boolean isToken(ParseTree node) {
		Pattern pattern = Pattern.compile(this.validVarNameRegex);
        Matcher matcher = pattern.matcher(node.getText());
        return (!isRestrictedKeyword(node) && matcher.matches() &&
        		!(node.getParent().getClass().equals(JavaParser.ClassOrInterfaceTypeContext.class)) &&
        		!(node.getParent().getClass().equals(JavaParser.PrimitiveTypeContext.class)));
	}
	
	@Override
	public boolean isFormalParameter(ParseTree node) {
        return node.getClass().equals(JavaParser.FormalParameterContext.class);
	}

	@Override
	protected String getMethodDeclarationName(ParseTree node) {
		return node.getChild(1).getText();
	}

	@Override
	protected ParseTree getMethodDeclarationParams(ParseTree node) {
		return node.getChild(2).getChild(1);
	}

	@Override
	public MethodCallTraversal getMethodCallTraversal(ParseTree node) {
		return new JavaMethodCallTraversal(node.getParent());
	}

	@Override
	public boolean isTerminal(ParseTree node) {
		return node.getClass().equals(TerminalNodeImpl.class);
	}

	@Override
	public boolean isLocalVariableDeclaration(ParseTree node) {
		return node.getClass().equals(JavaParser.LocalVariableDeclarationContext.class);
	}

	@Override
	public boolean isAssignmentExpression(ParseTree node) {
		if (node.getClass().equals(JavaParser.StatementExpressionContext.class)) {
			ParseTree child = node.getChild(0);
			if (child.getChildCount() > 1) {
				child = child.getChild(1);
				if (child.getText().equals("=")) {
					return true;
				}
			}
		}
		return false;
	}

    @Override
    public boolean isPartOfExpression(ParseTree node) {
        return node.getClass().equals(JavaParser.ExpressionContext.class);
    }
}
