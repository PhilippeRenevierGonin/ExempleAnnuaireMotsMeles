package fr.uca.prg.words.service.ie;

public class Message {

    private boolean error;
    private String description;

    public Message(boolean isError, String description) {
        setError(isError);
        setDescription(description);
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isError() {
        return error;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
