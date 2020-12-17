package com.buaa.compilec0.symbol;

import com.buaa.compilec0.util.Pos;

public class ParamSymbol extends Symbol{
    private boolean isConstant;     //这个参数是否是常量

    /**
     * @param symbolType    PARAM
     * @param dataType      数据类型 int double
     * @param symbolName    参数名称
     * @param level         层数 这里默认的是函数所在的层次：0,但是我们要把它放到别的层次里面去
     * @param offset        第几个参数, 默认从0开始数
     * @param startPos      开始位置
     * @param isConstant    是不是常量
     */
    public ParamSymbol(SymbolType symbolType, DataType dataType, String symbolName, int level, int offset, Pos startPos, boolean isConstant) {
        super(symbolType, dataType, symbolName, level+1, offset, startPos);
        this.isConstant = isConstant;
    }

    public boolean isConstant() {
        return isConstant;
    }
}