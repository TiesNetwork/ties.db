package com.tiesdb.protocol.v0.impl;

import static org.ebml.EBMLReader.parseEBMLCode;
import static org.ebml.EBMLReader.readEBMLCodeAsBytes;
import static org.ebml.EBMLReader.readEBMLCodeSize;

import java.nio.ByteBuffer;
import java.util.EmptyStackException;
import java.util.Objects;
import java.util.Stack;

import org.ebml.io.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Input;
import com.tiesdb.protocol.api.data.ElementContainer;
import com.tiesdb.protocol.api.data.ElementReader;
import com.tiesdb.protocol.api.data.ElementType;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.TiesElement;
import com.tiesdb.protocol.v0.exception.DataParsingException;
import com.tiesdb.protocol.v0.exception.PacketSegmentationException;
import com.tiesdb.protocol.v0.impl.ebml.TiesEBMLDataSource;

public class ElementReaderImpl extends ElementHelper implements ElementReader<TiesElement> {

	private static final Logger LOG = LoggerFactory.getLogger(ElementReaderImpl.class);

	private final TiesEBMLDataSource ds;
	private final Stack<StackElement> stack;
	private final ElementFactory eFactory;

	private TiesEBMLType nextType;
	private long nextSize;

	public ElementReaderImpl(Input input, ElementFactory eFactory) {
		this.eFactory = eFactory;
		this.ds = getDataSource(input);
		this.stack = new Stack<>();
	}

	protected TiesEBMLDataSource getDataSource(Input input) {
		return new TiesEBMLDataSource(input);
	}

	private void stackAdvance(long size, boolean recursive) {
		if (!stack.isEmpty()) {
			StackElement elm = stack.peek();
			elm.remainSize -= size;
			if (elm.remainSize == 0 && recursive) {
				stackAdvance(stack.pop().totalSize, recursive);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void stackPush(TiesElement element) {
		stack.push(new StackElement((ElementContainer<TiesElement>) element, nextSize));
	}

	private TiesEBMLType readHeader() {
		while (!ds.isFinished()) {
			long start = ds.getFilePointer();
			ByteBuffer typeCode = readEBMLCodeAsBytes(ds);
			if (typeCode == null) {
				throw new PacketSegmentationException("EBML ID read failed");
			}
			TiesEBMLType type = getForCode(typeCode);
			long size = readEBMLCodeAlt(ds);
			if (size == -1) {
				throw new PacketSegmentationException("EBML size read failed");
			}
			stackAdvance(ds.getFilePointer() - start, false);

			// TODO Auto skip to be configurable
			/* Auto skip */
			if (type == null) {
				ds.skip(size);
				stackAdvance(size, true);
				LOG.warn("Unknown EBML ID {}", Integer.toHexString(typeCode.asIntBuffer().get()));
				continue;
			}

			this.nextType = type;
			this.nextSize = size;

			break;
		}
		return nextType;
	}

	private static long readEBMLCodeAlt(final DataSource source) {
		// Begin loop with byte set to newly read byte.
		final byte firstByte = source.readByte();
		final int numBytes = readEBMLCodeSize(firstByte);
		if (numBytes == 0) {
			// Invalid size
			return -1;
		}

		// Setup space to store the bits
		final ByteBuffer data = ByteBuffer.allocate(numBytes);

		// Clear the 1 at the front of this byte, all the way to the beginning of the
		// size
		data.put((byte) (firstByte & ((0xFF >>> (numBytes)))));

		if (numBytes > 1) {
			// Read the rest of the size.
			source.read(data);
		}
		data.flip();
		return parseEBMLCode(data);
	}

	private void readValue(TiesElementValue<?> element, long size) {
		org.ebml.Element elm = element.getType().getProtoType().getInstance();
		elm.setSize(size);
		elm.readData(ds);
		stackAdvance(size, true);
		element.setFromElementValue(asExtended(elm));
	}

	private void reset() {
		nextSize = 0;
		nextType = null;
	}

	@Override
	public boolean hasNext() {
		return null != (nextType == null && !ds.isFinished() ? (nextType = readHeader()) : nextType);
	}

	@Override
	public ElementType nextType() {
		return hasNext() ? nextType : null;
	}

	@Override
	public long nextSize() {
		return hasNext() ? nextSize : -1;
	}

	@Override
	public void skipNext() {
		stackAdvance(ds.skip(nextSize), true);
		reset();
	}

	@Override
	public TiesElement readNext() {
		if (!hasNext()) {
			return null;
		}
		try {
			TiesElement element = eFactory.getElement(nextType);
			if (!stack.isEmpty()) {
				stack.peek().element.accept(element);
			}
			if (element instanceof ElementContainer) {
				stackPush(element);
			} else if (element instanceof TiesElementValue) {
				readValue((TiesElementValue<?>) element, nextSize);
			} else {
				throw new DataParsingException(new TiesDBProtocolException("Unknown element type to be read"));
			}
			reset();
			return element;
		} catch (TiesDBProtocolException e) {
			throw new DataParsingException(e);
		}
	}

	@Override
	public int stackSize() {
		return stack.size();
	}

	@Override
	public int stackSearch(ElementContainer<TiesElement> ec) {
		return stack.search(new StackElement(ec, 0));
	}

	@Override
	public ElementContainer<TiesElement> stackPeek() {
		return stack.isEmpty() ? null : stack.peek().element;
	}

	@Override
	public ElementContainer<TiesElement> stackGet(int fromHead) throws ArrayIndexOutOfBoundsException {
		return stack.get(stack.size() - fromHead).element;
	}

	@Override
	public ElementContainer<TiesElement> stackSkip(int fromHead) throws EmptyStackException {
		skipNext();
		while (!stack.isEmpty() && fromHead-- > 0) {
			stackAdvance(ds.skip(stack.peek().remainSize), true);
		}
		return stack.isEmpty() ? null : stack.peek().element;
	}

	private static class StackElement {

		private final ElementContainer<TiesElement> element;
		private final long totalSize;
		private long remainSize;

		public StackElement(ElementContainer<TiesElement> element, long size) {
			Objects.requireNonNull(element);
			this.element = element;
			this.remainSize = this.totalSize = size;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((element == null) ? 0 : element.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StackElement other = (StackElement) obj;
			if (element == null) {
				if (other.element != null)
					return false;
			} else if (!element.equals(other.element))
				return false;
			return true;
		}
	}
}
