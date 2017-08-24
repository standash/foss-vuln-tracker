package it.unitn.repoman.core.lang;

import org.antlr.v4.runtime.*;

import it.unitn.repoman.core.lang.wrappers.c.CWrapper;
import it.unitn.repoman.core.lang.wrappers.java.JavaWrapper;
import it.unitn.repoman.core.lang.wrappers.generic.Wrapper;
import it.unitn.repoman.core.lang.parsers.c.CLexer;
import it.unitn.repoman.core.lang.parsers.c.CParser;
import it.unitn.repoman.core.lang.parsers.java.JavaLexer;
import it.unitn.repoman.core.lang.parsers.java.JavaParser;

public class LanguageFactory {
	
	private static Wrapper wrapper;
	private static Parser parser;
	private static ParserRuleContext root;

	private LanguageFactory() {}
	
	public static void init(String language, String contents) {
        try {
        	if (language.toLowerCase().equals("java")) {
        		wrapper = new JavaWrapper();
        		JavaLexer lexer = new JavaLexer(CharStreams.fromString(contents));
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                parser = new JavaParser(tokens);
                root = ((JavaParser)parser).compilationUnit();
            }
        	else if (language.toLowerCase().equals("c")) {
        		wrapper = new CWrapper();
        		CLexer lexer = new CLexer(CharStreams.fromString(contents));
        		CommonTokenStream tokens = new CommonTokenStream(lexer);
        		parser = new CParser(tokens);
        		root  = ((CParser)parser).compilationUnit();
        	}
        	/*
        	else if (language.toLowerCase().equals("javascript")) {
        		//TODO
        	}
        	*/
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public static Wrapper getWrapper() {
		return wrapper;
	}
	
	public static Parser getParser() {
		return parser;
	}
	
	public static ParserRuleContext getRoot() {
		return root;
	}
}
