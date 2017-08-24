package it.unitn.repoman.core.utils.printers;

import java.util.Set;


import it.unitn.repoman.core.lang.LanguageFactory;
import it.unitn.repoman.core.slicers.LightweightSlice;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.*;

public class ConsolePrinterListener implements ParseTreeListener {
	private final Set<Integer> selectedLines;
	private final Parser parser;
    private final StringBuilder builder = new StringBuilder();
    private int indent = 0;
    private boolean marked = false;    
    private String MARK_START = "\u001B[31m";
    private String MARK_END = "\u001B[0m";
	    
    public ConsolePrinterListener(Parser parser, LightweightSlice slice) {
    	this.parser = parser;
    	this.selectedLines = slice.getSelectedLines();
        builder.append("Selected lines: " + slice.getSelectedLines() + "\n");
        builder.append("#Selected lines: " + slice.getSelectedLines().size() + "\n");
        builder.append("\n\n");
        ParseTreeWalker.DEFAULT.walk(this, LanguageFactory.getRoot());
    }
    
    @Override
    public void visitTerminal(TerminalNode node) {
        if (marked) {
            mark(builder);
        }

    	if (builder.length() > 0)
            builder.append(' ');
        
        String text = Trees.getNodeText(node, this.parser);
        if (text.equals("{")) {
        	indent++;     	
        	builder.append(Utils.escapeWhitespace(text, false));        	        	
            unmark(builder);
        	appendNewline();
        }
        else if (text.equals(";")) {
        	builder.append(Utils.escapeWhitespace(text, false));
            unmark(builder);
        	appendNewline();        	
        }
        else if (text.equals("}")) {
        	indent--;
        	appendNewline();
        	builder.append(" " + Utils.escapeWhitespace(text, false));
            unmark(builder);
        	appendNewline();
        }
        else {
            if (!text.contains("<EOF>")) {
                builder.append(Utils.escapeWhitespace(text, false));
            }
        }       	
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
    	builder.append(Utils.escapeWhitespace(Trees.getNodeText(node, this.parser), false));
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        marked = this.selectedLines.contains(ctx.getStart().getLine());
    }
    
    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
    }    
    
    private void appendNewline() {    	
    	builder.append("\n");  	
    	for (int i=0; i<indent; i++) {
    		builder.append("    ");    		
    	}   	
    }
    
    private void unmark(StringBuilder builder) {
		builder.append(MARK_END);
		marked = false;
    }
    
    private void mark(StringBuilder builder) {
    	builder.append(MARK_START);
		marked = true;
    }

    
    @Override
    public String toString() {
        return builder.toString();
    }
}