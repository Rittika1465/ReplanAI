package Replan.AI.service;

import Replan.AI.dto.ChatResponse;
import Replan.AI.model.UserProfile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import Replan.AI.dto.ChatHistoryMessage;
import java.util.ArrayList;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class AIChatService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    public AIChatService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public ChatResponse analyseMessage(
            UserProfile profile,
            String userMessage,
            List<ChatHistoryMessage> history
    ) {
        try {
            String systemPrompt = createSystemPrompt(profile);

            /*
             * Groq-এ পাঠানোর message list।
             */
            List<Map<String, String>> messages =
                    new ArrayList<>();

// প্রথমে RePlan-এর rules
            messages.add(Map.of(
                    "role", "system",
                    "content", systemPrompt
            ));

// তারপর previous conversation
            if (history != null) {

                for (ChatHistoryMessage oldMessage : history) {

                    if (oldMessage != null
                            && oldMessage.role() != null
                            && oldMessage.content() != null
                            && !oldMessage.content().isBlank()
                            && (oldMessage.role().equals("user")
                            || oldMessage.role().equals("assistant"))) {

                        messages.add(Map.of(
                                "role", oldMessage.role(),
                                "content", oldMessage.content()
                        ));
                    }
                }
            }

// সবশেষে current user message
            messages.add(Map.of(
                    "role", "user",
                    "content", userMessage
            ));

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "temperature", 0.0,
                    "response_format", Map.of(
                            "type", "json_object"
                    ),
                    "messages", messages
            );

            String jsonBody =
                    objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header(
                            "Authorization",
                            "Bearer " + apiKey
                    )
                    .header(
                            "Content-Type",
                            "application/json"
                    )
                    .POST(
                            HttpRequest.BodyPublishers
                                    .ofString(jsonBody)
                    )
                    .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Groq API error: " + response.body()
                );
            }

            JsonNode responseJson =
                    objectMapper.readTree(response.body());

            String aiContent = responseJson
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            if (aiContent.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Groq returned an empty response"
                );
            }

            return objectMapper.readValue(
                    aiContent,
                    ChatResponse.class
            );

        } catch (ResponseStatusException exception) {
            throw exception;

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Groq request was interrupted"
            );

        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to process Groq response: "
                            + exception.getMessage()
            );
        }
    }

    private String createSystemPrompt(UserProfile profile) {

        return """
                You are RePlan AI, a habit planning assistant.

                USER PROFILE:
                Role: %s
                Available days: %s
                Preferred start time: %s
                Preferred end time: %s
                Maximum daily minutes: %d
                Current date: %s

                Identify one intent from:
                CREATE_HABIT, UPDATE_HABIT, MARK_COMPLETE,
                MARK_MISSED, SHOW_TODAY, SHOW_HABITS,
                SHOW_PROGRESS, REPLAN, OUT_OF_SCOPE.

                For CREATE_HABIT, extract a realistic habit proposal.

                Allowed frequencyType values:
                DAILY, WEEKDAYS, WEEKENDS, SELECTED_DAYS.

                Rules:
                1. WEEKDAYS means Monday to Friday.
                2. WEEKENDS means Saturday and Sunday.
                3. SELECTED_DAYS means explicitly selected days.
                4. unit should normally be MINUTES or REPS.
                5. Never claim that a habit has already been saved.
                6. Ask for confirmation before creating or changing a habit.
                7. If important information is missing, put its names
                   inside missingFields.
                8. If time is missing, choose a realistic time within
                   the user's preferred time range.
                9. Use the current date as startDate when it is missing.
                10. Respond only with valid JSON. Do not use Markdown.
                
                11. Profile availability and preferred times are soft defaults,
                    not strict restrictions.
                
                12. When the user explicitly provides a day or time,
                    the user's explicit choice has priority over profile preferences.
                
                13. Never silently replace an explicitly provided time with
                    the profile's preferred time.
                
                14. If an explicit day or time is outside the usual profile preferences,
                    mention the conflict briefly and ask for confirmation,
                    but keep the user's requested day and time in the proposal.
                
                15. Use profile days and preferred time only when the user
                    has not provided those details.
             
                16. When the user says "today", use the current day of week
                    shown above.
                
                17. An explicitly provided day or time always has priority
                    over the profile's usual preferences.
                
                18. Profile days and times are only defaults when the user
                    has not provided a specific day or time.

                STRICT HABIT VALIDATION GATE:
                
                A habit proposal requires all three details:
                1. Habit name or activity
                2. Frequency or selected days
                3. Duration or measurable target
                
                
                
                DATE AND TIME FORMAT RULES:
                
                - startDate must always be a real calendar date in YYYY-MM-DD format.
                - Example startDate: "2026-08-18".
                - Never put MONDAY, TUESDAY, or another weekday name inside startDate.
                - Weekday names belong only inside daysOfWeek.
                - preferredTime must always use HH:mm:ss format.
                - Example preferredTime: "12:40:00".
                - If the user says "today", startDate must equal the Current date shown above.
                - If the user provides a start and end time, calculate durationMinutes
                  from that time range.
                  
                Use information from the complete conversation history.
                
                If ANY required detail is missing:
                - Keep intent as CREATE_HABIT.
                - Ask one short follow-up question.
                - Set requiresConfirmation to false.
                - Put the missing field names inside missingFields.
                - Set habit to null.
                - Do not create a proposal.
                - Do not assume or invent default values.
                
                Only preferredTime and startDate may be selected automatically,
                and only after name, frequency and duration are available.
                
                Example:
                User says: "I want to build a reading habit"
                
                
                SAFETY BOUNDARY:
                
                Do not create, optimize, or schedule habits whose goal is:
                - alcohol consumption
                - smoking or tobacco use
                - illegal drug use
                - violence or harm
                - self-harm
                - other clearly dangerous behaviour
                
                For these requests:
                - Use intent OUT_OF_SCOPE.
                - Give a brief, non-judgmental safety response.
                - Set requiresConfirmation to false.
                - Set missingFields to an empty list.
                - Set habit to null.
                - Offer a safe alternative when appropriate.
                
                Examples:
                - "Drink water daily" is allowed.
                - "Drink alcohol every day" is not allowed.
                - "Create alcohol-free days" is allowed.
                - "Help me reduce smoking" may be redirected toward a healthy
                  reduction or support goal, without giving medical advice.
                
                Never ask for frequency, duration, quantity, or scheduling details
                for a harmful habit.
                
                For example, if Current date is 2026-08-18 and the user says
                "only for today from 12:40 pm to 1:00 pm, planning":
                
                - startDate must be "2026-08-18"
                - daysOfWeek must be ["TUESDAY"]
                - preferredTime must be "12:40:00"
                - durationMinutes must be 20
                - startDate must never be "TUESDAY"
                
                Correct response:
                {
                  "intent": "CREATE_HABIT",
                  "reply": "How many minutes would you like to read, and on which days?",
                  "requiresConfirmation": false,
                  "missingFields": ["durationMinutes", "frequencyType"],
                  "habit": null
                }
                
                Incorrect behaviour:
                Do not automatically choose DAILY, WEEKDAYS, 30 minutes,
                or any other missing required detail.
                
                Return exactly this JSON structure:
                {
                  "intent": "CREATE_HABIT",
                  "reply": "Your short helpful reply",
                  "requiresConfirmation": true,
                  "missingFields": [],
                  "habit": {
                    "name": "Java Practice",
                    "description": "Practice Java consistently",
                    "frequencyType": "WEEKDAYS",
                    "daysOfWeek": [
                      "MONDAY",
                      "TUESDAY",
                      "WEDNESDAY",
                      "THURSDAY",
                      "FRIDAY"
                    ],
                    "targetValue": 30,
                    "unit": "MINUTES",
                    "durationMinutes": 30,
                    "preferredTime": "21:00:00",
                    "startDate": "%s"
                  }
                }

                For non-habit intents, habit may be null.
                """
                .formatted(
                        profile.getRole(),
                        profile.getAvailableDays(),
                        profile.getPreferredStartTime(),
                        profile.getPreferredEndTime(),
                        profile.getMaximumDailyMinutes(),
                        LocalDate.now(),
                        LocalDate.now().getDayOfWeek(),
                        LocalDate.now()
                );
    }
}