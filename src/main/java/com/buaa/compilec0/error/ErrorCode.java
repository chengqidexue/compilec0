package com.buaa.compilec0.error;

public enum ErrorCode {
    /**
     * 词法分析错误
     */
    NoError,        // Should be only used internally.
    StreamError,
    EOF,
    InvalidDouble,
    InvalidInput,
    InvalidString,
    InvalidChar,
    InvalidIdentifier,
    IntegerOverflow, // int32_t overflow.
    NoBegin,
    NoEnd,
    /**
     * 语法分析错误
     */
    InvalidReturnType,
    InvalidBreak,
    InvalidContinue,
    NoMainFunction,
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
    NoSuchParam,
    InvalidAssignment,
    InvalidPrint,
    ExpectedToken,
    InvalidOperator,
    SymbolLevelNotExist,
    NotExistDataType,
    InvalidDataType,
    FunctionParamDataTypeNotMap,
    FunctionParamsNotSuit,
    InvalidDataChange

}
