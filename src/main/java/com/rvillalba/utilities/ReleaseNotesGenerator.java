package com.rvillalba.utilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class ReleaseNotesGenerator {

    private static ObjectId findActualRefObjectId(Ref ref, Repository repo) {
        final Ref repoPeeled = repo.peel(ref);
        if (repoPeeled.getPeeledObjectId() != null) {
            return repoPeeled.getPeeledObjectId();
        }
        return ref.getObjectId();
    }

    private static void gitLogFromTagToNow(Repository repository, String tag1, String version, String pathFile)
            throws IOException, GitAPIException, ParseException {
        Git git = new Git(repository);
        Ref refFrom = repository.findRef(tag1);
        RevCommit latestCommit = git.log().call().iterator().next();
        Iterable<RevCommit> log = git.log().addRange(findActualRefObjectId(refFrom, repository), latestCommit).call();
        StringBuilder csvLines = new StringBuilder();
        csvLines.append(StringUtils.join(new String[] { "Cliente", "Horas", "Recurso", "#Area", "Area", "Funcionalidad", "Descripcion", "Version",
                "Released", "publicable", "publicado", "tipo cambio", "#peticion" }, ";") + "\n");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        for (Iterator<RevCommit> iterator = log.iterator(); iterator.hasNext();) {
            RevCommit rev = iterator.next();
            String[] splittedMessage = rev.getFullMessage().split("\\r?\\n");
            if (splittedMessage.length == 6) {
                cleanMessages(splittedMessage);
                csvLines.append(StringUtils.join(
                        new String[] { splittedMessage[4], splittedMessage[5], rev.getAuthorIdent().getName(), "", "", splittedMessage[1],
                                splittedMessage[2], version, simpleDateFormat.format(rev.getAuthorIdent().getWhen()), "", "", splittedMessage[0] },
                        ";") + "\n");
            }
        }
        FileUtils.writeByteArrayToFile(new File(pathFile + "_" + version + "_" + new SimpleDateFormat("ddMMyyyy_hhmm").format(new Date()) + ".csv"),
                csvLines.toString().getBytes());
        git.close();
    }

    private static void cleanMessages(String[] splittedMessage) {
        for (int i = 0; i < splittedMessage.length; i++) {
            splittedMessage[i] = splittedMessage[i].replaceAll("\\[.*\\]", "");
        }
    }

    public static void main(String[] args) throws IOException, GitAPIException, ParseException {
        String version = "versionFromMaven";
        String fromRevision = "V5.5.0";

        if (args != null && args.length > 0) {
            fromRevision = args[0];
            if (args != null && args.length > 1) {
                version = args[1];
            }
        }
        System.out.println("fromRevision:" + fromRevision + " version:" + version);
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        repositoryBuilder.setMustExist(true);
        File gitBase = new File(".git");
        String pathFile = "resources/releaseNotes/releaseNotes";
        if (!gitBase.exists()) {
            gitBase = new File("../.git");
            pathFile = "../resources/releaseNotes/releaseNotes";
        }
        System.out.println("gitBasePath:" + gitBase.getAbsolutePath());
        repositoryBuilder.setGitDir(gitBase);
        Repository repository = repositoryBuilder.build();
        if (repository.getObjectDatabase().exists()) {
            gitLogFromTagToNow(repository, fromRevision, version, pathFile);
        }
    }
}
