package it.unitn.repoman.core.lang.traversals.c;

import java.util.HashSet;
import java.util.Set;
import it.unitn.repoman.core.lang.traversals.generic.DFSTraversal;
import it.unitn.repoman.core.lang.traversals.generic.MethodCallTraversal;
import it.unitn.repoman.core.lang.parsers.c.CParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class CMethodCallTraversal extends MethodCallTraversal {

	public CMethodCallTraversal(ParseTree node) {
		super(node);
	}

	private class ParamsTraversal extends DFSTraversal {
		private Set<ParseTree> params = new HashSet<>();
		@Override
		public void onEnterNode(ParseTree node) {
			if (node instanceof CParser.PrimaryExpressionContext) {
				params.add(node);
			}
		}
	}
	
	@Override 
	public void onEnterNode(ParseTree node) {
		skip--;
		if (node.getParent() instanceof CParser.InitDeclaratorListContext) {
			if (node.getChildCount() == 1) {
				try {
					this.methodContext = node.getParent().getParent();
					this.callNameNode = methodContext.getChild(0);
					ParseTree declarator = node.getChild(0).getChild(0);
					ParseTree lbrace = declarator.getChild(0);
					this.paramsNode = declarator.getChild(1);
					ParseTree rbrace = declarator.getChild(2);
					if (lbrace.getText().equals("(")) {
    					if (paramsNode.getText().equals(")")) {
    						this.isMethodCall = true;
    						paramsNode = null;
    					}
    					else if (rbrace.getText().equals(")")) {
    						this.isMethodCall = true;
    					}

    				}	
					if (paramsNode != null) {
						this.params.add(paramsNode.getChild(0));
					}
					
				}
				catch (NullPointerException e) {
    				this.isMethodCall = false;
    			}
				return;
			}
			else {
				skip = 5;
			}
		}
		else if (node.getParent() instanceof CParser.ExpressionStatementContext 
												&& !(node instanceof TerminalNode)) {
			if (node.getChild(0).getChildCount() != 1) {
					skip = 7;
				}
				else {
					skip = 1;
				}
		}
        if (skip == 0) {
        	if (node instanceof CParser.PostfixExpressionContext)  {
        		if (node.getChild(0) instanceof CParser.PrimaryExpressionContext) {
        			skip = -1;
        		}
        		else {
        			try { 
        				this.methodContext = node;
        				this.callNameNode = node.getChild(0);
        				ParseTree lbrace = node.getChild(1);
        				this.paramsNode = node.getChild(2);
        				ParseTree rbrace = node.getChild(3);
        				if (lbrace.getText().equals("(")) {
        					if (paramsNode.getText().equals(")")) {
        						this.isMethodCall = true;
        						paramsNode = null;
        					}
        					else if (rbrace.getText().equals(")")) {
        						this.isMethodCall = true;
        					}

        				}
        			}
        			catch (NullPointerException e) {
        				this.isMethodCall = false;
        			}
        			if (this.paramsNode != null) {
        				ParamsTraversal pt = new ParamsTraversal();
        				pt.traverse(paramsNode);
        				this.params.addAll(pt.params);	
        			}
        		}
        	}
        	else {
        		skip++;
        	}
		}
	}
}
