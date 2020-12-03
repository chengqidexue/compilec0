//package com.buaa.compilec0;
//
//import com.buaa.compilec0.analyser.Analyser;
//import com.buaa.compilec0.error.CompileError;
//import com.buaa.compilec0.instruction.Instruction;
//import com.buaa.compilec0.tokenizer.StringIter;
//import com.buaa.compilec0.tokenizer.Token;
//import com.buaa.compilec0.tokenizer.Tokenizer;
//import com.buaa.compilec0.tokenizer.TokenType;
//import net.sourceforge.argparse4j.ArgumentParsers;
//import net.sourceforge.argparse4j.impl.Arguments;
//import net.sourceforge.argparse4j.inf.ArgumentParser;
//import net.sourceforge.argparse4j.inf.ArgumentParserException;
//import net.sourceforge.argparse4j.inf.Namespace;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Scanner;
//
//@SpringBootApplication
//public class Compilec0Application {
//
//    public static void main(String[] args) throws CompileError {
//        var argparse = buildArgparse();
//        Namespace result;
//        try {
//            result = (Namespace) argparse.parseArgs(args);
//        } catch (ArgumentParserException e1) {
//            argparse.handleError(e1);
//            return;
//        }
//
//        var inputFileName = result.getString("input");
//        var outputFileName = result.getString("output");
//
//        InputStream input;
//        if (inputFileName.equals("-")) {
//            input = System.in;
//        } else {
//            try {
//                input = new FileInputStream(inputFileName);
//            } catch (FileNotFoundException e) {
//                System.err.println("Cannot find input file.");
//                e.printStackTrace();
//                System.exit(2);
//                return;
//            }
//        }
//
//        PrintStream output;
//        if (outputFileName.equals("-")) {
//            output = System.out;
//        } else {
//            try {
//                output = new PrintStream(new FileOutputStream(outputFileName));
//            } catch (FileNotFoundException e) {
//                System.err.println("Cannot open output file.");
//                e.printStackTrace();
//                System.exit(2);
//                return;
//            }
//        }
//
//        Scanner scanner;
//        scanner = new Scanner(input);
//        var iter = new StringIter(scanner);
//        var tokenizer = tokenize(iter);
//
//        if (result.getBoolean("tokenize")) {
//            // tokenize
//            var tokens = new ArrayList<Token>();
//            try {
//                while (true) {
//                    var token = tokenizer.nextToken();
//                    if (token.getTokenType().equals(TokenType.EOF)) {
//                        break;
//                    }
//                    tokens.add(token);
//                }
//            } catch (Exception e) {
//                // 遇到错误不输出，直接退出
//                System.err.println(e);
//                System.exit(0);
//                return;
//            }
//            for (Token token : tokens) {
//                output.println(token.toString());
//            }
//        } else if (result.getBoolean("analyse")) {
//            // analyze
//            var analyzer = new Analyser(tokenizer);
//            List<Instruction> instructions;
//            try {
//                instructions = analyzer.analyse();
//            } catch (Exception e) {
//                // 遇到错误不输出，直接退出
//                System.err.println(e);
//                System.exit(0);
//                return;
//            }
//            for (Instruction instruction : instructions) {
//                output.println(instruction.toString());
//            }
//        } else {
//            System.err.println("Please specify either '--analyse' or '--tokenize'.");
//            System.exit(3);
//        }
//    }
//
//    private static ArgumentParser buildArgparse() {
//        var builder = ArgumentParsers.newFor("compilec0");
//        var parser = builder.build();
//        parser.addArgument("-t", "--tokenize").help("Tokenize the input").action(Arguments.storeTrue());
//        parser.addArgument("-l", "--analyse").help("Analyze the input").action(Arguments.storeTrue());
//        parser.addArgument("-o", "--output").help("Set the output file").required(true).dest("output")
//                .action(Arguments.store());
//        parser.addArgument("file").required(true).dest("input").action(Arguments.store()).help("Input file");
//        return parser;
//    }
//
//    private static Tokenizer tokenize(StringIter iter) {
//        var tokenizer = new Tokenizer(iter);
//        return tokenizer;
//    }
//
//}