package com.buaa.compilec0.symbol;

import com.buaa.compilec0.util.Pos;

public class ConstantSymbol extends Symbol{
    public ConstantSymbol(SymbolType symbolType, DataType dataType, String symbolName, int level, int offset, Pos startPos) {
        super(symbolType, dataType, symbolName, level, offset, startPos);
    }
}
