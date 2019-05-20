package com.example.DataEnricher.repositories;

import com.example.DataEnricher.entities.EnrichedParsedPacket;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface EnrichedParsedPacketsRepository extends MongoRepository<EnrichedParsedPacket, String> {
    Optional<EnrichedParsedPacket> findFirstByOrderByTimestampAsc();
}
