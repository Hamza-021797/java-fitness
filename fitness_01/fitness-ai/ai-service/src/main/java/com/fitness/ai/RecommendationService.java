package com.fitness.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    static final String DISCLAIMER = "\n\nGeneral fitness guidance only - not medical advice.";
    private final GeminiClient gemini;
    private final RecommendationRepository repo;

    public void generate(WorkoutCompletedEvent e) {
        if (repo.findByActivityId(e.activityId()).isPresent()) return;   // idempotent on redelivery
        String text = null; String source = "GEMINI";
        try {
            if (gemini.configured()) text = clean(gemini.generate(prompt(e)));
        } catch (Exception ex) {
            log.warn("Gemini call failed for activity {}: {}", e.activityId(), ex.getMessage());
        }
        if (text == null || text.isBlank()) { text = fallback(e); source = "FALLBACK"; }
        var r = new Recommendation();
        r.setActivityId(e.activityId()); r.setUserId(e.userId());
        r.setContent(text + DISCLAIMER); r.setSource(source);
        repo.save(r);
    }

    private String prompt(WorkoutCompletedEvent e) {
        return """
            You are a supportive fitness coach. A user just finished a workout:
            type=%s, duration=%d minutes, distance=%s km, calories=%s, notes=%s.
            Give general, non-medical guidance in under 120 words: one line on what went well,
            two concrete suggestions for recovery or the next session, and one hydration/nutrition tip.
            Do not diagnose or recommend medication. Plain text, no markdown headings.
            """.formatted(e.type(), e.durationMinutes(), e.distanceKm(), e.caloriesBurned(),
                e.notes() == null ? "none" : e.notes().replaceAll("[\\r\\n]+", " "));
    }

    /** Validate model output: trim, drop control characters, cap length. */
    private String clean(String s) {
        if (s == null) return null;
        s = s.replaceAll("[\\p{Cntrl}&&[^\\n]]", "").trim();
        return s.length() > 1500 ? s.substring(0, 1500) : s;
    }

    private String fallback(WorkoutCompletedEvent e) {
        return "Nice work on your " + e.type().toLowerCase() + " session (" + e.durationMinutes() + " min). "
            + "Cool down, stretch the muscles you used, drink water, and plan a lighter day before your next hard session.";
    }
}
