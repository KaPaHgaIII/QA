package ru.kapahgaiii.qa.dto;

public class QuestionInfo {

    private int questionId;

    private String type;

    private int value;

    public QuestionInfo() {
    }

    public QuestionInfo(int questionId, String type, int value) {
        this.questionId = questionId;
        this.type = type;
        this.value = value;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
