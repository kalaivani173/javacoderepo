package com.npci.UPISim.dto;


import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Info")
public class Info {
    @XmlElement(name = "Identity")
    private Identity identity;

    @XmlElement(name = "Rating")
    private Rating rating;

    public Info() {
    }

    // getters and setters
    public Identity getIdentity() { return identity; }
    public void setIdentity(Identity identity) { this.identity = identity; }

    public Rating getRating() { return rating; }
    public void setRating(Rating rating) { this.rating = rating; }


}