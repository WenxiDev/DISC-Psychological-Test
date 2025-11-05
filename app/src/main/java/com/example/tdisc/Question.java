package com.example.tdisc;

public class Question {

    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String key;
    private char dominance;
    private char influence;
    private char compliance;
    private char steadiness;

    public Question(String question, String optionA, String optionB, String optionC, String optionD, char dominance, char influence, char compliance, char steadiness) {
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.dominance = dominance;
        this.influence = influence;
        this.compliance = compliance;
        this.steadiness = steadiness;
    }

    public String getQuestion() {
        return question;
    }

    public String getOptionA() {
        return optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public char getDominance() {
        return dominance;
    }

    public char getInfluence() {
        return influence;
    }

    public char getCompliance() {
        return compliance;
    }

    public char getSteadiness() {
        return steadiness;
    }

}
