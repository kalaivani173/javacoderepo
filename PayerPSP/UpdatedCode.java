import org.springframework.http.HttpStatus;
                              @RequestParam String payeeVpa, 
            return new ResponseEntity<>(reqPayXml, HttpStatus.OK);
   public String getDelegate() { return delegate; }
   public void setDelegate(String delegate) { this.delegate = delegate; }
   public String getDelegate() { return delegate; }
   public void setDelegate(String delegate) { this.delegate = delegate; }
   @XmlAttribute(name = "delegate")
   @XmlAttribute(name = "delegate")
