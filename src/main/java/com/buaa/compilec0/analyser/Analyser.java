package com.buaa.compilec0.analyser;

import com.buaa.compilec0.error.*;
import com.buaa.compilec0.instruction.Instruction;
import com.buaa.compilec0.symbol.*;
import com.buaa.compilec0.tokenizer.Token;
import com.buaa.compilec0.tokenizer.TokenType;
import com.buaa.compilec0.tokenizer.Tokenizer;
import com.buaa.compilec0.util.Pos;

import java.util.ArrayList;
import java.util.List;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    private SymbolTable symbolTable = new SymbolTable();

    /**
     * 符号表的层次
     * 永远指向我现在所分析到的层次
     */
    private int level = 0;

    /**
     * 下一个变量的栈偏移
     */
    private int nextOffset = 0;

    /**
     * 所有的tokens
     */
    ArrayList<Token> allTokens;
    /**
     * 要获取的token的编号
     */
    private int tokenIndex = -1;

    /**
     * 当前偷看的 token
     */
    Token peekedToken = null;

    public Analyser(Tokenizer tokenizer) {
        this.allTokens = new ArrayList<>();
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        /**
         * 首先将所有的tokens存到list中
         */
        getAllTokens();
        /**
         * push主函数符号表
         */
        symbolTable.pushSymbolTable();
        /**
         * TODO:将所有的库函数加入符号表
         */
        Pos defaultPos = new Pos(0, 0);
        String defaultParamName = "defaultName";
        /**
         * 读入一个有符号整数
         * GETINT,
         */
        symbolTable.addFunctionSymbol(DataType.VOID, "getint", level, getNextOffset(), defaultPos);
        symbolTable.setFunctionSymbolReturnType("getint", DataType.INT, defaultPos);
        /**
         * 读入一个浮点数
         * GETDOUBLE,
         */
        symbolTable.addFunctionSymbol(DataType.VOID, "getdouble", level, getNextOffset(), defaultPos);
        symbolTable.setFunctionSymbolReturnType("getdouble", DataType.DOUBLE, defaultPos);
        /**
         * 读入一个字符
         * GETCHAR,
         */
        symbolTable.addFunctionSymbol(DataType.VOID, "getchar", level, getNextOffset(), defaultPos);
        symbolTable.setFunctionSymbolReturnType("getchar", DataType.CHAR, defaultPos);
        /**
         * 输出一个整数
         * PUTINT,
         */
        symbolTable.addFunctionSymbol(DataType.VOID, "putint", level, getNextOffset(), defaultPos);
        symbolTable.setFunctionSymbolReturnType("putint", DataType.VOID, defaultPos);
        symbolTable.addFunctionParamSymbol("putint", DataType.INT, defaultParamName, level, getNextOffset(), defaultPos);
        /**
         * 输出一个浮点数
         * PUTDOUBLE,
         */
        symbolTable.addFunctionSymbol(DataType.VOID, "putdouble", level, getNextOffset(), defaultPos);
        symbolTable.setFunctionSymbolReturnType("putdouble", DataType.VOID, defaultPos);
        symbolTable.addFunctionParamSymbol("putdouble", DataType.DOUBLE, defaultParamName, level, getNextOffset(), defaultPos);
        /**
         * 输出一个字符
         * PUTCHAR,
         */
        symbolTable.addFunctionSymbol(DataType.VOID, "putchar", level, getNextOffset(), defaultPos);
        symbolTable.setFunctionSymbolReturnType("putchar", DataType.VOID, defaultPos);
        symbolTable.addFunctionParamSymbol("putchar", DataType.INT, defaultParamName, level, getNextOffset(), defaultPos);

        /**
         * 将编号为这个整数的全局常量看作字符串输出
         *  PUTSTR,
         */
        symbolTable.addFunctionSymbol(DataType.VOID, "putstr", level, getNextOffset(), defaultPos);
        symbolTable.setFunctionSymbolReturnType("putstr", DataType.VOID, defaultPos);
        symbolTable.addFunctionParamSymbol("putstr", DataType.INT, defaultParamName, level, getNextOffset(), defaultPos);

        /**
         * 输出一个换行
         * PUTLN,
         */
        symbolTable.addFunctionSymbol(DataType.VOID, "putln", level, getNextOffset(), defaultPos);
        symbolTable.setFunctionSymbolReturnType("putln", DataType.VOID, defaultPos);

        analyseProgram();
        return instructions;
    }

    /**
     * 获取所有的token到tokens
     */
    public void getAllTokens() throws TokenizeError {
        while (true) {
            var token = tokenizer.nextToken();
            if (token.getTokenType().equals(TokenType.EOF)) {
                this.allTokens.add(token);
                break;
            }
            this.allTokens.add(token);
        }
    }

    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = allTokens.get(tokenIndex + 1);
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return 返回的是下一个token
     * @throws TokenizeError 不会抛出错误目前
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            this.tokenIndex++;
            return token;
        } else {
            return allTokens.get(++tokenIndex);
        }
    }

    /**
     * 将指针指向上一个token
     * 即：回退一步
     */
    private void back() throws TokenizeError {
        this.tokenIndex--;
        peekedToken = null;
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt 类型
     * @return true
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
     * 如果下一个 token 的类型是数据类型(int、void、double), 则前进一个 token 并返回，否则抛出异常
     * int double void
     *
     * @return 这个  token
     * @throws CompileError 类型不匹配
     */
    private Token expectType() throws CompileError {
        var token = peek();
        var tokenType = token.getTokenType();
        if (tokenType == TokenType.INT ||
                tokenType == TokenType.VOID ||
                tokenType == TokenType.DOUBLE) {
            return next();
        } else {
            ArrayList<TokenType> types = new ArrayList<>();
            types.add(TokenType.INT);
            types.add(TokenType.VOID);
            types.add(TokenType.DOUBLE);
            throw new ExpectedTokenError(types, token);
        }
    }

    /**
     * 把token的类型转化为数据类型
     *
     * @param token
     * @return
     * @throws CompileError
     */
    private DataType getDataTypeFromToken(Token token) throws CompileError {
        if (token.getTokenType() == TokenType.INT) {
            return DataType.INT;
        } else if (token.getTokenType() == TokenType.DOUBLE) {
            return DataType.DOUBLE;
        } else if (token.getTokenType() == TokenType.VOID) {
            return DataType.VOID;
        } else {
            throw new AnalyzeError(ErrorCode.NotExistDataType, token.getStartPos());
        }
    }

    /**
     * 获取下一个变量的栈偏移
     *
     * @return
     */
    private int getNextOffset() {
        return this.nextOffset++;
    }

    /**
     * program -> item*
     * item -> function | decl_stmt
     */
    private void analyseProgram() throws CompileError {
        //开始分析
        Token nextPeekToken = peek();
        while (true) {
            if (nextPeekToken.getTokenType() == TokenType.EOF) {
                break;
            } else if (nextPeekToken.getTokenType() == TokenType.FN_KW) {
                analyseFunction();
            } else if (nextPeekToken.getTokenType() == TokenType.LET_KW || nextPeekToken.getTokenType() == TokenType.CONST_KW) {
                analyseDeclareStatement();
            } else {
                throw new AnalyzeError(ErrorCode.InvalidInput, nextPeekToken.getStartPos());
            }
            nextPeekToken = peek();
        }
    }

    /**
     * function_param -> 'const'? IDENT ':' ty
     * function_param_list -> function_param (',' function_param)*
     * function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
     *
     * @throws CompileError
     */
    private void analyseFunction() throws CompileError {
        //fn
        expect(TokenType.FN_KW);
        //IDENT
        var ident = expect(TokenType.IDENT);
        symbolTable.addFunctionSymbol(DataType.VOID, ident.getValueString(), level, getNextOffset(), ident.getStartPos());

        //函数定义中是一层符号表
        ++level;
        symbolTable.pushSymbolTable();

        //(
        expect(TokenType.L_PAREN);
        var token = peek();
        if (token.getTokenType() != TokenType.R_PAREN) {
            //参数列表
            analyseFunctionParamList(ident.getValueString());
        }
        //)
        expect(TokenType.R_PAREN);
        //->
        expect(TokenType.ARROW);
        //ty
        var type = expectType();

        //设置函数的返回类型
        var dataType = getDataTypeFromToken(type);
        symbolTable.setFunctionSymbolReturnType(ident.getValueString(), dataType, type.getStartPos());

        //因为函数定义应该在同一层，所以我们这里不应该在加入一层了
        var temp = expect(TokenType.L_BRACE);
        var next = peek();
        while (next.getTokenType() != TokenType.R_BRACE) {
            analyseStatement();
            next = peek();
        }
        expect(TokenType.R_BRACE);
        --level;
        symbolTable.popSymbolTable();
    }

    /**
     * function_param_list -> function_param (',' function_param)*
     */
    private void analyseFunctionParamList(String functionName) throws CompileError {
        //参数
        analyseFunctionParam(functionName);
        var next = peek();
        while (next.getTokenType() == TokenType.COMMA) {
            expect(TokenType.COMMA);
            analyseFunctionParam(functionName);
            next = peek();
        }
    }

    /**
     * function_param -> 'const'? IDENT ':' ty
     *
     * @throws CompileError
     */
    private void analyseFunctionParam(String functionName) throws CompileError {
        var next = peek();
        if (next.getTokenType() == TokenType.CONST_KW) {
            //const
            expect(TokenType.CONST_KW);
            //IDENT
            var ident = expect(TokenType.IDENT);
            //:
            expect(TokenType.COLON);
            //ty
            var type = expectType();
            DataType dataType = getDataTypeFromToken(type);
            //既要添加到符号表中，又要添加到函数符号的参数列表中
            symbolTable.addConstantSymbol(dataType, ident.getValueString(), level, getNextOffset(), ident.getStartPos());
            symbolTable.addFunctionParamSymbol(functionName, dataType, ident.getValueString(), level, getNextOffset(), ident.getStartPos());

        } else if (next.getTokenType() == TokenType.IDENT) {
            //IDENT
            var ident = expect(TokenType.IDENT);
            //:
            expect(TokenType.COLON);
            //ty
            var type = expectType();
            DataType dataType = getDataTypeFromToken(type);

            symbolTable.addVariableSymbol(dataType, ident.getValueString(), level, getNextOffset(), ident.getStartPos(), true);
            symbolTable.addFunctionParamSymbol(functionName, dataType, ident.getValueString(), level, getNextOffset(), ident.getStartPos());
        }
    }

    /**
     * decl_stmt -> let_decl_stmt | const_decl_stmt
     *
     * @throws CompileError
     */
    private void analyseDeclareStatement() throws CompileError {
        Token nextPeekToken = peek();
        if (nextPeekToken.getTokenType() == TokenType.LET_KW) {
            analyseLetDeclareStatement();
        } else if (nextPeekToken.getTokenType() == TokenType.CONST_KW) {
            analyseConstDeclareStatement();
        } else {
            throw new AnalyzeError(ErrorCode.InvalidInput, nextPeekToken.getStartPos());
        }
    }

    /**
     * let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
     */
    private void analyseLetDeclareStatement() throws CompileError {
        //let
        expect(TokenType.LET_KW);
        //IDENT
        var ident = expect(TokenType.IDENT);
        //:
        expect(TokenType.COLON);
        //ty
        var type = expectType();
        var dataType = getDataTypeFromToken(type);

        var next = peek();
        if (next.getTokenType() == TokenType.ASSIGN) {
            expect(TokenType.ASSIGN);
            var tempDataType = analyseExpression();

            if (tempDataType != dataType) {
                throw new AnalyzeError(ErrorCode.InvalidDataType, ident.getStartPos());
            }
            //加入符号表
            symbolTable.addVariableSymbol(dataType, ident.getValueString(), level, getNextOffset(), ident.getStartPos(), true);
        } else {
            symbolTable.addVariableSymbol(dataType, ident.getValueString(), level, getNextOffset(), ident.getStartPos(), false);
        }
        //;
        expect(TokenType.SEMICOLON);
    }

    /**
     * const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
     */
    private void analyseConstDeclareStatement() throws CompileError {
        //const
        expect(TokenType.CONST_KW);
        //IDENT
        var ident = expect(TokenType.IDENT);
        //:
        expect(TokenType.COLON);
        //ty
        var type = expectType();
        var dataType = getDataTypeFromToken(type);

        //=
        expect(TokenType.ASSIGN);
        //expr
        analyseExpression();
        //;
        expect(TokenType.SEMICOLON);
        //加入符号表
        symbolTable.addConstantSymbol(dataType, ident.getValueString(), level, getNextOffset(), ident.getStartPos());
    }

    /**
     * block_stmt -> '{' stmt* '}'
     *
     * @throws CompileError
     */
    private void analyseBlockStatement() throws CompileError {
        ++level;
        symbolTable.pushSymbolTable();
        //{
        expect(TokenType.L_BRACE);
        var next = peek();
        while (next.getTokenType() != TokenType.R_BRACE) {
            analyseStatement();
            next = peek();
        }
        //}
        expect(TokenType.R_BRACE);
        --level;
        symbolTable.popSymbolTable();
    }

    /**
     * 语句
     * stmt ->
     * expr_stmt
     * | decl_stmt
     * | if_stmt
     * | while_stmt
     * | break_stmt
     * | continue_stmt
     * | return_stmt
     * | block_stmt
     * | empty_stmt
     *
     * @throws CompileError
     */
    private void analyseStatement() throws CompileError {
        var next = peek();
        var tokenType = next.getTokenType();
        //decl_stmt
        if (tokenType == TokenType.LET_KW) {
            analyseLetDeclareStatement();
        } else if (tokenType == TokenType.CONST_KW) {
            analyseConstDeclareStatement();
        }
        //if
        else if (tokenType == TokenType.IF_KW) {
            analyseIfStatement();
        }
        //while
        else if (tokenType == TokenType.WHILE_KW) {
            analyseWhileStatement();
        }
        //break
        else if (tokenType == TokenType.BREAK_KW) {
            analyseBreakStatement();
        }
        //continue
        else if (tokenType == TokenType.CONTINUE_KW) {
            analyseContinueStatement();
        }
        //return
        else if (tokenType == TokenType.RETURN_KW) {
            analyseReturnStatement();
        }
        //block
        else if (tokenType == TokenType.L_BRACE) {
            analyseBlockStatement();
        }
        //empty
        else if (tokenType == TokenType.SEMICOLON) {
            analyseEmptyStatement();
        }
        //expr_statement
        else {
            analyseExpressionStatement();
        }
    }

    /**
     * empty_stmt -> ';'
     */
    private void analyseEmptyStatement() throws CompileError {
        //;
        expect(TokenType.SEMICOLON);
    }

    /**
     * return_stmt -> 'return' expr? ';'
     *
     * @throws CompileError
     */
    private void analyseReturnStatement() throws CompileError {
        //return
        expect(TokenType.RETURN_KW);
        var next = peek();
        if (next.getTokenType() == TokenType.SEMICOLON) {
            //;
            expect(TokenType.SEMICOLON);
        } else {
            //expr
            analyseExpression();
            //;
            expect(TokenType.SEMICOLON);
        }
    }

    /**
     * continue_stmt -> 'continue' ';'
     *
     * @throws CompileError
     */
    private void analyseContinueStatement() throws CompileError {
        //continue
        expect(TokenType.CONTINUE_KW);
        //;
        expect(TokenType.SEMICOLON);
    }

    /**
     * break_stmt -> 'break' ';'
     *
     * @throws CompileError
     */
    private void analyseBreakStatement() throws CompileError {
        //break
        expect(TokenType.BREAK_KW);
        //;
        expect(TokenType.SEMICOLON);
    }

    /**
     * while_stmt -> 'while' expr block_stmt
     *
     * @throws CompileError
     */
    private void analyseWhileStatement() throws CompileError {
        //while
        expect(TokenType.WHILE_KW);
        //expr
        analyseExpression();
        //block
        analyseBlockStatement();
    }

    /**
     * if_stmt -> 'if' expr block_stmt ('else' 'if' expr block_stmt)* ('else' block_stmt)?
     *
     * @throws CompileError
     */
    private void analyseIfStatement() throws CompileError {
        //if
        expect(TokenType.IF_KW);
        //expr
        analyseExpression();
        //block
        analyseBlockStatement();
        var next = peek();
        //else if | else
        while (next.getTokenType() == TokenType.ELSE_KW) {
            expect(TokenType.ELSE_KW);
            next = peek();

            //else if
            if (next.getTokenType() == TokenType.IF_KW) {
                expect(TokenType.IF_KW);
                analyseBlockStatement();
                next = peek();
            }
            //else
            else if (next.getTokenType() == TokenType.L_BRACE) {
                analyseBlockStatement();
                break;
            }
        }
    }

    /**
     * expr_stmt -> expr ';'
     */
    private void analyseExpressionStatement() throws CompileError {
        //expr
        analyseExpression();
        //;
        expect(TokenType.SEMICOLON);
    }

    /**
     * // # 表达式
     * expr ->
     * operator_expr
     * | negate_expr
     * | assign_expr
     * | as_expr
     * | call_expr
     * | literal_expr
     * | ident_expr
     * | group_expr
     *
     * @return Datatype 表达式计算得到的数据类型
     * 为了修改左递归，我们将expression修改一下
     * 将赋值语句从Expression中移除
     * assign_expr -> IDENT '=' additive_expr
     * expr -> assign_expr
     * |additive_expr ('=='|'!='|'>'|'>='|'<'|'<=' additive_expr)?
     * @throws CompileError
     */
    private DataType analyseExpression() throws CompileError {
        DataType dataType = DataType.VOID;
        var token = peek();
        if (token.getTokenType() == TokenType.IDENT) {
            //赋值语句吗
            next();
            token = peek();
            back();
            if (token.getTokenType() == TokenType.ASSIGN) {
                analyseAssignStatement();
                dataType = DataType.VOID;
                return dataType;
            }
            //非赋值语句那就只能是加法表达式，因为赋值表达式和条件表达式都不能作为返回值
        }

        dataType = analyseAdditiveExpression();
        token = peek();
        if (token.getTokenType() == TokenType.EQ || token.getTokenType() == TokenType.NEQ
                || token.getTokenType() == TokenType.GT || token.getTokenType() == TokenType.GE
                || token.getTokenType() == TokenType.LT || token.getTokenType() == TokenType.LE) {
            var boolSymbol = next();
            var tempDataType = analyseAdditiveExpression();
            if (tempDataType != dataType) {
                throw new AnalyzeError(ErrorCode.InvalidDataType, token.getStartPos());
            }
            dataType = DataType.BOOL;
        }
        return dataType;
    }

    /**
     * assign_expr -> IDENT '=' additive_expr
     *
     * @throws CompileError
     */
    private void analyseAssignStatement() throws CompileError {
        //IDENT
        var ident = expect(TokenType.IDENT);
        var symbol = symbolTable.findSymbolBySymbolName(level, ident.getValueString(), ident.getStartPos());
        if (symbol == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, ident.getStartPos());
        }
        if (symbol instanceof ConstantSymbol) {
            throw new AnalyzeError(ErrorCode.AssignToConstant, ident.getStartPos());
        }
        if (symbol instanceof FunctionSymbol) {
            throw new AnalyzeError(ErrorCode.AssignToFunction, ident.getStartPos());
        }
        var dataType = symbol.getDataType();

        //=
        expect(TokenType.ASSIGN);
        //additive_expr
        var tempDataType = analyseAdditiveExpression();

        //赋值类型不匹配
        if (dataType != tempDataType) {
            throw new AnalyzeError(ErrorCode.InvalidDataType, ident.getStartPos());
        }

        //如果是普通变量，要将initialize赋值成true
        if (symbol instanceof VariableSymbol) {
            symbolTable.setVariableInitialized(level, ident.getValueString(), ident.getStartPos());
        }
    }

    /**
     * additive_expr -> mult_expr {'+'|'-' mult_expr}
     *
     * @throws CompileError
     */
    private DataType analyseAdditiveExpression() throws CompileError {
        DataType dataType = analyseMultExpression();
        while ((check(TokenType.PLUS)) || (check(TokenType.MINUS))) {
            var nameToken = next();
            var tempDataType = analyseMultExpression();

            if (tempDataType != dataType) {
                throw new AnalyzeError(ErrorCode.InvalidDataType, nameToken.getStartPos());
            }
        }
        return dataType;
    }

    /**
     * mult_expr -> as_expr {'*'|'/' as_expr}
     *
     * @throws CompileError
     */
    private DataType analyseMultExpression() throws CompileError {
        DataType dataType = analyseAsExpression();
        while (check(TokenType.MUL) || check(TokenType.DIV)) {
            var nameToken = next();
            var tempDataType = analyseAsExpression();

            if (tempDataType != dataType) {
                throw new AnalyzeError(ErrorCode.InvalidDataType, nameToken.getStartPos());
            }
        }
        return dataType;
    }

    /**
     * 类型转换表达式
     * as_expr -> single_expr {'as' ty}
     */
    private DataType analyseAsExpression() throws CompileError {
        var dataType = analyseSingleExpression();
        while (check(TokenType.AS_KW)) {
            //as
            expect(TokenType.AS_KW);
            //ty
            var type = expectType();
            var tempDataType = getDataTypeFromToken(type);
            dataType = tempDataType;
        }
        return dataType;
    }

    /**
     * 单目表达式
     * single_expr -> {'-'} primary_expr
     */
    private DataType analyseSingleExpression() throws CompileError {
        DataType dataType;
        var token = peek();
        if (token.getTokenType() == TokenType.MINUS) {
            expect(TokenType.MINUS);
            dataType = analyseSingleExpression();
        } else {
            dataType = analysePrimaryExpression();
        }
        return dataType;
    }

    /**
     * 最小表达式
     * primary_expr -> call_expr
     * | literal_expr
     * | ident_expr
     * | group_expr
     */
    private DataType analysePrimaryExpression() throws CompileError {
        DataType dataType;
        var token = peek();
        if (token.getTokenType() == TokenType.IDENT) {
            next();
            token = peek();
            back();
            if (token.getTokenType() == TokenType.L_PAREN) {
                dataType = analyseCallExpression();
            } else {
                dataType = analyseIdentExpression();
            }
        } else if (token.getTokenType() == TokenType.UINT_LITERAL
                || token.getTokenType() == TokenType.DOUBLE_LITERAL
                || token.getTokenType() == TokenType.STRING_LITERAL
                || token.getTokenType() == TokenType.CHAR_LITERAL) {
            dataType = analyseLiteralExpression();
        } else if (token.getTokenType() == TokenType.L_PAREN) {
            expect(TokenType.L_PAREN);
            dataType = analyseExpression();
            expect(TokenType.R_PAREN);
        } else {
            throw new AnalyzeError(ErrorCode.InvalidInput, token.getStartPos());
        }
        return dataType;
    }

    /**
     * 调用函数的表达式
     * call_param_list -> expr (',' expr)*
     * call_expr -> IDENT '(' call_param_list? ')'
     */
    private DataType analyseCallExpression() throws CompileError {
        DataType dataType;
        //IDENT
        var ident = expect(TokenType.IDENT);
        //TODO:如果是库函数该如何处理?
        Symbol symbol = symbolTable.findSymbolBySymbolName(level, ident.getValueString(), ident.getStartPos());
        if (symbol == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, ident.getStartPos());
        }
        if (!(symbol instanceof FunctionSymbol)) {
            throw new AnalyzeError(ErrorCode.NotAFunction, ident.getStartPos());
        }
        var paramIndex = -1;
        FunctionSymbol functionSymbol = (FunctionSymbol) symbol;
        //获得函数的返回类型
        dataType = functionSymbol.getReturnType();

        //(
        expect(TokenType.L_PAREN);
        if (!check(TokenType.R_PAREN)) {
            while (true) {
                var tempDataType = analyseExpression();
                ++paramIndex;
                if (paramIndex < functionSymbol.getParamsSize()) {
                    //参数类型不匹配
                    if (functionSymbol.getParamDataTypeByIndex(paramIndex) != tempDataType) {
                        throw new AnalyzeError(ErrorCode.FunctionParamDataTypeNotMap, ident.getStartPos());
                    }
                } else {
                    throw new AnalyzeError(ErrorCode.FunctionParamsNotSuit, ident.getStartPos());
                }
                if (check(TokenType.R_PAREN)) {
                    break;
                }
                expect(TokenType.COMMA);
            }
        }
        //)
        expect(TokenType.R_PAREN);
        if (paramIndex != functionSymbol.getParamsSize() - 1) {
            throw new AnalyzeError(ErrorCode.FunctionParamsNotSuit, ident.getStartPos());
        }
        return dataType;
    }

    /**
     * ident_expr -> IDENT
     */
    private DataType analyseIdentExpression() throws CompileError {
        DataType dataType;
        //IDENT
        var ident = expect(TokenType.IDENT);
        Symbol symbol = symbolTable.findSymbolBySymbolName(level, ident.getValueString(), ident.getStartPos());
        if (symbol == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, ident.getStartPos());
        }
        if (symbol instanceof VariableSymbol) {
            VariableSymbol _symbol = (VariableSymbol) symbol;
            if (!_symbol.isInitialized()) {
                throw new AnalyzeError(ErrorCode.NotInitialized, ident.getStartPos());
            }
        }
        dataType = symbol.getDataType();
        return dataType;
    }

    /**
     * literal_expr -> UINT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL | CHAR_LITERAL
     */
    private DataType analyseLiteralExpression() throws CompileError {
        DataType dataType;
        var token = next();
        if (token.getTokenType() == TokenType.UINT_LITERAL) {
            //TODO
            dataType = DataType.INT;
        } else if (token.getTokenType() == TokenType.DOUBLE_LITERAL) {
            //TODO
            dataType = DataType.DOUBLE;
        } else if (token.getTokenType() == TokenType.STRING_LITERAL) {
            //TODO
            dataType = DataType.STRING;
        } else if (token.getTokenType() == TokenType.CHAR_LITERAL) {
            //TODO
            dataType = DataType.CHAR;
        } else {
            throw new AnalyzeError(ErrorCode.InvalidInput, token.getStartPos());
        }
        return dataType;
    }
}