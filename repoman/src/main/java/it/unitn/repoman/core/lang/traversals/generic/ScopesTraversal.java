package it.unitn.repoman.core.lang.traversals.generic;

import java.util.Set;
import java.util.HashSet;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public class ScopesTraversal extends DFSTraversal {
	private Set<ParserRuleContext> fields = new HashSet<>();
	private Set<ParserRuleContext> methods = new HashSet<>();	
	private int currentLine = 0;
	private int totalLines = 0;
	private final Set<Integer> collectableLines = new HashSet<>();
	
	public ScopesTraversal(ParserRuleContext root) {
		traverse(root);
	}
	
	@Override
    public void onEnterNode(ParseTree node) {
		//collect all "collectable" lines
		if (wrapper.isCollectable(node)) {
			int line = ((ParserRuleContext)node).getStart().getLine();
			this.collectableLines.add(line);
		}
		if (wrapper.isRuleContext(node)) {
			int lookupLine = ((ParserRuleContext)node).getStart().getLine();
			if (currentLine != lookupLine) {
                currentLine = lookupLine;
                this.totalLines++;
			}
		}
		if (wrapper.isFieldDeclaration(node)) {
			fields.add((ParserRuleContext)node);
		}
        if (wrapper.isMethodDeclaration(node)) {
        	methods.add((ParserRuleContext)node);
        }
	}
    
	public Set<ParserRuleContext> getScopes() {
    	return this.methods;
    }    

	public Set<ParserRuleContext> getFields() {
    	return this.fields;
    }    
	
	public int getTotalLinesCount() {
		return this.totalLines;
	}
	
	public Set<Integer> getCollectableLines() {
		return this.collectableLines;
	}
}
