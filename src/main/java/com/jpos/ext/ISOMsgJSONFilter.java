package com.jpos.ext;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.RawIncomingFilter;
import org.jpos.util.LogEvent;

import com.jpos.ext.ISOMsgJSON;

public class ISOMsgJSONFilter implements RawIncomingFilter, Configurable {
	
	private String[] fields;
	private String[] jsonNames;
	@Override
	public ISOMsg filter(ISOChannel channel, ISOMsg m, LogEvent evt)
			throws VetoException {
		return filter(channel, m, null, null, evt);
	}
	@Override
	public ISOMsg filter(ISOChannel channel, ISOMsg m, byte[] header,
			byte[] image, LogEvent evt) throws VetoException {
		((ISOMsgJSON) m).setTranslations(fields,jsonNames);
		return m;
	}

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		fields = cfg.getAll("fieldNumber");
		jsonNames = cfg.getAll("JSONName");
		if (fields.length != jsonNames.length){
			throw new ConfigurationException("Mismatched number of fields and jsonNames");
		}
	}

}
