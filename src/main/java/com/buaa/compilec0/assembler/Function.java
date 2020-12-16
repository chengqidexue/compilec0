package com.buaa.compilec0.assembler;

import com.buaa.compilec0.symbol.DataType;

import java.util.ArrayList;

public class Function {
    private int globalOffset;                           //函数对应的全局变量id
    private DataType returnType;                        //函数的返回类型
    private int paramSize;                              //函数的参数个数
    private int localVariableSize;                      //函数的局部变量的个数
    private ArrayList<Instruction> instructions;

    public Function(int globalOffset) {
        this.globalOffset = globalOffset;
    }

    public Function(int globalOffset, DataType returnType, int paramSize, int localVariableSize) {
        this.globalOffset = globalOffset;
        this.returnType = returnType;
        this.paramSize = paramSize;
        this.localVariableSize = localVariableSize;
        this.instructions = new ArrayList<>();
    }

    public int getGlobalOffset() {
        return globalOffset;
    }

    public void setGlobalOffset(int globalOffset) {
        this.globalOffset = globalOffset;
    }

    public DataType getReturnType() {
        return returnType;
    }

    public void setReturnType(DataType returnType) {
        this.returnType = returnType;
    }

    public int getParamSize() {
        return paramSize;
    }

    public void setParamSize(int paramSize) {
        this.paramSize = paramSize;
    }

    public int getLocalVariableSize() {
        return localVariableSize;
    }

    public void setLocalVariableSize(int localVariableSize) {
        this.localVariableSize = localVariableSize;
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(ArrayList<Instruction> instructions) {
        this.instructions = instructions;
    }

    @Override
    public String toString() {
        int retSlot = 0;
        if (returnType == DataType.DOUBLE || returnType == DataType.INT) {
            retSlot = 1;
        }
        StringBuilder instructionsString = new StringBuilder();
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            StringBuilder ins = new StringBuilder();
            ins.append("\n    " + i + ": " + instruction.toString());
            instructionsString.append(ins.toString());
        }
        return "fn [" + globalOffset + "] " + retSlot + " " + paramSize + "->" + localVariableSize + "{"
                + instructionsString.toString()
                +"}";
    }
}