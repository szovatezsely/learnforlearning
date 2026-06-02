package com.learnforlearning.web.dto;

import com.learnforlearning.domain.Specialization;
import jakarta.validation.constraints.NotNull;

public class SpecializationForm {

    @NotNull(message = "Ki kell választanod a specializációt!")
    private Specialization specialization;

    public Specialization getSpecialization() {
        return specialization;
    }

    public void setSpecialization(Specialization specialization) {
        this.specialization = specialization;
    }
}
