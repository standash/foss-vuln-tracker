package it.unitn.molerat.repos.wrappers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.lib.Ref;

public final class GitRepoWrapper extends RepoWrapper {
	
	private final String repoRoot;
	private final Repository repo;
	private final Git git;
	
	public GitRepoWrapper(String root) throws IOException {
		this.diffFilePrefix = "diff --git a";
		this.repoRoot = root;
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		builder.setGitDir(new File(this.repoRoot + "/.git"));
		this.repo = builder.build();
		this.git = new Git(this.repo);
	}
	
	public Map<String, String> getTagsAndCommits() throws Exception {
		Map<String, String> tags = new HashMap<>();
		List<Ref> call = git.tagList().call();
		for (Ref tagref : call) {
            tags.put(tagref.getName(), tagref.getPeeledObjectId().getName());
		}
		return tags;
	}

	@Override
	public String doDiff(String path, String leftRev, String rightRev) throws Exception {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		OutputStream out = new ByteArrayOutputStream();
		try { DiffFormatter df = new DiffFormatter(out);
			df.setRepository(this.git.getRepository());
			df.setDetectRenames(true);
			List<DiffEntry> entries = df.scan(getTreeIterator(leftRev), getTreeIterator(rightRev));
			df.close();
			df.format(entries);
		}
		catch(Exception e)	 {
			System.out.println("[JGit error] " + e.getMessage());
			throw new Exception(e);
		}
		return out.toString();
	}
	
	@Override
	public String doDiff(String leftRev, String rightRev) throws Exception {
		return this.doDiff("", leftRev, rightRev);
	}

	@Override
	public String doCat(String path, String rev) throws Exception {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		String output = "";
		final ObjectId id = this.repo.resolve(rev);
		ObjectReader reader = this.repo.newObjectReader();
		RevWalk walk = null;
		try {
			walk = new RevWalk(reader);
			RevCommit commit = walk.parseCommit(id);
			RevTree tree = commit.getTree();
			TreeWalk treewalk = TreeWalk.forPath(reader, path, tree);
			if (treewalk != null) {
				byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
				output = new String(data, "utf-8");
			}
		} finally {
			reader.close();
			if (walk != null) {
				walk.close();
			}
		}
		return output;
	}

	@Override
	public String getBasePath() {
		return this.repoRoot;
	}

	private AbstractTreeIterator getTreeIterator(String name) throws IOException {
		final ObjectId id = this.repo.resolve(name);
		if (id == null) {
			throw new IllegalArgumentException(name);
		}
		final CanonicalTreeParser p = new CanonicalTreeParser();
		try (ObjectReader or = this.repo.newObjectReader();
				RevWalk rw = new RevWalk(this.repo)) {
			p.reset(or, rw.parseTree(id));
			return p;
		}
	}

	// TODO: UNIMPLEMENTED
	@Override
	public void annotate(String path, String rev, Object callback) throws Exception {
	}


	@Override
	public Set<String> getRevisionNumbers(String topRev) throws Exception {
		Set<String> commits = new LinkedHashSet<>();
		for (RevCommit commit : git.log().add(this.repo.resolve(topRev + "~1")).call()) {
			commits.add(commit.getName());
		}
		return commits;
	}

	// TODO: UNIMPLEMENTED
	@Override
	public Map<Integer, String> determineOriginatingRevision(String filePath, String revision, Map<Integer, String> lines) throws Exception {
		return new HashMap<>();
	}

	// TODO: UNIMPLEMENTED
    @Override
    protected String getReleaseTag(String release) throws Exception {
        /* 
        Map<String, String> tags = this.getTagsAndCommits();
        for (String key : tags.keySet()) {
            String candidate = key.replace("refs/tags/", "");
            System.out.println("KEY: " + candidate);
        }
        */
        return ("refs/tags/" + release);
    }

    @Override
    public String getReleaseCommit(String release) throws Exception {
        String tagName = getReleaseTag(release);
        String tagCommit = this.getTagsAndCommits().get(tagName);
        Iterator<RevCommit> iter = git.log().add(this.repo.resolve(tagCommit)).call().iterator();
        iter.next();
        return iter.next().getName();
    }

	@Override
	public Set<String> getRevisionFiles(String rev, String filter) throws Exception {
		Set<String> files = new LinkedHashSet<>();
		RevWalk revWalk = null;
		TreeWalk treeWalk = null;
		try {
			ObjectId revId = this.repo.resolve(rev);
			revWalk = new RevWalk(this.repo);
			RevCommit commit = revWalk.parseCommit(revId);
			RevTree tree = commit.getTree();
			treeWalk = new TreeWalk(this.repo);
			treeWalk.addTree(tree);
			treeWalk.setRecursive(true);
			
			treeWalk.setFilter(PathSuffixFilter.create(filter));
			while(treeWalk.next()) {
				files.add(treeWalk.getPathString());
			}
		} catch(Exception e) {
			throw new Exception("[JGit error] " + e.getMessage());
		} finally {
			if (revWalk != null){
				revWalk.close();
			}
			if (treeWalk != null){
				treeWalk.close();
			}
		}
		return files;
	}

	@Override
	public Set<String> getAllRepositoryTransactions() throws Exception {
		Iterable<RevCommit> logs = git.log().call();
		Set<String> commits = new LinkedHashSet<>();
		Iterator<RevCommit> it = logs.iterator();
		if (it.hasNext()) {
			String latestCommit = it.next().getName();
			commits.add(latestCommit);
			commits.addAll(this.getRevisionNumbers(latestCommit));
		}
		return commits;
	}
}
