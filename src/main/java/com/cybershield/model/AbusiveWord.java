package com.cybershield.model;

import jakarta.persistence.*;

@Entity
@Table(name = "abusive_words")
public class AbusiveWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    private String language;

    private String category;

    private String severity;

    private Integer score;

    private String recommendedAction;

    @Column(nullable = false)
    private Boolean active = true;

    public AbusiveWord() {
    }

    public AbusiveWord(String keyword, String language, String category, String severity, Integer score, Boolean active) {
        this(keyword, language, category, severity, score, null, active);
    }

    public AbusiveWord(String keyword, String language, String category, String severity, Integer score, String recommendedAction, Boolean active) {
        this.keyword = keyword;
        this.language = language;
        this.category = category;
        this.severity = severity;
        this.score = score;
        this.recommendedAction = recommendedAction;
        this.active = (active != null) ? active : true;
    }

    @PrePersist
    protected void onCreate() {
        if (this.active == null) {
            this.active = true;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
