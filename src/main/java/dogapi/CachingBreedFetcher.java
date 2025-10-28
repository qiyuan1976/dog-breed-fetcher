package dogapi;

import java.util.*;

/**
 * This BreedFetcher caches fetch request results to improve performance and
 * lessen the load on the underlying data source. An implementation of BreedFetcher
 * must be provided. The number of calls to the underlying fetcher are recorded.
 *
 * If a call to getSubBreeds produces a BreedNotFoundException, then it is NOT cached
 * in this implementation. The provided tests check for this behaviour.
 *
 * The cache maps the name of a breed to its list of sub breed names.
 */
public class CachingBreedFetcher implements BreedFetcher {
    private final Map<String, List<String>> cache = new HashMap<>();
    private final BreedFetcher delegate;
    private int callsMade = 0;

    public CachingBreedFetcher(BreedFetcher fetcher) {
        this.delegate = fetcher;
    }

    @Override
    public synchronized List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        String key = (breed == null ? "" : breed.trim().toLowerCase());
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        callsMade++;
        List<String> result = delegate.getSubBreeds(key); // may throw
        List<String> unmod = Collections.unmodifiableList(new ArrayList<>(result));
        cache.put(key, unmod);
        return unmod;
    }

    public int getCallsMade() {
        return callsMade;
    }
}
