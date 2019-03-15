package com.example.DataEnricher.tasks;


import com.example.DataEnricher.entities.*;
import com.example.DataEnricher.repositories.OUIRepository;
import com.example.DataEnricher.repositories.PacketRepository;
import com.example.DataEnricher.repositories.TimestampRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Component
public class RawPacketTask {

    Logger logger = LoggerFactory.getLogger(RawPacketTask.class);

    private final TimestampRepository timestampRepository;
    private final PacketRepository packetRepository;
    private final OUIRepository ouiRepository;

    @Autowired
    public RawPacketTask(TimestampRepository timestampRepository, PacketRepository packetRepository, OUIRepository ouiRepository) {
        this.timestampRepository = timestampRepository;
        this.packetRepository = packetRepository;
        this.ouiRepository = ouiRepository;
    }

    /**
     * This task scans the rawPackets collection every 5 minutes (300000 ms, by default).
     * Since we don't know a priori the time needed to end the task we have to use fixedDelay attribute to ensure that
     * the time elapsed between two consecutive execution of the task is at least tasks.RawPacket.delay ms and that the
     * previous execution is effectively terminated.
     * --> The fixedDelay property makes sure that there is a delay of n millisecond between the finish time of an
     * execution of a task and the start time of the next execution of the task.
     * --> The fixedRate property runs the scheduled task at every n milliseconds.
     */
    @Scheduled(fixedDelayString = "${tasks.RawPacket.delay :300000}")
    public void updateData(){
        Instant start = Instant.now();
        long endTimestamp = Instant.now().toEpochMilli();
        logger.info("Starting updateData task");
        Timestamp timestamp = timestampRepository.findAll().get(0); //we should have only 1 entry in the collection
        final long lastTStamp = timestamp.getTimestamp();
        AtomicLong timestampOut = new AtomicLong(lastTStamp);
        Stream<Packet> stream = packetRepository.findAllByTimestampBetween(lastTStamp, endTimestamp);
        stream.forEach( p -> {
            Optional<OUI> optionalOUIDevice = ouiRepository.findByOui(p.getDeviceMac().substring(0, 8));
            if(optionalOUIDevice.isPresent()) {
                p.setDeviceOUI(optionalOUIDevice.get().getShortName()); //set device mac oui
            } else {
                p.setDeviceOUI("Unknown");
            }
            List<TaggedParameter> newTags = new ArrayList<>();
            for(TaggedParameter tp: p.getTaggedParameters()){ //scan tagged parameters to find dd
                if(tp.getTag().startsWith("dd")){
                    String m = tp.getTag().substring(2,8);
                    String newM = m.charAt(0)+m.charAt(1)+":"+m.charAt(2)+m.charAt(3)+":"+m.charAt(4)+m.charAt(5);
                    optionalOUIDevice = ouiRepository.findByOui(newM);
                    if(optionalOUIDevice.isPresent()){
                        TaggedParameterDD taggedParameterDD = new TaggedParameterDD(tp.getTag(),tp.getLength(), tp.getValue(),optionalOUIDevice.get().getShortName());
                        newTags.add(taggedParameterDD);
                    } else{
                        TaggedParameterDD taggedParameterDD = new TaggedParameterDD(tp.getTag(),tp.getLength(), tp.getValue(),"Unknown");
                        newTags.add(taggedParameterDD);
                    }
                }else{
                    newTags.add(tp);
                }
            }
            p.setTaggedParameters(newTags);
            packetRepository.save(p);
            if(p.getTimestamp() > lastTStamp)
                timestampOut.set(p.getTimestamp());
        } );
        timestamp.setTimestamp(timestampOut.get());
        timestampRepository.save(timestamp);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start,finish).toMinutes();
        logger.info("updateData task took "+ timeElapsed+ "minutes");
    }
}

