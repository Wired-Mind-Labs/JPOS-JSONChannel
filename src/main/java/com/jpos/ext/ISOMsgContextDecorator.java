package com.jpos.ext;

import java.io.Serializable;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.transaction.Context;

public class ISOMsgContextDecorator implements Configurable {

	private static final String SPACE_NAME_KEY = "destinationSpace";
	private static final String SPACE_QUEUE_KEY = "queueKey";
	private static final String CONFIGURATION_EX_MSG = SPACE_NAME_KEY + " and " + SPACE_QUEUE_KEY + " properties must be specified.";
	
	private Space<Serializable, Object> destinationSpace;
	private String destinationQueueKey;
	
	public boolean process(ISOSource source, ISOMsg msg) {
		Context msgContext = new Context();
		msgContext.put("source", source);
		msgContext.put("msg", msg, true);
		
		destinationSpace.out(destinationQueueKey, msgContext);
		
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setConfiguration(Configuration cfg)
			throws ConfigurationException {
		destinationSpace = SpaceFactory.getSpace(cfg.get(SPACE_NAME_KEY));
		destinationQueueKey = cfg.get(SPACE_QUEUE_KEY);
		if (destinationQueueKey == null || destinationSpace == null) {
			throw new ConfigurationException(CONFIGURATION_EX_MSG);
		}
	}
}
