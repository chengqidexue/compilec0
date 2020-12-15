package com.buaa.compilec0.symbol;

import com.buaa.compilec0.error.AnalyzeError;
import com.buaa.compilec0.error.CompileError;
import com.buaa.compilec0.error.ErrorCode;
import com.buaa.compilec0.util.Pos;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    /**
     * 将每层的符号表组织成一个链表
     * level = 0 为主程序符号表，栈式符号表结构
     */
    public ArrayList<HashMap<String, Symbol>> symbolTables = new ArrayList<>();

    /**
     * 获得下一层符号表
     * 即入栈一个新的符号表
     */
    public void pushSymbolTable() {
        symbolTables.add(new HashMap<>());
    }

    /**
     * 弹出符号表
     * 即弹栈符号表
     */
    public void popSymbolTable() {
        symbolTables.remove(symbolTables.size()-1);
    }

    /**
     * 是否存在该符号
     * @param level 符号表的层数
     * @param symbolName 符号的名称
     * @param startPos 开始的位置
     * @return true 存在
     * @throws AnalyzeError
     */
    public boolean isExistSymbol(int level, String symbolName, Pos startPos) throws CompileError {
        if (level >= symbolTables.size()) {
            throw new AnalyzeError(ErrorCode.SymbolLevelNotExist, startPos);
        }
        return symbolTables.get(level).get(symbolName) != null;
    }


    /**
     * 添加一个变量符号
     * @param dataType  数据类型
     * @param symbolName 名称
     * @param level 层次
     * @param offset 栈上的偏移
     * @param startPos 开始的位置
     * @param initialized 是否初始化
     * @throws CompileError 编译错误
     */
    public void addVariableSymbol(DataType dataType, String symbolName, int level,
                                         int offset, Pos startPos, boolean initialized) throws CompileError {
        if (isExistSymbol(level, symbolName, startPos)) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, startPos);
        }
        else {
            symbolTables.get(level).put(symbolName, new VariableSymbol(SymbolType.VARIABLE, dataType, symbolName, level, offset, startPos, initialized));
        }
    }

    /**
     * 添加一个常量符号
     * @param dataType  数据类型
     * @param symbolName 名称
     * @param level 层次
     * @param offset 栈上的偏移
     * @param startPos 开始的位置
     * @throws CompileError 编译错误
     */
    public void addConstantSymbol(DataType dataType, String symbolName, int level,
                                         int offset, Pos startPos) throws CompileError {
        if (isExistSymbol(level, symbolName, startPos)) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, startPos);
        }
        else {
            symbolTables.get(level).put(symbolName, new ConstantSymbol(SymbolType.CONSTANT, dataType, symbolName, level, offset, startPos));
        }
    }

    /**
     * 添加一个函数符号
     * @param dataType 数据类型
     * @param symbolName 名称
     * @param level 层次
     * @param offset 栈上的偏移
     * @param startPos 开始的位置
     * @throws CompileError 编译错误
     */
    public void addFunctionSymbol(DataType dataType, String symbolName, int level,
                                         int offset, Pos startPos) throws CompileError {
        if (isExistSymbol(level, symbolName, startPos)) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, startPos);
        }
        else {
            symbolTables.get(level).put(symbolName, new FunctionSymbol(SymbolType.FUNCTION, dataType, symbolName, level, offset, startPos));
        }
    }

    public void addFunctionParamSymbol(DataType dataType, String symbolName, int level,
                                              int offset, Pos startPos, boolean isConstant) {
        //TODO
    }

}