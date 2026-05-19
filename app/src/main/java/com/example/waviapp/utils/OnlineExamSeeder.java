package com.example.waviapp.utils;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Seed dữ liệu thi online lên Firestore:
 *  - 4 kỳ thi với 20–30 câu TOEIC Part 5
 *  - 50 tài khoản giả lập đã thi với điểm ngẫu nhiên
 * Gọi: OnlineExamSeeder.seedIfNeeded(callback)
 */
public class OnlineExamSeeder {

    private static final String TAG = "OnlineExamSeeder";
    private static final String SEED_FLAG_COLLECTION = "seedFlags";
    private static final String SEED_FLAG_DOC = "onlineExamSeeded";

    public interface SeedCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // ===================== ENTRY POINT =====================

    public static void seedIfNeeded(SeedCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(SEED_FLAG_COLLECTION).document(SEED_FLAG_DOC).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && Boolean.TRUE.equals(doc.getBoolean("done"))) {
                        Log.d(TAG, "Đã seed trước đó, bỏ qua.");
                        if (callback != null) callback.onSuccess();
                    } else {
                        Log.d(TAG, "Bắt đầu seed dữ liệu thi online...");
                        seedAllData(db, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Không kiểm tra được flag seed: " + e.getMessage());
                    seedAllData(db, callback);
                });
    }

    // ===================== SEED MAIN =====================

    private static void seedAllData(FirebaseFirestore db, SeedCallback callback) {
        List<ExamData> exams = buildExams();
        AtomicInteger pending = new AtomicInteger(exams.size());

        for (ExamData exam : exams) {
            Map<String, Object> examMap = new HashMap<>();
            examMap.put("title", exam.title);
            examMap.put("description", exam.description);
            examMap.put("totalQuestions", exam.questions.size());
            examMap.put("durationMinutes", exam.durationMinutes);
            examMap.put("startTime", exam.startTime);
            examMap.put("endTime", exam.endTime);
            examMap.put("active", true);
            examMap.put("colorTag", exam.colorTag);
            examMap.put("participantCount", 50);

            db.collection("onlineExams").document(exam.examId)
                    .set(examMap)
                    .addOnSuccessListener(aVoid -> {
                        seedQuestions(db, exam, () -> {
                            seedFakeResults(db, exam, () -> {
                                if (pending.decrementAndGet() == 0) {
                                    markSeedDone(db, callback);
                                }
                            });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi tạo exam: " + e.getMessage());
                        if (pending.decrementAndGet() == 0) {
                            markSeedDone(db, callback);
                        }
                    });
        }
    }

    // ===================== SEED QUESTIONS =====================

    private static void seedQuestions(FirebaseFirestore db, ExamData exam, Runnable onDone) {
        AtomicInteger pending = new AtomicInteger(exam.questions.size());
        for (int i = 0; i < exam.questions.size(); i++) {
            QuestionData q = exam.questions.get(i);
            Map<String, Object> qMap = new HashMap<>();
            qMap.put("questionId", "q" + (i + 1));
            qMap.put("examId", exam.examId);
            qMap.put("questionText", q.text);
            qMap.put("optionA", q.optionA);
            qMap.put("optionB", q.optionB);
            qMap.put("optionC", q.optionC);
            qMap.put("optionD", q.optionD);
            qMap.put("correctAnswer", q.correct);
            qMap.put("order", i + 1);

            db.collection("onlineExams").document(exam.examId)
                    .collection("questions").document("q" + (i + 1))
                    .set(qMap)
                    .addOnCompleteListener(t -> {
                        if (pending.decrementAndGet() == 0) onDone.run();
                    });
        }
    }

    // ===================== SEED FAKE RESULTS (50 users) =====================

    private static final String[] FAKE_NAMES = {
            "Minh Khoa", "Ngọc Linh", "Thanh Tú", "Quốc Hùng", "Bảo Châu",
            "Hoài Nam", "Thu Hương", "Đức Anh", "Kim Ngân", "Trọng Nghĩa",
            "Phương Thảo", "Gia Bảo", "Khánh Linh", "Văn Tùng", "Thùy Dung",
            "Hải Phong", "Ngọc Mai", "Tiến Dũng", "Lan Anh", "Quốc Bảo",
            "Mỹ Hạnh", "Đình Lâm", "Thanh Hằng", "Tuấn Khải", "Thị Thu",
            "Hữu Toàn", "Bích Trâm", "Minh Trang", "Trung Kiên", "Cẩm Vân",
            "Thành Đạt", "Xuân Mai", "Gia Huy", "Hồng Nhung", "Anh Tú",
            "Diệu Linh", "Phi Long", "Ngọc Bích", "Khánh Hòa", "Đức Thịnh",
            "Quỳnh Như", "Văn Khải", "Thái Bình", "Mỹ Duyên", "Khắc Huy",
            "Thục Anh", "Tuấn Minh", "Bảo Ngọc", "Hà Linh", "Việt Anh"
    };

    private static void seedFakeResults(FirebaseFirestore db, ExamData exam, Runnable onDone) {
        Random rnd = new Random();
        int total = exam.questions.size();
        AtomicInteger pending = new AtomicInteger(FAKE_NAMES.length);

        for (int i = 0; i < FAKE_NAMES.length; i++) {
            String fakeUserId = "fake_user_" + (i + 1);
            String name = FAKE_NAMES[i];

            // Điểm phân bổ tự nhiên: trung bình ở 60–80%
            int minScore = (int)(total * 0.4);
            int maxScore = total;
            int score = minScore + rnd.nextInt(maxScore - minScore + 1);

            // Thời gian làm bài: 3–20 phút
            int durationSec = 180 + rnd.nextInt(exam.durationMinutes * 60 - 180);

            // Thời gian nộp bài: ngẫu nhiên trong 7 ngày gần đây
            long now = System.currentTimeMillis();
            long submittedMs = now - (long)(rnd.nextInt(7 * 24 * 60) * 60 * 1000L);
            Timestamp submittedAt = new Timestamp(submittedMs / 1000, 0);

            Map<String, Object> rMap = new HashMap<>();
            rMap.put("userId", fakeUserId);
            rMap.put("displayName", name);
            rMap.put("examId", exam.examId);
            rMap.put("score", score);
            rMap.put("totalQuestions", total);
            rMap.put("durationSeconds", durationSec);
            rMap.put("submittedAt", submittedAt);

            db.collection("onlineExams").document(exam.examId)
                    .collection("results").document(fakeUserId)
                    .set(rMap)
                    .addOnCompleteListener(t -> {
                        if (pending.decrementAndGet() == 0) onDone.run();
                    });
        }
    }

    // ===================== MARK DONE =====================

    private static void markSeedDone(FirebaseFirestore db, SeedCallback callback) {
        Map<String, Object> flag = new HashMap<>();
        flag.put("done", true);
        flag.put("timestamp", Timestamp.now());
        db.collection(SEED_FLAG_COLLECTION).document(SEED_FLAG_DOC)
                .set(flag)
                .addOnCompleteListener(t -> {
                    Log.d(TAG, "Seed hoàn thành!");
                    if (callback != null) callback.onSuccess();
                });
    }

    // ===================== DATA DEFINITIONS =====================

    private static List<ExamData> buildExams() {
        List<ExamData> list = new ArrayList<>();

        // --- Kỳ thi 1: Dragon Quiz ---
        Calendar c1Start = Calendar.getInstance();
        c1Start.add(Calendar.DAY_OF_YEAR, -3);
        Calendar c1End = Calendar.getInstance();
        c1End.add(Calendar.DAY_OF_YEAR, 4);
        list.add(new ExamData(
                "dragon_quiz_s1",
                "🐉 Dragon Quiz Season 1",
                "Kiểm tra từ vựng và ngữ pháp TOEIC cơ bản. Ai đạt điểm cao nhất sẽ nhận gói Premium!",
                20, "#9C27B0",
                new Timestamp(c1Start.getTimeInMillis() / 1000, 0),
                new Timestamp(c1End.getTimeInMillis() / 1000, 0),
                buildDragonQuizQuestions()
        ));

        // --- Kỳ thi 2: TOEIC Readiness Test ---
        Calendar c2Start = Calendar.getInstance();
        c2Start.add(Calendar.DAY_OF_YEAR, -7);
        Calendar c2End = Calendar.getInstance();
        c2End.add(Calendar.DAY_OF_YEAR, 0);
        list.add(new ExamData(
                "toeic_readiness_1",
                "📋 TOEIC Readiness Test #1",
                "Bài kiểm tra đánh giá năng lực TOEIC Reading. 25 câu Part 5 trong 20 phút.",
                25, "#1976D2",
                new Timestamp(c2Start.getTimeInMillis() / 1000, 0),
                new Timestamp(c2End.getTimeInMillis() / 1000, 0),
                buildReadinessTestQuestions()
        ));

        // --- Kỳ thi 3: TOEIC Summit Contest ---
        Calendar c3Start = Calendar.getInstance();
        c3Start.add(Calendar.DAY_OF_YEAR, 2);
        Calendar c3End = Calendar.getInstance();
        c3End.add(Calendar.DAY_OF_YEAR, 9);
        list.add(new ExamData(
                "toeic_summit_s3",
                "🏆 TOEIC Summit Contest S3",
                "Cuộc thi tranh tài TOEIC giữa các học viên. 30 câu hỏi toàn diện trong 25 phút.",
                25, "#D32F2F",
                new Timestamp(c3Start.getTimeInMillis() / 1000, 0),
                new Timestamp(c3End.getTimeInMillis() / 1000, 0),
                buildSummitQuestions()
        ));

        // --- Kỳ thi 4: Quick Vocab Challenge ---
        Calendar c4Start = Calendar.getInstance();
        c4Start.add(Calendar.DAY_OF_YEAR, -1);
        Calendar c4End = Calendar.getInstance();
        c4End.add(Calendar.DAY_OF_YEAR, 6);
        list.add(new ExamData(
                "vocab_challenge_1",
                "⚡ Quick Vocab Challenge",
                "Thử thách từ vựng nhanh! 20 câu trong 15 phút. Tốc độ và độ chính xác đều được tính.",
                15, "#388E3C",
                new Timestamp(c4Start.getTimeInMillis() / 1000, 0),
                new Timestamp(c4End.getTimeInMillis() / 1000, 0),
                buildVocabChallengeQuestions()
        ));

        return list;
    }

    // ===================== QUESTION BANKS =====================

    private static List<QuestionData> buildDragonQuizQuestions() {
        return Arrays.asList(
            new QuestionData("The manager asked all employees to _____ the new safety regulations.", "follow", "following", "followed", "follows", "A"),
            new QuestionData("The company will _____ its annual report next Monday.", "release", "releasing", "released", "releases", "A"),
            new QuestionData("Please submit your application _____ the deadline.", "before", "after", "during", "since", "A"),
            new QuestionData("The new software has been _____ by thousands of users worldwide.", "adopted", "adapt", "adopting", "adoption", "A"),
            new QuestionData("She received a _____ increase in her salary after the performance review.", "significant", "significance", "significantly", "signify", "A"),
            new QuestionData("The board of directors will _____ a new CEO next week.", "appoint", "appointed", "appointing", "appointment", "A"),
            new QuestionData("Our company prides itself _____ delivering high-quality products.", "on", "in", "at", "for", "A"),
            new QuestionData("The project was completed ahead _____ schedule.", "of", "from", "with", "by", "A"),
            new QuestionData("All employees are required to _____ a training session.", "attend", "attending", "attended", "attends", "A"),
            new QuestionData("The merger will _____ place next quarter.", "take", "have", "make", "do", "A"),
            new QuestionData("She is _____ for managing the company's social media accounts.", "responsible", "responsibility", "responsibly", "response", "A"),
            new QuestionData("The conference will be _____ in Seoul this year.", "held", "hold", "holding", "holds", "A"),
            new QuestionData("We need to _____ costs in order to remain competitive.", "reduce", "reducing", "reduced", "reduction", "A"),
            new QuestionData("The new policy will _____ all departments.", "affect", "effect", "affecting", "affected", "A"),
            new QuestionData("The CEO gave a _____ speech at the annual conference.", "motivational", "motivation", "motivate", "motivated", "A"),
            new QuestionData("Our products are _____ available in over 30 countries.", "currently", "current", "currency", "currents", "A"),
            new QuestionData("The team worked _____ to finish the project on time.", "diligently", "diligent", "diligence", "diligents", "A"),
            new QuestionData("The company has _____ its headquarters to a new building.", "moved", "moving", "move", "movement", "A"),
            new QuestionData("Employees are encouraged to _____ feedback to their supervisors.", "provide", "providing", "provided", "provision", "A"),
            new QuestionData("The new regulation _____ companies to disclose financial data.", "requires", "required", "requiring", "requirement", "A")
        );
    }

    private static List<QuestionData> buildReadinessTestQuestions() {
        return Arrays.asList(
            new QuestionData("Despite the heavy rain, the construction _____ continued on schedule.", "work", "working", "worked", "works", "A"),
            new QuestionData("The sales team _____ their quarterly target by 20%.", "exceeded", "exceed", "exceeding", "exceeds", "A"),
            new QuestionData("All participants must _____ a non-disclosure agreement before the meeting.", "sign", "signing", "signed", "signs", "A"),
            new QuestionData("The factory will be temporarily _____ for renovation.", "closed", "close", "closing", "closure", "A"),
            new QuestionData("We appreciate your _____ to our customer service team.", "feedback", "feedbacks", "feed", "feeding", "A"),
            new QuestionData("The new hire has quickly _____ to the company culture.", "adapted", "adopt", "adapting", "adaptation", "A"),
            new QuestionData("Marketing campaigns should be _____ to the target audience.", "tailored", "tailor", "tailoring", "tailors", "A"),
            new QuestionData("The quarterly report will be _____ to all shareholders.", "distributed", "distribute", "distributing", "distribution", "A"),
            new QuestionData("_____ completing the training, employees will receive a certificate.", "After", "Before", "During", "While", "A"),
            new QuestionData("The presentation was _____ by the CEO personally.", "delivered", "deliver", "delivering", "delivery", "A"),
            new QuestionData("Due to high demand, the product is temporarily _____ of stock.", "out", "off", "in", "at", "A"),
            new QuestionData("The company plans to _____ its operations to Asia next year.", "expand", "expanding", "expanded", "expansion", "A"),
            new QuestionData("She has been with the firm _____ over ten years.", "for", "since", "during", "within", "A"),
            new QuestionData("The new equipment will _____ productivity significantly.", "boost", "boosting", "boosted", "boosts", "A"),
            new QuestionData("All invoices must be _____ within 30 days of receipt.", "paid", "pay", "paying", "payment", "A"),
            new QuestionData("The workshop is designed _____ new managers.", "for", "to", "with", "by", "A"),
            new QuestionData("The employee handbook has been _____ to reflect new policies.", "updated", "update", "updating", "updates", "A"),
            new QuestionData("We _____ that all guests register upon arrival.", "request", "requesting", "requested", "requests", "A"),
            new QuestionData("The seminar covered a wide _____ of topics.", "range", "ranges", "ranging", "ranged", "A"),
            new QuestionData("Employees should _____ their supervisors before taking leave.", "notify", "notifying", "notified", "notification", "A"),
            new QuestionData("The annual budget _____ has been approved by the board.", "proposal", "propose", "proposing", "proposed", "A"),
            new QuestionData("All equipment should be _____ properly after use.", "stored", "store", "storing", "storage", "A"),
            new QuestionData("The company _____ a new employee wellness program last month.", "launched", "launch", "launching", "launches", "A"),
            new QuestionData("The renovation project is expected to be _____ by December.", "completed", "complete", "completing", "completion", "A"),
            new QuestionData("Please _____ your schedule if you wish to attend the event.", "confirm", "confirmed", "confirming", "confirmation", "A")
        );
    }

    private static List<QuestionData> buildSummitQuestions() {
        return Arrays.asList(
            new QuestionData("The committee will _____ the proposal at its next meeting.", "review", "reviewing", "reviewed", "reviews", "A"),
            new QuestionData("The company _____ to improve customer satisfaction this year.", "aims", "aimed", "aiming", "aim", "A"),
            new QuestionData("The department head sent _____ to all staff about the changes.", "a memo", "an order", "a payment", "a result", "A"),
            new QuestionData("Our customer service team is available _____ the clock.", "around", "for", "at", "by", "A"),
            new QuestionData("The _____ of the new branch was celebrated with a grand opening.", "launch", "launching", "launched", "launches", "A"),
            new QuestionData("Employees are expected to behave _____ at all times.", "professionally", "professional", "profession", "professions", "A"),
            new QuestionData("The updated guidelines are _____ on the company intranet.", "available", "availability", "avail", "availed", "A"),
            new QuestionData("The CEO stressed the _____ of meeting project deadlines.", "importance", "important", "importantly", "import", "A"),
            new QuestionData("We were pleased _____ the results of the customer survey.", "with", "for", "at", "on", "A"),
            new QuestionData("The _____ team will coordinate all logistics for the event.", "organizing", "organized", "organize", "organization", "A"),
            new QuestionData("The shipping company _____ delivery within two business days.", "guarantees", "guarantee", "guaranteeing", "guaranteed", "A"),
            new QuestionData("A new coffee shop has _____ near our office building.", "opened", "open", "opening", "opens", "A"),
            new QuestionData("Staff members should _____ any suspicious activity immediately.", "report", "reporting", "reported", "reports", "A"),
            new QuestionData("The recruitment process is _____ to all qualified applicants.", "open", "opening", "opened", "opens", "A"),
            new QuestionData("The company _____ a partnership with a leading tech firm.", "announced", "announce", "announcing", "announcement", "A"),
            new QuestionData("The sales figures _____ a significant improvement over last quarter.", "show", "showed", "showing", "shown", "A"),
            new QuestionData("Employees who work overtime are _____ additional compensation.", "entitled to", "responsible for", "interested in", "capable of", "A"),
            new QuestionData("The marketing _____ has been approved and will begin next month.", "campaign", "campaigned", "campaigning", "campaigns", "A"),
            new QuestionData("All team members should _____ the weekly progress meeting.", "attend", "attending", "attended", "attends", "A"),
            new QuestionData("The firm has _____ its commitment to sustainable practices.", "reaffirmed", "reaffirm", "reaffirming", "reaffirmation", "A"),
            new QuestionData("The client _____ three revisions to the original design.", "requested", "request", "requesting", "requests", "A"),
            new QuestionData("Our profits have grown _____ over the past five years.", "steadily", "steady", "steadiness", "steadied", "A"),
            new QuestionData("The training program is _____ for all new employees.", "mandatory", "mandate", "mandating", "mandated", "A"),
            new QuestionData("The company will _____ its pricing strategy based on market conditions.", "adjust", "adjusting", "adjusted", "adjustment", "A"),
            new QuestionData("The office renovation is _____ on schedule and will finish by Friday.", "on", "ahead", "behind", "within", "A")
        );
    }

    private static List<QuestionData> buildVocabChallengeQuestions() {
        return Arrays.asList(
            new QuestionData("The shipment was _____ due to bad weather conditions.", "delayed", "delay", "delaying", "delays", "A"),
            new QuestionData("Please _____ your badge at all times while on the premises.", "wear", "wearing", "worn", "wears", "A"),
            new QuestionData("The audit revealed several _____ in the accounting system.", "discrepancies", "discrepancy", "discrepant", "discrepantly", "A"),
            new QuestionData("Her _____ performance earned her a promotion.", "outstanding", "outstand", "outstandingly", "outstandingness", "A"),
            new QuestionData("The vendor agreed to _____ a 10% discount on bulk orders.", "offer", "offering", "offered", "offers", "A"),
            new QuestionData("The contract was _____ by both parties last Tuesday.", "signed", "sign", "signing", "signable", "A"),
            new QuestionData("All submissions must _____ with the formatting guidelines.", "comply", "complying", "complied", "compliance", "A"),
            new QuestionData("The company _____ over 500 employees across three countries.", "employs", "employed", "employing", "employment", "A"),
            new QuestionData("She _____ as the keynote speaker at the annual conference.", "served", "serve", "serving", "service", "A"),
            new QuestionData("The new product line will _____ next spring.", "launch", "launching", "launched", "launches", "A"),
            new QuestionData("The CEO was _____ about the company's future prospects.", "optimistic", "optimism", "optimistically", "optimize", "A"),
            new QuestionData("Please _____ the attached document for your records.", "retain", "retaining", "retained", "retention", "A"),
            new QuestionData("The company made a _____ decision to invest in renewable energy.", "strategic", "strategy", "strategically", "strategize", "A"),
            new QuestionData("The project manager _____ weekly meetings to track progress.", "conducts", "conduct", "conducting", "conducted", "A"),
            new QuestionData("Employees are _____ to take at least one training course per year.", "encouraged", "encourage", "encouraging", "encouragement", "A"),
            new QuestionData("The office is _____ with the latest technology.", "equipped", "equip", "equipping", "equipment", "A"),
            new QuestionData("The results _____ the effectiveness of the new strategy.", "demonstrate", "demonstrating", "demonstrated", "demonstration", "A"),
            new QuestionData("She was _____ for her contributions to the research team.", "recognized", "recognize", "recognizing", "recognition", "A"),
            new QuestionData("The company has a strong _____ in the Asian market.", "presence", "present", "presenting", "presented", "A"),
            new QuestionData("All department heads must _____ their budgets by end of quarter.", "finalize", "finalizing", "finalized", "finalization", "A")
        );
    }

    // ===================== INNER CLASSES =====================

    private static class ExamData {
        String examId, title, description, colorTag;
        int durationMinutes;
        Timestamp startTime, endTime;
        List<QuestionData> questions;

        ExamData(String examId, String title, String description,
                 int durationMinutes, String colorTag,
                 Timestamp startTime, Timestamp endTime,
                 List<QuestionData> questions) {
            this.examId = examId;
            this.title = title;
            this.description = description;
            this.durationMinutes = durationMinutes;
            this.colorTag = colorTag;
            this.startTime = startTime;
            this.endTime = endTime;
            this.questions = questions;
        }
    }

    private static class QuestionData {
        String text, optionA, optionB, optionC, optionD, correct;

        QuestionData(String text, String optionA, String optionB,
                     String optionC, String optionD, String correct) {
            this.text = text;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.correct = correct;
        }
    }
}
