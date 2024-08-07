package main.java.com.testing.document_management;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for store data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
    private final List<Document> storage = new LinkedList<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null) {
            document.setId(UUID.randomUUID().toString());
            document.setCreated(Instant.now());
        }
        else {
            Optional<Document> existingDocumentOp = findById(document.getId());
            if (existingDocumentOp.isPresent()) {
                Document existingDocument = existingDocumentOp.get();
                Instant created = existingDocument.getCreated();
                document.setCreated(created);
                storage.remove(existingDocument);
            }
            else {
                document.setCreated(Instant.now());
            }
        }

        storage.add(document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return storage.stream()
            .filter(document -> matches(document, request))
            .toList();
    }

    private boolean matches(Document document, SearchRequest request) {
        if (request.getTitlePrefixes() != null) {
            boolean titleMatches = request.getTitlePrefixes().stream()
                .anyMatch(prefix -> document.getTitle() != null && document.getTitle().startsWith(prefix));
            if (!titleMatches) return false;
        }

        if (request.getContainsContents() != null) {
            boolean contentMatches = request.getContainsContents().stream()
                .anyMatch(content -> document.getContent() != null && document.getContent().contains(content));
            if (!contentMatches) return false;
        }

        if (request.getAuthorIds() != null) {
            boolean authorMatches = request.getAuthorIds().contains(document.getAuthor().getId());
            if (!authorMatches) return false;
        }

        if (request.getCreatedFrom() != null && document.getCreated().isBefore(request.getCreatedFrom())) {
            return false;
        }
        if (request.getCreatedTo() != null && document.getCreated().isAfter(request.getCreatedTo())) {
            return false;
        }

        return true;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return storage.stream()
            .filter(document -> document.getId().equals(id))
            .findFirst();
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}