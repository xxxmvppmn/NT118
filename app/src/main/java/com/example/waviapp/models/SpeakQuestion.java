package com.example.waviapp.models;

/**
 * Model cho một câu hỏi luyện nói (Speaking).
 */
public class SpeakQuestion {
    private String prompt;          // Văn bản / câu hỏi cần đọc/trả lời
    private String instruction;     // Hướng dẫn ngắn (VD: "Read the following text aloud")
    private String sampleAnswer;    // Câu trả lời mẫu (TTS sẽ đọc cái này)
    private int prepTimeSec;        // Thời gian chuẩn bị (giây)
    private int responseTimeSec;    // Thời gian trả lời (giây)

    public SpeakQuestion(String instruction, String prompt, String sampleAnswer,
                         int prepTimeSec, int responseTimeSec) {
        this.instruction = instruction;
        this.prompt = prompt;
        this.sampleAnswer = sampleAnswer;
        this.prepTimeSec = prepTimeSec;
        this.responseTimeSec = responseTimeSec;
    }

    public String getPrompt() { return prompt; }
    public String getInstruction() { return instruction; }
    public String getSampleAnswer() { return sampleAnswer; }
    public int getPrepTimeSec() { return prepTimeSec; }
    public int getResponseTimeSec() { return responseTimeSec; }
}
