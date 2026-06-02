package com.learnforlearning.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class GradeForm {

    @NotNull(message = "Meg kell adnod ezt az opciót!")
    private Long subjectId;

    @NotNull(message = "Meg kell adnod ezt az opciót!")
    @Min(value = 1, message = "A megadott értéknek 1 és 5 között kell lennie!")
    @Max(value = 5, message = "A megadott értéknek 1 és 5 között kell lennie!")
    private Integer grade;

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }
}
