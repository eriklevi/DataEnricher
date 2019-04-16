package com.example.DataEnricher.tasks;


import com.example.DataEnricher.HelperMethods;
import com.example.DataEnricher.entities.*;
import com.example.DataEnricher.repositories.OUIRepository;
import com.example.DataEnricher.repositories.PacketRepository;
import com.example.DataEnricher.repositories.ProvaRepository;
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
    private final ProvaRepository provaRepository;

    @Autowired
    public RawPacketTask(TimestampRepository timestampRepository, PacketRepository packetRepository, OUIRepository ouiRepository, ProvaRepository provaRepository) {
        this.timestampRepository = timestampRepository;
        this.packetRepository = packetRepository;
        this.ouiRepository = ouiRepository;
        this.provaRepository = provaRepository;
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
        logger.info("Starting enricher task");
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
                        p.setCompleteDeviceOui(optionalOUIDevice.get().getCompleteName());
                    } else {
                        p.setDeviceOui("Unknown");
                        p.setCompleteDeviceOui("Unknown");
                    }

                List<TaggedParameter> newTags = new ArrayList<>();
                for (TaggedParameter tp : p.getTaggedParameters()) { //scan tagged parameters to find dd
                    if (tp.getTag().startsWith("dd")) {
                        String m = tp.getTag().substring(2, 8);
                        String newM = ""+m.charAt(0) + m.charAt(1) + ":" + m.charAt(2) + m.charAt(3) + ":" + m.charAt(4) + m.charAt(5);
                        optionalOUIDevice = ouiRepository.findByOui(newM);
                        if (optionalOUIDevice.isPresent()) {
                            TaggedParameterDD taggedParameterDD = new TaggedParameterDD(tp.getTag(), tp.getLength(), tp.getValue(), optionalOUIDevice.get().getShortName(), optionalOUIDevice.get().getCompleteName(), newM);
                            newTags.add(taggedParameterDD);
                        } else {
                            TaggedParameterDD taggedParameterDD = new TaggedParameterDD(tp.getTag(), tp.getLength(), tp.getValue(), "Unknown", "Unknown", newM);
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
                Integer lengthWithoutTag00 = (p.getTaggedParametersLength()/2)-p.getTaggedParameters().get(0).getLength(); //tag 00 is always first
                byte[] length = ByteBuffer.allocate(4).putInt(lengthWithoutTag00).array();
                String tagList = p.getTaggedParameters().stream()
                        .map( o -> o.getTag())
                        .collect(Collectors.joining(""));
                String contentList = p.getTaggedParameters().stream()
                        .filter( o -> {
                            String tag = o.getTag();
                            if(tag.equals("00") || tag.equals("03"))
                                return false;
                            return true;
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

    //qualcosa non va, non salva il timestamp giusto
    /*@Scheduled(fixedDelayString = "${tasks.RawPacket.delay :300000}")
    public void newFingerprint(){
        Instant start = Instant.now();
        logger.info("Starting fingerprint task");
        Prova prova = provaRepository.findAll().get(0); //we should have only 1 entry in the collection
        final long lastTStamp = prova.getTimestamp(); //this is the timestamp of the last processed packet
        //this is to store the value of the last processed packet inside the lambda, it always contains the timestamp
        // of the last processed packet
        AtomicLong timestampOut = new AtomicLong(lastTStamp);
        long endTimestamp = Instant.now().toEpochMilli(); //this is the current time timestamp
        Stream<Packet> stream = packetRepository.findAllByTimestampBetween(lastTStamp, endTimestamp);
        stream.forEach( p -> {
            try {
                //calcoliamo fingerprint
                String contentList = p.getTaggedParameters().stream()
                        .filter( o -> {
                            String tag = o.getTag();
                            if(tag.equals("00") || tag.equals("03") || tag.equals("dd0050f208") ||tag.equals("dd00904c04") || tag.equals("6b") || tag.equals("7f") || tag.equals("2d"))
                                return false;
                            return true;
                        })
                        .map(o -> o.getValue())
                        .collect(Collectors.joining(""));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(contentList.getBytes());
                //data = (lunghezzaPayload - lunghezzaSSID) + stringa dei tag + contenuto dei tag scelti
                p.setFingerprintv2(HelperMethods.bytesToHex(DigestUtils.md5Digest(outputStream.toByteArray())));
                contentList = p.getTaggedParameters().stream()
                        .filter( o -> {
                            String tag = o.getTag();
                            if(tag.equals("00") || tag.equals("03") ||tag.equals("6b"))
                                return false;
                            return true;
                        })
                        .map(o -> {
                            String tag = o.getTag();
                            String value = o.getValue();
                            if(tag.equals("dd0050f208"))
                                return value.substring(0,value.length()-2);
                            if(tag.equals("dd00904c04")){
                                if(value.length() >= 20)
                                    return value.substring(0,14)+value.substring(20);
                            }
                            if(tag.equals("2d")){
                                if(value.length()>=10)
                                    return value.substring(2,8)+value.substring(10);
                            }
                            if(tag.equals("7f")){
                                if(value.length() >= 12)
                                    return value.substring(0,6)+value.substring(8,12);
                            }
                            //default case
                            return value;
                        })
                        .collect(Collectors.joining(""));
                ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
                outputStream2.write(contentList.getBytes());
                //data = (lunghezzaPayload - lunghezzaSSID) + stringa dei tag + contenuto dei tag scelti
                p.setFingerprintv3(HelperMethods.bytesToHex(DigestUtils.md5Digest(outputStream2.toByteArray())));
                packetRepository.save(p);
                if (p.getTimestamp() > timestampOut.get())
                    timestampOut.set(p.getTimestamp());

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                prova.setTimestamp(timestampOut.get());
                provaRepository.save(prova);
                Instant finish = Instant.now();
                long timeElapsed = Duration.between(start,finish).toMinutes();
                logger.error("fingerprint task aborted after "+ timeElapsed+ " minutes");
            }
        } );
        prova.setTimestamp(timestampOut.get());
        provaRepository.save(prova);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start,finish).toMinutes();
        logger.info("updateData task completed. Took "+ timeElapsed+ " minutes");
    }*/
}

