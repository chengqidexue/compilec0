package com.buaa.compilec0.symbol;

import com.buaa.compilec0.util.Pos;

public class ParamSymbol extends Symbol{
    /**
     * const类型的参数
     */
    boolean isConstant;

    public ParamSymbol(SymbolType symbolType, DataType dataType, String symbolName, int level, int offset, Pos startPos, boolean isConstant) {
        super(symbolType, dataType, symbolName, level, offset, startPos);
        this.isConstant = isConstant;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public void setConstant(boolean constant) {
        isConstant = constant;
    }
}