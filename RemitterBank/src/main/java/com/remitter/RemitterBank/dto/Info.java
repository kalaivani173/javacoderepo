package com.remitter.RemitterBank.dto;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)

public class Info {
    @XmlElement(name = "Identity")
    private Identity identity;

    public Info() {
    }

    @XmlElement(name = "Rating")
    private Rating rating;

    // getters and setters
    public Identity getIdentity() { return identity; }
    public void setIdentity(Identity identity) { this.identity = identity; }

    public Rating getRating() { return rating; }
    public void setRating(Rating rating) { this.rating = rating; }


}
