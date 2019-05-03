package com.example.DataEnricher.repositories;

import com.example.DataEnricher.entities.SnifferData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SniffersRepository extends MongoRepository<SnifferData, String> {
}
