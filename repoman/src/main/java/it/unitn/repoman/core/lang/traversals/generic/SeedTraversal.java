package it.unitn.repoman.core.lang.traversals.generic;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import it.unitn.repoman.core.exceptions.InvalidLineException;

public final class SeedTraversal extends DFSTraversal {
    private ParserRuleContext seedScope = null;
    private ParserRuleContext seedStmt = null;
    private int seedLine;

    public SeedTraversal(int line) {
        this.seedLine = line;
    }

    @Override
    public void onEnterNode(ParseTree node) {
    	if (wrapper.isMethodDeclaration(node)) {
    		this.seedScope = (ParserRuleContext)node;
    	}
    	if (wrapper.isCollectable(node)) {
    		ParserRuleContext nodeCtx = (ParserRuleContext)node;
    		if (this.seedStmt == null && nodeCtx.getStart().getLine() == this.seedLine) {
                this.seedStmt = (ParserRuleContext)node;
                terminate();
    		}
       }
    }

    public ParserRuleContext getSeedScope() throws InvalidLineException {
    	return this.seedScope;
    }
    
    public ParserRuleContext getSeedStmt() throws InvalidLineException {
    	return this.seedStmt;
    }
}

