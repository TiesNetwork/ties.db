package com.tiesdb.protocol.v0;

import static com.tiesdb.protocol.api.data.Version.VersionComprator.MINOR;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.api.TiesDBProtocol;
import com.tiesdb.protocol.api.TiesDBProtocolHandler;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Input;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Output;
import com.tiesdb.protocol.api.data.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.TiesConversationHandler;
import com.tiesdb.protocol.v0.impl.ElementFactory;
import com.tiesdb.protocol.v0.impl.ElementReaderImpl;
import com.tiesdb.protocol.v0.impl.ElementWriterImpl;
import com.tiesdb.protocol.v0.impl.ProtocolHelper;

public class TiesDBProtocolV0 implements TiesDBProtocol {

	private static final Logger LOG = LoggerFactory.getLogger(TiesDBProtocolV0.class);

	private static final Version VERSION = new Version(0, 0, 1);

	private final ElementFactory elementFactory;
	private final ProtocolHelper protocolHelper;

	public TiesDBProtocolV0() {
		this(null, null);
	}

	public TiesDBProtocolV0(ElementFactory elementFactory, ProtocolHelper protocolHelper) {
		this.elementFactory = elementFactory != null ? elementFactory : getDefaultElementFactory();
		this.protocolHelper = protocolHelper != null ? protocolHelper : getDefaultProtocolHelper();
	}

	@Override
	public Version getVersion() {
		return VERSION;
	}

	@Override
	public boolean createChannel(TiesDBProtocolPacketChannel packetChannel, TiesDBProtocolHandler handler) throws TiesDBProtocolException {
		Objects.requireNonNull(packetChannel);
		Objects.requireNonNull(handler);
		((TiesConversationHandler) handler).handle(createConversation(this, packetChannel, VERSION));
		return true;
	}

	@Override
	public boolean acceptChannel(TiesDBProtocolPacketChannel packetChannel, TiesDBProtocolHandler handler) throws TiesDBProtocolException {
		Objects.requireNonNull(packetChannel);
		Objects.requireNonNull(handler);
		if (!TiesConversationHandler.class.isInstance(handler)) {
			throw new IllegalArgumentException(
					"TiesConversationHandler of " + handler.getClass() + " should implement an " + TiesConversationHandler.class);
		}
		Version version = null;
		Input input = null;
		try {
			input = packetChannel.getInput();
			input.peekStart();
			version = protocolHelper.parsePacketHeader(input);
			if (!checkVersion(version)) {
				return false;
			}
			input.peekSkip();
		} finally {
			try {
				if (input != null && input.isPeeking()) {
					input.peekRewind();
				}
			} catch (Throwable th) {
				LOG.error("Can't rewind input.", th);
			}
		}
		((TiesConversationHandler) handler).handle(createConversation(this, packetChannel, version));
		return true;
	}

	protected TiesDBConversationV0 createConversation(TiesDBProtocolV0 protocol, TiesDBProtocolPacketChannel packetChannel, Version version)
			throws TiesDBProtocolException {
		return new TiesDBConversationV0(protocol, packetChannel, version);
	}

	protected boolean checkVersion(Version version) throws TiesDBProtocolException {
		return MINOR.compare(VERSION, version) == 0;
	}

	/* CONVERSATION ATTRIBUTES */

	protected ElementFactory getDefaultElementFactory() {
		LOG.debug("Using default ElementFactory");
		return ElementFactory.Default.INSTANCE;
	}

	protected ProtocolHelper getDefaultProtocolHelper() {
		LOG.debug("Using default ProtocolHelper");
		return ProtocolHelper.Default.INSTANCE;
	}

	protected Output writeHeader(Output output) {
		protocolHelper.writePacketHeader(VERSION, output);
		return output;
	}

	public ElementReaderImpl createReader(TiesDBProtocolPacketChannel packetChannel) {
		return new ElementReaderImpl(packetChannel.getInput(), elementFactory);
	}

	public ElementWriterImpl createWriter(TiesDBProtocolPacketChannel packetChannel) {
		return new ElementWriterImpl(writeHeader(packetChannel.getOutput()));
	}

	/* END CONVERSATION ATTRIBUTES */
}
