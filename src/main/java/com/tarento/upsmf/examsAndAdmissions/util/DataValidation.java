package com.tarento.upsmf.examsAndAdmissions.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class DataValidation {

    public static boolean isFirstNameValid(String firstName) {
        return firstName != null && firstName.matches("^[a-zA-Z ]+$");
    }

    public static boolean isLastNameValid(String lastName) {
        return lastName != null && lastName.matches("^[a-zA-Z ]+$");
    }

    public static boolean isEnrollmentNumberValid(String enrollmentNumber) {
        return enrollmentNumber != null && enrollmentNumber.matches("^[a-zA-Z0-9]+$");
    }

    public static boolean isMotherNameValid(String motherName) {
        return motherName != null && motherName.matches("^[a-zA-Z ]+$");
    }

    public static boolean isFatherNameValid(String fatherName) {
        return fatherName != null && fatherName.matches("^[a-zA-Z ]+$");
    }

    public static boolean isCourseNameValid(String courseName) {
        return courseName != null && courseName.matches("^[a-zA-Z0-9 _]+$");
    }

    public static boolean isExamCycleValid(String examCycle) {
        return examCycle == null || examCycle.matches("^[a-zA-Z0-9 _]+$");
    }
    public static boolean isExamValid(String exam) {
        return exam == null || exam.matches("^[a-zA-Z0-9 _]+$");
    }

    public static boolean isMarksValid(Integer marks) {
        return marks == null || (marks >= 0 && marks <= 100);
    }

    public static boolean isPassingMarksValid(Integer passingMarks) {
        return passingMarks == null || (passingMarks >= 0 && passingMarks <= 100);
    }

    public static boolean isGradeValid(String grade) {
        return grade == null || !grade.isEmpty();
    }

    public static boolean isResultValid(String result) {
        return result == null || !result.isEmpty();
    }

    public static boolean isDateValid(LocalDate date) {
        String[] datePatterns = {"dd-MM-yyyy", "dd/MM/yyyy", "yyyy-MM-dd"};

        for (String pattern : datePatterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                String formattedDate = date.format(formatter);
                return date.equals(LocalDate.parse(formattedDate, formatter));
            } catch (DateTimeParseException e) {
                e.getMessage();
            }
        }return false;
    }

    //    public static boolean isTimeFormatValid(String timeStr) {
//        // Regular expression for "HH:mm:ss" format
//        String timePattern = "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]";
//        return timeStr.matches(timePattern);
//    }
    public static boolean isMarksBetweenOneAndHundred(Integer marks) {
        return marks != null && marks >= 1 && marks <= 100;
    }
    public static boolean isDateValid(Date date) {
        if (date == null) {
            return false;
        }
        Date now = new Date();
        return !date.after(now);
    }
    public static boolean isAttendancePercentageValid(double percentage) {
        return percentage >= 0 && percentage <= 100;
    }

    public static boolean isAbsentDaysValid(int absentDays) {
        return absentDays >= 0;
    }

    public static boolean isPresentDaysValid(int presentDays) {
        return presentDays >= 0;
    }

    public static boolean isNumberOfWorkingDaysValid(int workingDays) {
        return workingDays >= 0;
    }
}

