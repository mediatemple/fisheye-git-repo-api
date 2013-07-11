package net.mediatemple.fisheye;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crucible.spi.services.ImpersonationService;
import com.atlassian.crucible.spi.services.Operation;
import com.atlassian.crucible.spi.PluginIdAware;
import com.atlassian.crucible.spi.PluginId;

import com.atlassian.fisheye.spi.admin.data.AuthenticationData;
import com.atlassian.fisheye.spi.admin.data.AuthenticationStyle;
import com.atlassian.fisheye.spi.admin.data.GitRepositoryData;
import com.atlassian.fisheye.spi.admin.data.RepositoryIndexingStatus;
import com.atlassian.fisheye.spi.admin.data.RepositoryState;
import com.atlassian.fisheye.spi.admin.services.RepositoryAdminService;
import com.atlassian.fisheye.spi.admin.services.RepositoryIndexer;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.pluginsettings.PluginSettings;

public class GitRepoAPI extends HttpServlet implements PluginIdAware {
    private final RepositoryAdminService repositoryAdminService;
    private final PluginSettingsFactory settingsFactory;
    private final ImpersonationService impersonationService;
    private static final Logger LOG = LoggerFactory.getLogger("atlassian.plugin");
    private PluginId pluginId;

    public GitRepoAPI(PluginSettingsFactory settingsFactory, RepositoryAdminService repositoryAdminService, ImpersonationService impersonationService) {
        this.settingsFactory  = settingsFactory;
        this.repositoryAdminService = repositoryAdminService;
        this.impersonationService = impersonationService;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if ("/update".equals(pathInfo)) {
            doCreateUpdateRepository(request, response);
        }
        else if ("/delete".equals(pathInfo)) {
            doDeleteRepository(request, response);
        }
        else {
            sendError(response, "unknown command");
        }
    }

    private void doCreateUpdateRepository(HttpServletRequest request, final HttpServletResponse response) throws IOException {
        PluginSettings settings = settingsFactory.createGlobalSettings();
        final String pkey = (String) settings.get("net.mediatemple.fisheye.gitrepoapi.privatekey");

        if (pkey == null || pkey.length() < 10) {
            sendError(response, "create failed, private key configuration needed, see admin section");
            return;
        }

        final String repositoryName = request.getParameter("name");
        if (repositoryName == null) {
            sendError(response, "The 'name' parameter is required.");
            return;
        }
        if (!repositoryName.matches("^[a-zA-Z0-9\\-_\\.]+$")) {
            sendError(response, "Repository name must match '^[a-zA-Z0-9\\\\-_\\\\.]+$'");
            return;
        }

        final String repositoryUrl = request.getParameter("url");
        if (repositoryUrl == null) {
            sendError(response, "The 'url' parameter is required.");
            return;
        }
        if (!repositoryUrl.matches("ssh://[a-zA-Z0-9]+@[a-z0-9\\.]+(:[0-9]+)?/[a-zA-Z0-9\\-_\\/\\.]+\\.git$")) {
            sendError(response, "Repository url must match 'ssh://[a-zA-Z0-9]+@[a-z0-9\\\\.]+(:[0-9]+)?/[a-zA-Z0-9\\\\-_\\\\/\\\\.]+\\\\.git$', e.g. ssh://git@git.host.name/path/to/repo.git");
            return;
        }

        Boolean bool = Boolean.FALSE;
        try {
            bool = impersonationService.doPrivilegedAction(this.pluginId, new Operation<Boolean, Exception>() {

                public Boolean perform() throws Exception {
                    if (!repositoryAdminService.exists(repositoryName)) {
                        LOG.info("repo does not exist");
                        try {
                            LOG.debug("going to create repositorydata");
                            GitRepositoryData repoData = new GitRepositoryData(repositoryName, repositoryUrl);
                            repoData.setStoreDiff(true);
                            repoData.setRenameOption(3);

                            LOG.debug("going to create authentication");
                            AuthenticationData authentication = new AuthenticationData();
                            authentication.setPrivateKey(pkey);
                            authentication.setAuthenticationStyle(AuthenticationStyle.SSH_KEY_WITHOUT_PASSPHRASE);
                            repoData.setAuthentication(authentication);

                            LOG.info("Creating repo " + repositoryName);
                            repositoryAdminService.create(repoData);
                            RepositoryState state;

                            LOG.info("Disabling polling for " + repositoryName);
                            repositoryAdminService.disablePolling(repositoryName);


                            LOG.info("Enabling repo " + repositoryName);
                            repositoryAdminService.enable(repositoryName);

                            //state = repositoryAdminService.getState(repositoryName);
                            //LOG.error("after enable, state is " + state);

                            LOG.info("Starting repo " + repositoryName);
                            repositoryAdminService.start(repositoryName);

                            //state = repositoryAdminService.getState(repositoryName);
                            //LOG.error("after start, state is " + state);
                        } catch (Exception e) {
                            LOG.error(e.toString());
                            sendError(response, "create failed, see logs for details");
                            return Boolean.FALSE;
                        }
                    }

                    else {
                        LOG.info("updating existing repository");
                        try {
                            LOG.debug("going to update repositorydata");
                            GitRepositoryData repoData = (GitRepositoryData)repositoryAdminService.getRepositoryData(repositoryName);
                            repoData.setLocation(repositoryUrl);

                            LOG.debug("going to update authentication");
                            AuthenticationData authentication = new AuthenticationData();
                            authentication.setAuthenticationStyle(AuthenticationStyle.NONE);
                            repoData.setAuthentication(authentication);

                            LOG.info("Updating repo " + repositoryName);
                            repositoryAdminService.update(repoData);

                            LOG.info("Disabling polling for " + repositoryName);
                            repositoryAdminService.disablePolling(repositoryName);

                            if (!repositoryAdminService.isEnabled(repositoryName)) {
                                LOG.info("Enabling repo " + repositoryName);
                                repositoryAdminService.enable(repositoryName);
                            }

                            if (repositoryAdminService.getState(repositoryName) == RepositoryState.STOPPED) {
                                LOG.info("Starting repo " + repositoryName);
                                repositoryAdminService.start(repositoryName);
                            }

                            //state = repositoryAdminService.getState(repositoryName);
                            //LOG.error("after start, state is " + state);

                        } catch (Exception e) {
                            LOG.error(e.toString());
                            sendError(response, "create failed, see logs for details");
                            return Boolean.FALSE;
                        }
                    }

                    LOG.info("Starting indexing on repo " + repositoryName);
                    RepositoryIndexer indexer = repositoryAdminService.getIndexer(repositoryName);
                    //logIndexingStatus(indexer);
                    indexer.startIncrementalIndexing();
                    //logIndexingStatus(indexer);

                    return Boolean.TRUE;
                }

            });
        } catch (Exception e) {
            LOG.error("Unexpected error in doPrivilegedAction");
            LOG.error(e.toString());
        }

        if (bool.booleanValue()) {
            sendSuccess(response, "create/update successful");
        }
    }

    private void doDeleteRepository(HttpServletRequest request, final HttpServletResponse response) throws IOException {

        final String repositoryName = request.getParameter("name");
        if (repositoryName == null) {
            sendError(response, "The 'name' parameter is required.");
            return;
        }

        Boolean bool = Boolean.FALSE;
        try {
            bool = impersonationService.doPrivilegedAction(this.pluginId, new Operation<Boolean, Exception>() {

                public Boolean perform() throws Exception {
                    if (repositoryAdminService.exists(repositoryName)) {

                        try {
                            LOG.info("Stopping repository: " + repositoryName);
                            repositoryAdminService.stop(repositoryName);
                        } catch (Exception e) {
                            LOG.error(e.toString());
                        }

                        RepositoryState state;
                        for (int i = 5; i > 0; i--) {
                            state = repositoryAdminService.getState(repositoryName);
                            if (state.equals(RepositoryState.STOPPED)) {
                                break;
                            }
                            else {
                                LOG.info("Repository " + repositoryName + " not stopped, trying again in 5 seconds");
                                try {
                                    Thread.sleep(5 * 1000);
                                    if (!state.equals(RepositoryState.STOPPING)) {
                                        repositoryAdminService.stop(repositoryName);
                                    }
                                } catch (Exception e) {
                                    LOG.error(e.toString());
                                }
                            }
                        }

                        state = repositoryAdminService.getState(repositoryName);
                        if (!state.equals(RepositoryState.STOPPED)) {
                            sendError(response, "delete failed, repo refused to stop");
                            return Boolean.FALSE;
                        }

                        try {
                            LOG.info("Disabling repository: " + repositoryName);
                            repositoryAdminService.disable(repositoryName);
                        } catch (Exception e) {
                            LOG.error(e.toString());
                        }
                        try {
                            LOG.info("Deleting repository: " + repositoryName);
                            repositoryAdminService.delete(repositoryName);
                        } catch (Exception e) {
                            LOG.error(e.toString());
                            sendError(response, "delete failed, see logs for details");
                            return Boolean.FALSE;
                        }
                    }

                    return Boolean.TRUE;
                }

            });
        } catch (Exception e) {
            LOG.error("Unexpected error in doPrivilegedAction");
            LOG.error(e.toString());
        }

        if (bool.booleanValue()) {
            sendSuccess(response, "delete successful");
        }
    }

    private void logIndexingStatus(RepositoryIndexer indexer) {
        RepositoryIndexingStatus status = indexer.getIndexingStatus();
        LOG.info("--------");
        LOG.info("message: " + status.getMessage());
        LOG.info("full index in progress: " + Boolean.toString(status.isFullIndexingInProgress()));
        LOG.info("incremental index in progress: " + Boolean.toString(status.isIncrementalIndexingInProgress()));
        LOG.info("linesOfContent index in progress: " + Boolean.toString(status.isLinesOfContentIndexingInProgress()));
    }

    private void sendSuccess(HttpServletResponse response, String message) throws IOException {
        sendResponse(response, "{\"response\":\""+message+"\"}");
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        sendResponse(response, "{\"error\":\""+message+"\"}");
    }

    private void sendResponse(HttpServletResponse response, String content) throws IOException {
        response.setContentType("application/json");
        response.getWriter().print(content);
    }

    public void setPluginId(PluginId pluginId) {
        this.pluginId = pluginId;
    }
}
