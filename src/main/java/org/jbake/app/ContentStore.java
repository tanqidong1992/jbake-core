/*
 * The MIT License
 *
 * Copyright 2015 jdlee.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jbake.app;

import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.jbake.model.DocumentAttributes;
import org.jbake.model.DocumentTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author jdlee
 */
public class ContentStore {

    private static final String STATEMENT_GET_PUBLISHED_POST_BY_TYPE_AND_TAG = "select * from %s where status='published' and ? in tags order by date desc";
    private static final String STATEMENT_GET_DOCUMENT_STATUS_BY_DOCTYPE_AND_URI = "select sha1,rendered from %s where sourceuri=?";
    private static final String STATEMENT_GET_PUBLISHED_COUNT = "select count(*) as count from %s where status='published'";
    private static final String STATEMENT_MARK_CONTENT_AS_RENDERD = "update %s set rendered=true where rendered=false and cached=true";
    private static final String STATEMENT_DELETE_DOCTYPE_BY_SOURCEURI = "delete from %s where sourceuri=?";
    private static final String STATEMENT_GET_UNDRENDERED_CONTENT = "select * from %s where rendered=false order by date desc";
    private static final String STATEMENT_GET_SIGNATURE_FOR_TEMPLATES = "select sha1 from Signatures where key='templates'";
    private static final String STATEMENT_GET_TAGS_FROM_PUBLISHED_POSTS = "select tags from post where status='published'";
    private static final String STATEMENT_GET_ALL_CONTENT_BY_DOCTYPE = "select * from %s order by date desc";
    private static final String STATEMENT_GET_PUBLISHED_CONTENT_BY_DOCTYPE = "select * from %s where status='published' order by date desc";
    private static final String STATEMENT_GET_PUBLISHED_POSTS_BY_TAG = "select * from post where status='published' and ? in tags order by date desc";
    private static final String STATEMENT_GET_TAGS_BY_DOCTYPE = "select tags from %s where status='published'";
    private static final String STATEMENT_INSERT_TEMPLATES_SIGNATURE = "insert into Signatures(key,sha1) values('templates',?)";
    private static final String STATEMENT_DELETE_ALL = "delete from %s";
    private static final String STATEMENT_UPDATE_TEMPLATE_SIGNATURE = "update Signatures set sha1=? where key='templates'";

    private final Logger logger = LoggerFactory.getLogger(ContentStore.class);
    private final String type;
    private final String name;

    private ODatabaseSession db;

    private long start = -1;
    private long limit = -1;
    private OrientDB orient;

    public ContentStore(final String type, String name) {
        this.type = type;
        this.name = name;
    }


    public void startup() {
        startupIfEnginesAreMissing();

        if (type.equalsIgnoreCase(ODatabaseType.PLOCAL.name())) {
            orient = new OrientDB(type + ":" + name, OrientDBConfig.defaultConfig());
        } else {
            orient = new OrientDB(type + ":", OrientDBConfig.defaultConfig());
        }

        orient.createIfNotExists(name, ODatabaseType.valueOf(type.toUpperCase()));

        db = orient.open(name, "admin", "admin");

        activateOnCurrentThread();

        updateSchema();
    }

    public long getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void resetPagination() {
        this.start = -1;
        this.limit = -1;
    }

    public final void updateSchema() {

        OSchema schema = db.getMetadata().getSchema();

        for (String docType : DocumentTypes.getDocumentTypes()) {
            if (!schema.existsClass(docType)) {
                createDocType(schema, docType);
            }
        }
        if (!schema.existsClass("Signatures")) {
            createSignatureType(schema);
        }
    }

    public void close() {
        if (db != null) {
            activateOnCurrentThread();
            db.close();
        }

        if (orient != null) {
            orient.close();
        }
        DBUtil.closeDataStore();
    }

    public void shutdown() {

//        Orient.instance().shutdown();
    }

    private void startupIfEnginesAreMissing() {
        // Using a jdk which doesn't bundle a javascript engine
        // throws a NoClassDefFoundError while logging the warning
        // see https://github.com/orientechnologies/orientdb/issues/5855
        OLogManager.instance().setWarnEnabled(false);

        // If an instance of Orient was previously shutdown all engines are removed.
        // We need to startup Orient again.
        if (Orient.instance().getEngines().isEmpty()) {
            Orient.instance().startup();
        }
        OLogManager.instance().setWarnEnabled(true);
    }

    public void drop() {
        activateOnCurrentThread();
//        db.drop();

        orient.drop(name);
    }

    private void activateOnCurrentThread() {
        if (db != null) {
            db.activateOnCurrentThread();
        } else {
            System.out.println("db is null on activate");
        }
    }


    /**
     * Get a document by sourceUri and update it from the given map.
     * @param incomingDocMap The document's db columns.
     * @return The saved document.
     * @throws IllegalArgumentException if sourceUri or docType are null, or if the document doesn't exist.
     */
    public ODocument mergeDocument(Map<String, ? extends Object> incomingDocMap) {
        String sourceUri = (String) incomingDocMap.get(DocumentAttributes.SOURCE_URI.toString());

        if (null == sourceUri) {
            throw new IllegalArgumentException("Document sourceUri is null.");
        }

        String docType = (String) incomingDocMap.get(Crawler.Attributes.TYPE);

        if (null == docType) {
            throw new IllegalArgumentException("Document docType is null.");
        }

        // Get a document by sourceUri
        String sql = "SELECT * FROM " + docType + " WHERE sourceuri=?";
        activateOnCurrentThread();
        List<ODocument> results = db.command(new OSQLSynchQuery<ODocument>(sql)).execute(sourceUri);
        if (results.isEmpty()) {
            throw new JBakeException("No document with sourceUri '" + sourceUri + "'.");
        }

        // Update it from the given map.
        ODocument incomingDoc = new ODocument(docType);
        incomingDoc.fromMap(incomingDocMap);
        ODocument merged = results.get(0).merge(incomingDoc, true, false);
        return merged;
    }


    public long getDocumentCount(String docType) {
        activateOnCurrentThread();
        return db.countClass(docType);
    }

    public long getPublishedCount(String docType) {
        String statement = String.format(STATEMENT_GET_PUBLISHED_COUNT, docType);
        return (Long) query(statement).get(0).get("count");
    }

    /*
     * In fact, the URI should be the only input as there can only be one document at given URI; but the DB is split per document type for some reason.
     */
    public DocumentList getDocumentByUri(String docType, String uri) {
        return query("select * from " + docType + " where sourceuri=?", uri);
    }

    public DocumentList getDocumentStatus(String docType, String uri) {
        String statement = String.format(STATEMENT_GET_DOCUMENT_STATUS_BY_DOCTYPE_AND_URI, docType);
        return query(statement, uri);
    }

    public DocumentList getPublishedPosts() {
        return getPublishedContent("post");
    }

    public DocumentList getPublishedPosts(boolean applyPaging) {
        return getPublishedContent("post", applyPaging);
    }

    public DocumentList getPublishedPostsByTag(String tag) {
        return query(STATEMENT_GET_PUBLISHED_POSTS_BY_TAG, tag);
    }

    public DocumentList getPublishedDocumentsByTag(String tag) {
        final DocumentList documents = new DocumentList();

        for (final String docType : DocumentTypes.getDocumentTypes()) {
            String statement = String.format(STATEMENT_GET_PUBLISHED_POST_BY_TYPE_AND_TAG, docType);
            DocumentList documentsByTag = query(statement, tag);
            documents.addAll(documentsByTag);
        }
        return documents;
    }

    public DocumentList getPublishedPages() {
        return getPublishedContent("page");
    }

    public DocumentList getPublishedContent(String docType) {
        return getPublishedContent(docType, false);
    }

    private DocumentList getPublishedContent(String docType, boolean applyPaging) {
        String query = String.format(STATEMENT_GET_PUBLISHED_CONTENT_BY_DOCTYPE, docType);
        if (applyPaging && hasStartAndLimitBoundary()) {
            query += " SKIP " + start + " LIMIT " + limit;
        }
        return query(query);
    }

    public DocumentList getAllContent(String docType) {
        return getAllContent(docType, false);
    }

    public DocumentList getAllContent(String docType, boolean applyPaging) {
        String query = String.format(STATEMENT_GET_ALL_CONTENT_BY_DOCTYPE, docType);
        if (applyPaging && hasStartAndLimitBoundary()) {
            query += " SKIP " + start + " LIMIT " + limit;
        }
        return query(query);
    }

    private boolean hasStartAndLimitBoundary() {
        return (start >= 0) && (limit > -1);
    }

    private DocumentList getAllTagsFromPublishedPosts() {
        return query(STATEMENT_GET_TAGS_FROM_PUBLISHED_POSTS);
    }

    private DocumentList getSignaturesForTemplates() {
        return query(STATEMENT_GET_SIGNATURE_FOR_TEMPLATES);
    }

    public DocumentList getUnrenderedContent(String docType) {
        String statement = String.format(STATEMENT_GET_UNDRENDERED_CONTENT, docType);
        return query(statement);
    }

    public void deleteContent(String docType, String uri) {
        String statement = String.format(STATEMENT_DELETE_DOCTYPE_BY_SOURCEURI, docType);
        executeCommand(statement, uri);
    }

    public void markContentAsRendered(String docType) {
        String statement = String.format(STATEMENT_MARK_CONTENT_AS_RENDERD, docType);
        executeCommand(statement);
    }

    private void updateSignatures(String currentTemplatesSignature) {
        executeCommand(STATEMENT_UPDATE_TEMPLATE_SIGNATURE, currentTemplatesSignature);
    }

    public void deleteAllByDocType(String docType) {
        String statement = String.format(STATEMENT_DELETE_ALL, docType);
        executeCommand(statement);
    }

    private void insertTemplatesSignature(String currentTemplatesSignature) {
        executeCommand(STATEMENT_INSERT_TEMPLATES_SIGNATURE, currentTemplatesSignature);
    }

    private DocumentList query(String sql) {
        activateOnCurrentThread();
        OResultSet results = db.query(sql);
        return DocumentList.wrap(results);
    }

    private DocumentList query(String sql, Object... args) {
        activateOnCurrentThread();
        OResultSet results = db.command(sql, args);
        return DocumentList.wrap(results);
    }

    private void executeCommand(String query, Object... args) {
        activateOnCurrentThread();
        db.command(new OCommandSQL(query)).execute(args);
    }

    public Set<String> getTags() {
        DocumentList docs = this.getAllTagsFromPublishedPosts();
        Set<String> result = new HashSet<>();
        for (Map<String, Object> document : docs) {
            String[] tags = DBUtil.toStringArray(document.get(Crawler.Attributes.TAGS));
            Collections.addAll(result, tags);
        }
        return result;
    }

    public Set<String> getAllTags() {
        Set<String> result = new HashSet<>();
        for (String docType : DocumentTypes.getDocumentTypes()) {
            String statement = String.format(STATEMENT_GET_TAGS_BY_DOCTYPE, docType);
            DocumentList docs = query(statement);
            for (Map<String, Object> document : docs) {
                String[] tags = DBUtil.toStringArray(document.get(Crawler.Attributes.TAGS));
                Collections.addAll(result, tags);
            }
        }
        return result;
    }

    private void createDocType(final OSchema schema, final String docType) {
        logger.debug("Create document class '{}'", docType);


        OClass page = schema.createClass(docType);

        // Primary key
        String attribName = DocumentAttributes.SOURCE_URI.toString();
        page.createProperty(attribName, OType.STRING).setNotNull(true);
        page.createIndex(docType + "sourceUriIndex", OClass.INDEX_TYPE.UNIQUE, attribName);

        attribName = DocumentAttributes.SHA1.toString();
        page.createProperty(attribName, OType.STRING).setNotNull(true);
        page.createIndex(docType + "sha1Index", OClass.INDEX_TYPE.NOTUNIQUE, attribName);

        attribName = DocumentAttributes.CACHED.toString();
        page.createProperty(attribName, OType.BOOLEAN).setNotNull(true);
        page.createIndex(docType + "cachedIndex", OClass.INDEX_TYPE.NOTUNIQUE, attribName);

        attribName = DocumentAttributes.RENDERED.toString();
        page.createProperty(attribName, OType.BOOLEAN).setNotNull(true);
        page.createIndex(docType + "renderedIndex", OClass.INDEX_TYPE.NOTUNIQUE, attribName);

        attribName = DocumentAttributes.STATUS.toString();
        page.createProperty(attribName, OType.STRING).setNotNull(true);
        page.createIndex(docType + "statusIndex", OClass.INDEX_TYPE.NOTUNIQUE, attribName);
    }

    private void createSignatureType(OSchema schema) {
        OClass signatures = schema.createClass("Signatures");
        signatures.createProperty(String.valueOf(DocumentAttributes.SHA1), OType.STRING).setNotNull(true);
        signatures.createIndex("sha1Idx", OClass.INDEX_TYPE.UNIQUE, DocumentAttributes.SHA1.toString());
    }

    public void updateAndClearCacheIfNeeded(boolean needed, File templateFolder) {

        boolean clearCache = needed;

        if (!needed) {
            clearCache = updateTemplateSignatureIfChanged(templateFolder);
        }

        if (clearCache) {
            deleteAllDocumentTypes();
            this.updateSchema();
        }
    }

    private boolean updateTemplateSignatureIfChanged(File templateFolder) {
        boolean templateSignatureChanged = false;

        DocumentList docs = this.getSignaturesForTemplates();
        String currentTemplatesSignature;
        try {
            currentTemplatesSignature = FileUtil.sha1(templateFolder);
        } catch (Exception e) {
            currentTemplatesSignature = "";
        }
        if (!docs.isEmpty()) {
            String sha1 = (String) docs.get(0).get(String.valueOf(DocumentAttributes.SHA1));
            if (!sha1.equals(currentTemplatesSignature)) {
                this.updateSignatures(currentTemplatesSignature);
                templateSignatureChanged = true;
            }
        } else {
            // first computation of templates signature
            this.insertTemplatesSignature(currentTemplatesSignature);
            templateSignatureChanged = true;
        }
        return templateSignatureChanged;
    }

    private void deleteAllDocumentTypes() {
        for (String docType : DocumentTypes.getDocumentTypes()) {
            try {
                this.deleteAllByDocType(docType);
            } catch (Exception e) {
                // maybe a non existing document type
            }
        }
    }

    public boolean isActive() {
        return db.isActiveOnCurrentThread();
    }
}
