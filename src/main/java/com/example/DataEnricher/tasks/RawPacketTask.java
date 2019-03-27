package com.example.DataEnricher.tasks;


import com.example.DataEnricher.HelperMethods;
import com.example.DataEnricher.entities.*;
import com.example.DataEnricher.repositories.OUIRepository;
import com.example.DataEnricher.repositories.PacketRepository;
import com.example.DataEnricher.repositories.TimestampRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.time.*;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RawPacketTask {

    private Logger logger = LoggerFactory.getLogger(RawPacketTask.class);

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
        logger.info("Starting updateData task");
        Timestamp timestamp = timestampRepository.findAll().get(0); //we should have only 1 entry in the collection
        final long lastTStamp = timestamp.getTimestamp(); //this is the timestamp of the last processed packet
        //this is to store the value of the last processed packet inside the lambda, it always contains the timestamp
        // of the last processed packet
        AtomicLong timestampOut = new AtomicLong(lastTStamp);
        long endTimestamp = Instant.now().toEpochMilli(); //this is the current time timestamp
        Stream<Packet> stream = packetRepository.findAllByTimestampBetween(lastTStamp, endTimestamp);
        stream.forEach( p -> {
            try {
                    Optional<OUI> optionalOUIDevice = ouiRepository.findByOui(p.getDeviceMac().substring(0, 8));
                    if (optionalOUIDevice.isPresent()) {
                        p.setDeviceOui(optionalOUIDevice.get().getShortName()); //set device mac oui
                    } else {
                        p.setDeviceOui("Unknown");
                    }

                List<TaggedParameter> newTags = new ArrayList<>();
                for (TaggedParameter tp : p.getTaggedParameters()) { //scan tagged parameters to find dd
                    if (tp.getTag().startsWith("dd")) {
                        String m = tp.getTag().substring(2, 8);
                        String newM = ""+m.charAt(0) + m.charAt(1) + ":" + m.charAt(2) + m.charAt(3) + ":" + m.charAt(4) + m.charAt(5);
                        optionalOUIDevice = ouiRepository.findByOui(newM);
                        if (optionalOUIDevice.isPresent()) {
                            TaggedParameterDD taggedParameterDD = new TaggedParameterDD(tp.getTag(), tp.getLength(), tp.getValue(), optionalOUIDevice.get().getShortName(), optionalOUIDevice.get().getCompleteName());
                            newTags.add(taggedParameterDD);
                        } else {
                            TaggedParameterDD taggedParameterDD = new TaggedParameterDD(tp.getTag(), tp.getLength(), tp.getValue(), "Unknown", "Unknown");
                            newTags.add(taggedParameterDD);
                        }
                    } else {
                        newTags.add(tp);

                    }
                }
                p.setTaggedParameters(newTags);
                LocalDateTime t = Instant.ofEpochMilli(p.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                p.setYear(t.getYear());
                p.setMonth(t.getMonthValue());
                p.setWeekOfYear(t.get(WeekFields.ISO.weekOfYear()));
                p.setDayOfMonth(t.getDayOfMonth());
                p.setDayOfWeek(t.getDayOfWeek().getValue());
                p.setHour(t.getHour());
                p.setQuarter(t.getMinute()/15+1); //raggruppo su 15 minuti 1-4...0-14 15-29.....
                p.setFiveMinute(t.getMinute()/5+1); //raggruppo per 5 minuti 1-12...0-4 5-9.....
                p.setMinute(t.getMinute());
                //calcoliamo fingerprint
                Integer lengthWithoutTag00 = p.getTaggedParametersLength()-p.getTaggedParameters().get(0).getLength(); //tag 00 is always first
                byte[] length = ByteBuffer.allocate(4).putInt(lengthWithoutTag00).array();
                String tagList = p.getTaggedParameters().stream()
                        .map( o -> o.getTag())
                        .collect(Collectors.joining(""));
                String contentList = p.getTaggedParameters().stream()
                        .filter( o -> {
                            String tag = o.getTag();
                            if(tag.equals("00") || tag.equals("03"))
                                return false;
                            return false;
                        })
                        .map(o -> o.getValue())
                        .collect(Collectors.joining(""));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(length);
                outputStream.write(tagList.getBytes());
                outputStream.write(contentList.getBytes());
                //data = (lunghezzaPayload - lunghezzaSSID) + stringa dei tag + contenuto dei tag scelti
                p.setFingerprint(HelperMethods.bytesToHex(DigestUtils.md5Digest(outputStream.toByteArray())));
                packetRepository.save(p);
                if (p.getTimestamp() > timestampOut.get())
                    timestampOut.set(p.getTimestamp());

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                timestamp.setTimestamp(timestampOut.get());
                timestampRepository.save(timestamp);
                Instant finish = Instant.now();
                long timeElapsed = Duration.between(start,finish).toMinutes();
                logger.error("updateData task aborted after "+ timeElapsed+ " minutes");
            }
        } );
        timestamp.setTimestamp(timestampOut.get());
        timestampRepository.save(timestamp);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start,finish).toMinutes();
        logger.info("updateData task completed. Took "+ timeElapsed+ " minutes");
    }
}

