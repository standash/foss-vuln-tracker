package it.unitn.molerat.data.db;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import it.unitn.molerat.data.memory.AnalysisEntry;
import it.unitn.molerat.evidence.VulnerabilityEvidence;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.*;

public class MongoWrapper {

    private static final String projectCollection = "projects";
    private static final String vulnsCollection = "vulns";
    private static final String vulnEvdCollection = "vuln_evidences";

    private MongoClient client = null;
    private MongoDatabase db = null;

    public MongoWrapper(String dbName) {
        this.client = new MongoClient();
        this.db = client.getDatabase(dbName);
    }

    private FindIterable<Document> getEntries(BasicDBObject query, String collection) {
        MongoCollection<Document> docs = db.getCollection(collection);
        return docs.find(query);
    }

    private Document getEntry(BasicDBObject query, String collection) {
        return getEntries(query, collection).first();
    }

    private ObjectId getEntryId(Document doc) {
        return (doc != null) ? doc.getObjectId("_id") : null;
    }

    private Document getProjectEntry(String name, String repoType, String repoPath) {
        BasicDBObject query = new BasicDBObject();
        query.put("name", name);
        query.put("repo_type", repoType);
        query.put("repo_path", repoPath);
        return getEntry(query, projectCollection);
    }

    private Document getProjectEntry(ObjectId projectId) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", projectId);
        return getEntry(query, projectCollection);
    }

    private ObjectId getProjectId(String name, String repoType, String repoPath) {
        return getEntryId(getProjectEntry(name, repoType, repoPath));
    }

    private ObjectId getProjectId(ObjectId cveId) {
        return getCveEntry(cveId).getObjectId("owner_id");
    }

    private Document getCveEntry(ObjectId projectId, String cve) {
        BasicDBObject query = new BasicDBObject();
        query.put("cve", cve);
        query.put("owner_id", projectId);
        return getEntry(query, vulnsCollection);
    }

    private Document getCveEntry(ObjectId cveId) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", cveId);
        return getEntry(query, vulnsCollection);
    }

    private ObjectId getCveId(ObjectId projectId, String cve) {
        return getEntryId(getCveEntry(projectId, cve));
    }

    public Set<ObjectId> getAllCveIds() {
        Set<ObjectId> ids = new LinkedHashSet<>();
        MongoCollection<Document> vulns = db.getCollection(vulnsCollection);
        FindIterable<Document> docs = vulns.find();
        for (Document doc : docs) {
            ids.add(doc.getObjectId("_id"));
        }
        return ids;
    }

    private FindIterable<Document> getVulnEvidences(ObjectId cveId) {
       BasicDBObject query = new BasicDBObject() ;
       query.put("owner_id", cveId);
       FindIterable<Document> result = getEntries(query, vulnEvdCollection);
       result.sort(new BasicDBObject("order", -1));
       return result;
    }

    private ObjectId insertProject(String name, String repoType, String repoPath) {
        Document proj = getProjectEntry(name, repoType, repoPath);
        if (proj == null) {
            MongoCollection<Document> projects = db.getCollection(projectCollection);
            proj = new Document("name", name)
                    .append("repo_type", repoType)
                    .append("repo_path", repoPath);
            projects.insertOne(proj);

        }
        return getEntryId(proj);
    }

    private ObjectId insertCve(ObjectId projectId, String cve, String fixCommit) {
        Document vuln = getCveEntry(projectId, cve);
        if (vuln == null) {
            MongoCollection<Document> vulns = db.getCollection(vulnsCollection);
            vuln = new Document("cve", cve)
                    .append("fix_commit", fixCommit)
                    .append("owner_id", projectId)
                    .append("processed", false);
            vulns.insertOne(vuln);
            MongoCollection<Document> projects = db.getCollection(projectCollection);
            projects.findOneAndUpdate(new BasicDBObject("_id", projectId), new BasicDBObject("$push",
                    new BasicDBObject("vulns", getEntryId(vuln))));
        }
        return getEntryId(vuln);
    }

    private boolean isVulnProcessed(ObjectId cveId) {
        MongoCollection<Document> vulns = db.getCollection(vulnsCollection);
        Document cve = vulns.find(new BasicDBObject("_id", cveId)).first();
        return (Boolean)cve.get("processed");
    }

    public boolean insertAnalysisEntry(AnalysisEntry entry) {
    	// if there's no molerat.evidence for some reason, return false
    	if (entry.getVulnEvidences().isEmpty()) {
    		return false;
    	}
    	
        ObjectId projectId = insertProject(entry.getProjectName(), entry.getRepositoryType(), entry.getRepositoryPath());
        ObjectId cveId = insertCve(projectId, entry.getCveName(), entry.getFixCommit());
        MongoCollection<Document> vulns = db.getCollection(vulnsCollection);
        MongoCollection<Document> evds = db.getCollection(vulnEvdCollection);

        if (isVulnProcessed(cveId)) {
            vulns.updateOne(
                    new BasicDBObject("_id", cveId),
                    new BasicDBObject("$set", new BasicDBObject("processed", false))
            );
            evds.deleteMany(new BasicDBObject("owner_id", cveId));
        }

        List<Document> records = new LinkedList<Document>();
        int order= 0;
        Set<String> commits = entry.getCommits();
        for (String commit : commits) {
            Set<VulnerabilityEvidence> evidences = entry.getVulnEvidences().get(commit);
            if (evidences != null) {
               for (VulnerabilityEvidence e : evidences)  {
                   BasicDBObject record = new BasicDBObject();
                   record.put("owner_id", cveId);
                   record.put("revision", commit);
                   record.put("order", order);
                   record.put("file_path", e.getPath());
                   record.put("container", e.getContainer());
                   record.put("line_number", e.getLineNumber());
                   record.put("line_contents", e.getLineContents());
                   Document doc = new Document(record);
                   records.add(doc);
               }
            }
            order--;
        }
        
        evds.insertMany(records);
        vulns.updateOne(
            new BasicDBObject("_id", cveId),
                new BasicDBObject("$set", new BasicDBObject("processed", true))
        );
        //create index
        evds.createIndex(Indexes.descending("order"));
        return true;
    }

    public AnalysisEntry getAnalysisEntry(String projectName, String cveName, String repoType, String repoPath)  {
        ObjectId projectId = getProjectId(projectName, repoType, repoPath);
        ObjectId cveId = getCveId(projectId, cveName);
        return getAnalysisEntry(cveId);
    }

    public boolean analysisEntryExists(String projectName, String cveName, String repoType, String repoPath) {
        ObjectId projectId = getProjectId(projectName, repoType, repoPath);
        ObjectId cveId = getCveId(projectId, cveName);
        return (projectId != null && cveId != null);
    }

    public Set<AnalysisEntry> getAnalysisEntries() {
        Set<AnalysisEntry> entries = new LinkedHashSet<>();
        FindIterable<Document> cves = db.getCollection(vulnsCollection).find();
        for (Document cve : cves) {
           ObjectId cveId = cve.getObjectId("_id");
           AnalysisEntry entry = getAnalysisEntry(cveId);
           entries.add(entry);
        }
        return entries;
    }

    public AnalysisEntry getAnalysisEntry(ObjectId cveId) {
        if (!isVulnProcessed(cveId)) {
            return null;
        }
        ObjectId projectId = getProjectId(cveId);
        Document projectDoc = getProjectEntry(projectId);
        String projectName = projectDoc.getString("name");
        String repoType = projectDoc.getString("repo_type");
        String repoPath = projectDoc.getString("repo_path");

        Document cveDoc = getCveEntry(cveId);
        String cveName = cveDoc.getString("cve");
        String fixCommit =  cveDoc.getString("fix_commit");

        Map<String, Set<VulnerabilityEvidence>> vulnEvidences = new LinkedHashMap<>();
        FindIterable<Document> docs = getVulnEvidences(cveId);
        for (Document doc : docs) {
            String filePath = doc.getString("file_path");
            String container = doc.getString("container");
            String revision = doc.getString("revision");
            int lineNumber = doc.getInteger("line_number");
            String lineContents = doc.getString("line_contents");
            VulnerabilityEvidence evidence = new VulnerabilityEvidence(filePath, revision, container, lineNumber, lineContents);
            if (vulnEvidences.containsKey(revision)) {
                vulnEvidences.get(revision).add(evidence);
            }
            else {
                Set<VulnerabilityEvidence> set = new LinkedHashSet<>();
                set.add(evidence);
                vulnEvidences.put(evidence.getCommit(), set);
            }
        }
        /* TODO: add stuff for getting the changes molerat.evidence
            Map<String, Set<ChangeEvidence>> chgEvidences = new TreeMap<>();
        */
        return new AnalysisEntry(cveName, projectName, repoType, repoPath, fixCommit, vulnEvidences.keySet(), vulnEvidences, null);
    }
}
