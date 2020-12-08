package com.buaa.compilec0.symbol;

public class Symbol {
    private SymbolType symbolType;
    private DataType dataType;
    private String symbolName;
    private int level;
    private int offset;

    public Symbol(SymbolType symbolType, DataType dataType, String symbolName, int level, int offset) {
        this.symbolType = symbolType;
        this.dataType = dataType;
        this.symbolName = symbolName;
        this.level = level;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "symbolType=" + symbolType +
                ", dataType=" + dataType +
                ", symbolName='" + symbolName + '\'' +
                ", level=" + level +
                ", offset=" + offset +
                '}';
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getSymbolName() {
        return symbolName;
    }

    public void setSymbolName(String symbolName) {
        this.symbolName = symbolName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
