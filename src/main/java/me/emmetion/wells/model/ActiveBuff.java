package me.emmetion.wells.model;

import org.bukkit.Particle;

import java.sql.Timestamp;

public class ActiveBuff {

    private String buff_id;
    private BuffType buffData;
    private Timestamp endDate;

    public ActiveBuff(String buff_id, Timestamp endDate) {
        this.buff_id = buff_id;
        try {
            buffData = BuffType.valueOf(buff_id);
        } catch (IllegalArgumentException e) { // this exception will cause all unknown buff_ids to have to value if removed in update.
            buffData = BuffType.NONE;
            buff_id = "NONE";
        }

        this.endDate = endDate;
    }

    public String getBuffID() {
        return buff_id;
    }

    public boolean hasWellParticle() {
        return this.buffData != null && this.buffData.anim_particle != null;
    }

    public Particle getWellParticle() {
        return this.buffData.getParticle();
    }

    public boolean isNone() {
        return this.buffData.equals(BuffType.NONE);
    }

    public Timestamp getEndTimestamp() {
        return endDate;
    }

    /**
     * This method is called upon every server tick. It's applied
     * to every ActiveBuff object.
     *
     * This will update a buff's information about being ended.
     */
    public void update() {
        if (hasEnded()) {
            if (this.buffData != BuffType.NONE) {
                this.buffData = BuffType.NONE;
                this.buff_id = "NONE";
            }
        }
    }

    public String getEndDateAsString() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        int duration = endDate.compareTo(now);
        System.out.println("duration = "+duration);

        if (duration <= 0) {
            return "00d 00h 00m 00s";
        }
        System.out.println("duration = "+duration);

        long milliseconds = Math.abs(duration);
        long seconds = milliseconds / 1000;
        long days = seconds / (24 * 60 * 60);
        long hours = (seconds % (24 * 60 * 60)) / (60 * 60);
        long minutes = ((seconds % (24 * 60 * 60)) % (60 * 60)) / 60;
        seconds = ((seconds % (24 * 60 * 60)) % (60 * 60)) % 60;



        System.out.println("milliseconds = " + milliseconds);
        System.out.println("minutes = " + minutes);
        System.out.println("seconds = " + seconds);
        System.out.println("days = " + days);
        System.out.println("hours = " + hours);
        System.out.println("minutes = " + hours);

        return String.format("%02dd %02dh %02dm %02ds", days, hours, minutes, seconds);
    }

    public boolean hasEnded() {
        if (endDate == null)
            return true;
        long compared = endDate.compareTo(new Timestamp(System.currentTimeMillis()));
        // "a value less than 0 if this Timestamp object is before the given argument"
        return compared <= 0;
    }


    public static ActiveBuff defaultActiveBuff() {
        return new ActiveBuff("NONE", null);
    }

    @Override
    public String toString() {
        return "ActiveBuff{" +
                "buff_id='" + buff_id + '\'' +
                ", buffData=" + buffData +
                ", endDate=" + endDate +
                '}';
    }

    public enum BuffType {
        NONE("NONE", null),
        FARM_BOOST("FARM_BOOST", Particle.VILLAGER_HAPPY),
        RESISTANCE("RESISTANCE", Particle.WAX_OFF);

        private String buff_id;
        private Particle anim_particle;

        BuffType(String buff_id, Particle particle) {
            this.buff_id = buff_id;
            this.anim_particle = particle;
        }

        public String getBuffID() {
            return buff_id;
        }

        public Particle getParticle() {
            return anim_particle;
        }

        @Override
        public String toString() {
            return "BuffData{" +
                    "anim_particle=" + anim_particle +
                    '}';
        }
    }
}
