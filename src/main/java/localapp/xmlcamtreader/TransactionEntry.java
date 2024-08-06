package localapp.xmlcamtreader;

import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

public record TransactionEntry(String ertNap, String trID, String trName, String ref, Boolean isCredit,
							   String amount, String upName, String upSzla, String notice,
							   String creditDebitIndicator, String status, String bookingDate) {
	

	public static class Builder {
		private String ertNap;
		private String trID;
		private String trName;
		private String ref;
		private Boolean isCredit;
		private String amount;
		private String upName;
		private String upSzla;
		private String notice;
		private String creditDebitIndicator;
		private String status;
		private String bookingDate;
		private String accountServicerReference;
		private String transactionEndToEndIdentification;
		private final Node currentNode;
		private final XPath xpath;
		
		Builder(Node node, XPath xpath) {
			this.currentNode = node;
			this.xpath = xpath;
		}
		
		public Builder trID(String elementPath) throws XPathExpressionException {
			this.trID = ((String) this.xpath.evaluate(elementPath, this.currentNode, XPathConstants.STRING))
					.substring(0,3);
			return this;
		}
		
		public Builder trName(String elementPath) throws XPathExpressionException {
			this.trName = (String) this.xpath.evaluate(elementPath, this.currentNode, XPathConstants.STRING);
			return this;
		}
		
		public Builder amount(String elementPath) throws XPathExpressionException {
			this.amount = (String) this.xpath.evaluate(elementPath, this.currentNode, XPathConstants.STRING);
			return this;
		}
		
		public Builder creditDebitIndicator(String elementPath) throws XPathExpressionException {
			this.creditDebitIndicator = (String) this.xpath.evaluate(elementPath, this.currentNode, XPathConstants.STRING);
			return this;
		}
		
		public Builder isCredit() {
			if (creditDebitIndicator.equalsIgnoreCase("CRDT")) {
				this.isCredit = Boolean.TRUE;
			} else {
				this.isCredit = Boolean.FALSE;
			}
			return this;
		}
		
		public Builder status(String elementPath) throws XPathExpressionException {
			this.status = (String) this.xpath.evaluate(elementPath, this.currentNode, XPathConstants.STRING);
			return this;
		}
		
		public Builder bookingDate(String elementPath) throws XPathExpressionException {
			this.bookingDate = (String) this.xpath.evaluate(elementPath, this.currentNode, XPathConstants.STRING);
			return this;
		}
		
		public Builder ertNap(String elementPath) throws XPathExpressionException {
			this.ertNap = (String) this.xpath.evaluate(elementPath, this.currentNode, XPathConstants.STRING);
			return this;
		}
		
		public Builder transactionEndToEndIdentification(String elementPath) throws XPathExpressionException {
			this.transactionEndToEndIdentification = (String) this.xpath.evaluate(elementPath, this.currentNode, XPathConstants.STRING);
			return this;
		}
		
		public Builder accountServicerReference(String elementPath) throws XPathExpressionException {
			this.accountServicerReference = (String) this.xpath.evaluate(elementPath, this.currentNode, XPathConstants.STRING);
			return this;
		}
		
		// ezt még tisztázni kell majd a pénzügyessel - nem egyértelmű...!
		public Builder ref() {
			if (!accountServicerReference.isEmpty()) {
				this.ref = accountServicerReference;
			} else if (!transactionEndToEndIdentification.isEmpty()) {
				ref = transactionEndToEndIdentification;
			} else {
				ref = "tranz.azon.szám HIÁNYZIK!";
			}
			return this;
		}
		
		public Builder upName(String elementPath) throws XPathExpressionException {
			this.upName = (String) this.xpath.evaluate(elementPath, this.currentNode, XPathConstants.STRING);
			return this;
		}
		
		public Builder upSzla(String elementPath) throws XPathExpressionException {
			this.upSzla = (String) this.xpath.evaluate(elementPath, this.currentNode, XPathConstants.STRING);
			return this;
		}
		
		public Builder notice(String elementPath) throws XPathExpressionException {
			this.notice = (String) this.xpath.evaluate(elementPath, this.currentNode, XPathConstants.STRING);
			return this;
		}
		
		public TransactionEntry build() {
			return new TransactionEntry(ertNap, trID, trName, ref, isCredit, amount, upName, upSzla, notice,
					creditDebitIndicator, status, bookingDate);
		}
		
	}
	
}