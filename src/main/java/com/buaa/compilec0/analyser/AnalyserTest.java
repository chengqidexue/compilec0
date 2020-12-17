package com.buaa.compilec0.analyser;

import com.buaa.compilec0.assembler.BinaryCode;
import com.buaa.compilec0.tokenizer.StringIter;
import com.buaa.compilec0.tokenizer.Tokenizer;

import java.io.File;
import java.util.Scanner;

public class AnalyserTest {
    public static void main(String[] args) throws Exception{
        File input = new File("input.c0");
        Scanner scanner;
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer = tokenize(iter);
        var analyser = new Analyser(tokenizer);
        var assembler =  analyser.analyse();
        System.out.println(assembler);
        File output = new File("output.o0");
        BinaryCode binaryCode = new BinaryCode(assembler);
        binaryCode.writeToOutput(output);
    }

    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}

//    /**
//     * <程序> ::= 'begin'<主过程>'end'
//     */
//    private void analyseProgram() throws CompileError {
//        // 示例函数，示例如何调用子程序
//        // 'begin'
//        expect(TokenType.Begin);
//
//        analyseMain();
////        System.out.println("================开始进行END、EOF分析==================");
//        // 'end'
//        expect(TokenType.End);
//        expect(TokenType.EOF);
//    }
//
//    /**
//     * <主过程> ::= <常量声明><变量声明><语句序列>
//     * @throws CompileError
//     */
//    private void analyseMain() throws CompileError {
//        //解析常量声明语句
//        analyseConstantDeclaration();
//
//        //解析变量声明语句
//        analyseVariableDeclaration();
//
//        //解析语句序列
//        analyseStatementSequence();
//
//    }
//
//    /**
//     * 分析常量声明语句
//     * <常量声明> ::= {<常量声明语句>}
//     * <常量声明语句> ::= 'const'<标识符>'='<常表达式>';'
//     * <常表达式> ::= [<符号>]<无符号整数>
//     * @throws CompileError
//     */
//    private void analyseConstantDeclaration() throws CompileError {
////        System.out.println("================开始进行常量声明语句分析==================");
//        // 示例函数，示例如何解析常量声明
//        // 如果下一个 token 是 const 就继续
//        while (nextIf(TokenType.Const) != null) {
//            // 变量名
//            var nameToken = expect(TokenType.Ident);
//
//            // 等于号
//            expect(TokenType.Equal);
//
//            // 常表达式
//            var x = analyseConstantExpression();
//
//            // 分号
//            expect(TokenType.Semicolon);
//
//            //加入符号表
//            addSymbol(nameToken.getValueString(), true, true, nameToken.getStartPos());
//
//            //加入instructions
//            //生成一次LIT指令加载常量
//            instructions.add(new Instruction(Operation.LIT, x));
//        }
//    }
//
//    /**
//     * 分析变量声明语句
//     * <变量声明> ::= {<变量声明语句>}
//     * <变量声明语句> ::= 'var'<标识符>['='<表达式>]';'
//     * @throws CompileError
//     */
//    private void analyseVariableDeclaration() throws CompileError {
////        System.out.println("================开始进行变量声明语句分析==================");
//        //如果下一个token是var就继续
//        while(nextIf(TokenType.Var) != null) {
//            //变量名
//            var nameToken = expect(TokenType.Ident);
//
//            //是否已经声明
//            if (isDeclare(nameToken.getValueString())) {
//                throw new AnalyzeError(ErrorCode.DuplicateDeclaration, nameToken.getStartPos());
//            }
//
//            //初值为0
//            instructions.add(new Instruction(Operation.LIT, 0));
//
//            //var a = 1;
//            if (nextIf(TokenType.Equal) != null) {
//                //表达式
//                analyseExpression();
//
//                //分号
//                expect(TokenType.Semicolon);
//
//                //加入符号表
//                addSymbol(nameToken.getValueString(), true, false, nameToken.getStartPos());
//
//                //加入instructions
//                int index = getOffset(nameToken.getValueString(), nameToken.getStartPos());
//                instructions.add(new Instruction(Operation.STO, index));
//            }
//
//            //var a;
//            else if (nextIf(TokenType.Semicolon) != null) {
//                //加入符号表
//                addSymbol(nameToken.getValueString(), false, false, nameToken.getStartPos());
//            }
//
//            else {
//                throw new Error("Not implemented");
//            }
//        }
//    }
//
//    /**
//     * 分析语句序列
//     * <语句序列> ::= {<语句>}
//     * <语句> ::= <赋值语句>|<输出语句>|<空语句>
//     * <赋值语句> ::= <标识符>'='<表达式>';'
//     * <输出语句> ::= 'print' '(' <表达式> ')' ';'
//     * <空语句> ::= ';'
//     * @throws CompileError
//     */
//    private void analyseStatementSequence() throws CompileError {
////        System.out.println("================开始进行语句序列分析==================");
//        //分析语句序列
//        while((check(TokenType.Ident) || check(TokenType.Print) || check(TokenType.Semicolon)))  {
//            analyseStatement();
//        }
//    }
//
//    /**
//     * <语句> ::= <赋值语句>|<输出语句>|<空语句>
//     * <赋值语句> ::= <标识符>'='<表达式>';'
//     * <输出语句> ::= 'print' '(' <表达式> ')' ';'
//     * <空语句> ::= ';'
//     * @throws CompileError
//     */
//    private void analyseStatement() throws CompileError {
//        //赋值语句
//        if (check(TokenType.Ident)) {
//            analyseAssignmentStatement();
//        }
//        //输出语句
//        else if (check(TokenType.Print)) {
//            analyseOutputStatement();
//        }
//        //空语句
//        else if (check(TokenType.Semicolon)) {
//            expect(TokenType.Semicolon);
//        }
//    }
//
//    /**
//     * <常表达式> ::= [<符号>]<无符号整数>
//     * <符号> ::= '+'|'-'
//     * @return x 返回常表达式的结果，有可能是负数 ,返回的是long，但是其实应该是int
//     * @throws CompileError
//     */
//    private Integer analyseConstantExpression() throws CompileError {
////        throw new Error("Not implemented");
//        long x = 0;
//
//        //负数'-'
//        if (nextIf(TokenType.Minus) != null) {
//            var uIntToken = next();
//            if (uIntToken.getTokenType() != TokenType.Uint) {
////                throw new Error("Not implemented");
//                //下一个应该是无符号整数
//                throw new ExpectedTokenError(TokenType.Uint, uIntToken);
//            }
//            else {
//                x = (long) uIntToken.getValue();
//                x = -x;
//            }
//
//            if (x > 2147483647 || x < -2147483648) {
//                throw new AnalyzeError(ErrorCode.IntegerOverflow, uIntToken.getStartPos());
//            }
//        }
//
//        //正数'+'
//        else if (nextIf(TokenType.Plus) != null) {
//            var uIntToken = next();
//            if (uIntToken.getTokenType() != TokenType.Uint) {
//                throw new ExpectedTokenError(TokenType.Uint, uIntToken);
//            }
//            else {
//                x = (long) uIntToken.getValue();
//            }
//            if (x > 2147483647 || x < -2147483648) {
//                throw new AnalyzeError(ErrorCode.IntegerOverflow, uIntToken.getStartPos());
//            }
//        }
//
//        //无符号
//        else if (check(TokenType.Uint)) {
//            var uIntToken = next();
//            x = (long) uIntToken.getValue();
//            if (x > 2147483647 || x < -2147483648) {
//                throw new AnalyzeError(ErrorCode.IntegerOverflow, uIntToken.getStartPos());
//            }
//        }
//
//        else {
//            throw new Error("Not implemented");
//        }
//
//        return Integer.valueOf(String.valueOf(x));
//    }
//
//    /**
//     * 分析赋值语句
//     * <赋值语句> ::= <标识符>'='<表达式>';'
//     * @throws CompileError
//     */
//    private void analyseAssignmentStatement() throws CompileError {
////        System.out.println("================开始进行赋值语句分析==================");
////        throw new Error("Not implemented");
//        //变量是否声明？
//        //变量是否是常量？
//        //是否需要生成instruction
//
//        //获取标识符
//        var nameToken = expect(TokenType.Ident);
//
//        //变量是否声明
//        if (!isDeclare(nameToken.getValueString())) {
//            throw new AnalyzeError(ErrorCode.NotDeclared, nameToken.getStartPos());
//        }
//
//        //变量是否是常量？给常量赋值
//        if (isConstant(nameToken.getValueString(), nameToken.getStartPos())) {
//            throw new AnalyzeError(ErrorCode.AssignToConstant, nameToken.getStartPos());
//        }
//
//        //'='
//        expect(TokenType.Equal);
//
//        //表达式
//        analyseExpression();
//
//        //';'
//        expect(TokenType.Semicolon);
//
//        //加入instruction
//        var index = getOffset(nameToken.getValueString(), nameToken.getStartPos());
//        instructions.add(new Instruction(Operation.STO, index));
//
//        //设置init
//        declareSymbol(nameToken.getValueString(), nameToken.getStartPos());
//    }
//
//    /**
//     * 分析输出语句
//     * <输出语句> ::= 'print' '(' <表达式> ')' ';'
//     * @throws CompileError
//     */
//    private void analyseOutputStatement() throws CompileError {
////        System.out.println("================开始进行输出语句分析==================");
//        expect(TokenType.Print);
//        expect(TokenType.LParen);
//        analyseExpression();
//        expect(TokenType.RParen);
//        expect(TokenType.Semicolon);
//        instructions.add(new Instruction(Operation.WRT));
//    }
//
//    /**
//     * 分析表达式
//     * <表达式> ::= <项>{<加法型运算符><项>}
//     * <加法型运算符> ::= '+'|'-'
//     * @throws CompileError
//     */
//    private void analyseExpression() throws CompileError {
////        System.out.println("================开始进行表达式分析==================");
////        throw new Error("Not implemented");
//        analyseItem();
//        while ((check(TokenType.Plus)) || (check(TokenType.Minus))) {
//            var nameToken = next();
//            analyseItem();
//            //生成instructions
//            if (nameToken.getTokenType() == TokenType.Plus) {
//                instructions.add(new Instruction(Operation.ADD));
//            } else if (nameToken.getTokenType() == TokenType.Minus) {
//                instructions.add(new Instruction(Operation.SUB));
//            }
//        }
//    }
//
//
//    /**
//     * 分析项
//     * <项> ::= <因子>{<乘法型运算符><因子>}
//     * <乘法型运算符> ::= '*'|'/'
//     * @throws CompileError
//     */
//    private void analyseItem() throws CompileError {
////        System.out.println("================开始进行项分析==================");
////        throw new Error("Not implemented");
//        analyseFactor();
//        while (check(TokenType.Mult) || check(TokenType.Div)) {
//            var nameToken = next();
//            analyseFactor();
//            //生成instructions
//            if (nameToken.getTokenType() == TokenType.Mult) {
//                instructions.add(new Instruction(Operation.MUL));
//            } else if (nameToken.getTokenType() == TokenType.Div) {
//                instructions.add(new Instruction(Operation.DIV));
//            }
//        }
//    }
//
//    /**
//     * 分析因子
//     * <因子> ::= [<符号>]( <标识符> | <无符号整数> | '('<表达式>')' )
//     * @throws CompileError
//     */
//    private void analyseFactor() throws CompileError {
////        System.out.println("================开始进行因子分析==================");
//        boolean negate;
//        if (nextIf(TokenType.Minus) != null) {
//            negate = true;
//            // 计算结果需要被 0 减
//            instructions.add(new Instruction(Operation.LIT, 0));
//        } else {
//            nextIf(TokenType.Plus);
//            negate = false;
//        }
//
//        if (check(TokenType.Ident)) {
//            // 调用相应的处理函数
//            var nameToken = next();
//            //判断是否已经init
//            if (!(isInitialized(nameToken.getValueString()))) {
//                throw new AnalyzeError(ErrorCode.NotInitialized, nameToken.getStartPos());
//            }
//            var index = getOffset(nameToken.getValueString(), nameToken.getStartPos());
//            instructions.add(new Instruction(Operation.LOD, index));
//        }
//        else if (check(TokenType.Uint)) {
////            System.out.println("================无符号整数==================");
//            // 调用相应的处理函数
//            var nameToken = next();
////            System.out.println(nameToken);
//            var x = Integer.valueOf(nameToken.getValue().toString());
//            instructions.add(new Instruction(Operation.LIT, x));
//        }
//        else if (check(TokenType.LParen)) {
//            expect(TokenType.LParen);
//            analyseExpression();
//            expect(TokenType.RParen);
//        } else {
//            // 都不是，摸了
//            throw new ExpectedTokenError(List.of(TokenType.Ident, TokenType.Uint, TokenType.LParen), next());
//        }
//
//        if (negate) {
//            instructions.add(new Instruction(Operation.SUB));
//        }
//    }

//        /**
//         * TODO:将所有的库函数加入符号表
//         */
//        Pos defaultPos = new Pos(0, 0);
//        String defaultParamName = "defaultName";
//        /**
//         * 读入一个有符号整数
//         * GETINT,
//         */
//        symbolTable.addFunctionSymbol(DataType.VOID, "getint", level, getNextGlobalOffset(), defaultPos);
//        symbolTable.setFunctionSymbolReturnType("getint", DataType.INT, defaultPos);
//        /**
//         * 读入一个浮点数
//         * GETDOUBLE,
//         */
//        symbolTable.addFunctionSymbol(DataType.VOID, "getdouble", level, getNextGlobalOffset(), defaultPos);
//        symbolTable.setFunctionSymbolReturnType("getdouble", DataType.DOUBLE, defaultPos);
//        /**
//         * 读入一个字符
//         * GETCHAR,
//         */
//        symbolTable.addFunctionSymbol(DataType.VOID, "getchar", level, getNextGlobalOffset(), defaultPos);
//        symbolTable.setFunctionSymbolReturnType("getchar", DataType.CHAR, defaultPos);
//        /**
//         * 输出一个整数
//         * PUTINT,
//         */
//        symbolTable.addFunctionSymbol(DataType.VOID, "putint", level, getNextGlobalOffset(), defaultPos);
//        symbolTable.setFunctionSymbolReturnType("putint", DataType.VOID, defaultPos);
//        symbolTable.addFunctionParamSymbol("putint", DataType.INT, defaultParamName, level, getNextOffset(), defaultPos);
//        /**
//         * 输出一个浮点数
//         * PUTDOUBLE,
//         */
//        symbolTable.addFunctionSymbol(DataType.VOID, "putdouble", level, getNextGlobalOffset(), defaultPos);
//        symbolTable.setFunctionSymbolReturnType("putdouble", DataType.VOID, defaultPos);
//        symbolTable.addFunctionParamSymbol("putdouble", DataType.DOUBLE, defaultParamName, level, getNextOffset(), defaultPos);
//        /**
//         * 输出一个字符
//         * PUTCHAR,
//         */
//        symbolTable.addFunctionSymbol(DataType.VOID, "putchar", level, getNextGlobalOffset(), defaultPos);
//        symbolTable.setFunctionSymbolReturnType("putchar", DataType.VOID, defaultPos);
//        symbolTable.addFunctionParamSymbol("putchar", DataType.INT, defaultParamName, level, getNextOffset(), defaultPos);
//
//        /**
//         * 将编号为这个整数的全局常量看作字符串输出
//         *  PUTSTR,
//         */
//        symbolTable.addFunctionSymbol(DataType.VOID, "putstr", level, getNextGlobalOffset(), defaultPos);
//        symbolTable.setFunctionSymbolReturnType("putstr", DataType.VOID, defaultPos);
//        symbolTable.addFunctionParamSymbol("putstr", DataType.INT, defaultParamName, level, getNextOffset(), defaultPos);
//
//        /**
//         * 输出一个换行
//         * PUTLN,
//         */
//        symbolTable.addFunctionSymbol(DataType.VOID, "putln", level, getNextGlobalOffset(), defaultPos);
//        symbolTable.setFunctionSymbolReturnType("putln", DataType.VOID, defaultPos);