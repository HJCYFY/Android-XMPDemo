package com.arcsoft.xmp_demo;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.options.PropertyOptions;

import java.util.ArrayList;

public class XmpBuilder {

    private String NAMESPACE_URI;
    private String NAMESPACE_PREFIX;

    private ArrayList<String> propertyName = new ArrayList<>();
    private ArrayList<String> propertyValue = new ArrayList<>();

    public void setNamespace(String uri,String prefix) {
        NAMESPACE_URI = uri;
        NAMESPACE_PREFIX = prefix;
    }

    public void addMetaData(String name,String value) {
        if(propertyName.contains(name)){
            propertyValue.set(propertyName.indexOf(name),value);
        }else {
            propertyName.add(name);
            propertyValue.add(value);
        }
    }

    public XMPMeta build() throws XMPException {

        if(NAMESPACE_URI == null || NAMESPACE_PREFIX == null) {
            return null;
        }

        XMPMeta xmpMeta = XMPMetaFactory.create();
        XMPMetaFactory.getSchemaRegistry().registerNamespace(NAMESPACE_URI, NAMESPACE_PREFIX);

        for(int i=0; i<propertyName.size();++i) {
            setProperty(xmpMeta,propertyName.get(i),propertyValue.get(i));
        }

        return xmpMeta;
    }


    private void setProperty(XMPMeta xmpMeta, String name, String value) throws XMPException {
        PropertyOptions propertyOptions = new PropertyOptions();
        propertyOptions.setHasQualifiers(true);
        xmpMeta.setProperty(NAMESPACE_URI, name, value, propertyOptions);
    }


    String prettyPrint(String prefix) {
        StringBuilder sb = new StringBuilder();

        for(int i=0;i<propertyName.size();++i) {
            sb.append(prefix)
                    .append(propertyName.get(i))
                    .append(": ")
                    .append(propertyValue.get(i))
                    .append('\n');
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        String ret = new String();
        for(int i=0;i<propertyName.size();++i) {
            ret += propertyName.get(i) + " = " + propertyValue.get(i)+"\n";
        }
        return ret;
    }
}
