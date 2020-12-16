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
     * level = 0 为主程序符号表，栈底，栈式符号表结构
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
     * 是否在同层符号表中存在该符号
     * @param level 符号表的层数
     * @param symbolName 符号的名称
     * @param startPos 开始的位置
     * @return true 存在
     * @throws AnalyzeError
     */
    public boolean isSymbolExistedInSameLevel(int level, String symbolName, Pos startPos) throws CompileError {
        if (level >= symbolTables.size()) {
            throw new AnalyzeError(ErrorCode.SymbolLevelNotExist, startPos);
        }
        return symbolTables.get(level).get(symbolName) != null;
    }

    /**
     * 查看在本层及父层中是否存在该符号
     * @param level 层数
     * @param symbolName 符号名称
     * @param startPos 开始位置
     * @return 返回值
     * @throws CompileError 编译错误
     */
    public boolean isSymbolExistedInAllLevel(int level, String symbolName, Pos startPos) throws CompileError {
        if (level >= symbolTables.size()) {
            throw new AnalyzeError(ErrorCode.SymbolLevelNotExist, startPos);
        }
        for (int i = level; i >= 0; --i) {
            if (symbolTables.get(i).containsKey(symbolName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 用符号名称找到存在的符号
     * @param level 当前的层数
     * @param symbolName 名称
     * @return Symbol 返回的符号
     * @throws CompileError
     */
    public Symbol findSymbolBySymbolName(int level, String symbolName, Pos startPos) throws CompileError {
        if (level >= symbolTables.size()) {
            throw new AnalyzeError(ErrorCode.SymbolLevelNotExist, startPos);
        }
        for (int i = level; i >= 0 ; i--) {
            var symbol = symbolTables.get(i).get(symbolName);
            if (symbol != null) {
                return symbol;
            }
        }
        return null;
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
        if (isSymbolExistedInSameLevel(level, symbolName, startPos)) {

            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, startPos);
        }
        else {
            symbolTables.get(level).put(symbolName, new VariableSymbol(SymbolType.VARIABLE, dataType, symbolName, level, offset, startPos, initialized));
        }
    }

    /**
     * 找到变量符号并给变量符号的initialized
     * @param level 层数
     * @param symbolName 名称
     * @param startPos 位置
     * @throws CompileError
     */
    public void setVariableInitialized(int level, String symbolName, Pos startPos) throws CompileError {
        if (level >= symbolTables.size()) {
            throw new AnalyzeError(ErrorCode.SymbolLevelNotExist, startPos);
        }
        if (findSymbolBySymbolName(level, symbolName, startPos) == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, startPos);
        }
        for (int i = 0; i <= level; i++) {
            var symbol = symbolTables.get(i).get(symbolName);
            if (symbol != null) {
                var _symbol = (VariableSymbol) symbol;
                _symbol.setInitialized(true);
            }
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
        if (isSymbolExistedInSameLevel(level, symbolName, startPos)) {
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
        if (isSymbolExistedInSameLevel(level, symbolName, startPos)) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, startPos);
        }
        else {
            symbolTables.get(level).put(symbolName, new FunctionSymbol(SymbolType.FUNCTION, dataType, symbolName, level, offset, startPos));
        }
    }

    /**
     * 给函数增加一个参数
     * @param functionName  函数名称
     * @param dataType 参数类型
     * @param symbolName 参数名称
     * @param level 所属的层数
     * @param offset 栈上的偏移
     * @param startPos 开始的位置
     * @throws CompileError
     */
    public void addFunctionParamSymbol(String functionName, DataType dataType, String symbolName, int level,
                                       int offset, Pos startPos) throws CompileError{
        var table = symbolTables.get(0);
        var functionSymbol = table.get(functionName);
        if (functionSymbol == null) {
            throw new AnalyzeError(ErrorCode.FunctionNotDeclared, startPos);
        }
        FunctionSymbol _functionSymbol;
        if (functionSymbol instanceof FunctionSymbol) {
            _functionSymbol = (FunctionSymbol) functionSymbol;
            _functionSymbol.addParams(symbolName, new ParamSymbol(SymbolType.PARAM, dataType, symbolName, level, offset, startPos));
        }
        else {
            throw new AnalyzeError(ErrorCode.NotAFunction, startPos);
        }
    }

    /**
     * 给函数设置返回值类型
     * @param functionName  函数名称
     * @param returnType 返回类型
     * @param startPos 开始位置
     * @throws CompileError 错误
     */
    public void setFunctionSymbolReturnType(String functionName, DataType returnType, Pos startPos) throws CompileError{
        var table = symbolTables.get(0);
        var functionSymbol = table.get(functionName);
        if (functionSymbol == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, startPos);
        }
        FunctionSymbol _functionSymbol;
        if (functionSymbol instanceof FunctionSymbol) {
            _functionSymbol = (FunctionSymbol) functionSymbol;
            _functionSymbol.setReturnType(returnType);
        }
        else {
            throw new AnalyzeError(ErrorCode.NotAFunction, startPos);
        }
    }


}