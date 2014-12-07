package photogift.server.filter;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.json.JsonFactory;
import com.google.gson.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Used as an abstract factory for testing static and final method calls.
 */
class GoogleIdTokenRepository {

    public GoogleIdToken parse(JsonFactory jsonFactory, String idTokenString) throws IOException {
        return GoogleIdToken.parse(jsonFactory, idTokenString);
    }

    public boolean verifyAudience(GoogleIdToken idToken, List<String> audience) {
        return idToken.verifyAudience(audience);
    }

    public TokenInfoResponse fromJson(ByteArrayOutputStream out)
            throws UnsupportedEncodingException {
        return fromJson(out.toString("UTF-8"), TokenInfoResponse.class);
    }

    /**
     * @param json  Object to convert to instance representation.
     * @param clazz Type to which object should be converted.
     * @return Instance representation of the given JSON object.
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    private static final String TIME_SCHEME = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final TimeZone TZ = TimeZone.getTimeZone("UTC");
    private static final DateFormat DF = new SimpleDateFormat(TIME_SCHEME);

    static {
        DF.setTimeZone(TZ);
    }

    /**
     * JSON serializer for java.util.Date, required when serializing larger objects containing
     * Date members.
     */
    public static final JsonSerializer<Date> DATE_SERIALIZER = new JsonSerializer<Date>() {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            try {
                return new JsonPrimitive(DF.format(src));
            } catch (NullPointerException e) {
                return null;
            }
        }
    };

    /**
     * JSON deserializer for java.util.Date, required when deserializing larger objects containing
     * Date members.
     */
    public static final JsonDeserializer<Date> DATE_DESERIALIZER = new JsonDeserializer<Date>() {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT,
                                JsonDeserializationContext context) throws JsonParseException {
            try {
                return DF.parse(json.getAsString());
            } catch (NullPointerException e) {
                return null;
            } catch (ParseException e) {
                return null;
            }
        }
    };

    /**
     * Gson object to use in all serialization and deserialization.
     */
    private static ExclusionStrategy excludeReadOnly = new ReadOnlyExclusionStrategy();
    public static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Date.class, DATE_SERIALIZER)
            .registerTypeAdapter(Date.class, DATE_DESERIALIZER)
            .addDeserializationExclusionStrategy(excludeReadOnly)
            .disableHtmlEscaping()
            .create();

    /**
     * Excludes any field (or class) that is tagged with an "@ReadOnly" annotation.
     */
    private static class ReadOnlyExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return clazz.getAnnotation(ReadOnly.class) != null;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(ReadOnly.class) != null;
        }
    }

    /**
     * Custom annotation to indicate read-only access to Gson.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ReadOnly {
    }
}