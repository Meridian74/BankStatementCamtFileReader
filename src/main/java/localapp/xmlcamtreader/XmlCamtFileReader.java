package localapp.xmlcamtreader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import localapp.config.ObjectMapperConfig;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XmlCamtFileReader extends AbstractXmlCamFileReader {
	// Ezen az útvonalon van a Statement elementben a bankszámla száma
	private static final String ACCOUNT_NUMBER_XML_VALUE = "camt:Acct/camt:Id/camt:Othr/camt:Id/text()";
	
	// Balance element a Statement elemen belül
	private static final String BALANCE_XML_ELEMENT = "camt:Bal";
	
	// Balance típusa (pl nyitó, záregyenleg, vagy info/zárolás)
	private static final String BALANCE_TYPE_VALUE = "camt:Tp/camt:CdOrPrtry/camt:Cd/text()";
	
	// Balance elementben tárolt összeg értéke
	private static final String BALANCE_AMOUNT_VALUE = "camt:Amt/text()";
	
	// Bankszámlaszámhoz tartozó Statement dátum értéke
	private static final String STATEMENT_DATE_VALUE = "camt:CreDtTm/text()";
	
	// **********************************************************
	// ***** bankszámla kivonatban az entry elemek path-jai *****
	
	// entry XMl node-ok neve
	private static final String ENTRY_XML_ELEMENT = "camt:Ntry";
	
	// entry sorszám - referencia szám
	private static final String ENTRY_BANK_TRANSACTION_CODE_VALUE = "camt:BkTxCd/camt:Prtry/camt:Cd/text()";
	
	// átutalás típusa, neve
	private static final String ENTRY_BANK_TRANSACTION_NAME_VALUE = "camt:NtryDtls/camt:TxDtls/camt:AddtlTxInf/text()";
	
	// tranzakció összegének értéke
	private static final String ENTRY_AMOUNT_VALUE = "camt:Amt/text()";
	
	// tranzakció indikátor: DBIT, CRDT
	private static final String ENTRY_CREDIT_DEBIT_INDICATOR_VALUE = "camt:CdtDbtInd/text()";
	
	// tranzakció státusza: BOOK
	private static final String ENTRY_STATUS_VALUE = "camt:Sts/text()";
	
	// tranzakció könyvelési dátuma
	private static final String ENTRY_BOOKING_DATE_VALUE = "camt:BookgDt/camt:Dt/text()";
	
	// tranzakció érték dátuma
	private static final String ENTRY_VALUE_DATE_VALUE = "camt:ValDt/camt:Dt/text()";
	
	// tranzakció azonosító száma
	private static final String ENTRY_END_TO_END_ID_VALUE = "camt:NtryDtls/camt:TxDtls/camt:Refs/camt:EndToEndId/text()";
	private static final String ENTRY_ACCOUNT_SERVICER_REF_VALUE = "camt:AcctSvcrRef/text()";
	
	// tranzakciót elkövető neve
	private static final String ENTRY_RELATED_PARTIES_VALUE = "camt:NtryDtls/camt:TxDtls/camt:RltdPties/camt:Dbtr/camt:Nm/text()";
	
	// ahonnan érkezik az utalás - bankszámlaszém
	private static final String ENTRY_DEBTOR_ACCOUNT_VALUE = "camt:NtryDtls/camt:TxDtls/camt:RltdPties/camt:DbtrAcct/camt:Id/camt:Othr/camt:Id/text()";
	
	// megjegyzés
	private static final String ENTRY_NOTICE_VALUE = "camt:NtryDtls/camt:TxDtls/camt:RmtInf/camt:Ustrd/text()";
	
	
	
	public XmlCamtFileReader(File filename) throws RuntimeException {
		super(filename);
	}
	
	
	public List<String> getAccountNumbersFromFile() throws XPathExpressionException {
		List<String> accountNumbers = new ArrayList<>();
		
		XPath xpath = super.getXpath();
		NodeList statements = super.getStatements();
		
		for (int i = 0; i < statements.getLength(); i++) {
			Node statementNode = statements.item(i);
			String numberOfAccount = (String) xpath.evaluate(ACCOUNT_NUMBER_XML_VALUE,
					statementNode, XPathConstants.STRING);
			accountNumbers.add(numberOfAccount);
		}
		return accountNumbers;
	}
	
	public AccountBalance getBalancesByAccountNumber(String selectedAccountNumber) throws XPathExpressionException {
		AccountBalance accountBalance = null;
		XPath xpath = super.getXpath();
		NodeList statements = super.getStatements();
		
		// Végignézzük a Statement node-okat, keressük a paraméterként kapott számlaszámot
		for (int index = 0; index < statements.getLength(); index++) {
			Node statementNode = statements.item(index);
			String numberOfAccount = (String) xpath.evaluate(ACCOUNT_NUMBER_XML_VALUE,
					statementNode, XPathConstants.STRING);
			numberOfAccount = numberOfAccount.replace("-", "");
			
			// ha megvan a számlaszám amit keresünk, akkor kiolvassuk és beállítjuk a hozzátartozó egyenlegeket
			if (selectedAccountNumber.startsWith(numberOfAccount)) {
				accountBalance = createAccountBalance(xpath, statementNode);
				// elmentjük azt is, hogy melyik indexű 'Stmt' node volt - későbbi kiválasztás gyorsítsához
				accountBalance.setStatementIndex(index);
				break;
			}
		}
		
		return accountBalance;
	}
	
	public String getStatementID(int currentStatementIndex) throws XPathExpressionException {
		XPath xpath = super.getXpath();
		Node statementNode = super.getStatements().item(currentStatementIndex);
		return ((String) xpath.evaluate(ACCOUNT_NUMBER_XML_VALUE, statementNode, XPathConstants.STRING));
	}
	
	public String getStatementDate(int currentStatementIndex) throws XPathExpressionException {
		XPath xpath = super.getXpath();
		Node statementNode = super.getStatements().item(currentStatementIndex);
		String date = ((String) xpath.evaluate(STATEMENT_DATE_VALUE, statementNode, XPathConstants.STRING));
		date = date.replace("-", "")
				.trim()
				.substring(0,8);
		
		return date;
	}
	
	public List<TransactionEntry> getAllTransactionEntry(int currentStatementIndex) throws XPathExpressionException, JsonProcessingException {
		ObjectMapper om = ObjectMapperConfig.getObjectMapper();
		List<TransactionEntry> entries = new ArrayList<>();
		
		XPath xpath = super.getXpath();
		Node statementNode = super.getStatements().item(currentStatementIndex);
		
		// Entry-k listája a Statement (Stmt) node-on belül
		XPathExpression expr = xpath.compile(ENTRY_XML_ELEMENT);
		NodeList entryList = (NodeList) expr.evaluate(statementNode, XPathConstants.NODESET);
		for (int index = 0; index < entryList.getLength(); index++) {
			Node currentInnerNode = entryList.item(index);
			TransactionEntry.Builder builder = new TransactionEntry.Builder(currentInnerNode, xpath);
			TransactionEntry currentEntry = builder
					.trID(ENTRY_BANK_TRANSACTION_CODE_VALUE)	// az egyenleg típusa
					.trName(ENTRY_BANK_TRANSACTION_NAME_VALUE)	// tranzakció neve (pl 'Azonnali Utalás'
					.amount(ENTRY_AMOUNT_VALUE)					// az tranzakció összege
					.creditDebitIndicator(ENTRY_CREDIT_DEBIT_INDICATOR_VALUE)	// tranzakció jelző (típus?)
					.isCredit()									// CRDT akkor credit (+), egyébként debit(-)
					.status(ENTRY_STATUS_VALUE)					// tranzakció státusza - pl 'BOOK'
					.bookingDate(ENTRY_BOOKING_DATE_VALUE)		// tranzakció könyvelési dátuma
					.ertNap(ENTRY_VALUE_DATE_VALUE)				// tranzakció érték szerinti(?) dátuma
					.accountServicerReference(ENTRY_ACCOUNT_SERVICER_REF_VALUE) 	// banki tranzakció azonosító számának helye ...
					.transactionEndToEndIdentification(ENTRY_END_TO_END_ID_VALUE)	// ... de sok esetben nincs (pl OTP! -> 'NOTPROVIDED')
					.ref()										// tranzakció számának beállítása
					.upName(ENTRY_RELATED_PARTIES_VALUE)		// az összeget utaló neve
					.upSzla(ENTRY_DEBTOR_ACCOUNT_VALUE)			// az összeget utalójának a bankszámlaszáma
					.notice(ENTRY_NOTICE_VALUE)					// tranzakció közleménye
					.build();
			
			entries.add(currentEntry);
			System.out.println("Srsz: " + index + ". DATA: " + om.writeValueAsString(currentEntry).substring(0,128));
		}
		
		return entries;
	}
	
	private AccountBalance createAccountBalance(XPath xpath, Node statementNode) throws XPathExpressionException {
		AccountBalance accountBalance = new AccountBalance();
		
		// Balance node-ok (lista) kiválasztása a statement node-on belül
		XPathExpression expr = xpath.compile(BALANCE_XML_ELEMENT);
		NodeList nodeList = (NodeList) expr.evaluate(statementNode, XPathConstants.NODESET);
		
		// végigmegyünk a 'Balance' node-okon és lekérdezzük a számunkra érdekes belső node-ok értékeit
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentInnerNode = nodeList.item(i);

			// az egyenleg típusa
			String type = ((String) xpath.evaluate(BALANCE_TYPE_VALUE, currentInnerNode, XPathConstants.STRING))
					.toUpperCase();
		
			// egyenleg számszerű összege
			Double amount = Double.valueOf(
					((String) xpath.evaluate(BALANCE_AMOUNT_VALUE, currentInnerNode, XPathConstants.STRING))
							.trim()
			);
			
			// beállítjuk a AccountBalance értékeit a talált típusoknat mint kulcsokat használva a Map-ban
			switch (type.toUpperCase()) {
				case "OPBD" -> accountBalance.addBalance(AccountBalanceType.NYITO_EGYENLEG, amount);
				case "CLBD" -> accountBalance.addBalance(AccountBalanceType.ZARO_EGYENLEG, amount);
				case "INFO" -> accountBalance.addBalance(AccountBalanceType.ZAROLT_EGYENLEG, amount);
				default -> throw new IllegalArgumentException("Ismeretlen egyenlegtípus a bankkivonat file-ban: '"
														   + type + "'.");
			}
			
		}
		
		return accountBalance;
	}

}