// Currency.java
package com.djbc.dutyfree.domain.enums;

public enum Currency {
    XOF("Franc CFA", "FCFA"),
    EUR("Euro", "€"),
    USD("US Dollar", "$");

    private final String name;
    private final String symbol;

    Currency(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }
}