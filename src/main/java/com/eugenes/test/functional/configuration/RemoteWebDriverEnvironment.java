package com.eugenes.test.functional.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.inject.Inject;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;

class RemoteWebDriverEnvironment implements WebDriverEnvironment {

    @Value("${driver.remote.hub.host:localhost}")
    private String seleniumGridHubHost;

    @Inject
    private WebDriver driver;

    @Override
    public String getHostname() {
        return getHostOfNode();
    }

    private String getHostOfNode() {
        try {
            RemoteWebDriver remoteDriver = (RemoteWebDriver) ((WrapsDriver) this.driver).getWrappedDriver();

            URL url = new URL(String.format("http://%s:4444/grid/api/testsession?session=%s", seleniumGridHubHost, remoteDriver
                    .getSessionId().toString()));
            BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST", url.toExternalForm());
            HttpClient client = HttpClientBuilder.create().build();
            HttpHost host = new HttpHost(url.getHost(), url.getPort());
            HttpResponse response = client.execute(host, request);
            JSONObject object = extractObject(response);
            URL nodeUrl = new URL(object.getString("proxyId"));
            return nodeUrl.getHost();
        } catch (Exception e) {
            return "Unknown Grid Node";

        }

    }

    private JSONObject extractObject(final HttpResponse resp) throws IOException, JSONException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
        StringBuilder s = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            s.append(line);
        }
        rd.close();
        return new JSONObject(s.toString());
    }
}