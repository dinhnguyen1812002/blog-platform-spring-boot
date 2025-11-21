package com.Nguyen.blogplatform.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZoneId;

@Entity
@Table(name = "traffic_counters", uniqueConstraints = {
        @UniqueConstraint(name = "uk_traffic_period_date", columnNames = {"period_type", "period_date"})
})
public class Traffic {

    public enum PeriodType { DAY, MONTH, YEAR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_date", nullable = false)
    private LocalDate periodDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", length = 10, nullable = false)
    private PeriodType periodType;

    @Column(name = "access_count", nullable = false)
    private long accessCount = 0L;

    @Column(name = "zone_id", nullable = false, length = 100)
    private String zoneId = ZoneId.systemDefault().getId();

    public Traffic() {}

    public Traffic(LocalDate periodDate, PeriodType periodType) {
        this.periodDate = periodDate;
        this.periodType = periodType;
    }

    public Long getId() { return id; }

    public LocalDate getPeriodDate() { return periodDate; }

    public void setPeriodDate(LocalDate periodDate) { this.periodDate = periodDate; }

    public PeriodType getPeriodType() { return periodType; }

    public void setPeriodType(PeriodType periodType) { this.periodType = periodType; }

    public long getAccessCount() { return accessCount; }

    public void setAccessCount(long accessCount) { this.accessCount = accessCount; }

    public void increment(long delta) { this.accessCount += delta; }

    public String getZoneId() { return zoneId; }

    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
}
