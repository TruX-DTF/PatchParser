# PatchParser

Description
----------------
A tool of collecting patch-related commits and parsing code changes of patches.<br>
First, it automatically collects bug-fix commits from source code repositories of Java projects with two criteria. 
It further automatically parses bug-fix patches by taking input the buggy and fixed versions of a Java code file containing a bug fix.

**Criteria of collecting bug-fix commits in software repositories:**
 - **Keyword matching**: search commit messages for bug-related keywords (namely *bug*, *error*, *fault*, *fix*, *patch* or *repair*).
 - **Bug linking**: we identify the reported and fixed bug IDs (e.g, **MATH-929**) in JIRA issue tracking system with two criteria: (1) **Issue Type** is `bug` and (2) **Resolution** is `fixed`. PatchParser thus collects bug-fix commits by identifying such reported and fixed bug IDs in commit messages.

**Parsing of bug-fix patches:**<br>
PatchParser takes input the buggy and fixed versions of a Java code file containing a bug fix, which are extracted by traversing the project repository with collected bug fix commits in the previous process, to parse bug fix patches by leveraging [GumTree](https://github.com/GumTreeDiff/gumtree/). PatchParser further regroups [GumTree](https://github.com/GumTreeDiff/gumtree/) output to present code change actions of bug-fix patches at AST level in terms of hierarchical construct.

The results of parsed patches are fine-grained, detailed and hierarchical code change actions at abstract syntax tree (AST) level, which can explicitly provide a graphic representation of the hierarchical repair actions for each bug-fix patch. 
The parsed patches can help researchers understand the code change action knowledge of bug fixes at AST level, 
which further can be used to mine fix patterns for Java bugs. 
PatchParser can provide the detailed statistics of fine-grained code entities impacted by patches, which can be used to build the knowledge on repair actions with fine-grained code entities for researchers.

**Example of a bug-fix patch parsed by PatchParser:**<br>
```diff
Project: commons-math
Bug ID: MATH-29
Commit ID: cedf0d27f9e9341a9e9fa8a192735a0c2e11be40
--- a/src/main/java/org/apache/commons/math3/distribution/MultivariateNormalDistribution.java
+++ b/src/main/java/org/apache/commons/math3/distribution/MultivariateNormalDistribution.java
@@ -183, 3 +183, 3 @@
-   return FastMath.pow(2 * FastMath.PI, -dim / 2) *
+   return FastMath.pow(2 * FastMath.PI, -0.5 * dim) *
              FastMath.pow(covarianceMatrixDeterminant, -0.5) * getExponentTerm(vals);

 Parsed Result:
 UPD ReturnStatement@@"return FastMath.pow(2 * FastMath.PI, -dim / 2) * FastMath.pow(covarianceMatrixDeterminant, -0.5) * getExponentTerm(vals);" to "return FastMath.pow(2 * FastMath.PI, -0.5 * dim) * FastMath.pow(covarianceMatrixDeterminant, -0.5) * getExponentTerm(vals);".
 ---UPD InfixExpression@@"FastMath.pow(2 * FastMath.PI, -dim / 2) * FastMath.pow(covarianceMatrixDeterminant, -0.5) * getExponentTerm(vals)" to "FastMath.pow(2 * FastMath.PI, -0.5 * dim) * FastMath.pow(covarianceMatrixDeterminant, -0.5) * getExponentTerm(vals)".
 ------UPD MethodInvocation@@"FastMath.pow(2 * FastMath.PI, -dim / 2)" to "FastMath.pow(2 * FastMath.PI, -0.5 * dim)".
 ---------UPD InfixExpression@@"-dim / 2" to "-0.5 * dim".
 ------------UPD PrefixExpression@@"-dim" to "-0.5".
 ---------------DEL SimpleName@@"dim" from "-dim".
 ---------------INS NumberLiteral@@"0.5" to "-dim".
 ------------UPD Operator@@"/" to "*".
 ------------DEL NumberLiteral@@"2" from "2".
 ------------INS SimpleName@@"dim" to "2".
```

This document describes how to use this dataset and how to reproduce the result of our paper below. Please cite the following paper if you utilize the tool.

```
@inproceedings{liu2018closer,
  Author = {Liu, Kui and Kim, Dongsun and Koyuncu, Anil and Li, Li and Bissyande, Tegawend{\'e} Fran{\c{c}}ois D Assise and Le Traon, Yves},
  Title = {A Closer Look at Real-World Patches},
  Booktitle = {Proceedings of the 34th IEEE International Conference on Software Maintenance and Evolution},
  Series = {ICSME 2018},
  Year = {2018},
  address = {Madrid, Spain},
  pages = {304--315}
}
```

Requirement
----------------
  - Java 1.8
  - Maven 3.3.9

How to run the PatchParser
------------------------------
1. Clone the PatchParser:
  - `git clone https://github.com/AutoProRepair/PatchParser.git`

2. Collecting the six Java projects ([`commons-io`](https://github.com/apache/commons-io), [`commons-lang`](https://github.com/apache/commons-lang), [`commons-math`](https://github.com/apache/commons-math), [`derby`](https://github.com/apache/derby), [`lucene-solr`](https://github.com/apache/lucene-solr), and [`mahout`](https://github.com/apache/mahout)) from GitHub to create the subjects.
- `cd PatchParser`
- `./subjects/run.sh` After running it, there are six new directories for the six Java projects in the directory "`subjects/`", which have the same names as their project names respectively.

3. Collecting patch-related commits and parsing code changes of patches
- `./run.sh`

* If it executes successfully, 
    * The **first step** makes statistics of project LOC, which show the code line numbers of all projects respectively.
    * The **second step** will collect the fixed bug reports from JIRA issue tracking system. The results are stored in the directory "`data/BugReports/`".<br>
    * The **third step** collects bug-fix-related commits with bugID of bug reports and bug-related keywords from project repositories.
It also will fileter out changes of test code. Its output consists of three kinds of files: 
        * **Buggy version** of a Java code file containing a bug, stored in the directory "`data/PatchCommits/Linked_or_Keywords/<ProjectName>/prevFiles/`".
        * **Fixed version** of the Java code file, stored in the directory "`data/PatchCommits/Linked_or_Keywords/<ProjectName>/revFiles/`".
        * **Diff Hunk** of the code changes of fixing the bug, stored in the directory "`data/PatchCommits/Linked_or_Keywords/<ProjectName>/DiffEntries/`".
    * The **forth step** will further filter out the Java code files that only contain non-Java code changes.<br>
    * The **fifth step** makes statistics of diff hunk sizes of code changes. The results will be stored in the directory "`data/DiffentrySizes/`".<br>
    * The **sixth step** will parse code changes of patches and make statistics of fine-grained code entities impatced by patches. The results will be stored in the directory "`data/ParseResults/`". 
