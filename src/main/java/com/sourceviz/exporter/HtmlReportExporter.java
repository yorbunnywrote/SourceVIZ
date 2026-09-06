package com.sourceviz.exporter;

import com.sourceviz.analyzer.CodeAnalyzer;
import com.sourceviz.model.CodeEntity;
import com.sourceviz.model.MethodFlowchart;
import com.sourceviz.model.Relationship;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class HtmlReportExporter {

    public static void generateReport(CodeAnalyzer analyzer, Path outputFile) throws IOException {
        StringBuilder sb = new StringBuilder();

        String archMermaid = MermaidExporter.generateArchitectureMermaid(analyzer);
        String fileDepsMermaid = MermaidExporter.generateFileDependenciesMermaid(analyzer);
        List<MethodFlowchart> flowcharts = analyzer.getFlowcharts();

        sb.append("<!DOCTYPE html>\n<html lang=\"ru\">\n<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("    <title>SourceVIZ 2.0 - Отчет по анализу кода и блок-схемам</title>\n");
        sb.append("    <script src=\"https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js\"></script>\n");
        sb.append("    <style>\n");
        sb.append(getStyles());
        sb.append("    </style>\n</head>\n<body>\n");

        // Header & Stats
        sb.append("    <header class=\"header\">\n");
        sb.append("        <div class=\"logo\">⚡ SourceVIZ <span>2.0 (Java Edition)</span></div>\n");
        sb.append("        <div class=\"stats-grid\">\n");
        sb.append(String.format("            <div class=\"stat-card\"><div class=\"val\">%d</div><div class=\"lbl\">Всего файлов</div></div>\n", analyzer.getScanner().getAllFiles().size()));
        sb.append(String.format("            <div class=\"stat-card\"><div class=\"val\">%d</div><div class=\"lbl\">C/C++ файлов</div></div>\n", analyzer.getParsedCppFiles()));
        sb.append(String.format("            <div class=\"stat-card\"><div class=\"val\">%d</div><div class=\"lbl\">Java файлов</div></div>\n", analyzer.getParsedJavaFiles()));
        sb.append(String.format("            <div class=\"stat-card\"><div class=\"val\">%d</div><div class=\"lbl\">Сущностей кода</div></div>\n", analyzer.getEntities().size()));
        sb.append(String.format("            <div class=\"stat-card\"><div class=\"val\">%d</div><div class=\"lbl\">Связей</div></div>\n", analyzer.getRelationships().size()));
        sb.append(String.format("            <div class=\"stat-card highlight\"><div class=\"val\">%d</div><div class=\"lbl\">Блок-схем функций</div></div>\n", flowcharts.size()));
        sb.append("        </div>\n");
        sb.append("    </header>\n\n");

        // Navigation Tabs
        sb.append("    <nav class=\"tabs\">\n");
        sb.append("        <button class=\"tab-btn active\" onclick=\"openTab('tab-flowcharts')\">📊 Алгоритмические блок-схемы</button>\n");
        sb.append("        <button class=\"tab-btn\" onclick=\"openTab('tab-architecture')\">🏛 Архитектура и зависимости</button>\n");
        sb.append("        <button class=\"tab-btn\" onclick=\"openTab('tab-entities')\">📋 Список компонентов</button>\n");
        sb.append("        <button class=\"tab-btn\" onclick=\"openTab('tab-relationships')\">🔗 Связи и вызовы</button>\n");
        sb.append("    </nav>\n\n");

        // TAB 1: FLOWCHARTS
        sb.append("    <main id=\"tab-flowcharts\" class=\"tab-content active\">\n");
        sb.append("        <div class=\"flowchart-layout\">\n");
        sb.append("            <aside class=\"sidebar\">\n");
        sb.append("                <div class=\"search-box\">\n");
        sb.append("                    <input type=\"text\" id=\"methodSearch\" placeholder=\"Поиск функции/метода...\" onkeyup=\"filterMethods()\">\n");
        sb.append("                </div>\n");
        sb.append("                <ul class=\"method-list\" id=\"methodList\">\n");

        for (int i = 0; i < flowcharts.size(); i++) {
            MethodFlowchart fc = flowcharts.get(i);
            String activeClass = (i == 0) ? "active" : "";
            sb.append(String.format("                    <li class=\"%s\" onclick=\"selectFlowchart(%d)\" data-name=\"%s\">\n",
                    activeClass, i, escapeHtml(fc.getFullName().toLowerCase())));
            sb.append(String.format("                        <div class=\"m-name\">%s</div>\n", escapeHtml(fc.getFullName())));
            sb.append(String.format("                        <div class=\"m-file\">%s (L%d-%d)</div>\n",
                    escapeHtml(fc.getShortFileName()), fc.getStartLine(), fc.getEndLine()));
            sb.append("                    </li>\n");
        }

        sb.append("                </ul>\n");
        sb.append("            </aside>\n");

        sb.append("            <section class=\"diagram-viewer\">\n");
        sb.append("                <div class=\"viewer-header\">\n");
        sb.append("                    <div>\n");
        sb.append("                        <h2 id=\"currentMethodTitle\">Выберите функцию</h2>\n");
        sb.append("                        <span id=\"currentMethodMeta\" class=\"badge\"></span>\n");
        sb.append("                    </div>\n");
        sb.append("                    <div class=\"viewer-actions\">\n");
        sb.append("                        <button class=\"btn\" onclick=\"copyMermaidCode()\">📋 Скопировать Mermaid</button>\n");
        sb.append("                    </div>\n");
        sb.append("                </div>\n");
        sb.append("                <div class=\"mermaid-container\" id=\"flowchartContainer\">\n");
        sb.append("                    <div class=\"mermaid\" id=\"mermaidTarget\"></div>\n");
        sb.append("                </div>\n");
        sb.append("            </section>\n");
        sb.append("        </div>\n");
        sb.append("    </main>\n\n");

        // TAB 2: ARCHITECTURE & DEPENDENCIES
        sb.append("    <main id=\"tab-architecture\" class=\"tab-content\">\n");
        sb.append("        <div class=\"arch-section\">\n");
        sb.append("            <div class=\"view-toggles\">\n");
        sb.append("                <button class=\"btn active\" onclick=\"showArchGraph('arch-files')\">Граф файлов (Include/Import)</button>\n");
        sb.append("                <button class=\"btn\" onclick=\"showArchGraph('arch-all')\">Полный граф сущностей</button>\n");
        sb.append("            </div>\n");
        sb.append("            <div id=\"arch-files\" class=\"mermaid-container active-graph\">\n");
        sb.append("                <div class=\"mermaid\">").append(fileDepsMermaid).append("</div>\n");
        sb.append("            </div>\n");
        sb.append("            <div id=\"arch-all\" class=\"mermaid-container\" style=\"display:none;\">\n");
        sb.append("                <div class=\"mermaid\">").append(archMermaid).append("</div>\n");
        sb.append("            </div>\n");
        sb.append("        </div>\n");
        sb.append("    </main>\n\n");

        // TAB 3: ENTITIES TABLE
        sb.append("    <main id=\"tab-entities\" class=\"tab-content\">\n");
        sb.append("        <div class=\"table-card\">\n");
        sb.append("            <div class=\"search-box\" style=\"margin-bottom:15px;\"><input type=\"text\" id=\"entitySearch\" placeholder=\"Поиск по сущностям...\" onkeyup=\"filterTable('entitiesTable', 0)\"></div>\n");
        sb.append("            <table id=\"entitiesTable\">\n");
        sb.append("                <thead><tr><th>Имя</th><th>Тип</th><th>Сигнатура / Детали</th><th>Файл</th><th>Строка</th></tr></thead>\n");
        sb.append("                <tbody>\n");
        for (CodeEntity e : analyzer.getEntities()) {
            sb.append(String.format("                    <tr><td><strong>%s</strong></td><td><span class=\"badge badge-%s\">%s</span></td><td><code>%s</code></td><td>%s</td><td>%d</td></tr>\n",
                    escapeHtml(e.getDisplayName()),
                    e.getType().name().toLowerCase(),
                    escapeHtml(e.getType().name()),
                    escapeHtml(e.getSignature()),
                    escapeHtml(e.getShortFileName()),
                    e.getLineNumber()));
        }
        sb.append("                </tbody>\n            </table>\n        </div>\n    </main>\n\n");

        // TAB 4: RELATIONSHIPS TABLE
        sb.append("    <main id=\"tab-relationships\" class=\"tab-content\">\n");
        sb.append("        <div class=\"table-card\">\n");
        sb.append("            <div class=\"search-box\" style=\"margin-bottom:15px;\"><input type=\"text\" id=\"relSearch\" placeholder=\"Поиск по связям...\" onkeyup=\"filterTable('relTable', 0)\"></div>\n");
        sb.append("            <table id=\"relTable\">\n");
        sb.append("                <thead><tr><th>Откуда (From)</th><th>Тип связи</th><th>Куда (To)</th><th>Файл источника</th></tr></thead>\n");
        sb.append("                <tbody>\n");
        for (Relationship r : analyzer.getRelationships()) {
            sb.append(String.format("                    <tr><td>%s</td><td><span class=\"badge\">%s</span></td><td>%s</td><td>%s</td></tr>\n",
                    escapeHtml(r.getFromEntity()),
                    escapeHtml(r.getType()),
                    escapeHtml(r.getToEntity()),
                    escapeHtml(MermaidExporter.getFileNameOrTarget(r.getFromFile()))));
        }
        sb.append("                </tbody>\n            </table>\n        </div>\n    </main>\n\n");

        // Embed Data and Script
        sb.append("    <script>\n");
        sb.append(getJavascript(flowcharts));
        sb.append("    </script>\n");
        sb.append("</body>\n</html>\n");

        if (outputFile.getParent() != null) Files.createDirectories(outputFile.getParent());
        Files.writeString(outputFile, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String getStyles() {
        return """
        :root {
            --bg-color: #0f172a;
            --card-bg: #1e293b;
            --card-border: #334155;
            --text-color: #f8fafc;
            --text-muted: #94a3b8;
            --primary: #3b82f6;
            --primary-hover: #2563eb;
            --success: #10b981;
            --warning: #f59e0b;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            background-color: var(--bg-color);
            color: var(--text-color);
            line-height: 1.5;
            min-height: 100vh;
        }
        .header {
            padding: 24px 32px;
            background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
            border-bottom: 1px solid var(--card-border);
        }
        .logo {
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 18px;
            letter-spacing: -0.5px;
        }
        .logo span {
            font-size: 14px;
            font-weight: normal;
            background: #2563eb;
            padding: 2px 8px;
            border-radius: 6px;
            vertical-align: middle;
        }
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
            gap: 16px;
        }
        .stat-card {
            background: var(--card-bg);
            border: 1px solid var(--card-border);
            padding: 14px 18px;
            border-radius: 10px;
        }
        .stat-card.highlight {
            border-color: #3b82f6;
            background: #1e3a8a33;
        }
        .stat-card .val {
            font-size: 26px;
            font-weight: 700;
            color: #60a5fa;
        }
        .stat-card.highlight .val {
            color: #34d399;
        }
        .stat-card .lbl {
            font-size: 12px;
            color: var(--text-muted);
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .tabs {
            display: flex;
            background: #1e293b;
            padding: 0 32px;
            border-bottom: 1px solid var(--card-border);
        }
        .tab-btn {
            background: none;
            border: none;
            color: var(--text-muted);
            padding: 14px 20px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            border-bottom: 2px solid transparent;
            transition: all 0.2s;
        }
        .tab-btn:hover { color: var(--text-color); }
        .tab-btn.active {
            color: #60a5fa;
            border-bottom-color: #60a5fa;
        }
        .tab-content { display: none; padding: 24px 32px; }
        .tab-content.active { display: block; }
        .flowchart-layout {
            display: grid;
            grid-template-columns: 320px 1fr;
            gap: 20px;
            height: calc(100vh - 250px);
        }
        .sidebar {
            background: var(--card-bg);
            border: 1px solid var(--card-border);
            border-radius: 12px;
            overflow: hidden;
            display: flex;
            flex-direction: column;
        }
        .search-box {
            padding: 12px;
            background: #0f172a;
            border-bottom: 1px solid var(--card-border);
        }
        .search-box input {
            width: 100%;
            padding: 8px 12px;
            background: #1e293b;
            border: 1px solid var(--card-border);
            border-radius: 6px;
            color: var(--text-color);
            outline: none;
        }
        .method-list {
            list-style: none;
            overflow-y: auto;
            flex: 1;
        }
        .method-list li {
            padding: 10px 16px;
            border-bottom: 1px solid #283548;
            cursor: pointer;
            transition: background 0.15s;
        }
        .method-list li:hover { background: #2a374a; }
        .method-list li.active {
            background: #1e3a8a;
            border-left: 3px solid #60a5fa;
        }
        .method-list .m-name {
            font-size: 14px;
            font-weight: 600;
            color: #e2e8f0;
        }
        .method-list .m-file {
            font-size: 12px;
            color: var(--text-muted);
        }
        .diagram-viewer {
            background: var(--card-bg);
            border: 1px solid var(--card-border);
            border-radius: 12px;
            padding: 20px;
            display: flex;
            flex-direction: column;
            overflow: hidden;
        }
        .viewer-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding-bottom: 16px;
            border-bottom: 1px solid var(--card-border);
        }
        .viewer-header h2 { font-size: 18px; }
        .badge {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
            background: #334155;
            color: #cbd5e1;
            margin-top: 4px;
        }
        .btn {
            background: #2563eb;
            color: white;
            border: none;
            padding: 8px 14px;
            border-radius: 6px;
            cursor: pointer;
            font-size: 13px;
            font-weight: 500;
        }
        .btn:hover { background: var(--primary-hover); }
        .mermaid-container {
            flex: 1;
            overflow: auto;
            padding: 20px;
            display: flex;
            justify-content: center;
            align-items: flex-start;
            background: #090d16;
            border-radius: 8px;
            margin-top: 16px;
        }
        .table-card {
            background: var(--card-bg);
            border: 1px solid var(--card-border);
            border-radius: 12px;
            padding: 20px;
            overflow-x: auto;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 13px;
        }
        th, td {
            padding: 10px 14px;
            text-align: left;
            border-bottom: 1px solid var(--card-border);
        }
        th {
            background: #172033;
            color: var(--text-muted);
            font-weight: 600;
        }
        tr:hover { background: #26334d; }
        .badge-class { background: #1e40af; color: #bfdbfe; }
        .badge-function, .badge-method { background: #065f46; color: #a7f3d0; }
        .badge-include, .badge-import { background: #92400e; color: #fde68a; }
        .badge-struct { background: #6b21a8; color: #e9d5ff; }
        """;
    }

    private static String getJavascript(List<MethodFlowchart> flowcharts) {
        StringBuilder sb = new StringBuilder();
        sb.append("const flowchartsData = [\n");

        for (MethodFlowchart fc : flowcharts) {
            sb.append("    {\n");
            sb.append(String.format("        name: %s,\n", jsonString(fc.getFullName())));
            sb.append(String.format("        file: %s,\n", jsonString(fc.getShortFileName())));
            sb.append(String.format("        lines: 'L%d-%d',\n", fc.getStartLine(), fc.getEndLine()));
            sb.append(String.format("        complexity: %d,\n", fc.getCyclomaticComplexity()));
            sb.append(String.format("        mermaid: %s\n", jsonString(fc.toMermaid())));
            sb.append("    },\n");
        }
        sb.append("];\n\n");

        sb.append("""
        mermaid.initialize({
            startOnLoad: true,
            theme: 'dark',
            securityLevel: 'loose',
            flowchart: { curve: 'basis', htmlLabels: true }
        });

        let currentSelectedIndex = 0;

        function openTab(tabId) {
            document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
            document.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));
            document.getElementById(tabId).classList.add('active');
            event.target.classList.add('active');
        }

        function showArchGraph(graphId) {
            document.getElementById('arch-files').style.display = (graphId === 'arch-files') ? 'flex' : 'none';
            document.getElementById('arch-all').style.display = (graphId === 'arch-all') ? 'flex' : 'none';
            event.target.parentElement.querySelectorAll('.btn').forEach(b => b.classList.remove('active'));
            event.target.classList.add('active');
        }

        async function selectFlowchart(idx) {
            if (idx < 0 || idx >= flowchartsData.length) return;
            currentSelectedIndex = idx;
            const data = flowchartsData[idx];

            document.querySelectorAll('#methodList li').forEach((li, i) => {
                li.classList.toggle('active', i === idx);
            });

            document.getElementById('currentMethodTitle').innerText = data.name;
            document.getElementById('currentMethodMeta').innerText =
                `${data.file} (${data.lines}) | Сложность (ветвления): ${data.complexity}`;

            const container = document.getElementById('flowchartContainer');
            container.innerHTML = '<div class="mermaid" id="mermaidTarget">' + data.mermaid + '</div>';

            try {
                await mermaid.run({ nodes: [document.getElementById('mermaidTarget')] });
            } catch (err) {
                console.error('Mermaid render error:', err);
                container.innerHTML = '<pre style="color:#f87171;padding:10px;">' + data.mermaid + '</pre>';
            }
        }

        function filterMethods() {
            const val = document.getElementById('methodSearch').value.toLowerCase();
            document.querySelectorAll('#methodList li').forEach(li => {
                const name = li.getAttribute('data-name');
                li.style.display = name.includes(val) ? 'block' : 'none';
            });
        }

        function filterTable(tableId, colIdx) {
            const input = event.target.value.toLowerCase();
            const rows = document.querySelectorAll('#' + tableId + ' tbody tr');
            rows.forEach(r => {
                const text = r.children[colIdx].innerText.toLowerCase();
                r.style.display = text.includes(input) ? '' : 'none';
            });
        }

        function copyMermaidCode() {
            if (flowchartsData.length === 0) return;
            const code = flowchartsData[currentSelectedIndex].mermaid;
            navigator.clipboard.writeText(code).then(() => {
                alert('Mermaid-код скопирован в буфер обмена!');
            });
        }

        window.addEventListener('DOMContentLoaded', () => {
            if (flowchartsData.length > 0) {
                selectFlowchart(0);
            }
        });
        """);

        return sb.toString();
    }

    private static String jsonString(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
