package com.buaa.compilec0.analyser;

import com.buaa.compilec0.error.*;
import com.buaa.compilec0.instruction.Instruction;
import com.buaa.compilec0.instruction.Operation;
import com.buaa.compilec0.tokenizer.Token;
import com.buaa.compilec0.tokenizer.TokenType;
import com.buaa.compilec0.tokenizer.Tokenizer;
import com.buaa.compilec0.util.Pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    HashMap<String, SymbolEntry> symbolTable = new HashMap<>();

    /** 下一个变量的栈偏移 */
    int nextOffset = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        return instructions;
    }

    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     * 
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     * 
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     * 
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    /**
     * 获取下一个变量的栈偏移
     * 
     * @return
     */
    private int getNextVariableOffset() {
        return this.nextOffset++;
    }

    /**
     * 添加一个符号
     * 
     * @param name          名字
     * @param isInitialized 是否已赋值
     * @param isConstant    是否是常量
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addSymbol(String name, boolean isInitialized, boolean isConstant, Pos curPos) throws AnalyzeError {
        if (this.symbolTable.get(name) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        } else {
            this.symbolTable.put(name, new SymbolEntry(isConstant, isInitialized, getNextVariableOffset()));
        }
    }

    /**
     * 设置符号为已赋值
     * 
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    private void declareSymbol(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            entry.setInitialized(true);
        }
    }

    /**
     * 判断一个变量是否已经声明
     * @param name  变量名称
     * @return true 已经声明； false 未声明
     */
    private boolean isDeclare(String name) {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * 判断一个变量是否已经赋值
     * @param name  变量名称
     * @return
     */
    private boolean isInitialized(String name) {
        var entry = this.symbolTable.get(name);
        return entry.isInitialized;
    }


    /**
     * 获取变量在栈上的偏移
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 栈偏移
     * @throws AnalyzeError
     */
    private int getOffset(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.getStackOffset();
        }
    }

    /**
     * 获取变量是否是常量
     * 
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    private boolean isConstant(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }

    /**
     * <程序> ::= 'begin'<主过程>'end'
     */
    private void analyseProgram() throws CompileError {
        // 示例函数，示例如何调用子程序
        // 'begin'
        expect(TokenType.Begin);

        analyseMain();
//        System.out.println("================开始进行END、EOF分析==================");
        // 'end'
        expect(TokenType.End);
        expect(TokenType.EOF);
    }

    /**
     * <主过程> ::= <常量声明><变量声明><语句序列>
     * @throws CompileError
     */
    private void analyseMain() throws CompileError {
        //解析常量声明语句
        analyseConstantDeclaration();

        //解析变量声明语句
        analyseVariableDeclaration();

        //解析语句序列
        analyseStatementSequence();

    }

    /**
     * 分析常量声明语句
     * <常量声明> ::= {<常量声明语句>}
     * <常量声明语句> ::= 'const'<标识符>'='<常表达式>';'
     * <常表达式> ::= [<符号>]<无符号整数>
     * @throws CompileError
     */
    private void analyseConstantDeclaration() throws CompileError {
//        System.out.println("================开始进行常量声明语句分析==================");
        // 示例函数，示例如何解析常量声明
        // 如果下一个 token 是 const 就继续
        while (nextIf(TokenType.Const) != null) {
            // 变量名
            var nameToken = expect(TokenType.Ident);

            // 等于号
            expect(TokenType.Equal);

            // 常表达式
            var x = analyseConstantExpression();

            // 分号
            expect(TokenType.Semicolon);

            //加入符号表
            addSymbol(nameToken.getValueString(), true, true, nameToken.getStartPos());

            //加入instructions
            //生成一次LIT指令加载常量
            instructions.add(new Instruction(Operation.LIT, x));
        }
    }

    /**
     * 分析变量声明语句
     * <变量声明> ::= {<变量声明语句>}
     * <变量声明语句> ::= 'var'<标识符>['='<表达式>]';'
     * @throws CompileError
     */
    private void analyseVariableDeclaration() throws CompileError {
//        System.out.println("================开始进行变量声明语句分析==================");
        //如果下一个token是var就继续
        while(nextIf(TokenType.Var) != null) {
            //变量名
            var nameToken = expect(TokenType.Ident);

            //是否已经声明
            if (isDeclare(nameToken.getValueString())) {
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration, nameToken.getStartPos());
            }

            //初值为0
            instructions.add(new Instruction(Operation.LIT, 0));

            //var a = 1;
            if (nextIf(TokenType.Equal) != null) {
                //表达式
                analyseExpression();

                //分号
                expect(TokenType.Semicolon);

                //加入符号表
                addSymbol(nameToken.getValueString(), true, false, nameToken.getStartPos());

                //加入instructions
                int index = getOffset(nameToken.getValueString(), nameToken.getStartPos());
                instructions.add(new Instruction(Operation.STO, index));
            }

            //var a;
            else if (nextIf(TokenType.Semicolon) != null) {
                //加入符号表
                addSymbol(nameToken.getValueString(), false, false, nameToken.getStartPos());
            }

            else {
                throw new Error("Not implemented");
            }
        }
    }

    /**
     * 分析语句序列
     * <语句序列> ::= {<语句>}
     * <语句> ::= <赋值语句>|<输出语句>|<空语句>
     * <赋值语句> ::= <标识符>'='<表达式>';'
     * <输出语句> ::= 'print' '(' <表达式> ')' ';'
     * <空语句> ::= ';'
     * @throws CompileError
     */
    private void analyseStatementSequence() throws CompileError {
//        System.out.println("================开始进行语句序列分析==================");
        //分析语句序列
        while((check(TokenType.Ident) || check(TokenType.Print) || check(TokenType.Semicolon)))  {
            analyseStatement();
        }
    }

    /**
     * <语句> ::= <赋值语句>|<输出语句>|<空语句>
     * <赋值语句> ::= <标识符>'='<表达式>';'
     * <输出语句> ::= 'print' '(' <表达式> ')' ';'
     * <空语句> ::= ';'
     * @throws CompileError
     */
    private void analyseStatement() throws CompileError {
        //赋值语句
        if (check(TokenType.Ident)) {
            analyseAssignmentStatement();
        }
        //输出语句
        else if (check(TokenType.Print)) {
            analyseOutputStatement();
        }
        //空语句
        else if (check(TokenType.Semicolon)) {
            expect(TokenType.Semicolon);
        }
    }

    /**
     * <常表达式> ::= [<符号>]<无符号整数>
     * <符号> ::= '+'|'-'
     * @return x 返回常表达式的结果，有可能是负数 ,返回的是long，但是其实应该是int
     * @throws CompileError
     */
    private Integer analyseConstantExpression() throws CompileError {
//        throw new Error("Not implemented");
        long x = 0;

        //负数'-'
        if (nextIf(TokenType.Minus) != null) {
            var uIntToken = next();
            if (uIntToken.getTokenType() != TokenType.Uint) {
//                throw new Error("Not implemented");
                //下一个应该是无符号整数
                throw new ExpectedTokenError(TokenType.Uint, uIntToken);
            }
            else {
                x = (long) uIntToken.getValue();
                x = -x;
            }

            if (x > 2147483647 || x < -2147483648) {
                throw new AnalyzeError(ErrorCode.IntegerOverflow, uIntToken.getStartPos());
            }
        }

        //正数'+'
        else if (nextIf(TokenType.Plus) != null) {
            var uIntToken = next();
            if (uIntToken.getTokenType() != TokenType.Uint) {
                throw new ExpectedTokenError(TokenType.Uint, uIntToken);
            }
            else {
                x = (long) uIntToken.getValue();
            }
            if (x > 2147483647 || x < -2147483648) {
                throw new AnalyzeError(ErrorCode.IntegerOverflow, uIntToken.getStartPos());
            }
        }

        //无符号
        else if (check(TokenType.Uint)) {
            var uIntToken = next();
            x = (long) uIntToken.getValue();
            if (x > 2147483647 || x < -2147483648) {
                throw new AnalyzeError(ErrorCode.IntegerOverflow, uIntToken.getStartPos());
            }
        }

        else {
            throw new Error("Not implemented");
        }

        return Integer.valueOf(String.valueOf(x));
    }

    /**
     * 分析赋值语句
     * <赋值语句> ::= <标识符>'='<表达式>';'
     * @throws CompileError
     */
    private void analyseAssignmentStatement() throws CompileError {
//        System.out.println("================开始进行赋值语句分析==================");
//        throw new Error("Not implemented");
        //变量是否声明？
        //变量是否是常量？
        //是否需要生成instruction

        //获取标识符
        var nameToken = expect(TokenType.Ident);

        //变量是否声明
        if (!isDeclare(nameToken.getValueString())) {
            throw new AnalyzeError(ErrorCode.NotDeclared, nameToken.getStartPos());
        }

        //变量是否是常量？给常量赋值
        if (isConstant(nameToken.getValueString(), nameToken.getStartPos())) {
            throw new AnalyzeError(ErrorCode.AssignToConstant, nameToken.getStartPos());
        }

        //'='
        expect(TokenType.Equal);

        //表达式
        analyseExpression();

        //';'
        expect(TokenType.Semicolon);

        //加入instruction
        var index = getOffset(nameToken.getValueString(), nameToken.getStartPos());
        instructions.add(new Instruction(Operation.STO, index));

        //设置init
        declareSymbol(nameToken.getValueString(), nameToken.getStartPos());
    }

    /**
     * 分析输出语句
     * <输出语句> ::= 'print' '(' <表达式> ')' ';'
     * @throws CompileError
     */
    private void analyseOutputStatement() throws CompileError {
//        System.out.println("================开始进行输出语句分析==================");
        expect(TokenType.Print);
        expect(TokenType.LParen);
        analyseExpression();
        expect(TokenType.RParen);
        expect(TokenType.Semicolon);
        instructions.add(new Instruction(Operation.WRT));
    }

    /**
     * 分析表达式
     * <表达式> ::= <项>{<加法型运算符><项>}
     * <加法型运算符> ::= '+'|'-'
     * @throws CompileError
     */
    private void analyseExpression() throws CompileError {
//        System.out.println("================开始进行表达式分析==================");
//        throw new Error("Not implemented");
        analyseItem();
        while ((check(TokenType.Plus)) || (check(TokenType.Minus))) {
            var nameToken = next();
            analyseItem();
            //生成instructions
            if (nameToken.getTokenType() == TokenType.Plus) {
                instructions.add(new Instruction(Operation.ADD));
            } else if (nameToken.getTokenType() == TokenType.Minus) {
                instructions.add(new Instruction(Operation.SUB));
            }
        }
    }


    /**
     * 分析项
     * <项> ::= <因子>{<乘法型运算符><因子>}
     * <乘法型运算符> ::= '*'|'/'
     * @throws CompileError
     */
    private void analyseItem() throws CompileError {
//        System.out.println("================开始进行项分析==================");
//        throw new Error("Not implemented");
        analyseFactor();
        while (check(TokenType.Mult) || check(TokenType.Div)) {
            var nameToken = next();
            analyseFactor();
            //生成instructions
            if (nameToken.getTokenType() == TokenType.Mult) {
                instructions.add(new Instruction(Operation.MUL));
            } else if (nameToken.getTokenType() == TokenType.Div) {
                instructions.add(new Instruction(Operation.DIV));
            }
        }
    }

    /**
     * 分析因子
     * <因子> ::= [<符号>]( <标识符> | <无符号整数> | '('<表达式>')' )
     * @throws CompileError
     */
    private void analyseFactor() throws CompileError {
//        System.out.println("================开始进行因子分析==================");
        boolean negate;
        if (nextIf(TokenType.Minus) != null) {
            negate = true;
            // 计算结果需要被 0 减
            instructions.add(new Instruction(Operation.LIT, 0));
        } else {
            nextIf(TokenType.Plus);
            negate = false;
        }

        if (check(TokenType.Ident)) {
            // 调用相应的处理函数
            var nameToken = next();
            //判断是否已经init
            if (!(isInitialized(nameToken.getValueString()))) {
                throw new AnalyzeError(ErrorCode.NotInitialized, nameToken.getStartPos());
            }
            var index = getOffset(nameToken.getValueString(), nameToken.getStartPos());
            instructions.add(new Instruction(Operation.LOD, index));
        }
        else if (check(TokenType.Uint)) {
//            System.out.println("================无符号整数==================");
            // 调用相应的处理函数
            var nameToken = next();
//            System.out.println(nameToken);
            var x = Integer.valueOf(nameToken.getValue().toString());
            instructions.add(new Instruction(Operation.LIT, x));
        }
        else if (check(TokenType.LParen)) {
            expect(TokenType.LParen);
            analyseExpression();
            expect(TokenType.RParen);
        } else {
            // 都不是，摸了
            throw new ExpectedTokenError(List.of(TokenType.Ident, TokenType.Uint, TokenType.LParen), next());
        }

        if (negate) {
            instructions.add(new Instruction(Operation.SUB));
        }
    }
}