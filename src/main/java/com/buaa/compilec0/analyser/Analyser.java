package com.buaa.compilec0.analyser;

import com.buaa.compilec0.assembler.*;
import com.buaa.compilec0.error.*;
import com.buaa.compilec0.library.LibFuncUtils;
import com.buaa.compilec0.library.LibFunctions;
import com.buaa.compilec0.symbol.*;
import com.buaa.compilec0.tokenizer.Token;
import com.buaa.compilec0.tokenizer.TokenType;
import com.buaa.compilec0.tokenizer.Tokenizer;
import com.buaa.compilec0.util.Pos;

import java.util.ArrayList;

public final class Analyser {

    Tokenizer tokenizer;
    Assembler assembler = new Assembler();

    private SymbolTable symbolTable = new SymbolTable();

    /**
     * 符号表的层次
     * 永远指向我现在所分析到的层次
     */
    private int level = 0;

    /**
     * 下一个全局变量的偏移
     */
    private int globalOffset = 0;

    private int getNextGlobalOffset() {
        return globalOffset++;
    }

    /**
     * 下一个局部变量的偏移
     * 总是等于此时函数中的局部变量的偏移
     * 这个也用来记录函数中共产生了多少个局部变量
     */
    private int localOffset = 0;

    private void initLocalOffset() {
        localOffset = 0;
    }

    private int getNextLocalOffset() {
        return localOffset++;
    }

    /**
     * 开始函数的指令index
     */
    private int startFunctionInstructionIndex = 0;

    /**
     * 初始化的函数名称
     */
    private String initFunctionName = "INITIAL_FUNCTION";

    /**
     * 记录当前编译到哪层函数下了
     * 初始化为init
     */
    private String nowFunctionName = "";

    /**
     * 记录当前工作的指令应该添加的地方
     */
    private Function nowInstructionFunction;
    private int nowInstructionFunctionIndex;

    /**
     * 设置当前是否在while语句中
     * 用于处理continue, break
     */
    private boolean isInWhile = false;
    private int startWhileIndex;
    private int impossibleBrNum = 100000000;

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
    }

    public Assembler analyse() throws CompileError {
        /**
         * 首先将所有的tokens存到list中
         */
        getAllTokens();
        /**
         * push主函数符号表
         */
        symbolTable.pushSymbolTable();
        /**
         * 设置工作函数为默认
         */
        nowFunctionName = initFunctionName;

        analyseProgram();
        //设置开始函数的index
        assembler.setStartFunctionGlobalOffset(assembler.globals.size());
        //调用call
        Symbol symbol = symbolTable.findSymbolBySymbolName(0, "main", new Pos(0, 0));
        var stackAllocNum = 0;
        int callOffset;
        if (symbol == null) {
            throw new AnalyzeError(ErrorCode.NoMainFunction, new Pos(0, 0));
        } else if (symbol instanceof FunctionSymbol) {
            FunctionSymbol main = (FunctionSymbol) symbol;
            callOffset = main.getOffset();
            if (main.getReturnType() != DataType.VOID) {
                stackAllocNum = 1;
            }
        } else {
            throw new AnalyzeError(ErrorCode.NoMainFunction, new Pos(0, 0));
        }
        assembler.startFunction.addInstruction(new Instruction(startFunctionInstructionIndex++, Operation.stackalloc, stackAllocNum));
        assembler.startFunction.addInstruction(new Instruction(startFunctionInstructionIndex++, Operation.callname, callOffset));
        if (stackAllocNum == 1) {
            assembler.startFunction.addInstruction(new Instruction(startFunctionInstructionIndex++, Operation.popn, 1));
        }
        return assembler;
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

        //设置工作函数
        nowFunctionName = ident.getValueString();
        nowInstructionFunction = new Function(globalOffset);
        nowInstructionFunctionIndex = 0;

        //添加global
        assembler.globals.add(new Global(ident.getValueString(), GlobalType.FUNCTION));
        //添加符号表
        symbolTable.addFunctionSymbol(DataType.VOID, ident.getValueString(), level, getNextGlobalOffset(), ident.getStartPos());

        //(
        expect(TokenType.L_PAREN);
        var token = peek();
        if (token.getTokenType() != TokenType.R_PAREN) {
            //参数列表
            analyseFunctionParamList(ident.getValueString());
        }

        //设置函数的参数大小
        var paramSize = symbolTable.getParamSizeByFunctionName(ident.getValueString(), ident.getStartPos());
        nowInstructionFunction.setParamSize(paramSize);

        //)
        expect(TokenType.R_PAREN);
        //->
        expect(TokenType.ARROW);
        //ty
        var type = expectType();

        //设置函数的返回类型
        var dataType = getDataTypeFromToken(type);
        nowInstructionFunction.setReturnType(dataType);
        symbolTable.setFunctionSymbolReturnType(ident.getValueString(), dataType, type.getStartPos());

        initLocalOffset();
        analyseBlockStatement();
        //设置函数中局部变量的数目
        nowInstructionFunction.setLocalVariableSize(localOffset);
        symbolTable.setFunctionLocalVariableSize(ident.getValueString(), localOffset, ident.getStartPos());

        if (nowInstructionFunction.getInstructions().size() == 0) {
            nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.ret));
        }
        //退出时将工作函数目录设置回默认的并添加到functions
        assembler.addFunction(nowInstructionFunction);
        nowFunctionName = initFunctionName;
    }

    /**
     * function_param_list -> function_param (',' function_param)*
     */
    private void analyseFunctionParamList(String functionName) throws CompileError {
        //参数的index
        int paramIndex = 0;
        //参数
        analyseFunctionParam(functionName, paramIndex);
        paramIndex++;
        var next = peek();
        while (next.getTokenType() == TokenType.COMMA) {
            expect(TokenType.COMMA);
            analyseFunctionParam(functionName, paramIndex);
            paramIndex++;
            next = peek();
        }
    }

    /**
     * function_param -> 'const'? IDENT ':' ty
     *
     * @throws CompileError
     */
    private void analyseFunctionParam(String functionName, int paramIndex) throws CompileError {
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
            //添加到函数的参数表中
            symbolTable.addFunctionParamSymbol(functionName, dataType, ident.getValueString(), level, paramIndex, ident.getStartPos(), true);

        } else if (next.getTokenType() == TokenType.IDENT) {
            //IDENT
            var ident = expect(TokenType.IDENT);
            //:
            expect(TokenType.COLON);
            //ty
            var type = expectType();
            DataType dataType = getDataTypeFromToken(type);
            symbolTable.addFunctionParamSymbol(functionName, dataType, ident.getValueString(), level, paramIndex, ident.getStartPos(), false);
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

        //判断这是一个全局变量还是一个局部变量
        int offset;
        if (level == 0) {
            offset = getNextGlobalOffset();
            assembler.addGlobal(new Global(ident.getValueString(), GlobalType.VARIABLE));
        } else {
            //如果是局部变量，还要判断一下是否和函数的参数重名
            var param = symbolTable.findFunctionParamSymbolBySymbolName(nowFunctionName, ident.getValueString(), ident.getStartPos());
            if (param != null) {
                throw new AnalyzeError(ErrorCode.DuplicateWithTheParam, ident.getStartPos());
            }
            offset = getNextLocalOffset();
        }
        var next = peek();
        if (next.getTokenType() == TokenType.ASSIGN) {
            expect(TokenType.ASSIGN);
            //先在栈上加载一个地址
            if (level == 0) {
                assembler.startFunction.addInstruction(new Instruction(startFunctionInstructionIndex++, Operation.globa, offset));
            } else {
                nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.loca, offset));
            }
            var tempDataType = analyseExpression();
            if (tempDataType != dataType) {
                throw new AnalyzeError(ErrorCode.InvalidDataType, ident.getStartPos());
            }
            //加入符号表
            symbolTable.addVariableSymbol(dataType, ident.getValueString(), level, offset, ident.getStartPos(), true);

            //生成指令
            if (level == 0) {
                assembler.startFunction.addInstruction(new Instruction(startFunctionInstructionIndex++, Operation.store64));
            } else {
                nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.store64));
            }
        } else {
            symbolTable.addVariableSymbol(dataType, ident.getValueString(), level, offset, ident.getStartPos(), false);
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

        int offset;
        if (level == 0) {
            offset = getNextGlobalOffset();
            assembler.addGlobal(new Global(ident.getValueString(), GlobalType.CONSTANT));
            assembler.startFunction.addInstruction(new Instruction(startFunctionInstructionIndex++, Operation.globa, offset));
        } else {
            var param = symbolTable.findFunctionParamSymbolBySymbolName(nowFunctionName, ident.getValueString(), ident.getStartPos());
            if (param != null) {
                throw new AnalyzeError(ErrorCode.DuplicateWithTheParam, ident.getStartPos());
            }
            offset = getNextLocalOffset();
            nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.loca, offset));
        }
        //=
        expect(TokenType.ASSIGN);
        //expr
        var tempDataType = analyseExpression();
        if (tempDataType != dataType) {
            throw new AnalyzeError(ErrorCode.InvalidDataType, ident.getStartPos());
        }
        //;
        expect(TokenType.SEMICOLON);
        //加入符号表
        symbolTable.addConstantSymbol(dataType, ident.getValueString(), level, offset, ident.getStartPos());
        //生成指令
        if (level == 0) {
            assembler.startFunction.addInstruction(new Instruction(startFunctionInstructionIndex++, Operation.store64));
        } else {
            nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.store64));
        }
    }

    /**
     * block_stmt -> '{' stmt* '}'
     *
     * @throws CompileError
     */
    private void analyseBlockStatement() throws CompileError {
        //新增一级符号表
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

        //pop该符号表
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
        if (next.getTokenType() != TokenType.SEMICOLON) {
            //expr
            nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.arga, 0));
            var dataType = analyseExpression();
            if (dataType != nowInstructionFunction.getReturnType()) {
                throw new AnalyzeError(ErrorCode.InvalidReturnType, next.getStartPos());
            }
            nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.store64));
        }
        //;
        expect(TokenType.SEMICOLON);
        nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.ret));
    }

    /**
     * continue_stmt -> 'continue' ';'
     *
     * @throws CompileError
     */
    private void analyseContinueStatement() throws CompileError {
        //continue
        var continueToken = expect(TokenType.CONTINUE_KW);
        if (!isInWhile) {
            throw new AnalyzeError(ErrorCode.InvalidContinue, continueToken.getStartPos());
        }
        //跳转回到while开始的语句
        int num = startWhileIndex - nowInstructionFunctionIndex - 1;
        nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.br, num));
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
        var breakToken = expect(TokenType.BREAK_KW);
        if (!isInWhile) {
            throw new AnalyzeError(ErrorCode.InvalidBreak, breakToken.getStartPos());
        }
        nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.br, impossibleBrNum));
        //;
        expect(TokenType.SEMICOLON);
    }

    /**
     * while_stmt -> 'while' expr block_stmt
     *
     * @throws CompileError
     */
    private void analyseWhileStatement() throws CompileError {
        startWhileIndex = nowInstructionFunctionIndex;
        isInWhile = true;
        //while
        var whileToken = expect(TokenType.WHILE_KW);
        //expr
        var dataType = analyseExpression();
        if (dataType != DataType.INT && dataType != DataType.DOUBLE && dataType != DataType.BOOL) {
            throw new AnalyzeError(ErrorCode.InvalidDataType, whileToken.getStartPos());
        }
        //如果为真就继续执行
        nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.brtrue, 1));
        //如果为假就跳过block
        int num = 0;
        int jumpBlockIndex = nowInstructionFunctionIndex++;
        Instruction brInstruction = new Instruction(jumpBlockIndex, Operation.br, num);
        nowInstructionFunction.addInstruction(brInstruction);

        //block
        analyseBlockStatement();

        //跳转回到while开始的语句
        num = startWhileIndex - nowInstructionFunctionIndex - 1;
        nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.br, num));

        //设置那个跳过block的偏移
        brInstruction = nowInstructionFunction.getInstructions().get(jumpBlockIndex);
        num = nowInstructionFunctionIndex - jumpBlockIndex - 1;
        brInstruction.setX(num);
        isInWhile = false;

        //判断一下哪里有break
        for (int i = 0; i < nowInstructionFunction.getInstructions().size(); i++) {
            var temp = nowInstructionFunction.getInstructions().get(i);
            if ((int) temp.getX() == impossibleBrNum) {
                num = nowInstructionFunctionIndex - i;
                temp.setX(num);
            }
        }
    }

    /**
     * if_stmt -> 'if' expr block_stmt ('else' 'if' expr block_stmt)* ('else' block_stmt)?
     *
     * @throws CompileError
     */
    private void analyseIfStatement() throws CompileError {
        //if
        var ifToken = expect(TokenType.IF_KW);
        //expr
        var dataType = analyseExpression();
        if (dataType != DataType.INT && dataType != DataType.DOUBLE && dataType != DataType.BOOL) {
            throw new AnalyzeError(ErrorCode.InvalidDataType, ifToken.getStartPos());
        }
        //如果为真的话跳转执行
        nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.brtrue, 1)); //8
        //如果为假的话跳过block执行序列
        int num = 0;
        int jumpBlockIndex = nowInstructionFunctionIndex++; //9
        Instruction brInstruction = new Instruction(jumpBlockIndex, Operation.br, num);
        nowInstructionFunction.addInstruction(brInstruction);

        //block
        analyseBlockStatement();  //11-12 此时的nowIndex = 13

        //跳到整个if语句执行结束后的语句
        num = 0;
        int jumpAllIfIndex = nowInstructionFunctionIndex++;               //12
        Instruction brEndIfInstruction = new Instruction(jumpAllIfIndex, Operation.br, num);

        //给跳过block的语句设置好偏移
        var temp = nowInstructionFunction.getInstructions().get(jumpBlockIndex);
        num = nowInstructionFunctionIndex - jumpBlockIndex - 1;
        temp.setX(num);

        int elseNum = 0;
        int elseEndIndex;

        var next = peek();
        //else if | else
        if (next.getTokenType() == TokenType.ELSE_KW) {
            expect(TokenType.ELSE_KW);
            next = peek();
            //else if
            if (next.getTokenType() == TokenType.IF_KW) {
                analyseIfStatement();
            }
            //else
            else if (next.getTokenType() == TokenType.L_BRACE) {
                analyseBlockStatement();
                elseEndIndex = nowInstructionFunctionIndex++;
                nowInstructionFunction.addInstruction(new Instruction(elseEndIndex, Operation.br, elseNum));
            }
        }

        //给跳过接下来的IF设置好偏移
        //nowIndex 13
        temp = nowInstructionFunction.getInstructions().get(jumpAllIfIndex);
        num = nowInstructionFunctionIndex - jumpAllIfIndex - 1;
        temp.setX(num);
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
                return dataType;
            }
        }

        dataType = analyseAdditiveExpression();
        token = peek();
        //我们的比较方式主要通过栈顶的值是不是0来决定，如果是0，就代表false,否则代表true
        if (token.getTokenType() == TokenType.EQ || token.getTokenType() == TokenType.NEQ
                || token.getTokenType() == TokenType.GT || token.getTokenType() == TokenType.GE
                || token.getTokenType() == TokenType.LT || token.getTokenType() == TokenType.LE) {
            var boolSymbol = next();
            var tempDataType = analyseAdditiveExpression();
            if (tempDataType != dataType) {
                throw new AnalyzeError(ErrorCode.InvalidDataType, boolSymbol.getStartPos());
            }
            Operation operation;
            if (dataType == DataType.INT)
                operation = Operation.cmpi;
            else if (dataType == DataType.DOUBLE)
                operation = Operation.cmpf;
            else {
                throw new AnalyzeError(ErrorCode.InvalidOperator, boolSymbol.getStartPos());
            }
            nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, operation));
            switch (boolSymbol.getTokenType()) {
                case EQ:
                    nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.not));
                    break;
                case NEQ:
                    break;
                case GT:
                    nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.setgt));
                    break;
                case GE:
                    nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.setlt));
                    nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.not));
                    break;
                case LT:
                    nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.setlt));
                    break;
                case LE:
                    nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.setgt));
                    nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.not));
                    break;
                default:
                    throw new AnalyzeError(ErrorCode.InvalidOperator, boolSymbol.getStartPos());
            }
            dataType = DataType.BOOL;
        }
        return dataType;
    }

    /**
     * assign_expr -> IDENT '=' additive_expr
     * 赋值语句，只有可能是参数或者是变量
     * 只有可能出现在函数中
     *
     * @throws CompileError
     */
    private void analyseAssignStatement() throws CompileError {
        //IDENT
        var ident = expect(TokenType.IDENT);
        Symbol symbol;
        //先判断是不是参数
        symbol = symbolTable.findFunctionParamSymbolBySymbolName(nowFunctionName, ident.getValueString(), ident.getStartPos());
        if (symbol == null) {
            //找不到在去本层和上层的符号表中寻找
            symbol = symbolTable.findSymbolBySymbolName(level, ident.getValueString(), ident.getStartPos());
        }
        if (symbol == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, ident.getStartPos());
        }

        if (symbol instanceof ConstantSymbol) {
            throw new AnalyzeError(ErrorCode.AssignToConstant, ident.getStartPos());
        }
        if (symbol instanceof FunctionSymbol) {
            throw new AnalyzeError(ErrorCode.AssignToFunction, ident.getStartPos());
        }
        if (symbol instanceof ParamSymbol) {
            ParamSymbol paramSymbol = (ParamSymbol) symbol;
            if (paramSymbol.isConstant()) {
                throw new AnalyzeError(ErrorCode.AssignToConstantParam, ident.getStartPos());
            }
        }
        //在栈顶放上地址，根据ident的类型的不同，决定去哪里找
        if (symbol instanceof VariableSymbol) {
            if (symbol.getLevel() == 0) {
                //全局变量
                nowInstructionFunction.addInstruction(new Instruction(startFunctionInstructionIndex++, Operation.globa, symbol.getOffset()));
            } else {
                //局部变量
                nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.loca, symbol.getOffset()));
            }
        } else if (symbol instanceof ParamSymbol) {
            ParamSymbol paramSymbol = (ParamSymbol) symbol;
            //查看有没有返回值
            //默认没有返回值
            var returnFlag = 0;
            if (nowInstructionFunction.getReturnType() != DataType.VOID) {
                returnFlag = 1;
            }
            //判断是第几个参数
            var paramIndex = paramSymbol.getOffset();
            nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.arga, paramIndex + returnFlag));
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

        nowInstructionFunction.addInstruction(new Instruction(nowInstructionFunctionIndex++, Operation.store64));
    }

    /**
     * additive_expr -> mult_expr {'+'|'-' mult_expr}
     * 可能出现在整体上也可能在函数中
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

            //添加instructions，要根据环境不同决定添加到什么地方
            Function beAddedFunction;
            int instructionIndex;
            Operation operation;
            if (level == 0) {
                beAddedFunction = assembler.startFunction;
                instructionIndex = startFunctionInstructionIndex++;
            } else {
                beAddedFunction = nowInstructionFunction;
                instructionIndex = nowInstructionFunctionIndex++;
            }
            switch (nameToken.getTokenType()) {
                case PLUS:
                    if (dataType == DataType.INT) {
                        operation = Operation.addi;
                    } else if (dataType == DataType.DOUBLE) {
                        operation = Operation.addf;
                    } else {
                        throw new AnalyzeError(ErrorCode.InvalidDataType, nameToken.getStartPos());
                    }
                    break;
                case MINUS:
                    if (dataType == DataType.INT) {
                        operation = Operation.subi;
                    } else if (dataType == DataType.DOUBLE) {
                        operation = Operation.subf;
                    } else {
                        throw new AnalyzeError(ErrorCode.InvalidDataType, nameToken.getStartPos());
                    }
                    break;
                default:
                    throw new AnalyzeError(ErrorCode.InvalidOperator, nameToken.getStartPos());
            }
            beAddedFunction.addInstruction(new Instruction(instructionIndex, operation));
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

            Function beAddedFunction;
            int instructionIndex;
            Operation operation;
            if (level == 0) {
                beAddedFunction = assembler.startFunction;
                instructionIndex = startFunctionInstructionIndex++;
            } else {
                beAddedFunction = nowInstructionFunction;
                instructionIndex = nowInstructionFunctionIndex++;
            }
            switch (nameToken.getTokenType()) {
                case MUL:
                    if (dataType == DataType.INT) {
                        operation = Operation.muli;
                    } else if (dataType == DataType.DOUBLE) {
                        operation = Operation.mulf;
                    } else {
                        throw new AnalyzeError(ErrorCode.InvalidDataType, nameToken.getStartPos());
                    }
                    break;
                case DIV:
                    if (dataType == DataType.INT) {
                        operation = Operation.divi;
                    } else if (dataType == DataType.DOUBLE) {
                        operation = Operation.divf;
                    } else {
                        throw new AnalyzeError(ErrorCode.InvalidDataType, nameToken.getStartPos());
                    }
                    break;
                default:
                    throw new AnalyzeError(ErrorCode.InvalidOperator, nameToken.getStartPos());
            }
            beAddedFunction.addInstruction(new Instruction(instructionIndex, operation));
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
            Function beAddedFunction;
            int instructionIndex;
            Operation operation;
            if (level == 0) {
                beAddedFunction = assembler.startFunction;
                instructionIndex = startFunctionInstructionIndex++;
            } else {
                beAddedFunction = nowInstructionFunction;
                instructionIndex = nowInstructionFunctionIndex++;
            }
            if (dataType.equals(DataType.INT) && tempDataType.equals(DataType.INT)) {
                operation = Operation.nop;
            } else if (dataType.equals(DataType.INT) && tempDataType.equals(DataType.DOUBLE)) {
                operation = Operation.itof;
            } else if (dataType.equals(DataType.DOUBLE) && tempDataType.equals(DataType.DOUBLE)) {
                operation = Operation.nop;
            } else if (dataType.equals(DataType.DOUBLE) && tempDataType.equals(DataType.INT)) {
                operation = Operation.ftoi;
            } else {
                throw new AnalyzeError(ErrorCode.InvalidDataChange, type.getStartPos());
            }
            beAddedFunction.addInstruction(new Instruction(instructionIndex, operation));

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
            Function beAddedFunction;
            int instructionIndex;
            if (level == 0) {
                beAddedFunction = assembler.startFunction;
                instructionIndex = startFunctionInstructionIndex++;
            } else {
                beAddedFunction = nowInstructionFunction;
                instructionIndex = nowInstructionFunctionIndex++;
            }
            Operation operation = (dataType.equals(DataType.INT)) ? Operation.negi : Operation.negf;
            beAddedFunction.addInstruction(new Instruction(instructionIndex, operation));
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
        Function beAddedFunction;
        int instructionIndex;
        if (level == 0) {
            beAddedFunction = assembler.startFunction;
        } else {
            beAddedFunction = nowInstructionFunction;
        }
        var libFunc = LibFuncUtils.isLibFunction(ident);
        if (libFunc != LibFunctions.NOTLIBFUN) {
            switch (libFunc) {
                //getint
                case GETINT: {
                    expect(TokenType.L_PAREN);
                    expect(TokenType.R_PAREN);
                    instructionIndex = (level == 0) ? startFunctionInstructionIndex++ : nowInstructionFunctionIndex++;
                    beAddedFunction.addInstruction(new Instruction(instructionIndex, Operation.scani));
                    dataType = DataType.INT;
                    break;
                }
                //getdouble
                case GETDOUBLE: {
                    expect(TokenType.L_PAREN);
                    expect(TokenType.R_PAREN);
                    instructionIndex = (level == 0) ? startFunctionInstructionIndex++ : nowInstructionFunctionIndex++;
                    beAddedFunction.addInstruction(new Instruction(instructionIndex, Operation.scanf));
                    dataType = DataType.DOUBLE;
                    break;
                }
                //getchar
                case GETCHAR: {
                    expect(TokenType.L_PAREN);
                    expect(TokenType.R_PAREN);
                    instructionIndex = (level == 0) ? startFunctionInstructionIndex++ : nowInstructionFunctionIndex++;
                    beAddedFunction.addInstruction(new Instruction(instructionIndex, Operation.scanc));
                    dataType = DataType.INT;
                    break;
                }
                //putint
                case PUTINT: {
                    var l = expect(TokenType.L_PAREN);
                    var tempDataType = analyseExpression();
                    if (tempDataType != DataType.INT) {
                        throw new AnalyzeError(ErrorCode.InvalidDataType, l.getStartPos());
                    }
                    expect(TokenType.R_PAREN);
                    instructionIndex = (level == 0) ? startFunctionInstructionIndex++ : nowInstructionFunctionIndex++;
                    beAddedFunction.addInstruction(new Instruction(instructionIndex, Operation.printi));
                    dataType = DataType.VOID;
                    break;
                }
                //putdouble
                case PUTDOUBLE: {
                    var l = expect(TokenType.L_PAREN);
                    var tempDataType = analyseExpression();
                    if (tempDataType != DataType.DOUBLE) {
                        throw new AnalyzeError(ErrorCode.InvalidDataType, l.getStartPos());
                    }
                    expect(TokenType.R_PAREN);
                    instructionIndex = (level == 0) ? startFunctionInstructionIndex++ : nowInstructionFunctionIndex++;
                    beAddedFunction.addInstruction(new Instruction(instructionIndex, Operation.printf));
                    dataType = DataType.VOID;
                    break;
                }
                //putchar
                case PUTCHAR: {
                    var l = expect(TokenType.L_PAREN);
                    var tempDataType = analyseExpression();
                    if (tempDataType != DataType.INT) {
                        throw new AnalyzeError(ErrorCode.InvalidDataType, l.getStartPos());
                    }
                    instructionIndex = (level == 0) ? startFunctionInstructionIndex++ : nowInstructionFunctionIndex++;
                    beAddedFunction.addInstruction(new Instruction(instructionIndex, Operation.printc));
                    expect(TokenType.R_PAREN);
                    dataType = DataType.VOID;
                    break;
                }
                //putstr
                case PUTSTR: {
                    expect(TokenType.L_PAREN);
                    var str = expect(TokenType.STRING_LITERAL);
                    String temp = str.getValueString();
                    assembler.addGlobal(new Global(temp, GlobalType.STRING));
                    var num = assembler.globals.size() - 1;
                    instructionIndex = (level == 0) ? startFunctionInstructionIndex++ : nowInstructionFunctionIndex++;
                    beAddedFunction.addInstruction(new Instruction(instructionIndex, Operation.push, num));
                    instructionIndex = (level == 0) ? startFunctionInstructionIndex++ : nowInstructionFunctionIndex++;
                    beAddedFunction.addInstruction(new Instruction(instructionIndex, Operation.prints));
                    expect(TokenType.R_PAREN);
                    dataType = DataType.VOID;
                    break;
                }
                //putln
                case PUTLN: {
                    expect(TokenType.L_PAREN);
                    expect(TokenType.R_PAREN);
                    instructionIndex = (level == 0) ? startFunctionInstructionIndex++ : nowInstructionFunctionIndex++;
                    beAddedFunction.addInstruction(new Instruction(instructionIndex, Operation.println));
                    dataType = DataType.VOID;
                    break;
                }
                default:
                    throw new AnalyzeError(ErrorCode.NoSuchLibFunction, ident.getStartPos());
            }
        } else {
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

            var stackAllocNum = 0;
            if (dataType != DataType.VOID) {
                stackAllocNum = 1;
            }

            instructionIndex = (level == 0) ? startFunctionInstructionIndex++ : nowInstructionFunctionIndex++;
            beAddedFunction.addInstruction(new Instruction(instructionIndex, Operation.stackalloc, stackAllocNum));
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
            instructionIndex = (level == 0) ? startFunctionInstructionIndex++ : nowInstructionFunctionIndex++;
            beAddedFunction.addInstruction(new Instruction(instructionIndex, Operation.callname, functionSymbol.getOffset()));
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
        Symbol symbol;
        //先判断是不是函数的参数
        if (level != 0) {
            symbol = symbolTable.findFunctionParamSymbolBySymbolName(nowFunctionName, ident.getValueString(), ident.getStartPos());
            if (symbol == null) {
                symbol = symbolTable.findSymbolBySymbolName(level, ident.getValueString(), ident.getStartPos());
            }
        }
        //全局
        else {
            symbol = symbolTable.findSymbolBySymbolName(level, ident.getValueString(), ident.getStartPos());
        }

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
        //添加instructions
        Function beAddedFunction;
        int instructionIndex;
        Operation operation;
        int offset = symbol.getOffset();
        if (level == 0) {
            //全局中引用
            beAddedFunction = assembler.startFunction;
            instructionIndex = startFunctionInstructionIndex++;
            operation = Operation.globa;
        } else {
            //局部中引用
            beAddedFunction = nowInstructionFunction;
            instructionIndex = nowInstructionFunctionIndex++;
            if (symbol.getLevel() == 0) {
                //引用全局量
                operation = Operation.globa;
            } else if (symbol instanceof ParamSymbol) {
                //引用参数
                operation = Operation.arga;
                if (nowInstructionFunction.getReturnType() != DataType.VOID) {
                    offset++;
                }
            } else {
                //引用局部变量
                operation = Operation.loca;
            }
        }
        beAddedFunction.addInstruction(new Instruction(instructionIndex, operation, offset));
        beAddedFunction.addInstruction(new Instruction(instructionIndex, Operation.load64));
        return dataType;
    }

    /**
     * literal_expr -> UINT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL | CHAR_LITERAL
     */
    private DataType analyseLiteralExpression() throws CompileError {
        DataType dataType;
        var token = next();
        Function beAddedFunction;
        int instructionIndex;
        int num = 0;
        if (level == 0) {
            beAddedFunction = assembler.startFunction;
            instructionIndex = startFunctionInstructionIndex++;
        } else {
            beAddedFunction = nowInstructionFunction;
            instructionIndex = nowInstructionFunctionIndex++;
        }
        if (token.getTokenType() == TokenType.UINT_LITERAL) {
            num = (int) token.getValue();
            dataType = DataType.INT;
        } else if (token.getTokenType() == TokenType.DOUBLE_LITERAL) {
            //TODO
            dataType = DataType.DOUBLE;
        } else if (token.getTokenType() == TokenType.STRING_LITERAL) {
            String temp = token.getValueString();
            assembler.addGlobal(new Global(temp, GlobalType.STRING));
            num = assembler.globals.size() - 1;
            dataType = DataType.STRING;
        } else if (token.getTokenType() == TokenType.CHAR_LITERAL) {
            //TODO
            dataType = DataType.CHAR;
        } else {
            throw new AnalyzeError(ErrorCode.InvalidInput, token.getStartPos());
        }
        beAddedFunction.addInstruction(new Instruction(instructionIndex, Operation.push, num));
        return dataType;
    }
}