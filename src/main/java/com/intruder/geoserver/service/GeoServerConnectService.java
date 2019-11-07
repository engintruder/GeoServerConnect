package com.intruder.geoserver.service;


import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
public class GeoServerConnectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoServerConnectService.class);

    @Autowired
    GeoServerConnetDAO dao;

    /**
     * get URL for OGC standard
     * @param type
     * @return
     */
    public String makeURL(String type){
        if (type.equals("ROOT")){
            return dao.getGeoServerURL();
        }
        if (type.equals("WMTS")){
            return dao.getGeoServerURL() + "gwc/service/wmts";
        }
/*
        if (type.equals("WFS")){
            return dao.getGeoServerURL() + "/wfs?service=WFS&version=1.1.0&request=GetFeature&" +
                    "outputFormat=json&srsname=EPSG:3857&typeNames=";
        }
*/
        return null;
    }



    /**
     * Get All layers (divided RASTER | VECTOR)
     * @return
     */
    public String getLayers() {
        JSONArray result = new JSONArray();
        JSONArray ws = dao.workspaceOrLayers("layers", HttpMethod.GET);


        ws.forEach(content -> {
            JSONObject obj = (JSONObject) content;
            String url=obj.getString("href");
            String item = dao.freeConnect(url, HttpMethod.GET);

            JSONObject itemObj = new JSONObject(item).getJSONObject("layer");
            JSONObject tmp = new JSONObject();

            tmp.put("name", itemObj.getString("name"));
            tmp.put("type", itemObj.getString("type"));

            String workspace = itemObj.getJSONObject("resource").getString("name");
            workspace = workspace.split(":")[0];
            tmp.put("workspace", workspace);

            String detailHref=itemObj.getJSONObject("resource").getString("href");
            item = dao.freeConnect(detailHref, HttpMethod.GET);
            String rootStr = "";
            if (tmp.getString("type").equals("VECTOR")){
                rootStr = "featureType";
            } else if (tmp.getString("type").equals("RASTER")){
                rootStr = "coverage";
            }
            itemObj = new JSONObject(item).getJSONObject(rootStr);
            tmp.put("BBox", itemObj.get("latLonBoundingBox"));
            result.put(tmp);
        });
        return result.toString();
    }



    /**
     * remove layer
     * @param ws
     * @param name
     * @return
     */
    public String removeLayer(String ws,String name) {
        dao.deleteLayer(ws, name);
        return "{}";
    }


    public JSONObject getFeautre(String _workspace, String _name){
        String url = dao.getGeoServerURL() + "/wfs?service=WFS&version=1.1.0&request=GetFeature&" +
                "outputFormat=json&srsname=EPSG:3857&typeNames=";
        JSONArray ws = dao.workspaceOrLayers("layers", HttpMethod.GET);
        String encoding = null;
        try {
            encoding = URLEncoder.encode(_name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String names = _workspace + ":" + encoding;
        JSONObject o= null;

        for(int i=0; i<ws.length(); i++) {
            JSONObject obj = ws.getJSONObject(i);
            String n = obj.getString("name");
            if (n.equals(names)) {
                String href = obj.getString("href");
                String[] arr = href.split("/");
                String endPoint = arr[arr.length - 1].replace(".json", "");
                url = url + endPoint;
                break;
            }
        }
        return dao.getFeautre(url);
    }

}
