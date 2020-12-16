package com.buaa.compilec0.assembler;

import com.buaa.compilec0.symbol.DataType;

import java.util.ArrayList;

public class Assembler {
    //全局变量区
    private ArrayList<Global> globals;

    //开始函数
    private Global start;
    private Function startFunction;

    //函数区
    private ArrayList<Function> functions;

    public Assembler() {
        globals = new ArrayList<>();
        functions = new ArrayList<>();
        start = new Global("_start", GlobalType.FUNCTION);
        startFunction = new Function(0, DataType.VOID, 0, 0);
    }

    //设置_start的functionOffset
    //这里默认的是最后一个
    public void setStartFunctionGlobalOffset(int globalOffset) {
        startFunction.setGlobalOffset(globalOffset);
    }
}