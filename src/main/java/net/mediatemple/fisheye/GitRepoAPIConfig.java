package net.mediatemple.fisheye;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;

public class GitRepoAPIConfig extends HttpServlet {

    private final PluginSettingsFactory settingsFactory;
    private final TemplateRenderer templateRenderer;

    private static final Logger LOG = LoggerFactory.getLogger("atlassian.plugin");

    public GitRepoAPIConfig(PluginSettingsFactory settingsFactory, TemplateRenderer templateRenderer) {
        this.settingsFactory  = settingsFactory;
        this.templateRenderer = templateRenderer;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PluginSettings settings = settingsFactory.createGlobalSettings();
        String privateKey = (String) settings.get("net.mediatemple.fisheye.gitrepoapi.privatekey");

        if (privateKey == null) {
            privateKey = "";
        }

        renderPage(request, response, privateKey);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String privateKey = request.getParameter("private_key");

        PluginSettings settings = settingsFactory.createGlobalSettings();
        settings.put("net.mediatemple.fisheye.gitrepoapi.privatekey", privateKey);

        renderPage(request, response, privateKey);
    }

    private void renderPage(HttpServletRequest request, HttpServletResponse response, String privateKey) throws IOException {
        request.setAttribute("decorator", "atl.admin");
        response.setContentType("text/html");

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("private_key", privateKey);

        templateRenderer.render("config.vm", params, response.getWriter());
    }
}
