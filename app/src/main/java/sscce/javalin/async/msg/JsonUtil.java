package sscce.javalin.async.msg;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

public class JsonUtil {
    private static final Moshi moshi = new Moshi.Builder().build();
    private static final JsonAdapter<Request> reqAdapter = moshi.adapter(Request.class);
    private static final JsonAdapter<Response> resAdapter = moshi.adapter(Response.class);

    private JsonUtil() { /* static util class */ }

    public static String toJson(Request reqEvent) {
        return reqAdapter.toJson(reqEvent);
    }

    public static Request toRequest(String json) throws IOException {
        return reqAdapter.fromJson(json);
    }

    public static String toJson(Response resEvent) {
        return resAdapter.toJson(resEvent);
    }

    public static Response toResponse(String json) throws IOException {
        return resAdapter.fromJson(json);
    }
}
