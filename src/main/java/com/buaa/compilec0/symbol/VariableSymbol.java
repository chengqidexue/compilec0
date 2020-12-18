package com.buaa.compilec0.symbol;

import com.buaa.compilec0.util.Pos;

public class VariableSymbol extends Symbol{
    boolean initialized;

    public VariableSymbol(SymbolType symbolType, DataType dataType, String symbolName, int level, long offset, Pos startPos, boolean initialized) {
        super(symbolType, dataType, symbolName, level, offset, startPos);
        this.initialized = initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}