package org.bigbluebutton.api.service.impl;

import org.bigbluebutton.api.messaging.messages.MakePresentationDownloadableMsg;
import org.bigbluebutton.api.model.entity.Metadata;
import org.bigbluebutton.api.model.entity.Recording;
import org.bigbluebutton.api.service.RecordingService;
import org.bigbluebutton.api.service.XmlService;
import org.bigbluebutton.api.util.DataStore;
import org.bigbluebutton.api.util.RecordingMetadataReaderHelper;
import org.bigbluebutton.api2.domain.UploadedTrack;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordingServiceDbImpl implements RecordingService {

    private static final Logger logger = LoggerFactory.getLogger(RecordingServiceDbImpl.class);

    private String processDir = "/var/bigbluebutton/recording/process";
    private String publishedDir = "/var/bigbluebutton/published";
    private String unpublishedDir = "/var/bigbluebutton/unpublished";
    private String deletedDir = "/var/bigbluebutton/deleted";

    private RecordingMetadataReaderHelper recordingServiceHelper;
    private String recordStatusDir;
    private String captionsDir;
    private String presentationBaseDir;
    private String defaultServerUrl;
    private String defaultTextTrackUrl;

    private DataStore dataStore;
    private XmlService xmlService;

    public RecordingServiceDbImpl() {
        dataStore = DataStore.getInstance();
        xmlService = new XmlServiceImpl();
    }

    @Override
    public Boolean validateTextTrackSingleUseToken(String recordId, String caption, String token) {
        return null;
    }

    @Override
    public String getRecordingTextTracks(String recordId) {
        return null;
    }

    @Override
    public String putRecordingTextTrack(UploadedTrack track) {
        return null;
    }

    @Override
    public String getCaptionTrackInboxDir() {
        return null;
    }

    @Override
    public String getCaptionsDir() {
        return null;
    }

    @Override
    public boolean isRecordingExist(String recordId) {
        return dataStore.findRecordingByRecordId(recordId) != null;
    }

    @Override
    public String getRecordings2x(List<String> idList, List<String> states, Map<String, String> metadataFilters) {
        Set<Recording> recordings = new HashSet<>();
        recordings.addAll(dataStore.findAll(Recording.class));

        Set<Recording> recordingsByState = new HashSet<>();
        for(String state: states) {
            List<Recording> r = dataStore.findRecordingsByState(state);
            if(r != null) recordingsByState.addAll(r);
        }

        if(recordingsByState.size() > 0) {
            recordings.retainAll(recordingsByState);
        }

        List<Metadata> metadata = new ArrayList<>();
        for(Map.Entry<String, String> metadataFilter: metadataFilters.entrySet()) {
            List<Metadata> m = dataStore.findMetadataByFilter(metadataFilter.getKey(), metadataFilter.getValue());
            if(m != null) metadata.addAll(m);
        }

        Set<Recording> recordingsByMetadata = new HashSet<>();
        for(Metadata m: metadata) {
            recordingsByMetadata.add(m.getRecording());
        }

        if(recordingsByMetadata.size() > 0) {
            recordings.retainAll(recordingsByMetadata);
        }

        return xmlService.recordingsToXml(recordings);
    }

    @Override
    public boolean existAnyRecording(List<String> idList) {
        for(String id: idList) {
            if(dataStore.findRecordingByRecordId(id) != null) return true;
        }
        return false;
    }

    @Override
    public boolean changeState(String recordingId, String state) {
        if(Stream.of(Recording.State.values()).anyMatch(x -> x.getValue().equals(state))) {
            Recording recording = dataStore.findRecordingByRecordId(recordingId);
            if(recording != null) {
                recording.setState(state);
                dataStore.save(recording);
                return true;
            } else {
                logger.error("A recording with ID {} does not exist", recordingId);
            }
        } else {
            logger.error("State [{}] is not a valid state", state);
        }
        return false;
    }

    @Override
    public void updateMetaParams(List<String> recordIDs, Map<String, String> metaParams) {
        Set<Recording> recordings = new HashSet<>();
        for(String id: recordIDs) {
            Recording recording = dataStore.findRecordingByRecordId(id);
            if(recording != null) recordings.add(recording);
        }

        for(Recording recording: recordings) {
            Set<Metadata> metadata = recording.getMetadata();

            for(Map.Entry<String, String> entry: metaParams.entrySet()) {
                for(Metadata m: metadata) {
                    if(m.getKey().equals(entry.getKey())) {
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

            dataStore.save(recording);
        }
    }

    @Override
    public void startIngestAndProcessing(String meetingId) {

    }

    @Override
    public void markAsEnded(String meetingId) {

    }

    @Override
    public void kickOffRecordingChapterBreak(String meetingId, Long timestamp) {

    }

    @Override
    public void processMakePresentationDownloadableMsg(MakePresentationDownloadableMsg msg) {

    }

    @Override
    public File getDownloadablePresentationFile(String meetingId, String presId, String presFilename) {
        return null;
    }

    public void setRecordingStatusDir(String dir) {
        recordStatusDir = dir;
    }

    public void setUnpublishedDir(String dir) {
        unpublishedDir = dir;
    }

    public void setPresentationBaseDir(String dir) {
        presentationBaseDir = dir;
    }

    public void setDefaultServerUrl(String url) {
        defaultServerUrl = url;
    }

    public void setDefaultTextTrackUrl(String url) {
        defaultTextTrackUrl = url;
    }

    public void setPublishedDir(String dir) {
        publishedDir = dir;
    }

    public void setCaptionsDir(String dir) {
        captionsDir = dir;
    }

    public void setRecordingServiceHelper(RecordingMetadataReaderHelper r) {
        recordingServiceHelper = r;
    }
}
