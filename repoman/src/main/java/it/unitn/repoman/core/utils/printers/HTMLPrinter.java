package it.unitn.repoman.core.utils.printers;

import java.util.Set;

public class HTMLPrinter {

    protected final StringBuilder builder = new StringBuilder();

    public HTMLPrinter(String fileContents, Set<Integer> selectedLines) {

    }

    @Override
    public String toString() {
        return builder.toString();
    }

}
