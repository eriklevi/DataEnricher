package com.example.DataEnricher.repositories;

import com.example.DataEnricher.entities.Packet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface PacketRepository extends MongoRepository<Packet, String> {

    Stream<Packet> findAllByTimestampBetween(long startTimestamp, long endTimestamp);
}
