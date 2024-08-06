package hu.multialarm;

import hu.multialarm.xmlcamtreader.TransactionEntry;

import java.util.List;

public class Result {
	
	String account;
	String date;
	List<TransactionEntry> entries;
	
	public String getAccount() {
		return account;
	}
	
	public void setAccount(String account) {
		this.account = account;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public List<TransactionEntry> getEntries() {
		return entries;
	}
	
	public void setEntries(List<TransactionEntry> entries) {
		this.entries = entries;
	}
}
