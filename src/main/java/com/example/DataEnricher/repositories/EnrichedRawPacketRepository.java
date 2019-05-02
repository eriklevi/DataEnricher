package com.example.DataEnricher.repositories;

import com.example.DataEnricher.entities.EnrichedRawPacket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface EnrichedRawPacketRepository extends MongoRepository<EnrichedRawPacket, String> {

    Stream<EnrichedRawPacket> findAllByTimestampBetween(long startTimestamp, long endTimestamp);
    Optional<EnrichedRawPacket> findFirstByOrderByTimestampAsc();
}
