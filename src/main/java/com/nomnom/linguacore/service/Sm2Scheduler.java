package com.nomnom.linguacore.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
@Component
public class Sm2Scheduler {
    private static final BigDecimal MIN_EF = new BigDecimal("1.3");
    public record Sm2Result(BigDecimal easiness, int repetitions, int intervalDays, LocalDate dueDate) {}
    public Sm2Result schedule(BigDecimal ef,int repetitions,int intervalDays,int grade){
        BigDecimal q = new BigDecimal(5 - grade);
        BigDecimal delta = new BigDecimal("0.1")
                .subtract(q.multiply(
                        new BigDecimal("0.08").add(q.multiply(new BigDecimal("0.02")))));
        BigDecimal newEf = ef.add(delta);
        if(newEf.compareTo(MIN_EF) < 0){
            newEf = MIN_EF;
        }
        int newInterval;
        int newRepetitions;
        if (grade < 3) {
            newRepetitions = 0;
            newInterval = 1;
        }else {
            if(repetitions==0){
                newInterval=1;
            }else if(repetitions==1){
                newInterval=6;
            }else{
                newInterval = newEf.multiply(new BigDecimal(intervalDays)).setScale(0, RoundingMode.HALF_UP).intValue();
            }
            newRepetitions=repetitions +1;
        }
        LocalDate dueDate = LocalDate.now().plusDays(newInterval);
        return new Sm2Result(newEf, newRepetitions, newInterval, dueDate);
    }
}
