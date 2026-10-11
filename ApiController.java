package com.skillwallet.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ApiController {
    private final JdbcTemplate jdbc;
    public ApiController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Integer db = jdbc.queryForObject("SELECT 1", Integer.class);
        return Map.of("status", "UP", "service", "skill-wallet-api", "database", db != null ? "connected" : "unknown");
    }

    @GetMapping("/overview")
    public Map<String, Object> overview() {
        return Map.of(
            "learners", count("SELECT COUNT(*) FROM app_users WHERE role <> 'ADMIN'"),
            "activeSkills", count("SELECT COUNT(*) FROM skills WHERE active = TRUE"),
            "learningPaths", count("SELECT COUNT(*) FROM learning_paths WHERE published = TRUE"),
            "openReports", count("SELECT COUNT(*) FROM moderation_reports WHERE status = 'OPEN'"),
            "sessions", count("SELECT COUNT(*) FROM sessions")
        );
    }

    @GetMapping("/skills")
    public List<Map<String, Object>> skills() {
        return jdbc.queryForList("SELECT s.id, s.name, s.category, s.level, s.description, s.points_per_session AS points, s.owner_id, COALESCE(u.full_name, 'Community mentor') AS owner_name FROM skills s LEFT JOIN app_users u ON u.id=s.owner_id WHERE s.active=TRUE ORDER BY s.id");
    }

    @PostMapping("/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createSkill(@Valid @RequestBody SkillInput input) {
        jdbc.update("INSERT INTO skills (owner_id, name, category, level, description, points_per_session) VALUES (?, ?, ?, ?, ?, ?)", input.ownerId(), input.name().trim(), input.category().trim(), input.level(), input.description(), input.points());
        Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return Map.of("id", id, "message", "Skill listing created", "name", input.name().trim());
    }

    @PostMapping("/skill-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> requestSkill(@Valid @RequestBody SkillRequestInput input) {
        Integer exists = jdbc.queryForObject("SELECT COUNT(*) FROM skills WHERE id=? AND active=TRUE", Integer.class, input.skillId());
        if (exists == null || exists == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found");
        jdbc.update("INSERT INTO skill_requests (skill_id, requester_id, message) VALUES (?, ?, ?)", input.skillId(), input.requesterId(), input.message());
        Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return Map.of("id", id, "status", "PENDING", "message", "Skill request submitted");
    }

    @GetMapping("/wallet/{userId}")
    public Map<String, Object> wallet(@PathVariable long userId) {
        try {
            Map<String, Object> user = jdbc.queryForMap("SELECT id, full_name, wallet_points FROM app_users WHERE id=?", userId);
            List<Map<String, Object>> transactions = jdbc.queryForList("SELECT id, amount, transaction_type, description, created_at FROM wallet_transactions WHERE user_id=? ORDER BY created_at DESC LIMIT 20", userId);
            return Map.of("user", user, "transactions", transactions);
        } catch (EmptyResultDataAccessException e) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"); }
    }

    @GetMapping("/learning-paths")
    public List<Map<String, Object>> learningPaths() {
        return jdbc.queryForList("SELECT id, title, description, category, level, estimated_hours FROM learning_paths WHERE published=TRUE ORDER BY id");
    }

    @GetMapping("/jobs")
    public List<Map<String, Object>> jobs() {
        return jdbc.queryForList("SELECT id, title, company, location, employment_type, description, application_url FROM jobs WHERE published=TRUE ORDER BY created_at DESC");
    }

    @GetMapping("/community/posts")
    public List<Map<String, Object>> posts() {
        return jdbc.queryForList("SELECT p.id, p.title, p.body, p.tags, p.created_at, u.full_name AS author_name, u.avatar_initials FROM community_posts p JOIN app_users u ON u.id=p.author_id WHERE p.status='PUBLISHED' ORDER BY p.created_at DESC LIMIT 50");
    }

    @PostMapping("/community/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createPost(@Valid @RequestBody PostInput input) {
        jdbc.update("INSERT INTO community_posts (author_id, title, body, tags) VALUES (?, ?, ?, ?)", input.authorId(), input.title().trim(), input.body().trim(), input.tags());
        Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return Map.of("id", id, "message", "Community post created");
    }

    @GetMapping("/sessions")
    public List<Map<String, Object>> sessions() {
        return jdbc.queryForList("SELECT s.id, s.starts_at, s.duration_minutes, s.status, s.meeting_mode, sk.name AS skill_name, learner.full_name AS learner_name, mentor.full_name AS mentor_name FROM sessions s JOIN skills sk ON sk.id=s.skill_id JOIN app_users learner ON learner.id=s.learner_id JOIN app_users mentor ON mentor.id=s.mentor_id ORDER BY s.starts_at");
    }

    private long count(String sql) { Long n = jdbc.queryForObject(sql, Long.class); return n == null ? 0 : n; }

    public record SkillInput(@NotNull Long ownerId, @NotBlank String name, @NotBlank String category, @NotBlank String level, String description, @Positive int points) {}
    public record SkillRequestInput(@Positive long skillId, @Positive long requesterId, String message) {}
    public record PostInput(@Positive long authorId, @NotBlank String title, @NotBlank String body, String tags) {}
}
