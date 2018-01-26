package com.tiesdb.protocol.v0r0.api.message;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;

import com.tiesdb.protocol.v0r0.api.message.part.BlockchainAddress;

public class Cheque {

	private Long timestamp;
	private UUID range;
	private Long number;
	private BigInteger amount;
	private BlockchainAddress[] receiptNodes;

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public UUID getRange() {
		return range;
	}

	public void setRange(UUID range) {
		this.range = range;
	}

	public Long getNumber() {
		return number;
	}

	public void setNumber(Long number) {
		this.number = number;
	}

	public BigInteger getAmount() {
		return amount;
	}

	public void setAmount(BigInteger amount) {
		this.amount = amount;
	}

	public BlockchainAddress[] getReceiptNodes() {
		return receiptNodes;
	}

	public void setReceiptNodes(BlockchainAddress[] receiptNodes) {
		this.receiptNodes = receiptNodes;
	}

	@Override
	public String toString() {
		return "Cheque [timestamp=" + timestamp + ", range=" + range + ", number=" + number + ", amount=" + amount + ", receiptNodes="
				+ Arrays.toString(receiptNodes) + "]";
	}

}
