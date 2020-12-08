package com.buaa.compilec0.symbol;

public class VariableSymbol extends Symbol{
    boolean initialized;

    public VariableSymbol(SymbolType symbolType, DataType dataType, String symbolName, int level, int offset, boolean initialized) {
        super(symbolType, dataType, symbolName, level, offset);
        this.initialized = initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public String toString() {
        return "VariableSymbol{" +
                "initialized=" + initialized +
                '}';
    }
}
