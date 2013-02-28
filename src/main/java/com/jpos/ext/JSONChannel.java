package com.jpos.ext;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

public class JSONChannel extends BaseChannel {
	
	private BufferedReader reader;
	private Socket socket;
	
	public JSONChannel() {
		super();
	}
	
	public JSONChannel(String host, int port, ISOPackager p) {
		super(host,port,p);
	}

	public JSONChannel(ISOPackager p) throws IOException {
		super(p);
	}

	public JSONChannel(ISOPackager p, ServerSocket serverSocket) throws IOException {
		this();
		setPackager(p);
		setServerSocket(serverSocket);
	}
	
	@Override
	public ISOMsg receive() throws IOException, ISOException {
	    byte[] b = null;
	    byte[] header = null;
	    LogEvent evt = new LogEvent(this, "receive");
	    ISOMsg m = new ISOMsgJSON();

	    m.setSource(this);
	    try {
	      if (!isConnected()) {
	        throw new ISOException("unconnected ISOChannel");
	      }
	      synchronized (this.serverInLock) {
	        int len = getMessageLength();
	        int hLen = getHeaderLength();

	        if (len == -1) {
	          if (hLen > 0) {
	            header = readHeader(hLen);
	          }
	          b = streamReceive();
	        }
	        else if ((len > 0) && (len <= getMaxPacketLength())) {
	          if (hLen > 0)
	          {
	            header = readHeader(hLen);
	            len -= header.length;
	          }
	          b = new byte[len];
	          getMessage(b, 0, len);
	          getMessageTrailler();
	        }
	        else {
	          throw new ISOException("receive length " + len + " seems strange - maxPacketLength = " + getMaxPacketLength());
	        }
	      }
	      ((ISOMsgJSON) m).setJsonInformation(new String(b));
	      evt.addMessage(m);
	      m = applyIncomingFilters(m, header, b, evt);
	      notifyObservers(m);
	    } catch (ISOException e) {
	      evt.addMessage(e);
	      if (header != null) {
	        evt.addMessage("--- header ---");
	        evt.addMessage(ISOUtil.hexdump(header));
	      }
	      if (b != null) {
	        evt.addMessage("--- data ---");
	        evt.addMessage(ISOUtil.hexdump(b));
	      }
	      throw e;
	    } catch (EOFException e) {
	      closeSocket();
	      evt.addMessage("<peer-disconnect/>");
	      throw e;
	    } catch (SocketException e) {
	      closeSocket();
	      if (this.usable)
	        evt.addMessage("<peer-disconnect>" + e.getMessage() + "</peer-disconnect>");
	      throw e;
	    } catch (InterruptedIOException e) {
	      closeSocket();
	      evt.addMessage("<io-timeout/>");
	      throw e;
	    } catch (IOException e) {
	      closeSocket();
	      if (this.usable)
	        evt.addMessage(e);
	      throw e;
	    } catch (Exception e) {
	      //evt.addMessage(m);
	      evt.addMessage(e);
	      throw new ISOException("unexpected exception", e);
	    } finally {
	      Logger.log(evt);
	    }
	    return m;
	    }
	
	@Override
	  protected void connect(Socket socket) throws IOException {
	    super.connect(socket);
	    this.socket = socket;
	    this.reader = new BufferedReader(new InputStreamReader(this.serverIn));
	  }

    private void closeSocket() throws IOException {
        Socket s = null;
        synchronized (this) {
            if (socket != null) {
                s = socket; // we don't want more than one thread
                socket = null;     // attempting to close the socket
            }
        }
        if (s != null) {
            try {
                s.setSoLinger (true, 5);
                s.shutdownOutput();  // This will force a TCP FIN to be sent.
            } catch (SocketException e) {
                // safe to ignore - can be closed already
                // e.printStackTrace();
            }
            s.close ();
        }
    }

	@Override
	  public void disconnect() throws IOException {
	    super.disconnect();
	    if (this.reader != null)
	      this.reader.close();
	    this.reader = null;
	  }
	
	@Override
	protected byte[] streamReceive()  throws IOException {
	    int sp = 0;
	    StringBuffer sb = new StringBuffer();
	    while (this.reader != null) {
	      int s = this.reader.read();
	      if (s == -1)
	        throw new EOFException();
	      sb.append((char) s);
	      if (((char) s) == '{')
	        sp++;
	      if (((char) s) == '}')
	      {
	        sp--; if (sp <= 0)
	          break;
	      }
	    }
	    return sb.toString().getBytes();
	}
}
