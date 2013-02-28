package com.edo.jpos.ext;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Properties;

import org.jpos.core.SimpleConfiguration;

public class SerializableConfiguration extends SimpleConfiguration
		implements Externalizable {

	Properties props;
	
	public SerializableConfiguration() {this(new Properties());}
	
	public SerializableConfiguration(Properties props) {
		super(props);
		this.props = props;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(props);
		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		props.putAll((Properties) in.readObject());
	}

}
