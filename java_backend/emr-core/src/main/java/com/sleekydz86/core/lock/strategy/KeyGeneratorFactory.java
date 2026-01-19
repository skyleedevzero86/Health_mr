package com.sleekydz86.core.lock.strategy;

import com.sleekydz86.core.lock.annotation.DistributedLock;
import com.sleekydz86.core.lock.annotation.Idempotent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KeyGeneratorFactory {

    private final SpelKeyGenerator spelKeyGenerator;
    private final ParameterKeyGenerator parameterKeyGenerator;
    private final HeaderKeyGenerator headerKeyGenerator;
    private final Map<DistributedLock.LockKeyType, LockKeyGenerator> lockKeyGenerators;
    private final Map<Idempotent.IdempotencyKeyType, LockKeyGenerator> idempotencyKeyGenerators;

    public KeyGeneratorFactory(
            SpelKeyGenerator spelKeyGenerator,
            ParameterKeyGenerator parameterKeyGenerator,
            HeaderKeyGenerator headerKeyGenerator) {
        this.spelKeyGenerator = spelKeyGenerator;
        this.parameterKeyGenerator = parameterKeyGenerator;
        this.headerKeyGenerator = headerKeyGenerator;
        this.lockKeyGenerators = Map.of(
                DistributedLock.LockKeyType.SPEL, spelKeyGenerator,
                DistributedLock.LockKeyType.PARAMETER, parameterKeyGenerator
        );
        this.idempotencyKeyGenerators = Map.of(
                Idempotent.IdempotencyKeyType.SPEL, spelKeyGenerator,
                Idempotent.IdempotencyKeyType.PARAMETER, parameterKeyGenerator,
                Idempotent.IdempotencyKeyType.HEADER, headerKeyGenerator
        );
    }

    public LockKeyGenerator getLockKeyGenerator(DistributedLock.LockKeyType keyType) {
        return lockKeyGenerators.getOrDefault(keyType, spelKeyGenerator);
    }

    public LockKeyGenerator getIdempotencyKeyGenerator(Idempotent.IdempotencyKeyType keyType) {
        return idempotencyKeyGenerators.getOrDefault(keyType, spelKeyGenerator);
    }
}
