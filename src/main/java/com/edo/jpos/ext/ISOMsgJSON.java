package com.edo.jpos.ext;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintStream;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.json.JSONException;
import org.json.JSONObject;

import com.edo.jpos.ext.ISOMsgAdapter;


public class ISOMsgJSON extends ISOMsgAdapter {
	
	private static final String KEY_NOT_FOUND = "NOTFOUND";
	private DateTimeFormatter incomingFormat = ISODateTimeFormat.basicDateTime().withZone(DateTimeZone.UTC);
	private String jsonInformation;
	private String[] fieldNumbers;
	private String[] jsonKeys;
	private String txnKeyPrefix = "";
	private transient JSONObject json;
	
	public ISOMsgJSON() {
		super();
	}
	
	public ISOMsgJSON(String json) throws ISOException {
		super();
		try {
			this.setJsonInformation(json);
		} catch (JSONException e) {
			throw new ISOException("Cannot create ISOMsgJSON", e);
		}
	}

	public ISOMsgJSON(int fldno) {
		super(fldno);
		json = new JSONObject();
		jsonInformation = json.toString();
	}

	@Override
	public void set(ISOComponent c) throws ISOException {
		// TODO Auto-generated method stub
		//super.set(c);
		try {
			json.put(((ISOMsgJSON) c).getJSONObjNumber(), ((ISOMsgJSON) c).getJsonObj());
			jsonInformation = json.toString();
		} catch (JSONException e) {
			throw new ISOException("Cannot apply inner json " + ((ISOMsgJSON) c).getJSONObjNumber() + " - " + ((ISOMsgJSON) c).getJsonInformation(), e);
		}
	}

	@Override
	public void set(int fldno, String value) throws ISOException {
		String key = getKey(fldno);
		if (key == KEY_NOT_FOUND) {
			key = String.valueOf(fldno);
		}
		if (json != null) {
			try {
				json.put(key, value);
			} catch (JSONException e) {
				throw new ISOException("Cannot append value to key " + key, e);
			}
			jsonInformation = json.toString();
		}
	}

	@Override
	public void set(String fpath, String value) throws ISOException {
		StringTokenizer st = new StringTokenizer(fpath, ".");
		ISOMsgJSON m = this;
		while (true) {
			int fldno = Integer.parseInt(st.nextToken());
			if (st.hasMoreTokens()) {
				Object obj = m.getValue(fldno);
				if ((obj instanceof ISOMsg)) {
					m = (ISOMsgJSON) obj;
				} else {
					if (value == null) {
						break;
					}
					m.set(m = new ISOMsgJSON(fldno));
				}
			} else {
				m.set(fldno, value);
				break;
			}
		}
		jsonInformation = json.toString();
	}

	@Override
	public void set(String fpath, ISOComponent c) throws ISOException {
		// TODO Auto-generated method stub
		super.set(fpath, c);
	}

	@Override
	public void set(String fpath, byte[] value) throws ISOException {
		throw new ISOException("Operation not supported.");
	}

	@Override
	public void set(int fldno, byte[] value) throws ISOException {
		throw new ISOException("Operation not supported.");
	}

	@Override
	public void dump(PrintStream p, String indent) {
	    p.print(indent + "<" + "json" + ">");
	    p.println(this.jsonInformation);
	    p.println(indent + "</" + "json" + ">");
	}

	@Override
	public ISOComponent getComponent(String fpath) throws ISOException {
	    StringTokenizer st = new StringTokenizer(fpath, ".");
	    ISOMsgJSON m = this;
	    ISOComponent component = null;
	    while (true) {
	    	String key = st.nextToken();
	    	if (NumberUtils.isDigits(key)) {
	    		component = m.getComponent(Integer.parseInt(key));
	    	} else {
	    		if (!(m.getJsonObj().isNull(key))) {
	    			try {
						Object obj = m.getJsonObj().get(key);
						if (obj instanceof JSONObject) {
							component = new ISOMsgJSON();
							((ISOMsgJSON) component).setJsonInformation(obj.toString());
						} else {
				    		component = new org.jpos.iso.ISOField(1,String.valueOf(obj));
						}
					} catch (JSONException e) {
						throw new ISOException(e);
					}
				}
	    	}
	    	if (!st.hasMoreTokens()) break;
	    	if ((component instanceof ISOMsgJSON)) {
	    		m = (ISOMsgJSON)component;
	    	}
	    	else {
	    		throw new ISOException("Invalid path '" + fpath + "'");
	    	}
	    }

	    return component;
	}
	
	@Override
	public ISOComponent getComponent(int fldno) {
		ISOComponent component = null;
	    Object obj = null;
	    if (json.has(String.valueOf(fldno))) {
	    	try {
				obj = json.get(String.valueOf(fldno));
				if (obj instanceof JSONObject) {
					component = new ISOMsgJSON();
					((ISOMsgJSON) component).setJsonInformation(obj.toString());
				}else {
		    		component = new org.jpos.iso.ISOField(fldno,String.valueOf(obj));
				}
			} catch (JSONException e) {
				//ignore
			}
	    }
	    return component;
	}

	@Override
	public Object getValue(String fpath) throws ISOException {
	    StringTokenizer st = new StringTokenizer(fpath, ".");
	    ISOMsgJSON m = this;
	    Object obj;
	    while (true) {
	    	String key = st.nextToken();
	    	if (NumberUtils.isDigits(key)) {
	    		obj = m.getValue(Integer.parseInt(key));
	    	} else {
	    		obj = m.getComponent(key);
	    		if (obj instanceof ISOField) {
	    			obj = ((ISOField) obj).getValue();
	    		}
	    	}
	      if (!st.hasMoreTokens()) break;
	      if ((obj instanceof ISOMsgJSON)) {
	        m = (ISOMsgJSON)obj;
	      }
	      else {
	        throw new ISOException("Invalid path '" + fpath + "'");
	      }
	    }

	    return obj;
	  }

	@Override
	public String getString(int fldno) {
		String result = null;
		if (fldno == 7) {
    		DateTime txnDate = null;
    		try {
    			txnDate = incomingFormat.parseDateTime(StringUtils.defaultIfEmpty((String) getValue(getKey(fldno)),incomingFormat.print(new Date().getTime())));
    		} catch (ISOException e) {
				//ignore
			}
			return ISODate.getDateTime(txnDate.toDate(),TimeZone.getTimeZone("GMT"));
		}
		try {
			result = (String) getValue(getKey(fldno));
		} catch( ISOException e) {
			//ignore
		}
		return result;
	}

	@Override
	public Object getValue(int fldno)
		    throws ISOException
		  {
		    ISOComponent c = getComponent(fldno);
		    return c != null ? c.getValue() : getString(fldno);
		  }
	
	@Override
	public boolean hasField(int fldno) {
		return ArrayUtils.contains(fieldNumbers, String.valueOf(fldno));
	}

	@Override
	public boolean hasField(String fpath) throws ISOException {
	    StringTokenizer st = new StringTokenizer(fpath, ".");
	    ISOMsgJSON m = this;
	    while (true) {
	      int fldno = Integer.parseInt(st.nextToken());
	      if (st.hasMoreTokens()) {
	        Object obj = m.getValue(fldno);
	        if ((obj instanceof ISOMsgJSON)) {
	          m = (ISOMsgJSON)obj;
	        }
	        else
	        {
	          return false;
	        }
	      } else {
	        return m.hasField(fldno);
	      }
	    }
	  }

	@Override
	public boolean hasFields() {
		// TODO Auto-generated method stub
		return super.hasFields();
	}

	@Override
	public String getMTI() throws ISOException {
		return json.optString(getKey(0));
	}

	private String getKey(int i) {
		int index = ArrayUtils.indexOf(fieldNumbers, String.valueOf(i));
		
		return (index < 0) ? (json.has(String.valueOf(i))) ? String.valueOf(i) : KEY_NOT_FOUND : jsonKeys[index].replaceFirst(txnKeyPrefix, "");
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub
		super.writeExternal(out);
		out.writeObject(jsonInformation);
		out.writeObject(fieldNumbers);
		out.writeObject(jsonKeys);
		out.writeObject(txnKeyPrefix);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		super.readExternal(in);
		jsonInformation = (String) in.readObject();
		fieldNumbers = (String[]) in.readObject();
		jsonKeys = (String[]) in.readObject();
		txnKeyPrefix = (String) in.readObject();
		try {
			json = new JSONObject(jsonInformation);
		} catch (JSONException e) {
			throw new IOException("Cannot create ISOMsgJSON", e);
		}
	}

	public String getJsonInformation() {
		return jsonInformation;
	}

	public void setJsonInformation(String jsonInformation) throws JSONException {
		this.jsonInformation = jsonInformation;
		if (jsonInformation != null) {
			json = new JSONObject(jsonInformation);
		}
	}

	public void setTranslations(String[] fields, String[] jsonNames) {
		this.fieldNumbers = fields;
		this.jsonKeys = jsonNames;		
	}
	
	public JSONObject getJsonObj() {
		return this.json;
	}
	
	protected String getJSONObjNumber() {
		return String.valueOf(this.fieldNumber);
	}

	@Override
	public void merge(ISOMsg m) {
		if (m instanceof ISOMsgJSON) {
			this.setTranslations(((ISOMsgJSON) m).fieldNumbers, ((ISOMsgJSON) m).jsonKeys);
			this.jsonInformation = ((ISOMsgJSON) m).getJsonInformation();
			this.fieldNumber = ((ISOMsgJSON) m).fieldNumber;
			this.json = ((ISOMsgJSON) m).getJsonObj();
			if (this.cfg == null)
				this.cfg = ((ISOMsgJSON) m).cfg;
		}
	}
	
	public void setKeyPrefix(String prefix) {
		this.txnKeyPrefix = prefix;
	}

}
