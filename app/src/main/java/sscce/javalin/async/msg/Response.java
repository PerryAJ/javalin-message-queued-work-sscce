package sscce.javalin.async.msg;

public class Response {
    public final String id;
    public final String content;

    public Response(String id, String content) {
        this.id = id;
        this.content = content;
    }
}
