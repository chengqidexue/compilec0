package com.buaa.compilec0.symbol;

import com.buaa.compilec0.util.Pos;

public class Symbol {
    private SymbolType symbolType;      //符号的类型，常量，变量，函数，参数
    private DataType dataType;          //数据类型，如果是函数的话，就是返回类型
    private String symbolName;          //符号的名称ident
    private int level;                  //符号所在的层次
    private int offset;                 //在栈上的偏移
    private Pos startPos;               //符号的起始位置

    public Symbol(SymbolType symbolType, DataType dataType, String symbolName, int level, int offset, Pos startPos) {
        this.symbolType = symbolType;
        this.dataType = dataType;
        this.symbolName = symbolName;
        this.level = level;
        this.offset = offset;
        this.startPos = startPos;
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "symbolType=" + symbolType +
                ", dataType=" + dataType +
                ", symbolName='" + symbolName + '\'' +
                ", level=" + level +
                ", offset=" + offset +
                ", startPos=" + startPos +
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

    public Pos getStartPos() {
        return startPos;
    }

    public void setStartPos(Pos startPos) {
        this.startPos = startPos;
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
