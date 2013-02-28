package com.jpos.ext;


import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;

public class ISORequestListenerImpl implements ISORequestListener, Configurable {

	private ISOMsgContextDecorator contextDecorator;
	@Override
	public boolean process(ISOSource source, ISOMsg msg) {		
		return contextDecorator.process(source, msg);
	}

	@Override
	public void setConfiguration(Configuration cfg)
			throws ConfigurationException {
		contextDecorator = new ISOMsgContextDecorator();
		contextDecorator.setConfiguration(cfg);
	}
}
