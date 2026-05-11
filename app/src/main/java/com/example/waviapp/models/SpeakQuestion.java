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

    private int partNumber;         // Phân loại Part (1-6)
    private boolean showQuestionText; // Hiển thị prompt hay chỉ phát audio
    private Integer audioResId;     // ID của file mp3 trong res/raw (null nếu không có)

    // Constructor cũ cho Practice
    public SpeakQuestion(String instruction, String prompt, String sampleAnswer,
                         int prepTimeSec, int responseTimeSec) {
        this.instruction = instruction;
        this.prompt = prompt;
        this.sampleAnswer = sampleAnswer;
        this.prepTimeSec = prepTimeSec;
        this.responseTimeSec = responseTimeSec;
        this.partNumber = 1;
        this.showQuestionText = true;
        this.audioResId = null;
    }

    // Constructor mới cho Mock Test
    public SpeakQuestion(int partNumber, String instruction, String prompt, String sampleAnswer,
                         int prepTimeSec, int responseTimeSec, boolean showQuestionText, Integer audioResId) {
        this.partNumber = partNumber;
        this.instruction = instruction;
        this.prompt = prompt;
        this.sampleAnswer = sampleAnswer;
        this.prepTimeSec = prepTimeSec;
        this.responseTimeSec = responseTimeSec;
        this.showQuestionText = showQuestionText;
        this.audioResId = audioResId;
    }

    public String getPrompt() { return prompt; }
    public String getInstruction() { return instruction; }
    public String getSampleAnswer() { return sampleAnswer; }
    public int getPrepTimeSec() { return prepTimeSec; }
    public int getResponseTimeSec() { return responseTimeSec; }
    public int getPartNumber() { return partNumber; }
    public boolean isShowQuestionText() { return showQuestionText; }
    public Integer getAudioResId() { return audioResId; }
}
