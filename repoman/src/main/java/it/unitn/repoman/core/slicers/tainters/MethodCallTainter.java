package it.unitn.repoman.core.slicers.tainters;

import it.unitn.repoman.core.lang.traversals.generic.MethodCallTraversal;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.*;

public class MethodCallTainter extends AbstractTainter {

    public MethodCallTainter(Set<ParserRuleContext> collectables, Set<ParserRuleContext> seeds) {
        super(collectables, seeds);
    }

    @Override
    protected void initSeeds(Set<ParserRuleContext> seeds) {
        this.taintedStatements.addAll(seeds);
    }

    @Override
    public Set<ParserRuleContext> iterate(ParserRuleContext taintedStmt) {
        Set<ParserRuleContext> elementsToAdd = new LinkedHashSet<>();
        ParserRuleContext taintedScope = wrapper.getContainer(taintedStmt);

        for (ParserRuleContext collectable : collectables) {
            if (!taintedStatements.contains(collectable)) {
                if (wrapper.getContainer(collectable).equals(taintedScope)) {
                    MethodCallTraversal t = wrapper.getMethodCallTraversal(collectable);
                    MethodCallTraversal tt = wrapper.getMethodCallTraversal(taintedStmt);
                    if (t.getMethodName().equals(tt.getMethodName()) && t.getParamsNumber() == tt.getParamsNumber()) {
                        elementsToAdd.add(collectable);
                    }
                }
            }
        }

        return elementsToAdd;
    }
}
