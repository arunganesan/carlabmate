package edu.umich.carlab.trips;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TripRecord {

    private final String TAG = "TripRecord";

    /// record id for database use (primary key)
    private Integer id;

    /// the date the trip started
    private Date startDate;

    /// the date the trip ended
    private Date endDate;

    private Integer speed = 0;

    private String surveyResponse;
    private boolean surveyUploaded;

    public TripRecord() {
        startDate = new Date();
    }


    public Integer getSpeedMax() {
        return speed;
    }


    /**
     * DESCRIPTION:
     * Getter method for the id attribute.
     *
     * @return Integer - the id value.
     */
    public Integer getID() {
        return id;
    }

    /**
     * DESCRIPTION:
     * Setter method for the id attribute.
     *
     * @param id - the Integer id value.
     */
    public void setID(Integer id) {
        this.id = id;
    }

    /**
     * DESCRIPTION:
     * Getter method for the date attribute.
     *
     * @return Date - the start date value
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * DESCRIPTION:
     * Setter method for the date attribute.
     *
     * @param date - the Date value.
     */
    public void setStartDate(Date date) {
        this.startDate = date;
    }

    /**
     * DESCRIPTION:
     * Getter method for the date attribute.
     *
     * @return Date - the end date value
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * DESCRIPTION:
     * Setter method for the date attribute.
     *
     * @param date - the Date value.
     */
    public void setEndDate(Date date) {
        this.endDate = date;
    }

    /**
     * DESCRIPTION:
     * Getter method for the date attribute as a String value.
     *
     * @return String - the date value (MM/dd/yyyy).
     */
    public String getStartDateString() {
        //todo
        //return dateFormatter.format(this.startDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        return sdf.format(this.startDate);
    }

    /**
     * DESCRIPTION:
     * Getter method for the date attribute as a String value.
     *
     * @return String - the date value (MM/dd/yyyy).
     */
    public String getEndDateString() {
        //todo
        //return dateFormatter.format(this.startDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        return sdf.format(this.endDate);
    }


    public String getSurveyResponse() {
        return surveyResponse;
    }

    public void setSurveyResponse(String response) {
        this.surveyResponse = response;
    }


    public boolean getSurveyUploaded() {
        return surveyUploaded;
    }


    public void setSurveyUploaded(boolean uploaded) {
        this.surveyUploaded = uploaded;
    }
}

