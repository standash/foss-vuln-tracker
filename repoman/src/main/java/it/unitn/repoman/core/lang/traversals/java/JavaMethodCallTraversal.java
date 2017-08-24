package it.unitn.repoman.core.lang.traversals.java;

import java.util.LinkedList;
import java.util.List;

import it.unitn.repoman.core.lang.traversals.generic.DotSymbolTraversal;
import it.unitn.repoman.core.lang.traversals.generic.MethodCallTraversal;
import it.unitn.repoman.core.lang.traversals.generic.DFSTraversal;
import it.unitn.repoman.core.lang.parsers.java.JavaParser;

import org.antlr.v4.runtime.tree.ParseTree;

public class JavaMethodCallTraversal extends MethodCallTraversal {

	public JavaMethodCallTraversal(ParseTree node) {
		super(node);
	}

	private class ParamsTraversal extends DFSTraversal {
		private List<ParseTree> params = new LinkedList<>();
		@Override
		public void onEnterNode(ParseTree node) {
			if (node.getClass().equals(JavaParser.ExpressionContext.class) &&
					node.getParent().getClass().equals(JavaParser.ExpressionListContext.class)) {
				SingleParamTraversal t = new SingleParamTraversal();
                t.traverse(node);
                if (t.paramName != null) {
                	params.add(t.paramName);
				}
			}

		}
	}

	private class SingleParamTraversal extends DFSTraversal {
		private ParseTree paramName = null;
		@Override
		public void onEnterNode(ParseTree node) {
			if (wrapper.isTerminal(node) && wrapper.isToken(node)) {
				paramName = node;
				terminate();
			}
		}
	}
	
	@Override
	public void onEnterNode(ParseTree node) {
		skip--;
		if (node.getClass().equals(JavaParser.LocalVariableDeclarationContext.class)) {
			skip = 10;
		}
		if (node.getClass().equals(JavaParser.StatementExpressionContext.class)) {
			if (node.getChild(0).getChild(1).getText().equals("(")) {
				skip = 1;
			}
			else {
				skip = 6;
			}
		}
		else if (node.getClass().equals(JavaParser.ExpressionContext.class)) {
            if (node.getChild(1) != null && node.getChild(1).getText().equals("(")) {
                skip = 0;
            }
            else {
                skip = 5;
            }
        }

		if (skip == 0) {
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
					} else if (rbrace.getText().equals(")")) {
						this.isMethodCall = true;
					}
				}

				if (paramsNode != null) {
					ParamsTraversal pt = new ParamsTraversal();
					pt.traverse(paramsNode);
					for (ParseTree param : pt.params) {
						if (wrapper.isToken(param)) {
							this.params.add(param);
						}
					}
				}
			}
			catch (NullPointerException e) {
				this.isMethodCall = false;
			}
			finally {
				terminate();
			}
		}
	}

	@Override
    public String getMethodName() {
        if (callNameNode != null) {
            DotSymbolTraversal t = new DotSymbolTraversal(callNameNode);
            return t.getRightmostNodeName();
        }
        return "";
    }
	
}
