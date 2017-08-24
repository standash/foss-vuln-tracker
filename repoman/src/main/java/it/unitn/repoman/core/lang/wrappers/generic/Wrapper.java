package it.unitn.repoman.core.lang.wrappers.generic;

import it.unitn.repoman.core.lang.traversals.generic.MethodCallTraversal;
import it.unitn.repoman.core.lang.traversals.generic.SymbolExtractTraversal;
import it.unitn.repoman.core.lang.traversals.generic.AssignmentSymbolExtractTraversal;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public abstract class Wrapper {
	
	protected String validVarNameRegex = "[A-Za-z_][A-Za-z_0-9]*$";
	
	protected String[] restrictedKeywords = {""};
	
	public abstract boolean isTerminal(ParseTree node);
	
	public abstract boolean isStatement(ParseTree node);

	public abstract boolean isLocalVariableDeclaration(ParseTree node);
	
	public abstract boolean isAssignmentExpression(ParseTree node);

    public abstract boolean isPartOfExpression(ParseTree node);

	public abstract boolean isReturnStatement(ParseTree node);
	
	public abstract boolean isConditionalExpression(ParseTree node);

	public abstract boolean isMethodDeclaration(ParseTree node);
	
	public abstract boolean isFieldDeclaration(ParseTree node);
	
	public abstract boolean isToken(ParseTree node);

	public abstract boolean isFormalParameter(ParseTree node);


    protected abstract String getMethodDeclarationName(ParseTree node);

    protected abstract ParseTree getMethodDeclarationParams(ParseTree node);
    
    public boolean isClassDeclaration(ParseTree node) {
		return false;
	}
	
	public boolean isTypeDeclaration(ParseTree node) {
		return false;
	}

	public boolean isCollectable(ParseTree node) {
		if (!(node instanceof ParserRuleContext)) {
			return false;
		}
		return (this.isStatement(node) || 
				this.isConditionalExpression(node) 	|| 
				this.isReturnStatement(node) ||
				this.isFieldDeclaration(node));
	}
	
	public boolean isRuleContext(ParseTree node) {
		return node instanceof ParserRuleContext;
	}
	
	protected boolean isRestrictedKeyword(ParseTree node) {
		for (int i = 0; i < this.restrictedKeywords.length; i++) {
			if (this.restrictedKeywords[i].equals(node.getText())) {
				return true;
			}
		}
		return false;
	}
	
	public ParseTree getDefinedVar(ParserRuleContext stmt) {
		ParseTree defVar = null;
		if (this.isLocalVariableDeclaration(stmt) || this.isAssignmentExpression(stmt) ||
				this.isFieldDeclaration(stmt)) {
			AssignmentSymbolExtractTraversal traversal = new AssignmentSymbolExtractTraversal(stmt);
			defVar = traversal.getLhs();
		}
		return defVar;
	}
	
	public Set<ParseTree> getReferencedVars(ParserRuleContext stmt) {
		Set<ParseTree> refVars = new LinkedHashSet<>();
		if (isLocalVariableDeclaration(stmt) || isAssignmentExpression(stmt) || isFieldDeclaration(stmt))  {
			AssignmentSymbolExtractTraversal traversal = new AssignmentSymbolExtractTraversal(stmt);
			for (ParseTree var : traversal.getRhsList()) {
				refVars.add(var);
			}
		}
		else if (isConditionalExpression(stmt) || isMethodCall(stmt) || isReturnStatement(stmt)) {
			SymbolExtractTraversal traversal = new SymbolExtractTraversal(stmt);
			for (ParseTree var : traversal.getSymbols()) {
				refVars.add(var);
			}
		}
		return refVars;
	}
	
    public ParserRuleContext getContainer(ParseTree ctx) {
        ParseTree c = ctx.getParent();
        while (!isTypeDeclaration(c)) {
            if (isMethodDeclaration(c)) {
                return (ParserRuleContext)c;
            }
            c = c.getParent();
        }
        return (ParserRuleContext)c;
    }

    public boolean precedes(ParserRuleContext stmt1, ParserRuleContext stmt2) {
    	if (stmt1 == null || stmt2 == null) {
    		return false;
    	}
    	return (stmt1.getStart().getLine() < stmt2.getStart().getLine());
    }
    

	public Collection<? extends ParseTree> getExternalSeeds(ParseTree method, ParseTree decl) {
		// TODO Auto-generated method stub
		final Set<ParseTree> seeds = new HashSet<>();
		return seeds;
	}

	public abstract MethodCallTraversal getMethodCallTraversal(ParseTree node);
	
	public boolean isMethodCall(ParseTree stmt) {
		MethodCallTraversal traversal = this.getMethodCallTraversal(stmt);
		return traversal.isMethodCall();
	}
}
