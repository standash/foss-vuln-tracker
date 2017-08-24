package it.unitn.repoman.core.lang.traversals.generic;

import it.unitn.repoman.core.lang.LanguageFactory;
import it.unitn.repoman.core.lang.wrappers.generic.Wrapper;
import java.lang.UnsupportedOperationException;
import org.antlr.v4.runtime.tree.ParseTree;

public class DFSTraversal implements Traversal {
    protected boolean isTerminated = false;
    protected ParseTree lastNode = null; 
    protected Wrapper wrapper = LanguageFactory.getWrapper();
   
    @Override
    public void traverse(ParseTree root) {
    	if (root == null) {
    		return;
    	}
        for (int i=0; i < root.getChildCount(); i++) {
            if (isTerminated) {
                onTerminate();
                break;
            }
            lastNode = root.getChild(i);
            onEnterNode(lastNode);
            traverse(lastNode);
            onExitNode(lastNode);
        }
    }


    public void traverse() {
       throw new UnsupportedOperationException("ERROR: This method is not implemented!");
    }

    protected boolean isWithinContext(ParseTree node) {
    	return true;
    }

    @Override
    public void onEnterNode(ParseTree node) {
    }

    @Override
    public void onExitNode(ParseTree node) {
    }
    
    protected void onTerminate() {
    }

    protected void terminate() {
        this.isTerminated = true;
    }

    public ParseTree getLastNode() {
        return this.lastNode;
    }

}
