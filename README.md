# Qudini Function Utilities

Utilities for functions; currently handles [thunking](https://en.wikipedia.org/wiki/Thunk) and
[memoisation](https://en.wikipedia.org/wiki/Memoization):

```java
import com.qudini.function.Thunk;

import static com.qudini.function.Memoizer.memoize;

class Application {

    // Expensive DB operations that don't become stale:-

    private final Thunk<List<Merchant>> limitedMerchants = Thunk.of(() -> stream()
            .flatMap(UserRole::streamMerchants)
            .distinct()
            .collect(toList()));

    private final Thunk<List<Merchant>> allMerchants = Thunk.of(() -> Merchant
            .find(NOT_ARCHIVED)
            .fetch()); 
    
    // A DB operation that can become stale, but becoming eventually consistent after a day is acceptable.
    private static Supplier<PassphraseHashingAlgorithm> queryRecommendedAlgorithms = memoize(
            Duration.ofDays(1),
            () -> query(
                    PassphraseHashingAlgorithm.class,
                    (criteria, root, criteriaQuery) -> criteriaQuery
                            .orderBy(
                                    criteria.desc(root),
                                    criteria.desc(root.get("processorCost")),
                                    criteria.desc(root.get("memoryCost")),
                                    criteria.desc(root.get("parallelisationParameter")),
                                    criteria.desc(root.get("derivedKeyLength"))
                            )
                            .where(criteria.equal(
                                    root.get("algorithmName"),
                                    PassphraseHashingAlgorithmName.SCRYPT
                            ))
            )
                    .setMaxResults(2)
                    .getResultList()
    );
}
 ```