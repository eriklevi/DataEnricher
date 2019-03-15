package com.example.DataEnricher.repositories;

import com.example.DataEnricher.entities.Timestamp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimestampRepository extends MongoRepository<Timestamp, String> {
}
