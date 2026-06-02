package com.learnforlearning.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TeacherSuggestionForm {

    @NotBlank(message = "Meg kell adnod ezt az opciót!")
    private String name;

    @NotNull(message = "Meg kell adnod ezt az opciót!")
    private Long subjectId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }
}
