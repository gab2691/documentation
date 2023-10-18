package br.documentation.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@Service
public class JGitService {

    public static final String JAVA_DOCS_GIT = "https://gitlab.santanderbr.corp/developer-experience/chapter-java/docs.git";
    private static final String remoteRepo = "https://gitlab.santanderbr.corp/developer-experience/chapter-java/docs/blob/master/";
    private final String localRepo = "C:\\Users\\T734536\\OneDrive - Santander Office 365\\Documents\\Workspace\\docsRepo\\docs";
    private final String user = "t734536";
    private final String pass =  "Gbb0914@";
    private final Logger looger = Logger.getLogger(JGitService.class.getName());

    public void updateLocalRepo() {
        try {
            looger.info("Starting Update Local repo: " + localRepo);
            Repository local = new FileRepositoryBuilder().setGitDir(new File(localRepo + "/.git")).build();

            UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider(user, pass);

            new Git(local).pull()
                    .setRemoteBranchName("master")
                    .setCredentialsProvider(usernamePasswordCredentialsProvider)
                    .call();
            looger.info("Updating Complete");
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException("Error tryng update local repo: " + localRepo + e.getCause());
        }
    }

    public String pushDoc(String title) {
        looger.info("Starting push DOC: " + title);
        try(Git git = Git.open((new File(localRepo)))){
            git.add()
                    .addFilepattern("docs/" + title + ".md").call();

            git.commit()
                    .setAll(true)
                    .setMessage("Doc " + title).call();

            git.push()
                    .setRemote(JAVA_DOCS_GIT)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, pass))
                    .setForce(true)
                    .call();
            looger.info("Complete pushing DOC: " + title);
        } catch (Exception e){
            throw new RuntimeException("Error tring pushing DOC: " + title + e.getCause());
        }
        return this.remoteRepo + title + ".md";
    }
}
