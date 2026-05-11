package com.example.waviapp.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

/**
 * Tạo 10 đề thi Mock Test và đẩy lên Firestore.
 * Gọi MockTestSeeder.seedAllTests(callback) một lần duy nhất.
 */
public class MockTestSeeder {
    private static final String TAG = "MockTestSeeder";
    private static final String COL = "mock_test_details";

    public interface SeedCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // ====== IMAGE POOLS (Unsplash) ======
    // Speaking Part 2: 20 ảnh (2 per test)
    private static final String[][] SPEAK_IMGS = {
        {"https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=600","https://images.unsplash.com/photo-1600880292203-757bb62b4baf?w=600"},
        {"https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=600","https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=600"},
        {"https://images.unsplash.com/photo-1556761175-5973dc0f32e7?w=600","https://images.unsplash.com/photo-1542744173-8e7e53415bb0?w=600"},
        {"https://images.unsplash.com/photo-1497215842964-222b430dc094?w=600","https://images.unsplash.com/photo-1521737604893-d14cc237f11d?w=600"},
        {"https://images.unsplash.com/photo-1504384308090-c894fdcc538d?w=600","https://images.unsplash.com/photo-1553877522-43269d4ea984?w=600"},
        {"https://images.unsplash.com/photo-1531482615713-2afd69097998?w=600","https://images.unsplash.com/photo-1552581234-26160f608093?w=600"},
        {"https://images.unsplash.com/photo-1560264280-88b68371db39?w=600","https://images.unsplash.com/photo-1515378960530-7c0da6231fb1?w=600"},
        {"https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=600","https://images.unsplash.com/photo-1557804506-669a67965ba0?w=600"},
        {"https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=600","https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=600"},
        {"https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=600","https://images.unsplash.com/photo-1551434678-e076c223a692?w=600"}
    };
    // Writing Part 1: 50 ảnh (5 per test)
    private static final String[][] WRITE_IMGS = {
        {"https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=600","https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600","https://images.unsplash.com/photo-1552664730-d307ca884978?w=600","https://images.unsplash.com/photo-1556910103-1c02745aae4d?w=600","https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=600"},
        {"https://images.unsplash.com/photo-1517048676732-d65bc937f952?w=600","https://images.unsplash.com/photo-1524178232363-1fb2b075b655?w=600","https://images.unsplash.com/photo-1571260899304-425eee4c7efc?w=600","https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=600","https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=600"},
        {"https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=600","https://images.unsplash.com/photo-1559599101-f09722fb4948?w=600","https://images.unsplash.com/photo-1571624436279-b272aff752b5?w=600","https://images.unsplash.com/photo-1466637574441-749b8f19452f?w=600","https://images.unsplash.com/photo-1527529482837-4698179dc6ce?w=600"},
        {"https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=600","https://images.unsplash.com/photo-1581092795360-fd1ca04f0952?w=600","https://images.unsplash.com/photo-1560179707-f14e90ef3623?w=600","https://images.unsplash.com/photo-1485182708500-e8f1f318ba72?w=600","https://images.unsplash.com/photo-1523050854058-8df90110c8f1?w=600"},
        {"https://images.unsplash.com/photo-1551836022-d5d88e9218df?w=600","https://images.unsplash.com/photo-1513258496099-48168024aec0?w=600","https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=600","https://images.unsplash.com/photo-1505236858219-8359eb29e329?w=600","https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=600"},
        {"https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=600","https://images.unsplash.com/photo-1553484771-371a605b060b?w=600","https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600","https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=600","https://images.unsplash.com/photo-1577563908411-5077b6dc7624?w=600"},
        {"https://images.unsplash.com/photo-1504384764586-bb4cdc1707b0?w=600","https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d?w=600","https://images.unsplash.com/photo-1542626991-cbc4e32524cc?w=600","https://images.unsplash.com/photo-1556742393-d75f468bfcb0?w=600","https://images.unsplash.com/photo-1530099486328-e021101a494a?w=600"},
        {"https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=600","https://images.unsplash.com/photo-1590402494682-cd3fb53b1f70?w=600","https://images.unsplash.com/photo-1558618666-fcd25c85f82e?w=600","https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?w=600","https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=600"},
        {"https://images.unsplash.com/photo-1551135049-8a33b5883817?w=600","https://images.unsplash.com/photo-1572021335469-31706a17aaef?w=600","https://images.unsplash.com/photo-1565688534245-05d6b5be184a?w=600","https://images.unsplash.com/photo-1559136555-9303baea8ebd?w=600","https://images.unsplash.com/photo-1535982330050-f1c2fb79ff78?w=600"},
        {"https://images.unsplash.com/photo-1556745757-8d76bdb6984b?w=600","https://images.unsplash.com/photo-1562564055-71e051d33c19?w=600","https://images.unsplash.com/photo-1568992687947-868a62a9f521?w=600","https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=600","https://images.unsplash.com/photo-1561489413-985b06da5bee?w=600"}
    };

    public static void seedAllTests(SeedCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final int[] done = {0};
        final int total = 10;
        for (int i = 0; i < total; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("speaking_questions", buildSpeaking(i));
            data.put("writing_questions", buildWriting(i));
            db.collection(COL).document("Test " + (i + 1)).set(data)
                .addOnSuccessListener(a -> { done[0]++; Log.d(TAG,"Seeded "+done[0]+"/"+total); if(done[0]==total && callback!=null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if(callback!=null) callback.onFailure(e.getMessage()); });
        }
    }

    // ===================== SPEAKING DATA =====================
    private static final String[][] P1_PROMPTS = {
        {"Welcome to Greenwood Shopping Center. We are open from 9 a.m. to 9 p.m. Monday through Saturday. Free parking is available in the underground garage for all shoppers.","Attention all passengers. Flight 302 to Singapore has been delayed due to severe weather. The new departure time is 4:30 p.m."},
        {"Thank you for calling Riverside Hotel. Our check-in time is 3 p.m. and check-out is at noon. For reservations, please press 1.","Good morning everyone. Today's staff meeting will begin at 10 a.m. in Conference Room B. Please bring your quarterly reports."},
        {"Welcome aboard the city tour bus. Our first stop will be the National Museum, followed by the Central Park and the Art Gallery.","Attention shoppers. We will be closing in 30 minutes. Please bring your final purchases to the checkout counter."},
        {"Thank you for visiting Green Valley Clinic. Please have your insurance card ready and sign in at the front desk upon arrival.","Hello and welcome to today's webinar on digital marketing strategies. We will begin the presentation shortly."},
        {"Ladies and gentlemen, welcome to the annual science fair. Exhibits are located on the first and second floors of the convention center.","Good afternoon. This is a reminder that the library will close early today at 5 p.m. due to a special event."},
        {"Welcome to Pacific Airlines. For your safety, please listen carefully to the following instructions before takeoff.","Thank you for joining our cooking class today. Please wash your hands and put on an apron before we begin."},
        {"Attention all employees. The company health screening will take place next Monday in the main lobby from 9 a.m. to 4 p.m.","Good evening and welcome to the Grand Theater. Tonight's performance will begin at 7:30. Please silence your phones."},
        {"Welcome to the Downtown Fitness Center. New members can pick up their access cards at the reception desk on the first floor.","Thank you for calling TechSupport. Your call is important to us. Please hold and an agent will be with you shortly."},
        {"Good morning passengers. Train number 45 to Boston will depart from Platform 3 in approximately 10 minutes.","Welcome to the Sunshine Bakery. Today's special is our freshly baked sourdough bread and homemade croissants."},
        {"Attention visitors. The museum gift shop is now open on the ground floor. We offer a wide selection of books and souvenirs.","Hello everyone. Before we start the workshop, please make sure you have downloaded the required software on your laptop."}
    };

    private static final String[][] P2_DESC = {
        {"A woman typing on a laptop at a desk with coffee","Two men in suits having a meeting in a conference room"},
        {"A group of people working together at a large table","A teacher writing on a whiteboard in a classroom"},
        {"People socializing at an outdoor cafe","Colleagues looking at a computer screen together"},
        {"An open office space with workers at their desks","A team celebrating with high fives"},
        {"A person giving a presentation on stage","Workers collaborating around a laptop"},
        {"A busy restaurant kitchen with chefs cooking","People having a discussion in a modern office"},
        {"A woman working remotely from home","A man explaining a chart on a screen"},
        {"A businessman talking on his phone outdoors","Two colleagues reviewing documents together"},
        {"A scientist working in a laboratory","People attending a technology conference"},
        {"A developer coding at multiple monitors","A team brainstorming with sticky notes on a wall"}
    };

    private static final String[][] P3_TOPICS = {
        {"shopping habits","Where do you usually shop?","What do you buy?","Describe your favorite store."},
        {"travel preferences","How often do you travel?","Where was your last trip?","Describe your ideal vacation."},
        {"work habits","What time do you start work?","Do you prefer working alone or in teams?","Describe your typical workday."},
        {"food and dining","What is your favorite cuisine?","How often do you eat out?","Describe your favorite restaurant."},
        {"exercise routines","What type of exercise do you do?","How often do you exercise?","Describe your fitness routine."},
        {"reading habits","What kind of books do you read?","How much time do you spend reading?","Describe a book you enjoyed."},
        {"technology use","What devices do you use daily?","How has technology changed your life?","Describe your favorite app."},
        {"hobbies","What do you do in your free time?","When did you start this hobby?","Describe why you enjoy it."},
        {"education","What did you study?","What was your favorite subject?","Describe a memorable learning experience."},
        {"movies and entertainment","What type of movies do you like?","How often do you watch movies?","Describe a movie you recommend."}
    };

    private static final String[][] P4_SCHEDULES = {
        {"ANNUAL COMPANY PICNIC","Saturday, June 15","11 AM - 4 PM","Riverside Park","BBQ, volleyball, races"},
        {"SALES CONFERENCE","Monday, March 3","9 AM - 5 PM","Grand Hotel","Keynote, workshops, networking"},
        {"CHARITY RUN","Sunday, April 20","7 AM - 11 AM","Central Park","5K and 10K races, prizes"},
        {"PRODUCT LAUNCH","Friday, May 9","2 PM - 6 PM","Tech Center","Demo, Q&A, reception"},
        {"TRAINING WORKSHOP","Wednesday, July 16","10 AM - 3 PM","Room 204","Leadership skills, lunch provided"},
        {"HEALTH FAIR","Thursday, Aug 21","8 AM - 2 PM","Main Lobby","Free screenings, fitness demos"},
        {"BOOK CLUB MEETING","Tuesday, Sep 9","6 PM - 8 PM","City Library","Discussion, guest author"},
        {"VOLUNTEER DAY","Saturday, Oct 11","9 AM - 1 PM","Community Center","Beach cleanup, tree planting"},
        {"YEAR-END PARTY","Friday, Dec 19","7 PM - 11 PM","Hilton Ballroom","Dinner, awards, live music"},
        {"TECH EXPO","Thursday, Nov 13","10 AM - 6 PM","Convention Hall","AI demos, VR experience, talks"}
    };

    private static final String[] P5_TOPICS = {
        "Should companies allow employees to work from home permanently?",
        "Is it better to have a university degree or work experience?",
        "Should companies invest more in employee training programs?",
        "Is social media beneficial or harmful for business marketing?",
        "Should public transportation be free for all citizens?",
        "Is it better to specialize in one skill or learn many skills?",
        "Should companies provide free meals to their employees?",
        "Is online learning as effective as traditional classroom learning?",
        "Should governments limit working hours to improve quality of life?",
        "Is automation good or bad for the job market?"
    };

    // ===================== WRITING DATA =====================
    private static final String[][] W1_KEYWORDS = {
        {"woman","reading"},{"man","repairing"},{"employees","meeting"},{"chef","preparing"},{"tourists","photograph"},
        {"students","studying"},{"doctor","examining"},{"workers","constructing"},{"children","playing"},{"waiter","serving"},
        {"musician","performing"},{"teacher","explaining"},{"athlete","training"},{"artist","painting"},{"engineer","designing"},
        {"nurse","caring"},{"driver","delivering"},{"farmer","harvesting"},{"pilot","flying"},{"firefighter","rescuing"},
        {"librarian","organizing"},{"photographer","capturing"},{"scientist","researching"},{"baker","baking"},{"guard","patrolling"},
        {"mechanic","fixing"},{"tailor","sewing"},{"florist","arranging"},{"dentist","treating"},{"coach","instructing"},
        {"cashier","scanning"},{"janitor","cleaning"},{"reporter","interviewing"},{"veterinarian","examining"},{"architect","drafting"},
        {"barista","brewing"},{"painter","decorating"},{"plumber","installing"},{"electrician","wiring"},{"pharmacist","dispensing"},
        {"receptionist","greeting"},{"programmer","coding"},{"surgeon","operating"},{"judge","presiding"},{"sailor","navigating"},
        {"translator","interpreting"},{"therapist","counseling"},{"inspector","checking"},{"curator","displaying"},{"conductor","leading"}
    };

    private static final String[][] W2_EMAILS = {
        {"Sarah","Team Building Event","Would you prefer indoor or outdoor? What day works? Any activity suggestions?"},
        {"Mark","Training Feedback","What was most useful? Was it too long? What topics for future?"},
        {"Lisa","Office Renovation","Which area needs improvement? Open plan or cubicles? When should we start?"},
        {"David","Holiday Schedule","When do you want time off? Can you cover Dec 24? Any swap requests?"},
        {"Emily","New Software","Have you tried the new system? Any issues? What features do you like?"},
        {"James","Lunch Policy","Do you prefer catered or delivery? Any dietary restrictions? Budget preference?"},
        {"Anna","Remote Work Survey","How many days remote? What challenges? What tools do you need?"},
        {"Tom","Conference Attendance","Which sessions interest you? Do you need travel support? Share findings?"},
        {"Rachel","Mentorship Program","Would you like a mentor? What skills to develop? How often to meet?"},
        {"Kevin","Sustainability Plan","Ideas for reducing waste? Carpool interest? Energy saving suggestions?"}
    };

    private static final String[] W3_TOPICS = {
        "Technology has made communication easier but less meaningful. Do you agree?",
        "Is it better to work for a large corporation or a small company?",
        "Should companies prioritize profits or social responsibility?",
        "Is remote work the future of employment?",
        "Should university education be free for everyone?",
        "Are robots and AI a threat to human employment?",
        "Should advertising to children be banned?",
        "Is it important for companies to have diverse workforces?",
        "Should governments invest more in renewable energy?",
        "Is experience more valuable than formal education in the workplace?"
    };

    // ===================== BUILD METHODS =====================
    private static List<Map<String,Object>> buildSpeaking(int t) {
        List<Map<String,Object>> list = new ArrayList<>();
        // Part 1: Q1-Q2
        list.add(sq(1,true,"Read the following text aloud.",P1_PROMPTS[t][0],P1_PROMPTS[t][0],45,45,""));
        list.add(sq(1,true,"Read the following text aloud.",P1_PROMPTS[t][1],P1_PROMPTS[t][1],45,45,""));
        // Part 2: Q3-Q4
        list.add(sq(2,false,"Describe the picture in as much detail as possible.","",P2_DESC[t][0],45,30,SPEAK_IMGS[t][0]));
        list.add(sq(2,false,"Describe the picture in as much detail as possible.","",P2_DESC[t][1],45,30,SPEAK_IMGS[t][1]));
        // Part 3: Q5-Q7
        String ctx = "Imagine a marketing firm is researching "+P3_TOPICS[t][0]+". You agreed to a phone interview.";
        list.add(sq(3,false,ctx+"\n\nQ5: "+P3_TOPICS[t][1],P3_TOPICS[t][1],"Sample answer for: "+P3_TOPICS[t][1],3,15,""));
        list.add(sq(3,false,ctx+"\n\nQ6: "+P3_TOPICS[t][2],P3_TOPICS[t][2],"Sample answer for: "+P3_TOPICS[t][2],3,15,""));
        list.add(sq(3,false,ctx+"\n\nQ7: "+P3_TOPICS[t][3],P3_TOPICS[t][3],"Sample answer for: "+P3_TOPICS[t][3],3,30,""));
        // Part 4: Q8-Q10
        String sch = "📋 "+P4_SCHEDULES[t][0]+"\nDate: "+P4_SCHEDULES[t][1]+"\nTime: "+P4_SCHEDULES[t][2]+"\nLocation: "+P4_SCHEDULES[t][3]+"\nActivities: "+P4_SCHEDULES[t][4];
        list.add(sq(4,false,"Use the information provided.\n\n"+sch+"\n\nQ8: When and where is the event?","When and where?","The event is on "+P4_SCHEDULES[t][1]+" at "+P4_SCHEDULES[t][3]+".",45,15,""));
        list.add(sq(4,false,"Use the information provided.\n\n"+sch+"\n\nQ9: What activities are planned?","What activities?","Activities include "+P4_SCHEDULES[t][4]+".",3,15,""));
        list.add(sq(4,false,"Use the information provided.\n\n"+sch+"\n\nQ10: Give me all the details.","All details please.","The "+P4_SCHEDULES[t][0]+" is on "+P4_SCHEDULES[t][1]+", "+P4_SCHEDULES[t][2]+" at "+P4_SCHEDULES[t][3]+". Activities: "+P4_SCHEDULES[t][4]+".",3,30,""));
        // Part 5: Q11
        list.add(sq(5,false,"Express your opinion on the topic below.",P5_TOPICS[t],"In my opinion, this is an important topic that requires careful consideration...",45,60,""));
        return list;
    }

    private static List<Map<String,Object>> buildWriting(int t) {
        List<Map<String,Object>> list = new ArrayList<>();
        int base = t * 5;
        // Part 1: Q1-Q5
        for (int i = 0; i < 5; i++) {
            int idx = base + i;
            list.add(wq(1,"Part 1","Write ONE sentence using the TWO words below.","",W1_KEYWORDS[idx][0],W1_KEYWORDS[idx][1],
                "The "+W1_KEYWORDS[idx][0]+" is "+W1_KEYWORDS[idx][1]+" with great dedication.",5,30,WRITE_IMGS[t][i]));
        }
        // Part 2: Q6-Q7
        for (int i = 0; i < 2; i++) {
            int idx = t * 2 + i;
            String email = "From: "+W2_EMAILS[idx][0]+"\nSubject: "+W2_EMAILS[idx][1]+"\n\n"+W2_EMAILS[idx][2];
            list.add(wq(2,"Part 2","Read the email and respond to ALL questions.",email,"","",
                "Dear "+W2_EMAILS[idx][0]+",\n\nThank you for your email. Here are my responses to your questions...\n\nBest regards",50,150,""));
        }
        // Part 3: Q8
        list.add(wq(3,"Part 3","Write an essay. Give reasons and examples. At least 150 words.",W3_TOPICS[t],"","",
            "This is an important topic in today's society. I believe that...\n\nIn conclusion, careful consideration of multiple perspectives is essential.",150,300,""));
        return list;
    }

    private static Map<String,Object> sq(int p,boolean ra,String inst,String prompt,String ans,int prep,int resp,String img) {
        Map<String,Object> m = new HashMap<>();
        m.put("partNumber",p); m.put("isReadAloud",ra); m.put("instruction",inst);
        m.put("prompt",prompt); m.put("sampleAnswer",ans); m.put("prepTimeSec",prep);
        m.put("responseTimeSec",resp); m.put("imageUrl",img!=null?img:"");
        return m;
    }

    private static Map<String,Object> wq(int p,String type,String inst,String prompt,String k1,String k2,String ans,int min,int max,String img) {
        Map<String,Object> m = new HashMap<>();
        m.put("partNumber",p); m.put("taskType",type); m.put("instruction",inst);
        m.put("prompt",prompt); m.put("keyword1",k1); m.put("keyword2",k2);
        m.put("sampleAnswer",ans); m.put("minWords",min); m.put("maxWords",max);
        m.put("imageUrl",img!=null?img:"");
        return m;
    }
}
