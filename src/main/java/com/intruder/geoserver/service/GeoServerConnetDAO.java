package com.intruder.geoserver.service;


import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;


@Repository
public class GeoServerConnetDAO {


    private static final Logger LOGGER = LoggerFactory.getLogger(GeoServerConnetDAO.class);

    @Value("${geoserver.url}")
    private String  geoServer;

    @Value("${geoserver.user}")
    private String user;

    @Value("${geoserver.password}")
    private String password;


    public String getGeoServerURL() {
        return geoServer;
    }




    /**
     * Workspaces or Layers
     * @param _type		workspaces | layers
     * @param _method 	GET | POST | DELETE | PUT
     * @return
     */
    public JSONArray workspaceOrLayers(String _type, HttpMethod _method) {
        String url = _type;
        String result =  RESTFulConnect(url, _method);
        JSONObject obj = new JSONObject(result);
        JSONObject step = obj.getJSONObject(_type);
        String lastStrRemove = _type.substring(0, _type.length()-1);
        JSONArray contents = step.getJSONArray(lastStrRemove);
        return contents;
    }


    /**
     * Methods Datastores or Coverages or Featuretypes or Coveragestores
     * @param _type	datastores | coverages | featuretypes | coveragestores
     * @param _ws		workspace name
     * @param _method	GET | POST | DELETE | PUT
     * @return
     */
    public JSONArray storesOrCoverageOrFeaturetypes(String _type, String _ws, HttpMethod _method) {
        String url = "workspaces/" + _ws + "/" + _type;
        String result =  RESTFulConnect(url, _method);
        JSONObject obj = new JSONObject(result);
        JSONObject step = obj.getJSONObject(_type);
        String lastStrRemove = _type.substring(0, _type.length()-1);
        JSONArray contents = step.getJSONArray(lastStrRemove);
        return contents;
    }


    /**
     * get Vector data (GeoJson)
     * @param _url
     * @return
     */
    public JSONObject getFeautre(String _url){
        URI uri = null;
        try {
            uri = new URI(_url);
            InputStreamReader is = new InputStreamReader(uri.toURL().openStream(), "UTF-8");
            JSONTokener tokener = new JSONTokener(is);
            JSONObject obj = new JSONObject(tokener);
            return obj;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }




    /**
     * Layer Information (necessary workspace and layer name)
     * @param _ws			workspace name
     * @param _layerName	layer name
     * @return
     */
    public void deleteLayer(String _ws, String _layerName) {
        String url = "workspaces/" + _ws + "/layers/" + _layerName;
        String search =  RESTFulConnect(url, HttpMethod.GET);
        JSONObject obj = new JSONObject(search).getJSONObject("layer");
        JSONObject resource = obj.getJSONObject("resource");
        RESTFulConnect(url, HttpMethod.DELETE);
        freeConnect(resource.getString("href"), HttpMethod.DELETE);
    }






    /**
     * RESTFul method connect for GeoServer RESTFul
     * @param _url
     * @param _type
     * @return
     */
    private String RESTFulConnect(String _url, HttpMethod _type) {
        RestTemplate restTemplate = new RestTemplate();
        String url = geoServer + "/rest/" + _url + ".json";
        HttpEntity<String> request = new HttpEntity<String>(createHeaders(user, password));
        ResponseEntity<String> result = restTemplate.exchange(url,
                _type,
                request,
                String.class);
        return result.getBody();
    }

    /**
     * free url string RESTful method connect for GeoServer
     * @param _url
     * @param _type
     * @return
     */
    public String freeConnect(String _url, HttpMethod _type) {
        try {
            _url = URLDecoder.decode(_url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> request = new HttpEntity<String>(createHeaders(user, password));
        ResponseEntity<String> result = restTemplate.exchange(_url,
                _type,
                request,
                String.class);

        return result.getBody();
    }


    /**
     * RESTFul Security Connect
     * @param _username
     * @param _password
     * @return
     */
    private HttpHeaders createHeaders(String _username, String _password){
        String auth = _username + ":" + _password;
        byte[] encodeAuth = Base64.encodeBase64(auth.getBytes());
        String str = new String(encodeAuth);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + str);
        return headers;
    }




}
