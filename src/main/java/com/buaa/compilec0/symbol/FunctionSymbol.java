package com.buaa.compilec0.symbol;

import com.buaa.compilec0.error.AnalyzeError;
import com.buaa.compilec0.error.CompileError;
import com.buaa.compilec0.error.ErrorCode;
import com.buaa.compilec0.util.Pos;

import java.util.ArrayList;
import java.util.HashMap;

public class FunctionSymbol extends Symbol {
    //函数的参数列表
    //参数名:符号
    private DataType returnType;                        //返回类型
    private HashMap<String, ParamSymbol> paramsMap;     //参数的Map
    private ArrayList<ParamSymbol> params;              //参数的列表
    private int localSize = 0;                          //局部变量的数量

    /**
     *
     * @param symbolType    FUNCTION
     * @param dataType      没有数据类型VOID
     * @param symbolName    函数名
     * @param level         属于第0层：0
     * @param offset        globalOffset
     * @param startPos      开始的位置
     */
    public FunctionSymbol(SymbolType symbolType, DataType dataType, String symbolName, int level, int offset, Pos startPos) {
        super(symbolType, dataType, symbolName, level, offset, startPos);
        this.paramsMap = new HashMap<>();
        this.params = new ArrayList<>();
    }

    public DataType getReturnType() {
        return returnType;
    }

    public void setReturnType(DataType returnType) {
        this.returnType = returnType;
    }

    public int getLocalSize() {
        return localSize;
    }

    public void setLocalSize(int localSize) {
        this.localSize = localSize;
    }

    /**
     * 增加一个函数的参数
     * @param paramName paramName
     * @param paramSymbol 可能是const，也可能是variable
     * @throws CompileError
     */
    public void addParams(String paramName, ParamSymbol paramSymbol) throws CompileError {
        if (paramsMap.containsKey(paramName)) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, paramSymbol.getStartPos());
        }
        paramsMap.put(paramName, paramSymbol);
        params.add(paramSymbol);
    }

    /**
     * 根据索引返回参数的数据类型
     * @param paramIndex
     * @return
     */
    public DataType getParamDataTypeByIndex(int paramIndex) {
        return params.get(paramIndex).getDataType();
    }

    /**
     * 根据参数名称返回参数
     * @param paramName 参数名称
     * @return  参数
     */
    public ParamSymbol getParamByParamName(String paramName) {
        return paramsMap.get(paramName);
    }

    /**
     * 参数的长度
     * @return
     */
    public int getParamsSize() {
        return params.size();
    }
}