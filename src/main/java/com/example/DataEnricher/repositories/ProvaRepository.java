package com.example.DataEnricher.repositories;

import com.example.DataEnricher.entities.Prova;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProvaRepository extends MongoRepository<Prova, String> {
}
