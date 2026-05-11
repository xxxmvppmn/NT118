package com.example.waviapp.models;

/**
 * Model cho một bài tập viết (Writing).
 * Hỗ trợ cả Practice mode (local) và Mock Test mode (Firestore).
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
    private int partNumber;         // Phân loại Part (1-3)
    private String imageUrl;        // URL ảnh từ Firebase Storage (null/rỗng nếu không có)

    // --- Giữ lại trường cũ để backward-compatible với Practice mode ---
    private Integer imageResId;

    // Constructor rỗng (BẮT BUỘC cho Firestore deserialization)
    public WriteQuestion() {}

    // Constructor cho Practice mode (không đổi)
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
        this.partNumber = 1;
        this.imageResId = null;
        this.imageUrl = null;
    }

    // Constructor cho Mock Test (local hardcode - backward compatible)
    public WriteQuestion(int partNumber, String taskType, String instruction, String prompt,
                         String keyword1, String keyword2, String sampleAnswer,
                         int minWords, int maxWords, Integer imageResId) {
        this.partNumber = partNumber;
        this.taskType = taskType;
        this.instruction = instruction;
        this.prompt = prompt;
        this.keyword1 = keyword1;
        this.keyword2 = keyword2;
        this.sampleAnswer = sampleAnswer;
        this.minWords = minWords;
        this.maxWords = maxWords;
        this.imageResId = imageResId;
        this.imageUrl = imageResId != null ? "" : null;
    }

    // Constructor cho Mock Test từ Firestore (dùng imageUrl)
    public WriteQuestion(int partNumber, String taskType, String instruction, String prompt,
                         String keyword1, String keyword2, String sampleAnswer,
                         int minWords, int maxWords, String imageUrl) {
        this.partNumber = partNumber;
        this.taskType = taskType;
        this.instruction = instruction;
        this.prompt = prompt;
        this.keyword1 = keyword1;
        this.keyword2 = keyword2;
        this.sampleAnswer = sampleAnswer;
        this.minWords = minWords;
        this.maxWords = maxWords;
        this.imageUrl = imageUrl;
        this.imageResId = null;
    }

    // Getters
    public String getTaskType() { return taskType; }
    public String getInstruction() { return instruction; }
    public String getPrompt() { return prompt; }
    public String getKeyword1() { return keyword1; }
    public String getKeyword2() { return keyword2; }
    public String getSampleAnswer() { return sampleAnswer; }
    public int getMinWords() { return minWords; }
    public int getMaxWords() { return maxWords; }
    public int getPartNumber() { return partNumber; }
    public Integer getImageResId() { return imageResId; }
    public String getImageUrl() { return imageUrl; }

    // Setters (cần cho Firestore deserialization)
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public void setKeyword1(String keyword1) { this.keyword1 = keyword1; }
    public void setKeyword2(String keyword2) { this.keyword2 = keyword2; }
    public void setSampleAnswer(String sampleAnswer) { this.sampleAnswer = sampleAnswer; }
    public void setMinWords(int minWords) { this.minWords = minWords; }
    public void setMaxWords(int maxWords) { this.maxWords = maxWords; }
    public void setPartNumber(int partNumber) { this.partNumber = partNumber; }
    public void setImageResId(Integer imageResId) { this.imageResId = imageResId; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
