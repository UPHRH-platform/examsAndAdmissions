package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.Course;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExamCycleRepository extends JpaRepository<ExamCycle, Long> {
    
    // Fetch all non-obsolete records
    List<ExamCycle> findByObsolete(Integer value);

    @Query("SELECT ec.examCycleName FROM ExamCycle ec WHERE ec.id = :examCycleId")
    String getExamCycleNameById(@Param("examCycleId") Long examCycleId);

    // Fetch a non-obsolete record by ID
    Optional<ExamCycle> findByIdAndObsolete(Long id, Integer value);
    ExamCycle findByExamCycleName(String name);

    ExamCycle findByExamCycleNameAndCourseAndStartDateAndEndDate(String examCycleName, Course course, LocalDate startDate, LocalDate endDate);

    @Query(value = "select * from exam_cycle ec  where ec.course_id =:courseId and date_part('year', ec.start_date) = :startYear", nativeQuery = true)
    List<ExamCycle> searchExamCycleByCourseIdAndStartYear(@Param("courseId") String courseId, @Param("startYear") Integer startYear);

    @Query(value = "select * from exam_cycle ec  where ec.course_id =:courseId and date_part('year', ec.start_date) = :startYear and date_part('year', ec.end_date) <= :endYear", nativeQuery = true)
    List<ExamCycle> searchExamCycleByCourseIdAndStartYearAndEndYear(@Param("courseId") String courseId, @Param("startYear") Integer startYear, @Param("endYear") Integer endYear);

    List<ExamCycle> findByCourseInAndEndDateAfter(List<Course> courses, LocalDate currentDate);
    @Query("select ec.id from ExamCycle ec  where ec.examCycleName =:examCycleName")
    Long getIdByExamCycleName(String examCycleName);
}
