package it.unitn.repoman.core.slicers.tainters;

import it.unitn.repoman.core.lang.LanguageFactory;
import it.unitn.repoman.core.lang.wrappers.generic.Wrapper;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.*;

public abstract class AbstractTainter {

    protected final Wrapper wrapper = LanguageFactory.getWrapper();
    protected final Set<ParserRuleContext> collectables;
    protected final TaintedVariableSet taints = new TaintedVariableSet();
    protected final Set<ParserRuleContext> taintedStatements = new LinkedHashSet<>();

    public AbstractTainter(Set<ParserRuleContext> collectables, List<ParseTree> taintedVars) {
        this.collectables = collectables;
        Set<ParserRuleContext> seeds = new LinkedHashSet<>();

        for (ParseTree taintedVar : taintedVars) {
            ParserRuleContext taintedVarScope = wrapper.getContainer(taintedVar);
            taints.addTaintedVariable(taintedVarScope, taintedVar);

            for (ParserRuleContext collectable : collectables) {
                ParserRuleContext collectableScope = wrapper.getContainer(collectable);
                if (taintedVarScope.equals(collectableScope)) {
                    Set<ParseTree> referencedVars = wrapper.getReferencedVars(collectable);
                    for (ParseTree refVar : referencedVars) {
                        if (refVar.getText().equals(taintedVar.getText())) {
                            seeds.add(collectable);
                            taints.addTaintedVariable(collectableScope, refVar);
                        }
                    }
                }
            }
        }
        initSeeds(seeds);
        expand();
    }

    public AbstractTainter(Set<ParserRuleContext> collectables, Set<ParserRuleContext> seeds) {
        this.collectables = collectables;
        initSeeds(seeds);
        expand();
    }

    protected void expand() {
        Queue<ParserRuleContext> q = new LinkedList<>();
        q.addAll(taintedStatements);
        while (!q.isEmpty()) {
            ParserRuleContext stmt = q.poll();
            Set<ParserRuleContext> growths = iterate(stmt);
            taintedStatements.add(stmt);
            q.addAll(growths);
        }
    }

    public Set<Integer> getCollectedLines() {
        Set<Integer> lineNumbers = new LinkedHashSet<>();
        for (ParserRuleContext stmt : this.taintedStatements) {
            lineNumbers.add(stmt.getStart().getLine());
        }
        return lineNumbers;
    }

    public TaintedVariableSet getTaintedVars() {
        return this.taints;
    }

    protected abstract void initSeeds(Set<ParserRuleContext> seeds);
    public abstract Set<ParserRuleContext> iterate(ParserRuleContext taintedStmt);
}
