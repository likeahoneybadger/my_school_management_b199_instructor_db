package com.project.schoolmanagment.entity.concretes.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * The Lesson class represents a lesson.
 * It contains information about the lesson, such as the name,
 * credit score, compulsory status, and the lesson programs associated with it.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String lessonName;

    private Integer creditScore;

    private Boolean isCompulsory;
    @JsonIgnore
    @ManyToMany(mappedBy = "lessons",cascade = CascadeType.REMOVE)
    private Set<LessonProgram> lessonPrograms;  //  getLessonPrograms()-> null// it keeps no memory. -> better for performance.
//    private Set<LessonProgram> lessonPrograms2 = new HashSet<>();   //  getLessonPrograms2()-> emptyCollection, it initializes an object.
                                                                    //  Initializing an object, will cost memory.


}
