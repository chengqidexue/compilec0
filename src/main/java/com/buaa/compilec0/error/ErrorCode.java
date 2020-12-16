package com.buaa.compilec0.error;

public enum ErrorCode {
    /**
     * 词法分析错误
     */
    NoError,        // Should be only used internally.
    StreamError,
    EOF,
    InvalidInput,
    InvalidString,
    InvalidIdentifier,
    IntegerOverflow, // int32_t overflow.
    NoBegin,
    NoEnd,
    /**
     * 语法分析错误
     */
    NeedIdentifier,
    ConstantNeedValue,
    NoSemicolon,
    InvalidVariableDeclaration,
    IncompleteExpression,
    NotDeclared,
    FunctionNotDeclared,
    AssignToConstant,
    AssignToFunction,
    AssignToConstantParam,
    NoSuchLibFunction,
    DuplicateDeclaration,
    DuplicateWithTheParam,
    NotAFunction,
    NotInitialized,
    InvalidAssignment,
    InvalidPrint,
    ExpectedToken,
    InvalidOperator,
    SymbolLevelNotExist,
    NotExistDataType,
    InvalidDataType,
    FunctionParamDataTypeNotMap,
    FunctionParamsNotSuit

}
