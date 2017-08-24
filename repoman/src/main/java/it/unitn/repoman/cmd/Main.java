package it.unitn.repoman.cmd;

import it.unitn.repoman.core.lang.LanguageFactory;
import it.unitn.repoman.core.slicers.LightweightSlice;
import it.unitn.repoman.core.utils.printers.ConsolePrinterListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.apache.commons.cli.*;

public class Main {
	
	public static void main(String[] args) {
        Options opts = new Options();

        Option fileOpt = Option.builder("f")
                .argName("e.g, ./Code.java")
                .longOpt("file")
                .required(true)
                .hasArg()
                .desc(".java file")
                .build();
        opts.addOption(fileOpt);

        Option lineNumbersOpt = Option.builder("l")
                .argName("e.g, '1 2 3 4 ...'")
                .longOpt("line-numbers")
                .desc("Seed lines (line numbers separated with spaces)")
                .required(true)
                .hasArgs()
                .build();
        opts.addOption(lineNumbersOpt);

        CommandLineParser cmdParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = cmdParser.parse(opts, args);
            String file = cmd.getOptionValue("f");

            LanguageFactory.init("Java", readFile(file));
            String[] argLines = cmd.getOptionValues("l");
            Set<Integer> lines = new LinkedHashSet<>();
            for (int i=0; i<argLines.length; i++) {
                lines.add(Integer.parseInt(argLines[i]));
            }
			Parser parser = LanguageFactory.getParser();
            LightweightSlice slice = new LightweightSlice(LanguageFactory.getRoot(), lines);
			ParseTreeListener printer = new ConsolePrinterListener(parser, slice);
			System.out.println(printer);
		}
        catch (ParseException e) {
            System.out.println("ERROR: " + e.getMessage());
            helpFormatter.printHelp("utility-name", opts);
        }
		catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}	
	}

	private static String readFile(String filename) throws IOException {
		File javaFile = new File(filename);
		if (!javaFile.exists())
			throw new FileNotFoundException(filename);

		BufferedReader in = new BufferedReader(new FileReader(javaFile));
		StringBuffer buffer = new StringBuffer();
		String line = null;
		try {
			while ( (line = in.readLine()) != null) {
				buffer.append(line);
				buffer.append('\n');
			}
		}
		finally {
			in.close();
		}
		return buffer.toString();
	}
}
