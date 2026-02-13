import jakarta.xml.bind.annotation.XmlElement;
    @XmlElement(name = "NewField") // New field added
    private String newField; // New field added
    public String getNewField() { // Getter for new field
        return newField;
    }

    public void setNewField(String newField) { // Setter for new field
        this.newField = newField;
    }
