package it.unitn.repoman.core.lang.wrappers.c;

import it.unitn.repoman.core.lang.traversals.c.CMethodCallTraversal;
import it.unitn.repoman.core.lang.traversals.generic.MethodCallTraversal;
import it.unitn.repoman.core.lang.wrappers.generic.Wrapper;
import it.unitn.repoman.core.lang.parsers.c.CParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;


public class CWrapper extends Wrapper {

	public CWrapper() {
		this.restrictedKeywords = new String[]{ "return", "true", "false"};
	}

	@Override
	public boolean isStatement(ParseTree node) {
		return node instanceof CParser.DeclarationContext ||
				node instanceof CParser.ExpressionStatementContext;
	}

	@Override
	public boolean isReturnStatement(ParseTree node) {
		return (node instanceof CParser.JumpStatementContext && node.getChild(0).getText().equals("return"));
	}

	@Override
	public boolean isConditionalExpression(ParseTree node) {
		return (node instanceof CParser.ExpressionContext && 
				node.getParent() instanceof CParser.SelectionStatementContext);
	}

	@Override
	public boolean isMethodDeclaration(ParseTree node) {
		return node instanceof CParser.FunctionDefinitionContext;
	}

	@Override
	public boolean isFieldDeclaration(ParseTree node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isToken(ParseTree node) {
		Pattern pattern = Pattern.compile(this.validVarNameRegex);
        Matcher matcher = pattern.matcher(node.getText());
        return (!isRestrictedKeyword(node) && matcher.matches() &&
        		node instanceof TerminalNodeImpl);
	}

	@Override
	public boolean isFormalParameter(ParseTree node) {
		return node instanceof CParser.DeclaratorContext && 
				node.getParent() instanceof CParser.ParameterDeclarationContext;
	}

	@Override
	protected String getMethodDeclarationName(ParseTree node) {
		return node.getChild(1).getChild(0).getChild(0).getText();
	}

	@Override
	protected ParseTree getMethodDeclarationParams(ParseTree node) {
		return node.getChild(1).getChild(0).getChild(2);
	}

	@Override
	public MethodCallTraversal getMethodCallTraversal(ParseTree node) {
		return new CMethodCallTraversal(node);
	}

	@Override
	public boolean isTerminal(ParseTree node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLocalVariableDeclaration(ParseTree node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAssignmentExpression(ParseTree node) {
		// TODO Auto-generated method stub
		return false;
	}

	// TODO: implement the method
    @Override
    public boolean isPartOfExpression(ParseTree node) {
        return false;
    }


}
