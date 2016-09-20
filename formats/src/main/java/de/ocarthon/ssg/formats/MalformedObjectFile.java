package de.ocarthon.ssg.formats;

public class MalformedObjectFile extends Exception {

    public MalformedObjectFile() {
    }

    public MalformedObjectFile(String message) {
        super(message);
    }

    public MalformedObjectFile(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedObjectFile(Throwable cause) {
        super(cause);
    }
}
