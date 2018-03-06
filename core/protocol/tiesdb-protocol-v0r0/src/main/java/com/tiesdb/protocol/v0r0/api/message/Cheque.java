/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.protocol.v0r0.api.message;

public class Cheque {

	private ChequeData chequeData;
	private BlockchainAddress chequeSigner;
	private byte[] chequeRawBytes;

	public ChequeData getChequeData() {
		return chequeData;
	}

	public void setChequeData(ChequeData chequeData) {
		this.chequeData = chequeData;
	}

	public BlockchainAddress getChequeSigner() {
		return chequeSigner;
	}

	public void setChequeSigner(BlockchainAddress chequeSigner) {
		this.chequeSigner = chequeSigner;
	}

	public byte[] getChequeRawBytes() {
		return chequeRawBytes;
	}

	public void setChequeRawBytes(byte[] chequeRawBytes) {
		this.chequeRawBytes = chequeRawBytes;
	}

	@Override
	public String toString() {
		return "Cheque [chequeData=" + chequeData + ", chequeSigner=" + chequeSigner + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chequeData == null) ? 0 : chequeData.hashCode());
		result = prime * result + ((chequeSigner == null) ? 0 : chequeSigner.hashCode());
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
		Cheque other = (Cheque) obj;
		if (chequeData == null) {
			if (other.chequeData != null)
				return false;
		} else if (!chequeData.equals(other.chequeData))
			return false;
		if (chequeSigner == null) {
			if (other.chequeSigner != null)
				return false;
		} else if (!chequeSigner.equals(other.chequeSigner))
			return false;
		return true;
	}

}
