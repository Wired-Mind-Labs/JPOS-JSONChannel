package com.jpos.ext;

import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jpos.ext.ISOMsgJSON;

public class ISORequestListenerImplForJSON extends ISORequestListenerImpl {

	private String txnArrayKey;
	private String alternateSingleKey;
	@Override
	public boolean process(ISOSource source, ISOMsg msg) {	
		
		if (msg instanceof ISOMsgJSON) {
			JSONObject json = ((ISOMsgJSON) msg).getJsonObj();
			if (StringUtils.isEmpty(txnArrayKey)) {
				super.process(source,msg);
			} else {
				try {
					JSONArray batchJSONTxns = getTxnArray(json, txnArrayKey);
					for(int i=0; i < batchJSONTxns.length(); i++) {
						JSONObject jsonTxn = batchJSONTxns.getJSONObject(i);
						String[] extraNames = JSONObject.getNames(json);
						for(String extraName : extraNames) {
							if (!txnArrayKey.startsWith(extraName)) {
								jsonTxn.put(extraName, json.get(extraName));
							}
						}
						ISOMsgJSON newMsg = (ISOMsgJSON) msg.clone();
						newMsg.setKeyPrefix(txnArrayKey);
						newMsg.merge(msg);
						newMsg.setJsonInformation(jsonTxn.toString());
						super.process(source, newMsg);
					}
				} catch (Exception e) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	@Override
	public void setConfiguration(Configuration cfg)
			throws ConfigurationException {
		txnArrayKey = cfg.get("txnArrayKey");
		alternateSingleKey = cfg.get("alternateKeyForSingle","map");
		super.setConfiguration(cfg);
	}
	
	private JSONArray getTxnArray(JSONObject json, String jsonArrayIndicator) {
		StringTokenizer st = new StringTokenizer(jsonArrayIndicator,".");
		JSONObject current = json;
		String currentToken = null;
		while(true) {
			currentToken = st.nextToken();
			if (st.hasMoreTokens()) {
				current = current.optJSONObject(currentToken);
			} else {
				break;
			}
		}
		JSONArray txnArray = current.optJSONArray(currentToken);
		if (txnArray == null) {
			txnArray = new JSONArray();
			JSONObject singleTxn = current.optJSONObject(currentToken);
			if (singleTxn == null) {
				singleTxn = current.optJSONObject(alternateSingleKey);
			}
			txnArray.put(singleTxn);
		}
		
		return txnArray;
	}
}
