package com.jpos.ext;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.jpos.core.Configuration;
import org.jpos.iso.ISOMsg;


public class ISOMsgAdapter extends ISOMsg {

	protected Configuration cfg;
	public ISOMsgAdapter() {super();}
	public ISOMsgAdapter(int fldno) {super(fldno);}
	
	public void setConfiguration(Configuration cfg) {
		this.cfg = cfg;
	}

	@Override
	public Object clone() {
		return cloneImpl((ISOMsg) super.clone());
	}

	@Override
	public Object clone(int[] cloneFieldArray) {
		return cloneImpl((ISOMsg) super.clone(cloneFieldArray));
	}
	
	protected ISOMsgAdapter cloneImpl(ISOMsg rawClone) {
		return (ISOMsgAdapter) rawClone;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		
		cfg = (Configuration) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeObject(cfg);
	}
}
