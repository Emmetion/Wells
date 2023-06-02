package me.emmetion.wells.model;

import java.sql.Date;
import java.time.Instant;

public class ActiveBuff {

    private String buff_id;
    private Date endDate;

    public ActiveBuff(String buff_id, Date endDate) {
        this.buff_id = buff_id;
        this.endDate = endDate;
    }

    public String getBuffID() {
        return buff_id;
    }

    public Date getEndDate() {
        return endDate;
    }

    public boolean hasEnded() {
        Date date = new Date(new java.util.Date().getTime());
        long time = date.getTime(); // current time;
        long end_time = endDate.getTime();
        if (time >= end_time) {
            return true;
        }
        return false;
    }

    public static ActiveBuff defaultActiveBuff() {
        return new ActiveBuff("none", null);
    }


}
