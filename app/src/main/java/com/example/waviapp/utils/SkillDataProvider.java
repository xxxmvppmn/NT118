package com.example.waviapp.utils;

import com.example.waviapp.models.SpeakQuestion;
import com.example.waviapp.models.WriteQuestion;

import java.util.ArrayList;
import java.util.List;

/**
 * Cung cấp dữ liệu mock cho Speaking và Writing.
 */
public class SkillDataProvider {

    // =========================================================
    // SPEAKING DATA (6 parts, mỗi part 5 câu)
    // =========================================================

    public static List<SpeakQuestion> getSpeakQuestions(int partIndex) {
        switch (partIndex) {
            case 0: return getSpeakPart1();
            case 1: return getSpeakPart2();
            case 2: return getSpeakPart3();
            case 3: return getSpeakPart4();
            case 4: return getSpeakPart5();
            case 5: return getSpeakPart6();
            default: return getSpeakPart1();
        }
    }

    /** Part 1: Read a Text Aloud (45 giây đọc) */
    private static List<SpeakQuestion> getSpeakPart1() {
        List<SpeakQuestion> list = new ArrayList<>();
        list.add(new SpeakQuestion(
            "Read the following text aloud.",
            "Welcome to Greenwood Shopping Center.\nWe are open from 9 a.m. to 9 p.m., Monday through Saturday.\nOn Sundays, we open at 10 a.m. and close at 6 p.m.\nFree parking is available in Lots A and B.",
            "Welcome to Greenwood Shopping Center. We are open from 9 a.m. to 9 p.m., Monday through Saturday. On Sundays, we open at 10 a.m. and close at 6 p.m. Free parking is available in Lots A and B.",
            45, 45
        ));
        list.add(new SpeakQuestion(
            "Read the following text aloud.",
            "Attention, all passengers.\nFlight 302 to Singapore has been delayed by one hour.\nThe new departure time is 4:30 p.m.\nPlease proceed to Gate 12 for further updates.\nWe apologize for any inconvenience.",
            "Attention, all passengers. Flight 302 to Singapore has been delayed by one hour. The new departure time is 4:30 p.m. Please proceed to Gate 12 for further updates. We apologize for any inconvenience.",
            45, 45
        ));
        list.add(new SpeakQuestion(
            "Read the following text aloud.",
            "Thank you for calling Riverdale Bank.\nIf you wish to check your account balance, press 1.\nTo report a lost or stolen card, press 2.\nFor loan inquiries, press 3.\nTo speak with a customer representative, please hold.",
            "Thank you for calling Riverdale Bank. If you wish to check your account balance, press 1. To report a lost or stolen card, press 2. For loan inquiries, press 3. To speak with a customer representative, please hold.",
            45, 45
        ));
        list.add(new SpeakQuestion(
            "Read the following text aloud.",
            "The annual company picnic will be held on Saturday, July 15th.\nThe event takes place at Riverside Park from noon until 5 p.m.\nAll employees and their families are welcome.\nFood and beverages will be provided.\nPlease RSVP by July 8th.",
            "The annual company picnic will be held on Saturday, July 15th. The event takes place at Riverside Park from noon until 5 p.m. All employees and their families are welcome. Food and beverages will be provided. Please RSVP by July 8th.",
            45, 45
        ));
        list.add(new SpeakQuestion(
            "Read the following text aloud.",
            "Harrington City Library is pleased to announce extended hours.\nStarting next Monday, we will be open until 9 p.m. on weekdays.\nWeekend hours remain the same: 10 a.m. to 5 p.m.\nMembership is free for all city residents.",
            "Harrington City Library is pleased to announce extended hours. Starting next Monday, we will be open until 9 p.m. on weekdays. Weekend hours remain the same: 10 a.m. to 5 p.m. Membership is free for all city residents.",
            45, 45
        ));
        return list;
    }

    /** Part 2: Describe a Picture (30 giây chuẩn bị, 45 giây nói) */
    private static List<SpeakQuestion> getSpeakPart2() {
        List<SpeakQuestion> list = new ArrayList<>();
        list.add(new SpeakQuestion(
            "You will describe the picture.\nDescribe what you see in as much detail as possible.",
            "🖼️ [Picture: A woman is working at a desk in an office. She is using a laptop and has several documents around her. There is a cup of coffee next to the laptop.]",
            "In this picture, I can see a woman working at a desk in what appears to be an office environment. She is focused on her laptop computer. There are several documents spread around her workspace. A cup of coffee is placed next to her laptop, suggesting she has been working for some time.",
            30, 45
        ));
        list.add(new SpeakQuestion(
            "You will describe the picture.\nDescribe what you see in as much detail as possible.",
            "🖼️ [Picture: Two men are having a meeting in a conference room. One is pointing at a whiteboard with charts, and the other is taking notes in a notebook.]",
            "This picture shows two men in a conference room. One man is standing and pointing at a whiteboard that displays several charts and graphs. The other man is sitting at the table and taking notes in a notebook. They appear to be having a business meeting.",
            30, 45
        ));
        list.add(new SpeakQuestion(
            "You will describe the picture.\nDescribe what you see in as much detail as possible.",
            "🖼️ [Picture: A busy street market in the morning. Vendors are arranging fresh vegetables and fruits on their stalls. Several customers are browsing.]",
            "This picture depicts a busy street market, likely in the morning. Various vendors are arranging fresh vegetables and fruits on their stalls. Several customers are walking through the market and browsing the products. The scene appears lively and colorful.",
            30, 45
        ));
        list.add(new SpeakQuestion(
            "You will describe the picture.\nDescribe what you see in as much detail as possible.",
            "🖼️ [Picture: A group of people are exercising in a park. Some are jogging on a path, others are doing stretches on the grass. Trees are visible in the background.]",
            "In this picture, I can see a group of people exercising outdoors in a park. Some individuals are jogging along a path, while others are doing stretching exercises on the grass. The background shows several tall trees, creating a pleasant natural setting.",
            30, 45
        ));
        list.add(new SpeakQuestion(
            "You will describe the picture.\nDescribe what you see in as much detail as possible.",
            "🖼️ [Picture: A chef is cooking in a restaurant kitchen. He is stirring a large pot on a gas stove. Kitchen utensils and ingredients are visible around him.]",
            "This picture shows a chef working in a professional restaurant kitchen. He is standing at a gas stove, stirring a large pot. Various kitchen utensils are hanging nearby, and different ingredients are arranged around the cooking area. He appears to be focused on preparing a dish.",
            30, 45
        ));
        return list;
    }

    /** Part 3: Respond to Questions (15 giây chuẩn bị, 15–30 giây trả lời) */
    private static List<SpeakQuestion> getSpeakPart3() {
        List<SpeakQuestion> list = new ArrayList<>();
        list.add(new SpeakQuestion(
            "You are being interviewed about your shopping habits.\nAnswer the following question.",
            "Where do you usually do your grocery shopping, and why do you prefer that place?",
            "I usually do my grocery shopping at the local supermarket near my home. I prefer it because it has a wide variety of products and the prices are reasonable. It is also conveniently located, so I can stop by on my way home from work.",
            15, 30
        ));
        list.add(new SpeakQuestion(
            "You are being interviewed about transportation.\nAnswer the following question.",
            "How do you typically commute to work or school? What do you like or dislike about it?",
            "I usually commute to work by bus. I like it because I don't have to worry about traffic or parking. However, sometimes the bus can be crowded during rush hours, which can be uncomfortable. Overall, I think it is an economical and environmentally friendly option.",
            15, 30
        ));
        list.add(new SpeakQuestion(
            "You are being interviewed about your free time activities.\nAnswer the following question.",
            "What do you enjoy doing in your free time? How often do you do it?",
            "In my free time, I enjoy reading books and watching documentaries. I try to read for at least thirty minutes every evening before bed. On weekends, I sometimes watch documentary films about nature or history. These activities help me relax and learn new things.",
            15, 30
        ));
        list.add(new SpeakQuestion(
            "You are being interviewed about eating habits.\nAnswer the following question.",
            "Do you prefer cooking at home or eating at restaurants? Please explain your preference.",
            "I prefer cooking at home most of the time. It allows me to control the ingredients and make healthier meals. It is also more economical than eating out. However, I do enjoy going to restaurants occasionally, especially when I want to try new cuisines or celebrate special occasions.",
            15, 30
        ));
        list.add(new SpeakQuestion(
            "You are being interviewed about technology.\nAnswer the following question.",
            "How has technology changed the way you work or study? Give a specific example.",
            "Technology has significantly changed the way I work. For example, I now use cloud-based applications to collaborate with my colleagues remotely. We can share documents and edit them simultaneously without being in the same location. This has made teamwork much more efficient and flexible.",
            15, 30
        ));
        return list;
    }

    /** Part 4: Respond to Questions Using Information Provided (30 giây chuẩn bị, 30 giây) */
    private static List<SpeakQuestion> getSpeakPart4() {
        List<SpeakQuestion> list = new ArrayList<>();
        list.add(new SpeakQuestion(
            "Use the schedule below to answer the question.\n\n📋 SCHEDULE: City Library Workshop\n• Date: Saturday, March 10\n• Time: 10 AM – 12 PM\n• Location: Room 204, 2nd Floor\n• Fee: Free for members / $5 for non-members\n• Registration: Required by March 7",
            "When is the registration deadline for the workshop?",
            "According to the schedule, the registration deadline for the City Library Workshop is March 7th. This is three days before the event, which takes place on Saturday, March 10th.",
            30, 30
        ));
        list.add(new SpeakQuestion(
            "Use the schedule below to answer the question.\n\n📋 SCHEDULE: City Library Workshop\n• Date: Saturday, March 10\n• Time: 10 AM – 12 PM\n• Location: Room 204, 2nd Floor\n• Fee: Free for members / $5 for non-members\n• Registration: Required by March 7",
            "How much does it cost for a non-member to attend the workshop?",
            "Based on the schedule, it costs five dollars for a non-member to attend the City Library Workshop. Library members, however, can attend free of charge.",
            30, 30
        ));
        list.add(new SpeakQuestion(
            "Use the schedule below to answer the question.\n\n📋 MENU: Blue Harbor Café\n• Breakfast (7–11 AM): Pancakes $8, Eggs Benedict $10, Oatmeal $6\n• Lunch (11 AM–3 PM): Grilled Salmon $18, Caesar Salad $12, Pasta $14\n• Dinner (5–9 PM): Steak $28, Seafood Platter $32, Vegetarian Set $20\n• Drinks: Coffee $4, Fresh Juice $5, Smoothie $6",
            "What time does the lunch menu start, and what is the cheapest option?",
            "The lunch menu at Blue Harbor Café starts at 11 a.m. and runs until 3 p.m. The cheapest lunch option is the Caesar Salad, which is priced at twelve dollars.",
            30, 30
        ));
        list.add(new SpeakQuestion(
            "Use the schedule below to answer the question.\n\n📋 MENU: Blue Harbor Café\n• Breakfast (7–11 AM): Pancakes $8, Eggs Benedict $10, Oatmeal $6\n• Lunch (11 AM–3 PM): Grilled Salmon $18, Caesar Salad $12, Pasta $14\n• Dinner (5–9 PM): Steak $28, Seafood Platter $32, Vegetarian Set $20\n• Drinks: Coffee $4, Fresh Juice $5, Smoothie $6",
            "A customer wants a vegetarian dinner option. What would you recommend and how much does it cost?",
            "For a customer who prefers vegetarian options, I would recommend the Vegetarian Set from the dinner menu. It is priced at twenty dollars and is available from 5 p.m. to 9 p.m.",
            30, 30
        ));
        list.add(new SpeakQuestion(
            "Use the schedule below to answer the question.\n\n📋 SCHEDULE: Northside Gym\n• Mon / Wed / Fri: Yoga (6 AM), Cycling (7 AM), Pilates (8 AM)\n• Tue / Thu: Strength Training (6 AM), Zumba (7 AM)\n• Sat: Boot Camp (8 AM), Swimming (10 AM)\n• Sun: CLOSED\n• Membership: $50/month or $500/year",
            "What classes are offered on Saturdays, and what time do they start?",
            "According to the gym schedule, two classes are offered on Saturdays. The first is Boot Camp, which starts at 8 a.m., and the second is Swimming, which begins at 10 a.m. The gym is closed on Sundays.",
            30, 30
        ));
        return list;
    }

    /** Part 5: Propose a Solution (20 giây chuẩn bị, 60 giây nói) */
    private static List<SpeakQuestion> getSpeakPart5() {
        List<SpeakQuestion> list = new ArrayList<>();
        list.add(new SpeakQuestion(
            "Listen to the problem and propose a solution.\nYou have 20 seconds to prepare and 60 seconds to respond.",
            "🎧 [Voice message from a colleague]\n\"Hi, I'm calling because we have a problem. Our main presenter for tomorrow's client meeting just called in sick. The presentation is scheduled for 10 a.m. and we have 20 slides to cover. I'm not sure what to do. Can you help?\"",
            "Hi, I understand the situation. Here is what I suggest. First, let's check if any other team member is familiar with the presentation content and could step in as the presenter. If that's possible, we should brief them tonight. Alternatively, we could ask the client to reschedule to later in the week, explaining the situation professionally. We should also consider presenting the slides digitally and sharing them with the client in advance so they can review the content even if the live presentation needs to be postponed.",
            20, 60
        ));
        list.add(new SpeakQuestion(
            "Listen to the problem and propose a solution.\nYou have 20 seconds to prepare and 60 seconds to respond.",
            "🎧 [Voice message from your manager]\n\"We have a situation. Our delivery truck broke down and we have 50 packages that need to be delivered to clients by end of day today. The repair shop says it will take at least 3 hours. What can we do?\"",
            "Thank you for informing me. Here is a proposed solution. We should immediately contact a local courier or logistics company to handle today's deliveries on an emergency basis. Meanwhile, we should prioritize the packages by urgency and contact clients whose deliveries might be delayed to manage their expectations. For critical deliveries, we could also consider renting a vehicle. I will start making calls right away to find the most cost-effective option.",
            20, 60
        ));
        list.add(new SpeakQuestion(
            "Listen to the problem and propose a solution.\nYou have 20 seconds to prepare and 60 seconds to respond.",
            "🎧 [Voice message from a customer]\n\"Hello, I placed an order online three weeks ago and I still haven't received my package. I've emailed support twice but haven't gotten a response. I'm very frustrated and just want to know where my order is.\"",
            "Hello, I sincerely apologize for the inconvenience you've experienced. I completely understand your frustration. Here is what I'd like to do. First, I will personally look into your order status right now and track the shipment. If there was an error in processing or shipping, I will arrange for an expedited replacement to be sent to you immediately at no extra cost. I will also ensure that our customer support team follows up with you within 24 hours. Thank you for your patience.",
            20, 60
        ));
        list.add(new SpeakQuestion(
            "Listen to the problem and propose a solution.\nYou have 20 seconds to prepare and 60 seconds to respond.",
            "🎧 [Voice message from a coworker]\n\"Hey, our office printer has been broken for two days and we have an important report to print for the board meeting tomorrow morning. IT says it might not be fixed until next week. We have about 30 pages to print. Any ideas?\"",
            "Hi, I understand the urgency. Let me suggest a few options. We could use a nearby print shop to print the documents today — I know there's one about five minutes away from the office. Another option is to send the report digitally to all board members in advance and display it on a projector during the meeting, which would actually eliminate the need for printing altogether. I think the digital approach might even be more professional and environmentally friendly.",
            20, 60
        ));
        list.add(new SpeakQuestion(
            "Listen to the problem and propose a solution.\nYou have 20 seconds to prepare and 60 seconds to respond.",
            "🎧 [Voice message from your event coordinator]\n\"We're in trouble. Two of our three speakers for tomorrow's conference have just canceled due to a flight delay. We have 200 registered attendees. The event starts in 18 hours. What should we do?\"",
            "This is a challenging situation, but here is a plan. First, let's immediately check if any local experts in the same field could fill in as speakers on short notice. We should reach out to our professional network tonight. Second, we could extend the breaks and Q&A sessions to fill the time slots. Third, if the original speakers are willing, they might be able to present their sessions remotely via video call, which could actually add an interesting element to the conference. Let's also prepare an updated agenda and communicate the changes to attendees first thing in the morning.",
            20, 60
        ));
        return list;
    }

    /** Part 6: Express an Opinion (15 giây chuẩn bị, 60 giây nói) */
    private static List<SpeakQuestion> getSpeakPart6() {
        List<SpeakQuestion> list = new ArrayList<>();
        list.add(new SpeakQuestion(
            "Express your opinion on the following topic.\nSupport your opinion with reasons and examples.",
            "Do you think companies should allow employees to work from home permanently? Why or why not?",
            "In my opinion, companies should offer flexible work-from-home options, but not make it permanent for all roles. Working from home increases productivity for many employees because it eliminates commuting time and allows for a comfortable work environment. However, some jobs require in-person collaboration and teamwork that is difficult to replicate remotely. I believe a hybrid model — where employees can work from home two or three days per week — strikes the best balance between flexibility and collaboration.",
            15, 60
        ));
        list.add(new SpeakQuestion(
            "Express your opinion on the following topic.\nSupport your opinion with reasons and examples.",
            "Some people believe that university education is essential for career success. Do you agree or disagree? Why?",
            "I partially agree with this view. While a university degree opens doors to many professional opportunities and provides valuable knowledge, I believe it is not the only path to success. Many successful entrepreneurs and professionals have achieved great things without a traditional university education, often through vocational training, self-learning, or practical experience. The most important factors are skills, determination, and adaptability. That said, for certain fields like medicine or law, formal education remains absolutely necessary.",
            15, 60
        ));
        list.add(new SpeakQuestion(
            "Express your opinion on the following topic.\nSupport your opinion with reasons and examples.",
            "Is social media more harmful or beneficial to society? Give your reasons.",
            "I believe social media has both significant benefits and serious drawbacks. On the positive side, it connects people across distances, facilitates information sharing, and has empowered social movements. Businesses can reach customers more effectively, and individuals can build communities around shared interests. However, the negative effects are also real: the spread of misinformation, cyberbullying, and the impact on mental health — particularly among young people — are serious concerns. Overall, I think social media is a powerful tool, and its impact depends on how responsibly it is used.",
            15, 60
        ));
        list.add(new SpeakQuestion(
            "Express your opinion on the following topic.\nSupport your opinion with reasons and examples.",
            "Do you think governments should invest more in public transportation or in road infrastructure? Why?",
            "I strongly believe that governments should prioritize investment in public transportation. Public transit systems such as buses, trains, and subways can move large numbers of people efficiently, reducing traffic congestion and air pollution. Expanding road infrastructure, while sometimes necessary, often encourages more private car use, which worsens congestion and environmental problems in the long run. Countries like Japan and Germany demonstrate that excellent public transport systems greatly improve quality of life and economic productivity.",
            15, 60
        ));
        list.add(new SpeakQuestion(
            "Express your opinion on the following topic.\nSupport your opinion with reasons and examples.",
            "Some people think that children spend too much time on screens. Do you agree? What can parents do about it?",
            "I agree that excessive screen time is a growing concern for children. Spending too many hours on devices can affect their physical health, reduce time for outdoor activities, and impact their social development. However, not all screen time is equal — educational content and video calls with family can be beneficial. Parents can help by setting reasonable daily limits, encouraging outdoor play and hobbies, and modeling healthy screen habits themselves. Having technology-free family meals and bedtime routines can also make a significant difference.",
            15, 60
        ));
        return list;
    }

    // =========================================================
    // WRITING DATA (3 parts, mỗi part 5 bài)
    // =========================================================

    public static List<WriteQuestion> getWriteQuestions(int partIndex) {
        switch (partIndex) {
            case 0: return getWritePart1();
            case 1: return getWritePart2();
            case 2: return getWritePart3();
            default: return getWritePart1();
        }
    }

    /** Part 1: Write a Sentence Based on a Picture */
    private static List<WriteQuestion> getWritePart1() {
        List<WriteQuestion> list = new ArrayList<>();
        list.add(new WriteQuestion(
            "Part 1 - Write a Sentence",
            "Write ONE sentence about the picture using the TWO words or phrases below.",
            "🖼️ [Picture: A woman is reading a book in a library.]",
            "woman", "quietly",
            "The woman is quietly reading a book in the library.",
            5, 20
        ));
        list.add(new WriteQuestion(
            "Part 1 - Write a Sentence",
            "Write ONE sentence about the picture using the TWO words or phrases below.",
            "🖼️ [Picture: A man is repairing a bicycle on the sidewalk.]",
            "man", "carefully",
            "The man is carefully repairing the bicycle on the sidewalk.",
            5, 20
        ));
        list.add(new WriteQuestion(
            "Part 1 - Write a Sentence",
            "Write ONE sentence about the picture using the TWO words or phrases below.",
            "🖼️ [Picture: Several employees are having a meeting around a large table.]",
            "employees", "conference room",
            "The employees are having a meeting in the conference room.",
            5, 20
        ));
        list.add(new WriteQuestion(
            "Part 1 - Write a Sentence",
            "Write ONE sentence about the picture using the TWO words or phrases below.",
            "🖼️ [Picture: A chef is preparing food in a large restaurant kitchen.]",
            "chef", "restaurant kitchen",
            "The chef is preparing food in a large restaurant kitchen.",
            5, 20
        ));
        list.add(new WriteQuestion(
            "Part 1 - Write a Sentence",
            "Write ONE sentence about the picture using the TWO words or phrases below.",
            "🖼️ [Picture: A group of tourists is looking at a map near a landmark.]",
            "tourists", "looking at",
            "A group of tourists is looking at a map near the famous landmark.",
            5, 20
        ));
        return list;
    }

    /** Part 2: Respond to a Written Request (Email reply) */
    private static List<WriteQuestion> getWritePart2() {
        List<WriteQuestion> list = new ArrayList<>();
        list.add(new WriteQuestion(
            "Part 2 - Respond to a Written Request",
            "Read the email below and write a response. Your response should answer all the questions in the email.",
            "From: Sarah Johnson\nSubject: Team Lunch\n\nHi,\nI am planning a team lunch for next Friday.\nCould you let me know:\n1. Whether you can attend?\n2. Any food preferences or allergies?\n3. A restaurant you'd recommend nearby?\n\nThanks,\nSarah",
            "", "",
            "Hi Sarah,\n\nThank you for organizing the team lunch. I am happy to confirm that I can attend next Friday.\n\nRegarding food preferences, I enjoy Italian and Japanese cuisine. I do not have any allergies. One restaurant I would recommend is Bella Italia on Pine Street — they have a great set lunch menu that is suitable for groups.\n\nLooking forward to it!\n\nBest regards,\n[Your Name]",
            50, 150
        ));
        list.add(new WriteQuestion(
            "Part 2 - Respond to a Written Request",
            "Read the email below and write a response. Your response should answer all the questions in the email.",
            "From: Mark Davis\nSubject: Feedback on Training Session\n\nDear Colleague,\n\nI recently attended the company training session and would love your feedback.\nPlease share:\n1. What did you find most useful?\n2. What could be improved?\n3. Would you recommend it to others?\n\nThank you,\nMark",
            "", "",
            "Dear Mark,\n\nThank you for reaching out. I was glad to attend the training session last week.\n\nI found the section on project management tools most useful, as it introduced several software applications that I have already started using in my daily work. Regarding improvements, I felt that the session was slightly too long and could benefit from more interactive exercises to keep participants engaged. That said, I would definitely recommend this training to others, particularly those new to the company.\n\nHope this feedback is helpful!\n\nBest regards,\n[Your Name]",
            50, 150
        ));
        list.add(new WriteQuestion(
            "Part 2 - Respond to a Written Request",
            "Read the email below and write a response. Your response should answer all the questions in the email.",
            "From: Lisa Park\nSubject: Office Supply Order\n\nHello,\n\nI am preparing the quarterly office supply order.\nCould you please tell me:\n1. What supplies does your team need?\n2. How urgent is the request?\n3. Any specific brands you prefer?\n\nThank you,\nLisa",
            "", "",
            "Hello Lisa,\n\nThank you for coordinating this. Here is the information you requested.\n\nOur team mainly needs printing paper, ballpoint pens, and staples. We are also running low on whiteboard markers. In terms of urgency, the paper and markers are needed within the next week, while the other items are less urgent. As for brands, we prefer HP paper and Pilot pens as they have good quality and durability.\n\nPlease let me know if you need any additional information.\n\nBest regards,\n[Your Name]",
            50, 150
        ));
        list.add(new WriteQuestion(
            "Part 2 - Respond to a Written Request",
            "Read the email below and write a response. Your response should answer all the questions in the email.",
            "From: Tom Ellis\nSubject: Company Wellness Program Survey\n\nDear Staff,\n\nWe are launching a wellness program and need your input:\n1. What wellness activities interest you (gym, yoga, meditation, etc.)?\n2. What time would work best (morning, lunch break, evening)?\n3. Would you be willing to contribute a small monthly fee?\n\nRegards,\nTom",
            "", "",
            "Dear Tom,\n\nThank you for initiating the wellness program. I think it is a wonderful idea for the company.\n\nIn terms of activities, I am most interested in yoga and meditation classes, as they help manage stress effectively. A lunch break session would work best for me, as it would not interfere with my morning commute or evening commitments. Regarding the monthly fee, I would be willing to contribute a small amount, perhaps up to ten dollars per month, if the program is well-organized and consistent.\n\nLooking forward to the new program!\n\nBest regards,\n[Your Name]",
            50, 150
        ));
        list.add(new WriteQuestion(
            "Part 2 - Respond to a Written Request",
            "Read the email below and write a response. Your response should answer all the questions in the email.",
            "From: Alice Wong\nSubject: New Project Kickoff\n\nHi Team,\n\nWe are starting a new project next month.\nPlease share:\n1. Your availability for the kickoff meeting (suggest a time)\n2. Your role and how you can contribute\n3. Any concerns or questions you have\n\nThanks,\nAlice",
            "", "",
            "Hi Alice,\n\nThank you for the update on the new project. I am very excited to be part of it.\n\nRegarding the kickoff meeting, I am available any weekday morning next month, and I would suggest Tuesday or Wednesday at 10 a.m. for maximum team availability. In terms of my role, I can contribute to the data analysis and reporting aspects of the project, having worked on similar projects before. One question I have is whether we will be using any new project management tools, as I would like to allocate time to learn them in advance.\n\nLooking forward to working together!\n\nBest regards,\n[Your Name]",
            50, 150
        ));
        return list;
    }

    /** Part 3: Write an Essay */
    private static List<WriteQuestion> getWritePart3() {
        List<WriteQuestion> list = new ArrayList<>();
        list.add(new WriteQuestion(
            "Part 3 - Write an Essay",
            "Write an essay on the topic below. State your opinion and support it with reasons and examples.\nWrite at least 150 words.",
            "Topic: Some people believe that businesses have a responsibility to protect the environment. Others think profit should be the main priority. Discuss both views and give your opinion.",
            "", "",
            "In today's world, businesses play a significant role in both economic development and environmental impact. While some argue that profit should be a company's primary concern, I strongly believe that businesses also have a fundamental responsibility to protect the environment.\n\nThose who prioritize profit argue that companies must generate revenue to survive, create jobs, and drive innovation. Without financial success, businesses cannot sustain their operations or contribute to economic growth.\n\nHowever, neglecting environmental responsibilities can lead to long-term consequences that harm both society and businesses themselves. Climate change, pollution, and resource depletion threaten the stability of markets and communities. Forward-thinking companies recognize that sustainable practices not only protect the planet but also build consumer trust and drive long-term profitability.\n\nIn conclusion, I believe that profit and environmental responsibility are not mutually exclusive. Businesses that adopt sustainable models can achieve financial success while contributing positively to the world. Governments should also support and incentivize environmentally responsible business practices.",
            150, 300
        ));
        list.add(new WriteQuestion(
            "Part 3 - Write an Essay",
            "Write an essay on the topic below. State your opinion and support it with reasons and examples.\nWrite at least 150 words.",
            "Topic: Technology has made communication easier, but some argue it has made our relationships less meaningful. Do you agree or disagree?",
            "", "",
            "Technology has revolutionized the way we communicate, enabling instant connection across great distances. While I agree that technology can sometimes make interactions feel superficial, I ultimately believe that it has made communication more meaningful for many people.\n\nCritics argue that digital communication lacks the depth of face-to-face interaction. Text messages and social media posts can be misinterpreted, and people may feel less connected despite being constantly online. The rise of distractions from devices can also reduce the quality of conversations.\n\nNevertheless, technology has given people the ability to maintain relationships that would otherwise be impossible. Families separated by distance can video call regularly. Online communities connect individuals with shared interests who might never meet in person. During the pandemic, digital tools kept businesses running and allowed people to stay connected with loved ones.\n\nIn conclusion, while there are valid concerns about technology's effect on relationships, its benefits far outweigh the drawbacks when used mindfully. The key lies in using technology as a supplement to, rather than a replacement for, meaningful human interaction.",
            150, 300
        ));
        list.add(new WriteQuestion(
            "Part 3 - Write an Essay",
            "Write an essay on the topic below. State your opinion and support it with reasons and examples.\nWrite at least 150 words.",
            "Topic: Many companies now offer internships as part of their recruitment process. Do you think internships are beneficial for students and companies? Why or why not?",
            "", "",
            "Internships have become an integral part of modern career development, and I believe they offer significant benefits for both students and companies.\n\nFor students, internships provide invaluable real-world experience that cannot be gained in a classroom. They allow young people to apply academic knowledge in practical settings, develop professional skills, and build networks that can be crucial for future employment. Many students also use internships to discover their strengths and refine their career direction, making them more focused and motivated graduates.\n\nFor companies, internships serve as an effective talent pipeline. Businesses can identify promising candidates early, train them according to their culture, and potentially recruit them as full-time employees after graduation. This reduces hiring costs and risks compared to recruiting external candidates with no knowledge of the company.\n\nIn conclusion, internships represent a win-win arrangement. When structured thoughtfully, they create meaningful learning opportunities for students while helping companies invest strategically in future talent. I strongly encourage both academic institutions and businesses to prioritize quality internship programs.",
            150, 300
        ));
        list.add(new WriteQuestion(
            "Part 3 - Write an Essay",
            "Write an essay on the topic below. State your opinion and support it with reasons and examples.\nWrite at least 150 words.",
            "Topic: Is it better to work for a large corporation or a small company? Discuss the advantages and disadvantages of each.",
            "", "",
            "Choosing between a large corporation and a small company is one of the most important career decisions a professional can make. Both options have distinct advantages and disadvantages that depend on an individual's priorities and personality.\n\nLarge corporations offer stability, competitive salaries, comprehensive benefits, and structured career paths. They often have well-established training programs and provide opportunities to work with international teams. However, employees may feel like a small part of a big machine, with limited opportunities to make a meaningful individual impact.\n\nSmall companies, on the other hand, offer greater autonomy and the chance to take on diverse responsibilities. Employees often develop a wider range of skills and can see the direct results of their contributions. The trade-off is typically lower compensation and less job security.\n\nIn my opinion, the best choice depends on one's career stage. Early-career professionals may benefit from the structure of a large company, while those with experience might thrive in a dynamic small business environment. Ultimately, a balance of both experiences throughout one's career can be highly rewarding.",
            150, 300
        ));
        list.add(new WriteQuestion(
            "Part 3 - Write an Essay",
            "Write an essay on the topic below. State your opinion and support it with reasons and examples.\nWrite at least 150 words.",
            "Topic: Some people prefer to plan everything carefully before starting a task. Others prefer to begin immediately and adjust as they go. Which approach do you think is more effective at work?",
            "", "",
            "In professional settings, two contrasting work styles are common: meticulous planning before starting, and diving in immediately with adjustments along the way. I believe that a balanced combination of both approaches is most effective.\n\nPlanning before starting has clear advantages. It allows workers to identify potential problems, allocate resources efficiently, and set clear objectives. Well-planned projects tend to be completed on time and within budget, which is especially important in complex or high-stakes tasks.\n\nOn the other hand, being flexible and starting quickly can be valuable in fast-paced environments where circumstances change rapidly. Agile methodologies in software development, for example, embrace iteration over rigid planning. This approach encourages learning through doing and responding to feedback.\n\nIn conclusion, neither extreme is universally superior. Effective professionals learn to assess each situation and decide how much planning is appropriate. For routine tasks, jumping in promptly may save time. For complex projects, careful planning is essential. Developing the judgment to know which approach to use is, itself, a critical professional skill.",
            150, 300
        ));
        return list;
    }
}
