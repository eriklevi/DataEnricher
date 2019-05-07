package com.example.DataEnricher.tasks;

import com.example.DataEnricher.entities.*;
import com.example.DataEnricher.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


@Component
public class CountProbeRequestTask {

    private Logger logger = LoggerFactory.getLogger(RawPacketTask.class);

    private final ProvaRepository provaRepository;
    private final EnrichedRawPacketRepository enrichedRawPacketRepository;
    private final MongoTemplate mongoTemplate;
    private final CountedPacketsRepository countedPacketsRepository;
    private final EnrichedParsedPacketsRepository enrichedParsedPacketsRepository;
    private final SniffersRepository sniffersRepository;

    public CountProbeRequestTask(ProvaRepository provaRepository, EnrichedRawPacketRepository enrichedRawPacketRepository, MongoTemplate mongoTemplate, CountedPacketsRepository countedPacketsRepository, EnrichedParsedPacketsRepository enrichedParsedPacketsRepository, SniffersRepository sniffersRepository) {
        this.provaRepository = provaRepository;
        this.enrichedRawPacketRepository = enrichedRawPacketRepository;
        this.mongoTemplate = mongoTemplate;
        this.countedPacketsRepository = countedPacketsRepository;
        this.enrichedParsedPacketsRepository = enrichedParsedPacketsRepository;
        this.sniffersRepository = sniffersRepository;
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
        Map<String, SnifferData> listaSniffers = sniffersRepository
                .findAll()
                .stream()
                .collect(Collectors.toMap(SnifferData::getMac, Function.identity()));
        if(listaSniffers.size() == 0){
            logger.error("No sniffers are active at the moment!");
            return;
        }
        Instant start = Instant.now().atZone(ZoneId.of("CET")).toInstant();
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
            timestamp.setCounterTimestamp(Math.min(rawPacketTimestamp, parsedPacketTimestamp));
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
        minutesToSubtract = (ldtNow.getMinute()%5)*60*1000;
        long lastSlot = nowTimestamp-minutesToSubtract-secondsToSubtract-millisToSubtract;
        //we skip the iteration if this condition occur, its rare but can appen
        if(firstSlot == lastSlot){
            Instant finish = Instant.now();
            logger.info("Count task terminata in {} millisecondi, è passato troppo poco tempo dall'ultima esecuzione!", Duration.between(start, finish).toMillis());
            return;}
        // pipeline global dump
        MatchOperation matchOperation;
        GroupOperation groupOperation;
        ProjectionOperation projectionOperation;
        SortOperation sortOperation;
        Aggregation aggregation;
        AggregationResults<CountResult> aggregationResults;

        matchOperation = match(new Criteria("global").is(true).and("timestamp").gte(firstSlot).lt(lastSlot));
        groupOperation = group("year", "month", "dayOfMonth", "hour", "fiveMinute","snifferMac")
                .count().as("totPackets")
                .addToSet("deviceMac").as("macs");
        projectionOperation = project()
                .andExpression("_id").as("timeFrame")
                .andExpression("totPackets").as("totPackets")
                .andExpression("macs").size().as("totMacs");
        sortOperation = sort(new Sort(Sort.Direction.ASC, "timeFrame.year", "timeFrame.month", "timeFrame.dayOfMonth", "timeFrame.hour", "timeFrame.fiveMinute"));
        aggregation = newAggregation(matchOperation,groupOperation,projectionOperation,sortOperation);
        aggregationResults =  mongoTemplate.aggregate(aggregation, "rawPackets" ,CountResult.class);
        List<CountResult> crGlobalDump = aggregationResults.getMappedResults();
        //pipeline local dump
        matchOperation = match(new Criteria("global").is(false).and("timestamp").gte(firstSlot).lt(lastSlot));
        groupOperation = group("year", "month", "dayOfMonth", "hour", "fiveMinute","snifferMac")
                .count().as("totPackets")
                .addToSet("fingerprint").as("fp");
        projectionOperation = project()
                .andExpression("_id").as("timeFrame")
                .andExpression("totPackets").as("totPackets")
                .andExpression("fp").size().as("totMacs");
        aggregation = newAggregation(matchOperation,groupOperation,projectionOperation,sortOperation);
        aggregationResults =  mongoTemplate.aggregate(aggregation, "rawPackets" ,CountResult.class);
        List<CountResult> crLocalDump = aggregationResults.getMappedResults();
        // pipeline global parsed
        matchOperation = match(new Criteria("global").is(true).and("timestamp").gte(firstSlot).lt(lastSlot));
        groupOperation = group("year", "month", "dayOfMonth", "hour", "fiveMinute","snifferMac")
                .count().as("totPackets")
                .addToSet("deviceMac").as("macs");
        projectionOperation = project()
                .andExpression("_id").as("timeFrame")
                .andExpression("totPackets").as("totPackets")
                .andExpression("macs").size().as("totMacs");
        aggregation = newAggregation(matchOperation,groupOperation,projectionOperation,sortOperation);
        aggregationResults =  mongoTemplate.aggregate(aggregation, "parsedPackets" ,CountResult.class);
        List<CountResult> crGlobalParsed = aggregationResults.getMappedResults();
        // pipeline local parsed
        matchOperation = match(new Criteria("global").is(false).and("timestamp").gte(firstSlot).lt(lastSlot));
        groupOperation = group("year", "month", "dayOfMonth", "hour", "fiveMinute","snifferMac")
                .count().as("totPackets")
                .addToSet("fingerprint").as("macs");
        projectionOperation = project()
                .andExpression("_id").as("timeFrame")
                .andExpression("totPackets").as("totPackets")
                .andExpression("macs").size().as("totMacs");
        aggregation = newAggregation(matchOperation,groupOperation,projectionOperation,sortOperation);
        aggregationResults =  mongoTemplate.aggregate(aggregation, "parsedPackets" ,CountResult.class);
        List<CountResult> crLocalParsed = aggregationResults.getMappedResults();
        timestamp.setCounterTimestamp(lastSlot);
        Map<Integer, CountedPackets> map = new HashMap<>();
        //avendo anche il mac dentro CountResultId, avrò un oggetto al massimo n sniffers * n timeframe
        //la cosa imortante e che nonndevo fare controlli per l'iserimento dei globali visto che avendo nella chiave il mac
        //ne avrò diversi per lo stesso time frame
            for(CountResult crGlobal: crGlobalDump){
                CountedPackets cp = new CountedPackets();
                cp.setTimeFrame(crGlobal.getTimeFrame());
                cp.setGlobalPackets(crGlobal.getTotPackets());
                cp.setTotalDistinctMacAddresses(crGlobal.getTotMacs());
                cp.setLocalPackets(0);
                cp.setTotalPackets();
                cp.setTotalEstimatedDevices();
                cp.setSnifferMac(crGlobal.getTimeFrame().getSnifferMac());
                SnifferData snifferData = listaSniffers.get(cp.getSnifferMac());
                cp.setSnifferName(snifferData.getName());
                cp.setBuildingName(snifferData.getBuildingName());
                cp.setRoomName(snifferData.getRoomName());
                map.put(cp.hashCode(), cp);
            }
            for(CountResult crLocal: crLocalDump){
                if(map.containsKey(crLocal.hashCode())){
                    //if we have a match it means that we have already set an object with the global data and the same sniffer and the same timeframe
                    CountedPackets cp = map.get(crLocal.hashCode());
                    cp.setLocalPackets(crLocal.getTotPackets());
                    cp.setTotalDistinctFingerprints(crLocal.getTotMacs());
                    cp.setTotalPackets();
                    cp.setTotalEstimatedDevices();
                    SnifferData snifferData = listaSniffers.get(cp.getSnifferMac());
                    cp.setSnifferName(snifferData.getName());
                    cp.setBuildingName(snifferData.getBuildingName());
                    cp.setRoomName(snifferData.getRoomName());
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
                    SnifferData snifferData = listaSniffers.get(cp.getSnifferMac());
                    cp.setSnifferName(snifferData.getName());
                    cp.setBuildingName(snifferData.getBuildingName());
                    cp.setRoomName(snifferData.getRoomName());
                    map.put(cp.hashCode(), cp);
                }
            }
            for(CountResult crGlobalP: crGlobalParsed) {
                if (map.containsKey(crGlobalP.hashCode())) {
                    //if we have a match it means that we have already set an object with the global data and the same sniffer and the same timeframe
                    CountedPackets cp = map.get(crGlobalP.hashCode());
                    cp.setGlobalPackets(crGlobalP.getTotPackets()+cp.getGlobalPackets()); //qui sommiamo per tebnere conto del caso in cui uno sniffer passi da una modalità all'altra
                    cp.setTotalDistinctMacAddresses(crGlobalP.getTotMacs()+cp.getTotalDistinctMacAddresses());
                    cp.setTotalPackets();
                    cp.setTotalEstimatedDevices();
                    SnifferData snifferData = listaSniffers.get(cp.getSnifferMac());
                    cp.setSnifferName(snifferData.getName());
                    cp.setBuildingName(snifferData.getBuildingName());
                    cp.setRoomName(snifferData.getRoomName());
                    map.replace(cp.hashCode(), cp);
                } else {
                    //in this case we have data for the local and not for the global for the same sniffer and same timeframe
                    CountedPackets cp = new CountedPackets();
                    cp.setTimeFrame(crGlobalP.getTimeFrame());
                    cp.setGlobalPackets(crGlobalP.getTotPackets());
                    cp.setTotalDistinctMacAddresses(crGlobalP.getTotMacs());
                    cp.setLocalPackets(0);
                    cp.setTotalPackets();
                    cp.setTotalEstimatedDevices();
                    cp.setSnifferMac(crGlobalP.getTimeFrame().getSnifferMac());
                    SnifferData snifferData = listaSniffers.get(cp.getSnifferMac());
                    cp.setSnifferName(snifferData.getName());
                    cp.setBuildingName(snifferData.getBuildingName());
                    cp.setRoomName(snifferData.getRoomName());
                    map.put(cp.hashCode(), cp);
                }
            }
            for(CountResult crLocalP: crLocalParsed){
                if(map.containsKey(crLocalP.hashCode())){
                    //if we have a match it means that we have already set an object with the global data and the same sniffer and the same timeframe
                    CountedPackets cp = map.get(crLocalP.hashCode());
                    cp.setLocalPackets(crLocalP.getTotPackets() + cp.getLocalPackets());
                    cp.setTotalDistinctFingerprints(crLocalP.getTotMacs() + cp.getTotalDistinctFingerprints());
                    cp.setTotalPackets();
                    cp.setTotalEstimatedDevices();
                    SnifferData snifferData = listaSniffers.get(cp.getSnifferMac());
                    cp.setSnifferName(snifferData.getName());
                    cp.setBuildingName(snifferData.getBuildingName());
                    cp.setRoomName(snifferData.getRoomName());
                    map.replace(cp.hashCode(), cp);
                } else{
                    //in this case we have data for the local and not for the global for the same sniffer and same timeframe
                    CountedPackets cp = new CountedPackets();
                    cp.setTimeFrame(crLocalP.getTimeFrame());
                    cp.setLocalPackets(crLocalP.getTotPackets());
                    cp.setTotalDistinctFingerprints(crLocalP.getTotMacs());
                    cp.setGlobalPackets(0);
                    cp.setTotalPackets();
                    cp.setTotalEstimatedDevices();
                    cp.setSnifferMac(crLocalP.getTimeFrame().getSnifferMac());
                    SnifferData snifferData = listaSniffers.get(cp.getSnifferMac());
                    cp.setSnifferName(snifferData.getName());
                    cp.setBuildingName(snifferData.getBuildingName());
                    cp.setRoomName(snifferData.getRoomName());
                    map.put(cp.hashCode(), cp);
                }
            }
            countedPacketsRepository.saveAll(map.values());
            timestamp.setCounterTimestamp(lastSlot);
            provaRepository.save(timestamp); //da mettere al fondo
            Instant finish = Instant.now().atZone(ZoneId.of("CET")).toInstant();
            logger.info("Count task terminata in {} millisecondi", Duration.between(start, finish).toMillis());
    }
}
