package com.rupeek.maidbooking.model.maid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Maid {
    private String id;
    private String name;
    private String locality;
    private Gender gender;
    private double rating;
    private double hourlyRate;
    private Set<ServiceType> skills = new HashSet<>();
    private List<AvailabilityWindow> availabilityWindows = new ArrayList<>();

    public Maid() {
        this.id = UUID.randomUUID().toString();
    }

    public Maid(String name, String locality, Gender gender, double rating, double hourlyRate,
                Set<ServiceType> skills, List<AvailabilityWindow> availabilityWindows) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.locality = locality;
        this.gender = gender;
        this.rating = rating;
        this.hourlyRate = hourlyRate;
        this.skills = skills != null ? new HashSet<>(skills) : new HashSet<>();
        this.availabilityWindows = availabilityWindows != null ? new ArrayList<>(availabilityWindows) : new ArrayList<>();
    }

    public boolean isWorkingDuring(TimeSlot slot) {
        return availabilityWindows.stream().anyMatch(window -> window.covers(slot));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Set<ServiceType> getSkills() {
        return skills;
    }

    public void setSkills(Set<ServiceType> skills) {
        this.skills = skills;
    }

    public List<AvailabilityWindow> getAvailabilityWindows() {
        return availabilityWindows;
    }

    public void setAvailabilityWindows(List<AvailabilityWindow> availabilityWindows) {
        this.availabilityWindows = availabilityWindows;
    }
}
