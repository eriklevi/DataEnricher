package com.example.DataEnricher.tasks;

import com.example.DataEnricher.entities.*;
import com.example.DataEnricher.repositories.CountedPacketsRepository;
import com.example.DataEnricher.repositories.EnrichedRawPacketRepository;
import com.example.DataEnricher.repositories.EnrichedParsedPacketsRepository;
import com.example.DataEnricher.repositories.ProvaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


@Component
public class CountProbeRequestTask {

    private Logger logger = LoggerFactory.getLogger(RawPacketTask.class);

    private final ProvaRepository provaRepository;
    private final EnrichedRawPacketRepository enrichedRawPacketRepository;
    private final MongoTemplate mongoTemplate;
    private final CountedPacketsRepository countedPacketsRepository;
    private final EnrichedParsedPacketsRepository enrichedParsedPacketsRepository;

    public CountProbeRequestTask(ProvaRepository provaRepository, EnrichedRawPacketRepository enrichedRawPacketRepository, MongoTemplate mongoTemplate, CountedPacketsRepository countedPacketsRepository, EnrichedParsedPacketsRepository enrichedParsedPacketsRepository) {
        this.provaRepository = provaRepository;
        this.enrichedRawPacketRepository = enrichedRawPacketRepository;
        this.mongoTemplate = mongoTemplate;
        this.countedPacketsRepository = countedPacketsRepository;
        this.enrichedParsedPacketsRepository = enrichedParsedPacketsRepository;
    }

    /**
     * This task is used to count all the fp and mac addresses to estimate the number of devices in a time frame of 5 minutes
     * Since we need to have a CountedPackets object for all the possible time frames we need to create this frames in advance
     * The algorithm is divided in different parts:
     * 1- We retrieve the timestamp in order to have our left and right limit
     * 2- We retrieve all the distinct snifferMac present in the considered time interval in order to create a CountedPacket
     * object for each timeframe and each snifferMac since we want to distinguish by sniffer
     * 3- We create the countedpAckets object and insert them in a dictionary, the timeframe start timestamp and the sniffermac are the key
     * 4- we retrieve the data from de db via mongoaggregation
     * 5- we insert the data in each timeframe based on timeframe start timestamp and
     */
    //aggiungere condizioni sui tempi per evitare che parta troppo presto
    @Scheduled(fixedDelayString = "${tasks.RawPacket.delay :600000}")
    public void count(){
        Instant start = Instant.now();
        logger.info("Starting counter task @ "+ start);
        Prova timestamp = provaRepository.findAll().get(0);
        if(timestamp.getCounterTimestamp() == 0){
            //non possiamo generare i timeslot da 0 in poi quindi cerchiamo il primo del db
            Optional<EnrichedRawPacket> optionalPacket = enrichedRawPacketRepository.findFirstByOrderByTimestampAsc();
            Optional<EnrichedParsedPacket> optionalEnrichedParsedPacket = enrichedParsedPacketsRepository.findFirstByOrderByTimestampAsc();
            long rawPacketTimestamp;
            long parsedPacketTimestamp;
            if(optionalPacket.isPresent()){
                rawPacketTimestamp = optionalPacket.get().getTimestamp();
            } else {
                logger.error("Something wrong with timestamp in counter");
                return;
            }
            if(optionalEnrichedParsedPacket.isPresent()){
                parsedPacketTimestamp = optionalEnrichedParsedPacket.get().getTimestamp();
            } else{
                logger.error("Something wrong with timestamp in counter");
                return;
            }
            timestamp.setTimestamp(Math.min(rawPacketTimestamp, parsedPacketTimestamp));
        }
        LocalDateTime ldt = Instant.ofEpochMilli(timestamp.getCounterTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        long millisToSubtract = timestamp.getCounterTimestamp()%1000;
        long secondsToSubtract = (ldt.getSecond())*1000;
        long minutesToSubtract = (ldt.getMinute()%5)*60*1000; //rimuoviamo i minuti per arrivare ad un multiplo di 5 così da avere lo slot temporale
        long firstSlot = timestamp.getCounterTimestamp()-minutesToSubtract-secondsToSubtract-millisToSubtract;
        long nowTimestamp = Instant.now().toEpochMilli();
        LocalDateTime ldtNow = Instant.ofEpochMilli(nowTimestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
        millisToSubtract = nowTimestamp%1000;
        secondsToSubtract = ldtNow.getSecond()*1000;
        minutesToSubtract = ldtNow.getMinute()*60*1000;
        long lastSlot = nowTimestamp-minutesToSubtract-secondsToSubtract-millisToSubtract;
        //we skip the iteration if this condition occur, its rare but can appen
        if(firstSlot == lastSlot)
            return;
        // pipeline global
        MatchOperation selectGlobal = match(new Criteria("global").is(true).and("timestamp").gte(firstSlot).lt(lastSlot));
        GroupOperation groupByTimeFrame = group("year", "month", "dayOfMonth", "hour", "fiveMinute","snifferMac")
                .count().as("totPackets")
                .addToSet("deviceMac").as("macs");
        ProjectionOperation extractData = project()
                .andExpression("_id").as("timeFrame")
                .andExpression("totPackets").as("totPackets")
                .andExpression("macs").size().as("totMacs");
        SortOperation sortOperation = sort(new Sort(Sort.Direction.ASC, "timeFrame.year", "timeFrame.month", "timeFrame.dayOfMonth", "timeFrame.hour", "timeFrame.fiveMinute"));
        Aggregation aggregation = newAggregation(selectGlobal,groupByTimeFrame,extractData,sortOperation);
        AggregationResults<CountResult> result =  mongoTemplate.aggregate(aggregation, "rawPackets" ,CountResult.class);
        List<CountResult> crList = result.getMappedResults();
        //pipeline local
        MatchOperation selectLocal = match(new Criteria("global").is(false).and("timestamp").gte(firstSlot).lt(lastSlot));
        GroupOperation groupByTimeFrameLocal = group("year", "month", "dayOfMonth", "hour", "fiveMinute","snifferMac")
                .count().as("totPackets")
                .addToSet("fingerprint").as("fp");
        ProjectionOperation extractDataLocal = project()
                .andExpression("_id").as("timeFrame")
                .andExpression("totPackets").as("totPackets")
                .andExpression("fp").size().as("totMacs");
        Aggregation aggregationLocal = newAggregation(selectLocal,groupByTimeFrameLocal,extractDataLocal,sortOperation);
        AggregationResults<CountResult> resultLocal =  mongoTemplate.aggregate(aggregationLocal, "rawPackets" ,CountResult.class);
        List<CountResult> crListLocal = resultLocal.getMappedResults();
        timestamp.setCounterTimestamp(lastSlot);
        Map<Integer, CountedPackets> map = new HashMap<>();
        //avendo anche il mac dentro CountResultId, avrò un oggetto al massimo n sniffers * n timeframe
        //la cosa imortante e che nonndevo fare controlli per l'iserimento dei globali visto che avendo nella chiave il mac
        //ne avrò diversi per lo stesso time frame
        for(CountResult crGlobal: crList){
            CountedPackets cp = new CountedPackets();
            cp.setTimeFrame(crGlobal.getTimeFrame());
            cp.setGlobalPackets(crGlobal.getTotPackets());
            cp.setTotalDistinctMacAddresses(crGlobal.getTotMacs());
            cp.setLocalPackets(0);
            cp.setTotalPackets();
            cp.setTotalEstimatedDevices();
            cp.setSnifferMac(crGlobal.getTimeFrame().getSnifferMac());
            map.put(cp.hashCode(), cp);
        }
        for(CountResult crLocal: crListLocal){
            if(map.containsKey(crLocal.hashCode())){
                //if we have a match it means that we have already set an object with the global data and the same sniffer and the same timeframe
                CountedPackets cp = map.get(crLocal.hashCode());
                cp.setLocalPackets(crLocal.getTotPackets());
                cp.setTotalDistinctFingerprints(crLocal.getTotMacs());
                cp.setTotalPackets();
                cp.setTotalEstimatedDevices();
                map.replace(cp.hashCode(), cp);
            } else{
                //in this case we have data for the local and not for the global for the same sniffer and same timeframe
                CountedPackets cp = new CountedPackets();
                cp.setTimeFrame(crLocal.getTimeFrame());
                cp.setLocalPackets(crLocal.getTotPackets());
                cp.setTotalDistinctFingerprints(crLocal.getTotMacs());
                cp.setGlobalPackets(0);
                cp.setTotalPackets();
                cp.setTotalEstimatedDevices();
                cp.setSnifferMac(crLocal.getTimeFrame().getSnifferMac());
                map.put(cp.hashCode(), cp);
            }
        }
        countedPacketsRepository.saveAll(map.values());
        timestamp.setCounterTimestamp(lastSlot);
        provaRepository.save(timestamp); //da mettere al fondo
    }
}
