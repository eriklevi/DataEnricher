package com.example.DataEnricher.repositories;

import com.example.DataEnricher.entities.EnrichedParsedPacket;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EnrichedParsedPacketsRepository extends MongoRepository<EnrichedParsedPacket, String> {
    Optional<EnrichedParsedPacket> findFirstByOrderByTimestampAsc();
}
