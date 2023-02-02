package sscce.javalin.async.msg;

public class Request {
    public final String id;
    // omitted: headers, params, body, etc

    public Request(String id) {
        this.id = id;
    }
}
