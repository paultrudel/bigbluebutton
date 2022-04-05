package org.bigbluebutton.api.service.impl;

import org.bigbluebutton.api.model.entity.*;
import org.bigbluebutton.api.service.XmlService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import org.springframework.data.domain.*;

public class XmlServiceImpl implements XmlService {

    private static Logger logger = LoggerFactory.getLogger(XmlServiceImpl.class);

    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;

    @Override
    public String recordingsToXml(Collection<Recording> recordings) {
        logger.info("Converting {} recordings to xml", recordings.size());
        try {
            setup();
            Document document = builder.newDocument();

            Element rootElement = createElement(document, "recordings", null);
            document.appendChild(rootElement);

            String xml;
            Document secondDoc;
            Node node;

            for(Recording recording: recordings) {
                xml = recordingToXml(recording);
                secondDoc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
                node = document.importNode(secondDoc.getDocumentElement(), true);
                rootElement.appendChild(node);
            }

            String result = documentToString(document);
            logger.info("========== Result ==========");
            logger.info("{}", result);
            logger.info("============================");
            return result;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String recordingToXml(Recording recording) {
        logger.info("Converting {} to xml", recording);
        try {
            setup();
            Document document = builder.newDocument();

            Element rootElement = createElement(document,"recording", null);
            document.appendChild(rootElement);
            appendFields(document, rootElement, recording, new String[] {"id", "metadata", "format", "callbackData"}, Type.CHILD);

            Element meta = createElement(document, "meta", null);
            rootElement.appendChild(meta);

            String xml;
            Document secondDoc;
            Node node;

            if(recording.getMetadata() != null) {
                for(Metadata metadata: recording.getMetadata()) {
                    xml = metadataToXml(metadata);
                    secondDoc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
                    node = document.importNode(secondDoc.getDocumentElement(), true);
                    meta.appendChild(node);
                }
            }

            if(recording.getFormat() != null) {
                xml = playbackFormatToXml(recording.getFormat());
                secondDoc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
                node = document.importNode(secondDoc.getDocumentElement(), true);
                rootElement.appendChild(node);
            }

            if(recording.getCallbackData() != null) {
                xml = callbackDataToXml(recording.getCallbackData());
                secondDoc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
                node = document.importNode(secondDoc.getDocumentElement(), true);
                rootElement.appendChild(node);
            }

            String result = documentToString(document);
            logger.info("========== Result ==========");
            logger.info("{}", result);
            logger.info("============================");
            return result;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String metadataToXml(Metadata metadata) {
        logger.info("Converting {} to xml", metadata);

        try {
            setup();
            Document document = builder.newDocument();

            Element rootElement = createElement(document, metadata.getKey(), metadata.getValue());
            document.appendChild(rootElement);

            String result = documentToString(document);
            logger.info("========== Result ==========");
            logger.info("{}", result);
            logger.info("============================");
            return result;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String playbackFormatToXml(PlaybackFormat playbackFormat) {
        logger.info("Converting {} to xml", playbackFormat);

        try {
            setup();
            Document document = builder.newDocument();

            Element rootElement = createElement(document, "playback", null);
            document.appendChild(rootElement);
            appendFields(document, rootElement, playbackFormat, new String[] {"id", "recording", "thumbnails"}, Type.CHILD);

            if(playbackFormat.getThumbnails() != null && !playbackFormat.getThumbnails().isEmpty()) {
                Element images = createElement(document, "images", null);
                rootElement.appendChild(images);

                List<Thumbnail> thumbnails = new ArrayList<>(playbackFormat.getThumbnails());
                Collections.sort(thumbnails);

                for(Thumbnail thumbnail: thumbnails) {
                    String xml = thumbnailToXml(thumbnail);
                    Document thumbnailDoc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
                    Node node = document.importNode(thumbnailDoc.getDocumentElement(), true);
                    images.appendChild(node);
                }
            }

            String result = documentToString(document);
            logger.info("========== Result ==========");
            logger.info("{}", result);
            logger.info("============================");
            return result;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String thumbnailToXml(Thumbnail thumbnail) {
        logger.info("Converting {} to xml", thumbnail);

        try {
            setup();
            Document document = builder.newDocument();

            Element rootElement = createElement(document, "image", thumbnail.getUrl());
            document.appendChild(rootElement);
            appendFields(document, rootElement, thumbnail, new String[] {"id", "url", "playbackFormat"}, Type.ATTRIBUTE);

            String result = documentToString(document);
            logger.info("========== Result ==========");
            logger.info("{}", result);
            logger.info("============================");
            return result;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String callbackDataToXml(CallbackData callbackData) {
        logger.info("Converting {} to xml", callbackData);

        try {
            setup();
            Document document = builder.newDocument();

            Element rootElement = createElement(document, "callback", null);
            document.appendChild(rootElement);
            appendFields(document, rootElement, callbackData, new String[] {"id", "recording"}, Type.CHILD);

            String result = documentToString(document);
            logger.info("========== Result ==========");
            logger.info("{}", result);
            logger.info("============================");
            return result;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String constructResponseFromRecordingsXml(String xml) {
        logger.info("Constructing response from recordings xml");

        try {
            setup();
            Document document = builder.newDocument();

            Element rootElement = createElement(document, "response", null);
            document.appendChild(rootElement);

            Element returnCode = createElement(document, "returncode", "SUCCESS");
            rootElement.appendChild(returnCode);

            Document recordingsDoc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            Node recordingsNode = document.importNode(recordingsDoc.getDocumentElement(), true);
            rootElement.appendChild(recordingsNode);

            String result = documentToString(document);
            logger.info("========== Result ==========");
            logger.info("{}", result);
            logger.info("============================");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String constructPaginatedResponse(Page<?> page, String response) {
        logger.info("Constructing paginated response");

        try {
            setup();

            if(response == null || response.equals("")) {
                return null;
            }

            Document document = builder.parse(new ByteArrayInputStream(response.getBytes()));
            Element rootElement = document.getDocumentElement();

            Element pagination = createElement(document, "pagination", null);

            String xml;
            Document secondDoc;
            Node node;

            xml = pageableToXml(page.getPageable());
            secondDoc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            node = document.importNode(secondDoc.getDocumentElement(), true);
            pagination.appendChild(node);

            Element totalElements = createElement(document, "totalElements", String.valueOf(page.getNumberOfElements()));
            pagination.appendChild(totalElements);

            Element last = createElement(document, "last", String.valueOf(page.isLast()));
            pagination.appendChild(last);

            Element totalPages = createElement(document, "totalPages",
                    String.valueOf(Math.ceil(page.getNumberOfElements() / page.getSize())));
            pagination.appendChild(totalPages);

            Element first = createElement(document, "first", String.valueOf(page.isFirst()));
            pagination.appendChild(first);

            Element empty = createElement(document, "empty", String.valueOf(!page.hasContent()));
            pagination.appendChild(empty);

            rootElement.appendChild(pagination);

            String result = documentToString(document);
            logger.info("========== Result ==========");
            logger.info("{}", result);
            logger.info("============================");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String pageableToXml(Pageable pageable) {
        logger.info("Converting {} to xml", pageable);

        try {
            setup();
            Document document = builder.newDocument();

            Element rootElement = createElement(document, "pageable", null);

            Sort sort = pageable.getSort();
            Element sortElement = createElement(document, "sort", null);
            logger.info("Sort {}", sort);

            Element offset = createElement(document, "offset", String.valueOf(pageable.getOffset()));
            rootElement.appendChild(offset);

            Element pageSize = createElement(document, "pageSize", String.valueOf(pageable.getPageSize()));
            rootElement.appendChild(pageSize);

            Element pageNumber = createElement(document, "pageNumber", String.valueOf(pageable.getPageNumber()));
            rootElement.appendChild(pageNumber);

            Element paged = createElement(document, "paged", String.valueOf(pageable.isPaged()));
            rootElement.appendChild(paged);

            Element unpaged = createElement(document, "unpaged", String.valueOf(pageable.isUnpaged()));
            rootElement.appendChild(unpaged);

            String result = documentToString(document);
            logger.info("========== Result ==========");
            logger.info("{}", result);
            logger.info("============================");
            return result;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void setup() throws ParserConfigurationException {
        if(factory == null) factory = DocumentBuilderFactory.newInstance();
        if(builder == null) builder = factory.newDocumentBuilder();
    }

    private Element createElement(Document document, String name, String value) {
        Element element = document.createElement(name);
        if(value != null) element.setTextContent(value);
        return element;
    }

    public String documentToString(Document document) {
        String output = null;

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            output = writer.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return output;
    }

    private void appendFields(Document document, Element parent, Object object, String[] ignoredFields, Type type) throws IllegalAccessException {
        Field[] fields = object.getClass().getDeclaredFields();

        for(Field field: fields) {
            if(Arrays.stream(ignoredFields).anyMatch(field.getName()::equals)) continue;
            field.setAccessible(true);
            Object fieldValue = field.get(object);
            if(fieldValue != null) {
                if(fieldValue instanceof LocalDateTime) {
                    fieldValue = localDateTimeToEpoch((LocalDateTime) fieldValue);
                }

                switch(type) {
                    case CHILD:
                        Element child = createElement(document, field.getName(), fieldValue.toString());
                        parent.appendChild(child);
                        break;
                    case ATTRIBUTE:
                        parent.setAttribute(field.getName(), fieldValue.toString());
                        break;
                }
            }
        }

    }

    private String localDateTimeToEpoch(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return String.valueOf(instant.toEpochMilli());
    }

    private enum Type {
        CHILD,
        ATTRIBUTE
    }
}