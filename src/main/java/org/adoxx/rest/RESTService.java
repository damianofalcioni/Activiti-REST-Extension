package org.adoxx.rest;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLInputFactory;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;

import com.fasterxml.jackson.databind.ObjectMapper;


@Path("")
public class RESTService {
    
    @POST
    @Path("convertBPMN2ACTIVITI")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public String convertBPMN2ACTIVITI(String bpmnContent){
        try{
            BpmnModel bpmnModel = new BpmnXMLConverter()
                    .convertToBpmnModel(createSafeXmlInputFactory().createXMLStreamReader(
                            new InputStreamReader(new ByteArrayInputStream(bpmnContent.getBytes("UTF-8")), "UTF-8")));
    
            if (bpmnModel.getMainProcess() == null || bpmnModel.getMainProcess().getId() == null)
                throw new Exception("Incorrect BPMN model");
            if (bpmnModel.getLocationMap().isEmpty())
                throw new Exception("BPMN DI not present");
    
            String bpmnModelJson = new BpmnJsonConverter().convertToJson(bpmnModel).toString();
    
            return bpmnModelJson;
        }catch(Exception ex){
            ex.printStackTrace();
            return "{'error':'"+ex.getMessage().replace("\\", "\\\\").replace("\"", "\\\"")+"'}";
        }
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_XML)
    @Path("convertACTIVITI2BPMN")
    public String convertACTIVITI2BPMN(String activitiContent){
        try{
        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(new ObjectMapper().readTree(activitiContent));
        String bpmnModelXml = new String(new BpmnXMLConverter().convertToXML(bpmnModel), "UTF-8");
        return bpmnModelXml;
        }catch(Exception ex){
            ex.printStackTrace();
            return "<ERROR><![CDATA["+ex.getMessage()+"]]></ERROR>";
        }
    }

    private XMLInputFactory createSafeXmlInputFactory() {
        XMLInputFactory xif = XMLInputFactory.newInstance();
        if (xif.isPropertySupported(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES)) {
            xif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        }

        if (xif.isPropertySupported(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES)) {
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        }

        if (xif.isPropertySupported(XMLInputFactory.SUPPORT_DTD)) {
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        }
        return xif;
    }
}
