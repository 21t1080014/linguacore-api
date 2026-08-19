package com.nomnom.linguacore.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class Sm2SchedulerTest {
    private final Sm2Scheduler scheduler = new Sm2Scheduler();
    @Test
    void viDuChuan_grade4_interval6_rep2() {
        var result = scheduler.schedule(new BigDecimal("2.5"), 2, 6, 4);
        assertEquals(15, result.intervalDays());
        assertEquals(3, result.repetitions());
        assertEquals(0, new BigDecimal("2.5").compareTo(result.easiness()));
    }
    @Test
    void quen_gradeDuoi3_resetVeMotNgay() {
        // grade 2 (quên): dù đang có tiến độ tốt, phải reset
        var r = scheduler.schedule(new BigDecimal("2.5"), 5, 40, 2);
        // TODO: assertEquals repetitions = 0
        assertEquals(0,r.repetitions());
        // TODO: assertEquals interval = 1
        assertEquals(1,r.intervalDays());
    }
    @Test
    void lanOnDauTien_rep0_interval1() {
        var r = scheduler.schedule(new BigDecimal("2.5"), 0, 0, 4);
        // TODO: assertEquals interval = 1
        assertEquals(1,r.intervalDays());
    }
    @Test
    void lanOnThuHai_rep1_interval6() {
        var r = scheduler.schedule(new BigDecimal("2.5"), 1, 1, 4);
        // TODO: assertEquals interval = 6
        assertEquals(6,r.intervalDays());
    }
    @Test
    void sanEF_khongXuongDuoi1_3() {
        // grade 0 kéo EF xuống rất mạnh (−0.8), nhưng phải chặn ở 1.3
        var r = scheduler.schedule(new BigDecimal("1.4"), 3, 10, 0);
        // TODO: kiểm easiness == 1.3
        assertEquals(0, new BigDecimal("1.3").compareTo(r.easiness()));
    }
    @Test
    void grade5_efTang() {
        // grade 5 → EF tăng 0.1 → 2.5 thành 2.6
        var r = scheduler.schedule(new BigDecimal("2.5"), 2, 6, 5);
        // TODO: kiểm easiness == 2.6
        assertEquals(0,new BigDecimal("2.6").compareTo(r.easiness()));
    }
}

