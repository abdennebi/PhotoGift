package photogift.server.config;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;

@Configuration
public class GooglePlusConfig {

    @Value("classpath:/client_secrets.json")
    Resource clientSecretsResource;

    /**
     * HttpTransport to use for external requests.
     */
    @Bean
    public static HttpTransport httpTransport() {
        return new UrlFetchTransport();
    }

    @Bean
    public static JsonFactory jsonFactory() {
        return new GsonFactory();
    }

    @Bean
    @Autowired
    public GoogleClientSecrets googleClientSecrets(JsonFactory jsonFactory) {
        try {
            return GoogleClientSecrets.load(jsonFactory, new InputStreamReader(clientSecretsResource.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize client secrets", e);
        }
    }
}
