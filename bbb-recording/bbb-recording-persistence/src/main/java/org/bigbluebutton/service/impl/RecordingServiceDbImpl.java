package org.bigbluebutton.service.impl;

import org.bigbluebutton.dao.entity.Metadata;
import org.bigbluebutton.dao.entity.Recording;
import org.bigbluebutton.dao.repository.MetadataRepository;
import org.bigbluebutton.dao.repository.RecordingRepository;
import org.bigbluebutton.service.RecordingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Qualifier("dbImpl")
public class RecordingServiceDbImpl implements RecordingService {

    private static final Logger logger = LoggerFactory.getLogger(RecordingServiceDbImpl.class);

    private final RecordingRepository recordingRepository;
    private final MetadataRepository metadataRepository;

    public RecordingServiceDbImpl(RecordingRepository recordingRepository, MetadataRepository metadataRepository) {
        this.recordingRepository = recordingRepository;
        this.metadataRepository = metadataRepository;
    }

    @Override
    public List<Recording> searchRecordings(List<String> meetingIds, List<String> recordIds, List<String> states, Map<String, String> meta) {
        logger.info("Retrieving all recordings");
        Set<Recording> recordings = new HashSet<>(recordingRepository.findAll());

        Set<Recording> recordingsByMeetingId = new HashSet<>();
        for(String id: meetingIds) {
            List<Recording> r = recordingRepository.findByMeetingId(id);
            if(r != null) recordingsByMeetingId.addAll(r);
        }

        logger.info("Filtering recordings by meeting ID");
        if(recordingsByMeetingId.size() > 0) {
            recordings.retainAll(recordingsByMeetingId);
        }
        logger.info("{} recordings remaining", recordings.size());

        Set<Recording> recordingsByRecordId = new HashSet<>();
        for(String id: recordIds) {
            Optional<Recording> recording = recordingRepository.findByRecordId(id);
            recording.ifPresent(recordingsByRecordId::add);
        }

        logger.info("Filtering recordings by meeting ID");
        if(recordingsByRecordId.size() > 0) {
            recordings.retainAll(recordingsByRecordId);
        }
        logger.info("{} recordings remaining", recordings.size());

        Set<Recording> recordingsByState = new HashSet<>();
        for(String state: states) {
            List<Recording> r = recordingRepository.findByState(state);
            if(r != null) recordingsByState.addAll(r);
        }

        logger.info("Filtering recordings by state");
        if(recordingsByState.size() > 0) {
            recordings.retainAll(recordingsByState);
        }
        logger.info("{} recordings remaining", recordings.size());

        List<Metadata> metadata = new ArrayList<>();
        for(Map.Entry<String, String> filter: meta.entrySet()) {
            List<Metadata> m = metadataRepository.findByKeyAndValue(filter.getKey(), filter.getValue());
            if(m != null) metadata.addAll(m);
        }

        Set<Recording> recordingsByMetadata = new HashSet<>();
        for(Metadata m: metadata) {
            recordingsByMetadata.add(m.getRecording());
        }

        logger.info("Filtering recordings by metadata");
        if(recordingsByMetadata.size() > 0) {
            recordings.retainAll(recordingsByMetadata);
        }
        logger.info("{} recordings remaining", recordings.size());

        return List.copyOf(recordings);
    }

    @Override
    public Recording findRecording(String recordId) {
        Optional<Recording> recording = recordingRepository.findByRecordId(recordId);
        return recording.orElse(null);
    }

    @Override
    public Recording updateRecording(String recordId, Map<String, String> meta) {
        Optional<Recording> optional = recordingRepository.findByRecordId(recordId);

        if(optional.isPresent()) {
            Recording recording = optional.get();
            Set<Metadata> metadata = recording.getMetadata();

            for(Map.Entry<String, String> entry: meta.entrySet()) {
                for (Metadata m : metadata) {
                    if (m.getKey().equals(entry.getKey())) {
                        m.setValue(entry.getValue());
                    } else {
                        Metadata newParam = new Metadata();
                        newParam.setKey(entry.getKey());
                        newParam.setValue(entry.getValue());
                        newParam.setRecording(recording);
                        recording.addMetadata(newParam);
                    }
                }
            }

            recordingRepository.save(recording);
            return recording;
        }

        return null;
    }

    @Override
    public Recording publishRecording(String recordId, boolean publish) {
        Optional<Recording> optional = recordingRepository.findByRecordId(recordId);

        if(optional.isPresent()) {
            Recording recording = optional.get();
            recording.setPublished(publish);
            String state = (publish) ? Recording.State.STATE_PUBLISHED.getValue() : Recording.State.STATE_UNPUBLISHED.getValue();
            recording.setState(state);
            recordingRepository.save(recording);
            return recording;
        }

        return null;
    }

    @Override
    public boolean deleteRecording(String recordId) {
        Optional<Recording> recording = recordingRepository.findByRecordId(recordId);

        if(recording.isPresent()) {
            recordingRepository.delete(recording.get());
            return true;
        }

        return false;
    }
}
