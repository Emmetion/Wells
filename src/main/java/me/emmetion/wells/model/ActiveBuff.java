package me.emmetion.wells.model;

import org.bukkit.ChatColor;
import org.bukkit.Particle;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class ActiveBuff {

    private String buff_id;
    private BuffData buffData;
    private Timestamp endDate;

    public ActiveBuff(String buff_id, Timestamp endDate) {
        this.buff_id = buff_id;
        try {
            buffData = BuffData.valueOf(buff_id);
        } catch (IllegalArgumentException e) { // this exception will cause all unknown buff_ids to have to value if removed in update.
            buffData = BuffData.NONE;
            buff_id = "NONE";
        }

        this.endDate = endDate;
    }

    public String getBuffID() {
        return buff_id;
    }

    public boolean isNone() {
        return this.buff_id.equals("NONE");
    }

    public Timestamp getEndTimestamp() {
        return endDate;
    }

    public String getEndDateAsString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEEE dd MMMMM yyyy HH:mm:ss.SSSZ", Locale.ENGLISH);

        return simpleDateFormat.format(endDate);
    }

//        Date date = new Date();
//        Time time = new Time();
//
//        Instant instant = new LocalDateTime();
//        Date curr = new Date(new java.util.Date().getTime());
//        Date end = this.endDate;

    public boolean hasEnded() {
        Date date = new Date(new java.util.Date().getTime());
        long time = date.getTime(); // current time;
        long end_time = endDate.getTime(); // end time
        if (time >= end_time) {
            return true;
        }
        return false;
    }

    public String getTimeRemainingString() {
        if (hasEnded()) {
            return ChatColor.RED + "0d 0h 0m";
        }
        Date date = new Date(new java.util.Date().getTime());
        Timestamp end_date = this.endDate;
        return ChatColor.YELLOW + date.toLocalDate().minus(end_date.getTime(), ChronoUnit.MILLIS).toString();
    }

    public static ActiveBuff defaultActiveBuff() {
        return new ActiveBuff("NONE", null);
    }

    @Override
    public String toString() {
        return "ActiveBuff{" +
                "buff_id='" + buff_id + '\'' +
                ", buff_end=" + endDate +
                '}';
    }

    public enum BuffData {
        NONE("none", null),
        FARM_BOOST("FARM_BOOST", Particle.SCRAPE);

        private String buff_id;
        private Particle anim_particle;

        BuffData(String buff_id, Particle particle) {
            this.buff_id = buff_id;
            this.anim_particle = particle;
        }

        public String getBuffID() {
            return buff_id;
        }

        public Particle getParticle() {
            return anim_particle;
        }
    }
}
