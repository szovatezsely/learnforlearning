package com.learnforlearning.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubjectSuggestionForm {

    @NotBlank(message = "Meg kell adnod ezt az opciót!")
    private String name;

    @NotBlank(message = "Meg kell adnod ezt az opciót!")
    private String code;

    @NotNull(message = "Meg kell adnod ezt az opciót!")
    @Min(value = 1, message = "A megadott értéknek 1 és 20 között kell lennie!")
    @Max(value = 20, message = "A megadott értéknek 1 és 20 között kell lennie!")
    private Integer credit;

    @NotBlank(message = "Meg kell adnod ezt az opciót!")
    private String url;

    private boolean evenSemester;
    private boolean existsOnA;
    private boolean existsOnB;
    private boolean existsOnC;
    private boolean optionalOnA;
    private boolean optionalOnB;
    private boolean optionalOnC;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEvenSemester() {
        return evenSemester;
    }

    public void setEvenSemester(boolean evenSemester) {
        this.evenSemester = evenSemester;
    }

    public boolean isExistsOnA() {
        return existsOnA;
    }

    public void setExistsOnA(boolean existsOnA) {
        this.existsOnA = existsOnA;
    }

    public boolean isExistsOnB() {
        return existsOnB;
    }

    public void setExistsOnB(boolean existsOnB) {
        this.existsOnB = existsOnB;
    }

    public boolean isExistsOnC() {
        return existsOnC;
    }

    public void setExistsOnC(boolean existsOnC) {
        this.existsOnC = existsOnC;
    }

    public boolean isOptionalOnA() {
        return optionalOnA;
    }

    public void setOptionalOnA(boolean optionalOnA) {
        this.optionalOnA = optionalOnA;
    }

    public boolean isOptionalOnB() {
        return optionalOnB;
    }

    public void setOptionalOnB(boolean optionalOnB) {
        this.optionalOnB = optionalOnB;
    }

    public boolean isOptionalOnC() {
        return optionalOnC;
    }

    public void setOptionalOnC(boolean optionalOnC) {
        this.optionalOnC = optionalOnC;
    }
}
