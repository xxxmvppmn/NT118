package com.example.waviapp.models;

/**
 * Model cho một câu hỏi luyện nói (Speaking).
 * Hỗ trợ cả Practice mode (local) và Mock Test mode (Firestore).
 */
public class SpeakQuestion {
    private String prompt;          // Văn bản / câu hỏi cần đọc/trả lời
    private String instruction;     // Hướng dẫn ngắn (VD: "Read the following text aloud")
    private String sampleAnswer;    // Câu trả lời mẫu
    private int prepTimeSec;        // Thời gian chuẩn bị (giây)
    private int responseTimeSec;    // Thời gian trả lời (giây)
    private int partNumber;         // Phân loại Part (1-5)
    private String imageUrl;        // URL ảnh từ Firebase Storage (null/rỗng nếu không có)
    private boolean isReadAloud;    // Cờ đánh dấu để TTS tự đọc Prompt (Part 1)

    // --- Giữ lại trường cũ để backward-compatible với Practice mode ---
    private Integer imageResId;

    // Constructor rỗng (BẮT BUỘC cho Firestore deserialization)
    public SpeakQuestion() {}

    // Constructor cho Practice mode (không đổi)
    public SpeakQuestion(String instruction, String prompt, String sampleAnswer,
                         int prepTimeSec, int responseTimeSec) {
        this.instruction = instruction;
        this.prompt = prompt;
        this.sampleAnswer = sampleAnswer;
        this.prepTimeSec = prepTimeSec;
        this.responseTimeSec = responseTimeSec;
        this.partNumber = 1;
        this.imageResId = null;
        this.imageUrl = null;
        this.isReadAloud = false;
    }

    // Constructor cho Mock Test (local hardcode - backward compatible)
    public SpeakQuestion(int partNumber, String instruction, String prompt, String sampleAnswer,
                         int prepTimeSec, int responseTimeSec, boolean isReadAloud, Integer imageResId) {
        this.partNumber = partNumber;
        this.instruction = instruction;
        this.prompt = prompt;
        this.sampleAnswer = sampleAnswer;
        this.prepTimeSec = prepTimeSec;
        this.responseTimeSec = responseTimeSec;
        this.isReadAloud = isReadAloud;
        this.imageResId = imageResId;
        this.imageUrl = null;
    }

    // Constructor cho Mock Test từ Firestore (dùng imageUrl)
    public SpeakQuestion(int partNumber, String instruction, String prompt, String sampleAnswer,
                         int prepTimeSec, int responseTimeSec, boolean isReadAloud, String imageUrl) {
        this.partNumber = partNumber;
        this.instruction = instruction;
        this.prompt = prompt;
        this.sampleAnswer = sampleAnswer;
        this.prepTimeSec = prepTimeSec;
        this.responseTimeSec = responseTimeSec;
        this.isReadAloud = isReadAloud;
        this.imageUrl = imageUrl;
        this.imageResId = null;
    }

    // Getters
    public String getPrompt() { return prompt; }
    public String getInstruction() { return instruction; }
    public String getSampleAnswer() { return sampleAnswer; }
    public int getPrepTimeSec() { return prepTimeSec; }
    public int getResponseTimeSec() { return responseTimeSec; }
    public int getPartNumber() { return partNumber; }
    public Integer getImageResId() { return imageResId; }
    public String getImageUrl() { return imageUrl; }
    public boolean isReadAloud() { return isReadAloud; }

    // Setters (cần cho Firestore deserialization)
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
    public void setSampleAnswer(String sampleAnswer) { this.sampleAnswer = sampleAnswer; }
    public void setPrepTimeSec(int prepTimeSec) { this.prepTimeSec = prepTimeSec; }
    public void setResponseTimeSec(int responseTimeSec) { this.responseTimeSec = responseTimeSec; }
    public void setPartNumber(int partNumber) { this.partNumber = partNumber; }
    public void setImageResId(Integer imageResId) { this.imageResId = imageResId; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setReadAloud(boolean readAloud) { isReadAloud = readAloud; }
}
