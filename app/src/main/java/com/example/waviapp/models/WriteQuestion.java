package com.example.waviapp.models;

/**
 * Model cho một bài tập viết (Writing).
 */
public class WriteQuestion {
    private String taskType;        // VD: "Part 1 - Write a Sentence"
    private String instruction;     // Hướng dẫn ngắn
    private String prompt;          // Đề bài (câu, email, chủ đề luận)
    private String keyword1;        // Từ khoá 1 (dùng cho Part 1)
    private String keyword2;        // Từ khoá 2 (dùng cho Part 1)
    private String sampleAnswer;    // Câu trả lời mẫu
    private int minWords;           // Số từ tối thiểu
    private int maxWords;           // Số từ tối đa

    public WriteQuestion(String taskType, String instruction, String prompt,
                         String keyword1, String keyword2,
                         String sampleAnswer, int minWords, int maxWords) {
        this.taskType = taskType;
        this.instruction = instruction;
        this.prompt = prompt;
        this.keyword1 = keyword1;
        this.keyword2 = keyword2;
        this.sampleAnswer = sampleAnswer;
        this.minWords = minWords;
        this.maxWords = maxWords;
    }

    public String getTaskType() { return taskType; }
    public String getInstruction() { return instruction; }
    public String getPrompt() { return prompt; }
    public String getKeyword1() { return keyword1; }
    public String getKeyword2() { return keyword2; }
    public String getSampleAnswer() { return sampleAnswer; }
    public int getMinWords() { return minWords; }
    public int getMaxWords() { return maxWords; }
}
