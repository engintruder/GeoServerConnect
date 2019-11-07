package com.intruder.geoserver.api;


import com.intruder.geoserver.service.GeoServerConnectService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/GeoServer")
public class GeoServerRESTFulController {


    @Autowired
    GeoServerConnectService service;

    @RequestMapping(value="/", method = {RequestMethod.GET}, produces="text/json;charset=UTF-8")
    public String getLayers() {
        return service.getLayers();

    }
    @RequestMapping(value="/{ws}/{name}", method = {RequestMethod.DELETE}, produces="text/json;charset=UTF-8")
    public String removeLayer(@PathVariable String ws, @PathVariable String name) {
        return service.removeLayer(ws, name);

    }

    @RequestMapping(value="/url/{type}", method={RequestMethod.GET})
    public String getVisualizationServiceURL(@PathVariable String type){
        String result = "{ \"result\" : " + "\"" + service.makeURL(type) +"\"}";
        return result;
    }

    @RequestMapping(value="/feature/{ws}/{name}", method={RequestMethod.GET}, produces="text/json;charset=UTF-8")
    public String getGeoJSON(@PathVariable String ws, @PathVariable String name) {
        JSONObject result=service.getFeautre(ws, name);
        result.put("workspace", ws);
        result.put("name", name);
        return result.toString();
    }

}
