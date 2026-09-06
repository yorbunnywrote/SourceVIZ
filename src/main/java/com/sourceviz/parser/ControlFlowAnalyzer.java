package com.sourceviz.parser;

import com.sourceviz.model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ControlFlowAnalyzer {

    public static MethodFlowchart buildFlowchart(
            String methodName,
            String className,
            String sourceFile,
            int startLine,
            int endLine,
            String bodyCode) {

        MethodFlowchart flowchart = new MethodFlowchart(methodName, className, sourceFile, startLine, endLine);
        AtomicInteger idGen = new AtomicInteger(1);

        String startId = "node_" + idGen.getAndIncrement();
        String methodSig = (className.isEmpty() ? "" : className + "::") + methodName + "()";
        flowchart.addNode(new FlowNode(startId, FlowNodeType.START, "Начало: " + methodSig, "", startLine));

        List<Statement> stmts = parseStatements(bodyCode, startLine);

        FlowGraphFragment fragment = compileStatements(stmts, flowchart, idGen);

        if (fragment.entryNodeId != null) {
            flowchart.addEdge(startId, fragment.entryNodeId);

            if (!fragment.exitNodeIds.isEmpty()) {
                String endId = "node_" + idGen.getAndIncrement();
                flowchart.addNode(new FlowNode(endId, FlowNodeType.END, "Конец: " + methodName, "", endLine));
                for (String exitId : fragment.exitNodeIds) {
                    flowchart.addEdge(exitId, endId);
                }
            }
        } else {
            String endId = "node_" + idGen.getAndIncrement();
            flowchart.addNode(new FlowNode(endId, FlowNodeType.END, "Конец: " + methodName, "", endLine));
            flowchart.addEdge(startId, endId);
        }

        return flowchart;
    }

    // ==========================================
    // Flow Graph Compilation Logic
    // ==========================================

    private static class FlowGraphFragment {
        String entryNodeId;
        List<String> exitNodeIds = new ArrayList<>();
        List<String> breakNodeIds = new ArrayList<>();
        List<String> continueNodeIds = new ArrayList<>();

        FlowGraphFragment(String entryNodeId, String exitNodeId) {
            this.entryNodeId = entryNodeId;
            if (exitNodeId != null) {
                this.exitNodeIds.add(exitNodeId);
            }
        }

        FlowGraphFragment() {}
    }

    private static FlowGraphFragment compileStatements(
            List<Statement> statements,
            MethodFlowchart chart,
            AtomicInteger idGen) {

        FlowGraphFragment result = new FlowGraphFragment();
        if (statements == null || statements.isEmpty()) {
            return result;
        }

        List<String> currentExits = new ArrayList<>();

        for (int i = 0; i < statements.size(); i++) {
            Statement stmt = statements.get(i);
            FlowGraphFragment frag = compileStatement(stmt, chart, idGen);

            if (frag.entryNodeId == null) {
                continue;
            }

            if (result.entryNodeId == null) {
                result.entryNodeId = frag.entryNodeId;
            }

            // Connect previous exits to this entry
            for (String exit : currentExits) {
                chart.addEdge(exit, frag.entryNodeId);
            }

            currentExits = new ArrayList<>(frag.exitNodeIds);
            result.breakNodeIds.addAll(frag.breakNodeIds);
            result.continueNodeIds.addAll(frag.continueNodeIds);

            // If currentExits is empty (e.g. return statement), remaining statements are unreachable
            if (currentExits.isEmpty() && i < statements.size() - 1) {
                break;
            }
        }

        result.exitNodeIds = currentExits;
        return result;
    }

    private static FlowGraphFragment compileStatement(
            Statement stmt,
            MethodFlowchart chart,
            AtomicInteger idGen) {

        if (stmt instanceof IfStatement ifs) {
            return compileIf(ifs, chart, idGen);
        } else if (stmt instanceof WhileStatement ws) {
            return compileWhile(ws, chart, idGen);
        } else if (stmt instanceof ForStatement fs) {
            return compileFor(fs, chart, idGen);
        } else if (stmt instanceof DoWhileStatement dws) {
            return compileDoWhile(dws, chart, idGen);
        } else if (stmt instanceof ReturnStatement rs) {
            String id = "node_" + idGen.getAndIncrement();
            chart.addNode(new FlowNode(id, FlowNodeType.END, "Return " + rs.expr, rs.expr, rs.line));
            FlowGraphFragment frag = new FlowGraphFragment();
            frag.entryNodeId = id; // exits are empty because it returns!
            return frag;
        } else if (stmt instanceof SimpleStatement ss) {
            String id = "node_" + idGen.getAndIncrement();
            FlowNodeType type = FlowNodeType.PROCESS;
            String text = ss.text.trim();
            if (isIoStatement(text)) {
                type = FlowNodeType.INPUT_OUTPUT;
            } else if (isCallStatement(text)) {
                type = FlowNodeType.SUBROUTINE;
            }
            chart.addNode(new FlowNode(id, type, shorten(text, 60), text, ss.line));
            return new FlowGraphFragment(id, id);
        } else if (stmt instanceof BlockStatement bs) {
            return compileStatements(bs.statements, chart, idGen);
        }

        return new FlowGraphFragment();
    }

    private static FlowGraphFragment compileIf(
            IfStatement ifs,
            MethodFlowchart chart,
            AtomicInteger idGen) {

        String condId = "node_" + idGen.getAndIncrement();
        chart.addNode(new FlowNode(condId, FlowNodeType.CONDITION, "Если: " + shorten(ifs.condition, 50), ifs.condition, ifs.line));

        FlowGraphFragment thenFrag = compileStatement(ifs.thenBranch, chart, idGen);
        FlowGraphFragment elseFrag = ifs.elseBranch != null ? compileStatement(ifs.elseBranch, chart, idGen) : null;

        FlowGraphFragment result = new FlowGraphFragment();
        result.entryNodeId = condId;

        if (thenFrag.entryNodeId != null) {
            chart.addEdge(condId, thenFrag.entryNodeId, "Да (True)");
            result.exitNodeIds.addAll(thenFrag.exitNodeIds);
            result.breakNodeIds.addAll(thenFrag.breakNodeIds);
            result.continueNodeIds.addAll(thenFrag.continueNodeIds);
        } else {
            result.exitNodeIds.add(condId);
        }

        if (elseFrag != null && elseFrag.entryNodeId != null) {
            chart.addEdge(condId, elseFrag.entryNodeId, "Нет (False)");
            result.exitNodeIds.addAll(elseFrag.exitNodeIds);
            result.breakNodeIds.addAll(elseFrag.breakNodeIds);
            result.continueNodeIds.addAll(elseFrag.continueNodeIds);
        } else {
            // False branch simply bypasses to merge
            result.exitNodeIds.add(condId);
        }

        return result;
    }

    private static FlowGraphFragment compileWhile(
            WhileStatement ws,
            MethodFlowchart chart,
            AtomicInteger idGen) {

        String condId = "node_" + idGen.getAndIncrement();
        chart.addNode(new FlowNode(condId, FlowNodeType.LOOP_CONDITION, "Пока: " + shorten(ws.condition, 50), ws.condition, ws.line));

        FlowGraphFragment bodyFrag = compileStatement(ws.body, chart, idGen);

        FlowGraphFragment result = new FlowGraphFragment();
        result.entryNodeId = condId;

        if (bodyFrag.entryNodeId != null) {
            chart.addEdge(condId, bodyFrag.entryNodeId, "Да (Тело)");
            for (String exit : bodyFrag.exitNodeIds) {
                chart.addEdge(exit, condId); // loop back
            }
            for (String cont : bodyFrag.continueNodeIds) {
                chart.addEdge(cont, condId);
            }
            result.exitNodeIds.addAll(bodyFrag.breakNodeIds);
        }

        // Loop condition false exits the loop
        result.exitNodeIds.add(condId);

        return result;
    }

    private static FlowGraphFragment compileFor(
            ForStatement fs,
            MethodFlowchart chart,
            AtomicInteger idGen) {

        String condId = "node_" + idGen.getAndIncrement();
        chart.addNode(new FlowNode(condId, FlowNodeType.LOOP_CONDITION, "Цикл: " + shorten(fs.header, 50), fs.header, fs.line));

        FlowGraphFragment bodyFrag = compileStatement(fs.body, chart, idGen);

        FlowGraphFragment result = new FlowGraphFragment();
        result.entryNodeId = condId;

        if (bodyFrag.entryNodeId != null) {
            chart.addEdge(condId, bodyFrag.entryNodeId, "Да (Итерация)");
            for (String exit : bodyFrag.exitNodeIds) {
                chart.addEdge(exit, condId);
            }
            for (String cont : bodyFrag.continueNodeIds) {
                chart.addEdge(cont, condId);
            }
            result.exitNodeIds.addAll(bodyFrag.breakNodeIds);
        }

        result.exitNodeIds.add(condId);
        return result;
    }

    private static FlowGraphFragment compileDoWhile(
            DoWhileStatement dws,
            MethodFlowchart chart,
            AtomicInteger idGen) {

        FlowGraphFragment bodyFrag = compileStatement(dws.body, chart, idGen);
        String condId = "node_" + idGen.getAndIncrement();
        chart.addNode(new FlowNode(condId, FlowNodeType.LOOP_CONDITION, "Пока: " + shorten(dws.condition, 50), dws.condition, dws.line));

        FlowGraphFragment result = new FlowGraphFragment();
        if (bodyFrag.entryNodeId != null) {
            result.entryNodeId = bodyFrag.entryNodeId;
            for (String exit : bodyFrag.exitNodeIds) {
                chart.addEdge(exit, condId);
            }
            chart.addEdge(condId, bodyFrag.entryNodeId, "Да (Повтор)");
            result.exitNodeIds.addAll(bodyFrag.breakNodeIds);
        } else {
            result.entryNodeId = condId;
        }

        result.exitNodeIds.add(condId);
        return result;
    }

    // ==========================================
    // AST Statement Hierarchy
    // ==========================================

    public interface Statement {}

    public static class SimpleStatement implements Statement {
        public final String text;
        public final int line;
        public SimpleStatement(String text, int line) { this.text = text; this.line = line; }
    }

    public static class ReturnStatement implements Statement {
        public final String expr;
        public final int line;
        public ReturnStatement(String expr, int line) { this.expr = expr; this.line = line; }
    }

    public static class IfStatement implements Statement {
        public final String condition;
        public final Statement thenBranch;
        public final Statement elseBranch;
        public final int line;
        public IfStatement(String condition, Statement thenBranch, Statement elseBranch, int line) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
            this.line = line;
        }
    }

    public static class WhileStatement implements Statement {
        public final String condition;
        public final Statement body;
        public final int line;
        public WhileStatement(String condition, Statement body, int line) {
            this.condition = condition;
            this.body = body;
            this.line = line;
        }
    }

    public static class ForStatement implements Statement {
        public final String header;
        public final Statement body;
        public final int line;
        public ForStatement(String header, Statement body, int line) {
            this.header = header;
            this.body = body;
            this.line = line;
        }
    }

    public static class DoWhileStatement implements Statement {
        public final Statement body;
        public final String condition;
        public final int line;
        public DoWhileStatement(Statement body, String condition, int line) {
            this.body = body;
            this.condition = condition;
            this.line = line;
        }
    }

    public static class BlockStatement implements Statement {
        public final List<Statement> statements = new ArrayList<>();
    }

    // ==========================================
    // Parser of Body Code into Statements
    // ==========================================

    public static List<Statement> parseStatements(String code, int baseLine) {
        List<Statement> list = new ArrayList<>();
        if (code == null || code.isBlank()) return list;

        String cleaned = cleanComments(code);
        TokenScanner scanner = new TokenScanner(cleaned, baseLine);

        while (scanner.hasNext()) {
            Statement s = parseSingleStatement(scanner);
            if (s != null) {
                list.add(s);
            }
        }

        return list;
    }

    private static Statement parseSingleStatement(TokenScanner s) {
        s.skipWhitespace();
        if (!s.hasNext()) return null;

        int currentLine = s.getLine();
        char ch = s.peek();

        if (ch == '{') {
            s.next(); // eat '{'
            BlockStatement block = new BlockStatement();
            while (s.hasNext() && s.peek() != '}') {
                Statement inner = parseSingleStatement(s);
                if (inner != null) block.statements.add(inner);
                s.skipWhitespace();
            }
            if (s.hasNext() && s.peek() == '}') s.next(); // eat '}'
            return block;
        }

        String token = s.peekWord();
        if ("if".equals(token)) {
            s.skipWord(); // eat 'if'
            String cond = s.readParenGroup();
            Statement thenBranch = parseSingleStatement(s);
            Statement elseBranch = null;
            s.skipWhitespace();
            if (s.hasNext() && "else".equals(s.peekWord())) {
                s.skipWord(); // eat 'else'
                elseBranch = parseSingleStatement(s);
            }
            return new IfStatement(cond.isEmpty() ? "условие" : cond, thenBranch, elseBranch, currentLine);
        }

        if ("while".equals(token)) {
            s.skipWord();
            String cond = s.readParenGroup();
            Statement body = parseSingleStatement(s);
            return new WhileStatement(cond.isEmpty() ? "условие" : cond, body, currentLine);
        }

        if ("for".equals(token)) {
            s.skipWord();
            String header = s.readParenGroup();
            Statement body = parseSingleStatement(s);
            return new ForStatement(header.isEmpty() ? "цикл" : header, body, currentLine);
        }

        if ("do".equals(token)) {
            s.skipWord();
            Statement body = parseSingleStatement(s);
            s.skipWhitespace();
            String cond = "";
            if (s.hasNext() && "while".equals(s.peekWord())) {
                s.skipWord();
                cond = s.readParenGroup();
            }
            if (s.hasNext() && s.peek() == ';') s.next();
            return new DoWhileStatement(body, cond.isEmpty() ? "условие" : cond, currentLine);
        }

        if ("return".equals(token)) {
            s.skipWord();
            String expr = s.readUntil(';');
            if (s.hasNext() && s.peek() == ';') s.next();
            return new ReturnStatement(expr.trim(), currentLine);
        }

        // Generic simple statement up to ';'
        String stmtText = s.readUntil(';');
        if (s.hasNext() && s.peek() == ';') s.next();
        stmtText = stmtText.trim();
        if (stmtText.isEmpty()) return null;

        return new SimpleStatement(stmtText, currentLine);
    }

    // ==========================================
    // Helper Token Scanner
    // ==========================================

    private static class TokenScanner {
        private final String input;
        private int pos = 0;
        private int line;

        TokenScanner(String input, int baseLine) {
            this.input = input;
            this.line = baseLine;
        }

        boolean hasNext() { return pos < input.length(); }
        char peek() { return input.charAt(pos); }
        int getLine() { return line; }

        char next() {
            char c = input.charAt(pos++);
            if (c == '\n') line++;
            return c;
        }

        void skipWhitespace() {
            while (hasNext() && Character.isWhitespace(peek())) {
                next();
            }
        }

        String peekWord() {
            skipWhitespace();
            int p = pos;
            StringBuilder sb = new StringBuilder();
            while (p < input.length() && (Character.isLetterOrDigit(input.charAt(p)) || input.charAt(p) == '_')) {
                sb.append(input.charAt(p++));
            }
            return sb.toString();
        }

        void skipWord() {
            skipWhitespace();
            while (hasNext() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
                next();
            }
        }

        String readParenGroup() {
            skipWhitespace();
            if (!hasNext() || peek() != '(') return "";
            next(); // eat '('
            StringBuilder sb = new StringBuilder();
            int depth = 1;
            while (hasNext() && depth > 0) {
                char c = next();
                if (c == '(') depth++;
                else if (c == ')') {
                    depth--;
                    if (depth == 0) break;
                }
                sb.append(c);
            }
            return sb.toString().trim();
        }

        String readUntil(char stopChar) {
            StringBuilder sb = new StringBuilder();
            int braceDepth = 0;
            int parenDepth = 0;
            boolean inString = false;
            char stringChar = 0;

            while (hasNext()) {
                char c = peek();

                if (!inString && (c == '"' || c == '\'')) {
                    inString = true;
                    stringChar = c;
                } else if (inString && c == stringChar) {
                    if (pos > 0 && input.charAt(pos - 1) != '\\') {
                        inString = false;
                    }
                }

                if (!inString) {
                    if (c == '{') braceDepth++;
                    else if (c == '}') {
                        if (braceDepth == 0) break; // outer end
                        braceDepth--;
                    } else if (c == '(') parenDepth++;
                    else if (c == ')') parenDepth = Math.max(0, parenDepth - 1);
                    else if (c == stopChar && braceDepth == 0 && parenDepth == 0) {
                        break;
                    }
                }

                sb.append(next());
            }
            return sb.toString();
        }
    }

    private static String cleanComments(String code) {
        StringBuilder sb = new StringBuilder();
        boolean inBlock = false;
        boolean inLine = false;
        boolean inStr = false;
        char strQuote = 0;

        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            char next = (i + 1 < code.length()) ? code.charAt(i + 1) : 0;

            if (inLine) {
                if (c == '\n') {
                    inLine = false;
                    sb.append(c);
                } else {
                    sb.append(' ');
                }
            } else if (inBlock) {
                if (c == '*' && next == '/') {
                    inBlock = false;
                    sb.append("  ");
                    i++;
                } else {
                    if (c == '\n') sb.append('\n');
                    else sb.append(' ');
                }
            } else if (inStr) {
                sb.append(c);
                if (c == strQuote && code.charAt(i - 1) != '\\') {
                    inStr = false;
                }
            } else {
                if (c == '/' && next == '/') {
                    inLine = true;
                    sb.append("  ");
                    i++;
                } else if (c == '/' && next == '*') {
                    inBlock = true;
                    sb.append("  ");
                    i++;
                } else if (c == '"' || c == '\'') {
                    inStr = true;
                    strQuote = c;
                    sb.append(c);
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    private static boolean isIoStatement(String text) {
        return text.contains("cout") || text.contains("cin") ||
                text.contains("printf") || text.contains("scanf") ||
                text.contains("System.out") || text.contains("System.err") ||
                text.contains("Scanner") || text.contains("print");
    }

    private static boolean isCallStatement(String text) {
        return text.matches("^[a-zA-Z0-9_.]+\\s*\\(.*\\).*") && !text.startsWith("if") && !text.startsWith("for") && !text.startsWith("while");
    }

    private static String shorten(String s, int maxLen) {
        if (s == null) return "";
        s = s.replaceAll("\\s+", " ").trim();
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }
}
