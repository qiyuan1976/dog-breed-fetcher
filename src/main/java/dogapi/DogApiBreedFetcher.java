package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        String b = (breed == null ? "" : breed.trim().toLowerCase());
        if (b.isEmpty()) {
            throw new BreedNotFoundException(breed);
        }
        String url = "https://dog.ceo/api/breed/" + b + "/list";
        Request req = new Request.Builder().url(url).build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) {
                throw new BreedNotFoundException(breed);
            }
            String body = resp.body().string();
            JSONObject json = new JSONObject(body);
            String status = json.optString("status", "error");
            if ("error".equalsIgnoreCase(status)) {
                // API returns 200 with "status":"error" for unknown main breed
                throw new BreedNotFoundException(breed);
            }
            JSONArray arr = json.getJSONArray("message");
            List<String> out = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) {
                out.add(arr.getString(i));
            }
            return out;
        } catch (Exception e) {
            // Wrap any I/O/parse errors as "breed not found" per spec/comments
            throw new BreedNotFoundException(breed);
        }
    }
}
