## About

This is the source code behind the paper [``A Screening Test for Disclosed
Vulnerabilities in FOSS
Components''](https://ieeexplore.ieee.org/abstract/document/8316943).

The tool allows to identify and extract the potentially vulnerable coding (using
a commit that fixed a CVE), and track its origins in the repository history to
identify the versions that are likely not affected by a CVE.

The project consists of two parts: 

1. "repoman" - a simple program slicer for Java source code.

2. "molerat" - a library that is using "repoman" and other methods to 
identify and track the potentially vulnerable coding.


## Usage

#### Prerequisites

1. Java compiler and runtime (tested with jdk 1.8).
    
2. Maven (tested with version 3.5.0)

3. MongoDB (tested with version 3.4)
    * make sure the mongodb service is running

#### Building and packaging

Building with Maven is pretty straightforward. The following commands are
available:

1. "mvn compile" - automatically downloads the dependencies and builds the
entire project tree.

2. "mvn package" - same as the previous command, but also creates executable jar
files for "repoman" and "molerat" that can be found in the "target" folder of
each project.

3. "mvn clean" - deletes all temporary folders.

#### Basic usage

The project can be compiled into an executable jar library:
1. Execute the "mvn package" command
2. Navigate to the "./molerat/target" folder
3. Run it with "java -jar molerat.jar" (this command will display the help message).

Currently, it is possible to run the analysis by either specifying all
parameters (such as repository path, tracker type, etc.) manually, or by
providing a .csv file, where each line corresponds to one vulnerability to be
analyzed, and contains the following fields separated with commas:

1. Project name  (e.g., "Tomcat")
2. CVE identifier (e.g., "CVE-2014-0230")
3. Repository type (either "git" for Git, or "svn" for Apache Subversion)
4. Path to the working copy of the repository (e.g., "/home/user/tomcat")
5. A revision/commit id of the vulnerability fix (e.g., "e28dd578fad90a6d5726ec34f3245c9f99d909a5A")
6. The name of a method for extracting the vulnerability evidence (e.g., "SliceDecayVulnerabilityEvidenceTracker")

NOTE: the list of available trackers can be shown by running the "java -jar
molerat.jar --list-trackers" command

IMPORTANT: if you are running the analysis using a .csv input file, please make
sure that the fields are specified in the exact order as shown above.


#### MongoDB database

There is no access control for the database, just make sure that the "bindIp"
setting is set to "127.0.0.1" which allows only local access (typically, this
setting is enabled by default, but you might consult
"https://docs.mongodb.com/manual/administration/configuration/" webpage to make
sure).

The database has following collections and relationships between them: 

1. "projects" -> the collection that lists all projects for which the analysis was performed ("projects" has one-to-many relationship with "vulns")

```
db.projects.findOne();
{ 
    _id : "",           -> the id of a project (bson id)
    name : "",          -> the name of a project (e.g., "Tomcat")
    repo_type : "",     -> the type of its source repository (e.g., "git")
    repo_path : "",     -> the path of the repository (e.g., "/home/user/tomcat")
    vulns : [           -> the list of CVEs for which an analysis was performed 
        vuln_id : "",           (e.g,. "CVE-2014-0230", ...)
        ...
    ]
}
```

2. "vulns" -> the collection that lists all CVEs for which the analysis was performed ("vulns" has one-to-gazillion relationship with "entries")

```
db.vulns.findOne();
{   
    _id : "",           -> the id of a CVE (bson id)
    cve : "",           -> the name of a CVE
    owner_id : "",      -> the bson id of a corresponding project
    fix_commit : "",    -> the id of a commit that fixed the CVE
}
```

3. "entries" -> the collection that lists vulnerability evidence entries 

```
db.entries.findOne();
{
    _id : "",               -> the id of an evicence entry (bson id)
    owner_id : "",          -> the id of a corresponding CVE
    revision : "",          -> the current commit/revision to which the entry belongs
    revision_distance : "", -> this number indincates how far the current revision is from fix
    file_path : "",         -> the path of a file to which the entry belongs
    container : "",         -> the method/constructor to which the entry belongs
    line_number : "",       -> the number of the line of code 
    line_contents : ""      -> the contents of the line of code 
}
```

## References

S. Dashevskyi, A. D. Brucker and F. Massacci, "A Screening Test for Disclosed Vulnerabilities in FOSS Components," in
IEEE Transactions on Software Engineering.  doi: 10.1109/TSE.2018.2816033 URL:
http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=8316943&isnumber=4359463

## License

This project is licensed under the [MIT License](LICENSE).
