package it.unitn.molerat.repos.wrappers;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.ISvnObjectReceiver;
import org.tmatesoft.svn.core.wc2.SvnAnnotate;
import org.tmatesoft.svn.core.wc2.SvnAnnotateItem;
import org.tmatesoft.svn.core.wc2.SvnCat;
import org.tmatesoft.svn.core.wc2.SvnDiff;
import org.tmatesoft.svn.core.wc2.SvnLog;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnRevisionRange;
import org.tmatesoft.svn.core.wc2.SvnTarget;

public final class SvnRepoWrapper extends RepoWrapper {

	private final SVNURL url;
	protected SVNRepository repo = null;
	protected final SvnOperationFactory opFactory = new SvnOperationFactory();

	public SvnRepoWrapper(String url) throws SVNException {
		this.diffFilePrefix = "Index: ";
		this.url = SVNURL.parseURIEncoded(url);
		this.repo = SVNRepositoryFactory.create(this.url);
	}
	
	protected String doDiff(SvnTarget leftRev, SvnTarget rightRev) throws SVNException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		SvnDiff diff = opFactory.createDiff();
		diff.setOutput(output);
		diff.setSources(leftRev, rightRev);
		diff.run();
		return output.toString();
	}
	
	public Set<String> getRevisionNumbers(String topRev) throws SVNException {
		Set<String> revNumbers = new LinkedHashSet<>();
		LinkedList<SVNLogEntry> logEntries = new LinkedList<>();
		SvnLog log = opFactory.createLog();
		SvnTarget target = SvnTarget.fromURL(this.url);
		log.addRange(SvnRevisionRange.create(SVNRevision.create(0), SVNRevision.create(Long.parseLong(topRev))));
		log.setSingleTarget(target);
		log.run(logEntries);
		Iterator<SVNLogEntry> it = logEntries.descendingIterator();
        if (it.hasNext()) {
            it.next(); // discard the topRev
        }
		while (it.hasNext()) {
			SVNLogEntry entry = it.next();
			revNumbers.add(String.valueOf(entry.getRevision()));
		}
		return revNumbers;
	}

	
	protected void annotate(final SvnTarget target, ISvnObjectReceiver<SvnAnnotateItem> callback) throws SVNException {
		SvnAnnotate annotate = opFactory.createAnnotate();
		annotate.setSingleTarget(target);
		final SVNRevision startRev = SVNRevision.create(1);
		final SVNRevision endRev = target.getPegRevision();
		annotate.setStartRevision(startRev);
		annotate.setEndRevision(endRev);
		annotate.setReceiver(callback);
		annotate.run();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void annotate(String path, String rev, Object callback) throws Exception {
		long revLong = Long.parseLong(rev);
		SvnTarget target = SvnTarget.fromURL(SVNURL.parseURIEncoded(this.getBasePath() + path), SVNRevision.create(revLong));
		this.annotate(target, ((ISvnObjectReceiver<SvnAnnotateItem>)callback));
	}

	protected String doCat(final SvnTarget target) throws SVNException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		SvnCat cat = this.opFactory.createCat();
		cat.addTarget(target);
		cat.setOutput(output);
		cat.run();
		return output.toString();
	}

	@Override
	public String doDiff(String leftRev, String rightRev) throws SVNException {
		return this.doDiff("", leftRev, rightRev);
	}
	
	@Override
	public String doDiff(String path, String leftRev, String rightRev) throws SVNException {
		long leftLong = Long.parseLong(leftRev);
		long rightLong = Long.parseLong(rightRev);
		SvnTarget left = SvnTarget.fromURL(SVNURL.parseURIEncoded(this.url + path), SVNRevision.create(leftLong));
		SvnTarget right = SvnTarget.fromURL(SVNURL.parseURIEncoded(this.url + path), SVNRevision.create(rightLong));
		return this.doDiff(left, right);
	}
	

	@Override
	public String getBasePath() {
		return this.url.toString();
	}

	@Override
	public String doCat(String path, String rev) throws SVNException {
		long revLong = Long.parseLong(rev);
		SvnTarget target = SvnTarget.fromURL(SVNURL.parseURIEncoded(this.url + path), SVNRevision.create(revLong));
		return this.doCat(target);
	}

	//---------------------------------------------------------------------------------------------------- 
	public String getTopmostRevision(String url) throws SVNException {
		SvnLog log = opFactory.createLog();
		SvnTarget target = SvnTarget.fromURL(SVNURL.parseURIEncoded(url));
		log.addRange(SvnRevisionRange.create(null, null));
		log.setSingleTarget(target);
		SVNLogEntry entry = log.run();
		return String.valueOf(entry.getRevision());
	}
	//---------------------------------------------------------------------------------------------------- 
	
	/*---------------------------------------------------------------------------------------------------- 
	*  example found at: http://blog.gmane.org/gmane.comp.version-control.subversion.javasvn.user/month=20131101
	----------------------------------------------------------------------------------------------------*/ 
	@Override
	public Map<Integer, String> determineOriginatingRevision(final String filePath, 
																final String revision, final Map<Integer, String> lines) throws Exception {
		final Map<Integer, String> originating = new TreeMap<Integer, String>();
		final long startRev = 1;
		final long endRev = Long.parseLong(revision);
		this.annotate(filePath, String.valueOf(endRev), new ISvnObjectReceiver<SvnAnnotateItem>() {
			@Override
			public void receive(SvnTarget target, SvnAnnotateItem itm) throws SVNException {
				if (itm.isLine()) {
                    final long revision = itm.getRevision();
                    final int lineNumber = itm.getLineNumber() + 1;
					if (startRev < revision && revision <= endRev) {
						if (lines.containsKey(lineNumber)) {
							originating.put(lineNumber, String.valueOf(revision));
						}
                    }
                }
			}
		});
		return originating;
	}

	// TODO: UNIMPLEMENTED
    @Override
    protected String getReleaseTag(String release) throws Exception {
		return "";
    }

	// TODO: UNIMPLEMENTED
    @Override
    public String getReleaseCommit(String release) throws Exception {
		return "";
    }


	// TODO: UNIMPLEMENTED
	@Override
	public Set<String> getRevisionFiles(String rev, String filter) throws Exception {
		return new LinkedHashSet<>();
	}

	// TODO: UNIMPLEMENTED
	@Override
	public Set<String> getAllRepositoryTransactions() throws Exception {
		return new LinkedHashSet<>();
	}


}
