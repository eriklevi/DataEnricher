package com.example.DataEnricher.repositories;

import com.example.DataEnricher.entities.CountedPackets;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CountedPacketsRepository extends MongoRepository<CountedPackets, String> {
}
