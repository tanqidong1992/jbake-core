package org.jbake.app;

import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.jbake.model.DocumentModel;

import java.util.LinkedList;

/**
 * Wraps an OrientDB document iterator into a model usable by
 * template engines.
 *
 * @author Cédric Champeau
 */
public class DocumentList<T> extends LinkedList<T> {

    public static DocumentList<DocumentModel> wrap(OResultSet docs) {
        DocumentList<DocumentModel> list = new DocumentList<>();
        while (docs.hasNext()) {
            OResult next = docs.next();
            list.add(DBUtil.documentToModel(next));
        }
        docs.close();
        return list;
    }

}
