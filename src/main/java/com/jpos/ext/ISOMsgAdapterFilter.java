package com.jpos.ext;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;

import org.jdom.Element;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.XmlConfigurable;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.util.LogEvent;

import com.jpos.ext.SerializableConfiguration;

public class ISOMsgAdapterFilter implements ISOFilter, XmlConfigurable {

	Configuration cfg;
	
	@Override
	public ISOMsg filter(ISOChannel channel, ISOMsg msg, LogEvent event)
			throws VetoException {
		ISOMsgAdapter msgAdapter = null;
		try {
			msgAdapter = (ISOMsgAdapter) Class.forName(cfg.get("adapter-class","com.edo.jpos.ISOMsgAdapter")).newInstance();
		} catch (InstantiationException e) {
			msgAdapter = new ISOMsgAdapter();
		} catch (IllegalAccessException e) {
			msgAdapter = new ISOMsgAdapter();
		} catch (ClassNotFoundException e) {
			msgAdapter = new ISOMsgAdapter();
		}
		msgAdapter.setConfiguration(cfg);
		msgAdapter.merge(msg);
		if (msg.getHeader() != null) {
			msgAdapter.setHeader(msg.getHeader());
		}
		return msgAdapter;
	}

	@Override
	public void setConfiguration(Element e)
			throws ConfigurationException {
		this.cfg=getConfiguration(e);
	}

	/** Copied from org.jpos.q2.QFactory **/
	public Configuration getConfiguration(Element e)
			throws ConfigurationException {
		Properties props = new Properties();
		Iterator iter = e.getChildren("property").iterator();
		while (iter.hasNext()) {
			Element property = (Element) iter.next();
			String name = property.getAttributeValue("name");
			String value = property.getAttributeValue("value");
			String file = property.getAttributeValue("file");
			if (file != null) {
				try {
					props.load(new FileInputStream(new File(file)));
				} catch (Exception ex) {
					throw new ConfigurationException(file, ex);
				}
			} else if ((name != null) && (value != null)) {
				Object obj = props.get(name);
				if ((obj instanceof String[])) {
					String[] mobj = (String[]) (String[]) obj;
					String[] m = new String[mobj.length + 1];
					System.arraycopy(mobj, 0, m, 0, mobj.length);
					m[mobj.length] = value;
					props.put(name, m);
				} else if ((obj instanceof String)) {
					String[] m = new String[2];
					m[0] = ((String) obj);
					m[1] = value;
					props.put(name, m);
				} else {
					props.put(name, value);
				}
			}
		}
		return new SerializableConfiguration(props);
	}
}
