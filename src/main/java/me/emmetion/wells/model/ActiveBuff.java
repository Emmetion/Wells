package me.emmetion.wells.model;

import org.bukkit.Particle;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

public class ActiveBuff {

    private BuffType buffType;
    private Timestamp endDate;

    public ActiveBuff(BuffType buffType, Timestamp endDate) {
        this.buffType = buffType;
        this.endDate = endDate;
    }

    public ActiveBuff(BuffType buffType, LocalDateTime endTime) {
        this.buffType = buffType;
        this.endDate = Timestamp.valueOf(endTime);
    }

    public static ActiveBuff defaultActiveBuff() {
        return new ActiveBuff(BuffType.NONE, LocalDateTime.now());
    }

    public String getBuffID() {
        return buffType.getBuffID();
    }

    public BuffType getBuffType() {
        return this.buffType;
    }

    public boolean hasWellParticle() {
        return this.buffType != null && this.buffType.getParticle() != null;
    }

    public Particle getWellParticle() {
        return this.buffType.getParticle();
    }

    public boolean isNone() {
        return this.buffType.equals(BuffType.NONE);
    }

    public Timestamp getEndTimestamp() {
        return endDate;
    }

    /**
     * This method is called upon every server tick. It's applied
     * to every ActiveBuff object.
     * This will update a buff's information about being ended.
     */
    public void update() {
        if (emptyBuff()) {
            return;
        }

        if (hasEnded() && this.buffType != BuffType.NONE) {
            this.buffType = ActiveBuff.defaultActiveBuff().buffType;
        }
    }

    public void addTwentySeconds() {
        if (emptyBuff()) {
            return;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (this.endDate == null || this.endDate.toInstant().isBefore(now.toInstant())) {
            return;
        }

        Instant minutesAhead = endDate.toInstant().plus(Duration.ofSeconds(20));
        this.endDate = new Timestamp(minutesAhead.getEpochSecond()); // sets the endDate ahead 5 minutes;
    }

    public String getEndDateAsString() {
        Duration remaining = getRemainingDuration();

        int seconds = remaining.toSecondsPart();
        int minutes = remaining.toMinutesPart();
        int hours = remaining.toHoursPart();
        int days = (int) remaining.toDaysPart();

        StringBuilder builder = new StringBuilder("&b");

        if (days > 0) {
            builder.append(days + "d ");
        }

        if (hours > 0) {
            builder.append(hours + "h ");
        }

        if (minutes > 0) {
            builder.append(minutes + "m ");
        }

        if (seconds > 0) {
            builder.append(seconds + "s ");
        }

        return builder.toString();
    }

    public Duration getRemainingDuration() {
        if (endDate == null)
            return Duration.ofSeconds(0);

        Timestamp now = new Timestamp(System.currentTimeMillis());
        return Duration.between(now.toInstant(), endDate.toInstant());
    }

    public boolean hasEnded() {
        if (endDate == null)
            return true;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Duration duration = Duration.between(now.toInstant(), endDate.toInstant());

        return duration.isNegative() || duration.isZero();
    }

    private boolean emptyBuff() {
        return this.buffType == null || this.buffType.equals(BuffType.NONE) || this.endDate == null || this.endDate.toInstant().isBefore(Instant.now());
    }

    @Override
    public String toString() {
        return "ActiveBuff{" + "buffType=" + buffType + ", endDate=" + endDate + '}';
    }

}
