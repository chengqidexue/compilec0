package com.buaa.compilec0.assembler;

import com.buaa.compilec0.symbol.DataType;

import java.util.ArrayList;

public class Assembler {
    //全局变量区
    public ArrayList<Global> globals;

    //开始函数
    public Global start;
    public Function startFunction;

    //函数区
    public ArrayList<Function> functions;

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

    //增加一个函数
    public void addFunction(Function function) {
        functions.add(function);
    }

    public void addGlobal(Global global) {
        globals.add(global);
    }

    @Override
    public String toString() {
        StringBuilder assembler = new StringBuilder();
        for (Global global : globals) {
            assembler.append(global.toString() + "\n\n");
        }
        assembler.append(start.toString() + "\n\n");

        assembler.append(startFunction.toString() + "\n\n");

        for (Function function : functions) {
            assembler.append(function.toString() + "\n\n");
        }

        return assembler.toString();
    }
}