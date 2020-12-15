package com.buaa.compilec0.symbol;

import com.buaa.compilec0.error.AnalyzeError;
import com.buaa.compilec0.error.CompileError;
import com.buaa.compilec0.error.ErrorCode;
import com.buaa.compilec0.tokenizer.Token;
import com.buaa.compilec0.util.Pos;

import javax.xml.stream.events.DTD;
import java.util.ArrayList;
import java.util.HashMap;

public class FunctionSymbol extends Symbol {
    //函数的参数列表
    //参数名:符号
    private DataType returnType = this.getDataType();
    private HashMap<String, ParamSymbol> paramsMap;

    public DataType getReturnType() {
        return returnType;
    }

    private ArrayList<ParamSymbol> params;

    public FunctionSymbol(SymbolType symbolType, DataType dataType, String symbolName, int level, int offset, Pos startPos) {
        super(symbolType, dataType, symbolName, level, offset, startPos);
        this.paramsMap = new HashMap<>();
        this.params = new ArrayList<>();
    }

    public HashMap<String, ParamSymbol> getParamsMap() {
        return paramsMap;
    }

    public void setParamsMap(HashMap<String, ParamSymbol> paramsMap) {
        this.paramsMap = paramsMap;
    }

    public ArrayList<ParamSymbol> getParams() {
        return params;
    }

    public void setParams(ArrayList<ParamSymbol> params) {
        this.params = params;
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
     * 判断参数是否是constant类型的
     * @param paramName
     * @return
     */
    public boolean isParamConstant(String paramName) {
        return paramsMap.get(paramName).isConstant();
    }

    /**
     * 参数的长度
     * @return
     */
    public int getParamsSize() {
        return params.size();
    }
}