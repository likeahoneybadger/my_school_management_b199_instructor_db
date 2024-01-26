package com.project.schoolmanagment.entity.concretes.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.schoolmanagment.entity.concretes.user.User;
import com.project.schoolmanagment.entity.enums.Note;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StudentInfo {
    @Id
    @GeneratedValue
    private Long id;
    private Integer absentee;
    private Double midtermExam;

    private Double finalExam;

    private Double examAverage;

    private String infoNote;

    @Enumerated(EnumType.STRING)
    private Note letterGrade;

    @JsonIgnore
    @ManyToOne
    private User student;

    @JsonIgnore
    @ManyToOne
    private User teacher;
    @ManyToOne
    private Lesson lesson;

    @OneToOne
    private EducationTerm educationTerm;


}
