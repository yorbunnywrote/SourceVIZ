package com.sourceviz.model;

public enum FlowNodeType {
    START("Начало"),
    END("Конец / Return"),
    PROCESS("Действие"),
    CONDITION("Условие (if)"),
    LOOP_CONDITION("Условие цикла (while/for)"),
    INPUT_OUTPUT("Ввод / Вывод"),
    SUBROUTINE("Вызов функции"),
    MERGE("Слияние веток");

    private final String title;

    FlowNodeType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
