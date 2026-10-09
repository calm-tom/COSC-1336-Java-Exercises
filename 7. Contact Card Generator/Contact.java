class Contact {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    public void setFirstName(String name) { this.firstName = name; }
    public String getFirstName() { return firstName; }
    public void setLastName(String name) { this.lastName = name; }
    public String getLastName() { return lastName; }
    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return email; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPhone() { return phone; }
    
    //don't ask me why I need to see formatted phone numbers, I can't tell you... also GenAI.
    private String formatPhoneNumber() {
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 10) {
            return String.format("(%s) %s-%s", digits.substring(0, 3), digits.substring(3, 6), digits.substring(6));
        }
        return phone;
    }

    public String displayContact() {
        String template = """
               --------------------------------------------
               ---- Current Contact -----------------------
               --------------------------------------------
               Name:           %s %s
               Email Address:  %s
               Phone Number:   %s
               --------------------------------------------
               """;
        //doing it like this also adds extensibility for JSON, SQL, or vcard!
        return String.format(template, firstName, lastName, email, formatPhoneNumber());
    }
}